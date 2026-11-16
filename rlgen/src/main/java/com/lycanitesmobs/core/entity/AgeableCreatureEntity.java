package com.lycanitesmobs.core.entity;

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
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.entity.goals.actions.FollowParentGoal;
import com.lycanitesmobs.core.entity.goals.actions.MateGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindParentGoal;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.ItemDrop;
import com.lycanitesmobs.core.info.Variant;
import com.lycanitesmobs.core.item.ItemCustomSpawnEgg;
import java.util.HashMap;


public abstract class AgeableCreatureEntity extends BaseCreatureEntity {
   private float scaledWidth = -1.0F;
   private float scaledHeight;
   private AgeableCreatureEntity breedingTarget;
   public int growthTime = -24000;
   public boolean canGrow = true;
   public double babySpawnChance = 0.0;
   public int loveTime;
   private int loveTimeMax = 600;
   public int breedingCooldown = 6000;
   public boolean hasBeenFarmed = false;
   protected static final EntityDataAccessor<Integer> AGE = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Integer> LOVE = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.INT);

   public AgeableCreatureEntity(Level world) {
      super(world);
   }

   @Override
   public void setupMob() {
      if (this.babySpawnChance > 0.0 && this.random.nextDouble() < this.babySpawnChance) {
         this.setAge(this.growthTime);
      }

      super.setupMob();
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(AGE, 0);
      builder.define(LOVE, 0);
   }

   @Override
   protected void initEntityAI() {
      this.tasks.addTask(this.nextTravelGoalIndex++, new MateGoal(this).setMateDistance(5.0));
      super.initEntityAI();
      this.targetTasks.addTask(this.nextFindTargetIndex++, new FindParentGoal(this).setSightCheck(false).setDistance(32.0));
      this.tasks.addTask(this.nextTravelGoalIndex++, new FollowParentGoal(this).setSpeed(1.0).setStrayDistance(3.0));
   }

   @Override
   public String getAgeName() {
      return this.isBaby() ? LanguageManager.translate("entity.baby") : "";
   }

   @Override
   public boolean isPersistant() {
      return this.hasBeenFarmed ? true : super.isPersistant();
   }

   public void setFarmed() {
      this.hasBeenFarmed = true;
      if (this.timeUntilPortal > this.getPortalCooldown()) {
         this.timeUntilPortal = this.getPortalCooldown();
      }
   }

   @Override
   public void getRandomSubspecies() {
      if (!this.isBaby()) {
         super.getRandomSubspecies();
      }
   }

   @Override
   public void getRandomVariant() {
      if (!this.isBaby()) {
         super.getRandomVariant();
      }
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (this.level().isClientSide) {
         this.setScaleForAge(this.isBaby());
      } else if (this.canGrow) {
         int age = this.getAge();
         if (age < 0) {
            this.setAge(++age);
         } else if (age > 0) {
            this.setAge(--age);
         }
      }

      if (!this.canBreed()) {
         this.loveTime = 0;
      }

      if (!this.level().isClientSide) {
         this.entityData.set(LOVE, this.loveTime);
      }

      if (this.level().isClientSide) {
         this.loveTime = this.getIntFromDataManager(LOVE);
      }

      if (this.isInLove()) {
         this.setFarmed();
         this.loveTime--;
         if (this.level().isClientSide) {
            EnumParticleTypes particle = ParticleTypes.HEART;
            if (this.loveTime % 10 == 0) {
               double d0 = this.random.nextGaussian() * 0.02;
               double d1 = this.random.nextGaussian() * 0.02;
               double d2 = this.random.nextGaussian() * 0.02;
               this.level()
                  .addParticle(particle, this.getX() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(), this.getY() + 0.5 + (double)(this.random.nextFloat() * this.getBbHeight()), this.getZ() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(), d0, d1, d2);
            }
         }
      }
   }

   @Override
   protected void customServerAiStep() {
      if (!this.canBreed()) {
         this.loveTime = 0;
      }

      super.customServerAiStep();
   }

   @Override
   public boolean canDropItem(ItemDrop itemDrop) {
      return itemDrop.adultOnly && this.isBaby() ? false : super.canDropItem(itemDrop);
   }

   @Override
   public HashMap<Integer, String> getInteractCommands(Player player, InteractionHand hand, ItemStack itemStack) {
      HashMap<Integer, String> commands = new HashMap<>();
      commands.putAll(super.getInteractCommands(player, hand, itemStack));
      if (itemStack != null) {
         if (itemStack.getItem() == this.creatureInfo.creatureType.getSpawnEgg()) {
            commands.put(BaseCreatureEntity.COMMAND_PIORITIES.ITEM_USE.id, "Spawn Baby");
         }

         if (this.isBreedingItem(itemStack) && this.canBreed() && !this.isInLove()) {
            commands.put(BaseCreatureEntity.COMMAND_PIORITIES.ITEM_USE.id, "Breed");
         }
      }

      return commands;
   }

   @Override
   public boolean performCommand(String command, Player player, InteractionHand hand, ItemStack itemStack) {
      if (command.equals("Spawn Baby") && !this.level().isClientSide) {
         ItemCustomSpawnEgg itemCustomSpawnEgg = (ItemCustomSpawnEgg)itemStack.getItem();
         CreatureInfo spawnEggCreatureInfo = itemCustomSpawnEgg.getCreatureInfo(itemStack);
         if (spawnEggCreatureInfo != null && spawnEggCreatureInfo.entityClass != null && spawnEggCreatureInfo.entityClass.isAssignableFrom(this.getClass())) {
            AgeableCreatureEntity baby = this.createChild(this);
            if (baby != null) {
               baby.setAge(baby.growthTime);
               baby.moveTo(this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F);
               baby.setFarmed();
               this.level().addFreshEntity(baby);
               if (itemStack.hasDisplayName()) {
                  baby.setCustomNameTag(itemStack.getHoverName());
               }

               this.consumePlayersItem(player, hand, itemStack);
            }
         }

         return true;
      } else if (command.equals("Breed") && this.breed()) {
         this.consumePlayersItem(player, hand, itemStack);
         return true;
      } else {
         return super.performCommand(command, player, hand, itemStack);
      }
   }

   public int getGrowingAge() {
      return this.getIntFromDataManager(AGE);
   }

   public void setGrowingAge(int age) {
      this.entityData.set(AGE, age);
      this.setScaleForAge(this.isBaby());
   }

   public void addGrowth(int growth) {
      int age = this.getAge();
      age += growth * 20;
      if (age > 0) {
         age = 0;
      }

      this.setAge(age);
   }

   public boolean isBaby() {
      return this.getAge() < 0;
   }

   public boolean shouldFollowParent() {
      return this.isBaby();
   }

   public boolean shouldFindParent() {
      return this.isBaby();
   }

   public void setScaleForAge(boolean age) {
      this.setAgeScale(age ? 0.5F : 1.0F);
   }

   protected final void setAgeScale(float age) {
      super.setSize(this.scaledWidth * age, this.scaledHeight * age);
   }

   @Override
   protected void setSize(float width, float height) {
      boolean validWidth = this.scaledWidth > 0.0F;
      this.scaledWidth = width;
      this.scaledHeight = height;
      if (!validWidth) {
         this.setAgeScale(1.0F);
      }

      super.setSize(width, height);
   }

   @Override
   public void updateSize() {
      this.setSize(this.setWidth, this.setHeight);
      this.setScaleForAge(this.isBaby());
   }

   @Override
   public boolean canBeTempted() {
      return !this.isInLove() && super.canBeTempted();
   }

   public AgeableCreatureEntity getBreedingTarget() {
      return this.breedingTarget;
   }

   public void setBreedingTarget(AgeableCreatureEntity target) {
      this.breedingTarget = target;
   }

   public AgeableCreatureEntity createChild(AgeableCreatureEntity partener) {
      return (AgeableCreatureEntity)this.creatureInfo.createEntity(this.level());
   }

   public boolean isBreedingItem(ItemStack itemStack) {
      return this.creatureInfo.isFarmable() && this.getAirSupply() > -100 ? this.creatureInfo.canEat(itemStack) : false;
   }

   public boolean canBreedWith(AgeableCreatureEntity partner) {
      if (partner == this) {
         return false;
      } else if (partner.getClass() != this.getClass()) {
         return false;
      } else {
         return this.getSubspecies() != partner.getSubspecies() ? false : this.isInLove() && partner.isInLove();
      }
   }

   public boolean isInLove() {
      return this.loveTime > 0;
   }

   public boolean canMate() {
      return this.isInLove();
   }

   public boolean breed() {
      if (!this.canBreed()) {
         return false;
      } else {
         this.loveTime = this.loveTimeMax;
         return true;
      }
   }

   public boolean canBreed() {
      return this.getAge() == 0;
   }

   public void procreate(AgeableCreatureEntity partner) {
      AgeableCreatureEntity baby = this.createChild(partner);
      if (baby != null) {
         this.finishBreeding();
         partner.finishBreeding();
         baby.setAge(baby.growthTime);
         baby.setSubspecies(this.getSubspeciesIndex());
         Variant babyVariant = this.getSubspecies().getChildVariant(this, this.getVariant(), partner.getVariant());
         baby.applyVariant(babyVariant != null ? babyVariant.index : 0);
         baby.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());

         for (int i = 0; i < 7; i++) {
            double d0 = this.random.nextGaussian() * 0.02;
            double d1 = this.random.nextGaussian() * 0.02;
            double d2 = this.random.nextGaussian() * 0.02;
            this.level()
               .addParticle(ParticleTypes.HEART, this.getX() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(), this.getY() + 0.5 + (double)(this.random.nextFloat() * this.getBbHeight()), this.getZ() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(), d0, d1, d2);
         }

         this.onCreateBaby(partner, baby);
         this.level().addFreshEntity(baby);

         for (Player player : this.level().getPlayers(Player.class, playerx -> playerx != null && this.distanceTo(playerx) <= 5.0F)) {
            ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(player);
            if (extendedPlayer != null) {
               extendedPlayer.studyCreature(this, CreatureManager.getInstance().config.creatureBreedKnowledge, false, true);
            }
         }
      }
   }

   public void onCreateBaby(AgeableCreatureEntity partner, AgeableCreatureEntity baby) {
   }

   public void finishBreeding() {
      this.setAge(this.breedingCooldown);
      this.setBreedingTarget(null);
      this.loveTime = 0;
   }

   @Override
   public void readAdditionalSaveData(CompoundTag nbtTagCompound) {
      super.readAdditionalSaveData(nbtTagCompound);
      if (nbtTagCompound.contains("Age")) {
         this.setAge(nbtTagCompound.getInt("Age"));
      } else {
         this.setAge(0);
      }

      if (nbtTagCompound.contains("InLove")) {
         this.loveTime = nbtTagCompound.getInt("InLove");
      } else {
         this.loveTime = 0;
      }

      if (nbtTagCompound.contains("HasBeenFarmed") && nbtTagCompound.getBoolean("HasBeenFarmed")) {
         this.setFarmed();
      }
   }

   @Override
   public void addAdditionalSaveData(CompoundTag nbtTagCompound) {
      super.addAdditionalSaveData(nbtTagCompound);
      nbtTagCompound.putInt("Age", this.getAge());
      nbtTagCompound.putInt("InLove", this.loveTime);
      nbtTagCompound.putBoolean("HasBeenFarmed", this.hasBeenFarmed);
   }
}
