package com.bigpets;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup(BigPetsConfig.GROUP)
public interface BigPetsConfig extends Config
{
	String GROUP = "big-pets";

	@Range(min = 0, max = 500)
	@ConfigItem(
		keyName = "petSizePercentage",
		name = "Pet size (%)",
		description = "Visual size: 100 is normal, 0 hides the pet. The original clickbox is intact."
	)
	default int petSizePercentage()
	{
		return 100;
	}

	@ConfigItem(
		keyName = "resizeAllPets",
		name = "Resize all pets",
		description = "Resize other player's pets and POH pets",
		position = 1
	)
	default boolean resizeAllPets()
	{
		return false;
	}

	@ConfigItem(
		keyName = "filterCatsAndDogs",
		name = "Filter cats and dogs",
		description = "Keep cats and dogs at their normal size.",
		position = 2
	)
	default boolean filterCatsAndDogs()
	{
		return false;
	}

	@ConfigItem(
		keyName = "filterQuestAndEventPets",
		name = "Filter quest and Event pets",
		description = "Keep quest and event pets at normal size.(except the Broav)",
		position = 3
	)
	default boolean filterQuestAndEventPets()
	{
		return false;
	}
}
