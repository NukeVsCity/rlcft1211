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
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.GetBlockGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.GetItemGoal;
import com.lycanitesmobs.core.info.ObjectLists;
import java.util.ArrayList;


public class EntityKobold extends TameableCreatureEntity implements Enemy {
   public boolean griefing = true;
   public boolean theivery = true;
   private int torchLootingTime = 20;

   public EntityKobold(Level world) {
      super(LycEntities.type(EntityKobold.class), world);
      this.attribute = EnumCreatureAttribute.UNDEFINED;
      this.hasAttackSound = true;
      this.spreadFire = false;
      this.canGrow = false;
      this.babySpawnChance = 0.1;
      this.setupMob();
   }

   @Override
   protected void initEntityAI() {
      this.tasks.addTask(this.nextIdleGoalIndex++, new GetItemGoal(this).setDistanceMax(8.0).setSpeed(1.2));
      if (this.griefing) {
         this.tasks
            .addTask(this.nextIdleGoalIndex++, new GetBlockGoal(this).setDistanceMax(8).setSpeed(1.2).setBlockName("torch").setTamedLooting(false));
      }

      super.initEntityAI();
      this.tasks.addTask(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setTargetClass(Player.class).setLongMemory(false));
      this.tasks.addTask(this.nextCombatGoalIndex++, new AttackMeleeGoal(this));
   }

   @Override
   public void loadCreatureFlags() {
      this.griefing = this.creatureInfo.getFlag("griefing", this.griefing);
      this.theivery = this.creatureInfo.getFlag("theivery", this.theivery);
   }

   protected void despawnEntity() {
      super.despawnEntity();
      if (this.isRemoved() && !this.isTamed() && this.inventory.hasBagItems()) {
         this.inventory.dropInventory();
      }
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (!this.isTamed() && this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && this.griefing && this.torchLootingTime-- <= 0) {
         this.torchLootingTime = 60;
         int distance = 2;
         String targetName = "torch";
         new ArrayList();

         for (int x = (int)this.getX() - distance; x < (int)this.getX() + distance; x++) {
            for (int y = (int)this.getY() - distance; y < (int)this.getY() + distance; y++) {
               for (int z = (int)this.getZ() - distance; z < (int)this.getZ() + distance; z++) {
                  BlockPos pos = LycPos.of(x, y, z);
                  Block searchBlock = this.level().getBlockState(pos).getBlock();
                  if (searchBlock != Blocks.AIR) {
                     BlockPos possibleTarget = null;
                     if (ObjectLists.isName(searchBlock, targetName)) {
                        this.level().destroyBlock(pos, true);
                        break;
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public boolean shouldCreatureGroupRevenge(LivingEntity target) {
      return target instanceof Player && target.getHealth() / target.getMaxHealth() <= 0.5F ? true : super.shouldCreatureGroupRevenge(target);
   }

   @Override
   public boolean shouldCreatureGroupHunt(LivingEntity target) {
      return target instanceof Player && target.getHealth() / target.getMaxHealth() <= 0.5F ? true : super.shouldCreatureGroupHunt(target);
   }

   @Override
   public boolean shouldCreatureGroupFlee(LivingEntity target) {
      return target instanceof Player && target.getHealth() / target.getMaxHealth() <= 0.5F ? false : super.shouldCreatureGroupFlee(target);
   }

   @Override
   public boolean canAttackEntity(LivingEntity targetEntity) {
      return !this.isTamed() && targetEntity.getHealth() / targetEntity.getMaxHealth() > 0.5F ? false : super.canAttackEntity(targetEntity);
   }

   @Override
   public boolean canPickupItems() {
      return this.theivery;
   }
}
