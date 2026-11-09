package com.github.alexthe666.iceandfire.entity.ai.legacy;

import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** 1.12.2 World helpers the ported AI needs. */
public final class LegacyAi {
   private LegacyAi() {
   }

   /** 1.12.2 World.getNearestAttackablePlayer(x, y, z, maxXZDistance, maxYDistance, null, predicate), incl. its maxY * maxY check. */
   @Nullable
   public static Player getNearestAttackablePlayer(Level level, double x, double y, double z, double maxXZ, double maxY, @Nullable Predicate<Player> predicate) {
      return getNearestAttackablePlayer(level, x, y, z, maxXZ, maxY, null, predicate);
   }

   /** The same with the playerToDouble range multiplier. */
   @Nullable
   public static Player getNearestAttackablePlayer(
      Level level, double x, double y, double z, double maxXZ, double maxY, @Nullable ToDoubleFunction<Player> weight, @Nullable Predicate<Player> predicate
   ) {
      double closest = -1.0;
      Player result = null;

      for (Player player : level.players()) {
         if (!player.getAbilities().invulnerable && player.isAlive() && !player.isSpectator() && (predicate == null || predicate.test(player))) {
            double distSq = player.distanceToSqr(x, player.getY(), z);
            double range = maxXZ;
            if (player.isShiftKeyDown()) {
               range = maxXZ * 0.8F;
            }

            if (player.isInvisible()) {
               float f = player.getArmorCoverPercentage();
               if (f < 0.1F) {
                  f = 0.1F;
               }

               range *= (double)(0.7F * f);
            }

            if (weight != null) {
               range *= weight.applyAsDouble(player);
            }

            if ((maxY < 0.0 || Math.abs(player.getY() - y) < maxY * maxY) && (maxXZ < 0.0 || distSq < range * range) && (closest == -1.0 || distSq < closest)) {
               closest = distSq;
               result = player;
            }
         }
      }

      return result;
   }
}
