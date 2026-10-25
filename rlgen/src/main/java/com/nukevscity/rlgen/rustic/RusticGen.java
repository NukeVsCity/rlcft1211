package com.nukevscity.rlgen.rustic;

import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.deco.DWorld;
import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.quark.QuarkGen;
import com.nukevscity.rlgen.rc.BiomeDict;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Rustic 1.1.7 WorldGeneratorRustic (IWorldGenerator weight 0) with RLCraft's rustic.cfg: wildberries, surface herbs and cave herbs at
 * 0.04 each, 4 / 8 / 8 attempts. Trees are off because Dynamic Trees replaces them, beehives have chance 0 and slate is disabled.
 */
public final class RusticGen {
   private static final float WILDBERRY_CHANCE = 0.04F;
   private static final float HERB_CHANCE = 0.04F;
   private static final int WILDBERRY_ATTEMPTS = 4;
   private static final int HERB_ATTEMPTS = 8;
   private static final String[] BERRY_BLACKLIST = {"COLD", "SNOWY", "SANDY", "SAVANNA", "MESA", "MUSHROOM", "NETHER", "END", "DEAD", "WASTELAND"};

   private RusticGen() {
   }

   public static void generate(DWorld w, int chunkX, int chunkZ) {
      Random random = QuarkGen.fmlRandom(w.seed(), chunkX, chunkZ);
      BlockPos center = new BlockPos(chunkX * 16 + 8, w.height(chunkX * 16 + 8, chunkZ * 16 + 8), chunkZ * 16 + 8);
      if (random.nextFloat() < WILDBERRY_CHANCE) {
         wildberries(w, random, center);
      }
      if (random.nextFloat() < HERB_CHANCE) {
         surfaceHerbs(w, random, center);
      }
      if (random.nextFloat() < HERB_CHANCE) {
         caveHerbs(w, random, center);
      }
   }

   private static void wildberries(DWorld w, Random rand, BlockPos pos) {
      Biome biome = w.biome(pos);
      for (String type : BERRY_BLACKLIST) {
         if (BiomeDict.hasType(biome, type)) {
            return;
         }
      }
      for (int i = 0; i < WILDBERRY_ATTEMPTS; i++) {
         int x = pos.getX() + rand.nextInt(7) - rand.nextInt(7);
         int z = pos.getZ() + rand.nextInt(7) - rand.nextInt(7);
         BlockPos genPos = w.topSolidOrLiquid(new BlockPos(x, 0, z));
         BlockState state = w.get(genPos);
         if (DWorld.isLiquid(state)) {
            continue;
         }
         // BlockBush.canPlaceBlockAt (replaceable, grass/dirt/farmland below) && canSustainPlant(Plains); soil must not be liquid
         BlockState soil = w.get(genPos.below());
         if (state.canBeReplaced() && !DWorld.isLiquid(soil) && isPlainsSoil(soil)) {
            w.set(genPos, RLGen.WILDBERRY_BUSH.get().defaultBlockState().setValue(WildberryBushBlock.BERRIES, rand.nextBoolean()));
         }
      }
   }

   private static void surfaceHerbs(DWorld w, Random rand, BlockPos pos) {
      Biome biome = w.biome(pos);
      if (biome.isSnowyBiome()) {
         return;
      }
      String herb = herbForBiome(biome, rand);
      if (herb == null) {
         return;
      }
      for (int i = 0; i < HERB_ATTEMPTS; i++) {
         int x = pos.getX() + rand.nextInt(7) - rand.nextInt(7);
         int z = pos.getZ() + rand.nextInt(7) - rand.nextInt(7);
         BlockPos genPos = w.topSolidOrLiquid(new BlockPos(x, 0, z));
         if (w.isAir(genPos) && sustains(herb, w.get(genPos.below()))) {
            w.set(genPos, block(herb).defaultBlockState());
         }
      }
   }

   private static void caveHerbs(DWorld w, Random rand, BlockPos pos) {
      String herb = rand.nextInt(2) == 0 ? "core_root" : "mooncap_mushroom";
      for (int i = 0; i < HERB_ATTEMPTS; i++) {
         int x = pos.getX() + rand.nextInt(7) - rand.nextInt(7);
         int z = pos.getZ() + rand.nextInt(7) - rand.nextInt(7);
         int y = rand.nextInt(32) + 10;
         BlockPos genPos = new BlockPos(x, y, z);
         if (w.isAir(genPos)) {
            int d = 0;
            while (w.isAir(genPos.below(d)) && genPos.getY() - d >= 4) {
               d++;
            }
            genPos = genPos.below(d - 1);
            if (sustains(herb, w.get(genPos.below()))) {
               w.set(genPos, block(herb).defaultBlockState());
            }
         }
      }
   }

   /** Herbs.getRandomHerbForBiome */
   private static String herbForBiome(Biome biome, Random rand) {
      List<String> herbs = new ArrayList<>();
      if (BiomeDict.hasType(biome, "JUNGLE")) {
         herbs.addAll(List.of("blood_orchid", "horsetail", "marsh_mallow", "mooncap_mushroom"));
      } else if (BiomeDict.hasType(biome, "SANDY") || BiomeDict.hasType(biome, "SAVANNA") || BiomeDict.hasType(biome, "MESA")) {
         herbs.add("aloe_vera");
      } else if (BiomeDict.hasType(biome, "MOUNTAIN") && !BiomeDict.hasType(biome, "SNOWY")) {
         herbs.addAll(List.of("wind_thistle", "cloudsbluff"));
      } else if (BiomeDict.hasType(biome, "SWAMP") && !BiomeDict.hasType(biome, "SNOWY")) {
         herbs.addAll(List.of("chamomile", "horsetail", "marsh_mallow"));
      } else if (BiomeDict.hasType(biome, "FOREST") && !BiomeDict.hasType(biome, "SNOWY")) {
         herbs.addAll(List.of("chamomile", "cohosh", "ginseng", "horsetail"));
      } else if (BiomeDict.hasType(biome, "PLAINS") && !BiomeDict.hasType(biome, "SNOWY")) {
         herbs.addAll(List.of("chamomile", "ginseng", "horsetail", "wind_thistle"));
      } else {
         return null;
      }
      return herbs.get(rand.nextInt(herbs.size()));
   }

   private static Block block(String herb) {
      return RLGen.RUSTIC_HERBS.get(herb).get();
   }

   /** Block.canSustainPlant for the herb's EnumPlantType (aloe vera: Desert, core root and mooncap: Cave, others: Plains). */
   private static boolean sustains(String herb, BlockState soil) {
      return switch (herb) {
         case "aloe_vera" -> soil.is(net.minecraft.tags.BlockTags.SAND) || soil.is(net.minecraft.tags.BlockTags.TERRACOTTA);
         case "core_root", "mooncap_mushroom" -> soil.isSolid();
         default -> isPlainsSoil(soil);
      };
   }

   private static boolean isPlainsSoil(BlockState s) {
      return s.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK) || s.is(net.minecraft.world.level.block.Blocks.DIRT)
         || s.is(net.minecraft.world.level.block.Blocks.COARSE_DIRT) || s.is(net.minecraft.world.level.block.Blocks.PODZOL)
         || s.is(net.minecraft.world.level.block.Blocks.FARMLAND);
   }
}
