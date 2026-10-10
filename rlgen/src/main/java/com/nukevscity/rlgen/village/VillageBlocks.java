package com.nukevscity.rlgen.village;

import java.util.Set;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * StructureVillagePieces.Village.getBiomeSpecificBlockState (structure types 1 desert, 2 savanna, 3 taiga) with Forge's
 * GetVillageBlockID event as Traverse answered it (red desert, desert shrubland, arid highland).
 */
public final class VillageBlocks {
   /** Traverse DESERT_REPLACEMENTS biomes; red_desert has its own table. */
   private static final Set<String> TRAVERSE_DESERT = Set.of("traverse:desert_shrubland", "traverse:arid_highland");

   private VillageBlocks() {
   }

   private static boolean isLog(BlockState s) {
      // 1.12 Blocks.LOG / LOG2: the six vanilla wood logs
      Block b = s.getBlock();
      return b == Blocks.OAK_LOG || b == Blocks.SPRUCE_LOG || b == Blocks.BIRCH_LOG || b == Blocks.JUNGLE_LOG || b == Blocks.ACACIA_LOG
         || b == Blocks.DARK_OAK_LOG;
   }

   private static boolean isPlanks(BlockState s) {
      Block b = s.getBlock();
      return b == Blocks.OAK_PLANKS || b == Blocks.SPRUCE_PLANKS || b == Blocks.BIRCH_PLANKS || b == Blocks.JUNGLE_PLANKS || b == Blocks.ACACIA_PLANKS
         || b == Blocks.DARK_OAK_PLANKS;
   }

   private static BlockState stairs(Block to, BlockState from) {
      return to.defaultBlockState().setValue(StairBlock.FACING, from.getValue(StairBlock.FACING));
   }

   /** @param biome the start piece's 1.12 biome name, or null (Forge passes null once the start piece is gone) */
   public static BlockState biomeSpecific(BlockState s, int structureType, String biome) {
      if ("traverse:red_desert".equals(biome)) {
         if (isLog(s) || s.is(Blocks.COBBLESTONE) || s.is(Blocks.GRAVEL)) {
            return Blocks.RED_SANDSTONE.defaultBlockState();
         } else if (isPlanks(s)) {
            return Blocks.CUT_RED_SANDSTONE.defaultBlockState();
         } else if (s.is(Blocks.OAK_STAIRS) || s.is(Blocks.COBBLESTONE_STAIRS)) {
            return stairs(Blocks.RED_SANDSTONE_STAIRS, s);
         }
         return s;
      }
      if (biome != null && TRAVERSE_DESERT.contains(biome)) {
         return desert(s);
      }
      if (structureType == 1) {
         return desert(s);
      } else if (structureType == 3) {
         if (isLog(s)) {
            return Blocks.SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, s.getValue(RotatedPillarBlock.AXIS));
         }
         if (isPlanks(s)) {
            return Blocks.SPRUCE_PLANKS.defaultBlockState();
         }
         if (s.is(Blocks.OAK_STAIRS)) {
            return stairs(Blocks.SPRUCE_STAIRS, s);
         }
         if (s.is(Blocks.OAK_FENCE)) {
            return Blocks.SPRUCE_FENCE.defaultBlockState();
         }
      } else if (structureType == 2) {
         if (isLog(s)) {
            return Blocks.ACACIA_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, s.getValue(RotatedPillarBlock.AXIS));
         }
         if (isPlanks(s)) {
            return Blocks.ACACIA_PLANKS.defaultBlockState();
         }
         if (s.is(Blocks.OAK_STAIRS)) {
            return stairs(Blocks.ACACIA_STAIRS, s);
         }
         if (s.is(Blocks.COBBLESTONE)) {
            return Blocks.ACACIA_LOG.defaultBlockState();
         }
         if (s.is(Blocks.OAK_FENCE)) {
            return Blocks.ACACIA_FENCE.defaultBlockState();
         }
      }
      return s;
   }

   /** Vanilla desert villages; Traverse's DESERT_REPLACEMENTS are the same swaps. */
   private static BlockState desert(BlockState s) {
      if (isLog(s)) {
         return Blocks.SANDSTONE.defaultBlockState();
      }
      if (s.is(Blocks.COBBLESTONE)) {
         return Blocks.SANDSTONE.defaultBlockState();
      }
      if (isPlanks(s)) {
         return Blocks.CUT_SANDSTONE.defaultBlockState();
      }
      if (s.is(Blocks.OAK_STAIRS) || s.is(Blocks.COBBLESTONE_STAIRS)) {
         return stairs(Blocks.SANDSTONE_STAIRS, s);
      }
      if (s.is(Blocks.GRAVEL)) {
         return Blocks.SANDSTONE.defaultBlockState();
      }
      return s;
   }
}
