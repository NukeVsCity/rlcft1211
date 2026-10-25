package com.lycanitesmobs.core.dungeon.instance;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import com.nukevscity.rlgen.lyc.dungeon.*;

import com.lycanitesmobs.core.dungeon.definition.DungeonSchematic;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import com.yungnickyoung.minecraft.bettermineshafts.mc.World;

public class DungeonInstance {
   public UUID uuid;
   public DungeonSchematic schematic;
   public boolean complete = false;
   public int chunksBuilt = 0;
   public BlockPos originPos;
   public ChunkPos chunkMin;
   public ChunkPos chunkMax;
   public World world;
   long seed = 0L;
   public Random random;
   public DungeonLayout layout;

   public void setOrigin(BlockPos blockPos) {
      this.originPos = blockPos;
   }

   /** worldRand stands for 1.12's world.rand, which picked the schematic and the seed. */
   public boolean init(World world, Random worldRand) {
      this.world = world;
      if (this.complete) {
         return true;
      } else if (this.world != null && this.originPos != null) {
         if (this.schematic == null) {
            List<DungeonSchematic> schematics = new ArrayList<>();

            for (DungeonSchematic schematic : com.lycanitesmobs.core.dungeon.DungeonManager.getInstance().schematics.values()) {
               if (schematic.canBuild(world, this.originPos)) {
                  schematics.add(schematic);
               }
            }

            if (schematics.isEmpty()) {
               return false;
            }

            if (schematics.size() == 1) {
               this.schematic = schematics.get(0);
            } else {
               this.schematic = schematics.get(worldRand.nextInt(schematics.size()));
            }
         }

         if (this.seed == 0L) {
            this.seed = worldRand.nextLong();
         }

         this.random = new Random(this.seed);
         this.originPos = this.schematic.placer.calculatePosition(world, this.originPos, this.random);
         
         if (this.layout == null) {
            this.layout = new DungeonLayout(this);
            this.layout.generate(this.random);
         }

         
         return true;
      } else {
         Lyc.warn("Dungeon", "Tried to initialise a dungeon with a missing world or origin. " + this);
         return false;
      }
   }

   public boolean isChunkPosWithin(ChunkPos chunkPos, int padding) {
      return chunkPos.x < this.chunkMin.x - padding || chunkPos.x > this.chunkMax.x + padding
         ? false
         : chunkPos.z >= this.chunkMin.z - padding && chunkPos.z <= this.chunkMax.z + padding;
   }

   /** The sectors 1.12 built when chunkPos was populated (its +8 area), with that populate's random. */
   public void buildChunk(World world, ChunkPos chunkPos, Random random) {
      if (this.layout != null && this.layout.sectorChunkMap.containsKey(chunkPos)) {
         for (SectorInstance sectorInstance : this.layout.sectorChunkMap.get(chunkPos)) {
            sectorInstance.build(world, chunkPos, random);
         }
      }
   }

   public long getSeed() {
      return this.seed;
   }

   public void setSeed(long seed) {
      this.seed = seed;
   }

   @Override
   public String toString() {
      String schematic = "";
      if (this.schematic != null) {
         schematic = " - Schematic: " + this.schematic.name;
      }

      String tpCommand = "/tp " + this.originPos.getX() + " " + (this.originPos.getY() + 2) + " " + this.originPos.getZ() + " ";
      return "Dungeon Instance"
         + schematic
         + " - TP Command: "
         + tpCommand
         + " - Origin: "
         + this.originPos
         + " - Complete: "
         + this.complete
         + " - Seed: "
         + this.seed
         + " - ID: "
         + this.uuid;
   }
}
