package ichttt.mods.firstaid.common.damagesystem.distribution;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.damagesystem.DamageablePart;
import ichttt.mods.firstaid.common.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.common.network.FirstAidNetwork;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** 1.12 HealthDistribution: healing from outside (potions, food regen, setHealth) spread on the parts. */
public class HealthDistribution {
   private static final List<EnumPlayerPart> parts = new ArrayList<>(Arrays.asList(EnumPlayerPart.VALUES));

   public static void manageHealth(float health, PlayerDamageModel damageModel, Player player, boolean sendChanges, boolean distribute) {
      if (sendChanges && player.level().isClientSide) {
         FirstAid.LOGGER.warn("The sendChanges flag was set on the client, it can however only work on the server!");
         sendChanges = false;
      } else if (sendChanges && !(player instanceof ServerPlayer)) {
         sendChanges = false;
      }
      float toHeal = distribute ? health / 8.0F : health;
      Collections.shuffle(parts, new java.util.Random(player.level().random.nextLong()));
      List<DamageablePart> damageableParts = new ArrayList<>(parts.size());
      for (EnumPlayerPart part : parts) {
         damageableParts.add(damageModel.getFromEnum(part));
      }
      if (distribute) {
         damageableParts.sort(Comparator.comparingDouble(value -> (double)((float)value.getMaxHealth() - value.currentHealth)));
      }
      float[] healingDone = new float[8];
      for (int i = 0; i < 8; i++) {
         DamageablePart part = damageableParts.get(i);
         float diff = toHeal - part.heal(toHeal, player, !player.level().isClientSide);
         diff = (float)Math.round(diff * 10000.0F) / 10000.0F;
         healingDone[part.part.id - 1] = diff;
         health -= diff;
         if (distribute) {
            if (i < 7) {
               toHeal = health / (7.0F - (float)i);
            }
         } else {
            toHeal -= diff;
            if (toHeal <= 0.0F) {
               break;
            }
         }
      }
      if (sendChanges) {
         ServerPlayer sp = (ServerPlayer)player;
         if (sp.connection != null) {
            FirstAidNetwork.addHealth(sp, healingDone);
         } else {
            damageModel.scheduleResync();
         }
      }
   }

   public static void distributeHealth(float health, Player player, boolean sendChanges) {
      manageHealth(health, FirstAid.getDamageModel(player), player, sendChanges, true);
   }

   public static void addRandomHealth(float health, Player player, boolean sendChanges) {
      manageHealth(health, FirstAid.getDamageModel(player), player, sendChanges, false);
   }
}
