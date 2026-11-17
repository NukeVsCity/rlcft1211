package ichttt.mods.firstaid.common.damagesystem.distribution;

import ichttt.mods.firstaid.api.IDamageDistribution;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.damagesystem.DamageablePart;
import ichttt.mods.firstaid.common.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.common.network.FirstAidNetwork;
import ichttt.mods.firstaid.common.util.ArmorUtils;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** 1.12 EqualDamageDistribution (explosions): reduced by all armor + vanilla potion/enchantment calculations, then spread on all parts. */
public class EqualDamageDistribution implements IDamageDistribution {
   private final boolean tryNoKill;
   private final float reductionMultiplier;

   public EqualDamageDistribution(boolean tryNoKill, float reductionMultiplier) {
      this.tryNoKill = tryNoKill;
      this.reductionMultiplier = reductionMultiplier;
   }

   private float reduceDamage(float originalDamage, Player player, DamageSource source) {
      float damage = originalDamage;
      for (EquipmentSlot slot : CommonUtils.ARMOR_SLOTS) {
         ItemStack armor = player.getItemBySlot(slot);
         damage = ArmorUtils.applyArmor(player, armor, source, damage, slot);
         if (damage <= 0.0F) {
            return 0.0F;
         }
      }
      damage = ArmorUtils.applyPotionDamageCalculations(player, source, damage);
      if (damage <= 0.0F) {
         return 0.0F;
      }
      float reduction = originalDamage - damage;
      if (reduction > 0.0F) {
         reduction *= this.reductionMultiplier;
      }
      damage = originalDamage - reduction;
      return damage <= 0.0F ? 0.0F : DamageDistribution.onLivingDamage(player, source, damage);
   }

   private float distributeOnParts(float damage, PlayerDamageModel damageModel, Player player, boolean tryNoKillThisRound) {
      int iterationCounter = 0;
      int divCount = EnumPlayerPart.VALUES.length;
      float damageLeft = damage;
      float prevDamageLeft;
      do {
         prevDamageLeft = damageLeft;
         float toDamage = damageLeft / (float)divCount;
         divCount = 0;
         damageLeft = 0.0F;
         for (DamageablePart part : damageModel) {
            if (part.currentHealth > 0.0F) {
               damageLeft += part.damage(toDamage, player, !player.hasEffect(FirstAid.MORPHINE), tryNoKillThisRound ? 1.0F : 0.0F);
               divCount++;
            }
         }
         if (iterationCounter >= 50) {
            FirstAid.LOGGER.warn("Not done distribution equally after 50 rounds, diff {}. Dropping!", Math.abs(prevDamageLeft - damageLeft));
            break;
         }
         iterationCounter++;
      } while (prevDamageLeft != damageLeft);
      return damageLeft;
   }

   @Override
   public float distributeDamage(float damage, Player player, DamageSource source, boolean addStat) {
      damage = this.reduceDamage(damage, player, source);
      if (damage <= 0.0F) {
         return 0.0F;
      }
      PlayerDamageModel damageModel = FirstAid.getDamageModel(player);
      float damageLeft = this.distributeOnParts(damage, damageModel, player, this.tryNoKill);
      if (damageLeft > 0.0F && this.tryNoKill) {
         damageLeft = this.distributeOnParts(damage, damageModel, player, false);
      }
      FirstAidNetwork.syncDamageModel((ServerPlayer)player, damageModel, false);
      float effectiveDmg = damage - damageLeft;
      if (effectiveDmg < 3.4028235E37F) {
         player.awardStat(Stats.DAMAGE_TAKEN, Math.round(effectiveDmg * 10.0F));
      }
      return damageLeft;
   }

   @Override
   public boolean skipGlobalPotionModifiers() {
      return true;
   }

   /** 1.12 DirectDamageDistribution (debug command) */
   public static class DirectDamageDistribution implements IDamageDistribution {
      private final EnumPlayerPart part;
      private final boolean debuff;

      public DirectDamageDistribution(EnumPlayerPart part, boolean debuff) {
         this.part = part;
         this.debuff = debuff;
      }

      @Override
      public float distributeDamage(float damage, Player player, DamageSource source, boolean addStat) {
         return FirstAid.getDamageModel(player).getFromEnum(this.part).damage(damage, player, this.debuff);
      }
   }
}
