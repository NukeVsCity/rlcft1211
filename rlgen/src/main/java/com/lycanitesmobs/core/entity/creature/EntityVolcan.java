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
import java.util.HashMap;


public class EntityVolcan extends TameableCreatureEntity implements Enemy {
   public int blockMeltingRadius = 2;

   public EntityVolcan(Level world) {
      super(world);
      this.attribute = EnumCreatureAttribute.UNDEFINED;
      this.hasAttackSound = true;
      this.setupMob();
      this.setStepHeightCompat(1.0F);
   }

   @Override
   protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(2, new AttackMeleeGoal(this).setLongMemory(true));
   }

   @Override
   public void loadCreatureFlags() {
      this.blockMeltingRadius = this.creatureInfo.getFlag("blockMeltingRadius", this.blockMeltingRadius);
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (!this.level().isClientSide && this.updateTick % 40L == 0L) {
         for (Object entityObj : this.getNearbyEntities(LivingEntity.class, null, 4.0)) {
            LivingEntity target = (LivingEntity)entityObj;
            if (target != this && this.canAttackClass(target.getClass()) && this.canAttackEntity(target) && this.getSensing().hasLineOfSight(target)) {
               target.igniteForSeconds(2);
            }
         }
      }

      if (!this.level().isClientSide
         && this.updateTick % 40L == 0L
         && this.blockMeltingRadius > 0
         && !this.isTamed()
         && this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
         int range = this.blockMeltingRadius;

         for (int w = -((int)Math.ceil((double)this.getBbWidth()) + range); (double)w <= Math.ceil((double)this.getBbWidth()) + (double)range; w++) {
            for (int d = -((int)Math.ceil((double)this.getBbWidth()) + range); (double)d <= Math.ceil((double)this.getBbWidth()) + (double)range; d++) {
               for (int h = -((int)Math.ceil((double)this.getBbHeight()) + range); (double)h <= Math.ceil((double)this.getBbHeight()); h++) {
                  Block block = this.level().getBlockState(this.blockPosition().offset(w, h, d)).getBlock();
                  if (block == Blocks.COBBLESTONE || block == Blocks.GRAVEL) {
                     BlockState blockState = Blocks.LAVA.getStateFromMeta(11);
                     this.level().setBlock(this.blockPosition().offset(w, h, d), blockState, 3);
                  }
               }
            }
         }
      }

      if (this.level().isClientSide) {
         for (int i = 0; i < 2; i++) {
            this.level()
               .addParticle(ParticleTypes.FLAME, this.getX() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), this.getY() + this.random.nextDouble() * (double)this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), 0.0, 0.0, 0.0);
            this.level()
               .addParticle(ParticleTypes.DRIPPING_LAVA, this.getX() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), this.getY() + this.random.nextDouble() * (double)this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), 0.0, 0.0, 0.0);
         }

         if (this.tickCount % 10 == 0) {
            for (int i = 0; i < 2; i++) {
               this.level()
                  .addParticle(ParticleTypes.FLAME, this.getX() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), this.getY() + this.random.nextDouble() * (double)this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), 0.0, 0.0, 0.0);
            }
         }
      }
   }

   @Override
   public boolean attackMelee(Entity target, double damageScale) {
      if (!super.attackMelee(target, damageScale)) {
         return false;
      } else {
         if (target instanceof Silverfish) {
            target.discard();
         }

         return true;
      }
   }

   @Override
   public boolean isFlying() {
      return true;
   }

   @Override
   public HashMap<Integer, String> getInteractCommands(Player player, InteractionHand hand, ItemStack itemStack) {
      HashMap<Integer, String> commands = new HashMap<>();
      commands.putAll(super.getInteractCommands(player, hand, itemStack));
      if (itemStack != null && itemStack.getItem() == Items.BUCKET && this.isTamed()) {
         commands.put(BaseCreatureEntity.COMMAND_PIORITIES.ITEM_USE.id, "Water");
      }

      return commands;
   }

   @Override
   public boolean performCommand(String command, Player player, InteractionHand hand, ItemStack itemStack) {
      if (command.equals("Water")) {
         this.replacePlayersItem(player, hand, itemStack, new ItemStack(Items.LAVA_BUCKET));
         return true;
      } else {
         return super.performCommand(command, player, hand, itemStack);
      }
   }

   @Override
   public boolean petControlsEnabled() {
      return true;
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
      return damageSrc.is(DamageTypeTags.IS_FIRE) ? 0.0F : super.getDamageModifier(damageSrc);
   }

   @Override
   public boolean isDamageTypeApplicable(String type, DamageSource source, float damage) {
      return !type.equals("cactus") && !type.equals("inWall") ? super.isDamageTypeApplicable(type, source, damage) : false;
   }

   @Override
   public boolean canBurn() {
      return false;
   }

   @Override
   public boolean waterDamage() {
      return true;
   }
}
