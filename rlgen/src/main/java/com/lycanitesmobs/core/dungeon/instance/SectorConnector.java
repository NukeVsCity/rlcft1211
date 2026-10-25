package com.lycanitesmobs.core.dungeon.instance;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import com.nukevscity.rlgen.lyc.dungeon.*;

import java.util.Random;
import com.yungnickyoung.minecraft.bettermineshafts.mc.World;

public class SectorConnector {
   public BlockPos position;
   public SectorInstance parentSector;
   public SectorInstance childSector;
   public Direction facing = Direction.SOUTH;
   public boolean closed = false;
   public int level = 0;

   public SectorConnector(BlockPos position, SectorInstance parentSector, int level, Direction facing) {
      this.position = position;
      this.parentSector = parentSector;
      this.level = level;
      this.facing = facing;
   }

   public boolean canConnect(DungeonLayout dungeonLayout, SectorInstance sectorInstance) {
      if (this.closed) {
         return false;
      } else if (sectorInstance == null) {
         return this.facing != Direction.UP;
      } else if ("tower".equalsIgnoreCase(sectorInstance.dungeonSector.type)) {
         return this.facing == Direction.UP;
      } else if (this.facing == Direction.UP) {
         return false;
      } else {
         boolean tempConnector = false;
         if (sectorInstance.parentConnector == null) {
            sectorInstance.parentConnector = this;
            tempConnector = true;
         }

         for (SectorInstance nearbySector : sectorInstance.getNearbySectors()) {
            if (sectorInstance.collidesWith(nearbySector)) {
               if (tempConnector) {
                  sectorInstance.parentConnector = null;
               }

               return false;
            }
         }

         if (tempConnector) {
            sectorInstance.parentConnector = null;
         }

         return true;
      }
   }

   public void buildEntrance(World world, ChunkPos chunkPos, Random random) {
      SectorInstance sectorInstance = this.parentSector;
      if (sectorInstance == null) {
         sectorInstance = this.childSector;
      }

      if (sectorInstance != null) {
         Vec3i size = sectorInstance.getRoomSize();
         if (this.childSector != null && sectorInstance != this.childSector) {
            Vec3i childSize = this.childSector.getRoomSize();
            size = new Vec3i(
               Math.min(size.getX(), childSize.getX()),
               Math.min(size.getY(), childSize.getY()),
               Math.min(size.getZ(), childSize.getZ())
            );
         }

         int entranceHeight = 2;
         int entranceRadius = 1;
         int startX = this.position.getX();
         int stopX = this.position.getX();
         int startY = Math.max(1, this.position.getY() + 1);
         int stopY = Math.min(256 - 1, this.position.getY() + 1 + entranceHeight);
         int startZ = this.position.getZ();
         int stopZ = this.position.getZ();
         if (this.facing.getAxis() == Direction.Axis.Z) {
            startX -= entranceRadius;
            stopX += entranceRadius;
            if (size.getX() % 2 != 0) {
               startX--;
            }

            if (this.facing == Direction.SOUTH) {
               startZ -= 2;
               stopZ++;
            } else {
               startZ--;
               stopZ += 2;
            }
         } else {
            startZ -= entranceRadius;
            stopZ += entranceRadius;
            if (size.getZ() % 2 != 0) {
               startZ--;
            }

            if (this.facing == Direction.EAST) {
               startX -= 2;
               stopX++;
            } else {
               startX--;
               stopX += 2;
            }
         }

         MutableBlockPos buildPos = new MutableBlockPos();

         for (int x = startX; x <= stopX; x++) {
            for (int y = startY; y <= stopY; y++) {
               for (int z = startZ; z <= stopZ; z++) {
                  buildPos.set(x, y, z);
                  if (!world.hasTileEntity(buildPos)) {
                     BlockState blockState = sectorInstance.getAirBlockForPos(buildPos);
                     if (blockState != null) {
                        sectorInstance.placeBlock(world, chunkPos, buildPos, blockState, this.facing, random);
                     }
                  }
               }
            }
         }
      }
   }

   public void buildTest(World world, ChunkPos chunkPos, Random random) {
      SectorInstance sectorInstance = this.parentSector;
      if (sectorInstance == null) {
         sectorInstance = this.childSector;
      }

      if (sectorInstance != null) {
         sectorInstance.placeBlock(world, chunkPos, this.position.offset(0, 1, 0), Blocks.GOLD_BLOCK.defaultBlockState(), Direction.SOUTH, random);
         if (this.facing == Direction.SOUTH) {
            for (int z = 1; z <= 3; z++) {
               sectorInstance.placeBlock(
                  world, chunkPos, this.position.offset(0, 1, z), Blocks.REDSTONE_BLOCK.defaultBlockState(), Direction.SOUTH, random
               );
            }
         } else if (this.facing == Direction.EAST) {
            for (int x = 1; x <= 3; x++) {
               sectorInstance.placeBlock(
                  world, chunkPos, this.position.offset(x, 1, 0), Blocks.REDSTONE_BLOCK.defaultBlockState(), Direction.SOUTH, random
               );
            }
         } else if (this.facing == Direction.NORTH) {
            for (int z = -1; z >= -3; z--) {
               sectorInstance.placeBlock(
                  world, chunkPos, this.position.offset(0, 1, z), Blocks.REDSTONE_BLOCK.defaultBlockState(), Direction.SOUTH, random
               );
            }
         } else {
            for (int x = -1; x >= -3; x--) {
               sectorInstance.placeBlock(
                  world, chunkPos, this.position.offset(x, 1, 0), Blocks.REDSTONE_BLOCK.defaultBlockState(), Direction.SOUTH, random
               );
            }
         }
      }
   }
}
