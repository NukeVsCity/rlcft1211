package com.charles445.simpledifficulty.item;

import com.charles445.simpledifficulty.api.SDCapabilities;
import com.charles445.simpledifficulty.api.thirst.ThirstEnum;
import com.charles445.simpledifficulty.config.ModConfig;
import com.charles445.simpledifficulty.util.DamageUtil;
import com.charles445.simpledifficulty.util.ThirstUtil;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

/** 1.12 ItemCanteen (+ api IItemCanteen). The 1.12 stack NBT ("Doses", "CanteenType") lives in the custom data component. */
public class ItemCanteen extends ItemDrinkBase {
   public static final String CANTEENTYPE = "CanteenType";
   public static final String DOSES = "Doses";

   public ItemCanteen(Properties props) {
      super(props, 1);
   }

   /** getSubItems */
   public void fillTab(CreativeModeTab.Output items) {
      ItemStack emptyCanteen = new ItemStack(this);
      this.createTypeTag(emptyCanteen);
      this.setCanteenEmpty(emptyCanteen);
      ItemStack fullCanteen = emptyCanteen.copy();
      this.setCanteenFull(fullCanteen);
      ItemStack purifiedCanteen = fullCanteen.copy();
      this.setTypeTag(purifiedCanteen, ThirstEnum.PURIFIED.ordinal());
      items.accept(emptyCanteen);
      items.accept(fullCanteen);
      items.accept(purifiedCanteen);
   }

   protected String keyPrefix() {
      return "item.simpledifficulty:canteen_";
   }

   @Override
   public Component getName(ItemStack stack) {
      if (this.isCanteenEmpty(stack)) {
         return Component.translatable(this.keyPrefix() + "empty");
      } else {
         int type = this.getTypeTag(stack);
         return Component.translatable(type >= ThirstEnum.values().length || type < 0 ? this.keyPrefix() + "broken" : this.keyPrefix() + ThirstEnum.values()[type].toString());
      }
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      int typetag = this.getTypeTag(stack);
      if (!this.isCanteenFull(stack) || typetag == ThirstEnum.NORMAL.ordinal()) {
         ThirstEnum.BlockPosResult traceBlockPos = ThirstUtil.traceWater(player);
         if (traceBlockPos != null) {
            ThirstEnum trace = traceBlockPos.thirstEnum;
            if (trace == ThirstEnum.PURIFIED) {
               world.removeBlock(traceBlockPos.pos, false);
            }
            if (trace == ThirstEnum.RAIN) {
               trace = ThirstEnum.NORMAL;
            }
            this.tryAddDose(stack, trace);
            DamageUtil.commonPlayPlayerSound(player, SoundEvents.BUCKET_FILL);
            player.startUsingItem(hand);
            player.swing(hand);
            player.stopUsingItem();
            return InteractionResultHolder.success(stack);
         }
      }
      return this.useDrink(stack, player, hand);
   }

   protected InteractionResultHolder<ItemStack> useDrink(ItemStack stack, Player player, InteractionHand hand) {
      if (!this.isCanteenEmpty(stack) && (SDCapabilities.getThirstData(player).isThirsty() || !ModConfig.isThirstEnabled())) {
         player.startUsingItem(hand);
         return InteractionResultHolder.success(stack);
      }
      return InteractionResultHolder.fail(stack);
   }

   @Override
   public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entityLiving) {
      if (world.isClientSide || !(entityLiving instanceof Player player)) {
         return stack;
      } else if (this.isCanteenEmpty(stack)) {
         return stack;
      } else {
         ThirstUtil.takeDrink(player, this.getThirstLevel(stack), this.getSaturationLevel(stack), this.getDirtyChance(stack));
         this.removeDose(stack);
         return stack;
      }
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.translatable("item.durability", this.getDoses(stack), this.getMaxDoses(stack)));
   }

   private double getDurabilityForDisplay(ItemStack stack) {
      double max = this.getMaxDoses(stack);
      return max == 0.0 ? 1.0 : (max - (double)this.getDoses(stack)) / max;
   }

   @Override
   public boolean isBarVisible(ItemStack stack) {
      return !this.isCanteenEmpty(stack);
   }

   @Override
   public int getBarWidth(ItemStack stack) {
      return (int)Math.round(13.0 - this.getDurabilityForDisplay(stack) * 13.0);
   }

   @Override
   public int getBarColor(ItemStack stack) {
      return Mth.hsvToRgb(Math.max(0.0F, (float)(1.0 - this.getDurabilityForDisplay(stack))) / 3.0F, 1.0F, 1.0F);
   }

   @Override
   public int getThirstLevel(ItemStack stack) {
      ThirstEnum thirstEnum = this.getThirstEnum(stack);
      return thirstEnum == null ? 0 : thirstEnum.getThirst();
   }

   @Override
   public float getSaturationLevel(ItemStack stack) {
      ThirstEnum thirstEnum = this.getThirstEnum(stack);
      return thirstEnum == null ? 0.0F : thirstEnum.getSaturation();
   }

   @Override
   public float getDirtyChance(ItemStack stack) {
      ThirstEnum thirstEnum = this.getThirstEnum(stack);
      return thirstEnum == null ? 0.0F : thirstEnum.getThirstyChance();
   }

   @Nullable
   public ThirstEnum getThirstEnum(ItemStack stack) {
      int type = this.getTypeTag(stack);
      return type >= ThirstEnum.values().length || type < 0 ? null : ThirstEnum.values()[type];
   }

   private static CompoundTag tag(ItemStack stack) {
      CustomData d = stack.get(DataComponents.CUSTOM_DATA);
      return d == null ? null : d.getUnsafe();
   }

   protected static void setInt(ItemStack stack, String key, int value) {
      CustomData.update(DataComponents.CUSTOM_DATA, stack, t -> t.putInt(key, value));
   }

   /** getTypeTag: a canteen without NBT becomes an empty normal canteen, a wrong tag type is replaced by NORMAL */
   protected int getTypeTag(ItemStack stack) {
      if (tag(stack) == null) {
         this.createTypeTag(stack);
         this.setCanteenEmpty(stack);
      }
      if (tag(stack).get(CANTEENTYPE) instanceof IntTag i) {
         return i.getAsInt();
      } else {
         this.createTypeTag(stack);
         return ThirstEnum.NORMAL.ordinal();
      }
   }

   protected void setTypeTag(ItemStack stack, ThirstEnum thirstEnum) {
      this.setTypeTag(stack, thirstEnum.ordinal());
   }

   protected void setTypeTag(ItemStack stack, int tag) {
      setInt(stack, CANTEENTYPE, tag);
   }

   protected void createTypeTag(ItemStack stack) {
      this.setTypeTag(stack, ThirstEnum.NORMAL.ordinal());
   }

   protected int getDosesTag(ItemStack stack) {
      if (tag(stack) == null) {
         this.createDosesTag(stack);
         this.setCanteenEmpty(stack);
      }
      if (tag(stack).get(DOSES) instanceof IntTag i) {
         return i.getAsInt();
      } else {
         this.createDosesTag(stack);
         return 0;
      }
   }

   protected void setDosesTag(ItemStack stack, int doses) {
      setInt(stack, DOSES, doses);
   }

   protected void createDosesTag(ItemStack stack) {
      this.setDosesTag(stack, 0);
   }

   public int getDoses(ItemStack stack) {
      return this.getDosesTag(stack);
   }

   public int getMaxDoses(ItemStack stack) {
      return ModConfig.server.canteenDoses;
   }

   public boolean isCanteenFull(ItemStack stack) {
      return this.getDoses(stack) >= this.getMaxDoses(stack);
   }

   public boolean isCanteenEmpty(ItemStack stack) {
      return this.getDoses(stack) <= 0;
   }

   public void setCanteenFull(ItemStack stack) {
      this.setDosesInternal(stack, this.getMaxDoses(stack));
   }

   public void setCanteenEmpty(ItemStack stack) {
      this.setDosesInternal(stack, 0);
   }

   public void removeDose(ItemStack stack) {
      if (!this.isCanteenEmpty(stack)) {
         this.setDosesInternal(stack, this.getDoses(stack) - 1);
      }
   }

   public void setDoses(ItemStack stack, int amount) {
      this.setDosesInternal(stack, amount);
   }

   public void setDoses(ItemStack stack, ThirstEnum thirstEnum, int amount) {
      this.formatCanteen(stack, thirstEnum);
      this.setDosesInternal(stack, amount);
   }

   public boolean tryAddDose(ItemStack stack, ThirstEnum thirstEnum) {
      int oldDamage = Math.max(0, this.getDoses(stack));
      boolean format = this.formatCanteen(stack, thirstEnum);
      if (thirstEnum == ThirstEnum.NORMAL) {
         this.setDosesInternal(stack, this.getMaxDoses(stack));
      } else {
         this.setDosesInternal(stack, this.getDoses(stack) + 1);
      }
      return format || this.getDoses(stack) != oldDamage;
   }

   protected boolean formatCanteen(ItemStack stack, ThirstEnum thirstEnum) {
      if (thirstEnum != this.getThirstEnum(stack)) {
         this.setCanteenEmpty(stack);
         this.setTypeTag(stack, thirstEnum);
         return true;
      } else {
         this.getDoses(stack);
         return false;
      }
   }

   protected void setDosesInternal(ItemStack stack, int amount) {
      if (amount <= 0) {
         this.setDosesTag(stack, 0);
      } else {
         this.setDosesTag(stack, Math.min(amount, this.getMaxDoses(stack)));
      }
   }
}
