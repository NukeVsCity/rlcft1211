package com.lycanitesmobs.core.entity.navigate;

import com.google.common.collect.Sets;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.compat.LycPos;
import com.lycanitesmobs.compat.Material;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.Target;
import net.minecraft.world.phys.AABB;

/**
 * Lycanites 1.12 CreatureNodeProcessor (a WalkNodeProcessor with swimming and flying) on the 1.21 NodeEvaluator.
 * 1.12 names: init = prepare, postProcess = done, openPoint = getNode, findPathOptions = getNeighbors (1.21's PathFinder
 * applies the distance limit itself), getPathNodeType = getPathType. 1.12 cactus types are 1.21 DAMAGE_OTHER/DANGER_OTHER.
 */
public class CreatureNodeProcessor extends NodeEvaluator implements ICreatureNodeProcessor {
   public BaseCreatureEntity entityCreature;
   protected float avoidsWater;
   protected BlockGetter blockaccess;

   @Override
   public void prepare(PathNavigationRegion sourceIn, Mob mob) {
      super.prepare(sourceIn, mob);
      this.blockaccess = sourceIn;
      this.avoidsWater = mob.getPathfindingMalus(PathType.WATER);
      if (mob instanceof BaseCreatureEntity) {
         this.entityCreature = (BaseCreatureEntity)mob;
         this.updateEntitySize(mob);
      }
   }

   @Override
   public void updateEntitySize(Entity updateEntity) {
      this.entityWidth = Mth.floor(this.getWidth(updateEntity) + 1.0);
      this.entityHeight = Mth.floor(updateEntity.getBbHeight() + 1.0F);
      this.entityDepth = Mth.floor(this.getWidth(updateEntity) + 1.0);
   }

   @Override
   public void done() {
      if (this.entityCreature != null) {
         this.mob.setPathfindingMalus(PathType.WATER, this.avoidsWater);
      }

      super.done();
   }

   public boolean getCanSwim() {
      return this.entityCreature == null ? this.canFloat() : this.entityCreature.canWade() || this.entityCreature.isStrongSwimmer();
   }

   public boolean swimming() {
      if (this.entityCreature == null) {
         return false;
      } else {
         return this.entityCreature.isInWater() ? this.entityCreature.isStrongSwimmer() : false;
      }
   }

   public boolean flying() {
      return this.entityCreature != null && this.entityCreature.isFlying() && !this.entityCreature.isInWater();
   }

   public double getWidth() {
      return this.getWidth(this.mob);
   }

   public double getWidth(Entity entity) {
      return Math.min(3.0, (double)entity.getBbWidth());
   }

   protected Node openPoint(int x, int y, int z) {
      return this.getNode(x, y, z);
   }

   @Override
   public Target getTarget(double x, double y, double z) {
      return !this.flying() && !this.swimming()
         ? new Target(this.openPoint(Mth.floor(x), Mth.floor(y), Mth.floor(z)))
         : new Target(this.openPoint(Mth.floor(x), Mth.floor(y + 0.5), Mth.floor(z)));
   }

   @Override
   public Node getStart() {
      if (!this.flying() && !this.swimming()) {
         int posY;
         if (this.canFloat() && this.mob.isInWater()) {
            posY = (int)this.mob.getBoundingBox().minY;
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(Mth.floor(this.mob.getX()), posY, Mth.floor(this.mob.getZ()));

            for (Block block = this.blockaccess.getBlockState(blockpos$mutableblockpos).getBlock();
               block == Blocks.WATER;
               block = this.blockaccess.getBlockState(blockpos$mutableblockpos).getBlock()
            ) {
               blockpos$mutableblockpos.set(Mth.floor(this.mob.getX()), ++posY, Mth.floor(this.mob.getZ()));
            }
         } else if (this.mob.onGround()) {
            posY = Mth.floor(this.mob.getBoundingBox().minY + 0.5);
         } else {
            BlockPos blockpos = LycPos.of(this.mob);

            while (
               (
                     Material.of(this.blockaccess.getBlockState(blockpos)) == Material.AIR
                        || this.blockaccess.getBlockState(blockpos).isPathfindable(PathComputationType.LAND)
                  )
                  && blockpos.getY() > this.mob.level().getMinBuildHeight()
            ) {
               blockpos = blockpos.below();
            }

            posY = blockpos.above().getY();
         }

         BlockPos offsetXZ = LycPos.of(this.mob);
         PathType targetNodeType = this.getPathNodeType(this.mob, offsetXZ.getX(), posY, offsetXZ.getZ());
         if (this.mob.getPathfindingMalus(targetNodeType) < 0.0F) {
            Set<BlockPos> set = Sets.newHashSet();
            set.add(LycPos.of(this.mob.getBoundingBox().minX, (double)posY, this.mob.getBoundingBox().minZ));
            set.add(LycPos.of(this.mob.getBoundingBox().minX, (double)posY, this.mob.getBoundingBox().maxZ));
            set.add(LycPos.of(this.mob.getBoundingBox().maxX, (double)posY, this.mob.getBoundingBox().minZ));
            set.add(LycPos.of(this.mob.getBoundingBox().maxX, (double)posY, this.mob.getBoundingBox().maxZ));

            for (BlockPos betterPos : set) {
               PathType pathnodetype = this.getPathNodeType(this.mob, betterPos);
               if (this.mob.getPathfindingMalus(pathnodetype) >= 0.0F) {
                  return this.openPoint(betterPos.getX(), betterPos.getY(), betterPos.getZ());
               }
            }
         }

         return this.openPoint(offsetXZ.getX(), posY, offsetXZ.getZ());
      } else {
         return this.openPoint(
            Mth.floor(this.mob.getBoundingBox().minX), Mth.floor(this.mob.getBoundingBox().minY + 0.5), Mth.floor(this.mob.getBoundingBox().minZ)
         );
      }
   }

   private static boolean usable(Node node) {
      return node != null && !node.closed;
   }

   /** 1.12 findPathOptions (the 1.12 distanceTo(target) < maxDistance test is done by the 1.21 PathFinder). */
   @Override
   public int getNeighbors(Node[] pathOptions, Node currentPoint) {
      if (!this.flying() && !this.swimming()) {
         int i = 0;
         int j = 0;
         PathType pathnodetype = this.getPathNodeType(this.mob, currentPoint.x, currentPoint.y + 1, currentPoint.z);
         if (this.mob.getPathfindingMalus(pathnodetype) >= 0.0F) {
            j = Mth.floor(Math.max(1.0F, this.mob.maxUpStep()));
         }

         BlockPos blockpos = LycPos.of(currentPoint.x, currentPoint.y, currentPoint.z).below();
         double d0 = (double)currentPoint.y - (1.0 - this.blockMaxY(blockpos));
         Node pathpoint = this.getSafePoint(currentPoint.x, currentPoint.y, currentPoint.z + 1, j, d0, Direction.SOUTH);
         Node pathpoint1 = this.getSafePoint(currentPoint.x - 1, currentPoint.y, currentPoint.z, j, d0, Direction.WEST);
         Node pathpoint2 = this.getSafePoint(currentPoint.x + 1, currentPoint.y, currentPoint.z, j, d0, Direction.EAST);
         Node pathpoint3 = this.getSafePoint(currentPoint.x, currentPoint.y, currentPoint.z - 1, j, d0, Direction.NORTH);
         if (usable(pathpoint)) {
            pathOptions[i++] = pathpoint;
         }

         if (usable(pathpoint1)) {
            pathOptions[i++] = pathpoint1;
         }

         if (usable(pathpoint2)) {
            pathOptions[i++] = pathpoint2;
         }

         if (usable(pathpoint3)) {
            pathOptions[i++] = pathpoint3;
         }

         boolean flag = pathpoint3 == null || pathpoint3.type == PathType.OPEN || pathpoint3.costMalus != 0.0F;
         boolean flag1 = pathpoint == null || pathpoint.type == PathType.OPEN || pathpoint.costMalus != 0.0F;
         boolean flag2 = pathpoint2 == null || pathpoint2.type == PathType.OPEN || pathpoint2.costMalus != 0.0F;
         boolean flag3 = pathpoint1 == null || pathpoint1.type == PathType.OPEN || pathpoint1.costMalus != 0.0F;
         if (flag && flag3) {
            Node pathpoint4 = this.getSafePoint(currentPoint.x - 1, currentPoint.y, currentPoint.z - 1, j, d0, Direction.NORTH);
            if (usable(pathpoint4)) {
               pathOptions[i++] = pathpoint4;
            }
         }

         if (flag && flag2) {
            Node pathpoint5 = this.getSafePoint(currentPoint.x + 1, currentPoint.y, currentPoint.z - 1, j, d0, Direction.NORTH);
            if (usable(pathpoint5)) {
               pathOptions[i++] = pathpoint5;
            }
         }

         if (flag1 && flag3) {
            Node pathpoint6 = this.getSafePoint(currentPoint.x - 1, currentPoint.y, currentPoint.z + 1, j, d0, Direction.SOUTH);
            if (usable(pathpoint6)) {
               pathOptions[i++] = pathpoint6;
            }
         }

         if (flag1 && flag2) {
            Node pathpoint7 = this.getSafePoint(currentPoint.x + 1, currentPoint.y, currentPoint.z + 1, j, d0, Direction.SOUTH);
            if (usable(pathpoint7)) {
               pathOptions[i++] = pathpoint7;
            }
         }

         return i;
      } else {
         int ix = 0;

         for (Direction enumfacing : Direction.values()) {
            Node pathpointx;
            if (this.flying()) {
               pathpointx = this.getFlightNode(
                  currentPoint.x + enumfacing.getStepX(), currentPoint.y + enumfacing.getStepY(), currentPoint.z + enumfacing.getStepZ()
               );
            } else {
               pathpointx = this.getWaterNode(
                  currentPoint.x + enumfacing.getStepX(), currentPoint.y + enumfacing.getStepY(), currentPoint.z + enumfacing.getStepZ()
               );
            }

            if (usable(pathpointx)) {
               pathOptions[ix++] = pathpointx;
            }
         }

         return ix;
      }
   }

   /** 1.12 getBoundingBox(world, pos).maxY of the block (0 for no shape). */
   private double blockMaxY(BlockPos pos) {
      net.minecraft.world.phys.shapes.VoxelShape shape = this.blockaccess.getBlockState(pos).getCollisionShape(this.blockaccess, pos);
      return shape.isEmpty() ? 0.0 : shape.max(Direction.Axis.Y);
   }

   private boolean collidesWithAnyBlock(AABB box) {
      return !this.mob.level().noCollision(box);
   }

   private Node getSafePoint(int x, int y, int z, int p_186332_4_, double p_186332_5_, Direction facing) {
      Node pathpoint = null;
      BlockPos blockpos = new BlockPos(x, y, z);
      BlockPos blockpos1 = blockpos.below();
      double d0 = (double)y - (1.0 - this.blockMaxY(blockpos1));
      if (d0 - p_186332_5_ > 1.125) {
         return null;
      } else {
         PathType pathnodetype = this.getPathNodeType(this.mob, x, y, z);
         float f = this.mob.getPathfindingMalus(pathnodetype);
         double entityRadius = this.getWidth() / 2.0;
         if (f >= 0.0F) {
            pathpoint = this.openPoint(x, y, z);
            pathpoint.type = pathnodetype;
            pathpoint.costMalus = Math.max(pathpoint.costMalus, f);
         }

         if (pathnodetype == PathType.WALKABLE) {
            return pathpoint;
         } else {
            if (pathpoint == null && p_186332_4_ > 0 && pathnodetype != PathType.FENCE && pathnodetype != PathType.TRAPDOOR) {
               pathpoint = this.getSafePoint(x, y + 1, z, p_186332_4_ - 1, p_186332_5_, facing);
               if (pathpoint != null && (pathpoint.type == PathType.OPEN || pathpoint.type == PathType.WALKABLE) && this.getWidth() < 1.0) {
                  double d2 = (double)(x - facing.getStepX()) + 0.5;
                  double d3 = (double)(z - facing.getStepZ()) + 0.5;
                  AABB axisalignedbb = new AABB(
                     d2 - entityRadius, (double)y + 0.001, d3 - entityRadius, d2 + entityRadius, (double)((float)y + this.mob.getBbHeight()), d3 + entityRadius
                  );
                  double blockTop = this.blockMaxY(blockpos);
                  AABB axisalignedbb2 = axisalignedbb.expandTowards(0.0, blockTop - 0.002, 0.0);
                  if (this.collidesWithAnyBlock(axisalignedbb2)) {
                     pathpoint = null;
                  }
               }
            }

            if (pathnodetype == PathType.OPEN) {
               AABB axisalignedbb3 = new AABB(
                  (double)x - entityRadius + 0.5,
                  (double)y + 0.001,
                  (double)z - entityRadius + 0.5,
                  (double)x + entityRadius + 0.5,
                  (double)((float)y + this.mob.getBbHeight()),
                  (double)z + entityRadius + 0.5
               );
               if (this.collidesWithAnyBlock(axisalignedbb3)) {
                  return null;
               }

               if (this.getWidth() >= 1.0) {
                  PathType pathnodetype1 = this.getPathNodeType(this.mob, x, y - 1, z);
                  if (pathnodetype1 == PathType.BLOCKED) {
                     pathpoint = this.openPoint(x, y, z);
                     pathpoint.type = PathType.WALKABLE;
                     pathpoint.costMalus = Math.max(pathpoint.costMalus, f);
                     return pathpoint;
                  }
               }

               int i = 0;

               while (y > this.mob.level().getMinBuildHeight() && pathnodetype == PathType.OPEN) {
                  y--;
                  if (i++ >= this.mob.getMaxFallDistance()) {
                     return null;
                  }

                  pathnodetype = this.getPathNodeType(this.mob, x, y, z);
                  f = this.mob.getPathfindingMalus(pathnodetype);
                  if (pathnodetype != PathType.OPEN && f >= 0.0F) {
                     pathpoint = this.openPoint(x, y, z);
                     pathpoint.type = pathnodetype;
                     pathpoint.costMalus = Math.max(pathpoint.costMalus, f);
                     break;
                  }

                  if (f < 0.0F) {
                     return null;
                  }
               }
            }

            return pathpoint;
         }
      }
   }

   public PathType getPathNodeType(
      BlockGetter blockaccessIn, int x, int y, int z, Mob entitylivingIn, int xSize, int ySize, int zSize, boolean canBreakDoorsIn, boolean canEnterDoorsIn
   ) {
      if (this.swimming()) {
         return PathType.WATER;
      } else {
         EnumSet<PathType> enumset = EnumSet.noneOf(PathType.class);
         PathType pathnodetype = PathType.BLOCKED;
         BlockPos blockpos = LycPos.of(entitylivingIn);

         for (int i = 0; i < xSize; i++) {
            for (int j = 0; j < ySize; j++) {
               for (int k = 0; k < zSize; k++) {
                  int l = i + x;
                  int i1 = j + y;
                  int j1 = k + z;
                  PathType pathnodetype1 = this.getPathNodeType(blockaccessIn, l, i1, j1);
                  if (pathnodetype1 == PathType.DOOR_WOOD_CLOSED && canBreakDoorsIn && canEnterDoorsIn) {
                     pathnodetype1 = PathType.WALKABLE;
                  }

                  if (pathnodetype1 == PathType.DOOR_OPEN && !canEnterDoorsIn) {
                     pathnodetype1 = PathType.BLOCKED;
                  }

                  if (pathnodetype1 == PathType.RAIL
                     && !(blockaccessIn.getBlockState(blockpos).getBlock() instanceof BaseRailBlock)
                     && !(blockaccessIn.getBlockState(blockpos.below()).getBlock() instanceof BaseRailBlock)) {
                     pathnodetype1 = PathType.FENCE;
                  }

                  if (i == 0 && j == 0 && k == 0) {
                     pathnodetype = pathnodetype1;
                  }

                  enumset.add(pathnodetype1);
               }
            }
         }

         if (enumset.contains(PathType.FENCE)) {
            return PathType.FENCE;
         } else {
            PathType pathnodetype2 = PathType.BLOCKED;

            for (PathType pathnodetype3 : enumset) {
               if (entitylivingIn.getPathfindingMalus(pathnodetype3) < 0.0F) {
                  return pathnodetype3;
               }

               if (entitylivingIn.getPathfindingMalus(pathnodetype3) >= entitylivingIn.getPathfindingMalus(pathnodetype2)) {
                  pathnodetype2 = pathnodetype3;
               }
            }

            return pathnodetype == PathType.OPEN && entitylivingIn.getPathfindingMalus(pathnodetype2) == 0.0F ? PathType.OPEN : pathnodetype2;
         }
      }
   }

   public PathType getPathNodeType(Mob entitylivingIn, BlockPos pos) {
      return this.swimming() ? PathType.WATER : this.getPathNodeType(entitylivingIn, pos.getX(), pos.getY(), pos.getZ());
   }

   public PathType getPathNodeType(Mob entitylivingIn, int x, int y, int z) {
      return this.swimming()
         ? PathType.WATER
         : this.getPathNodeType(
            this.blockaccess, x, y, z, entitylivingIn, this.entityWidth, this.entityHeight, this.entityDepth, this.canOpenDoors(), this.canPassDoors()
         );
   }

   @Override
   public PathType getPathTypeOfMob(PathfindingContext context, int x, int y, int z, Mob mob) {
      return this.getPathNodeType(mob, x, y, z);
   }

   @Override
   public PathType getPathType(PathfindingContext context, int x, int y, int z) {
      return this.getPathNodeType(context.level(), x, y, z);
   }

   public PathType getPathNodeType(BlockGetter blockaccessIn, int x, int y, int z) {
      if (this.swimming()) {
         return PathType.WATER;
      } else {
         PathType pathnodetype = this.getPathNodeTypeRaw(blockaccessIn, x, y, z);
         if (pathnodetype == PathType.OPEN && y >= this.minY() + 1) {
            Block block = blockaccessIn.getBlockState(new BlockPos(x, y - 1, z)).getBlock();
            PathType pathnodetype1 = this.getPathNodeTypeRaw(blockaccessIn, x, y - 1, z);
            pathnodetype = pathnodetype1 != PathType.WALKABLE
                  && pathnodetype1 != PathType.OPEN
                  && pathnodetype1 != PathType.WATER
                  && pathnodetype1 != PathType.LAVA
               ? PathType.WALKABLE
               : PathType.OPEN;
            if (pathnodetype1 == PathType.DAMAGE_FIRE || block == Blocks.MAGMA_BLOCK) {
               pathnodetype = PathType.DAMAGE_FIRE;
            }

            if (pathnodetype1 == PathType.DAMAGE_OTHER) {
               pathnodetype = PathType.DAMAGE_OTHER;
            }
         }

         BlockPos.MutableBlockPos blockpos$pooledmutableblockpos = new BlockPos.MutableBlockPos();
         if (pathnodetype == PathType.WALKABLE) {
            for (int j = -1; j <= 1; j++) {
               for (int i = -1; i <= 1; i++) {
                  if (j != 0 || i != 0) {
                     BlockState state1 = blockaccessIn.getBlockState(blockpos$pooledmutableblockpos.set(j + x, y, i + z));
                     Block block1 = state1.getBlock();
                     if (block1 == Blocks.CACTUS) {
                        pathnodetype = PathType.DANGER_OTHER;
                     } else if (block1 == Blocks.FIRE) {
                        pathnodetype = PathType.DANGER_FIRE;
                     } else if (NodeEvaluator.isBurningBlock(state1)) {
                        pathnodetype = PathType.DAMAGE_FIRE;
                     }
                  }
               }
            }
         }

         return pathnodetype;
      }
   }

   private int minY() {
      return this.mob != null ? this.mob.level().getMinBuildHeight() : 0;
   }

   protected PathType getPathNodeTypeRaw(BlockGetter blockAccess, int x, int y, int z) {
      BlockPos blockpos = new BlockPos(x, y, z);
      BlockState iblockstate = blockAccess.getBlockState(blockpos);
      Block block = iblockstate.getBlock();
      Material material = Material.of(iblockstate);
      PathType type = iblockstate.getBlockPathType(blockAccess, blockpos, this.mob);
      if (type != null) {
         return type;
      } else if (material == Material.AIR) {
         return PathType.OPEN;
      } else if (block == Blocks.OAK_TRAPDOOR || block == Blocks.IRON_TRAPDOOR || block == Blocks.LILY_PAD
         || iblockstate.getBlock() instanceof net.minecraft.world.level.block.TrapDoorBlock) {
         return PathType.TRAPDOOR;
      } else if (block == Blocks.FIRE) {
         return PathType.DAMAGE_FIRE;
      } else if (block == Blocks.CACTUS) {
         return PathType.DAMAGE_OTHER;
      } else if (block instanceof DoorBlock && material == Material.WOOD && !iblockstate.getValue(DoorBlock.OPEN)) {
         return PathType.DOOR_WOOD_CLOSED;
      } else if (block instanceof DoorBlock && material == Material.IRON && !iblockstate.getValue(DoorBlock.OPEN)) {
         return PathType.DOOR_IRON_CLOSED;
      } else if (block instanceof DoorBlock && iblockstate.getValue(DoorBlock.OPEN)) {
         return PathType.DOOR_OPEN;
      } else if (block instanceof BaseRailBlock) {
         return PathType.RAIL;
      } else if (!(block instanceof FenceBlock) && !(block instanceof WallBlock) && (!(block instanceof FenceGateBlock) || iblockstate.getValue(FenceGateBlock.OPEN))) {
         if (material == Material.WATER) {
            return PathType.WATER;
         } else if (material == Material.LAVA) {
            return PathType.LAVA;
         } else {
            return iblockstate.isPathfindable(PathComputationType.LAND) ? PathType.OPEN : PathType.BLOCKED;
         }
      } else {
         return PathType.FENCE;
      }
   }

   protected Node getFlightNode(int x, int y, int z) {
      PathType pathnodetype = this.isFlyablePathNode(x, y, z);
      if (this.entityCreature != null && this.entityCreature.isStrongSwimmer() && pathnodetype == PathType.WATER) {
         return this.openPoint(x, y, z);
      } else {
         return pathnodetype == PathType.OPEN ? this.openPoint(x, y, z) : null;
      }
   }

   protected PathType isFlyablePathNode(int x, int y, int z) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for (int i = x; i < x + this.entityWidth; i++) {
         for (int j = y; j < y + this.entityHeight; j++) {
            for (int k = z; k < z + this.entityDepth; k++) {
               BlockState iblockstate = this.blockaccess.getBlockState(blockpos$mutableblockpos.set(i, j, k));
               Material material = Material.of(iblockstate);
               if (!material.isSolid() && !material.isLiquid()) {
                  return PathType.OPEN;
               }

               if (material != Material.AIR) {
                  if (this.entityCreature != null && this.entityCreature.isStrongSwimmer()) {
                     return this.isSwimmablePathNode(x, y, z);
                  }

                  return PathType.BLOCKED;
               }
            }
         }
      }

      return PathType.OPEN;
   }

   private Node getWaterNode(int x, int y, int z) {
      PathType pathnodetype;
      if (this.entityCreature != null && this.entityCreature.isFlying()) {
         pathnodetype = this.isFlyablePathNode(x, y, z);
         if (pathnodetype == PathType.OPEN) {
            return this.openPoint(x, y, z);
         }
      } else {
         pathnodetype = this.isSwimmablePathNode(x, y, z);
      }

      return pathnodetype == PathType.WATER ? this.openPoint(x, y, z) : null;
   }

   private PathType isSwimmablePathNode(int x, int y, int z) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for (int i = x; i < x + this.entityWidth; i++) {
         for (int j = y; j < y + Math.min(this.entityHeight, 3); j++) {
            for (int k = z; k < z + this.entityDepth; k++) {
               BlockState iblockstate = this.blockaccess.getBlockState(blockpos$mutableblockpos.set(i, j, k));
               if (this.entityCreature == null) {
                  return PathType.BLOCKED;
               }

               if (!this.entityCreature.waterDamage()) {
                  if (Material.of(iblockstate) != Material.WATER) {
                     return PathType.BLOCKED;
                  }
               } else if (!this.entityCreature.canBurn()) {
                  if (Material.of(iblockstate) != Material.LAVA) {
                     return PathType.BLOCKED;
                  }
               } else if (!this.entityCreature.canFreeze() && iblockstate.getBlock() != ObjectManager.getBlock("ooze")) {
                  return PathType.BLOCKED;
               }
            }
         }
      }

      return PathType.WATER;
   }
}
