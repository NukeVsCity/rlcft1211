package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.entity.ai.AquaticAIGetInWater;
import com.github.alexthe666.iceandfire.entity.ai.AquaticAIGetOutOfWater;
import com.github.alexthe666.iceandfire.entity.ai.PathNavigateAmphibious;
import com.github.alexthe666.iceandfire.entity.ai.SirenAIFindWaterTarget;
import com.github.alexthe666.iceandfire.entity.ai.SirenAIWander;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAIAttackMelee;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAIHurtByTarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAILookIdle;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAINearestAttackableTarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAIWatchClosest;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.Legacy112;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.LegacyMoveControl;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.LegacyTasksGoal;
import com.github.alexthe666.iceandfire.message.AirWaterNetwork;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.ilexiconn.llibrary.server.animation.AnimationHandler;
import net.ilexiconn.llibrary.server.animation.IAnimatedEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

/** Ice and Fire 1.7.1 EntitySiren. */
public class EntitySiren extends Monster implements IAnimatedEntity {
   private int animationTick;
   private Animation currentAnimation;
   private static final EntityDataAccessor<Integer> HAIR_COLOR = SynchedEntityData.defineId(EntitySiren.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> AGGRESSIVE = SynchedEntityData.defineId(EntitySiren.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> SING_POSE = SynchedEntityData.defineId(EntitySiren.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> SINGING = SynchedEntityData.defineId(EntitySiren.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> SWIMMING = SynchedEntityData.defineId(EntitySiren.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> CHARMED = SynchedEntityData.defineId(EntitySiren.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Byte> CLIMBING = SynchedEntityData.defineId(EntitySiren.class, EntityDataSerializers.BYTE);
   /** client only: ChainBuffer */
   public net.ilexiconn.llibrary.client.model.tools.ChainBuffer tail_buffer;
   public float singProgress;
   private boolean isSinging;
   private boolean isSwimming;
   public float swimProgress;
   public static final int SEARCH_RANGE = 32;
   private boolean isLandNavigator;
   private int ticksAgressive;
   public int singCooldown;
   public static Animation ANIMATION_BITE = Animation.create(20);
   public static Animation ANIMATION_PULL = Animation.create(20);
   public static final ResourceKey<LootTable> LOOT = AirWaterEntities.loot("siren");
   public static final Predicate<Entity> SIREN_PREY = e -> e instanceof Player p && !p.isCreative() && !p.isSpectator() || e instanceof Villager
      || e instanceof IHearsSiren;
   public final EntityAITasks tasks = new EntityAITasks();
   public final EntityAITasks targetTasks = new EntityAITasks();

   public EntitySiren(EntityType<? extends EntitySiren> type, Level level) {
      super(type, level);
      this.switchNavigator(true);
      this.setPathfindingMalus(PathType.WATER, 0.0F);
      this.tasks.addTask(0, new SirenAIFindWaterTarget(this));
      this.tasks.addTask(1, new AquaticAIGetInWater(this, 1.0));
      this.tasks.addTask(1, new AquaticAIGetOutOfWater(this, 1.0));
      this.tasks.addTask(2, new SirenAIWander(this, 1.0));
      this.tasks.addTask(3, new EntityAILookIdle(this));
      this.tasks.addTask(3, new EntityAIAttackMelee(this, 1.0, false));
      this.tasks.addTask(6, new EntityAIWatchClosest(this, Player.class, 8.0F, 1.0F));
      this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
      this.targetTasks.addTask(4, new EntityAINearestAttackableTarget<>(this, Player.class, 0, true, false,
         entity -> this.isAgressive() && !entity.isCreative() && !entity.isSpectator()));
      this.targetTasks.addTask(4, new EntityAINearestAttackableTarget<>(this, Villager.class, 0, true, false, entity -> this.isAgressive()));
      this.goalSelector.addGoal(0, new LegacyTasksGoal(this, this.targetTasks, this.tasks));
      if (level.isClientSide) {
         this.tail_buffer = AirWaterEntities.clientLLibraryChainBuffer();
      }
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MOVEMENT_SPEED, 0.25)
         .add(Attributes.ATTACK_DAMAGE, 6.0)
         .add(Attributes.MAX_HEALTH, com.github.alexthe666.iceandfire.IceAndFire.CONFIG.sirenMaxHealth)
         .add(Attributes.STEP_HEIGHT, 2.0);
   }

   @Override
   protected void registerGoals() {
   }

   @Override
   protected int getBaseExperienceReward() {
      return 10 + this.level().random.nextInt(10);
   }

   @Override
   public boolean removeWhenFarAway(double distance) {
      return false;
   }

   @Override
   protected ResourceKey<LootTable> getDefaultLootTable() {
      return LOOT;
   }

   @Override
   public float getWalkTargetValue(BlockPos pos, LevelReader level) {
      return Legacy112.isWaterMaterial(level.getBlockState(pos)) ? 10.0F : super.getWalkTargetValue(pos, level);
   }

   @Override
   public boolean doHurtTarget(Entity entityIn) {
      if (this.getRandom().nextInt(2) == 0) {
         if (this.getAnimation() != ANIMATION_PULL) {
            this.setAnimation(ANIMATION_PULL);
            this.playSound(AirWaterRefs.sound("naga_attack"), 1.0F, 1.0F);
         }
      } else if (this.getAnimation() != ANIMATION_BITE) {
         this.setAnimation(ANIMATION_BITE);
         this.playSound(AirWaterRefs.sound("naga_attack"), 1.0F, 1.0F);
      }
      return true;
   }

   public boolean isDirectPathBetweenPoints(Vec3 vec1, Vec3 vec2) {
      return AirWaterUtils.rayTraceBlocks(this, vec1, new Vec3(vec2.x, vec2.y + (double)this.getBbHeight() * 0.5, vec2.z), false) == null;
   }

   private void switchNavigator(boolean onLand) {
      if (onLand) {
         this.moveControl = new LegacyMoveControl(this);
         this.navigation = new PathNavigateAmphibious(this, this.level());
         this.isLandNavigator = true;
      } else {
         this.moveControl = new SwimmingMoveHelper();
         this.navigation = new WaterBoundPathNavigation(this, this.level());
         this.isLandNavigator = false;
      }
   }

   private boolean isPathOnHighGround() {
      if (this.navigation != null && this.navigation.getPath() != null && this.navigation.getPath().getEndNode() != null) {
         var end = this.navigation.getPath().getEndNode();
         BlockPos target = new BlockPos(end.x, end.y, end.z);
         BlockPos siren = AirWaterUtils.pos(this);
         return this.level().isEmptyBlock(siren.above()) && this.level().isEmptyBlock(target.above()) && target.getY() >= siren.getY();
      }
      return false;
   }

   @Override
   public boolean canDrownInFluidType(net.neoforged.neoforge.fluids.FluidType type) {
      return false;
   }

   public boolean canBreatheUnderwaterIaf() {
      return true;
   }

   @Override
   public boolean isInWater() {
      return super.isInWater() || this.isEyeInFluid(FluidTags.WATER);
   }

   /** 1.12 Entity.isOverWater: water within about 20 blocks below or above the box. */
   public static boolean isOverWater(Entity e) {
      AABB bb = e.getBoundingBox();
      AABB box = new AABB(bb.minX, bb.maxY - 20.0, bb.minZ, bb.maxX, bb.minY + 20.0, bb.maxZ).deflate(0.001);
      return BlockPos.betweenClosedStream(box).anyMatch(p -> Legacy112.isWaterMaterial(e.level().getBlockState(p)));
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (this.singCooldown > 0) {
         this.singCooldown--;
         this.setSinging(false);
      }

      if (!this.level().isClientSide && this.getTarget() == null && !this.isAgressive()) {
         this.setSinging(true);
      }

      if (this.getAnimation() == ANIMATION_BITE && this.getTarget() != null && this.distanceToSqr(this.getTarget()) < 4.0 && this.getAnimationTick() == 5) {
         this.getTarget().hurt(this.damageSources().mobAttack(this), (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE));
      }

      if (this.getAnimation() == ANIMATION_PULL && this.getTarget() != null && this.distanceToSqr(this.getTarget()) < 12.0 && this.getAnimationTick() == 5) {
         LivingEntity target = this.getTarget();
         target.hurt(this.damageSources().mobAttack(this), (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE));
         Vec3 m = target.getDeltaMovement();
         double mx = m.x + (Math.signum(this.getX() - target.getX()) * 0.5 - m.x) * 0.100000000372529 * 5.0;
         double my = m.y + (Math.signum(this.getY() - target.getY() + 1.0) * 0.5 - m.y) * 0.100000000372529 * 5.0;
         double mz = m.z + (Math.signum(this.getZ() - target.getZ()) * 0.5 - m.z) * 0.100000000372529 * 5.0;
         target.setDeltaMovement(mx, my, mz);
         target.hurtMarked = true;
         double d0 = this.getX() - target.getX();
         double d2 = this.getZ() - target.getZ();
         double d1 = this.getY() - 1.0 - target.getY();
         double d3 = Math.sqrt(d0 * d0 + d2 * d2);
         float f = (float)(Mth.atan2(d2, d0) * (180.0 / Math.PI)) - 90.0F;
         float f1 = (float)(-(Mth.atan2(d1, d3) * (180.0 / Math.PI)));
         target.setXRot(AirWaterUtils.updateRotation(target.getXRot(), f1, 30.0F));
         target.setYRot(AirWaterUtils.updateRotation(target.getYRot(), f, 30.0F));
      }

      if (this.level().isClientSide) {
         AirWaterEntities.swingBuffer(this.tail_buffer, 40.0F, 10, 2.5F, this);
      }

      if (this.isAgressive()) {
         this.ticksAgressive++;
      } else {
         this.ticksAgressive = 0;
      }

      if (this.ticksAgressive > 300 && this.isAgressive() && this.getTarget() == null && !this.level().isClientSide) {
         this.setAggressive(false);
         this.ticksAgressive = 0;
         this.setSinging(false);
      }

      if (this.isInWater() && !this.isSirenSwimming()) {
         this.setSirenSwimming(true);
      }

      if (!this.isInWater() && this.isSirenSwimming()) {
         this.setSirenSwimming(false);
      }

      boolean pathOnHighGround = this.isPathOnHighGround() || this.getTarget() != null && !this.getTarget().isInWater() && !isOverWater(this.getTarget());
      if ((this.getTarget() == null || !this.getTarget().isInWater() && !isOverWater(this.getTarget())) && pathOnHighGround && this.isInWater()) {
         this.jumpFromGround();
         this.doWaterSplashEffect();
      }

      if (this.isInWater() && !pathOnHighGround && this.isLandNavigator) {
         this.switchNavigator(false);
      }

      if ((!this.isInWater() || pathOnHighGround) && !this.isLandNavigator) {
         this.switchNavigator(true);
      }

      if (this.getTarget() instanceof Player p && p.isCreative()) {
         this.setTarget(null);
         this.setAggressive(false);
      }

      if (this.getTarget() != null && !this.isAgressive()) {
         this.setAggressive(true);
      }

      boolean singing = this.isActuallySinging() && !this.isAgressive() && !this.isInWater() && this.onGround();
      if (singing && this.singProgress < 20.0F) {
         this.singProgress++;
      } else if (!singing && this.singProgress > 0.0F) {
         this.singProgress--;
      }

      boolean swimming = this.isSirenSwimming();
      if (swimming && this.swimProgress < 20.0F) {
         this.swimProgress++;
      } else if (!swimming && this.swimProgress > 0.0F) {
         this.swimProgress -= 0.5F;
      }

      if (!this.level().isClientSide && !AirWaterUtils.isStone(this) && this.isActuallySinging()) {
         this.checkForPrey();
         this.updateLure();
      }

      if (!this.level().isClientSide && AirWaterUtils.isStone(this) && this.isSinging()) {
         this.setSinging(false);
      }

      if (this.isActuallySinging() && !this.isInWater() && this.getRandom().nextInt(3) == 0) {
         this.yBodyRot = this.getYRot();
         float radius = -0.9F;
         float angle = (float)(Math.PI / 180.0) * this.yBodyRot - 3.0F;
         double extraX = (double)(radius * Mth.sin((float)(Math.PI + (double)angle)));
         double extraY = 1.2F;
         double extraZ = (double)(radius * Mth.cos(angle));
         AirWaterEntities.spawnParticle("siren_music", this.level(), this.getX() + extraX + (double)this.random.nextFloat() - 0.5,
            this.getY() + extraY + (double)this.random.nextFloat() - 0.5, this.getZ() + extraZ + (double)this.random.nextFloat() - 0.5, 0.0, 0.0, 0.0);
      }

      if (this.isActuallySinging() && !this.isInWater() && this.tickCount % 200 == 0) {
         this.playSound(AirWaterRefs.sound("siren_song"), 2.0F, 1.0F);
      }

      AnimationHandler.INSTANCE.updateAnimations(this);
   }

   private void checkForPrey() {
      this.setSinging(true);
   }

   public static boolean isWearingEarplugs(LivingEntity entity) {
      ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
      return AirWaterRefs.is(helmet, "earplugs") || !helmet.isEmpty() && helmet.getItem().getDescriptionId().contains("earmuff");
   }

   @Override
   public boolean hurt(DamageSource source, float amount) {
      if (source.getEntity() instanceof LivingEntity attacker) {
         this.triggerOtherSirens(attacker);
      }
      return super.hurt(source, amount);
   }

   public void triggerOtherSirens(LivingEntity aggressor) {
      for (Entity entity : this.level().getEntities(this, this.getBoundingBox().inflate(12.0, 12.0, 12.0))) {
         if (entity instanceof EntitySiren siren) {
            siren.setTarget(aggressor);
            siren.setAggressive(true);
            siren.setSinging(false);
         }
      }
   }

   public void updateLure() {
      if (this.tickCount % 20 == 0) {
         for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(50.0, 12.0, 50.0), SIREN_PREY)) {
            SirenEntityProperties sirenProps = SirenEntityProperties.get(entity);
            if (!isWearingEarplugs(entity) && sirenProps != null && (!sirenProps.isCharmed || sirenProps.getSiren(this.level()) == null)) {
               sirenProps.isCharmed = true;
               sirenProps.sirenID = this.getId();
            }
         }
      }
   }

   @Override
   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("HairColor", this.getHairColor());
      tag.putBoolean("Aggressive", this.isAgressive());
      tag.putInt("SingingPose", this.getSingingPose());
      tag.putBoolean("Singing", this.isSinging());
      tag.putBoolean("Swimming", this.isSirenSwimming());
      tag.putBoolean("Passive", this.isCharmed());
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setHairColor(tag.getInt("HairColor"));
      this.setAggressive(tag.getBoolean("Aggressive"));
      this.setSingingPose(tag.getInt("SingingPose"));
      this.setSinging(tag.getBoolean("Singing"));
      this.setSirenSwimming(tag.getBoolean("Swimming"));
      this.setCharmed(tag.getBoolean("Passive"));
   }

   public boolean isSinging() {
      return this.level().isClientSide ? (this.isSinging = this.entityData.get(SINGING)) : this.isSinging;
   }

   public boolean wantsToSing() {
      return this.isSinging() && this.isInWater() && !this.isAgressive();
   }

   public boolean isActuallySinging() {
      return this.isSinging() && !this.wantsToSing();
   }

   public void setSinging(boolean singing) {
      if (this.singCooldown > 0) {
         singing = false;
      }
      this.entityData.set(SINGING, singing);
      if (!this.level().isClientSide) {
         this.isSinging = singing;
         if (this.isAddedToLevel()) {
            PacketDistributor.sendToPlayersTrackingEntity(this, new AirWaterNetwork.SirenSong(this.getId(), singing));
         }
      }
   }

   /** 1.7.1 isSwimming (renamed: 1.21 Entity.isSwimming is the swimming pose) */
   public boolean isSirenSwimming() {
      return this.level().isClientSide ? (this.isSwimming = this.entityData.get(SWIMMING)) : this.isSwimming;
   }

   public void setSirenSwimming(boolean swimming) {
      this.entityData.set(SWIMMING, swimming);
      if (!this.level().isClientSide) {
         this.isSwimming = swimming;
      }
   }

   @Override
   public void setAggressive(boolean aggressive) {
      if (this.entityData != null) {
         this.entityData.set(AGGRESSIVE, aggressive);
      }
   }

   public boolean isAgressive() {
      return this.entityData.get(AGGRESSIVE);
   }

   public void setCharmed(boolean charmed) {
      this.entityData.set(CHARMED, charmed);
   }

   public boolean isCharmed() {
      return this.entityData.get(CHARMED);
   }

   public void setHairColor(int hairColor) {
      this.entityData.set(HAIR_COLOR, hairColor);
   }

   public int getHairColor() {
      return this.entityData.get(HAIR_COLOR);
   }

   public void setSingingPose(int pose) {
      this.entityData.set(SING_POSE, Mth.clamp(pose, 0, 2));
   }

   public int getSingingPose() {
      return this.entityData.get(SING_POSE);
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(HAIR_COLOR, 0);
      builder.define(SING_POSE, 0);
      builder.define(AGGRESSIVE, false);
      builder.define(SINGING, false);
      builder.define(SWIMMING, false);
      builder.define(CHARMED, false);
      builder.define(CLIMBING, (byte)0);
   }

   @Nullable
   @Override
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData data) {
      data = super.finalizeSpawn(level, difficulty, spawnType, data);
      this.setHairColor(this.getRandom().nextInt(3));
      this.setSingingPose(this.getRandom().nextInt(3));
      return data;
   }

   @Override
   public int getAnimationTick() {
      return this.animationTick;
   }

   @Override
   public void setAnimationTick(int tick) {
      this.animationTick = tick;
   }

   @Override
   public Animation getAnimation() {
      return this.currentAnimation;
   }

   @Override
   public void setAnimation(Animation animation) {
      this.currentAnimation = animation;
   }

   @Override
   public Animation[] getAnimations() {
      return new Animation[]{NO_ANIMATION, ANIMATION_BITE, ANIMATION_PULL};
   }

   @Nullable
   @Override
   protected SoundEvent getAmbientSound() {
      return AirWaterRefs.sound(this.isAgressive() ? "naga_idle" : "mermaid_idle");
   }

   @Nullable
   @Override
   protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
      return AirWaterRefs.sound(this.isAgressive() ? "naga_hurt" : "mermaid_hurt");
   }

   @Nullable
   @Override
   protected SoundEvent getDeathSound() {
      return AirWaterRefs.sound(this.isAgressive() ? "naga_die" : "mermaid_die");
   }

   public static boolean isDrawnToSong(Entity entity) {
      return entity instanceof Player p && !p.isCreative() || entity instanceof Villager || entity instanceof IHearsSiren;
   }

   /** EntitySiren.travel: its own water movement (speed 0.1, drag 0.8 * 0.9) on the server, vanilla on land. */
   @Override
   public void travel(Vec3 move) {
      if (this.isEffectiveAi()) {
         if (this.isInWater()) {
            this.moveRelative(0.1F, move);
            float f4 = 0.8F;
            float d0 = (float)this.getAttributeValue(Attributes.WATER_MOVEMENT_EFFICIENCY) * 3.0F;
            if (d0 > 3.0F) {
               d0 = 3.0F;
            }
            if (!this.onGround()) {
               d0 *= 0.5F;
            }
            if (d0 > 0.0F) {
               f4 += (0.54600006F - f4) * d0 / 3.0F;
            }
            this.move(MoverType.SELF, this.getDeltaMovement());
            Vec3 m = this.getDeltaMovement();
            this.setDeltaMovement(m.x * f4 * 0.900000011920929, m.y * 0.900000011920929 * f4, m.z * 0.900000011920929 * f4);
            this.calculateEntityAnimation(false);
         } else {
            super.travel(move);
         }
      }
   }

   class SwimmingMoveHelper extends LegacyMoveControl {
      private final EntitySiren siren = EntitySiren.this;

      SwimmingMoveHelper() {
         super(EntitySiren.this);
      }

      @Override
      public void tick() {
         if (this.isMoveTo() && !this.siren.getNavigation().isDone() && !this.siren.isVehicle()) {
            double distanceX = this.wantedX - this.siren.getX();
            double distanceY = this.wantedY - this.siren.getY();
            double distanceZ = this.wantedZ - this.siren.getZ();
            double distance = Math.abs(distanceX * distanceX + distanceZ * distanceZ);
            double distanceWithY = Math.sqrt(distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ);
            distanceY /= distanceWithY;
            float angle = (float)(Math.atan2(distanceZ, distanceX) * 180.0 / Math.PI) - 90.0F;
            this.siren.setYRot(this.rotlerp(this.siren.getYRot(), angle, 30.0F));
            this.siren.setSpeed(1.0F);
            Vec3 m = this.siren.getDeltaMovement();
            double my = m.y + (double)this.siren.getSpeed() * distanceY * 0.1;
            double mx = m.x;
            double mz = m.z;
            if (distance < (double)Math.max(1.0F, this.mob.getBbWidth())) {
               float f = this.siren.getYRot() * (float)(Math.PI / 180.0);
               mx -= (double)(Mth.sin(f) * 0.35F);
               mz += (double)(Mth.cos(f) * 0.35F);
            }
            this.siren.setDeltaMovement(mx, my, mz);
         } else if (this.isJumping()) {
            this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
            if (this.mob.onGround()) {
               this.setWait();
            }
         } else {
            this.siren.setSpeed(0.0F);
         }
      }
   }
}
