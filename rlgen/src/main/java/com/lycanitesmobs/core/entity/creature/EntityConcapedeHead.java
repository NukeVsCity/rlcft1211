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
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.TemptGoal;
import com.lycanitesmobs.core.entity.goals.targeting.DefendEntitiesGoal;
import com.lycanitesmobs.core.info.CreatureManager;


public class EntityConcapedeHead extends AgeableCreatureEntity {
   public static int CONCAPEDE_SIZE_MAX = 10;
   public BaseCreatureEntity backSegment;
   public boolean isHungry = true;

   public EntityConcapedeHead(Level world) {
      super(LycEntities.type(EntityConcapedeHead.class), world);
      this.attribute = EnumCreatureAttribute.ARTHROPOD;
      this.hasAttackSound = true;
      this.canGrow = true;
      this.babySpawnChance = 0.0;
      this.setupMob();
   }

   @Override
   protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(this.nextDistractionGoalIndex++, new TemptGoal(this).setIncludeDiet(true));
      this.tasks.addTask(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(false));
      this.targetTasks.addTask(this.nextSpecialTargetIndex++, new DefendEntitiesGoal(this, EntityConcapedeSegment.class));
   }

   @Override
   public void loadCreatureFlags() {
      CONCAPEDE_SIZE_MAX = this.creatureInfo.getFlag("sizeMax", CONCAPEDE_SIZE_MAX);
   }

   @Override
   public void onFirstSpawn() {
      if (!this.level().isClientSide && this.backSegment == null) {
         this.setGrowingAge(-this.growthTime / 4);
         int segmentCount = this.getRandom().nextInt(CONCAPEDE_SIZE_MAX);
         AgeableCreatureEntity parentSegment = this;

         for (int segment = 0; segment < segmentCount; segment++) {
            EntityConcapedeSegment segmentEntity = (EntityConcapedeSegment)CreatureManager.getInstance()
               .getCreature("concapedesegment")
               .createEntity(parentSegment.level());
            segmentEntity.moveTo(parentSegment.getX(), parentSegment.getY(), parentSegment.getZ(), 0.0F, 0.0F);
            segmentEntity.setParentTarget(parentSegment);
            segmentEntity.applyVariant(this.getVariantIndex());
            segmentEntity.setSizeScale(this.sizeScale);
            segmentEntity.spawnEventType = this.spawnEventType;
            segmentEntity.firstSpawn = false;
            parentSegment.level().addFreshEntity(segmentEntity);
            parentSegment = segmentEntity;
         }
      }

      super.onFirstSpawn();
   }

   @Override
   public boolean canBePushed() {
      return false;
   }

   @Override
   public boolean shouldFollowParent() {
      return false;
   }

   @Override
   public boolean rollLookChance() {
      return false;
   }

   @Override
   public boolean rollWanderChance() {
      return false;
   }

   @Override
   public void setGrowingAge(int age) {
      if (!this.level().isClientSide
         && !this.firstSpawn
         && age == 0
         && !this.isHungry
         && CreatureManager.getInstance().getCreature("concapedesegment") != null) {
         age = -(this.growthTime / 4);
         this.isHungry = true;
         int size = 0;
         BaseCreatureEntity lastSegment = this;

         while (size <= CONCAPEDE_SIZE_MAX) {
            size++;
            BaseCreatureEntity trailingSegment = null;
            if (lastSegment instanceof EntityConcapedeHead) {
               trailingSegment = ((EntityConcapedeHead)lastSegment).backSegment;
            } else if (lastSegment instanceof EntityConcapedeSegment) {
               trailingSegment = ((EntityConcapedeSegment)lastSegment).backSegment;
            }

            if (trailingSegment == null || trailingSegment == lastSegment) {
               break;
            }

            lastSegment = trailingSegment;
         }

         if (size < CONCAPEDE_SIZE_MAX) {
            EntityConcapedeSegment segmentEntity = (EntityConcapedeSegment)CreatureManager.getInstance()
               .getCreature("concapedesegment")
               .createEntity(lastSegment.level());
            segmentEntity.moveTo(lastSegment.getX(), lastSegment.getY(), lastSegment.getZ(), 0.0F, 0.0F);
            segmentEntity.setParentTarget(lastSegment);
            segmentEntity.applyVariant(this.getVariantIndex());
            segmentEntity.setSizeScale(this.sizeScale);
            segmentEntity.spawnEventType = this.spawnEventType;
            segmentEntity.firstSpawn = false;
            lastSegment.level().addFreshEntity(segmentEntity);
         }
      }

      super.setGrowingAge(age);
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
      return true;
   }

   @Override
   public boolean canAttackEntity(LivingEntity target) {
      return target instanceof EntityConcapedeSegment ? false : super.canAttackEntity(target);
   }

   @Override
   public boolean isAggressive() {
      if (this.isInLove()) {
         return false;
      } else {
         this.level();
         return this.isDay() ? this.testLightLevel() < 2 : super.isAggressive();
      }
   }

   @Override
   public boolean isProtective(Entity entity) {
      if (this.isInLove()) {
         return false;
      } else {
         if (entity instanceof EntityConcapedeSegment) {
            for (BaseCreatureEntity checkCreature = (BaseCreatureEntity)entity;
               checkCreature.hasParent();
               checkCreature = (BaseCreatureEntity)checkCreature.getParentTarget()
            ) {
               if (checkCreature.getParentTarget() == this) {
                  return true;
               }

               if (!(checkCreature.getParentTarget() instanceof BaseCreatureEntity)) {
                  break;
               }
            }
         }

         return false;
      }
   }

   @Override
   public boolean canClimb() {
      return true;
   }

   @Override
   public AgeableCreatureEntity createChild(AgeableCreatureEntity partner) {
      return null;
   }

   @Override
   public boolean canBreed() {
      return this.getGrowingAge() >= 0;
   }

   @Override
   public boolean breed() {
      if (super.breed()) {
         this.isHungry = false;
         if (this.getAge() == 0) {
            this.setGrowingAge(0);
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean canMate() {
      return false;
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

   @Override
   public void readAdditionalSaveData(CompoundTag nbtTagCompound) {
      if (nbtTagCompound.contains("IsHungry")) {
         this.isHungry = nbtTagCompound.getBoolean("IsHungry");
      }

      super.readAdditionalSaveData(nbtTagCompound);
   }

   @Override
   public void addAdditionalSaveData(CompoundTag nbtTagCompound) {
      super.addAdditionalSaveData(nbtTagCompound);
      nbtTagCompound.putBoolean("IsHungry", this.isHungry);
   }
}
