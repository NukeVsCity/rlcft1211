package com.lycanitesmobs.core.entity;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.entity.ambient.*;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.lycanitesmobs.compat.*;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.*;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.*;
import net.minecraft.network.chat.*;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.*;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.item.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.*;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.*;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.common.*;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.*;
import net.neoforged.neoforge.event.tick.*;
import com.google.common.base.Predicate;
import com.lycanitesmobs.ExtendedWorld;
import com.lycanitesmobs.GuiHandler;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.api.IFusable;
import com.lycanitesmobs.api.IGroupBoss;
import com.lycanitesmobs.api.IGroupHeavy;
import com.lycanitesmobs.api.IGroupIce;
import com.lycanitesmobs.api.Targeting;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.container.CreatureContainer;
import com.lycanitesmobs.core.entity.damagesources.MinionEntityDamageSource;
import com.lycanitesmobs.core.entity.goals.actions.AvoidGoal;
import com.lycanitesmobs.core.entity.goals.actions.FollowFuseGoal;
import com.lycanitesmobs.core.entity.goals.actions.FollowMasterGoal;
import com.lycanitesmobs.core.entity.goals.actions.LookIdleGoal;
import com.lycanitesmobs.core.entity.goals.actions.MoveRestrictionGoal;
import com.lycanitesmobs.core.entity.goals.actions.PaddleGoal;
import com.lycanitesmobs.core.entity.goals.actions.StayByWaterGoal;
import com.lycanitesmobs.core.entity.goals.actions.TemptGoal;
import com.lycanitesmobs.core.entity.goals.actions.WanderGoal;
import com.lycanitesmobs.core.entity.goals.actions.WatchClosestGoal;
import com.lycanitesmobs.core.entity.goals.targeting.AvoidIfHitGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindAttackTargetGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindFuseTargetGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindGroupAttackTargetGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindGroupAvoidTargetGoal;
import com.lycanitesmobs.core.entity.goals.targeting.RevengeGoal;
import com.lycanitesmobs.core.entity.navigate.CreatureMoveHelper;
import com.lycanitesmobs.core.entity.navigate.CreaturePathNavigate;
import com.lycanitesmobs.core.entity.navigate.DirectNavigator;
import com.lycanitesmobs.core.entity.navigate.ICreatureNodeProcessor;
import com.lycanitesmobs.core.info.CreatureGroup;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureKnowledge;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.ElementInfo;
import com.lycanitesmobs.core.info.ExtraMobBehaviour;
import com.lycanitesmobs.core.info.ItemDrop;
import com.lycanitesmobs.core.info.Subspecies;
import com.lycanitesmobs.core.info.Variant;
import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;
import com.lycanitesmobs.core.inventory.InventoryCreature;
import com.lycanitesmobs.core.item.equipment.ItemEquipmentPart;
import com.lycanitesmobs.core.network.MessageCreature;
import com.lycanitesmobs.core.pets.PetEntry;
import com.lycanitesmobs.core.spawner.SpawnerEventListener;
import com.lycanitesmobs.core.tileentity.TileEntitySummoningPedestal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


public abstract class BaseCreatureEntity extends Mob {
   public static Boolean ENABLE_HITAREAS = false;
   /** 1.12 RangedAttribute "generic.defense" / "generic.rangedSpeed" (registered in LycEntities). */
   public static final Holder<Attribute> DEFENSE = com.lycanitesmobs.LycEntities.DEFENSE;
   public static final Holder<Attribute> RANGED_SPEED = com.lycanitesmobs.LycEntities.RANGED_SPEED;
   /** 1.12 EntityLiving.tasks / targetTasks (created before initEntityAI like 1.12, see createNavigation). */
   public EntityAITasks tasks;
   public EntityAITasks targetTasks;
   /** 1.12 Entity.width/height set by setSize (1.21 asks getDefaultDimensions). */
   private float legacyWidth;
   private float legacyHeight;
   private boolean legacyConstructed;
   public CreatureInfo creatureInfo;
   public CreatureStats creatureStats;
   public Subspecies subspecies = null;
   public Variant variant = null;
   public EnumCreatureAttribute attribute = EnumCreatureAttribute.UNDEAD;
   public CreatureRelationships relationships;
   public ExtraMobBehaviour extraMobBehaviour;
   public long updateTick = 0L;
   protected boolean syncQueued = true;
   public String spawnEventType = "";
   public int spawnEventCount = -1;
   public PetEntry petEntry;
   public boolean altarSummoned = false;
   public int spawnedWithBlockProtection = 0;
   public boolean forceBossHealthBar = false;
   public TileEntitySummoningPedestal summoningPedestal;
   public float setWidth;
   public float setDepth;
   public float setHeight;
   public double sizeScale = 1.0;
   public EntityHitArea[][][] hitAreas;
   public float hitAreaWidthScale = 1.0F;
   public float hitAreaHeightScale = 1.0F;
   public int attackTime = 20;
   protected int level = 1;
   protected int experience = 0;
   public byte attackPhase = 0;
   public byte attackPhaseMax = 0;
   public int fleeTime = 200;
   public int currentFleeTime = 0;
   public float fleeHealthPercent = 0.0F;
   public int battlePhase = 0;
   public int damageMax = 0;
   public float damageLimit = 0.0F;
   protected int growingAge;
   public static int BOSS_DAMAGE_LIMIT = 50;
   public float damageTakenThisSec = 0.0F;
   public float healthLastTick = -1.0F;
   public int bossRange = 60;
   public boolean isAggressiveByDefault = true;
   public boolean spreadFire = false;
   public boolean stealthPrev = false;
   public int currentBlockingTime = 0;
   public int blockingTime = 60;
   public LivingEntity pickupEntity;
   public boolean solidCollision = false;
   public boolean trueSight = false;
   protected BlockPos homePosition = LycPos.of(0, 0, 0);
   protected float homeDistanceMax = -1.0F;
   protected BlockPos arenaCenter = null;
   public int nextPriorityGoalIndex;
   public int nextDistractionGoalIndex;
   public int nextCombatGoalIndex;
   public int nextTravelGoalIndex;
   public int nextIdleGoalIndex;
   public int nextReactTargetIndex;
   public int nextSpecialTargetIndex;
   public int nextFindTargetIndex;
   public boolean firstSpawn = true;
   public boolean needsInitialLevel = true;
   public boolean spawnsInBlock = false;
   public boolean spawnsUnderground = true;
   public boolean spawnsOnLand = true;
   public boolean spawnsInWater = false;
   public boolean isLavaCreature = false;
   public boolean isMinion = false;
   public boolean isTemporary = false;
   public int temporaryDuration = 0;
   public boolean forceNoDespawn = false;
   public boolean spawnedRare = false;
   public boolean spawnedAsBoss = false;
   private boolean leashAIActive = false;
   private EntityAIBase leashMoveTowardsRestrictionAI = new MoveRestrictionGoal(this);
   public DirectNavigator directNavigator;
   protected List<Class<? extends Entity>> hostileTargetClasses = new ArrayList<>();
   private LivingEntity masterTarget;
   private LivingEntity parentTarget;
   private LivingEntity avoidTarget;
   private LivingEntity fixateTarget;
   private UUID fixateUUID = null;
   private LivingEntity perchTarget;
   public List<Player> playerTargets = new ArrayList<>();
   public List<LivingEntity> minions = new ArrayList<>();
   public List<Player> guiViewers = new ArrayList<>();
   public int guiRefreshTick = 0;
   public int guiRefreshTime = 2;
   protected int attackCooldownMax = 5;
   private int attackCooldown = 0;
   public boolean hasAttackSound = false;
   public boolean hasStepSound = true;
   public boolean hasJumpSound = false;
   public int flySoundSpeed = 0;
   public boolean extraAnimation01 = false;
   public ServerBossEvent bossInfo;
   public float onlyRenderTicks = -1.0F;
   public boolean initialized = false;
   protected static final EntityDataAccessor<Byte> TARGET = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.BYTE);
   protected static final EntityDataAccessor<Byte> ATTACK_PHASE = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.BYTE);
   protected static final EntityDataAccessor<Byte> ANIMATION_STATE = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.BYTE);
   protected static final EntityDataAccessor<Integer> ANIMATION_ATTACK_COOLDOWN_MAX = SynchedEntityData.defineId(
      BaseCreatureEntity.class, EntityDataSerializers.INT
   );
   protected static final EntityDataAccessor<Byte> CLIMBING = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.BYTE);
   protected static final EntityDataAccessor<Float> STEALTH = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.FLOAT);
   protected static final EntityDataAccessor<Boolean> BABY = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.BOOLEAN);
   protected static final EntityDataAccessor<Byte> COLOR = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.BYTE);
   protected static final EntityDataAccessor<Float> SIZE = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.FLOAT);
   protected static final EntityDataAccessor<Integer> LEVEL = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Integer> EXPERIENCE = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Byte> SUBSPECIES = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.BYTE);
   protected static final EntityDataAccessor<Byte> VARIANT = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.BYTE);
   protected static final EntityDataAccessor<Optional<BlockPos>> ARENA = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
   public static final EntityDataAccessor<ItemStack> EQUIPMENT_HEAD = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.ITEM_STACK);
   public static final EntityDataAccessor<ItemStack> EQUIPMENT_CHEST = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.ITEM_STACK);
   public static final EntityDataAccessor<ItemStack> EQUIPMENT_LEGS = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.ITEM_STACK);
   public static final EntityDataAccessor<ItemStack> EQUIPMENT_FEET = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.ITEM_STACK);
   public static final EntityDataAccessor<ItemStack> EQUIPMENT_BAG = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.ITEM_STACK);
   public static final EntityDataAccessor<ItemStack> EQUIPMENT_SADDLE = SynchedEntityData.defineId(BaseCreatureEntity.class, EntityDataSerializers.ITEM_STACK);
   public InventoryCreature inventory;
   public List<ItemDrop> drops = new ArrayList<>();
   public List<ItemDrop> savedDrops = new ArrayList<>();
   protected boolean dropsRequirePlayerDamage = false;
   public FindAttackTargetGoal aiTargetPlayer = null;
   public RevengeGoal aiDefendAnimals = null;

   public BaseCreatureEntity(EntityType<? extends BaseCreatureEntity> type, Level world) {
      super(type, world);
      this.setWidth = (float)this.creatureInfo.width;
      this.setDepth = (float)this.creatureInfo.width;
      this.setHeight = (float)this.creatureInfo.height;
      this.legacyWidth = this.setWidth;
      this.legacyHeight = this.setHeight;
      this.refreshDimensions();
      this.moveControl = this.createMoveHelper();
      if (!this.canBurn()) {
         this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
         this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
         if (this.canBreatheUnderwater112()) {
            this.setPathfindingMalus(PathType.LAVA, 8.0F);
            if (!this.canBreatheAir() || this.isLavaCreature) {
               this.setPathfindingMalus(PathType.LAVA, 0.0F);
            }
         }
      }

      if (this.waterDamage()) {
         this.setPathfindingMalus(PathType.WATER, -1.0F);
      } else if (this.canBreatheUnderwater112()) {
         this.setPathfindingMalus(PathType.WATER, 8.0F);
         if (!this.canBreatheAir()) {
            this.setPathfindingMalus(PathType.WATER, 0.0F);
         }
      }

      if (this.canWade() && this.getNavigation() instanceof GroundPathNavigation) {
         GroundPathNavigation groundNavigator = (GroundPathNavigation)this.getNavigation();
         groundNavigator.setCanFloat(true);
      }
   }

   /**
    * 1.21 Mob creates the navigation in its constructor on both sides, before registerGoals: this is where 1.12 EntityLivingBase
    * ran applyEntityAttributes (then setHealth(getMaxHealth())) and EntityLiving created the task lists and its navigator.
    */
   @Override
   protected PathNavigation createNavigation(Level world) {
      if (!this.legacyConstructed) {
         this.legacyConstructed = true;
         this.tasks = new EntityAITasks();
         this.targetTasks = new EntityAITasks();
         this.applyEntityAttributes();
         this.setHealth(this.getMaxHealth());
      }
      return this.createNavigator(world);
   }

   /** 1.12 EntityLiving constructor: initEntityAI (server only). */
   @Override
   protected void registerGoals() {
      Legacy112.install(this, this.tasks, this.targetTasks);
      this.initEntityAI();
   }

   /**
    * 1.21 Mob creates the navigation in its constructor on both sides, before registerGoals: this is where 1.12 EntityLivingBase
    * ran applyEntityAttributes (then setHealth(getMaxHealth())) and EntityLiving created the task lists and its navigator.
    */
   @Override
   protected PathNavigation createNavigation(Level world) {
      if (!this.legacyConstructed) {
         this.legacyConstructed = true;
         this.tasks = new EntityAITasks();
         this.targetTasks = new EntityAITasks();
         this.applyEntityAttributes();
         this.setHealth(this.getMaxHealth());
      }
      return this.createNavigator(world);
   }

   /** 1.12 EntityLiving constructor: initEntityAI (server only). */
   @Override
   protected void registerGoals() {
      Legacy112.install(this, this.tasks, this.targetTasks);
      this.initEntityAI();
   }

   protected void applyEntityAttributes() {
      this.loadCreatureFlags();
      this.creatureStats = new CreatureStats(this);
      this.relationships = new CreatureRelationships(this);
      this.extraMobBehaviour = new ExtraMobBehaviour(this);
      this.directNavigator = new DirectNavigator(this);
      this.nextPriorityGoalIndex = 10;
      this.nextDistractionGoalIndex = 30;
      this.nextCombatGoalIndex = 50;
      this.nextTravelGoalIndex = 70;
      this.nextIdleGoalIndex = 90;
      this.nextReactTargetIndex = 10;
      this.nextSpecialTargetIndex = 30;
      this.nextFindTargetIndex = 50;
      this.applyDynamicAttributes();
   }

   public void applyDynamicAttributes() {
      this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(this.creatureStats.getHealth());
      this.getAttribute(DEFENSE).setBaseValue(this.creatureStats.getDefense());
      this.getAttribute(Attributes.ARMOR).setBaseValue(this.creatureStats.getArmor());
      this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.creatureStats.getSpeed());
      this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(this.creatureStats.getKnockbackResistance());
      this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(this.creatureStats.getSight());
      this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(this.creatureStats.getDamage());
      this.getAttribute(Attributes.ATTACK_SPEED).setBaseValue(this.creatureStats.getAttackSpeed());
      this.getAttribute(RANGED_SPEED).setBaseValue(this.creatureStats.getRangedSpeed());
   }

   public void refreshAttributes() {
      this.applyDynamicAttributes();
      this.setHealth(this.getMaxHealth());
      this.refreshBossHealthName();
   }

   public void loadCreatureFlags() {
   }

   protected void initEntityAI() {
      if (this instanceof IFusable) {
         this.targetTasks.addTask(this.nextSpecialTargetIndex++, new FindFuseTargetGoal(this));
      }

      this.targetTasks.addTask(this.nextFindTargetIndex++, new AvoidIfHitGoal(this).setHelpCall(true));
      this.targetTasks.addTask(this.nextFindTargetIndex++, new RevengeGoal(this).setHelpCall(true).setCheckSight(true));
      this.tasks.addTask(this.nextPriorityGoalIndex++, new PaddleGoal(this));
      this.tasks.addTask(this.nextPriorityGoalIndex++, new StayByWaterGoal(this));
      this.tasks
         .addTask(this.nextPriorityGoalIndex++, new AvoidGoal(this).setNearSpeed(1.3).setFarSpeed(1.2).setNearDistance(5.0).setFarDistance(20.0));
      if (this.creatureInfo.isTameable()) {
         this.tasks.addTask(this.nextDistractionGoalIndex++, new TemptGoal(this).setTemptDistanceMin(4.0));
      }

      if (this instanceof IFusable) {
         this.tasks.addTask(this.nextDistractionGoalIndex++, new FollowFuseGoal(this).setLostDistance(16.0));
      }

      this.targetTasks.addTask(this.nextFindTargetIndex++, new FindGroupAttackTargetGoal(this));
      this.targetTasks.addTask(this.nextFindTargetIndex++, new FindGroupAvoidTargetGoal(this).setTameTargetting(false));
      this.tasks.addTask(this.nextTravelGoalIndex++, new FollowMasterGoal(this).setStrayDistance(12.0));
      this.tasks.addTask(this.nextIdleGoalIndex++, new WanderGoal(this));
      this.tasks.addTask(this.nextIdleGoalIndex++, new WatchClosestGoal(this).setTargetClass(Player.class));
      this.tasks.addTask(this.nextIdleGoalIndex++, new LookIdleGoal(this));
   }

   public void setupMob() {
      this.setWidth = (float)((double)this.setWidth * this.creatureInfo.hitboxScale);
      this.setHeight = (float)((double)this.setHeight * this.creatureInfo.hitboxScale);
      this.updateSize();
      this.getAttribute(Attributes.STEP_HEIGHT).setBaseValue(0.5F);
      this.xpReward = this.creatureInfo.experience;
      this.inventory = new InventoryCreature(this.getName().getString(), this);
      this.loadItemDrops();
      if (ItemEquipmentPart.MOB_PART_DROPS.containsKey(this.creatureInfo.getId())) {
         for (ItemEquipmentPart itemEquipmentPart : ItemEquipmentPart.MOB_PART_DROPS.get(this.creatureInfo.getId())) {
            ItemDrop partDrop = new ItemDrop(LycReg.key(itemEquipmentPart).toString(), 0, itemEquipmentPart.dropChance).setMaxAmount(1);
            partDrop.bonusAmount = false;
            partDrop.amountMultiplier = false;
            this.drops.add(partDrop);
         }
      }

      this.setAttackCooldownMax(this.attackCooldownMax);
   }

   public void loadItemDrops() {
      this.drops.addAll(this.creatureInfo.drops);
      this.drops.addAll(CreatureManager.getInstance().config.getGlobalDrops());
   }

   public void addSavedItemDrop(ItemDrop itemDrop) {
      this.drops.add(itemDrop);
      this.savedDrops.add(itemDrop);
   }

   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      this.creatureInfo = CreatureManager.getInstance().getCreature(this.getClass());
      super.defineSynchedData(builder);
      builder.define(TARGET, (byte)0);
      builder.define(ATTACK_PHASE, (byte)0);
      builder.define(ANIMATION_STATE, (byte)0);
      builder.define(ANIMATION_ATTACK_COOLDOWN_MAX, 0);
      builder.define(CLIMBING, (byte)0);
      builder.define(STEALTH, 0.0F);
      builder.define(COLOR, (byte)0);
      builder.define(SIZE, 1.0F);
      builder.define(LEVEL, 1);
      builder.define(EXPERIENCE, 0);
      builder.define(SUBSPECIES, (byte)0);
      builder.define(VARIANT, (byte)0);
      builder.define(ARENA, Optional.empty());
      InventoryCreature.registerDataParameters(builder);
      this.initialized = true;
   }

   /** 1.12 getName() (a String in 1.12). */
   @Override
   public Component getName() {
      return this.hasCustomName() ? this.getCustomName() : Component.literal(this.getFullName());
   }

   /** 1.12 Entity.isImmuneToFire, set from canBurn() in setupMob/onUpdate. */
   @Override
   public boolean fireImmune() {
      return !this.canBurn();
   }

   public String getFullName() {
      String nameFormatting = LanguageManager.translate("entity.lycanitesmobs.creature.name.format");
      String[] nameParts = nameFormatting.split("\\|");
      if (nameParts.length < 4 || nameFormatting.equals("entity.lycanitesmobs.creature.name.format")) {
         nameParts = new String[]{"age", "variant", "subspecies", "species", "level"};
      }

      StringBuilder name = new StringBuilder();
      List<String> nameComponents = new ArrayList<>();

      for (String namePart : nameParts) {
         switch (namePart) {
            case "age":
               nameComponents.add(this.getAgeName());
               break;
            case "variant":
               nameComponents.add(this.getVariantName());
               break;
            case "subspecies":
               nameComponents.add(this.getSubspeciesName());
               break;
            case "species":
               nameComponents.add(this.getSpeciesName());
               break;
            case "level":
               nameComponents.add(this.getLevelName());
         }
      }

      boolean first = true;

      for (String nameComponent : nameComponents) {
         if (!nameComponent.isEmpty()) {
            if (!first) {
               name.append(" ");
            }

            first = false;
            name.append(nameComponent);
         }
      }

      return name.toString();
   }

   public String getAgeName() {
      return "";
   }

   public String getSpeciesName() {
      return this.creatureInfo.getTitle();
   }

   public String getVariantName() {
      return this.getVariant() != null ? this.getVariant().getTitle() : "";
   }

   public String getSubspeciesName() {
      return this.getSubspecies() != null ? this.getSubspecies().getTitle() : "";
   }

   public String getLevelName() {
      return this.getLevel() < 2 ? "" : " " + LanguageManager.translate("entity.level") + " " + this.getLevel();
   }

   public void queueSync() {
      if (!this.level().isClientSide) {
         this.syncQueued = true;
      }
   }

   public void doSync() {
      this.syncQueued = false;
      if (!this.level().isClientSide) {
         for (Player player : this.relationships.getPlayers()) {
            CreatureRelationshipEntry relationshipEntry = this.relationships.getEntry(player);
            if (relationshipEntry != null) {
               MessageCreature message = new MessageCreature(this, relationshipEntry.reputation);
               LycanitesMobs.packetHandler.sendToPlayer(message, (ServerPlayer)player);
            }
         }
      }
   }

   public boolean getBoolFromDataManager(EntityDataAccessor<Boolean> key) {
      try {
         return (Boolean)this.getEntityData().get(key);
      } catch (Exception var3) {
         return false;
      }
   }

   public byte getByteFromDataManager(EntityDataAccessor<Byte> key) {
      try {
         return (Byte)this.getEntityData().get(key);
      } catch (Exception var3) {
         return 0;
      }
   }

   public int getIntFromDataManager(EntityDataAccessor<Integer> key) {
      try {
         return (Integer)this.getEntityData().get(key);
      } catch (Exception var3) {
         return 0;
      }
   }

   public float getFloatFromDataManager(EntityDataAccessor<Float> key) {
      try {
         return (Float)this.getEntityData().get(key);
      } catch (Exception var3) {
         return 0.0F;
      }
   }

   public String getStringFromDataManager(EntityDataAccessor<String> key) {
      try {
         return (String)this.getEntityData().get(key);
      } catch (Exception var3) {
         return null;
      }
   }

   public Optional<UUID> getUUIDFromDataManager(EntityDataAccessor<Optional<UUID>> key) {
      try {
         return (Optional<UUID>)this.getEntityData().get(key);
      } catch (Exception var3) {
         return null;
      }
   }

   public ItemStack getItemStackFromDataManager(EntityDataAccessor<ItemStack> key) {
      try {
         return (ItemStack)this.getEntityData().get(key);
      } catch (Exception var3) {
         return ItemStack.EMPTY;
      }
   }

   public Optional<BlockPos> getBlockPosFromDataManager(EntityDataAccessor<Optional<BlockPos>> key) {
      try {
         return (Optional<BlockPos>)this.getEntityData().get(key);
      } catch (Exception var3) {
         return Optional.empty();
      }
   }

   /** 1.21 replacement for 1.12 Entity.width/height. */
   @Override
   protected EntityDimensions getDefaultDimensions(Pose pose) {
      if (this.legacyWidth <= 0.0F) {
         return super.getDefaultDimensions(pose);
      }
      return EntityDimensions.scalable(this.legacyWidth, this.legacyHeight);
   }

   /** 1.21 replacement for 1.12 Entity.width/height. */
   @Override
   protected EntityDimensions getDefaultDimensions(Pose pose) {
      if (this.legacyWidth <= 0.0F) {
         return super.getDefaultDimensions(pose);
      }
      return EntityDimensions.scalable(this.legacyWidth, this.legacyHeight);
   }

   public boolean getCanSpawnHere() {
      return this.checkSpawnVanilla(this.level(), this.blockPosition());
   }

   public int getMaxSpawnedInChunk() {
      return this.creatureInfo.creatureSpawn.spawnGroupMax;
   }

   /** 1.12 EntityLiving.isNotColliding (vanilla: no liquid, no collisions; water mobs only check collisions). */
   public boolean isNotColliding() {
      return !this.spawnsInWater
         ? this.level().isUnobstructed(this) && this.level().noCollision(this) && !this.level().containsAnyLiquid(this.getBoundingBox())
         : this.level().isUnobstructed(this) && this.level().noCollision(this, this.getBoundingBox());
   }

   @Override
   public boolean checkSpawnObstruction(LevelReader level) {
      return this.isNotColliding();
   }

   public boolean checkSpawnVanilla(Level world, BlockPos pos) {
      if (world.isClientSide) {
         return false;
      } else {
         LycanitesMobs.logDebug("MobSpawns", " ~O==================== Vanilla Spawn Check: " + this.creatureInfo.getName() + " ====================O~");
         LycanitesMobs.logDebug("MobSpawns", "Attempting to Spawn: " + this.creatureInfo.getName());
         LycanitesMobs.logDebug("MobSpawns", "Target Spawn Location: " + pos);
         LycanitesMobs.logDebug("MobSpawns", "Checking if creature is enabled...");
         if (this.creatureInfo.enabled && this.creatureInfo.creatureSpawn.enabled) {
            LycanitesMobs.logDebug("MobSpawns", "Checking for peaceful difficulty...");
            if (!this.creatureInfo.peaceful && this.level().getDifficulty() == Difficulty.PEACEFUL) {
               return false;
            } else {
               LycanitesMobs.logDebug("MobSpawns", "Checking gamerules...");
               if (!this.level().getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
                  return false;
               } else {
                  LycanitesMobs.logDebug("MobSpawns", "Fixed spawn check (light level, collisions)...");
                  if (!this.fixedSpawnCheck(world, pos)) {
                     return false;
                  } else {
                     LycanitesMobs.logDebug("MobSpawns", "Checking for nearby Mob Spawner...");
                     if (this.isSpawnerNearby(world, pos)) {
                        LycanitesMobs.logDebug("MobSpawns", "Mob Spawner found, skipping other checks.");
                        LycanitesMobs.logDebug("MobSpawns", "Vanilla Spawn Check Passed!");
                        return true;
                     } else {
                        LycanitesMobs.logDebug("MobSpawns", "No Mob Spawner found.");
                        LycanitesMobs.logDebug("MobSpawns", "Global Spawn Check (Master Dimension List, etc)...");
                        if (!CreatureManager.getInstance().spawnConfig.isAllowedGlobal(world)) {
                           return false;
                        } else {
                           LycanitesMobs.logDebug("MobSpawns", "Environment spawn check (dimension, group limit, ground type, water, lava, underground)...");
                           if (!this.environmentSpawnCheck(world, pos)) {
                              return false;
                           } else {
                              LycanitesMobs.logDebug("MobSpawns", "Vanilla Spawn Check Passed!");
                              return true;
                           }
                        }
                     }
                  }
               }
            }
         } else {
            return false;
         }
      }
   }

   public boolean fixedSpawnCheck(Level world, BlockPos pos) {
      if (!this.checkSpawnLightLevel(world, pos)) {
         return false;
      } else {
         LycanitesMobs.logDebug("MobSpawns", "Checking collision...");
         if (!this.checkSpawnCollision(world, pos)) {
            return false;
         } else {
            LycanitesMobs.logDebug("MobSpawns", "Counting mobs of the same kind, max allowed is: " + this.creatureInfo.creatureSpawn.spawnAreaLimit);
            return this.checkSpawnGroupLimit(world, pos, CreatureManager.getInstance().spawnConfig.spawnLimitRange);
         }
      }
   }

   public boolean environmentSpawnCheck(Level world, BlockPos pos) {
      if (this.creatureInfo.creatureSpawn.worldDayMin > 0.0) {
         int currentDay = (int)Math.floor((double)world.getGameTime() / 24000.0);
         LycanitesMobs.logDebug(
            "MobSpawns", "Checking world age, currently on day: " + currentDay + ", must be at least day: " + this.creatureInfo.creatureSpawn.worldDayMin + "."
         );
         if ((double)currentDay < this.creatureInfo.creatureSpawn.worldDayMin) {
            return false;
         }
      }

      LycanitesMobs.logDebug("MobSpawns", "Checking dimension.");
      if (!this.isNativeDimension(this.level())) {
         return false;
      } else {
         LycanitesMobs.logDebug("MobSpawns", "Block preference.");
         if (this.getBlockPathWeight(pos.getX(), pos.getY(), pos.getZ()) < 0.0F) {
            return false;
         } else {
            LycanitesMobs.logDebug("MobSpawns", "Checking for liquid (water, lava, ooze, etc).");
            if (!this.spawnsInWater && this.level().containsAnyLiquid(this.getBoundingBox())) {
               return false;
            } else if (!this.spawnsOnLand && !this.level().containsAnyLiquid(this.getBoundingBox())) {
               return false;
            } else {
               LycanitesMobs.logDebug("MobSpawns", "Checking for underground.");
               if (!this.spawnsUnderground && this.isBlockUnderground(pos.getX(), pos.getY() + 1, pos.getZ())) {
                  return false;
               } else {
                  LycanitesMobs.logDebug("MobSpawns", "Checking for nearby bosses.");
                  return this.checkSpawnBoss(world, pos);
               }
            }
         }
      }
   }

   public boolean isNativeDimension(Level world) {
      return this.creatureInfo.creatureSpawn.isAllowedDimension(world);
   }

   public boolean checkSpawnCollision(Level world, BlockPos pos) {
      return this.spawnsInBlock || LycWorld.getCollisionBoxes(this.level(), this, this.getBoundingBox()).isEmpty();
   }

   public boolean checkSpawnLightLevel(Level world, BlockPos pos) {
      if (this.creatureInfo.creatureSpawn.spawnsInDark && this.creatureInfo.creatureSpawn.spawnsInLight) {
         return true;
      } else if (!this.creatureInfo.creatureSpawn.spawnsInDark && !this.creatureInfo.creatureSpawn.spawnsInLight) {
         return false;
      } else {
         byte light = this.testLightLevel(pos);
         return this.creatureInfo.creatureSpawn.spawnsInDark && light <= 1 ? true : this.creatureInfo.creatureSpawn.spawnsInLight && light >= 2;
      }
   }

   public boolean checkSpawnGroupLimit(Level world, BlockPos pos, double range) {
      if (range <= 0.0) {
         return true;
      } else {
         int typesLimit = CreatureManager.getInstance().spawnConfig.typeSpawnLimit;
         int speciesLimit = this.creatureInfo.creatureSpawn.spawnAreaLimit;
         if (typesLimit <= 0 && speciesLimit <= 0) {
            return true;
         } else {
            List<BaseCreatureEntity> targets = this.getNearbyEntities(
               BaseCreatureEntity.class, entity -> BaseCreatureEntity.class.isAssignableFrom(entity.getClass()), range
            );
            int typesFound = 0;
            int speciesFound = 0;

            for (BaseCreatureEntity targetCreature : targets) {
               if (targetCreature.creatureInfo.peaceful == this.creatureInfo.peaceful) {
                  typesFound++;
               }

               if (this.creatureInfo.entityClass.isAssignableFrom(targetCreature.getClass())) {
                  speciesFound++;
               }
            }

            if (range > 0.0) {
               if (typesLimit > 0 && typesFound >= typesLimit) {
                  return false;
               }

               if (speciesLimit > 0 && speciesFound >= speciesLimit) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   public boolean checkSpawnBoss(Level world, BlockPos pos) {
      List bosses = this.getNearbyEntities(
         BaseCreatureEntity.class, entity -> IGroupBoss.class.isAssignableFrom(entity.getClass()), CreatureManager.getInstance().spawnConfig.spawnLimitRange
      );
      return bosses.size() == 0;
   }

   /** 1.12 onInitialSpawn: 1.21 finalizeSpawn does what 1.12 EntityLiving.onInitialSpawn did (follow range bonus, left handed). */
   @Override
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData livingData) {
      return this.onInitialSpawn(difficulty, super.finalizeSpawn(level, difficulty, spawnType, livingData));
   }

   public SpawnGroupData onInitialSpawn(DifficultyInstance difficulty, SpawnGroupData livingData) {
      return livingData;
   }

   /** 1.12 canDespawn (1.21 asks removeWhenFarAway). */
   @Override
   public boolean removeWhenFarAway(double distance) {
      return this.canDespawn();
   }

   protected boolean canDespawn() {
      if (this.creatureInfo.creatureSpawn.despawnForced) {
         return true;
      } else if (!this.creatureInfo.creatureSpawn.despawnNatural) {
         return false;
      } else if (!this.creatureInfo.boss && (!this.isRareVariant() || Variant.RARE_DESPAWNING)) {
         return !this.isPersistant() && !this.isLeashed() && (!this.hasCustomName() || !"".equals(this.spawnEventType)) ? true : false;
      } else {
         return false;
      }
   }

   public boolean isPersistant() {
      return this.forceNoDespawn;
   }

   /** 1.12 enablePersistence */
   @Override
   public void setPersistenceRequired() {
      super.setPersistenceRequired();
      this.forceNoDespawn = true;
   }

   public boolean despawnCheck() {
      if (this.level().isClientSide) {
         return false;
      } else if (!this.creatureInfo.enabled) {
         return true;
      } else if (this.isTemporary && this.temporaryDuration-- <= 0) {
         return true;
      } else if (!this.creatureInfo.peaceful && this.level().getDifficulty() == Difficulty.PEACEFUL && !this.hasCustomName()) {
         return true;
      } else {
         ExtendedWorld worldExt = ExtendedWorld.getForWorld(this.level());
         if (worldExt == null || "".equals(this.spawnEventType) || this.spawnEventCount < 0 || this.spawnEventCount == worldExt.getWorldEventCount()) {
            return false;
         } else if (!this.isLeashed() && !this.isPersistant()) {
            return true;
         } else {
            this.spawnEventType = "";
            this.spawnEventCount = -1;
            return false;
         }
      }
   }

   public boolean isSpawnerNearby(Level world, BlockPos pos) {
      int x = pos.getX();
      int y = pos.getY();
      int z = pos.getZ();
      if (world == null) {
         return false;
      } else {
         int checkRange = 8;

         for (int i = x - checkRange; i <= x + checkRange; i++) {
            for (int j = y - checkRange; j <= y + checkRange; j++) {
               for (int k = z - checkRange; k <= z + checkRange; k++) {
                  BlockState spawnerBlock = world.getBlockState(LycPos.of(i, j, k));
                  if (spawnerBlock != null) {
                     BlockEntity tileEntity = world.getBlockEntity(LycPos.of(i, j, k));
                     if (tileEntity != null && tileEntity instanceof SpawnerBlockEntity) {
                        try {
                           SpawnerBlockEntity mobSpawner = (SpawnerBlockEntity)tileEntity;
                           CompoundTag nbt = new CompoundTag();
                           mobSpawner.getSpawner().save(nbt);
                           if (nbt.contains("SpawnData")) {
                              CompoundTag spawnData = nbt.getCompound("SpawnData").getCompound("entity");
                              if (spawnData.contains("id")) {
                                 String mobSpawnEntityId = spawnData.getString("id");
                                 if (mobSpawnEntityId.equalsIgnoreCase(this.creatureInfo.getId())) {
                                    return true;
                                 }
                              }
                           }
                        } catch (Exception var16) {
                        }
                     }
                  }
               }
            }
         }

         return false;
      }
   }

   public boolean isBlockUnderground(int x, int y, int z) {
      if (this.level().canSeeSky(LycPos.of(x, y, z))) {
         return false;
      } else {
         for (int j = y; j < this.level().getHeight(); j++) {
            Material blockMaterial = Material.of(this.level().getBlockState(LycPos.of(x, j, z)));
            if (blockMaterial != Material.AIR
               && blockMaterial != Material.LEAVES
               && blockMaterial != Material.PLANTS
               && blockMaterial != Material.GRASS
               && blockMaterial != Material.VINE) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean isBoss() {
      return this.isBossAlways() || this.spawnedAsBoss;
   }

   public boolean isBossAlways() {
      return this.creatureInfo.boss;
   }

   public boolean isNonBoss() {
      return !this.isBoss();
   }

   public void createBossInfo(BossEvent.BossBarColor color, boolean darkenSky) {
      String name = this.getName().getString();
      if (this.isBossAlways()) {
         name = name + " (" + LanguageManager.translate("entity.phase") + " " + (this.getBattlePhase() + 1) + ")";
      }

      this.bossInfo = (ServerBossEvent)new ServerBossEvent(Component.literal(name), color, BossEvent.BossBarOverlay.PROGRESS).setDarkenScreen(darkenSky);
   }

   public BossEvent getBossInfo() {
      if (this.bossInfo == null && this.showBossInfo() && !this.level().isClientSide) {
         if (this.isBoss()) {
            this.createBossInfo(BossEvent.BossBarColor.RED, false);
         } else {
            this.createBossInfo(BossEvent.BossBarColor.GREEN, false);
         }
      }

      return this.bossInfo;
   }

   public void refreshBossHealthName() {
      if (this.bossInfo != null) {
         String name = this.getFullName();
         if (this.isBossAlways()) {
            name = name + " (" + LanguageManager.translate("entity.phase") + " " + (this.getBattlePhase() + 1) + ")";
         }

         this.bossInfo.setName(Component.literal(name));
      }
   }

   public void summonMinion(LivingEntity minion, double angle, double distance) {
      double angleRadians = Math.toRadians(angle);
      double x = this.getX() + (((double)this.getBbWidth() + distance) * Math.cos(angleRadians) - Math.sin(angleRadians));
      double y = this.getY() + 1.0;
      if (minion instanceof BaseCreatureEntity && ((BaseCreatureEntity)minion).isCurrentlyFlying()) {
         y += (double)(this.getBbHeight() / 2.0F);
      }

      double z = this.getZ() + ((double)this.getBbWidth() + distance) * Math.sin(angleRadians) + Math.cos(angleRadians);
      minion.moveTo(x, y, z, this.random.nextFloat() * 360.0F, 0.0F);
      this.level().addFreshEntity(minion);
      if (minion instanceof BaseCreatureEntity) {
         ((BaseCreatureEntity)minion).setMinion(true);
         if (!this.isRareVariant()) {
            ((BaseCreatureEntity)minion).applyVariant(this.getVariantIndex());
         }

         ((BaseCreatureEntity)minion).setSubspecies(this.getSubspeciesIndex());
         ((BaseCreatureEntity)minion).setMasterTarget(this);
         ((BaseCreatureEntity)minion).spawnEventType = this.spawnEventType;
         if (this.isTemporary) {
            ((BaseCreatureEntity)minion).setTemporary(this.temporaryDuration);
         }

         ((BaseCreatureEntity)minion).onFirstSpawn();
      }

      if (this.getTarget() != null) {
         minion.setLastHurtByMob(this.getTarget());
      }

      this.addMinion(minion);
   }

   public boolean addMinion(LivingEntity minion) {
      if (this.minions.contains(minion)) {
         return false;
      } else {
         this.minions.add(minion);
         return true;
      }
   }

   public List<LivingEntity> getMinions(Class<? extends Entity> filterClass) {
      if (filterClass == null) {
         return this.minions;
      } else {
         List<LivingEntity> filteredMinions = new ArrayList<>();

         for (LivingEntity minion : this.minions) {
            if (filterClass.isAssignableFrom(minion.getClass())) {
               filteredMinions.add(minion);
            }
         }

         return filteredMinions;
      }
   }

   public void onMinionUpdate(LivingEntity minion, long tick) {
   }

   public void onMinionDeath(LivingEntity minion, DamageSource damageSource) {
   }

   public void onTryToDamageMinion(LivingEntity minion, float damageAmount) {
   }

   public void setMinion(boolean minion) {
      this.isMinion = minion;
   }

   public boolean isMinion() {
      return this.isMinion;
   }

   public void setTemporary(int duration) {
      this.temporaryDuration = duration;
      this.isTemporary = true;
   }

   public void unsetTemporary() {
      this.isTemporary = false;
      this.temporaryDuration = 0;
   }

   public boolean isBoundPet() {
      return this.hasPetEntry();
   }

   public boolean hasPetEntry() {
      return this.getPetEntry() != null;
   }

   public PetEntry getPetEntry() {
      return this.petEntry;
   }

   public void setPetEntry(PetEntry petEntry) {
      this.petEntry = petEntry;
   }

   public boolean isPetType(String type) {
      return !this.hasPetEntry() ? false : type.equals(this.getPetEntry().getType());
   }

   public void onFirstSpawn() {
      this.firstSpawn = false;
      if (this.hasPetEntry()) {
         if (this.getPetEntry().summonSet != null && this.getPetEntry().summonSet.playerExt != null) {
            this.getPetEntry().summonSet.playerExt.sendPetEntryToPlayer(this.getPetEntry());
         }
      } else if (!this.isMinion()) {
         if (this.needsInitialLevel) {
            this.applyLevel(this.getStartingLevel());
         }

         if (this.getSubspeciesIndex() == 0 && this.getVariantIndex() == 0) {
            this.getRandomSubspecies();
            if (CreatureManager.getInstance().config.variantSpawn && !this.creatureInfo.creatureSpawn.disableVariants) {
               this.getRandomVariant();
            }
         }

         if (this.sizeScale == 1.0 && CreatureManager.getInstance().config.randomSizes) {
            this.getRandomSize();
         }
      }
   }

   public void getRandomSubspecies() {
      if (!this.isMinion()) {
         Subspecies randomSubspecies = this.creatureInfo.getRandomSubspecies(this);
         this.setSubspecies(randomSubspecies.index);
         LycanitesMobs.logDebug("Subspecies", "Setting " + this.getSpeciesName() + " subspecies to " + randomSubspecies.getTitle());
      }
   }

   public void getRandomVariant() {
      if (!this.isMinion()) {
         Variant randomVariant = this.getSubspecies().getRandomVariant(this, this.spawnedRare);
         if (randomVariant != null) {
            LycanitesMobs.logDebug("Subspecies", "Setting " + this.getSpeciesName() + " variant to " + randomVariant.getTitle());
            this.applyVariant(randomVariant.index);
         } else {
            LycanitesMobs.logDebug("Subspecies", "Setting " + this.getSpeciesName() + " variant to base variant.");
            this.applyVariant(0);
         }
      }
   }

   public void getRandomSize() {
      double range = CreatureManager.getInstance().config.randomSizeMax - CreatureManager.getInstance().config.randomSizeMin;
      double randomScale = range * this.getRandom().nextDouble();
      double scale = CreatureManager.getInstance().config.randomSizeMin + randomScale;
      if (this.getVariant() != null) {
         scale *= this.getVariant().getScale();
      }

      this.setSizeScale(scale);
   }

   public int getAge() {
      if (this.level().isClientSide) {
         return this.getBoolFromDataManager(BABY) ? -1 : 1;
      } else {
         return this.growingAge;
      }
   }

   public int getLevel() {
      return this.level().isClientSide ? this.getIntFromDataManager(LEVEL) : this.level;
   }

   public int getStartingLevel() {
      int startingLevelMin = Math.max(1, CreatureManager.getInstance().config.startingLevelMin);
      if (CreatureManager.getInstance().config.startingLevelMax > startingLevelMin) {
         return startingLevelMin + this.getRandom().nextInt(CreatureManager.getInstance().config.startingLevelMax - startingLevelMin);
      } else {
         if (CreatureManager.getInstance().config.levelPerDay > 0.0 && CreatureManager.getInstance().config.levelPerDayMax > 0) {
            int day = (int)Math.floor((double)this.level().getGameTime() / 23999.0);
            double levelGain = Math.min(
               CreatureManager.getInstance().config.levelPerDay * (double)day, (double)CreatureManager.getInstance().config.levelPerDayMax
            );
            startingLevelMin += (int)Math.floor(levelGain);
         }

         if (CreatureManager.getInstance().config.levelPerLocalDifficulty > 0.0) {
            double levelGain = (double)this.level().getCurrentDifficultyAt(this.blockPosition()).getEffectiveDifficulty();
            startingLevelMin += Math.max(0, (int)Math.floor(levelGain - 1.5));
         }

         return startingLevelMin;
      }
   }

   public void applyLevel(int level) {
      this.needsInitialLevel = false;
      this.setLevel(level);
      this.refreshAttributes();
   }

   public void setLevel(int level) {
      this.level = level;
      this.entityData.set(LEVEL, level);
   }

   public void addLevel(int level) {
      this.applyLevel(this.level + level);
   }

   public void updateLevelExperience() {
      if (!this.level().isClientSide) {
         this.entityData.set(EXPERIENCE, this.experience);
      }

      if (this.getExperience() >= this.creatureStats.getExperienceForNextLevel()) {
         this.setExperience(this.getExperience() - this.creatureStats.getExperienceForNextLevel());
         this.addLevel(1);
      }
   }

   public int getExperience() {
      return this.level().isClientSide ? this.getIntFromDataManager(EXPERIENCE) : this.experience;
   }

   public void setExperience(int experience) {
      this.experience = experience;
      this.updateLevelExperience();
   }

   public void addExperience(int experience) {
      this.experience += experience;
      this.updateLevelExperience();
   }

   public int getMeleeCooldown() {
      return Math.round((float)(1.0 / this.getAttribute(Attributes.ATTACK_SPEED).getValue() * 20.0));
   }

   public int getRangedCooldown() {
      return Math.round((float)(1.0 / this.getAttribute(RANGED_SPEED).getValue() * 20.0));
   }

   public int getEffectDuration(int seconds) {
      return Math.round((float)seconds * (float)this.creatureStats.getEffect() * 20.0F);
   }

   public int getEffectAmplifier(float scale) {
      return Math.round((float)this.creatureStats.getAmplifier());
   }

   public int getEffectStrength(float value) {
      return Math.round(value * (float)this.creatureStats.getAmplifier());
   }

   public List<ElementInfo> getElements() {
      return this.getSubspecies() != null && !this.getSubspecies().elements.isEmpty() ? this.getSubspecies().elements : this.creatureInfo.elements;
   }

   public boolean hasElement(ElementInfo element) {
      return this.getElements().contains(element);
   }

   public void setSubspecies(int subspeciesIndex) {
      this.subspecies = this.creatureInfo.getSubspecies(subspeciesIndex);
   }

   public void setVariant(int variantIndex) {
      this.variant = this.getSubspecies().getVariant(variantIndex);
      if (this.variant != null) {
         float scaledExp = (float)this.creatureInfo.experience;
         if ("uncommon".equals(this.variant.rarity)) {
            scaledExp = (float)Math.round((float)((double)this.creatureInfo.experience * Variant.UNCOMMON_EXPERIENCE_SCALE));
         } else if ("rare".equals(this.variant.rarity)) {
            scaledExp = (float)Math.round((float)((double)this.creatureInfo.experience * Variant.RARE_EXPERIENCE_SCALE));
         }

         this.xpReward = Math.round(scaledExp);
         if ("rare".equals(this.variant.rarity)) {
            this.damageLimit = (float)BOSS_DAMAGE_LIMIT;
            this.damageMax = BOSS_DAMAGE_LIMIT;
         }
      }
   }

   public void applyVariant(int variantIndex) {
      this.setVariant(variantIndex);
      this.refreshAttributes();
   }

   public Subspecies getSubspecies() {
      if (this.subspecies == null) {
         this.subspecies = this.creatureInfo.getSubspecies(0);
      }

      return this.subspecies;
   }

   public Variant getVariant() {
      return this.variant;
   }

   public int getSubspeciesIndex() {
      return this.getSubspecies().index;
   }

   public int getVariantIndex() {
      return this.getVariant() != null ? this.getVariant().index : 0;
   }

   public boolean isRareVariant() {
      return this.getVariant() != null && "rare".equals(this.getVariant().rarity);
   }

   public int scaleKnowledgeExperience(int knowledgeExperience) {
      if (this.isBoss()) {
         knowledgeExperience = Math.round((float)CreatureManager.getInstance().config.creatureBossKnowledgeScale * (float)knowledgeExperience);
      } else if (this.getVariant() != null) {
         knowledgeExperience = Math.round((float)CreatureManager.getInstance().config.creatureVariantKnowledgeScale * (float)knowledgeExperience);
      }

      return knowledgeExperience;
   }

   public void updateBattlePhase() {
   }

   public int getBattlePhase() {
      return this.battlePhase;
   }

   public void setBattlePhase(int phase) {
      if (this.getBattlePhase() != phase) {
         this.battlePhase = phase;
         this.refreshBossHealthName();
         this.playPhaseSound();
      }
   }

   public LivingEntity transform(Class transformClass, Entity partner, boolean destroyPartner) {
      LivingEntity transformedEntity = null;

      try {
         transformedEntity = (LivingEntity)transformClass.getConstructor(Level.class).newInstance(this.level());
      } catch (Exception var12) {
         var12.printStackTrace();
      }

      if (transformedEntity == null) {
         return null;
      } else {
         if (transformedEntity instanceof BaseCreatureEntity) {
            BaseCreatureEntity transformedCreature = (BaseCreatureEntity)transformedEntity;
            transformedCreature.firstSpawn = false;
            if (this.isTemporary) {
               transformedCreature.setTemporary(this.temporaryDuration);
            }

            if (this.isMinion()) {
               transformedCreature.setMinion(true);
            }

            if (this.hasMaster()) {
               transformedCreature.setMasterTarget(this.getMasterTarget());
            }

            if (partner instanceof BaseCreatureEntity) {
               BaseCreatureEntity partnerCreature = (BaseCreatureEntity)partner;
               Variant fusionVariant = transformedCreature.getSubspecies().getChildVariant(this, this.getVariant(), partnerCreature.getVariant());
               transformedCreature.setSubspecies(this.getSubspeciesIndex());
               transformedCreature.applyVariant(fusionVariant != null ? fusionVariant.index : 0);
               transformedCreature.setSizeScale(this.sizeScale + partnerCreature.sizeScale);
               if (partnerCreature.summoningPedestal != null) {
                  partnerCreature.summoningPedestal.minions.add(transformedCreature);
                  transformedCreature.summoningPedestal = partnerCreature.summoningPedestal;
               }

               if (this.summoningPedestal != null) {
                  this.summoningPedestal.minions.add(transformedCreature);
                  transformedCreature.summoningPedestal = this.summoningPedestal;
               }

               int transformedLevel = this.getLevel();
               if ("lowest".equalsIgnoreCase(CreatureManager.getInstance().config.elementalFusionLevelMix)) {
                  if (transformedLevel > partnerCreature.getLevel()) {
                     transformedLevel = partnerCreature.getLevel();
                  }
               } else if ("highest".equalsIgnoreCase(CreatureManager.getInstance().config.elementalFusionLevelMix)) {
                  if (transformedLevel < partnerCreature.getLevel()) {
                     transformedLevel = partnerCreature.getLevel();
                  }
               } else {
                  transformedLevel += partnerCreature.getLevel();
               }

               transformedCreature.applyLevel(Math.round((float)transformedLevel * (float)CreatureManager.getInstance().config.elementalFusionLevelMultiplier));
               if (transformedCreature instanceof TameableCreatureEntity) {
                  TameableCreatureEntity fusionTameable = (TameableCreatureEntity)transformedCreature;
                  if (this instanceof TameableCreatureEntity) {
                     Entity owner = ((TameableCreatureEntity)this).getPlayerOwner();
                     if (owner != null) {
                        transformedCreature.applyLevel(transformedLevel);
                        fusionTameable.setPlayerOwner((Player)owner);
                        ((TameableCreatureEntity)this).copyPetBehaviourTo(fusionTameable);
                     }
                  } else {
                     Entity partnerOwner = null;
                     if (partnerCreature instanceof TameableCreatureEntity) {
                        Entity var14 = ((TameableCreatureEntity)partnerCreature).getPlayerOwner();
                        if (var14 != null) {
                           transformedCreature.applyLevel(transformedLevel);
                           fusionTameable.setPlayerOwner(var14);
                           ((TameableCreatureEntity)partnerCreature).copyPetBehaviourTo(fusionTameable);
                           if (partnerCreature.isTemporary) {
                              transformedCreature.setTemporary(partnerCreature.temporaryDuration);
                           }

                           transformedCreature.setMinion(partnerCreature.isMinion());
                           if (partnerCreature.hasMaster()) {
                              transformedCreature.setMasterTarget(partnerCreature.getMasterTarget());
                           }
                        }
                     }
                  }
               }
            } else {
               transformedCreature.setSubspecies(this.getSubspeciesIndex());
               transformedCreature.applyVariant(this.getVariantIndex());
               transformedCreature.setSizeScale(this.sizeScale);
               transformedCreature.applyLevel(this.getLevel());
               if (transformedCreature instanceof TameableCreatureEntity) {
                  TameableCreatureEntity fusionTameable = (TameableCreatureEntity)transformedCreature;
                  if (this.getOwner() != null && this.getOwner() instanceof Player) {
                     fusionTameable.setPlayerOwner((Player)this.getOwner());
                     if (this instanceof TameableCreatureEntity) {
                        ((TameableCreatureEntity)this).copyPetBehaviourTo(fusionTameable);
                     }
                  }
               }
            }
         }

         transformedEntity.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
         this.level().addFreshEntity(transformedEntity);
         this.discard();
         if (partner != null && destroyPartner) {
            partner.discard();
         }

         return transformedEntity;
      }
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.level().isClientSide) {
         this.updateLeashedState();
      }
      if (!this.creatureInfo.dummy) {
         this.onSyncUpdate();
         if (!this.level().isClientSide) {
            this.updateHitAreas();
         }

         if (this.despawnCheck()) {
            if (!this.isBoundPet() || this.isTemporary) {
               this.inventory.dropInventory();
            }

            this.discard();
         }

         if (!this.canWalk() && !this.isCurrentlyFlying() && !this.isInWater() && this.isMoving() || !this.canMove()) {
            this.clearMovement();
         }

         if (!this.level().isClientSide || this.isControlledByLocalInstance()) {
            this.setBesideClimbableBlock(this.horizontalCollision);
            if (!this.onGround() && this.flySoundSpeed > 0 && this.tickCount % 20 == 0) {
               this.playFlySound();
            }
         }

         if (!this.level().isClientSide && this.isFlying() && this.hasAttackTarget() && this.updateTick % 40L == 0L) {
            this.leap(0.0, 0.4);
         }

         LivingEntity perchTarget = this.getPerchTarget();
         if (perchTarget != null) {
            ExtendedEntity perchEntityExt = ExtendedEntity.getForEntity(this.getPerchTarget());
            if (perchEntityExt != null) {
               Vec3 perchPosition = perchEntityExt.getPerchPosition();
               this.setPos(perchPosition.x, perchPosition.y, perchPosition.z);
               LycMotion.setX(this, perchTarget.getDeltaMovement().x);
               LycMotion.setY(this, perchTarget.getDeltaMovement().y);
               LycMotion.setZ(this, perchTarget.getDeltaMovement().z);
               this.setYRot(perchTarget.getYRot());
            }

            if (perchTarget instanceof Player && !perchTarget.isPassenger()) {
               ExtendedPlayer perchPlayerExt = ExtendedPlayer.getForPlayer((Player)perchTarget);
               if (perchPlayerExt.isControlActive(ExtendedPlayer.CONTROL_ID.MOUNT_DISMOUNT)) {
                  this.perchOnEntity(null);
               }
            }
         }

         if (!this.level().isClientSide && this.isBoss() && this.updateTick % 20L == 0L && this.playerTargets.isEmpty()) {
            this.heal(1.0F);
         }

         if (this.bossInfo != null) {
            this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());
         }

         if (!this.level().isClientSide && this.updateTick % 40L == 0L) {
            for (Player player : this.level()
               .getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(64.0), playerx -> playerx != null && this.distanceTo(playerx) <= 5.0F)) {
               ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(player);
               if (extendedPlayer != null) {
                  CreatureKnowledge creatureKnowledge = extendedPlayer.getBeastiary().getCreatureKnowledge(this.creatureInfo.getName());
                  if (creatureKnowledge == null || creatureKnowledge.rank < 1) {
                     extendedPlayer.studyCreature(this, CreatureManager.getInstance().config.creatureProximityKnowledge, false, false);
                  }
               }
            }
         }

         if (!this.level().isClientSide && this.guiViewers.size() <= 0) {
            this.guiRefreshTick = 0;
         }

         if (!this.level().isClientSide && this.guiRefreshTick > 0 && --this.guiRefreshTick <= 0) {
            this.refreshGUIViewers();
            this.guiRefreshTick = 0;
         }
      }
   }

   protected void customServerAiStep() {
      if (this.useDirectNavigator()) {
         this.directNavigator.updateFlight();
      }

      super.customServerAiStep();
   }

   public void aiStep() {
      if (this.damageLimit > 0.0F) {
         if (this.healthLastTick < 0.0F) {
            this.healthLastTick = this.getHealth();
         }

         if (this.healthLastTick - this.getHealth() > this.damageLimit) {
            this.setHealth(this.healthLastTick - this.damageLimit);
         }

         this.healthLastTick = this.getHealth();
         if (!this.level().isClientSide && this.updateTick % 20L == 0L) {
            this.damageTakenThisSec = 0.0F;
         }
      }

      super.aiStep();
      if (this.creatureInfo.dummy) {
         this.updateTick++;
      } else {
         if (this.attackCooldown > 0) {
            this.attackCooldown--;
            if (this.attackCooldown > this.getAttackCooldownMax()) {
               this.triggerAttackCooldown();
            }
         }

         this.updateBattlePhase();
         this.updateSwingTime();
         if (this.currentBlockingTime > 0) {
            this.currentBlockingTime--;
         }

         if (this.currentBlockingTime < 0) {
            this.currentBlockingTime = 0;
         }

         if (!this.level().isClientSide && this.firstSpawn) {
            this.onFirstSpawn();
         }

         if (!this.level().isClientSide && !this.hasFixateTarget() && this.fixateUUID != null) {
            double range = 64.0;

            for (LivingEntity possibleFixateTarget : this.level()
               .getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(range, range, range))) {
               if (possibleFixateTarget != this && possibleFixateTarget.getUUID().equals(this.fixateUUID)) {
                  this.setFixateTarget(possibleFixateTarget);
                  break;
               }
            }

            this.fixateUUID = null;
         }

         if (this.hasFixateTarget()) {
            this.setTarget(this.getFixateTarget());
         }

         if (this.hasAttackTarget() && this.getTarget() instanceof Player) {
            Player targetPlayer = (Player)this.getTarget();
            if (targetPlayer.getAbilities().instabuild) {
               this.setTarget(null);
            }
         }

         if (this.hasAvoidTarget() && this.currentFleeTime-- <= 0) {
            this.setAvoidTarget(null);
         }

         if (!this.onGround() && this.getDeltaMovement().y < 0.0) {
            LycMotion.setY(this, this.getDeltaMovement().y * this.getFallingMod());
         }

         if (!this.level().isClientSide && this.daylightBurns() && this.level().isDay()) {
            float brightness = this.getLightLevelDependentMagicValue();
            if (brightness > 0.5F
               && this.random.nextFloat() * 30.0F < (brightness - 0.4F) * 2.0F
               && this.level().canSeeSky(this.blockPosition())) {
               boolean shouldBurn = true;
               ItemStack helmet = this.inventory.getEquipmentStack("head");
               if (!helmet.isEmpty()) {
                  if (helmet.isDamageableItem()) {
                     helmet.setDamageValue(helmet.getDamageValue() + this.random.nextInt(2));
                     if (helmet.getDamageValue() >= helmet.getMaxDamage()) {
                        this.onEquippedItemBroken(helmet.getItem(), EquipmentSlot.HEAD);
                        this.setCurrentItemOrArmor(4, ItemStack.EMPTY);
                     }
                  }

                  shouldBurn = false;
               }

               if (shouldBurn) {
                  this.igniteForSeconds(8);
               }
            }
         }

         if (!this.level().isClientSide && this.waterDamage() && this.isInWaterRainOrBubble() && !this.isInLava()) {
            this.hurt(LycDamage.src(DamageTypes.DROWN), 1.0F);
         }

         if (!this.level().isClientSide && !this.canBreatheAir()) {
            int currentAir = this.getAirSupply();
            if (this.isAlive()) {
               if (!this.isLavaCreature && !this.waterContact() || this.isLavaCreature && !this.lavaContact()) {
                  this.setAirSupply(--currentAir);
                  if (this.getAirSupply() <= -200) {
                     this.setAirSupply(-180);
                     this.hurt(LycDamage.src(DamageTypes.DROWN), 2.0F);
                  }
               } else {
                  this.setAirSupply(299);
               }
            }
         }

         if (!this.level().isClientSide) {
            float light = this.getLightLevelDependentMagicValue();
            if (!this.creatureInfo.creatureSpawn.spawnsInLight && light > 0.5F) {
               this.noActionTime += 2;
            } else if (!this.creatureInfo.creatureSpawn.spawnsInDark && light <= 0.5F) {
               this.noActionTime += 2;
            }
         }

         if (!this.level().isClientSide) {
            if (this.isStealthed() && !this.isInvisible()) {
               this.setInvisible(true);
            } else if (!this.isStealthed() && this.isInvisible() && !this.hasEffect(MobEffects.INVISIBILITY)) {
               this.setInvisible(false);
            }
         }

         if (this.isStealthed()) {
            if (this.stealthPrev != this.isStealthed()) {
               this.startStealth();
            }

            this.onStealth();
         } else if (this.isInvisible() && !this.hasEffect(MobEffects.INVISIBILITY) && !this.level().isClientSide) {
            this.setInvisible(false);
         }

         this.stealthPrev = this.isStealthed();
         if (this.tickCount % 20 == 0 && !this.level().isClientSide && this.isAlive() && this.canPickupItems()) {
            this.pickupItems();
         }

         if (!this.level().isClientSide && this.pickupEntity != null) {
            if (!this.pickupEntity.isAlive()) {
               this.dropPickupEntity();
            } else if (Math.sqrt((double)this.distanceTo(this.pickupEntity)) > 32.0) {
               this.dropPickupEntity();
            }
         }

         this.getBossInfo();
         if (this.isBossAlways() || this.spawnedWithBlockProtection > 0) {
            ExtendedWorld extendedWorld = ExtendedWorld.getForWorld(this.level());
            extendedWorld.bossUpdate(this);
         }

         if (!this.minions.isEmpty()) {
            for (LivingEntity minion : this.minions.toArray(new LivingEntity[0])) {
               if (!minion.isAlive()) {
                  this.minions.remove(minion);
               }
            }
         }

         if (this.getMasterTarget() != null && this.getMasterTarget() instanceof BaseCreatureEntity) {
            ((BaseCreatureEntity)this.getMasterTarget()).onMinionUpdate(this, this.updateTick);
         }

         this.updateTick++;
      }
   }

   public void onSyncUpdate() {
      if (this.syncQueued) {
         this.doSync();
      }

      if (!this.level().isClientSide) {
         byte targets = 0;
         if (this.getTarget() != null) {
            targets += BaseCreatureEntity.TARGET_BITS.ATTACK.id;
         }

         if (this.getMasterTarget() != null) {
            targets += BaseCreatureEntity.TARGET_BITS.MASTER.id;
         }

         if (this.getParentTarget() != null) {
            targets += BaseCreatureEntity.TARGET_BITS.PARENT.id;
         }

         if (this.getAvoidTarget() != null) {
            targets += BaseCreatureEntity.TARGET_BITS.AVOID.id;
         }

         if (this.getControllingPassenger() != null) {
            targets += BaseCreatureEntity.TARGET_BITS.RIDER.id;
         }

         if (this.getPickupEntity() != null) {
            targets += BaseCreatureEntity.TARGET_BITS.PICKUP.id;
         }

         if (this.getPerchTarget() != null) {
            targets += BaseCreatureEntity.TARGET_BITS.PERCH.id;
         }

         this.entityData.set(TARGET, targets);
      }

      if (!this.level().isClientSide) {
         this.entityData.set(ATTACK_PHASE, this.attackPhase);
      }

      if (!this.level().isClientSide) {
         byte animations = 0;
         if (this.isAttackOnCooldown()) {
            animations += BaseCreatureEntity.ANIMATION_STATE_BITS.ATTACKED.id;
         }

         if (this.onGround()) {
            animations += BaseCreatureEntity.ANIMATION_STATE_BITS.GROUNDED.id;
         }

         if (this.wasTouchingWater) {
            animations += BaseCreatureEntity.ANIMATION_STATE_BITS.IN_WATER.id;
         }

         if (this.isBlocking()) {
            animations += BaseCreatureEntity.ANIMATION_STATE_BITS.BLOCKING.id;
         }

         if (this.isMinion()) {
            animations += BaseCreatureEntity.ANIMATION_STATE_BITS.MINION.id;
         }

         if (this.extraAnimation01()) {
            animations += BaseCreatureEntity.ANIMATION_STATE_BITS.EXTRA01.id;
         }

         if (this.spawnedAsBoss) {
            animations += BaseCreatureEntity.ANIMATION_STATE_BITS.BOSS.id;
         }

         this.entityData.set(ANIMATION_STATE, animations);
      } else if (this.level().isClientSide) {
         byte animationState = this.getByteFromDataManager(ANIMATION_STATE);
         if ((animationState & BaseCreatureEntity.ANIMATION_STATE_BITS.ATTACKED.id) > 0) {
            if (!this.isAttackOnCooldown()) {
               this.triggerAttackCooldown();
            }
         } else {
            this.resetAttackCooldown();
         }

         this.setOnGround((animationState & BaseCreatureEntity.ANIMATION_STATE_BITS.GROUNDED.id) > 0);
         this.wasTouchingWater = (animationState & BaseCreatureEntity.ANIMATION_STATE_BITS.IN_WATER.id) > 0;
         this.extraAnimation01 = (animationState & BaseCreatureEntity.ANIMATION_STATE_BITS.EXTRA01.id) > 0;
         this.spawnedAsBoss = (animationState & BaseCreatureEntity.ANIMATION_STATE_BITS.BOSS.id) > 0;
      }

      if (this.level().isClientSide) {
         this.isMinion = (this.getByteFromDataManager(ANIMATION_STATE) & BaseCreatureEntity.ANIMATION_STATE_BITS.MINION.id) > 0;
      }

      if (!this.level().isClientSide) {
         this.entityData.set(SUBSPECIES, (byte)this.getSubspeciesIndex());
      } else if (this.getSubspeciesIndex() != this.getByteFromDataManager(SUBSPECIES)) {
         this.setSubspecies(this.getByteFromDataManager(SUBSPECIES));
      }

      if (!this.level().isClientSide) {
         this.entityData.set(VARIANT, (byte)this.getVariantIndex());
      } else if (this.getVariantIndex() != this.getByteFromDataManager(VARIANT)) {
         this.applyVariant(this.getByteFromDataManager(VARIANT));
      }

      if (!this.level().isClientSide) {
         this.entityData.set(SIZE, (float)this.sizeScale);
      } else if (this.sizeScale != (double)this.getFloatFromDataManager(SIZE)) {
         this.setSizeScale((double)this.getFloatFromDataManager(SIZE));
      }

      if (!this.level().isClientSide) {
         this.entityData.set(ARENA, this.getArenaCenter() != null ? Optional.of(this.getArenaCenter()) : Optional.empty());
      }
   }

   public void updateHitAreas() {
      if (ENABLE_HITAREAS) {
         int hitAreaWidthCount = Math.max(1, Math.round(this.getBbWidth() * this.hitAreaWidthScale / 4.0F));
         int hitAreaHeightCount = Math.max(1, Math.round(this.getBbHeight() * this.hitAreaHeightScale / 4.0F));
         if (hitAreaWidthCount < 2 && hitAreaHeightCount < 2) {
            this.hitAreas = (EntityHitArea[][][])null;
         } else {
            if (this.hitAreas == null
               || this.hitAreas[0] == null
               || this.hitAreas[0][0] == null
               || this.hitAreas.length != hitAreaHeightCount
               || this.hitAreas[0].length != hitAreaWidthCount
               || this.hitAreas[0][0].length != hitAreaWidthCount) {
               this.hitAreas = new EntityHitArea[hitAreaHeightCount][hitAreaWidthCount][hitAreaWidthCount];
            }

            for (int y = 0; y < hitAreaHeightCount; y++) {
               for (int x = 0; x < hitAreaWidthCount; x++) {
                  for (int z = 0; z < hitAreaWidthCount; z++) {
                     if (y == 0 || y == hitAreaHeightCount - 1 || x == 0 || x == hitAreaWidthCount - 1 || z == 0 || z == hitAreaWidthCount - 1) {
                        if (this.hitAreas[y][x][z] == null) {
                           this.hitAreas[y][x][z] = new EntityHitArea(
                              this,
                              this.getBbWidth() / (float)hitAreaWidthCount * this.hitAreaWidthScale,
                              this.getBbHeight() / (float)hitAreaHeightCount * this.hitAreaHeightScale
                           );
                           this.level().addFreshEntity(this.hitAreas[y][x][z]);
                        }

                        // 1.12 sets posX/Y/Z directly (no bounding box update)
                        this.hitAreas[y][x][z].setPosRaw(this.getX()
                           - (double)(this.getBbWidth() * this.hitAreaWidthScale / 2.0F)
                           + (double)(this.getBbWidth() * this.hitAreaWidthScale / (float)hitAreaWidthCount / 2.0F)
                           + (double)(this.getBbWidth() * this.hitAreaWidthScale / (float)hitAreaWidthCount * (float)x), this.getY()
                           + (double)(this.getBbHeight() / (float)hitAreaHeightCount * this.hitAreaHeightScale * (float)y), this.getZ()
                           - (double)(this.getBbWidth() * this.hitAreaWidthScale / 2.0F)
                           + (double)(this.getBbWidth() * this.hitAreaWidthScale / (float)hitAreaWidthCount / 2.0F)
                           + (double)(this.getBbWidth() * this.hitAreaWidthScale / (float)hitAreaWidthCount * (float)z));
                        this.hitAreas[y][x][z].setYRot(this.getYRot());
                     }
                  }
               }
            }
         }
      }
   }

   /** 1.12 getCollisionBoundingBox: solid like a boat when solidCollision. */
   @Override
   public boolean canBeCollidedWith() {
      return this.solidCollision;
   }

   public AABB getRenderBoundingBox() {
      return this.getBoundingBox();
   }

   public float getBlockPathWeight(int par1, int par2, int par3) {
      if (this.creatureInfo.creatureSpawn.spawnsInDark && !this.creatureInfo.creatureSpawn.spawnsInLight) {
         return 0.5F - this.level().getLightLevelDependentMagicValue(LycPos.of(par1, par2, par3));
      } else {
         return this.creatureInfo.creatureSpawn.spawnsInLight && !this.creatureInfo.creatureSpawn.spawnsInDark
            ? this.level().getLightLevelDependentMagicValue(LycPos.of(par1, par2, par3)) - 0.5F
            : 0.0F;
      }
   }

   public boolean useDirectNavigator() {
      return false;
   }

   public boolean shouldSwim() {
      if (!this.isInWater() && !this.isInLava()) {
         return false;
      } else if (this.canWade() && this.canBreatheUnderwater112()) {
         boolean targetInWater = true;
         if (this.getTarget() != null) {
            targetInWater = this.getTarget().isInWater();
         } else if (this.getParentTarget() != null) {
            targetInWater = this.getParentTarget().isInWater();
         } else if (this.getMasterTarget() != null) {
            targetInWater = this.getMasterTarget().isInWater();
         }

         if (!targetInWater) {
            BlockState blockState = this.level().getBlockState(this.blockPosition().above());
            if (blockState.isAir()) {
               return false;
            }
         }

         return true;
      } else {
         return this.isStrongSwimmer();
      }
   }

   public boolean isInLava() {
      AABB boundingBox = this.getBoundingBox().inflate(-0.1F, -0.4F, -0.1F);
      if (this.getBbWidth() / 2.0F > 2.0F || this.getBbHeight() > 2.0F) {
         double radius = Math.min((double)this.getBbWidth() / 2.0, 2.0);
         double height = Math.min((double)this.getBbHeight(), 2.0);
         boundingBox = new AABB(
            this.getX() - radius,
            this.getY(),
            this.getZ() - radius,
            this.getX() + radius,
            this.getY() + height,
            this.getZ() + radius
         );
      }

      // 1.12 World.isMaterialInBB(box, LAVA)
      return this.level().getBlockStates(boundingBox).anyMatch(state -> Material.of(state) == Material.LAVA);
   }

   @Override
   public void move(MoverType type, Vec3 motion) {
      this.move(type, motion.x, motion.y, motion.z);
   }

   public void move(MoverType type, double x, double y, double z) {
      if (Double.isNaN(x)) {
         x = 0.0;
      }

      if (Double.isNaN(y)) {
         y = 0.0;
      }

      if (Double.isNaN(z)) {
         z = 0.0;
      }

      x = Math.max(Math.min(x, 10.0), -10.0);
      y = Math.max(Math.min(y, 10.0), -10.0);
      z = Math.max(Math.min(z, 10.0), -10.0);
      super.move(type, new Vec3(x, y, z));
   }

   @Override
   public void travel(Vec3 input) {
      this.travel((float)input.x, (float)input.y, (float)input.z);
   }

   public void travel(float strafe, float up, float forward) {
      if (!this.useDirectNavigator()) {
         if (this.isFlying() && !this.isInWater() && !this.isInLava()) {
            this.moveFlyingWithHeading(strafe, forward);
            this.updateLimbSwing();
         } else if (this.shouldSwim()) {
            this.moveSwimmingWithHeading(strafe, forward);
            this.updateLimbSwing();
         } else {
            super.travel(new Vec3(strafe, up, forward));
         }
      } else {
         this.directNavigator.flightMovement(strafe, forward);
         this.updateLimbSwing();
      }
   }

   public void updateLimbSwing() {
      double distanceX = this.getX() - this.xo;
      double distanceZ = this.getZ() - this.zo;
      float distance = (float)Math.sqrt(distanceX * distanceX + distanceZ * distanceZ) * 4.0F;
      if (distance > 1.0F) {
         distance = 1.0F;
      }

      // 1.12 prevLimbSwingAmount/limbSwingAmount/limbSwing = 1.21 WalkAnimationState.update
      this.walkAnimation.update(distance, 0.4F);
   }

   public void moveFlyingWithHeading(float strafe, float forward) {
      float f = 0.91F;
      if (this.onGround()) {
         f = this.level()
               .getBlockState(
                  LycPos.of(
                     Mth.floor(this.getX()),
                     Mth.floor(this.getBoundingBox().minY) - 1,
                     Mth.floor(this.getZ())
                  )
               )
               .getBlock()
               .getFriction()
            * 0.91F;
      }

      float f1 = 0.16277136F / (f * f * f);
      this.moveFlying(strafe, forward, this.onGround() ? 0.1F * f1 : 0.02F);
      f = 0.91F;
      if (this.onGround()) {
         f = this.level()
               .getBlockState(
                  LycPos.of(
                     Mth.floor(this.getX()),
                     Mth.floor(this.getBoundingBox().minY) - 1,
                     Mth.floor(this.getZ())
                  )
               )
               .getBlock()
               .getFriction()
            * 0.91F;
      }

      this.move(MoverType.SELF, this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z);
      LycMotion.mulX(this, (double)f);
      LycMotion.mulY(this, (double)f);
      LycMotion.mulZ(this, (double)f);
      double d1 = this.getX() - this.xo;
      double d0 = this.getZ() - this.zo;
      float f2 = (float)Math.sqrt(d1 * d1 + d0 * d0) * 4.0F;
      if (f2 > 1.0F) {
         f2 = 1.0F;
      }

      this.walkAnimation.update(f2, 0.4F);
   }

   /** 1.12 Lycanites moveFlying: it computes a friction and never applies it (checked in the 1.12 bytecode), kept as is. */
   public void moveFlying(float strafe, float forward, float friction) {
      if (!this.isPushedByFluid() && this.canWalk() && this.isInWater()) {
         float sliperryness = 0.91F;
         BlockPos blockpos$pooledmutableblockpos = LycPos.of(
            this.getX(), this.getBoundingBox().minY - 1.0, this.getZ()
         );
         if (this.onGround()) {
            sliperryness = this.level().getBlockState(blockpos$pooledmutableblockpos).getBlock().getFriction() * sliperryness * 2.0F;
         }

         float f7 = 0.16277136F / (sliperryness * sliperryness * sliperryness);
         friction = this.getSpeed() * f7;
      }
   }

   public void moveSwimmingWithHeading(float strafe, float forward) {
      super.moveRelative(0.1F, new Vec3(strafe, 0.0F, forward));
      this.move(MoverType.SELF, this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z);
      LycMotion.mulX(this, 0.9F);
      LycMotion.mulY(this, 0.9F);
      LycMotion.mulZ(this, 0.9F);
   }

   protected PathNavigation createNavigator(Level world) {
      return new CreaturePathNavigate(this, world);
   }

   protected MoveControl createMoveHelper() {
      return new CreatureMoveHelper(this);
   }

   public PathNavigation getNavigation() {
      return super.getNavigation();
   }

   public MoveControl getMoveControl() {
      return super.getMoveControl();
   }

   public void clearMovement() {
      if (!this.useDirectNavigator() && this.getNavigation() != null) {
         this.getNavigation().stop();
      } else {
         this.directNavigator.clearTargetPosition(1.0);
      }
   }

   public boolean rollLookChance() {
      return this.getRandom().nextFloat() < 0.02F;
   }

   public boolean rollWanderChance() {
      return this.getBbWidth() >= 3.0F ? this.getRandom().nextDouble() <= 5.0E-4 : this.getRandom().nextDouble() <= 0.008;
   }

   /** 1.12 EntityLiving.updateLeashedState override (1.21 Entity.baseTick runs the vanilla leash part, Leashable.tickLeash). */
   protected void updateLeashedState() {
      if (this.isLeashed() && this.getLeashHolder() != null && this.getLeashHolder().level() == this.level()) {
         Entity entity = this.getLeashHolder();
         this.setHome((int)entity.getX(), (int)entity.getY(), (int)entity.getZ(), 5.0F);
         float distance = this.distanceTo(entity);
         this.testLeash(distance);
         if (!this.leashAIActive) {
            this.tasks.addTask(2, this.leashMoveTowardsRestrictionAI);
            if (!this.isStrongSwimmer()) {
               this.setPathfindingMalus(PathType.WATER, 0.0F);
            }

            this.leashAIActive = true;
         }

         if (distance > 4.0F) {
            this.getNavigation().moveTo(entity, 1.0);
         }

         if (distance > 6.0F) {
            double d0 = (entity.getX() - this.getX()) / (double)distance;
            double d1 = (entity.getY() - this.getY()) / (double)distance;
            double d2 = (entity.getZ() - this.getZ()) / (double)distance;
            LycMotion.setX(this, this.getDeltaMovement().x + d0 * Math.abs(d0) * 0.4);
            LycMotion.setY(this, this.getDeltaMovement().y + d1 * Math.abs(d1) * 0.4);
            LycMotion.setZ(this, this.getDeltaMovement().z + d2 * Math.abs(d2) * 0.4);
         }

         if (distance > 10.0F) {
            this.dropLeash(true, true);
         }
      } else if (!this.isLeashed() && this.leashAIActive) {
         this.leashAIActive = false;
         this.tasks.removeTask(this.leashMoveTowardsRestrictionAI);
         if (!this.isStrongSwimmer()) {
            this.setPathfindingMalus(PathType.WATER, PathType.WATER.getMalus());
         }

         this.detachHome();
      }
   }

   public boolean isPushedByFluid() {
      return !this.isStrongSwimmer() && !this.isBoss();
   }

   public boolean isMoving() {
      return !this.useDirectNavigator() ? this.getNavigation().getPath() != null : !this.directNavigator.atTargetPosition();
   }

   public boolean canBePushed() {
      return super.isPushable();
   }

   public boolean canBeLeashedTo(Player player) {
      return false;
   }

   public void testLeash(float distance) {
   }

   public void setAIMoveSpeed(float speed) {
      super.setSpeed(speed * this.getAISpeedModifier());
   }

   public float getAISpeedModifier() {
      return !this.canWalk() && !this.isInWater() && !this.isFlying() ? 0.1F : 1.0F;
   }

   public double getFallingMod() {
      return 1.0;
   }

   protected float getWaterSlowDown() {
      return !this.isPushedByFluid() ? 1.0F : 0.8F;
   }

   public void leap(double distance, double leapHeight) {
      if (!this.isFlying()) {
         this.playJumpSound();
      }

      float yaw = this.getYRot();
      float pitch = this.getXRot();
      double angle = Math.toRadians((double)yaw);
      double xAmount = -Math.sin(angle);
      double yAmount = leapHeight;
      double zAmount = Math.cos(angle);
      if (this.isFlying()) {
         yAmount = Math.sin(Math.toRadians((double)pitch)) * distance + this.getDeltaMovement().y * 0.2;
      }

      this.push(xAmount * distance + this.getDeltaMovement().x * 0.2, yAmount, zAmount * distance + this.getDeltaMovement().z * 0.2);
      CommonHooks.onLivingJump(this);
   }

   public void leap(float range, double leapHeight, Entity target) {
      if (target != null) {
         this.leap(range, leapHeight, target.blockPosition());
      }
   }

   public void leap(float range, double leapHeight, BlockPos targetPos) {
      if (targetPos != null) {
         if (!this.isFlying()) {
            this.playJumpSound();
         }

         double distance = targetPos.distSqr(this.blockPosition());
         if (distance > 4.0 && distance <= (double)(range * range)) {
            double xDist = (double)(targetPos.getX() - this.blockPosition().getX());
            double zDist = (double)(targetPos.getZ() - this.blockPosition().getZ());
            if (xDist == 0.0) {
               xDist = 0.05;
            }

            if (zDist == 0.0) {
               zDist = 0.05;
            }

            double xzDist = (double)(float)Math.sqrt(xDist * xDist + zDist * zDist);
            this.push(xDist / xzDist * 0.5 * 0.8 + this.getDeltaMovement().x * 0.2, leapHeight, zDist / xzDist * 0.5 * 0.8 + this.getDeltaMovement().z * 0.2);
            CommonHooks.onLivingJump(this);
         }
      }
   }

   public void strafe(double distance, double leapHeight) {
      boolean opposite = false;
      if (distance < 0.0) {
         distance = -distance;
         opposite = true;
      }

      float yaw = this.getYRot() + (opposite ? -90.0F : 90.0F);
      float pitch = this.getXRot();
      double angle = Math.toRadians((double)yaw);
      double xAmount = -Math.sin(angle);
      double yAmount = leapHeight;
      double zAmount = Math.cos(angle);
      if (this.isFlying()) {
         yAmount = Math.sin(Math.toRadians((double)pitch)) * distance + this.getDeltaMovement().y * 0.2;
      }

      this.push(xAmount * distance + this.getDeltaMovement().x * 0.2, yAmount, zAmount * distance + this.getDeltaMovement().z * 0.2);
   }

   public boolean isLookingAtMe(Entity targetEntity) {
      if (targetEntity == null) {
         return false;
      } else {
         Vec3 targetViewVector = targetEntity.getViewVector(1.0F).normalize();
         Vec3 distance = new Vec3(
            this.getX() - targetEntity.getX(),
            this.getY() + (double)this.getEyeHeight() - (targetEntity.getY() + (double)targetEntity.getEyeHeight()),
            this.getZ() - targetEntity.getZ()
         );
         double distanceStraight = distance.length();
         distance = distance.normalize();
         double lookDistance = targetViewVector.dot(distance);
         double lookRange = 1.5;
         double comparison = 1.0 - lookRange / distanceStraight;
         return !(targetEntity instanceof Player)
            ? lookDistance > comparison
            : lookDistance > comparison && ((Player)targetEntity).hasLineOfSight(this);
      }
   }

   public void setHome(int x, int y, int z, float distance) {
      this.setHomePosition(x, y, z);
      this.setHomeDistanceMax(distance);
   }

   public void setHomePosition(int x, int y, int z) {
      this.homePosition = LycPos.of(x, y, z);
   }

   public void setHomeDistanceMax(float distance) {
      this.homeDistanceMax = distance;
   }

   public BlockPos getHomePosition() {
      return this.homePosition;
   }

   public float getHomeDistanceMax() {
      return this.homeDistanceMax;
   }

   public void detachHome() {
      this.setHomeDistanceMax(-1.0F);
   }

   public boolean hasHome() {
      return this.getHomePosition() != null && this.getHomeDistanceMax() >= 0.0F;
   }

   public boolean positionNearHome(int x, int y, int z) {
      return !this.hasHome() ? true : this.getDistanceFromHome(x, y, z) < (double)(this.getHomeDistanceMax() * this.getHomeDistanceMax());
   }

   public double getDistanceFromHome(int x, int y, int z) {
      return !this.hasHome() ? 0.0 : Math.sqrt(this.homePosition.distToLowCornerSqr(x, y, z));
   }

   public double getDistanceFromHome() {
      return Math.sqrt(this.homePosition.distToLowCornerSqr((int)this.getX(), (int)this.getY(), (int)this.getZ()));
   }

   public boolean hasArenaCenter() {
      return this.getArenaCenter() != null;
   }

   public void setArenaCenter(BlockPos pos) {
      this.arenaCenter = pos;
   }

   public BlockPos getArenaCenter() {
      return this.arenaCenter;
   }

   public BlockPos getWanderPosition(BlockPos wanderPosition) {
      return wanderPosition;
   }

   public int restrictYHeightFromGround(BlockPos coords, int minY, int maxY) {
      int groundY = this.getGroundY(coords);
      int airYMax = Math.min(this.getAirY(coords), groundY + maxY);
      int airYMin = Math.min(airYMax, groundY + minY);
      return airYMin >= airYMax ? airYMin : airYMin + this.getRandom().nextInt(airYMax - airYMin);
   }

   public int getGroundY(BlockPos pos) {
      int y = pos.getY();
      if (y <= 0) {
         return 0;
      } else {
         BlockState startBlock = this.level().getBlockState(pos);
         if (startBlock.isAir()) {
            for (int possibleGroundY = Math.max(0, y - 1); possibleGroundY >= 0; y = possibleGroundY--) {
               BlockState possibleGroundBlock = this.level().getBlockState(LycPos.of(pos.getX(), possibleGroundY, pos.getZ()));
               if (!possibleGroundBlock.isAir()) {
                  break;
               }
            }
         }

         return y;
      }
   }

   public int getAirY(BlockPos pos) {
      int y = pos.getY();
      int yMax = this.level().getMaxBuildHeight() - 1;
      if (y >= yMax) {
         return yMax;
      } else if (this.level().canSeeSky(pos)) {
         return yMax;
      } else {
         BlockState startBlock = this.level().getBlockState(pos);
         if (startBlock.isAir()) {
            for (int possibleAirY = Math.min(yMax, y + 1); possibleAirY <= yMax; y = possibleAirY++) {
               BlockState possibleGroundBlock = this.level().getBlockState(LycPos.of(pos.getX(), possibleAirY, pos.getZ()));
               if (!possibleGroundBlock.isAir()) {
                  break;
               }
            }
         }

         return y;
      }
   }

   public int getWaterSurfaceY(BlockPos pos) {
      int y = pos.getY();
      if (y <= 0) {
         return 0;
      } else {
         int yMax = this.level().getMaxBuildHeight() - 1;
         if (y >= yMax) {
            return yMax;
         } else {
            int yLimit = 24;
            yMax = Math.min(yMax, y + yLimit);
            BlockState startBlock = this.level().getBlockState(pos);
            if (Material.of(startBlock) == Material.WATER) {
               int possibleSurfaceY;
               for (possibleSurfaceY = y + 1; possibleSurfaceY <= yMax; possibleSurfaceY++) {
                  BlockState possibleSurfaceBlock = this.level()
                     .getBlockState(LycPos.of(pos.getX(), possibleSurfaceY, pos.getZ()));
                  if (possibleSurfaceBlock.isAir()) {
                     return possibleSurfaceY;
                  }

                  if (Material.of(possibleSurfaceBlock) != Material.WATER) {
                     return possibleSurfaceY - 1;
                  }
               }

               return Math.max(possibleSurfaceY - 1, y);
            } else {
               return y;
            }
         }
      }
   }

   protected void setSize(float width, float height) {
      width = (float)((double)width * (double)((float)this.sizeScale) * this.creatureInfo.sizeScale);
      height = (float)((double)height * (double)((float)this.sizeScale) * this.creatureInfo.sizeScale);
      // 1.12 Entity.setSize
      if (width != this.legacyWidth || height != this.legacyHeight) {
         this.legacyWidth = width;
         this.legacyHeight = height;
         this.refreshDimensions();
      }
      this.hitAreas = (EntityHitArea[][][])null;
      if (!this.level().isClientSide && this.getNavigation() != null && this.getNavigation().getNodeEvaluator() instanceof ICreatureNodeProcessor) {
         ((ICreatureNodeProcessor)this.getNavigation().getNodeEvaluator()).updateEntitySize(this);
      }
   }

   public void updateSize() {
      this.setSize(Math.max(this.setWidth, 0.5F), Math.max(this.setHeight, 0.5F));
   }

   public void setSizeScale(double scale) {
      this.sizeScale = scale;
      this.updateSize();
   }

   public double getRenderScale() {
      return this.sizeScale * this.creatureInfo.sizeScale;
   }

   public boolean canAttackClass(Class targetClass) {
      return CreatureManager.getInstance().config.mobsAttackVillagers || targetClass != Villager.class;
   }

   public boolean canAttackEntity(LivingEntity targetEntity) {
      if (!CreatureManager.getInstance().config.mobsAttackVillagers && targetEntity instanceof Villager) {
         return false;
      } else if (this.level().getDifficulty() == Difficulty.PEACEFUL && targetEntity instanceof Player) {
         return false;
      } else if (!Targeting.isValidTarget(this, targetEntity)) {
         return false;
      } else {
         if (targetEntity instanceof Player) {
            Player targetPlayer = (Player)targetEntity;
            if (targetPlayer.getAbilities().invulnerable) {
               return false;
            }
         }

         if (this.isAlliedTo(targetEntity)) {
            return false;
         } else {
            CreatureRelationshipEntry relationshipEntry = this.relationships.getEntry(targetEntity);
            if (relationshipEntry != null && !relationshipEntry.canAttack()) {
               return false;
            } else {
               if (targetEntity instanceof BaseCreatureEntity) {
                  BaseCreatureEntity targetCreature = (BaseCreatureEntity)targetEntity;
                  if (!this.canAttackOwnSpecies() && targetCreature.getClass() == this.getClass() && !targetCreature.isTamed()) {
                     return false;
                  }

                  if (targetCreature.getMasterTarget() == this) {
                     return false;
                  }

                  if (!(this instanceof IGroupBoss) && !this.isTamed()) {
                     if (targetCreature.isBoss()) {
                        return false;
                     }

                     if (this.isRareVariant()) {
                        return false;
                     }
                  }
               }

               return true;
            }
         }
      }
   }

   public boolean canAttackOwnSpecies() {
      return false;
   }

   public double getPhysicalRange() {
      double range = (double)this.getBbWidth() + 1.5;
      if (this.isFlying()) {
         range += this.getFlightOffset();
      }

      return range * range;
   }

   public double getMeleeAttackRange(LivingEntity attackTarget, double additionalReach) {
      double creatureRange = this.getPhysicalRange();
      double targetSize = 1.0;
      if (attackTarget != null) {
         targetSize = (double)((attackTarget.getBbWidth() + 1.0F) * (attackTarget.getBbWidth() + 1.0F));
      }

      return creatureRange + targetSize + additionalReach;
   }

   public boolean shouldCreatureGroupRevenge(LivingEntity target) {
      boolean shouldRevenge = this.creatureInfo.getGroups().isEmpty();
      boolean shouldPackHunt = false;

      for (CreatureGroup group : this.creatureInfo.getGroups()) {
         if (group.shouldRevenge(target)) {
            shouldRevenge = true;
         }

         if (group.shouldPackHunt(target)) {
            shouldPackHunt = true;
         }
      }

      boolean canPackHunt = shouldPackHunt && this.isInPack();
      return shouldRevenge || canPackHunt;
   }

   public boolean shouldCreatureGroupHunt(LivingEntity target) {
      boolean shouldFlee = false;
      boolean shouldHunt = false;
      boolean shouldPackHunt = false;

      for (CreatureGroup group : this.creatureInfo.getGroups()) {
         if (group.shouldFlee(target)) {
            shouldFlee = true;
         }

         if (group.shouldHunt(target)) {
            shouldHunt = true;
         }

         if (group.shouldPackHunt(target)) {
            shouldPackHunt = true;
         }
      }

      boolean canPackHunt = shouldPackHunt && this.isInPack();
      return shouldFlee && !canPackHunt ? false : shouldHunt || shouldPackHunt;
   }

   public boolean shouldCreatureGroupFlee(LivingEntity target) {
      if (!this.isBoss() && !this.isRareVariant() && !this.isTamed()) {
         boolean shouldFlee = false;
         boolean shouldPackHunt = false;

         for (CreatureGroup group : this.creatureInfo.getGroups()) {
            if (group.shouldFlee(target)) {
               shouldFlee = true;
            }

            if (group.shouldPackHunt(target)) {
               shouldPackHunt = true;
            }
         }

         boolean canPackHunt = shouldPackHunt && this.isInPack();
         return shouldFlee && !canPackHunt;
      } else {
         return false;
      }
   }

   public LivingEntity getMasterAttackTarget() {
      if (this.masterTarget == null) {
         return null;
      } else {
         return this.masterTarget instanceof Mob ? ((Mob)this.masterTarget).getTarget() : null;
      }
   }

   public LivingEntity getParentAttackTarget() {
      if (this.parentTarget == null) {
         return null;
      } else if (this.parentTarget instanceof BaseCreatureEntity) {
         return ((BaseCreatureEntity)this.parentTarget).getTarget();
      } else {
         return this.parentTarget instanceof PathfinderMob ? ((PathfinderMob)this.parentTarget).getTarget() : null;
      }
   }

   public void setLastHurtByMob(LivingEntity entityLivingBase) {
      super.setLastHurtByMob(entityLivingBase);
   }

   public boolean attackMelee(Entity target, double damageScale) {
      if (this.isBlocking() && !this.canAttackWhileBlocking()) {
         return false;
      } else if (!this.attackEntityAsMob(target, damageScale)) {
         return false;
      } else {
         if (target instanceof LivingEntity) {
            LivingEntity livingTarget = (LivingEntity)target;
            if (!livingTarget.isBlocking() || !livingTarget.getUseItem().canPerformAction(net.neoforged.neoforge.common.ItemAbilities.SHIELD_BLOCK)) {
               if (this.spreadFire && this.isOnFire() && (double)this.random.nextFloat() < this.creatureStats.getEffect()) {
                  target.igniteForSeconds(this.getEffectDuration(4) / 20);
               }

               if (this.creatureStats.getAmplifier() >= 0.0) {
                  this.applyDebuffs(livingTarget, 1, 1);
               }
            }
         }

         this.triggerAttackCooldown();
         this.playAttackSound();
         return true;
      }
   }

   public boolean attackHitscan(Entity target, double damageScale) {
      if (this.isBlocking() && !this.canAttackWhileBlocking()) {
         return false;
      } else if (target == null || !this.hasLineOfSight(target)) {
         return false;
      } else if (!this.attackEntityAsMob(target, damageScale)) {
         return false;
      } else {
         if (target instanceof LivingEntity) {
            LivingEntity livingTarget = (LivingEntity)target;
            if ((!livingTarget.isBlocking() || !livingTarget.getUseItem().canPerformAction(net.neoforged.neoforge.common.ItemAbilities.SHIELD_BLOCK))
               && this.creatureStats.getAmplifier() >= 0.0) {
               this.applyDebuffs(livingTarget, 1, 1);
            }
         }

         this.triggerAttackCooldown();
         this.playAttackSound();
         return true;
      }
   }

   public void attackRanged(Entity target, float range) {
      if (!this.isBlocking() || this.canAttackWhileBlocking()) {
         this.triggerAttackCooldown();
         this.playAttackSound();
      }
   }

   public boolean doRangedDamage(Entity target, ThrowableProjectile projectile, float damage, boolean noPierce) {
      damage = (float)((double)damage * (this.creatureStats.getDamage() / 2.0));
      double pierceDamage = noPierce ? 0.0 : this.creatureStats.getPierce();
      boolean success;
      if ((double)damage <= pierceDamage) {
         success = target.hurt(
            this.getDamageSource((EntityDamageSource)EntityDamageSource.causeThrownDamage(projectile, this).setDamageBypassesArmor(), false).setDamageIsAbsolute(), damage
         );
      } else {
         int hurtResistantTimeBefore = target.invulnerableTime;
         if (pierceDamage > 0.0) {
            target.hurt(
               this.getDamageSource((EntityDamageSource)EntityDamageSource.causeThrownDamage(projectile, this).setDamageBypassesArmor(), false).setDamageIsAbsolute(), (float)pierceDamage
            );
         }

         target.invulnerableTime = hurtResistantTimeBefore;
         damage = (float)((double)damage - pierceDamage);
         success = target.hurt(this.getDamageSource((EntityDamageSource)EntityDamageSource.causeThrownDamage(projectile, this), false), damage);
      }

      if (success && target instanceof LivingEntity && this.creatureStats.getAmplifier() >= 0.0) {
         this.applyDebuffs((LivingEntity)target, 1, 1);
      }

      return success;
   }

   public BaseProjectileEntity fireProjectile(
      String projectileName, Entity target, float range, float angle, Vec3 offset, float velocity, float scale, float inaccuracy
   ) {
      ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile(projectileName);
      return projectileInfo == null
         ? null
         : this.fireProjectile(projectileInfo.createProjectile(this.level(), this), target, range, angle, offset, velocity, scale, inaccuracy);
   }

   public BaseProjectileEntity fireProjectile(
      Class projectileClass, Entity target, float range, float angle, Vec3 offset, float velocity, float scale, float inaccuracy
   ) {
      BaseProjectileEntity projectile = null;

      try {
         projectile = (BaseProjectileEntity)projectileClass.getConstructor(Level.class, LivingEntity.class).newInstance(this.level(), this);
      } catch (Exception var11) {
         LycanitesMobs.logWarning("", "Unable to create a projectile from the class: " + projectileClass);
      }

      return this.fireProjectile(projectile, target, range, angle, offset, velocity, scale, inaccuracy);
   }

   public BaseProjectileEntity fireProjectile(
      BaseProjectileEntity projectile, Entity target, float range, float angle, Vec3 offset, float velocity, float scale, float inaccuracy
   ) {
      if (projectile == null) {
         return null;
      } else {
         LycMotion.setPosX(projectile, projectile.getX() + offset.x * this.sizeScale);
         LycMotion.setPosY(projectile, projectile.getY() - ((double)(this.getBbHeight() / 4.0F) + offset.y * this.sizeScale));
         LycMotion.setPosZ(projectile, projectile.getZ() + offset.z * this.sizeScale);
         projectile.setProjectileScale(scale);
         Vec3 facing = this.getFacingPositionDouble(this.getX(), this.getY(), this.getZ(), (double)range, (double)angle);
         double distanceX = facing.x - this.getX();
         double distanceZ = facing.z - this.getZ();
         double distanceXZ = (double)(float)Math.sqrt(distanceX * distanceX + distanceZ * distanceZ) * 0.0;
         double distanceY = distanceXZ;
         if (target != null) {
            double targetX = target.getX() - this.getX();
            double targetZ = target.getZ() - this.getZ();
            double newX = targetX * Math.cos((double)angle) - targetZ * Math.sin((double)angle);
            double newY = targetX * Math.sin((double)angle) + targetZ * Math.cos((double)angle);
            targetX = newX + this.getX();
            targetZ = newY + this.getZ();
            distanceX = targetX - this.getX();
            double var10001 = (double)target.getBbHeight();
            distanceY = target.getBoundingBox().minY + var10001 * 0.5 - projectile.getY();
            distanceZ = targetZ - this.getZ();
         }

         projectile.shoot(distanceX, distanceY, distanceZ, velocity, inaccuracy);
         this.level().addFreshEntity(projectile);
         if (projectile.getLaunchSound() != null) {
            this.playSound(projectile.getLaunchSound(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
         }

         return projectile;
      }
   }

   public byte getAttackPhase() {
      return this.getByteFromDataManager(ATTACK_PHASE);
   }

   public void setAttackPhase(byte setAttackPhase) {
      this.attackPhase = setAttackPhase;
   }

   public void nextAttackPhase() {
      if (++this.attackPhase > this.attackPhaseMax - 1) {
         this.attackPhase = 0;
      }
   }

   public boolean attackEntityAsMob(Entity target, double damageScale) {
      if (!this.isAlive()) {
         return false;
      } else if (target == null) {
         return false;
      } else if (!this.hasLineOfSight(target)) {
         return false;
      } else {
         float damage = this.getAttackDamage(damageScale);
         int enchantmentKnockback = 0;
         boolean targetIsShielding = false;
         if (target instanceof Player) {
            Player targetPlayer = (Player)target;
            ItemStack playerActiveItemStack = targetPlayer.getUseItem();
            targetIsShielding = targetPlayer.isBlocking() && playerActiveItemStack.canPerformAction(net.neoforged.neoforge.common.ItemAbilities.SHIELD_BLOCK);
         }

         double pierceDamage = this.creatureStats.getPierce();
         if (targetIsShielding) {
            pierceDamage = 0.0;
         }

         boolean attackSuccess;
         if ((double)damage <= pierceDamage) {
            attackSuccess = target.hurt(this.getDamageSource(null, false).setDamageBypassesArmor().setDamageIsAbsolute(), damage);
         } else {
            if (pierceDamage > 0.0) {
               int hurtResistantTimeBefore = target.invulnerableTime;
               target.hurt(this.getDamageSource(null, false).setDamageBypassesArmor().setDamageIsAbsolute(), (float)pierceDamage);
               target.invulnerableTime = hurtResistantTimeBefore;
               damage = (float)((double)damage - pierceDamage);
            }

            attackSuccess = target.hurt(this.getDamageSource(null, false), damage);
         }

         if (attackSuccess) {
            this.setLastHurtMob(target);
            if (enchantmentKnockback > 0) {
               target.push(
                  (double)(-Mth.sin(this.getYRot() * (float) Math.PI / 180.0F) * (float)enchantmentKnockback * 0.5F),
                  0.1,
                  (double)(Mth.cos(this.getYRot() * (float) Math.PI / 180.0F) * (float)enchantmentKnockback * 0.5F)
               );
               LycMotion.mulX(this, 0.6);
               LycMotion.mulZ(this, 0.6);
            }

            // 1.12 EnchantmentHelper.getFireAspectModifier(this): 1.21 applies fire aspect in doPostAttackEffects below

            if (target instanceof Player && this.canInteruptShields(true)) {
               Player targetPlayer = (Player)target;
               if (targetIsShielding && this.canInteruptShields(false)) {
                  ItemStack playerActiveItemStack = targetPlayer.isUsingItem() ? targetPlayer.getUseItem() : ItemStack.EMPTY;
                  targetPlayer.getCooldowns().addCooldown(playerActiveItemStack.getItem(), 100);
                  this.level().broadcastEntityEvent(targetPlayer, (byte)30);
               }
            }

            // 1.12 applyEnchantments(this, target)
            if (this.level() instanceof ServerLevel serverLevel) {
               net.minecraft.world.item.enchantment.EnchantmentHelper.doPostAttackEffects(serverLevel, target, this.getDamageSource(null, false));
            }
         }

         return attackSuccess;
      }
   }

   public boolean canTargetBlockDamageSource(LivingEntity target, DamageSource damageSource) {
      if (!damageSource.is(DamageTypeTags.BYPASSES_ARMOR) && target.isBlocking()) {
         Vec3 vec3d = damageSource.getSourcePosition();
         if (vec3d != null) {
            Vec3 vec3d1 = target.getViewVector(1.0F);
            Vec3 vec3d2 = new Vec3(target.getX(), target.getY(), target.getZ()).subtract(vec3d).normalize();
            vec3d2 = new Vec3(vec3d2.x, 0.0, vec3d2.z);
            if (vec3d2.dot(vec3d1) < 0.0) {
               return true;
            }
         }
      }

      return false;
   }

   public float getAttackDamage(double damageScale) {
      float damage = (float)this.getAttribute(Attributes.ATTACK_DAMAGE).getValue();
      return (float)((double)damage * damageScale);
   }

   public EntityDamageSource getDamageSource(EntityDamageSource nestedDamageSource, boolean playerCredit) {
      return (EntityDamageSource)(nestedDamageSource != null ? nestedDamageSource : EntityDamageSource.causeMobDamage(this));
   }

   public boolean canInteruptShields(boolean checkAbility) {
      return false;
   }

   /** 1.12 BaseCreatureEntity.damageEntity (1.21 LivingEntity.actuallyHurt; the Forge LivingHurtEvent already ran in hurt). */
   @Override
   protected void actuallyHurt(DamageSource damageSrc, float damageAmount) {
      if (!this.isInvulnerableTo(damageSrc)) {
         if (!(damageAmount <= 0.0F)) {
            damageAmount *= this.getDamageModifier(damageSrc);
            damageAmount = this.getDamageAfterArmorAbsorb(damageSrc, damageAmount);
            damageAmount = this.getDamageAfterMagicAbsorb(damageSrc, damageAmount);
            damageAmount = this.getDamageAfterDefense(damageAmount);
            if ((this.isBoss() || this.isRareVariant()) && !(damageSrc.getEntity() instanceof Player)) {
               damageAmount *= 0.25F;
            }

            float var10 = Math.max(damageAmount - this.getAbsorptionAmount(), 0.0F);
            this.setAbsorptionAmount(this.getAbsorptionAmount() - (damageAmount - var10));
            damageAmount = var10;
            if (damageAmount != 0.0F) {
               float healthBeforeDamage = this.getHealth();
               this.getCombatTracker().recordDamage(damageSrc, damageAmount);
               this.setHealth(healthBeforeDamage - damageAmount);
               this.setAbsorptionAmount(this.getAbsorptionAmount() - damageAmount);
            }
         }
      }
   }

   public boolean hurt(DamageSource damageSrc, float damageAmount) {
      if (this.level().isClientSide) {
         return false;
      } else if (this.isInvulnerableTo(damageSrc)) {
         return false;
      } else if (!this.isDamageTypeApplicable(damageSrc.getMsgId(), damageSrc, damageAmount)) {
         return false;
      } else if (!this.isDamageEntityApplicable(damageSrc.getEntity())) {
         return false;
      } else {
         if (this.dropsRequirePlayerDamage && damageSrc.getEntity() instanceof Player) {
            if (damageSrc instanceof MinionEntityDamageSource) {
               MinionEntityDamageSource minionEntityDamageSource = (MinionEntityDamageSource)damageSrc;
               if (minionEntityDamageSource.getMinion() instanceof TameableCreatureEntity) {
                  TameableCreatureEntity tameableCreatureEntity = (TameableCreatureEntity)minionEntityDamageSource.getMinion();
                  if (!tameableCreatureEntity.fromSummoningPedestal) {
                     this.dropsRequirePlayerDamage = false;
                  }
               }
            } else {
               this.dropsRequirePlayerDamage = false;
            }
         }

         if (super.hurt(damageSrc, damageAmount)) {
            this.onDamage(damageSrc, damageAmount);
            if (this.isBoss()
               && this.playerTargets != null
               && damageSrc.getEntity() != null
               && damageSrc.getEntity() instanceof Player
               && !this.playerTargets.contains(damageSrc.getEntity())) {
               this.playerTargets.add((Player)damageSrc.getEntity());
            }

            Entity entity = damageSrc.getDirectEntity();
            if (entity instanceof ThrowableProjectile) {
               entity = ((ThrowableProjectile)entity).getOwner();
            }

            if (entity instanceof LivingEntity && this.getRider() != entity && this.getVehicle() != entity && entity != this) {
               this.setLastHurtByMob((LivingEntity)entity);
               CreatureRelationshipEntry relationshipEntry = this.relationships.getOrCreateEntry(entity);
               int reputationAmount = 50 + this.getRandom().nextInt(50);
               relationshipEntry.decreaseReputation(reputationAmount);
            }

            return true;
         } else {
            return false;
         }
      }
   }

   public boolean attackEntityFromArea(EntityHitArea entityHitArea, DamageSource damageSrc, float damage) {
      return this.hurt(damageSrc, damage);
   }

   public float getDamageAfterDefense(float damage) {
      float defense = (float)this.creatureStats.getDefense();
      float minDamage = 0.0F;
      if (this.isBlocking()) {
         if (defense <= 0.0F) {
            defense = 1.0F;
         }

         defense *= (float)this.getBlockingMultiplier();
      }

      damage = Math.max(damage - defense, 1.0F);
      if (this.damageMax > 0) {
         damage = Math.min(damage, (float)this.damageMax);
      }

      return Math.max(damage, minDamage);
   }

   public void onDamage(DamageSource damageSrc, float damage) {
      this.damageTakenThisSec += damage;
   }

   public float getDamageModifier(DamageSource damageSrc) {
      return 1.0F;
   }

   public void die(DamageSource damageSource) {
      super.die(damageSource);
      if (this.dead) {
         if (this.isBossAlways()) {
            ExtendedWorld extendedWorld = ExtendedWorld.getForWorld(this.level());
            extendedWorld.bossRemoved(this);
         }

         if (!this.level().isClientSide) {
            if (!this.isBoundPet()) {
               this.inventory.dropInventory();
            }

            if (damageSource.getEntity() != null && damageSource.getEntity() instanceof Player) {
               try {
                  Player player = (Player)damageSource.getEntity();
                  // 1.12 player.addStat(<creature>.kill): Lycanites statistics are not registered in 1.21
                  if (!this.isTamed()) {
                     ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(player);
                     if (extendedPlayer != null) {
                        extendedPlayer.studyCreature(this, CreatureManager.getInstance().config.creatureKillKnowledge, false, false);
                     }
                  }
               } catch (Exception var4) {
               }
            }
         }

         if (this.getMasterTarget() != null && this.getMasterTarget() instanceof BaseCreatureEntity) {
            ((BaseCreatureEntity)this.getMasterTarget()).onMinionDeath(this, damageSource);
         }
      }
   }

   /** 1.12 getExperiencePoints(player) */
   @Override
   protected int getBaseExperienceReward() {
      return !this.isTamed() && !this.isMinion() && !this.dropsRequirePlayerDamage ? super.getBaseExperienceReward() : 0;
   }

   public boolean isAggressive() {
      return this.extraMobBehaviour != null && this.extraMobBehaviour.aggressiveOverride ? true : this.isAggressiveByDefault;
   }

   public boolean isHostileTo(Entity target) {
      if (target == null) {
         return false;
      } else if (this.hostileTargetClasses.contains(target.getClass())) {
         return true;
      } else {
         for (CreatureGroup group : this.creatureInfo.getGroups()) {
            if (group.shouldHunt(target) || group.shouldPackHunt(target)) {
               return true;
            }
         }

         return false;
      }
   }

   public void setHostileTo(Class<? extends Entity> targetClass) {
      if (this.hostileTargetClasses == null) {
         this.hostileTargetClasses = new ArrayList<>();
      }

      if (!this.hostileTargetClasses.contains(targetClass)) {
         this.hostileTargetClasses.add(targetClass);
      }
   }

   public boolean isProtective(Entity entity) {
      return entity.getClass() == this.getClass();
   }

   public boolean hasAttackTarget() {
      return !this.level().isClientSide
         ? this.getTarget() != null
         : (this.getByteFromDataManager(TARGET) & BaseCreatureEntity.TARGET_BITS.ATTACK.id) > 0;
   }

   public LivingEntity getMasterTarget() {
      return this.masterTarget;
   }

   public void setMasterTarget(LivingEntity setTarget) {
      this.masterTarget = setTarget;
   }

   public boolean hasMaster() {
      return !this.level().isClientSide
         ? this.getMasterTarget() != null
         : (this.getByteFromDataManager(TARGET) & BaseCreatureEntity.TARGET_BITS.MASTER.id) > 0;
   }

   public LivingEntity getParentTarget() {
      return this.parentTarget;
   }

   public void setParentTarget(LivingEntity setTarget) {
      this.parentTarget = setTarget;
   }

   public boolean hasParent() {
      return !this.level().isClientSide
         ? this.getParentTarget() != null
         : (this.getByteFromDataManager(TARGET) & BaseCreatureEntity.TARGET_BITS.PARENT.id) > 0;
   }

   public LivingEntity getAvoidTarget() {
      return this.avoidTarget;
   }

   public void setAvoidTarget(LivingEntity setTarget) {
      this.currentFleeTime = this.fleeTime;
      this.avoidTarget = setTarget;
   }

   public boolean hasAvoidTarget() {
      return !this.level().isClientSide
         ? this.getAvoidTarget() != null
         : (this.getByteFromDataManager(TARGET) & BaseCreatureEntity.TARGET_BITS.AVOID.id) > 0;
   }

   public LivingEntity getFixateTarget() {
      return this.fixateTarget;
   }

   public void setFixateTarget(LivingEntity target) {
      this.fixateTarget = target;
   }

   public boolean hasFixateTarget() {
      return this.getFixateTarget() != null;
   }

   public LivingEntity getPerchTarget() {
      return this.perchTarget;
   }

   public void setPerchTarget(LivingEntity setTarget) {
      this.perchTarget = setTarget;
   }

   public boolean hasPerchTarget() {
      return !this.level().isClientSide
         ? this.getPerchTarget() != null
         : (this.getByteFromDataManager(TARGET) & BaseCreatureEntity.TARGET_BITS.PERCH.id) > 0;
   }

   public boolean rollAttackTargetChance(LivingEntity target) {
      return true;
   }

   public boolean canEntityBeSeen(Entity target) {
      return super.hasLineOfSight(target);
   }

   public Entity getOwner() {
      return null;
   }

   public UUID getOwnerId() {
      return null;
   }

   public LivingEntity getRider() {
      return this.getControllingPassenger() instanceof LivingEntity ? (LivingEntity)this.getControllingPassenger() : null;
   }

   public void setRiderTarget(Entity setTarget) {
      this.addPassenger(setTarget);
   }

   public boolean hasRiderTarget() {
      return !this.level().isClientSide
         ? this.getControllingPassenger() != null
         : (this.getByteFromDataManager(TARGET) & BaseCreatureEntity.TARGET_BITS.RIDER.id) > 0;
   }

   @Override
   public LivingEntity getControllingPassenger() {
      return this.getPassengers().isEmpty() ? null : (this.getPassengers().get(0) instanceof LivingEntity living ? living : null);
   }

   protected boolean canAddPassenger(Entity entity) {
      return this.isBoss() ? false : super.canAddPassenger(entity);
   }

   public boolean canBeSteered() {
      return false;
   }

   public boolean canBeTargetedBy(LivingEntity entity) {
      if (this.isBoss() && entity instanceof BaseCreatureEntity) {
         BaseCreatureEntity entityCreature = (BaseCreatureEntity)entity;
         if (entityCreature instanceof TameableCreatureEntity) {
            TameableCreatureEntity entityTameable = (TameableCreatureEntity)entity;
            if (entityTameable.getPlayerOwner() != null) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public boolean isInPack() {
      return this.creatureInfo.packSize <= 1 || this.countAllies(10.0) >= this.creatureInfo.packSize;
   }

   public BlockPos getFacingPosition(double distance) {
      return this.getFacingPosition(this, distance, 0.0);
   }

   public BlockPos getFacingPosition(Entity entity, double distance, double angleOffset) {
      return this.getFacingPosition(entity.getX(), entity.getY(), entity.getZ(), distance, (double)entity.getYRot() + angleOffset);
   }

   public BlockPos getFacingPosition(double x, double y, double z, double distance, double angle) {
      angle = Math.toRadians(angle);
      double xAmount = -Math.sin(angle);
      double zAmount = Math.cos(angle);
      return LycPos.of(x + distance * xAmount, y, z + distance * zAmount);
   }

   public Vec3 getFacingPositionDouble(double x, double y, double z, double distance, double angle) {
      if (distance == 0.0) {
         distance = 1.0;
      }

      angle = Math.toRadians(angle);
      double xAmount = -Math.sin(angle);
      double zAmount = Math.cos(angle);
      return new Vec3(x + distance * xAmount, y, z + distance * zAmount);
   }

   public boolean isTamed() {
      return false;
   }

   public boolean isHostile() {
      return this.isAggressive();
   }

   public boolean isCreatureType(MobCategory type, boolean forSpawnCount) {
      if (forSpawnCount) {
         if (this.isMinion()) {
            return false;
         } else {
            for (MobCategory creatureType : this.creatureInfo.creatureSpawn.vanillaSpawnerTypes) {
               if (creatureType == type) {
                  return true;
               }
            }

            return false;
         }
      } else if (type == MobCategory.MONSTER) {
         try {
            return this.isHostile();
         } catch (Exception var5) {
            return true;
         }
      } else {
         // 1.12 EnumCreatureType.getCreatureClass()
         Class<?> creatureClass = type == MobCategory.CREATURE ? Animal.class : type == MobCategory.AMBIENT ? AmbientCreature.class : WaterAnimal.class;
         return creatureClass.isAssignableFrom(this.getClass());
      }
   }

   public boolean canMove() {
      return !this.isBlocking();
   }

   public boolean canWalk() {
      return true;
   }

   public boolean canWade() {
      return true;
   }

   public boolean canFloat() {
      return !this.canBreatheUnderwater112();
   }

   public boolean canDive() {
      return this.canBreatheUnderwater112();
   }

   public boolean isStrongSwimmer() {
      return this.extraMobBehaviour != null && this.extraMobBehaviour.swimmingOverride;
   }

   public boolean canJump() {
      return !this.isBlocking();
   }

   public boolean canClimb() {
      return false;
   }

   public boolean isFlying() {
      return this.extraMobBehaviour != null && this.extraMobBehaviour.flightOverride;
   }

   public int getFlyingHeight() {
      return !this.isFlying() ? 20 : 0;
   }

   public boolean isSafeToLand() {
      if (this.onGround()) {
         return true;
      } else {
         return Material.of(this.level().getBlockState(this.blockPosition().below())).isSolid()
            ? true
            : Material.of(this.level().getBlockState(this.blockPosition().below(2))).isSolid();
      }
   }

   public double getFlightOffset() {
      return 0.0;
   }

   public boolean isCurrentlyFlying() {
      return this.isFlying();
   }

   public boolean canBeTempted() {
      if (this.isRareVariant() || this.spawnedAsBoss) {
         return false;
      } else if (this.creatureInfo.isFarmable()) {
         return true;
      } else {
         return this.isInPack() && !CreatureManager.getInstance().config.packTreatLuring ? false : this.creatureInfo.isTameable();
      }
   }

   public void onEat() {
   }

   public boolean canStealth() {
      return this.extraMobBehaviour != null && this.extraMobBehaviour.stealthOverride;
   }

   public float getStealth() {
      return this.getFloatFromDataManager(STEALTH);
   }

   public void setStealth(float setStealth) {
      setStealth = Math.min(setStealth, 1.0F);
      setStealth = Math.max(setStealth, 0.0F);
      if (!this.level().isClientSide) {
         this.entityData.set(STEALTH, setStealth);
      }
   }

   public boolean isStealthed() {
      return this.getStealth() >= 1.0F;
   }

   public void startStealth() {
   }

   public void onStealth() {
      if (!this.level().isClientSide
         && this.getTarget() != null
         && this.getTarget() instanceof Mob
         && ((Mob)this.getTarget()).getTarget() != null) {
         ((Mob)this.getTarget()).setTarget(null);
      }
   }

   public boolean onClimbable() {
      if (!this.isFlying() && (!this.isStrongSwimmer() || !this.isInWater())) {
         return this.canClimb() ? (this.getByteFromDataManager(CLIMBING) & 1) != 0 : super.onClimbable();
      } else {
         return false;
      }
   }

   public void setBesideClimbableBlock(boolean collided) {
      if (this.canClimb()) {
         byte climbing = this.getByteFromDataManager(CLIMBING);
         if (collided) {
            climbing = (byte)(climbing | 1);
         } else {
            climbing = (byte)(climbing & -2);
         }

         this.entityData.set(CLIMBING, climbing);
      }
   }

   public boolean isBesideClimbableBlock() {
      return (this.getByteFromDataManager(CLIMBING) & 1) != 0;
   }

   private DamageSource lycFallSource;
   private boolean lycFallResult;

   /** 1.21 causeFallDamage calls the 1.12 fall(distance, multiplier) chain. */
   @Override
   public boolean causeFallDamage(float fallDistance, float damageMultiplier, DamageSource source) {
      this.lycFallSource = source;
      this.lycFallResult = false;
      this.fall(fallDistance, damageMultiplier);
      return this.lycFallResult;
   }

   public void fall(float fallDistance, float damageMultiplier) {
      if (!this.isFlying()) {
         fallDistance -= this.getFallResistance();
         if (this.getFallResistance() >= 100.0F) {
            fallDistance = 0.0F;
         }

         this.lycFallResult = super.causeFallDamage(fallDistance, damageMultiplier, this.lycFallSource != null ? this.lycFallSource : this.damageSources().fall());
      }
   }

   protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
      if (!this.isFlying()) {
         super.checkFallDamage(y, onGround, state, pos);
      }
   }

   public void setBlocking() {
      this.currentBlockingTime = this.blockingTime;
   }

   public boolean isBlocking() {
      return this.level().isClientSide
         ? (this.getByteFromDataManager(ANIMATION_STATE) & BaseCreatureEntity.ANIMATION_STATE_BITS.BLOCKING.id) > 0
         : this.currentBlockingTime > 0;
   }

   public boolean canAttackWhileBlocking() {
      return false;
   }

   public int getBlockingMultiplier() {
      return 4;
   }

   public boolean canPickupEntity(LivingEntity entity) {
      if (this.getPickupEntity() == entity) {
         return false;
      } else if (entity instanceof IGroupBoss) {
         return false;
      } else {
         if (entity instanceof Player) {
            if (((Player)entity).isCreative()) {
               return false;
            }

            if (((Player)entity).isSpectator()) {
               return false;
            }
         }

         if (entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).hasPickupEntity()) {
            return false;
         } else if (CreatureManager.getInstance().creatureGroups.containsKey("boss")
            && CreatureManager.getInstance().creatureGroups.get("boss").hasEntity(entity)) {
            return false;
         } else {
            boolean heavyTarget = entity instanceof IGroupHeavy || entity.getBbHeight() >= 4.0F || entity.getBbWidth() >= 4.0F;
            if (heavyTarget && !(this instanceof IGroupHeavy)) {
               return false;
            } else {
               ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(entity);
               if (extendedEntity == null) {
                  return false;
               } else if ((
                     entity.getVehicle() == null || entity.getVehicle() instanceof Boat || entity.getVehicle() instanceof AbstractMinecart
                  )
                  && entity.getControllingPassenger() == null) {
                  if (ObjectManager.getEffect("weight") != null && entity.hasEffect(ObjectManager.getEffect("weight"))) {
                     return false;
                  } else {
                     return ObjectManager.getEffect("repulsion") != null && entity.hasEffect(ObjectManager.getEffect("repulsion"))
                        ? false
                        : extendedEntity.pickedUpByEntity == null || extendedEntity.pickedUpByEntity instanceof FearEntity;
                  }
               } else {
                  return false;
               }
            }
         }
      }
   }

   public void pickupEntity(LivingEntity entity) {
      ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(entity);
      if (extendedEntity != null) {
         extendedEntity.setPickedUpByEntity(this);
      }

      this.pickupEntity = entity;
      this.clearMovement();
   }

   public LivingEntity getPickupEntity() {
      return this.pickupEntity;
   }

   public boolean hasPickupEntity() {
      return !this.level().isClientSide
         ? this.getPickupEntity() != null
         : (this.getByteFromDataManager(TARGET) & BaseCreatureEntity.TARGET_BITS.PICKUP.id) > 0;
   }

   public void dropPickupEntity() {
      ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(this.getPickupEntity());
      if (extendedEntity != null) {
         extendedEntity.setPickedUpByEntity(null);
      }

      this.pickupEntity = null;
   }

   public double[] getPickupOffset(Entity entity) {
      return new double[]{0.0, 0.0, 0.0};
   }

   public boolean canAttackWithPickup() {
      return false;
   }

   public void destroyArea(int x, int y, int z, float strength, boolean drop) {
      this.destroyArea(x, y, z, strength, drop, 0);
   }

   public void destroyArea(int x, int y, int z, float strength, boolean drop, int range) {
      this.destroyArea(x, y, z, strength, drop, range, null, 0);
   }

   public void destroyArea(int x, int y, int z, float strength, boolean drop, int range, Player player, int chain) {
      range = Math.max(range - 1, 0);

      for (int w = -((int)Math.ceil((double)this.getBbWidth()) - range); (double)w <= Math.ceil((double)this.getBbWidth()) + (double)range; w++) {
         for (int d = -((int)Math.ceil((double)this.getBbWidth()) - range); (double)d <= Math.ceil((double)this.getBbWidth()) + (double)range; d++) {
            for (int h = 0; (double)h <= Math.ceil((double)this.getBbHeight()); h++) {
               BlockPos breakPos = LycPos.of(x + w, y + h, z + d);
               if (this.level().getBlockEntity(breakPos) == null) {
                  BlockState blockState = this.level().getBlockState(breakPos);
                  float hardness = blockState.getDestroySpeed(this.level(), breakPos);
                  Material material = Material.of(blockState);
                  if (hardness >= 0.0F
                     && strength >= hardness
                     && strength >= blockState.getBlock().getExplosionResistance()
                     && material != Material.WATER
                     && material != Material.LAVA
                     && (player == null || w != 0 || h != 0 || d != 0)) {
                     SpawnerEventListener.getInstance().onBlockBreak(this.level(), breakPos, blockState, player, chain);
                     this.level().destroyBlock(breakPos, drop);
                  }
               }
            }
         }
      }
   }

   public void destroyAreaBlock(int x, int y, int z, Class<? extends Block> blockClass, boolean drop, int range) {
      for (int w = -((int)Math.ceil((double)this.getBbWidth()) + range); (double)w <= Math.ceil((double)this.getBbWidth()) + (double)range; w++) {
         for (int d = -((int)Math.ceil((double)this.getBbWidth()) + range); (double)d <= Math.ceil((double)this.getBbWidth()) + (double)range; d++) {
            for (int h = 0; (double)h <= Math.ceil((double)this.getBbHeight()); h++) {
               BlockPos breakPos = LycPos.of(x + w, y + h, z + d);
               if (this.level().getBlockEntity(breakPos) == null) {
                  BlockState blockState = this.level().getBlockState(breakPos);
                  if (blockClass.isInstance(blockState.getBlock())) {
                     this.level().destroyBlock(breakPos, drop);
                  }
               }
            }
         }
      }
   }

   public boolean extraAnimation01() {
      return this.extraAnimation01;
   }

   public void applyDebuffs(LivingEntity entity, int duration, int amplifier) {
      for (ElementInfo element : this.getElements()) {
         element.debuffEntity(entity, this.getEffectDuration(duration), this.getEffectAmplifier((float)amplifier));
      }
   }

   public void applyBuffs(LivingEntity entity, int duration, int amplifier) {
      if (this.creatureStats.getAmplifier() >= 0.0) {
         for (ElementInfo element : this.getElements()) {
            element.buffEntity(entity, this.getEffectDuration(duration), this.getEffectAmplifier((float)amplifier));
         }
      }
   }

   protected Item getDropItem() {
      return this.drops != null && this.drops.get(0) != null && !this.isMinion() && !this.isBoundPet() ? this.drops.get(0).getItemStack().getItem() : null;
   }

   protected void dropFewItems(boolean playerKill, int lootingModifier) {
      if (!this.level().isClientSide && !this.isMinion() && !this.isBoundPet() && !this.dropsRequirePlayerDamage) {
         int variantScale = 1;
         if (this.isRareVariant()) {
            variantScale = Variant.RARE_DROP_SCALE;
         } else if (this.getVariant() != null && "uncommon".equals(this.getVariant().rarity)) {
            variantScale = Variant.UNCOMMON_DROP_SCALE;
         }

         for (ItemDrop itemDrop : this.drops) {
            if (this.canDropItem(itemDrop)) {
               int multiplier = 1;
               if (itemDrop.variantIndex < 0) {
                  multiplier *= variantScale;
               }

               if (this.extraMobBehaviour != null && this.extraMobBehaviour.itemDropMultiplierOverride != 1.0) {
                  multiplier = Math.round((float)multiplier * (float)this.extraMobBehaviour.itemDropMultiplierOverride);
               }

               int quantity = itemDrop.getQuantity(this.random, lootingModifier, multiplier);
               if (quantity > 0) {
                  this.spawnAtLocation(itemDrop.getEntityDropItemStack(this, quantity));
               }
            }
         }
      }
   }

   public boolean canDropItem(ItemDrop itemDrop) {
      return itemDrop.subspeciesIndex >= 0 && itemDrop.subspeciesIndex != this.getSubspeciesIndex()
         ? false
         : itemDrop.variantIndex < 0 || itemDrop.variantIndex == this.getVariantIndex();
   }

   public void dropItem(ItemStack itemStack) {
      this.spawnAtLocation(itemStack, 0.0F);
   }

   @Override
   public ItemEntity spawnAtLocation(ItemStack itemStack, float heightOffset) {
      if (itemStack.getCount() != 0) {
         EntityItemCustom entityItem = new EntityItemCustom(
            this.level(), this.getX(), this.getY() + (double)heightOffset, this.getZ(), itemStack
         );
         entityItem.setPickUpDelay(10);
         this.applyDropEffects(entityItem);
         if (this.captureDrops() != null) {
            this.captureDrops().add(entityItem);
         } else {
            this.level().addFreshEntity(entityItem);
         }

         return entityItem;
      } else {
         return null;
      }
   }

   public void applyDropEffects(EntityItemCustom entityItem) {
   }

   public void setDropsRequirePlayerDamage(boolean requiresPlayerDamage) {
      this.dropsRequirePlayerDamage = requiresPlayerDamage;
   }

   public void perchOnEntity(LivingEntity target) {
      if (target == null) {
         if (this.getPerchTarget() != null) {
            ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(this.getPerchTarget());
            if (extendedEntity != null) {
               extendedEntity.setPerchedByEntity(null);
            }
         }

         this.setPerchTarget(null);
      } else {
         ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(target);
         if (extendedEntity != null) {
            this.setPerchTarget(target);
            extendedEntity.setPerchedByEntity(this);
         }
      }
   }

   public void openGUI(Player player) {
      if (!this.level().isClientSide) {
         this.addGUIViewer(player);
         this.refreshGUIViewers();
         this.openGUIToPlayer(player);
      }
   }

   public void addGUIViewer(Player player) {
      if (!this.level().isClientSide) {
         this.guiViewers.add(player);
      }
   }

   public void removeGUIViewer(Player player) {
      if (!this.level().isClientSide) {
         this.guiViewers.remove(player);
      }
   }

   public void refreshGUIViewers() {
      if (!this.level().isClientSide) {
         if (this.guiViewers.size() > 0) {
            for (Player player : this.guiViewers.toArray(new Player[this.guiViewers.size()])) {
               if (player.containerMenu != null && player.containerMenu instanceof CreatureContainer) {
                  if (((CreatureContainer)(Object)player.containerMenu).creature == this) {
                     this.openGUIToPlayer(player);
                  } else {
                     this.removeGUIViewer(player);
                  }
               }
            }
         }
      }
   }

   public void openGUIToPlayer(Player player) {
      if (player != null) {
         GuiHandler.openGui(player, GuiHandler.GuiType.ENTITY.id, this.level(), this.getId(), 0, 0);
      }
   }

   public void scheduleGUIRefresh() {
      this.guiRefreshTick = this.guiRefreshTime + 1;
   }

   public boolean processInteract(Player player, InteractionHand hand) {
      if (this.hasPerchTarget()) {
         return false;
      } else {
         return this.assessInteractCommand(this.getInteractCommands(player, hand, player.getItemInHand(hand)), player, hand, player.getItemInHand(hand))
            ? true
            : false;
      }
   }

   /** 1.12 processInitialInteract -> processInteract (1.21 Mob.interact already did leads and name tags). */
   @Override
   protected InteractionResult mobInteract(Player player, InteractionHand hand) {
      return this.processInteract(player, hand) ? InteractionResult.sidedSuccess(this.level().isClientSide) : InteractionResult.PASS;
   }

   public boolean assessInteractCommand(HashMap<Integer, String> commands, Player player, InteractionHand hand, ItemStack itemStack) {
      if (commands.isEmpty()) {
         return false;
      } else {
         int priority = 100;

         for (int testPriority : commands.keySet()) {
            if (testPriority < priority) {
               priority = testPriority;
            }
         }

         if (!commands.containsKey(priority)) {
            return false;
         } else {
            this.performCommand(commands.get(priority), player, hand, itemStack);
            return true;
         }
      }
   }

   public HashMap<Integer, String> getInteractCommands(Player player, InteractionHand hand, ItemStack itemStack) {
      HashMap<Integer, String> commands = new HashMap<>();
      if (itemStack != null) {
         if (itemStack.getItem() == Items.LEAD && this.canBeLeashedTo(player)) {
            commands.put(BaseCreatureEntity.COMMAND_PIORITIES.ITEM_USE.id, "Leash");
         }

         if (itemStack.getItem() == Items.NAME_TAG) {
            if (this.canNameTag(player)) {
               return new HashMap<>();
            }

            commands.put(BaseCreatureEntity.COMMAND_PIORITIES.ITEM_USE.id, "Name Tag");
         }

         if (this.canBeColored(player) && itemStack.getItem() == Items.INK_SAC) {
            commands.put(BaseCreatureEntity.COMMAND_PIORITIES.ITEM_USE.id, "Color");
         }
      }

      return commands;
   }

   public boolean performCommand(String command, Player player, InteractionHand hand, ItemStack itemStack) {
      if ("Leash".equals(command)) {
         this.setLeashedTo(player, true);
         this.consumePlayersItem(player, hand, itemStack);
         return true;
      } else {
         if ("Color".equals(command)) {
            int colorID = 15 - itemStack.getDamageValue();
            if (colorID != this.getColor()) {
               this.setColor(colorID);
               this.consumePlayersItem(player, hand, itemStack);
               return true;
            }
         }

         return false;
      }
   }

   public boolean canNameTag(Player player) {
      return true;
   }

   public boolean shouldShowName() {
      return this.getVariant() != null && !this.hasCustomName() ? this.renderVariantNameTag() : super.shouldShowName();
   }

   public boolean renderVariantNameTag() {
      return CreatureManager.getInstance().config.subspeciesTags;
   }

   public void consumePlayersItem(Player player, InteractionHand hand, ItemStack itemStack) {
      this.consumePlayersItem(player, hand, itemStack, 1);
   }

   public void consumePlayersItem(Player player, InteractionHand hand, ItemStack itemStack, int amount) {
      if (!player.getAbilities().instabuild) {
         itemStack.shrink(amount);
      }
   }

   public void replacePlayersItem(Player player, InteractionHand hand, ItemStack itemStack, ItemStack newStack) {
      this.replacePlayersItem(player, hand, itemStack, 1, newStack);
   }

   public void replacePlayersItem(Player player, InteractionHand hand, ItemStack itemStack, int amount, ItemStack newStack) {
      if (!player.getAbilities().instabuild) {
         itemStack.shrink(amount);
      }

      if (itemStack.isEmpty()) {
         player.setItemInHand(hand, newStack);
      } else if (!player.getInventory().add(newStack)) {
         player.drop(newStack, false);
      }
   }

   public void performGUICommand(Player player, byte guiCommandID) {
      this.scheduleGUIRefresh();
   }

   public boolean canCarryItems() {
      return this.getInventorySize() > 0;
   }

   public int getInventorySize() {
      return this.inventory.getSizeInventory();
   }

   public int getInventorySizeMax() {
      return Math.max(this.getNoBagSize(), this.getBagSize());
   }

   public boolean hasBag() {
      return this.inventory.getEquipmentStack("bag") != null;
   }

   public int getNoBagSize() {
      return this.extraMobBehaviour != null && this.extraMobBehaviour.inventorySizeOverride > 0 ? this.extraMobBehaviour.inventorySizeOverride : 0;
   }

   public int getBagSize() {
      return this.creatureInfo != null ? this.creatureInfo.bagSize : 5;
   }

   public boolean canPickupItems() {
      return this.extraMobBehaviour != null && this.extraMobBehaviour.itemPickupOverride;
   }

   public int getSpaceForStack(ItemStack pickupStack) {
      return this.inventory.getSpaceForStack(pickupStack);
   }

   public boolean canEquip() {
      return this.creatureInfo.isTameable();
   }

   public void setCurrentItemOrArmor(int slot, ItemStack itemStack) {
      String type = "item";
      if (slot == 0) {
         type = "weapon";
      }

      if (slot == 1) {
         type = "feet";
      }

      if (slot == 2) {
         type = "legs";
      }

      if (slot == 3) {
         type = "chest";
      }

      if (slot == 4) {
         type = "head";
      }

      if (slot == 100) {
         type = "bag";
      }

      this.inventory.setEquipmentStack(type, itemStack);
   }

   public String getEquipmentName(String type) {
      return this.inventory.getEquipmentGrade(type) != null ? type + this.inventory.getEquipmentGrade(type) : null;
   }

   public int getTotalArmorValue() {
      return super.getArmorValue() + this.inventory.getArmorValue();
   }

   public void pickupItems() {
      for (ItemEntity entityItem : this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(1.0, 0.0, 1.0))) {
         if (!entityItem.isRemoved() && entityItem.getItem() != null) {
            ItemStack itemStack = entityItem.getItem();
            int space = this.getSpaceForStack(itemStack);
            if (space > 0) {
               this.onPickupStack(itemStack);
               this.doItemPickup(entityItem);
            }
         }
      }
   }

   public void onPickupStack(ItemStack itemStack) {
   }

   public void doItemPickup(ItemEntity entityItem) {
      if (!entityItem.isRemoved() && entityItem.getItem() != null) {
         ItemStack leftoverStack = this.inventory.autoInsertStack(entityItem.getItem());
         if (leftoverStack != null) {
            entityItem.setItem(leftoverStack);
         } else {
            entityItem.discard();
         }
      }
   }

   public void onBlockPlaced(BlockPos blockPos, BlockState blockState) {
   }

   public void onBuildTaskComplete(CreatureBuildTask buildTask) {
   }

   public boolean isInvulnerableTo(DamageSource source) {
      return this.damageLimit > 0.0F && this.damageTakenThisSec >= this.damageLimit ? true : super.isInvulnerableTo(source);
   }

   public boolean isDamageTypeApplicable(String type, DamageSource source, float damage) {
      if (!"inWall".equals(type) && !"cactus".equals(type) || !this.isRareVariant() && !this.isBoss()) {
         if ("inWall".equals(type)) {
            return !CreatureManager.getInstance().config.suffocationImmunity && !this.hasPerchTarget();
         } else if ("drown".equals(type)) {
            return !CreatureManager.getInstance().config.drownImmunity;
         } else {
            return "fall".equals(type) ? this.getFallResistance() < 100.0F : true;
         }
      } else {
         return false;
      }
   }

   public boolean isDamageEntityApplicable(Entity entity) {
      if (this.isBoss() || this.isRareVariant()) {
         if (entity == null) {
            return false;
         }

         if (this.distanceTo(entity) > (float)this.bossRange) {
            if (entity instanceof Player) {
               ((Player)entity).displayClientMessage(Component.literal(LanguageManager.translate("boss.damage.protection.range")), true);
            }

            return false;
         }
      }

      return true;
   }

   public boolean canBeAffected(MobEffectInstance potionEffect) {
      for (ElementInfo element : this.getElements()) {
         if (!element.isEffectApplicable(potionEffect)) {
            return false;
         }
      }

      return true;
   }

   public boolean canBurn() {
      if (this.extraMobBehaviour != null && this.extraMobBehaviour.fireImmunityOverride) {
         return false;
      } else {
         for (ElementInfo element : this.getElements()) {
            if (!element.canBurn()) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean daylightBurns() {
      return false;
   }

   public boolean canFreeze() {
      if (this instanceof IGroupIce) {
         return false;
      } else {
         for (ElementInfo element : this.getElements()) {
            if (!element.canFreeze()) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean waterDamage() {
      return false;
   }

   public boolean webProof() {
      return false;
   }

   public void makeStuckInBlock(BlockState lycState, Vec3 lycMotion) {
      if (!this.webProof()) {
         super.makeStuckInBlock(lycState, lycMotion);
      }
   }

   public boolean canBreatheAir() {
      return true;
   }

   public boolean canBreatheUnderwater112() {
      return this.extraMobBehaviour != null && this.extraMobBehaviour.waterBreathingOverride;
   }

   /** 1.12 canBreatheUnderwater() decided vanilla drowning; 1.21 asks NeoForge canDrownInFluidType. */
   @Override
   public boolean canDrownInFluidType(net.neoforged.neoforge.fluids.FluidType type) {
      if (type == net.neoforged.neoforge.common.NeoForgeMod.WATER_TYPE.value()) {
         return !this.canBreatheUnderwater112();
      }
      return super.canDrownInFluidType(type);
   }

   /** 1.12 canBreatheUnderwater() decided vanilla drowning; 1.21 asks NeoForge canDrownInFluidType. */
   @Override
   public boolean canDrownInFluidType(net.neoforged.neoforge.fluids.FluidType type) {
      if (type == net.neoforged.neoforge.common.NeoForgeMod.WATER_TYPE.value()) {
         return !this.canBreatheUnderwater112();
      }
      return super.canDrownInFluidType(type);
   }

   public boolean canBreatheUnderlava() {
      return true;
   }

   public void setAir(int air) {
      if (air != 300 || this.canBreatheAir()) {
         super.setAirSupply(air);
      }
   }

   public boolean isInWater() {
      return this.isLavaCreature && this.getBbWidth() <= 2.0F ? this.isInLava() || super.isInWater() : super.isInWater();
   }

   public boolean waterContact() {
      return this.isInWaterRainOrBubble()
         ? true
         : this.level().isRaining() && !this.isBlockUnderground((int)this.getX(), (int)this.getY(), (int)this.getZ());
   }

   public boolean lavaContact() {
      return this.isInLava();
   }

   public boolean canBreatheAtLocation(BlockPos pos) {
      BlockState blockState = this.level().getBlockState(pos);
      if (blockState == null) {
         return true;
      } else if (this.canBreatheAir() && Material.of(blockState) == Material.AIR) {
         return true;
      } else {
         if (this.canBreatheUnderwater112()) {
            if (!this.isLavaCreature && Material.of(blockState) == Material.WATER) {
               return true;
            }

            if (this.isLavaCreature && Material.of(blockState) == Material.LAVA) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean isSwimmable(int x, int y, int z) {
      BlockState blockState = this.level().getBlockState(LycPos.of(x, y, z));
      if (blockState == null) {
         return false;
      } else {
         return this.isLavaCreature && Material.LAVA.equals(Material.of(blockState))
            ? true
            : Material.WATER.equals(Material.of(blockState));
      }
   }

   public float getFallResistance() {
      return 0.0F;
   }

   public byte testLightLevel() {
      return this.testLightLevel(this.blockPosition());
   }

   public byte testLightLevel(BlockPos pos) {
      BlockState spawnBlockState = this.level().getBlockState(pos);
      if (pos.getY() < 0) {
         return 0;
      } else {
         if (Material.of(spawnBlockState) == Material.WATER && CreatureManager.getInstance().spawnConfig.useSurfaceLightLevel) {
            pos = LycPos.of(pos.getX(), this.getWaterSurfaceY(pos), pos.getZ());
         } else {
            pos = LycPos.of(pos.getX(), this.getGroundY(pos), pos.getZ());
         }

         int light = this.level().getMaxLocalRawBrightness(pos);
         if (this.level().isThundering()) {
            // 1.12: skylightSubtracted temporarily 10
            light = this.level().getMaxLocalRawBrightness(pos, 10);
         }

         if (light == 0) {
            return 0;
         } else if (light <= 7) {
            return 1;
         } else {
            return (byte)(light <= 14 ? 2 : 3);
         }
      }
   }

   public boolean isDaytime() {
      if (!this.level().isClientSide) {
         return this.level().isDay();
      } else {
         long time = this.level().getDayTime();
         return time < 12500L ? true : time < 12542L || time >= 23460L;
      }
   }

   public int nearbyCreatureCount(Class targetClass, double range) {
      return this.getNearbyEntities(Entity.class, entity -> targetClass == null || targetClass.isAssignableFrom(entity.getClass()), range).size();
   }

   public int countAllies(double range) {
      return this.getNearbyEntities(
            Entity.class,
            entity -> this.getClass().isAssignableFrom(entity.getClass())
                  && entity instanceof BaseCreatureEntity
                  && this.isTamed() == ((BaseCreatureEntity)entity).isTamed(),
            range
         )
         .size();
   }

   public EnumCreatureAttribute getCreatureAttribute() {
      return this.attribute;
   }

   public double getMountedXOffset() {
      return this.getSubspecies() != null && this.getSubspecies().mountOffset != null
         ? (double)this.getBbWidth() * this.getSubspecies().mountOffset.x
         : (double)this.getBbWidth() * this.creatureInfo.mountOffset.x;
   }

   public double getMountedYOffset() {
      return this.getSubspecies() != null && this.getSubspecies().mountOffset != null
         ? (double)this.getBbHeight() * this.getSubspecies().mountOffset.y
         : (double)this.getBbHeight() * this.creatureInfo.mountOffset.y;
   }

   public double getMountedZOffset() {
      return this.getSubspecies() != null && this.getSubspecies().mountOffset != null
         ? (double)this.getBbWidth() * this.getSubspecies().mountOffset.z
         : (double)this.getBbWidth() * this.creatureInfo.mountOffset.z;
   }

   public <T extends Entity> List<T> getNearbyEntities(Class<? extends T> clazz, Predicate<Entity> predicate, double range) {
      return this.level().getEntitiesOfClass(clazz, this.getBoundingBox().inflate(range, range, range), predicate);
   }

   public <T extends Entity> T getNearestEntity(Class<? extends T> clazz, Class filterClass, double range, boolean canAttack) {
      List aoeTargets = this.getNearbyEntities(clazz, entity -> filterClass == null || filterClass.isAssignableFrom(entity.getClass()), range);
      if (aoeTargets.size() == 0) {
         return null;
      } else {
         double nearestDistance = range + 10.0;
         T nearestEntity = null;

         for (Object entityObj : aoeTargets) {
            T targetEntity = (T)entityObj;
            if (targetEntity != this
               && targetEntity instanceof LivingEntity
               && (!canAttack || this.canAttackEntity((LivingEntity)targetEntity))
               && targetEntity != this.getControllingPassenger()) {
               double distance = (double)this.distanceTo(targetEntity);
               if (distance < nearestDistance) {
                  nearestDistance = distance;
                  nearestEntity = targetEntity;
               }
            }
         }

         return nearestEntity;
      }
   }

   public void readAdditionalSaveData(CompoundTag nbtTagCompound) {
      if (this.creatureInfo.dummy) {
         super.readAdditionalSaveData(nbtTagCompound);
      } else {
         if (nbtTagCompound.contains("FirstSpawn")) {
            this.firstSpawn = nbtTagCompound.getBoolean("FirstSpawn");
         } else {
            this.firstSpawn = true;
         }

         if (nbtTagCompound.contains("SpawnEventType")) {
            this.spawnEventType = nbtTagCompound.getString("SpawnEventType");
         }

         if (nbtTagCompound.contains("SpawnEventCount")) {
            this.spawnEventCount = nbtTagCompound.getInt("SpawnEventCount");
         }

         if (nbtTagCompound.contains("Stealth")) {
            this.setStealth(nbtTagCompound.getFloat("Stealth"));
         }

         if (nbtTagCompound.contains("IsMinion")) {
            this.setMinion(nbtTagCompound.getBoolean("IsMinion"));
         }

         if (nbtTagCompound.contains("IsTemporary") && nbtTagCompound.getBoolean("IsTemporary") && nbtTagCompound.contains("TemporaryDuration")) {
            this.setTemporary(nbtTagCompound.getInt("TemporaryDuration"));
         } else {
            this.unsetTemporary();
         }

         if (nbtTagCompound.contains("IsBoundPet") && nbtTagCompound.getBoolean("IsBoundPet") && !this.hasPetEntry()) {
            this.discard();
         }

         if (nbtTagCompound.contains("ForceNoDespawn")) {
            this.forceNoDespawn = nbtTagCompound.getBoolean("ForceNoDespawn");
         }

         if (nbtTagCompound.contains("Color")) {
            this.setColor(nbtTagCompound.getByte("Color"));
         }

         if (nbtTagCompound.contains("Size")) {
            this.setSizeScale(nbtTagCompound.getDouble("Size"));
         }

         if (!this.firstSpawn && nbtTagCompound.contains("Subspecies") && !nbtTagCompound.contains("Variant")) {
            this.setSubspecies(Variant.getIndexFromOld(nbtTagCompound.getByte("Subspecies")));
            this.setVariant(Variant.getIndexFromOld(nbtTagCompound.getByte("Subspecies")));
         } else {
            if (nbtTagCompound.contains("Subspecies")) {
               this.setSubspecies(nbtTagCompound.getByte("Subspecies"));
            }

            if (nbtTagCompound.contains("Variant")) {
               if (this.firstSpawn) {
                  this.applyVariant(nbtTagCompound.getByte("Variant"));
               } else {
                  this.setVariant(nbtTagCompound.getByte("Variant"));
               }
            }
         }

         if (nbtTagCompound.contains("MobLevel")) {
            if (this.firstSpawn) {
               this.applyLevel(nbtTagCompound.getInt("MobLevel"));
            } else {
               this.setLevel(nbtTagCompound.getInt("MobLevel"));
            }
         }

         if (nbtTagCompound.contains("Experience")) {
            this.setExperience(nbtTagCompound.getInt("Experience"));
         }

         if (nbtTagCompound.contains("SpawnedAsBoss")) {
            this.spawnedAsBoss = nbtTagCompound.getBoolean("SpawnedAsBoss");
         }

         if (nbtTagCompound.contains("SpawnedWithBlockProtection")) {
            this.spawnedWithBlockProtection = nbtTagCompound.getInt("SpawnedWithBlockProtection");
         }

         super.readAdditionalSaveData(nbtTagCompound);
         this.relationships.load(nbtTagCompound);
         this.inventory.readFromNBT(nbtTagCompound);
         if (nbtTagCompound.contains("Drops")) {
            ListTag nbtDropList = nbtTagCompound.getList("Drops", 10);

            for (int i = 0; i < nbtDropList.size(); i++) {
               CompoundTag dropNBT = nbtDropList.getCompound(i);
               ItemDrop drop = new ItemDrop(dropNBT);
               this.addSavedItemDrop(drop);
            }
         }

         if (nbtTagCompound.contains("DropsRequirePlayerDamage")) {
            this.dropsRequirePlayerDamage = nbtTagCompound.getBoolean("DropsRequirePlayerDamage");
         }

         if (nbtTagCompound.contains("ExtraBehaviour")) {
            this.extraMobBehaviour.readFromNBT(nbtTagCompound.getCompound("ExtraBehaviour"));
         }

         if (nbtTagCompound.contains("HomeX")
            && nbtTagCompound.contains("HomeY")
            && nbtTagCompound.contains("HomeZ")
            && nbtTagCompound.contains("HomeDistanceMax")) {
            this.setHome(
               nbtTagCompound.getInt("HomeX"),
               nbtTagCompound.getInt("HomeY"),
               nbtTagCompound.getInt("HomeZ"),
               nbtTagCompound.getFloat("HomeDistanceMax")
            );
         }

         if (nbtTagCompound.contains("ArenaX") && nbtTagCompound.contains("ArenaY") && nbtTagCompound.contains("ArenaZ")) {
            this.setArenaCenter(
               LycPos.of(nbtTagCompound.getInt("ArenaX"), nbtTagCompound.getInt("ArenaY"), nbtTagCompound.getInt("ArenaZ"))
            );
         }

         if (nbtTagCompound.contains("FixateUUIDMost") && nbtTagCompound.contains("FixateUUIDLeast")) {
            this.fixateUUID = new UUID(nbtTagCompound.getLong("FixateUUIDMost"), nbtTagCompound.getLong("FixateUUIDLeast"));
         }

         if (nbtTagCompound.contains("MinionIds")) {
            ListTag minionIds = nbtTagCompound.getList("MinionIds", 10);

            for (int i = 0; i < minionIds.size(); i++) {
               CompoundTag minionId = minionIds.getCompound(i);
               if (minionId.contains("ID")) {
                  Entity entity = this.level().getEntity(minionId.getInt("ID"));
                  if (entity instanceof LivingEntity) {
                     this.addMinion((LivingEntity)entity);
                  }
               }
            }
         }
      }
   }

   public void addAdditionalSaveData(CompoundTag nbtTagCompound) {
      if (this.creatureInfo.dummy) {
         super.addAdditionalSaveData(nbtTagCompound);
      } else {
         nbtTagCompound.putBoolean("FirstSpawn", this.firstSpawn);
         nbtTagCompound.putString("SpawnEventType", this.spawnEventType);
         nbtTagCompound.putInt("SpawnEventCount", this.spawnEventCount);
         nbtTagCompound.putFloat("Stealth", this.getStealth());
         nbtTagCompound.putBoolean("IsMinion", this.isMinion());
         nbtTagCompound.putBoolean("IsTemporary", this.isTemporary);
         nbtTagCompound.putInt("TemporaryDuration", this.temporaryDuration);
         nbtTagCompound.putBoolean("IsBoundPet", this.isBoundPet());
         nbtTagCompound.putBoolean("ForceNoDespawn", this.forceNoDespawn);
         nbtTagCompound.putByte("Color", (byte)this.getColor());
         nbtTagCompound.putByte("Subspecies", (byte)this.getSubspeciesIndex());
         nbtTagCompound.putByte("Variant", (byte)this.getVariantIndex());
         nbtTagCompound.putDouble("Size", this.sizeScale);
         nbtTagCompound.putInt("MobLevel", this.getLevel());
         nbtTagCompound.putInt("Experience", this.getExperience());
         nbtTagCompound.putBoolean("SpawnedAsBoss", this.spawnedAsBoss);
         nbtTagCompound.putInt("SpawnedWithBlockProtection", this.spawnedWithBlockProtection);
         if (this.hasHome()) {
            BlockPos homePos = this.getHomePosition();
            nbtTagCompound.putInt("HomeX", homePos.getX());
            nbtTagCompound.putInt("HomeY", homePos.getY());
            nbtTagCompound.putInt("HomeZ", homePos.getZ());
            nbtTagCompound.putFloat("HomeDistanceMax", this.getHomeDistanceMax());
         }

         if (this.hasArenaCenter()) {
            BlockPos arenaPos = this.getArenaCenter();
            nbtTagCompound.putInt("ArenaX", arenaPos.getX());
            nbtTagCompound.putInt("ArenaY", arenaPos.getY());
            nbtTagCompound.putInt("ArenaZ", arenaPos.getZ());
         }

         if (this.getFixateTarget() != null) {
            nbtTagCompound.putLong("FixateUUIDMost", this.getFixateTarget().getUUID().getMostSignificantBits());
            nbtTagCompound.putLong("FixateUUIDLeast", this.getFixateTarget().getUUID().getLeastSignificantBits());
         }

         super.addAdditionalSaveData(nbtTagCompound);
         this.relationships.save(nbtTagCompound);
         this.inventory.writeToNBT(nbtTagCompound);
         ListTag nbtDropList = new ListTag();

         for (ItemDrop drop : this.savedDrops) {
            CompoundTag dropNBT = new CompoundTag();
            if (drop.writeToNBT(dropNBT)) {
               nbtDropList.add(dropNBT);
            }
         }

         nbtTagCompound.put("Drops", nbtDropList);
         nbtTagCompound.putBoolean("DropsRequirePlayerDamage", this.dropsRequirePlayerDamage);
         CompoundTag extTagCompound = new CompoundTag();
         this.extraMobBehaviour.writeToNBT(extTagCompound);
         nbtTagCompound.put("ExtraBehaviour", extTagCompound);
         ListTag minionIds = new ListTag();

         for (LivingEntity minion : this.minions) {
            CompoundTag minionId = new CompoundTag();
            minionId.putInt("ID", minion.getId());
            minionIds.add(minionId);
         }

         nbtTagCompound.put("MinionIds", minionIds);
      }
   }

   public int getAttackCooldown() {
      return this.attackCooldown;
   }

   public boolean isAttackOnCooldown() {
      return this.getAttackCooldown() > 0;
   }

   public void triggerAttackCooldown() {
      this.attackCooldown = this.getAttackCooldownMax();
   }

   public void resetAttackCooldown() {
      this.attackCooldown = 0;
      this.setAttackCooldownMax(this.getAttackCooldownMax());
   }

   public int getAttackCooldownMax() {
      return !this.level().isClientSide ? this.attackCooldownMax : this.getIntFromDataManager(ANIMATION_ATTACK_COOLDOWN_MAX);
   }

   public void setAttackCooldownMax(int cooldownMax) {
      this.attackCooldownMax = cooldownMax;
      if (!this.level().isClientSide) {
         this.getEntityData().set(ANIMATION_ATTACK_COOLDOWN_MAX, this.attackCooldownMax);
      }
   }

   public ResourceLocation getTexture() {
      return this.getTexture("");
   }

   public ResourceLocation getTexture(String suffix) {
      String textureName = this.getTextureName();
      if (this.getSubspecies().name != null) {
         textureName = textureName + "_" + this.getSubspecies().name;
      }

      if (this.getVariant() != null) {
         textureName = textureName + "_" + this.getVariant().color;
      }

      if (!"".equals(suffix)) {
         textureName = textureName + "_" + suffix;
      }

      if (AssetManager.getTexture(textureName) == null) {
         AssetManager.addTexture(textureName, this.creatureInfo.modInfo, "textures/entity/" + textureName.toLowerCase() + ".png");
      }

      return AssetManager.getTexture(textureName);
   }

   public ResourceLocation getEquipmentTexture(String equipmentName) {
      if (!this.canEquip()) {
         return this.getTexture();
      } else {
         if (this.getSubspecies() != null && this.getSubspecies().name != null) {
            equipmentName = this.getSubspecies().name + "_" + equipmentName;
         }

         return this.getSubTexture(equipmentName);
      }
   }

   public ResourceLocation getSubTexture(String subName) {
      subName = subName.toLowerCase();
      String textureName = this.getTextureName();
      textureName = textureName + "_" + subName;
      if (AssetManager.getTexture(textureName) == null) {
         AssetManager.addTexture(textureName, this.creatureInfo.modInfo, "textures/entity/" + textureName.toLowerCase() + ".png");
      }

      return AssetManager.getTexture(textureName);
   }

   public String getTextureName() {
      return this.creatureInfo.getName();
   }

   public boolean canBeColored(Player player) {
      return false;
   }

   public int getColor() {
      return this.getByteFromDataManager(COLOR) & 15;
   }

   public void setColor(int color) {
      if (!this.level().isClientSide) {
         this.entityData.set(COLOR, (byte)(color & 15));
      }
   }

   public boolean showBossInfo() {
      if (this.forceBossHealthBar || this.isBoss()) {
         return true;
      } else {
         return this.isRareVariant() ? Variant.RARE_HEALTH_BARS : false;
      }
   }

   public void startSeenByPlayer(ServerPlayer player) {
      super.startSeenByPlayer(player);
      if (this.getBossInfo() != null) {
         this.bossInfo.addPlayer(player);
      }
   }

   public void stopSeenByPlayer(ServerPlayer player) {
      super.stopSeenByPlayer(player);
      if (this.getBossInfo() != null) {
         this.bossInfo.removePlayer(player);
      }
   }

   protected float getSoundVolume() {
      if (this.isBoss()) {
         return 4.0F;
      } else {
         return this.isRareVariant() ? 2.0F : 1.0F;
      }
   }

   public String getSoundName() {
      String soundSuffix = "";
      if (this.getSubspecies() != null && this.getSubspecies().name != null) {
         soundSuffix = soundSuffix + "." + this.getSubspecies().name;
      }

      return this.creatureInfo.getName() + soundSuffix;
   }

   public int getAmbientSoundInterval() {
      return CreatureManager.getInstance().config.idleSoundTicks;
   }

   protected SoundEvent getAmbientSound() {
      return AssetManager.getSound(this.getSoundName() + "_say");
   }

   protected SoundEvent getHurtSound(DamageSource damageSource) {
      return AssetManager.getSound(this.getSoundName() + "_hurt");
   }

   protected SoundEvent getDeathSound() {
      return AssetManager.getSound(this.getSoundName() + "_death");
   }

   @Override
   protected void playStepSound(BlockPos pos, BlockState block) {
      if (!this.isCurrentlyFlying()) {
         if (!this.hasStepSound) {
            super.playStepSound(pos, block);
         } else {
            this.playSound(
               AssetManager.getSound(this.getSoundName() + "_step"), this.getSoundVolume(), 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F)
            );
         }
      }
   }

   protected SoundEvent getFallSound(int height) {
      return height > 4 ? SoundEvents.HOSTILE_BIG_FALL : SoundEvents.HOSTILE_SMALL_FALL;
   }

   protected SoundEvent getSwimSound() {
      return SoundEvents.HOSTILE_SWIM;
   }

   protected SoundEvent getSplashSound() {
      return SoundEvents.HOSTILE_SPLASH;
   }

   public void playJumpSound() {
      if (this.hasJumpSound) {
         this.playSound(AssetManager.getSound(this.getSoundName() + "_jump"), this.getSoundVolume(), 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
      }
   }

   public void playFlySound() {
      if (this.isFlying() && !this.hasPerchTarget()) {
         this.playSound(AssetManager.getSound(this.getSoundName() + "_fly"), this.getSoundVolume(), 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
      }
   }

   public void playAttackSound() {
      if (this.hasAttackSound) {
         this.playSound(
            AssetManager.getSound(this.getSoundName() + "_attack"), this.getSoundVolume(), 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F)
         );
      }
   }

   public void playPhaseSound() {
      if (AssetManager.getSound(this.creatureInfo.getName() + "_phase") != null) {
         this.playSound(
            AssetManager.getSound(this.getSoundName() + "_phase"), this.getSoundVolume() * 2.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F)
         );
      }
   }

   public void playSound(SoundEvent sound, float volume, float pitch) {
      super.playSound(sound, volume, pitch);
   }

   public static enum ANIMATION_STATE_BITS {
      ATTACKED((byte)1),
      GROUNDED((byte)2),
      IN_WATER((byte)4),
      BLOCKING((byte)8),
      MINION((byte)16),
      EXTRA01((byte)32),
      BOSS((byte)64);

      public final byte id;

      private ANIMATION_STATE_BITS(byte value) {
         this.id = value;
      }

      public byte getValue() {
         return this.id;
      }
   }

   public static enum COMMAND_PIORITIES {
      OVERRIDE(0),
      IMPORTANT(1),
      EQUIPPING(2),
      ITEM_USE(3),
      EMPTY_HAND(4),
      MAIN(5);

      public final int id;

      private COMMAND_PIORITIES(int value) {
         this.id = value;
      }

      public int getValue() {
         return this.id;
      }
   }

   public static enum GUI_COMMAND {
      CLOSE((byte)0),
      SITTING((byte)1),
      FOLLOWING((byte)2),
      PASSIVE((byte)3),
      STANCE((byte)4),
      PVP((byte)5),
      TELEPORT((byte)6),
      SPAWNING((byte)7),
      RELEASE((byte)8);

      public byte id;

      private GUI_COMMAND(byte i) {
         this.id = i;
      }
   }

   public class GroupData implements SpawnGroupData {
      public boolean isChild;

      public GroupData(boolean child) {
         this.isChild = child;
      }
   }

   public static enum PET_COMMAND_ID {
      ACTIVE((byte)0),
      TELEPORT((byte)1),
      PVP((byte)2),
      RELEASE((byte)3),
      PASSIVE((byte)4),
      DEFENSIVE((byte)5),
      ASSIST((byte)6),
      AGGRESSIVE((byte)7),
      FOLLOW((byte)8),
      WANDER((byte)9),
      SIT((byte)10),
      FLEE((byte)11);

      public byte id;

      private PET_COMMAND_ID(byte i) {
         this.id = i;
      }
   }

   public static enum TARGET_BITS {
      ATTACK((byte)1),
      MASTER((byte)2),
      PARENT((byte)4),
      AVOID((byte)8),
      RIDER((byte)16),
      PICKUP((byte)32),
      PERCH((byte)64);

      public final byte id;

      private TARGET_BITS(byte value) {
         this.id = value;
      }

      public byte getValue() {
         return this.id;
      }
   }

   public static enum TARGET_TYPES {
      ENEMY((byte)1),
      ALLY((byte)2),
      SELF((byte)4);

      public final byte id;

      private TARGET_TYPES(byte value) {
         this.id = value;
      }

      public byte getValue() {
         return this.id;
      }
   }
}
