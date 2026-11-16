package com.lycanitesmobs.core.entity.goals.actions;

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
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.info.ObjectLists;
import com.lycanitesmobs.core.item.consumable.CreatureTreatItem;


public class TemptGoal extends EntityAIBase {
   private BaseCreatureEntity host;
   private Player player;
   private double speed = 1.0;
   private ItemStack temptItemStack = null;
   private String temptList = null;
   private int retemptTime;
   private int retemptTimeMax = 0;
   private double temptDistanceMin = 1.0;
   private double temptDistanceMax = 16.0;
   private boolean scaredByPlayerMovement = false;
   private boolean stopAttack = false;
   private boolean includeTreats = true;
   private boolean includeDiet = false;
   private boolean alwaysTempted = false;
   private double targetX;
   private double targetY;
   private double targetZ;
   private double targetPitch;
   private double targetYaw;
   private boolean canSwim;
   private boolean isRunning;

   public TemptGoal(BaseCreatureEntity setHost) {
      this.host = setHost;
      this.setMutexBits(3);
   }

   public TemptGoal setSpeed(double setSpeed) {
      this.speed = setSpeed;
      return this;
   }

   public TemptGoal setItemStack(ItemStack item) {
      this.temptItemStack = item;
      return this;
   }

   public TemptGoal setItemList(String list) {
      this.temptList = list;
      return this;
   }

   public TemptGoal setRetemptTime(int time) {
      this.retemptTimeMax = time;
      return this;
   }

   public TemptGoal setTemptDistanceMin(double dist) {
      this.temptDistanceMin = dist;
      return this;
   }

   public TemptGoal setTemptDistanceMax(double dist) {
      this.temptDistanceMax = dist;
      return this;
   }

   public TemptGoal setScaredByMovement(boolean scared) {
      this.scaredByPlayerMovement = scared;
      return this;
   }

   public TemptGoal setStopAttack(boolean setStopAttack) {
      this.stopAttack = setStopAttack;
      return this;
   }

   public TemptGoal setIncludeTreats(boolean includeTreats) {
      this.includeTreats = includeTreats;
      return this;
   }

   public TemptGoal setIncludeDiet(boolean includeDiet) {
      this.includeDiet = includeDiet;
      return this;
   }

   public TemptGoal setAlwaysTempted(boolean alwaysTempted) {
      this.alwaysTempted = alwaysTempted;
      return this;
   }

   public boolean shouldExecute() {
      if (this.retemptTime > 0) {
         this.retemptTime--;
         return false;
      } else if (!this.host.canBeTempted()) {
         return false;
      } else if (this.host instanceof TameableCreatureEntity && this.host.isTamed()) {
         return false;
      } else {
         if (this.player == null) {
            this.player = this.host.level().getClosestPlayerToEntity(this.host, this.temptDistanceMax);
            if (this.player == null) {
               return false;
            }
         } else if (this.host.distanceToSqr(this.player) > this.temptDistanceMax * this.temptDistanceMax) {
            this.player = null;
            return false;
         }

         if (!this.alwaysTempted && !this.isTemptStack(this.player.getMainHandItem()) && !this.isTemptStack(this.player.getOffhandItem())) {
            this.player = null;
            return false;
         } else {
            this.host.setStealth(0.0F);
            return true;
         }
      }
   }

   public boolean isTemptStack(ItemStack itemStack) {
      if (itemStack.isEmpty()) {
         return false;
      } else {
         if (this.includeTreats
            && this.host.creatureInfo.isTameable()
            && this.host.creatureInfo.creatureType != null
            && itemStack.getItem() instanceof CreatureTreatItem) {
            CreatureTreatItem itemTreat = (CreatureTreatItem)itemStack.getItem();
            if (this.host.creatureInfo.creatureType == itemTreat.getCreatureType()) {
               return true;
            }
         }

         if (this.includeDiet && this.host.creatureInfo.canEat(itemStack)) {
            return true;
         } else {
            if (this.temptList != null) {
               if (ObjectLists.inItemList(this.temptList, itemStack)) {
                  return true;
               }
            } else if (this.temptItemStack != null) {
               if (itemStack.getItem() != this.temptItemStack.getItem()) {
                  return false;
               }

               if (itemStack.getMetadata() != this.temptItemStack.getMetadata()) {
                  return false;
               }

               return true;
            }

            return false;
         }
      }
   }

   public boolean shouldContinueExecuting() {
      if (this.scaredByPlayerMovement) {
         if ((double)this.host.distanceTo(this.player) < 36.0) {
            if (this.player.distanceToSqr(this.targetX, this.targetY, this.targetZ) > 0.010000000000000002) {
               return false;
            }

            if (Math.abs((double)this.player.getXRot() - this.targetPitch) > 5.0 || Math.abs((double)this.player.getYRot() - this.targetYaw) > 5.0) {
               return false;
            }
         } else {
            this.targetX = this.player.getX();
            this.targetY = this.player.getY();
            this.targetZ = this.player.getZ();
         }

         this.targetPitch = (double)this.player.getXRot();
         this.targetYaw = (double)this.player.getYRot();
      }

      return this.shouldExecute();
   }

   public void startExecuting() {
      this.targetX = this.player.getX();
      this.targetY = this.player.getY();
      this.targetZ = this.player.getZ();
      this.isRunning = true;
      if (this.host.getNavigation() instanceof GroundPathNavigation) {
         GroundPathNavigation navigateGround = (GroundPathNavigation)this.host.getNavigation();
         this.canSwim = !navigateGround.getCanSwim();
         navigateGround.setCanFloat(true);
      }

      if (this.stopAttack) {
         this.host.setTarget(null);
      }
   }

   public void resetTask() {
      this.player = null;
      this.host.getNavigation().stop();
      this.retemptTime = this.retemptTimeMax;
      if (this.host instanceof AgeableCreatureEntity) {
         AgeableCreatureEntity ageable = (AgeableCreatureEntity)this.host;
         if (!ageable.isBaby() && !ageable.canBreed()) {
            Math.max(this.retemptTime *= 10, 100);
         }
      }

      this.isRunning = false;
      if (this.host.getNavigation() instanceof GroundPathNavigation) {
         GroundPathNavigation navigateGround = (GroundPathNavigation)this.host.getNavigation();
         navigateGround.setCanFloat(this.canSwim);
      }
   }

   public void updateTask() {
      if (this.stopAttack) {
         this.host.setTarget(null);
      }

      this.host.getLookControl().setLookAt(this.player, 30.0F, (float)this.host.getMaxHeadXRot());
      if (this.host.distanceToSqr(this.player) < this.temptDistanceMin * this.temptDistanceMin) {
         this.host.clearMovement();
      } else if (!this.host.useDirectNavigator()) {
         this.host.getNavigation().moveTo(this.player, this.speed);
      } else {
         this.host
            .directNavigator
            .setTargetPosition(LycPos.of((int)this.player.getX(), (int)this.player.getY(), (int)this.player.getZ()), this.speed);
      }
   }

   public boolean isRunning() {
      return this.isRunning;
   }
}
