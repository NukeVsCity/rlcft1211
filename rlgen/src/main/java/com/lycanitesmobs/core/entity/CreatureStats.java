package com.lycanitesmobs.core.entity;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
import com.lycanitesmobs.compat.*;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.*;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.*;
import net.minecraft.network.chat.*;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.*;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.ambient.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.item.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.*;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.*;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.common.*;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.*;
import net.neoforged.neoforge.event.tick.*;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.Variant;
import java.util.Locale;


public class CreatureStats {
   public static int BASE_LEVELUP_EXPERIENCE = 100;
   public static String[] STAT_NAMES = new String[]{
      "health", "defense", "armor", "speed", "damage", "attackSpeed", "rangedSpeed", "effect", "amplifier", "pierce", "sight"
   };
   public BaseCreatureEntity entity;

   public CreatureStats(BaseCreatureEntity entity) {
      this.entity = entity;
   }

   public double getHealth() {
      String statName = "health";
      double statValue = this.entity.creatureInfo.health;
      if (!this.entity.isTamed()) {
         statValue *= this.getDifficultyMultiplier(statName);
         statValue *= this.getVariantMultiplier(statName);
         if (this.entity.extraMobBehaviour != null) {
            statValue *= this.entity.extraMobBehaviour.multiplierHealth;
            statValue += (double)this.entity.extraMobBehaviour.boostHealth;
         }
      }

      statValue *= this.getLevelMultiplier(statName);
      return Math.max(0.0, statValue);
   }

   public double getDefense() {
      String statName = "defense";
      double statValue = this.entity.creatureInfo.defense;
      if (!this.entity.isTamed()) {
         statValue *= this.getDifficultyMultiplier(statName);
         statValue *= this.getVariantMultiplier(statName);
         if (this.entity.extraMobBehaviour != null) {
            statValue *= this.entity.extraMobBehaviour.multiplierDefense;
            statValue += (double)this.entity.extraMobBehaviour.boostDefense;
         }
      }

      return statValue * this.getLevelMultiplier(statName);
   }

   public double getArmor() {
      String statName = "armor";
      double statValue = this.entity.creatureInfo.armor;
      if (!this.entity.isTamed()) {
         statValue *= this.getDifficultyMultiplier(statName);
         statValue *= this.getVariantMultiplier(statName);
         if (this.entity.extraMobBehaviour != null) {
            statValue *= this.entity.extraMobBehaviour.multiplierArmor;
            statValue += (double)this.entity.extraMobBehaviour.boostArmor;
         }
      }

      return statValue * this.getLevelMultiplier(statName);
   }

   public double getSpeed() {
      String statName = "speed";
      double statValue = this.entity.creatureInfo.speed / 100.0;
      if (!this.entity.isTamed()) {
         statValue *= this.getDifficultyMultiplier(statName);
         statValue *= this.getVariantMultiplier(statName);
         if (this.entity.extraMobBehaviour != null) {
            statValue *= this.entity.extraMobBehaviour.multiplierSpeed;
            statValue += (double)this.entity.extraMobBehaviour.boostSpeed;
         }
      }

      return statValue * this.getLevelMultiplier(statName);
   }

   public double getDamage() {
      String statName = "damage";
      double statValue = this.entity.creatureInfo.damage;
      if (!this.entity.isTamed()) {
         statValue *= this.getDifficultyMultiplier(statName);
         statValue *= this.getVariantMultiplier(statName);
         if (this.entity.extraMobBehaviour != null) {
            statValue *= this.entity.extraMobBehaviour.multiplierDamage;
            statValue += (double)this.entity.extraMobBehaviour.boostDamage;
         }
      }

      return statValue * this.getLevelMultiplier(statName);
   }

   public double getAttackSpeed() {
      String statName = "attackSpeed";
      double statValue = this.entity.creatureInfo.attackSpeed;
      if (!this.entity.isTamed()) {
         statValue *= this.getDifficultyMultiplier(statName);
         statValue *= this.getVariantMultiplier(statName);
         if (this.entity.extraMobBehaviour != null) {
            statValue *= this.entity.extraMobBehaviour.multiplierHaste;
            statValue += (double)this.entity.extraMobBehaviour.boostHaste;
         }
      }

      return statValue * this.getLevelMultiplier(statName);
   }

   public double getRangedSpeed() {
      String statName = "rangedSpeed";
      double statValue = this.entity.creatureInfo.rangedSpeed;
      if (!this.entity.isTamed()) {
         statValue *= this.getDifficultyMultiplier(statName);
         statValue *= this.getVariantMultiplier(statName);
         if (this.entity.extraMobBehaviour != null) {
            statValue *= this.entity.extraMobBehaviour.multiplierHaste;
            statValue += (double)this.entity.extraMobBehaviour.boostHaste;
         }
      }

      return statValue * this.getLevelMultiplier(statName);
   }

   public double getEffect() {
      String statName = "effect";
      double statValue = this.entity.creatureInfo.effectDuration;
      if (!this.entity.isTamed()) {
         statValue *= this.getDifficultyMultiplier(statName);
         statValue *= this.getVariantMultiplier(statName);
         if (this.entity.extraMobBehaviour != null) {
            statValue *= this.entity.extraMobBehaviour.multiplierEffect;
            statValue += (double)this.entity.extraMobBehaviour.boostEffect;
         }
      }

      return statValue * this.getLevelMultiplier(statName);
   }

   public double getAmplifier() {
      String statName = "amplifier";
      double statValue = this.entity.creatureInfo.effectAmplifier;
      if (!this.entity.isTamed()) {
         statValue *= this.getDifficultyMultiplier(statName);
         statValue *= this.getVariantMultiplier(statName);
      }

      return statValue * this.getLevelMultiplier(statName);
   }

   public double getPierce() {
      String statName = "pierce";
      double statValue = this.entity.creatureInfo.pierce;
      if (!this.entity.isTamed()) {
         statValue *= this.getDifficultyMultiplier(statName);
         statValue *= this.getVariantMultiplier(statName);
         if (this.entity.extraMobBehaviour != null) {
            statValue *= this.entity.extraMobBehaviour.multiplierPierce;
            statValue += (double)this.entity.extraMobBehaviour.boostPierce;
         }
      }

      return statValue * this.getLevelMultiplier(statName);
   }

   public double getSight() {
      String statName = "sight";
      double statValue = this.entity.creatureInfo.sight;
      if (!this.entity.isTamed()) {
         statValue *= this.getDifficultyMultiplier(statName);
         statValue *= this.getVariantMultiplier(statName);
      }

      return statValue * this.getLevelMultiplier(statName);
   }

   public int BagSize() {
      return this.entity.creatureInfo.bagSize;
   }

   public double getKnockbackResistance() {
      return this.entity.creatureInfo.knockbackResistance;
   }

   protected double getDifficultyMultiplier(String stat) {
      Difficulty difficulty = this.entity.level().getDifficulty();
      String difficultyName = "Easy";
      if (difficulty.getId() >= 3) {
         difficultyName = "Hard";
      } else if (difficulty == Difficulty.NORMAL) {
         difficultyName = "Normal";
      }

      return CreatureManager.getInstance().getDifficultyMultiplier(difficultyName.toUpperCase(Locale.ENGLISH), stat.toUpperCase(Locale.ENGLISH));
   }

   protected double getVariantMultiplier(String stat) {
      return this.entity.getVariant() != null
            && Variant.STAT_MULTIPLIERS.containsKey(this.entity.getVariant().rarity.toUpperCase(Locale.ENGLISH) + "-" + stat.toUpperCase(Locale.ENGLISH))
         ? Variant.STAT_MULTIPLIERS.get(this.entity.getVariant().rarity.toUpperCase(Locale.ENGLISH) + "-" + stat.toUpperCase(Locale.ENGLISH))
         : 1.0;
   }

   protected double getLevelMultiplier(String stat) {
      double statLevel = (double)Math.max(0, this.entity.getLevel() - 1);
      return 1.0 + statLevel * CreatureManager.getInstance().getLevelMultiplier(stat.toUpperCase(Locale.ENGLISH));
   }

   public int getExperienceForNextLevel() {
      return BASE_LEVELUP_EXPERIENCE + Math.round((float)(BASE_LEVELUP_EXPERIENCE * (this.entity.getLevel() - 1)) * 0.25F);
   }
}
