package com.lycanitesmobs.core.entity.goals.actions.abilities;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
import com.lycanitesmobs.LycEntities;
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
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.BaseGoal;


public class EffectAuraGoal extends BaseGoal {
   protected Potion effect;
   protected float auraRange = 10.0F;
   protected int effectSeconds = 5;
   protected int effectAmplifier = 0;
   protected boolean checkSight = true;
   protected float damageAmount = 0.0F;
   protected int duration = 200;
   protected int cooldownDuration = 0;
   protected int tickRate = 40;
   protected byte targetTypes = BaseCreatureEntity.TARGET_TYPES.ENEMY.id;
   protected String targetCreatureType = null;
   public int abilityTime = 0;
   public int cooldownTime = this.cooldownDuration;

   public EffectAuraGoal(BaseCreatureEntity setHost) {
      super(setHost);
   }

   public EffectAuraGoal setDuration(int duration) {
      this.duration = duration;
      return this;
   }

   public EffectAuraGoal setCooldown(int cooldown) {
      this.cooldownDuration = cooldown;
      this.cooldownTime = cooldown;
      return this;
   }

   public EffectAuraGoal setTickRate(int tickRate) {
      this.tickRate = tickRate;
      return this;
   }

   public EffectAuraGoal setEffect(Potion effect) {
      this.effect = effect;
      return this;
   }

   public EffectAuraGoal setEffect(String effectName) {
      this.effect = ObjectManager.getEffect(effectName);
      return this;
   }

   public EffectAuraGoal setRange(float auraRange) {
      this.auraRange = auraRange;
      return this;
   }

   public EffectAuraGoal setEffectSeconds(int baseEffectSeconds) {
      this.effectSeconds = baseEffectSeconds;
      return this;
   }

   public EffectAuraGoal setAmplifier(int effectAmplifier) {
      this.effectAmplifier = effectAmplifier;
      return this;
   }

   public EffectAuraGoal setCheckSight(boolean checkSight) {
      this.checkSight = checkSight;
      return this;
   }

   public EffectAuraGoal setDamageAmount(float damageAmount) {
      this.damageAmount = damageAmount;
      return this;
   }

   public EffectAuraGoal setTargetTypes(byte targetTypes) {
      this.targetTypes = targetTypes;
      return this;
   }

   public EffectAuraGoal setTargetCreatureType(String creatureTypeName) {
      this.targetCreatureType = creatureTypeName;
      return this;
   }

   public void startExecuting() {
      this.cooldownTime = this.cooldownDuration;
      this.abilityTime = 0;
   }

   public void updateTask() {
      if (this.cooldownTime-- > 0) {
         this.abilityTime = 0;
      } else if (this.abilityTime++ >= this.duration && this.cooldownDuration > 0) {
         this.cooldownTime = this.cooldownDuration;
      } else if (this.abilityTime % this.tickRate == 0) {
         MobEffectInstance effectInstance = null;
         if (this.effect != null) {
            effectInstance = new MobEffectInstance(this.effect, this.host.getEffectDuration(this.effectSeconds), this.effectAmplifier);
         }

         for (LivingEntity target : this.host
            .getNearbyEntities(
               LivingEntity.class,
               entity -> {
                  if (!(entity instanceof LivingEntity)) {
                     return false;
                  } else {
                     boolean validTarget = false;
                     if (entity == this.host) {
                        if ((this.targetTypes & BaseCreatureEntity.TARGET_TYPES.SELF.id) > 0) {
                           validTarget = true;
                        }
                     } else {
                        if ((this.targetTypes & BaseCreatureEntity.TARGET_TYPES.ALLY.id) > 0) {
                           if (this.host.isTamed() && this.host instanceof TameableCreatureEntity) {
                              if (entity instanceof TameableCreatureEntity
                                 && ((TameableCreatureEntity)entity).getPlayerOwner() == ((TameableCreatureEntity)this.host).getPlayerOwner()) {
                                 validTarget = true;
                              }
                           } else if (entity instanceof BaseCreatureEntity && !((BaseCreatureEntity)entity).isTamed()) {
                              validTarget = true;
                           }
                        }

                        if ((this.targetTypes & BaseCreatureEntity.TARGET_TYPES.ENEMY.id) > 0
                           && (this.host.canAttackClass(entity.getClass()) || this.host.canAttackEntity((LivingEntity)entity))) {
                           validTarget = true;
                        }
                     }

                     if (!validTarget) {
                        return false;
                     } else if (this.targetCreatureType != null) {
                        return entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).creatureInfo.creatureType != null
                           ? this.targetCreatureType.equals(((BaseCreatureEntity)entity).creatureInfo.creatureType.getName())
                           : false;
                     } else {
                        return !this.checkSight || this.host.getSensing().hasLineOfSight(entity);
                     }
                  }
               },
               (double)this.auraRange
            )) {
            if (effectInstance != null) {
               if (!target.canBeAffected(effectInstance)) {
                  continue;
               }

               target.addEffect(effectInstance);
            }

            if (this.damageAmount != 0.0F) {
               DamageSource damageSource = new EntityDamageSource("mob", this.host);
               damageSource.setDamageIsAbsolute();
               damageSource.setDamageBypassesArmor();
               target.hurt(damageSource, this.damageAmount);
               if (this.host.minions.contains(target)) {
                  this.host.onTryToDamageMinion(target, this.damageAmount);
               }
            }
         }
      }
   }
}
