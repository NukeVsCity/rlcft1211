package com.github.alexthe666.iceandfire.client;

import com.github.alexthe666.iceandfire.core.ModKeys;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.message.DragonNetwork;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/** 1.12 EntityDragonBase.updateClientControls: the rider's keys become the dragon's control state, sent to the server. */
@EventBusSubscriber(modid = "iceandfire", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class DragonClientControls {
   private DragonClientControls() {
   }

   @SubscribeEvent
   public static void setup(FMLClientSetupEvent event) {
      EntityDragonBase.clientControls = DragonClientControls::updateClientControls;
   }

   public static void updateClientControls(EntityDragonBase dragon) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return;
      }
      if (dragon.isRidingPlayer(mc.player)) {
         byte previousState = dragon.getControlState();
         dragon.up(mc.options.keyJump.isDown());
         dragon.down(ModKeys.dragon_down.isDown());
         dragon.attack(ModKeys.dragon_fireAttack.isDown());
         dragon.strike(ModKeys.dragon_strike.isDown());
         dragon.dismount(mc.options.keyShift.isDown());
         byte controlState = dragon.getControlState();
         if (controlState != previousState) {
            DragonNetwork.sendControl(dragon, controlState);
         }
      }

      if (dragon.getVehicle() != null && dragon.getVehicle() == mc.player) {
         byte previousState = dragon.getControlState();
         dragon.dismount(mc.options.keyShift.isDown());
         byte controlState = dragon.getControlState();
         if (controlState != previousState) {
            DragonNetwork.sendControl(dragon, controlState);
         }
      }
   }
}
