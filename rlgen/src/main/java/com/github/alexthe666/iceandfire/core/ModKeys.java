package com.github.alexthe666.iceandfire.core;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/**
 * 1.12 ModKeys (client only). Same keys and categories as 1.12: LWJGL2 key codes 19 (R), 34 (G), 45 (X), 65 (F7) are the GLFW
 * keys R, G, X, F7. Registered from {@code DragonClient} (RegisterKeyMappingsEvent).
 */
public class ModKeys {
   public static KeyMapping dragon_fireAttack;
   public static KeyMapping dragon_strike;
   public static KeyMapping dragon_down;
   public static KeyMapping dragon_change_view;

   public static void init(RegisterKeyMappingsEvent event) {
      dragon_fireAttack = new KeyMapping("key.dragon_fireAttack", InputConstants.KEY_R, "key.categories.gameplay");
      dragon_strike = new KeyMapping("key.dragon_strike", InputConstants.KEY_G, "key.categories.gameplay");
      dragon_down = new KeyMapping("key.dragon_down", InputConstants.KEY_X, "key.categories.gameplay");
      dragon_change_view = new KeyMapping("key.dragon_change_view", InputConstants.KEY_F7, "key.categories.misc");
      event.register(dragon_fireAttack);
      event.register(dragon_strike);
      event.register(dragon_down);
      event.register(dragon_change_view);
   }
}
