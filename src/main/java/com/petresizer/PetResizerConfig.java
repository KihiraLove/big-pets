package com.petresizer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup(PetResizerConfig.GROUP)
public interface PetResizerConfig extends Config
{
	String GROUP = "pet-resizer";

	@Range(min = 0, max = 500)
	@ConfigItem(
		keyName = "petSizePercentage",
		name = "Pet size (%)",
		description = "Visual size: 100 is normal, 0 hides the pet. Keeps the original clickbox. Requires RuneLite GPU."
	)
	default int petSizePercentage()
	{
		return 100;
	}

	@ConfigItem(
		keyName = "resizeAllPets",
		name = "Resize all pets",
		description = "Apply the pet size to other players' followers and POH pets as well as your own follower.",
		position = 1
	)
	default boolean resizeAllPets()
	{
		return false;
	}

	@ConfigItem(
		keyName = "filterCatsAndDogs",
		name = "Filter cats and dogs",
		description = "Keep cats, kittens, hellcats, clockwork cats, dogs and puppies at their normal size.",
		position = 2
	)
	default boolean filterCatsAndDogs()
	{
		return false;
	}

	@ConfigItem(
		keyName = "filterQuestAndEventPets",
		name = "Filter quest and Event pets",
		description = "Keep quest and event pets at normal size, including rocks, eggs, fish and the spooky chair. Broav is never filtered.",
		position = 3
	)
	default boolean filterQuestAndEventPets()
	{
		return false;
	}
}
