package com.bigpets;

import net.runelite.api.Client;
import net.runelite.api.Model;
import net.runelite.api.NPC;
import net.runelite.api.RuneLiteObject;

class PetVisual extends RuneLiteObject
{
	private static final int MODEL_SCALE = 128;
	private static final int NORMAL_SIZE = 100;

	private final Client client;
	private final NPC pet;
	private int scale = MODEL_SCALE;

	PetVisual(Client client, NPC pet)
	{
		super(client);
		this.client = client;
		this.pet = pet;
	}

	void setSizePercentage(int percentage)
	{
		scale = Math.round(MODEL_SCALE * percentage / (float) NORMAL_SIZE);
	}

	@Override
	public Model getModel()
	{
		// Both NPC models and applyTransformations results can be shared scratch models.
		// Obtain and scale a fresh model at draw time; never retain one between pets or frames.
		Model original = pet.getModel();
		if (original == null)
		{
			return null;
		}
		Model scaled = client.applyTransformations(original, null, 0, null, 0);
		if (scaled != null)
		{
			scaled.scale(scale, scale, scale);
		}
		return scaled;
	}
}
