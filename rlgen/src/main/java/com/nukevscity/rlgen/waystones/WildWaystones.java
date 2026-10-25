package com.nukevscity.rlgen.waystones;

import com.nukevscity.rlgen.deco.DWorld;
import com.nukevscity.rlgen.quark.QuarkGen;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * Waystones 4.1.0 LegacyWorldGen (IWorldGenerator weight 0) with RLCraft's "Change to generate just anywhere"=10: in 10 of 10000
 * chunks, up to 5 tries for a spot on the surface; a mossy waystone facing a random way. Waystones (1.21) supplies the block when it
 * is installed, otherwise a mossy stone stand-in.
 */
public final class WildWaystones {
   private static final float CHANCE = 10.0F / 10000.0F;

   private WildWaystones() {
   }

   public static void generate(DWorld w, int chunkX, int chunkZ) {
      Random random = QuarkGen.fmlRandom(w.seed(), chunkX, chunkZ);
      if (!(random.nextFloat() <= CHANCE)) {
         return;
      }
      for (int i = 0; i < 5; i++) {
         int blockX = chunkX * 16 + random.nextInt(16);
         int blockZ = chunkZ * 16 + random.nextInt(16);
         BlockPos pos = w.topSolidOrLiquid(new BlockPos(blockX, 0, blockZ));
         BlockPos posUp = pos.above();
         BlockState prev = w.get(pos);
         BlockState prevUp = w.get(posUp);
         // EnumFacing.values()[2 + nextInt(4)]: north, south, west, east
         Direction facing = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST}[random.nextInt(4)];
         if (!prev.is(Blocks.WATER) && prev.canBeReplaced() && prevUp.isAir()) {
            ResourceLocation id = ResourceLocation.parse("waystones:mossy_waystone");
            if (BuiltInRegistries.BLOCK.containsKey(id)) {
               Block b = BuiltInRegistries.BLOCK.get(id);
               w.set(pos, with(with(b.defaultBlockState(), "half", "lower"), "facing", facing.getSerializedName()));
               w.set(posUp, with(with(b.defaultBlockState(), "half", "upper"), "facing", facing.getSerializedName()));
            } else {
               w.set(pos, Blocks.MOSSY_STONE_BRICK_WALL.defaultBlockState());
               w.set(posUp, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
            }
            break;
         }
      }
   }

   private static BlockState with(BlockState s, String name, String value) {
      Property<?> p = s.getBlock().getStateDefinition().getProperty(name);
      return p == null ? s : set(s, p, value);
   }

   private static <T extends Comparable<T>> BlockState set(BlockState s, Property<T> p, String value) {
      return p.getValue(value).map(v -> s.setValue(p, v)).orElse(s);
   }
}
