package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.ai.PathNavigateMyrmex;
import com.github.alexthe666.iceandfire.structures.WorldGenMyrmexHive;
import com.github.alexthe666.iceandfire.world.LegacyBiomeTypes;
import com.github.alexthe666.iceandfire.world.MyrmexWorldData;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.ilexiconn.llibrary.server.animation.AnimationHandler;
import net.ilexiconn.llibrary.server.animation.IAnimatedEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Ice and Fire 1.7.1 EntityMyrmexBase (castes, growth, hive membership, reputation and trading as an IMerchant). */
public abstract class EntityMyrmexBase extends Animal implements IAnimatedEntity, Merchant {
   private static final EntityDataAccessor<Byte> CLIMBING = SynchedEntityData.defineId(EntityMyrmexBase.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Integer> GROWTH_STAGE = SynchedEntityData.defineId(EntityMyrmexBase.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> VARIANT = SynchedEntityData.defineId(EntityMyrmexBase.class, EntityDataSerializers.BOOLEAN);
   private int animationTick;
   private Animation currentAnimation;
   private MyrmexHive hive;
   public boolean isEnteringHive = false;
   public boolean isBeingGuarded = false;
   protected int growthTicks = 1;
   private float ageScale = 1.0F;
   public static final net.minecraft.resources.ResourceLocation TEXTURE_DESERT_LARVA = IafLand.rl("textures/models/myrmex/myrmex_desert_larva.png");
   public static final net.minecraft.resources.ResourceLocation TEXTURE_DESERT_PUPA = IafLand.rl("textures/models/myrmex/myrmex_desert_pupa.png");
   public static final net.minecraft.resources.ResourceLocation TEXTURE_JUNGLE_LARVA = IafLand.rl("textures/models/myrmex/myrmex_jungle_larva.png");
   public static final net.minecraft.resources.ResourceLocation TEXTURE_JUNGLE_PUPA = IafLand.rl("textures/models/myrmex/myrmex_jungle_pupa.png");
   public static final Animation ANIMATION_PUPA_WIGGLE = Animation.create(20);
   @Nullable
   private Player buyingPlayer;
   @Nullable
   private MerchantOffers buyingList;
   private UUID lastBuyingPlayer;
   private int careerId;
   private int careerLevel;
   private boolean needsInitilization;
   private int timeUntilReset;
   private int wealth;

   public EntityMyrmexBase(EntityType<? extends EntityMyrmexBase> type, Level worldIn) {
      super(type, worldIn);
   }

   public static AttributeSupplier.Builder createBaseAttributes() {
      return Mob.createMobAttributes().add(Attributes.ATTACK_DAMAGE).add(Attributes.FOLLOW_RANGE, 128.0).add(Attributes.STEP_HEIGHT, 2.0);
   }

   public boolean canMove() {
      return this.getGrowthStage() > 1 && !this.isTrading();
   }

   @Override
   public boolean isBaby() {
      return this.getGrowthStage() < 2;
   }

   public boolean isChild() {
      return this.isBaby();
   }

   @Override
   public void setBaby(boolean baby) {
   }

   @Override
   protected void customServerAiStep() {
      if (!this.isTrading() && this.timeUntilReset > 0) {
         this.timeUntilReset--;
         if (this.timeUntilReset <= 0) {
            if (this.needsInitilization) {
               MerchantOffers renewed = new MerchantOffers();
               for (MerchantOffer merchantrecipe : this.buyingList) {
                  if (merchantrecipe.isOutOfStock()) {
                     int extra = this.random.nextInt(6) + this.random.nextInt(6) + 2;
                     renewed.add(new MerchantOffer(merchantrecipe.getItemCostA(), merchantrecipe.getItemCostB(), merchantrecipe.getResult(), merchantrecipe.getUses(), merchantrecipe.getMaxUses() + extra, merchantrecipe.getXp(), merchantrecipe.getPriceMultiplier()));
                  } else {
                     renewed.add(merchantrecipe);
                  }
               }
               this.buyingList = renewed;

               this.populateBuyingList();
               this.needsInitilization = false;
               if (this.getHive() != null && this.lastBuyingPlayer != null) {
                  this.level().broadcastEntityEvent(this, (byte)14);
                  this.getHive().setWorld(this.level());
                  this.getHive().modifyPlayerReputation(this.lastBuyingPlayer, 1);
               }
            }

            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
         }
      }

      super.customServerAiStep();
   }

   @Override
   protected int getBaseExperienceReward() {
      return this.getCasteImportance() * 7 + this.level().random.nextInt(3);
   }

   @Override
   public boolean shouldDropExperience() {
      return true;
   }

   @Override
   public boolean hurt(DamageSource dmg, float i) {
      if (dmg.is(DamageTypes.IN_WALL) && this.getGrowthStage() < 2) {
         return false;
      } else {
         if (this.getGrowthStage() < 2) {
            this.setAnimation(ANIMATION_PUPA_WIGGLE);
         }

         return super.hurt(dmg, i);
      }
   }

   @Override
   protected float getJumpPower() {
      return 0.52F;
   }

   /** 1.12 jump(): always the sprint boost forward */
   @Override
   public void jumpFromGround() {
      double my = (double)this.getJumpPower();
      if (this.hasEffect(MobEffects.JUMP)) {
         my += (double)((float)(this.getEffect(MobEffects.JUMP).getAmplifier() + 1) * 0.1F);
      }

      float f = this.getYRot() * (float)(Math.PI / 180.0);
      var m = this.getDeltaMovement();
      this.setDeltaMovement(m.x - (double)(Mth.sin(f) * 0.2F), my, m.z + (double)(Mth.cos(f) * 0.2F));
      this.hasImpulse = true;
      net.neoforged.neoforge.common.CommonHooks.onLivingJump(this);
   }

   /** getBlockPathWeight */
   @Override
   public float getWalkTargetValue(BlockPos pos, net.minecraft.world.level.LevelReader level) {
      return isResin(level.getBlockState(pos.below())) ? 10.0F : level.getPathfindingCostFromLightLevels(pos);
   }

   @Override
   protected PathNavigation createNavigation(Level worldIn) {
      return new PathNavigateMyrmex(this, worldIn);
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(CLIMBING, (byte)0);
      builder.define(GROWTH_STAGE, 2);
      builder.define(VARIANT, false);
   }

   @Override
   public void tick() {
      super.tick();
      if (this.getGrowthStage() < 2 && this.getVehicle() != null && this.getVehicle() instanceof EntityMyrmexBase) {
         float yaw = this.getVehicle().getYRot();
         this.setYRot(yaw);
         this.yHeadRot = yaw;
         this.yBodyRot = 0.0F;
         this.yBodyRotO = 0.0F;
      }

      this.setScaleForAge(false);
      if (!this.level().isClientSide) {
         this.setBesideClimbableBlock(this.horizontalCollision && (this.onGround() || !this.verticalCollision));
      }

      if (this.getGrowthStage() < 2) {
         this.growthTicks++;
         if (this.growthTicks == IceAndFire.CONFIG.myrmexLarvaTicks) {
            this.setGrowthStage(this.getGrowthStage() + 1);
            this.growthTicks = 0;
         }
      }

      if (!this.level().isClientSide && this.getGrowthStage() < 2 && this.getRandom().nextInt(150) == 0 && this.getAnimation() == NO_ANIMATION) {
         this.setAnimation(ANIMATION_PUPA_WIGGLE);
      }

      if (this.getTarget() != null && !(this.getTarget() instanceof Player) && this.getNavigation().isDone()) {
         this.setTarget(null);
      }

      if (this.getTarget() != null
         && (
            haveSameHive(this, this.getTarget())
               || this.getTarget() instanceof TamableAnimal && !this.canAttackTamable((TamableAnimal)this.getTarget())
               || this.getTarget() instanceof Player && this.getHive() != null && !this.getHive().isPlayerReputationTooLowToFight(this.getTarget().getUUID())
         )) {
         this.setTarget(null);
      }

      if (this.getHealth() < this.getMaxHealth() && this.tickCount % 500 == 0 && this.isOnResin()) {
         this.heal(1.0F);
         this.level().broadcastEntityEvent(this, (byte)76);
      }

      AnimationHandler.INSTANCE.updateAnimations(this);
   }

   @Override
   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("GrowthStage", this.getGrowthStage());
      tag.putInt("GrowthTicks", this.growthTicks);
      tag.putBoolean("Variant", this.isJungle());
      if (this.getHive() != null) {
         tag.putUUID("HiveUUID", this.getHive().hiveUUID);
      }

      tag.putInt("Career", this.careerId);
      tag.putInt("CareerLevel", this.careerLevel);
      tag.putInt("Riches", this.wealth);
      if (this.buyingList != null) {
         MerchantOffers.CODEC.encodeStart(this.registryAccess().createSerializationContext(NbtOps.INSTANCE), this.buyingList).result().ifPresent(t -> tag.put("Offers", t));
      }
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setGrowthStage(tag.getInt("GrowthStage"));
      this.growthTicks = tag.getInt("GrowthTicks");
      this.setJungleVariant(tag.getBoolean("Variant"));
      if (tag.hasUUID("HiveUUID")) {
         this.setHive(MyrmexWorldData.get(this.level()).getHiveFromUUID(tag.getUUID("HiveUUID")));
      }
      this.careerId = tag.getInt("Career");
      this.careerLevel = tag.getInt("CareerLevel");
      if (tag.contains("Offers", Tag.TAG_COMPOUND)) {
         MerchantOffers.CODEC.parse(this.registryAccess().createSerializationContext(NbtOps.INSTANCE), tag.get("Offers")).resultOrPartial().ifPresent(o -> this.buyingList = o);
      }

      this.wealth = tag.getInt("Riches");
   }

   @Override
   public void setTradingPlayer(@Nullable Player player) {
      this.buyingPlayer = player;
   }

   @Nullable
   @Override
   public Player getTradingPlayer() {
      return this.buyingPlayer;
   }

   @Nullable
   public Player getCustomer() {
      return this.buyingPlayer;
   }

   public void setCustomer(@Nullable Player player) {
      this.buyingPlayer = player;
   }

   public boolean isTrading() {
      return this.buyingPlayer != null;
   }

   @Override
   public MerchantOffers getOffers() {
      if (this.buyingList == null) {
         this.populateBuyingList();
      }

      return this.buyingList;
   }

   @Override
   public void overrideOffers(MerchantOffers offers) {
   }

   /** verifySellingItem */
   @Override
   public void notifyTradeUpdated(ItemStack stack) {
      if (!this.level().isClientSide && this.ambientSoundTime > -this.getAmbientSoundInterval() + 20) {
         this.ambientSoundTime = -this.getAmbientSoundInterval();
         this.playSound(stack.isEmpty() ? IafLand.sound("myrmex_hurt") : IafLand.sound("myrmex_idle"), this.getSoundVolume(), this.getVoicePitch());
      }
   }

   /** useRecipe */
   @Override
   public void notifyTrade(MerchantOffer recipe) {
      recipe.increaseUses();
      this.ambientSoundTime = -this.getAmbientSoundInterval();
      if (this.getHive() != null && this.getCustomer() != null) {
         this.getHive().setWorld(this.level());
         this.getHive().modifyPlayerReputation(this.getCustomer().getUUID(), 2);
      }

      this.playSound(IafLand.sound("myrmex_idle"), this.getSoundVolume(), this.getVoicePitch());
      int i = 3 + this.random.nextInt(4);
      if (recipe.getUses() == 1 || this.random.nextInt(5) == 0) {
         this.timeUntilReset = 40;
         this.needsInitilization = true;
         if (this.buyingPlayer != null) {
            this.lastBuyingPlayer = this.buyingPlayer.getUUID();
         } else {
            this.lastBuyingPlayer = null;
         }

         i += 5;
      }

      if (IafLand.isItem(recipe.getCostA().getItem(), "myrmex_desert_resin") || IafLand.isItem(recipe.getCostA().getItem(), "myrmex_jungle_resin")) {
         this.wealth = this.wealth + recipe.getCostA().getCount();
      }

      if (recipe.shouldRewardExp()) {
         this.level().addFreshEntity(new ExperienceOrb(this.level(), this.getX(), this.getY() + 0.5, this.getZ(), i));
      }
   }

   @Override
   public int getVillagerXp() {
      return 0;
   }

   @Override
   public void overrideXp(int xp) {
   }

   @Override
   public boolean showProgressBar() {
      return false;
   }

   @Override
   public SoundEvent getNotifyTradeSound() {
      return IafLand.sound("myrmex_idle");
   }

   @Override
   public boolean isClientSide() {
      return this.level().isClientSide;
   }

   /** 0 worker, 1 soldier, 2 sentinel, 3 royal, 4 queen: the career of getProfessionForge() */
   public abstract int getCareerCaste();

   private void populateBuyingList() {
      if (this.careerId != 0 && this.careerLevel != 0) {
         this.careerLevel++;
      } else {
         // getProfessionForge().getRandomCareer(rand): every myrmex profession has one career
         this.careerId = this.random.nextInt(1) + 1;
         this.careerLevel = 1;
      }

      if (this.buyingList == null) {
         this.buyingList = new MerchantOffers();
      }

      int j = this.careerLevel - 1;
      List<MyrmexTrades.BasicTrade> trades = MyrmexTrades.getTrades(this.getCareerCaste(), this.isJungle(), j);
      if (trades != null) {
         for (MyrmexTrades.BasicTrade trade : trades) {
            trade.addMerchantRecipe(this.buyingList, this.random);
         }
      }
   }

   public boolean canAttackTamable(TamableAnimal tameable) {
      return tameable.getOwner() != null && this.getHive() != null ? this.getHive().isPlayerReputationTooLowToFight(tameable.getOwnerUUID()) : true;
   }

   @Override
   public Component getDisplayName() {
      if (!this.hasCustomName() && this.buyingList == null && !this.level().isClientSide) {
         this.populateBuyingList();
      }

      return super.getDisplayName();
   }

   public Level getWorld() {
      return this.level();
   }

   public BlockPos getPos() {
      return this.blockPosition();
   }

   public void setGrowthStage(int stage) {
      this.entityData.set(GROWTH_STAGE, stage);
   }

   public int getGrowthStage() {
      return this.entityData.get(GROWTH_STAGE);
   }

   public boolean isJungle() {
      return this.entityData.get(VARIANT);
   }

   public void setJungleVariant(boolean isJungle) {
      this.entityData.set(VARIANT, isJungle);
   }

   public boolean isBesideClimbableBlock() {
      return (this.entityData.get(CLIMBING) & 1) != 0;
   }

   public void setBesideClimbableBlock(boolean climbing) {
      byte b0 = this.entityData.get(CLIMBING);
      if (climbing) {
         b0 = (byte)(b0 | 1);
      } else {
         b0 = (byte)(b0 & -2);
      }

      this.entityData.set(CLIMBING, b0);
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
      return new Animation[]{ANIMATION_PUPA_WIGGLE};
   }

   /** setRevengeTarget */
   @Override
   public void setLastHurtByMob(@Nullable LivingEntity livingBase) {
      if (this.getHive() == null || livingBase == null || livingBase instanceof Player && this.getHive().isPlayerReputationTooLowToFight(livingBase.getUUID())) {
         super.setLastHurtByMob(livingBase);
      }

      if (this.getHive() != null && livingBase != null) {
         this.getHive().addOrRenewAgressor(livingBase, this.getImportance());
      }

      if (this.getHive() != null && livingBase != null && livingBase instanceof Player) {
         int i = -5 * this.getCasteImportance();
         this.getHive().setWorld(this.level());
         this.getHive().modifyPlayerReputation(livingBase.getUUID(), i);
         if (this.isAlive()) {
            this.level().broadcastEntityEvent(this, (byte)13);
         }
      }
   }

   @Override
   public void die(DamageSource cause) {
      if (this.getHive() != null) {
         Entity entity = cause.getEntity();
         if (entity != null) {
            this.getHive().setWorld(this.level());
            this.getHive().modifyPlayerReputation(entity.getUUID(), -15);
         }
      }

      super.die(cause);
   }

   @Override
   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      ItemStack itemstack = player.getItemInHand(hand);
      boolean flag2 = IafLand.isItem(itemstack.getItem(), "myrmex_jungle_staff") || IafLand.isItem(itemstack.getItem(), "myrmex_desert_staff");
      if (flag2) {
         this.onStaffInteract(player, itemstack);
         player.swing(hand);
         return InteractionResult.SUCCESS;
      } else {
         boolean flag = itemstack.getItem() == Items.NAME_TAG || itemstack.getItem() == Items.LEAD;
         if (flag) {
            itemstack.interactLivingEntity(player, this, hand);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
         } else if (!(itemstack.getItem() instanceof SpawnEggItem egg && egg.getType(itemstack) == this.getType())
            && this.getGrowthStage() >= 2
            && this.isAlive()
            && !this.isTrading()
            && !this.isChild()
            && !player.isShiftKeyDown()) {
            if (this.level().isClientSide) {
               return InteractionResult.SUCCESS;
            }
            if (this.buyingList == null) {
               this.populateBuyingList();
            }

            if (hand == InteractionHand.MAIN_HAND) {
               player.awardStat(Stats.TALKED_TO_VILLAGER);
            }

            if (this.buyingList.isEmpty() || this.getHive() != null && this.getHive().isPlayerReputationTooLowToTrade(player.getUUID())) {
               if (this.buyingList.isEmpty()) {
                  return super.mobInteract(player, hand);
               }
            } else {
               this.setCustomer(player);
               this.openTradingScreen(player, this.getDisplayName(), 1);
            }

            return InteractionResult.SUCCESS;
         } else {
            return super.mobInteract(player, hand);
         }
      }
   }

   /** ItemMyrmexStaff on a myrmex: bind the staff to this hive (the staff stores "HiveUUID" in its custom data). */
   public void onStaffInteract(Player player, ItemStack itemstack) {
      CompoundTag tag = itemstack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
      UUID staffUUID = tag.hasUUID("HiveUUID") ? tag.getUUID("HiveUUID") : null;
      if (!this.level().isClientSide) {
         if (player.isCreative() || this.getHive() == null || this.getHive().canPlayerCommandHive(player.getUUID())) {
            if (this.getHive() == null) {
               player.displayClientMessage(Component.translatable("myrmex.message.null_hive"), true);
            } else if (staffUUID != null && staffUUID.equals(this.getHive().hiveUUID)) {
               player.displayClientMessage(Component.translatable("myrmex.message.staff_already_set"), true);
            } else {
               this.getHive().setWorld(this.level());
               EntityMyrmexQueen queen = this.getHive().getQueen();
               BlockPos center = this.getHive().getCenterGround();
               if (queen != null && queen.hasCustomName()) {
                  player.displayClientMessage(Component.translatable("myrmex.message.staff_set_named", queen.getCustomName(), center.getX(), center.getY(), center.getZ()), true);
               } else {
                  player.displayClientMessage(Component.translatable("myrmex.message.staff_set_unnamed", center.getX(), center.getY(), center.getZ()), true);
               }

               tag.putUUID("HiveUUID", this.getHive().hiveUUID);
               net.minecraft.world.item.component.CustomData.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, itemstack, tag);
            }
         }
      }
   }

   @Nullable
   @Override
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData livingdata) {
      livingdata = super.finalizeSpawn(level, difficulty, spawnType, livingdata);
      this.setHive(MyrmexWorldData.get(this.level()).getNearestHive(this.blockPosition(), 400));
      if (this.getHive() != null) {
         this.setJungleVariant(isJungleBiome(this.level(), this.getHive().getCenter()));
      } else {
         this.setJungleVariant(this.random.nextBoolean());
      }

      this.populateBuyingList();
      return livingdata;
   }

   /** top/filler block of the biome is not sand and the biome is not SANDY */
   private static boolean isJungleBiome(Level world, BlockPos position) {
      Holder<Biome> biome = world.getBiome(position);
      // sand top/filler blocks: deserts, beaches, mesas (red sand is a 1.12 Blocks.SAND variant)
      return !LegacyBiomeTypes.hasType(biome, "SANDY") && !LegacyBiomeTypes.hasType(biome, "BEACH") && !LegacyBiomeTypes.hasType(biome, "MESA")
         && !biome.is(net.minecraft.tags.BiomeTags.HAS_DESERT_PYRAMID);
   }

   public abstract boolean shouldLeaveHive();

   public abstract boolean shouldEnterHive();

   /** 1.12 EntityAgeable.setScale: larva 0.5, pupa 0.75, adult 1 of the caste's size */
   public void setScaleForAge(boolean baby) {
      float scale = this.getGrowthStage() == 0 ? 0.5F : (this.getGrowthStage() == 1 ? 0.75F : 1.0F);
      if (scale != this.ageScale) {
         this.ageScale = scale;
         this.refreshDimensions();
      }
   }

   @Override
   protected EntityDimensions getDefaultDimensions(Pose pose) {
      return this.getType().getDimensions().scale(this.ageScale);
   }

   @Override
   public float getAgeScale() {
      return 1.0F;
   }

   public abstract net.minecraft.resources.ResourceLocation getAdultTexture();

   public abstract float getModelScale();

   public net.minecraft.resources.ResourceLocation getTexture() {
      if (this.getGrowthStage() == 0) {
         return this.isJungle() ? TEXTURE_JUNGLE_LARVA : TEXTURE_DESERT_LARVA;
      } else if (this.getGrowthStage() == 1) {
         return this.isJungle() ? TEXTURE_JUNGLE_PUPA : TEXTURE_DESERT_PUPA;
      } else {
         return this.getAdultTexture();
      }
   }

   public MyrmexHive getHive() {
      return this.hive;
   }

   public void setHive(MyrmexHive newHive) {
      this.hive = newHive;
      if (this.hive != null) {
         this.hive.addMyrmex(this);
      }
   }

   public static boolean haveSameHive(EntityMyrmexBase myrmex, Entity entity) {
      if (entity instanceof EntityMyrmexBase other && myrmex.getHive() != null && other.getHive() != null && myrmex.isJungle() == other.isJungle()) {
         return myrmex.getHive().getCenter() == other.getHive().getCenter();
      } else {
         return entity instanceof EntityMyrmexEgg ? myrmex.isJungle() == ((EntityMyrmexEgg)entity).isJungle() : false;
      }
   }

   /** collideWithEntity */
   @Override
   protected void doPush(Entity entityIn) {
      if (!haveSameHive(this, entityIn)) {
         entityIn.push(this);
      }
   }

   public boolean canSeeSky() {
      return this.level().canSeeSky(this.blockPosition());
   }

   public static boolean isResin(BlockState state) {
      String id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
      return id.contains("myrmex") && id.contains("resin") && !id.contains("glass");
   }

   public static boolean isEdibleBlock(BlockState blockState) {
      String id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).getPath();
      if (id.contains("myrmex") && id.contains("biolight")) {
         return false;
      }
      return blockState.is(BlockTags.LEAVES)
         || blockState.getBlock() instanceof BushBlock
         || blockState.getBlock() instanceof VineBlock
         || blockState.getBlock() instanceof CactusBlock
         || blockState.getBlock() instanceof LeavesBlock
         || blockState.is(Blocks.SUGAR_CANE)
         || blockState.is(BlockTags.REPLACEABLE_BY_TREES) && !blockState.isAir() && blockState.getFluidState().isEmpty();
   }

   public boolean isOnResin() {
      double d0 = this.getY() - 1.0;
      BlockPos blockpos = BlockPos.containing(this.getX(), d0, this.getZ());

      while (this.level().isEmptyBlock(blockpos) && blockpos.getY() > this.level().getMinBuildHeight() + 1) {
         blockpos = blockpos.below();
      }

      return isResin(this.level().getBlockState(blockpos));
   }

   public boolean isInNursery() {
      if (this.getHive() != null
         && this.getHive().getRooms(WorldGenMyrmexHive.RoomType.NURSERY).isEmpty()
         && this.getHive().getRandomRoom(WorldGenMyrmexHive.RoomType.NURSERY, this.getRandom(), this.blockPosition()) != null) {
         return false;
      } else if (this.getHive() != null) {
         BlockPos nursery = this.getHive().getRandomRoom(WorldGenMyrmexHive.RoomType.NURSERY, this.getRandom(), this.blockPosition());
         return this.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(nursery)) < 45.0;
      } else {
         return false;
      }
   }

   @Override
   public void travel(net.minecraft.world.phys.Vec3 travelVector) {
      if (!this.canMove()) {
         super.travel(net.minecraft.world.phys.Vec3.ZERO);
      } else {
         super.travel(travelVector);
      }
   }

   public int getImportance() {
      return this.getGrowthStage() < 2 ? 1 : this.getCasteImportance();
   }

   public abstract int getCasteImportance();

   public boolean needsGaurding() {
      return true;
   }

   public boolean shouldMoveThroughHive() {
      return true;
   }

   public boolean shouldWander() {
      return this.getHive() == null;
   }

   @Override
   public void handleEntityEvent(byte id) {
      if (id == 76) {
         this.playVillagerEffect();
      } else {
         super.handleEntityEvent(id);
      }
   }

   @Nullable
   @Override
   protected SoundEvent getAmbientSound() {
      return IafLand.sound("myrmex_idle");
   }

   @Nullable
   @Override
   protected SoundEvent getHurtSound(DamageSource source) {
      return IafLand.sound("myrmex_hurt");
   }

   @Nullable
   @Override
   protected SoundEvent getDeathSound() {
      return IafLand.sound("myrmex_die");
   }

   @Override
   protected void playStepSound(BlockPos pos, BlockState blockIn) {
      this.playSound(IafLand.sound("myrmex_walk"), 0.16F * this.getMyrmexPitch() * (this.getRandom().nextFloat() * 0.6F + 0.4F), 1.0F);
   }

   protected void playBiteSound() {
      this.playSound(IafLand.sound("myrmex_bite"), 1.0F * this.getMyrmexPitch(), 1.0F);
   }

   protected void playStingSound() {
      this.playSound(IafLand.sound("myrmex_sting"), 1.0F * this.getMyrmexPitch(), 0.6F);
   }

   protected void playVillagerEffect() {
      for (int i = 0; i < 7; i++) {
         double d0 = this.random.nextGaussian() * 0.02;
         double d1 = this.random.nextGaussian() * 0.02;
         double d2 = this.random.nextGaussian() * 0.02;
         this.level()
            .addParticle(
               ParticleTypes.HAPPY_VILLAGER,
               this.getX() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
               this.getY() + 0.5 + (double)(this.random.nextFloat() * this.getBbHeight()),
               this.getZ() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
               d0,
               d1,
               d2
            );
      }
   }

   public float getMyrmexPitch() {
      return this.getBbWidth();
   }

   public static int getRandomCaste(Level world, RandomSource random, boolean royal) {
      float rand = random.nextFloat();
      if (royal) {
         if ((double)rand > 0.9) {
            return 2;
         } else if ((double)rand > 0.75) {
            return 3;
         } else {
            return (double)rand > 0.5 ? 1 : 0;
         }
      } else if ((double)rand > 0.8) {
         return 3;
      } else {
         return (double)rand > 0.6 ? 1 : 0;
      }
   }

   /** The 1.12 vanilla target predicate of the castes: !IMob.VISIBLE_MOB_SELECTOR, not the same hive, alive. */
   protected static boolean notVisibleMob(Entity entity) {
      return !(entity instanceof net.minecraft.world.entity.monster.Enemy && !entity.isInvisible());
   }
}
