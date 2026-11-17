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
| SR Parasites | IWorldGenerator is empty; biome "infested" exists but `SRPBiomes.register` is never called: not in the lists (spreads in game only) | NONE |
| RTG | own WorldType only; Default worlds untouched | NONE |
| Lost Cities | own dimension | SKIP (user dropped) |
| BiomeManager.removeBiome | only Traverse (its own re-add) | DONE |

## Terrain / carving
| Mod | What | Status |
|---|---|---|
| Vanilla 1.12 ChunkGeneratorOverworld | noise terrain | DONE |
| **Better Caves 2.0.4** | replaces vanilla caves + ravines (InitMapGenEvent): type 1/2 caves, surface caves, vanilla caves (density 14), caverns, water regions, flattened bedrock off. Config bettercaves-1_12_2.cfg | DONE: original source (tag v1.12.2-2.0.4) in `com.yungnickyoung.minecraft.bettercaves`, MC types swapped for `mc/` stand-ins, RLCraft values in `config/RLCraftConfig` |
| Better Mineshafts 2.2.1 | replaces mineshafts (spawn rate 0.003, RLCraft variants.json) | DONE: original source in `com.yungnickyoung.minecraft.bettermineshafts` with `mc/` stand-ins (scripts/port_bms.py, then hand fixes), `bms/` places each 1.12 populate area with its populate random; 1.21 mineshafts removed from rlgen biomes |

## Decoration / per-chunk generators (visible)
| Mod | What | Status |
|---|---|---|
| Vanilla BiomeDecorator + Traverse/Defiled decorate | | DONE |
| Dynamic Trees (+ Traverse, Defiled add-ons) | trees | DONE |
| Quark | revamp stone, fairy rings, swamp mushrooms | DONE |
| Quark | underground biomes (9, DIRT ore event), varied dungeons (DUNGEON populate event, populate random), clay, blossom trees, monster boxes, speleothems, cave roots, archaeologist houses (IWorldGenerators by weight); crystal caves and pirate ships planned with Ice and Fire's | DONE (`quark/`, `iaf/QuarkPlanned`); monster boxes don't spawn mobs yet, pirates/archaeologist not spawned |
| Quark | buried treasure (treasure maps in loot, chest made when a player holds the map) | SKIP (gameplay, not world gen) |
| **No Tree Punching** | loose rocks on the ground everywhere (WorldGenRocks, frequency 8, Quark marble/limestone rocks) | DONE (`ntp/`, rlgen:loose_rock_*) |
| **Rustic** | herbs 0.04 (x8, surface + cave), wildberries 0.04 (x4); slate and beehives off; olive (0.02) / ironwood (0.01) are Dynamic Trees species spliced into the DT populator (weights 10 and 5 per 1000) | DONE (`rustic/`): RusticGen ports WorldGeneratorRustic (fmlRandom); herbs/wildberry bush are rlgen blocks with Rustic textures; olive/ironwood are rlgen DT species spliced by scripts/gen_rustic_trees.py |
| **Snow! Real Magic!** | ReplaceSnowWorldGen: snow layers inside grass/flowers in snowy biomes | DONE (freeze pass; the plant is replaced, not kept inside the snow) |
| **Coral Reef** | coral reefs and rocks in oceans; biome list matches only "ocean" (quirk kept) | DONE (`coral/`, rlgen:reef, reef_rock, coral_1..6) |
| **Charm** | VillageDecorations: inside (themes per vanilla piece: storage, beds, rugs, frames, stands, torches, lanterns) and outside (RLCraft: lights 1, mobs 0.5, barrels 0.4, zombie erosion; flowers/crops/trees/pumpkins/mushrooms 0) at the FLOWERS event with the decoration random; AbandonedCrates 0.1 (y 28..48); MoreVillageBiomes off | DONE (`charm/`): crates are rlgen:crate_*/crate_sealed_* with Charm's textures, barrels/composters/bookshelf chests are 1.21 barrels/composters/chiseled bookshelves, flavored cakes plain cakes; loot rlgen:charm/* (scripts/gen_charm_loot.py) |
| **Waystones** | waystones in villages (1.0) and anywhere (10/10000 per chunk), mossy | DONE: village piece (Waystones handler, alphabetical order) + WildWaystones; real waystones:mossy_waystone when Waystones 1.21 is installed, else mossy wall + chiseled stone brick stand-in |
| Realistic Torches | generateLitTorches=false: no world-gen effect | NONE |
| Lycanites Mobs | fluid lakes (weight 0): ooze (cold), poison (swamp/spooky), acid (wasteland/dead) 0.08 surface, 0.01 underground; moglava nether only | DONE (`lyc/`, rlgen:ooze/acid/poison fluids with Lycanites textures, no effects yet) |
| Lycanites Mobs | dungeons (distance 45 chunks, tower chance 0.1): aberrant station, desert crypts, lush tomb, shadow labyrinth, stream shrine (jar defaults + RLCraft overrides, scripts/gen_lyc_dungeons.py) | DONE: decompiled `com.lycanitesmobs.core.dungeon` + `lyc/dungeon` (planned as chunks are first generated, kept in rlgen_lyc_dungeons; theme blocks are rlgen blocks with Lycanites textures); spawners get zombies, bosses not spawned, Lycanites fires are vanilla fire |
| Ice and Fire 1.7.1 | StructureGenerator (weight 0): fire/ice dragon roosts and dens, cyclops caves, siren islands, silver/sapphire ore, fire/frost lilies; mobs and dragon skeletons not spawned (their random draws kept) | DONE (`iaf/`, planned on 1.12 terrain, replayed after each populate area) |
| Ice and Fire 1.7.1 | gorgon temples (template converted by scripts/convert_template112.py), myrmex hives (600 apart, saved in rlgen_myrmex) | DONE |
| Ice and Fire 1.7.1 | pixie villages (pixie house as a block model), snow villages | DONE (`iaf/IafVillage`) |
| Recurrent Complex | all active structures | DONE |
| Battle Towers / Roguelike / Doomlike / Ruins | | DONE |
| Bountiful | bounty board in villages | DONE: village piece (templates in data/rlgen/structure/bountiful) with rlgen:bounty_board, or bountiful:bountyboard when Bountiful 1.21 is installed |
| Serene Seasons | season-dependent snow/colours at populate | INSTALL the 1.21 version (not ported) |
| RLTweaker | RecurrentHandler (RC populate), LostCities handler: no change to generated blocks | NONE |
| Locks | locks on generated chests (game mechanic, needs the mod) | SKIP for now |
| RLArtifacts | underground chests: chance 0 in config | NONE |
| YUNG's Law | ore hiding disabled in config | NONE |
| BetterNether, Charm nether gold | nether | SKIP (user keeps 1.21 nether) |
| SilentLib, FoamFix, Phosphor, RenderLib, RLMixins, BetterBiomeBlend, EntityCulling | libraries / rendering / chunk IO | NONE |
| Chunk Animator | client effect | install 1.21 version |

## Found in the v0.8.0 re-check (all done in v0.9.0)
| Mod | What | Status |
|---|---|---|
| Vanilla 1.12 structures | strongholds, desert/jungle temples, witch huts, igloos, ocean monuments, woodland mansions | DONE (`stronghold/`, `scattered/`, `vanilla/monument`, `vanilla/mansion`): built in the 1.12 populate order with the shared populate random (`deco/LegacyStructures`), 1.21 marker structures for /locate, eyes of ender, maps, spawns |
| 1.21-only structures | trial chambers, trail ruins, outposts, ruined portals, shipwrecks, ocean ruins, buried treasure, 1.21 mineshafts/villages | REMOVED (`world/VanillaStructureFilter`: only minecraft:ancient_cities stays, below Y 0); no pillager patrols in rlgen biomes |
| Populate random sync | mineshafts and villages built in populate (`bms/MineshaftPopulate`, `village/VillagePopulate`); Recurrent Complex decorate-event draws, Dynamic Trees denials (`deco/DecorateEvents`); Dynamic Trees grows after population (legacy_plants in VEGETAL_DECORATION before DT, weight>20 generators in legacy_late) | DONE |
| Recurrent Complex | big_mushroom decoration (`rc/RcDecoration`); RC ALLOWs decorate events in roofed forests, so vanilla dark oaks/birch/oak and big mushrooms grow there besides DT (`deco/VanillaTrees`) | DONE |
| Quark | swamp vine colour | DONE (client) |
| Recurrent Complex | 33 End structures + end megacity, 7 nether | DONE (`rc/RcDim`, `world/DimensionOracle`, structures recurrent_complex_end/_nether) |
| Ruins | End ("sky") and nether ("hell") | DONE (per-dimension templates and saved data) |
| Lycanites Mobs | nether/End dungeons, moglava lakes | DONE |
| Doomlike Dungeons | nether | DONE (`nether/`) |
| Quark | nether basalt, broken portals, fossils, obsidian spikes, smokers, nether speleothems; biotite (by dragon kill, natural gen off in config) | DONE (`nether/`, `quark/BiotiteGen`) |
| Loot | modded items in loot (Spartan, Ice and Fire, Baubles, ...) | TODO (needs the item mods) |
| Mobs in structures | Lycanites/IaF spawners get stand-ins, dragons/pirates/monster box mobs not spawned | TODO (needs the mob mods) |
| RLMixins, Inspirations, Simple Difficulty, Fishing Made Better, BNBGamingLib, antiqueatlas | re-checked hits: trades, cauldrons, rain collector, fishing data, scripts, map markers | NONE |

Comparison with a real 1.12 save: `rlgen/scripts/compare112.py` (+ `gen_compare_world.sh` for our side), needs the save's region files and level.dat.

## Gameplay ports
| Mod | Status |
|---|---|
| LLibrary 1.7.20 (parts IaF uses) | DONE (v0.10.0) |
| Ice and Fire 1.7.1 | DONE (v0.10.0): items/blocks/recipes/loot/config, dragons (server+client), land mobs, air/water mobs, world-gen spawns |
| Lycanites Mobs 2.0.8.9 | DONE (v0.11.0) |
| Mechanics (Simple Difficulty, First Aid, NTP, Spartan, RLCombat/Dodge/ShieldBreak, Baubles, Scaling Health/Infernal/Champions/Bloodmoon, Reskillable/CompatSkills/Level Up, Potion Core/SME/Better Survival) | TODO |

## Blocks inside structures
Counted per block over the overworld RC structures. The most used now are real rlgen blocks with the mods' textures
(`scripts/gen_mod_blocks.py`, data/rlgen/mod_blocks.json): BetterNether tiles/bone/reeds/stalagnate/cincinnasite, Quark carved wood,
duskbound, marble, quilted wool, clay tiles, midori, grate, Traverse fir and blue rock slab, Defiled tenebra/bricks/ores, plus all the
Ice and Fire and Quark world-gen blocks. Long tail (Rustic, Charm, Varied Commodities, Inspirations, Lycanites fluids) stays vanilla
look-alikes (convert_rc.py MOD_BLOCKS). After changing the mapping: re-run convert_rc.py and gen_legacy_tables.py.

## Checker (run after every milestone)
1. Re-run the jar scan; every jar with a hook must have a row above.
2. Seed spot checks against the user's 1.12 screenshots (biome name + height), log them in CLAUDE.md.
3. For each TODO turned DONE: dev server, forceload, top/side render, confirm no errors in the log.

## All RLCraft 2.9.3 files (187, from the manifest)
| File | Kind | Status |
|---|---|---|
| AIReducer-1.12.2-0.3.0.jar | Performance/client/UI tweak | not needed (1.21 equivalents can be installed) |
| AdHooks-1.12.2-3.3.1.0-build.0626.jar | Library | only when a ported mod needs it |
| Advanced-Fishing-1.12.2-1.3.3.jar | Gameplay (misc) | TODO |
| AnimEmis_Forgottenitems.zip | Resource pack | with its mod |
| AnimEmis_InF_SW_Blue.zip | Resource pack | with its mod |
| AnimEmis_defiledlands.zip | Resource pack | with its mod |
| AnimEmis_srparasites.zip | Resource pack | with its mod |
| Aquaculture-1.12.2-1.6.8.jar | Gameplay (misc) | TODO |
| AttributeFix-Forge-1.12.2-1.0.12.jar | RLCraft tweaks/scripts | TODO (apply what they change) |
| AutoRegLib-1.3-32.jar | Library | only when a ported mod needs it |
| BNBGamingLib-1.12.2-2.17.6.jar | Library | only when a ported mod needs it |
| BQTweaker-1.3.5.jar | Gameplay (misc) | TODO |
| BQ_Multiblock_Structure_Integration-1.0.9.jar | Gameplay (misc) | TODO |
| BattleTowers-1.12.2.jar | World gen | DONE (world gen) |
| Baubles-1.12-1.5.2.jar | Gameplay (items/baubles) | TODO |
| BedBreakBegone-1.0.2.jar | Performance/client/UI tweak | not needed (1.21 equivalents can be installed) |
| BetterFoliage-MC1.12-2.3.3.jar | Client visuals/feel | TODO later (client look/feel: port 1.12 or check) |
| BetterMineshaftsForge-1.12.2-2.2.1.jar | World gen | DONE (world gen) |
| BetterQuesting-3.5.329.jar | Gameplay (misc) | TODO |
| BetterTabs-1.0.4.jar | Performance/client/UI tweak | not needed (1.21 equivalents can be installed) |
| BlockOverlayFix-1.12.2-1.0.0.jar | Performance/client/UI tweak | not needed (1.21 equivalents can be installed) |
| Bloodmoon-MC1.12.2-1.5.3.jar | Gameplay (mob difficulty/spawn rules) | TODO |
| Born In A Barn V1.8-1.12-1.1.jar | Gameplay (mobs) | TODO |
| Bountiful Baubles-1.12.2-0.1.8.jar | Gameplay (items/baubles) | TODO |
| Bountiful-1.12.2-2.2.3.jar | World gen + gameplay | world gen DONE, gameplay TODO |
| Charm-1.12.2-1.4.1.jar | World gen + gameplay | world gen DONE, gameplay TODO |
| ChunkAnimator-1.12.2-1.2.1.jar | Client visuals/feel | install the 1.21 version (no gameplay change) |
| CollisionDamage-1.2.2.jar | Gameplay (combat/items) | TODO |
| CompatSkills-1.12.2-1.17.0.jar | Gameplay (skills) | TODO |
| ContentTweaker-1.12.2-4.10.0.jar | RLCraft tweaks/scripts | TODO (apply what they change) |
| Controlling-3.0.12.2.jar | Performance/client/UI tweak | not needed (1.21 equivalents can be installed) |
| CoralReef-2.5-1.12.2.jar | World gen | DONE (world gen) |
| CraftTweaker2-1.12-4.1.20.687.jar | RLCraft tweaks/scripts | TODO (apply what they change) |
| CreativeCore_v1.10.70_mc1.12.2.jar | Library | only when a ported mod needs it |
| CustomMainMenu-MC1.12.2-2.0.9.1.jar | Performance/client/UI tweak | not needed (1.21 equivalents can be installed) |
| DoomlikeDungeons-1.14.12-MC1.12.2.jar | World gen | DONE (world gen) |
| DynamicSurroundings-1.12.2-3.6.2.1.jar | Client visuals/feel | TODO later (client look/feel: port 1.12 or check) |
| DynamicSurroundingsHuds-1.12.2-3.6.0.0.jar | Client visuals/feel | TODO later (client look/feel: port 1.12 or check) |
| DynamicTrees-1.12.2-0.9.29.jar | World gen | DONE (world gen) |
| DynamicTreesTraverse-1.12.2-2.1.jar | World gen | DONE (world gen) |
| ElenaiDodge-1.12.2-2.1.jar | Gameplay (combat/items) | TODO |
| Emis_Rlcraft.zip | Resource pack | with its mod |
| EnchantmentDescriptions-1.12.2-1.1.20.jar | Client visuals/feel | TODO later (client look/feel: port 1.12 or check) |
| EnhancedVisuals_v1.4.4_mc1.12.2.jar | Client visuals/feel | TODO later (client look/feel: port 1.12 or check) |
| EntityCulling-1.12.2-6.3.1.jar | Performance/client/UI tweak | not needed (1.21 equivalents can be installed) |
| FamiliarFauna-1.12.2-1.0.11.jar | Gameplay (mobs) | TODO |
| FancyBlockParticles-1.12.x-2.4.1.jar | Client visuals/feel | TODO later (client look/feel: port 1.12 or check) |
| FantasticLib-1.12.2.047.jar | Library | only when a ported mod needs it |
| FishingMadeBetter-1.12.2-2.2.6.jar | Gameplay (misc) | TODO |
| FoodExpansion1.3.3-1.12.2.jar | Gameplay (survival) | TODO |
| ForgeEndertech-1.12.2-4.5.6.1-build.0648.jar | Library | only when a ported mod needs it |
| Forgelin-1.8.4.jar | Library | only when a ported mod needs it |
| FriendlyEndermite_v1.0.1_mc1.12.2.jar | Gameplay (mobs) | TODO |
| GlobalGameRules-1.12.2-2.2.7.jar | RLCraft tweaks/scripts | TODO (apply what they change) |
| GooglyEyes-1.12.2-7.1.1.jar | Gameplay (misc) | TODO |
| HelpFixer-1.12.1-1.5.18.jar | Performance/client/UI tweak | not needed (1.21 equivalents can be installed) |
| ISeeDragons-1.2.1.jar | Client visuals/feel | TODO later (client look/feel: port 1.12 or check) |
| InfernalMobs-1.12.2.jar | Gameplay (mob difficulty/spawn rules) | TODO |
| Inspirations-1.12.2-0.2.9.jar | Gameplay (misc) | TODO |
| InventoryTweaks-1.64+dev.151.jar | Performance/client/UI tweak | not needed (1.21 equivalents can be installed) |
| ItemPhysic_Full_1.4.37_mc1.12.2.jar | Gameplay (misc) | TODO |
| IvToolkit-1.3.3-1.12.jar | World gen | DONE (world gen) |
| Kiwi-1.12.2-0.5.3.32.jar | Library | only when a ported mod needs it |
| LegendaryTooltips-1.12.2-1.1.9.jar | Client visuals/feel | TODO later (client look/feel: port 1.12 or check) |
| Level Up! 2-1.1.23-1.12.jar | Gameplay (skills) | TODO |
| Leviosa.zip | Resource pack | with its mod |
| LoadingScreens-1.12.2-0.3.1.jar | Performance/client/UI tweak | not needed (1.21 equivalents can be installed) |
| Locks-1.12.2-3.0.0.jar | Gameplay (misc) | TODO |
| LootTweaker-0.3.1+MC1.12.2.jar | RLCraft tweaks/scripts | TODO (apply what they change) |
| Lum.1.12.2.zip | Resource pack | with its mod |
| LycanitesMobs32X.zip | Gameplay (mobs) | DONE (v0.11.0) |
| MTLib-3.0.7.jar | Library | only when a ported mod needs it |
| MainMenuScale-1.3.2.jar | Performance/client/UI tweak | not needed (1.21 equivalents can be installed) |
| Mantle-1.12-1.3.3.55.jar | Library | only when a ported mod needs it |
| MaterialTweaker-1.1.1.jar | Gameplay (combat/items) | TODO |
| MmmMmmMmmMmm-1.12-1.14.jar | Gameplay (misc) | TODO |
| MoBends_1.12.2-1.2.1-19.12.21.jar | Client visuals/feel | TODO later (client look/feel: port 1.12 or check) |
| MouseTweaks-2.10.1-mc1.12.2.jar | Performance/client/UI tweak | not needed (1.21 equivalents can be installed) |
| MultiMine-1.12.2.jar | Gameplay (misc) | TODO |
| Neat 1.4-17.jar | Client visuals/feel | TODO later (client look/feel: port 1.12 or check) |
| OreLib-1.12.2-3.6.0.1.jar | Library | only when a ported mod needs it |
| Placebo-1.12.2-1.6.0.jar | Library | only when a ported mod needs it |
| PortalDupeBegone-1.0.0.jar | Performance/client/UI tweak | not needed (1.21 equivalents can be installed) |
| PotionCore-1.9_for_1.12.2.jar | Gameplay (combat/items) | TODO |
| PotionFingers-r1.0-8.jar | Client visuals/feel | TODO later (client look/feel: port 1.12 or check) |
| QualityTools-1.0.7_for_1.12.2.jar | Gameplay (combat/items) | TODO |
| Quark-r1.6-179.jar | World gen + gameplay | world gen DONE, gameplay TODO |
| RLArtifacts-1.1.2.jar | Gameplay (items/baubles) | TODO |
| RLCombat-1.12.2-2.0.8.jar | Gameplay (combat/items) | TODO |
| RLMixins-1.2.15.jar | RLCraft tweaks/scripts | TODO (apply what they change) |
| RLTooltips.zip | Resource pack | with its mod |
| RLTweaker-1.12.2-0.5.12.jar | RLCraft tweaks/scripts | TODO (apply what they change) |
| RTG-1.12.2-6.1.0.0-snapshot.1.jar | World gen | SKIP (user dropped / not used) |
| ReachFix-1.12.2-1.0.8.jar | Performance/client/UI tweak | not needed (1.21 equivalents can be installed) |
| RealBench-1.12.2-1.3.3.jar | Gameplay (misc) | TODO |
| RealisticTorches-1.12.2-2.1.2.jar | Gameplay (survival) | TODO |
| RebornCore-1.12.2-3.19.5-universal.jar | Library | only when a ported mod needs it |
| RecurrentComplex-1.4.8.4.jar | World gen | DONE (world gen) |
| RenderLib-1.12.2-1.2.8.jar | Library | only when a ported mod needs it |
| Reskillable-1.12.2-1.13.0.jar | Gameplay (skills) | TODO |
| ResourceLoader-MC1.12.1-1.5.3.jar | Library | only when a ported mod needs it |
| RoguelikeDungeonsFnarEdition-1.12.2-2.4.4.jar | World gen | DONE (world gen) |
| Ruins-1.12.2.jar | World gen | DONE (world gen) |
| SRParasites-1.12.2v1.9.11.jar | Gameplay (mobs) | TODO |
| ScalingHealth-1.12.2-1.3.42+147.jar | Gameplay (mob difficulty/spawn rules) | TODO |
| SereneSeasons-1.12.2-1.2.18-universal.jar | Gameplay | TODO (1.12 port) |
| SetBonus-1.12.2.020.jar | Gameplay (combat/items) | TODO |
| ShieldBreak-1.1.3.jar | Gameplay (combat/items) | TODO |
| SilentLib-1.12.2-3.0.14+168.jar | Library | only when a ported mod needs it |
| SimpleDifficulty-1.12.2-0.3.9.jar | Gameplay (survival) | DONE (v0.12.0) |
| SnowRealMagic-1.12.2-0.7.1.jar | World gen | DONE (world gen) |
| SoManyEnchantments-0.5.5.jar | Gameplay (combat/items) | TODO |
| SoundFilters-0.12.1_for_1.12.jar | Client visuals/feel | TODO later (client look/feel: port 1.12 or check) |
| SpartanHUDBaubles-1.12.2-1.0.0.jar | Gameplay (items/baubles) | TODO |
| SpartanShields-1.12.2-1.5.5.jar | Gameplay (combat/items) | TODO |
| SpartanWeaponry-1.12.2-1.5.3.jar | Gameplay (combat/items) | TODO |
| SpawnerControl-1.6.3b.jar | Gameplay (mob difficulty/spawn rules) | TODO |
| StandardExpansion-3.4.173.jar | RLCraft tweaks/scripts | TODO (apply what they change) |
| Surge-1.12.2-2.0.79.jar | Performance/client/UI tweak | not needed (1.21 equivalents can be installed) |
| Toast Control-1.12.2-1.8.1.jar | Performance/client/UI tweak | not needed (1.21 equivalents can be installed) |
| ToolBelt-1.12.2-1.9.14.jar | Gameplay (items/baubles) | TODO |
| Traverse-1.12.2-1.6.0-69.jar | World gen | DONE (world gen) |
| Trinkets and Baubles-32.4.jar | Gameplay (items/baubles) | TODO |
| VariedCommodities_1.12.2-(31Mar23).jar | Gameplay (misc) | TODO |
| Waystones_1.12.2-4.1.0.jar | World gen + gameplay | world gen DONE, gameplay TODO |
| WearableBackpacks-RLCraft-1.12.2-3.2.6.jar | Gameplay (items/baubles) | TODO |
| Wilhelm.zip | Resource pack | with its mod |
| WolfArmorAndStorage-3.8.1+1.12.2.alpha.1-SNAPSHOT-signed.jar | Gameplay (items/baubles) | TODO |
| XPFromHarvest-1.12.2-1.2.0.jar | Gameplay (survival) | TODO |
| YungsLaw-1.12.2-1.0.4.jar | RLCraft tweaks/scripts | TODO (apply what they change) |
| [___MixinCompat-0.8___].jar | Library | only when a ported mod needs it |
| _MixinBootstrap-1.1.0.jar | Library | only when a ported mod needs it |
| alcatrazcore-1.0.4.jar | Library | only when a ported mod needs it |
| antiqueatlas-1.12.2-4.6.3.jar | Gameplay (misc) | TODO |
| anvilpatch-1.1.1.jar | Performance/client/UI tweak | not needed (1.21 equivalents can be installed) |
| armorunder-mc1.12.2-1.0.0.jar | Gameplay (misc) | TODO |
| astikorcarts-1.12.2-0.1.2.8.jar | Gameplay (misc) | TODO |
| base-1.12.2-3.14.0.jar | Library | only when a ported mod needs it |
| better_survival-1.5.3.jar | Gameplay (combat/items) | TODO |
| betterbiomeblend-1.12.2-1.1.7-forge.jar | Performance/client/UI tweak | not needed (1.21 equivalents can be installed) |
| bettercaves-1.12.2-2.0.4.jar | World gen | DONE (world gen) |
| betternether-0.1.8.6.jar | World gen | SKIP (user dropped / not used) |
| callablehorses-1.12.2-1.1.1.jar | Gameplay (misc) | TODO |
| carrotslib-mc1.12.2-1.0.0b1.jar | Library | only when a ported mod needs it |
| carryon-1.12.2-1.12.7.23.jar | Gameplay (misc) | TODO |
| champions-1.12.2-1.0.11.10.jar | Gameplay (mob difficulty/spawn rules) | TODO |
| classyhats-1.6.0.jar | Gameplay (items/baubles) | TODO |
| comforts-1.12.2-1.4.1.3.jar | Gameplay (survival) | TODO |
| corpsecomplex-1.12.2-1.0.12.0.jar | Gameplay (survival) | TODO |
| craftablechainmail-1.0.jar | Gameplay (survival) | TODO |
| defiledlands-1.12.2-1.4.3.jar | World gen | DONE (world gen) |
| disenchanter[1.12]1.8.jar | Gameplay (combat/items) | TODO |
| dynamictreesdefiledlands-1.12.2-1.0.0.jar | World gen | DONE (world gen) |
| firstaid-1.6.22.jar | Gameplay (survival) | DONE (v0.12.0) |
| foamfix-0.10.15-1.12.2.jar | Performance/client/UI tweak | not needed (1.21 equivalents can be installed) |
| forgottenitems-1.12.2-1.3.1.4.jar | Gameplay (items/baubles) | TODO |
| framevoidpatch-1.0.0.jar | Performance/client/UI tweak | not needed (1.21 equivalents can be installed) |
| fxcontrol-1.12-0.1.15.jar | Gameplay (mob difficulty/spawn rules) | TODO |
| grapplemod-1.12.2-v12.3.jar | Gameplay (misc) | TODO |
| iChunUtil-1.12.2-7.2.2.jar | Library | only when a ported mod needs it |
| iceandfire-1.7.1-1.12.2.jar | Gameplay | DONE (v0.10.0) |
| incontrol-1.12-3.9.18.jar | Gameplay (mob difficulty/spawn rules) | TODO |
| itlt-1.12.x-1.0.4.jar | Performance/client/UI tweak | not needed (1.21 equivalents can be installed) |
| jei_1.12.2-4.16.1.302.jar | Performance/client/UI tweak | not needed (1.21 equivalents can be installed) |
| librarianlib-1.12.2-4.22.jar | Library | only when a ported mod needs it |
| llibrary-1.7.20-1.12.2.jar | Gameplay | DONE (v0.10.0) |
| lostcities-1.12-2.0.22.jar | World gen | SKIP (user dropped / not used) |
| lycanitesmobs-1.12.2-2.0.8.9.jar | Gameplay (mobs) | DONE (v0.11.0) |
| mcw-bridges-1.0.6b-mc1.12.2.jar | Gameplay (misc) | TODO |
| modtweaker-4.0.20.11.jar | RLCraft tweaks/scripts | TODO (apply what they change) |
| notreepunching-2.0.21.jar | World gen + gameplay | DONE (v0.12.0) |
| particleculling-1.12.2-v1.4.1.jar | Performance/client/UI tweak | not needed (1.21 equivalents can be installed) |
| phosphor-1.12.2-0.2.6+build50-universal.jar | Performance/client/UI tweak | not needed (1.21 equivalents can be installed) |
| pigstep-1.12.jar | Gameplay (mobs) | TODO |
| playerbosses-1.12.2-1.1.0.jar | Gameplay (mobs) | TODO |
| roughTweaks0.2.4-1.12.2.jar | RLCraft tweaks/scripts | TODO (apply what they change) |
| rustic-1.1.7.jar | World gen + gameplay | world gen DONE, gameplay TODO |
| sit-1.12.2-v1.3.jar | Gameplay (misc) | TODO |
| spark-forge.jar | Performance/client/UI tweak | not needed (1.21 equivalents can be installed) |
| spartandefiled-1.0.jar | Gameplay (combat/items) | TODO |
| spartanfire_rlcraft-1.1.0.jar | Gameplay (combat/items) | TODO |
| switchbow-1.6.8.jar | Gameplay (combat/items) | TODO |
| tnt_utilities-mc1.12-1.2.3.jar | Gameplay (misc) | TODO |
| trumpetskeleton-1.12-1.0.2.1.jar | Gameplay (mobs) | TODO |
| tschipplib-1.12.2-1.1.7.jar | Library | only when a ported mod needs it |
| tumbleweed-1.12-0.4.7.jar | Gameplay (mobs) | TODO |
| xptome-1.12.2-v1.1.6.jar | Gameplay (survival) | TODO |
