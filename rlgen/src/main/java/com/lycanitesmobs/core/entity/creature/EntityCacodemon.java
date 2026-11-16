package com.lycanitesmobs.core.entity.creature;

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
import net.minecraft.world.entity.boss.enderdragon.*;
import net.minecraft.world.entity.boss.wither.*;
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
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindAttackTargetGoal;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;


public class EntityCacodemon extends RideableCreatureEntity {
   public boolean griefing = true;

   public EntityCacodemon(Level world) {
      super(LycEntities.type(EntityCacodemon.class), world);
      this.attribute = EnumCreatureAttribute.UNDEAD;
      this.hasAttackSound = false;
      this.setupMob();
      this.setStepHeightCompat(1.0F);
      this.hitAreaWidthScale = 1.5F;
   }

   @Override
   protected void initEntityAI() {
      super.initEntityAI();
      this.tasks
         .addTask(this.nextCombatGoalIndex++, new AttackRangedGoal(this).setSpeed(0.25).setRange(40.0F).setMinChaseDistance(10.0F).setLongMemory(false));
      this.targetTasks.addTask(this.nextFindTargetIndex++, new FindAttackTargetGoal(this).addTargets(Ghast.class));
   }

   @Override
   public void loadCreatureFlags() {
      this.griefing = this.creatureInfo.getFlag("griefing", this.griefing);
   }

   @Override
   public void aiStep() {
      if (!this.level().isClientSide && this.isRareVariant() && this.hasAttackTarget() && this.tickCount % 20 == 0) {
         this.allyUpdate();
      }

      super.aiStep();
   }

   @Override
   public void riderEffects(LivingEntity rider) {
      if (rider.hasEffect(MobEffects.WITHER)) {
         rider.removeEffect(MobEffects.WITHER);
      }

      if (rider.isOnFire()) {
         rider.clearFire();
      }
   }

   public void allyUpdate() {
      if (!this.level().isClientSide) {
         if (CreatureManager.getInstance().getCreature("wraith").enabled
            && this.nearbyCreatureCount(CreatureManager.getInstance().getCreature("wraith").getEntityClass(), 64.0) < 10) {
            float random = this.random.nextFloat();
            if (random <= 0.1F) {
               this.spawnAlly(this.getX() - 2.0 + (double)(random * 4.0F), this.getY(), this.getZ() - 2.0 + (double)(random * 4.0F));
            }
         }
      }
   }

   public void spawnAlly(double x, double y, double z) {
      EntityWraith minion = (EntityWraith)CreatureManager.getInstance().getCreature("wraith").createEntity(this.level());
      minion.moveTo(x, y, z, this.getYRot(), this.getXRot());
      minion.setMinion(true);
      minion.setMasterTarget(this);
      this.level().addFreshEntity(minion);
      if (this.getTarget() != null) {
         minion.setLastHurtByMob(this.getTarget());
      }

      minion.setSizeScale(this.sizeScale);
   }

   @Override
   public boolean isFlying() {
      return true;
   }

   @Override
   public boolean petControlsEnabled() {
      return true;
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
   public boolean canAttackEntity(LivingEntity target) {
      return !(target instanceof EntityTrite)
            && !(target instanceof EntityAstaroth)
            && !(target instanceof EntityAsmodeus)
            && !(target instanceof EntityWraith)
         ? super.canAttackEntity(target)
         : false;
   }

   @Override
   public void attackRanged(Entity target, float range) {
      this.fireProjectile("demonicblast", target, range, 0.0F, new Vec3(0.0, 0.0, 0.0), 0.6F, 2.0F, 1.0F);
      super.attackRanged(target, range);
   }

   @Override
   public boolean isDamageEntityApplicable(Entity entity) {
      return entity instanceof EntityCacodemon ? false : super.isDamageEntityApplicable(entity);
   }

   @Override
   public boolean isDamageTypeApplicable(String type, DamageSource source, float damage) {
      return "explosion".equals(type) ? false : super.isDamageTypeApplicable(type, source, damage);
   }

   @Override
   public boolean canBurn() {
      return false;
   }

   @Override
   public double getMountedYOffset() {
      return (double)this.getBbHeight() * 0.9;
   }

   @Override
   public void mountAbility(Entity rider) {
      if (!this.level().isClientSide) {
         if (!this.abilityToggled) {
            if (this.hasPickupEntity()) {
               this.dropPickupEntity();
            } else if (!(this.getStamina() < this.getStaminaCost())) {
               if (rider instanceof Player) {
                  Player player = (Player)rider;
                  ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile("demonicblast");
                  if (projectileInfo != null) {
                     BaseProjectileEntity projectile = projectileInfo.createProjectile(this.level(), player);
                     this.level().addFreshEntity(projectile);
                     this.playSound(projectile.getLaunchSound(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
                     this.triggerAttackCooldown();
                  }
               }

               this.applyStaminaCost();
            }
         }
      }
   }

   @Override
   public float getStaminaCost() {
      return 10.0F;
   }

   @Override
   public int getStaminaRecoveryWarmup() {
      return 40;
   }

   @Override
   public float getStaminaRecoveryMax() {
      return 1.0F;
   }

   public float getBrightness() {
      return this.isAttackOnCooldown() ? 1.0F : super.getLightLevelDependentMagicValue();
   }

   public int getBrightnessForRender() {
      return this.isAttackOnCooldown() ? 15728880 : super.getLightLevelDependentMagicValue();
   }
}
