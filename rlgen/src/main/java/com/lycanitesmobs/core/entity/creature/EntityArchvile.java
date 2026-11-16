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
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.GoalConditions;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.entity.goals.actions.ChaseGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.EffectAuraGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.FaceTargetGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.SummonMinionsGoal;
import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;


public class EntityArchvile extends TameableCreatureEntity implements Enemy {
   public EntityArchvile(Level world) {
      super(world);
      this.attribute = EnumCreatureAttribute.UNDEAD;
      this.hasAttackSound = false;
      this.setupMob();
   }

   @Override
   protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(this.nextIdleGoalIndex, new FaceTargetGoal(this));
      this.tasks.addTask(this.nextCombatGoalIndex, new ChaseGoal(this).setMinDistance(16.0F).setMaxDistance(64.0F).setSpeed(1.0));
      this.tasks
         .addTask(this.nextCombatGoalIndex++, new AttackRangedGoal(this).setSpeed(1.0).setRange(32.0F).setMinChaseDistance(16.0F).setChaseTime(-1));
      this.tasks
         .addTask(
            this.nextCombatGoalIndex,
            new EffectAuraGoal(this)
               .setEffect(MobEffects.DAMAGE_BOOST)
               .setAmplifier(2)
               .setEffectSeconds(2)
               .setRange(32.0F)
               .setCheckSight(false)
               .setTargetTypes(BaseCreatureEntity.TARGET_TYPES.ALLY.id)
               .setTargetCreatureType("demon")
         );
      this.tasks
         .addTask(
            this.nextCombatGoalIndex,
            new EffectAuraGoal(this)
               .setEffect(MobEffects.MOVEMENT_SPEED)
               .setAmplifier(2)
               .setEffectSeconds(2)
               .setRange(32.0F)
               .setCheckSight(false)
               .setTargetTypes(BaseCreatureEntity.TARGET_TYPES.ALLY.id)
               .setTargetCreatureType("demon")
         );
      this.tasks
         .addTask(
            this.nextCombatGoalIndex,
            new EffectAuraGoal(this)
               .setEffect(MobEffects.DAMAGE_RESISTANCE)
               .setAmplifier(2)
               .setEffectSeconds(2)
               .setRange(32.0F)
               .setCheckSight(false)
               .setTargetTypes(BaseCreatureEntity.TARGET_TYPES.ALLY.id)
               .setTargetCreatureType("demon")
         );
      this.tasks
         .addTask(
            this.nextCombatGoalIndex,
            new SummonMinionsGoal(this).setMinionInfo("belph").setSummonCap(2).setConditions(new GoalConditions().setRareVariantOnly(true))
         );
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (this.level().isClientSide) {
         for (int i = 0; i < 2; i++) {
            this.level()
               .addParticle(ParticleTypes.SMOKE, this.getX() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), this.getY() + this.random.nextDouble() * (double)this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), 0.0, 0.0, 0.0);
            this.level()
               .addParticle(ParticleTypes.FLAME, this.getX() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), this.getY() + this.random.nextDouble() * (double)this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), 0.0, 0.0, 0.0);
         }
      }
   }

   @Override
   public void attackRanged(Entity target, float range) {
      ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile("doomfireball");
      BaseProjectileEntity projectile = projectileInfo.createProjectile(this.level(), this);
      if (projectile != null) {
         projectile.setProjectileScale(2.0F);
         projectile.shoot(0.0, -1.0, 0.0, 0.5F, 4.0F);
         projectile.set(
            (double)target.blockPosition().getX(),
            (double)(target.blockPosition().getY() + 4) + this.random.nextDouble() * 3.0,
            (double)target.blockPosition().getZ()
         );
         this.level().addFreshEntity(projectile);
      }

      super.attackRanged(target, range);
   }

   @Override
   public boolean canBurn() {
      return false;
   }

   @Override
   public boolean petControlsEnabled() {
      return true;
   }
}
