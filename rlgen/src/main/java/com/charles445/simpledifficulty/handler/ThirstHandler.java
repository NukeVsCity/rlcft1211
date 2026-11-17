package com.charles445.simpledifficulty.handler;

import com.charles445.simpledifficulty.api.SDCapabilities;
import com.charles445.simpledifficulty.api.SDPotions;
import com.charles445.simpledifficulty.api.config.JsonConfig;
import com.charles445.simpledifficulty.api.thirst.ThirstEnum;
import com.charles445.simpledifficulty.config.ModConfig;
import com.charles445.simpledifficulty.item.ItemCanteen;
import com.charles445.simpledifficulty.network.PacketHandler;
import com.charles445.simpledifficulty.util.DamageUtil;
import com.charles445.simpledifficulty.util.ThirstUtil;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/** 1.12 ThirstHandler (+ CompatRightClick.cauldronHandler, the handler 1.12 used without Inspirations). */
public class ThirstHandler {
   @SubscribeEvent
   public void onLivingEntityUseItemFinish(LivingEntityUseItemEvent.Finish event) {
      if (ModConfig.isThirstEnabled() && event.getEntity() instanceof Player player) {
         if (player.level().isClientSide) {
            return;
         }
         ItemStack stack = event.getItem();
         List<JsonConfig.JsonConsumableThirst> consumableList = JsonConfig.consumableThirst.get(stack.getItem());
         if (consumableList != null) {
            for (JsonConfig.JsonConsumableThirst jct : consumableList) {
               if (jct != null && jct.matches(stack)) {
                  ThirstUtil.takeDrink(player, jct.amount, jct.saturation, jct.thirstyChance);
                  return;
               }
            }
         }
         if (stack.getItem() == Items.POTION) {
            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
            Holder<Potion> potionType = contents == null ? null : contents.potion().orElse(null);
            if (potionType != null && potionType.unwrapKey().isPresent()) {
               String modDomain = potionType.unwrapKey().get().location().getNamespace();
               if (modDomain.equals("minecraft")) {
                  if (potionType.is(Potions.WATER) || potionType.is(Potions.AWKWARD) || potionType.is(Potions.MUNDANE) || potionType.is(Potions.THICK)) {
                     ThirstUtil.takeDrink(player, ThirstEnum.NORMAL);
                     return;
                  }
                  ThirstUtil.takeDrink(player, ThirstEnum.POTION);
                  return;
               } else if (SDPotions.potionTypes.values().stream().anyMatch(h -> h.value() == potionType.value())) {
                  ThirstUtil.takeDrink(player, ThirstEnum.POTION);
                  return;
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
      if (ModConfig.isThirstEnabled()) {
         Player player = event.getEntity();
         if (event.getLevel().isClientSide) {
            if (event.getHand() == InteractionHand.MAIN_HAND && this.clientCheckWater(player)) {
               this.clientSendDrinkMessageAndPlaySound(player);
            }
         } else if (event.getHand() == InteractionHand.MAIN_HAND) {
            Level world = event.getLevel();
            BlockPos pos = event.getPos();
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() == Blocks.CAULDRON || state.getBlock() == Blocks.WATER_CAULDRON) {
               this.cauldron(world, pos, state, player);
            }
         }
      }
   }

   /** CompatRightClick.cauldronHandler (drinking does not lower the water level, like 1.12) */
   private void cauldron(Level world, BlockPos pos, BlockState state, Player player) {
      ItemStack heldItem = player.getMainHandItem();
      int level = state.hasProperty(LayeredCauldronBlock.LEVEL) ? state.getValue(LayeredCauldronBlock.LEVEL) : 0;
      if (heldItem.isEmpty() && player.isShiftKeyDown()) {
         if (SDCapabilities.getThirstData(player).isThirsty() && level > 0) {
            ThirstUtil.takeDrink(player, ThirstEnum.NORMAL);
            DamageUtil.serverPlayBlockSound(world, pos, SoundEvents.GENERIC_DRINK);
         }
      } else if (heldItem.getItem() instanceof ItemCanteen canteen && level > 0) {
         if (canteen.tryAddDose(heldItem, ThirstEnum.NORMAL)) {
            DamageUtil.serverPlayBlockSound(world, pos, SoundEvents.BUCKET_FILL);
         }
      }
   }

   @SubscribeEvent
   public void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
      if (ModConfig.isThirstEnabled() && event.getHand() == InteractionHand.MAIN_HAND && this.clientCheckWater(event.getEntity())) {
         this.clientSendDrinkMessageAndPlaySound(event.getEntity());
      }
   }

   private boolean clientCheckWater(Player player) {
      return player.isShiftKeyDown() && ModConfig.isThirstEnabled() && ThirstUtil.traceWaterToDrink(player) != null;
   }

   private void clientSendDrinkMessageAndPlaySound(Player player) {
      PacketHandler.sendDrinkWater();
      player.swing(InteractionHand.MAIN_HAND);
      DamageUtil.commonPlayPlayerSound(player, SoundEvents.GENERIC_DRINK);
   }

   @SubscribeEvent
   public void onAttackEntity(AttackEntityEvent event) {
      if (ModConfig.isThirstEnabled() && !event.getEntity().level().isClientSide) {
         Player player = event.getEntity();
         if (!shouldSkipThirst(player)) {
            Entity monster = event.getTarget();
            if (monster.isAttackable() && !monster.skipAttackInteraction(player)) {
               addExhaustion(player, (float)ModConfig.server.thirst.thirstAttacking);
            }
         }
      }
   }

   @SubscribeEvent
   public void onBlockBreak(BlockEvent.BreakEvent event) {
      if (ModConfig.isThirstEnabled() && !event.getLevel().isClientSide()) {
         Player player = event.getPlayer();
         if (!shouldSkipThirst(player) && event.getState().canHarvestBlock(event.getLevel(), event.getPos(), player)) {
            addExhaustion(player, (float)ModConfig.server.thirst.thirstBreakBlock);
         }
      }
   }

   /** 1.12 LivingHurtEvent: the source's hunger damage (1.21 damage type exhaustion) */
   @SubscribeEvent
   public void onLivingHurt(LivingIncomingDamageEvent event) {
      if (ModConfig.isThirstEnabled() && !event.getEntity().level().isClientSide && event.getAmount() != 0.0F && event.getEntity() instanceof Player player) {
         if (!shouldSkipThirst(player)) {
            addExhaustion(player, event.getSource().getFoodExhaustion());
         }
      }
   }

   @SubscribeEvent
   public void onLivingJump(LivingEvent.LivingJumpEvent event) {
      if (ModConfig.isThirstEnabled() && !event.getEntity().level().isClientSide && event.getEntity() instanceof Player player) {
         if (!shouldSkipThirst(player)) {
            addExhaustion(player, (float)(player.isSprinting() ? ModConfig.server.thirst.thirstSprintJump : ModConfig.server.thirst.thirstJump));
         }
      }
   }

   private static boolean shouldSkipThirst(Player player) {
      return player.isCreative() || player.isSpectator();
   }

   private static void addExhaustion(Player player, float exhaustion) {
      SDCapabilities.getThirstData(player).addThirstExhaustion(exhaustion);
   }
}
