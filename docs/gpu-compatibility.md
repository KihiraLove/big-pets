# GPU compatibility

## Source review

Reviewed on 2026-09-12:

| Renderer | Source | Original visual hiding |
| --- | --- | --- |
| Built-in GPU | Local RuneLite source; compiled against RuneLite 1.12.38 | `RenderCallbackManager.drawObject` in `drawTemp` / `drawDynamic` |
| GPU Experimental | [gpu-experimental](https://github.com/KihiraLove/runelite-plugins/tree/bc5b3f97a37920123fbe5d135fb57f6043fe043c) | Same object callback as built-in GPU |
| GPU Legacy | [gpu-legacy](https://github.com/KihiraLove/runelite-plugins/tree/08c784abc517f4b1f2e6fd9271a52142f9312542) | Legacy `DrawCallbacks.draw`; no object callback |
| 117 HD, zone renderer | Local RLHD commit `7a3c679547c3525a983ff3f15953ec626324b60c` | `ModelStreamingManager.drawTemp` calls the object callback |
| 117 HD, legacy renderer | Same RLHD commit | `LegacyRenderer.draw`; no object callback |

The old `instanceof GpuPlugin` requirement excluded all three alternative plugins before scaling was attempted. Replacing that check alone is insufficient: the legacy paths would render the original pet alongside the resized copy.

## Implementation

- Require `Client.isGpu()` and an installed draw callback.
- Keep the existing scaled-copy and object-callback approach for modern renderers.
- Wrap alternative renderers in `PetDrawCallbacks`. For legacy draw calls involving a replaced NPC, calculate the original model bounds and call `Client.checkClickbox` with the original model, projection, orientation, coordinates, and hash. Skip uploading that original visual; the replacement RuneLiteObject is drawn normally.
- Forward all other callbacks, including default methods and render-thread overloads. Lombok generates these forwards at compile time; the plugin uses no reflection or third-party renderer classes.
- Keep the built-in GPU callback unchanged. Racecar currently checks its concrete type, so wrapping it would break racecar even on built-in GPU.
- Reuse the adapter across frames. Deactivate it when displaced, and restore its delegate only if it still owns the client's callback. Never reinstall a stopped renderer over a newly selected renderer.

This is a compatibility prototype, not an in-game verification. Legacy picking now calls the public client picking API directly rather than passing through renderer-specific viewport/area culling. Test camera edges and occlusion carefully. HD's NPC-specific material overrides may also differ for a replacement RuneLiteObject.

## Manual verification

Run each available renderer separately, including both 117 HD renderer modes where available. Use the installed Plugin Hub versions to validate actual compatibility; fork source inspection alone does not establish that installed versions behave identically.

1. With your own pet following, try 50%, 200%, and 500%. Check walking, turning, idle animations, and camera rotation. There should be exactly one pet visual.
2. Try 0%: the visual disappears, while the original pet location retains its normal menu/clickbox. At 100%, the normal visual returns. The enlarged area outside the original clickbox should not become clickable.
3. Test the original clickbox near screen edges, behind the camera, behind scenery, and on different floors. Check that unrelated NPCs retain normal rendering and menus.
4. Enable Resize all pets; check another player's pet and POH pets. Exercise both filters, including 0% with a filtered pet.
5. Switch between GPU renderers and software mode with Big Pets enabled. Toggle Big Pets off/on, dismiss or change pets, teleport, and log out/in. Check for duplicates, stale visuals, and rendering errors.
6. In 117 HD, inspect textured/glowing pets, materials, lighting, and shadows. Try model caching and renderer mode changes where exposed by that version.
7. With built-in GPU, confirm the existing racecar plugin still recognizes the renderer. Racecar's alternative-renderer support is a later task.

Use `./gradlew run` from the Big Pets root to launch the development client. For a Jagex Account, follow RuneLite's [Using Jagex Accounts](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts) instructions.

## Applying the approach to racecar later

Racecar has the same built-in-renderer gate and object-hiding hook. Once the above tests pass, the adapter can be adapted for racecar's hidden source follower. Test both plugins together: callback adapters must forward to each other, displaced adapters must be inert, and shutdown must not overwrite another plugin's callback. This change does not modify racecar.
