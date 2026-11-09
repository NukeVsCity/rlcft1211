package com.github.alexthe666.iceandfire.core;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/** Ice and Fire 1.7.1 ModKeys (client only): R breath, G strike, X down, F7 dragon camera view. */
@EventBusSubscriber(modid = "iceandfire", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ModKeys {
   private ModKeys() {
   }

   public static final KeyMapping dragon_fireAttack = new KeyMapping("key.dragon_fireAttack", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.gameplay");
   public static final KeyMapping dragon_strike = new KeyMapping("key.dragon_strike", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.categories.gameplay");
   public static final KeyMapping dragon_down = new KeyMapping("key.dragon_down", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, "key.categories.gameplay");
   public static final KeyMapping dragon_change_view = new KeyMapping("key.dragon_change_view", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F7, "key.categories.misc");

   @SubscribeEvent
   public static void register(RegisterKeyMappingsEvent event) {
      event.register(dragon_fireAttack);
      event.register(dragon_strike);
      event.register(dragon_down);
      event.register(dragon_change_view);
   }
}
