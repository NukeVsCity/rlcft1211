package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.entity.ai.MyrmexAIDefendHive;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAIForage;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAIForageForItems;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAILeaveHive;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAILookAtTradePlayer;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAIMoveThroughHive;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAIPickupBabies;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAIReEnterHive;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAIStoreBabies;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAIStoreItems;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAITradePlayer;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAIWander;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIAttackMelee;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIHurtByTarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAINearestAttackableTarget;
import java.util.ArrayList;
import javax.annotation.Nullable;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;

/** Ice and Fire 1.7.1 EntityMyrmexWorker */
public class EntityMyrmexWorker extends EntityMyrmexBase {
   private static final ResourceLocation TEXTURE_DESERT = IafLand.rl("textures/models/myrmex/myrmex_desert_worker.png");
   private static final ResourceLocation TEXTURE_JUNGLE = IafLand.rl("textures/models/myrmex/myrmex_jungle_worker.png");
   public boolean keepSearching = true;
   public static final Animation ANIMATION_BITE = Animation.create(15);
   public static final Animation ANIMATION_STING = Animation.create(15);
   public static final ResourceKey<LootTable> DESERT_LOOT = ResourceKey.create(Registries.LOOT_TABLE, IafLand.rl("myrmex_worker_desert"));
   public static final ResourceKey<LootTable> JUNGLE_LOOT = ResourceKey.create(Registries.LOOT_TABLE, IafLand.rl("myrmex_worker_jungle"));

   public EntityMyrmexWorker(EntityType<? extends EntityMyrmexWorker> type, Level worldIn) {
      super(type, worldIn);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return createBaseAttributes().add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.ATTACK_DAMAGE, 3.0).add(Attributes.MAX_HEALTH, 20.0).add(Attributes.ARMOR, 4.0);
   }

   @Override
   protected ResourceKey<LootTable> getDefaultLootTable() {
      return this.isJungle() ? JUNGLE_LOOT : DESERT_LOOT;
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (this.getAnimation() == ANIMATION_BITE && this.getTarget() != null && this.getAnimationTick() == 6) {
         this.playBiteSound();
         double dist = this.distanceToSqr(this.getTarget());
         if (dist < 4.0) {
            this.getTarget().hurt(this.damageSources().mobAttack(this), (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE)));
         }
      }

      if (this.getAnimation() == ANIMATION_STING && this.getAnimationTick() == 0) {
         this.playStingSound();
      }

      if (this.getAnimation() == ANIMATION_STING && this.getTarget() != null && this.getAnimationTick() == 6) {
         double dist = this.distanceToSqr(this.getTarget());
         if (dist < 4.0) {
            this.getTarget().hurt(this.damageSources().mobAttack(this), (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2));
            this.getTarget().addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
         }
      }

      ItemStack held = this.getItemInHand(InteractionHand.MAIN_HAND);
      if (!held.isEmpty() && MyrmexTrades.isMyrmexEgg(held)) {
         boolean isJungle = IafLand.isItem(held.getItem(), "myrmex_jungle_egg");
         int metadata = MyrmexTrades.eggCaste(held);
         EntityMyrmexEgg egg = IafLandMobs.MYRMEX_EGG.get().create(this.level());
         egg.copyPosition(this);
         egg.setJungle(isJungle);
         egg.setMyrmexCaste(metadata);
         if (!this.level().isClientSide) {
            this.level().addFreshEntity(egg);
         }

         egg.startRiding(this);
         this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
      }

      if (!this.getPassengers().isEmpty()) {
         for (Entity entity : new ArrayList<>(this.getPassengers())) {
            if (entity instanceof EntityMyrmexBase && ((EntityMyrmexBase)entity).getGrowthStage() >= 2) {
               entity.stopRiding();
            }
         }
      }
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(0, new MyrmexAITradePlayer(this));
      this.goalSelector.addGoal(0, new MyrmexAILookAtTradePlayer(this));
      this.goalSelector.addGoal(1, new EntityAIAttackMelee(this, 1.0, true));
      this.goalSelector.addGoal(2, new MyrmexAIStoreBabies(this, 1.0));
      this.goalSelector.addGoal(3, new MyrmexAIStoreItems(this, 1.0));
      this.goalSelector.addGoal(4, new MyrmexAIReEnterHive(this, 1.0));
      this.goalSelector.addGoal(4, new MyrmexAILeaveHive(this, 1.0));
      this.goalSelector.addGoal(6, new MyrmexAIForage(this));
      this.goalSelector.addGoal(7, new MyrmexAIMoveThroughHive(this, 1.0));
      this.goalSelector.addGoal(8, new MyrmexAIWander(this, 1.0));
      this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 6.0F));
      this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new MyrmexAIDefendHive(this));
      this.targetSelector.addGoal(2, new MyrmexAIForageForItems(this));
      this.targetSelector.addGoal(3, new MyrmexAIPickupBabies(this));
      this.targetSelector.addGoal(4, new EntityAIHurtByTarget(this, false));
      this.targetSelector.addGoal(4, new MyrmexAIAttackPlayers(this));
      this.targetSelector.addGoal(5, new EntityAINearestAttackableTarget<>(this, Mob.class, 10, true, true,
         entity -> entity != null && notVisibleMob(entity) && !EntityMyrmexBase.haveSameHive(this, entity) && LandUtils.isAlive(entity)));
   }

   @Override
   public ResourceLocation getAdultTexture() {
      return this.isJungle() ? TEXTURE_JUNGLE : TEXTURE_DESERT;
   }

   @Override
   public float getModelScale() {
      return 0.6F;
   }

   @Override
   public int getCareerCaste() {
      return 0;
   }

   @Override
   public boolean shouldLeaveHive() {
      return !this.holdingSomething();
   }

   @Override
   public boolean shouldEnterHive() {
      return this.holdingSomething();
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

   private boolean holdingSomething() {
      return this.getHeldEntity() != null || !this.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() || this.getTarget() != null;
   }

   public boolean holdingBaby() {
      return this.getHeldEntity() != null && (this.getHeldEntity() instanceof EntityMyrmexBase || this.getHeldEntity() instanceof EntityMyrmexEgg);
   }

   @Override
   public int getCasteImportance() {
      return 0;
   }

   @Override
   public Animation[] getAnimations() {
      return new Animation[]{ANIMATION_PUPA_WIGGLE, ANIMATION_BITE, ANIMATION_STING};
   }

   @Override
   protected void positionRider(Entity passenger, Entity.MoveFunction callback) {
      super.positionRider(passenger, callback);
      if (this.hasPassenger(passenger)) {
         this.yBodyRot = this.getYRot();
         float radius = 1.05F;
         float angle = (float)(Math.PI / 180.0) * this.yBodyRot;
         double extraX = (double)(radius * Mth.sin((float)(Math.PI + (double)angle)));
         double extraZ = (double)(radius * Mth.cos(angle));
         passenger.setPos(this.getX() + extraX, this.getY() + 0.25, this.getZ() + extraZ);
      }
   }

   @Override
   public boolean hurt(DamageSource source, float amount) {
      if ((double)amount >= 1.0 && !this.level().isClientSide && this.getRandom().nextInt(3) == 0 && !this.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() && !LandUtils.isStone(this)) {
         this.spawnAtLocation(this.getItemInHand(InteractionHand.MAIN_HAND), 0.0F);
         this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
      }

      if ((double)amount >= 1.0 && !this.getPassengers().isEmpty()) {
         for (Entity entity : new ArrayList<>(this.getPassengers())) {
            entity.stopRiding();
         }
      }

      return super.hurt(source, amount);
   }

   @Nullable
   public Entity getHeldEntity() {
      return this.getPassengers().isEmpty() ? null : this.getPassengers().get(0);
   }

   /** a worker picking up resin thrown by a player raises that player's reputation by 5 */
   public void onPickupItem(ItemEntity itemEntity) {
      Item item = itemEntity.getItem().getItem();
      if (IafLand.isItem(item, "myrmex_jungle_resin") && this.isJungle() || IafLand.isItem(item, "myrmex_desert_resin") && !this.isJungle()) {
         Player owner = itemEntity.getOwner() instanceof Player p ? p : null;
         if (owner != null && this.getHive() != null) {
            this.getHive().modifyPlayerReputation(owner.getUUID(), 5);
            this.playSound(SoundEvents.SLIME_SQUISH, 1.0F, 1.0F);
            if (!this.level().isClientSide) {
               this.level().addFreshEntity(new ExperienceOrb(this.level(), owner.getX(), owner.getY(), owner.getZ(), 1 + this.random.nextInt(3)));
            }
         }
      }
   }
}
