package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAILookIdle;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAISwimming;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIWatchClosest;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAIDefendHive;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAILookAtTradePlayer;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAIReEnterHive;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAITradePlayer;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAIWanderHiveCenter;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexQueenAIWander;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIAttackMelee;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIHurtByTarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAINearestAttackableTarget;
import com.github.alexthe666.iceandfire.structures.MyrmexNestGen;
import java.util.ArrayList;
import java.util.Random;
import javax.annotation.Nullable;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;

/** Ice and Fire 1.7.1 EntityMyrmexQueen: lays the eggs, digs a nest when she sees the sky. */
public class EntityMyrmexQueen extends EntityMyrmexBase {
   private static final ResourceLocation TEXTURE_DESERT = IafLand.rl("textures/models/myrmex/myrmex_desert_queen.png");
   private static final ResourceLocation TEXTURE_JUNGLE = IafLand.rl("textures/models/myrmex/myrmex_jungle_queen.png");
   public static final Animation ANIMATION_BITE = Animation.create(15);
   public static final Animation ANIMATION_STING = Animation.create(15);
   public static final Animation ANIMATION_EGG = Animation.create(20);
   public static final Animation ANIMATION_DIGNEST = Animation.create(45);
   private static final EntityDataAccessor<Boolean> HASMADEHOME = SynchedEntityData.defineId(EntityMyrmexQueen.class, EntityDataSerializers.BOOLEAN);
   private int eggTicks = 0;
   public static final ResourceKey<LootTable> DESERT_LOOT = ResourceKey.create(Registries.LOOT_TABLE, IafLand.rl("myrmex_queen_desert"));
   public static final ResourceKey<LootTable> JUNGLE_LOOT = ResourceKey.create(Registries.LOOT_TABLE, IafLand.rl("myrmex_queen_jungle"));

   public EntityMyrmexQueen(EntityType<? extends EntityMyrmexQueen> type, Level worldIn) {
      super(type, worldIn);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return createBaseAttributes().add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ATTACK_DAMAGE, 10.0).add(Attributes.MAX_HEALTH, 120.0).add(Attributes.ARMOR, 15.0);
   }

   @Override
   protected ResourceKey<LootTable> getDefaultLootTable() {
      return this.isJungle() ? JUNGLE_LOOT : DESERT_LOOT;
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(HASMADEHOME, true);
   }

   @Override
   public void setCustomName(@Nullable Component name) {
      if (name != null && !name.equals(this.getCustomName()) && this.getHive() != null) {
         this.getHive().colonyName = name.getString();
      }

      super.setCustomName(name);
   }

   @Override
   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("EggTicks", this.eggTicks);
      tag.putBoolean("MadeHome", this.hasMadeHome());
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.eggTicks = tag.getInt("EggTicks");
      this.setMadeHome(tag.getBoolean("MadeHome"));
   }

   public boolean hasMadeHome() {
      return this.entityData.get(HASMADEHOME);
   }

   public void setMadeHome(boolean madeHome) {
      this.entityData.set(HASMADEHOME, madeHome);
   }

   @Override
   public int getCareerCaste() {
      return 4;
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (this.getAnimation() == ANIMATION_DIGNEST) {
         this.spawnGroundEffects(3.0F);
      }

      if (this.getHive() != null) {
         this.getHive().tick(0, this.level());
      }

      if (this.hasMadeHome() && this.getGrowthStage() >= 2 && !this.canSeeSky()) {
         this.eggTicks++;
      } else if (this.canSeeSky()) {
         this.setAnimation(ANIMATION_DIGNEST);
         if (this.getAnimationTick() == 42) {
            int down = Math.max(15, this.blockPosition().getY() - 20 + this.getRandom().nextInt(10));
            BlockPos genPos = BlockPos.containing(this.getX(), (double)down, this.getZ());
            if (!this.level().isClientSide) {
               MyrmexNestGen hiveGen = new MyrmexNestGen(this.isJungle());
               hiveGen.generate(this.level(), new Random(this.getRandom().nextLong()), genPos);
               this.setHive(hiveGen.hive);
            }
            this.setMadeHome(true);
            this.moveTo((double)genPos.getX(), (double)down, (double)genPos.getZ(), 0.0F, 0.0F);
            this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 30));

            for (int i = 0; i < 3; i++) {
               EntityMyrmexWorker worker = IafLandMobs.MYRMEX_WORKER.get().create(this.level());
               worker.copyPosition(this);
               worker.setHive(this.getHive());
               worker.setJungleVariant(this.isJungle());
               if (!this.level().isClientSide) {
                  this.level().addFreshEntity(worker);
               }
            }

            return;
         }
      }

      if (!this.level().isClientSide && this.eggTicks > IceAndFire.CONFIG.myrmexPregnantTicks && this.getHive() == null
         || !this.level().isClientSide && this.getHive() != null && this.getHive().repopulate() && this.eggTicks > IceAndFire.CONFIG.myrmexPregnantTicks) {
         float radius = -5.25F;
         float angle = (float)(Math.PI / 180.0) * this.yBodyRot;
         double extraX = (double)(radius * Mth.sin((float)(Math.PI + (double)angle)));
         double extraZ = (double)(radius * Mth.cos(angle));
         BlockPos eggPos = BlockPos.containing(this.getX() + extraX, this.getY() + 0.75, this.getZ() + extraZ);
         if (this.level().isEmptyBlock(eggPos)) {
            this.setAnimation(ANIMATION_EGG);
            if (this.getAnimationTick() == 10) {
               EntityMyrmexEgg egg = IafLandMobs.MYRMEX_EGG.get().create(this.level());
               egg.setJungle(this.isJungle());
               int caste = getRandomCaste(this.level(), this.getRandom(), this.getHive() == null || this.getHive().reproduces);
               egg.setMyrmexCaste(caste);
               egg.moveTo(this.getX() + extraX, this.getY() + 0.75, this.getZ() + extraZ, 0.0F, 0.0F);
               if (this.getHive() != null) {
                  egg.hiveUUID = this.getHive().hiveUUID;
               }

               if (!this.level().isClientSide) {
                  this.level().addFreshEntity(egg);
               }

               this.eggTicks = 0;
            }
         }
      }

      if (this.getAnimation() == ANIMATION_BITE && this.getTarget() != null && this.getAnimationTick() == 6) {
         this.playBiteSound();
         double dist = this.distanceToSqr(this.getTarget());
         if (dist < 14.0) {
            this.getTarget().hurt(this.damageSources().mobAttack(this), (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE)));
         }
      }

      if (this.getAnimation() == ANIMATION_STING && this.getAnimationTick() == 0) {
         this.playStingSound();
      }

      if (this.getAnimation() == ANIMATION_STING && this.getTarget() != null && this.getAnimationTick() == 6) {
         double dist = this.distanceToSqr(this.getTarget());
         if (dist < 14.0) {
            LivingEntity t = this.getTarget();
            t.hurt(this.damageSources().mobAttack(this), (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2));
            t.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 2));
            t.hasImpulse = true;
            float f = Mth.sqrt(0.5F);
            Vec3 m = t.getDeltaMovement();
            double mx = m.x / 2.0 - 0.5 / (double)f * 4.0;
            double mz = m.z / 2.0 - 0.5 / (double)f * 4.0;
            double my = m.y;
            if (t.onGround()) {
               my /= 2.0;
               my += 4.0;
               if (my > 0.4F) {
                  my = 0.4F;
               }
            }
            t.setDeltaMovement(mx, my, mz);
            t.hurtMarked = true;
         }
      }
   }

   @Override
   public boolean isInvulnerableTo(DamageSource source) {
      return super.isInvulnerableTo(source) || this.getAnimation() == ANIMATION_DIGNEST;
   }

   @Override
   protected void initEntityAI() {
      this.tasks.addTask(0, new EntityAISwimming(this));
      this.tasks.addTask(0, new MyrmexAITradePlayer(this));
      this.tasks.addTask(0, new MyrmexAILookAtTradePlayer(this));
      this.tasks.addTask(1, new EntityAIAttackMelee(this, 1.0, true));
      this.tasks.addTask(3, new MyrmexAIReEnterHive(this, 1.0));
      this.tasks.addTask(4, new MyrmexAIWanderHiveCenter(this, 1.0));
      this.tasks.addTask(5, new MyrmexQueenAIWander(this, 1.0));
      this.tasks.addTask(6, new EntityAIWatchClosest(this, Player.class, 6.0F));
      this.tasks.addTask(7, new EntityAILookIdle(this));
      this.targetTasks.addTask(1, new MyrmexAIDefendHive(this));
      this.targetTasks.addTask(2, new EntityAIHurtByTarget(this, false));
      this.targetTasks.addTask(3, new MyrmexAIAttackPlayers(this));
      this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, Mob.class, 10, true, true,
         entity -> entity != null && notVisibleMob(entity) && !EntityMyrmexBase.haveSameHive(this, entity) && LandUtils.isAlive(entity)));
   }

   @Override
   public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource source) {
      return false;
   }

   @Override
   public boolean shouldMoveThroughHive() {
      return false;
   }

   @Override
   public ResourceLocation getAdultTexture() {
      return this.isJungle() ? TEXTURE_JUNGLE : TEXTURE_DESERT;
   }

   @Override
   public float getModelScale() {
      return 1.75F;
   }

   @Override
   public int getCasteImportance() {
      return 3;
   }

   @Override
   public boolean shouldLeaveHive() {
      return false;
   }

   @Override
   public boolean shouldEnterHive() {
      return true;
   }

   @Override
   public boolean doHurtTarget(Entity entityIn) {
      if (this.getGrowthStage() < 2) {
         return false;
      } else if (this.getAnimation() != ANIMATION_STING && this.getAnimation() != ANIMATION_BITE) {
         this.setAnimation(this.getRandom().nextBoolean() ? ANIMATION_STING : ANIMATION_BITE);
         if (!this.level().isClientSide && this.getRandom().nextInt(3) == 0 && !this.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
            this.spawnAtLocation(this.getItemInHand(InteractionHand.MAIN_HAND), 0.0F);
            this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
         }

         if (!this.getPassengers().isEmpty()) {
            for (Entity entity : new ArrayList<>(this.getPassengers())) {
               entity.stopRiding();
            }
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean canMove() {
      return super.canMove() && this.hasMadeHome();
   }

   public void spawnGroundEffects(float size) {
      for (int i = 0; (float)i < size * 3.0F; i++) {
         for (int i1 = 0; i1 < 10; i1++) {
            double motionX = this.getRandom().nextGaussian() * 0.07;
            double motionY = this.getRandom().nextGaussian() * 0.07;
            double motionZ = this.getRandom().nextGaussian() * 0.07;
            float radius = size * this.random.nextFloat();
            float angle = (float)(Math.PI / 180.0) * this.yBodyRot * 3.14F * this.random.nextFloat();
            double extraX = (double)(radius * Mth.sin((float)(Math.PI + (double)angle)));
            double extraY = 0.8F;
            double extraZ = (double)(radius * Mth.cos(angle));
            BlockState iblockstate = this.level().getBlockState(new BlockPos(Mth.floor(this.getX() + extraX), Mth.floor(this.getY() + extraY) - 1, Mth.floor(this.getZ() + extraZ)));
            if (!iblockstate.isAir() && this.level().isClientSide) {
               this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, iblockstate), true, this.getX() + extraX, this.getY() + extraY, this.getZ() + extraZ, motionX, motionY, motionZ);
            }
         }
      }
   }

   @Override
   public Animation[] getAnimations() {
      return new Animation[]{ANIMATION_PUPA_WIGGLE, ANIMATION_BITE, ANIMATION_STING, ANIMATION_EGG, ANIMATION_DIGNEST};
   }
}
