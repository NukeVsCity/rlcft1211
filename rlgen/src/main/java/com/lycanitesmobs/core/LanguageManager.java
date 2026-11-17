package com.lycanitesmobs.core;

import net.minecraft.locale.Language;

/**
 * 1.12 com.lycanitesmobs.client.localisation.LanguageManager.translate, used by common code (creature names, messages).
 * 1.21 Language holds the client's loaded language (with mod lang files) on the client and en_us on a dedicated server.
 */
public class LanguageManager {
   public static String translate(String key) {
      return Language.getInstance().getOrDefault(key);
   }
}
