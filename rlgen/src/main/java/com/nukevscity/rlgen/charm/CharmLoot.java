package com.nukevscity.rlgen.charm;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

/** Charm's loot tables are rlgen:charm/... (converted from the Charm jar); vanilla ones keep their 1.21 names. */
public final class CharmLoot {
   private CharmLoot() {
   }

   public static ResourceKey<LootTable> key(String id) {
      if (id.startsWith("charm:")) {
         id = "rlgen:charm/" + id.substring("charm:".length());
      } else if (id.equals("minecraft:simple_dungeon")) {
         id = "minecraft:chests/simple_dungeon";
      }
      return ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(id));
   }

   /** Containers get the loot table; the bookshelf chest (a chiseled bookshelf) gets up to lootSize books from it right away. */
   public static void fill(WorldGenLevel level, BlockEntity tile, String loot, int lootSize, long seed) {
      if (tile instanceof RandomizableContainerBlockEntity c) {
         c.setLootTable(key(loot), seed);
      } else if (tile instanceof ChiseledBookShelfBlockEntity shelf) {
         ServerLevel server = level.getLevel();
         LootTable table = server.getServer().reloadableRegistries().getLootTable(key(loot));
         List<ItemStack> items = table.getRandomItems(new LootParams.Builder(server).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(tile.getBlockPos()))
            .create(LootContextParamSets.CHEST), seed);
         int slot = 0;
         BlockState state = level.getBlockState(tile.getBlockPos());
         net.minecraft.core.NonNullList<ItemStack> books = net.minecraft.core.NonNullList.withSize(6, ItemStack.EMPTY);
         for (ItemStack stack : items) {
            if (slot >= Math.min(lootSize, 6)) {
               break;
            }
            books.set(slot, stack.copyWithCount(1));
            state = state.setValue(ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.get(slot), true);
            slot++;
         }
         // setItem would update the block state through the level, which a block entity in a generating chunk doesn't have yet
         net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
         net.minecraft.world.ContainerHelper.saveAllItems(tag, books, level.registryAccess());
         shelf.loadCustomOnly(tag, level.registryAccess());
         level.setBlock(tile.getBlockPos(), state, 2);
      }
   }
}
