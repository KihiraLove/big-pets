# Pet Resizer

Change the visual size of your currently following pet from **0% to 500%**.
The default is **100%** (normal size); **0%** hides the pet's model.

Enable **Resize all pets** to apply the same size to other players' followers and
POH menagerie pets in the scene, even when you have no follower of your own.
This option defaults to off. Changes are visible only on your client.

Two optional filters keep selected pets at normal size. Both default to off and
apply to your own follower as well as **Resize all pets**, including at 0%:

- **Filter cats and dogs**: all twelve pet dog breeds and their puppies, all cat
  colours and ages, Hell-kitten, Hellcat, Overgrown/Wily/Lazy hellcats, and Clockwork cat.
  Bloodhound and boss pets such as Hellpuppy remain resizable.
- **Filter quest and Event pets**: Spooky chair, Pet rock, Egg (Humphrey Dumphrey),
  all three fishbowl fish colours, Mayor of Catherby, and every Archibald variant.
  Cats and dogs are controlled separately. **Broav always remains resizable.**

Enable RuneLite's **GPU** plugin, then set **Pet size (%)** in Pet Resizer's settings.
Resizing uses a separate visual copy of your pet's current animated model. The
original pet's clickbox and menu options stay unchanged, even at 0% or 500%.
With **Resize all pets** off, other players' pets and POH pets are unaffected.

With GPU disabled or another renderer active, the pet retains its normal appearance.
Returning to 100% or disabling Pet Resizer also restores normal rendering.

## Rules

Checked on September 10, 2026: [Jagex's Third Party Client Guidelines](https://secure.runescape.com/m=news/third-party-client-guidelines?oldschool=1)
prohibit moving or resizing 3D click zones. This plugin changes only separate
visual models and retains the original NPCs for click detection and menu options.
Neither those guidelines nor [RuneLite's restrictions](https://github.com/runelite/runelite/wiki/Rejected-or-Rolled-Back-Features)
explicitly prohibit cosmetic resizing of pets. This is our interpretation of the
published rules, not explicit Jagex approval or a guarantee of Plugin Hub acceptance.

## Development testing

Run `./gradlew run` from this directory. For a Jagex account, follow RuneLite's
[Using Jagex Accounts](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts)
guide to log in to the development client.

- Try 0%, 1%, 50%, 100%, 200%, and 500% while standing and moving.
- Check the original clickbox and pet menu options at each size. Enlarged areas
  outside the original clickbox should not become clickable.
- Try pet transformations, animations, pickup and re-drop, and a different pet.
- Check teleporting, changing planes, hopping worlds, and logging out and back in.
- Toggle Pet Resizer and GPU; check that no duplicate or stale pet remains.
- With **Resize all pets** off, check that nearby players' pets stay normal size.
- Enable it while pets are already visible, then check other players' followers
  and several POH pets (including cats and transformed boss pets). Test with no
  follower of your own, and at 0%, 100%, and 500%.
- Check that POH servants, dungeon monsters, and ordinary NPCs stay unchanged.
- Turn the option off: only your own follower should stay resized. Check pets
  entering/leaving the scene and entering/leaving a house for stale copies.
- Toggle each filter while matching pets are already resized, including your own
  cat/dog follower. They should return to normal size immediately and resize again
  when the filter is turned off. Repeat at 0% to check they remain visible.
- With both filters on, verify Broav and boss/skilling pets still resize, and test
  puppy/adult dogs, cat/hellcat variants, fish colours and Archibald forms in a POH.

In-game behavior needs manual verification; a successful build or client launch
does not verify rendering or clickboxes.
