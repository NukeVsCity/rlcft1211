package com.lycanitesmobs.compat;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;

/** 1.12 EnumParticleTypes.getByName: the 1.12 particle names (exact, case sensitive) to 1.21 particles. */
public final class LycParticles {
   private LycParticles() {
   }

   public static ParticleOptions byName(String name) {
      if (name == null) {
         return null;
      }
      switch (name) {
         case "explode": return ParticleTypes.POOF;
         case "largeexplode": return ParticleTypes.EXPLOSION;
         case "hugeexplosion": return ParticleTypes.EXPLOSION_EMITTER;
         case "fireworksSpark": return ParticleTypes.FIREWORK;
         case "bubble": return ParticleTypes.BUBBLE;
         case "splash": return ParticleTypes.SPLASH;
         case "wake": return ParticleTypes.FISHING;
         case "suspended": return ParticleTypes.UNDERWATER;
         case "depthsuspend": return ParticleTypes.MYCELIUM;
         case "crit": return ParticleTypes.CRIT;
         case "magicCrit": return ParticleTypes.ENCHANTED_HIT;
         case "smoke": return ParticleTypes.SMOKE;
         case "largesmoke": return ParticleTypes.LARGE_SMOKE;
         case "spell": return ParticleTypes.EFFECT;
         case "instantSpell": return ParticleTypes.INSTANT_EFFECT;
         // 1.12 mobSpell / mobSpellAmbient took the colour from the speed arguments (0, 0, 0 here: black)
         case "mobSpell": return ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFF000000);
         case "mobSpellAmbient": return ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0x26000000);
         case "witchMagic": return ParticleTypes.WITCH;
         case "dripWater": return ParticleTypes.DRIPPING_WATER;
         case "dripLava": return ParticleTypes.DRIPPING_LAVA;
         case "angryVillager": return ParticleTypes.ANGRY_VILLAGER;
         case "happyVillager": return ParticleTypes.HAPPY_VILLAGER;
         case "townaura": return ParticleTypes.MYCELIUM;
         case "note": return ParticleTypes.NOTE;
         case "portal": return ParticleTypes.PORTAL;
         case "enchantmenttable": return ParticleTypes.ENCHANT;
         case "flame": return ParticleTypes.FLAME;
         case "lava": return ParticleTypes.LAVA;
         case "cloud": return ParticleTypes.CLOUD;
         // 1.12 reddust with a red of 0 was drawn red
         case "reddust": return DustParticleOptions.REDSTONE;
         case "snowballpoof": return ParticleTypes.ITEM_SNOWBALL;
         case "snowshovel": return ParticleTypes.ITEM_SNOWBALL;
         case "slime": return ParticleTypes.ITEM_SLIME;
         case "heart": return ParticleTypes.HEART;
         case "droplet": return ParticleTypes.RAIN;
         case "mobappearance": return ParticleTypes.ELDER_GUARDIAN;
         case "dragonbreath": return ParticleTypes.DRAGON_BREATH;
         case "endRod": return ParticleTypes.END_ROD;
         case "damageIndicator": return ParticleTypes.DAMAGE_INDICATOR;
         case "sweepAttack": return ParticleTypes.SWEEP_ATTACK;
         case "totem": return ParticleTypes.TOTEM_OF_UNDYING;
         case "spit": return ParticleTypes.SPIT;
         default: return null;
      }
   }
}
