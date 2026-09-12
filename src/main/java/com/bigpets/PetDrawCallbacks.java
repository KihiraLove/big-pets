package com.bigpets;

import java.util.function.Predicate;
import lombok.Getter;
import lombok.experimental.Delegate;
import net.runelite.api.Client;
import net.runelite.api.Model;
import net.runelite.api.Projection;
import net.runelite.api.Renderable;
import net.runelite.api.Scene;
import net.runelite.api.hooks.DrawCallbacks;

/**
 * Supplies visual-only hiding for renderers using the legacy draw callback.
 * Modern renderers continue to use RenderCallback.drawObject instead.
 */
final class PetDrawCallbacks implements DrawCallbacks
{
	private final Client client;
	private final Predicate<Renderable> hidden;
	private volatile boolean active = true;

	// Lombok generates forwarding methods at compile time, including default methods.
	// No reflection or dependency on a third-party renderer is needed.
	@Getter
	@Delegate
	private final DrawCallbacks delegate;

	PetDrawCallbacks(Client client, DrawCallbacks delegate, Predicate<Renderable> hidden)
	{
		this.client = client;
		this.delegate = delegate;
		this.hidden = hidden;
	}

	void deactivate()
	{
		active = false;
	}

	@Override
	public void draw(Projection projection, Scene scene, Renderable renderable,
		int orientation, int x, int y, int z, long hash)
	{
		if (!active || !hidden.test(renderable))
		{
			delegate.draw(projection, scene, renderable, orientation, x, y, z, hash);
			return;
		}

		// Legacy renderers perform picking inside draw(), so skipping their draw
		// also requires checking the unchanged NPC model's original clickbox here.
		Model model = renderable.getModel();
		if (model != null)
		{
			model.calculateBoundsCylinder();
			renderable.setModelHeight(model.getModelHeight());
			client.checkClickbox(projection, model, orientation, x, y, z, hash);
		}
	}
}
