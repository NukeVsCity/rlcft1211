package com.lycanitesmobs.core.entity.goals.targeting;

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
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.ExtendedEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;


public class RevengeRiderGoal extends FindAttackTargetGoal {
   private TameableCreatureEntity host;
   private int revengeTime;

   public RevengeRiderGoal(TameableCreatureEntity setHost) {
      super(setHost);
      this.host = setHost;
      this.tameTargeting = true;
   }

   public RevengeRiderGoal setHelpCall(boolean setHelp) {
      this.callForHelp = setHelp;
      return this;
   }

   public RevengeRiderGoal setSightCheck(boolean setSightCheck) {
      this.checkSight = setSightCheck;
      return this;
   }

   public RevengeRiderGoal setOnlyNearby(boolean setNearby) {
      this.nearbyOnly = setNearby;
      return this;
   }

   public RevengeRiderGoal setCantSeeTimeMax(int setCantSeeTimeMax) {
      this.cantSeeTimeMax = setCantSeeTimeMax;
      return this;
   }

   @Override
   public boolean shouldExecute() {
      if (!this.host.hasRiderTarget()) {
         return false;
      } else if (this.host.getRider() == null) {
         return false;
      } else {
         return this.getRiderRevengeTime() == this.revengeTime ? false : this.isEntityTargetable(this.getRiderRevengeTarget(), false);
      }
   }

   @Override
   public void startExecuting() {
      this.target = this.getRiderRevengeTarget();
      this.revengeTime = this.getRiderRevengeTime();

      try {
         if (this.callForHelp) {
            double d0 = this.getTargetDistance();

            for (BaseCreatureEntity possibleAlly : this.host
               .level()
               .getEntitiesOfClass(this.host.getClass(), this.host.getBoundingBox().inflate(d0, 4.0, d0), input -> input instanceof Mob)) {
               if (possibleAlly != this.host && possibleAlly.getTarget() == null && !possibleAlly.isAlliedTo(this.target)) {
                  possibleAlly.setTarget(this.target);
               }
            }
         }
      } catch (Exception var6) {
         LycanitesMobs.logWarning("", "An exception occurred when selecting help targets in rider revenge, this has been skipped to prevent a crash.");
         var6.printStackTrace();
      }

      super.startExecuting();
   }

   @Override
   public boolean shouldStopTargeting(LivingEntity target) {
      return target != this.getRiderRevengeTarget();
   }

   @Override
   protected boolean isValidTarget(LivingEntity target) {
      return true;
   }

   public LivingEntity getRiderRevengeTarget() {
      ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(this.host.getRider());
      return extendedEntity != null ? extendedEntity.lastAttackedEntity : null;
   }

   public int getRiderRevengeTime() {
      ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(this.host.getRider());
      return extendedEntity != null ? extendedEntity.lastAttackedTime : 0;
   }
}
