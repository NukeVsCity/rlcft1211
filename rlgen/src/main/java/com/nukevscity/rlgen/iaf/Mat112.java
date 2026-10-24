package com.nukevscity.rlgen.iaf;

import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.block.LegacyPlantBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

/** The 1.12 Material of a 1.21 block state, as far as Ice and Fire's generators test it. */
enum Mat112 {
   AIR, GRASS, GROUND, SAND, ROCK, WOOD, LEAVES, PLANTS, VINE, WATER, LAVA, SNOW, CRAFTED_SNOW, ICE, PACKED_ICE, OTHER;

   static Mat112 of(BlockState s) {
      if (s.isAir()) {
         return AIR;
      }
      if (s.is(Blocks.WATER)) {
         return WATER;
      }
      if (s.is(Blocks.LAVA)) {
         return LAVA;
      }
      if (s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.MYCELIUM) || s.is(RLGen.GRASS_DEFILED.get()) || s.is(RLGen.CHARED_GRASS.get()) || s.is(RLGen.FROZEN_GRASS.get())) {
         return GRASS;
      }
      if (s.is(Blocks.DIRT) || s.is(Blocks.COARSE_DIRT) || s.is(Blocks.PODZOL) || s.is(Blocks.FARMLAND) || s.is(Blocks.DIRT_PATH)
         || s.is(RLGen.DIRT_DEFILED.get()) || s.is(RLGen.CHARED_DIRT.get()) || s.is(RLGen.FROZEN_DIRT.get()) || s.is(RLGen.CHARED_GRAVEL.get())
         || s.is(RLGen.FROZEN_GRAVEL.get()) || s.is(RLGen.CHARED_GRASS_PATH.get()) || s.is(RLGen.FROZEN_GRASS_PATH.get()) || s.getBlock() instanceof PileBlock) {
         return GROUND;
      }
      // BlockFalling is Material.SAND: sand, red sand, gravel, concrete powder, Defiled sand/gravel, ash
      if (s.is(BlockTags.SAND) || s.is(Blocks.GRAVEL) || s.is(RLGen.SAND_DEFILED.get()) || s.is(RLGen.GRAVEL_DEFILED.get()) || s.is(RLGen.ASH.get())
         || s.getBlock() instanceof net.minecraft.world.level.block.ConcretePowderBlock) {
         return SAND;
      }
      if (s.is(Blocks.SNOW)) {
         return SNOW;
      }
      if (s.is(Blocks.SNOW_BLOCK)) {
         return CRAFTED_SNOW;
      }
      if (s.is(Blocks.ICE) || s.is(Blocks.FROSTED_ICE)) {
         return ICE;
      }
      if (s.is(Blocks.PACKED_ICE) || s.is(Blocks.BLUE_ICE)) {
         return PACKED_ICE;
      }
      if (s.is(BlockTags.LEAVES) || s.getBlock() instanceof LeavesBlock) {
         return LEAVES;
      }
      if (s.is(RLGen.FROZEN_SPLINTERS.get())) {
         return WOOD;
      }
      SoundType sound = s.getSoundType();
      if (sound == SoundType.WOOD || sound == SoundType.CHERRY_WOOD || sound == SoundType.BAMBOO_WOOD || sound == SoundType.NETHER_WOOD
         || s.is(BlockTags.LOGS) || s.is(BlockTags.PLANKS)) {
         return WOOD;
      }
      // BlockBush is Material.PLANTS except tall grass, ferns, dead bushes and double plants (VINE)
      if (s.is(Blocks.SHORT_GRASS) || s.is(Blocks.FERN) || s.is(Blocks.DEAD_BUSH) || s.is(BlockTags.TALL_FLOWERS) || s.is(Blocks.TALL_GRASS)
         || s.is(Blocks.LARGE_FERN) || s.is(Blocks.VINE) || s.getBlock() instanceof LegacyPlantBlock) {
         return VINE;
      }
      if (s.getBlock() instanceof BushBlock || s.is(Blocks.SUGAR_CANE) || s.is(Blocks.COCOA) || s.is(Blocks.LILY_PAD)) {
         return PLANTS;
      }
      if (sound == SoundType.METAL || sound == SoundType.ANVIL || sound == SoundType.COPPER || sound == SoundType.NETHERITE_BLOCK) {
         return OTHER;
      }
      if (s.requiresCorrectToolForDrops() && s.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
         return ROCK;
      }
      return OTHER;
   }

   static float hardness(BlockState s) {
      return s.getDestroySpeed(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
   }

   /** Block.isOpaqueCube */
   static boolean opaqueCube(BlockState s) {
      return s.canOcclude() && s.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
   }

   /** World.isBlockFullCube: a full collision box. */
   static boolean fullCube(BlockState s) {
      return s.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
   }

   /** BlockDirt (dirt, coarse dirt, podzol) */
   static boolean dirt(BlockState s) {
      return s.is(Blocks.DIRT) || s.is(Blocks.COARSE_DIRT) || s.is(Blocks.PODZOL);
   }

   /** WorldGenMinable's StonePredicate / BlockMatcher.forBlock(STONE): stone and its 1.12 variants. */
   static boolean stone(BlockState s) {
      return s.is(Blocks.STONE) || s.is(Blocks.GRANITE) || s.is(Blocks.DIORITE) || s.is(Blocks.ANDESITE) || s.is(Blocks.POLISHED_GRANITE)
         || s.is(Blocks.POLISHED_DIORITE) || s.is(Blocks.POLISHED_ANDESITE);
   }

   /** WorldGenFireDragonRoosts.burnGround for one block, or null when it stays. */
   static BlockState burn(BlockState s) {
      if (hardness(s) == -1.0F) {
         return null;
      }
      Mat112 m = of(s);
      if (m == GRASS) {
         return RLGen.CHARED_GRASS.get().defaultBlockState();
      } else if (m == GROUND && dirt(s)) {
         return RLGen.CHARED_DIRT.get().defaultBlockState();
      } else if (m == GROUND && s.is(Blocks.GRAVEL)) {
         return RLGen.CHARED_GRAVEL.get().defaultBlockState();
      } else if (m != ROCK || !s.is(Blocks.COBBLESTONE)) {
         if (m == ROCK) {
            return RLGen.CHARED_STONE.get().defaultBlockState();
         } else if (s.is(Blocks.DIRT_PATH)) {
            return RLGen.CHARED_GRASS_PATH.get().defaultBlockState();
         } else if (m == WOOD) {
            return RLGen.ASH.get().defaultBlockState();
         } else if (m == LEAVES || m == PLANTS) {
            return Blocks.AIR.defaultBlockState();
         }
         return null;
      } else {
         return RLGen.CHARED_COBBLESTONE.get().defaultBlockState();
      }
   }

   /** WorldGenIceDragonRoosts.burnGround (freezing) for one block, or null when it stays. */
   static BlockState freeze(BlockState s, boolean sky) {
      if (hardness(s) == -1.0F) {
         return null;
      }
      Mat112 m = of(s);
      if (m != GRASS && (m != CRAFTED_SNOW || !sky)) {
         if ((m != GROUND || !dirt(s)) && (m != CRAFTED_SNOW || sky)) {
            if (m == GROUND && s.is(Blocks.GRAVEL)) {
               return RLGen.FROZEN_GRAVEL.get().defaultBlockState();
            } else if (m != ROCK || !s.is(Blocks.COBBLESTONE)) {
               if (m == ROCK) {
                  return RLGen.FROZEN_STONE.get().defaultBlockState();
               } else if (s.is(Blocks.DIRT_PATH)) {
                  return RLGen.FROZEN_GRASS_PATH.get().defaultBlockState();
               } else if (m == WOOD) {
                  return RLGen.FROZEN_SPLINTERS.get().defaultBlockState();
               } else if (m == LEAVES || m == PLANTS || m == SNOW) {
                  return Blocks.AIR.defaultBlockState();
               }
               return null;
            } else {
               return RLGen.FROZEN_COBBLESTONE.get().defaultBlockState();
            }
         } else {
            return RLGen.FROZEN_DIRT.get().defaultBlockState();
         }
      } else {
         return RLGen.FROZEN_GRASS.get().defaultBlockState();
      }
   }

   /** getTranslationKey().contains("chared") / ("frozen") */
   static boolean charred(BlockState s, boolean fire) {
      if (fire) {
         return s.is(RLGen.CHARED_DIRT.get()) || s.is(RLGen.CHARED_GRASS.get()) || s.is(RLGen.CHARED_STONE.get()) || s.is(RLGen.CHARED_COBBLESTONE.get())
            || s.is(RLGen.CHARED_GRAVEL.get()) || s.is(RLGen.CHARED_GRASS_PATH.get());
      }
      return s.is(RLGen.FROZEN_DIRT.get()) || s.is(RLGen.FROZEN_GRASS.get()) || s.is(RLGen.FROZEN_STONE.get()) || s.is(RLGen.FROZEN_COBBLESTONE.get())
         || s.is(RLGen.FROZEN_GRAVEL.get()) || s.is(RLGen.FROZEN_GRASS_PATH.get()) || s.is(RLGen.FROZEN_SPLINTERS.get());
   }
}
