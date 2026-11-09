package com.github.alexthe666.iceandfire.client;

import com.github.alexthe666.iceandfire.entity.EntityDeathWorm;
import com.github.alexthe666.iceandfire.message.MessageDeathWormControl;
import javax.annotation.Nullable;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.PacketDistributor;

/** Client-only code the land mob entities call while on the client side (kept out of the entity classes). */
public final class LandClientHooks {
   private LandClientHooks() {
   }

   @Nullable
   private static KeyMapping strike;

   /** ModKeys.dragon_strike, registered by the dragon client part ("key.dragon_strike"); none until then. */
   @Nullable
   private static KeyMapping strikeKey() {
      if (strike == null) {
         for (KeyMapping k : Minecraft.getInstance().options.keyMappings) {
            if ("key.dragon_strike".equals(k.getName())) {
               strike = k;
            }
         }
      }
      return strike;
   }

   /** EntityDeathWorm.updateClientControls */
   public static void deathWormControls(EntityDeathWorm worm) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return;
      }
      if (worm.isRidingPlayer(mc.player)) {
         byte previousState = worm.getControlState();
         worm.up(mc.options.keyJump.isDown());
         worm.dismount(mc.options.keyShift.isDown());
         KeyMapping k = strikeKey();
         worm.attack(k != null && k.isDown());
         byte controlState = worm.getControlState();
         if (controlState != previousState) {
            PacketDistributor.sendToServer(new MessageDeathWormControl(worm.getId(), controlState, worm.getX(), worm.getY(), worm.getZ()));
         }
      }

      if (worm.getVehicle() != null && worm.getVehicle() == mc.player) {
         byte previousState = worm.getControlState();
         worm.dismount(mc.options.keyShift.isDown());
         byte controlState = worm.getControlState();
         if (controlState != previousState) {
            PacketDistributor.sendToServer(new MessageDeathWormControl(worm.getId(), controlState, worm.getX(), worm.getY(), worm.getZ()));
         }
      }
   }

   /** tail_buffer.calculateChainSwingBuffer(90, 20, 5, worm). STUB(iaf/llibrary): needs ChainBuffer. */
   public static void deathWormTail(EntityDeathWorm worm) {
   }
}
