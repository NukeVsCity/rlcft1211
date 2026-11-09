package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.api.FoodUtils;
import com.github.alexthe666.iceandfire.core.ModItems;
import com.github.alexthe666.iceandfire.core.ModSounds;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAISit;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIWatchClosest;
import com.github.alexthe666.iceandfire.enums.EnumDragonEgg;
import com.github.alexthe666.iceandfire.inventory.ContainerDragon;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.ilexiconn.llibrary.server.animation.AnimationHandler;
import net.ilexiconn.llibrary.server.animation.IAnimatedEntity;
import net.ilexiconn.llibrary.server.entity.multipart.IMultipartEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Ice and Fire 1.7.1 EntityDragonBase (RLCraft 2.9.3). Ported line by line from the 1.12 class; names of fields and methods are
 * kept (the client renderer reads them) except where 1.21 already has a method with that name and other semantics:
 * 1.12 isSleeping() is {@link #isDragonSleeping()} (1.21 LivingEntity.isSleeping means "in a bed"), 1.12 isChild() is also
 * 1.21 isBaby(), attackEntityAsMob is reached through doHurtTarget, onLivingUpdate is aiStep and onUpdate is tick.
 * The AI runs on the 1.12 task scheduler ({@link EntityAITasks}).
 */
public abstract class EntityDragonBase extends TamableAnimal
   implements IMultipartEntity, IAnimatedEntity, IDragonFlute, IDeadMob, IVillagerFear, IAnimalFear, IDropArmor, EntityAISit.Sitter,
   EntityAIWatchClosest.FaceSpeeds {
   private static final int FLIGHT_CHANCE_PER_TICK = 1500;
   private static final EntityDataAccessor<Integer> HUNGER = SynchedEntityData.defineId(EntityDragonBase.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> AGE_TICKS = SynchedEntityData.defineId(EntityDragonBase.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> GENDER = SynchedEntityData.defineId(EntityDragonBase.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(EntityDragonBase.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> SLEEPING = SynchedEntityData.defineId(EntityDragonBase.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> FIREBREATHING = SynchedEntityData.defineId(EntityDragonBase.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> HOVERING = SynchedEntityData.defineId(EntityDragonBase.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> FLYING = SynchedEntityData.defineId(EntityDragonBase.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> HEAD_ARMOR = SynchedEntityData.defineId(EntityDragonBase.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> NECK_ARMOR = SynchedEntityData.defineId(EntityDragonBase.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> BODY_ARMOR = SynchedEntityData.defineId(EntityDragonBase.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> TAIL_ARMOR = SynchedEntityData.defineId(EntityDragonBase.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> MODEL_DEAD = SynchedEntityData.defineId(EntityDragonBase.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> DEATH_STAGE = SynchedEntityData.defineId(EntityDragonBase.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Byte> CONTROL_STATE = SynchedEntityData.defineId(EntityDragonBase.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Boolean> TACKLE = SynchedEntityData.defineId(EntityDragonBase.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> AGINGDISABLED = SynchedEntityData.defineId(EntityDragonBase.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> COMMAND = SynchedEntityData.defineId(EntityDragonBase.class, EntityDataSerializers.INT);
   /** Fire and ice dragons share these growth stages (render size range per stage). */
   private static final float[][] DEFAULT_STAGES = new float[][]{{1.0F, 3.0F}, {3.0F, 7.0F}, {7.0F, 12.5F}, {12.5F, 20.0F}, {20.0F, 30.0F}};
   // 1.12 quirk kept: these are static and recreated by every dragon constructor
   public static Animation ANIMATION_EAT;
   public static Animation ANIMATION_SPEAK;
   public static Animation ANIMATION_BITE;
   public static Animation ANIMATION_SHAKEPREY;
   public static Animation ANIMATION_WINGBLAST;
   public static Animation ANIMATION_ROAR;
   public static Animation ANIMATION_TAILWHACK;
   public double minimumDamage;
   public double maximumDamage;
   public double minimumHealth;
   public double maximumHealth;
   public double minimumSpeed;
   public double maximumSpeed;
   public double minimumArmor;
   public double maximumArmor;
   public float sitProgress;
   public float sleepProgress;
   public float hoverProgress;
   public float flyProgress;
   public float fireBreathProgress;
   public int fireStopTicks;
   public int flyTicks;
   public float modelDeadProgress;
   public float ridingProgress;
   public float tackleProgress;
   public SimpleContainer dragonInv;
   public boolean isDaytime;
   public boolean attackDecision;
   public int flightCycle;
   public BlockPos airTarget;
   public BlockPos homePos;
   public boolean hasHomePosition = false;
   /** Client-side chain buffers of the 1.12 renderer (IFChainBuffer roll, ReversedBuffer turn, ChainBuffer tail); set by the client part. */
   public Object roll_buffer;
   public Object turn_buffer;
   public Object tail_buffer;
   public int spacebarTicks;
   public float[][] growth_stages;
   public boolean isFire = this instanceof EntityFireDragon;
   /** 1.12 LegSolverQuadruped (client model helper); set by the client part. */
   public Object legSolver;
   protected int flyHovering;
   private boolean isSleeping;
   private boolean isSitting;
   private boolean isHovering;
   private boolean isFlying;
   private boolean isBreathingFire;
   private boolean isTackling;
   private int fireTicks;
   private int hoverTicks;
   private boolean isModelDead;
   private int animationTick;
   private Animation currentAnimation;
   public int walkCycle;
   private int tacklingTicks;
   private int ticksStill;
   private float lastScale;
   private float dragonScale = 1.0F;
   private EntityDragonPart headPart;
   private EntityDragonPart neckPart;
   private EntityDragonPart rightWingUpperPart;
   private EntityDragonPart rightWingLowerPart;
   private EntityDragonPart leftWingUpperPart;
   private EntityDragonPart leftWingLowerPart;
   private EntityDragonPart tail1Part;
   private EntityDragonPart tail2Part;
   private EntityDragonPart tail3Part;
   private EntityDragonPart tail4Part;
   /** 1.12 EntityLiving.tasks / targetTasks. */
   public final EntityAITasks tasks = new EntityAITasks();
   public final EntityAITasks targetTasks = new EntityAITasks();
   /** 1.12 EntityTameable.aiSit. */
   protected EntityAISit aiSit;

   /** Client hook for 1.12 updateClientControls (keybinds); set by the client classes. */
   public static volatile java.util.function.Consumer<EntityDragonBase> clientControls = dragon -> {
   };
   /** Client hook for the 1.12 renderer buffers (roll/turn/tail chain buffers, leg solver); set by the client part. */
   public static volatile java.util.function.Consumer<EntityDragonBase> clientBuffers = dragon -> {
   };

   protected EntityDragonBase(
      EntityType<? extends EntityDragonBase> type,
      Level world,
      double minimumDamage,
      double maximumDamage,
      double minimumHealth,
      double maximumHealth,
      double minimumSpeed,
      double maximumSpeed
   ) {
      super(type, world);
      this.minimumDamage = minimumDamage;
      this.maximumDamage = maximumDamage;
      this.minimumHealth = minimumHealth;
      this.maximumHealth = maximumHealth;
      this.minimumSpeed = minimumSpeed;
      this.maximumSpeed = maximumSpeed;
      this.minimumArmor = 1.0;
      this.maximumArmor = 20.0;
      ANIMATION_EAT = Animation.create(20);
      this.updateAttributes();
      this.initDragonInv();
      this.resetParts(1.0F);
      this.noCulling = true;
      this.initEntityAI();
      EntityAITasks.install(this, this.goalSelector, this.tasks, this.targetSelector, this.targetTasks);
   }

   /** 1.12 applyEntityAttributes (EntityLiving + EntityDragonBase). */
   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes()
         .add(Attributes.MOVEMENT_SPEED, 0.3)
         .add(Attributes.MAX_HEALTH, 20.0)
         .add(Attributes.ATTACK_DAMAGE, 1.0)
         .add(Attributes.FOLLOW_RANGE, (double)Math.min(2048, IceAndFire.CONFIG.dragonTargetSearchLength))
         .add(Attributes.ARMOR, 4.0)
         .add(Attributes.STEP_HEIGHT, 1.0);
   }

   /** 1.12 initEntityAI: fills {@link #tasks} and {@link #targetTasks}. */
   protected abstract void initEntityAI();

   @Override
   protected void registerGoals() {
      // the 1.12 tasks are added in initEntityAI, after the subclass fields exist
   }

   public RandomSource getRNG() {
      return this.getRandom();
   }

   public void resetParts(float scale) {
      this.removeParts();
      this.headPart = new EntityDragonPart(this, 1.55F * scale, 0.0F, 0.6F * scale, 0.5F * scale, 0.35F * scale, 1.5F);
      this.neckPart = new EntityDragonPart(this, 0.85F * scale, 0.0F, 0.7F * scale, 0.5F * scale, 0.2F * scale, 1.0F);
      this.rightWingUpperPart = new EntityDragonPart(this, 1.0F * scale, 90.0F, 0.5F * scale, 0.85F * scale, 0.3F * scale, 0.5F);
      this.rightWingLowerPart = new EntityDragonPart(this, 1.4F * scale, 100.0F, 0.3F * scale, 0.85F * scale, 0.2F * scale, 0.5F);
      this.leftWingUpperPart = new EntityDragonPart(this, 1.0F * scale, -90.0F, 0.5F * scale, 0.85F * scale, 0.3F * scale, 0.5F);
      this.leftWingLowerPart = new EntityDragonPart(this, 1.4F * scale, -100.0F, 0.3F * scale, 0.85F * scale, 0.2F * scale, 0.5F);
      this.tail1Part = new EntityDragonPart(this, -0.75F * scale, 0.0F, 0.6F * scale, 0.35F * scale, 0.35F * scale, 1.0F);
      this.tail2Part = new EntityDragonPart(this, -1.15F * scale, 0.0F, 0.45F * scale, 0.35F * scale, 0.35F * scale, 1.0F);
      this.tail3Part = new EntityDragonPart(this, -1.5F * scale, 0.0F, 0.35F * scale, 0.35F * scale, 0.35F * scale, 1.0F);
      this.tail4Part = new EntityDragonPart(this, -1.95F * scale, 0.0F, 0.25F * scale, 0.45F * scale, 0.3F * scale, 1.5F);
   }

   /** 1.12 removeParts: the parts were never in the world (1.12 getParts() returned null), so dropping them is enough. */
   public void removeParts() {
      this.headPart = null;
      this.neckPart = null;
      this.rightWingUpperPart = null;
      this.rightWingLowerPart = null;
      this.leftWingUpperPart = null;
      this.leftWingLowerPart = null;
      this.tail1Part = null;
      this.tail2Part = null;
      this.tail3Part = null;
      this.tail4Part = null;
   }

   public void updateParts() {
      for (EntityDragonPart part : this.getDragonParts()) {
         if (part != null) {
            part.onUpdate();
         }
      }
   }

   /** The ten 1.12 hitbox parts (head, neck, wings, tail). Like in 1.12 they are not part of world queries. */
   public EntityDragonPart[] getDragonParts() {
      return new EntityDragonPart[]{
         this.headPart, this.neckPart, this.rightWingUpperPart, this.rightWingLowerPart, this.leftWingUpperPart, this.leftWingLowerPart,
         this.tail1Part, this.tail2Part, this.tail3Part, this.tail4Part
      };
   }

   public boolean canDestroyBlock(BlockPos pos) {
      BlockState state = this.level().getBlockState(pos);
      float hardness = state.getDestroySpeed(this.level(), pos);
      return state.canEntityDestroy(this.level(), pos, this) && hardness >= 0.0F;
   }

   @Override
   public boolean isMobDead() {
      return this.isModelDead();
   }

   @Override
   public int getHorizontalFaceSpeed() {
      return 10 * this.getDragonStage() / 5;
   }

   private void initDragonInv() {
      SimpleContainer animalchest = this.dragonInv;
      this.dragonInv = new SimpleContainer(4) {
         @Override
         public void setChanged() {
            super.setChanged();
            EntityDragonBase.this.onInventoryChanged();
         }
      };
      if (animalchest != null) {
         int i = Math.min(animalchest.getContainerSize(), this.dragonInv.getContainerSize());

         for (int j = 0; j < i; j++) {
            ItemStack itemstack = animalchest.getItem(j);
            if (!itemstack.isEmpty()) {
               this.dragonInv.setItem(j, itemstack.copy());
            }
         }
      }
   }

   /** 1.12 ItemStackHandler.onContentsChanged of the dragon: armor sound when a piece changes, then updateDragonSlots. */
   private void onInventoryChanged() {
      if (this.level().isClientSide) {
         return;
      }
      int dragonArmorHead = this.getArmorInSlot(0);
      int dragonArmorNeck = this.getArmorInSlot(1);
      int dragonArmorBody = this.getArmorInSlot(2);
      int dragonArmorTail = this.getArmorInSlot(3);
      if (this.tickCount > 20) {
         if (dragonArmorHead != this.getIntFromArmor(this.dragonInv.getItem(0))
            || dragonArmorNeck != this.getIntFromArmor(this.dragonInv.getItem(1))
            || dragonArmorBody != this.getIntFromArmor(this.dragonInv.getItem(2))
            || dragonArmorTail != this.getIntFromArmor(this.dragonInv.getItem(3))) {
            this.playSound(SoundEvents.HORSE_ARMOR.value(), 0.5F, 1.0F);
         }
      }
      this.updateDragonSlots();
   }

   public void updateDragonSlots() {
      this.setArmorInSlot(0, this.getIntFromArmor(this.dragonInv.getItem(0)));
      this.setArmorInSlot(1, this.getIntFromArmor(this.dragonInv.getItem(1)));
      this.setArmorInSlot(2, this.getIntFromArmor(this.dragonInv.getItem(2)));
      this.setArmorInSlot(3, this.getIntFromArmor(this.dragonInv.getItem(3)));
      double armorStep = (this.maximumArmor - this.minimumArmor) / 125.0;
      double oldValue = this.minimumArmor + armorStep * (double)this.getAgeInDays();
      this.getAttribute(Attributes.ARMOR).setBaseValue(oldValue + this.calculateArmorModifier());
   }

   public void openGUI(Player playerEntity) {
      if (!this.level().isClientSide && (!this.isVehicle() || this.hasPassenger(playerEntity))) {
         playerEntity.openMenu(
            new SimpleMenuProvider((id, inventory, player) -> new ContainerDragon(id, inventory, this), this.getDisplayName()),
            buf -> buf.writeInt(this.getId())
         );
      }
   }

   @Override
   public int getAmbientSoundInterval() {
      return 90;
   }

   /** 1.12 onDeathUpdate: the corpse stays (model dead) until it is harvested down to its last death stage. */
   @Override
   protected void tickDeath() {
      this.deathTime = 0;
      if (!this.isModelDead() && !this.level().isClientSide && this.lastHurtByPlayerTime > 0) {
         int i = this.getExperiencePoints(this.lastHurtByPlayer);
         i = net.neoforged.neoforge.event.EventHooks.getExperienceDrop(this, this.lastHurtByPlayer, i);
         ExperienceOrb.award((ServerLevel)this.level(), this.position(), i);
      }

      this.setModelDead(true);
      if (this.getDeathStage() >= this.getAgeInDays() / 5) {
         this.discard();

         for (int k = 0; k < 40; k++) {
            double d2 = this.random.nextGaussian() * 0.02;
            double d0 = this.random.nextGaussian() * 0.02;
            double d1 = this.random.nextGaussian() * 0.02;
            if (this.level().isClientSide) {
               this.level()
                  .addParticle(
                     ParticleTypes.POOF,
                     this.getX() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
                     this.getY() + (double)(this.random.nextFloat() * this.getBbHeight()),
                     this.getZ() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
                     d2,
                     d0,
                     d1
                  );
            }
         }

         for (int kx = 0; kx < 3; kx++) {
            double d2 = this.random.nextGaussian() * 0.02;
            double d0 = this.random.nextGaussian() * 0.02;
            double d1 = this.random.nextGaussian() * 0.02;
            double px = this.getX() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth();
            double py = this.getY() + (double)(this.random.nextFloat() * this.getBbHeight());
            double pz = this.getZ() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth();
            if (this.isFire) {
               if (this.level().isClientSide) {
                  this.level().addParticle(ParticleTypes.FLAME, px, py, pz, d2, d0, d1);
               }
            } else {
               DragonParticles.spawnParticle("snowflake", this.level(), px, py, pz, d2, d0, d1);
            }
         }
      }
   }

   /** XP is dropped by {@link #tickDeath} like 1.12, not by 1.21's die(). */
   @Override
   protected void dropExperience(@Nullable Entity attacker) {
   }

   @Override
   public void remove(Entity.RemovalReason reason) {
      this.removeParts();
      super.remove(reason);
   }

   protected int getExperiencePoints(@Nullable Player player) {
      return this.isChild() ? 15 : 15 + this.getDragonStage() * 35;
   }

   public int getIntFromArmor(ItemStack stack) {
      if (!stack.isEmpty() && stack.is(ModItems.dragon_armor_iron)) {
         return 1;
      } else if (!stack.isEmpty() && stack.is(ModItems.dragon_armor_gold)) {
         return 2;
      } else {
         return !stack.isEmpty() && stack.is(ModItems.dragon_armor_diamond) ? 3 : 0;
      }
   }

   /** 1.12 isAIDisabled. */
   @Override
   public boolean isNoAi() {
      return this.isModelDead() || DragonCompat.isStone(this) || super.isNoAi();
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(HUNGER, 0);
      builder.define(AGE_TICKS, 0);
      builder.define(GENDER, false);
      builder.define(VARIANT, 0);
      builder.define(SLEEPING, false);
      builder.define(FIREBREATHING, false);
      builder.define(HOVERING, false);
      builder.define(FLYING, false);
      builder.define(HEAD_ARMOR, 0);
      builder.define(NECK_ARMOR, 0);
      builder.define(BODY_ARMOR, 0);
      builder.define(TAIL_ARMOR, 0);
      builder.define(DEATH_STAGE, 0);
      builder.define(MODEL_DEAD, false);
      builder.define(CONTROL_STATE, (byte)0);
      builder.define(TACKLE, false);
      builder.define(AGINGDISABLED, false);
      builder.define(COMMAND, 0);
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

   public boolean strike() {
      return (this.entityData.get(CONTROL_STATE) >> 3 & 1) == 1;
   }

   public boolean dismount() {
      return (this.entityData.get(CONTROL_STATE) >> 4 & 1) == 1;
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

   public void strike(boolean strike) {
      this.setStateField(3, strike);
   }

   public void dismount(boolean dismount) {
      this.setStateField(4, dismount);
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

   public void setCommand(int command) {
      this.entityData.set(COMMAND, command);
      if (command == 1) {
         this.setSitting(true);
      } else {
         this.setSitting(false);
      }
   }

   public int getCommand() {
      return this.entityData.get(COMMAND);
   }

   @Override
   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      compound.putBoolean("Sitting", this.isSitting());
      compound.putInt("Hunger", this.getHunger());
      compound.putInt("AgeTicks", this.getAgeInTicks());
      compound.putBoolean("Gender", this.isMale());
      compound.putInt("Variant", this.getVariant());
      compound.putBoolean("Sleeping", this.isDragonSleeping());
      compound.putBoolean("FireBreathing", this.isBreathingFire());
      compound.putBoolean("AttackDecision", this.attackDecision);
      compound.putBoolean("Hovering", this.isHovering());
      compound.putBoolean("Flying", this.isFlying());
      compound.putInt("ArmorHead", this.getArmorInSlot(0));
      compound.putInt("ArmorNeck", this.getArmorInSlot(1));
      compound.putInt("ArmorBody", this.getArmorInSlot(2));
      compound.putInt("ArmorTail", this.getArmorInSlot(3));
      compound.putInt("DeathStage", this.getDeathStage());
      compound.putBoolean("ModelDead", this.isModelDead());
      compound.putFloat("DeadProg", this.modelDeadProgress);
      compound.putBoolean("Tackle", this.isTackling());
      if (this.dragonInv != null) {
         ListTag nbttaglist = new ListTag();

         for (int i = 0; i < this.dragonInv.getContainerSize(); i++) {
            ItemStack itemstack = this.dragonInv.getItem(i);
            if (!itemstack.isEmpty()) {
               CompoundTag nbttagcompound = new CompoundTag();
               nbttagcompound.putByte("Slot", (byte)i);
               nbttaglist.add(itemstack.save(this.registryAccess(), nbttagcompound));
            }
         }

         compound.put("Items", nbttaglist);
      }

      compound.putBoolean("HasHomePosition", this.hasHomePosition);
      if (this.homePos != null && this.hasHomePosition) {
         compound.putInt("HomeAreaX", this.homePos.getX());
         compound.putInt("HomeAreaY", this.homePos.getY());
         compound.putInt("HomeAreaZ", this.homePos.getZ());
      }

      compound.putBoolean("AgingDisabled", this.isAgingDisabled());
      compound.putInt("Command", this.getCommand());
   }

   @Override
   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      if (this.aiSit != null) {
         this.aiSit.setSitting(compound.getBoolean("Sitting"));
      }
      this.setHunger(compound.getInt("Hunger"));
      this.setAgeInTicks(compound.getInt("AgeTicks"));
      this.setGender(compound.getBoolean("Gender"));
      this.setVariant(compound.getInt("Variant"));
      this.setSleeping(compound.getBoolean("Sleeping"));
      this.setBreathingFire(compound.getBoolean("FireBreathing"));
      this.attackDecision = compound.getBoolean("AttackDecision");
      this.setHovering(compound.getBoolean("Hovering"));
      this.setFlying(compound.getBoolean("Flying"));
      this.setArmorInSlot(0, compound.getInt("ArmorHead"));
      this.setArmorInSlot(1, compound.getInt("ArmorNeck"));
      this.setArmorInSlot(2, compound.getInt("ArmorBody"));
      this.setArmorInSlot(3, compound.getInt("ArmorTail"));
      ListTag nbttaglist = compound.getList("Items", Tag.TAG_COMPOUND);
      this.initDragonInv();

      for (int i = 0; i < nbttaglist.size(); i++) {
         CompoundTag nbttagcompound = nbttaglist.getCompound(i);
         int j = nbttagcompound.getByte("Slot") & 255;
         if (j < this.dragonInv.getContainerSize()) {
            this.dragonInv.setItem(j, ItemStack.parseOptional(this.registryAccess(), nbttagcompound));
         }
      }

      this.setDeathStage(compound.getInt("DeathStage"));
      this.setModelDead(compound.getBoolean("ModelDead"));
      this.modelDeadProgress = compound.getFloat("DeadProg");
      this.hasHomePosition = compound.getBoolean("HasHomePosition");
      if (this.hasHomePosition && compound.getInt("HomeAreaX") != 0 && compound.getInt("HomeAreaY") != 0 && compound.getInt("HomeAreaZ") != 0) {
         this.homePos = new BlockPos(compound.getInt("HomeAreaX"), compound.getInt("HomeAreaY"), compound.getInt("HomeAreaZ"));
      }

      this.setTackling(compound.getBoolean("Tackle"));
      this.setAgingDisabled(compound.getBoolean("AgingDisabled"));
      this.setCommand(compound.getInt("Command"));
      this.updateAttributes();
   }

   @Nullable
   @Override
   public LivingEntity getControllingPassenger() {
      for (Entity passenger : this.getPassengers()) {
         if (passenger instanceof Player && this.getTarget() != passenger) {
            Player player = (Player)passenger;
            if (this.isTame() && this.getOwnerUUID() != null && this.getOwnerUUID().equals(player.getUUID())) {
               return player;
            }
         }
      }

      return null;
   }

   public boolean isRidingPlayer(Player player) {
      return this.getControllingPassenger() != null
         && this.getControllingPassenger() instanceof Player
         && this.getControllingPassenger().getUUID().equals(player.getUUID());
   }

   private void updateAttributes() {
      double healthStep = (this.maximumHealth - this.minimumHealth) / 125.0;
      double attackStep = (this.maximumDamage - this.minimumDamage) / 125.0;
      double speedStep = (this.maximumSpeed - this.minimumSpeed) / 125.0;
      double armorStep = (this.maximumArmor - this.minimumArmor) / 125.0;
      if (this.getAgeInDays() <= 125) {
         this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double)Math.round(this.minimumHealth + healthStep * (double)this.getAgeInDays()));
         this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double)Math.round(this.minimumDamage + attackStep * (double)this.getAgeInDays()));
         this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.minimumSpeed + speedStep * (double)this.getAgeInDays());
         double oldValue = this.minimumArmor + armorStep * (double)this.getAgeInDays();
         this.getAttribute(Attributes.ARMOR).setBaseValue(oldValue + this.calculateArmorModifier());
      }
   }

   public int getHunger() {
      return this.entityData.get(HUNGER);
   }

   public void setHunger(int hunger) {
      this.entityData.set(HUNGER, Math.min(100, hunger));
   }

   public int getVariant() {
      return this.entityData.get(VARIANT);
   }

   public void setVariant(int variant) {
      this.entityData.set(VARIANT, variant);
   }

   public int getAgeInDays() {
      return this.entityData.get(AGE_TICKS) / 24000;
   }

   public void setAgeInDays(int age) {
      this.entityData.set(AGE_TICKS, age * 24000);
   }

   public int getAgeInTicks() {
      return this.entityData.get(AGE_TICKS);
   }

   public void setAgeInTicks(int age) {
      this.entityData.set(AGE_TICKS, age);
   }

   public int getDeathStage() {
      return this.entityData.get(DEATH_STAGE);
   }

   public void setDeathStage(int stage) {
      this.entityData.set(DEATH_STAGE, stage);
   }

   public boolean isMale() {
      return this.entityData.get(GENDER);
   }

   public boolean isModelDead() {
      return this.level().isClientSide ? (this.isModelDead = this.entityData.get(MODEL_DEAD)) : this.isModelDead;
   }

   public void setModelDead(boolean modeldead) {
      this.entityData.set(MODEL_DEAD, modeldead);
      if (!this.level().isClientSide) {
         this.isModelDead = modeldead;
      }
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

   public void setGender(boolean male) {
      this.entityData.set(GENDER, male);
   }

   /** 1.12 EntityDragonBase.isSleeping (renamed: 1.21 LivingEntity.isSleeping means "lying in a bed"). */
   public boolean isDragonSleeping() {
      if (this.level().isClientSide) {
         boolean isSleeping = this.entityData.get(SLEEPING);
         this.isSleeping = isSleeping;
         return isSleeping;
      } else {
         return this.isSleeping;
      }
   }

   public void setSleeping(boolean sleeping) {
      this.entityData.set(SLEEPING, sleeping);
      if (!this.level().isClientSide) {
         this.isSleeping = sleeping;
      }
   }

   public boolean isBlinking() {
      return this.tickCount % 50 > 43;
   }

   public boolean isBreathingFire() {
      if (this.level().isClientSide) {
         boolean breathing = this.entityData.get(FIREBREATHING);
         this.isBreathingFire = breathing;
         return breathing;
      } else {
         return this.isBreathingFire;
      }
   }

   public void setBreathingFire(boolean breathing) {
      this.entityData.set(FIREBREATHING, breathing);
      if (!this.level().isClientSide) {
         this.isBreathingFire = breathing;
      }
   }

   public void setTackling(boolean tackling) {
      this.entityData.set(TACKLE, tackling);
      if (!this.level().isClientSide) {
         this.isTackling = tackling;
      }
   }

   public void setAgingDisabled(boolean isAgingDisabled) {
      this.entityData.set(AGINGDISABLED, isAgingDisabled);
   }

   @Override
   protected boolean canAddPassenger(Entity passenger) {
      return this.getPassengers().size() < 2;
   }

   /** 1.12 isSitting: the sitting bit of EntityTameable's TAMED byte (1.21 TamableAnimal's sitting pose). */
   public boolean isSitting() {
      if (this.level().isClientSide) {
         boolean isSitting = this.isInSittingPose();
         this.isSitting = isSitting;
         return isSitting;
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

   public int getArmorInSlot(int i) {
      switch (i) {
         case 1:
            return this.entityData.get(NECK_ARMOR);
         case 2:
            return this.entityData.get(BODY_ARMOR);
         case 3:
            return this.entityData.get(TAIL_ARMOR);
         default:
            return this.entityData.get(HEAD_ARMOR);
      }
   }

   public void riderShootFire(Entity controller) {
   }

   /** 1.12 onKillEntity. */
   @Override
   public boolean killedEntity(ServerLevel level, LivingEntity entity) {
      boolean result = super.killedEntity(level, entity);
      this.setHunger(this.getHunger() + FoodUtils.getFoodPoints(entity));
      return result;
   }

   public void setArmorInSlot(int i, int armorType) {
      switch (i) {
         case 0:
            this.entityData.set(HEAD_ARMOR, armorType);
            break;
         case 1:
            this.entityData.set(NECK_ARMOR, armorType);
            break;
         case 2:
            this.entityData.set(BODY_ARMOR, armorType);
            break;
         case 3:
            this.entityData.set(TAIL_ARMOR, armorType);
      }

      double armorStep = (this.maximumArmor - this.minimumArmor) / 125.0;
      double oldValue = this.minimumArmor + armorStep * (double)this.getAgeInDays();
      this.getAttribute(Attributes.ARMOR).setBaseValue(oldValue + this.calculateArmorModifier());
   }

   private double calculateArmorModifier() {
      double val = 1.0;

      for (int i = 0; i < 4; i++) {
         switch (this.getArmorInSlot(i)) {
            case 1:
               val += 2.0;
               break;
            case 2:
               val += 3.0;
               break;
            case 3:
               val += 5.0;
         }
      }

      return val;
   }

   public boolean canMove() {
      return DragonCompat.isStone(this)
         ? false
         : !this.isSitting()
            && !this.isDragonSleeping()
            && this.getControllingPassenger() == null
            && !this.isModelDead()
            && this.sleepProgress == 0.0F
            && this.getAnimation() != ANIMATION_SHAKEPREY;
   }

   /**
    * 1.12 processInteract while the dragon is a corpse (model dead): harvesting blood, the heart (and an egg from females of
    * stage 4+), the skull and loot-table drops one death stage at a time. Reached through the EntityInteract event because
    * 1.21's Mob.interact refuses dead mobs.
    */
   public boolean processDeadInteract(Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      int lastDeathStage = this.getAgeInDays() / 5;
      if (this.isModelDead() && this.getDeathStage() < lastDeathStage && player.mayBuild()) {
         if (!this.level().isClientSide
            && !stack.isEmpty()
            && stack.is(Items.GLASS_BOTTLE)
            && this.getDeathStage() < lastDeathStage / 2
            && IceAndFire.CONFIG.dragonDropBlood) {
            if (!player.getAbilities().instabuild) {
               stack.shrink(1);
            }

            this.setDeathStage(this.getDeathStage() + 1);
            player.getInventory().add(new ItemStack(this instanceof EntityFireDragon ? ModItems.fire_dragon_blood : ModItems.ice_dragon_blood, 1));
            return true;
         } else {
            if (!this.level().isClientSide && stack.isEmpty() && IceAndFire.CONFIG.dragonDropSkull) {
               if (this.getDeathStage() == lastDeathStage - 1) {
                  ItemStack skull = DragonCompat.dragonSkull(this.isFire ? 0 : 1, this.getDragonStage(), this.getAgeInDays());
                  this.setDeathStage(this.getDeathStage() + 1);
                  if (!this.level().isClientSide) {
                     this.spawnAtLocation(skull, 1.0F);
                  }

                  this.discard();
               } else if (this.getDeathStage() == lastDeathStage / 2 - 1 && IceAndFire.CONFIG.dragonDropHeart) {
                  ItemStack heart = new ItemStack(this instanceof EntityFireDragon ? ModItems.fire_dragon_heart : ModItems.ice_dragon_heart, 1);
                  ItemStack egg = new ItemStack(this.getVariantEgg(this.random.nextInt(4)), 1);
                  if (!this.level().isClientSide) {
                     this.spawnAtLocation(heart, 1.0F);
                     if (!this.isMale() && this.getDragonStage() > 3) {
                        this.spawnAtLocation(egg, 1.0F);
                     }
                  }

                  this.setDeathStage(this.getDeathStage() + 1);
               } else {
                  this.setDeathStage(this.getDeathStage() + 1);
                  ItemStack drop = this.getRandomDrop();
                  if (!drop.isEmpty() && !this.level().isClientSide) {
                     this.spawnAtLocation(drop, 1.0F);
                  }
               }
            }

            return true;
         }
      }
      return false;
   }

   /** 1.12 processInteract of a living dragon. */
   @Override
   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (this.isModelDead()) {
         return this.processDeadInteract(player, hand) ? InteractionResult.SUCCESS : super.mobInteract(player, hand);
      }
      if (!this.isModelDead() && this.isOwner(player)) {
         if (!stack.isEmpty()) {
            if (this.isFood(stack) && this.isAdult()) {
               this.setAge(0);
               this.usePlayerItem(player, hand, stack);
               this.setInLove(player);
               return InteractionResult.SUCCESS;
            }

            int itemFoodAmount = FoodUtils.getFoodPoints(stack, true, !this.isFire);
            if (itemFoodAmount <= 0 || this.getHunger() >= 100 && !(this.getHealth() < this.getMaxHealth())) {
               if (stack.is(ModItems.dragon_meal)) {
                  this.growDragon(1);
                  this.setHunger(this.getHunger() + 20);
                  this.heal(Math.min(this.getHealth(), (float)((int)(this.getMaxHealth() / 2.0F))));
                  this.playSound(SoundEvents.GENERIC_EAT, this.getSoundVolume(), this.getVoicePitch());
                  this.spawnItemCrackParticles(stack.getItem());
                  this.spawnItemCrackParticles(Items.BONE);
                  this.spawnItemCrackParticles(Items.BONE_MEAL);
                  this.eatFoodBonus(stack);
                  if (!player.isCreative()) {
                     stack.shrink(1);
                  }

                  return InteractionResult.SUCCESS;
               }

               if (stack.is(ModItems.sickly_dragon_meal) && !this.isAgingDisabled()) {
                  this.setHunger(this.getHunger() + 20);
                  this.heal(this.getMaxHealth());
                  this.playSound(SoundEvents.ZOMBIE_VILLAGER_CURE, this.getSoundVolume(), this.getVoicePitch());
                  this.spawnItemCrackParticles(stack.getItem());
                  this.spawnItemCrackParticles(Items.BONE);
                  this.spawnItemCrackParticles(Items.BONE_MEAL);
                  this.spawnItemCrackParticles(Items.POISONOUS_POTATO);
                  this.spawnItemCrackParticles(Items.POISONOUS_POTATO);
                  this.setAgingDisabled(true);
                  this.eatFoodBonus(stack);
                  if (!player.isCreative()) {
                     stack.shrink(1);
                  }

                  return InteractionResult.SUCCESS;
               }

               if (stack.is(ModItems.dragon_stick)) {
                  if (player.isShiftKeyDown()) {
                     BlockPos pos = this.blockPosition();
                     this.homePos = pos;
                     this.hasHomePosition = true;
                     player.displayClientMessage(Component.translatable("dragon.command.new_home", this.homePos.getX(), this.homePos.getY(), this.homePos.getZ()), true);
                     return InteractionResult.SUCCESS;
                  }

                  this.playSound(SoundEvents.ZOMBIE_INFECT, this.getSoundVolume(), this.getVoicePitch());
                  this.setCommand(this.getCommand() + 1);
                  if (this.getCommand() > 1) {
                     this.setCommand(0);
                  }

                  player.displayClientMessage(Component.translatable("dragon.command." + (this.getCommand() == 1 ? "sit" : "stand")), true);
                  return InteractionResult.SUCCESS;
               }

               if (!stack.is(ModItems.dragon_horn) || this.level().isClientSide || DragonCompat.isStone(this)) {
                  return super.mobInteract(player, hand);
               }

               this.playSound(SoundEvents.ZOMBIE_VILLAGER_CONVERTED, 3.0F, 1.25F);
               ItemStack stack1 = new ItemStack(this.isFire ? ModItems.dragon_horn_fire : ModItems.dragon_horn_ice);
               CompoundTag tag = new CompoundTag();
               this.addAdditionalSaveData(tag);
               DragonCompat.storeDragonInHorn(stack1, this, tag);
               player.setItemInHand(hand, stack1);
               this.discard();
               return InteractionResult.SUCCESS;
            }

            this.setHunger(this.getHunger() + itemFoodAmount);
            this.setHealth(Math.min(this.getMaxHealth(), (float)((int)(this.getHealth() + (float)(itemFoodAmount / 10)))));
            this.playSound(SoundEvents.GENERIC_EAT, this.getSoundVolume(), this.getVoicePitch());
            this.spawnItemCrackParticles(stack.getItem());
            this.eatFoodBonus(stack);
            if (!player.isCreative()) {
               stack.shrink(1);
            }

            return InteractionResult.SUCCESS;
         } else {
            if (stack.isEmpty() && !player.isShiftKeyDown() && !this.isRemoved()) {
               if (this.getDragonStage() < 2) {
                  this.startRiding(player, true);
               }

               if (this.getDragonStage() > 2 && !player.isPassenger()) {
                  player.setShiftKeyDown(false);
                  player.startRiding(this, true);
                  this.setSleeping(false);
               }

               if (this.getDragonStage() < 2) {
                  this.startRiding(player, true);
               }

               return InteractionResult.SUCCESS;
            }

            if (stack.isEmpty() && player.isShiftKeyDown()) {
               this.openGUI(player);
               return InteractionResult.SUCCESS;
            }
         }
      }

      return super.mobInteract(player, hand);
   }

   /** 1.12 EntityTameable.isOwner. */
   public boolean isOwner(@Nullable LivingEntity entity) {
      return entity == this.getOwner();
   }

   private ItemStack getRandomDrop() {
      ItemStack stack = this.getItemFromLootTable();
      if (stack.is(ModItems.dragonbone)) {
         this.playSound(SoundEvents.SKELETON_AMBIENT, 1.0F, 1.0F);
      } else {
         this.playSound(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 1.0F, 1.0F);
      }

      return stack;
   }

   public abstract ResourceKey<LootTable> getDeadLootTable();

   public ItemStack getItemFromLootTable() {
      if (!(this.level() instanceof ServerLevel serverLevel)) {
         return ItemStack.EMPTY;
      }
      LootTable loottable = serverLevel.getServer().reloadableRegistries().getLootTable(this.getDeadLootTable());
      LootParams.Builder builder = new LootParams.Builder(serverLevel)
         .withParameter(LootContextParams.THIS_ENTITY, this)
         .withParameter(LootContextParams.ORIGIN, this.position())
         .withParameter(LootContextParams.DAMAGE_SOURCE, this.damageSources().generic());
      if (this.lastHurtByPlayer != null) {
         builder = builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, this.lastHurtByPlayer).withLuck(this.lastHurtByPlayer.getLuck());
      }

      List<ItemStack> loot = loottable.getRandomItems(builder.create(LootContextParamSets.ENTITY), this.random);
      return loot.isEmpty() ? ItemStack.EMPTY : loot.get(0);
   }

   public void eatFoodBonus(ItemStack stack) {
   }

   /** 1.12 despawnEntity: EntityAnimal.canDespawn() is false, so dragons never despawn (whatever "Dragons Despawn" says). */
   @Override
   public boolean removeWhenFarAway(double distance) {
      return false;
   }

   public void growDragon(int ageInDays) {
      if (!this.isAgingDisabled()) {
         this.setAgeInDays(this.getAgeInDays() + ageInDays);
         this.setScaleForAge(false);
         if (this.getAgeInDays() % 25 == 0) {
            for (int i = 0; (float)i < this.getRenderSize() * 4.0F; i++) {
               double motionX = this.getRNG().nextGaussian() * 0.07;
               double motionY = this.getRNG().nextGaussian() * 0.07;
               double motionZ = this.getRNG().nextGaussian() * 0.07;
               AABB bb = this.getBoundingBox();
               float f = (float)((double)this.getRNG().nextFloat() * (bb.maxX - bb.minX) + bb.minX);
               float f1 = (float)((double)this.getRNG().nextFloat() * (bb.maxY - bb.minY) + bb.minY);
               float f2 = (float)((double)this.getRNG().nextFloat() * (bb.maxZ - bb.minZ) + bb.minZ);
               if (this.level().isClientSide) {
                  this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, (double)f, (double)f1, (double)f2, motionX, motionY, motionZ);
               }
            }
         }

         this.updateAttributes();
      }
   }

   public void spawnItemCrackParticles(Item item) {
      for (int i = 0; i < 15; i++) {
         double motionX = this.getRNG().nextGaussian() * 0.07;
         double motionY = this.getRNG().nextGaussian() * 0.07;
         double motionZ = this.getRNG().nextGaussian() * 0.07;
         float headPosX = (float)(this.getX() + (double)(1.9F * this.getRenderSize() * 0.3F) * Math.cos((double)(this.getYRot() + 90.0F) * Math.PI / 180.0));
         float headPosZ = (float)(this.getZ() + (double)(1.9F * this.getRenderSize() * 0.3F) * Math.sin((double)(this.getYRot() + 90.0F) * Math.PI / 180.0));
         float headPosY = (float)(this.getY() + (double)(this.getRenderSize() * 0.2F));
         if (this.level().isClientSide) {
            this.level()
               .addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(item)), (double)headPosX, (double)headPosY, (double)headPosZ, motionX, motionY, motionZ);
         }
      }
   }

   public boolean isDaytime() {
      return this.level().isDay();
   }

   private boolean isStuck() {
      return !this.isTame()
         && (
            !this.getNavigation().isDone()
                  && (
                     this.getNavigation().getPath() == null
                        || this.getNavigation().getPath().getEndNode() != null
                           && this.distanceToSqr(Vec3.atLowerCornerOf(this.endNodePos(this.getNavigation().getPath().getEndNode()))) > 15.0
                  )
               || this.airTarget != null
         )
         && this.ticksStill > 80
         && !this.isHovering()
         && this.canMove();
   }

   private BlockPos endNodePos(Node node) {
      return new BlockPos(node.x, node.y, node.z);
   }

   protected double motionX() {
      return this.getDeltaMovement().x;
   }

   protected double motionY() {
      return this.getDeltaMovement().y;
   }

   protected double motionZ() {
      return this.getDeltaMovement().z;
   }

   protected void setMotion(double x, double y, double z) {
      this.setDeltaMovement(x, y, z);
   }

   protected void setMotionY(double y) {
      Vec3 m = this.getDeltaMovement();
      this.setDeltaMovement(m.x, y, m.z);
   }

   protected boolean mobGriefing() {
      return this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
   }

   /** 1.12 onLivingUpdate. */
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

         if (this.isBeyondHeight()) {
            this.setMotionY(this.motionY() - 0.1F);
         }

         if (this.isInLove()) {
            this.level().broadcastEntityEvent(this, (byte)18);
         }

         if ((int)this.xo == (int)this.getX() && (int)this.zo == (int)this.getZ()) {
            this.ticksStill++;
         } else {
            this.ticksStill = 0;
         }

         if (this.getDragonStage() >= 3 && this.isStuck() && this.mobGriefing() && IceAndFire.CONFIG.dragonGriefing != 2) {
            if (this.getAnimation() == NO_ANIMATION && this.tickCount % 5 == 0) {
               this.setAnimation(ANIMATION_TAILWHACK);
            }

            if (this.getAnimation() == ANIMATION_TAILWHACK && this.getAnimationTick() == 10) {
               BlockBreakExplosion explosion = new BlockBreakExplosion(this.level(), this, this.getX(), this.getY(), this.getZ(), (float)(4 * this.getDragonStage() - 2));
               explosion.doExplosionA();
               explosion.doExplosionB(true);
               this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.0F, 1.0F);
            }
         }
      }

      if (this.isTackling() && !this.isFlying() && this.onGround()) {
         this.tacklingTicks++;
         if (this.tacklingTicks == 40) {
            this.tacklingTicks = 0;
            this.setTackling(false);
            this.setFlying(false);
         }
      }

      if (this.walkCycle < 39) {
         this.walkCycle++;
      } else {
         this.walkCycle = 0;
      }

      if (!this.level().isClientSide && this.getRNG().nextInt(500) == 0 && !this.isModelDead() && !this.isDragonSleeping()) {
         this.roar();
      }

      if (!this.level().isClientSide
         && this.onGround()
         && this.getNavigation().isDone()
         && this.getTarget() != null
         && this.getTarget().getY() - 3.0 > this.getY()
         && this.getRNG().nextInt(15) == 0
         && this.canMove()
         && !this.isHovering()
         && !this.isFlying()
         && !this.isChild()) {
         this.setHovering(true);
         this.setSleeping(false);
         this.setSitting(false);
         this.flyHovering = 0;
         this.hoverTicks = 0;
         this.flyTicks = 0;
      }

      if (this.getAnimation() == ANIMATION_WINGBLAST && (this.getAnimationTick() == 17 || this.getAnimationTick() == 22 || this.getAnimationTick() == 28)) {
         this.spawnGroundEffects();
         if (this.getTarget() != null) {
            boolean flag = this.getTarget()
               .hurt(this.damageSources().mobAttack(this), (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE) / 4));
            this.getTarget().knockback((double)((float)this.getDragonStage() * 0.6F), 1.0, 1.0);
            this.attackDecision = this.getRNG().nextBoolean();
         }
      }

      if (!this.level().isClientSide
         && this.isFlying()
         && this.getTarget() != null
         && this.attackDecision
         && this.isDirectPathBetweenPoints(this.position(), this.getTarget().position())) {
         this.setTackling(true);
      }

      if (!this.level().isClientSide
         && this.isFlying()
         && this.getTarget() != null
         && this.isTackling()
         && this.getBoundingBox().expandTowards(2.0, 2.0, 2.0).intersects(this.getTarget().getBoundingBox())) {
         this.attackDecision = true;
         this.getTarget().hurt(this.damageSources().mobAttack(this), (float)(this.getDragonStage() * 3));
         this.spawnGroundEffects();
         this.setFlying(false);
         this.setHovering(false);
      }

      if (!this.level().isClientSide && this.isTackling() && this.getTarget() == null) {
         this.setTackling(false);
      }

      if (DragonCompat.isStone(this)) {
         this.setFlying(false);
         this.setHovering(false);
      } else {
         if (this.isFlying() && this.tickCount % 40 == 0 || this.isFlying() && this.isDragonSleeping()) {
            this.setFlying(false);
            this.setFlying(true);
            this.setSleeping(false);
         }

         if (!this.canMove() && this.getTarget() != null) {
            this.setTarget(null);
         }

         if (!this.canMove()) {
            this.getNavigation().stop();
         }

         if (this.getControllingPassenger() != null) {
            if (this.motionY() > 0.5) {
               this.setMotionY(0.5);
            }

            if (this.motionY() < -0.5) {
               this.setMotionY(-0.5);
            }
         } else {
            if (this.motionY() > 0.5) {
               this.setMotionY(0.5);
            }

            if (this.motionY() < -0.5) {
               this.setMotionY(-0.5);
            }

            if (this.motionY() > 1.0) {
               this.setMotionY(0.0);
            }
         }

         this.updateCheckPlayer();
         AnimationHandler.INSTANCE.updateAnimations(this);
         if ((this.isFlying() || this.isHovering()) && !this.isModelDead()) {
            if (this.flightCycle < 58) {
               this.flightCycle += 2;
            } else {
               this.flightCycle = 0;
            }

            if (this.flightCycle == 2) {
               this.playSound(DragonCompat.snd(ModSounds.DRAGON_FLIGHT), this.getSoundVolume() * (float)IceAndFire.CONFIG.dragonFlapNoiseDistance, this.getVoicePitch());
            }

            if (this.flightCycle > 10 && this.flightCycle < 12) {
               this.spawnGroundEffects();
            }

            if (this.isModelDead() && this.flightCycle != 0) {
               this.flightCycle = 0;
            }
         }

         if (this.isModelDead() && (this.isFlying() || this.isHovering())) {
            this.setFlying(false);
            this.setHovering(false);
         }

         boolean sitting = this.isSitting() && !this.isModelDead() && !this.isDragonSleeping() && !this.isHovering() && !this.isFlying();
         if (sitting && this.sitProgress < 20.0F) {
            this.sitProgress += 0.5F;
         } else if (!sitting && this.sitProgress > 0.0F) {
            this.sitProgress -= 0.5F;
         }

         boolean sleeping = this.isDragonSleeping() && !this.isHovering() && !this.isFlying();
         if (sleeping && this.sleepProgress < 20.0F) {
            this.sleepProgress += 0.5F;
         } else if (!sleeping && this.sleepProgress > 0.0F) {
            this.sleepProgress -= 0.5F;
         }

         boolean fireBreathing = this.isBreathingFire();
         if (fireBreathing && this.fireBreathProgress < 5.0F) {
            this.fireBreathProgress += 0.5F;
         } else if (!fireBreathing && this.fireBreathProgress > 0.0F) {
            this.fireBreathProgress -= 0.5F;
         }

         boolean hovering = this.isHovering();
         if (hovering && this.hoverProgress < 20.0F) {
            this.hoverProgress += 0.5F;
         } else if (!hovering && this.hoverProgress > 0.0F) {
            this.hoverProgress -= 0.5F;
         }

         boolean tackling = this.isTackling();
         if (tackling && this.tackleProgress < 5.0F) {
            this.tackleProgress += 0.5F;
         } else if (!tackling && this.tackleProgress > 0.0F) {
            this.tackleProgress--;
         }

         boolean flying = !tackling && this.isFlying() || !this.onGround() && !this.isHovering() && this.airTarget != null;
         if (flying && this.flyProgress < 20.0F) {
            this.flyProgress += 0.5F;
         } else if (!flying && this.flyProgress > 0.0F) {
            this.flyProgress -= 0.5F;
         }

         boolean modeldead = this.isModelDead();
         if (modeldead && this.modelDeadProgress < 20.0F) {
            this.modelDeadProgress += 0.5F;
         } else if (!modeldead && this.modelDeadProgress > 0.0F) {
            this.modelDeadProgress -= 0.5F;
         }

         boolean riding = this.isPassenger() && this.getVehicle() != null && this.getVehicle() instanceof Player;
         if (riding && this.ridingProgress < 20.0F) {
            this.ridingProgress += 0.5F;
         } else if (!riding && this.ridingProgress > 0.0F) {
            this.ridingProgress -= 0.5F;
         }

         if (!this.isModelDead()) {
            if (!this.level().isClientSide && this.onGround() && this.doesWantToLand() && (this.isFlying() || this.isHovering())) {
               this.setFlying(false);
               this.setHovering(false);
            }

            if (this.getControllingPassenger() != null && !this.onGround() && (this.isFlying() || this.isHovering())) {
               this.setMotionY(this.motionY() * 0.0);
            }

            if (this.isHovering()) {
               if (this.isDragonSleeping()) {
                  this.setHovering(false);
               }

               this.hoverTicks++;
               if (this.doesWantToLand() && !this.onGround()) {
                  this.setMotionY(this.motionY() - 0.25);
               } else {
                  if (this.getControllingPassenger() == null && !this.isBeyondHeight()) {
                     this.setMotionY(this.motionY() + 0.08);
                  }

                  if (this.hoverTicks > 40) {
                     if (!this.isChild()) {
                        this.setFlying(true);
                     }

                     this.setHovering(false);
                     this.flyHovering = 0;
                     this.hoverTicks = 0;
                     this.flyTicks = 0;
                  }
               }
            }

            if (this.isDragonSleeping()) {
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

            if ((this.isHovering() || this.isFlying()) && this.isDragonSleeping()) {
               this.setFlying(false);
               this.setHovering(false);
            }

            if (!DragonCompat.isStone(this)
               && (
                  !this.level().isClientSide
                        && this.getRNG().nextInt(FLIGHT_CHANCE_PER_TICK) == 0
                        && !this.isSitting()
                        && !this.isFlying()
                        && this.getPassengers().isEmpty()
                        && !this.isChild()
                        && !this.isHovering()
                        && !this.isDragonSleeping()
                        && this.canMove()
                        && this.onGround()
                     || this.getY() < -1.0
               )) {
               this.setHovering(true);
               this.setSleeping(false);
               this.setSitting(false);
               this.flyHovering = 0;
               this.hoverTicks = 0;
               this.flyTicks = 0;
            }

            if (this.getTarget() != null
               && this.getTarget().getY() + 5.0 < this.getY()
               && !DragonCompat.isStone(this)
               && !this.level().isClientSide
               && !this.isSitting()
               && !this.isFlying()
               && this.getPassengers().isEmpty()
               && !this.isChild()
               && !this.isHovering()
               && !this.isDragonSleeping()
               && this.canMove()
               && this.onGround()) {
               this.setHovering(true);
               this.setSleeping(false);
               this.setSitting(false);
               this.flyHovering = 0;
               this.hoverTicks = 0;
               this.flyTicks = 0;
            }

            if (this.getTarget() != null && !this.getPassengers().isEmpty() && this.getOwner() != null && this.getPassengers().contains(this.getOwner())) {
               this.setTarget(null);
            }

            if (!this.isAgingDisabled()) {
               this.setAgeInTicks(this.getAgeInTicks() + 1);
               if (this.getAgeInTicks() % 24000 == 0) {
                  this.updateAttributes();
                  this.growDragon(0);
               }
            }

            if (this.getAgeInTicks() % IceAndFire.CONFIG.dragonHungerTickRate == 0 && this.getHunger() > 0) {
               this.setHunger(this.getHunger() - 1);
            }

            if (this.attackDecision
               && this.getTarget() != null
               && (double)this.distanceTo(this.getTarget()) > Math.min(this.getBoundingBox().getSize() * 5.0, 25.0)
               && !this.isChild()) {
               this.attackDecision = false;
            }

            if ((!this.attackDecision || this.getRNG().nextInt(750) == 0) && this.getDragonStage() < 2) {
               this.attackDecision = this.getRNG().nextBoolean();

               for (int i = 0; i < 5; i++) {
                  float radiusAdd = (float)i * 0.15F;
                  float headPosX = (float)(
                     this.getX() + (double)(1.8F * this.getRenderSize() * (0.3F + radiusAdd)) * Math.cos((double)(this.getYRot() + 90.0F) * Math.PI / 180.0)
                  );
                  float headPosZ = (float)(
                     this.getZ() + (double)(1.8F * this.getRenderSize() * (0.3F + radiusAdd)) * Math.sin((double)(this.getYRot() + 90.0F) * Math.PI / 180.0)
                  );
                  float headPosY = (float)(this.getY() + 0.5 * (double)this.getRenderSize() * 0.3F);
                  if (this.isFire && this.level().isClientSide) {
                     this.level().addParticle(ParticleTypes.LARGE_SMOKE, (double)headPosX, (double)headPosY, (double)headPosZ, 0.0, 0.0, 0.0);
                  } else if (this.level().isClientSide) {
                     DragonParticles.spawnParticle("dragonice", this.level(), (double)headPosX, (double)headPosY, (double)headPosZ, 0.0, 0.0, 0.0);
                  }
               }

               if (this.isFire) {
                  this.playSound(SoundEvents.FIRE_EXTINGUISH, 1.0F, 1.0F);
               } else {
                  this.playSound(SoundEvents.BOTTLE_FILL_DRAGONBREATH, 1.0F, 1.0F);
               }
            }

            if (this.isBreathingFire()) {
               this.fireTicks++;
               if (this.fireTicks > this.getDragonStage() * 25 || this.getOwner() != null && this.getPassengers().contains(this.getOwner()) && this.fireStopTicks <= 0) {
                  this.setBreathingFire(false);
                  this.attackDecision = this.getRNG().nextBoolean();
                  this.fireTicks = 0;
               }

               if (this.fireStopTicks > 0 && this.getOwner() != null && this.getPassengers().contains(this.getOwner())) {
                  this.fireStopTicks--;
               }
            }

            if (this.isFlying() && this.getTarget() != null && this.getBoundingBox().expandTowards(3.0, 3.0, 3.0).intersects(this.getTarget().getBoundingBox())) {
               this.attackEntityAsMob(this.getTarget());
            }

            this.breakBlock();
         }
      }
   }

   private boolean isBeyondHeight() {
      return this.getY() > (double)this.level().getMaxBuildHeight() ? true : this.getY() > (double)IceAndFire.CONFIG.maxDragonFlight;
   }

   public void breakBlock() {
      if (IceAndFire.CONFIG.dragonGriefing != 2 || this.isTame() && !IceAndFire.CONFIG.tamedDragonGriefing) {
         float hardness = IceAndFire.CONFIG.dragonGriefing != 1 && this.getDragonStage() > 3 ? 5.0F : 1.6F;
         if (!this.isModelDead() && this.getDragonStage() >= 3 && this.canMove()) {
            AABB bb = this.getBoundingBox();
            for (int a = (int)Math.round(bb.minX) - 1; a <= (int)Math.round(bb.maxX) + 1; a++) {
               for (int b = (int)Math.round(bb.minY) + 1; b <= (int)Math.round(bb.maxY) + 2 && b <= 127; b++) {
                  for (int c = (int)Math.round(bb.minZ) - 1; c <= (int)Math.round(bb.maxZ) + 1; c++) {
                     BlockPos pos = new BlockPos(a, b, c);
                     BlockState state = this.level().getBlockState(pos);
                     Block block = state.getBlock();
                     if (!state.isAir()
                        && !(block instanceof BushBlock)
                        && !(block instanceof LiquidBlock)
                        && block != Blocks.BEDROCK
                        && state.getDestroySpeed(this.level(), pos) < hardness
                        && DragonUtils.canDragonBreak(block)
                        && this.canDestroyBlock(pos)) {
                        Vec3 m = this.getDeltaMovement();
                        this.setDeltaMovement(m.x * 0.6, m.y, m.z * 0.6);
                        if (!this.level().isClientSide) {
                           this.level().destroyBlock(pos, true);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public void spawnGroundEffects() {
      for (int i = 0; (float)i < this.getRenderSize(); i++) {
         for (int i1 = 0; i1 < 20; i1++) {
            double motionX = this.getRNG().nextGaussian() * 0.07;
            double motionY = this.getRNG().nextGaussian() * 0.07;
            double motionZ = this.getRNG().nextGaussian() * 0.07;
            float radius = 0.75F * (0.7F * this.getRenderSize() / 3.0F) * -3.0F;
            float angle = (float) (Math.PI / 180.0) * this.yBodyRot + (float)i1 * 1.0F;
            double extraX = (double)(radius * Mth.sin((float)(Math.PI + (double)angle)));
            double extraY = 0.8F;
            double extraZ = (double)(radius * Mth.cos(angle));
            BlockState iblockstate = this.level()
               .getBlockState(new BlockPos(Mth.floor(this.getX() + extraX), Mth.floor(this.getY() + extraY) - 1, Mth.floor(this.getZ() + extraZ)));
            if (!iblockstate.isAir() && this.level().isClientSide) {
               this.level()
                  .addAlwaysVisibleParticle(
                     new BlockParticleOption(ParticleTypes.BLOCK, iblockstate), true, this.getX() + extraX, this.getY() + extraY, this.getZ() + extraZ, motionX, motionY, motionZ
                  );
            }
         }
      }
   }

   /** 1.12 fall(): no fall damage. */
   @Override
   public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
      return false;
   }

   public boolean isActuallyBreathingFire() {
      return this.fireTicks > 20 && this.isBreathingFire();
   }

   public boolean doesWantToLand() {
      return this.flyTicks > 6000 || this.down() || this.flyTicks > 40 && this.flyProgress == 0.0F || DragonCompat.isStone(this);
   }

   public abstract String getVariantName(int variant);

   /** 1.12 updatePassenger. */
   @Override
   protected void positionRider(Entity passenger, Entity.MoveFunction callback) {
      super.positionRider(passenger, callback);
      if (this.hasPassenger(passenger)) {
         if (this.getControllingPassenger() != null && this.getControllingPassenger().getId() == passenger.getId()) {
            if (this.isModelDead()) {
               passenger.stopRiding();
            }

            float speed_walk = 0.2F;
            float speed_idle = 0.05F;
            float speed_fly = 0.2F;
            float degree_walk = 0.5F;
            float degree_idle = 0.5F;
            float degree_fly = 0.5F;
            this.yBodyRot = this.getYRot();
            this.setYRot(passenger.getYRot());
            float hoverAddition = this.hoverProgress * -0.001F;
            float flyAddition = this.flyProgress * -1.0E-4F;
            float flyBody = Math.max(this.flyProgress, this.hoverProgress) * 0.0065F;
            float radius = 0.75F * (0.3F - flyBody) * this.getRenderSize() + this.getRenderSize() / 3.0F * flyAddition * 0.0065F;
            float angle = (float) (Math.PI / 180.0) * this.yBodyRot;
            double extraX = (double)(radius * Mth.sin((float)(Math.PI + (double)angle)));
            double extraZ = (double)(radius * Mth.cos(angle));
            float bob0 = !this.isFlying() && !this.isHovering()
               ? 0.0F
               : (!(this.hoverProgress > 0.0F) && !(this.flyProgress > 0.0F) ? 0.0F : this.bob(-speed_fly, degree_fly * 5.0F, false, (float)this.tickCount, -0.0625F));
            float bob1 = this.bob(speed_walk * 2.0F, degree_walk * 1.7F, false, this.walkAnimation.position(), this.walkAnimation.speed() * -0.0625F);
            float bob2 = this.bob(speed_idle, degree_idle * 1.3F, false, (float)this.tickCount, -0.0625F);
            double extraY_pre = 0.8F;
            double extraY = (extraY_pre - (double)hoverAddition + (double)flyAddition) * (double)(this.getRenderSize() / 3.0F)
               - 0.35 * (double)(1.0F - this.getRenderSize() / 30.0F)
               + (double)bob0
               + (double)bob1
               + (double)bob2;
            passenger.setPos(this.getX() + extraX, this.getY() + extraY, this.getZ() + extraZ);
         } else {
            this.updatePreyInMouth(passenger);
         }
      }
   }

   private float bob(float speed, float degree, boolean bounce, float f, float f1) {
      float bob = (float)(Math.sin((double)(f * speed)) * (double)f1 * (double)degree - (double)(f1 * degree));
      if (bounce) {
         bob = (float)(-Math.abs(Math.sin((double)(f * speed)) * (double)f1 * (double)degree));
      }

      return bob * this.getRenderSize() / 3.0F;
   }

   private void updatePreyInMouth(Entity prey) {
      this.setAnimation(ANIMATION_SHAKEPREY);
      if (this.getAnimation() == ANIMATION_SHAKEPREY && this.getAnimationTick() > 55 && prey != null) {
         prey.hurt(
            this.damageSources().mobAttack(this),
            prey instanceof Player ? 17.0F : (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 4.0F
         );
         prey.stopRiding();
      }

      this.yBodyRot = this.getYRot();
      float modTick_0 = (float)(this.getAnimationTick() - 25);
      float modTick_1 = this.getAnimationTick() > 25 && this.getAnimationTick() < 55
         ? 8.0F * Mth.clamp(Mth.sin((float)(Math.PI + (double)modTick_0 * 0.25)), -0.8F, 0.8F)
         : 0.0F;
      float modTick_2 = this.getAnimationTick() > 30 ? 10.0F : (float)Math.max(0, this.getAnimationTick() - 20);
      float radius = 0.75F * (0.6F * this.getRenderSize() / 3.0F) * -3.0F;
      float angle = (float) (Math.PI / 180.0) * this.yBodyRot + 3.15F + modTick_1 * 2.0F * 0.015F;
      double extraX = (double)(radius * Mth.sin((float)(Math.PI + (double)angle)));
      double extraZ = (double)(radius * Mth.cos(angle));
      double extraY = modTick_2 == 0.0F ? 0.0 : 0.035F * ((double)(this.getRenderSize() / 3.0F) + (double)modTick_2 * 0.5 * (double)(this.getRenderSize() / 3.0F));
      if (prey != null) {
         prey.setPos(this.getX() + extraX, this.getY() + extraY, this.getZ() + extraZ);
      }
   }

   public int getDragonStage() {
      int age = this.getAgeInDays();
      if (age >= 100) {
         return 5;
      } else if (age >= 75) {
         return 4;
      } else if (age >= 50) {
         return 3;
      } else {
         return age >= 25 ? 2 : 1;
      }
   }

   public boolean isTeen() {
      return this.getDragonStage() < 4 && this.getDragonStage() > 2;
   }

   public boolean isAdult() {
      return this.getDragonStage() >= 4;
   }

   /** 1.12 isChild. */
   public boolean isChild() {
      return this.getDragonStage() < 2;
   }

   @Override
   public boolean isBaby() {
      return this.isChild();
   }

   /** 1.12 onInitialSpawn (spawn eggs, /summon, natural spawns). */
   @Nullable
   @Override
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData livingdata) {
      // 1.12 EntityAgeable had no "spawn as baby" group logic
      livingdata = super.finalizeSpawn(level, difficulty, spawnType, livingdata == null ? new AgeableMob.AgeableMobGroupData(false) : livingdata);
      this.setGender(this.getRNG().nextBoolean());
      int age = this.getRNG().nextInt(80) + 1;
      this.growDragon(age);
      this.setVariant(new Random().nextInt(4));
      this.setSleeping(false);
      this.updateAttributes();
      double healthStep = (this.maximumHealth - this.minimumHealth) / 125.0;
      this.heal((float)Math.round(this.minimumHealth + healthStep * (double)age));
      this.attackDecision = true;
      this.setHunger(50);
      return livingdata;
   }

   /** 1.12 attackEntityFrom. */
   @Override
   public boolean hurt(DamageSource dmg, float i) {
      if (this.isModelDead()) {
         return false;
      } else if (this.isVehicle() && dmg.getEntity() != null && this.getControllingPassenger() != null && dmg.getEntity() == this.getControllingPassenger()) {
         return false;
      } else if ((dmg.getMsgId().contains("arrow") || this.getVehicle() != null && dmg.getEntity() != null && dmg.getEntity().is(this.getVehicle())) && this.isPassenger()) {
         return false;
      } else if (!dmg.is(DamageTypes.IN_WALL) && !dmg.is(DamageTypes.FALLING_BLOCK)) {
         if (!this.level().isClientSide && dmg.getEntity() != null && this.getRNG().nextInt(4) == 0) {
            this.roar();
         }

         return super.hurt(dmg, i);
      } else {
         return false;
      }
   }

   /** 1.12 onUpdate. */
   @Override
   public void tick() {
      super.tick();
      this.updateParts();
      this.setScaleForAge(true);
      if (this.level().isClientSide) {
         this.updateClientControls();
      }

      if (!this.isModelDead()) {
         if (this.up()) {
            if (!this.isFlying() && !this.isHovering()) {
               this.spacebarTicks += 2;
            }

            if (this.isFlying() || this.isHovering()) {
               this.setMotionY(this.motionY() + 0.4);
            }
         } else if (this.dismount() && (this.isFlying() || this.isHovering())) {
            this.setMotionY(this.motionY() - 0.4);
            this.setFlying(false);
            this.setHovering(false);
         }

         if (this.down() && (this.isFlying() || this.isHovering())) {
            this.setMotionY(this.motionY() - 0.4);
         }

         if (!this.dismount() && (this.isFlying() || this.isHovering())) {
            this.setMotionY(this.motionY() + 0.01);
         }

         if (this.attack() && this.getControllingPassenger() != null && this.getDragonStage() > 1) {
            this.setBreathingFire(true);
            this.riderShootFire(this.getControllingPassenger());
            this.fireStopTicks = 10;
         }

         if (this.strike() && this.getControllingPassenger() != null && this.getControllingPassenger() instanceof Player) {
            LivingEntity target = DragonUtils.riderLookingAtEntity(
               this, this.getControllingPassenger(), (double)this.getDragonStage() + (this.getBoundingBox().maxX - this.getBoundingBox().minX)
            );
            if (this.getAnimation() != ANIMATION_BITE) {
               this.setAnimation(ANIMATION_BITE);
            }

            if (target != null && !DragonUtils.hasSameOwner(this, target)) {
               target.hurt(this.damageSources().mobAttack(this), (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE)));
            }
         }

         if (this.getControllingPassenger() != null && this.getControllingPassenger().isShiftKeyDown()) {
            if (this.getControllingPassenger() instanceof Player player) {
               DragonCompat.setDismountedDragon(player, true);
            }

            this.getControllingPassenger().stopRiding();
         }

         double horizontal = Math.max(Math.abs(this.motionZ()), Math.abs(this.motionX()));
         if (this.isFlying() && !this.isHovering() && this.getControllingPassenger() != null && !this.onGround() && horizontal < 0.1F) {
            this.setHovering(true);
            this.setFlying(false);
         }

         if (this.isHovering() && !this.isFlying() && this.getControllingPassenger() != null && !this.onGround() && horizontal > 0.1F) {
            this.setFlying(true);
            this.setHovering(false);
         }

         if (this.spacebarTicks > 0) {
            this.spacebarTicks--;
         }

         if (this.spacebarTicks > 20 && this.getOwner() != null && this.getPassengers().contains(this.getOwner()) && !this.isFlying() && !this.isHovering()) {
            this.setHovering(true);
         }

         if (this.level().isClientSide && !this.isModelDead()) {
            clientBuffers.accept(this);
         }

         if (this.getTarget() != null && this.getVehicle() == null && this.getTarget().isRemoved()
            || this.getTarget() != null && this.getTarget() instanceof EntityDragonBase && this.getTarget().isRemoved()) {
            this.setTarget(null);
         }

         if (!this.level().isClientSide
            && !this.isInWater()
            && !this.isDragonSleeping()
            && this.onGround()
            && !this.isFlying()
            && !this.isHovering()
            && this.getTarget() == null
            && !this.isDaytime()
            && this.getRNG().nextInt(250) == 0
            && this.getTarget() == null
            && this.getPassengers().isEmpty()) {
            this.setSleeping(true);
         }

         if (!this.level().isClientSide
            && this.isDragonSleeping()
            && (
               this.isFlying()
                  || this.isHovering()
                  || this.isInWater()
                  || this.level().canSeeSky(this.blockPosition()) && this.isDaytime() && !this.isTame()
                  || this.isDaytime() && this.isTame()
                  || this.getTarget() != null
                  || !this.getPassengers().isEmpty()
            )) {
            this.setSleeping(false);
         }

         if (this.isSitting() && this.getControllingPassenger() != null) {
            this.setSitting(false);
         }
      }
   }

   /** 1.12 setScaleForAge / EntityAgeable.setScale: the hitbox is 0.78 x 1.2 times min(renderSize * 0.35, 7). */
   public void setScaleForAge(boolean par1) {
      float scale = Math.min(this.getRenderSize() * 0.35F, 7.0F);
      if (scale != this.dragonScale) {
         this.dragonScale = scale;
         this.refreshDimensions();
      }
      if (scale != this.lastScale) {
         this.resetParts(this.getRenderSize() / 3.0F);
      }

      this.lastScale = scale;
   }

   @Override
   protected EntityDimensions getDefaultDimensions(Pose pose) {
      return EntityDimensions.scalable(0.78F, 1.2F).scale(this.dragonScale <= 0.0F ? 1.0F : this.dragonScale);
   }

   @Override
   public float getAgeScale() {
      return 1.0F;
   }

   @Override
   public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
      super.onSyncedDataUpdated(key);
      if (AGE_TICKS.equals(key)) {
         this.setScaleForAge(true);
      }
   }

   /** 1.12 updateFallState: nothing (no fall distance builds up). */
   @Override
   protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
   }

   public float getRenderSize() {
      float[][] stages = this.growth_stages != null ? this.growth_stages : DEFAULT_STAGES;
      float step = (stages[this.getDragonStage() - 1][1] - stages[this.getDragonStage() - 1][0]) / 25.0F;
      return this.getAgeInDays() > 125
         ? stages[this.getDragonStage() - 1][0] + step * 25.0F
         : stages[this.getDragonStage() - 1][0] + step * (float)this.getAgeFactor();
   }

   private int getAgeFactor() {
      return this.getDragonStage() > 1 ? this.getAgeInDays() - 25 * (this.getDragonStage() - 1) : this.getAgeInDays();
   }

   /** 1.12 attackEntityAsMob. */
   public boolean attackEntityAsMob(Entity entityIn) {
      if (this.isTackling()) {
         return false;
      } else if (this.isModelDead()) {
         return false;
      } else {
         return this.hurtWithAttackDamage(entityIn);
      }
   }

   /** entityIn.attackEntityFrom(causeMobDamage(this), (int) attack damage), then 1.12 applyEnchantments on a hit. */
   protected boolean hurtWithAttackDamage(Entity entityIn) {
      DamageSource source = this.damageSources().mobAttack(this);
      boolean flag = entityIn.hurt(source, (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE)));
      if (flag && this.level() instanceof ServerLevel serverLevel) {
         EnchantmentHelper.doPostAttackEffects(serverLevel, entityIn, source);
      }

      return flag;
   }

   /** entityIn.attackEntityFrom(causeMobDamage(this), (int) attack damage) without applyEnchantments (the 1.12 bite/tail). */
   protected boolean hurtWithAttackDamageNoEnchant(Entity entityIn) {
      return entityIn.hurt(this.damageSources().mobAttack(this), (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE)));
   }

   @Override
   public boolean doHurtTarget(Entity target) {
      return this.attackEntityAsMob(target);
   }

   /** 1.12 updateRidden (the dragon riding a player's shoulder). */
   @Override
   public void rideTick() {
      Entity entity = this.getVehicle();
      if (this.isPassenger() && entity.isRemoved()) {
         this.stopRiding();
      } else {
         this.setDeltaMovement(Vec3.ZERO);
         this.tick();
         if (this.isPassenger()) {
            this.updateRiding(entity);
         }
      }
   }

   public void updateRiding(Entity riding) {
      if (riding != null && riding.hasPassenger(this) && riding instanceof Player player) {
         int i = riding.getPassengers().indexOf(this);
         float radius = (i == 2 ? 0.0F : 0.4F) + (float)(player.isFallFlying() ? 2 : 0);
         float angle = (float) (Math.PI / 180.0) * player.yBodyRot + (float)(i == 1 ? -90 : (i == 0 ? 90 : 0));
         double extraX = (double)(radius * Mth.sin((float)(Math.PI + (double)angle)));
         double extraZ = (double)(radius * Mth.cos(angle));
         double extraY = (riding.isShiftKeyDown() ? 1.2 : 1.4) + (i == 2 ? 0.4 : 0.0);
         this.setYRot(player.yHeadRot);
         this.yHeadRot = player.yHeadRot;
         this.yRotO = player.yHeadRot;
         this.setPos(riding.getX() + extraX, riding.getY() + extraY, riding.getZ() + extraZ);
         if (this.getControlState() == 16 || player.isFallFlying()) {
            this.stopRiding();
         }
      }
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
      return this.isModelDead() ? NO_ANIMATION : this.currentAnimation;
   }

   @Override
   public void setAnimation(Animation animation) {
      if (!this.isModelDead()) {
         this.currentAnimation = animation;
      }
   }

   /** 1.12 playLivingSound. */
   @Override
   public void playAmbientSound() {
      if (!this.isDragonSleeping() && !this.isModelDead() && !this.level().isClientSide) {
         if (this.getAnimation() == NO_ANIMATION) {
            this.setAnimation(ANIMATION_SPEAK);
         }

         super.playAmbientSound();
      }
   }

   @Override
   protected void playHurtSound(DamageSource source) {
      if (!this.isModelDead()) {
         if (this.getAnimation() == NO_ANIMATION && !this.level().isClientSide) {
            this.setAnimation(ANIMATION_SPEAK);
         }

         super.playHurtSound(source);
      }
   }

   @Override
   public Animation[] getAnimations() {
      return new Animation[]{IAnimatedEntity.NO_ANIMATION, ANIMATION_EAT};
   }

   @Nullable
   @Override
   public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob ageable) {
      return null;
   }

   /** 1.12 canMateWith. */
   @Override
   public boolean canMate(Animal otherAnimal) {
      if (otherAnimal instanceof EntityDragonBase && otherAnimal != this && otherAnimal.getClass() == this.getClass()) {
         EntityDragonBase dragon = (EntityDragonBase)otherAnimal;
         if (this.isMale() && !dragon.isMale() || !this.isMale() && dragon.isMale()) {
            return true;
         }
      }

      return false;
   }

   public EntityDragonEgg createEgg(EntityDragonBase ageable) {
      int i = Mth.floor(this.getX());
      int j = Mth.floor(this.getY());
      int k = Mth.floor(this.getZ());
      BlockPos pos = new BlockPos(i, j, k);
      EntityDragonEgg dragon = new EntityDragonEgg(IafDragonEntities.DRAGON_EGG.get(), this.level());
      dragon.setEggType(EnumDragonEgg.byMetadata(new Random().nextInt(3) + (this.isFire ? 0 : 4)));
      dragon.setPos((double)pos.getX() + 0.5, (double)(pos.getY() + 1), (double)pos.getZ() + 0.5);
      return dragon;
   }

   public void flyAround() {
      if (this.airTarget != null) {
         if (!this.isTargetInAir() || this.flyTicks > 6000 || !this.isFlying()) {
            this.airTarget = null;
         }

         this.flyTowardsTarget();
      }
   }

   /** 1.12 isTargetBlocked (World.rayTraceBlocks from the dragon's block position, no liquids, selection boxes). */
   public boolean isTargetBlocked(Vec3 target) {
      if (target != null) {
         BlockHitResult rayTrace = this.level()
            .clip(new ClipContext(Vec3.atLowerCornerOf(this.blockPosition()), target, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this));
         if (rayTrace.getType() != HitResult.Type.MISS) {
            BlockPos pos = BlockPos.containing(rayTrace.getLocation());
            if (this.level().isEmptyBlock(pos) && (DragonCompat.material(this.level(), pos) != DragonCompat.Material.WATER || this.isFire)) {
               return rayTrace.getType() != HitResult.Type.BLOCK;
            }

            return true;
         }
      }

      return false;
   }

   public void flyTowardsTarget() {
      if (this.airTarget != null && this.airTarget.getY() > IceAndFire.CONFIG.maxDragonFlight) {
         this.airTarget = new BlockPos(this.airTarget.getX(), IceAndFire.CONFIG.maxDragonFlight, this.airTarget.getZ());
      }

      if (this.airTarget != null
         && this.isTargetInAir()
         && this.isFlying()
         && this.getDistanceSquared(new Vec3((double)this.airTarget.getX(), this.getY(), (double)this.airTarget.getZ())) > 3.0F) {
         double y = this.attackDecision ? (double)this.airTarget.getY() : this.getY();
         double targetX = (double)this.airTarget.getX() + 0.5 - this.getX();
         double targetY = Math.min(y, 256.0) + 1.0 - this.getY();
         double targetZ = (double)this.airTarget.getZ() + 0.5 - this.getZ();
         double mx = this.motionX() + (Math.signum(targetX) * 0.5 - this.motionX()) * 0.100000000372529 * this.getFlySpeed();
         double my = this.motionY() + (Math.signum(targetY) * 0.5 - this.motionY()) * 0.100000000372529 * this.getFlySpeed();
         double mz = this.motionZ() + (Math.signum(targetZ) * 0.5 - this.motionZ()) * 0.100000000372529 * this.getFlySpeed();
         this.setMotion(mx, my, mz);
         float angle = (float)(Math.atan2(this.motionZ(), this.motionX()) * 180.0 / Math.PI) - 90.0F;
         this.zza = 0.5F;
         double d0 = (double)this.airTarget.getX() + 0.5 - this.getX();
         double d2 = (double)this.airTarget.getZ() + 0.5 - this.getZ();
         double d1 = y + 0.5 - this.getY();
         double d3 = (double)Mth.sqrt((float)(d0 * d0 + d2 * d2));
         float f = (float)(Mth.atan2(d2, d0) * (180.0 / Math.PI)) - 90.0F;
         float f1 = (float)(-(Mth.atan2(d1, d3) * (180.0 / Math.PI)));
         this.setXRot(this.updateRotation(this.getXRot(), f1, 30.0F));
         this.setYRot(this.updateRotation(this.getYRot(), f, 30.0F));
         if (!this.isFlying()) {
            this.setFlying(true);
         }
      } else {
         this.airTarget = null;
      }

      if (this.airTarget != null
         && this.isTargetInAir()
         && this.isFlying()
         && this.getDistanceSquared(new Vec3((double)this.airTarget.getX(), this.getY(), (double)this.airTarget.getZ())) < 3.0F
         && this.doesWantToLand()) {
         this.setFlying(false);
         this.setHovering(true);
         this.flyHovering = 1;
      }
   }

   private double getFlySpeed() {
      return (double)((2 + this.getAgeInDays() / 125 * 2) * (this.isTackling() ? 2 : 1));
   }

   public boolean isTackling() {
      if (this.level().isClientSide) {
         boolean tackling = this.entityData.get(TACKLE);
         this.isTackling = tackling;
         return tackling;
      } else {
         return this.isTackling;
      }
   }

   public boolean isAgingDisabled() {
      return this.entityData.get(AGINGDISABLED);
   }

   protected boolean isTargetInAir() {
      return this.airTarget != null
         && (
            this.level().getBlockState(this.airTarget).isAir()
               || this instanceof EntityIceDragon
                  && (DragonCompat.material(this.level(), this.airTarget) == DragonCompat.Material.WATER || this.level().getBlockState(this.airTarget).isAir())
         );
   }

   private float updateRotation(float angle, float targetAngle, float maxIncrease) {
      float f = Mth.wrapDegrees(targetAngle - angle);
      if (f > maxIncrease) {
         f = maxIncrease;
      }

      if (f < -maxIncrease) {
         f = -maxIncrease;
      }

      return angle + f;
   }

   public float getDistanceSquared(Vec3 vec3d) {
      float f = (float)(this.getX() - vec3d.x);
      float f1 = (float)(this.getY() - vec3d.y);
      float f2 = (float)(this.getZ() - vec3d.z);
      return f * f + f1 * f1 + f2 * f2;
   }

   public abstract ItemLike getVariantScale(int variant);

   public abstract ItemLike getVariantEgg(int variant);

   /** 1.12 updateClientControls (keybinds of the rider); the client part sets {@link #clientControls}. */
   protected void updateClientControls() {
      clientControls.accept(this);
   }

   /** 1.12 travel(strafe, vertical, forward). */
   @Override
   public void travel(Vec3 input) {
      float strafe = (float)input.x;
      float vertical = (float)input.y;
      float forward = (float)input.z;
      if (this.getAnimation() != ANIMATION_SHAKEPREY && (this.canMove() || this.isVehicle())) {
         if (this.isVehicle()) {
            LivingEntity controller = this.getControllingPassenger();
            if (controller != null && controller != this.getTarget()) {
               this.setYRot(controller.getYRot());
               this.yRotO = this.getYRot();
               this.setXRot(controller.getXRot() * 0.5F);
               this.setRot(this.getYRot(), this.getXRot());
               this.yBodyRot = this.getYRot();
               this.yHeadRot = this.yBodyRot;
               strafe = controller.xxa * 0.5F;
               forward = controller.zza;
               if (forward <= 0.0F) {
                  forward *= 0.25F;
               }

               if (this.isFlying() || this.isHovering()) {
                  Vec3 m = this.getDeltaMovement();
                  this.setDeltaMovement(m.x * 1.06, m.y, m.z * 1.06);
               }

               this.riddenJumpFactor = true;
               this.setSpeed(this.onGround() ? (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) : (float)this.getFlySpeed());
               super.travel(new Vec3((double)strafe, 0.0, (double)forward));
               this.riddenJumpFactor = false;
               return;
            }
         }

         super.travel(new Vec3((double)strafe, (double)vertical, (double)forward));
      } else {
         super.travel(Vec3.ZERO);
      }
   }

   /** 1.12 jumpMovementFactor: 0.05 while steered by the rider, 0.02 otherwise. */
   private boolean riddenJumpFactor;

   @Override
   protected float getFlyingSpeed() {
      return this.riddenJumpFactor ? 0.05F : 0.02F;
   }

   public void updateCheckPlayer() {
      double checklength = this.getBoundingBox().getSize() * 3.0;
      Player player = this.level().getNearestPlayer(this, checklength);
      if (!this.isTame() && this.isDragonSleeping() && player != null && !this.isOwner(player) && !player.isCreative()) {
         this.setSleeping(false);
         this.setSitting(false);
         this.setTarget(player);
      }
   }

   @Override
   public boolean dismountsUnderwater() {
      return false;
   }

   public boolean isDirectPathBetweenPoints(Vec3 vec1, Vec3 vec2) {
      BlockHitResult hit = this.level()
         .clip(new ClipContext(vec1, new Vec3(vec2.x, vec2.y + (double)this.getBbHeight() * 0.5, vec2.z), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
      return hit.getType() != HitResult.Type.BLOCK;
   }

   /** 1.12 onDeath: the dragon's own inventory (armor) drops. */
   @Override
   public void die(DamageSource cause) {
      super.die(cause);
      if (this.dragonInv != null && !this.level().isClientSide) {
         for (int i = 0; i < this.dragonInv.getContainerSize(); i++) {
            ItemStack itemstack = this.dragonInv.getItem(i);
            if (!itemstack.isEmpty()) {
               this.spawnAtLocation(itemstack, 0.0F);
            }
         }
      }
   }

   @Override
   public void onHearFlute(Player player) {
      if (this.isTame() && this.isOwner(player) && (this.isFlying() || this.isHovering())) {
         this.setFlying(false);
         this.setHovering(false);
      }
   }

   public abstract SoundEvent getRoarSound();

   public void roar() {
      if (!DragonCompat.isStone(this)) {
         if (this.getAnimation() != ANIMATION_ROAR) {
            this.setAnimation(ANIMATION_ROAR);
            this.playSound(this.getRoarSound(), this.getSoundVolume() + 2.0F + (float)Math.max(0, this.getDragonStage() - 3), this.getVoicePitch());
         }

         if (this.getDragonStage() > 3) {
            int size = (this.getDragonStage() - 3) * 30;

            for (Entity entity : this.level().getEntities(this, this.getBoundingBox().expandTowards((double)size, (double)size, (double)size))) {
               boolean isStrongerDragon = entity instanceof EntityDragonBase && ((EntityDragonBase)entity).getDragonStage() >= this.getDragonStage();
               if (entity instanceof LivingEntity && !isStrongerDragon) {
                  LivingEntity living = (LivingEntity)entity;
                  if (!this.isOwner(living) && !this.isOwnersPet(living)) {
                     living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 30 * size));
                  } else {
                     living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 30 * size));
                  }
               }
            }
         }
      }
   }

   private boolean isOwnersPet(LivingEntity living) {
      return this.isTame()
         && this.getOwner() != null
         && living instanceof TamableAnimal
         && ((TamableAnimal)living).getOwner() != null
         && this.getOwner().is(((TamableAnimal)living).getOwner());
   }

   public boolean isDirectPathBetweenPoints(Entity entity, Vec3 vec1, Vec3 vec2) {
      BlockHitResult hit = entity.level()
         .clip(new ClipContext(vec1, new Vec3(vec2.x, vec2.y + (double)entity.getBbHeight() * 0.5, vec2.z), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
      return hit.getType() != HitResult.Type.BLOCK;
   }

   public boolean shouldRenderEyes() {
      return !this.isDragonSleeping() && !this.isModelDead() && !this.isBlinking();
   }

   @Override
   public boolean shouldAnimalsFear(Entity entity) {
      return DragonUtils.canTameDragonAttack(this, entity);
   }

   @Override
   public void dropArmor() {
      if (this.dragonInv != null && !this.level().isClientSide) {
         for (int i = 0; i < this.dragonInv.getContainerSize(); i++) {
            ItemStack itemstack = this.dragonInv.getItem(i);
            if (!itemstack.isEmpty()) {
               this.spawnAtLocation(itemstack, 0.0F);
            }
         }
      }
   }

   /** 1.12 dragons were never pushed by players' collisions any differently; keep vanilla pushing. */
   @Override
   public boolean isPushable() {
      return super.isPushable();
   }
}
