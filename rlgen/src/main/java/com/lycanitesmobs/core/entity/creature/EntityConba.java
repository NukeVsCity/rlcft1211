package com.lycanitesmobs.core.entity.creature;

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
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.FireProjectilesGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindAvoidTargetGoal;
import com.lycanitesmobs.core.info.CreatureManager;


public class EntityConba extends TameableCreatureEntity implements Enemy {
   AttackMeleeGoal aiAttackMelee;
   public boolean vespidInfection = false;
   public int vespidInfectionTime = 0;

   public EntityConba(Level world) {
      super(world);
      this.attribute = EnumCreatureAttribute.UNDEFINED;
      this.hasAttackSound = true;
      this.setupMob();
   }

   @Override
   protected void initEntityAI() {
      this.aiAttackMelee = new AttackMeleeGoal(this).setLongMemory(true).setEnabled(false);
      this.tasks.addTask(this.nextPriorityGoalIndex++, this.aiAttackMelee);
      super.initEntityAI();
      this.tasks.addTask(this.nextCombatGoalIndex++, new FireProjectilesGoal(this).setProjectile("poop").setFireRate(20).setVelocity(1.2F));
      this.targetTasks
         .addTask(this.nextSpecialTargetIndex++, new FindAvoidTargetGoal(this).setTargetClass(Player.class).setTameTargetting(false));
      this.targetTasks.addTask(this.nextSpecialTargetIndex++, new FindAvoidTargetGoal(this).setTargetClass(Villager.class));
   }

   @Override
   public String getSpeciesName() {
      String infection = "";
      if (this.vespidInfection) {
         String entityName = this.creatureInfo.getName();
         if (entityName != null) {
            infection = LanguageManager.translate("entity." + this.creatureInfo.modInfo.modid + "." + entityName + ".infected") + " ";
         }
      }

      return super.getSpeciesName();
   }

   @Override
   public String getTextureName() {
      return this.vespidInfection ? super.getTextureName() + "_infected" : super.getTextureName();
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (this.onGround() && !this.level().isClientSide) {
         if (this.hasAvoidTarget()) {
            if (this.random.nextInt(10) == 0) {
               this.leap(1.0F, 0.6, this.getTarget());
            }
         } else if (this.random.nextInt(50) == 0 && this.isMoving()) {
            this.leap(1.0, 0.6);
         }
      }

      if (!this.level().isClientSide) {
         if (!this.vespidInfection && "theswarm".equals(this.spawnEventType)) {
            this.vespidInfection = true;
         }

         if (this.vespidInfection && !this.level().isClientSide) {
            this.aiAttackMelee.setEnabled(true);
            if (this.vespidInfectionTime++ >= 1200) {
               this.spawnVespidSwarm();
               this.discard();
            }
         } else {
            this.aiAttackMelee.setEnabled(false);
         }
      }

      if (this.level().isClientSide) {
         this.vespidInfection = this.extraAnimation01();
         if (this.vespidInfection) {
            for (int i = 0; i < 2; i++) {
               this.level()
                  .addParticle(ParticleTypes.WITCH, this.getX() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), this.getY() + this.random.nextDouble() * (double)this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), 0.0, 0.0, 0.0);
            }
         }
      }
   }

   @Override
   public boolean shouldCreatureGroupFlee(LivingEntity target) {
      return this.isTamed() ? false : super.shouldCreatureGroupFlee(target);
   }

   @Override
   public boolean canAttackEntity(LivingEntity target) {
      return !(target instanceof EntityVespid) && !(target instanceof EntityVespidQueen) ? super.canAttackEntity(target) : false;
   }

   @Override
   public void attackRanged(Entity target, float range) {
      this.fireProjectile("poop", target, range, 0.0F, new Vec3(0.0, 0.0, 0.0), 1.2F, 2.0F, 1.0F);
      super.attackRanged(target, range);
   }

   @Override
   public void die(DamageSource damageSource) {
      if (!this.level().isClientSide && this.vespidInfection) {
         this.spawnVespidSwarm();
      }

      super.die(damageSource);
   }

   public void spawnVespidSwarm() {
      int j = 2 + this.random.nextInt(5) + this.level().getDifficulty().getId() - 1;

      for (int k = 0; k < j; k++) {
         float f = ((float)(k % 2) - 0.5F) * this.getBbWidth() / 4.0F;
         float f1 = ((float)(k / 2) - 0.5F) * this.getBbWidth() / 4.0F;
         EntityVespid vespid = (EntityVespid)CreatureManager.getInstance().getCreature("vespid").createEntity(this.level());
         vespid.moveTo(
            this.getX() + (double)f, this.getY() + 0.5, this.getZ() + (double)f1, this.random.nextFloat() * 360.0F, 0.0F
         );
         vespid.applyVariant(this.getVariantIndex());
         vespid.setAge(vespid.growthTime);
         vespid.spawnEventType = this.spawnEventType;
         this.level().addFreshEntity(vespid);
         if (this.getTarget() != null) {
            vespid.setLastHurtByMob(this.getTarget());
         }
      }
   }

   @Override
   public boolean petControlsEnabled() {
      return true;
   }

   @Override
   public boolean extraAnimation01() {
      return !this.level().isClientSide ? this.vespidInfection : this.extraAnimation01;
   }

   @Override
   public int getNoBagSize() {
      return 0;
   }

   @Override
   public int getBagSize() {
      return this.creatureInfo.bagSize;
   }

   @Override
   public float getFallResistance() {
      return 100.0F;
   }

   public void readFromNBT(CompoundTag nbtTagCompound) {
      super.readFromNBT(nbtTagCompound);
      if (nbtTagCompound.contains("VespidInfection")) {
         this.vespidInfection = nbtTagCompound.getBoolean("VespidInfection");
      }

      if (nbtTagCompound.contains("VespidInfectionTime")) {
         this.vespidInfectionTime = nbtTagCompound.getInt("VespidInfectionTime");
      }
   }

   @Override
   public void addAdditionalSaveData(CompoundTag nbtTagCompound) {
      super.addAdditionalSaveData(nbtTagCompound);
      nbtTagCompound.putBoolean("VespidInfection", this.vespidInfection);
      if (this.vespidInfection) {
         nbtTagCompound.putInt("VespidInfectionTime", this.vespidInfectionTime);
      }
   }
}
