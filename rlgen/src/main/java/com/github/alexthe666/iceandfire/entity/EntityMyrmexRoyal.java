package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.entity.ai.MyrmexAIDefendHive;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAIFindMate;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAILeaveHive;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAILookAtTradePlayer;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAIMoveThroughHive;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAIMoveToMate;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAIReEnterHive;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAITradePlayer;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAIWander;
import com.github.alexthe666.iceandfire.entity.ai.MyrmexAIWanderHiveCenter;
import com.github.alexthe666.iceandfire.entity.ai.goal112.EntityAIAttackMelee;
import com.github.alexthe666.iceandfire.entity.ai.goal112.EntityAIHurtByTarget;
import com.github.alexthe666.iceandfire.entity.ai.goal112.EntityAINearestAttackableTarget;
import java.util.EnumSet;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Ice and Fire 1.7.1 EntityMyrmexRoyal: flies out in the breeding season, mates with a royal of another hive into a queen. */
public class EntityMyrmexRoyal extends EntityMyrmexBase {
   private static final ResourceLocation TEXTURE_DESERT = IafLand.rl("textures/models/myrmex/myrmex_desert_royal.png");
   private static final ResourceLocation TEXTURE_JUNGLE = IafLand.rl("textures/models/myrmex/myrmex_jungle_royal.png");
   private static final EntityDataAccessor<Boolean> FLYING = SynchedEntityData.defineId(EntityMyrmexRoyal.class, EntityDataSerializers.BOOLEAN);
   public static final Animation ANIMATION_BITE = Animation.create(15);
   public static final Animation ANIMATION_STING = Animation.create(15);
   private int hiveTicks = 0;
   public int releaseTicks = 0;
   private int breedingTicks = 0;
   public int daylightTicks = 0;
   public float flyProgress;
   private boolean isFlying;
   private boolean isLandNavigator;
   private boolean isMating = false;
   public EntityMyrmexRoyal mate;
   public static final ResourceKey<LootTable> DESERT_LOOT = ResourceKey.create(Registries.LOOT_TABLE, IafLand.rl("myrmex_royal_desert"));
   public static final ResourceKey<LootTable> JUNGLE_LOOT = ResourceKey.create(Registries.LOOT_TABLE, IafLand.rl("myrmex_royal_jungle"));

   public EntityMyrmexRoyal(EntityType<? extends EntityMyrmexRoyal> type, Level worldIn) {
      super(type, worldIn);
      this.switchNavigator(true);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return createBaseAttributes().add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.MAX_HEALTH, 50.0).add(Attributes.ARMOR, 9.0).add(Attributes.FLYING_SPEED, 0.3);
   }

   @Override
   protected ResourceKey<LootTable> getDefaultLootTable() {
      return this.isJungle() ? JUNGLE_LOOT : DESERT_LOOT;
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(FLYING, false);
   }

   private void switchNavigator(boolean onLand) {
      if (onLand) {
         this.moveControl = new MoveControl(this);
         this.navigation = new WallClimberNavigation(this, this.level());
         this.isLandNavigator = true;
      } else {
         this.moveControl = new FlyMoveHelper(this);
         this.navigation = new FlyingPathNavigation(this, this.level());
         this.isLandNavigator = false;
      }
   }

   public boolean isFlying() {
      return this.level().isClientSide ? (this.isFlying = this.entityData.get(FLYING)) : this.isFlying;
   }

   public void setFlying(boolean flying) {
      this.entityData.set(FLYING, flying);
      if (!this.level().isClientSide) {
         this.isFlying = flying;
      }
   }

   @Override
   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("HiveTicks", this.hiveTicks);
      tag.putInt("ReleaseTicks", this.releaseTicks);
      tag.putBoolean("Flying", this.isFlying());
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.hiveTicks = tag.getInt("HiveTicks");
      this.releaseTicks = tag.getInt("ReleaseTicks");
      this.setFlying(tag.getBoolean("Flying"));
   }

   @Override
   public void aiStep() {
      super.aiStep();
      boolean flying = this.isFlying() && !this.onGround();
      if (flying && this.flyProgress < 20.0F) {
         this.flyProgress++;
      } else if (!flying && this.flyProgress > 0.0F) {
         this.flyProgress--;
      }

      if (flying) {
         this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.08, 0.0));
      }

      if (flying && this.isLandNavigator) {
         this.switchNavigator(false);
      }

      if (!flying && !this.isLandNavigator) {
         this.switchNavigator(true);
      }

      if (this.canSeeSky()) {
         this.daylightTicks++;
      } else {
         this.daylightTicks = 0;
      }

      if (flying && this.canSeeSky() && this.isBreedingSeason()) {
         this.releaseTicks++;
      }

      if (!flying && this.canSeeSky() && this.daylightTicks > 300 && this.isBreedingSeason() && this.getTarget() == null && this.canMove() && this.onGround() && !this.isMating) {
         this.setFlying(true);
         this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.42, 0.0));
      }

      if (this.getGrowthStage() >= 2) {
         this.hiveTicks++;
      }

      if (this.getAnimation() == ANIMATION_BITE && this.getTarget() != null && this.getAnimationTick() == 6) {
         this.playBiteSound();
         double dist = this.distanceToSqr(this.getTarget());
         if (dist < 6.0) {
            this.getTarget().hurt(this.damageSources().mobAttack(this), (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE)));
         }
      }

      if (this.getAnimation() == ANIMATION_STING && this.getTarget() != null && this.getAnimationTick() == 6) {
         this.playStingSound();
         double dist = this.distanceToSqr(this.getTarget());
         if (dist < 6.0) {
            this.getTarget().hurt(this.damageSources().mobAttack(this), (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2));
            this.getTarget().addEffect(new MobEffectInstance(MobEffects.POISON, 70, 1));
         }
      }

      if (this.mate != null) {
         this.level().broadcastEntityEvent(this, (byte)77);
         if (this.distanceTo(this.mate) < 10.0F) {
            this.setFlying(false);
            this.mate.setFlying(false);
            this.isMating = true;
            if (this.onGround() && this.mate.onGround()) {
               this.breedingTicks++;
               if (this.breedingTicks > 100) {
                  if (this.isAlive()) {
                     this.mate.discard();
                     this.discard();
                     EntityMyrmexQueen queen = IafLandMobs.MYRMEX_QUEEN.get().create(this.level());
                     queen.copyPosition(this);
                     queen.setJungleVariant(this.isJungle());
                     queen.setMadeHome(false);
                     if (!this.level().isClientSide) {
                        this.level().addFreshEntity(queen);
                     }
                  }

                  this.isMating = false;
               }
            }
         }

         this.mate.mate = this;
         if (!this.mate.isAlive()) {
            this.mate.mate = null;
            this.mate = null;
         }
      }
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(0, new MyrmexAITradePlayer(this));
      this.goalSelector.addGoal(0, new MyrmexAILookAtTradePlayer(this));
      this.goalSelector.addGoal(0, new MyrmexAIMoveToMate(this, 1.0));
      this.goalSelector.addGoal(1, new AIFlyRandom());
      this.goalSelector.addGoal(2, new EntityAIAttackMelee(this, 1.0, true));
      this.goalSelector.addGoal(3, new MyrmexAILeaveHive(this, 1.0));
      this.goalSelector.addGoal(3, new MyrmexAIReEnterHive(this, 1.0));
      this.goalSelector.addGoal(4, new MyrmexAIMoveThroughHive(this, 1.0));
      this.goalSelector.addGoal(4, new MyrmexAIWanderHiveCenter(this, 1.0));
      this.goalSelector.addGoal(5, new MyrmexAIWander(this, 1.0));
      this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
      this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new MyrmexAIDefendHive(this));
      this.targetSelector.addGoal(2, new MyrmexAIFindMate(this));
      this.targetSelector.addGoal(3, new EntityAIHurtByTarget(this, false));
      this.targetSelector.addGoal(4, new MyrmexAIAttackPlayers(this));
      this.targetSelector.addGoal(4, new EntityAINearestAttackableTarget<>(this, Mob.class, 10, true, true,
         entity -> (!(entity instanceof EntityMyrmexBase) || !this.isBreedingSeason()) && !(entity instanceof EntityMyrmexRoyal)
            ? entity != null && notVisibleMob(entity) && !EntityMyrmexBase.haveSameHive(this, entity) && LandUtils.isAlive(entity)
            : false));
   }

   @Override
   public boolean canMate(Animal otherAnimal) {
      return this.canMateWith(otherAnimal);
   }

   public boolean canMateWith(Animal otherAnimal) {
      if (otherAnimal != this && otherAnimal != null) {
         if (otherAnimal.getClass() != this.getClass()) {
            return false;
         } else if (!(otherAnimal instanceof EntityMyrmexBase)) {
            return false;
         } else {
            return ((EntityMyrmexBase)otherAnimal).getHive() != null && this.getHive() != null ? !this.getHive().equals(((EntityMyrmexBase)otherAnimal).getHive()) : true;
         }
      } else {
         return false;
      }
   }

   @Override
   public int getCareerCaste() {
      return 3;
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
      return 1.25F;
   }

   @Override
   public int getCasteImportance() {
      return 2;
   }

   @Override
   public boolean shouldLeaveHive() {
      return this.isBreedingSeason();
   }

   @Override
   public boolean shouldEnterHive() {
      return !this.isBreedingSeason();
   }

   @Override
   public boolean doHurtTarget(Entity entityIn) {
      if (this.getGrowthStage() < 2) {
         return false;
      } else if (this.getAnimation() != ANIMATION_STING && this.getAnimation() != ANIMATION_BITE) {
         this.setAnimation(this.getRandom().nextBoolean() ? ANIMATION_STING : ANIMATION_BITE);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public Animation[] getAnimations() {
      return new Animation[]{ANIMATION_PUPA_WIGGLE, ANIMATION_BITE, ANIMATION_STING};
   }

   public boolean isBreedingSeason() {
      return this.getGrowthStage() >= 2 && this.hiveTicks > 4000 && (this.getHive() == null || this.getHive().reproduces);
   }

   @Override
   public void handleEntityEvent(byte id) {
      if (id == 76) {
         this.playEffect(20);
      } else if (id == 77) {
         this.playEffect(7);
      } else {
         super.handleEntityEvent(id);
      }
   }

   protected void playEffect(int hearts) {
      for (int i = 0; i < hearts; i++) {
         double d0 = this.random.nextGaussian() * 0.02;
         double d1 = this.random.nextGaussian() * 0.02;
         double d2 = this.random.nextGaussian() * 0.02;
         this.level()
            .addParticle(
               ParticleTypes.HEART,
               this.getX() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
               this.getY() + 0.5 + (double)(this.random.nextFloat() * this.getBbHeight()),
               this.getZ() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
               d0,
               d1,
               d2
            );
      }
   }

   public static BlockPos getPositionRelativetoGround(Entity entity, Level world, double x, double z, RandomSource rand) {
      BlockPos pos = BlockPos.containing(x, entity.getY(), z);

      for (int yDown = 0; yDown < 10; yDown++) {
         if (!world.isEmptyBlock(pos.below(yDown))) {
            return pos.above(yDown);
         }
      }

      return pos;
   }

   class AIFlyRandom extends Goal {
      BlockPos target;

      public AIFlyRandom() {
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      @Override
      public boolean canUse() {
         if (!EntityMyrmexRoyal.this.isFlying()) {
            return false;
         } else {
            this.target = EntityMyrmexRoyal.getPositionRelativetoGround(
               EntityMyrmexRoyal.this,
               EntityMyrmexRoyal.this.level(),
               EntityMyrmexRoyal.this.getX() + (double)EntityMyrmexRoyal.this.random.nextInt(30) - 15.0,
               EntityMyrmexRoyal.this.getZ() + (double)EntityMyrmexRoyal.this.random.nextInt(30) - 15.0,
               EntityMyrmexRoyal.this.random
            );
            return this.isDirectPathBetweenPoints(EntityMyrmexRoyal.this.blockPosition(), this.target)
               && !EntityMyrmexRoyal.this.getMoveControl().hasWanted()
               && EntityMyrmexRoyal.this.random.nextInt(2) == 0;
         }
      }

      protected boolean isDirectPathBetweenPoints(BlockPos posVec31, BlockPos posVec32) {
         BlockHitResult raytraceresult = EntityMyrmexRoyal.this.level()
            .clip(
               new ClipContext(
                  new Vec3((double)posVec31.getX() + 0.5, (double)posVec31.getY() + 0.5, (double)posVec31.getZ() + 0.5),
                  new Vec3((double)posVec32.getX() + 0.5, (double)posVec32.getY() + (double)EntityMyrmexRoyal.this.getBbHeight() * 0.5, (double)posVec32.getZ() + 0.5),
                  ClipContext.Block.COLLIDER,
                  ClipContext.Fluid.NONE,
                  EntityMyrmexRoyal.this
               )
            );
         return raytraceresult == null || raytraceresult.getType() == HitResult.Type.MISS;
      }

      @Override
      public boolean canContinueToUse() {
         return false;
      }

      @Override
      public boolean requiresUpdateEveryTick() {
         return true;
      }

      @Override
      public void tick() {
         if (!this.isDirectPathBetweenPoints(EntityMyrmexRoyal.this.blockPosition(), this.target)) {
            this.target = EntityMyrmexRoyal.getPositionRelativetoGround(
               EntityMyrmexRoyal.this,
               EntityMyrmexRoyal.this.level(),
               EntityMyrmexRoyal.this.getX() + (double)EntityMyrmexRoyal.this.random.nextInt(15) - 7.0,
               EntityMyrmexRoyal.this.getZ() + (double)EntityMyrmexRoyal.this.random.nextInt(15) - 7.0,
               EntityMyrmexRoyal.this.random
            );
         }

         if (EntityMyrmexRoyal.this.level().isEmptyBlock(this.target)) {
            EntityMyrmexRoyal.this.moveControl.setWantedPosition((double)this.target.getX() + 0.5, (double)this.target.getY() + 0.5, (double)this.target.getZ() + 0.5, 0.25);
            if (EntityMyrmexRoyal.this.getTarget() == null) {
               EntityMyrmexRoyal.this.getLookControl().setLookAt((double)this.target.getX() + 0.5, (double)this.target.getY() + 0.5, (double)this.target.getZ() + 0.5, 180.0F, 20.0F);
            }
         }
      }
   }

   class FlyMoveHelper extends MoveControl {
      public FlyMoveHelper(EntityMyrmexRoyal royal) {
         super(royal);
         this.speedModifier = 1.75;
      }

      @Override
      public void tick() {
         if (this.operation == MoveControl.Operation.MOVE_TO) {
            double d0 = this.wantedX - EntityMyrmexRoyal.this.getX();
            double d1 = this.wantedY - EntityMyrmexRoyal.this.getY();
            double d2 = this.wantedZ - EntityMyrmexRoyal.this.getZ();
            double d3 = d0 * d0 + d1 * d1 + d2 * d2;
            d3 = Math.sqrt(d3);
            if (d3 < EntityMyrmexRoyal.this.getBoundingBox().getSize()) {
               this.operation = MoveControl.Operation.WAIT;
               EntityMyrmexRoyal.this.setDeltaMovement(EntityMyrmexRoyal.this.getDeltaMovement().scale(0.5));
            } else {
               EntityMyrmexRoyal.this.setDeltaMovement(EntityMyrmexRoyal.this.getDeltaMovement().add(d0 / d3 * 0.15 * this.speedModifier, d1 / d3 * 0.15 * this.speedModifier, d2 / d3 * 0.15 * this.speedModifier));
               Vec3 m = EntityMyrmexRoyal.this.getDeltaMovement();
               if (EntityMyrmexRoyal.this.getTarget() == null) {
                  EntityMyrmexRoyal.this.setYRot(-((float)Mth.atan2(m.x, m.z)) * (180.0F / (float)Math.PI));
               } else {
                  double d4 = EntityMyrmexRoyal.this.getTarget().getX() - EntityMyrmexRoyal.this.getX();
                  double d5 = EntityMyrmexRoyal.this.getTarget().getZ() - EntityMyrmexRoyal.this.getZ();
                  EntityMyrmexRoyal.this.setYRot(-((float)Mth.atan2(d4, d5)) * (180.0F / (float)Math.PI));
               }
               EntityMyrmexRoyal.this.yBodyRot = EntityMyrmexRoyal.this.getYRot();
            }
         }
      }
   }
}
