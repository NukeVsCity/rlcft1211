# RLCraft 1.12.2 world generation on NeoForge 1.21.1

You are continuing a project started in another session. Read this whole file before doing anything.

## What the user wants

The user (NukeClips) grew up on RLCraft 1.12.2 and wants to play it again on 1.21.1 NeoForge, for himself only (not for release). Modern community packs (Dregora, Ice and Fire CE, etc.) do not feel like it, because they only reconfigure modern mods. The rule for this project is:

**Port the OLD 1.12 code and behaviour. Use modern mods and NeoForge 1.21.1 code only as a reference for API calls.** Faithful beats clean. If the old generator had quirks (structures floating or half-buried, weird biome edges, cliffs), keep them.

Phase 1 is world generation only. He wants to see whether we can really get the same worlds back before anything else gets ported.

## What actually makes RLCraft "Default" worlds (verified from his instance)

His Default worlds have empty `generatorOptions` and generator `default`. There is no custom terrain generator. RTG is installed, but he does NOT want the RTG look. The look comes from:

1. **The vanilla 1.12.2 overworld generator** (`ChunkGeneratorOverworld`): 3D octave Perlin noise driven by each biome's `baseHeight` / `heightVariation`, with the 5x5 parabolic biome weighting. This was removed in 1.18, which is why modern worlds never look like this.
2. **Traverse (1.12, v1.6.0)**: all 24 biomes enabled (see `ref/config/traverse/traverse.cfg`). The extreme-height ones (Cliffs, Crag Cliffs, Canyon, Rocky Plateau, Arid Highland, Mountainous Desert, Glacier Spikes) pushed through the 1.12 noise produce the "random massive mountains".
3. **Defiled Lands (1.12, v1.4.3)**: 7 biomes, weights lowered in `ref/config/defiled_lands.cfg` (weight 1 instead of 3).
4. **Dynamic Trees (1.12, v0.9.29)**: all trees. Config is in `ref/config/dynamictrees.cfg` and `ref/config/dynamictrees/`.
5. **Recurrent Complex (1.4.8.4)** with 202 RLCraft structures in `ref/structures/active/`. These include the furnished "lively" village houses (P_VillageInn, P_VillageLibrary, P_VillageBlacksmith, P_VillageFarmhouse, ...), towns, Sky Castle, Cathedral of Madness, pirate ships, Mayan temples. Format: `.rcst` is a zip containing `structure.json` (placement rules) and `worldData.nbt` (blocks, 1.12 IDs + metadata).
6. **Quark worldgen** (underground biomes, crystal caves, stone revamp, tree variants, fairy rings, pirate ships). See `ref/config/quark.cfg`. Lower priority.
7. **Chunk Animator**: the "chunks rise out of the ground" effect. A 1.21.1 NeoForge version exists. Just install it in the test instance. No porting.

Also in the pack (not Phase 1): Battle Towers, Roguelike Dungeons, Doomlike Dungeons, Ruins, Lost Cities (as a separate dimension, `rlcraft` profile in `ref/config/lostcities/`), BetterNether.

## Test seeds (from his 1.12 saves)

| Save | Seed | Generator |
|---|---|---|
| Chill Hardcore | 192984777546635623 | default |
| NukeVsCity Hardcore | -3090796961423839953 | default |
| NukeVsCity New Hard | -3571440325882671706 | default |
| Nuke2_9_2 - Copy | -3090796961423839953 | RTG (same seed as NukeVsCity Hardcore; do not copy this look) |

The real goal: create a world with the same seed and get the same terrain as his 1.12 save. He can take screenshots at coordinates in his 1.12 saves for comparison.

## Plan (work in this order, ship a jar after each milestone)

Credits are limited (about $100 total). Do milestones in order. After each one, build the jar, tell the user what to test, and stop. Don't gold-plate.

### M1: Legacy terrain (the most important part)
- NeoForge 1.21.1 mod (ModDevGradle MDK, Java 21). Suggested mod id `rlgen`.
- Custom `ChunkGenerator` (registered codec) plus a world preset ("RLCraft Legacy") selectable on the Create World screen.
- Port 1.12.2 `ChunkGeneratorOverworld` exactly: `NoiseGeneratorOctaves` (minLimit, maxLimit, main, depth), default `ChunkGeneratorSettings` values (coordinateScale 684.412, heightScale 684.412, mainNoise scales 80/160/80, depthNoise scale 200, baseSize 8.5, stretchY 12, biomeDepthWeight/ScaleWeight 1, etc.), the 5x5 parabolic biome weighting, the 4x8x4 trilinear interpolation, sea level 63.
- Use a dimension_type override so the overworld is Y 0 to 255 with bedrock at 0, like 1.12. (Or keep 1.21 height and generate the 1.12 terrain in 0-255. Pick what's faithful and say which.)
- Java `Random` and the 1.12 `seed` handling must match bit-for-bit so the same seed gives the same terrain.
- Port 1.12 `MapGenCaves` and `MapGenRavine`, and the per-biome `genTerrainBlocks` surface rules (mesa bands, extreme hills gravel/stone, beaches, snow).
- Reference: decompile 1.12.2 with MCP `stable_39` mappings (github and maven are reachable from the cloud). Use NeoForge 1.21.1 sources only for how to plug a ChunkGenerator in.

### M2: Legacy biome layout
- Port the 1.12 `GenLayer` stack (Island, FuzzyZoom, AddIsland, Zoom, RemoveTooMuchOcean, AddSnow, EdgeCoolWarm/HeatIce/Special, AddMushroomIsland, DeepOcean, Biome, BiomeEdge, Hills, RiverInit, Smooth, RiverMix, Shore, VoronoiZoom) and a `BiomeSource` that uses it.
- Include Forge 1.12 `BiomeManager` weighted lists (WARM/COOL/ICY/DESERT), with Traverse and Defiled Lands added at the weights and categories they registered in 1.12 (read their 1.12 source on GitHub: TerraformersMC/Traverse 1.12 branch, and Defiled Lands 1.12).
- Validation: with modded biomes switched off, the layer output for a seed must match 1.12 vanilla exactly (compare against Amidst or a 1.12 biome map for the same seed).
- Create 1.21 biome JSONs for every 1.12 biome that has no 1.21 equivalent (Traverse x24, Defiled x7, 1.12 variants like mutated_extreme_hills). Keep in a code-side table each biome's 1.12 baseHeight, heightVariation, temperature, rainfall, colours, top/filler blocks. M1 reads these values.

### M3: Trees and decoration
- 1.12 `BiomeDecorator` order and counts (ores at 1.12 heights, lakes, dungeons, flowers, grass, reeds, pumpkins).
- Trees: Dynamic Trees has an official 1.21.1 NeoForge version. Hook its worldgen into our biomes and match the 1.12 species per biome (from the DT 1.12 default populator JSON). If that can't look like 1.12, say so and propose porting the 1.12 DT generator instead.

### M4: Structures (Recurrent Complex)
- Converter for `.rcst` to 1.21 structure data: 1.12 ID+meta to 1.21 blockstates (use Mojang's flattening table or the minecraft-data legacy mapping). Remap modded blocks (quark, charm, betternether, rustic, variedcommodities, inspirations) to the closest vanilla block. Lycanites/Ice and Fire spawners and blocks become placeholders or vanilla until those mods are ported.
- A small runtime placer in the mod that reads `structure.json` and copies Recurrent Complex's natural placement: biome categories, generationWeight, the "surface" placer rays, spawnShiftY, rotation/mirroring. Do NOT convert to jigsaw. The old placement (including the glitchy floating/buried results) is part of the feel.
- `.rcig` inventory generators become loot tables.

### Later (not Phase 1, only if asked)
Quark worldgen bits, BetterNether, other dungeon mods, mobs (Lycanites, Ice and Fire 1.12 dragon AI with RLCraft's config: dragon attack damage 30), combat, everything else.

## User's priorities after M4 (session 3)
He wants the *visual feel* back: everything that was visibly generated in the world. Order:
1. 1.12 decoration (BiomeDecorator ores/flowers/grass/lakes/dungeons/snow, biome extras, Traverse/Defiled decorate).
2. 1.12 villages (MapGenVillage) with the RC "lively" house pieces; RC mazes.
3. Battle Towers, Roguelike Dungeons, Doomlike Dungeons, Ruins mod (all from his RLCraft 2.9.3 configs).
4. Quark world gen (underground biomes, crystal caves, stone revamp, etc.).
Dropped: BetterNether (he prefers the 1.21 nether), Lost Cities (waste of time). Mobs etc. only later if asked.

## Files in `ref/`
- `structures/`: all Recurrent Complex structures from his instance (`active/` is what generates).
- `config/`: Traverse, Dynamic Trees, Quark, Recurrent Complex, Defiled Lands, Chunk Animator, Lost Cities, Ice and Fire, Serene Seasons, Snow Real Magic, Better Foliage, Forge configs.
- `manifest.json`, `modlist.html`: exact mod versions in his RLCraft 2.9.3.

## Working with the user
- He wants builds and installs done by commands, never by Claude clicking around his screen.
- A mod change counts as done only once the built jar is installed in the game's mods folder. Install into a separate NeoForge 1.21.1 instance, never into his RLCraft 1.12.2 instance. If this session can't reach his computer, hand him the jar and the exact folder to put it in.
- Keep replies short and plain. He doesn't want long reports or spreadsheets.
- Personal use only. Most of these mods are all-rights-reserved, so don't publish anything.

## Progress log

### Session 1 (M1 + M2 terrain/biomes)
- Mod lives in `rlgen/` (ModDevGradle 2.0.148, NeoForge 21.1.257). Build: `cd rlgen && ./gradlew build` -> `build/libs/rlgen-<ver>.jar`.
- `com.nukevscity.rlgen.legacy` is a pure-Java copy of the 1.12.2 code, decompiled from the real 1.12.2 client jar with MCP stable_39 names (Vineflower, SpecialSource) plus the Forge 1.12.x patches (GenLayerBiome weighted lists, MapGenCaves/Ravine digBlock/isTopBlock). No MC 1.21 imports in there.
  - noise/, layer/ (whole GenLayer stack), gen/ (caves, ravines), biome/ (Biome + vanilla subclasses + `ModBiomes` for Traverse/Defiled, `Biomes` registry), `LegacyGenerator` (= ChunkGeneratorOverworld.generateChunk), `LegacyBiomeProvider`.
  - Biome lists order: vanilla, Defiled Lands (added in registry event, weight 1 each from cfg), Traverse (added at init/server start). See `BiomeLists.rlcraft()`.
  - Faithful quirks kept: Forge cave isTopBlock bug (never regrows grass), mutable Biome.topBlock, mesa pillar noise seeded with 0, unseeded hills river-zoom layers.
- Verified: `src/oracle/.../Oracle.java` runs real 1.12.2 classes (tools/client-mcp-1.12.2.jar + 1.12.2 libraries, not committed) next to the port: GenLayer output and 200 chunks match exactly (vanilla lists).
- 1.21 side (`world/`): `LegacyChunkGenerator` (rlgen:legacy) fills chunks from the port, no 1.21 carvers/surface; `LegacyBiomeSource` maps 1.12 biomes to 1.21 keys (`LegacyBiomeMap`); seed comes from `createState`. World preset `rlgen:rlcraft_legacy` (and `rlgen:vanilla_1_12`) on the normal 1.21 overworld dimension type (-64..320), user's choice.
- Session 2: the 1.12 terrain fills Y 0..255 unchanged; 1.12 bedrock (Y 0..4) becomes stone, and stone below Y 8 gets the 1.21 deepslate gradient. Y -64..-1 is a vanilla NoiseBasedChunkGenerator limited by `rlgen:underground` noise settings (min_y -64, height 64), so 1.21 noise caves/aquifers/geodes live there; below Y 0 the biome source returns deep_dark/lush_caves/dripstone_caves where the vanilla multi-noise source would. 1.21 carvers are not run (they would cut the 1.12 terrain). Test servers: use RCON (`run/server.properties`), gradle doesn't forward stdin. Never `pkill -f` with a pattern that also matches your own shell command.
- Traverse/Defiled biomes are rlgen:* biome JSONs built from vanilla templates (decoration is still 1.21 vanilla features until M3). Blocks rlgen:red_rock, blue_rock, grass/dirt/sand/stone_defiled with the old textures.
- Session 2 (M3 trees): every 1.12 biome is its own rlgen:<name> biome (generated by `rlgen/scripts/gen_worldgen_data.py` from 1.21 templates, with 1.12 climate and the template's biome tags so 1.21 structures still spawn). Trees = Dynamic Trees 1.21.1 (required dependency, `maven.modrinth:dynamictrees:1.7.2`; for runServer copy the jar to `run/mods`). `trees/rlgen/world_gen/default.json` replicates the DT 1.12 default populator (by 1.12 BiomeDictionary types and treesPerChunk) + DT Traverse + DT Defiled Lands rules per biome; `feature_cancellers.json` removes 1.21 trees in rlgen biomes. Custom species fir, autumn_red/orange/yellow/brown, mini_jungle, oak_shrub, tenebra use the original 1.12 JoCodes from the addon jars; 1.12 jungle/mega_jungle/tall_birch JoCodes override DT's (others are identical). `NeoForgeRegistryHandler.setup("rlgen")` is required for DT to create our branch/leaves blocks.
- Session 3 (M4 Recurrent Complex, v0.4.0):
  - `rlgen/scripts/convert_rc.py <rc jar structures/active> <ref/structures/active> <minecraft-data dir> rlgen/src/main/resources` converts all 386 active structures (RC jar built-ins + instance, instance wins by name) to `data/rlgen/rc/structures/<id>.nbt` (palette with 1.12 id/meta + 1.21 state, blocks, tiles with 1.21 BE nbt + RC loot tags + world scripts, entities), `loot/<inventoryGeneratorID>.json`, presets, categories, `legacy_ids.json`. Needs minecraft-data legacy.json/112_*.json/1211_blocks.json (pc/common, pc/1.12, pc/1.21.1). Modded blocks/items/mobs map to vanilla (MOD_BLOCKS, ITEM_MOD, MOD_MOBS); unknown ones become air/dropped.
  - Runtime `com.nukevscity.rlgen.rc` = port of RC 1.4.8 natural generation: `RcExpr` (RC expressions), `RcEnv`/`BiomeDict` (1.12 BiomeDictionary types incl. Traverse/Defiled), `RcPlacer` (GenericPlacer, limit rays, match factor, LineSelection as BitSets), `RcTransformers` (multi, natural, naturalAir, negativeSpace, replace/replaceAll, propertyReplace, ensureBlocks, ruins; villagereplace is a no-op outside villages), `RcGenerator` (populate random of 1.12 ChunkGeneratorOverworld, MixingStructureSelector, StructureGenerator seeds, prepare, chunk-clipped spawn, world scripts: command summon/setblock, strucGen simple + structure lists), `RcLoot` (multi/single loot tags, vanilla chests/* tables), `BlurredValueField` (IvToolkit).
  - 1.21 side: structure `rlgen:recurrent_complex` + structure_set (random_spread spacing 1) = every chunk asks "what would RC 1.12 generate when populating this chunk"; planning reads the 1.12 terrain from `LegacyChunkGenerator.oracle()` (no trees), each planned structure is an `RcPiece` that re-prepares deterministically (seed + oracle) and places itself chunk by chunk, then fixes fence/pane/stair shapes and pairs chests. Structures not exact per seed (1.12 iterated categories in identity-hash order), behaviour is.
  - Not done: decoration/sapling types (trees: baseWeight_tree=0 in reccomplex.cfg anyway), overlap check only within one chunk, spawn-distance check. `/rlgen rc <id>` places a structure at your position (testing). Loot keys that name a component file instead of an inventoryGeneratorID give empty chests, exactly like 1.12 (e.g. P_IslandCityFood).
  - Dev server: `-Drlgen.rcdebug=true` (set in build.gradle runs) logs every planned/failed structure.
- Session 3 continued (v0.5.0):
  - Decoration `com.nukevscity.rlgen.deco`: 1.12 populate + BiomeDecorator + vanilla/Traverse/Defiled biome decorate, split into placed features rlgen:legacy_lakes (LAKES), legacy_ores (UNDERGROUND_ORES), legacy_plants (TOP_LAYER_MODIFICATION) sharing one java Random per chunk (ThreadLocal), so Dynamic Trees grows between ores and plants like 1.12. Quark's revamp stone gen means no granite/diorite/andesite. Below Y 0: rlgen:underground/* ores and geodes. Freeze (ice/snow) at the end.
  - Villages `com.nukevscity.rlgen.village`: StructureVillagePieces mechanically ported (scratch script port_village.py: 1.12 names -> 1.21 StructurePiece API) + Forge patches; `LegacyVillageStructure` (rlgen:village, structure_set spacing 32 / separation 8 / salt 10387312 = 1.12 grid), biome check on the 1:4 layer, 1.12 MapGenBase random. RC "vanilla" generations become `RcVillagePiece` (handlers alphabetical, 1.12 used class identity-hash order). Traverse village biomes + GetVillageBlockID swaps (VillageBlocks), villagers with 1.12 professions (xp 1 so they keep it). No lakes in chunks a village reached. 1.21 jigsaw village tags removed from rlgen biomes. Plants from neighbour decoration count as air for village paths (1.12 built villages before plants).
  - Mazes `rc/maze`: IvToolkit maze components + RC WorldScriptMazeGenerator/WorldGenMaze/ReachabilityStrategy ported (Maze, MazeRc, MazeScript). Converter keeps "mazeGen" scripts as {type: maze, maze: <RC compound>}. Decompiler bug seen: MazePassage.normalize is `new MazeRoom(new int[dims])` (checked with javap), so check bytecode when decompiled code looks broken. Maze component order: alphabetical structure id.
  - Next (user order): Battle Towers, Roguelike Dungeons, Doomlike Dungeons, Ruins; then Quark world gen.
- Session 4 (v0.6.0). User dropped BetterNether and Lost Cities ("the feeling back: everything that was visually there").
  - RLCraft 2.9.3 pack configs are in the CurseForge zip (`https://mediafilez.forgecdn.net/files/4612/979/RLCraft%201.12.2%20-%20Release%20v2.9.3.zip`, overrides/config); the copies used are in resources.
  - Pattern for 1.12 IWorldGenerator mods: a structure with structure_set spacing 1 (one start per chunk, chunk-sized pieces); the IWorldGenerator chunk random is `new Random(seed)`, xSeed = nextLong() >> 3 (Forge's `>> 2 + 1`), chunkSeed = (xSeed*cx + zSeed*cz) ^ seed. 1.12 terrain for planning comes from `LegacyChunkGenerator.legacyState/legacyBiome`.
  - Battle Towers `towers/` (AS_WorldGenTower port, battletowers.cfg loot).
  - Roguelike Dungeons: Fnar's source kept in its packages (`greymerk`, `com.github.fnar`), 1.21 bits in `roguelike/` (BufferedEditor over the 1.12 terrain, split by chunk). Item/block flattening via `legacy/LegacyStates` (data/rlgen/legacy/{blocks,items}.json from `scripts/gen_legacy_tables.py`).
  - Doomlike Dungeons: `jaredbgreat.dldungeons` with `compat/World` bound to the chunk's WorldGenLevel; built per populate area like 1.12 (`doomlike/`). RLCraft config hard coded in ConfigHandler; quirk kept: Dungeon.applyTheme tests announceCommands, so every dungeon is HUGE. Lycanites/Ice and Fire spawner mobs get zombie/wither skeleton stand-ins.
  - Ruins: `atomicstryker.ruins.common` (templates parsed from data/rlgen/ruins, blocks are 1.12 names flattened at placement), `ruins/` decides each try once (oracle terrain) and saves decisions in SavedData `rlgen_ruins` so ruins are never half built; min distances are exploration-order dependent like 1.12.
  - Quark `quark/`: Revamp stone gen (granite/diorite/andesite + new rlgen:marble/limestone/jasper/slate with Quark textures, RLCraft cluster settings, run before dirt ores), fairy rings, big mushrooms in swamps.
  - Not done: Quark pirate ships (1 per 4000 ocean chunks, needs 1.12 structure-template converter), varied dungeons, underground biomes, crystal caves, speleothems, cave roots, monster boxes, clay underground, buried treasure. Mob equipment profiles of Roguelike (EntityProfiler) not ported.
- Session 4b (v0.6.1): user compared seed -6377475158484458490 with his 1.12 save; at -233,-3930 1.12 had Traverse Temperate Rainforest, we had mutated_taiga. Cause: Ice and Fire adds its Glacier biome to the COOL list (weight 4, "Glacier Spawn Weight") in the biome registry event, after Defiled Lands and before Traverse (Traverse re-adds its biomes at server start). Added `iceandfire:glacier` (legacy id 71, BiomeIceAndFireGlacier: base 2.125, variation 0.025, snow on packed ice) -> rlgen:iceandfire_glacier. Biome list order is now vanilla, Defiled, IaF glacier, Traverse. Checked 3 spots on that seed match his 1.12 save (extreme hills knob -516,-4782; plains village -444,-4584; rainforest mountain -233,-3930). Probe harness for biome-list variants: scratch `harness/h/Probe.java` (run with build/classes/java/main on the classpath).
