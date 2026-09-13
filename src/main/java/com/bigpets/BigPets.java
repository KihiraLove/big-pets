package com.bigpets;

import com.google.inject.Provides;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.Model;
import net.runelite.api.NPC;
import net.runelite.api.Renderable;
import net.runelite.api.RuneLiteObject;
import net.runelite.api.Scene;
import net.runelite.api.TileObject;
import net.runelite.api.WorldView;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.WorldViewLoaded;
import net.runelite.api.events.WorldViewUnloaded;
import net.runelite.api.hooks.DrawCallbacks;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.callback.RenderCallback;
import net.runelite.client.callback.RenderCallbackManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.PluginMessage;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.gpu.GpuPlugin;

@PluginDescriptor(
	name = "Big Pets",
	description = "Makes pets as big (or as small) as you want. Requires a GPU renderer (GPU, 117 HD, GPU(Experimental), GPU (Legacy)",
	tags = {"big", "pets", "resize", "size", "follower"}
)
public class BigPets extends Plugin
{
	private static final int NORMAL_SIZE = 100;
	private static final int MODEL_SCALE = 128;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private PluginManager pluginManager;

	@Inject
	private RenderCallbackManager renderCallbackManager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private BigPetsConfig config;

	@Inject
	private EventBus eventBus;

	private volatile boolean running;
	private boolean needsPetScan;
	private PetDrawCallbacks petDrawCallbacks;
	private Plugin gpuPlugin;
	private boolean needsVisualRequest;
	private ExternalPetVisual externalPet;
	private final Set<NPC> pets = Collections.newSetFromMap(new IdentityHashMap<>());
	private final Map<NPC, RuneLiteObject> resizedPets = new IdentityHashMap<>();

	private final RenderCallback renderCallback = new RenderCallback()
	{
		@Override
		public boolean drawObject(Scene scene, TileObject object)
		{
			return !(object instanceof GameObject)
				|| !isHiddenPet(((GameObject) object).getRenderable());
		}
	};

	@Override
	protected void startUp()
	{
		gpuPlugin = pluginManager.getPlugins().stream()
			.filter(plugin -> plugin instanceof GpuPlugin)
			.findFirst().orElse(null);
		if (configManager.getConfiguration(BigPetsConfig.GROUP, "petSizePercentage") == null)
		{
			String previousSize = configManager.getConfiguration(BigPetsConfig.GROUP, "petSizePercentage");
			if (previousSize != null)
			{
				configManager.setConfiguration(BigPetsConfig.GROUP, "petSizePercentage", previousSize);
			}
		}
		needsPetScan = true;
		needsVisualRequest = true;
		running = true;
		renderCallbackManager.register(renderCallback);
	}

	@Override
	protected void shutDown()
	{
		running = false;
		renderCallbackManager.unregister(renderCallback);
		clientThread.invokeLater(() ->
		{
			restoreDrawCallbacks();
			clearPets();
			clearExternalPet();
			pets.clear();
			needsPetScan = true;
		});
	}

	@Subscribe
	public void onBeforeRender(BeforeRender event)
	{
		if (!running)
		{
			return;
		}

		NPC follower = client.getFollower();
		int percentage = Math.max(0, Math.min(500, config.petSizePercentage()));
		if (needsVisualRequest)
		{
			needsVisualRequest = false;
			eventBus.post(new PluginMessage("racecar", "request-pet-visual"));
		}
		updateExternalPet(percentage);
		if (client.getGameState() != GameState.LOGGED_IN
			|| !client.isGpu() || client.getDrawCallbacks() == null || percentage == NORMAL_SIZE)
		{
			restoreDrawCallbacks();
			clearPets();
			return;
		}
		updateDrawCallbacks();

		boolean allPets = config.resizeAllPets();
		if (allPets && needsPetScan)
		{
			scanPets(client.getTopLevelWorldView());
			needsPetScan = false;
		}

		Iterator<Map.Entry<NPC, RuneLiteObject>> iterator = resizedPets.entrySet().iterator();
		while (iterator.hasNext())
		{
			Map.Entry<NPC, RuneLiteObject> entry = iterator.next();
			if (entry.getKey() != follower && (!allPets || !pets.contains(entry.getKey())))
			{
				removeVisual(entry.getValue());
				iterator.remove();
			}
		}

		if (allPets)
		{
			for (NPC pet : pets)
			{
				resizePet(pet, percentage);
			}
		}
		if (follower != null && (!allPets || !pets.contains(follower)))
		{
			resizePet(follower, percentage);
		}
	}

	private boolean isHiddenPet(Renderable renderable)
	{
		return running && renderable instanceof NPC && resizedPets.containsKey(renderable);
	}

	private void updateDrawCallbacks()
	{
		DrawCallbacks current = client.getDrawCallbacks();
		if (current instanceof GpuPlugin || gpuPlugin != null && pluginManager.isPluginActive(gpuPlugin))
		{
			restoreDrawCallbacks();
			return;
		}
		if (petDrawCallbacks != null && petDrawCallbacks.isInstalled(current))
		{
			return;
		}
		restoreDrawCallbacks();
		petDrawCallbacks = new PetDrawCallbacks(client, current, this::isHiddenPet);
		client.setDrawCallbacks(petDrawCallbacks);
	}

	private void restoreDrawCallbacks()
	{
		if (petDrawCallbacks != null)
		{
			petDrawCallbacks.deactivate();
			if (client.getDrawCallbacks() == petDrawCallbacks)
			{
				client.setDrawCallbacks(petDrawCallbacks.getDelegate());
			}
			petDrawCallbacks = null;
		}
	}

	private void resizePet(NPC pet, int percentage)
	{
		if (externalPet != null && externalPet.npc == pet || isFiltered(pet))
		{
			removeVisual(resizedPets.remove(pet));
			return;
		}

		if (percentage == 0)
		{
			removeVisual(resizedPets.put(pet, null));
			return;
		}

		Model original = pet.getModel();
		if (original == null)
		{
			removeVisual(resizedPets.remove(pet));
			return;
		}

		Model scaled = client.mergeModels(new Model[]{original});
		if (scaled == null)
		{
			removeVisual(resizedPets.remove(pet));
			return;
		}
		int scale = Math.round(MODEL_SCALE * percentage / (float) NORMAL_SIZE);
		scaled.scale(scale, scale, scale);

		RuneLiteObject resizedPet = resizedPets.get(pet);
		if (resizedPet == null)
		{
			resizedPet = client.createRuneLiteObject();
		}
		resizedPet.setModel(scaled);
		resizedPet.setLocation(pet.getLocalLocation(), pet.getWorldView().getPlane());
		resizedPet.setOrientation(pet.getCurrentOrientation());
		if (!resizedPet.isActive())
		{
			resizedPet.setActive(true);
		}
		resizedPets.put(pet, resizedPet);
	}

	@Subscribe
	public void onPluginMessage(PluginMessage event)
	{
		if (!running || !"racecar".equals(event.getNamespace()) || !"pet-visual".equals(event.getName()))
		{
			return;
		}
		Object npc = event.getData().get("npc");
		Object setSize = event.getData().get("setSize");
		Object isActive = event.getData().get("isActive");
		if (!(npc instanceof NPC) || !(setSize instanceof IntConsumer) || !(isActive instanceof BooleanSupplier))
		{
			return;
		}
		clientThread.invoke(() ->
		{
			if (!running || npc != client.getFollower() || !((BooleanSupplier) isActive).getAsBoolean())
			{
				return;
			}
			clearExternalPet();
			externalPet = new ExternalPetVisual((NPC) npc, (IntConsumer) setSize, (BooleanSupplier) isActive);
			removeVisual(resizedPets.remove(npc));
			updateExternalPet(Math.max(0, Math.min(500, config.petSizePercentage())));
		});
	}

	private boolean isFiltered(NPC pet)
	{
		return config.filterCatsAndDogs() && PetFilters.isCatOrDog(pet)
			|| config.filterQuestAndEventPets() && PetFilters.isQuestOrEventPet(pet);
	}

	private void updateExternalPet(int percentage)
	{
		if (externalPet == null)
		{
			return;
		}
		if (externalPet.npc != client.getFollower() || !externalPet.isActive.getAsBoolean())
		{
			clearExternalPet();
			return;
		}
		boolean resize = client.getGameState() == GameState.LOGGED_IN && client.isGpu()
			&& client.getDrawCallbacks() != null && !isFiltered(externalPet.npc);
		externalPet.setSize.accept(resize ? percentage : NORMAL_SIZE);
	}

	private void clearExternalPet()
	{
		if (externalPet != null)
		{
			externalPet.setSize.accept(NORMAL_SIZE);
			externalPet = null;
		}
	}

	private static final class ExternalPetVisual
	{
		private final NPC npc;
		private final IntConsumer setSize;
		private final BooleanSupplier isActive;

		private ExternalPetVisual(NPC npc, IntConsumer setSize, BooleanSupplier isActive)
		{
			this.npc = npc;
			this.setSize = setSize;
			this.isActive = isActive;
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		trackPet(event.getNpc());
	}

	@Subscribe
	public void onNpcChanged(NpcChanged event)
	{
		trackPet(event.getNpc());
	}

	private void trackPet(NPC npc)
	{
		if (PetNpcs.isPet(npc))
		{
			pets.add(npc);
		}
		else
		{
			pets.remove(npc);
			removeVisual(resizedPets.remove(npc));
		}
	}

	private void scanPets(WorldView worldView)
	{
		if (worldView == null)
		{
			return;
		}
		for (NPC npc : worldView.npcs())
		{
			trackPet(npc);
		}
		for (WorldView child : worldView.worldViews())
		{
			scanPets(child);
		}
	}

	@Subscribe
	public void onWorldViewLoaded(WorldViewLoaded event)
	{
		scanPets(event.getWorldView());
	}

	@Subscribe
	public void onWorldViewUnloaded(WorldViewUnloaded event)
	{
		pets.removeIf(npc -> npc.getWorldView() == event.getWorldView());
		Iterator<Map.Entry<NPC, RuneLiteObject>> iterator = resizedPets.entrySet().iterator();
		while (iterator.hasNext())
		{
			Map.Entry<NPC, RuneLiteObject> entry = iterator.next();
			if (entry.getKey().getWorldView() == event.getWorldView())
			{
				removeVisual(entry.getValue());
				iterator.remove();
			}
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		pets.remove(event.getNpc());
		removeVisual(resizedPets.remove(event.getNpc()));
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() != GameState.LOGGED_IN)
		{
			clearPets();
			clearExternalPet();
			needsVisualRequest = true;
			pets.clear();
			needsPetScan = true;
		}
	}

	private void clearPets()
	{
		resizedPets.values().forEach(BigPets::removeVisual);
		resizedPets.clear();
	}

	private static void removeVisual(RuneLiteObject resizedPet)
	{
		if (resizedPet != null)
		{
			resizedPet.setActive(false);
			resizedPet.setModel(null);
		}
	}

	@Provides
	BigPetsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BigPetsConfig.class);
	}
}
