package com.nukevscity.rlgen.roguelike;

import com.mojang.logging.LogUtils;
import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.slf4j.Logger;

/** The part of one Roguelike dungeon inside one chunk. */
public final class RoguelikePiece extends StructurePiece {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final int gridX;
   private final int gridZ;

   RoguelikePiece(ChunkPos chunk, int gridX, int gridZ) {
      super(RLGen.ROGUELIKE_PIECE.get(), 0, new BoundingBox(chunk.getMinBlockX(), -64, chunk.getMinBlockZ(), chunk.getMaxBlockX(), 319, chunk.getMaxBlockZ()));
      this.gridX = gridX;
      this.gridZ = gridZ;
   }

   public RoguelikePiece(StructurePieceSerializationContext context, CompoundTag tag) {
      super(RLGen.ROGUELIKE_PIECE.get(), tag);
      this.gridX = tag.getInt("GX");
      this.gridZ = tag.getInt("GZ");
   }

   @Override
   protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
      tag.putInt("GX", this.gridX);
      tag.putInt("GZ", this.gridZ);
   }

   private static boolean needsShape(Block b) {
      return b instanceof FenceBlock || b instanceof WallBlock || b instanceof IronBarsBlock || b instanceof StairBlock || b instanceof ChestBlock;
   }

   @Override
   public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox box,
                           ChunkPos chunkPos, BlockPos pivot) {
      if (!(chunkGenerator instanceof LegacyChunkGenerator generator)) {
         return;
      }
      try {
         RoguelikeDungeons.Built built = RoguelikeDungeons.built(generator, level.getSeed(), this.gridX, this.gridZ, level.registryAccess());
         RoguelikeDungeons.Slice slice = built.slices.get(chunkPos.toLong());
         if (slice == null) {
            return;
         }
         List<BlockPos> shaped = new ArrayList<>();
         for (Map.Entry<Long, BlockState> e : slice.blocks()) {
            BlockPos p = BlockPos.of(e.getKey());
            level.setBlock(p, e.getValue(), 2);
            if (needsShape(e.getValue().getBlock())) {
               shaped.add(p);
            }
         }
         for (BlockPos p : shaped) {
            BlockState s = level.getBlockState(p);
            BlockState u = Block.updateFromNeighbourShapes(s, level, p);
            if (u != s && !u.isAir()) {
               level.setBlock(p, u, 2 | 16);
            }
         }
         slice.tiles().forEach((pos, tile) -> this.applyTile(level, BlockPos.of(pos), tile));
      } catch (RuntimeException e) {
         LOGGER.error("rlgen: roguelike dungeon {} {} failed in chunk {}", this.gridX, this.gridZ, chunkPos, e);
      }
   }

   private void applyTile(WorldGenLevel level, BlockPos pos, BufferedEditor.Tile tile) {
      BlockEntity be = level.getBlockEntity(pos);
      if (be == null) {
         return;
      }
      if (tile.spawner != null && be instanceof SpawnerBlockEntity spawner) {
         spawner.getSpawner().load(level.getLevel(), pos, tile.spawner);
      }
      if (be instanceof BaseContainerBlockEntity container) {
         for (int i = 0; i < tile.items.length && i < container.getContainerSize(); i++) {
            if (tile.items[i] != null) {
               container.setItem(i, tile.items[i].copy());
            }
         }
      }
      if (tile.lootTable != null && be instanceof RandomizableContainerBlockEntity r) {
         ResourceLocation id = ResourceLocation.tryParse(tile.lootTable);
         if (id != null) {
            r.setLootTable(ResourceKey.create(Registries.LOOT_TABLE, id), tile.lootSeed);
         }
      }
      be.setChanged();
   }
}
