package com.lycanitesmobs.client;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.gui.beastiary.CreaturesBeastiaryScreen;
import com.lycanitesmobs.client.gui.beastiary.IndexBeastiaryScreen;
import com.lycanitesmobs.client.gui.beastiary.PetsBeastiaryScreen;
import com.lycanitesmobs.client.gui.beastiary.SummoningBeastiaryScreen;
import com.lycanitesmobs.client.gui.buttons.TabManager;
import com.lycanitesmobs.client.gui.overlays.MinionSelectionOverlay;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.network.MessagePlayerAttack;
import com.lycanitesmobs.core.network.MessagePlayerControl;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

/**
 * 1.12 KeyHandler. The 1.12 LWJGL 2 key codes are mapped to the same keys in GLFW: C (dismount), left shift (descend),
 * X (mount ability), G (index), R (minion selection); the others are unbound like 1.12. Registered from ClientProxy.
 */
public class KeyHandler {
   public static KeyHandler instance;
   public Minecraft mc;
   public boolean inventoryOpen = false;
   public KeyMapping dismount = new KeyMapping("key.mount.dismount", GLFW.GLFW_KEY_C, "Lycanites Mobs");
   public KeyMapping descend = new KeyMapping("key.mount.descend", GLFW.GLFW_KEY_LEFT_SHIFT, "Lycanites Mobs");
   public KeyMapping mountAbility = new KeyMapping("key.mount.ability", GLFW.GLFW_KEY_X, "Lycanites Mobs");
   public KeyMapping mountInventory = new KeyMapping("key.mount.inventory", InputConstants.UNKNOWN.getValue(), "Lycanites Mobs");
   public KeyMapping beastiary = new KeyMapping("key.beastiary", InputConstants.UNKNOWN.getValue(), "Lycanites Mobs");
   public KeyMapping index = new KeyMapping("key.index", GLFW.GLFW_KEY_G, "Lycanites Mobs");
   public KeyMapping pets = new KeyMapping("key.pets", InputConstants.UNKNOWN.getValue(), "Lycanites Mobs");
   public KeyMapping summoning = new KeyMapping("key.summoning", InputConstants.UNKNOWN.getValue(), "Lycanites Mobs");
   public KeyMapping minionSelection = new KeyMapping("key.minions", GLFW.GLFW_KEY_R, "Lycanites Mobs");

   public KeyHandler(Minecraft mc) {
      this.mc = mc;
      instance = this;
      NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, e -> this.onPlayerTick());
      NeoForge.EVENT_BUS.addListener(InputEvent.InteractionKeyMappingTriggered.class, this::onMouseEvent);
   }

   /** 1.12 ClientRegistry.registerKeyBinding calls. */
   public void registerKeys(RegisterKeyMappingsEvent event) {
      event.register(this.dismount);
      event.register(this.descend);
      event.register(this.mountAbility);
      event.register(this.mountInventory);
      event.register(this.index);
      event.register(this.beastiary);
      event.register(this.pets);
      event.register(this.summoning);
      event.register(this.minionSelection);
   }

   private static boolean isDown(KeyMapping key) {
      InputConstants.Key k = key.getKey();
      if (k.getValue() == InputConstants.UNKNOWN.getValue()) {
         return false;
      }
      long window = Minecraft.getInstance().getWindow().getWindow();
      return k.getType() == InputConstants.Type.MOUSE
         ? GLFW.glfwGetMouseButton(window, k.getValue()) == GLFW.GLFW_PRESS
         : InputConstants.isKeyDown(window, k.getValue());
   }

   public void onPlayerTick() {
      if (this.mc.player == null) {
         return;
      }
      ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(this.mc.player);
      if (playerExt != null) {
         byte controlStates = 0;
         // 1.12 Mouse.isButtonDown(1)
         if (GLFW.glfwGetMouseButton(this.mc.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS) {
            controlStates += ExtendedPlayer.CONTROL_ID.RIGHT_MOUSE.id;
         }

         if (!this.inventoryOpen && this.mc.screen != null && this.mc.screen.getClass() == InventoryScreen.class) {
            TabManager.addTabsToInventory(this.mc.screen);
            this.inventoryOpen = true;
         }

         if (this.inventoryOpen && (this.mc.screen == null || this.mc.screen.getClass() != InventoryScreen.class)) {
            this.inventoryOpen = false;
         }

         if (this.mountInventory.consumeClick()) {
            controlStates += ExtendedPlayer.CONTROL_ID.MOUNT_INVENTORY.id;
         }

         if (this.index.consumeClick()) {
            IndexBeastiaryScreen.openToPlayer(this.mc.player);
         }

         if (this.beastiary.consumeClick()) {
            CreaturesBeastiaryScreen.openToPlayer(this.mc.player);
         }

         if (this.pets.consumeClick()) {
            PetsBeastiaryScreen.openToPlayer(this.mc.player);
         }

         if (this.summoning.consumeClick()) {
            SummoningBeastiaryScreen.openToPlayer(this.mc.player);
         }

         if (!isDown(this.minionSelection) && this.mc.screen instanceof MinionSelectionOverlay) {
            this.mc.player.closeContainer();
         }

         // 1.12 mc.inGameHasFocus
         if (this.mc.screen == null && this.mc.mouseHandler.isMouseGrabbed()) {
            if (this.minionSelection.consumeClick()) {
               MinionSelectionOverlay.openToPlayer(this.mc.player);
            }

            if (this.mc.options.keyJump.isDown()) {
               controlStates += ExtendedPlayer.CONTROL_ID.JUMP.id;
            }

            if (this.descend.isDown()) {
               controlStates += ExtendedPlayer.CONTROL_ID.DESCEND.id;
            }

            if (this.dismount.isDown()) {
               controlStates += ExtendedPlayer.CONTROL_ID.MOUNT_DISMOUNT.id;
            }

            if (this.mountAbility.isDown()) {
               controlStates += ExtendedPlayer.CONTROL_ID.MOUNT_ABILITY.id;
            }

            if (this.mc.options.keyAttack.isDown()) {
               controlStates += ExtendedPlayer.CONTROL_ID.ATTACK.id;
            }
         }

         if (controlStates != playerExt.controlStates) {
            MessagePlayerControl message = new MessagePlayerControl(controlStates);
            LycanitesMobs.packetHandler.sendToServer(message);
            playerExt.controlStates = controlStates;
         }
      }
   }

   /** 1.12 MouseEvent, button 0: attacks on big entities out of vanilla reach are sent to the server. */
   public void onMouseEvent(InputEvent.InteractionKeyMappingTriggered event) {
      ExtendedPlayer playerExt = this.mc.player == null ? null : ExtendedPlayer.getForPlayer(this.mc.player);
      if (this.mc.player != null && playerExt != null && this.mc.hitResult != null) {
         if (event.isAttack() && !this.mc.player.isSpectator() && !(this.mc.player.getVehicle() instanceof net.minecraft.world.entity.vehicle.Boat)
            && this.mc.hitResult.getType() == HitResult.Type.ENTITY) {
            Entity entityHit = ((EntityHitResult)this.mc.hitResult).getEntity();
            if (playerExt.canMeleeBigEntity(entityHit)) {
               MessagePlayerAttack message = new MessagePlayerAttack(entityHit);
               LycanitesMobs.packetHandler.sendToServer(message);
            }
         }
      }
   }
}
