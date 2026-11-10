package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.entity.ai.PixieAIFlee;
import com.github.alexthe666.iceandfire.entity.ai.PixieAIFollowOwner;
import com.github.alexthe666.iceandfire.entity.ai.PixieAIPickupItem;
import com.github.alexthe666.iceandfire.entity.ai.PixieAISteal;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAILookIdle;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAISwimming;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIWatchClosest;
import com.github.alexthe666.iceandfire.entity.ai.legacy.LegacyMoveControl;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;

/** Ice and Fire 1.7.1 EntityPixie. */
public class EntityPixie extends TamableAnimal implements com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAISit.Sitter {
   public static final float[][] PARTICLE_RGB = new float[][]{
      {1.0F, 0.752F, 0.792F}, {0.831F, 0.662F, 1.0F}, {0.513F, 0.843F, 1.0F}, {0.654F, 0.909F, 0.615F}, {0.996F, 0.788F, 0.407F}
   };
   @SuppressWarnings("unchecked")
   public Holder<MobEffect>[] positivePotions = new Holder[]{MobEffects.DAMAGE_BOOST, MobEffects.JUMP, MobEffects.MOVEMENT_SPEED, MobEffects.LUCK,
      MobEffects.DIG_SPEED};
   @SuppressWarnings("unchecked")
   public Holder<MobEffect>[] negativePotions = new Holder[]{MobEffects.WEAKNESS, MobEffects.CONFUSION, MobEffects.MOVEMENT_SLOWDOWN,
      MobEffects.UNLUCK, MobEffects.DIG_SLOWDOWN};
   public static final ResourceKey<LootTable> LOOT = AirWaterEntities.loot("if_pixie");
   private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(EntityPixie.class, EntityDataSerializers.INT);
   public boolean slowSpeed = false;
   public int ticksUntilHouseAI;
   private BlockPos housePos;
   private PixieAIFlee<Player> aiFlee;
   private PixieAISteal aiTempt;
   public final EntityAITasks tasks = new EntityAITasks();
   public final EntityAITasks targetTasks = new EntityAITasks();
   private boolean sitting;

   public EntityPixie(EntityType<? extends EntityPixie> type, Level level) {
      super(type, level);
      this.moveControl = new AIMoveControl(this);
      this.xpReward = 3;
      this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
      this.initEntityAI();
      com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112.install(this, this.tasks, this.targetTasks);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return TamableAnimal.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.MAX_HEALTH, 10.0);
   }

   @Override
   protected void registerGoals() {
   }

   public LegacyMoveControl getLegacyMoveControl() {
      return (LegacyMoveControl)this.moveControl;
   }

   public static BlockPos getPositionRelativetoGround(Entity entity, Level world, double x, double z, RandomSource rand) {
      BlockPos pos = BlockPos.containing(x, entity.getY(), z);
      for (int yDown = 0; yDown < 3; yDown++) {
         if (!world.isEmptyBlock(pos.below(yDown))) {
            return pos.above(yDown);
         }
      }
      return pos;
   }

   public static BlockPos findAHouse(Entity entity, Level world) {
      BlockPos origin = AirWaterUtils.pos(entity);
      for (int xSearch = -10; xSearch < 10; xSearch++) {
         for (int ySearch = -10; ySearch < 10; ySearch++) {
            for (int zSearch = -10; zSearch < 10; zSearch++) {
               BlockEntity be = world.getBlockEntity(origin.offset(xSearch, ySearch, zSearch));
               if (PixieHouses.isPixieHouse(be) && !PixieHouses.hasPixie(be)) {
                  return origin.offset(xSearch, ySearch, zSearch);
               }
            }
         }
      }
      return origin;
   }

   /** 1.12 EntityTameable sitting flag */
   public boolean isSitting() {
      return this.isInSittingPose();
   }

   @Override
   public void setSitting(boolean sitting) {
      this.setInSittingPose(sitting);
   }

   @Override
   public boolean hurt(DamageSource source, float amount) {
      if (!this.level().isClientSide && this.getRandom().nextInt(3) == 0 && !this.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()
         && !AirWaterUtils.isStone(this)) {
         this.spawnAtLocation(this.getItemInHand(InteractionHand.MAIN_HAND), 0.0F);
         this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
         return true;
      } else {
         return !this.isOwnerClose()
               || !source.is(DamageTypes.FALLING_BLOCK) && !source.is(DamageTypes.IN_WALL) && (this.getOwner() == null || source.getEntity() != this.getOwner())
            ? super.hurt(source, amount)
            : false;
      }
   }

   @Override
   public void die(DamageSource cause) {
      if (!this.level().isClientSide && !this.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
         this.spawnAtLocation(this.getItemInHand(InteractionHand.MAIN_HAND), 0.0F);
         this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
      }
      super.die(cause);
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(COLOR, 0);
   }

   @Override
   protected void doPush(Entity entityIn) {
      if (this.getOwner() != entityIn) {
         entityIn.push(this);
      }
   }

   @Override
   protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
      if (onGroundIn) {
         if (this.fallDistance > 0.0F) {
            state.getBlock().fallOn(this.level(), state, pos, this, this.fallDistance);
         }
         this.fallDistance = 0.0F;
      } else if (y < 0.0) {
         this.fallDistance = (float)((double)this.fallDistance - y);
      }
   }

   /** EntityPixie.processInteract */
   @Override
   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      ItemStack held = player.getItemInHand(hand);
      boolean flag = held.is(Items.NAME_TAG) || held.is(Items.LEAD);
      if (flag) {
         return held.interactLivingEntity(player, this, hand);
      } else if (this.isOwnedBy(player)) {
         if (held.is(Items.SUGAR) && this.getHealth() < this.getMaxHealth()) {
            this.heal(5.0F);
            held.shrink(1);
            this.playSound(AirWaterRefs.sound("pixie_taunt"), 1.0F, 1.0F);
         } else {
            this.setSitting(!this.isSitting());
         }
         return InteractionResult.sidedSuccess(this.level().isClientSide);
      } else {
         if (AirWaterRefs.is(held, "jar_empty") && !this.isTame()) {
            if (!player.isCreative()) {
               held.shrink(1);
            }
            ItemStack stack = AirWaterEntities.pixieJar(this.getColor());
            if (!this.level().isClientSide) {
               if (!this.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                  this.spawnAtLocation(this.getItemInHand(InteractionHand.MAIN_HAND), 0.0F);
               }
               this.spawnAtLocation(stack, 0.0F);
            }
            this.discard();
         }
         return super.mobInteract(player, hand);
      }
   }

   /** EntityAnimal.isBreedingItem: wheat (pixies never breed: createChild is null) */
   @Override
   public boolean isFood(ItemStack stack) {
      return stack.is(Items.WHEAT);
   }

   public void flipAI(boolean flee) {
      if (flee) {
         this.tasks.removeTask(this.aiTempt);
         this.tasks.addTask(3, this.aiFlee);
      } else {
         this.tasks.removeTask(this.aiFlee);
         this.tasks.addTask(3, this.aiTempt);
      }
   }

   @Override
   public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
      return false;
   }

   protected void initEntityAI() {
      this.tasks.addTask(0, new EntityAISwimming(this));
      this.tasks.addTask(1, new PixieAIFollowOwner(this, 1.0, 2.0F, 4.0F));
      this.tasks.addTask(1, new PixieAIPickupItem(this, false));
      this.tasks.addTask(2, this.aiTempt = new PixieAISteal(this, 1.0));
      this.tasks.addTask(2, this.aiFlee = new PixieAIFlee<>(this, Player.class, 10.0F, e -> true));
      this.tasks.addTask(3, new AIMoveRandom());
      this.tasks.addTask(4, new AIEnterHouse());
      this.tasks.addTask(6, new EntityAIWatchClosest(this, Player.class, 6.0F));
      this.tasks.addTask(7, new EntityAILookIdle(this));
      this.tasks.removeTask(this.aiFlee);
   }

   @Nullable
   @Override
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData data) {
      data = super.finalizeSpawn(level, difficulty, spawnType, data);
      this.setColor(this.random.nextInt(5));
      this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
      return data;
   }

   private boolean isBeyondHeight() {
      if (this.getY() > (double)this.level().getMaxBuildHeight()) {
         return true;
      } else {
         BlockPos height = this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, this.blockPosition());
         int maxY = 20 + height.getY();
         return this.getY() > (double)maxY;
      }
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (!this.isSitting() && !this.isBeyondHeight()) {
         this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.08, 0.0));
      } else {
         this.getLegacyMoveControl().setWait();
      }

      if (this.level().isClientSide) {
         AirWaterEntities.spawnParticle("if_pixie", this.level(),
            this.getX() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
            this.getY() + (double)(this.random.nextFloat() * this.getBbHeight()),
            this.getZ() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
            PARTICLE_RGB[this.getColor()][0], PARTICLE_RGB[this.getColor()][1], PARTICLE_RGB[this.getColor()][2]);
      }

      if (this.ticksUntilHouseAI > 0) {
         this.ticksUntilHouseAI--;
      }

      if (!this.level().isClientSide && this.housePos != null && AirWaterUtils.distanceSqToCenter(this, this.housePos) < 1.5) {
         BlockEntity be = this.level().getBlockEntity(this.housePos);
         if (PixieHouses.isPixieHouse(be)) {
            if (PixieHouses.hasPixie(be)) {
               this.housePos = null;
               this.getLegacyMoveControl().setWait();
            } else {
               PixieHouses.enter(be, this.getColor(), this.getItemInHand(InteractionHand.MAIN_HAND), this.isTame(), this.getOwnerUUID());
               this.discard();
            }
         }
      }

      if (this.getOwner() != null && this.isOwnerClose() && this.tickCount % 80 == 0) {
         this.getOwner().addEffect(new MobEffectInstance(this.positivePotions[this.getColor()], 100, 0, false, false));
      }
   }

   public int getColor() {
      return Mth.clamp(this.entityData.get(COLOR), 0, 4);
   }

   public void setColor(int color) {
      this.entityData.set(COLOR, color);
   }

   @Override
   public void readAdditionalSaveData(CompoundTag compound) {
      this.setColor(compound.getInt("Color"));
      super.readAdditionalSaveData(compound);
      this.setSitting(compound.getBoolean("Sitting"));
   }

   @Override
   public void addAdditionalSaveData(CompoundTag compound) {
      compound.putInt("Color", this.getColor());
      super.addAdditionalSaveData(compound);
      compound.putBoolean("Sitting", this.isSitting());
   }

   @Nullable
   @Override
   public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
      return null;
   }

   public BlockPos getHousePos() {
      return this.housePos;
   }

   public boolean isOwnerClose() {
      return this.isTame() && this.getOwner() != null && this.distanceToSqr(this.getOwner()) < 100.0;
   }

   @Nullable
   @Override
   protected SoundEvent getAmbientSound() {
      return AirWaterRefs.sound("pixie_idle");
   }

   @Nullable
   @Override
   protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
      return AirWaterRefs.sound("pixie_hurt");
   }

   @Nullable
   @Override
   protected SoundEvent getDeathSound() {
      return AirWaterRefs.sound("pixie_die");
   }

   @Override
   protected ResourceKey<LootTable> getDefaultLootTable() {
      return LOOT;
   }

   private boolean isDirectPathBetweenPoints(BlockPos a, BlockPos b) {
      return AirWaterUtils.rayTraceBlocks(this, new Vec3(a.getX() + 0.5, a.getY() + 0.5, a.getZ() + 0.5),
         new Vec3(b.getX() + 0.5, b.getY() + (double)this.getBbHeight() * 0.5, b.getZ() + 0.5), false) == null;
   }

   class AIEnterHouse extends EntityAIBase {
      AIEnterHouse() {
         this.setMutexBits(1);
      }

      @Override
      public boolean shouldExecute() {
         if (!EntityPixie.this.isOwnerClose() && !EntityPixie.this.getLegacyMoveControl().isUpdating() && !EntityPixie.this.isSitting()
            && EntityPixie.this.random.nextInt(20) == 0 && EntityPixie.this.ticksUntilHouseAI == 0) {
            BlockPos blockpos1 = EntityPixie.findAHouse(EntityPixie.this, EntityPixie.this.level());
            return !blockpos1.equals(AirWaterUtils.pos(EntityPixie.this));
         } else {
            return false;
         }
      }

      @Override
      public boolean shouldContinueExecuting() {
         return false;
      }

      @Override
      public void updateTask() {
         for (int i = 0; i < 3; i++) {
            BlockPos blockpos1 = EntityPixie.findAHouse(EntityPixie.this, EntityPixie.this.level());
            EntityPixie.this.moveControl.setWantedPosition(blockpos1.getX() + 0.5, blockpos1.getY() + 0.5, blockpos1.getZ() + 0.5, 0.25);
            EntityPixie.this.housePos = blockpos1;
            if (EntityPixie.this.getTarget() == null) {
               EntityPixie.this.getLookControl().setLookAt(blockpos1.getX() + 0.5, blockpos1.getY() + 0.5, blockpos1.getZ() + 0.5, 180.0F, 20.0F);
            }
         }
      }
   }

   class AIMoveControl extends LegacyMoveControl {
      AIMoveControl(EntityPixie pixie) {
         super(pixie);
         this.speedModifier = 0.75;
      }

      @Override
      public void tick() {
         if (EntityPixie.this.slowSpeed) {
            this.speedModifier = 2.0;
         }
         if (this.isMoveTo()) {
            double d0 = this.wantedX - EntityPixie.this.getX();
            double d1 = this.wantedY - EntityPixie.this.getY();
            double d2 = this.wantedZ - EntityPixie.this.getZ();
            double d3 = d0 * d0 + d1 * d1 + d2 * d2;
            d3 = Math.sqrt(d3);
            Vec3 m = EntityPixie.this.getDeltaMovement();
            if (d3 < EntityPixie.this.getBoundingBox().getSize()) {
               this.setWait();
               EntityPixie.this.setDeltaMovement(m.scale(0.5));
            } else {
               EntityPixie.this.setDeltaMovement(m.x + d0 / d3 * 0.05 * this.speedModifier, m.y + d1 / d3 * 0.05 * this.speedModifier,
                  m.z + d2 / d3 * 0.05 * this.speedModifier);
               Vec3 n = EntityPixie.this.getDeltaMovement();
               if (EntityPixie.this.getTarget() == null) {
                  EntityPixie.this.setYRot(-((float)Mth.atan2(n.x, n.z)) * (180.0F / (float)Math.PI));
               } else {
                  double d4 = EntityPixie.this.getTarget().getX() - EntityPixie.this.getX();
                  double d5 = EntityPixie.this.getTarget().getZ() - EntityPixie.this.getZ();
                  EntityPixie.this.setYRot(-((float)Mth.atan2(d4, d5)) * (180.0F / (float)Math.PI));
               }
               EntityPixie.this.yBodyRot = EntityPixie.this.getYRot();
            }
         }
      }
   }

   class AIMoveRandom extends EntityAIBase {
      BlockPos target;

      AIMoveRandom() {
         this.setMutexBits(1);
      }

      @Override
      public boolean shouldExecute() {
         this.target = EntityPixie.getPositionRelativetoGround(EntityPixie.this, EntityPixie.this.level(),
            EntityPixie.this.getX() + (double)EntityPixie.this.random.nextInt(15) - 7.0,
            EntityPixie.this.getZ() + (double)EntityPixie.this.random.nextInt(15) - 7.0, EntityPixie.this.random);
         return !EntityPixie.this.isOwnerClose() && !EntityPixie.this.isSitting()
            && EntityPixie.this.isDirectPathBetweenPoints(AirWaterUtils.pos(EntityPixie.this), this.target)
            && !EntityPixie.this.getLegacyMoveControl().isUpdating() && EntityPixie.this.random.nextInt(4) == 0 && EntityPixie.this.housePos == null;
      }

      @Override
      public boolean shouldContinueExecuting() {
         return false;
      }

      @Override
      public void updateTask() {
         if (!EntityPixie.this.isDirectPathBetweenPoints(AirWaterUtils.pos(EntityPixie.this), this.target)) {
            this.target = EntityPixie.getPositionRelativetoGround(EntityPixie.this, EntityPixie.this.level(),
               EntityPixie.this.getX() + (double)EntityPixie.this.random.nextInt(15) - 7.0,
               EntityPixie.this.getZ() + (double)EntityPixie.this.random.nextInt(15) - 7.0, EntityPixie.this.random);
         }
         if (EntityPixie.this.level().isEmptyBlock(this.target)) {
            EntityPixie.this.moveControl.setWantedPosition(this.target.getX() + 0.5, this.target.getY() + 0.5, this.target.getZ() + 0.5, 0.25);
            if (EntityPixie.this.getTarget() == null) {
               EntityPixie.this.getLookControl().setLookAt(this.target.getX() + 0.5, this.target.getY() + 0.5, this.target.getZ() + 0.5, 180.0F, 20.0F);
            }
         }
      }
   }
}
