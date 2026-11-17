package com.charles445.simpledifficulty.api.temperature;

import java.util.LinkedHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** 1.12 TemperatureRegistry plus the 1.12 api.temperature interfaces (insertion order = summing order, like 1.12). */
public class TemperatureRegistry {
   public static LinkedHashMap<String, ITemperatureModifier> modifiers = new LinkedHashMap<>();
   public static LinkedHashMap<String, ITemperatureDynamicModifier> dynamicModifiers = new LinkedHashMap<>();

   public static void registerModifier(ITemperatureModifier modifier) {
      modifiers.put(modifier.getName(), modifier);
   }

   public static void registerDynamicModifier(ITemperatureDynamicModifier modifier) {
      dynamicModifiers.put(modifier.getName(), modifier);
   }

   /** 1.12 ITemperatureModifier. */
   public interface ITemperatureModifier {
      float getPlayerInfluence(Player player);

      float getWorldInfluence(Level world, BlockPos pos);

      String getName();
   }

   /** 1.12 ITemperatureDynamicModifier. */
   public interface ITemperatureDynamicModifier {
      float applyDynamicPlayerInfluence(Player player, float currentTemperature);

      float applyDynamicWorldInfluence(Level world, BlockPos pos, float currentTemperature);

      String getName();
   }

   /** 1.12 ITemperatureTileEntity. */
   public interface ITemperatureTileEntity {
      float getInfluence(BlockPos targetPos, double distance);
   }

   /** 1.12 TemporaryModifier. */
   public static class TemporaryModifier {
      public float temperature;
      public int duration;

      public TemporaryModifier(float temperature, int duration) {
         this.temperature = temperature;
         this.duration = duration;
      }
   }
}
