# Mortal Boons

A personal NeoForge mod (Minecraft 1.21.1, NeoForge 21.1.235) that adds roguelike boons:
players spend experience and items to roll a random boon with a random tier. Boons are
powerful but mortal — **die and you lose all of them**.

## How it works

- **Rolling**: a roll picks a boon at random (weighted, excluding boons the player already
  holds), then picks a tier uniformly within that boon's allowed range.
- **Tiers**: 1–4, displayed as Iron / Gold / Diamond / Netherite. Higher tier = stronger
  effect. Not every boon spans all tiers; some are fixed-tier (Guardian Angel is tier 4 only).
- **Slots**: a player holds up to 3 distinct boons. The same boon id can never be held twice.
- **Boon Altar**: a vertically stackable block (1–3 high); stack height gates progression —
  1 block rolls boons (slot cap 1), 2 unlocks reforge (tier reroll, cap 2), 3 unlocks reroll
  (replace a boon, cap 3, requires full slots by default). Shrinking an altar never removes
  held boons.
- **Abilities**: boons can grant active/passive/triggered abilities from the sibling
  `player_abilities` mod. This mod owns the mapping and calls `AbilityAPI.grant`/`revoke`
  directly (optional compat, PmmoCompat-style, no event bridge). On death-clone we revoke all
  grants under our source id, because `player_abilities`'s attachment copies on death and
  ours deliberately does not.
- **Cost & pacing**: each successive roll costs more (XP and/or items) and rolls share a
  cooldown. All configurable.
- **Death**: wipes every held boon. This is the core mechanic — implemented by registering
  the player data attachment *without* `.copyOnDeath()`.
- **Deferred (post-MVP)**: HUD/screens, datapack boon definitions, tooltip polish.

Key design docs in `claude_reference/`:
- `implementation_plan.md` — the phased build plan. Follow it; update it when the design changes.
- `curseforge_markdown.md` — player-facing mod description.

## Reference project

`/Users/nate/Workspace/minecraft/personal/player_abilities` is the sibling project whose
conventions and patterns this mod follows. Reused patterns: `LeveledValue`, `AttributeGrant`,
attachment + codec persistence, lifecycle event handlers, networking registration.

## Project info

- Mod id: `mortal_boons` (underscore — hyphens are illegal in mod ids)
- Base package / group: `net.silvertide.mortal_boons`
- Main mod class: `src/main/java/net/silvertide/mortal_boons/MortalBoons.java`
- Mod metadata: `src/main/templates/META-INF/neoforge.mods.toml` (values are substituted from `gradle.properties`)

## Conventions

- All mod id, name, version, and dependency ranges live in `gradle.properties` — change them there, not in mods.toml.
- Register content with `DeferredRegister` on the mod event bus from the `MortalBoons` constructor.
- Assets go under `src/main/resources/assets/mortal_boons/`.
- Boons are datapack-defined JSON loaded by a `SimpleJsonResourceReloadListener` at
  `data/<namespace>/mortal_boons/boons/<name>.json` (boon id = `<namespace>:<name>`).
  Built-ins ship as a **removable built-in datapack** ("Mortal Boons Default Boons",
  enabled by default) under `builtin_data_packs/default_boons/`, registered via
  `AddPackFindersEvent` — same pattern as the artifactory mod. Boons are pure data
  (attribute grants + optional ability grants) — no behavioral boon code in this mod.

## Build & run

- `./gradlew build` — build the mod jar (output in `build/libs/`).
- `./gradlew runClient` / `./gradlew runServer` — launch a dev instance.

---

# Reusable Engineering Standards

The sections below are project-agnostic. Copy this block (everything below the `---` separator) into any other project's `CLAUDE.md` unchanged to apply the same standards there.

## Code Style

**Never write comments.** No inline `//` comments, no `/* */` blocks, no javadoc, no leading explanatory headers on methods or fields. Code must be self-documenting through naming alone.

- Variable names describe what the value *is* (e.g. `armorCoveragePercent`, not `acp` with a comment).
- Method names describe what they *do* and under what conditions (e.g. `applyMultiplierIfAttackerIsPlayer`, not `applyBonus` with a comment explaining the player check).
- Extract a well-named helper method instead of writing a comment to explain a block.
- Constants get descriptive names that encode their meaning and unit (e.g. `KNIGHTMETAL_BONUS_DAMAGE_AT_FULL_ARMOR`, not `MAX` with a `// 2.0 vs fully-armored target` comment).
- If a name would need a comment to explain it, rename it until it doesn't.

Existing files may still contain comments and javadoc — leave them in place when editing unrelated code, but do not add new ones and prefer to delete obsolete ones when touching the surrounding code.

**Never leave dead code.** No unused methods, fields, classes, parameters, or imports. No "escape hatch" or "just in case" code. No commented-out blocks. If it's not called, delete it — the git history is the archive.

## Code Review

When asked to review code, do a "pass", check for issues, or otherwise audit a recent change, do **two** passes in order:

1. **Self-audit first.** Read the diff yourself. Fix the obvious — dead code, comments, naming, anything that violates the Code Style rules above. Report findings.
2. **Then spawn an independent reviewer** via the `/code-review` skill or a fresh agent. Give it only the diff and the goal, no context about why you made the choices you did. That catches the bugs you would otherwise rationalize away.
3. **Write to audit.md** Write the findings from the audit to audit.md in the claude_reference folder so we can checkmark them as we complete them.
4. **Project Problems check** by running a whole project search in the problems / project tab. Have the user download and put this file into the claude_reference/problems to check. Ask before deploys if we should do this.
   Don't skip step 2 because step 1 looked clean — the value of the independent reviewer is exactly that it doesn't share your blind spots.

## Version Control

**The user handles commits in git.** Never run `git add`, `git commit`, or `git push` — and don't suggest doing so — unless the user explicitly asks. Wrap up work by reporting what changed; staging and pushing are the user's job.
