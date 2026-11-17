package com.charles445.simpledifficulty.item;

import com.charles445.simpledifficulty.SimpleDifficulty;
import com.charles445.simpledifficulty.api.SDItems;
import com.charles445.simpledifficulty.config.ModConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** 1.12 ItemJuice: one item, the juice is the 1.12 metadata (simpledifficulty:metadata component, model predicate simpledifficulty:type). */
public class ItemJuice extends ItemDrinkBase {
   public ItemJuice(Properties props) {
      super(props);
   }

   public static ItemStack stack(JuiceEnum juice) {
      ItemStack s = new ItemStack(SDItems.juice.get());
      s.set(SimpleDifficulty.METADATA.get(), juice.ordinal());
      return s;
   }

   @Override
   public void runSecondaryEffect(Player player, ItemStack stack) {
      if (this.getEnumForStack(stack) == JuiceEnum.GOLDEN_APPLE && ModConfig.server.miscellaneous.goldenAppleJuiceEffect) {
         player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
         player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 0));
      }
   }

   @Override
   public Component getName(ItemStack stack) {
      return Component.translatable("item.simpledifficulty:juice_" + this.getEnumForStack(stack).toString());
   }

   @Override
   public int getThirstLevel(ItemStack stack) {
      return this.getEnumForStack(stack).getThirstLevel();
   }

   @Override
   public float getSaturationLevel(ItemStack stack) {
      return this.getEnumForStack(stack).getSaturation();
   }

   @Override
   public float getDirtyChance(ItemStack stack) {
      return 0.0F;
   }

   protected JuiceEnum getEnumForStack(ItemStack stack) {
      int meta = SimpleDifficulty.getMetadata(stack);
      return meta < 0 || meta >= JuiceEnum.values().length ? JuiceEnum.values()[0] : JuiceEnum.values()[meta];
   }

   @Override
   public boolean isFoil(ItemStack stack) {
      JuiceEnum type = this.getEnumForStack(stack);
      return type == JuiceEnum.GOLDEN_APPLE || type == JuiceEnum.GOLDEN_CARROT || type == JuiceEnum.GOLDEN_MELON || super.isFoil(stack);
   }

   public enum JuiceEnum {
      APPLE("apple", 8, 6.4F),
      BEETROOT("beetroot", 10, 8.0F),
      CACTUS("cactus", 9, 2.7F),
      CARROT("carrot", 8, 4.8F),
      CHORUS_FRUIT("chorus_fruit", 12, 7.2F),
      GOLDEN_APPLE("golden_apple", 20, 20.0F),
      GOLDEN_CARROT("golden_carrot", 14, 14.0F),
      GOLDEN_MELON("golden_melon", 16, 16.0F),
      MELON("melon", 8, 4.0F),
      PUMPKIN("pumpkin", 7, 4.9F);

      private final String name;
      private final int thirst;
      private final float saturation;

      JuiceEnum(String name, int thirst, float saturation) {
         this.name = name;
         this.thirst = thirst;
         this.saturation = saturation;
      }

      public int getThirstLevel() {
         return this.thirst;
      }

      public float getSaturation() {
         return this.saturation;
      }

      @Override
      public String toString() {
         return this.name;
      }
   }
}
