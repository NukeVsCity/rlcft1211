package com.nukevscity.rlgen.vanilla.mansion;

import com.nukevscity.rlgen.deco.DWorld;
import com.nukevscity.rlgen.legacy.ChunkPrimer;
import com.nukevscity.rlgen.legacy.LegacyGenerator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;

/** 1.12 WoodlandMansion.Start */
public final class MansionStart {
   final int chunkX;
   final int chunkZ;
   final List<MansionTemplate> components = new LinkedList<>();
   Box boundingBox;
   final boolean isValid;

   MansionStart(LegacyGenerator legacy, Random random, int i, int j) {
      this.chunkX = i;
      this.chunkZ = j;
      Rotation rotation = Rotation.values()[random.nextInt(Rotation.values().length)];
      ChunkPrimer chunkprimer = new ChunkPrimer();
      synchronized (LegacyGenerator.LOCK) {
         legacy.setBlocksInChunk(i, j, chunkprimer);
      }
      int k = 5;
      int l = 5;
      if (rotation == Rotation.CLOCKWISE_90) {
         k = -5;
      } else if (rotation == Rotation.CLOCKWISE_180) {
         k = -5;
         l = -5;
      } else if (rotation == Rotation.COUNTERCLOCKWISE_90) {
         l = -5;
      }

      int i1 = findGroundBlockIdx(chunkprimer, 7, 7);
      int j1 = findGroundBlockIdx(chunkprimer, 7, 7 + l);
      int k1 = findGroundBlockIdx(chunkprimer, 7 + k, 7);
      int l1 = findGroundBlockIdx(chunkprimer, 7 + k, 7 + l);
      int i2 = Math.min(Math.min(i1, j1), Math.min(k1, l1));
      if (i2 < 60) {
         this.isValid = false;
      } else {
         BlockPos blockpos = new BlockPos(i * 16 + 8, i2 + 1, j * 16 + 8);
         List<MansionTemplate> list = new LinkedList<>();
         WoodlandMansionPieces.generateMansion(MansionTemplate.SIZES, blockpos, rotation, list, random);
         this.components.addAll(list);
         this.updateBoundingBox();
         this.isValid = true;
      }
   }

   /**
    * 1.12 ChunkPrimer.findGroundBlockIdx, with its off-by-255 index: it reads data[(x << 12 | z << 8) + 255 + y], i.e. column z + 1
    * one block lower, so the result is the top of the next column + 1.
    */
   static int findGroundBlockIdx(ChunkPrimer primer, int x, int z) {
      int k = (x << 12 | z << 8) + 256 - 1;
      for (int l = 255; l >= 0; l--) {
         if (primer.data[k + l] != 0) {
            return l;
         }
      }
      return 0;
   }

   private void updateBoundingBox() {
      this.boundingBox = Box.getNewBoundingBox();
      for (MansionTemplate c : this.components) {
         this.boundingBox.expandTo(c.boundingBox);
      }
   }

   /** StructureStart.generateStructure + WoodlandMansion.Start.generateStructure (cobblestone under the first floor). */
   void generateStructure(DWorld world, Random random, Box box) {
      for (MansionTemplate c : this.components) {
         if (c.boundingBox.intersectsWith(box)) {
            c.addComponentParts(world, random, box);
         }
      }

      int i = this.boundingBox.minY;
      for (int j = box.minX; j <= box.maxX; j++) {
         for (int k = box.minZ; k <= box.maxZ; k++) {
            BlockPos blockpos = new BlockPos(j, i, k);
            if (!world.isAir(blockpos) && this.boundingBox.isVecInside(blockpos)) {
               boolean flag = false;
               for (MansionTemplate c : this.components) {
                  if (c.boundingBox.isVecInside(blockpos)) {
                     flag = true;
                     break;
                  }
               }

               if (flag) {
                  for (int l = i - 1; l > 1; l--) {
                     BlockPos blockpos1 = new BlockPos(j, l, k);
                     if (!world.isAir(blockpos1) && !DWorld.isLiquid(world.get(blockpos1))) {
                        break;
                     }
                     world.set(blockpos1, Blocks.COBBLESTONE.defaultBlockState());
                  }
               }
            }
         }
      }
   }
}
