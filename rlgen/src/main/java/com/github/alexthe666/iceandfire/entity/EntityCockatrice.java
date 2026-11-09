package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.ai.CockatriceAIAggroLook;
import com.github.alexthe666.iceandfire.entity.ai.CockatriceAIFollowOwner;
import com.github.alexthe666.iceandfire.entity.ai.CockatriceAIStareAttack;
import com.github.alexthe666.iceandfire.entity.ai.CockatriceAITarget;
import com.github.alexthe666.iceandfire.entity.ai.CockatriceAITargetItems;
import com.github.alexthe666.iceandfire.entity.ai.CockatriceAIWander;
import com.github.alexthe666.iceandfire.entity.ai.EntityAIAttackMeleeNoCooldown;
import com.github.alexthe666.iceandfire.entity.ai.goal112.EntityAIAttackMelee;
import com.github.alexthe666.iceandfire.entity.ai.goal112.EntityAIHurtByTarget;
import com.github.alexthe666.iceandfire.entity.ai.goal112.EntityAIOwnerHurtByTarget;
import com.github.alexthe666.iceandfire.entity.ai.goal112.EntityAIOwnerHurtTarget;
import java.util.List;
import javax.annotation.Nullable;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.ilexiconn.llibrary.server.animation.AnimationHandler;
import net.ilexiconn.llibrary.server.animation.IAnimatedEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Ice and Fire 1.7.1 EntityCockatrice. */
public class EntityCockatrice extends TamableAnimal implements IAnimatedEntity, IBlacklistedFromStatues, IVillagerFear {
   private int animationTick;
   private Animation currentAnimation;
   private static final EntityDataAccessor<Boolean> HEN = SynchedEntityData.defineId(EntityCockatrice.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> STARING = SynchedEntityData.defineId(EntityCockatrice.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> TARGET_ENTITY = SynchedEntityData.defineId(EntityCockatrice.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> TAMING_PLAYER = SynchedEntityData.defineId(EntityCockatrice.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> TAMING_LEVEL = SynchedEntityData.defineId(EntityCockatrice.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> COMMAND = SynchedEntityData.defineId(EntityCockatrice.class, EntityDataSerializers.INT);
   private boolean isSitting;
   public float sitProgress;
   private boolean isStaring;
   public float stareProgress;
   public int ticksStaring = 0;
   public static final Animation ANIMATION_JUMPAT = Animation.create(30);
   public static final Animation ANIMATION_WATTLESHAKE = Animation.create(20);
   public static final Animation ANIMATION_BITE = Animation.create(15);
   public static final Animation ANIMATION_SPEAK = Animation.create(10);
   public static final Animation ANIMATION_EAT = Animation.create(20);
   private CockatriceAIStareAttack aiStare;
   private EntityAIAttackMelee aiMelee;
   private boolean isMeleeMode = false;
   private LivingEntity targetedEntity;
   private int clientSideAttackTime;
   public static final float VIEW_RADIUS = 0.6F;
   public static final ResourceKey<LootTable> LOOT = ResourceKey.create(Registries.LOOT_TABLE, IafLand.rl("cockatrice"));

   public EntityCockatrice(EntityType<? extends EntityCockatrice> type, Level worldIn) {
      super(type, worldIn);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes()
         .add(Attributes.MOVEMENT_SPEED, 0.4)
         .add(Attributes.MAX_HEALTH, 40.0)
         .add(Attributes.ATTACK_DAMAGE, 5.0)
         .add(Attributes.FOLLOW_RANGE, 64.0)
         .add(Attributes.ARMOR, 2.0);
   }

   @Override
   protected int getBaseExperienceReward() {
      return 10 + this.level().random.nextInt(5);
   }

   /** getCanSpawnHere: the config roll, then the 1.12 EntityAnimal rules (grass below, light > 8). */
   public static boolean checkCockatriceSpawnRules(EntityType<? extends Animal> type, LevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
      return random.nextInt(IceAndFire.CONFIG.cockatriceSpawnCheckChance + 1) == 0 && Animal.checkAnimalSpawnRules(type, level, reason, pos, random);
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(1, new FloatGoal(this));
      this.goalSelector.addGoal(2, new CockatriceAIFollowOwner(this, 1.0, 10.0F, 2.0F));
      this.goalSelector.addGoal(2, new CockatriceAIWander(this, 1.0));
      this.goalSelector.addGoal(3, new SitWhenOrderedToGoal(this));
      this.goalSelector.addGoal(4, this.aiStare = new CockatriceAIStareAttack(this, 1.0, 0, 15.0F));
      this.goalSelector.addGoal(4, this.aiMelee = new EntityAIAttackMeleeNoCooldown(this, 1.5, false));
      this.goalSelector.addGoal(6, new CockatriceAIAggroLook(this));
      this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, LivingEntity.class, 6.0F));
      this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new CockatriceAITargetItems(this, false));
      this.targetSelector.addGoal(2, new EntityAIOwnerHurtByTarget(this));
      this.targetSelector.addGoal(3, new EntityAIOwnerHurtTarget(this));
      this.targetSelector.addGoal(4, new EntityAIHurtByTarget(this, false));
      this.targetSelector
         .addGoal(
            5,
            new CockatriceAITarget<>(
               this,
               LivingEntity.class,
               true,
               entity -> (
                     entity instanceof Enemy && this.isTame() && !(entity instanceof Creeper) && !(entity instanceof ZombifiedPiglin) && !(entity instanceof EnderMan)
                        || entity instanceof Player
                        || LandUtils.isAnimaniaFerret(entity)
                  )
                  && !LandUtils.isAnimaniaChicken(entity)
            )
         );
      this.goalSelector.removeGoal(this.aiMelee);
   }

   @Override
   protected ResourceKey<LootTable> getDefaultLootTable() {
      return LOOT;
   }

   @Override
   public boolean isAlliedTo(Entity entityIn) {
      return LandUtils.isAnimaniaChicken(entityIn) || super.isAlliedTo(entityIn);
   }

   @Override
   public boolean hurt(DamageSource source, float damage) {
      if (source.getEntity() != null && LandUtils.isAnimaniaFerret(source.getEntity())) {
         damage *= 5.0F;
      }

      return super.hurt(source, damage);
   }

   private boolean canUseStareOn(Entity entity) {
      return (!(entity instanceof IBlacklistedFromStatues) || ((IBlacklistedFromStatues)entity).canBeTurnedToStone()) && !LandUtils.isAnimaniaFerret(entity);
   }

   private void switchAI(boolean melee) {
      if (melee) {
         this.goalSelector.removeGoal(this.aiStare);
         if (this.aiMelee != null) {
            this.goalSelector.addGoal(4, this.aiMelee);
         }

         this.isMeleeMode = true;
      } else {
         this.goalSelector.removeGoal(this.aiMelee);
         if (this.aiStare != null) {
            this.goalSelector.addGoal(4, this.aiStare);
         }

         this.isMeleeMode = false;
      }
   }

   @Override
   public boolean doHurtTarget(Entity entityIn) {
      if (this.isStaring()) {
         return false;
      } else if (this.getRandom().nextBoolean()) {
         if (this.getAnimation() != ANIMATION_JUMPAT && this.getAnimation() != ANIMATION_BITE) {
            this.setAnimation(ANIMATION_JUMPAT);
         }

         return false;
      } else {
         if (this.getAnimation() != ANIMATION_BITE && this.getAnimation() != ANIMATION_JUMPAT) {
            this.setAnimation(ANIMATION_BITE);
         }

         return false;
      }
   }

   public boolean canMove() {
      return !this.isSitting() && (this.getAnimation() != ANIMATION_JUMPAT || this.getAnimationTick() >= 7);
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(HEN, false);
      builder.define(STARING, false);
      builder.define(TARGET_ENTITY, 0);
      builder.define(TAMING_PLAYER, 0);
      builder.define(TAMING_LEVEL, 0);
      builder.define(COMMAND, 0);
   }

   public void setTargetedEntity(int entityId) {
      this.entityData.set(TARGET_ENTITY, entityId);
   }

   public boolean hasTargetedEntity() {
      return this.entityData.get(TARGET_ENTITY) != 0;
   }

   public void setTamingPlayer(int entityId) {
      this.entityData.set(TAMING_PLAYER, entityId);
   }

   public boolean hasTamingPlayer() {
      return this.entityData.get(TAMING_PLAYER) != 0;
   }

   @Nullable
   public Entity getTamingPlayer() {
      if (!this.hasTamingPlayer()) {
         return null;
      } else if (this.level().isClientSide) {
         if (this.targetedEntity != null) {
            return this.targetedEntity;
         } else {
            Entity entity = this.level().getEntity(this.entityData.get(TAMING_PLAYER));
            if (entity instanceof LivingEntity) {
               this.targetedEntity = (LivingEntity)entity;
               return this.targetedEntity;
            } else {
               return null;
            }
         }
      } else {
         return this.level().getEntity(this.entityData.get(TAMING_PLAYER));
      }
   }

   @Nullable
   public LivingEntity getTargetedEntity() {
      boolean blindness = this.hasEffect(MobEffects.BLINDNESS) || this.getTarget() != null && this.getTarget().hasEffect(MobEffects.BLINDNESS);
      if (blindness) {
         return null;
      } else if (!this.hasTargetedEntity()) {
         return null;
      } else if (this.level().isClientSide) {
         if (this.targetedEntity != null) {
            return this.targetedEntity;
         } else {
            Entity entity = this.level().getEntity(this.entityData.get(TARGET_ENTITY));
            if (entity instanceof LivingEntity) {
               this.targetedEntity = (LivingEntity)entity;
               return this.targetedEntity;
            } else {
               return null;
            }
         }
      } else {
         return this.getTarget();
      }
   }

   @Override
   public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
      super.onSyncedDataUpdated(key);
      if (TARGET_ENTITY.equals(key)) {
         this.clientSideAttackTime = 0;
         this.targetedEntity = null;
      }
   }

   @Override
   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putBoolean("Hen", this.isHen());
      tag.putBoolean("Staring", this.isStaring());
      tag.putInt("TamingLevel", this.getTamingLevel());
      tag.putInt("TamingPlayer", this.entityData.get(TAMING_PLAYER));
      tag.putInt("Command", this.getCommand());
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setHen(tag.getBoolean("Hen"));
      this.setStaring(tag.getBoolean("Staring"));
      this.setTamingLevel(tag.getInt("TamingLevel"));
      this.setTamingPlayer(tag.getInt("TamingPlayer"));
      this.setCommand(tag.getInt("Command"));
   }

   public boolean isSitting() {
      if (this.level().isClientSide) {
         boolean isSitting = this.isInSittingPose();
         this.isSitting = isSitting;
         return isSitting;
      } else {
         return this.isSitting;
      }
   }

   /** 1.12 EntityTameable.setSitting: the synced sitting flag and EntityAISit. */
   public void setSitting(boolean sitting) {
      this.setOrderedToSit(sitting);
      this.setInSittingPose(sitting);
      if (!this.level().isClientSide) {
         this.isSitting = sitting;
      }
   }

   @Override
   public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource source) {
      return false;
   }

   @Nullable
   @Override
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData livingdata) {
      livingdata = super.finalizeSpawn(level, difficulty, spawnType, livingdata);
      this.setHen(this.getRandom().nextBoolean());
      return livingdata;
   }

   public void setHen(boolean hen) {
      this.entityData.set(HEN, hen);
   }

   public boolean isHen() {
      return this.entityData.get(HEN);
   }

   public void setTamingLevel(int level) {
      this.entityData.set(TAMING_LEVEL, level);
   }

   public int getTamingLevel() {
      return this.entityData.get(TAMING_LEVEL);
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

   public boolean isStaring() {
      return this.level().isClientSide ? (this.isStaring = this.entityData.get(STARING)) : this.isStaring;
   }

   public void setStaring(boolean staring) {
      this.entityData.set(STARING, staring);
      if (!this.level().isClientSide) {
         this.isStaring = staring;
      }
   }

   public void forcePreyToLook(Mob mob) {
      mob.getLookControl().setLookAt(this.getX(), this.getY() + (double)this.getEyeHeight(), this.getZ(), (float)mob.getHeadRotSpeed(), (float)mob.getMaxHeadXRot());
   }

   @Override
   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      boolean flag = stack.getItem() == Items.NAME_TAG || stack.getItem() == Items.LEAD;
      if (flag) {
         stack.interactLivingEntity(player, this, hand);
         return InteractionResult.sidedSuccess(this.level().isClientSide);
      } else if (stack.getItem() == Items.POISONOUS_POTATO) {
         return super.mobInteract(player, hand);
      } else {
         if (this.isTame() && this.isOwnedBy(player)) {
            if (LandUtils.isSeeds(stack) || stack.getItem() == Items.ROTTEN_FLESH) {
               if (this.getHealth() < this.getMaxHealth()) {
                  this.heal(8.0F);
                  this.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
                  stack.shrink(1);
               }

               return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            if (stack.isEmpty()) {
               this.setCommand(this.getCommand() + 1);
               if (this.getCommand() > 2) {
                  this.setCommand(0);
               }

               player.displayClientMessage(Component.translatable("cockatrice.command." + this.getCommand()), true);
               this.playSound(SoundEvents.ZOMBIE_INFECT, 1.0F, 1.0F);
               return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
         }

         return InteractionResult.PASS;
      }
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (this.level().getDifficulty() == Difficulty.PEACEFUL && this.getTarget() != null && this.getTarget() instanceof Player) {
         this.setTarget(null);
      }

      if (this.isSitting() && this.getCommand() != 1) {
         this.setSitting(false);
      }

      if (this.isSitting() && this.getTarget() != null) {
         this.setTarget(null);
      }

      if (!this.level().isClientSide) {
         if (this.getTarget() == null || !this.getTarget().isAlive()) {
            this.setTargetedEntity(0);
         } else if (this.isStaring() || this.shouldStareAttack(this.getTarget())) {
            this.setTargetedEntity(this.getTarget().getId());
         }
      }

      if (this.getAnimation() == ANIMATION_BITE && this.getTarget() != null && this.getAnimationTick() == 7) {
         double dist = this.distanceToSqr(this.getTarget());
         if (dist < 8.0) {
            this.getTarget().hurt(this.damageSources().mobAttack(this), (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE)));
         }
      }

      if (this.getAnimation() == ANIMATION_JUMPAT && this.getTarget() != null) {
         double dist = this.distanceToSqr(this.getTarget());
         double d0 = this.getTarget().getX() - this.getX();
         double d1 = this.getTarget().getZ() - this.getZ();
         float leap = Mth.sqrt((float)(d0 * d0 + d1 * d1));
         if (dist <= 16.0 && this.onGround() && this.getAnimationTick() > 7 && this.getAnimationTick() < 12) {
            Vec3 m = this.getDeltaMovement();
            double mx = m.x;
            double mz = m.z;
            if ((double)leap >= 1.0E-4) {
               mx = mx + d0 / (double)leap * 0.8F + mx * 0.2F;
               mz = mz + d1 / (double)leap * 0.8F + mz * 0.2F;
            }

            this.setDeltaMovement(mx, 0.5, mz);
         }

         if (dist < 4.0 && this.getAnimationTick() > 10) {
            this.getTarget().hurt(this.damageSources().mobAttack(this), (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE)));
            if ((double)leap >= 1.0E-4) {
               LivingEntity t = this.getTarget();
               if (t != null) {
                  Vec3 tm = t.getDeltaMovement();
                  Vec3 m = this.getDeltaMovement();
                  t.setDeltaMovement(tm.x + d0 / (double)leap * 0.8F + m.x * 0.2F, tm.y, tm.z + d1 / (double)leap * 0.8F + m.z * 0.2F);
               }
            }
         }
      }

      boolean sitting = this.isSitting();
      if (sitting && this.sitProgress < 20.0F) {
         this.sitProgress += 0.5F;
      } else if (!sitting && this.sitProgress > 0.0F) {
         this.sitProgress -= 0.5F;
      }

      boolean staring = this.isStaring();
      if (staring && this.stareProgress < 20.0F) {
         this.stareProgress += 0.5F;
      } else if (!staring && this.stareProgress > 0.0F) {
         this.stareProgress -= 0.5F;
      }

      if (!this.level().isClientSide) {
         if (staring) {
            this.ticksStaring++;
         } else {
            this.ticksStaring = 0;
         }
      }

      if (!this.level().isClientSide && staring && (this.getTarget() == null || this.shouldMelee())) {
         this.setStaring(false);
      }

      if (this.getTarget() != null) {
         this.getLookControl()
            .setLookAt(
               this.getTarget().getX(),
               this.getTarget().getY() + (double)this.getTarget().getEyeHeight(),
               this.getTarget().getZ(),
               (float)this.getHeadRotSpeed(),
               (float)this.getMaxHeadXRot()
            );
         if (!this.shouldMelee() && this.getTarget() instanceof Mob && !(this.getTarget() instanceof Player)) {
            this.forcePreyToLook((Mob)this.getTarget());
         }
      }

      boolean blindness = this.hasEffect(MobEffects.BLINDNESS) || this.getTarget() != null && this.getTarget().hasEffect(MobEffects.BLINDNESS);
      if (blindness) {
         this.setStaring(false);
      }

      if (!this.level().isClientSide
         && !blindness
         && this.getTarget() != null
         && EntityGorgon.isEntityLookingAt(this, this.getTarget(), 0.6F)
         && EntityGorgon.isEntityLookingAt(this.getTarget(), this, 0.6F)
         && !this.shouldMelee()) {
         if (!this.isStaring()) {
            this.setStaring(true);
         } else {
            int attackStrength = this.getFriendsCount(this.getTarget());
            if (this.level().getDifficulty() == Difficulty.HARD) {
               attackStrength++;
            }

            this.getTarget().addEffect(new MobEffectInstance(MobEffects.WITHER, 10, 2 + Math.min(1, attackStrength)));
            this.getTarget().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, Math.min(4, attackStrength)));
            this.getTarget().addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
            if (attackStrength >= 2 && this.getTarget().tickCount % 40 == 0) {
               this.getTarget().hurt(this.damageSources().wither(), (float)(attackStrength - 1));
            }

            this.getTarget().setLastHurtByMob(this);
            if (!this.isTame() && this.getTarget() instanceof Player) {
               this.setTamingPlayer(this.getTarget().getId());
               this.setTamingLevel(this.getTamingLevel() + 1);
               if (this.getTamingLevel() % 100 == 0) {
                  this.level().broadcastEntityEvent(this, (byte)46);
               }

               if (this.getTamingLevel() >= 1000) {
                  this.level().broadcastEntityEvent(this, (byte)45);
                  if (this.getTamingPlayer() != null && this.getTamingPlayer() instanceof Player) {
                     this.tame((Player)this.getTamingPlayer());
                  }

                  this.setTarget(null);
                  this.setTamingPlayer(0);
                  this.setTargetedEntity(0);
               }
            }
         }
      }

      if (!this.level().isClientSide && this.getTarget() == null && this.getRandom().nextInt(300) == 0 && this.getAnimation() == NO_ANIMATION) {
         this.setAnimation(ANIMATION_WATTLESHAKE);
      }

      if (!this.level().isClientSide) {
         if (this.shouldMelee() && !this.isMeleeMode) {
            this.switchAI(true);
         }

         if (!this.shouldMelee() && this.isMeleeMode) {
            this.switchAI(false);
         }
      }

      if (this.level().isClientSide
         && this.getTargetedEntity() != null
         && EntityGorgon.isEntityLookingAt(this, this.getTargetedEntity(), 0.6F)
         && EntityGorgon.isEntityLookingAt(this.getTargetedEntity(), this, 0.6F)
         && this.isStaring()
         && this.hasTargetedEntity()) {
         if (this.clientSideAttackTime < this.getAttackDuration()) {
            this.clientSideAttackTime++;
         }

         LivingEntity entitylivingbase = this.getTargetedEntity();
         if (entitylivingbase != null) {
            this.getLookControl().setLookAt(entitylivingbase, 90.0F, 90.0F);
            this.getLookControl().tick();
            double d5 = (double)this.getAttackAnimationScale(0.0F);
            double d0x = entitylivingbase.getX() - this.getX();
            double d1x = entitylivingbase.getY() + (double)(entitylivingbase.getBbHeight() * 0.5F) - (this.getY() + (double)this.getEyeHeight());
            double d2 = entitylivingbase.getZ() - this.getZ();
            double d3 = Math.sqrt(d0x * d0x + d1x * d1x + d2 * d2);
            d0x /= d3;
            d1x /= d3;
            d2 /= d3;
            double d4 = this.random.nextDouble();
            ParticleOptions spell = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFF000000 | 3484199);

            while (d4 < d3) {
               d4 += 1.8 - d5 + this.random.nextDouble() * (1.7 - d5);
               this.level().addParticle(spell, this.getX() + d0x * d4, this.getY() + d1x * d4 + (double)this.getEyeHeight(), this.getZ() + d2 * d4, 0.0, 0.0, 0.0);
            }
         }
      }

      AnimationHandler.INSTANCE.updateAnimations(this);
   }

   private int getFriendsCount(LivingEntity attackTarget) {
      if (this.getTarget() == null) {
         return 0;
      } else {
         float dist = (float)IceAndFire.CONFIG.cockatriceChickenSearchLength;
         List<EntityCockatrice> list = this.level().getEntitiesOfClass(EntityCockatrice.class, this.getBoundingBox().expandTowards((double)dist, (double)dist, (double)dist));
         int i = 0;

         for (EntityCockatrice cockatrice : list) {
            if (cockatrice != this && cockatrice.getTarget() != null && cockatrice.getTarget() == this.getTarget()) {
               boolean bothLooking = EntityGorgon.isEntityLookingAt(cockatrice, cockatrice.getTarget(), 0.6F)
                  && EntityGorgon.isEntityLookingAt(cockatrice.getTarget(), cockatrice, 0.6F);
               if (bothLooking) {
                  i++;
               }
            }
         }

         return i;
      }
   }

   public float getAttackAnimationScale(float f) {
      return ((float)this.clientSideAttackTime + f) / (float)this.getAttackDuration();
   }

   public boolean shouldStareAttack(Entity entity) {
      return this.distanceTo(entity) > 5.0F;
   }

   public int getAttackDuration() {
      return 80;
   }

   private boolean shouldMelee() {
      boolean blindness = this.hasEffect(MobEffects.BLINDNESS) || this.getTarget() != null && this.getTarget().hasEffect(MobEffects.BLINDNESS);
      return this.getTarget() != null
         && ((double)this.distanceTo(this.getTarget()) < 4.0 || LandUtils.isAnimaniaFerret(this.getTarget()) || blindness || !this.canUseStareOn(this.getTarget()));
   }

   @Override
   public void travel(Vec3 travelVector) {
      if (!this.canMove() && !this.isVehicle()) {
         travelVector = new Vec3(0.0, travelVector.y, 0.0);
      }

      super.travel(travelVector);
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
      return new Animation[]{NO_ANIMATION, ANIMATION_JUMPAT, ANIMATION_WATTLESHAKE, ANIMATION_BITE, ANIMATION_SPEAK, ANIMATION_EAT};
   }

   @Override
   public boolean canBeTurnedToStone() {
      return false;
   }

   public boolean isTargetBlocked(Vec3 target) {
      if (target != null) {
         BlockHitResult rayTrace = this.level().clip(new ClipContext(Vec3.atLowerCornerOf(this.blockPosition()), target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
         if (rayTrace != null && rayTrace.getType() != HitResult.Type.MISS) {
            BlockPos pos = BlockPos.containing(rayTrace.getLocation());
            if (!this.level().isEmptyBlock(pos)) {
               return true;
            }
         }
      }

      return false;
   }

   /** DragonUtils.getBlockInTargetsViewCockatrice */
   public static BlockPos getBlockInTargetsViewCockatrice(EntityCockatrice cockatrice, LivingEntity target) {
      float radius = (float)(10 + cockatrice.getRandom().nextInt(10));
      float neg = cockatrice.getRandom().nextBoolean() ? 1.0F : -1.0F;
      float angle = (float)(Math.PI / 180.0) * target.yHeadRot;
      double extraX = (double)(radius * Mth.sin((float)(Math.PI + (double)angle)));
      double extraZ = (double)(radius * Mth.cos(angle));
      BlockPos radialPos = BlockPos.containing(target.getX() + extraX, 0.0, target.getZ() + extraZ);
      BlockPos ground = LandUtils.getHeight(target.level(), radialPos);
      return !cockatrice.isTargetBlocked(Vec3.atLowerCornerOf(ground)) && cockatrice.distanceToSqr(Vec3.atCenterOf(ground)) > 30.0 ? ground : target.blockPosition();
   }

   @Nullable
   @Override
   protected SoundEvent getAmbientSound() {
      return IafLand.sound("cockatrice_idle");
   }

   @Nullable
   @Override
   protected SoundEvent getHurtSound(DamageSource source) {
      return IafLand.sound("cockatrice_hurt");
   }

   @Nullable
   @Override
   protected SoundEvent getDeathSound() {
      return IafLand.sound("cockatrice_die");
   }

   @Override
   public void handleEntityEvent(byte id) {
      if (id == 45) {
         this.playEffect(true);
      } else if (id == 46) {
         this.playEffect(false);
      } else {
         super.handleEntityEvent(id);
      }
   }

   protected void playEffect(boolean play) {
      ParticleOptions enumparticletypes = ParticleTypes.HEART;
      if (!play) {
         enumparticletypes = ParticleTypes.DAMAGE_INDICATOR;
      }

      for (int i = 0; i < 7; i++) {
         double d0 = this.random.nextGaussian() * 0.02;
         double d1 = this.random.nextGaussian() * 0.02;
         double d2 = this.random.nextGaussian() * 0.02;
         this.level()
            .addParticle(
               enumparticletypes,
               this.getX() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
               this.getY() + 0.5 + (double)(this.random.nextFloat() * this.getBbHeight()),
               this.getZ() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
               d0,
               d1,
               d2
            );
      }
   }
}
