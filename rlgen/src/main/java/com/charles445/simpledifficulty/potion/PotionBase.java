package com.charles445.simpledifficulty.potion;

import com.charles445.simpledifficulty.api.SDCapabilities;
import com.charles445.simpledifficulty.api.SDDamageSources;
import com.charles445.simpledifficulty.api.SDPotions;
import com.charles445.simpledifficulty.capability.TemperatureCapability;
import com.charles445.simpledifficulty.config.ModConfig;
import com.charles445.simpledifficulty.util.DamageUtil;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * 1.12 PotionBase and its subclasses. 1.12 Potion.isReady(duration, amplifier) = shouldApplyEffectTickThisTick, performEffect =
 * applyEffectTick. isBadEffect true = HARMFUL, setBeneficial = BENEFICIAL, else NEUTRAL. The icons are the 1.12 textures/potions
 * files as mob_effect sprites.
 */
public abstract class PotionBase extends MobEffect {
   protected PotionBase(MobEffectCategory category, int liquidColor) {
      super(category, liquidColor);
   }

   public void removePotionCoreEffect(LivingEntity entity, Holder<MobEffect> potion) {
      MobEffectInstance active = entity.getEffect(potion);
      if (active != null && active.getDuration() > 1) {
         entity.removeEffect(potion);
         entity.addEffect(new MobEffectInstance(potion, 1));
      }
   }

   @Override
   public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
      return this.isReadyVar(duration, amplifier, 50);
   }

   public boolean isReadyVar(int duration, int amplifier, int var) {
      int k = var >> amplifier;
      return k > 0 ? duration % k == 0 : true;
   }

   /** PotionThermia */
   public abstract static class Thermia extends PotionBase {
      private final ResourceKey<DamageType> damage;

      protected Thermia(int color, ResourceKey<DamageType> damage) {
         super(MobEffectCategory.HARMFUL, color);
         this.damage = damage;
      }

      @Override
      public boolean applyEffectTick(LivingEntity entity, int amplifier) {
         // 1.12: both thermias test heat_resist here
         if (entity instanceof Player player && !entity.hasEffect(SDPotions.heat_resist)) {
            Level world = entity.level();
            if (DamageUtil.isModDangerous(world) && DamageUtil.healthAboveDifficulty(world, player)) {
               TemperatureCapability capability = SDCapabilities.getTemperatureData(player);
               float damage = 0.5F + 0.5F * (float)capability.getTemperatureDamageCounter() * (float)ModConfig.server.temperature.temperatureDamageScaling;
               player.hurt(SDDamageSources.source(player, this.damage), damage);
               capability.addTemperatureDamageCounter(1);
            }
         }
         return true;
      }
   }

   public static class Hyperthermia extends Thermia {
      public Hyperthermia() {
         super(16762972, SDDamageSources.HYPERTHERMIA);
      }
   }

   public static class Hypothermia extends Thermia {
      public Hypothermia() {
         super(6089727, SDDamageSources.HYPOTHERMIA);
      }
   }

   public static class Parasites extends PotionBase {
      private int savedDuration = 0;

      public Parasites() {
         super(MobEffectCategory.NEUTRAL, 16769463);
      }

      @Override
      public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
         this.savedDuration = duration;
         return true;
      }

      @Override
      public boolean applyEffectTick(LivingEntity entity, int amplifier) {
         if (entity instanceof Player player) {
            Level world = entity.level();
            double hunger = ModConfig.server.thirst.thirstParasitesHunger;
            if (hunger > 0.0) {
               player.causeFoodExhaustion((float)hunger * (float)(amplifier + 1));
            }
            if (DamageUtil.isModDangerous(world) && DamageUtil.healthAboveDifficulty(world, player)) {
               double poison = ModConfig.server.thirst.thirstParasitesDamage;
               if (poison > 0.0 && this.isReadyVar(this.savedDuration, amplifier, 25) && player.getRandom().nextDouble() < poison) {
                  player.hurt(SDDamageSources.source(player, SDDamageSources.PARASITES), 1.0F);
               }
            }
         }
         return true;
      }
   }

   public static class Thirsty extends PotionBase {
      public Thirsty() {
         super(MobEffectCategory.HARMFUL, 2856192);
      }

      @Override
      public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
         return true;
      }

      @Override
      public boolean applyEffectTick(LivingEntity entity, int amplifier) {
         if (entity instanceof Player player) {
            SDCapabilities.getThirstData(player).addThirstExhaustion((float)ModConfig.server.thirst.thirstyStrength * (float)(1 + amplifier));
         }
         return true;
      }
   }

   public static class ResistCold extends PotionBase {
      public ResistCold() {
         super(MobEffectCategory.BENEFICIAL, 9368063);
      }

      @Override
      public boolean applyEffectTick(LivingEntity entity, int amplifier) {
         this.removePotionCoreEffect(entity, SDPotions.hypothermia);
         return true;
      }
   }

   public static class ResistHeat extends PotionBase {
      public ResistHeat() {
         super(MobEffectCategory.BENEFICIAL, 16764274);
      }

      @Override
      public boolean applyEffectTick(LivingEntity entity, int amplifier) {
         this.removePotionCoreEffect(entity, SDPotions.hyperthermia);
         return true;
      }
   }
}
