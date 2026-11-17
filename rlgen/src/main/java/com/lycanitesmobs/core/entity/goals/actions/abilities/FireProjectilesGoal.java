package com.lycanitesmobs.core.entity.goals.actions.abilities;

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
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.goals.BaseGoal;
import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;


public class FireProjectilesGoal extends BaseGoal {
   protected String projectileName;
   protected Class<? extends BaseProjectileEntity> projectileClass;
   protected float velocity = 1.6F;
   protected float inaccuracy = 0.0F;
   protected float scale = 1.0F;
   protected float angle = 0.0F;
   protected Vec3 offset = Vec3.ZERO;
   private int fireRate = 60;
   private boolean allPlayers = false;
   private int randomCount = 0;
   protected int phase = -1;
   protected boolean overhead = false;
   private int abilityTime = 60;
   private Entity attackTarget;

   public FireProjectilesGoal(BaseCreatureEntity setHost) {
      super(setHost);
   }

   public FireProjectilesGoal setPhase(int phase) {
      this.phase = phase;
      return this;
   }

   public FireProjectilesGoal setProjectile(String projectileName) {
      this.projectileName = projectileName;
      return this;
   }

   public FireProjectilesGoal setProjectile(Class<? extends BaseProjectileEntity> projectileClass) {
      this.projectileClass = projectileClass;
      return this;
   }

   public FireProjectilesGoal setFireRate(int fireRate) {
      this.fireRate = fireRate;
      return this;
   }

   public FireProjectilesGoal setVelocity(float velocity) {
      this.velocity = velocity;
      return this;
   }

   public FireProjectilesGoal setScale(float scale) {
      this.scale = scale;
      return this;
   }

   public FireProjectilesGoal setInaccuracy(int inaccuracy) {
      this.inaccuracy = (float)inaccuracy;
      return this;
   }

   public FireProjectilesGoal setAngle(int angle) {
      this.angle = (float)angle;
      return this;
   }

   public FireProjectilesGoal setOffset(Vec3 offset) {
      this.offset = offset;
      return this;
   }

   public FireProjectilesGoal setOverhead(boolean overhead) {
      this.overhead = overhead;
      return this;
   }

   public FireProjectilesGoal setAllPlayers(boolean allPlayers) {
      this.allPlayers = allPlayers;
      return this;
   }

   public FireProjectilesGoal setRandomCount(int randomCount) {
      this.randomCount = randomCount;
      return this;
   }

   @Override
   public boolean shouldExecute() {
      if (!super.shouldExecute()) {
         return false;
      } else if (this.projectileName == null && this.projectileClass == null) {
         return false;
      } else {
         this.attackTarget = this.host.getTarget();
         return !this.allPlayers && this.randomCount <= 0 && this.attackTarget == null ? false : this.phase < 0 || this.phase == this.host.getBattlePhase();
      }
   }

   public void startExecuting() {
      this.abilityTime = 1;
   }

   public void resetTask() {
      this.attackTarget = null;
   }

   public void updateTask() {
      if (this.abilityTime++ % Math.round((float)(1.0 / this.host.getAttribute(BaseCreatureEntity.RANGED_SPEED).getValue() * (double)this.fireRate)) == 0
         )
       {
         if (this.allPlayers) {
            for (Player target : this.host.playerTargets) {
               if (!target.getAbilities().invulnerable && !target.isSpectator()) {
                  this.fireProjectile(target);
               }
            }
         } else if (this.randomCount > 0) {
            for (int i = 0; i < this.randomCount; i++) {
               if (this.projectileName != null) {
                  this.host
                     .fireProjectile(
                        this.projectileName,
                        null,
                        this.host.getRandom().nextFloat() * 10.0F,
                        this.host.getRandom().nextFloat() * this.angle,
                        this.offset,
                        this.velocity,
                        this.scale,
                        this.inaccuracy
                     );
               } else if (this.projectileClass != null) {
                  this.host
                     .fireProjectile(
                        this.projectileClass,
                        null,
                        this.host.getRandom().nextFloat() * 10.0F,
                        this.host.getRandom().nextFloat() * this.angle,
                        this.offset,
                        this.velocity,
                        this.scale,
                        this.inaccuracy
                     );
               }
            }
         } else {
            this.fireProjectile(this.attackTarget);
         }
      }
   }

   public BaseProjectileEntity fireProjectile(Entity target) {
      BaseProjectileEntity projectile = this.createProjectile();
      if (this.overhead) {
         this.fireProjectileOverhead(projectile);
      } else {
         this.host.fireProjectile(projectile, target, this.host.distanceTo(target), this.angle, this.offset, this.velocity, this.scale, this.inaccuracy);
      }

      return projectile;
   }

   public BaseProjectileEntity createProjectile() {
      BaseProjectileEntity projectile = null;
      if (this.projectileName != null) {
         ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile(this.projectileName);
         if (projectileInfo == null) {
            return projectile;
         }

         projectile = projectileInfo.createProjectile(this.host.level(), this.host);
      } else if (this.projectileClass != null) {
         try {
            projectile = this.projectileClass.getConstructor(Level.class, LivingEntity.class).newInstance(this.host.level(), this.host);
         } catch (Exception var3) {
            LycanitesMobs.logWarning("", "Unable to create a projectile from the class: " + this.projectileClass);
         }
      }

      if (projectile != null) {
         projectile.setProjectileScale(this.scale);
      }

      return projectile;
   }

   public void fireProjectileOverhead(BaseProjectileEntity projectile) {
      Entity target = (Entity)(this.attackTarget != null ? this.attackTarget : this.host);
      projectile.shoot(0.0, -1.0, 0.0, 0.5F, this.inaccuracy);
      projectile.setPos(
         (double)target.blockPosition().getX(),
         (double)target.blockPosition().getY() + this.offset.y + this.host.getRandom().nextDouble() * 3.0,
         (double)target.blockPosition().getZ()
      );
      this.host.level().addFreshEntity(projectile);
   }
}
