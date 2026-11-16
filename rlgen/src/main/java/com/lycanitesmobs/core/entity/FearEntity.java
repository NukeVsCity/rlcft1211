package com.lycanitesmobs.core.entity;

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
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.core.inventory.InventoryCreature;


public class FearEntity extends BaseCreatureEntity {
   public Entity fearedEntity;

   public FearEntity(Level world) {
      super(world);
      this.attribute = EnumCreatureAttribute.UNDEFINED;
      this.hasStepSound = false;
      this.hasAttackSound = false;
      this.spreadFire = false;
      this.setSize(0.8F, 1.8F);
      this.setupMob();
   }

   public FearEntity(Level world, Entity feared) {
      this(world);
      this.setFearedEntity(feared);
   }

   @Override
   public void setupMob() {
      this.xpReward = 0;
      this.inventory = new InventoryCreature(this.getName(), this);
      this.isImmuneToFire = true;
   }

   @Override
   public void loadItemDrops() {
   }

   @Override
   protected boolean canDespawn() {
      return false;
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (!this.level().isClientSide) {
         if (this.fearedEntity != null && this.fearedEntity.isAlive() && this.fearedEntity instanceof LivingEntity) {
            LivingEntity fearedEntityLiving = (LivingEntity)this.fearedEntity;
            if (this.canPickupEntity(fearedEntityLiving)) {
               this.pickupEntity(fearedEntityLiving);
            }

            if (this.hasPickupEntity() && !(this.getPickupEntity() instanceof Player)) {
               this.getPickupEntity().setYRot(this.getYRot());
               this.getPickupEntity().setXRot(this.getXRot());
            }

            if (this.getPickupEntity() == null) {
               this.set(this.fearedEntity.getX(), this.fearedEntity.getY(), this.fearedEntity.getZ());
               LycMotion.setX(this, this.fearedEntity.getDeltaMovement().x);
               LycMotion.setY(this, this.fearedEntity.getDeltaMovement().y);
               LycMotion.setZ(this, this.fearedEntity.getDeltaMovement().z);
               this.fallDistance = 0.0F;
            }

            if (ObjectManager.getEffect("fear") != null && fearedEntityLiving.hasEffect(ObjectManager.getEffect("fear"))) {
               if (this.fearedEntity instanceof LivingEntity) {
                  if (fearedEntityLiving.hasEffect(MobEffects.LEVITATION)) {
                     MobEffectInstance activeDebuff = fearedEntityLiving.getEffect(MobEffects.LEVITATION);
                     this.addEffect(new MobEffectInstance(MobEffects.LEVITATION, activeDebuff.getDuration(), activeDebuff.getAmplifier()));
                  }

                  Potion instability = ObjectManager.getEffect("instability");
                  if (instability != null && fearedEntityLiving.hasEffect(instability)) {
                     MobEffectInstance activeDebuff = fearedEntityLiving.getEffect(instability);
                     this.addEffect(new MobEffectInstance(MobEffects.LEVITATION, activeDebuff.getDuration(), activeDebuff.getAmplifier()));
                  }
               }
            } else {
               this.discard();
            }
         } else {
            this.discard();
         }
      }
   }

   @Override
   public boolean rollWanderChance() {
      return true;
   }

   public void setFearedEntity(Entity feared) {
      this.fearedEntity = feared;
      this.setSize(feared.width, feared.height);
      this.noPhysics = feared.noPhysics;
      this.setStepHeightCompat(feared.stepHeight);
      this.moveTo(feared.getX(), feared.getY(), feared.getZ(), feared.getYRot(), feared.getXRot());
      if (feared instanceof LivingEntity && !(feared instanceof Player)) {
         LivingEntity fearedEntityLiving = (LivingEntity)feared;
         this.getAttribute(Attributes.MOVEMENT_SPEED)
            .setBaseValue(fearedEntityLiving.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue());
      }
   }

   @Override
   public boolean isDamageTypeApplicable(String type, DamageSource source, float damage) {
      return false;
   }

   @Override
   public boolean canBeAffected(MobEffectInstance par1PotionEffect) {
      return false;
   }

   @Override
   public boolean isFlying() {
      if (this.pickupEntity != null) {
         if (this.pickupEntity instanceof BaseCreatureEntity) {
            return ((BaseCreatureEntity)this.pickupEntity).isFlying();
         }

         if (this.pickupEntity instanceof FlyingMob) {
            return true;
         }

         if (this.pickupEntity instanceof Player) {
            return ((Player)this.pickupEntity).getAbilities().instabuild;
         }
      }

      return false;
   }

   public boolean isInvisible() {
      return true;
   }

   public boolean isInvisibleToPlayer(Player player) {
      return true;
   }

   public boolean canBeCollidedWith() {
      return false;
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return AssetManager.getSound("effect_fear");
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource damageSource) {
      return AssetManager.getSound("effect_fear");
   }

   @Override
   protected SoundEvent getDeathSound() {
      return AssetManager.getSound("effect_fear");
   }

   @Override
   public void playFlySound() {
   }
}
