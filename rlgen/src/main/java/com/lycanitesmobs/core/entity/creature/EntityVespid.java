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
import com.lycanitesmobs.core.entity.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.CreatureBuildTask;
import com.lycanitesmobs.core.entity.CreatureRelationshipEntry;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.PlaceBlockGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindMasterGoal;


public class EntityVespid extends AgeableCreatureEntity implements Enemy {
   public PlaceBlockGoal aiPlaceBlock;
   public CreatureBuildTask creatureBuildTask;
   private boolean hiveBuilding = true;

   public EntityVespid(Level world) {
      super(LycEntities.type(EntityVespid.class), world);
      this.attribute = EnumCreatureAttribute.ARTHROPOD;
      this.hasAttackSound = true;
      this.setupMob();
      this.canGrow = true;
      this.babySpawnChance = 0.1;
      this.setStepHeightCompat(1.0F);
      this.setAttackCooldownMax(10);
   }

   @Override
   protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(true));
      this.aiPlaceBlock = new PlaceBlockGoal(this).setMaxDistance(128.0).setSpeed(3.0).setReplaceLiquid(true).setReplaceSolid(true);
      this.tasks.addTask(this.nextIdleGoalIndex++, this.aiPlaceBlock);
      this.targetTasks.addTask(this.nextFindTargetIndex++, new FindMasterGoal(this).setTargetClass(EntityVespidQueen.class).setRange(64.0));
   }

   @Override
   public void loadCreatureFlags() {
      this.hiveBuilding = this.creatureInfo.getFlag("hiveBuilding", this.hiveBuilding);
   }

   @Override
   public boolean isPersistant() {
      return this.getMasterTarget() instanceof BaseCreatureEntity ? ((BaseCreatureEntity)this.getMasterTarget()).isPersistant() : super.isPersistant();
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (!this.level().isClientSide && this.hiveBuilding) {
         if (this.getMasterTarget() instanceof EntityVespidQueen) {
            if (this.getMasterTarget().isRemoved() && this.aiPlaceBlock.blockState != null) {
               this.aiPlaceBlock.resetTask();
            } else if (this.aiPlaceBlock.blockState == null) {
               EntityVespidQueen queen = (EntityVespidQueen)this.getMasterTarget();
               this.creatureBuildTask = queen.creatureStructure.takeBuildTask(this);
               if (this.creatureBuildTask != null) {
                  this.aiPlaceBlock.setBlockPlacement(this.creatureBuildTask.blockState, this.creatureBuildTask.pos);
               }
            }
         } else if (this.aiPlaceBlock.blockState != null) {
            this.aiPlaceBlock.resetTask();
         }
      }

      if (!this.level().isClientSide && this.getTarget() instanceof EntityConba && ((EntityConba)this.getTarget()).vespidInfection) {
         this.setTarget(null);
      }
   }

   @Override
   public void onBlockPlaced(BlockPos blockPos, BlockState blockState) {
      if (this.getMasterTarget() instanceof EntityVespidQueen) {
         EntityVespidQueen queen = (EntityVespidQueen)this.getMasterTarget();
         if (this.creatureBuildTask != null) {
            queen.creatureStructure.completeBuildTask(this.creatureBuildTask);
            this.creatureBuildTask = null;
         }
      }
   }

   @Override
   public boolean canAttackEntity(LivingEntity targetEntity) {
      if (targetEntity == this.getMasterTarget()) {
         return false;
      } else if (targetEntity instanceof EntityConba) {
         return false;
      } else if (!(targetEntity instanceof EntityVespid)
         || ((EntityVespid)targetEntity).hasMaster() && ((EntityVespid)targetEntity).getMasterTarget() != this.getMasterTarget()) {
         if (!(targetEntity instanceof EntityVespidQueen) || this.hasMaster() && this.getMasterTarget() != targetEntity) {
            if (this.hasMaster() && this.getMasterTarget() instanceof EntityVespidQueen) {
               EntityVespidQueen entityVespidQueen = (EntityVespidQueen)this.getMasterTarget();
               CreatureRelationshipEntry creatureRelationshipEntry = entityVespidQueen.relationships.getEntry(targetEntity);
               if (creatureRelationshipEntry != null && !creatureRelationshipEntry.canAttack()) {
                  return false;
               }
            }

            return super.canAttackEntity(targetEntity);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean isFlying() {
      return true;
   }

   @Override
   public float getDamageModifier(DamageSource damageSrc) {
      return damageSrc.is(DamageTypeTags.IS_FIRE) ? 4.0F : 1.0F;
   }

   @Override
   public boolean isDamageTypeApplicable(String type, DamageSource source, float damage) {
      return type.equals("inWall") ? false : super.isDamageTypeApplicable(type, source, damage);
   }

   @Override
   public boolean canEntityBeSeen(Entity entity) {
      return entity instanceof EntityVespidQueen ? true : super.hasLineOfSight(entity);
   }
}
