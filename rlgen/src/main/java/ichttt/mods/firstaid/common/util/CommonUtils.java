package ichttt.mods.firstaid.common.util;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.damagesystem.DamageablePart;
import ichttt.mods.firstaid.common.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.common.damagesystem.distribution.HealthDistribution;
import ichttt.mods.firstaid.common.network.FirstAidNetwork;
import ichttt.mods.firstaid.mixin.LivingEntityAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

/** 1.12 CommonUtils. */
public class CommonUtils {
   /** 1.12 index order: FEET, LEGS, CHEST, HEAD */
   public static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};
   private static final Map<EquipmentSlot, List<EnumPlayerPart>> slotToParts = new EnumMap<>(EquipmentSlot.class);

   static {
      slotToParts.put(EquipmentSlot.HEAD, Collections.singletonList(EnumPlayerPart.HEAD));
      slotToParts.put(EquipmentSlot.CHEST, Arrays.asList(EnumPlayerPart.LEFT_ARM, EnumPlayerPart.RIGHT_ARM, EnumPlayerPart.BODY));
      slotToParts.put(EquipmentSlot.LEGS, Arrays.asList(EnumPlayerPart.LEFT_LEG, EnumPlayerPart.RIGHT_LEG));
      slotToParts.put(EquipmentSlot.FEET, Arrays.asList(EnumPlayerPart.LEFT_FOOT, EnumPlayerPart.RIGHT_FOOT));
   }

   public static List<EnumPlayerPart> getPartListForSlot(EquipmentSlot slot) {
      return new ArrayList<>(slotToParts.get(slot));
   }

   public static EnumPlayerPart[] getPartArrayForSlot(EquipmentSlot slot) {
      return getPartListForSlot(slot).toArray(new EnumPlayerPart[0]);
   }

   /** Totem check with the setHealth tracking off (the totem's setHealth(1) must not heal the parts), else vanilla health 0. */
   public static void killPlayer(PlayerDamageModel damageModel, Player player, @Nullable DamageSource source) {
      if (player.level().isClientSide) {
         FirstAid.LOGGER.warn("Tried to kill the player on the client!");
      }
      if (source != null && FirstAidConfig.externalHealing.allowOtherHealingItems) {
         boolean protection;
         FirstAid.setTracking(player, false);
         try {
            protection = ((LivingEntityAccessor)player).firstaid$checkTotemDeathProtection(source);
         } finally {
            FirstAid.setTracking(player, true);
         }
         if (protection) {
            for (DamageablePart part : damageModel) {
               if (part.canCauseDeath) {
                  part.currentHealth = Math.max(part.currentHealth, 1.0F);
               }
            }
            if (player instanceof ServerPlayer sp) {
               FirstAidNetwork.syncDamageModel(sp, damageModel, false);
            }
            return;
         }
      }
      FirstAid.setHealthImpl(player, 0.0F);
   }

   public static boolean isValidArmorSlot(EquipmentSlot slot) {
      return slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR;
   }

   public static boolean isSurvivalOrAdventure(Player player) {
      return !player.isSpectator() && !player.isCreative();
   }

   public static void healPlayerByPercentage(double percentage, PlayerDamageModel damageModel, Player player) {
      Objects.requireNonNull(damageModel);
      int healValue = Math.toIntExact(Math.round((double)damageModel.getCurrentMaxHealth() * percentage));
      HealthDistribution.manageHealth((float)healValue, damageModel, player, true, false);
   }
}
