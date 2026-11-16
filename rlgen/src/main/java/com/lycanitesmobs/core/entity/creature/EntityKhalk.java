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
import com.lycanitesmobs.api.IGroupHeavy;
import com.lycanitesmobs.core.entity.EntityItemCustom;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;


public class EntityKhalk extends TameableCreatureEntity implements Enemy, IGroupHeavy {
   public boolean lavaDeath = true;

   public EntityKhalk(Level world) {
      super(LycEntities.type(EntityKhalk.class), world);
      this.attribute = EnumCreatureAttribute.UNDEFINED;
      this.spawnsOnLand = true;
      this.spawnsInWater = true;
      this.isLavaCreature = true;
      this.hasAttackSound = true;
      this.canGrow = true;
      this.babySpawnChance = 0.01;
      this.solidCollision = true;
      this.setupMob();
      this.setPathfindingMalus(PathType.LAVA, 0.0F);
   }

   @Override
   protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(this.nextCombatGoalIndex++, new AttackMeleeGoal(this));
   }

   @Override
   public void loadCreatureFlags() {
      this.lavaDeath = this.creatureInfo.getFlag("lavaDeath", this.lavaDeath);
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (this.onGround() && !this.level().isClientSide && this.hasAttackTarget() && this.random.nextInt(10) == 0) {
         this.leap(6.0F, 0.1, this.getTarget());
      }
   }

   @Override
   public float getAISpeedModifier() {
      return this.lavaContact() ? 2.0F : 1.0F;
   }

   @Override
   public float getBlockPathWeight(int x, int y, int z) {
      int waterWeight = 10;
      BlockPos pos = LycPos.of(x, y, z);
      if (this.level().getBlockState(pos).getBlock() == Blocks.LAVA) {
         return (super.getBlockPathWeight(x, y, z) + 1.0F) * (float)(waterWeight + 1);
      } else if (this.getTarget() != null) {
         return super.getBlockPathWeight(x, y, z);
      } else {
         return this.lavaContact() ? -999999.0F : super.getBlockPathWeight(x, y, z);
      }
   }

   @Override
   public boolean isPushedByFluid() {
      return false;
   }

   @Override
   public void die(DamageSource damageSource) {
      if (!this.level().isClientSide && this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && this.lavaDeath && !this.isTamed()) {
         int lavaWidth = (int)Math.floor((double)this.getBbWidth()) - 1;
         int lavaHeight = (int)Math.floor((double)this.getBbHeight()) - 1;

         for (int x = (int)this.getX() - lavaWidth; x <= (int)this.getX() + lavaWidth; x++) {
            for (int y = (int)this.getY(); y <= (int)this.getY() + lavaHeight; y++) {
               for (int z = (int)this.getZ() - lavaWidth; z <= (int)this.getZ() + lavaWidth; z++) {
                  Block block = this.level().getBlockState(LycPos.of(x, y, z)).getBlock();
                  if (block == Blocks.AIR) {
                     BlockState blockState = Blocks.LAVA.defaultBlockState().setValue(LiquidBlock.LEVEL, 11);
                     if (x == (int)this.getX() && y == (int)this.getY() && z == (int)this.getZ()) {
                        blockState = Blocks.LAVA.defaultBlockState().setValue(LiquidBlock.LEVEL, 12);
                     }

                     this.level().setBlock(LycPos.of(x, y, z), blockState, 3);
                  }
               }
            }
         }
      }

      super.die(damageSource);
   }

   @Override
   public boolean canBurn() {
      return false;
   }

   @Override
   public boolean waterDamage() {
      return true;
   }

   @Override
   public boolean canBreatheUnderlava() {
      return true;
   }

   @Override
   public boolean canBreatheAir() {
      return true;
   }

   @Override
   public float getDamageModifier(DamageSource damageSrc) {
      return damageSrc.is(DamageTypeTags.IS_FIRE) ? 0.0F : super.getDamageModifier(damageSrc);
   }

   @Override
   public void applyDropEffects(EntityItemCustom entityitem) {
      entityitem.setCanBurn(false);
   }

   public float getBrightness() {
      return 1.0F;
   }

   public int getBrightnessForRender() {
      return 15728880;
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
   public boolean petControlsEnabled() {
      return true;
   }
}
