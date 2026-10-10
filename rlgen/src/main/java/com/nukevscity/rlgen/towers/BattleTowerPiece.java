package com.nukevscity.rlgen.towers;

import com.mojang.logging.LogUtils;
import com.nukevscity.rlgen.RLGen;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.material.FluidState;
import org.slf4j.Logger;

/**
 * One Battle Tower. The first chunk that places it reads the live world around the site, like 1.12 did when its IWorldGenerator ran
 * (surface height, tower type, underground or not); then every chunk of the tower replays the same build and keeps its own blocks.
 */
public final class BattleTowerPiece extends StructurePiece {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final int x;
   private final int z;
   private final long seed;
   private int y = Integer.MIN_VALUE;
   private int type = -1;
   private boolean underground;
   private boolean dead;

   BattleTowerPiece(int x, int z, long seed) {
      super(RLGen.BATTLE_TOWER_PIECE.get(), 0, new BoundingBox(x - 7, 0, z - 7, x + 6, 255, z + 6));
      this.x = x;
      this.z = z;
      this.seed = seed;
   }

   public BattleTowerPiece(StructurePieceSerializationContext context, CompoundTag tag) {
      super(RLGen.BATTLE_TOWER_PIECE.get(), tag);
      this.x = tag.getInt("TX");
      this.z = tag.getInt("TZ");
      this.seed = tag.getLong("Seed");
      this.y = tag.contains("TY") ? tag.getInt("TY") : Integer.MIN_VALUE;
      this.type = tag.getInt("Type");
      this.underground = tag.getBoolean("Underground");
      this.dead = tag.getBoolean("Dead");
   }

   @Override
   protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
      tag.putInt("TX", this.x);
      tag.putInt("TZ", this.z);
      tag.putLong("Seed", this.seed);
      if (this.y != Integer.MIN_VALUE) {
         tag.putInt("TY", this.y);
      }
      tag.putInt("Type", this.type);
      tag.putBoolean("Underground", this.underground);
      tag.putBoolean("Dead", this.dead);
   }

   private synchronized void decide(WorldGenLevel level) {
      if (this.type >= 0 || this.dead) {
         return;
      }
      TowerGen.Reader live = (bx, by, bz) -> level.getBlockState(new BlockPos(bx, by, bz));
      Random random = new Random(this.seed);
      int surface = TowerGen.surfaceHeight(live, this.x, this.z);
      if (surface <= 49) {
         this.dead = true;
         return;
      }
      this.y = surface;
      TowerGen.Type t = TowerGen.choose(live, random, this.x, surface, this.z, false);
      this.underground = random.nextInt(100) + 1 < 40; // chanceTowerIsUnderGround=40 in RLCraft
      this.type = t.ordinal();
   }

   @Override
   public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox box,
                           ChunkPos chunkPos, BlockPos pivot) {
      try {
         this.decide(level);
         if (this.dead) {
            return;
         }
         Clipped view = new Clipped(level, box);
         TowerGen.generate(view, new Random(this.seed ^ 0x2F7A1C93E5B4D681L), this.x, this.y, this.z, TowerGen.Type.values()[this.type], this.underground);
         view.flood();
      } catch (RuntimeException e) {
         LOGGER.error("rlgen: battle tower at {} {} failed in chunk {}", this.x, this.z, chunkPos, e);
      }
   }

   private static final class Clipped implements TowerGen.View {
      private final WorldGenLevel level;
      private final BoundingBox box;
      private final List<BlockPos> air = new ArrayList<>();

      Clipped(WorldGenLevel level, BoundingBox box) {
         this.level = level;
         this.box = box;
      }

      @Override
      public boolean inside(int x, int y, int z) {
         return this.box.isInside(x, y, z) && y >= this.level.getMinBuildHeight() && y < this.level.getMaxBuildHeight();
      }

      @Override
      public BlockState get(int x, int y, int z) {
         return this.inside(x, y, z) ? this.level.getBlockState(new BlockPos(x, y, z)) : Blocks.AIR.defaultBlockState();
      }

      @Override
      public void set(int x, int y, int z, BlockState state) {
         if (this.inside(x, y, z)) {
            BlockPos p = new BlockPos(x, y, z);
            this.level.setBlock(p, state, 2);
            if (state.isAir()) {
               this.air.add(p);
            }
         }
      }

      @Override
      public BlockEntity blockEntity(int x, int y, int z) {
         return this.level.getBlockEntity(new BlockPos(x, y, z));
      }

      @Override
      public ServerLevel level() {
         return this.level.getLevel();
      }

      /** 1.12 set the air with block updates, so water next to a tower poured in. */
      void flood() {
         for (BlockPos p : this.air) {
            if (!this.level.getBlockState(p).isAir()) {
               continue;
            }
            for (Direction d : new Direction[]{Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
               BlockPos n = p.relative(d);
               if (!this.inside(n.getX(), n.getY(), n.getZ())) {
                  continue;
               }
               FluidState f = this.level.getFluidState(n);
               if (!f.isEmpty()) {
                  this.level.scheduleTick(n, f.getType(), f.getType().getTickDelay(this.level));
                  break;
               }
            }
         }
      }
   }
}
