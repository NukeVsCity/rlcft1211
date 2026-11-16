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



public abstract class DoorInteractGoal extends EntityAIBase {
   protected Mob host;
   protected DoorBlock targetDoor;
   boolean hasStoppedDoorInteraction;
   protected int entityPosX;
   protected int entityPosY;
   protected int entityPosZ;
   float entityPositionX;
   float entityPositionZ;

   public DoorInteractGoal(Mob par1EntityLiving) {
      this.host = par1EntityLiving;
   }

   public boolean shouldExecute() {
      if (!this.host.horizontalCollision) {
         return false;
      } else {
         Path pathEntity = this.host.getNavigation().getPath();
         if (pathEntity != null && !pathEntity.isDone()) {
            for (int i = 0; i < Math.min(pathEntity.getNextNodeIndex() + 2, pathEntity.getNodeCount()); i++) {
               Node pathpoint = pathEntity.getNode(i);
               this.entityPosX = pathpoint.x;
               this.entityPosY = pathpoint.y + 1;
               this.entityPosZ = pathpoint.z;
               if (this.host.distanceToSqr((double)this.entityPosX, this.host.getY(), (double)this.entityPosZ) <= 2.25) {
                  this.targetDoor = this.findUsableDoor(LycPos.of(this.entityPosX, this.entityPosY, this.entityPosZ));
                  if (this.targetDoor != null) {
                     return true;
                  }
               }
            }

            this.entityPosX = Mth.floor(this.host.getX());
            this.entityPosY = Mth.floor(this.host.getY() + 1.0);
            this.entityPosZ = Mth.floor(this.host.getZ());
            this.targetDoor = this.findUsableDoor(LycPos.of(this.entityPosX, this.entityPosY, this.entityPosZ));
            return this.targetDoor != null;
         } else {
            return false;
         }
      }
   }

   public void startExecuting() {
      this.hasStoppedDoorInteraction = false;
      this.entityPositionX = (float)((double)((float)this.entityPosX + 0.5F) - this.host.getX());
      this.entityPositionZ = (float)((double)((float)this.entityPosZ + 0.5F) - this.host.getZ());
   }

   public boolean shouldContinueExecuting() {
      return !this.hasStoppedDoorInteraction;
   }

   public void updateTask() {
      float f = (float)((double)((float)this.entityPosX + 0.5F) - this.host.getX());
      float f1 = (float)((double)((float)this.entityPosZ + 0.5F) - this.host.getZ());
      float f2 = this.entityPositionX * f + this.entityPositionZ * f1;
      if (f2 < 0.0F) {
         this.hasStoppedDoorInteraction = true;
      }
   }

   private DoorBlock findUsableDoor(BlockPos pos) {
      BlockState BlockState = this.host.level().getBlockState(pos);
      Block block = BlockState.getBlock();
      return block instanceof DoorBlock && Material.of(BlockState) == Material.WOOD ? (DoorBlock)block : null;
   }
}
