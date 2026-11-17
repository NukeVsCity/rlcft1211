package com.charles445.simpledifficulty.item;

import com.charles445.simpledifficulty.api.thirst.ThirstEnum;
import com.charles445.simpledifficulty.util.DamageUtil;
import com.charles445.simpledifficulty.util.ThirstUtil;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** 1.12 ItemDragonCanteen: always purified, capacity from extraItems.json (RLCraft: 8). */
public class ItemDragonCanteen extends ItemCanteen {
   public int capacity = 5;

   public ItemDragonCanteen(Properties props, @Nullable Map<String, String> settings) {
      super(props);
      if (settings != null && settings.get("capacity") != null) {
         try {
            this.capacity = Integer.parseInt(settings.get("capacity"));
         } catch (NumberFormatException ignored) {
         }
      }
   }

   @Override
   public int getMaxDoses(ItemStack stack) {
      return this.capacity;
   }

   @Override
   protected String keyPrefix() {
      return "item.simpledifficulty:dragon_canteen_";
   }

   @Override
   public void fillTab(CreativeModeTab.Output items) {
      ItemStack emptyCanteen = new ItemStack(this);
      this.tryAddDose(emptyCanteen, ThirstEnum.PURIFIED);
      this.setCanteenEmpty(emptyCanteen);
      ItemStack fullCanteen = emptyCanteen.copy();
      this.tryAddDose(fullCanteen, ThirstEnum.PURIFIED);
      this.setCanteenFull(fullCanteen);
      setInt(emptyCanteen, CANTEENTYPE, ThirstEnum.PURIFIED.ordinal());
      setInt(fullCanteen, CANTEENTYPE, ThirstEnum.PURIFIED.ordinal());
      items.accept(emptyCanteen);
      items.accept(fullCanteen);
   }

   @Nullable
   @Override
   public ThirstEnum getThirstEnum(ItemStack stack) {
      return ThirstEnum.PURIFIED;
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (!this.isCanteenFull(stack)) {
         ThirstEnum.BlockPosResult traceBlockPos = ThirstUtil.traceWater(player);
         if (traceBlockPos != null) {
            if (traceBlockPos.thirstEnum == ThirstEnum.PURIFIED) {
               world.removeBlock(traceBlockPos.pos, false);
            }
            this.tryAddDose(stack, ThirstEnum.PURIFIED);
            DamageUtil.commonPlayPlayerSound(player, SoundEvents.BUCKET_FILL);
            player.startUsingItem(hand);
            player.swing(hand);
            player.stopUsingItem();
            return InteractionResultHolder.success(stack);
         }
      }
      return this.useDrink(stack, player, hand);
   }
}
