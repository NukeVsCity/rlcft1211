package com.nukevscity.rlgen.deco;

import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.legacy.noise.NoiseGeneratorPerlin;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Biome.decorate + BiomeDecorator settings of every 1.12 biome RLCraft had: vanilla, Traverse 1.6.0 and Defiled Lands 1.4.3.
 * Tree generation is left to Dynamic Trees (it denied the TREE decorate event in 1.12, so tree-gated extras never ran).
 */
public class BiomeDeco {
   static final NoiseGeneratorPerlin GRASS_COLOR_NOISE = new NoiseGeneratorPerlin(new Random(2345L), 1);
   private static final Map<Biome, BiomeDeco> CACHE = new ConcurrentHashMap<>();

   // BiomeDecorator fields
   int waterlilyPerChunk;
   int treesPerChunk;
   float extraTreeChance = 0.1F;
   int flowersPerChunk = 2;
   int grassPerChunk = 1;
   int deadBushPerChunk;
   int mushroomsPerChunk;
   int reedsPerChunk;
   int cactiPerChunk;
   int gravelPatchesPerChunk = 1;
   int sandPatchesPerChunk = 3;
   int clayPerChunk = 1;
   int bigMushroomsPerChunk;
   boolean generateFalls = true;
   boolean mesaGold;

   /** The decorator counts for one chunk (plains changes them per chunk). */
   static final class Counts {
      int waterlilyPerChunk, treesPerChunk, flowersPerChunk, grassPerChunk, deadBushPerChunk, mushroomsPerChunk, reedsPerChunk, cactiPerChunk;
      int gravelPatchesPerChunk, sandPatchesPerChunk, clayPerChunk, bigMushroomsPerChunk;
      float extraTreeChance;
   }

   Counts counts() {
      Counts c = new Counts();
      c.waterlilyPerChunk = this.waterlilyPerChunk;
      c.treesPerChunk = this.treesPerChunk;
      c.extraTreeChance = this.extraTreeChance;
      c.flowersPerChunk = this.flowersPerChunk;
      c.grassPerChunk = this.grassPerChunk;
      c.deadBushPerChunk = this.deadBushPerChunk;
      c.mushroomsPerChunk = this.mushroomsPerChunk;
      c.reedsPerChunk = this.reedsPerChunk;
      c.cactiPerChunk = this.cactiPerChunk;
      c.gravelPatchesPerChunk = this.gravelPatchesPerChunk;
      c.sandPatchesPerChunk = this.sandPatchesPerChunk;
      c.clayPerChunk = this.clayPerChunk;
      c.bigMushroomsPerChunk = this.bigMushroomsPerChunk;
      return c;
   }

   /** Biome.decorate before the decorator runs (may change this chunk's counts, like plains). */
   void pre(DWorld w, Random r, BlockPos pos, Counts c) {
   }

   /** Biome.decorate after the decorator. */
   void post(DWorld w, Random r, BlockPos pos) {
   }

   BlockState flower(Random r, BlockPos pos) {
      return r.nextInt(3) > 0 ? Blocks.DANDELION.defaultBlockState() : Blocks.POPPY.defaultBlockState();
   }

   /** getRandomWorldGenForGrass: the plant WorldGenTallGrass (or Traverse WorldGenPlant) places. */
   BlockState grass(Random r) {
      return Blocks.SHORT_GRASS.defaultBlockState();
   }

   // ------------------------------------------------------------------ helpers shared by biomes

   static final BlockState[] FLOWER_TYPES = {
      Blocks.DANDELION.defaultBlockState(), Blocks.POPPY.defaultBlockState(), Blocks.BLUE_ORCHID.defaultBlockState(), Blocks.ALLIUM.defaultBlockState(),
      Blocks.AZURE_BLUET.defaultBlockState(), Blocks.RED_TULIP.defaultBlockState(), Blocks.ORANGE_TULIP.defaultBlockState(),
      Blocks.WHITE_TULIP.defaultBlockState(), Blocks.PINK_TULIP.defaultBlockState(), Blocks.OXEYE_DAISY.defaultBlockState()};

   /** BiomePlains.pickRandomFlower (Traverse copies it with other thresholds / tulip rolls). */
   static BlockState plainsFlower(Random r, BlockPos pos, double threshold, int tulipRoll) {
      double d0 = GRASS_COLOR_NOISE.getValue(pos.getX() / 200.0, pos.getZ() / 200.0);
      if (d0 < threshold) {
         return switch (r.nextInt(tulipRoll)) {
            case 0 -> Blocks.ORANGE_TULIP.defaultBlockState();
            case 1 -> Blocks.RED_TULIP.defaultBlockState();
            case 2 -> Blocks.PINK_TULIP.defaultBlockState();
            default -> Blocks.WHITE_TULIP.defaultBlockState();
         };
      } else if (r.nextInt(3) > 0) {
         int i = r.nextInt(3);
         return i == 0 ? Blocks.POPPY.defaultBlockState() : (i == 1 ? Blocks.AZURE_BLUET.defaultBlockState() : Blocks.OXEYE_DAISY.defaultBlockState());
      }
      return Blocks.DANDELION.defaultBlockState();
   }

   /** The DOUBLE_PLANT_GENERATOR loops: 7 (or n) tries at random heights. */
   static void doublePlants(DWorld w, Random r, BlockPos pos, Block plant, int tries) {
      for (int i = 0; i < tries; i++) {
         int j = r.nextInt(16) + 8;
         int k = r.nextInt(16) + 8;
         int l = r.nextInt(w.height(pos.getX() + j, pos.getZ() + k) + 32);
         Gen.doublePlant(w, r, pos.offset(j, l, k), plant);
      }
   }

   /** "rand.nextInt(n) == 0 then generate at getHeight(random spot)" (Traverse boulders, patches). */
   interface AtHeight {
      void gen(DWorld w, Random r, BlockPos at);
   }

   static void chance(DWorld w, Random r, BlockPos pos, int n, AtHeight g) {
      if (r.nextInt(n) == 0) {
         int x = r.nextInt(16) + 8;
         int z = r.nextInt(16) + 8;
         g.gen(w, r, w.height(pos.offset(x, 0, z)));
      }
   }

   static void boulder(DWorld w, Random r, BlockPos pos, int n, Block block, int radius) {
      chance(w, r, pos, n, (w2, r2, at) -> Gen.blockBlob(w2, r2, at, block.defaultBlockState(), radius, Gen.BLOB_GROUND));
   }

   static final List<Block> PATCH_DEFAULT = List.of(Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.STONE);

   // ------------------------------------------------------------------ the biomes

   public static BiomeDeco of(Biome biome) {
      return CACHE.computeIfAbsent(biome, b -> create(b.getRegistryName()));
   }

   static BiomeDeco create(String name) {
      return switch (name) {
         case "plains" -> plains(false);
         case "mutated_plains" -> plains(true);
         case "desert", "desert_hills", "mutated_desert" -> desert();
         case "extreme_hills", "smaller_extreme_hills", "extreme_hills_with_trees", "mutated_extreme_hills", "mutated_extreme_hills_with_trees" -> hills();
         case "forest", "forest_hills" -> forest(Forest.NORMAL);
         case "birch_forest", "birch_forest_hills", "mutated_birch_forest", "mutated_birch_forest_hills" -> forest(Forest.BIRCH);
         case "roofed_forest", "mutated_roofed_forest" -> forest(Forest.ROOFED);
         case "mutated_forest" -> forest(Forest.FLOWER);
         case "taiga", "taiga_hills", "taiga_cold", "taiga_cold_hills", "mutated_taiga", "mutated_taiga_cold" -> taiga(false);
         case "redwood_taiga", "redwood_taiga_hills", "mutated_redwood_taiga", "mutated_redwood_taiga_hills" -> taiga(true);
         case "swampland", "mutated_swampland" -> swamp();
         case "ice_flats", "ice_mountains" -> snow(false);
         case "mutated_ice_flats" -> snow(true);
         case "mushroom_island", "mushroom_island_shore" -> mushroom();
         case "beaches", "cold_beach", "stone_beach" -> beach();
         case "jungle", "jungle_hills", "mutated_jungle" -> jungle(false);
         case "jungle_edge", "mutated_jungle_edge" -> jungle(true);
         case "savanna", "savanna_rock" -> savanna(false);
         case "mutated_savanna", "mutated_savanna_rock" -> savanna(true);
         case "mesa", "mesa_clear_rock", "mutated_mesa", "mutated_mesa_clear_rock" -> mesa(false);
         case "mesa_rock", "mutated_mesa_rock" -> mesa(true);
         // ---- Traverse
         case "traverse:autumnal_woods", "traverse:autumnal_wooded_hills" -> counts(new BiomeDeco(), 10, 4, 6);
         case "traverse:woodlands" -> woodlands();
         case "traverse:mini_jungle" -> miniJungle();
         case "traverse:meadow" -> meadow();
         case "traverse:green_swamp" -> lushSwamp();
         case "traverse:red_desert", "traverse:desert_shrubland", "traverse:arid_highland" -> noFalls(desert());
         case "traverse:temperate_rainforest", "traverse:snowy_coniferous_forest" -> coniferous();
         case "traverse:badlands" -> badlands();
         case "traverse:mountainous_desert" -> mountainousDesert();
         case "traverse:rocky_plateau" -> rockyPlateau();
         case "traverse:forested_hills" -> forest(Forest.NORMAL);
         case "traverse:birch_forested_hills" -> forest(Forest.BIRCH);
         case "traverse:cliffs" -> cliffs();
         case "traverse:glacier" -> glacier(false);
         case "traverse:glacier_spikes" -> glacier(true);
         case "traverse:lush_hills" -> lushHills();
         case "traverse:canyon" -> canyon();
         case "traverse:crag_cliffs" -> cragCliffs();
         case "traverse:thicket" -> thicket();
         case "traverse:rocky_plains" -> rockyPlains();
         default -> name.startsWith("defiledlands:") ? DefiledDeco.create(name) : new BiomeDeco();
      };
   }

   static BiomeDeco counts(BiomeDeco d, int trees, int flowers, int grass) {
      d.treesPerChunk = trees;
      d.flowersPerChunk = flowers;
      d.grassPerChunk = grass;
      return d;
   }

   static BiomeDeco noFalls(BiomeDeco d) {
      d.generateFalls = false;
      return d;
   }

   static BiomeDeco plains(boolean sunflowers) {
      BiomeDeco d = new BiomeDeco() {
         @Override
         void pre(DWorld w, Random r, BlockPos pos, Counts c) {
            double d0 = GRASS_COLOR_NOISE.getValue((pos.getX() + 8) / 200.0, (pos.getZ() + 8) / 200.0);
            if (d0 < -0.8) {
               c.flowersPerChunk = 15;
               c.grassPerChunk = 5;
            } else {
               c.flowersPerChunk = 4;
               c.grassPerChunk = 10;
               doublePlants(w, r, pos, Blocks.TALL_GRASS, 7);
            }
            if (sunflowers) {
               doublePlants(w, r, pos, Blocks.SUNFLOWER, 10);
            }
         }

         @Override
         BlockState flower(Random r, BlockPos pos) {
            return plainsFlower(r, pos, -0.8, 4);
         }
      };
      d.extraTreeChance = 0.05F;
      return counts(d, 0, 4, 10);
   }

   static BiomeDeco desert() {
      BiomeDeco d = new BiomeDeco() {
         @Override
         void post(DWorld w, Random r, BlockPos pos) {
            if (r.nextInt(1000) == 0) {
               int i = r.nextInt(16) + 8;
               int j = r.nextInt(16) + 8;
               Gen.desertWell(w, r, w.height(pos.offset(i, 0, j)).above());
            }
            if (r.nextInt(64) == 0) {
               Gen.fossil(w, pos);
            }
         }
      };
      d.treesPerChunk = -999;
      d.deadBushPerChunk = 2;
      d.reedsPerChunk = 50;
      d.cactiPerChunk = 10;
      return d;
   }

   static BiomeDeco hills() {
      return new BiomeDeco() {
         @Override
         void post(DWorld w, Random r, BlockPos pos) {
            int i = 3 + r.nextInt(6);
            for (int j = 0; j < i; j++) {
               BlockPos b = pos.offset(r.nextInt(16), r.nextInt(28) + 4, r.nextInt(16));
               if (w.get(b).is(Blocks.STONE)) {
                  w.set(b, Blocks.EMERALD_ORE.defaultBlockState());
               }
            }
            for (int j1 = 0; j1 < 7; j1++) {
               BlockPos b = pos.offset(r.nextInt(16), r.nextInt(64), r.nextInt(16));
               Gen.minable(w, r, b, Blocks.INFESTED_STONE.defaultBlockState(), 9, Gen.NATURAL_STONE);
            }
         }
      };
   }

   enum Forest {
      NORMAL,
      FLOWER,
      BIRCH,
      ROOFED
   }

   static BiomeDeco forest(Forest type) {
      BiomeDeco d = new BiomeDeco() {
         @Override
         void pre(DWorld w, Random r, BlockPos pos, Counts c) {
            if (type == Forest.ROOFED) {
               // addMushrooms: the dark oaks themselves come from Dynamic Trees; 1 in 20 spots is a huge mushroom
               for (int i = 0; i < 4; i++) {
                  for (int j = 0; j < 4; j++) {
                     int k = i * 4 + 1 + 8 + r.nextInt(3);
                     int l = j * 4 + 1 + 8 + r.nextInt(3);
                     BlockPos b = w.height(pos.offset(k, 0, l));
                     if (r.nextInt(20) == 0) {
                        Gen.bigMushroom(w, r, b, null);
                     }
                  }
               }
            }
            int n = r.nextInt(5) - 3;
            if (type == Forest.FLOWER) {
               n += 2;
            }
            for (int j = 0; j < n; j++) {
               int k = r.nextInt(3);
               Block plant = k == 0 ? Blocks.LILAC : (k == 1 ? Blocks.ROSE_BUSH : Blocks.PEONY);
               for (int l = 0; l < 5; l++) {
                  int i1 = r.nextInt(16) + 8;
                  int j1 = r.nextInt(16) + 8;
                  int k1 = r.nextInt(w.height(pos.getX() + i1, pos.getZ() + j1) + 32);
                  if (Gen.doublePlant(w, r, new BlockPos(pos.getX() + i1, k1, pos.getZ() + j1), plant)) {
                     break;
                  }
               }
            }
         }

         @Override
         BlockState flower(Random r, BlockPos pos) {
            if (type == Forest.FLOWER) {
               double d0 = Mth.clamp((1.0 + GRASS_COLOR_NOISE.getValue(pos.getX() / 48.0, pos.getZ() / 48.0)) / 2.0, 0.0, 0.9999);
               BlockState s = FLOWER_TYPES[(int)(d0 * FLOWER_TYPES.length)];
               return s.is(Blocks.BLUE_ORCHID) ? Blocks.POPPY.defaultBlockState() : s;
            }
            return super.flower(r, pos);
         }
      };
      d.treesPerChunk = 10;
      d.grassPerChunk = 2;
      if (type == Forest.FLOWER) {
         d.treesPerChunk = 6;
         d.flowersPerChunk = 100;
         d.grassPerChunk = 1;
      }
      if (type == Forest.ROOFED) {
         d.treesPerChunk = -999;
      }
      return d;
   }

   static BiomeDeco taiga(boolean mega) {
      BiomeDeco d = new BiomeDeco() {
         @Override
         void pre(DWorld w, Random r, BlockPos pos, Counts c) {
            if (mega) {
               int i = r.nextInt(3);
               for (int j = 0; j < i; j++) {
                  int k = r.nextInt(16) + 8;
                  int l = r.nextInt(16) + 8;
                  Gen.blockBlob(w, r, w.height(pos.offset(k, 0, l)), Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 0, Gen.BLOB_GROUND);
               }
            }
            doublePlants(w, r, pos, Blocks.LARGE_FERN, 7);
         }

         @Override
         BlockState grass(Random r) {
            return r.nextInt(5) > 0 ? Blocks.FERN.defaultBlockState() : Blocks.SHORT_GRASS.defaultBlockState();
         }
      };
      d.treesPerChunk = 10;
      if (!mega) {
         d.grassPerChunk = 1;
         d.mushroomsPerChunk = 1;
      } else {
         d.grassPerChunk = 7;
         d.deadBushPerChunk = 1;
         d.mushroomsPerChunk = 3;
      }
      return d;
   }

   static BiomeDeco swamp() {
      BiomeDeco d = new BiomeDeco() {
         @Override
         BlockState flower(Random r, BlockPos pos) {
            return Blocks.BLUE_ORCHID.defaultBlockState();
         }

         @Override
         void post(DWorld w, Random r, BlockPos pos) {
            if (r.nextInt(64) == 0) {
               Gen.fossil(w, pos);
            }
         }
      };
      d.treesPerChunk = 2;
      d.flowersPerChunk = 1;
      d.deadBushPerChunk = 1;
      d.mushroomsPerChunk = 8;
      d.reedsPerChunk = 10;
      d.clayPerChunk = 1;
      d.waterlilyPerChunk = 4;
      d.sandPatchesPerChunk = 0;
      d.gravelPatchesPerChunk = 0;
      d.grassPerChunk = 5;
      return d;
   }

   static BiomeDeco snow(boolean superIcy) {
      return new BiomeDeco() {
         @Override
         void pre(DWorld w, Random r, BlockPos pos, Counts c) {
            if (superIcy) {
               for (int i = 0; i < 3; i++) {
                  int j = r.nextInt(16) + 8;
                  int k = r.nextInt(16) + 8;
                  Gen.iceSpike(w, r, w.height(pos.offset(j, 0, k)));
               }
               for (int l = 0; l < 2; l++) {
                  int i1 = r.nextInt(16) + 8;
                  int j1 = r.nextInt(16) + 8;
                  Gen.icePath(w, r, w.height(pos.offset(i1, 0, j1)), 4);
               }
            }
         }
      };
   }

   static BiomeDeco mushroom() {
      BiomeDeco d = counts(new BiomeDeco(), -100, -100, -100);
      d.mushroomsPerChunk = 1;
      d.bigMushroomsPerChunk = 1;
      return d;
   }

   static BiomeDeco beach() {
      BiomeDeco d = new BiomeDeco();
      d.treesPerChunk = -999;
      return d;
   }

   static BiomeDeco jungle(boolean edge) {
      BiomeDeco d = new BiomeDeco() {
         @Override
         BlockState grass(Random r) {
            return r.nextInt(4) == 0 ? Blocks.FERN.defaultBlockState() : Blocks.SHORT_GRASS.defaultBlockState();
         }

         @Override
         void post(DWorld w, Random r, BlockPos pos) {
            int i = r.nextInt(16) + 8;
            int j = r.nextInt(16) + 8;
            int k = r.nextInt(Math.max(1, w.height(pos.getX() + i, pos.getZ() + j) * 2));
            Gen.melon(w, r, pos.offset(i, k, j));
            for (int j1 = 0; j1 < 50; j1++) {
               int x = r.nextInt(16) + 8;
               int z = r.nextInt(16) + 8;
               Gen.vines(w, r, pos.offset(x, 128, z));
            }
         }
      };
      return counts(d, edge ? 2 : 50, 4, 25);
   }

   static BiomeDeco savanna(boolean mutated) {
      BiomeDeco d = new BiomeDeco() {
         @Override
         void pre(DWorld w, Random r, BlockPos pos, Counts c) {
            if (!mutated) {
               doublePlants(w, r, pos, Blocks.TALL_GRASS, 7);
            }
         }
      };
      return mutated ? counts(d, 2, 2, 5) : counts(d, 1, 4, 20);
   }

   static BiomeDeco mesa(boolean forest) {
      BiomeDeco d = new BiomeDeco();
      d.treesPerChunk = forest ? 5 : -999;
      d.deadBushPerChunk = 20;
      d.reedsPerChunk = 3;
      d.cactiPerChunk = 5;
      d.flowersPerChunk = 0;
      d.mesaGold = true;
      return d;
   }

   // ---- Traverse

   static BiomeDeco woodlands() {
      BiomeDeco d = new BiomeDeco() {
         @Override
         BlockState flower(Random r, BlockPos pos) {
            return plainsFlower(r, pos, -0.8, 4);
         }
      };
      return counts(d, 4, 6, 16);
   }

   static BiomeDeco miniJungle() {
      BiomeDeco d = new BiomeDeco() {
         @Override
         BlockState grass(Random r) {
            return r.nextInt(4) == 0 ? Blocks.FERN.defaultBlockState() : Blocks.SHORT_GRASS.defaultBlockState();
         }

         @Override
         void post(DWorld w, Random r, BlockPos pos) {
            int i = r.nextInt(16) + 8;
            int j = r.nextInt(16) + 8;
            int height = Math.max(1, w.height(pos.getX() + i, pos.getZ() + j) * 2);
            int k = r.nextInt(height);
            Gen.melon(w, r, pos.offset(i, k, j));
            for (int n = 0; n < 50; n++) {
               int x = r.nextInt(16) + 8;
               int z = r.nextInt(16) + 8;
               Gen.vines(w, r, pos.offset(x, 128, z));
            }
         }
      };
      counts(d, 30, 5, 25);
      d.reedsPerChunk = 2;
      d.clayPerChunk = 3;
      d.waterlilyPerChunk = 12;
      return d;
   }

   static BiomeDeco meadow() {
      BiomeDeco d = new BiomeDeco() {
         @Override
         BlockState flower(Random r, BlockPos pos) {
            return plainsFlower(r, pos, -0.8, 5);
         }
      };
      counts(d, 0, 15, 15);
      d.extraTreeChance = 0.2F;
      d.reedsPerChunk = 2;
      return d;
   }

   static BiomeDeco lushSwamp() {
      BiomeDeco d = new BiomeDeco() {
         @Override
         BlockState flower(Random r, BlockPos pos) {
            return Blocks.BLUE_ORCHID.defaultBlockState();
         }

         @Override
         void post(DWorld w, Random r, BlockPos pos) {
            if (r.nextInt(64) == 0) {
               Gen.fossil(w, pos);
            }
         }
      };
      counts(d, 2, 5, 10);
      d.deadBushPerChunk = 1;
      d.mushroomsPerChunk = 4;
      d.reedsPerChunk = 10;
      d.clayPerChunk = 1;
      d.waterlilyPerChunk = 4;
      d.sandPatchesPerChunk = 0;
      return d;
   }

   static BiomeDeco coniferous() {
      BiomeDeco d = counts(new BiomeDeco(), 7, 0, 16);
      d.mushroomsPerChunk = 2;
      return d;
   }

   static BiomeDeco badlands() {
      BiomeDeco d = new BiomeDeco() {
         @Override
         void pre(DWorld w, Random r, BlockPos pos, Counts c) {
            boulder(w, r, pos, 5, Blocks.COBBLESTONE, 1);
            chance(w, r, pos, 12, (w2, r2, at) -> Gen.lake(w2, r2, at, true));
            doublePlants(w, r, pos, Blocks.TALL_GRASS, 7);
         }
      };
      counts(d, 1, 0, 16);
      d.extraTreeChance = 4.0F;
      return d;
   }

   static BiomeDeco mountainousDesert() {
      BiomeDeco desert = desert();
      BiomeDeco d = new BiomeDeco() {
         @Override
         void pre(DWorld w, Random r, BlockPos pos, Counts c) {
            chance(w, r, pos, 9, (w2, r2, at) -> Gen.patch(w2, r2, at, Blocks.STONE.defaultBlockState(), 6, List.of(Blocks.SAND, Blocks.SANDSTONE), false));
            boulder(w, r, pos, 9, Blocks.COBBLESTONE, 1);
         }

         @Override
         void post(DWorld w, Random r, BlockPos pos) {
            desert.post(w, r, pos);
         }
      };
      d.treesPerChunk = -999;
      d.deadBushPerChunk = 2;
      d.reedsPerChunk = 50;
      d.cactiPerChunk = 10;
      return d;
   }

   static BiomeDeco rockyPlateau() {
      BiomeDeco d = new BiomeDeco() {
         @Override
         void pre(DWorld w, Random r, BlockPos pos, Counts c) {
            chance(w, r, pos, 7, (w2, r2, at) -> Gen.patch(w2, r2, at, Blocks.COARSE_DIRT.defaultBlockState(), 4, PATCH_DEFAULT, false));
            chance(w, r, pos, 9, (w2, r2, at) -> Gen.patch(w2, r2, at, Blocks.STONE.defaultBlockState(), 5, PATCH_DEFAULT, false));
            boulder(w, r, pos, 3, Blocks.COBBLESTONE, 1);
         }
      };
      counts(d, 0, 0, 8);
      d.extraTreeChance = 0.0F;
      return d;
   }

   static BiomeDeco cliffs() {
      return new BiomeDeco() {
         @Override
         void pre(DWorld w, Random r, BlockPos pos, Counts c) {
            boulder(w, r, pos, 9, Blocks.COBBLESTONE, 2);
         }
      };
   }

   static BiomeDeco glacier(boolean spikes) {
      BiomeDeco d = new BiomeDeco() {
         @Override
         void pre(DWorld w, Random r, BlockPos pos, Counts c) {
            boulder(w, r, pos, 9, Blocks.COBBLESTONE, 1);
            if (spikes && r.nextInt(3) == 0) {
               int j = r.nextInt(16) + 8;
               int k = r.nextInt(16) + 8;
               Gen.iceSpike(w, r, w.height(pos.offset(j, 0, k)));
            }
         }
      };
      d.treesPerChunk = 1;
      d.extraTreeChance = 4.0F;
      return d;
   }

   static BiomeDeco lushHills() {
      BiomeDeco d = new BiomeDeco() {
         @Override
         BlockState flower(Random r, BlockPos pos) {
            return plainsFlower(r, pos, -0.9, 4);
         }
      };
      return counts(d, 2, 10, 3);
   }

   static BiomeDeco canyon() {
      BiomeDeco d = new BiomeDeco() {
         @Override
         BlockState grass(Random r) {
            return RLGen.DEAD_GRASS.get().defaultBlockState();
         }
      };
      counts(d, -999, -999, 1);
      d.extraTreeChance = -999.0F;
      d.generateFalls = false;
      return d;
   }

   static BiomeDeco cragCliffs() {
      BiomeDeco d = new BiomeDeco() {
         @Override
         void pre(DWorld w, Random r, BlockPos pos, Counts c) {
            boulder(w, r, pos, 9, RLGen.BLUE_ROCK_COBBLESTONE.get(), 2);
         }

         @Override
         BlockState grass(Random r) {
            return RLGen.COLD_GRASS.get().defaultBlockState();
         }
      };
      counts(d, -999, -999, 2);
      d.extraTreeChance = -999.0F;
      d.generateFalls = false;
      return d;
   }

   static BiomeDeco thicket() {
      BiomeDeco d = new BiomeDeco() {
         @Override
         void pre(DWorld w, Random r, BlockPos pos, Counts c) {
            chance(w, r, pos, 4, (w2, r2, at) -> Gen.patch(w2, r2, at, Blocks.PODZOL.defaultBlockState(), 6, PATCH_DEFAULT, true));
            chance(w, r, pos, 5, (w2, r2, at) -> Gen.patch(w2, r2, at, Blocks.GRAVEL.defaultBlockState(), 4, PATCH_DEFAULT, false));
            doublePlants(w, r, pos, Blocks.TALL_GRASS, 7);
         }
      };
      counts(d, 35, 0, 3);
      d.extraTreeChance = 4.0F;
      return d;
   }

   static BiomeDeco rockyPlains() {
      BiomeDeco plains = plains(false);
      BiomeDeco d = new BiomeDeco() {
         @Override
         void pre(DWorld w, Random r, BlockPos pos, Counts c) {
            chance(w, r, pos, 4, (w2, r2, at) -> Gen.patch(w2, r2, at, Blocks.STONE.defaultBlockState(), 5, PATCH_DEFAULT, false));
            boulder(w, r, pos, 4, Blocks.COBBLESTONE, 1);
            plains.pre(w, r, pos, c);
         }

         @Override
         BlockState flower(Random r, BlockPos pos) {
            return plains.flower(r, pos);
         }
      };
      d.extraTreeChance = 0.05F;
      return counts(d, 0, 4, 10);
   }
}
