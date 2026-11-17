package com.alcatrazescapee.notreepunching;

import com.alcatrazescapee.notreepunching.util.HarvestBlockHandler;
import com.alcatrazescapee.notreepunching.util.PlayerInteractionHandler;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/** 1.12 ModEventHandler: slower breaking and no drops with the wrong tool, the extra drops, the always breakable blocks, knapping and chopping. */
public final class ModEventHandler {
   private ModEventHandler() {
   }

   @SubscribeEvent
   public static void breakSpeed(PlayerEvent.BreakSpeed event) {
      Player player = event.getEntity();
      if (!(player instanceof FakePlayer) && !player.isCreative() && ModConfig.GENERAL.enableBreakingChanges) {
         ItemStack stack = player.getMainHandItem();
         BlockState state = event.getState();
         if (HarvestBlockHandler.isInvalidTool(stack, player, state)) {
            Object[] tool = HarvestBlockHandler.getHarvestTool(state);
            event.setNewSpeed(event.getOriginalSpeed() * HarvestBlockHandler.getSpeedModifier(tool == null ? "" : (String)tool[0]));
         }
      }
   }

   /** onHarvestBlock (HarvestDropsEvent, LOWEST); the tool is the one used before it was damaged (1.12 IPlayerItem capability) */
   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void onHarvestBlock(BlockDropsEvent event) {
      if (event.getBreaker() instanceof Player player && !(player instanceof FakePlayer) && !player.isCreative() && ModConfig.GENERAL.enableBreakingChanges) {
         BlockState state = event.getState();
         ItemStack stack = event.getTool();
         var silk = event.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
         boolean isSilk = EnchantmentHelper.getItemEnchantmentLevel(silk, stack) > 0;
         List<ItemStack> drops = new ArrayList<>();
         for (ItemEntity e : event.getDrops()) {
            drops.add(e.getItem());
         }
         List<ItemStack> before = new ArrayList<>(drops);
         HarvestBlockHandler.addExtraDrops(drops, state, player, stack, isSilk);
         if (HarvestBlockHandler.isInvalidTool(stack, player, state)) {
            drops.clear();
         }
         if (!drops.equals(before)) {
            event.getDrops().clear();
            var pos = event.getPos();
            for (ItemStack d : drops) {
               var r = event.getLevel().random;
               ItemEntity e = new ItemEntity(event.getLevel(), pos.getX() + 0.5 + r.nextFloat() * 0.5 - 0.25, pos.getY() + 0.5 + r.nextFloat() * 0.5 - 0.25,
                  pos.getZ() + 0.5 + r.nextFloat() * 0.5 - 0.25, d);
               e.setDefaultPickUpDelay();
               event.getDrops().add(e);
            }
         }
      }
   }

   @SubscribeEvent
   public static void harvestBlockInitialCheck(PlayerEvent.HarvestCheck event) {
      if (HarvestBlockHandler.isWhitelisted(event.getTargetBlock())) {
         event.setCanHarvest(true);
      }
   }

   @SubscribeEvent
   public static void playerInteractEvent(PlayerInteractEvent.RightClickBlock event) {
      ItemStack stack = event.getItemStack();
      Player player = event.getEntity();
      if (event.getHand() == InteractionHand.OFF_HAND) {
         ItemStack mainStack = player.getItemInHand(InteractionHand.MAIN_HAND);
         if (PlayerInteractionHandler.hasAction(event.getLevel(), event.getPos(), mainStack, event.getFace())) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
         }
      }
      if (PlayerInteractionHandler.hasAction(event.getLevel(), event.getPos(), stack, event.getFace())
         && PlayerInteractionHandler.performAction(event.getLevel(), event.getPos(), player, stack, event.getFace(), event.getHand())) {
         event.setCancellationResult(InteractionResult.SUCCESS);
         event.setCanceled(true);
      }
   }
}
