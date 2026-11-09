package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.entity.ai.HippogryphAIAirTarget;
import com.github.alexthe666.iceandfire.entity.ai.HippogryphAIAttackMelee;
import com.github.alexthe666.iceandfire.entity.ai.HippogryphAIMate;
import com.github.alexthe666.iceandfire.entity.ai.HippogryphAITarget;
import com.github.alexthe666.iceandfire.entity.ai.HippogryphAITargetItems;
import com.github.alexthe666.iceandfire.entity.ai.HippogryphAIWander;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAIHurtByTarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAILookIdle;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAIOwnerHurtByTarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAIOwnerHurtTarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAISit;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAISwimming;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAITempt;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAIWatchClosest;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.LegacyTasksGoal;
import com.github.alexthe666.iceandfire.enums.EnumHippogryphTypes;
import com.github.alexthe666.iceandfire.inventory.ContainerHippogryph;
import javax.annotation.Nullable;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.ilexiconn.llibrary.server.animation.AnimationHandler;
import net.ilexiconn.llibrary.server.animation.IAnimatedEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Ice and Fire 1.7.1 EntityHippogryph. */
public class EntityHippogryph extends TamableAnimal implements IAnimatedEntity, IDragonFlute, IVillagerFear, IAnimalFear, IDropArmor, EntityAISit.Sittable,
   IAirWaterRideable {
   public static final ResourceKey<LootTable> LOOT = AirWaterEntities.loot("hippogryph");
   private static final int FLIGHT_CHANCE_PER_TICK = 1200;
   private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(EntityHippogryph.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> SADDLE = SynchedEntityData.defineId(EntityHippogryph.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> ARMOR = SynchedEntityData.defineId(EntityHippogryph.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> CHESTED = SynchedEntityData.defineId(EntityHippogryph.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> HOVERING = SynchedEntityData.defineId(EntityHippogryph.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> FLYING = SynchedEntityData.defineId(EntityHippogryph.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Byte> CONTROL_STATE = SynchedEntityData.defineId(EntityHippogryph.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Integer> COMMAND = SynchedEntityData.defineId(EntityHippogryph.class, EntityDataSerializers.INT);
   public static Animation ANIMATION_EAT = Animation.create(25);
   public static Animation ANIMATION_SPEAK = Animation.create(15);
   public static Animation ANIMATION_SCRATCH = Animation.create(25);
   public static Animation ANIMATION_BITE = Animation.create(20);
   public final EntityAITasks tasks = new EntityAITasks();
   public final EntityAITasks targetTasks = new EntityAITasks();
   public EntityAISit aiSit;
   public SimpleContainer hippogryphInventory;
   /** client only: IFChainBuffer */
   public com.github.alexthe666.iceandfire.client.model.IFChainBuffer roll_buffer;
   public float sitProgress;
   public float hoverProgress;
   public float flyProgress;
   public int spacebarTicks;
   public BlockPos airTarget;
   public int airBorneCounter;
   private boolean isSitting;
   private boolean isHovering;
   private boolean isFlying;
   private int animationTick;
   private Animation currentAnimation;
   private int flyTicks;
   private int hoverTicks;
   private boolean hasChestVarChanged = false;
   public BlockPos homePos;
   public boolean hasHomePosition = false;
   /** EntityLivingBase.jumpMovementFactor (never reset after the first ride, as in 1.12) */
   public float jumpMovementFactor = 0.02F;
   private final RiddenMotion riddenMotion = new RiddenMotion();

   public EntityHippogryph(EntityType<? extends EntityHippogryph> type, Level level) {
      super(type, level);
      this.initEntityAI();
      this.goalSelector.addGoal(0, new LegacyTasksGoal(this, this.targetTasks, this.tasks));
      this.initHippogryphInv();
      if (level.isClientSide) {
         this.roll_buffer = AirWaterEntities.clientChainBuffer();
      }
   }

   public static AttributeSupplier.Builder createAttributes() {
      return TamableAnimal.createMobAttributes()
         .add(Attributes.MOVEMENT_SPEED, 0.3)
         .add(Attributes.MAX_HEALTH, 40.0)
         .add(Attributes.ATTACK_DAMAGE, 5.0)
         .add(Attributes.FOLLOW_RANGE, 32.0)
         .add(Attributes.STEP_HEIGHT, 1.0);
   }

   @Override
   protected int getBaseExperienceReward() {
      return 7 + this.level().random.nextInt(10);
   }

   @Override
   protected void registerGoals() {
   }

   protected void initEntityAI() {
      this.tasks.addTask(1, new EntityAISwimming(this));
      this.tasks.addTask(2, this.aiSit = new EntityAISit(this));
      this.tasks.addTask(3, new HippogryphAIAttackMelee(this, 1.5, true));
      this.tasks.addTask(4, new HippogryphAIMate(this, 1.0));
      this.tasks.addTask(5, new EntityAITempt(this, 1.0, Items.RABBIT, false));
      this.tasks.addTask(5, new EntityAITempt(this, 1.0, Items.COOKED_RABBIT, false));
      this.tasks.addTask(6, new HippogryphAIAirTarget(this));
      this.tasks.addTask(7, new HippogryphAIWander(this, 1.0));
      this.tasks.addTask(8, new EntityAIWatchClosest(this, LivingEntity.class, 6.0F));
      this.tasks.addTask(8, new EntityAILookIdle(this));
      this.targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
      this.targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
      this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, false));
      this.targetTasks.addTask(4, new HippogryphAITargetItems(this, false));
      this.targetTasks.addTask(5, new HippogryphAITarget<>(this, LivingEntity.class, false,
         entity -> entity instanceof LivingEntity && !(entity instanceof AbstractHorse) && AirWaterUtils.isAlive(entity)));
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(VARIANT, 0);
      builder.define(ARMOR, 0);
      builder.define(SADDLE, false);
      builder.define(CHESTED, false);
      builder.define(HOVERING, false);
      builder.define(FLYING, false);
      builder.define(CONTROL_STATE, (byte)0);
      builder.define(COMMAND, 0);
   }

   @Override
   protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
   }

   @Override
   protected void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
      if (this.hasPassenger(passenger)) {
         this.yBodyRot = this.getYRot();
         this.setYRot(passenger.getYRot());
      }
      moveFunction.accept(passenger, this.getX(), this.getY() + 1.05F, this.getZ());
   }

   private void initHippogryphInv() {
      SimpleContainer animalchest = this.hippogryphInventory;
      this.hippogryphInventory = new SimpleContainer(18);
      this.hippogryphInventory.addListener(new HippogryphInventoryListener(this));
      if (animalchest != null) {
         int i = Math.min(animalchest.getContainerSize(), this.hippogryphInventory.getContainerSize());
         for (int j = 0; j < i; j++) {
            ItemStack itemstack = animalchest.getItem(j);
            if (!itemstack.isEmpty()) {
               this.hippogryphInventory.setItem(j, itemstack.copy());
            }
         }
      }
   }

   @Nullable
   @Override
   public LivingEntity getControllingPassenger() {
      for (Entity passenger : this.getPassengers()) {
         if (passenger instanceof Player player && this.getTarget() != passenger) {
            if (this.isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(player.getUUID())) {
               return player;
            }
         }
      }
      return null;
   }

   public int getIntFromArmor(ItemStack stack) {
      if (!stack.isEmpty() && AirWaterRefs.is(stack, "iron_hippogryph_armor")) {
         return 1;
      } else if (!stack.isEmpty() && AirWaterRefs.is(stack, "gold_hippogryph_armor")) {
         return 2;
      } else {
         return !stack.isEmpty() && AirWaterRefs.is(stack, "diamond_hippogryph_armor") ? 3 : 0;
      }
   }

   public boolean isBlinking() {
      return this.tickCount % 50 > 43;
   }

   private void particles(net.minecraft.core.particles.ParticleOptions particle, int count) {
      for (int i = 0; i < count; i++) {
         this.level().addParticle(particle,
            this.getX() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
            this.getY() + (double)(this.random.nextFloat() * this.getBbHeight()),
            this.getZ() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(), 0.0, 0.0, 0.0);
      }
   }

   private InteractionResult handled() {
      return InteractionResult.sidedSuccess(this.level().isClientSide);
   }

   @Override
   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      ItemStack itemstack = player.getItemInHand(hand);
      String s = ChatFormatting.stripFormatting(player.getName().getString());
      boolean isDev = s.equals("Alexthe666") || s.equals("Raptorfarian");
      if (this.isTame() && this.isOwnedBy(player)) {
         if (itemstack.is(Items.RED_DYE) && this.getEnumVariant() != EnumHippogryphTypes.ALEX && isDev) {
            this.setEnumVariant(EnumHippogryphTypes.ALEX);
            if (!player.isCreative()) {
               itemstack.shrink(1);
            }
            this.playSound(SoundEvents.ZOMBIE_INFECT, 1.0F, 1.0F);
            this.particles(new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 1.0F), 20);
            return this.handled();
         }

         if (itemstack.is(Items.LIGHT_GRAY_DYE) && this.getEnumVariant() != EnumHippogryphTypes.RAPTOR && isDev) {
            this.setEnumVariant(EnumHippogryphTypes.RAPTOR);
            if (!player.isCreative()) {
               itemstack.shrink(1);
            }
            this.playSound(SoundEvents.ZOMBIE_INFECT, 1.0F, 1.0F);
            this.particles(ParticleTypes.CLOUD, 20);
            return this.handled();
         }

         if (itemstack.is(Items.RABBIT_STEW) && this.getAge() == 0 && !this.isInLove()) {
            this.setInLove(player);
            this.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
            if (!player.isCreative()) {
               itemstack.shrink(1);
            }
            return this.handled();
         }

         if (itemstack.is(Items.STICK)) {
            if (player.isShiftKeyDown()) {
               this.homePos = AirWaterUtils.pos(this);
               this.hasHomePosition = true;
               player.displayClientMessage(Component.translatable("hippogryph.command.new_home", this.homePos.getX(), this.homePos.getY(),
                  this.homePos.getZ()), true);
               return this.handled();
            }
            this.setCommand(this.getCommand() + 1);
            if (this.getCommand() > 1) {
               this.setCommand(0);
            }
            player.displayClientMessage(Component.translatable("hippogryph.command." + (this.getCommand() == 1 ? "sit" : "stand")), true);
            return this.handled();
         }

         if (itemstack.is(Items.GLISTERING_MELON_SLICE) && this.getEnumVariant() != EnumHippogryphTypes.DODO) {
            this.setEnumVariant(EnumHippogryphTypes.DODO);
            if (!player.isCreative()) {
               itemstack.shrink(1);
            }
            this.playSound(SoundEvents.ZOMBIE_INFECT, 1.0F, 1.0F);
            this.particles(ParticleTypes.ENCHANT, 20);
            return this.handled();
         }

         if (itemstack.is(ItemTags.MEAT) && this.getHealth() < this.getMaxHealth()) {
            this.heal(5.0F);
            this.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
            this.particles(new ItemParticleOption(ParticleTypes.ITEM, itemstack.copyWithCount(1)), 3);
            if (!player.isCreative()) {
               itemstack.shrink(1);
            }
            return this.handled();
         }

         if (itemstack.isEmpty()) {
            if (player.isShiftKeyDown()) {
               this.openGUI(player);
               return this.handled();
            }
            if (this.isSaddled() && !this.isBaby() && !player.isPassenger()) {
               player.startRiding(this, true);
               return this.handled();
            }
         }
      }
      return super.mobInteract(player, hand);
   }

   /** EntityAnimal.isBreedingItem: wheat (the 1.12 default, Ice and Fire never overrode it). */
   @Override
   public boolean isFood(ItemStack stack) {
      return stack.is(Items.WHEAT);
   }

   public void openGUI(Player player) {
      if (!this.level().isClientSide && (!this.isVehicle() || this.hasPassenger(player))) {
         ContainerHippogryph.open(player, this);
      }
   }

   public boolean up() {
      return (this.entityData.get(CONTROL_STATE) & 1) == 1;
   }

   public boolean down() {
      return (this.entityData.get(CONTROL_STATE) >> 1 & 1) == 1;
   }

   public boolean attack() {
      return (this.entityData.get(CONTROL_STATE) >> 2 & 1) == 1;
   }

   public boolean dismount() {
      return (this.entityData.get(CONTROL_STATE) >> 3 & 1) == 1;
   }

   public void up(boolean up) {
      this.setStateField(0, up);
   }

   public void down(boolean down) {
      this.setStateField(1, down);
   }

   public void attack(boolean attack) {
      this.setStateField(2, attack);
   }

   public void dismount(boolean dismount) {
      this.setStateField(3, dismount);
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

   public void setCommand(int command) {
      this.entityData.set(COMMAND, command);
      this.setSitting(command == 1);
   }

   public int getCommand() {
      return this.entityData.get(COMMAND);
   }

   @Override
   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      compound.putBoolean("Sitting", this.isSitting());
      compound.putInt("Variant", this.getVariant());
      compound.putBoolean("Chested", this.isChested());
      compound.putBoolean("Saddled", this.isSaddled());
      compound.putBoolean("Hovering", this.isHovering());
      compound.putBoolean("Flying", this.isFlying());
      compound.putInt("Armor", this.getArmor());
      if (this.hippogryphInventory != null) {
         ListTag list = new ListTag();
         for (int i = 0; i < this.hippogryphInventory.getContainerSize(); i++) {
            ItemStack itemstack = this.hippogryphInventory.getItem(i);
            if (!itemstack.isEmpty()) {
               CompoundTag tag = new CompoundTag();
               tag.putByte("Slot", (byte)i);
               list.add(itemstack.save(this.registryAccess(), tag));
            }
         }
         compound.put("Items", list);
      }
      compound.putBoolean("HasHomePosition", this.hasHomePosition);
      if (this.homePos != null && this.hasHomePosition) {
         compound.putInt("HomeAreaX", this.homePos.getX());
         compound.putInt("HomeAreaY", this.homePos.getY());
         compound.putInt("HomeAreaZ", this.homePos.getZ());
      }
      compound.putInt("Command", this.getCommand());
   }

   @Override
   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      if (this.aiSit != null) {
         this.aiSit.setSitting(compound.getBoolean("Sitting"));
      }
      this.setSitting(compound.getBoolean("Sitting"));
      this.setVariant(compound.getInt("Variant"));
      this.setChested(compound.getBoolean("Chested"));
      this.setSaddled(compound.getBoolean("Saddled"));
      this.setHovering(compound.getBoolean("Hovering"));
      this.setFlying(compound.getBoolean("Flying"));
      this.setArmor(compound.getInt("Armor"));
      ListTag list = compound.getList("Items", 10);
      this.initHippogryphInv();
      for (int i = 0; i < list.size(); i++) {
         CompoundTag tag = list.getCompound(i);
         int j = tag.getByte("Slot") & 255;
         if (j < this.hippogryphInventory.getContainerSize()) {
            this.hippogryphInventory.setItem(j, ItemStack.parseOptional(this.registryAccess(), tag));
         }
      }
      this.hasHomePosition = compound.getBoolean("HasHomePosition");
      if (this.hasHomePosition && compound.getInt("HomeAreaX") != 0 && compound.getInt("HomeAreaY") != 0 && compound.getInt("HomeAreaZ") != 0) {
         this.homePos = new BlockPos(compound.getInt("HomeAreaX"), compound.getInt("HomeAreaY"), compound.getInt("HomeAreaZ"));
      }
      this.setCommand(compound.getInt("Command"));
   }

   public int getVariant() {
      return this.entityData.get(VARIANT);
   }

   public EnumHippogryphTypes getEnumVariant() {
      return EnumHippogryphTypes.values()[Mth.clamp(this.getVariant(), 0, EnumHippogryphTypes.values().length - 1)];
   }

   public void setEnumVariant(EnumHippogryphTypes variant) {
      this.setVariant(variant.ordinal());
   }

   public void setVariant(int variant) {
      this.entityData.set(VARIANT, variant);
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

   /** EntityHippogryph.isSitting: the TAMED flag bit 1 on the client, a field on the server. */
   public boolean isSitting() {
      if (this.level().isClientSide) {
         boolean sitting = this.isInSittingPose();
         this.isSitting = sitting;
         return sitting;
      } else {
         return this.isSitting;
      }
   }

   @Override
   public void setSitting(boolean sitting) {
      if (!this.level().isClientSide) {
         this.isSitting = sitting;
      }
      this.setInSittingPose(sitting);
   }

   public boolean isHovering() {
      return this.level().isClientSide ? (this.isHovering = this.entityData.get(HOVERING)) : this.isHovering;
   }

   public void setHovering(boolean hovering) {
      this.entityData.set(HOVERING, hovering);
      if (!this.level().isClientSide) {
         this.isHovering = hovering;
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

   public boolean canMove() {
      return AirWaterUtils.isStone(this) ? false : !this.isSitting() && this.getControllingPassenger() == null && this.sitProgress == 0.0F;
   }

   @Nullable
   @Override
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData data) {
      data = super.finalizeSpawn(level, difficulty, spawnType, data);
      this.setEnumVariant(EnumHippogryphTypes.getBiomeType(level.getBiome(this.blockPosition())));
      return data;
   }

   @Override
   public boolean hurt(DamageSource dmg, float amount) {
      return this.isVehicle() && dmg.getEntity() != null && this.getControllingPassenger() != null && dmg.getEntity() == this.getControllingPassenger()
         ? false
         : super.hurt(dmg, amount);
   }

   @Nullable
   @Override
   public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
      return null;
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

   @Nullable
   @Override
   protected SoundEvent getAmbientSound() {
      return AirWaterRefs.sound("hippogryph_idle");
   }

   @Nullable
   @Override
   protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
      return AirWaterRefs.sound("hippogryph_hurt");
   }

   @Nullable
   @Override
   protected SoundEvent getDeathSound() {
      return AirWaterRefs.sound("hippogryph_die");
   }

   @Override
   public Animation[] getAnimations() {
      return new Animation[]{IAnimatedEntity.NO_ANIMATION, ANIMATION_EAT, ANIMATION_BITE, ANIMATION_SPEAK, ANIMATION_SCRATCH};
   }

   public boolean isRidingPlayer(Player player) {
      return this.getControllingPassenger() != null && this.getControllingPassenger() instanceof Player
         && this.getControllingPassenger().getUUID().equals(player.getUUID());
   }

   @Override
   public boolean dismountsUnderwater() {
      return false;
   }

   public boolean isDirectPathBetweenPoints(Vec3 vec1, Vec3 vec2) {
      BlockHitResult hit = AirWaterUtils.rayTraceBlocks(this, vec1, new Vec3(vec2.x, vec2.y + (double)this.getBbHeight() * 0.5, vec2.z), false);
      return hit == null;
   }

   @Override
   public float getFlyingSpeed() {
      return this.jumpMovementFactor;
   }

   /** EntityHippogryph.travel(strafe, vertical, forward) */
   @Override
   public void travel(Vec3 move) {
      float strafe = (float)move.x;
      float vertical = (float)move.y;
      float forward = (float)move.z;
      if (!this.canMove() && !this.isVehicle()) {
         super.travel(new Vec3(0.0, vertical, 0.0));
      } else {
         if (this.isVehicle()) {
            LivingEntity controller = this.getControllingPassenger();
            if (controller != null) {
               strafe = controller.xxa * 0.5F;
               forward = controller.zza;
               if (forward <= 0.0F) {
                  forward *= 0.25F;
               }
               if (this.isFlying() || this.isHovering()) {
                  Vec3 m = this.getDeltaMovement();
                  this.setDeltaMovement(m.x * 1.06, m.y, m.z * 1.06);
               }
               this.jumpMovementFactor = 0.05F;
               this.setSpeed(this.onGround() ? (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) : 2.0F);
               super.travel(new Vec3(strafe, 0.0, forward));
               return;
            }
         }
         super.travel(new Vec3(strafe, vertical, forward));
      }
   }

   @Override
   public boolean doHurtTarget(Entity entityIn) {
      if (this.getAnimation() != ANIMATION_SCRATCH && this.getAnimation() != ANIMATION_BITE) {
         this.setAnimation(this.getRandom().nextBoolean() ? ANIMATION_SCRATCH : ANIMATION_BITE);
         return false;
      } else {
         return true;
      }
   }

   public ItemEntity createEgg(EntityHippogryph partner) {
      int i = Mth.floor(this.getX());
      int j = Mth.floor(this.getY());
      int k = Mth.floor(this.getZ());
      return new ItemEntity(this.level(), i, j, k, AirWaterEntities.hippogryphEgg(EnumHippogryphTypes.BLACK));
   }

   @Override
   public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
      return false;
   }

   private void attackTarget() {
      LivingEntity target = this.getTarget();
      target.hurt(this.damageSources().mobAttack(this), (float)(int)this.getAttributeValue(Attributes.ATTACK_DAMAGE));
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (!this.level().isClientSide) {
         if (this.isSitting() && (this.getCommand() != 1 || this.getControllingPassenger() != null)) {
            this.setSitting(false);
         }
         if (!this.isSitting() && this.getCommand() == 1 && this.getControllingPassenger() == null) {
            this.setSitting(true);
         }
         if (this.isSitting()) {
            this.getNavigation().stop();
         }
         if (this.random.nextInt(900) == 0 && this.deathTime == 0) {
            this.heal(1.0F);
         }
      }

      if (this.getAnimation() == ANIMATION_BITE && this.getTarget() != null && this.getAnimationTick() == 6) {
         double dist = this.distanceToSqr(this.getTarget());
         if (dist < 8.0) {
            this.attackTarget();
         }
      }

      if (this.getAnimation() == ANIMATION_SCRATCH && this.getTarget() != null && this.getAnimationTick() == 6) {
         double dist = this.distanceToSqr(this.getTarget());
         if (dist < 8.0) {
            this.attackTarget();
            LivingEntity target = this.getTarget();
            target.hasImpulse = true;
            float f = Mth.sqrt(0.5F);
            Vec3 m = target.getDeltaMovement();
            double mx = m.x / 2.0 - 0.5 / (double)f * 4.0;
            double mz = m.z / 2.0 - 0.5 / (double)f * 4.0;
            double my = m.y;
            if (target.onGround()) {
               my /= 2.0;
               my += 4.0;
               if (my > 0.4F) {
                  my = 0.4F;
               }
            }
            target.setDeltaMovement(mx, my, mz);
         }
      }

      if (!this.level().isClientSide && this.onGround() && this.getNavigation().isDone() && this.getTarget() != null
         && this.getTarget().getY() - 3.0 > this.getY() && this.getRandom().nextInt(15) == 0 && this.canMove() && !this.isHovering() && !this.isFlying()) {
         this.setHovering(true);
         this.hoverTicks = 0;
         this.flyTicks = 0;
      }

      boolean stone = AirWaterUtils.isStone(this);
      if (stone) {
         this.setFlying(false);
         this.setHovering(false);
      }

      if (!this.onGround()) {
         this.airBorneCounter++;
      } else {
         this.airBorneCounter = 0;
      }

      if (this.hasChestVarChanged && this.hippogryphInventory != null && !this.isChested()) {
         for (int i = 3; i < 18; i++) {
            if (!this.hippogryphInventory.getItem(i).isEmpty()) {
               if (!this.level().isClientSide) {
                  this.spawnAtLocation(this.hippogryphInventory.getItem(i), 1.0F);
               }
               this.hippogryphInventory.removeItemNoUpdate(i);
            }
         }
         this.hasChestVarChanged = false;
      }

      if (!this.onGround() && this.airTarget != null) {
         this.setFlying(true);
      }

      if (this.isFlying() && this.tickCount % 40 == 0 || this.isFlying() && this.isSitting()) {
         this.setFlying(true);
      }

      if (!this.canMove() && this.getTarget() != null) {
         this.setTarget(null);
      }

      if (!this.canMove()) {
         this.getNavigation().stop();
      }

      Vec3 m = this.getDeltaMovement();
      double my = m.y;
      if (this.getControllingPassenger() != null) {
         if (my > 0.5 && (this.isFlying() || this.isHovering())) {
            my = 0.5;
         }
         if (my < -0.5) {
            my = -0.5;
         }
      } else {
         if (my > 0.8) {
            my = 0.5;
         }
         if (my < -0.8) {
            my = -0.8;
         }
         if (my > 1.0) {
            my = 0.0;
         }
      }
      this.setDeltaMovement(m.x, my, m.z);

      AnimationHandler.INSTANCE.updateAnimations(this);
      boolean sitting = this.isSitting() && !this.isHovering() && !this.isFlying();
      if (sitting && this.sitProgress < 20.0F) {
         this.sitProgress += 0.5F;
      } else if (!sitting && this.sitProgress > 0.0F) {
         this.sitProgress -= 0.5F;
      }

      boolean hovering = this.isHovering();
      if (hovering && this.hoverProgress < 20.0F) {
         this.hoverProgress += 0.5F;
      } else if (!hovering && this.hoverProgress > 0.0F) {
         this.hoverProgress -= 0.5F;
      }

      boolean flying = this.isFlying() || !this.isHovering() && this.airBorneCounter > 50;
      if (flying && this.flyProgress < 20.0F) {
         this.flyProgress += 0.5F;
      } else if (!flying && this.flyProgress > 0.0F) {
         this.flyProgress -= 0.5F;
      }

      if ((flying || hovering) && this.tickCount % 20 == 0 && !this.onGround()) {
         this.playSound(SoundEvents.ENDER_DRAGON_FLAP, this.getSoundVolume() * (float)(com.github.alexthe666.iceandfire.IceAndFire.CONFIG.dragonFlapNoiseDistance / 2),
            0.6F + this.random.nextFloat() * 0.6F * this.getVoicePitch());
      }

      if (this.onGround() && this.doesWantToLand() && (this.isFlying() || this.isHovering())) {
         this.setFlying(false);
         this.setHovering(false);
      }

      if (this.getControllingPassenger() != null && !this.onGround() && (this.isFlying() || this.isHovering())) {
         this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.0, 1.0));
      }

      if (this.isHovering()) {
         if (this.isSitting()) {
            this.setHovering(false);
         }
         this.hoverTicks++;
         if (this.doesWantToLand()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.25, 0.0));
         } else {
            if (this.getControllingPassenger() == null) {
               this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.08, 0.0));
            }
            if (this.hoverTicks > 40) {
               if (!this.isBaby()) {
                  this.setFlying(true);
               }
               this.setHovering(false);
               this.hoverTicks = 0;
               this.flyTicks = 0;
            }
         }
      }

      if (this.isSitting()) {
         this.getNavigation().stop();
      }

      if (!this.isFlying() && !this.isHovering() && this.airTarget != null && this.onGround()) {
         this.airTarget = null;
      }

      if (this.isFlying() && this.airTarget == null && this.onGround() && this.getControllingPassenger() == null) {
         this.setFlying(false);
      }

      if (this.isFlying() && this.getTarget() == null) {
         this.flyAround();
      } else if (this.getTarget() != null) {
         this.flyTowardsTarget();
      }

      if (this.onGround() && this.flyTicks != 0) {
         this.flyTicks = 0;
      }

      if (this.isFlying() && this.doesWantToLand()) {
         this.setFlying(false);
         this.setHovering(!this.onGround());
         if (this.onGround()) {
            this.flyTicks = 0;
            this.setFlying(false);
         }
      }

      if (this.isFlying()) {
         this.flyTicks++;
      }

      if ((this.isHovering() || this.isFlying()) && this.isSitting()) {
         this.setFlying(false);
         this.setHovering(false);
      }

      if (!stone
         && (!this.level().isClientSide && this.getRandom().nextInt(FLIGHT_CHANCE_PER_TICK) == 0 && !this.isSitting() && !this.isFlying()
               && this.getPassengers().isEmpty() && !this.isBaby() && !this.isHovering() && !this.isSitting() && this.canMove() && this.onGround()
            || this.getY() < -1.0)) {
         this.setHovering(true);
         this.hoverTicks = 0;
         this.flyTicks = 0;
      }

      if (this.getTarget() != null && !this.getPassengers().isEmpty() && this.getOwner() != null && this.getPassengers().contains(this.getOwner())) {
         this.setTarget(null);
      }
   }

   public boolean doesWantToLand() {
      return this.flyTicks > 6000 || this.down() || this.flyTicks > 40 && this.flyProgress == 0.0F;
   }

   @Override
   public void tick() {
      this.riddenMotion.update(this);
      super.tick();
      if (this.level().isClientSide) {
         AirWaterEntities.updateClientControls(this);
      }

      if (this.up()) {
         if (this.airBorneCounter == 0) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, 1.0, 0.0));
         }
         if (!this.isFlying() && !this.isHovering()) {
            this.spacebarTicks += 2;
         }
         if (this.isFlying() || this.isHovering()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.4, 0.0));
         }
      } else if (this.dismount() && (this.isFlying() || this.isHovering())) {
         this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.4, 0.0));
         this.setFlying(false);
         this.setHovering(false);
      }

      if (this.down() && (this.isFlying() || this.isHovering())) {
         this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.4, 0.0));
      }

      if (!this.dismount() && (this.isFlying() || this.isHovering())) {
         this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.01, 0.0));
      }

      if (this.attack() && this.getControllingPassenger() != null && this.getControllingPassenger() instanceof Player rider) {
         LivingEntity target = AirWaterUtils.riderLookingAtEntity(this, rider, 3.0);
         if (this.getAnimation() != ANIMATION_BITE && this.getAnimation() != ANIMATION_SCRATCH) {
            this.setAnimation(this.getRandom().nextBoolean() ? ANIMATION_SCRATCH : ANIMATION_BITE);
         }
         if (target != null) {
            target.hurt(this.damageSources().mobAttack(this), (float)(int)this.getAttributeValue(Attributes.ATTACK_DAMAGE));
         }
      }

      if (this.getControllingPassenger() != null && this.getControllingPassenger().isShiftKeyDown()) {
         this.getControllingPassenger().stopRiding();
      }

      Vec3 motion = this.riddenMotion.motion(this);
      if (this.isFlying() && !this.isHovering() && this.getControllingPassenger() != null && !this.onGround()
         && Math.max(Math.abs(motion.z), Math.abs(motion.x)) < 0.1F) {
         this.setHovering(true);
         this.setFlying(false);
      }

      if (this.isHovering() && !this.isFlying() && this.getControllingPassenger() != null && !this.onGround()
         && Math.max(Math.abs(motion.z), Math.abs(motion.x)) > 0.1F) {
         this.setFlying(true);
         this.setHovering(false);
      }

      if (this.spacebarTicks > 0) {
         this.spacebarTicks--;
      }

      if (this.spacebarTicks > 20 && this.getOwner() != null && this.getPassengers().contains(this.getOwner()) && !this.isFlying() && !this.isHovering()) {
         this.setHovering(true);
      }

      if (this.level().isClientSide) {
         AirWaterEntities.flapBuffer(this.roll_buffer, 35.0F, 8, 6.0F, this);
      }

      if (this.getTarget() != null && this.getVehicle() == null && !this.getTarget().isAlive()) {
         this.setTarget(null);
      }
   }

   public void flyAround() {
      if (this.airTarget != null && this.isFlying()) {
         if (!this.isTargetInAir() || this.flyTicks > 6000 || !this.isFlying()) {
            this.airTarget = null;
         }
         this.flyTowardsTarget();
      }
   }

   public boolean isTargetBlocked(Vec3 target) {
      if (target != null) {
         BlockHitResult rayTrace = AirWaterUtils.rayTraceBlocks(this, Vec3.atLowerCornerOf(this.blockPosition()), target, false);
         if (rayTrace != null) {
            BlockPos pos = BlockPos.containing(rayTrace.getLocation());
            if (!this.level().isEmptyBlock(pos)) {
               return true;
            }
         }
      }
      return false;
   }

   public void flyTowardsTarget() {
      if (this.airTarget != null && this.isTargetInAir() && this.isFlying()
         && this.getDistanceSquared(new Vec3((double)this.airTarget.getX(), this.getY(), (double)this.airTarget.getZ())) > 3.0F) {
         double targetX = (double)this.airTarget.getX() + 0.5 - this.getX();
         double targetY = (double)Math.min(this.airTarget.getY(), 256) + 1.0 - this.getY();
         double targetZ = (double)this.airTarget.getZ() + 0.5 - this.getZ();
         Vec3 m = this.getDeltaMovement();
         double mx = m.x + (Math.signum(targetX) * 0.5 - m.x) * 0.100000000372529 * 2.0;
         double my = m.y + (Math.signum(targetY) * 0.5 - m.y) * 0.100000000372529 * 2.0;
         double mz = m.z + (Math.signum(targetZ) * 0.5 - m.z) * 0.100000000372529 * 2.0;
         this.setDeltaMovement(mx, my, mz);
         float angle = (float)(Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0F;
         float rotation = Mth.wrapDegrees(angle - this.getYRot());
         this.zza = 0.5F;
         this.yRotO = this.getYRot();
         this.setYRot(this.getYRot() + rotation);
         if (!this.isFlying()) {
            this.setFlying(true);
         }
      } else {
         this.airTarget = null;
      }

      if (this.airTarget != null && this.isTargetInAir() && this.isFlying()
         && this.getDistanceSquared(new Vec3((double)this.airTarget.getX(), this.getY(), (double)this.airTarget.getZ())) < 3.0F
         && this.doesWantToLand()) {
         this.setFlying(false);
         this.setHovering(true);
      }
   }

   protected boolean isTargetInAir() {
      return this.airTarget != null && this.level().getBlockState(this.airTarget).isAir();
   }

   public float getDistanceSquared(Vec3 vec3d) {
      float f = (float)(this.getX() - vec3d.x);
      float f1 = (float)(this.getY() - vec3d.y);
      float f2 = (float)(this.getZ() - vec3d.z);
      return f * f + f1 * f1 + f2 * f2;
   }

   @Override
   public void die(DamageSource cause) {
      super.die(cause);
      if (this.hippogryphInventory != null && !this.level().isClientSide) {
         for (int i = 0; i < this.hippogryphInventory.getContainerSize(); i++) {
            ItemStack itemstack = this.hippogryphInventory.getItem(i);
            if (!itemstack.isEmpty()) {
               this.spawnAtLocation(itemstack, 0.0F);
            }
         }
      }
   }

   public void refreshInventory() {
      ItemStack saddle = this.hippogryphInventory.getItem(0);
      ItemStack chest = this.hippogryphInventory.getItem(1);
      this.setSaddled(saddle.is(Items.SADDLE) && !saddle.isEmpty());
      this.setChested(chest.is(Items.CHEST) && !chest.isEmpty());
      this.setArmor(this.getIntFromArmor(this.hippogryphInventory.getItem(2)));
   }

   @Override
   public void onHearFlute(Player player) {
      if (this.isTame() && this.isOwnedBy(player) && (this.isFlying() || this.isHovering())) {
         this.airTarget = null;
         this.setFlying(false);
         this.setHovering(false);
      }
   }

   @Override
   protected ResourceKey<LootTable> getDefaultLootTable() {
      return LOOT;
   }

   @Override
   public boolean shouldAnimalsFear(Entity entity) {
      return AirWaterUtils.canTameDragonAttack(this, entity);
   }

   @Override
   public void dropArmor() {
      if (this.hippogryphInventory != null && !this.level().isClientSide) {
         for (int i = 0; i < this.hippogryphInventory.getContainerSize(); i++) {
            ItemStack itemstack = this.hippogryphInventory.getItem(i);
            if (!itemstack.isEmpty()) {
               this.spawnAtLocation(itemstack, 0.0F);
            }
         }
      }
   }

   static class HippogryphInventoryListener implements ContainerListener {
      final EntityHippogryph hippogryph;

      HippogryphInventoryListener(EntityHippogryph hippogryph) {
         this.hippogryph = hippogryph;
      }

      @Override
      public void containerChanged(Container container) {
         this.hippogryph.refreshInventory();
      }
   }
}
