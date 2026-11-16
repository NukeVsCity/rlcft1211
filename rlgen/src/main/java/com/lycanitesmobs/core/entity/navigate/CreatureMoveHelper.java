package com.lycanitesmobs.core.entity.navigate;

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


public class CreatureMoveHelper extends MoveControl {
   protected BaseCreatureEntity entityCreature;
   protected int courseChangeCooldown;

   public CreatureMoveHelper(BaseCreatureEntity baseCreatureEntity) {
      super(baseCreatureEntity);
      this.entityCreature = baseCreatureEntity;
   }

   public void onUpdateMoveHelper() {
      if (!this.isControlledByRider()) {
         if (this.entityCreature.isStrongSwimmer() && this.entityCreature.isInWater()) {
            this.onUpdateSwimming();
         } else if (this.entityCreature.isFlying() && !this.entityCreature.isInWater()) {
            this.onUpdateFlying();
         } else {
            super.onUpdateMoveHelper();
         }
      }
   }

   public boolean isControlledByRider() {
      return this.entityCreature != null && this.entityCreature.getControllingPassenger() instanceof Player && this.entityCreature.canBeSteered();
   }

   public void onUpdateSwimming() {
      if (this.action == Action.MOVE_TO && !this.entityCreature.getNavigation().isDone()) {
         double x = this.getX() - this.entityCreature.getX();
         double y = this.getY() - this.entityCreature.getY();
         double z = this.getZ() - this.entityCreature.getZ();
         double distance = x * x + y * y + z * z;
         distance = (double)(float)Math.sqrt(distance);
         y /= distance;
         float f = (float)(Mth.atan2(z, x) * (180.0 / Math.PI)) - 90.0F;
         this.entityCreature.setYRot(this.limitAngle(this.entityCreature.getYRot(), f, 90.0F));
         this.entityCreature.yBodyRot = this.entityCreature.getYRot();
         float f1 = (float)(this.speed * this.entityCreature.getAttribute(Attributes.MOVEMENT_SPEED).getValue());
         this.entityCreature.setSpeed(this.entityCreature.getSpeed() + (f1 - this.entityCreature.getSpeed()) * 0.125F);
         double d4 = Math.sin((double)(this.entityCreature.tickCount + this.entityCreature.getId()) * 0.5) * 0.05;
         double d5 = Math.cos((double)(this.entityCreature.getYRot() * (float) (Math.PI / 180.0)));
         double d6 = Math.sin((double)(this.entityCreature.getYRot() * (float) (Math.PI / 180.0)));
         LycMotion.addX(this.entityCreature, d4 * d5);
         LycMotion.addZ(this.entityCreature, d4 * d6);
         d4 = Math.sin((double)(this.entityCreature.tickCount + this.entityCreature.getId()) * 0.75) * 0.05;
         LycMotion.addY(this.entityCreature, d4 * (d6 + d5) * 0.25);
         LycMotion.setY(this.entityCreature, this.entityCreature.getDeltaMovement().y + (double)this.entityCreature.getSpeed() * y * 0.1);
         LookControl entitylookhelper = this.entityCreature.getLookControl();
         double d7 = this.entityCreature.getX() + x / distance * 2.0;
         double d8 = (double)this.entityCreature.getEyeHeight() + this.entityCreature.getY() + y / distance;
         double d9 = this.entityCreature.getZ() + z / distance * 2.0;
         double d10 = entitylookhelper.getLookPosX();
         double d11 = entitylookhelper.getLookPosY();
         double d12 = entitylookhelper.getLookPosZ();
         if (!entitylookhelper.getIsLooking()) {
            d10 = d7;
            d11 = d8;
            d12 = d9;
         }

         this.entityCreature.getLookControl().setLookAt(d10 + (d7 - d10) * 0.125, d11 + (d8 - d11) * 0.125, d12 + (d9 - d12) * 0.125, 10.0F, 40.0F);
      } else {
         this.entityCreature.setSpeed(0.0F);
      }
   }

   public void onUpdateFlying() {
      if (this.action == Action.MOVE_TO) {
         double xDistance = this.getX() - this.entityCreature.getX();
         double yDistance = this.getY() - this.entityCreature.getY();
         double zDistance = this.getZ() - this.entityCreature.getZ();
         double distance = xDistance * xDistance + yDistance * yDistance + zDistance * zDistance;
         if (this.courseChangeCooldown-- <= 0) {
            this.courseChangeCooldown = this.courseChangeCooldown + this.entityCreature.getRandom().nextInt(5) + 2;
            distance = (double)(float)Math.sqrt(distance);
            if (distance >= 1.0) {
               this.entityCreature.setSpeed((float)this.entityCreature.getAttribute(Attributes.MOVEMENT_SPEED).getValue());
               double speed = (double)this.entityCreature.getSpeed() / 2.4 * this.getSpeed();
               LycMotion.addX(this.entityCreature, xDistance / distance * speed);
               LycMotion.addY(this.entityCreature, yDistance / distance * speed);
               LycMotion.addZ(this.entityCreature, zDistance / distance * speed);
            } else {
               this.action = Action.WAIT;
            }
         }
      }

      if (this.entityCreature.getTarget() != null) {
         LivingEntity entitylivingbase = this.entityCreature.getTarget();
         double distanceX = entitylivingbase.getX() - this.entityCreature.getX();
         double distanceZ = entitylivingbase.getZ() - this.entityCreature.getZ();
         this.entityCreature.yBodyRot = this.entityCreature.setYRot(-((float)Mth.atan2(distanceX, distanceZ))
            * (180.0F / (float)Math.PI));
      } else if (this.action == Action.MOVE_TO) {
         this.entityCreature.yBodyRot = this.entityCreature.setYRot(-(
               (float)Mth.atan2(this.entityCreature.getDeltaMovement().x, this.entityCreature.getDeltaMovement().z)
            )
            * (180.0F / (float)Math.PI));
      }
   }
}
