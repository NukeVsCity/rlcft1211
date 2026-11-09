# Brief for every Ice and Fire agent (read fully)

You are one of 6 agents porting **Ice and Fire 1.7.1** (the exact version in RLCraft 2.9.3) and the parts of **LLibrary 1.7.20** it
needs to NeoForge 1.21.1, for one player (NukeClips), personal use only. World gen is already done (mod `rlgen/` in this repo; read
`CLAUDE.md` and `AUDIT.md` at the repo root for context).

## THE rule (the user insists on it)
Port the OLD 1.12 code and behaviour with RLCraft's config values. The modern Ice and Fire (CE / 1.20+) changed everything: AI,
stats, models, difficulty; its dragons are easy to kill and feel nothing like RLCraft. Never copy behaviour, stats, AI, models or
assets from a modern version. Use 1.21 code only for how to call the 1.21 API. Keep 1.12 quirks and difficulty: same numbers, same
AI decisions and goal priorities, same random calls, same Tabula models, textures, animations and sounds.

## Setup (first)
- `git reset --hard claude/sweet-cannon-iqmaol` (must contain commit 3c992e7 "Ice and Fire / llibrary mod skeletons").
- The skeleton exists: mod `iceandfire` (`com.github.alexthe666.iceandfire.IceAndFire`, shared DeferredRegisters in `IafRegistries`:
  BLOCKS, ITEMS, ENTITY_TYPES, BLOCK_ENTITY_TYPES, SOUND_EVENTS, MENUS, CREATIVE_TABS, PARTICLE_TYPES, MOB_EFFECTS,
  RECIPE_SERIALIZERS, ATTACHMENT_TYPES) and mod `llibrary` (`net.ilexiconn.llibrary.LLibrary`). Both are in neoforge.mods.toml.
- Keep the 1.12 package and class names (`com.github.alexthe666.iceandfire.entity.EntityDragonBase`, `net.ilexiconn.llibrary...`)
  so code ports nearly 1:1 and the agents' work fits together. Put your registrations in your own holder classes (e.g.
  `core/ModItems`, `entity/IafEntities...`) registering into `IafRegistries`, and hook them from `IceAndFire`'s constructor with one
  added line. Only add lines to shared files (IceAndFire.java, IafRegistries.java, neoforge.mods.toml, lang json, sounds.json): never
  reorder or reformat them.
- Assets: copy the 1.12 assets from the decompiled jar (textures, Tabula .tbl models, sounds, lang -> 1.21 json lang) into
  `rlgen/src/main/resources/assets/iceandfire/`. 1.12 item models/blockstates need converting to 1.21 format.

## Who does what (branches on GitHub, so you can merge each other's work)
Every agent pushes its branch to GitHub at least every ~45 minutes and whenever it has something others need:
`git push -u origin HEAD:<your branch>` — never push anywhere else.
- `iaf/llibrary` — LLibrary port (Tabula loader, AdvancedModelRenderer/AdvancedModelBase, ChainBuffer, Animator/Animation/
  IAnimatedEntity + sync, anything IaF references from net.ilexiconn).
- `iaf/core` — IaF config (RLCraft values), all items, blocks, block entities, materials/tools/armor, recipes, loot, sounds,
  creative tabs, network channel, the capabilities as attachments, IaF event handlers that are not mob AI.
- `iaf/dragons` — dragons, server side: EntityDragonBase/Fire/Ice, eggs, multipart, AI goals, flight, breath, growth, taming,
  sleeping, inventory/armor/GUI, drops, skeletons, the dragon-related events.
- `iaf/dragon-client` — dragons, client side: renderers, Tabula models + animations, layers (armor, eyes, riders, sleeping),
  breath FX/particles, egg render, dragon GUI screen.
- `iaf/mobs-land` — cyclops, gorgon (+ stone statue mechanic), troll, cockatrice, death worm, myrmex (all castes, hive AI,
  trading): server + client.
- `iaf/mobs-air-water` — hippogryph, amphithere, stymphalian bird, siren, hippocampus, sea serpent, pixie (+ pixie jar/house
  behaviour), snow villager if 1.7.1 has it: server + client.
Need something another agent owns (an item, an llibrary class, EntityDragonBase)? `git fetch origin <their branch> && git merge
origin/<their branch>` (resolve conflicts keeping both sides). If it is not there yet, write against the 1.12 class/method names
they will port and keep your build green with a temporary minimal stub in THEIR package that you clearly mark `// STUB(<branch>)`;
remove stubs once you merge the real thing. Do not port what another agent owns.

## Shared resources (read-only, outside your worktree)
S=/tmp/claude-0/-home-user-rlcft1211/981bfb13-b982-50dc-a2f7-80d7a2ebfa64/scratchpad ; P=$S/par
- Ice and Fire 1.7.1 decompiled with MCP names + assets: $S/dec/iaf (com/github/alexthe666/iceandfire/...). LLibrary: $S/dec/llib.
  Jars: $S/jars. When decompiled code looks wrong, check the bytecode with `javap -c -p` on the jar (decompilers make mistakes).
- 1.12.2 vanilla source (MCP names): $S/mc/src. 1.21.1 + NeoForge source: $S/mcsrc.
- RLCraft configs: $S/pack/x/overrides/config (ice_and_fire.cfg is the live one); CraftTweaker/LootTweaker scripts:
  $S/pack/x/overrides/scripts. RLCraft modlist/manifest: $S/pack/x.
- Region tools: `python3 $P/area.py $P <run>/world/region x0 y0 z0 x1 y1 z1` (block counts), $P/readmca.py.

## Machine rules (4 CPUs, 15 GB, shared by 6 agents)
- Build ONLY with `$P/build.sh <your worktree>/rlgen [gradle args]` (2 builds at a time). Never call ./gradlew directly.
- Dedicated server tests ONLY with `$P/withserver.sh <your worktree>/rlgen <log> <test script> [seed]` (machine-wide lock, one
  server or client at a time; it creates run/ if missing; keep sessions under ~10 min; RCON via `python3 $P/rcon.py "cmd" ...`:
  /summon, /data get entity, /attribute, /damage, /tick).
- Client tests (rendering, animations, GUIs): `$P/withclient.sh <your worktree>/rlgen <log> <world dir> <out prefix> [secs] [shots]`
  runs the real client on a virtual screen joined into a copy of <world dir> (make one with withserver.sh, it stays in
  <worktree>/rlgen/run/world) with a datapack in datapacks/<name>/ whose #minecraft:tick function runs your setup once when a player
  exists (guard with a scoreboard flag): tp @a, summon your mob in front of the camera, time set day, gamerule doMobSpawning false.
  The game window is 854x480 in the middle of the 1280x720 screenshot. Look at the PNGs with the Read tool and compare by eye with
  how the 1.12 model/texture should look.
- Never start servers/clients any other way, never kill java/gradle/Xvfb processes you did not start through these scripts.
- Commit often, messages ending with exactly:
    Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
    Claude-Session: https://claude.ai/code/session_01UGB4AErBBhcoyLTW3XR5EJ
  Do not bump the version, do not edit CLAUDE.md/AUDIT.md. No model names in commits or code.
- Style: 3-space indent like the repo; comments cite the 1.12 class being ported.

## Report (short, at the end)
What is ported and how faithful (list anything approximate or missing), how you tested (server results, screenshots you looked at),
which stubs remain, your branch and last pushed commit.
