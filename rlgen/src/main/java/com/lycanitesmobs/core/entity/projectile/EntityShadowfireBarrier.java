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
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.PotionBase;
import com.lycanitesmobs.api.IGroupBoss;
import com.lycanitesmobs.api.IGroupDemon;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.creature.EntityRahovart;
import com.lycanitesmobs.core.info.ObjectLists;
import java.util.List;


public class EntityShadowfireBarrier extends BaseProjectileEntity {
   public Entity shootingEntity;

   public EntityShadowfireBarrier(Level par1World) {
      super(LycEntities.type(EntityShadowfireBarrier.class), par1World);
   }

   public EntityShadowfireBarrier(Level par1World, LivingEntity par2EntityLivingBase) {
      super(LycEntities.type(EntityShadowfireBarrier.class), par1World, par2EntityLivingBase);
   }

   public EntityShadowfireBarrier(Level par1World, double par2, double par4, double par6) {
      super(LycEntities.type(EntityShadowfireBarrier.class), par1World, par2, par4, par6);
   }

   @Override
   public void setup() {
      this.entityName = "shadowfirebarrier";
      this.modInfo = LycanitesMobs.modInfo;
      this.setDamage(10);
      this.setProjectileScale(20.0F);
      this.setSize(10.0F, 10.0F);
      this.movement = false;
      this.ripper = true;
      this.pierceBlocks = true;
      this.projectileLife = 40;
      this.animationFrameMax = 19;
      this.textureTiling = 2;
      this.noPhysics = true;
      this.waterProof = true;
      this.lavaProof = true;
   }

   public boolean isOnFire() {
      return false;
   }

   @Override
   public void tick() {
      super.tick();
      Vec3 vec3 = new Vec3(this.getX(), this.getY(), this.getZ());
      Vec3 vec31 = new Vec3(this.getX() + this.getDeltaMovement().x, this.getY() + this.getDeltaMovement().y, this.getZ() + this.getDeltaMovement().z);
      HitResult movingobjectposition = LycWorld.rayTraceBlocks(this.level(), vec3, vec31);
      if (!this.level().isClientSide) {
         List list = this.level()
            .getEntitiesWithinAABBExcludingEntity(this, this.getBoundingBox().inflate(this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z).expandTowards(1.0, 1.0, 1.0));
         double d0 = 0.0;
         LivingEntity entitylivingbase = this.getOwner();

         for (int j = 0; j < list.size(); j++) {
            Entity entity = (Entity)list.get(j);
            this.onImpact(new HitResult(entity));
         }
      }
   }

   @Override
   public boolean handleWaterMovement() {
      return false;
   }

   @Override
   public boolean onEntityLivingDamage(LivingEntity entityLiving) {
      PotionBase decay = ObjectManager.getEffect("decay");
      if (decay != null) {
         MobEffectInstance effectDecay = new MobEffectInstance(decay, 100, 0);
         if (entityLiving.canBeAffected(effectDecay)) {
            entityLiving.addEffect(effectDecay);
         }
      }

      MobEffectInstance effectBlindness = new MobEffectInstance(MobEffects.BLINDNESS, 100, 0);
      if (entityLiving.canBeAffected(effectBlindness)) {
         entityLiving.addEffect(effectBlindness);
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
   public void onDamage(LivingEntity target, float damage, boolean attackSuccess) {
      for (MobEffectInstance potionEffect : target.getActiveEffects().toArray(new MobEffectInstance[target.getActiveEffects().size()])) {
         if (ObjectLists.inEffectList("buffs", potionEffect.getEffect())) {
            target.removeEffect(potionEffect.getEffect());
         }
      }

      boolean obliterate = true;
      if (target instanceof Player) {
         obliterate = false;
      } else if (target instanceof TamableAnimal) {
         obliterate = !(((TamableAnimal)target).getOwner() instanceof Player);
      } else if (target instanceof TameableCreatureEntity) {
         obliterate = !(((TameableCreatureEntity)target).getOwner() instanceof Player);
      }

      if (target instanceof IGroupBoss) {
         obliterate = false;
      }

      if (target instanceof OwnableEntity) {
         obliterate = ((OwnableEntity)target).getOwner() == null;
      }

      if (obliterate) {
         target.setHealth(0.0F);
      }

      super.onDamage(target, damage, attackSuccess);
   }

   @Override
   public SoundEvent getLaunchSound() {
      return AssetManager.getSound("hellfirewall");
   }

   public float getBrightness() {
      return 1.0F;
   }

   public int getBrightnessForRender() {
      return 15728880;
   }
}
