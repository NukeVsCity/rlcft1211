package com.lycanitesmobs.compat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

/** 1.12 World helpers on the 1.21 API. */
public final class LycWorld {
   private LycWorld() {
   }

   /** 1.12 World.rayTraceBlocks(start, end): null when nothing was hit. */
   public static BlockHitResult rayTraceBlocks(Level level, Vec3 start, Vec3 end) {
      return rayTraceBlocks(level, start, end, false, false, false);
   }

   /** 1.12 World.rayTraceBlocks(start, end, stopOnLiquid) */
   public static BlockHitResult rayTraceBlocks(Level level, Vec3 start, Vec3 end, boolean stopOnLiquid) {
      return rayTraceBlocks(level, start, end, stopOnLiquid, false, false);
   }

   /** 1.12 World.rayTraceBlocks(start, end, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock) */
   public static BlockHitResult rayTraceBlocks(Level level, Vec3 start, Vec3 end, boolean stopOnLiquid, boolean ignoreNoBox, boolean returnLast) {
      BlockHitResult r = level.clip(new ClipContext(start, end, ignoreNoBox ? ClipContext.Block.COLLIDER : ClipContext.Block.OUTLINE,
         stopOnLiquid ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, CollisionContext.empty()));
      return r.getType() == HitResult.Type.MISS ? null : r;
   }

   /** 1.12 World.getCollisionBoxes(entity, box) */
   public static java.util.List<net.minecraft.world.phys.shapes.VoxelShape> getCollisionBoxes(Level level, Entity entity, net.minecraft.world.phys.AABB box) {
      java.util.List<net.minecraft.world.phys.shapes.VoxelShape> list = new java.util.ArrayList<>();
      for (net.minecraft.world.phys.shapes.VoxelShape shape : level.getCollisions(entity, box)) {
         list.add(shape);
      }
      return list;
   }

   public static BlockHitResult rayTraceBlocks(Level level, Vec3 start, Vec3 end, Entity entity) {
      BlockHitResult r = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
      return r.getType() == HitResult.Type.MISS ? null : r;
   }
}
