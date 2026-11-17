package com.charles445.simpledifficulty.item;

import com.charles445.simpledifficulty.SimpleDifficulty;
import com.charles445.simpledifficulty.api.temperature.TemperatureEnum;
import com.charles445.simpledifficulty.config.ModConfig;
import com.charles445.simpledifficulty.util.WorldUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 1.12 ItemThermometer: the "temperature" model property is the world temperature at the holder (or item frame) / 26, re-read
 * every 10-17 ticks per stack (keyed by the stack's identity hash, like 1.12 ItemStack.hashCode).
 */
public class ItemThermometer extends Item {
   protected static Map<Integer, Long> hashAge = new HashMap<>();
   protected static Map<Integer, Float> hashTemp = new HashMap<>();
   protected static long lastAudit = 0L;

   public ItemThermometer(Properties props) {
      super(props);
   }

   protected static void audit(long worldTime) {
      if (worldTime - lastAudit >= 200L || worldTime < lastAudit) {
         lastAudit = worldTime;
         boolean mismatched = false;
         for (Integer key : hashTemp.keySet()) {
            if (!hashAge.containsKey(key)) {
               mismatched = true;
            }
         }
         for (Integer key : hashAge.keySet()) {
            if (!hashTemp.containsKey(key)) {
               mismatched = true;
            }
         }
         if (mismatched) {
            SimpleDifficulty.logger.warn("Thermometer audit had mismatched maps!");
            hashAge.clear();
            hashTemp.clear();
            return;
         }
         Set<Integer> removalSet = new HashSet<>();
         for (Map.Entry<Integer, Long> entry : hashAge.entrySet()) {
            if (worldTime - entry.getValue() >= 100L || worldTime < entry.getValue()) {
               removalSet.add(entry.getKey());
            }
         }
         for (Integer value : removalSet) {
            hashAge.remove(value);
            hashTemp.remove(value);
         }
      }
   }

   /** The 1.12 IItemPropertyGetter "temperature". */
   public static float property(ItemStack stack, @Nullable Level world, @Nullable LivingEntity pendingEntity) {
      Entity entity = pendingEntity != null ? pendingEntity : stack.getFrame();
      if (world == null && entity != null) {
         world = entity.level();
      }
      if (world == null || entity == null) {
         return 0.0F;
      } else {
         return ModConfig.isTemperatureEnabled() && ModConfig.client.thermometer.enableThermometer ? wobble(world, entity, System.identityHashCode(stack)) : 0.0F;
      }
   }

   private static float wobble(Level world, Entity entity, int hash) {
      Long age = hashAge.get(hash);
      Float temp = hashTemp.get(hash);
      long totalWorldTime = world.getGameTime();
      audit(totalWorldTime);
      boolean cleanStart = false;
      if (age == null && temp == null) {
         cleanStart = true;
      } else if (age == null || temp == null) {
         hashAge.remove(hash);
         hashTemp.remove(hash);
         cleanStart = true;
      }
      if (cleanStart) {
         hashAge.put(hash, totalWorldTime);
         float newTemp = calculateTemperature(world, entity);
         hashTemp.put(hash, newTemp);
         return newTemp;
      } else if (totalWorldTime - age >= 10L + (long)(hash & 7)) {
         float newTemp = calculateTemperature(world, entity);
         hashTemp.put(hash, newTemp);
         hashAge.put(hash, totalWorldTime);
         return newTemp;
      } else {
         return temp;
      }
   }

   private static float calculateTemperature(Level world, Entity entity) {
      int tempRange = TemperatureEnum.BURNING.getUpperBound() - TemperatureEnum.FREEZING.getLowerBound() + 1;
      return (float)WorldUtil.calculateClientWorldEntityTemperature(world, entity) / (float)tempRange;
   }
}
