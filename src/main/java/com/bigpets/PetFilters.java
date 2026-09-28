package com.bigpets;

import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.gameval.NpcID;

final class PetFilters
{
	private PetFilters()
	{
	}

	static boolean isDog(NPC npc)
	{
		return isDog(npc.getId()) || isDog(compositionId(npc));
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

	private static boolean isDog(int id)
	{
		switch (id)
		{
			case NpcID.LABRADOR_YELLOW:
			case NpcID.LABRADOR_BROWN:
			case NpcID.LABRADOR_BLACK:
			case NpcID.CHIHUAHUA_TAN:
			case NpcID.CHIHUAHUA_WHITE:
			case NpcID.CHIHUAHUA_TOASTED:
			case NpcID.COLLIE_CHOCO:
			case NpcID.COLLIE_MERLE:
			case NpcID.COLLIE_BW:
			case NpcID.CORGI_TAN:
			case NpcID.CORGI_YELLOW:
			case NpcID.CORGI_TOASTED:
			case NpcID.GREYHOUND_TAN:
			case NpcID.GREYHOUND_GREY:
			case NpcID.GREYHOUND_CREAM:
			case NpcID.HUSKY_BW:
			case NpcID.HUSKY_GREY:
			case NpcID.HUSKY_CHOCO:
			case NpcID.PUG_FAWN:
			case NpcID.PUG_BROWN:
			case NpcID.PUG_BLACK:
			case NpcID.SAMOYED_WHITE:
			case NpcID.SAMOYED_YELLOW:
			case NpcID.SAMOYED_BLACK:
			case NpcID.SHEPARD_CHOCO:
			case NpcID.SHEPARD_MERLE:
			case NpcID.SHEPARD_TOASTED:
			case NpcID.SHIBA_TAN:
			case NpcID.SHIBA_WHITE:
			case NpcID.SHIBA_TOASTED:
			case NpcID.SPANIEL_RED:
			case NpcID.SPANIEL_WHITE:
			case NpcID.SPANIEL_BLACK:
			case NpcID.YORKIE_BROWN:
			case NpcID.YORKIE_WHITE:
			case NpcID.YORKIE_YELLOW:
			case NpcID.LABRADOR_YELLOW_PUPPY:
			case NpcID.LABRADOR_CHOCO_PUPPY:
			case NpcID.LABRADOR_BLACK_PUPPY:
			case NpcID.HUSKY_BW_PUPPY:
			case NpcID.HUSKY_GREY_PUPPY:
			case NpcID.HUSKY_CHOCO_PUPPY:
			case NpcID.CHIHUAHUA_TAN_PUPPY:
			case NpcID.CHIHUAHUA_WHITE_PUPPY:
			case NpcID.CHIHUAHUA_TOASTED_PUPPY:
			case NpcID.COLLIE_CHOCO_PUPPY:
			case NpcID.COLLIE_MERLE_PUPPY:
			case NpcID.COLLIE_BW_PUPPY:
			case NpcID.CORGI_TAN_PUPPY:
			case NpcID.CORGI_YELLOW_PUPPY:
			case NpcID.CORGI_TOASTED_PUPPY:
			case NpcID.GREYHOUND_TAN_PUPPY:
			case NpcID.GREYHOUND_GREY_PUPPY:
			case NpcID.GREYHOUND_CREAM_PUPPY:
			case NpcID.PUG_FAWN_PUPPY:
			case NpcID.PUG_BROWN_PUPPY:
			case NpcID.PUG_BLACK_PUPPY:
			case NpcID.SAMOYED_WHITE_PUPPY:
			case NpcID.SAMOYED_YELLOW_PUPPY:
			case NpcID.SAMOYED_BLACK_PUPPY:
			case NpcID.SHEPARD_CHOCO_PUPPY:
			case NpcID.SHEPARD_MERLE_PUPPY:
			case NpcID.SHEPARD_TOASTED_PUPPY:
			case NpcID.SHIBA_TAN_PUPPY:
			case NpcID.SHIBA_WHITE_PUPPY:
			case NpcID.SHIBA_TOASTED_PUPPY:
			case NpcID.SPANIEL_RED_PUPPY:
			case NpcID.SPANIEL_WHITE_PUPPY:
			case NpcID.SPANIEL_BLACK_PUPPY:
			case NpcID.YORKIE_BROWN_PUPPY:
			case NpcID.YORKIE_WHITE_PUPPY:
			case NpcID.YORKIE_YELLOW_PUPPY:
			case NpcID.POH_LABRADOR_YELLOW:
			case NpcID.POH_LABRADOR_BROWN:
			case NpcID.POH_LABRADOR_BLACK:
			case NpcID.POH_CHIHUAHUA_TAN:
			case NpcID.POH_CHIHUAHUA_WHITE:
			case NpcID.POH_CHIHUAHUA_TOASTED:
			case NpcID.POH_COLLIE_CHOCO:
			case NpcID.POH_COLLIE_MERLE:
			case NpcID.POH_COLLIE_BW:
			case NpcID.POH_CORGI_TAN:
			case NpcID.POH_CORGI_YELLOW:
			case NpcID.POH_CORGI_TOASTED:
			case NpcID.POH_GREYHOUND_TAN:
			case NpcID.POH_GREYHOUND_GREY:
			case NpcID.POH_GREYHOUND_CREAM:
			case NpcID.POH_HUSKY_BW:
			case NpcID.POH_HUSKY_GREY:
			case NpcID.POH_HUSKY_CHOCO:
			case NpcID.POH_PUG_FAWN:
			case NpcID.POH_PUG_BROWN:
			case NpcID.POH_PUG_BLACK:
			case NpcID.POH_SAMOYED_WHITE:
			case NpcID.POH_SAMOYED_YELLOW:
			case NpcID.POH_SAMOYED_BLACK:
			case NpcID.POH_SHEPARD_CHOCO:
			case NpcID.POH_SHEPARD_MERLE:
			case NpcID.POH_SHEPARD_TOASTED:
			case NpcID.POH_SHIBA_TAN:
			case NpcID.POH_SHIBA_WHITE:
			case NpcID.POH_SHIBA_TOASTED:
			case NpcID.POH_SPANIEL_RED:
			case NpcID.POH_SPANIEL_WHITE:
			case NpcID.POH_SPANIEL_BLACK:
			case NpcID.POH_YORKIE_BROWN:
			case NpcID.POH_YORKIE_WHITE:
			case NpcID.POH_YORKIE_YELLOW:
				return true;
			default:
				return false;
		}
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

