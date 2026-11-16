package com.lycanitesmobs.core.entity.creature;

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
import com.lycanitesmobs.core.entity.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.targeting.DefendEntitiesGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindAttackTargetGoal;
import com.lycanitesmobs.core.info.CreatureManager;


public class EntityMakaAlpha extends AgeableCreatureEntity {
   public EntityMakaAlpha(Level world) {
      super(LycEntities.type(EntityMakaAlpha.class), world);
      this.attribute = EnumCreatureAttribute.UNDEFINED;
      this.hasAttackSound = true;
      this.attackCooldownMax = 10;
      this.setupMob();
   }

   @Override
   protected void initEntityAI() {
      super.initEntityAI();
      this.targetTasks.addTask(this.nextFindTargetIndex++, new FindAttackTargetGoal(this).addTargets(this.getClass()));
      this.targetTasks.addTask(this.nextSpecialTargetIndex++, new DefendEntitiesGoal(this, EntityMaka.class));
      this.tasks.addTask(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setTargetClass(Player.class).setLongMemory(false));
      this.tasks.addTask(this.nextCombatGoalIndex++, new AttackMeleeGoal(this));
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (this.hasAttackTarget()
         && this.getTarget() instanceof EntityMakaAlpha
         && (this.getHealth() / this.getMaxHealth() <= 0.25F || this.getTarget().getHealth() / this.getTarget().getMaxHealth() <= 0.25F)) {
         this.setTarget(null);
      }
   }

   @Override
   public float getBlockPathWeight(int x, int y, int z) {
      BlockState blockState = this.level().getBlockState(LycPos.of(x, y - 1, z));
      Block block = blockState.getBlock();
      if (block != Blocks.AIR) {
         if (Material.of(blockState) == Material.GRASS) {
            return 10.0F;
         }

         if (Material.of(blockState) == Material.GROUND) {
            return 7.0F;
         }
      }

      return super.getBlockPathWeight(x, y, z);
   }

   @Override
   public boolean canBeLeashedTo(Player player) {
      return true;
   }

   @Override
   public boolean canAttackEntity(LivingEntity target) {
      if (target instanceof EntityMaka) {
         return false;
      } else {
         return !(target instanceof EntityMakaAlpha)
               || !(this.getHealth() / this.getMaxHealth() <= 0.25F) && !(target.getHealth() / target.getMaxHealth() <= 0.25F)
            ? super.canAttackEntity(target)
            : false;
      }
   }

   @Override
   public boolean canAttackOwnSpecies() {
      return true;
   }

   public void setTarget(LivingEntity entity) {
      if (entity == null && this.getTarget() instanceof EntityMakaAlpha) {
         this.heal((this.getMaxHealth() - this.getHealth()) / 2.0F);
         this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 2, false, false));
         this.getTarget().heal((this.getMaxHealth() - this.getHealth()) / 2.0F);
         this.getTarget().addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 2, false, false));
      }

      super.setTarget(entity);
   }

   @Override
   public boolean rollAttackTargetChance(LivingEntity target) {
      return !(target instanceof Player) && target.getClass() != this.getClass() ? true : this.getRandom().nextDouble() <= 0.01;
   }

   @Override
   public boolean isProtective(Entity entity) {
      return entity instanceof EntityMaka ? true : super.isProtective(entity);
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
   public float getDamageModifier(DamageSource damageSrc) {
      float damageMod = super.getDamageModifier(damageSrc);
      if (damageSrc.getEntity() instanceof EntityMakaAlpha) {
         damageMod *= 2.0F;
      }

      return damageMod;
   }

   @Override
   public AgeableCreatureEntity createChild(AgeableCreatureEntity partner) {
      return (AgeableCreatureEntity)CreatureManager.getInstance().getCreature("maka").createEntity(this.level());
   }
}
