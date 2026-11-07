package com.nukevscity.rlgen.vanilla.monument;

import com.nukevscity.rlgen.deco.DWorld;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

/** 1.12.2 StructureComponent (the parts the monument uses), writing through {@link DWorld}. */
public abstract class StructureComponent {
   /** World.getSeaLevel(): ChunkGeneratorOverworld set it from ChunkGeneratorSettings.seaLevel */
   protected static final int SEA_LEVEL = 63;
   protected StructureBoundingBox boundingBox;
   @Nullable
   private Direction coordBaseMode;
   private Mirror mirror;
   private Rotation rotation;
   protected int componentType;

   public StructureComponent() {
   }

   protected StructureComponent(int i) {
      this.componentType = i;
   }

   public abstract boolean addComponentParts(DWorld world, Random random, StructureBoundingBox structureboundingbox);

   public StructureBoundingBox getBoundingBox() {
      return this.boundingBox;
   }

   public int getComponentType() {
      return this.componentType;
   }

   protected int getXWithOffset(int i, int j) {
      Direction facing = this.getCoordBaseMode();
      if (facing == null) {
         return i;
      } else {
         switch (facing) {
            case NORTH:
            case SOUTH:
               return this.boundingBox.minX + i;
            case WEST:
               return this.boundingBox.maxX - j;
            case EAST:
               return this.boundingBox.minX + j;
            default:
               return i;
         }
      }
   }

   protected int getYWithOffset(int i) {
      return this.getCoordBaseMode() == null ? i : i + this.boundingBox.minY;
   }

   protected int getZWithOffset(int i, int j) {
      Direction facing = this.getCoordBaseMode();
      if (facing == null) {
         return j;
      } else {
         switch (facing) {
            case NORTH:
               return this.boundingBox.maxZ - j;
            case SOUTH:
               return this.boundingBox.minZ + j;
            case WEST:
            case EAST:
               return this.boundingBox.minZ + i;
            default:
               return j;
         }
      }
   }

   protected void setBlockState(DWorld world, BlockState iblockstate, int i, int j, int k, StructureBoundingBox structureboundingbox) {
      BlockPos blockpos = new BlockPos(this.getXWithOffset(i, k), this.getYWithOffset(j), this.getZWithOffset(i, k));
      if (structureboundingbox.isVecInside(blockpos)) {
         if (this.mirror != Mirror.NONE) {
            iblockstate = iblockstate.mirror(this.mirror);
         }

         if (this.rotation != Rotation.NONE) {
            iblockstate = iblockstate.rotate(this.rotation);
         }

         world.set(blockpos, iblockstate);
      }
   }

   protected BlockState getBlockStateFromPos(DWorld world, int i, int j, int k, StructureBoundingBox structureboundingbox) {
      int l = this.getXWithOffset(i, k);
      int i1 = this.getYWithOffset(j);
      int j1 = this.getZWithOffset(i, k);
      BlockPos blockpos = new BlockPos(l, i1, j1);
      return !structureboundingbox.isVecInside(blockpos) ? Blocks.AIR.defaultBlockState() : world.get(blockpos);
   }

   protected void fillWithBlocks(
      DWorld world,
      StructureBoundingBox structureboundingbox,
      int i,
      int j,
      int k,
      int l,
      int i1,
      int j1,
      BlockState iblockstate,
      BlockState iblockstate1,
      boolean flag
   ) {
      for (int k1 = j; k1 <= i1; k1++) {
         for (int l1 = i; l1 <= l; l1++) {
            for (int i2 = k; i2 <= j1; i2++) {
               if (!flag || !this.getBlockStateFromPos(world, l1, k1, i2, structureboundingbox).isAir()) {
                  if (k1 != j && k1 != i1 && l1 != i && l1 != l && i2 != k && i2 != j1) {
                     this.setBlockState(world, iblockstate1, l1, k1, i2, structureboundingbox);
                  } else {
                     this.setBlockState(world, iblockstate, l1, k1, i2, structureboundingbox);
                  }
               }
            }
         }
      }
   }

   @SuppressWarnings("deprecation")
   protected void replaceAirAndLiquidDownwards(DWorld world, BlockState iblockstate, int i, int j, int k, StructureBoundingBox structureboundingbox) {
      int l = this.getXWithOffset(i, k);
      int i1 = this.getYWithOffset(j);
      int j1 = this.getZWithOffset(i, k);
      if (structureboundingbox.isVecInside(new BlockPos(l, i1, j1))) {
         while ((world.isAir(new BlockPos(l, i1, j1)) || world.get(new BlockPos(l, i1, j1)).liquid()) && i1 > 1) {
            world.set(new BlockPos(l, i1, j1), iblockstate);
            i1--;
         }
      }
   }

   public void offset(int i, int j, int k) {
      this.boundingBox.offset(i, j, k);
   }

   @Nullable
   public Direction getCoordBaseMode() {
      return this.coordBaseMode;
   }

   public void setCoordBaseMode(@Nullable Direction facing) {
      this.coordBaseMode = facing;
      if (facing == null) {
         this.rotation = Rotation.NONE;
         this.mirror = Mirror.NONE;
      } else {
         switch (facing) {
            case SOUTH:
               this.mirror = Mirror.LEFT_RIGHT;
               this.rotation = Rotation.NONE;
               break;
            case WEST:
               this.mirror = Mirror.LEFT_RIGHT;
               this.rotation = Rotation.CLOCKWISE_90;
               break;
            case EAST:
               this.mirror = Mirror.NONE;
               this.rotation = Rotation.CLOCKWISE_90;
               break;
            default:
               this.mirror = Mirror.NONE;
               this.rotation = Rotation.NONE;
         }
      }
   }
}
