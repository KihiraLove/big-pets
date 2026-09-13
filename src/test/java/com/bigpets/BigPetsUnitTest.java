package com.bigpets;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import java.util.Arrays;
import java.util.function.Supplier;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.IndexedObjectSet;
import net.runelite.api.Model;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.RuneLiteObject;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.WorldViewUnloaded;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.hooks.DrawCallbacks;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.callback.RenderCallback;
import net.runelite.client.callback.RenderCallbackManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.gpu.GpuPlugin;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class BigPetsUnitTest
{
	private final PluginManager pluginManager = mock(PluginManager.class);
	private final GpuPlugin builtin = mock(GpuPlugin.class);
	private final Client client = mock(Client.class);
	private final ClientThread clientThread = mock(ClientThread.class);
	private final RenderCallbackManager callbacks = mock(RenderCallbackManager.class);
	private final ConfigManager configManager = mock(ConfigManager.class);
	private final BigPetsConfig config = mock(BigPetsConfig.class);
	private final NPC follower = mock(NPC.class);
	private final Model original = mock(Model.class);
	private final Model copy = mock(Model.class);
	private final RuneLiteObject visual = mock(RuneLiteObject.class);
	private BigPets plugin;
	private RenderCallback callback;

	@Before
	public void setUp()
	{
		when(pluginManager.getPlugins()).thenReturn(java.util.Collections.singletonList(builtin));
		plugin = new BigPets();
		Guice.createInjector(new AbstractModule()
		{
			@Override
			protected void configure()
			{
				bind(Client.class).toInstance(client);
				bind(PluginManager.class).toInstance(pluginManager);
				bind(ClientThread.class).toInstance(clientThread);
				bind(RenderCallbackManager.class).toInstance(callbacks);
				bind(ConfigManager.class).toInstance(configManager);
				bind(BigPetsConfig.class).toInstance(config);
			}
		}).injectMembers(plugin);
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		when(client.isGpu()).thenReturn(true);
		when(client.getDrawCallbacks()).thenReturn(mock(GpuPlugin.class));
		doAnswer(invocation ->
		{
			when(client.getDrawCallbacks()).thenReturn(invocation.getArgument(0));
			return null;
		}).when(client).setDrawCallbacks(any());
		when(client.getFollower()).thenReturn(follower);
		when(follower.getWorldView()).thenReturn(mock(WorldView.class));
		when(follower.getLocalLocation()).thenReturn(new LocalPoint(6400, 6400));
		when(follower.getModel()).thenReturn(original);
		when(client.mergeModels(any(Model[].class))).thenReturn(copy);
		when(client.createRuneLiteObject()).thenReturn(visual);
		when(config.petSizePercentage()).thenReturn(200);
		doAnswer(invocation ->
		{
			invocation.getArgument(0, Runnable.class).run();
			return null;
		}).when(clientThread).invokeLater(any(Runnable.class));
		plugin.startUp();
		ArgumentCaptor<RenderCallback> captor = ArgumentCaptor.forClass(RenderCallback.class);
		verify(callbacks).register(captor.capture());
		callback = captor.getValue();
	}

	@Test
	public void scalesOnlyVisualCopyAndKeepsOriginalClickable()
	{
		plugin.onBeforeRender(new BeforeRender());
		verify(copy).scale(256, 256, 256);
		verify(original, never()).scale(anyInt(), anyInt(), anyInt());
		verify(visual).setModel(copy);
		assertTrue(callback.addEntity(follower, false));
		assertTrue(callback.addEntity(follower, true));
		assertFalse(draws(follower));
		assertTrue(draws(mock(NPC.class)));
	}

	@Test
	public void zeroHidesOnlyTheVisual()
	{
		when(config.petSizePercentage()).thenReturn(0);
		plugin.onBeforeRender(new BeforeRender());
		verify(client, never()).createRuneLiteObject();
		assertFalse(draws(follower));
		assertTrue(callback.addEntity(follower, false));
	}

	@Test
	public void normalSizeRemovesTheReplacement()
	{
		plugin.onBeforeRender(new BeforeRender());
		when(config.petSizePercentage()).thenReturn(100);
		plugin.onBeforeRender(new BeforeRender());
		verify(visual).setActive(false);
		assertTrue(draws(follower));
	}

	@Test
	public void unsupportedRendererRestoresNormalRendering()
	{
		plugin.onBeforeRender(new BeforeRender());
		when(client.getDrawCallbacks()).thenReturn(null);
		plugin.onBeforeRender(new BeforeRender());
		verify(visual).setActive(false);
		assertTrue(draws(follower));
	}

	@Test
	public void despawnRemovesTheReplacement()
	{
		plugin.onBeforeRender(new BeforeRender());
		plugin.onNpcDespawned(new NpcDespawned(follower));
		verify(visual).setActive(false);
		assertTrue(draws(follower));
	}

	@Test
	public void unavailableModelLeavesOriginalVisible()
	{
		when(follower.getModel()).thenReturn(null);
		plugin.onBeforeRender(new BeforeRender());
		assertTrue(draws(follower));
		verify(client, never()).createRuneLiteObject();
	}

	@Test
	public void repeatedFramesCopyFreshModelsInsteadOfScalingTheNpc()
	{
		plugin.onBeforeRender(new BeforeRender());
		plugin.onBeforeRender(new BeforeRender());
		verify(client, times(2)).mergeModels(new Model[]{original});
		verify(original, never()).scale(anyInt(), anyInt(), anyInt());
		verify(client).createRuneLiteObject();
	}

	@Test
	public void sizeIsClampedAtFiveTimesNormal()
	{
		when(config.petSizePercentage()).thenReturn(900);
		plugin.onBeforeRender(new BeforeRender());
		verify(copy).scale(640, 640, 640);
	}

	@Test
	public void shutdownRemovesVisualAndCallback()
	{
		plugin.onBeforeRender(new BeforeRender());
		plugin.shutDown();
		verify(callbacks).unregister(callback);
		verify(visual).setActive(false);
		assertTrue(draws(follower));
	}

	@Test
	public void allPetsDefaultsToOn()
	{
		assertTrue(new BigPetsConfig() {}.resizeAllPets());
	}

	@Test
	public void otherPlayersFollowersAreOptIn()
	{
		NPC other = pet(NpcID.YAMA_PET, true);
		plugin.onNpcSpawned(new NpcSpawned(other));
		plugin.onBeforeRender(new BeforeRender());
		assertTrue(draws(other));

		when(config.resizeAllPets()).thenReturn(true);
		plugin.onBeforeRender(new BeforeRender());
		assertFalse(draws(other));
		assertFalse(draws(follower));
		assertTrue(callback.addEntity(other, false));
		assertTrue(callback.addEntity(other, true));
	}

	@Test
	public void pohPetsResizeWithoutALocalFollower()
	{
		when(client.getFollower()).thenReturn(null);
		when(config.resizeAllPets()).thenReturn(true);
		NPC bossPet = pet(NpcID.POH_YAMA_PET, false);
		NPC cat = pet(NpcID.POH_GROWNCAT_DEFAULT, false);
		plugin.onNpcSpawned(new NpcSpawned(bossPet));
		plugin.onNpcSpawned(new NpcSpawned(cat));
		plugin.onBeforeRender(new BeforeRender());
		assertFalse(draws(bossPet));
		assertFalse(draws(cat));
		verify(client, times(2)).createRuneLiteObject();
	}

	@Test
	public void houseMonstersAndServantsAreNotPets()
	{
		when(config.resizeAllPets()).thenReturn(true);
		NPC monster = pet(NpcID.POH_RUNE_DRAGON, false);
		NPC servant = pet(NpcID.POH_SERVANT_DEMON, false);
		plugin.onNpcSpawned(new NpcSpawned(monster));
		plugin.onNpcSpawned(new NpcSpawned(servant));
		plugin.onBeforeRender(new BeforeRender());
		assertTrue(draws(monster));
		assertTrue(draws(servant));
		verify(monster, never()).getModel();
		verify(servant, never()).getModel();
	}

	@Test
	public void disablingAllPetsRestoresOthersButKeepsOwnPetResized()
	{
		when(config.resizeAllPets()).thenReturn(true);
		NPC other = pet(NpcID.POH_YAMA_PET, false);
		RuneLiteObject otherVisual = mock(RuneLiteObject.class);
		when(client.createRuneLiteObject()).thenReturn(otherVisual, visual);
		plugin.onNpcSpawned(new NpcSpawned(other));
		plugin.onBeforeRender(new BeforeRender());
		when(config.resizeAllPets()).thenReturn(false);
		plugin.onBeforeRender(new BeforeRender());
		assertTrue(draws(other));
		assertFalse(draws(follower));
		verify(otherVisual).setActive(false);
		verify(visual, never()).setActive(false);
	}

	@Test
	public void zeroSizePreservesEveryPetsClickbox()
	{
		when(config.resizeAllPets()).thenReturn(true);
		when(config.petSizePercentage()).thenReturn(0);
		NPC other = pet(NpcID.YAMA_PET, true);
		NPC poh = pet(NpcID.POH_YAMA_PET, false);
		plugin.onNpcSpawned(new NpcSpawned(other));
		plugin.onNpcSpawned(new NpcSpawned(poh));
		plugin.onBeforeRender(new BeforeRender());
		for (NPC npc : new NPC[]{follower, other, poh})
		{
			assertFalse(draws(npc));
			assertTrue(callback.addEntity(npc, false));
		}
		verify(client, never()).createRuneLiteObject();
	}

	@Test
	public void despawningOnePetDoesNotRemoveAnother()
	{
		when(config.resizeAllPets()).thenReturn(true);
		NPC other = pet(NpcID.YAMA_PET, true);
		RuneLiteObject otherVisual = mock(RuneLiteObject.class);
		when(client.createRuneLiteObject()).thenReturn(otherVisual, visual);
		plugin.onNpcSpawned(new NpcSpawned(other));
		plugin.onBeforeRender(new BeforeRender());
		plugin.onNpcDespawned(new NpcDespawned(other));
		plugin.onBeforeRender(new BeforeRender());
		assertTrue(draws(other));
		assertFalse(draws(follower));
		verify(otherVisual).setActive(false);
		verify(visual, never()).setActive(false);
	}

	@Test
	public void compositionChangesUpdatePetTracking()
	{
		when(config.resizeAllPets()).thenReturn(true);
		NPC other = pet(NpcID.POH_YAMA_PET, false);
		plugin.onNpcSpawned(new NpcSpawned(other));
		plugin.onBeforeRender(new BeforeRender());
		assertFalse(draws(other));
		when(other.getId()).thenReturn(NpcID.POH_SERVANT_DEMON);
		plugin.onNpcChanged(new NpcChanged(other, other.getComposition()));
		plugin.onBeforeRender(new BeforeRender());
		assertTrue(draws(other));
		when(other.getId()).thenReturn(NpcID.POH_DOM_PET);
		plugin.onNpcChanged(new NpcChanged(other, other.getComposition()));
		plugin.onBeforeRender(new BeforeRender());
		assertFalse(draws(other));
	}

	@Test
	public void existingPetsAreScannedOnceIncludingChildWorldViews()
	{
		when(config.resizeAllPets()).thenReturn(true);
		NPC other = pet(NpcID.YAMA_PET, true);
		NPC poh = pet(NpcID.POH_YAMA_PET, false);
		WorldView child = worldView(new NPC[]{poh});
		WorldView root = worldView(new NPC[]{other}, child);
		when(client.getTopLevelWorldView()).thenReturn(root);
		plugin.onBeforeRender(new BeforeRender());
		plugin.onBeforeRender(new BeforeRender());
		assertFalse(draws(other));
		assertFalse(draws(poh));
		verify(root).npcs();
		verify(child).npcs();
	}

	@Test
	public void unloadingWorldViewRemovesItsPets()
	{
		when(config.resizeAllPets()).thenReturn(true);
		NPC other = pet(NpcID.YAMA_PET, true);
		plugin.onNpcSpawned(new NpcSpawned(other));
		plugin.onBeforeRender(new BeforeRender());
		plugin.onWorldViewUnloaded(new WorldViewUnloaded(other.getWorldView()));
		plugin.onBeforeRender(new BeforeRender());
		assertTrue(draws(other));
		assertFalse(draws(follower));
	}

	@Test
	public void loadingClearsPetsAndRescansAfterLogin()
	{
		when(config.resizeAllPets()).thenReturn(true);
		NPC other = pet(NpcID.POH_YAMA_PET, false);
		WorldView root = worldView(new NPC[]{other});
		when(client.getTopLevelWorldView()).thenReturn(root);
		plugin.onBeforeRender(new BeforeRender());
		GameStateChanged loading = new GameStateChanged();
		loading.setGameState(GameState.LOADING);
		plugin.onGameStateChanged(loading);
		assertTrue(draws(other));
		plugin.onBeforeRender(new BeforeRender());
		assertFalse(draws(other));
		verify(root, times(2)).npcs();
	}

	@Test
	public void filtersDefaultToOff()
	{
		BigPetsConfig defaults = new BigPetsConfig() {};
		assertFalse(defaults.filterCatsAndDogs());
		assertFalse(defaults.filterQuestAndEventPets());
	}

	@Test
	public void filteringOwnCatRestoresItAndTurningFilterOffResizesItAgain()
	{
		when(follower.getId()).thenReturn(NpcID.GROWNCAT_HELL);
		plugin.onBeforeRender(new BeforeRender());
		assertFalse(draws(follower));
		when(config.filterCatsAndDogs()).thenReturn(true);
		plugin.onBeforeRender(new BeforeRender());
		assertTrue(draws(follower));
		verify(visual).setActive(false);
		when(config.filterCatsAndDogs()).thenReturn(false);
		plugin.onBeforeRender(new BeforeRender());
		assertFalse(draws(follower));
	}

	@Test
	public void filteredPetsStayVisibleEvenAtZeroPercent()
	{
		when(config.resizeAllPets()).thenReturn(true);
		when(config.petSizePercentage()).thenReturn(0);
		when(config.filterCatsAndDogs()).thenReturn(true);
		when(config.filterQuestAndEventPets()).thenReturn(true);
		NPC cat = pet(NpcID.POH_LAZYCAT_HELL, false);
		NPC egg = pet(NpcID.POH_EASTER26_EGG_07, false);
		plugin.onNpcSpawned(new NpcSpawned(cat));
		plugin.onNpcSpawned(new NpcSpawned(egg));
		plugin.onBeforeRender(new BeforeRender());
		assertTrue(draws(cat));
		assertTrue(draws(egg));
		assertFalse(draws(follower));
		assertTrue(callback.addEntity(cat, false));
		assertTrue(callback.addEntity(egg, false));
	}

	@Test
	public void filtersAreIndependent()
	{
		when(config.resizeAllPets()).thenReturn(true);
		NPC cat = pet(NpcID.POH_TOY_CAT_MENAGERIE, false);
		NPC fish = pet(NpcID.POH_FISHBOWL_MAYOR_OF_CATHERBY, false);
		plugin.onNpcSpawned(new NpcSpawned(cat));
		plugin.onNpcSpawned(new NpcSpawned(fish));
		when(config.filterQuestAndEventPets()).thenReturn(true);
		plugin.onBeforeRender(new BeforeRender());
		assertFalse(draws(cat));
		assertTrue(draws(fish));
		when(config.filterQuestAndEventPets()).thenReturn(false);
		when(config.filterCatsAndDogs()).thenReturn(true);
		plugin.onBeforeRender(new BeforeRender());
		assertTrue(draws(cat));
		assertFalse(draws(fish));
	}

	@Test
	public void bothFiltersKeepBroavAndBossPetsResizable()
	{
		when(config.resizeAllPets()).thenReturn(true);
		when(config.filterCatsAndDogs()).thenReturn(true);
		when(config.filterQuestAndEventPets()).thenReturn(true);
		when(follower.getId()).thenReturn(NpcID.WGS_BROAV);
		NPC broav = pet(NpcID.POH_BROAV, false);
		NPC hellpuppy = pet(NpcID.POH_HELLPET, false);
		NPC bloodhound = pet(NpcID.POH_BLOODHOUNDPET, false);
		plugin.onNpcSpawned(new NpcSpawned(broav));
		plugin.onNpcSpawned(new NpcSpawned(hellpuppy));
		plugin.onNpcSpawned(new NpcSpawned(bloodhound));
		plugin.onBeforeRender(new BeforeRender());
		assertFalse(draws(follower));
		assertFalse(draws(broav));
		assertFalse(draws(hellpuppy));
		assertFalse(draws(bloodhound));
	}

	@Test
	public void dogBreedsAndPuppiesAreTrackedAndFilteredWithoutFollowerFlag()
	{
		when(client.getFollower()).thenReturn(null);
		when(config.resizeAllPets()).thenReturn(true);
		LocalPoint location = follower.getLocalLocation();
		WorldView worldView = follower.getWorldView();
		for (String breed : new String[]{"Labrador", "Pug", "Spaniel", "Chihuahua",
			"Border Collie", "Corgi", "Greyhound", "Husky", "Samoyed",
			"Bernese Mountain Dog", "Shiba", "Yorkie"})
		{
			for (String name : new String[]{breed, breed + " puppy"})
			{
				NPC dog = mock(NPC.class);
				when(dog.getName()).thenReturn(name);
				when(dog.getModel()).thenReturn(original);
				when(dog.getLocalLocation()).thenReturn(location);
				when(dog.getWorldView()).thenReturn(worldView);
				plugin.onNpcSpawned(new NpcSpawned(dog));
				when(config.filterCatsAndDogs()).thenReturn(false);
				plugin.onBeforeRender(new BeforeRender());
				assertFalse(name, draws(dog));
				when(config.filterCatsAndDogs()).thenReturn(true);
				plugin.onBeforeRender(new BeforeRender());
				assertTrue(name, draws(dog));
				plugin.onNpcDespawned(new NpcDespawned(dog));
			}
		}
	}

	@Test
	public void catAndEventVariantsAreRecognised()
	{
		for (int id : new int[]{NpcID.KITTENPET_HELL, NpcID.OVERGROWNCAT_HELL,
			NpcID.WILEYCAT_HELL, NpcID.LAZYCAT_HELL, NpcID.POH_GROWNCAT_BLUEGREY,
			NpcID.POH_WILEYCAT_HELL, NpcID.POH_TOY_CAT})
		{
			assertTrue(PetFilters.isCatOrDog(pet(id, true)));
		}
		for (int id : new int[]{NpcID.POH_HW_CHAIR, NpcID.POH_ROCK, NpcID.POH_EGG,
			NpcID.POH_FISHBOWL_BLUEFISH, NpcID.POH_FISHBOWL_GREENFISH,
			NpcID.POH_FISHBOWL_SPINEFISH, NpcID.POH_FISHBOWL_MAYOR_OF_CATHERBY,
			NpcID.POH_EASTER26_EGG, NpcID.POH_EASTER26_EGG_07})
		{
			assertTrue(PetFilters.isQuestOrEventPet(pet(id, false)));
		}
	}

	@Test
	public void filtersUseCurrentTransformedAppearance()
	{
		when(config.filterCatsAndDogs()).thenReturn(true);
		NPCComposition transformed = mock(NPCComposition.class);
		when(follower.getTransformedComposition()).thenReturn(transformed);
		when(transformed.getId()).thenReturn(NpcID.KITTENPET_HELL);
		plugin.onBeforeRender(new BeforeRender());
		assertTrue(draws(follower));
		when(transformed.getId()).thenReturn(NpcID.YAMA_PET);
		plugin.onBeforeRender(new BeforeRender());
		assertFalse(draws(follower));
	}

	@Test
	public void thirdPartyGpuResizesPetsAndRestoresItsCallbackAtNormalSize()
	{
		DrawCallbacks renderer = mock(DrawCallbacks.class);
		when(client.getDrawCallbacks()).thenReturn(renderer);
		plugin.onBeforeRender(new BeforeRender());
		assertTrue(client.getDrawCallbacks() instanceof PetDrawCallbacks);
		verify(visual).setModel(copy);
		assertFalse(draws(follower));
		when(config.petSizePercentage()).thenReturn(100);
		plugin.onBeforeRender(new BeforeRender());
		assertSame(renderer, client.getDrawCallbacks());
		assertTrue(draws(follower));
	}

	@Test
	public void decoratedBuiltinGpuKeepsCallbacksAcrossDecoratorTogglesAndShutdown()
	{
		when(pluginManager.isPluginActive(builtin)).thenReturn(true);
		DrawCallbacks decorated = mock(DrawCallbacks.class);
		for (DrawCallbacks current : new DrawCallbacks[]{decorated, builtin, decorated})
		{
			when(client.getDrawCallbacks()).thenReturn(current);
			plugin.onBeforeRender(new BeforeRender());
			assertSame(current, client.getDrawCallbacks());
			assertFalse(draws(follower));
		}
		plugin.shutDown();
		assertSame(decorated, client.getDrawCallbacks());
		assertTrue(draws(follower));
		verify(client, never()).setDrawCallbacks(any());
	}

	@Test
	public void builtinActivationRemovesAnAlreadyInstalledAdapter()
	{
		DrawCallbacks decorated = mock(DrawCallbacks.class);
		when(client.getDrawCallbacks()).thenReturn(decorated);
		plugin.onBeforeRender(new BeforeRender());
		DrawCallbacks adapter = client.getDrawCallbacks();
		assertTrue(adapter instanceof PetDrawCallbacks);

		when(pluginManager.isPluginActive(builtin)).thenReturn(true);
		plugin.onBeforeRender(new BeforeRender());
		assertSame(decorated, client.getDrawCallbacks());
		assertFalse(draws(follower));
		adapter.draw(null, null, follower, 0, 1, 2, 3, 4L);
		verify(decorated).draw(null, null, follower, 0, 1, 2, 3, 4L);
	}

	@Test
	public void switchingBetweenAlternativeAndDecoratedBuiltinPreservesTheCurrentRenderer()
	{
		DrawCallbacks alternative = mock(DrawCallbacks.class);
		when(client.getDrawCallbacks()).thenReturn(alternative);
		plugin.onBeforeRender(new BeforeRender());
		DrawCallbacks oldAdapter = client.getDrawCallbacks();

		DrawCallbacks decorated = mock(DrawCallbacks.class);
		when(pluginManager.isPluginActive(builtin)).thenReturn(true);
		when(client.getDrawCallbacks()).thenReturn(decorated);
		clearInvocations(client);
		plugin.onBeforeRender(new BeforeRender());
		assertSame(decorated, client.getDrawCallbacks());
		verify(client, never()).setDrawCallbacks(any());
		oldAdapter.draw(null, null, follower, 0, 1, 2, 3, 4L);
		verify(alternative).draw(null, null, follower, 0, 1, 2, 3, 4L);

		when(pluginManager.isPluginActive(builtin)).thenReturn(false);
		when(client.getDrawCallbacks()).thenReturn(alternative);
		plugin.onBeforeRender(new BeforeRender());
		assertSame(alternative, ((PetDrawCallbacks) client.getDrawCallbacks()).getDelegate());
		assertFalse(draws(follower));
	}

	@Test
	public void builtinGpuKeepsItsCallbackIdentity()
	{
		DrawCallbacks renderer = client.getDrawCallbacks();
		plugin.onBeforeRender(new BeforeRender());
		assertSame(renderer, client.getDrawCallbacks());
		verify(client, never()).setDrawCallbacks(any());
	}

	@Test
	public void softwareModeWithACallbackDoesNotResize()
	{
		when(client.isGpu()).thenReturn(false);
		plugin.onBeforeRender(new BeforeRender());
		verify(client, never()).createRuneLiteObject();
		verify(client, never()).setDrawCallbacks(any());
		assertTrue(draws(follower));
	}

	@Test
	public void repeatedFramesReuseTheAdapter()
	{
		when(client.getDrawCallbacks()).thenReturn(mock(DrawCallbacks.class));
		plugin.onBeforeRender(new BeforeRender());
		DrawCallbacks adapter = client.getDrawCallbacks();
		plugin.onBeforeRender(new BeforeRender());
		assertSame(adapter, client.getDrawCallbacks());
		verify(client).setDrawCallbacks(adapter);
	}

	@Test
	public void switchingRenderersDoesNotRestoreTheOldRenderer()
	{
		DrawCallbacks first = mock(DrawCallbacks.class);
		when(client.getDrawCallbacks()).thenReturn(first);
		plugin.onBeforeRender(new BeforeRender());
		DrawCallbacks oldAdapter = client.getDrawCallbacks();
		DrawCallbacks second = mock(DrawCallbacks.class);
		when(client.getDrawCallbacks()).thenReturn(second);
		plugin.onBeforeRender(new BeforeRender());
		assertSame(second, ((PetDrawCallbacks) client.getDrawCallbacks()).getDelegate());
		oldAdapter.draw(null, null, follower, 0, 1, 2, 3, 4L);
		verify(first).draw(null, null, follower, 0, 1, 2, 3, 4L);
		plugin.shutDown();
		assertSame(second, client.getDrawCallbacks());
		verify(client, never()).setDrawCallbacks(first);
	}

	@Test
	public void rendererStoppingIsNotUndoneOnShutdown()
	{
		DrawCallbacks renderer = mock(DrawCallbacks.class);
		when(client.getDrawCallbacks()).thenReturn(renderer);
		plugin.onBeforeRender(new BeforeRender());
		when(client.getDrawCallbacks()).thenReturn(null);
		plugin.shutDown();
		verify(client, never()).setDrawCallbacks(renderer);
	}

	@Test
	public void zeroSizeWithLegacyRendererKeepsOriginalPicking()
	{
		DrawCallbacks renderer = mock(DrawCallbacks.class);
		when(client.getDrawCallbacks()).thenReturn(renderer);
		when(config.petSizePercentage()).thenReturn(0);
		plugin.onBeforeRender(new BeforeRender());
		client.getDrawCallbacks().draw(null, null, follower, 0, 1, 2, 3, 4L);
		verify(client).checkClickbox(null, original, 0, 1, 2, 3, 4L);
		verify(renderer, never()).draw(null, null, follower, 0, 1, 2, 3, 4L);
		verify(client, never()).createRuneLiteObject();
	}

	@Test
	public void anotherPluginsWrapperDoesNotCauseRepeatedWrapping()
	{
		DrawCallbacks renderer = mock(DrawCallbacks.class);
		when(client.getDrawCallbacks()).thenReturn(renderer);
		plugin.onBeforeRender(new BeforeRender());
		DrawCallbacks adapter = client.getDrawCallbacks();
		DrawCallbacks outer = mock(DrawCallbacks.class, withSettings().extraInterfaces(Supplier.class));
		doReturn(adapter).when((Supplier<?>) outer).get();
		when(client.getDrawCallbacks()).thenReturn(outer);
		clearInvocations(client);
		for (int i = 0; i < 100; i++)
		{
			plugin.onBeforeRender(new BeforeRender());
		}
		assertSame(outer, client.getDrawCallbacks());
		verify(client, never()).setDrawCallbacks(any());
		plugin.shutDown();
		assertSame(outer, client.getDrawCallbacks());
		adapter.draw(null, null, follower, 0, 1, 2, 3, 4L);
		verify(renderer).draw(null, null, follower, 0, 1, 2, 3, 4L);
	}

	private NPC pet(int id, boolean isFollower)
	{
		NPC npc = mock(NPC.class);
		NPCComposition composition = mock(NPCComposition.class);
		when(npc.getId()).thenReturn(id);
		when(npc.getComposition()).thenReturn(composition);
		when(composition.isFollower()).thenReturn(isFollower);
		when(npc.getWorldView()).thenReturn(mock(WorldView.class));
		when(npc.getLocalLocation()).thenReturn(new LocalPoint(6400, 6400));
		when(npc.getModel()).thenReturn(mock(Model.class));
		return npc;
	}

	private WorldView worldView(NPC[] npcs, WorldView... children)
	{
		WorldView worldView = mock(WorldView.class);
		doReturn(indexedSet(npcs)).when(worldView).npcs();
		doReturn(indexedSet(children)).when(worldView).worldViews();
		return worldView;
	}

	@SuppressWarnings("unchecked")
	private <T> IndexedObjectSet<T> indexedSet(T[] values)
	{
		IndexedObjectSet<T> set = mock(IndexedObjectSet.class);
		when(set.iterator()).thenAnswer(invocation -> Arrays.asList(values).iterator());
		return set;
	}

	private boolean draws(NPC npc)
	{
		GameObject object = mock(GameObject.class);
		when(object.getRenderable()).thenReturn(npc);
		return callback.drawObject(null, object);
	}
}
