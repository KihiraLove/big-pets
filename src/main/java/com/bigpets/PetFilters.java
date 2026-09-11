package com.bigpets;

import java.util.Locale;
import java.util.Set;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.gameval.NpcID;

final class PetFilters
{
    // will be changed to gamevals once they are added to RuneLite API
	private static final Set<String> DOG_BREEDS = Set.of(
		"labrador", "pug", "spaniel", "chihuahua", "border collie", "corgi",
		"greyhound", "husky", "samoyed", "bernese mountain dog", "shiba", "yorkie"
	);

	private PetFilters()
	{
	}

	static boolean isDog(NPC npc)
	{
		String name = name(npc);
		if (name.endsWith(" puppy"))
		{
			name = name.substring(0, name.length() - " puppy".length());
		}
		return DOG_BREEDS.contains(name);
	}

	static boolean isCatOrDog(NPC npc)
	{
		return isCat(npc.getId()) || isCat(compositionId(npc)) || isDog(npc);
	}

	static boolean isQuestOrEventPet(NPC npc)
	{
		return isQuestOrEventPet(npc.getId()) || isQuestOrEventPet(compositionId(npc));
	}

	private static int compositionId(NPC npc)
	{
		NPCComposition composition = npc.getTransformedComposition();
		return composition == null ? npc.getId() : composition.getId();
	}

	private static String name(NPC npc)
	{
		NPCComposition composition = npc.getTransformedComposition();
		String name = composition == null ? npc.getName() : composition.getName();
		return name == null ? "" : name.toLowerCase(Locale.ROOT);
	}

	private static boolean isCat(int id)
	{
		switch (id)
		{
			case NpcID.GROWNCAT:
			case NpcID.GROWNCAT_LIGHT:
			case NpcID.GROWNCAT_BROWN:
			case NpcID.GROWNCAT_BLACK:
			case NpcID.GROWNCAT_BROWNGREY:
			case NpcID.GROWNCAT_BLUEGREY:
			case NpcID.GROWNCAT_HELL:
			case NpcID.LAZYCAT_LIGHT:
			case NpcID.LAZYCAT:
			case NpcID.LAZYCAT_BROWN:
			case NpcID.LAZYCAT_BLACK:
			case NpcID.LAZYCAT_BROWNGREY:
			case NpcID.LAZYCAT_BLUEGREY:
			case NpcID.LAZYCAT_HELL:
			case NpcID.POH_TOY_CAT:
			case NpcID.WILEYCAT_LIGHT:
			case NpcID.WILEYCAT:
			case NpcID.WILEYCAT_BROWN:
			case NpcID.WILEYCAT_BLACK:
			case NpcID.WILEYCAT_BROWNGREY:
			case NpcID.WILEYCAT_BLUEGREY:
			case NpcID.WILEYCAT_HELL:
			case NpcID.KITTENPET1:
			case NpcID.KITTENPET_LIGHT:
			case NpcID.KITTENPET_BROWN:
			case NpcID.KITTENPET_BLACK:
			case NpcID.KITTENPET_BROWNGREY:
			case NpcID.KITTENPET_BLUEGREY:
			case NpcID.KITTENPET_HELL:
			case NpcID.OVERGROWNCAT:
			case NpcID.OVERGROWNCAT_LIGHT:
			case NpcID.OVERGROWNCAT_BROWN:
			case NpcID.OVERGROWNCAT_BLACK:
			case NpcID.OVERGROWNCAT_BROWNGREY:
			case NpcID.OVERGROWNCAT_BLUEGREY:
			case NpcID.OVERGROWNCAT_HELL:
			case NpcID.POH_TOY_CAT_MENAGERIE:
			case NpcID.POH_GROWNCAT_DEFAULT:
			case NpcID.POH_GROWNCAT_LIGHT:
			case NpcID.POH_GROWNCAT_BROWN:
			case NpcID.POH_GROWNCAT_BLACK:
			case NpcID.POH_GROWNCAT_BROWNGREY:
			case NpcID.POH_GROWNCAT_BLUEGREY:
			case NpcID.POH_GROWNCAT_HELL:
			case NpcID.POH_OVERGROWNCAT_DEFAULT:
			case NpcID.POH_OVERGROWNCAT_LIGHT:
			case NpcID.POH_OVERGROWNCAT_BROWN:
			case NpcID.POH_OVERGROWNCAT_BLACK:
			case NpcID.POH_OVERGROWNCAT_BROWNGREY:
			case NpcID.POH_OVERGROWNCAT_BLUEGREY:
			case NpcID.POH_OVERGROWNCAT_HELL:
			case NpcID.POH_LAZYCAT_DEFAULT:
			case NpcID.POH_LAZYCAT_LIGHT:
			case NpcID.POH_LAZYCAT_BROWN:
			case NpcID.POH_LAZYCAT_BLACK:
			case NpcID.POH_LAZYCAT_BROWNGREY:
			case NpcID.POH_LAZYCAT_BLUEGREY:
			case NpcID.POH_LAZYCAT_HELL:
			case NpcID.POH_WILEYCAT_DEFAULT:
			case NpcID.POH_WILEYCAT_LIGHT:
			case NpcID.POH_WILEYCAT_BROWN:
			case NpcID.POH_WILEYCAT_BLACK:
			case NpcID.POH_WILEYCAT_BROWNGREY:
			case NpcID.POH_WILEYCAT_BLUEGREY:
			case NpcID.POH_WILEYCAT_HELL:
				return true;
			default:
				return false;
		}
	}

	private static boolean isQuestOrEventPet(int id)
	{
		switch (id)
		{
			case NpcID.POH_ROCK:
			case NpcID.DAGANNOTH_DUNGEON_PRESSURE_PET_ROCK:
			case NpcID.POH_EGG:
			case NpcID.POH_HW_CHAIR:
			case NpcID.HW25_CHAIR_NPC_REWARD:
			case NpcID.POH_FISHBOWL_BLUEFISH:
			case NpcID.POH_FISHBOWL_GREENFISH:
			case NpcID.POH_FISHBOWL_SPINEFISH:
			case NpcID.POH_FISHBOWL_MAYOR_OF_CATHERBY:
			case NpcID.POH_EASTER26_EGG:
			case NpcID.POH_EASTER26_EGG_02:
			case NpcID.POH_EASTER26_EGG_03:
			case NpcID.POH_EASTER26_EGG_04:
			case NpcID.POH_EASTER26_EGG_05:
			case NpcID.POH_EASTER26_EGG_06:
			case NpcID.POH_EASTER26_EGG_07:
			case NpcID.DAGANNOTH_DUNGEON_PRESSURE_PET_EGG:
			case NpcID.DAGANNOTH_DUNGEON_PRESSURE_PET_EASTER26_EGG:
			case NpcID.DAGANNOTH_DUNGEON_PRESSURE_PET_EASTER26_EGG_02:
			case NpcID.DAGANNOTH_DUNGEON_PRESSURE_PET_EASTER26_EGG_03:
			case NpcID.DAGANNOTH_DUNGEON_PRESSURE_PET_EASTER26_EGG_04:
			case NpcID.DAGANNOTH_DUNGEON_PRESSURE_PET_EASTER26_EGG_05:
			case NpcID.DAGANNOTH_DUNGEON_PRESSURE_PET_EASTER26_EGG_06:
			case NpcID.DAGANNOTH_DUNGEON_PRESSURE_PET_EASTER26_EGG_07:
				return true;
			default:
				return false;
		}
	}
}

