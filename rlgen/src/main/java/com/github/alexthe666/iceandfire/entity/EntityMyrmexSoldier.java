package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.entity.ai.MyrmexAIDefendHive;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAIEscortEntity;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAIFindGaurdingEntity;
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
import net.ilexiconn.llibrary.server.animation.Animation;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;

/** Ice and Fire 1.7.1 EntityMyrmexSoldier */
public class EntityMyrmexSoldier extends EntityMyrmexBase {
   private static final ResourceLocation TEXTURE_DESERT = IafLand.rl("textures/models/myrmex/myrmex_desert_soldier.png");
   private static final ResourceLocation TEXTURE_JUNGLE = IafLand.rl("textures/models/myrmex/myrmex_jungle_soldier.png");
   public static final Animation ANIMATION_BITE = Animation.create(15);
   public static final Animation ANIMATION_STING = Animation.create(15);
   public EntityMyrmexBase guardingEntity = null;
   public static final ResourceKey<LootTable> DESERT_LOOT = ResourceKey.create(Registries.LOOT_TABLE, IafLand.rl("myrmex_soldier_desert"));
   public static final ResourceKey<LootTable> JUNGLE_LOOT = ResourceKey.create(Registries.LOOT_TABLE, IafLand.rl("myrmex_soldier_jungle"));

   public EntityMyrmexSoldier(EntityType<? extends EntityMyrmexSoldier> type, Level worldIn) {
      super(type, worldIn);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return createBaseAttributes().add(Attributes.MOVEMENT_SPEED, 0.35).add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.MAX_HEALTH, 40.0).add(Attributes.ARMOR, 6.0);
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
            this.getTarget().addEffect(new MobEffectInstance(MobEffects.POISON, 200, 2));
         }
      }

      if (this.guardingEntity != null) {
         this.guardingEntity.isBeingGuarded = true;
         this.isEnteringHive = this.guardingEntity.isEnteringHive;
         if (!this.guardingEntity.isAlive()) {
            this.guardingEntity.isBeingGuarded = false;
            this.guardingEntity = null;
         }
      }
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(0, new MyrmexAITradePlayer(this));
      this.goalSelector.addGoal(0, new MyrmexAILookAtTradePlayer(this));
      this.goalSelector.addGoal(1, new EntityAIAttackMelee(this, 1.0, true));
      this.goalSelector.addGoal(2, new MyrmexAIEscortEntity(this, 1.0));
      this.goalSelector.addGoal(2, new MyrmexAIReEnterHive(this, 1.0));
      this.goalSelector.addGoal(4, new MyrmexAILeaveHive(this, 1.0));
      this.goalSelector.addGoal(5, new MyrmexAIMoveThroughHive(this, 1.0));
      this.goalSelector.addGoal(6, new MyrmexAIWander(this, 1.0));
      this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
      this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new MyrmexAIDefendHive(this));
      this.targetSelector.addGoal(2, new MyrmexAIFindGaurdingEntity(this));
      this.targetSelector.addGoal(3, new EntityAIHurtByTarget(this, false));
      this.targetSelector.addGoal(4, new MyrmexAIAttackPlayers(this));
      this.targetSelector.addGoal(4, new EntityAINearestAttackableTarget<>(this, Mob.class, 10, true, true,
         entity -> entity != null && notVisibleMob(entity) && !EntityMyrmexBase.haveSameHive(this, entity) && LandUtils.isAlive(entity)));
   }

   @Override
   public int getCareerCaste() {
      return 1;
   }

   @Override
   public ResourceLocation getAdultTexture() {
      return this.isJungle() ? TEXTURE_JUNGLE : TEXTURE_DESERT;
   }

   @Override
   public float getModelScale() {
      return 0.8F;
   }

   @Override
   public int getCasteImportance() {
      return 1;
   }

   @Override
   public boolean shouldLeaveHive() {
      return false;
   }

   @Override
   public boolean shouldEnterHive() {
      return this.guardingEntity == null || !this.guardingEntity.canSeeSky() || this.guardingEntity.shouldEnterHive();
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
   public boolean needsGaurding() {
      return false;
   }

   @Override
   public Animation[] getAnimations() {
      return new Animation[]{ANIMATION_PUPA_WIGGLE, ANIMATION_BITE, ANIMATION_STING};
   }
}
