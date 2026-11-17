package ichttt.mods.firstaid.common.damagesystem.distribution;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.IDamageDistribution;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.damagesystem.DamageablePart;
import ichttt.mods.firstaid.common.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.common.network.FirstAidNetwork;
import ichttt.mods.firstaid.common.util.ArmorUtils;
import ichttt.mods.firstaid.common.util.CommonUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import org.apache.commons.lang3.tuple.Pair;

/** 1.12 IDamageDistribution + DamageDistribution (the per-slot armor/enchantment/LivingDamageEvent pipeline and the part damage). */
public abstract class DamageDistribution implements IDamageDistribution {
   public static float handleDamageTaken(IDamageDistribution damageDistribution, PlayerDamageModel damageModel, float damage, Player player, DamageSource source,
                                         boolean addStat, boolean redistributeIfLeft) {
      if (!damageDistribution.skipGlobalPotionModifiers()) {
         damage = ArmorUtils.applyGlobalPotionModifiers(player, source, damage);
      }
      if (damage != 0.0F) {
         player.causeFoodExhaustion(source.getFoodExhaustion());
         player.getCombatTracker().recordDamage(source, damage);
      }
      float left = damageDistribution.distributeDamage(damage, player, source, addStat);
      if (left > 0.0F && redistributeIfLeft) {
         boolean hasTriedNoKill = damageDistribution == RandomDamageDistribution.NEAREST_NOKILL || damageDistribution == RandomDamageDistribution.ANY_NOKILL;
         IDamageDistribution next = hasTriedNoKill ? RandomDamageDistribution.NEAREST_KILL : RandomDamageDistribution.getDefault();
         left = next.distributeDamage(left, player, source, addStat);
         if (left > 0.0F && !hasTriedNoKill) {
            left = RandomDamageDistribution.NEAREST_KILL.distributeDamage(left, player, source, addStat);
         }
      }
      // FirstAidLivingDamageEvent: no listener can cancel it here
      if (damageModel.isDead(player)) {
         CommonUtils.killPlayer(damageModel, player, source);
      }
      return left;
   }

   protected float minHealth(Player player, DamageablePart part) {
      return 0.0F;
   }

   protected float distributeDamageOnParts(float damage, PlayerDamageModel damageModel, EnumPlayerPart[] enumParts, Player player, boolean addStat) {
      ArrayList<DamageablePart> damageableParts = new ArrayList<>(enumParts.length);
      for (EnumPlayerPart part : enumParts) {
         damageableParts.add(damageModel.getFromEnum(part));
      }
      Collections.shuffle(damageableParts);
      for (DamageablePart part : damageableParts) {
         float minHealth = this.minHealth(player, part);
         float dmgDone = damage - part.damage(damage, player, !player.hasEffect(FirstAid.MORPHINE), minHealth);
         FirstAidNetwork.updatePart((ServerPlayer)player, part);
         if (addStat) {
            player.awardStat(Stats.DAMAGE_TAKEN, Math.round(dmgDone * 10.0F));
         }
         damage -= dmgDone;
         if (damage == 0.0F) {
            break;
         }
         if (damage < 0.0F) {
            FirstAid.LOGGER.error("Got negative damage {} left? Logic error? ", damage);
            break;
         }
      }
      return damage;
   }

   protected abstract List<Pair<EquipmentSlot, EnumPlayerPart[]>> getPartList();

   /** 1.12 ForgeHooks.onLivingDamage: the LivingDamageEvent after armor */
   protected static float onLivingDamage(Player player, DamageSource source, float damage) {
      return CommonHooks.onLivingDamagePre(player, new DamageContainer(source, damage));
   }

   @Override
   public float distributeDamage(float damage, Player player, DamageSource source, boolean addStat) {
      if (damage <= 0.0F) {
         return 0.0F;
      }
      PlayerDamageModel damageModel = FirstAid.getDamageModel(player);
      for (Pair<EquipmentSlot, EnumPlayerPart[]> pair : this.getPartList()) {
         EquipmentSlot slot = pair.getLeft();
         EnumPlayerPart[] parts = pair.getRight();
         if (Arrays.stream(parts).map(damageModel::getFromEnum).anyMatch(part -> part.currentHealth > this.minHealth(player, part))) {
            float originalDamage = damage;
            damage = ArmorUtils.applyArmor(player, player.getItemBySlot(slot), source, damage, slot);
            if (damage <= 0.0F) {
               return 0.0F;
            }
            damage = ArmorUtils.applyEnchantmentModifiers(player, slot, source, damage);
            if (damage <= 0.0F) {
               return 0.0F;
            }
            damage = onLivingDamage(player, source, damage);
            if (damage <= 0.0F) {
               return 0.0F;
            }
            float reducedDamage = damage;
            damage = this.distributeDamageOnParts(damage, damageModel, parts, player, addStat);
            if (damage == 0.0F) {
               break;
            }
            float absorbFactor = originalDamage / reducedDamage;
            float damageDistributed = reducedDamage - damage;
            damage = originalDamage - damageDistributed * absorbFactor;
         }
      }
      return damage;
   }
}
