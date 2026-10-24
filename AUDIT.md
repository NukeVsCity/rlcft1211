# RLCraft 2.9.3 world generation audit

Source: all 187 jars of the pack manifest (CurseForge), scanned by bytecode for world-gen hooks
(BiomeManager.addBiome/removeBiome, IWorldGenerator, terraingen events, village handlers, MapGenStructureIO,
WorldType/WorldProvider, references to ChunkGeneratorOverworld/GenLayer/BiomeDecorator/MapGenCaves, mixins into
world/gen), then each hit checked against RLCraft's config. Scanner: `rlgen/scripts/scanjar.py <jar dir> <out.json>` (jars: manifest projectID/fileID via curseforge.com/api/v1/mods/<p>/files/<f>/download). Re-run it when anything changes.

Status: DONE = ported; PART = partly ported; TODO = to port; SKIP = deliberately not ported (reason); NONE = hook found but no effect on generated worlds.

## Biome layout (everything that touches the GenLayer biome lists)
| Mod | What | Status |
|---|---|---|
| Vanilla/Forge | WARM/COOL/ICY/DESERT lists | DONE (verified vs real 1.12) |
| Defiled Lands | 7 biomes, weight 1 (registry event) | DONE |
| Ice and Fire 1.7.1 | Glacier, COOL weight 4 (registry event, after Defiled) | DONE (v0.6.1) |
| Traverse 1.6.0 | 24 biomes, re-added at server start (last) | DONE |
| SR Parasites | biome "infested" exists but `SRPBiomes.register` is never called: not in the lists (spreads in game only) | NONE |
| RTG | own WorldType only; Default worlds untouched | NONE |
| Lost Cities | own dimension | SKIP (user dropped) |
| BiomeManager.removeBiome | only Traverse (its own re-add) | DONE |

## Terrain / carving
| Mod | What | Status |
|---|---|---|
| Vanilla 1.12 ChunkGeneratorOverworld | noise terrain | DONE |
| **Better Caves 2.0.4** | replaces vanilla caves + ravines (InitMapGenEvent): type 1/2 caves, surface caves, vanilla caves (density 14), caverns, water regions, flattened bedrock off. Config bettercaves-1_12_2.cfg | DONE: original source (tag v1.12.2-2.0.4) in `com.yungnickyoung.minecraft.bettercaves`, MC types swapped for `mc/` stand-ins, RLCraft values in `config/RLCraftConfig` |
| Better Mineshafts | replaces mineshafts | TODO |

## Decoration / per-chunk generators (visible)
| Mod | What | Status |
|---|---|---|
| Vanilla BiomeDecorator + Traverse/Defiled decorate | | DONE |
| Dynamic Trees (+ Traverse, Defiled add-ons) | trees | DONE |
| Quark | revamp stone, fairy rings, swamp mushrooms | DONE |
| Quark | underground biomes, crystal caves, speleothems, cave roots, monster boxes, clay, varied dungeons, pirate ships, buried treasure | TODO |
| **No Tree Punching** | loose rocks on the ground everywhere (WorldGenRocks, frequency 8, Quark marble/limestone rocks) | DONE (`ntp/`, rlgen:loose_rock_*) |
| **Rustic** | herbs 0.04 (x8, surface + cave), wildberries 0.04 (x4); slate and beehives off; olive (0.02) / ironwood (0.01) are Dynamic Trees species spliced into the DT populator (weights 10 and 5 per 1000) | TODO (low: rare) |
| **Snow! Real Magic!** | ReplaceSnowWorldGen: snow layers inside grass/flowers in snowy biomes | TODO |
| **Coral Reef** | coral reefs and rocks in oceans; biome list matches only "ocean" (quirk kept) | DONE (`coral/`, rlgen:reef, reef_rock, coral_1..6) |
| **Charm** | VillageDecorations (inside/outside village decoration), AbandonedCrates; MoreVillageBiomes off; NetherGoldDeposits (nether, skip) | TODO |
| **Waystones** | waystones in villages (1.0) and anywhere (10/10000 per chunk), mossy | TODO |
| Realistic Torches | generateLitTorches=false: torches in generated chunks become unlit torches | TODO (small) |
| Lycanites Mobs | dungeons enabled (distance 45 chunks, towers), fluid lakes (check config) | TODO |
| Ice and Fire 1.7.1 | StructureGenerator (weight 0): fire/ice dragon roosts and dens, cyclops caves, siren islands, silver/sapphire ore, fire/frost lilies; mobs and dragon skeletons not spawned (their random draws kept) | DONE (`iaf/`, planned on 1.12 terrain, replayed after each populate area) |
| Ice and Fire 1.7.1 | gorgon temples (template converted by scripts/convert_template112.py), myrmex hives (600 apart, saved in rlgen_myrmex) | DONE |
| Ice and Fire 1.7.1 | pixie villages (pixie house as a block model), snow villages | DONE (`iaf/IafVillage`) |
| Recurrent Complex | all active structures | DONE |
| Battle Towers / Roguelike / Doomlike / Ruins | | DONE |
| Bountiful | bounty board in villages | TODO (small) |
| Serene Seasons | season-dependent snow/colours at populate | TODO (install 1.21 version) |
| RLTweaker | RecurrentHandler (RC populate), LostCities handler: check config | TODO (check) |
| Locks | locks on generated chests (game mechanic, needs the mod) | SKIP for now |
| RLArtifacts | underground chests: chance 0 in config | NONE |
| YUNG's Law | ore hiding disabled in config | NONE |
| BetterNether, Charm nether gold | nether | SKIP (user keeps 1.21 nether) |
| SilentLib, FoamFix, Phosphor, RenderLib, RLMixins, BetterBiomeBlend, EntityCulling | libraries / rendering / chunk IO | NONE |
| Chunk Animator | client effect | install 1.21 version |

## Blocks inside structures
Modded blocks in RC / Roguelike / Ruins / village structures map to vanilla look-alikes now (Quark, Charm, Rustic, Inspirations,
Varied Commodities, Ice and Fire, Lycanites, ...). TODO: real blocks for the most used ones (count usage first).

## Checker (run after every milestone)
1. Re-run the jar scan; every jar with a hook must have a row above.
2. Seed spot checks against the user's 1.12 screenshots (biome name + height), log them in CLAUDE.md.
3. For each TODO turned DONE: dev server, forceload, top/side render, confirm no errors in the log.
