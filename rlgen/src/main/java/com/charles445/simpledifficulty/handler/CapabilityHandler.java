package com.charles445.simpledifficulty.handler;

import com.charles445.simpledifficulty.api.SDCapabilities;
import com.charles445.simpledifficulty.capability.TemperatureCapability;
import com.charles445.simpledifficulty.capability.ThirstCapability;
import com.charles445.simpledifficulty.config.ModConfig;
import com.charles445.simpledifficulty.network.PacketHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** 1.12 CapabilityHandler: server-side ticking of both capabilities and the routine / dirty sync to the client (START phase). */
public class CapabilityHandler {
   @SubscribeEvent
   public void onPlayerTickPre(PlayerTickEvent.Pre event) {
      this.onPlayerTick(event.getEntity(), true);
   }

   @SubscribeEvent
   public void onPlayerTickPost(PlayerTickEvent.Post event) {
      this.onPlayerTick(event.getEntity(), false);
   }

   private void onPlayerTick(Player player, boolean start) {
      Level world = player.level();
      if (!world.isClientSide && player instanceof ServerPlayer sp) {
         int packetTimerThreshold = ModConfig.server.miscellaneous.routinePacketDelay;
         if (ModConfig.isTemperatureEnabled()) {
            TemperatureCapability temperatureCapability = SDCapabilities.getTemperatureData(player);
            temperatureCapability.tickUpdate(player, world, start);
            if (start && (temperatureCapability.isDirty() || temperatureCapability.getPacketTimer() % packetTimerThreshold == 0)) {
               temperatureCapability.setClean();
               PacketHandler.sendTemperatureUpdate(sp);
            }
         }
         if (ModConfig.isThirstEnabled() && !player.isSpectator() && !player.isCreative()) {
            ThirstCapability thirstCapability = SDCapabilities.getThirstData(player);
            thirstCapability.tickUpdate(player, world, start);
            if (start && (thirstCapability.isDirty() || thirstCapability.getPacketTimer() % packetTimerThreshold == 0)) {
               thirstCapability.setClean();
               PacketHandler.sendThirstUpdate(sp);
            }
         }
      }
   }
}
