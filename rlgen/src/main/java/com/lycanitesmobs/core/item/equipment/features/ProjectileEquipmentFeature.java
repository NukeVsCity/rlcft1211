package com.lycanitesmobs.core.item.equipment.features;

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
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.CustomProjectileEntity;
import com.lycanitesmobs.core.entity.ExtendedEntity;
import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;


public class ProjectileEquipmentFeature extends EquipmentFeature {
   public String projectileName;
   public String projectileTrigger = "secondary";
   public String projectilePattern = "simple";
   public double hitChance = 0.05;
   public int cooldown = 2;
   public int count = 1;
   public double spreadX = 0.0;
   public double spreadY = 0.0;
   public double ringRange = 0.0;
   public int bonusDamage = 0;
   private BaseProjectileEntity channeledProjectile;

   @Override
   public void loadFromJSON(JsonObject json) {
      super.loadFromJSON(json);
      this.projectileName = json.get("projectileName").getAsString();
      if (json.has("projectileTrigger")) {
         this.projectileTrigger = json.get("projectileTrigger").getAsString();
      }

      if (json.has("projectilePattern")) {
         this.projectilePattern = json.get("projectilePattern").getAsString();
      }

      if (json.has("hitChance")) {
         this.hitChance = json.get("hitChance").getAsDouble();
      }

      if (json.has("cooldown")) {
         this.cooldown = json.get("cooldown").getAsInt();
      }

      if (json.has("count")) {
         this.count = json.get("count").getAsInt();
      }

      if (json.has("spreadX")) {
         this.spreadX = json.get("spreadX").getAsDouble();
      }

      if (json.has("spreadY")) {
         this.spreadY = json.get("spreadY").getAsDouble();
      }

      if (json.has("ringRange")) {
         this.ringRange = json.get("ringRange").getAsDouble();
      }

      if (json.has("bonusDamage")) {
         this.bonusDamage = json.get("bonusDamage").getAsInt();
      }
   }

   @Override
   public boolean isActive(ItemStack itemStack, int level) {
      return !super.isActive(itemStack, level) ? false : ProjectileManager.getInstance().getProjectile(this.projectileName) != null;
   }

   @Override
   public String getDescription(ItemStack itemStack, int level) {
      if (!this.isActive(itemStack, level)) {
         return null;
      } else {
         ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile(this.projectileName);
         String description = LanguageManager.translate("equipment.feature." + this.featureType) + " " + projectileInfo.getTitle();
         if (!"simple".equals(this.projectilePattern)) {
            description = description + " " + LanguageManager.translate("equipment.feature.projectile.pattern." + this.projectilePattern);
         }

         description = description + " " + LanguageManager.translate("equipment.feature.projectile.trigger." + this.projectileTrigger);
         if ("hit".equals(this.projectileTrigger)) {
            description = description + " " + String.format("%.0f", this.hitChance * 100.0) + "%";
         } else {
            description = description + " " + String.format("%.1f", (float)this.cooldown / 20.0F) + "s";
         }

         return description;
      }
   }

   @Override
   public String getSummary(ItemStack itemStack, int level) {
      if (!this.isActive(itemStack, level)) {
         return null;
      } else {
         ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile(this.projectileName);
         String summary = projectileInfo.getTitle();
         if (this.bonusDamage != 0) {
            summary = summary + " +" + this.bonusDamage;
         }

         return summary;
      }
   }

   public boolean onUsePrimary(Level world, Player shooter, InteractionHand hand) {
      if (!"primary".equalsIgnoreCase(this.projectileTrigger)) {
         return false;
      } else {
         ExtendedEntity shooterExt = ExtendedEntity.getForEntity(shooter);
         if (shooterExt == null) {
            return false;
         } else if (shooterExt.getProjectileCooldown(1, this.projectileName) > 0) {
            return false;
         } else {
            shooterExt.setProjectileCooldown(1, this.projectileName, this.cooldown);
            this.fireProjectile(shooter);
            return true;
         }
      }
   }

   public boolean onUseSecondary(Level world, Player shooter, InteractionHand hand) {
      return "secondary".equalsIgnoreCase(this.projectileTrigger);
   }

   public boolean onHoldSecondary(LivingEntity shooter, int count) {
      if (!"secondary".equalsIgnoreCase(this.projectileTrigger)) {
         return false;
      } else {
         ExtendedEntity shooterExt = ExtendedEntity.getForEntity(shooter);
         if (shooterExt == null) {
            return false;
         } else if (shooterExt.getProjectileCooldown(2, this.projectileName) > 0) {
            return false;
         } else {
            shooterExt.setProjectileCooldown(2, this.projectileName, this.cooldown);
            this.fireProjectile(shooter);
            return true;
         }
      }
   }

   public boolean onHitEntity(ItemStack itemStack, LivingEntity target, LivingEntity attacker) {
      if (target != null && attacker != null && !attacker.level().isClientSide && !attacker.isShiftKeyDown() && "hit".equals(this.projectileTrigger)) {
         if (attacker.getRandom().nextDouble() <= this.hitChance) {
            this.fireProjectile(attacker);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public void fireProjectile(LivingEntity shooter) {
      if (shooter != null && !shooter.level().isClientSide && this.count > 0) {
         if (this.channeledProjectile != null) {
            if (this.channeledProjectile.isAlive()) {
               this.channeledProjectile.projectileLife = 20;
               return;
            }

            this.channeledProjectile = null;
         }

         Level world = shooter.level();
         BaseProjectileEntity mainProjectile = null;
         Vec3 firePos = new Vec3(shooter.getX(), shooter.getY() + (double)shooter.getBbHeight() * 0.65, shooter.getZ());
         double offsetX = 0.0;
         if ("spread".equals(this.projectilePattern)) {
            this.count = 10;
            this.spreadX = 45.0;
            this.spreadY = 10.0;

            for (int i = 0; i < this.count; i++) {
               double yaw = (double)shooter.getYRot() + this.spreadX * shooter.getRandom().nextDouble() - this.spreadX / 2.0;
               double pitch = (double)shooter.getXRot() + this.spreadY * shooter.getRandom().nextDouble() - this.spreadY / 2.0;
               ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile(this.projectileName);
               BaseProjectileEntity projectile = projectileInfo.createProjectile(world, shooter);
               projectile.setPos(firePos.x, firePos.y, firePos.z);
               projectile.shootFromRotation(shooter, (float)pitch, (float)yaw - (float)offsetX, 0.0F, (float)projectileInfo.velocity, 0.0F);
               projectile.setBonusDamage(this.bonusDamage);
               world.addFreshEntity(projectile);
               mainProjectile = projectile;
            }
         } else if ("ring".equals(this.projectilePattern)) {
            double angle = this.ringRange / (double)this.count;

            for (int i = 0; i < this.count; i++) {
               double yaw = (double)shooter.getYRot() + angle * (double)i - this.ringRange / 2.0;
               ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile(this.projectileName);
               BaseProjectileEntity projectile = projectileInfo.createProjectile(world, shooter);
               projectile.setPos(firePos.x, firePos.y, firePos.z);
               projectile.setBonusDamage(this.bonusDamage);
               world.addFreshEntity(projectile);
               projectile.shootFromRotation(shooter, shooter.getXRot(), (float)yaw - (float)offsetX, 0.0F, (float)projectileInfo.velocity, 0.0F);
               mainProjectile = projectile;
            }
         } else {
            ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile(this.projectileName);
            mainProjectile = projectileInfo.createProjectile(world, shooter);
            mainProjectile.setPos(firePos.x, firePos.y, firePos.z);
            mainProjectile.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot() - (float)offsetX, 0.0F, (float)projectileInfo.velocity, 0.0F);
            mainProjectile.setBonusDamage(this.bonusDamage);
            world.addFreshEntity(mainProjectile);
         }

         if (this.count == 1 && mainProjectile instanceof CustomProjectileEntity) {
            CustomProjectileEntity customProjectileEntity = (CustomProjectileEntity)mainProjectile;
            if (customProjectileEntity.shouldChannel()) {
               this.channeledProjectile = customProjectileEntity;
            }
         }

         if (shooter instanceof Player && mainProjectile != null) {
            world.playSound(
               null,
               shooter.blockPosition(),
               mainProjectile.getLaunchSound(),
               SoundSource.NEUTRAL,
               0.5F,
               0.4F / (shooter.getRandom().nextFloat() * 0.4F + 0.8F)
            );
         }
      }
   }

   public Vec3 getFacingPosition(LivingEntity entity, double distance, double angle) {
      angle = Math.toRadians(angle);
      double xAmount = -Math.sin(angle);
      double zAmount = Math.cos(angle);
      return new Vec3(entity.getX() + distance * xAmount, entity.getY(), entity.getZ() + distance * zAmount);
   }
}
