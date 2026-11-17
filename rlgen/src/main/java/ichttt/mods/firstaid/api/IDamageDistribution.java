package ichttt.mods.firstaid.api;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

/** 1.12 IDamageDistribution */
public interface IDamageDistribution {
   float distributeDamage(float damage, Player player, DamageSource source, boolean addStat);

   default boolean skipGlobalPotionModifiers() {
      return false;
   }
}
