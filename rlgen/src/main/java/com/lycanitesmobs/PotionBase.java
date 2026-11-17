package com.lycanitesmobs;

import com.lycanitesmobs.compat.LycReg;
import com.lycanitesmobs.core.AssetManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 1.12 PotionBase (a Potion with Lycanites' name and icon texture). The inventory/HUD icon rendering of 1.12 is client side
 * (lyc/client draws textures/effects/<name>.png). Effects are referred to by Holder (ObjectManager.getEffect).
 */
public class PotionBase extends MobEffect {
   public String name;

   public PotionBase(String name, boolean badEffect, int color) {
      super(badEffect ? MobEffectCategory.HARMFUL : MobEffectCategory.BENEFICIAL, color);
      this.name = name;
      LycReg.setName(this, "lycanitesmobs", name);
      AssetManager.addTexture("effect." + name, LycanitesMobs.modInfo, "textures/effects/" + name + ".png");
   }

   public boolean isInstant() {
      return false;
   }
}
