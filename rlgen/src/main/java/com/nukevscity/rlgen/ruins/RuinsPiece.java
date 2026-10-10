package com.nukevscity.rlgen.ruins;

import atomicstryker.ruins.common.RuinsWorld;
import com.github.fnar.minecraft.block.spawner.SpawnPotentialMapper1_12;
import com.mojang.logging.LogUtils;
import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

/** The part of one ruin inside one chunk. */
public final class RuinsPiece extends StructurePiece {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final int srcX;
   private final int srcZ;
   private final int index;
   private final int y;

   RuinsPiece(ChunkPos chunk, int srcX, int srcZ, int index, int y) {
      super(RLGen.RUINS_PIECE.get(), 0, new BoundingBox(chunk.getMinBlockX(), -64, chunk.getMinBlockZ(), chunk.getMaxBlockX(), 319, chunk.getMaxBlockZ()));
      this.srcX = srcX;
      this.srcZ = srcZ;
      this.index = index;
      this.y = y;
   }

   public RuinsPiece(StructurePieceSerializationContext context, CompoundTag tag) {
      super(RLGen.RUINS_PIECE.get(), tag);
      this.srcX = tag.getInt("SX");
      this.srcZ = tag.getInt("SZ");
      this.index = tag.getInt("I");
      this.y = tag.getInt("Y");
   }

   @Override
   protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
      tag.putInt("SX", this.srcX);
      tag.putInt("SZ", this.srcZ);
      tag.putInt("I", this.index);
      tag.putInt("Y", this.y);
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
         RuinsGen.Candidate c = RuinsGen.find(generator, level.getSeed(), this.srcX, this.srcZ, this.index);
         if (c == null) {
            return;
         }
         RuinsWorld built = RuinsGen.built(generator, c, this.y);
         List<BlockPos> shaped = new ArrayList<>();
         for (Map.Entry<Long, BlockState> e : built.blocks.entrySet()) {
            BlockPos p = BlockPos.of(e.getKey());
            if ((p.getX() >> 4) != chunkPos.x || (p.getZ() >> 4) != chunkPos.z) {
               continue;
            }
            BlockState s = e.getValue();
            RuinsWorld.Tile tile = built.tiles.get(e.getKey());
            if (tile != null && tile.skullType >= 0) {
               s = skull(s, tile);
            }
            level.setBlock(p, s, 2);
            if (needsShape(s.getBlock())) {
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
         for (Map.Entry<Long, RuinsWorld.Tile> e : built.tiles.entrySet()) {
            BlockPos p = BlockPos.of(e.getKey());
            if ((p.getX() >> 4) == chunkPos.x && (p.getZ() >> 4) == chunkPos.z) {
               this.applyTile(level, p, e.getValue());
            }
         }
      } catch (RuntimeException e) {
         LOGGER.error("rlgen: ruin {} {} #{} failed in chunk {}", this.srcX, this.srcZ, this.index, chunkPos, e);
      }
   }

   /** TileEntitySkull: the type (skeleton, wither, zombie, player, creeper, dragon) and the 16-step rotation of a floor skull. */
   private static BlockState skull(BlockState s, RuinsWorld.Tile tile) {
      boolean wall = s.getBlock() instanceof WallSkullBlock;
      Block b = switch (tile.skullType) {
         case 1 -> wall ? Blocks.WITHER_SKELETON_WALL_SKULL : Blocks.WITHER_SKELETON_SKULL;
         case 2 -> wall ? Blocks.ZOMBIE_WALL_HEAD : Blocks.ZOMBIE_HEAD;
         case 3 -> wall ? Blocks.PLAYER_WALL_HEAD : Blocks.PLAYER_HEAD;
         case 4 -> wall ? Blocks.CREEPER_WALL_HEAD : Blocks.CREEPER_HEAD;
         case 5 -> wall ? Blocks.DRAGON_WALL_HEAD : Blocks.DRAGON_HEAD;
         default -> wall ? Blocks.SKELETON_WALL_SKULL : Blocks.SKELETON_SKULL;
      };
      BlockState out = b.defaultBlockState();
      if (wall) {
         out = out.setValue(WallSkullBlock.FACING, s.getValue(WallSkullBlock.FACING));
      } else {
         out = out.setValue(SkullBlock.ROTATION, Math.floorMod(tile.skullRotation, 16));
      }
      return out;
   }

   private void applyTile(WorldGenLevel level, BlockPos pos, RuinsWorld.Tile tile) {
      BlockEntity be = level.getBlockEntity(pos);
      if (be == null) {
         return;
      }
      if (tile.spawner != null && be instanceof SpawnerBlockEntity spawner) {
         ResourceLocation id = ResourceLocation.tryParse(SpawnPotentialMapper1_12.entityId(tile.spawner));
         if (id == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
            id = ResourceLocation.withDefaultNamespace("zombie"); // Lycanites is not ported yet
         }
         spawner.setEntityId(BuiltInRegistries.ENTITY_TYPE.get(id), level.getRandom());
      }
      if (be instanceof Container container) {
         for (int i = 0; i < tile.items.length && i < container.getContainerSize(); i++) {
            if (tile.items[i] != null) {
               container.setItem(i, tile.items[i].copy());
            }
         }
         if (tile.lootTable != null) {
            this.fillFromLootTable(level, pos, container, tile);
         }
      }
      be.setChanged();
   }

   /** RuinTemplateRule.addChestGenChest: roll the table, keep targetCount stacks, then split and spread them over empty slots like 1.12. */
   private void fillFromLootTable(WorldGenLevel level, BlockPos pos, Container chest, RuinsWorld.Tile tile) {
      ServerLevel server = level.getLevel();
      ResourceLocation id = ResourceLocation.tryParse(tile.lootTable);
      if (id == null) {
         return;
      }
      LootTable table = server.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, id));
      LootParams params;
      if (id.getPath().startsWith("gameplay/fishing")) {
         params = new LootParams.Builder(server).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
            .withParameter(LootContextParams.TOOL, new ItemStack(Items.FISHING_ROD)).create(LootContextParamSets.FISHING);
      } else {
         params = new LootParams.Builder(server).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos)).create(LootContextParamSets.CHEST);
      }
      List<ItemStack> loot = new ArrayList<>(table.getRandomItems(params, tile.lootSeed));
      Random random = new Random(tile.lootSeed);
      int toRemove = loot.size() - tile.lootCount;
      if (tile.lootCount > 0 && toRemove > 0) {
         Collections.shuffle(loot, random);
         for (Iterator<ItemStack> iter = loot.iterator(); toRemove > 0; toRemove--) {
            iter.next();
            iter.remove();
         }
      }
      List<Integer> empty = new ArrayList<>();
      for (int i = 0; i < chest.getContainerSize(); i++) {
         if (chest.getItem(i).isEmpty()) {
            empty.add(i);
         }
      }
      Collections.shuffle(empty, random);
      shuffleItems(loot, empty.size(), random);
      for (ItemStack stack : loot) {
         if (empty.isEmpty()) {
            return;
         }
         chest.setItem(empty.remove(empty.size() - 1), stack);
      }
   }

   private static void shuffleItems(List<ItemStack> stacks, int targetSize, Random rand) {
      List<ItemStack> list = new ArrayList<>();
      Iterator<ItemStack> iterator = stacks.iterator();
      while (iterator.hasNext()) {
         ItemStack itemstack = iterator.next();
         if (itemstack.getCount() <= 0) {
            iterator.remove();
         } else if (itemstack.getCount() > 1) {
            list.add(itemstack);
            iterator.remove();
         }
      }
      targetSize -= stacks.size();
      while (targetSize > 0 && !list.isEmpty()) {
         ItemStack itemstack2 = list.remove(rand.nextInt(list.size()));
         int i = 1 + rand.nextInt(Math.max(1, itemstack2.getCount() / 2));
         itemstack2.shrink(i);
         ItemStack itemstack1 = itemstack2.copyWithCount(i);
         if (itemstack2.getCount() > 1 && rand.nextBoolean()) {
            list.add(itemstack2);
         } else {
            stacks.add(itemstack2);
         }
         if (itemstack1.getCount() > 1 && rand.nextBoolean()) {
            list.add(itemstack1);
         } else {
            stacks.add(itemstack1);
         }
      }
      stacks.addAll(list);
      Collections.shuffle(stacks, rand);
   }
}
