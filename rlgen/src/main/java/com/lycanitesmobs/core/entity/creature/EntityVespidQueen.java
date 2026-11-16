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
import com.lycanitesmobs.core.dungeon.DungeonManager;
import com.lycanitesmobs.core.entity.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.CreatureBuildTask;
import com.lycanitesmobs.core.entity.CreatureStructure;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.StayByHomeGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindAttackTargetGoal;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.item.consumable.CreatureTreatItem;


public class EntityVespidQueen extends TameableCreatureEntity implements Enemy {
   public final CreatureStructure creatureStructure;
   protected int swarmLimit = 10;

   public EntityVespidQueen(Level world) {
      super(world);
      this.attribute = EnumCreatureAttribute.ARTHROPOD;
      this.hasAttackSound = true;
      this.solidCollision = true;
      this.setupMob();
      this.canGrow = true;
      this.babySpawnChance = 0.0;
      this.setStepHeightCompat(1.0F);
      this.setAttackCooldownMax(10);
      this.creatureStructure = new CreatureStructure(this, DungeonManager.getInstance().getTheme("vespid_hive"));
   }

   @Override
   protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(true));
      this.tasks.addTask(this.nextTravelGoalIndex++, new StayByHomeGoal(this));
      this.targetTasks.addTask(this.nextFindTargetIndex++, new FindAttackTargetGoal(this).addTargets(this.getClass()));
      Class<? extends Entity> conbaType = CreatureManager.getInstance().getEntityClass("conba");
      if (conbaType != null) {
         this.targetTasks.addTask(this.nextFindTargetIndex++, new FindAttackTargetGoal(this).addTargets(conbaType));
      }
   }

   @Override
   public void loadCreatureFlags() {
      this.swarmLimit = this.creatureInfo.getFlag("swarmLimit", this.swarmLimit);
   }

   @Override
   public boolean isPersistant() {
      return this.hasHome() && this.level().getDifficulty() != Difficulty.PEACEFUL ? true : super.isPersistant();
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (!this.level().isClientSide) {
         if (this.updateTick > 0L && this.updateTick % 20L == 0L) {
            if (!this.hasHome()) {
               this.creatureStructure.setOrigin(this.blockPosition());
            }

            if (!this.creatureStructure.isPhaseComplete(0) || this.updateTick % 200L == 0L) {
               this.creatureStructure.refreshBuildTasks();
            }

            if (this.hasHome() && this.creatureStructure.getFinalPhaseBuildTaskSize() <= 10 && this.updateTick % 60L == 0L) {
               this.allyUpdate();
            }
         }

         if (!this.level().isClientSide && this.getTarget() instanceof EntityConba && ((EntityConba)this.getTarget()).vespidInfection) {
            this.setTarget(null);
         }
      }
   }

   @Override
   public void onBuildTaskComplete(CreatureBuildTask buildTask) {
      super.onBuildTaskComplete(buildTask);
      if (!this.hasHome()) {
         this.setHome(
            this.creatureStructure.getOrigin().getX(),
            this.creatureStructure.getOrigin().getY(),
            this.creatureStructure.getOrigin().getZ(),
            8.0F
         );
      }
   }

   @Override
   public void setHomePosition(int x, int y, int z) {
      super.setHomePosition(x, y, z);
      this.creatureStructure.setOrigin(LycPos.of(x, y, z));
   }

   @Override
   public boolean rollWanderChance() {
      return this.hasHome() ? false : this.getRandom().nextDouble() <= 8.0E-4;
   }

   public void allyUpdate() {
      if (!this.level().isClientSide) {
         if (this.swarmLimit > 0 && this.nearbyCreatureCount(CreatureManager.getInstance().getCreature("vespid").getEntityClass(), 32.0) < this.swarmLimit) {
            float random = this.random.nextFloat();
            if (random <= 0.05F) {
               LivingEntity minion = this.spawnAlly(
                  this.getX() - 2.0 + (double)(random * 4.0F), this.getY(), this.getZ() - 2.0 + (double)(random * 4.0F)
               );
               if (minion instanceof AgeableCreatureEntity) {
                  ((AgeableCreatureEntity)minion).setAge(((AgeableCreatureEntity)minion).growthTime);
               }
            }
         }
      }
   }

   public LivingEntity spawnAlly(double x, double y, double z) {
      LivingEntity minion = CreatureManager.getInstance().getCreature("vespid").createEntity(this.level());
      minion.moveTo(x, y, z, this.random.nextFloat() * 360.0F, 0.0F);
      if (minion instanceof BaseCreatureEntity) {
         ((BaseCreatureEntity)minion).applyVariant(this.getVariantIndex());
      }

      this.level().addFreshEntity(minion);
      if (this.getTarget() != null) {
         minion.setLastHurtByMob(this.getTarget());
      }

      return minion;
   }

   @Override
   public boolean attackMelee(Entity target, double damageScale) {
      if (!super.attackMelee(target, damageScale)) {
         return false;
      } else if (target instanceof EntityConba) {
         ((EntityConba)target).vespidInfection = true;
         return true;
      } else {
         return true;
      }
   }

   @Override
   public boolean canAttackEntity(LivingEntity targetEntity) {
      if (targetEntity instanceof EntityConba && ((EntityConba)targetEntity).vespidInfection) {
         return false;
      } else {
         return !(targetEntity instanceof EntityVespid) || ((EntityVespid)targetEntity).hasMaster() && ((EntityVespid)targetEntity).getMasterTarget() != this
            ? super.canAttackEntity(targetEntity)
            : false;
      }
   }

   @Override
   public boolean canAttackOwnSpecies() {
      return true;
   }

   @Override
   public boolean canBeTempted() {
      return true;
   }

   @Override
   public boolean isTamingItem(ItemStack itemStack) {
      if (!itemStack.isEmpty() && this.creatureInfo.creatureType != null) {
         if (itemStack.getItem() instanceof CreatureTreatItem) {
            CreatureTreatItem itemTreat = (CreatureTreatItem)itemStack.getItem();
            if (itemTreat.getCreatureType() == this.creatureInfo.creatureType) {
               return true;
            }
         }

         return super.isTamingItem(itemStack);
      } else {
         return false;
      }
   }

   @Override
   public boolean isFlying() {
      return true;
   }

   @Override
   public boolean canBePushed() {
      return false;
   }

   @Override
   public float getDamageModifier(DamageSource damageSrc) {
      return damageSrc.is(DamageTypeTags.IS_FIRE) ? 4.0F : 1.0F;
   }

   @Override
   public boolean isDamageTypeApplicable(String type, DamageSource source, float damage) {
      return type.equals("inWall") ? false : super.isDamageTypeApplicable(type, source, damage);
   }

   public void legacySetDead() {
      if (this.creatureStructure != null) {
         this.creatureStructure.removePitBLocks();
      }

      super.discard();
   }
}
