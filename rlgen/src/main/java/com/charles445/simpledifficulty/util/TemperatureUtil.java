package com.charles445.simpledifficulty.util;

import com.charles445.simpledifficulty.api.temperature.TemperatureEnum;
import com.charles445.simpledifficulty.api.temperature.TemperatureRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

/** 1.12 api TemperatureUtil + util.internal.TemperatureUtilInternal. */
public class TemperatureUtil {
   private static final String ARMOR_TEMPERATURE_TAG = "SDArmorTemp";

   public static int getPlayerTargetTemperature(Player player) {
      float sum = 0.0F;
      Level world = player.level();
      BlockPos pos = WorldUtil.getSidedBlockPos(world, player);
      for (TemperatureRegistry.ITemperatureModifier modifier : TemperatureRegistry.modifiers.values()) {
         sum += modifier.getWorldInfluence(world, pos);
         sum += modifier.getPlayerInfluence(player);
      }
      for (TemperatureRegistry.ITemperatureDynamicModifier dynmodifier : TemperatureRegistry.dynamicModifiers.values()) {
         sum = dynmodifier.applyDynamicWorldInfluence(world, pos, sum);
         sum = dynmodifier.applyDynamicPlayerInfluence(player, sum);
      }
      return (int)sum;
   }

   public static int getWorldTemperature(Level world, BlockPos pos) {
      float sum = 0.0F;
      for (TemperatureRegistry.ITemperatureModifier modifier : TemperatureRegistry.modifiers.values()) {
         sum += modifier.getWorldInfluence(world, pos);
      }
      for (TemperatureRegistry.ITemperatureDynamicModifier dynmodifier : TemperatureRegistry.dynamicModifiers.values()) {
         sum = dynmodifier.applyDynamicWorldInfluence(world, pos, sum);
      }
      return (int)sum;
   }

   public static int clampTemperature(int temperature) {
      return Mth.clamp(temperature, TemperatureEnum.FREEZING.getLowerBound(), TemperatureEnum.BURNING.getUpperBound());
   }

   public static TemperatureEnum getTemperatureEnum(int temp) {
      for (TemperatureEnum e : TemperatureEnum.values()) {
         if (e.matches(temp)) {
            return e;
         }
      }
      return temp < 0 ? TemperatureEnum.FREEZING : TemperatureEnum.BURNING;
   }

   public static void setArmorTemperatureTag(ItemStack stack, float temperature) {
      CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putFloat(ARMOR_TEMPERATURE_TAG, temperature));
   }

   public static float getArmorTemperatureTag(ItemStack stack) {
      CustomData data = stack.get(DataComponents.CUSTOM_DATA);
      if (data != null) {
         CompoundTag compound = data.getUnsafe();
         if (compound.get(ARMOR_TEMPERATURE_TAG) instanceof FloatTag f) {
            return f.getAsFloat();
         }
      }
      return 0.0F;
   }

   public static void removeArmorTemperatureTag(ItemStack stack) {
      CustomData data = stack.get(DataComponents.CUSTOM_DATA);
      if (data != null && data.contains(ARMOR_TEMPERATURE_TAG)) {
         CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.remove(ARMOR_TEMPERATURE_TAG));
      }
   }
}
