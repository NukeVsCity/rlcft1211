package com.charles445.simpledifficulty.handler;

import com.charles445.simpledifficulty.api.SDItems;
import com.charles445.simpledifficulty.config.ModConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/** 1.12 BlockHandler (ice and magma drop chunks) and MiscHandler (dismount stops the jump key). */
public class BlockHandler {
   @SubscribeEvent
   public void onHarvestDrops(BlockDropsEvent event) {
      ServerLevel world = event.getLevel();
      if (event.getBreaker() == null) {
         return;
      }
      var enchantments = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
      ItemStack tool = event.getTool();
      if (EnchantmentHelper.getItemEnchantmentLevel(enchantments.getOrThrow(Enchantments.SILK_TOUCH), tool) > 0) {
         return;
      }
      Block block = event.getState().getBlock();
      int fortune = Math.max(0, EnchantmentHelper.getItemEnchantmentLevel(enchantments.getOrThrow(Enchantments.FORTUNE), tool));
      if (block == Blocks.ICE && ModConfig.server.miscellaneous.iceDropsChunks) {
         event.getDrops().clear();
         this.add(event, new ItemStack(SDItems.ice_chunk.get(), world.random.nextInt(fortune + 1) + world.random.nextInt(2)));
      } else if (block == Blocks.MAGMA_BLOCK && ModConfig.server.miscellaneous.magmaDropsChunks) {
         event.getDrops().clear();
         this.add(event, new ItemStack(SDItems.magma_chunk.get(), world.random.nextInt(fortune + 1) + world.random.nextInt(3)));
      }
   }

   private void add(BlockDropsEvent event, ItemStack stack) {
      if (!stack.isEmpty()) {
         double x = event.getPos().getX() + 0.5 + (event.getLevel().random.nextFloat() * 0.5 - 0.25);
         double y = event.getPos().getY() + 0.5 + (event.getLevel().random.nextFloat() * 0.5 - 0.25);
         double z = event.getPos().getZ() + 0.5 + (event.getLevel().random.nextFloat() * 0.5 - 0.25);
         ItemEntity e = new ItemEntity(event.getLevel(), x, y, z, stack);
         e.setDefaultPickUpDelay();
         event.getDrops().add(e);
      }
   }

   /** MiscHandler.onDismount */
   @SubscribeEvent
   public void onDismount(EntityMountEvent event) {
      if (event.isDismounting() && event.getEntityMounting() instanceof Player player) {
         player.setJumping(false);
      }
   }
}
