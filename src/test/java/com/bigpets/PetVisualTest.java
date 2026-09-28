package com.bigpets;

import net.runelite.api.Client;
import net.runelite.api.Model;
import net.runelite.api.NPC;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class PetVisualTest
{
	private final Client client = mock(Client.class);
	private final NPC pet = mock(NPC.class);
	private final PetVisual visual = new PetVisual(client, pet);

	@Test
	public void scalesFreshAnimatedVerticesWithoutChangingSourceGeometry()
	{
		Model original = model(new float[]{10, -20});
		Model animated = model(new float[]{15, -30});
		Model scratch = sharedModel();
		when(pet.getModel()).thenReturn(original, animated);
		visual.setSizePercentage(200);

		assertSame(scratch, visual.getModel());
		assertArrayEquals(new float[]{20, -40}, scratch.getVerticesX(), 0f);
		assertSame(scratch, visual.getModel());
		assertArrayEquals(new float[]{30, -60}, scratch.getVerticesX(), 0f);
		assertArrayEquals(new float[]{10, -20}, original.getVerticesX(), 0f);
		assertArrayEquals(new float[]{15, -30}, animated.getVerticesX(), 0f);
		verify(original, never()).scale(anyInt(), anyInt(), anyInt());
		verify(animated, never()).scale(anyInt(), anyInt(), anyInt());
	}

	@Test
	public void interleavedPetsRebuildTheSharedModelWithoutCompoundingScale()
	{
		NPC other = mock(NPC.class);
		PetVisual otherVisual = new PetVisual(client, other);
		Model firstModel = model(new float[]{10, -20});
		Model secondModel = model(new float[]{40, -60});
		when(pet.getModel()).thenReturn(firstModel);
		when(other.getModel()).thenReturn(secondModel);
		Model scratch = sharedModel();
		visual.setSizePercentage(500);
		otherVisual.setSizePercentage(50);

		visual.getModel();
		assertArrayEquals(new float[]{50, -100}, scratch.getVerticesX(), 0f);
		otherVisual.getModel();
		assertArrayEquals(new float[]{20, -30}, scratch.getVerticesX(), 0f);
		visual.getModel();
		assertArrayEquals(new float[]{50, -100}, scratch.getVerticesX(), 0f);
	}

	@Test
	public void missingNpcModelIsHandledAtDrawTime()
	{
		assertNull(visual.getModel());
		verifyNoInteractions(client);
	}

	@Test
	public void missingTransformedModelIsHandledAtDrawTime()
	{
		when(pet.getModel()).thenReturn(mock(Model.class));
		assertNull(visual.getModel());
	}

	private Model model(float[] vertices)
	{
		Model model = mock(Model.class);
		when(model.getVerticesX()).thenReturn(vertices);
		return model;
	}

	private Model sharedModel()
	{
		Model scratch = model(new float[2]);
		when(client.applyTransformations(any(Model.class), isNull(), eq(0), isNull(), eq(0)))
			.thenAnswer(invocation ->
			{
				Model source = invocation.getArgument(0);
				System.arraycopy(source.getVerticesX(), 0, scratch.getVerticesX(), 0, 2);
				return scratch;
			});
		doAnswer(invocation ->
		{
			float factor = invocation.getArgument(0, Integer.class) / 128f;
			float[] vertices = scratch.getVerticesX();
			for (int i = 0; i < vertices.length; i++)
			{
				vertices[i] *= factor;
			}
			return scratch;
		}).when(scratch).scale(anyInt(), anyInt(), anyInt());
		return scratch;
	}
}
