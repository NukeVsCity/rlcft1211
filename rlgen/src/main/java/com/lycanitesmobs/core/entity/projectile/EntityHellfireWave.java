package com.lycanitesmobs.core.entity.projectile;

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
import com.lycanitesmobs.api.IGroupDemon;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.creature.EntityRahovart;


public class EntityHellfireWave extends BaseProjectileEntity {
   public EntityHellfireWall[][] hellfireWalls;
   protected int hellfireWidth = 5;
   protected int hellfireHeight = 3;
   protected int hellfireSize = 10;
   public int time = 0;
   public int timeMax = 200;
   public float angle = 90.0F;
   public double rotation = 0.0;

   public EntityHellfireWave(Level world) {
      super(LycEntities.type(EntityHellfireWave.class), world);
   }

   public EntityHellfireWave(Level world, LivingEntity shooterEntity) {
      super(LycEntities.type(EntityHellfireWave.class), world, shooterEntity);
   }

   public EntityHellfireWave(Level world, double x, double y, double z) {
      super(LycEntities.type(EntityHellfireWave.class), world, x, y, z);
   }

   @Override
   public void setup() {
      this.entityName = "hellfirewave";
      this.modInfo = LycanitesMobs.modInfo;
      this.setDamage(0);
      this.setProjectileScale(0.0F);
      this.setSize(2.0F, 2.0F);
      this.movement = false;
      this.ripper = true;
      this.pierceBlocks = true;
      this.projectileLife = 100;
      this.animationFrameMax = 59;
      this.noPhysics = true;
      this.waterProof = true;
      this.lavaProof = true;
   }

   public boolean isOnFire() {
      return false;
   }

   @Override
   public void tick() {
      if (!this.level().isClientSide) {
         if (this.time++ >= this.timeMax) {
            this.discard();
         }

         if (this.hellfireWalls == null) {
            this.hellfireWalls = new EntityHellfireWall[this.hellfireHeight][this.hellfireWidth];

            for (int row = 0; row < this.hellfireHeight; row++) {
               for (int col = 0; col < this.hellfireWidth; col++) {
                  if (this.getOwner() != null) {
                     this.hellfireWalls[row][col] = new EntityHellfireWavePart(this.level(), this.getOwner());
                  } else {
                     this.hellfireWalls[row][col] = new EntityHellfireWavePart(
                        this.level(), this.getX(), this.getY() + 5.0 + (double)(this.hellfireSize * row), this.getZ()
                     );
                  }

                  this.hellfireWalls[row][col].posY = this.getY() + (double)(this.hellfireSize * row);
                  this.level().addFreshEntity(this.hellfireWalls[row][col]);
                  this.hellfireWalls[row][col].setProjectileScale((float)(this.hellfireSize * 2));
               }
            }
         }

         for (int row = 0; row < this.hellfireHeight; row++) {
            for (int col = 0; col < this.hellfireWidth; col++) {
               double rotationRadians = Math.toRadians(
                  ((double)((float)col / (float)this.hellfireWidth * this.angle - this.angle / 2.0F) + this.rotation) % 360.0
               );
               double x = (double)((float)this.time / (float)this.timeMax * 200.0F) * Math.cos(rotationRadians) - Math.sin(rotationRadians);
               double z = (double)((float)this.time / (float)this.timeMax * 200.0F) * Math.sin(rotationRadians) + Math.cos(rotationRadians);
               this.hellfireWalls[row][col].posX = this.getX() + x;
               this.hellfireWalls[row][col].posY = this.getY() + (double)(this.hellfireSize * row);
               this.hellfireWalls[row][col].posZ = this.getZ() + z;
               this.hellfireWalls[row][col].projectileLife = 40;
               if (this.isRemoved()) {
                  this.hellfireWalls[row][col].discard();
               }
            }
         }
      }
   }

   @Override
   public boolean onEntityLivingDamage(LivingEntity entityLiving) {
      if (!entityLiving.fireImmune()) {
         entityLiving.igniteForSeconds(this.getEffectDuration(10) / 20);
      }

      return true;
   }

   @Override
   public boolean canDamage(LivingEntity targetEntity) {
      LivingEntity owner = this.getOwner();
      if (owner == null) {
         if (targetEntity instanceof EntityRahovart) {
            return false;
         }

         if (targetEntity instanceof IGroupDemon) {
            return false;
         }
      }

      return super.canDamage(targetEntity);
   }

   @Override
   public SoundEvent getLaunchSound() {
      return AssetManager.getSound("hellfirewave");
   }

   public float getBrightness() {
      return 1.0F;
   }

   public int getBrightnessForRender() {
      return 15728880;
   }

   @Override
   public ResourceLocation getTexture() {
      if (AssetManager.getTexture("hellfirewall") == null) {
         AssetManager.addTexture("hellfirewall", this.modInfo, "textures/items/hellfirewall.png");
      }

      return AssetManager.getTexture("hellfirewall");
   }
}
