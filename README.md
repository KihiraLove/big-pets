# Big Pets
Requires a GPU renderer: built-in **GPU**, **GPU Experimental**, **GPU Legacy**, or **117 HD**.
Change the visual size of your currently following pet from **0% to 500%**.
Normal size is **100%**, **0%** hides the pet's model.

**Options:**
- **Resize all pets** to apply the same size to other players' followers and POH menagerie pets.
- **Filter cats and dogs**: all twelve pet dog breeds and their puppies, all cat colours and ages, all hellcats, and Clockwork cat.
- **Filter quest and Event pets**: Spooky chair, Pet rock, Egg (Humphrey Dumphrey), all three fishbowl fish colours, Mayor of Catherby, and every Archibald variant. **(Except the Broav because he is cool)**

## Rendering
Big Pets draws a resized copy of the pet while retaining the original NPC for its clickbox and right-click menu. Software rendering leaves pets unchanged.

Built-in GPU, GPU Experimental, and 117 HD's zone renderer use RuneLite's object rendering callback to hide the original visual. GPU Legacy and 117 HD's legacy renderer use a forwarding adapter that skips the original visual but performs click detection with its unchanged model. No renderer fork or additional plugin dependency is required.

Alternative-renderer support is awaiting in-game verification. In particular, check 117 HD materials, lighting, and shadows: its NPC-specific model overrides may not apply to the replacement RuneLiteObject.

See [GPU compatibility notes](docs/gpu-compatibility.md) for the source review and test checklist.
