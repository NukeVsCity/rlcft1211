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
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.entity.projectile.EntityHellfireOrb;
import java.util.ArrayList;
import java.util.List;


public class EntityBehemoth extends TameableCreatureEntity implements Enemy {
   protected static final EntityDataAccessor<Integer> HELLFIRE_ENERGY = SynchedEntityData.defineId(EntityBehemoth.class, EntityDataSerializers.INT);
   public int hellfireEnergy = 0;
   public List<EntityHellfireOrb> hellfireOrbs = new ArrayList<>();

   public EntityBehemoth(Level world) {
      super(world);
      this.attribute = EnumCreatureAttribute.UNDEAD;
      this.hasAttackSound = false;
      this.setupMob();
   }

   @Override
   protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(false).setRange(1.0).setMaxChaseDistance(8.0F));
      this.tasks
         .addTask(this.nextCombatGoalIndex++, new AttackRangedGoal(this).setSpeed(1.0).setRange(16.0F).setMinChaseDistance(0.0F).setChaseTime(-1));
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(HELLFIRE_ENERGY, this.hellfireEnergy);
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (!this.level().isClientSide) {
         this.entityData.set(HELLFIRE_ENERGY, this.hellfireEnergy);
      } else {
         try {
            this.hellfireEnergy = (Integer)this.entityData.get(HELLFIRE_ENERGY);
         } catch (Exception var7) {
         }
      }

      if (this.level().isClientSide && this.hellfireEnergy > 0) {
         EntityRahovart.updateHellfireOrbs(this, this.updateTick, 3, this.hellfireEnergy, 1.0F, this.hellfireOrbs);
      }

      if (!this.level().isClientSide && this.isMoving() && this.tickCount % 5 == 0) {
         int trailHeight = 1;
         int trailWidth = 1;
         if (this.isRareVariant()) {
            trailWidth = 3;
         }

         for (int y = 0; y < trailHeight; y++) {
            Block block = this.level().getBlockState(this.blockPosition().offset(0, y, 0)).getBlock();
            if (block != null
               && (
                  block == Blocks.AIR
                     || block == Blocks.FIRE
                     || block == Blocks.SNOW_BLOCK
                     || block == Blocks.SHORT_GRASS
                     || block == ObjectManager.getBlock("frostfire")
                     || block == ObjectManager.getBlock("icefire")
                     || block == ObjectManager.getBlock("scorchfire")
                     || block == ObjectManager.getBlock("doomfire")
               )) {
               if (trailWidth == 1) {
                  this.level().setBlock(this.blockPosition().offset(0, y, 0), ObjectManager.getBlock("hellfire").defaultBlockState(), 3);
               } else {
                  for (int x = -(trailWidth / 2); x < trailWidth / 2 + 1; x++) {
                     for (int z = -(trailWidth / 2); z < trailWidth / 2 + 1; z++) {
                        this.level().setBlock(this.blockPosition().offset(x, y, z), ObjectManager.getBlock("hellfire").defaultBlockState(), 3);
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public boolean canAttackEntity(LivingEntity target) {
      return target instanceof EntityBelph ? false : super.canAttackEntity(target);
   }

   @Override
   public boolean canAttackWithPickup() {
      return true;
   }

   @Override
   public boolean attackMelee(Entity target, double damageScale) {
      if (!super.attackMelee(target, damageScale)) {
         return false;
      } else {
         if (target instanceof LivingEntity) {
            LivingEntity entityLivingBase = (LivingEntity)target;
            if (this.canPickupEntity(entityLivingBase)) {
               this.pickupEntity(entityLivingBase);
            } else if (this.getPickupEntity() == target && this.getRandom().nextBoolean()) {
               this.dropPickupEntity();
               Vec3 throwVelocity = this.getFacingPositionDouble(0.0, 1.0, 0.0, 2.0, (double)this.getYRot());
               LycMotion.setX(target, throwVelocity.x);
               LycMotion.setY(target, throwVelocity.y);
               LycMotion.setZ(target, throwVelocity.z);
            }
         }

         return true;
      }
   }

   @Override
   public void attackRanged(Entity target, float range) {
      this.fireProjectile("hellfireball", target, range, 0.0F, new Vec3(0.0, 0.0, 0.0), 1.2F, 2.0F, 1.0F);
      super.attackRanged(target, range);
   }

   @Override
   public double[] getPickupOffset(Entity entity) {
      Vec3 offset = this.getFacingPositionDouble(0.0, 2.0, 0.0, 1.5, (double)this.getYRot());
      return new double[]{offset.x, offset.y, offset.z};
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
   public boolean canBurn() {
      return false;
   }

   @Override
   public boolean petControlsEnabled() {
      return true;
   }

   @Override
   public ResourceLocation getTexture() {
      if (this.hasCustomName() && "Krampus".equals(this.getCustomName().getString())) {
         String textureName = this.getTextureName() + "_krampus";
         if (AssetManager.getTexture(textureName) == null) {
            AssetManager.addTexture(textureName, this.creatureInfo.modInfo, "textures/entity/" + textureName.toLowerCase() + ".png");
         }

         return AssetManager.getTexture(textureName);
      } else {
         return super.getTexture();
      }
   }
}
