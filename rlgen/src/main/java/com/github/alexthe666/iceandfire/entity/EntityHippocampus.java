package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.entity.ai.AquaticAIFindWaterTarget;
import com.github.alexthe666.iceandfire.entity.ai.AquaticAIGetInWater;
import com.github.alexthe666.iceandfire.entity.ai.HippocampusAIWander;
import com.github.alexthe666.iceandfire.entity.ai.PathNavigateAmphibious;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIMate;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITempt;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
import com.github.alexthe666.iceandfire.entity.ai.legacy.LegacyMoveControl;
import com.github.alexthe666.iceandfire.inventory.ContainerHippocampus;
import javax.annotation.Nullable;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.ilexiconn.llibrary.server.animation.AnimationHandler;
import net.ilexiconn.llibrary.server.animation.IAnimatedEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;

/** Ice and Fire 1.7.1 EntityHippocampus. */
public class EntityHippocampus extends TamableAnimal implements IAnimatedEntity, IDropArmor, IAirWaterRideable {
   private int animationTick;
   private Animation currentAnimation;
   public float onLandProgress;
   private boolean isLandNavigator;
   /** client only: ChainBuffer */
   public net.ilexiconn.llibrary.client.model.tools.ChainBuffer tail_buffer;
   private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(EntityHippocampus.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> SADDLE = SynchedEntityData.defineId(EntityHippocampus.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> ARMOR = SynchedEntityData.defineId(EntityHippocampus.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> CHESTED = SynchedEntityData.defineId(EntityHippocampus.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Byte> CONTROL_STATE = SynchedEntityData.defineId(EntityHippocampus.class, EntityDataSerializers.BYTE);
   public SimpleContainer hippocampusInventory;
   private boolean isSitting;
   private boolean hasChestVarChanged = false;
   public float sitProgress;
   public int airBorneCounter;
   public static final ResourceKey<LootTable> LOOT = AirWaterEntities.loot("hippocampus");
   public static Animation ANIMATION_SPEAK = Animation.create(15);
   public final EntityAITasks tasks = new EntityAITasks();
   public final EntityAITasks targetTasks = new EntityAITasks();

   public EntityHippocampus(EntityType<? extends EntityHippocampus> type, Level level) {
      super(type, level);
      this.switchNavigator(true);
      this.tasks.addTask(0, new EntityAITempt(this, 1.0, Items.SPONGE, false));
      this.tasks.addTask(0, new EntityAITempt(this, 1.0, Items.PRISMARINE_CRYSTALS, false));
      this.tasks.addTask(1, new AquaticAIFindWaterTarget(this, 10, true));
      this.tasks.addTask(2, new AquaticAIGetInWater(this, 1.0));
      this.tasks.addTask(3, new HippocampusAIWander(this, 1.0));
      this.tasks.addTask(4, new EntityAIMate(this, 1.0));
      com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112.install(this, this.tasks, this.targetTasks);
      if (level.isClientSide) {
         this.tail_buffer = AirWaterEntities.clientLLibraryChainBuffer();
      }
      this.initHippocampusInv();
   }

   public static AttributeSupplier.Builder createAttributes() {
      return TamableAnimal.createMobAttributes()
         .add(Attributes.MOVEMENT_SPEED, 0.3)
         .add(Attributes.MAX_HEALTH, 40.0)
         .add(Attributes.ATTACK_DAMAGE, 1.0)
         .add(Attributes.STEP_HEIGHT, 1.0);
   }

   @Override
   protected void registerGoals() {
   }

   @Override
   public float getWalkTargetValue(BlockPos pos, LevelReader level) {
      return Legacy112.isWaterMaterial(level.getBlockState(pos.below())) ? 10.0F : level.getPathfindingCostFromLightLevels(pos);
   }

   @Override
   public boolean isPushedByFluid() {
      return false;
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

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(VARIANT, 0);
      builder.define(ARMOR, 0);
      builder.define(SADDLE, false);
      builder.define(CHESTED, false);
      builder.define(CONTROL_STATE, (byte)0);
   }

   private void initHippocampusInv() {
      SimpleContainer animalchest = this.hippocampusInventory;
      this.hippocampusInventory = new SimpleContainer(18);
      this.hippocampusInventory.addListener(new HippocampusInventoryListener(this));
      if (animalchest != null) {
         int i = Math.min(animalchest.getContainerSize(), this.hippocampusInventory.getContainerSize());
         for (int j = 0; j < i; j++) {
            ItemStack itemstack = animalchest.getItem(j);
            if (!itemstack.isEmpty()) {
               this.hippocampusInventory.setItem(j, itemstack.copy());
            }
         }
      }
   }

   /** any rider who is not its target steers it (owner check only when mounting) */
   @Nullable
   @Override
   public LivingEntity getControllingPassenger() {
      for (Entity passenger : this.getPassengers()) {
         if (passenger instanceof Player player && this.getTarget() != passenger) {
            return player;
         }
      }
      return null;
   }

   public int getIntFromArmor(ItemStack stack) {
      if (stack.is(Items.IRON_HORSE_ARMOR)) {
         return 1;
      } else if (stack.is(Items.GOLDEN_HORSE_ARMOR)) {
         return 2;
      } else {
         return stack.is(Items.DIAMOND_HORSE_ARMOR) ? 3 : 0;
      }
   }

   private void setStateField(int i, boolean newState) {
      byte prevState = this.entityData.get(CONTROL_STATE);
      if (newState) {
         this.entityData.set(CONTROL_STATE, (byte)(prevState | 1 << i));
      } else {
         this.entityData.set(CONTROL_STATE, (byte)(prevState & ~(1 << i)));
      }
   }

   @Override
   public byte getControlState() {
      return this.entityData.get(CONTROL_STATE);
   }

   @Override
   public void setControlState(byte state) {
      this.entityData.set(CONTROL_STATE, state);
   }

   public boolean isRidingPlayer(Player player) {
      return this.getControllingPassenger() != null && this.getControllingPassenger().getUUID().equals(player.getUUID());
   }

   @Override
   public boolean dismountsUnderwater() {
      return false;
   }

   @Override
   protected void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
      if (this.hasPassenger(passenger)) {
         this.yBodyRot = this.getYRot();
         this.setYRot(passenger.getYRot());
      }
      double ymod1 = (double)this.onLandProgress * -0.02;
      moveFunction.accept(passenger, this.getX(), this.getY() + 0.6F + ymod1, this.getZ());
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (!this.level().isClientSide && this.random.nextInt(900) == 0 && this.deathTime == 0) {
         this.heal(1.0F);
      }
      AnimationHandler.INSTANCE.updateAnimations(this);
      if (this.getControllingPassenger() != null && this.tickCount % 20 == 0) {
         this.getControllingPassenger().addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 30, 0, true, false));
      }
      if (!this.onGround()) {
         this.airBorneCounter++;
      } else {
         this.airBorneCounter = 0;
      }
      if (this.level().isClientSide) {
         AirWaterEntities.updateClientControls(this);
         AirWaterEntities.swingBuffer(this.tail_buffer, 40.0F, 10, 1.0F, this);
      }
      if (this.up()) {
         if (!this.isInWater() && this.airBorneCounter == 0 && this.onGround()) {
            this.jumpFromGround();
         } else if (this.isInWater()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.4, 0.0));
         }
      }
      if (this.down()) {
         this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.4, 0.0));
      }
      if (this.isInWater() && this.isLandNavigator) {
         this.switchNavigator(false);
      }
      if (!this.isInWater() && !this.isLandNavigator) {
         this.switchNavigator(true);
      }
      boolean onLand = !this.isInWater();
      if (onLand && this.onLandProgress < 20.0F) {
         this.onLandProgress++;
      } else if (!onLand && this.onLandProgress > 0.0F) {
         this.onLandProgress--;
      }
      boolean sitting = this.isSitting();
      if (sitting && this.sitProgress < 20.0F) {
         this.sitProgress += 0.5F;
      } else if (!sitting && this.sitProgress > 0.0F) {
         this.sitProgress -= 0.5F;
      }
      if (this.hasChestVarChanged && !this.isChested()) {
         for (int i = 3; i < 18; i++) {
            if (!this.hippocampusInventory.getItem(i).isEmpty()) {
               if (!this.level().isClientSide) {
                  this.spawnAtLocation(this.hippocampusInventory.getItem(i), 1.0F);
               }
               this.hippocampusInventory.removeItemNoUpdate(i);
            }
         }
         this.hasChestVarChanged = false;
      }
   }

   public boolean up() {
      return (this.entityData.get(CONTROL_STATE) & 1) == 1;
   }

   public boolean down() {
      return (this.entityData.get(CONTROL_STATE) >> 1 & 1) == 1;
   }

   public boolean dismount() {
      return (this.entityData.get(CONTROL_STATE) >> 2 & 1) == 1;
   }

   public boolean isBlinking() {
      return this.tickCount % 50 > 43;
   }

   @Override
   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      compound.putBoolean("Sitting", this.isSitting());
      compound.putInt("Variant", this.getVariant());
      compound.putBoolean("Chested", this.isChested());
      compound.putBoolean("Saddled", this.isSaddled());
      compound.putInt("Armor", this.getArmor());
      ListTag list = new ListTag();
      for (int i = 0; i < this.hippocampusInventory.getContainerSize(); i++) {
         ItemStack itemstack = this.hippocampusInventory.getItem(i);
         if (!itemstack.isEmpty()) {
            CompoundTag tag = new CompoundTag();
            tag.putByte("Slot", (byte)i);
            list.add(itemstack.save(this.registryAccess(), tag));
         }
      }
      compound.put("Items", list);
   }

   @Override
   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      this.setSitting(compound.getBoolean("Sitting"));
      this.setVariant(compound.getInt("Variant"));
      this.setChested(compound.getBoolean("Chested"));
      this.setSaddled(compound.getBoolean("Saddled"));
      this.setArmor(compound.getInt("Armor"));
      ListTag list = compound.getList("Items", 10);
      this.initHippocampusInv();
      for (int i = 0; i < list.size(); i++) {
         CompoundTag tag = list.getCompound(i);
         int j = tag.getByte("Slot") & 255;
         if (j < this.hippocampusInventory.getContainerSize()) {
            this.hippocampusInventory.setItem(j, ItemStack.parseOptional(this.registryAccess(), tag));
         }
      }
   }

   public boolean isSaddled() {
      return this.entityData.get(SADDLE);
   }

   public void setSaddled(boolean saddle) {
      this.entityData.set(SADDLE, saddle);
   }

   public boolean isChested() {
      return this.entityData.get(CHESTED);
   }

   public void setChested(boolean chested) {
      this.entityData.set(CHESTED, chested);
      this.hasChestVarChanged = true;
   }

   public boolean isSitting() {
      if (this.level().isClientSide) {
         boolean sitting = this.isInSittingPose();
         this.isSitting = sitting;
         return sitting;
      } else {
         return this.isSitting;
      }
   }

   public void setSitting(boolean sitting) {
      if (!this.level().isClientSide) {
         this.isSitting = sitting;
      }
      this.setInSittingPose(sitting);
   }

   public int getArmor() {
      return this.entityData.get(ARMOR);
   }

   public void setArmor(int armorType) {
      this.entityData.set(ARMOR, armorType);
      double armorValue = switch (armorType) {
         case 1 -> 10.0;
         case 2 -> 20.0;
         case 3 -> 30.0;
         default -> 0.0;
      };
      this.getAttribute(Attributes.ARMOR).setBaseValue(armorValue);
   }

   public int getVariant() {
      return this.entityData.get(VARIANT);
   }

   public void setVariant(int variant) {
      this.entityData.set(VARIANT, variant);
   }

   @Nullable
   @Override
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData data) {
      data = super.finalizeSpawn(level, difficulty, spawnType, data);
      this.setVariant(this.getRandom().nextInt(6));
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
      return new Animation[]{IAnimatedEntity.NO_ANIMATION, ANIMATION_SPEAK};
   }

   @Nullable
   @Override
   public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob ageable) {
      if (ageable instanceof EntityHippocampus other) {
         EntityHippocampus hippo = AirWaterEntities.HIPPOCAMPUS.get().create(level);
         if (hippo != null) {
            hippo.setVariant(this.getRandom().nextBoolean() ? this.getVariant() : other.getVariant());
         }
         return hippo;
      }
      return null;
   }

   @Override
   public boolean canDrownInFluidType(net.neoforged.neoforge.fluids.FluidType type) {
      return false;
   }

   @Override
   public boolean isInWater() {
      return super.isInWater() || this.isEyeInFluid(FluidTags.WATER);
   }

   /** EntityHippocampus.travel */
   @Override
   public void travel(Vec3 move) {
      float strafe = (float)move.x;
      float vertical = (float)move.y;
      float forward = (float)move.z;
      if (this.isSitting()) {
         super.travel(Vec3.ZERO);
      } else {
         if (this.isVehicle()) {
            LivingEntity controller = this.getControllingPassenger();
            if (controller != null) {
               strafe = controller.xxa * 0.5F;
               forward = controller.zza;
               if (forward <= 0.0F) {
                  forward *= 0.25F;
               }
               this.fallDistance = 0.0F;
               if (this.isInWater()) {
                  this.moveRelative(1.0F, new Vec3(strafe, vertical, forward));
                  float f4 = 0.8F;
                  float d0 = 3.0F;
                  if (!this.onGround()) {
                     d0 *= 0.5F;
                  }
                  if (d0 > 0.0F) {
                     f4 += (0.54600006F - f4) * d0 / 3.0F;
                  }
                  Vec3 m = this.getDeltaMovement();
                  this.setDeltaMovement(m.x * f4 * 0.900000011920929, m.y * 0.900000011920929 * f4 + 0.01185, m.z * 0.900000011920929 * f4);
                  this.setSpeed(this.onGround() ? (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) : 1.0F);
                  double ox = this.getX();
                  double oy = this.getY();
                  double oz = this.getZ();
                  super.travel(new Vec3(strafe, 0.0, forward));
                  float delta = (float)Math.sqrt((this.getX() - ox) * (this.getX() - ox) + (this.getY() - oy) * (this.getY() - oy)
                     + (this.getZ() - oz) * (this.getZ() - oz)) * 4.0F;
                  if ((double)Math.min(delta, 1.0F) > 0.5 && this.isInWater() && this.isVehicle()) {
                     this.doWaterSplashEffect();
                  }
                  return;
               }
               forward = controller.zza * 0.25F;
               strafe = controller.xxa * 0.125F;
               this.setSpeed(this.onGround() ? (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) : 1.0F);
               super.travel(new Vec3(strafe, vertical, forward));
               return;
            }
         }
         if (this.isEffectiveAi()) {
            if (this.isInWater()) {
               this.moveRelative(0.1F, move);
               float f4x = 0.6F;
               float d0x = (float)this.getAttributeValue(Attributes.WATER_MOVEMENT_EFFICIENCY) * 3.0F;
               if (d0x > 3.0F) {
                  d0x = 3.0F;
               }
               if (!this.onGround()) {
                  d0x *= 0.5F;
               }
               if (d0x > 0.0F) {
                  f4x += (0.54600006F - f4x) * d0x / 3.0F;
               }
               this.move(MoverType.SELF, this.getDeltaMovement());
               Vec3 m = this.getDeltaMovement();
               this.setDeltaMovement(m.x * f4x * 0.900000011920929, m.y * 0.900000011920929 * f4x, m.z * 0.900000011920929 * f4x);
               this.calculateEntityAnimation(true);
            } else {
               super.travel(move);
            }
         }
      }
   }

   /** EntityAnimal.isBreedingItem */
   @Override
   public boolean isFood(ItemStack stack) {
      return stack.is(Items.PRISMARINE_CRYSTALS);
   }

   @Override
   public void playAmbientSound() {
      if (this.getAnimation() == NO_ANIMATION) {
         this.setAnimation(ANIMATION_SPEAK);
      }
      super.playAmbientSound();
   }

   @Override
   protected void playHurtSound(DamageSource source) {
      if (this.getAnimation() == NO_ANIMATION) {
         this.setAnimation(ANIMATION_SPEAK);
      }
      super.playHurtSound(source);
   }

   private InteractionResult done() {
      return InteractionResult.sidedSuccess(this.level().isClientSide);
   }

   @Override
   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      ItemStack itemstack = player.getItemInHand(hand);
      if (itemstack.is(Items.PRISMARINE_CRYSTALS) && this.getAge() == 0 && !this.isInLove()) {
         this.setSitting(false);
         this.setInLove(player);
         this.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
         if (!player.isCreative()) {
            itemstack.shrink(1);
         }
         return this.done();
      } else if (itemstack.is(Items.SPONGE)) {
         if (!this.level().isClientSide) {
            this.heal(5.0F);
            this.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
            for (int i = 0; i < 3; i++) {
               this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, itemstack.copyWithCount(1)),
                  this.getX() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
                  this.getY() + (double)(this.random.nextFloat() * this.getBbHeight()),
                  this.getZ() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(), 0.0, 0.0, 0.0);
            }
            if (!player.isCreative()) {
               itemstack.shrink(1);
            }
         }
         if (!this.isTame() && this.getRandom().nextInt(3) == 0) {
            if (!this.level().isClientSide) {
               this.tame(player);
            }
            for (int i = 0; i < 6; i++) {
               this.level().addParticle(ParticleTypes.HEART,
                  this.getX() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
                  this.getY() + (double)(this.random.nextFloat() * this.getBbHeight()),
                  this.getZ() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(), 0.0, 0.0, 0.0);
            }
         }
         return this.done();
      } else if (this.isOwnedBy(player) && itemstack.is(Items.STICK)) {
         this.setSitting(!this.isSitting());
         return this.done();
      } else {
         if (this.isOwnedBy(player) && itemstack.isEmpty()) {
            if (player.isShiftKeyDown()) {
               this.openGUI(player);
               return this.done();
            }
            if (this.isSaddled() && !this.isBaby() && !player.isPassenger() && !this.level().isClientSide) {
               player.startRiding(this, true);
               this.setSitting(false);
               return this.done();
            }
         }
         return super.mobInteract(player, hand);
      }
   }

   /** 1.7.1 opened this GUI on the client only (and synced the saddle/chest/armour flags by messages); here the server opens it. */
   public void openGUI(Player player) {
      if (!this.level().isClientSide && (!this.isVehicle() || !this.hasPassenger(player))) {
         ContainerHippocampus.open(player, this);
      }
   }

   public void up(boolean up) {
      this.setStateField(0, up);
   }

   public void down(boolean down) {
      this.setStateField(1, down);
   }

   public void dismount(boolean dismount) {
      this.setStateField(2, dismount);
   }

   public void refreshInventory() {
      ItemStack saddle = this.hippocampusInventory.getItem(0);
      ItemStack chest = this.hippocampusInventory.getItem(1);
      this.setSaddled(saddle.is(Items.SADDLE));
      this.setChested(chest.is(Items.CHEST));
      this.setArmor(this.getIntFromArmor(this.hippocampusInventory.getItem(2)));
   }

   @Nullable
   @Override
   protected SoundEvent getAmbientSound() {
      return AirWaterRefs.sound("hippocampus_idle");
   }

   @Nullable
   @Override
   protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
      return AirWaterRefs.sound("hippocampus_hurt");
   }

   @Nullable
   @Override
   protected SoundEvent getDeathSound() {
      return AirWaterRefs.sound("hippocampus_die");
   }

   @Override
   protected ResourceKey<LootTable> getDefaultLootTable() {
      return LOOT;
   }

   @Override
   public void dropArmor() {
      if (!this.level().isClientSide) {
         for (int i = 0; i < this.hippocampusInventory.getContainerSize(); i++) {
            ItemStack itemstack = this.hippocampusInventory.getItem(i);
            if (!itemstack.isEmpty()) {
               this.spawnAtLocation(itemstack, 0.0F);
            }
         }
      }
   }

   static class HippocampusInventoryListener implements ContainerListener {
      final EntityHippocampus hippocampus;

      HippocampusInventoryListener(EntityHippocampus hippocampus) {
         this.hippocampus = hippocampus;
      }

      @Override
      public void containerChanged(Container container) {
         this.hippocampus.refreshInventory();
      }
   }

   class SwimmingMoveHelper extends LegacyMoveControl {
      private final EntityHippocampus hippo = EntityHippocampus.this;

      SwimmingMoveHelper() {
         super(EntityHippocampus.this);
      }

      @Override
      public void tick() {
         if (this.isMoveTo() && !this.hippo.getNavigation().isDone() && !this.hippo.isVehicle()) {
            double distanceX = this.wantedX - this.hippo.getX();
            double distanceY = this.wantedY - this.hippo.getY();
            double distanceZ = this.wantedZ - this.hippo.getZ();
            double distance = Math.abs(distanceX * distanceX + distanceZ * distanceZ);
            double distanceWithY = Math.sqrt(distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ);
            distanceY /= distanceWithY;
            float angle = (float)(Math.atan2(distanceZ, distanceX) * 180.0 / Math.PI) - 90.0F;
            this.hippo.setYRot(this.rotlerp(this.hippo.getYRot(), angle, 30.0F));
            this.hippo.setSpeed(1.0F);
            Vec3 m = this.hippo.getDeltaMovement();
            double mx = m.x;
            double mz = m.z;
            double my = m.y + (double)this.hippo.getSpeed() * distanceY * 0.1;
            if (distance < (double)Math.max(1.0F, this.mob.getBbWidth())) {
               float f = this.hippo.getYRot() * (float)(Math.PI / 180.0);
               mx -= (double)(Mth.sin(f) * 0.35F);
               mz += (double)(Mth.cos(f) * 0.35F);
            }
            this.hippo.setDeltaMovement(mx, my, mz);
         } else if (this.isJumping()) {
            this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
            if (this.mob.onGround()) {
               this.setWait();
            }
         } else {
            this.hippo.setSpeed(0.0F);
         }
      }
   }
}
