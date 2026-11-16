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
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.block.BlockFireBase;
import com.lycanitesmobs.core.entity.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.BreakDoorGoal;
import com.lycanitesmobs.core.entity.goals.actions.MoveVillageGoal;


public class EntityGeist extends AgeableCreatureEntity implements Enemy {
   public boolean shadowfireDeath = true;

   public EntityGeist(Level world) {
      super(LycEntities.type(EntityGeist.class), world);
      this.attribute = EnumCreatureAttribute.UNDEAD;
      this.hasAttackSound = true;
      this.spreadFire = true;
      this.canGrow = false;
      this.babySpawnChance = 0.01;
      this.setupMob();
   }

   @Override
   protected void initEntityAI() {
      this.tasks.addTask(this.nextTravelGoalIndex++, new MoveVillageGoal(this));
      super.initEntityAI();
      this.tasks.addTask(this.nextDistractionGoalIndex++, new BreakDoorGoal(this));
      this.tasks.addTask(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setTargetClass(Player.class).setLongMemory(false));
      this.tasks.addTask(this.nextCombatGoalIndex++, new AttackMeleeGoal(this));
      if (this.getNavigation() instanceof GroundPathNavigation) {
         GroundPathNavigation pathNavigateGround = (GroundPathNavigation)this.getNavigation();
         pathNavigateGround.setCanOpenDoors(true);
         pathNavigateGround.setAvoidSun(true);
      }
   }

   @Override
   public void loadCreatureFlags() {
      this.shadowfireDeath = this.creatureInfo.getFlag("shadowfireDeath", this.shadowfireDeath);
   }

   public boolean killedEntity(ServerLevel lycLevel, LivingEntity entityLivingBase) {
      super.onKillEntity(entityLivingBase);
      if (this.level().getDifficulty().getId() >= 2 && entityLivingBase instanceof Villager) {
         if (this.level().getDifficulty().getId() == 2 && this.random.nextBoolean()) {
            return;
         }

         Villager entityvillager = (Villager)entityLivingBase;
         ZombieVillager entityzombievillager = new ZombieVillager(this.level());
         entityzombievillager.copyPosition(entityvillager);
         this.level().removeEntity(entityvillager);
         entityzombievillager.onInitialSpawn(this.level().getCurrentDifficultyAt(LycPos.of(entityzombievillager)), new BaseCreatureEntity.GroupData(false));
         entityzombievillager.setProfession(entityvillager.getProfession());
         entityzombievillager.setChild(entityvillager.isBaby());
         entityzombievillager.setNoAi(entityvillager.isNoAi());
         if (entityvillager.hasCustomName()) {
            entityzombievillager.setCustomName(Component.literal(entityvillager.getCustomName().getString()));
            entityzombievillager.setCustomNameVisible(entityvillager.getAlwaysRenderNameTag());
         }

         this.level().addFreshEntity(entityzombievillager);
         this.level().playEvent(null, 1016, entityzombievillager.blockPosition(), 0);
      }
   }

   @Override
   public void die(DamageSource damageSource) {
      try {
         int shadowfireWidth = (int)Math.floor((double)this.getBbWidth()) + 1;
         int shadowfireHeight = (int)Math.floor((double)this.getBbHeight()) + 1;
         boolean permanent = false;
         if (damageSource.getEntity() == this) {
            permanent = true;
            shadowfireWidth *= 5;
         }

         if (!this.level().isClientSide && (permanent || this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && this.shadowfireDeath)) {
            for (int x = (int)this.getX() - shadowfireWidth; x <= (int)this.getX() + shadowfireWidth; x++) {
               for (int y = (int)this.getY() - shadowfireHeight; y <= (int)this.getY() + shadowfireHeight; y++) {
                  for (int z = (int)this.getZ() - shadowfireWidth; z <= (int)this.getZ() + shadowfireWidth; z++) {
                     Block block = this.level().getBlockState(LycPos.of(x, y, z)).getBlock();
                     if (block != Blocks.AIR && block != ObjectManager.getBlock("shadowfire")) {
                        BlockPos placePos = LycPos.of(x, y + 1, z);
                        Block upperBlock = this.level().getBlockState(placePos).getBlock();
                        if (upperBlock == Blocks.AIR) {
                           this.level()
                              .setBlock(placePos, ObjectManager.getBlock("shadowfire").defaultBlockState().setValue(BlockFireBase.PERMANENT, permanent), 3);
                        }
                     }
                  }
               }
            }
         }
      } catch (Exception var11) {
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

   @Override
   public boolean daylightBurns() {
      return !this.isBaby() && !this.isMinion();
   }
}
