package com.charles445.simpledifficulty.handler;

import com.charles445.simpledifficulty.api.SDCapabilities;
import com.charles445.simpledifficulty.api.config.JsonConfig;
import com.charles445.simpledifficulty.config.ModConfig;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

/** 1.12 TemperatureHandler: consumables with a temporary temperature modifier. */
public class TemperatureHandler {
   @SubscribeEvent
   public void onLivingEntityUseItemFinish(LivingEntityUseItemEvent.Finish event) {
      if (ModConfig.isTemperatureEnabled() && event.getEntity() instanceof Player player && !player.level().isClientSide) {
         ItemStack stack = event.getItem();
         List<JsonConfig.JsonConsumableTemperature> consumableList = JsonConfig.consumableTemperature.get(stack.getItem());
         if (consumableList != null) {
            for (JsonConfig.JsonConsumableTemperature jct : consumableList) {
               if (jct != null && jct.matches(stack)) {
                  SDCapabilities.getTemperatureData(player).setTemporaryModifier(jct.group, jct.temperature, jct.duration);
                  break;
               }
            }
         }
      }
   }
}
