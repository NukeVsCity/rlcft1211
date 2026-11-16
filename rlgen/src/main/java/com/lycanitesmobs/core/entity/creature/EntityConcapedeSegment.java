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
import com.lycanitesmobs.core.entity.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.FollowParentGoal;
import com.lycanitesmobs.core.info.CreatureManager;
import java.util.UUID;


public class EntityConcapedeSegment extends AgeableCreatureEntity {
   UUID parentUUID = null;
   public BaseCreatureEntity backSegment;

   public EntityConcapedeSegment(Level world) {
      super(world);
      this.attribute = EnumCreatureAttribute.ARTHROPOD;
      this.hasAttackSound = true;
      this.hasStepSound = false;
      this.canGrow = true;
      this.babySpawnChance = 0.0;
      this.isAggressiveByDefault = false;
      this.setupMob();
   }

   @Override
   protected void initEntityAI() {
      this.tasks.addTask(this.nextTravelGoalIndex++, new FollowParentGoal(this).setSpeed(4.0).setStrayDistance(4.0));
      super.initEntityAI();
   }

   @Override
   public boolean environmentSpawnCheck(Level world, BlockPos pos) {
      return this.getNearbyEntities(EntityConcapedeHead.class, null, CreatureManager.getInstance().spawnConfig.spawnLimitRange).size() <= 0
         ? false
         : super.environmentSpawnCheck(world, pos);
   }

   @Override
   public void getRandomVariant() {
      if (this.subspecies == null && !this.hasParent()) {
         this.subspecies = this.creatureInfo.getRandomSubspecies(this);
      }

      if (this.hasParent() && this.getParentTarget() instanceof BaseCreatureEntity) {
         this.applyVariant(((BaseCreatureEntity)this.getParentTarget()).getSubspeciesIndex());
      }
   }

   @Override
   protected boolean canDespawn() {
      return !super.canDespawn() ? false : !this.hasParent();
   }

   @Override
   public void tick() {
      if (!this.level().isClientSide && !this.hasParent() && this.parentUUID != null && this.updateTick > 0L && this.updateTick % 40L == 0L) {
         double range = 64.0;

         for (AgeableCreatureEntity possibleConnection : this.level()
            .getEntitiesOfClass(AgeableCreatureEntity.class, this.getBoundingBox().inflate(range, range, range))) {
            if (possibleConnection != this && possibleConnection.getUUID().equals(this.parentUUID)) {
               this.setParentTarget(possibleConnection);
               break;
            }
         }

         this.parentUUID = null;
      }

      super.tick();
      if (!this.level().isClientSide) {
         if (this.backSegment != null && !this.backSegment.isAlive()) {
            this.backSegment = null;
         }

         if (this.hasParent() && !this.getParentTarget().isAlive()) {
            this.setParentTarget(null);
         }

         if (this.hasParent()) {
            this.getLookControl().setLookAt(this.getParentTarget(), 360.0F, 360.0F);
            this.faceEntity(this.getParentTarget(), 360.0F, 360.0F);
            Vec3 parentPos = this.getFacingPositionDouble(
               this.getParentTarget().getX(),
               this.getParentTarget().getY(),
               this.getParentTarget().getZ(),
               -0.65,
               (double)this.getParentTarget().getYRot()
            );
            double segmentPullThreshold = 0.15;
            double segmentDistance = Math.sqrt(this.distanceToSqr(parentPos.x, parentPos.y, parentPos.z));
            if (segmentDistance > segmentPullThreshold) {
               double dragAmount = segmentPullThreshold / 2.0;
               Vec3 dragPos = this.getFacingPositionDouble(
                  parentPos.x, parentPos.y, parentPos.z, dragAmount, this.position().dot(parentPos)
               );
               double distY = parentPos.y - this.getY();
               double dragY = this.getY() + distY / 2.0;
               this.set(dragPos.x, dragY, dragPos.z);
            }
         }

         if (!this.level().isClientSide && this.getAge() <= 0) {
            this.setAge(-this.growthTime);
         }
      }
   }

   @Override
   public boolean rollLookChance() {
      return this.hasParent() ? false : super.rollLookChance();
   }

   @Override
   public boolean rollWanderChance() {
      return this.hasParent() ? false : super.rollWanderChance();
   }

   @Override
   public boolean isFlying() {
      return false;
   }

   @Override
   public boolean canEntityBeSeen(Entity target) {
      return this.hasParent() && target == this.getParentTarget();
   }

   @Override
   public boolean canBePushed() {
      return !this.hasParent();
   }

   @Override
   public void setGrowingAge(int age) {
      if (this.hasParent()) {
         age = -this.growthTime;
      }

      super.setAge(age);
      if (age == 0 && !this.level().isClientSide) {
         EntityConcapedeHead concapedeHead = (EntityConcapedeHead)CreatureManager.getInstance().getCreature("concapede").createEntity(this.level());
         concapedeHead.copyPosition(this);
         concapedeHead.firstSpawn = false;
         concapedeHead.setAge(-this.growthTime / 4);
         concapedeHead.setSizeScale(this.sizeScale);
         concapedeHead.applyVariant(this.getVariantIndex());
         this.level().addFreshEntity(concapedeHead);
         if (this.backSegment != null) {
            this.backSegment.setParentTarget(concapedeHead);
         }

         this.discard();
      }
   }

   @Override
   public boolean shouldFollowParent() {
      return true;
   }

   @Override
   public float getBlockPathWeight(int x, int y, int z) {
      BlockState blockState = this.level().getBlockState(LycPos.of(x, y - 1, z));
      Block block = blockState.getBlock();
      if (block != Blocks.AIR) {
         if (Material.of(blockState) == Material.GRASS) {
            return 10.0F;
         }

         if (Material.of(blockState) == Material.GROUND) {
            return 7.0F;
         }
      }

      return super.getBlockPathWeight(x, y, z);
   }

   @Override
   public boolean canBeLeashedTo(Player player) {
      return !this.hasParent();
   }

   @Override
   public double getFallingMod() {
      if (this.level().isClientSide) {
         return 0.0;
      } else {
         return this.hasParent() && this.getParentTarget().getY() > this.getY() ? 0.0 : super.getFallingMod();
      }
   }

   @Override
   public void setParentTarget(LivingEntity setTarget) {
      if (setTarget != this) {
         if (setTarget instanceof EntityConcapedeSegment) {
            ((EntityConcapedeSegment)setTarget).backSegment = this;
         }

         if (setTarget instanceof EntityConcapedeHead) {
            ((EntityConcapedeHead)setTarget).backSegment = this;
         }
      }

      super.setParentTarget(setTarget);
   }

   @Override
   public boolean renderVariantNameTag() {
      return !this.hasParent();
   }

   @Override
   public boolean canClimb() {
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
   public boolean isDamageTypeApplicable(String type, DamageSource source, float damage) {
      return type.equals("inWall") ? false : super.isDamageTypeApplicable(type, source, damage);
   }

   @Override
   public float getFallResistance() {
      return 100.0F;
   }

   @Override
   public AgeableCreatureEntity createChild(AgeableCreatureEntity partner) {
      return null;
   }

   @Override
   public boolean breed() {
      if (!this.canBreed()) {
         return false;
      } else {
         this.setAge(0);
         return true;
      }
   }

   @Override
   public boolean canBreed() {
      return !this.hasParent();
   }

   @Override
   public boolean shouldFindParent() {
      return false;
   }

   @Override
   public void readAdditionalSaveData(CompoundTag nbtTagCompound) {
      if (nbtTagCompound.hasUUID("ParentUUID")) {
         this.parentUUID = nbtTagCompound.getUUID("ParentUUID");
      }

      super.readAdditionalSaveData(nbtTagCompound);
   }

   @Override
   public void addAdditionalSaveData(CompoundTag nbtTagCompound) {
      super.addAdditionalSaveData(nbtTagCompound);
      if (this.hasParent()) {
         nbtTagCompound.putUUID("ParentUUID", this.getParentTarget().getUUID());
      }
   }
}
