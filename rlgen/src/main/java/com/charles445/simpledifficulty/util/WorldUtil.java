package com.charles445.simpledifficulty.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** 1.12 WorldUtil. */
public class WorldUtil {
   public static BlockPos getSidedBlockPos(Level world, Entity entity) {
      if (!world.isClientSide) {
         return getPosition(entity);
      } else if (entity instanceof Player) {
         return BlockPos.containing(entity.position().add(0.0, 0.5, 0.0));
      } else {
         return entity instanceof ItemFrame ? BlockPos.containing(entity.position().add(0.0, -0.45, 0.0)) : getPosition(entity);
      }
   }

   /** 1.12 Entity.getPosition(): the block at the feet + 0.5 */
   public static BlockPos getPosition(Entity entity) {
      return BlockPos.containing(entity.getX(), entity.getY() + 0.5, entity.getZ());
   }

   public static int calculateClientWorldEntityTemperature(Level world, Entity entity) {
      return TemperatureUtil.clampTemperature(TemperatureUtil.getWorldTemperature(world, getSidedBlockPos(world, entity)));
   }

   public static boolean isChunkLoaded(Level world, BlockPos pos) {
      if (world.isClientSide) {
         return true;
      } else {
         return world instanceof ServerLevel sl && sl.getChunkSource().hasChunk(pos.getX() >> 4, pos.getZ() >> 4);
      }
   }
}
