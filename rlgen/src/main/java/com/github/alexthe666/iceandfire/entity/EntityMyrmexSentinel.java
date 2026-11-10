package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAILookIdle;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAISwimming;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIWatchClosest;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAIDefendHive;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAIFindHidingSpot;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAILeaveHive;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAILookAtTradePlayer;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAIMoveThroughHive;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAIReEnterHive;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAITradePlayer;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAIWander;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIAttackMelee;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIHurtByTarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAINearestAttackableTarget;
import java.util.ArrayList;
import javax.annotation.Nullable;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;

/** Ice and Fire 1.7.1 EntityMyrmexSentinel: hides on the surface, grabs and nibbles its prey. */
public class EntityMyrmexSentinel extends EntityMyrmexBase {
   private static final ResourceLocation TEXTURE_DESERT = IafLand.rl("textures/models/myrmex/myrmex_desert_sentinel.png");
   private static final ResourceLocation TEXTURE_JUNGLE = IafLand.rl("textures/models/myrmex/myrmex_jungle_sentinel.png");
   private static final ResourceLocation TEXTURE_DESERT_HIDDEN = IafLand.rl("textures/models/myrmex/myrmex_desert_sentinel_hidden.png");
   private static final ResourceLocation TEXTURE_JUNGLE_HIDDEN = IafLand.rl("textures/models/myrmex/myrmex_jungle_sentinel_hidden.png");
   public static final Animation ANIMATION_GRAB = Animation.create(15);
   public static final Animation ANIMATION_NIBBLE = Animation.create(10);
   public static final Animation ANIMATION_STING = Animation.create(25);
   public static final Animation ANIMATION_SLASH = Animation.create(25);
   private static final EntityDataAccessor<Boolean> HIDING = SynchedEntityData.defineId(EntityMyrmexSentinel.class, EntityDataSerializers.BOOLEAN);
   public float holdingProgress;
   public float hidingProgress;
   public int visibleTicks = 0;
   public int daylightTicks = 0;
   public static final ResourceKey<LootTable> DESERT_LOOT = ResourceKey.create(Registries.LOOT_TABLE, IafLand.rl("myrmex_sentinel_desert"));
   public static final ResourceKey<LootTable> JUNGLE_LOOT = ResourceKey.create(Registries.LOOT_TABLE, IafLand.rl("myrmex_sentinel_jungle"));

   public EntityMyrmexSentinel(EntityType<? extends EntityMyrmexSentinel> type, Level worldIn) {
      super(type, worldIn);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return createBaseAttributes().add(Attributes.MOVEMENT_SPEED, 0.35).add(Attributes.ATTACK_DAMAGE, 9.0).add(Attributes.MAX_HEALTH, 60.0).add(Attributes.ARMOR, 12.0);
   }

   @Override
   protected ResourceKey<LootTable> getDefaultLootTable() {
      return this.isJungle() ? JUNGLE_LOOT : DESERT_LOOT;
   }

   @Nullable
   public Entity getHeldEntity() {
      return this.getPassengers().isEmpty() ? null : this.getPassengers().get(0);
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (this.visibleTicks > 0) {
         this.visibleTicks--;
      } else {
         this.visibleTicks = 0;
      }

      if (this.canSeeSky()) {
         this.daylightTicks++;
      } else {
         this.daylightTicks = 0;
      }

      boolean holding = this.getHeldEntity() != null;
      boolean hiding = this.isHiding() && !this.isTrading();
      if (holding || this.isOnResin() || this.getTarget() != null || this.visibleTicks > 0) {
         this.setHiding(false);
      }

      if (holding && this.holdingProgress < 20.0F) {
         this.holdingProgress++;
      } else if (!holding && this.holdingProgress > 0.0F) {
         this.holdingProgress--;
      }

      if (hiding) {
         this.setYRot(this.yRotO);
      }

      if (hiding && this.hidingProgress < 20.0F) {
         this.hidingProgress++;
      } else if (!hiding && this.hidingProgress > 0.0F) {
         this.hidingProgress--;
      }

      if (this.getHeldEntity() != null) {
         this.setAnimation(ANIMATION_NIBBLE);
         if (this.getAnimationTick() == 5) {
            this.playBiteSound();
            this.getHeldEntity().hurt(this.damageSources().mobAttack(this), (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE) / 6));
         }
      }

      if (this.getAnimation() == ANIMATION_GRAB && this.getTarget() != null && this.getAnimationTick() == 7) {
         this.playStingSound();
         double dist = this.distanceToSqr(this.getTarget());
         if (dist < 16.0) {
            this.getTarget().hurt(this.damageSources().mobAttack(this), (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE) / 2));
            this.getTarget().startRiding(this);
         }
      }

      if (this.getAnimation() == ANIMATION_SLASH && this.getTarget() != null && this.getAnimationTick() % 5 == 0 && this.getAnimationTick() <= 20) {
         this.playBiteSound();
         double dist = this.distanceToSqr(this.getTarget());
         if (dist < 16.0) {
            this.getTarget().hurt(this.damageSources().mobAttack(this), (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE) / 4));
         }
      }

      if (this.getAnimation() == ANIMATION_STING && (this.getAnimationTick() == 0 || this.getAnimationTick() == 10)) {
         this.playStingSound();
      }

      if (this.getAnimation() == ANIMATION_STING && this.getTarget() != null && (this.getAnimationTick() == 6 || this.getAnimationTick() == 16)) {
         double dist = this.distanceToSqr(this.getTarget());
         if (dist < 18.0) {
            this.getTarget().hurt(this.damageSources().mobAttack(this), (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE)));
            this.getTarget().addEffect(new MobEffectInstance(MobEffects.POISON, 100, 3));
         }
      }
   }

   @Override
   protected void initEntityAI() {
      this.tasks.addTask(0, new EntityAISwimming(this));
      this.tasks.addTask(0, new MyrmexAIFindHidingSpot(this));
      this.tasks.addTask(0, new MyrmexAITradePlayer(this));
      this.tasks.addTask(0, new MyrmexAILookAtTradePlayer(this));
      this.tasks.addTask(1, new EntityAIAttackMelee(this, 1.0, true));
      this.tasks.addTask(3, new MyrmexAILeaveHive(this, 1.0));
      this.tasks.addTask(3, new MyrmexAIReEnterHive(this, 1.0));
      this.tasks.addTask(4, new MyrmexAIMoveThroughHive(this, 1.0));
      this.tasks.addTask(5, new MyrmexAIWander(this, 1.0));
      this.tasks.addTask(6, new EntityAIWatchClosest(this, Player.class, 6.0F));
      this.tasks.addTask(7, new EntityAILookIdle(this));
      this.targetTasks.addTask(1, new MyrmexAIDefendHive(this));
      this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, false));
      this.targetTasks.addTask(4, new MyrmexAIAttackPlayers(this));
      this.targetTasks.addTask(4, new EntityAINearestAttackableTarget<>(this, Mob.class, 4, true, true,
         entity -> entity != null && notVisibleMob(entity) && !EntityMyrmexBase.haveSameHive(this, entity) && LandUtils.isAlive(entity)));
   }

   @Override
   public int getCareerCaste() {
      return 2;
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(HIDING, false);
   }

   @Override
   public ResourceLocation getAdultTexture() {
      if (this.isHiding()) {
         return this.isJungle() ? TEXTURE_JUNGLE_HIDDEN : TEXTURE_DESERT_HIDDEN;
      } else {
         return this.isJungle() ? TEXTURE_JUNGLE : TEXTURE_DESERT;
      }
   }

   @Override
   public float getModelScale() {
      return 0.8F;
   }

   @Override
   public int getCasteImportance() {
      return 2;
   }

   @Override
   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putBoolean("Hiding", this.isHiding());
      tag.putInt("DaylightTicks", this.daylightTicks);
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setHiding(tag.getBoolean("Hiding"));
      this.daylightTicks = tag.getInt("DaylightTicks");
   }

   @Override
   public boolean shouldLeaveHive() {
      return true;
   }

   @Override
   public boolean shouldEnterHive() {
      return false;
   }

   @Override
   protected void positionRider(Entity passenger, Entity.MoveFunction callback) {
      super.positionRider(passenger, callback);
      if (this.hasPassenger(passenger)) {
         this.yBodyRot = this.getYRot();
         float radius = 1.25F;
         float extraY = 0.35F;
         if (this.getAnimation() == ANIMATION_GRAB) {
            int modTick = Mth.clamp(this.getAnimationTick(), 0, 10);
            radius = 3.25F - (float)modTick * 0.2F;
            extraY = (float)modTick * 0.035F;
         }

         float angle = (float)(Math.PI / 180.0) * this.yBodyRot;
         double extraX = (double)(radius * Mth.sin((float)(Math.PI + (double)angle)));
         double extraZ = (double)(radius * Mth.cos(angle));
         if (passenger.getBbHeight() >= 1.75F) {
            extraY = passenger.getBbHeight() - 2.0F;
         }

         passenger.setPos(this.getX() + extraX, this.getY() + (double)extraY, this.getZ() + extraZ);
      }
   }

   @Override
   public boolean hurt(DamageSource source, float amount) {
      if ((double)amount >= 1.0 && !this.getPassengers().isEmpty() && this.random.nextInt(2) == 0) {
         for (Entity entity : new ArrayList<>(this.getPassengers())) {
            entity.stopRiding();
         }
      }

      this.visibleTicks = 300;
      this.setHiding(false);
      return super.hurt(source, amount);
   }

   @Override
   public boolean doHurtTarget(Entity entityIn) {
      if (this.getGrowthStage() < 2) {
         return false;
      } else if (this.getAnimation() != ANIMATION_STING && this.getAnimation() != ANIMATION_SLASH && this.getAnimation() != ANIMATION_GRAB && this.getHeldEntity() == null) {
         if (this.getRandom().nextInt(2) == 0 && entityIn.getBbWidth() < 2.0F) {
            this.setAnimation(ANIMATION_GRAB);
         } else {
            this.setAnimation(this.getRandom().nextBoolean() ? ANIMATION_STING : ANIMATION_SLASH);
         }

         this.visibleTicks = 300;
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean needsGaurding() {
      return false;
   }

   @Override
   public Animation[] getAnimations() {
      return new Animation[]{ANIMATION_PUPA_WIGGLE, ANIMATION_SLASH, ANIMATION_STING, ANIMATION_GRAB, ANIMATION_NIBBLE};
   }

   @Override
   public boolean canMove() {
      return super.canMove() && this.getHeldEntity() == null && !this.isHiding();
   }

   @Override
   public boolean shouldRiderSit() {
      return false;
   }

   public boolean isHiding() {
      return this.entityData.get(HIDING);
   }

   public void setHiding(boolean hiding) {
      this.entityData.set(HIDING, hiding);
   }
}
