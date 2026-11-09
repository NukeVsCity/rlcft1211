package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.ai.DeathWormAIFindSandTarget;
import com.github.alexthe666.iceandfire.entity.ai.DeathWormAIGetInSand;
import com.github.alexthe666.iceandfire.entity.ai.DeathWormAITarget;
import com.github.alexthe666.iceandfire.entity.ai.DeathWormAIWander;
import com.github.alexthe666.iceandfire.entity.ai.DeathwormAITargetItems;
import com.github.alexthe666.iceandfire.entity.ai.PathNavigateDeathWormLand;
import com.github.alexthe666.iceandfire.entity.ai.PathNavigateDeathWormSand;
import com.github.alexthe666.iceandfire.entity.ai.goal112.EntityAIAttackMelee;
import com.github.alexthe666.iceandfire.entity.ai.goal112.EntityAIHurtByTarget;
import com.github.alexthe666.iceandfire.entity.ai.goal112.EntityAIOwnerHurtByTarget;
import com.github.alexthe666.iceandfire.entity.ai.goal112.EntityAIOwnerHurtTarget;
import javax.annotation.Nullable;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.ilexiconn.llibrary.server.animation.AnimationHandler;
import net.ilexiconn.llibrary.server.animation.IAnimatedEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.event.EventHooks;

/** Ice and Fire 1.7.1 EntityDeathWorm. */
public class EntityDeathWorm extends TamableAnimal implements IBlacklistedFromStatues, IAnimatedEntity, IVillagerFear, IAnimalFear, IPhasesThroughBlock {
   private int animationTick;
   private boolean willExplode = false;
   private int ticksTillExplosion = 60;
   private Animation currentAnimation;
   private LandPart[] segments = new LandPart[6];
   /** client: LLibrary ChainBuffer of the tail (created and updated by the client code) */
   public Object tail_buffer;
   private boolean isSandNavigator;
   private float sizeScale = 1.0F;
   private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(EntityDeathWorm.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(EntityDeathWorm.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Byte> CONTROL_STATE = SynchedEntityData.defineId(EntityDeathWorm.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Integer> WORM_AGE = SynchedEntityData.defineId(EntityDeathWorm.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<BlockPos> HOME = SynchedEntityData.defineId(EntityDeathWorm.class, EntityDataSerializers.BLOCK_POS);
   public static final Animation ANIMATION_BITE = Animation.create(10);
   public static final ResourceKey<LootTable> TAN_LOOT = key("deathworm_tan");
   public static final ResourceKey<LootTable> WHITE_LOOT = key("deathworm_white");
   public static final ResourceKey<LootTable> RED_LOOT = key("deathworm_red");
   public static final ResourceKey<LootTable> TAN_GIANT_LOOT = key("deathworm_tan_giant");
   public static final ResourceKey<LootTable> WHITE_GIANT_LOOT = key("deathworm_white_giant");
   public static final ResourceKey<LootTable> RED_GIANT_LOOT = key("deathworm_red_giant");

   private static ResourceKey<LootTable> key(String name) {
      return ResourceKey.create(Registries.LOOT_TABLE, IafLand.rl(name));
   }

   public EntityDeathWorm(EntityType<? extends EntityDeathWorm> type, Level worldIn) {
      super(type, worldIn);
      this.noCulling = true;
      this.switchNavigator(false);
      this.goalSelector.addGoal(0, new EntityAIAttackMelee(this, 1.5, true));
      this.goalSelector.addGoal(1, new DeathWormAIFindSandTarget(this, 10));
      this.goalSelector.addGoal(2, new DeathWormAIGetInSand(this, 1.0));
      this.goalSelector.addGoal(3, new DeathWormAIWander(this, 1.0));
      this.targetSelector.addGoal(1, new EntityAIOwnerHurtByTarget(this));
      this.targetSelector.addGoal(2, new EntityAIOwnerHurtTarget(this));
      this.targetSelector.addGoal(3, new EntityAIHurtByTarget(this, false));
      this.targetSelector.addGoal(3, new DeathwormAITargetItems(this, false, false));
      this.targetSelector
         .addGoal(
            5,
            new DeathWormAITarget<>(
               this,
               LivingEntity.class,
               false,
               input -> this.isTame()
                  ? input instanceof Monster
                  : (IceAndFire.CONFIG.deathWormAttackMonsters ? input instanceof LivingEntity : input instanceof Animal || input instanceof Player)
                     && LandUtils.isAlive(input)
                     && (!LandUtils.isDragon(input) || !LandUtils.isModelDead(input))
                     && !this.isOwnedBy(input)
            )
         );
      this.initSegments(1.0F);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes()
         .add(Attributes.MOVEMENT_SPEED, 0.15)
         .add(Attributes.ATTACK_DAMAGE, IceAndFire.CONFIG.deathWormAttackStrength)
         .add(Attributes.MAX_HEALTH, IceAndFire.CONFIG.deathWormMaxHealth)
         .add(Attributes.FOLLOW_RANGE, (double)Math.min(2048, IceAndFire.CONFIG.deathWormTargetSearchLength))
         .add(Attributes.ARMOR, 3.0)
         .add(Attributes.STEP_HEIGHT, 1.0);
   }

   /** getCanSpawnHere: sand below, the config roll, light above 8, then the 1.12 EntityLiving checks. */
   public static boolean checkDeathWormSpawnRules(EntityType<? extends Mob> type, LevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
      BlockState below = level.getBlockState(pos.below());
      return (below.is(Blocks.SAND) || below.is(Blocks.RED_SAND))
         && random.nextInt(1 + IceAndFire.CONFIG.deathWormSpawnCheckChance) == 0
         && level.getMaxLocalRawBrightness(pos) > 8;
   }

   @Override
   protected void registerGoals() {
      // the 1.12 worm added its AI in the constructor
   }

   public void onUpdateParts() {
      for (LandPart entity : this.segments) {
         if (entity != null) {
            entity.onUpdate();
         }
      }
   }

   @Override
   protected int getBaseExperienceReward() {
      return (int)(this.getScaleForAge() * 5.0F) + this.level().random.nextInt(10);
   }

   public void initSegments(float scale) {
      this.setScaleForAge(false);
      this.segments = new LandPart[11];

      for (int i = 0; i < this.segments.length; i++) {
         this.segments[i] = new LandPart(this, (-0.8F - (float)i * 0.8F) * scale, 0.0F, 0.0F, 0.7F * scale, 0.7F * scale, 1.0F);
      }
   }

   private void clearSegments() {
   }

   public void setExplosive(boolean explosive) {
      this.willExplode = true;
      this.ticksTillExplosion = 60;
   }

   public void setScaleForAge(boolean baby) {
      this.setScale(Math.min(this.getScaleForAge(), 7.0F));
   }

   /** 1.12 EntityAgeable.setScale: the hit box is the 0.8 x 0.8 base times this scale. */
   protected void setScale(float scale) {
      if (scale != this.sizeScale) {
         this.sizeScale = scale;
         this.refreshDimensions();
      }
   }

   @Override
   protected EntityDimensions getDefaultDimensions(Pose pose) {
      return this.getType().getDimensions().scale(this.sizeScale);
   }

   @Override
   public boolean doHurtTarget(Entity entityIn) {
      if (this.getAnimation() != ANIMATION_BITE) {
         this.setAnimation(ANIMATION_BITE);
         this.playSound(this.getScaleForAge() > 3.0F ? IafLand.sound("deathworm_giant_attack") : IafLand.sound("deathworm_attack"), 1.0F, 1.0F);
      }

      if (this.getRandom().nextInt(3) == 0 && this.getScaleForAge() > 1.0F && this.level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_MOBGRIEFING)) {
         SandExplosion explosion = new SandExplosion(this.level(), this, entityIn.getX(), entityIn.getY(), entityIn.getZ(), this.getScaleForAge());
         explosion.doExplosionA();
         explosion.doExplosionB(true);
      }

      return false;
   }

   @Override
   public void die(DamageSource cause) {
      this.clearSegments();
      super.die(cause);
   }

   /** onDeath: the drops are moved up to the sand surface (done in EventLivingLand.onDeathWormDrops). */
   public void moveDropToSurface(net.minecraft.world.entity.item.ItemEntity item) {
      item.setPos(item.getX(), (double)this.getSurface((int)item.getX(), (int)item.getY(), (int)item.getZ()), item.getZ());
   }

   @Override
   public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource source) {
      return false;
   }

   @Override
   protected ResourceKey<LootTable> getDefaultLootTable() {
      switch (this.getVariant()) {
         case 0:
            return this.getScaleForAge() > 3.0F ? TAN_GIANT_LOOT : TAN_LOOT;
         case 1:
            return this.getScaleForAge() > 3.0F ? WHITE_GIANT_LOOT : WHITE_LOOT;
         case 2:
            return this.getScaleForAge() > 3.0F ? RED_GIANT_LOOT : RED_LOOT;
         default:
            return null;
      }
   }

   @Nullable
   @Override
   public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob ageable) {
      return null;
   }

   @Override
   public boolean isFood(ItemStack stack) {
      return false;
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(VARIANT, 0);
      builder.define(SCALE, 1.0F);
      builder.define(CONTROL_STATE, (byte)0);
      builder.define(WORM_AGE, 10);
      builder.define(HOME, BlockPos.ZERO);
   }

   @Override
   public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
      super.onSyncedDataUpdated(key);
      // MessageDeathWormHitbox: the client rebuilt the segments with scale * age / 5
      if (this.level().isClientSide && (SCALE.equals(key) || WORM_AGE.equals(key))) {
         this.initSegments(this.getScaleForAge());
      }
   }

   @Override
   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      compound.putInt("Variant", this.getVariant());
      compound.putFloat("Scale", this.getDeathWormScale());
      compound.putInt("WormAge", this.getWormAge());
      compound.putLong("WormHome", this.getWormHome().asLong());
      compound.putBoolean("WillExplode", this.willExplode);
   }

   @Override
   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      this.setVariant(compound.getInt("Variant"));
      this.setDeathWormScale(compound.getFloat("Scale"));
      this.setWormAge(compound.getInt("WormAge"));
      this.setWormHome(BlockPos.of(compound.getLong("WormHome")));
      this.willExplode = compound.getBoolean("WillExplode");
   }

   private void setStateField(int i, boolean newState) {
      byte prevState = this.entityData.get(CONTROL_STATE);
      if (newState) {
         this.entityData.set(CONTROL_STATE, (byte)(prevState | 1 << i));
      } else {
         this.entityData.set(CONTROL_STATE, (byte)(prevState & ~(1 << i)));
      }
   }

   public byte getControlState() {
      return this.entityData.get(CONTROL_STATE);
   }

   public void setControlState(byte state) {
      this.entityData.set(CONTROL_STATE, state);
   }

   public int getVariant() {
      return this.entityData.get(VARIANT);
   }

   public void setVariant(int variant) {
      this.entityData.set(VARIANT, variant);
   }

   public BlockPos getWormHome() {
      return this.entityData.get(HOME);
   }

   public void setWormHome(BlockPos home) {
      if (home != null) {
         this.entityData.set(HOME, home);
      }
   }

   public int getWormAge() {
      return Math.max(1, this.entityData.get(WORM_AGE));
   }

   public void setWormAge(int age) {
      this.entityData.set(WORM_AGE, age);
   }

   /** 1.12 getScale() (the SCALE data parameter; renamed because 1.21 LivingEntity has its own getScale). */
   public float getDeathWormScale() {
      return this.entityData.get(SCALE);
   }

   public float getScaleForAge() {
      return this.getDeathWormScale() * ((float)this.getWormAge() / 5.0F);
   }

   public void setDeathWormScale(float scale) {
      this.entityData.set(SCALE, scale);
      this.updateAttributes();
      this.clearSegments();
      if (!this.level().isClientSide) {
         this.initSegments(scale * ((float)this.getWormAge() / 5.0F));
      }
   }

   @Nullable
   @Override
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData livingdata) {
      livingdata = super.finalizeSpawn(level, difficulty, spawnType, livingdata);
      this.setVariant(this.getRandom().nextInt(3));
      float size = 0.25F + (float)(Math.random() * 0.35F);
      this.setDeathWormScale(this.getRandom().nextInt(20) == 0 ? size * 4.0F : size);
      if (this.isSandBelow() && this.getScaleForAge() != 1.0F) {
         this.setDeltaMovement(this.getDeltaMovement().x, -0.5, this.getDeltaMovement().z);
      }

      return livingdata;
   }

   /** updatePassenger */
   @Override
   protected void positionRider(Entity passenger, Entity.MoveFunction callback) {
      super.positionRider(passenger, callback);
      if (this.hasPassenger(passenger)) {
         this.yBodyRot = this.getYRot();
         this.setYRot(passenger.getYRot());
         float radius = -0.5F * this.getScaleForAge();
         float angle = (float)(Math.PI / 180.0) * this.yBodyRot;
         double extraX = (double)(radius * Mth.sin((float)(Math.PI + (double)angle)));
         double extraZ = (double)(radius * Mth.cos(angle));
         passenger.setPos(this.getX() + extraX, this.getY() + (double)this.getEyeHeight() - 0.55F, this.getZ() + extraZ);
      }
   }

   @Nullable
   @Override
   public LivingEntity getControllingPassenger() {
      for (Entity passenger : this.getPassengers()) {
         if (passenger instanceof Player) {
            return (Player)passenger;
         }
      }

      return null;
   }

   @Override
   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      if (this.getWormAge() > 4
         && !player.isPassenger()
         && player.getMainHandItem().getItem() == Items.FISHING_ROD
         && player.getOffhandItem().getItem() == Items.FISHING_ROD
         && !this.level().isClientSide) {
         player.startRiding(this);
         return InteractionResult.SUCCESS;
      } else {
         return super.mobInteract(player, hand);
      }
   }

   private void switchNavigator(boolean inSand) {
      if (inSand) {
         this.moveControl = new EntityDeathWorm.SandMoveHelper();
         this.navigation = new PathNavigateDeathWormSand(this, this.level());
         this.isSandNavigator = true;
      } else {
         this.moveControl = new MoveControl(this);
         this.navigation = new PathNavigateDeathWormLand(this, this.level());
         this.isSandNavigator = false;
      }
   }

   @Override
   public boolean hurt(DamageSource source, float amount) {
      if (!source.is(DamageTypes.IN_WALL) && !source.is(DamageTypes.FALLING_BLOCK)) {
         return this.isVehicle() && source.getEntity() != null && this.getControllingPassenger() != null && source.getEntity() == this.getControllingPassenger()
            ? false
            : super.hurt(source, amount);
      } else {
         return false;
      }
   }

   /** isEntityInsideOpaqueBlock: sand does not suffocate the worm. */
   @Override
   public boolean isInWall() {
      BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

      for (int i = 0; i < 8; i++) {
         int j = Mth.floor(this.getY() + (double)(((float)((i >> 0) % 2) - 0.5F) * 0.1F) + (double)this.getEyeHeight());
         int k = Mth.floor(this.getX() + (double)(((float)((i >> 1) % 2) - 0.5F) * this.getBbWidth() * 0.8F));
         int l = Mth.floor(this.getZ() + (double)(((float)((i >> 2) % 2) - 0.5F) * this.getBbWidth() * 0.8F));
         if (pos.getX() != k || pos.getY() != j || pos.getZ() != l) {
            pos.set(k, j, l);
            BlockState state = this.level().getBlockState(pos);
            if (state.isSuffocating(this.level(), pos) && !IafLand.isSandMaterial(state)) {
               return true;
            }
         }
      }

      return false;
   }

   /** pushOutOfBlocks (used while ridden) */
   protected boolean pushOutOfBlocks(double x, double y, double z) {
      BlockPos blockpos = BlockPos.containing(x, y, z);
      double d0 = x - (double)blockpos.getX();
      double d1 = y - (double)blockpos.getY();
      double d2 = z - (double)blockpos.getZ();
      if (this.level().noCollision(this, this.getBoundingBox())) {
         return false;
      } else {
         Direction enumfacing = Direction.UP;
         double d3 = Double.MAX_VALUE;
         if (!this.isBlockFullCube(blockpos.west()) && d0 < d3) {
            d3 = d0;
            enumfacing = Direction.WEST;
         }

         if (!this.isBlockFullCube(blockpos.east()) && 1.0 - d0 < d3) {
            d3 = 1.0 - d0;
            enumfacing = Direction.EAST;
         }

         if (!this.isBlockFullCube(blockpos.north()) && d2 < d3) {
            d3 = d2;
            enumfacing = Direction.NORTH;
         }

         if (!this.isBlockFullCube(blockpos.south()) && 1.0 - d2 < d3) {
            d3 = 1.0 - d2;
            enumfacing = Direction.SOUTH;
         }

         if (!this.isBlockFullCube(blockpos.above()) && 1.0 - d1 < d3) {
            d3 = 1.0 - d1;
            enumfacing = Direction.UP;
         }

         float f = this.random.nextFloat() * 0.2F + 0.1F;
         float f1 = (float)enumfacing.getAxisDirection().getStep();
         Vec3 m = this.getDeltaMovement();
         if (enumfacing.getAxis() == Direction.Axis.X) {
            this.setDeltaMovement((double)(f1 * f), m.y * 0.75, m.z * 0.75);
         } else if (enumfacing.getAxis() == Direction.Axis.Y) {
            this.setDeltaMovement(m.x * 0.75, (double)(f1 * f), m.z * 0.75);
         } else if (enumfacing.getAxis() == Direction.Axis.Z) {
            this.setDeltaMovement(m.x * 0.75, m.y * 0.75, (double)(f1 * f));
         }

         return true;
      }
   }

   private boolean isBlockFullCube(BlockPos pos) {
      BlockState state = this.level().getBlockState(pos);
      VoxelShape shape = state.getCollisionShape(this.level(), pos);
      if (IafLand.isSandMaterial(state) || shape.isEmpty()) {
         return false;
      }
      var b = shape.bounds();
      return (b.getXsize() + b.getYsize() + b.getZsize()) / 3.0 >= 1.0;
   }

   private void updateAttributes() {
      this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(Math.min(0.2, 0.15 * (double)this.getScaleForAge()));
      this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(Math.max(1.0, IceAndFire.CONFIG.deathWormAttackStrength * (double)this.getScaleForAge()));
      this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(Math.max(6.0, IceAndFire.CONFIG.deathWormMaxHealth * (double)this.getScaleForAge()));
      this.heal(30.0F * this.getScaleForAge());
   }

   /** onKillEntity */
   @Override
   public boolean killedEntity(ServerLevel level, LivingEntity entityLivingIn) {
      if (this.isTame()) {
         this.heal(14.0F);
      }

      return super.killedEntity(level, entityLivingIn);
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (this.willExplode) {
         if (this.ticksTillExplosion == 0) {
            this.level()
               .explode(
                  null,
                  this.getX(),
                  this.getY(),
                  this.getZ(),
                  2.5F * this.getScaleForAge(),
                  false,
                  EventHooks.canEntityGrief(this.level(), this) ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE
               );
         } else {
            this.ticksTillExplosion--;
         }
      }

      if (this.tickCount == 1) {
         this.initSegments(this.getScaleForAge());
      }

      if (this.tickCount % 1000 == 0 && this.getWormAge() < 5) {
         this.setWormAge(Math.min(5, this.getWormAge() + 1));
         this.clearSegments();
         this.heal(15.0F);
         this.setDeathWormScale(this.getDeathWormScale());
         if (this.level().isClientSide) {
            for (int i = 0; (float)i < 10.0F * this.getScaleForAge(); i++) {
               this.level()
                  .addParticle(
                     ParticleTypes.HAPPY_VILLAGER,
                     this.getX() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
                     (double)((float)this.getSurface((int)Math.floor(this.getX()), (int)Math.floor(this.getY()), (int)Math.floor(this.getZ())) + 0.5F),
                     this.getZ() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
                     this.random.nextGaussian() * 0.02,
                     this.random.nextGaussian() * 0.02,
                     this.random.nextGaussian() * 0.02
                  );

               for (int j = 0; j < this.segments.length; j++) {
                  this.level()
                     .addParticle(
                        ParticleTypes.HAPPY_VILLAGER,
                        this.segments[j].posX + (double)(this.random.nextFloat() * this.segments[j].width * 2.0F) - (double)this.segments[j].width,
                        (double)((float)this.getSurface((int)Math.floor(this.segments[j].posX), (int)Math.floor(this.segments[j].posY), (int)Math.floor(this.segments[j].posZ)) + 0.5F),
                        this.segments[j].posZ + (double)(this.random.nextFloat() * this.segments[j].width * 2.0F) - (double)this.segments[j].width,
                        this.random.nextGaussian() * 0.02,
                        this.random.nextGaussian() * 0.02,
                        this.random.nextGaussian() * 0.02
                     );
               }
            }
         }
      }

      if (this.getControllingPassenger() != null) {
         if (this.isInWall()) {
            this.setDeltaMovement(this.getDeltaMovement().x, 2.0, this.getDeltaMovement().z);
         } else {
            this.noPhysics = false;
         }
      }

      if (this.getControllingPassenger() != null && this.getTarget() != null) {
         this.getNavigation().stop();
         this.setTarget(null);
      }

      if (this.getTarget() == null) {
         this.setXRot(0.0F);
      } else {
         this.lookAt(this.getTarget(), 10.0F, 10.0F);
         double dist = this.distanceToSqr(this.getTarget());
         if (dist >= 4.0 * (double)this.getScaleForAge() && dist <= 16.0 * (double)this.getScaleForAge() && (this.isInSand() || this.onGround())) {
            double d0 = this.getTarget().getX() - this.getX();
            double d1 = this.getTarget().getZ() - this.getZ();
            float leap = Mth.sqrt((float)(d0 * d0 + d1 * d1));
            Vec3 m = this.getDeltaMovement();
            double mx = m.x;
            double mz = m.z;
            if ((double)leap >= 1.0E-4) {
               mx = mx + d0 / (double)leap * 0.8F + mx * 0.2F;
               mz = mz + d1 / (double)leap * 0.8F + mz * 0.2F;
            }

            this.setDeltaMovement(mx, 0.5, mz);
            this.setAnimation(ANIMATION_BITE);
         }

         if (dist < Math.min(4.0, 4.0 * (double)this.getScaleForAge()) && this.getAnimation() == ANIMATION_BITE) {
            float f = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
            this.getTarget().hurt(this.damageSources().mobAttack(this), f);
            Vec3 m = this.getDeltaMovement();
            this.setDeltaMovement(m.x, m.y / 2.0 - 0.4, m.z);
         }
      }
   }

   public boolean canBeSteered() {
      return true;
   }

   public int getSurface(int x, int y, int z) {
      BlockPos pos = new BlockPos(x, y, z);

      while (!this.level().isEmptyBlock(pos) && pos.getY() < this.level().getMaxBuildHeight()) {
         pos = pos.above();
      }

      return pos.getY();
   }

   @Nullable
   @Override
   protected SoundEvent getAmbientSound() {
      return this.getScaleForAge() > 3.0F ? IafLand.sound("deathworm_giant_idle") : IafLand.sound("deathworm_idle");
   }

   @Nullable
   @Override
   protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
      return this.getScaleForAge() > 3.0F ? IafLand.sound("deathworm_giant_hurt") : IafLand.sound("deathworm_hurt");
   }

   @Nullable
   @Override
   protected SoundEvent getDeathSound() {
      return this.getScaleForAge() > 3.0F ? IafLand.sound("deathworm_giant_die") : IafLand.sound("deathworm_die");
   }

   @Override
   public void tick() {
      super.tick();
      this.setScaleForAge(false);
      this.onUpdateParts();
      if (this.attack() && this.getControllingPassenger() != null && this.getControllingPassenger() instanceof Player) {
         LivingEntity target = LandUtils.riderLookingAtEntity(this, this.getControllingPassenger(), 3.0);
         if (this.getAnimation() != ANIMATION_BITE) {
            this.setAnimation(ANIMATION_BITE);
            this.playSound(this.getScaleForAge() > 3.0F ? IafLand.sound("deathworm_giant_attack") : IafLand.sound("deathworm_attack"), 1.0F, 1.0F);
            if (this.getRandom().nextInt(3) == 0 && this.getScaleForAge() > 1.0F) {
               float radius = 1.5F * this.getScaleForAge();
               float angle = (float)(Math.PI / 180.0) * this.yBodyRot;
               double extraX = (double)(radius * Mth.sin((float)(Math.PI + (double)angle)));
               double extraZ = (double)(radius * Mth.cos(angle));
               SandExplosion explosion = new SandExplosion(
                  this.level(), this, this.getX() + extraX, this.getY() - (double)this.getEyeHeight(), this.getZ() + extraZ, this.getScaleForAge() * 0.75F
               );
               explosion.doExplosionA();
               explosion.doExplosionB(true);
            }
         }

         if (target != null) {
            target.hurt(this.damageSources().mobAttack(this), (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE)));
         }
      }

      if (!this.isInSand()) {
         this.noPhysics = false;
      } else {
         BlockState state = this.level()
            .getBlockState(BlockPos.containing(this.getX(), (double)this.getSurface((int)Math.floor(this.getX()), (int)Math.floor(this.getY()), (int)Math.floor(this.getZ())), this.getZ()).below());
         if (state.isSolidRender(this.level(), this.blockPosition()) && this.level().isClientSide) {
            BlockParticleOption crack = new BlockParticleOption(ParticleTypes.BLOCK, state);
            this.level()
               .addParticle(
                  crack,
                  this.getX() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
                  (double)((float)this.getSurface((int)Math.floor(this.getX()), (int)Math.floor(this.getY()), (int)Math.floor(this.getZ())) + 0.5F),
                  this.getZ() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
                  this.random.nextGaussian() * 0.02,
                  this.random.nextGaussian() * 0.02,
                  this.random.nextGaussian() * 0.02
               );

            for (int i = 0; i < this.segments.length; i++) {
               this.level()
                  .addParticle(
                     crack,
                     this.segments[i].prevPosX + (double)(this.random.nextFloat() * this.segments[i].width * 2.0F) - (double)this.segments[i].width,
                     (double)((float)this.getSurface((int)Math.floor(this.segments[i].prevPosX), (int)Math.floor(this.segments[i].prevPosY), (int)Math.floor(this.segments[i].prevPosZ)) + 0.5F),
                     this.segments[i].prevPosZ + (double)(this.random.nextFloat() * this.segments[i].width * 2.0F) - (double)this.segments[i].width,
                     this.random.nextGaussian() * 0.02,
                     this.random.nextGaussian() * 0.02,
                     this.random.nextGaussian() * 0.02
                  );
            }
         }

         if (this.tickCount % 10 == 0) {
            this.playSound(SoundEvents.SAND_BREAK, 1.0F, 0.5F);
         }
      }

      if (this.up() && this.onGround()) {
         this.jumpFromGround();
      }

      if (this.isInSand() && !this.isSandNavigator) {
         this.switchNavigator(true);
      }

      if (!this.isInSandStrict() && this.isSandNavigator) {
         this.switchNavigator(false);
      }

      if (this.level().isClientSide) {
         com.github.alexthe666.iceandfire.client.LandClientHooks.deathWormTail(this);
      }

      if (this.getControllingPassenger() != null) {
         this.noPhysics = false;
         this.pushOutOfBlocks(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
         if (this.isSandBelow() && this.level().isClientSide) {
            BlockParticleOption sand = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState());
            this.level()
               .addParticle(
                  sand,
                  this.getX() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
                  (double)((float)this.getSurface((int)Math.floor(this.getX()), (int)Math.floor(this.getY()), (int)Math.floor(this.getZ())) + 0.5F),
                  this.getZ() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
                  this.random.nextGaussian() * 0.02,
                  this.random.nextGaussian() * 0.02,
                  this.random.nextGaussian() * 0.02
               );

            for (int i = 0; i < this.segments.length; i++) {
               this.level()
                  .addParticle(
                     sand,
                     this.segments[i].posX + (double)(this.random.nextFloat() * this.segments[i].width * 2.0F) - (double)this.segments[i].width,
                     (double)((float)this.getSurface((int)Math.floor(this.segments[i].posX), (int)Math.floor(this.segments[i].posY), (int)Math.floor(this.segments[i].posZ)) + 0.5F),
                     this.segments[i].posZ + (double)(this.random.nextFloat() * this.segments[i].width * 2.0F) - (double)this.segments[i].width,
                     this.random.nextGaussian() * 0.02,
                     this.random.nextGaussian() * 0.02,
                     this.random.nextGaussian() * 0.02
                  );
            }
         }
      }

      if (this.level().isClientSide) {
         com.github.alexthe666.iceandfire.client.LandClientHooks.deathWormControls(this);
      }

      if (EntityGorgon.DEBUG && !this.level().isClientSide && this.tickCount % 20 == 0) {
         com.mojang.logging.LogUtils.getLogger().info("[worm] pos {} sand {} strict {} nav {} done {} target {} scale {}", this.position(), this.isInSand(), this.isInSandStrict(),
            this.navigation.getClass().getSimpleName(), this.navigation.isDone(), this.getTarget(), this.getScaleForAge());
      }

      AnimationHandler.INSTANCE.updateAnimations(this);
   }

   public boolean up() {
      return (this.entityData.get(CONTROL_STATE) & 1) == 1;
   }

   public boolean dismount() {
      return (this.entityData.get(CONTROL_STATE) >> 1 & 1) == 1;
   }

   public boolean attack() {
      return (this.entityData.get(CONTROL_STATE) >> 2 & 1) == 1;
   }

   public void up(boolean up) {
      this.setStateField(0, up);
   }

   public void dismount(boolean dismount) {
      this.setStateField(1, dismount);
   }

   public void attack(boolean attack) {
      this.setStateField(2, attack);
   }

   public boolean isRidingPlayer(Player player) {
      return this.getControllingPassenger() != null
         && this.getControllingPassenger() instanceof Player
         && this.getControllingPassenger().getUUID().equals(player.getUUID());
   }

   public boolean isSandBelow() {
      int i = Mth.floor(this.getX());
      int j = Mth.floor(this.getY() - 1.0);
      int k = Mth.floor(this.getZ());
      return IafLand.isSandMaterial(this.level().getBlockState(new BlockPos(i, j, k)));
   }

   public boolean isInSand() {
      return this.getControllingPassenger() == null && IafLand.isSandInBB(this.level(), this.getBoundingBox().inflate(0.25, 0.25, 0.25));
   }

   public boolean isInSandStrict() {
      return this.getControllingPassenger() == null && IafLand.isSandInBB(this.level(), this.getBoundingBox());
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
      return new Animation[]{ANIMATION_BITE};
   }

   public LandPart[] getWormParts() {
      return this.segments;
   }

   @Override
   public int getHeadRotSpeed() {
      return 10;
   }

   /** ridden: strafe * 0.5, forward, no vertical input; speed is the movement attribute on the ground, 1 in the air. */
   @Override
   protected Vec3 getRiddenInput(Player controller, Vec3 input) {
      if (this.getTarget() != null) {
         this.setTarget(null);
         this.getNavigation().stop();
      }

      this.fallDistance = 0.0F;
      return new Vec3(controller.xxa * 0.5F, 0.0, controller.zza);
   }

   @Override
   protected float getRiddenSpeed(Player controller) {
      return this.onGround() ? (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) : 1.0F;
   }

   @Override
   public void travel(Vec3 travelVector) {
      if (this.isVehicle() && this.canBeSteered() && this.getControllingPassenger() != null) {
         super.travel(travelVector);
         return;
      }

      float strafe = (float)travelVector.x;
      float vertical = (float)travelVector.y;
      float forward = (float)travelVector.z;
      if (this.isEffectiveAi()) {
         if (this.isInSandStrict()) {
            this.moveRelative(0.1F, new Vec3(strafe, vertical, forward));
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
            this.setDeltaMovement(m.x * (double)f4 * 0.900000011920929, m.y * 0.900000011920929 * (double)f4, m.z * 0.900000011920929 * (double)f4);
         } else {
            super.travel(travelVector);
         }
      }

      this.calculateEntityAnimation(false);
   }

   @Override
   public boolean shouldAnimalsFear(Entity entity) {
      return true;
   }

   @Override
   public boolean canBeTurnedToStone() {
      return false;
   }

   @Override
   public boolean canPhaseThroughBlock(Level world, BlockPos pos) {
      return IafLand.isSandMaterial(world.getBlockState(pos));
   }

   /** EntityDeathWorm.SandMoveHelper */
   public class SandMoveHelper extends MoveControl {
      private EntityDeathWorm worm = EntityDeathWorm.this;

      public SandMoveHelper() {
         super(EntityDeathWorm.this);
      }

      @Override
      public void tick() {
         if (this.operation == MoveControl.Operation.MOVE_TO && !this.worm.getNavigation().isDone() && !this.worm.isVehicle()) {
            double distanceX = this.wantedX - this.worm.getX();
            double distanceY = this.wantedY - this.worm.getY();
            double distanceZ = this.wantedZ - this.worm.getZ();
            double distance = Math.abs(distanceX * distanceX + distanceZ * distanceZ);
            double distanceWithY = Math.sqrt(distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ);
            distanceY /= distanceWithY;
            float angle = (float)(Math.atan2(distanceZ, distanceX) * 180.0 / Math.PI) - 90.0F;
            this.worm.setYRot(this.rotlerp(this.worm.getYRot(), angle, 30.0F));
            this.worm.setSpeed(1.0F);
            Vec3 m = this.worm.getDeltaMovement();
            this.worm.setDeltaMovement(m.x, m.y + (double)this.worm.getSpeed() * distanceY * 0.1, m.z);
            if (distance < (double)Math.max(1.0F, this.mob.getBbWidth())) {
               float f = this.worm.getYRot() * (float)(Math.PI / 180.0);
               m = this.worm.getDeltaMovement();
               this.worm.setDeltaMovement(m.x - (double)(Mth.sin(f) * 0.35F), m.y, m.z + (double)(Mth.cos(f) * 0.35F));
            }
         } else if (this.operation == MoveControl.Operation.JUMPING) {
            this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
         } else {
            this.worm.setSpeed(0.0F);
         }
      }
   }
}
