package com.lycanitesmobs.core.info.projectile.behaviours;

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
import com.google.gson.JsonObject;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.CustomProjectileEntity;
import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;


public class ProjectileBehaviourFireProjectiles extends ProjectileBehaviour {
   public String projectileName;
   public int tickRate = 10;
   public int persistentCount = 0;
   public int impactCount = 5;
   public float velocity = 1.2F;

   @Override
   public void loadFromJSON(JsonObject json) {
      this.projectileName = json.get("projectileName").getAsString();
      if (json.has("tickRate")) {
         this.tickRate = json.get("tickRate").getAsInt();
      }

      if (json.has("impactCount")) {
         this.impactCount = json.get("impactCount").getAsInt();
      }

      if (json.has("persistentCount")) {
         this.persistentCount = json.get("persistentCount").getAsInt();
      }

      if (json.has("velocity")) {
         this.velocity = json.get("velocity").getAsFloat();
      }
   }

   @Override
   public void onProjectileUpdate(BaseProjectileEntity projectile) {
      if (this.tickRate >= 0 && projectile.updateTick % (long)this.tickRate == 0L && !projectile.level().isClientSide) {
         if (this.persistentCount <= 0 || ((CustomProjectileEntity)projectile).spawnedProjectiles.size() < this.persistentCount) {
            this.createProjectile(projectile);
         }
      }
   }

   @Override
   public void onProjectileImpact(BaseProjectileEntity projectile, Level world, BlockPos pos) {
      if (!projectile.level().isClientSide) {
         for (int i = 0; i < this.impactCount; i++) {
            this.createProjectile(projectile);
         }

         for (BaseProjectileEntity spawnedProjectile : ((CustomProjectileEntity)projectile).spawnedProjectiles) {
            spawnedProjectile.discard();
         }
      }
   }

   public BaseProjectileEntity createProjectile(BaseProjectileEntity projectile) {
      ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile(this.projectileName);
      if (projectileInfo == null) {
         return null;
      } else {
         BaseProjectileEntity childProjectile;
         if (projectile.getThrower() != null) {
            childProjectile = projectileInfo.createProjectile(projectile.level(), projectile.getThrower());
            childProjectile.setPos(
               projectile.position().x, projectile.position().y, projectile.position().z
            );
         } else {
            childProjectile = projectileInfo.createProjectile(
               projectile.level(),
               projectile.position().x,
               projectile.position().y,
               projectile.position().z
            );
         }

         if (childProjectile instanceof CustomProjectileEntity) {
            ((CustomProjectileEntity)childProjectile).setParent(projectile);
         }

         if (this.persistentCount > 0 && childProjectile instanceof CustomProjectileEntity) {
            ((CustomProjectileEntity)childProjectile).laserAngle = 360.0F
               / (float)this.persistentCount
               * (float)((CustomProjectileEntity)projectile).spawnedProjectiles.size();
            ((CustomProjectileEntity)projectile).spawnedProjectiles.add(childProjectile);
         }

         double motionT = projectile.getDeltaMovement().x + projectile.getDeltaMovement().y + projectile.getDeltaMovement().z;
         if (projectile.getDeltaMovement().x < 0.0) {
            motionT -= projectile.getDeltaMovement().x * 2.0;
         }

         if (projectile.getDeltaMovement().y < 0.0) {
            motionT -= projectile.getDeltaMovement().y * 2.0;
         }

         if (projectile.getDeltaMovement().z < 0.0) {
            motionT -= projectile.getDeltaMovement().z * 2.0;
         }

         childProjectile.shoot(
            projectile.getDeltaMovement().x / motionT + (projectile.level().random.nextGaussian() - 0.5),
            projectile.getDeltaMovement().y / motionT + (projectile.level().random.nextGaussian() - 0.5),
            projectile.getDeltaMovement().z / motionT + (projectile.level().random.nextGaussian() - 0.5),
            this.velocity,
            0.0F
         );
         projectile.playSound(childProjectile.getLaunchSound(), 1.0F, 1.0F / (projectile.level().random.nextFloat() * 0.4F + 0.8F));
         projectile.level().addFreshEntity(childProjectile);
         return childProjectile;
      }
   }
}
