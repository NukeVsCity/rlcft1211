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
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.entity.projectile.EntityHellShield;
import com.lycanitesmobs.core.info.CreatureManager;


public class EntityAstaroth extends TameableCreatureEntity implements Enemy {
   public EntityAstaroth(Level world) {
      super(world);
      this.attribute = EnumCreatureAttribute.UNDEFINED;
      this.hasAttackSound = false;
      this.solidCollision = false;
      this.setupMob();
      this.hitAreaWidthScale = 1.5F;
      this.setStepHeightCompat(1.0F);
   }

   @Override
   protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(false).setRange(1.0).setMaxChaseDistance(8.0F));
      this.tasks
         .addTask(this.nextCombatGoalIndex++, new AttackRangedGoal(this).setSpeed(1.0).setRange(40.0F).setMinChaseDistance(16.0F).setChaseTime(-1));
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (this.updateTick % 20L == 0L
         && this.getMasterTarget() != null
         && this.getMasterTarget() instanceof EntityAsmodeus
         && ((BaseCreatureEntity)this.getMasterTarget()).getBattlePhase() > 0) {
         EntityHellShield projectile = new EntityHellShield(this.level(), this);
         projectile.setProjectileScale(3.0F);
         LycMotion.setPosY(projectile, projectile.getY() - (double)this.getBbHeight() * 0.35);
         double dX = this.getMasterTarget().getX() - this.getX();
         double dY = this.getMasterTarget().getY() + (double)this.getMasterTarget().height * 0.75 - projectile.getY();
         double dZ = this.getMasterTarget().getZ() - this.getZ();
         double distance = (double)((float)Math.sqrt(dX * dX + dZ * dZ) * 0.1F);
         float velocity = 0.8F;
         projectile.shoot(dX, dY + distance, dZ, velocity, 0.0F);
         this.level().addFreshEntity(projectile);
      }
   }

   @Override
   public boolean canAttackEntity(LivingEntity target) {
      return !(target instanceof EntityTrite) && !(target instanceof EntityCacodemon) && !(target instanceof EntityAsmodeus)
         ? super.canAttackEntity(target)
         : false;
   }

   @Override
   public void attackRanged(Entity target, float range) {
      this.fireProjectile("devilstar", target, range, 0.0F, new Vec3(0.0, -2.8F, 0.0), 1.2F, 1.0F, 1.0F);
      super.attackRanged(target, range);
   }

   @Override
   public void die(DamageSource damageSource) {
      if (!this.level().isClientSide && CreatureManager.getInstance().getCreature("trite").enabled) {
         int j = 2 + this.random.nextInt(5) + this.level().getDifficulty().getId() - 1;
         if (this.isTamed()) {
            j = 3;
         }

         for (int k = 0; k < j; k++) {
            EntityTrite trite = (EntityTrite)CreatureManager.getInstance().getCreature("trite").createEntity(this.level());
            this.summonMinion(trite, (double)(this.random.nextFloat() * 360.0F), 0.5);
            if (this.isTamed()) {
               trite.setTemporary(100);
            }
         }
      }

      super.die(damageSource);
   }

   @Override
   public int getNoBagSize() {
      return 0;
   }

   @Override
   public int getBagSize() {
      return this.creatureInfo.bagSize;
   }
}
