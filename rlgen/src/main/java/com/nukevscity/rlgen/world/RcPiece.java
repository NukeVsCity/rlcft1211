package com.nukevscity.rlgen.world;

import com.mojang.logging.LogUtils;
import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.rc.RcEnv;
import com.nukevscity.rlgen.rc.RcGenerator;
import com.nukevscity.rlgen.rc.RcTransform;
import com.nukevscity.rlgen.rc.RcWorldView;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.slf4j.Logger;

/** One RC structure (with its child structures), placed into each chunk it touches like RC's partial generation. */
public final class RcPiece extends StructurePiece {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Map<String, RcGenerator.Instance> CACHE = new LinkedHashMap<>(64, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<String, RcGenerator.Instance> eldest) {
         return this.size() > 48;
      }
   };

   private final String structure;
   private final String genId;
   private final long seed;
   private final RcTransform transform;
   private final BlockPos lower;
   private final int originX;
   private final int originZ;

   RcPiece(RcGenerator.Planned p, ChunkPos origin, BoundingBox extent) {
      super(RLGen.RC_PIECE.get(), 0, extent);
      this.structure = p.structure();
      this.genId = p.genId();
      this.seed = p.seed();
      this.transform = p.transform();
      this.lower = p.lower();
      this.originX = origin.x;
      this.originZ = origin.z;
      synchronized (CACHE) {
         CACHE.put(this.key(), p.instance());
      }
   }

   public RcPiece(StructurePieceSerializationContext context, CompoundTag tag) {
      super(RLGen.RC_PIECE.get(), tag);
      this.structure = tag.getString("Structure");
      this.genId = tag.getString("Gen");
      this.seed = tag.getLong("Seed");
      this.transform = RcTransform.from(tag.getInt("Rot"), tag.getBoolean("Mirror"));
      this.lower = new BlockPos(tag.getInt("LX"), tag.getInt("LY"), tag.getInt("LZ"));
      this.originX = tag.getInt("OX");
      this.originZ = tag.getInt("OZ");
   }

   @Override
   protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
      tag.putString("Structure", this.structure);
      tag.putString("Gen", this.genId);
      tag.putLong("Seed", this.seed);
      tag.putInt("Rot", this.transform.rotation());
      tag.putBoolean("Mirror", this.transform.mirrorX());
      tag.putInt("LX", this.lower.getX());
      tag.putInt("LY", this.lower.getY());
      tag.putInt("LZ", this.lower.getZ());
      tag.putInt("OX", this.originX);
      tag.putInt("OZ", this.originZ);
   }

   private String key() {
      return this.structure + "|" + this.seed + "|" + this.lower.asLong() + "|" + this.transform;
   }

   private RcGenerator.Instance instance(LegacyChunkGenerator generator) {
      String key = this.key();
      synchronized (CACHE) {
         RcGenerator.Instance inst = CACHE.get(key);
         if (inst != null) {
            return inst;
         }
      }
      RcGenerator.Instance inst = RcGenerator.prepareRoot(this.structure, this.genId, this.seed, this.transform, this.lower, generator.oracle());
      if (inst != null) {
         synchronized (CACHE) {
            CACHE.put(key, inst);
         }
      }
      return inst;
   }

   @Override
   public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource random, BoundingBox box,
                           ChunkPos chunkPos, BlockPos pivot) {
      if (!(chunkGenerator instanceof LegacyChunkGenerator generator)) {
         return;
      }
      try {
         RcGenerator.Instance inst = this.instance(generator);
         if (inst == null) {
            return;
         }
         LiveView view = new LiveView(level, chunkPos, generator.oracle());
         long s = chunkPos.x == this.originX && chunkPos.z == this.originZ ? this.seed : RcGenerator.chunkSeed(this.seed, chunkPos);
         RcGenerator.spawn(inst, view, box, s, new RcEnv(inst.env().biome, "natural", this.genId, null));
         view.finish();
      } catch (RuntimeException e) {
         LOGGER.error("rlgen: RC structure {} failed in chunk {}", this.structure, chunkPos, e);
      }
   }

   /** The real chunk (and its neighbours for reading); the 1.12 terrain oracle further away. */
   static final class LiveView implements RcWorldView {
      final WorldGenLevel level;
      final ChunkPos center;
      final RcGenerator.Oracle oracle;
      final List<BlockPos> written = new ArrayList<>();

      LiveView(WorldGenLevel level, ChunkPos center, RcGenerator.Oracle oracle) {
         this.level = level;
         this.center = center;
         this.oracle = oracle;
      }

      @Override
      public boolean isLive(BlockPos p) {
         return Math.abs((p.getX() >> 4) - this.center.x) <= 1 && Math.abs((p.getZ() >> 4) - this.center.z) <= 1
            && p.getY() >= this.level.getMinBuildHeight() && p.getY() < this.level.getMaxBuildHeight();
      }

      @Override
      public BlockState get(BlockPos p) {
         return this.isLive(p) ? this.level.getBlockState(p) : this.oracle.get(p);
      }

      @Override
      public boolean set(BlockPos p, BlockState s) {
         if (p.getY() < this.level.getMinBuildHeight() || p.getY() >= this.level.getMaxBuildHeight()) {
            return false;
         }
         this.level.setBlock(p, s, 2);
         this.written.add(p.immutable());
         return true;
      }

      @Override
      public WorldGenLevel level() {
         return this.level;
      }

      static boolean needsShape(Block b) {
         return b instanceof FenceBlock || b instanceof WallBlock || b instanceof IronBarsBlock || b instanceof StairBlock || b instanceof RedStoneWireBlock
            || b instanceof FenceGateBlock || b instanceof TripWireBlock || b instanceof SnowyDirtBlock;
      }

      boolean inCenter(BlockPos p) {
         return (p.getX() >> 4) == this.center.x && (p.getZ() >> 4) == this.center.z;
      }

      /** 1.12 worked out fence/pane/stair connections and double chests when drawing; 1.21 stores them in the block. */
      void finish() {
         for (BlockPos p : new java.util.LinkedHashSet<>(this.written)) {
            BlockState state = this.level.getBlockState(p);
            Block b = state.getBlock();
            if (needsShape(b)) {
               BlockState shaped = Block.updateFromNeighbourShapes(state, this.level, p);
               if (shaped != state && !shaped.isAir()) {
                  this.level.setBlock(p, shaped, 2 | 16);
               }
            } else if (b instanceof ChestBlock && state.getValue(ChestBlock.TYPE) == ChestType.SINGLE) {
               Direction facing = state.getValue(ChestBlock.FACING);
               BlockPos q = p.relative(facing.getClockWise());
               BlockState other = this.level.getBlockState(q);
               if (this.inCenter(q) && other.is(b) && other.getValue(ChestBlock.FACING) == facing && other.getValue(ChestBlock.TYPE) == ChestType.SINGLE) {
                  this.level.setBlock(p, state.setValue(ChestBlock.TYPE, ChestType.LEFT), 2 | 16);
                  this.level.setBlock(q, other.setValue(ChestBlock.TYPE, ChestType.RIGHT), 2 | 16);
               }
            }
         }
      }
   }
}
