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



public class BreakDoorGoal extends DoorInteractGoal {
   private int breakingTime;
   private int lastBreakTime = -1;

   public BreakDoorGoal(Mob setHost) {
      super(setHost);
   }

   @Override
   public boolean shouldExecute() {
      if (!super.shouldExecute()) {
         return false;
      } else {
         return !this.host.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
            ? false
            : !BlockDoor.isOpen(this.host.level(), LycPos.of(this.entityPosX, this.entityPosY, this.entityPosZ));
      }
   }

   @Override
   public void startExecuting() {
      super.startExecuting();
      this.breakingTime = 0;
   }

   @Override
   public boolean shouldContinueExecuting() {
      double distance = this.host.distanceToSqr((double)this.entityPosX, (double)this.entityPosY, (double)this.entityPosZ);
      return this.breakingTime <= 240
         && !BlockDoor.isOpen(this.host.level(), LycPos.of(this.entityPosX, this.entityPosY, this.entityPosZ))
         && distance < 4.0;
   }

   public void resetTask() {
      super.resetTask();
      this.host.level().sendBlockBreakProgress(this.host.getId(), LycPos.of(this.entityPosX, this.entityPosY, this.entityPosZ), -1);
   }

   @Override
   public void updateTask() {
      super.updateTask();
      if (this.host.getRandom().nextInt(20) == 0) {
         this.host.level().playEvent(1010, LycPos.of(this.entityPosX, this.entityPosY, this.entityPosZ), 0);
      }

      this.breakingTime++;
      int breaking = (int)((float)this.breakingTime / 240.0F * 10.0F);
      if (breaking != this.lastBreakTime) {
         this.host.level().sendBlockBreakProgress(this.host.getId(), LycPos.of(this.entityPosX, this.entityPosY, this.entityPosZ), breaking);
         this.lastBreakTime = breaking;
      }

      if (this.breakingTime == 240 && this.host.level().getDifficulty() == Difficulty.HARD) {
         this.host.level().removeBlock(LycPos.of(this.entityPosX, this.entityPosY, this.entityPosZ), false);
         this.host.level().playEvent(1012, LycPos.of(this.entityPosX, this.entityPosY, this.entityPosZ), 0);
         this.host.level().playEvent(2001, LycPos.of(this.entityPosX, this.entityPosY, this.entityPosZ), 0);
      }
   }
}
