# Brief for the two Lycanites agents (read fully)

You are one of 2 agents porting **Lycanites Mobs 2.0.8.9** (the exact version in RLCraft 2.9.3) to NeoForge 1.21.1 for one player
(NukeClips), personal use only. World gen and Ice and Fire 1.7.1 are already ported in this repo (mod `rlgen/`; read `CLAUDE.md` and
`AUDIT.md` at the repo root, and skim how Ice and Fire was done: `rlgen/src/main/java/com/github/alexthe666/iceandfire`, `net/ilexiconn/llibrary`).

## THE rule (the user insists on it)
Port the OLD 1.12 code and behaviour with RLCraft's config values. The user wants RLCraft as it was: a normal Minecraft map with
really dangerous creatures. Modern versions/forks "modernise" and dumb things down; never copy behaviour, stats, AI, models or assets from
them. Use 1.21 code only for how to call the 1.21 API. Keep 1.12 quirks and difficulty: same numbers, same AI decisions and priorities,
same random calls, same models, textures (RLCraft ships the LycanitesMobs32X resource pack: use its textures where it has them), sounds.

## Setup
- `git reset --hard origin/claude/sweet-cannon-iqmaol` first (must contain 4e25306 "v0.10.0: Ice and Fire 1.7.1").
- Mod id `lycanitesmobs`, its own `@Mod` class and `[[mods]]` entry in neoforge.mods.toml, its own DeferredRegisters, keep the 1.12
  package/class names (`com.lycanitesmobs...`) so code ports nearly 1:1. Only ADD lines to shared files.
- Already in the repo from world gen: `com.lycanitesmobs.core.dungeon` (the dungeon generator, decompiled + adapted, used by rlgen) and
  `com.nukevscity.rlgen.lyc` (lycanites dungeon structures, LycFluids ooze/acid/poison/moglava as rlgen fluids without effects,
  FluidLakes, MoglavaLakes, LycBlocks mapping lycanites:X -> rlgen:X stand-in blocks). Keep world gen working; the server agent
  replaces the stand-ins with the real lycanitesmobs blocks/fluids (with their effects) and makes dungeon spawners spawn the real mobs.
- The shared 1.12 AI scheduler from Ice and Fire (`com.github.alexthe666.iceandfire.entity.ai.legacy`: EntityAITasks with tick rate 3,
  mutex bits, priorities, run in one flagless 1.21 goal via Legacy112.install; RandomPositionGenerator; 1.12 vanilla tasks) can be reused
  (Lycanites 2.0 uses 1.21-like goal classes of its own in com.lycanitesmobs.core.entity.goals; port those with 1.12 timing: tick rate 3,
  mutex bits; you may move the shared scheduler to a neutral package like `com.nukevscity.legacy.ai` if both mods need it — then update
  the Ice and Fire imports in the same commit and make sure IaF still builds).
- The 1.12 model bridge in llibrary (`net.ilexiconn.llibrary.client.model.legacy`: ModelBase/ModelRenderer + LegacyRender replacing
  GlStateManager) is reusable for any 1.12 Java models.

## Who does what (branches on GitHub)
Push your branch at least every 30 minutes and whenever you have something the other needs: `git push -u origin HEAD:<branch>`.
Usage limits can stop you at any moment: unpushed work can be lost.
- `lyc/server` — everything server side: the core (ObjectManager, config with RLCraft values from $S/pack/x/overrides/config/lycanitesmobs:
  general.cfg, spawning.cfg, mobevents.cfg, worldgen.cfg, itemlists.cfg and the JSON folders creatures/, creaturegroups/, creaturetypes/,
  elements/, equipment/, projectiles/, spawners/, mobevents/, items/, globalspawner.json, globalmobevent.json, mobeventschedule.json;
  RLCraft's JSON overrides merge over the jar's JSON exactly like 1.12), CreatureManager/CreatureInfo/stats/levels/variants/subspecies/
  elements/buffs, BaseCreatureEntity and its subclasses, EVERY creature's entity class and AI goals (all ~120 creatures), projectiles,
  spawners and the spawn system (triggers: sky, darkness, lunar, ore break, crop, tree, fishing, chaos...), mob events (blood moon, etc.
  with RLCraft's schedule), items (equipment/forging system, charges, soulgazer, soulstones, treats, foods...), blocks and fluids (ooze,
  acid, poison, moglava with their effects), effects/potions, ExtendedEntity/ExtendedPlayer (as attachments), network, commands, beastiary
  data, taming/summoning/familiars, and the world-gen integration described above.
- `lyc/client` — everything client side: the OBJ model system (ModelCreatureObj + the JSON part/animation definitions:
  ModelObjAnimation/ModelObjState, animation parts, layers, glow/emissive layers, eyes) converted to 1.21 PoseStack/VertexConsumer with
  exact 1.12 transforms and animation math, every creature's renderer and model, projectile renderers, particles, the GUIs (beastiary,
  creature/summoning/equipment screens), HUD bits, client events, keybinds, and the textures (jar + 32X pack), sounds client side.
  Merge `origin/lyc/server` regularly for the entity classes; until they exist, code against the 1.12 names with marked `// STUB(lyc/server)`
  stubs in their package and remove them once merged. Do not port what the other agent owns.

## Shared resources (read-only, outside your worktree)
S=/tmp/claude-0/-home-user-rlcft1211/981bfb13-b982-50dc-a2f7-80d7a2ebfa64/scratchpad ; P=$S/par
- Lycanites 2.0.8.9 decompiled with MCP names + assets/JSON: $S/dec/lyc (com/lycanitesmobs/..., assets/lycanitesmobs/...). Jar:
  $S/jars/lycanitesmobs-1.12.2-2.0.8.9.jar; 32x resource pack: $S/jars/LycanitesMobs32X.zip. When decompiled code looks wrong, check
  the bytecode with `javap -c -p` (decompilers make mistakes).
- 1.12.2 vanilla source (MCP names): $S/mc/src. 1.21.1 + NeoForge source: $S/mcsrc. RLCraft pack: $S/pack/x (configs in overrides/config,
  scripts in overrides/scripts).
- Region tools: `python3 $P/area.py $P <run>/world/region x0 y0 z0 x1 y1 z1`, $P/readmca.py.

## Machine rules
- Build ONLY with `$P/build.sh <your worktree>/rlgen [gradle args]`. Never call ./gradlew directly.
- Dedicated server tests ONLY with `$P/withserver.sh <your worktree>/rlgen <log> <test script> [seed]` (machine-wide lock, one server or
  client at a time; keep sessions under ~10 min; RCON via `python3 $P/rcon.py "cmd" ...`).
- Client tests: `$P/withclient.sh <your worktree>/rlgen <log> <world dir> <out prefix> [secs] [shots]` (real client on a virtual
  screen joined into a copy of <world dir>, e.g. <worktree>/rlgen/run/world made by withserver.sh, with a datapack whose
  #minecraft:tick function sets up the scene once a player exists, guarded by a scoreboard flag). Game window 854x480 in the middle of
  the 1280x720 screenshot. Look at the PNGs with the Read tool.
- Never start servers/clients any other way, never kill java/gradle/Xvfb processes you did not start through these scripts.
- Commit often, messages ending with exactly:
    Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
    Claude-Session: https://claude.ai/code/session_01UGB4AErBBhcoyLTW3XR5EJ
  Do not bump the version, do not edit CLAUDE.md/AUDIT.md. No model names in commits or code. Style: 3-space indent; comments cite the
  1.12 class being ported.

## Report (short, at the end)
What is ported and how faithful (anything approximate or missing), how you tested (server results, screenshots you looked at), stubs
left, branch and last pushed commit.
