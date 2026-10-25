package com.yungnickyoung.minecraft.bettermineshafts.mc;

import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The 1.12 World as the mineshaft pieces saw it while one 1.12 chunk was populated. Writes are buffered so later reads in the same
 * populate see them, like 1.12; afterwards only the blocks, block entities and entities inside the 1.21 chunk being decorated are kept
 * (the other chunks of the 1.12 populate area get theirs when they are decorated, from the same run).
 */
public class World {
   public final WorldGenLevel level;
   private final LegacyChunkGenerator generator;
   private final ChunkPos chunk;
   private final Long2ObjectLinkedOpenHashMap<BlockState> blocks = new Long2ObjectLinkedOpenHashMap<>();
   private final List<BlockEntityAction> actions = new ArrayList<>();
   private final List<Entity> entities = new ArrayList<>();

   private record BlockEntityAction(BlockPos pos, Consumer<BlockEntity> action) {
   }

   public World(WorldGenLevel level, LegacyChunkGenerator generator, ChunkPos chunk) {
      this.level = level;
      this.generator = generator;
      this.chunk = chunk;
   }

   public boolean inChunk(BlockPos pos) {
      return (pos.getX() >> 4) == this.chunk.x && (pos.getZ() >> 4) == this.chunk.z;
   }

   /** The 1.21 region reaches one chunk around the decorated one; anything further reads as the 1.12 terrain. */
   private boolean readable(BlockPos pos) {
      return Math.abs((pos.getX() >> 4) - this.chunk.x) <= 1 && Math.abs((pos.getZ() >> 4) - this.chunk.z) <= 1;
   }

   public BlockState getBlockState(BlockPos pos) {
      if (pos.getY() < 0 || pos.getY() > 255) {
         return Blocks.AIR.defaultBlockState();
      }
      BlockState s = this.blocks.get(pos.asLong());
      if (s != null) {
         return s;
      }
      return this.readable(pos) ? this.level.getBlockState(pos) : this.generator.legacyState(pos);
   }

   public boolean setBlockState(BlockPos pos, BlockState state, int flags) {
      if (pos.getY() < 0 || pos.getY() > 255) {
         return false;
      }
      this.blocks.put(pos.asLong(), state);
      return true;
   }

   public boolean isAirBlock(BlockPos pos) {
      return this.getBlockState(pos).isAir();
   }

   /** Something to do to the block entity at pos once the blocks are placed (only happens inside the decorated chunk). */
   public void withTileEntity(BlockPos pos, Consumer<BlockEntity> action) {
      this.actions.add(new BlockEntityAction(pos.immutable(), action));
   }

   /** World.getTileEntity != null, for blocks placed in this run too. */
   public boolean hasTileEntity(BlockPos pos) {
      return this.getBlockState(pos).hasBlockEntity();
   }

   /** Chunk.getTopFilledSegment of the 1.12 terrain (the base y of the highest non-empty 16-block section). */
   public int topFilledSegment(BlockPos pos) {
      for (int y = 240; y >= 0; y -= 16) {
         for (int x = pos.getX() & ~15; x < (pos.getX() & ~15) + 16; x++) {
            for (int z = pos.getZ() & ~15; z < (pos.getZ() & ~15) + 16; z++) {
               for (int dy = 15; dy >= 0; dy--) {
                  if (!this.generator.legacyState(new BlockPos(x, y + dy, z)).isAir()) {
                     return y;
                  }
               }
            }
         }
      }
      return 0;
   }

   public void spawnEntity(Entity entity) {
      this.entities.add(entity);
   }

   public Biome getBiome(BlockPos pos) {
      return this.generator.legacyBiome(pos.getX(), pos.getZ());
   }

   /** The 1.12 terrain (for looking at columns far from the populate area, where 1.12 generated the chunk on demand). */
   public BlockState getTerrainState(BlockPos pos) {
      return this.generator.legacyState(pos);
   }

   /** Writes what landed in the decorated chunk; returns the positions written. */
   public List<BlockPos> flush() {
      List<BlockPos> placed = new ArrayList<>();
      for (var e : this.blocks.long2ObjectEntrySet()) {
         BlockPos p = BlockPos.of(e.getLongKey());
         if (this.inChunk(p)) {
            this.level.setBlock(p, e.getValue(), 2);
            placed.add(p);
         }
      }
      for (BlockEntityAction a : this.actions) {
         if (this.inChunk(a.pos)) {
            BlockEntity be = this.level.getBlockEntity(a.pos);
            if (be != null) {
               a.action.accept(be);
               be.setChanged();
            }
         }
      }
      for (Entity e : this.entities) {
         if (this.inChunk(e.blockPosition())) {
            this.level.addFreshEntity(e);
         }
      }
      return placed;
   }
}
