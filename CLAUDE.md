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

## Files in `ref/`
- `structures/`: all Recurrent Complex structures from his instance (`active/` is what generates).
- `config/`: Traverse, Dynamic Trees, Quark, Recurrent Complex, Defiled Lands, Chunk Animator, Lost Cities, Ice and Fire, Serene Seasons, Snow Real Magic, Better Foliage, Forge configs.
- `manifest.json`, `modlist.html`: exact mod versions in his RLCraft 2.9.3.

## Working with the user
- He wants builds and installs done by commands, never by Claude clicking around his screen.
- A mod change counts as done only once the built jar is installed in the game's mods folder. Install into a separate NeoForge 1.21.1 instance, never into his RLCraft 1.12.2 instance. If this session can't reach his computer, hand him the jar and the exact folder to put it in.
- Keep replies short and plain. He doesn't want long reports or spreadsheets.
- Personal use only. Most of these mods are all-rights-reserved, so don't publish anything.
