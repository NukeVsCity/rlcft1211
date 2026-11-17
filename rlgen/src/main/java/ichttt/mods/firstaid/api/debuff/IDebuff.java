package ichttt.mods.firstaid.api.debuff;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** 1.12 IDebuff */
public interface IDebuff {
   void handleDamageTaken(float damage, float healthPerMax, @Nullable ServerPlayer player);

   void handleHealing(float healingDone, float healthPerMax, @Nullable ServerPlayer player);

   default boolean isEnabled() {
      return true;
   }

   default void update(Player player) {
   }

   default void update(Player player, float healthPerMax) {
      this.update(player);
   }
}
