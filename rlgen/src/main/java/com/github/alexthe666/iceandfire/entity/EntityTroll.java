package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.ai.TrollAIFleeSun;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIAttackMelee;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIHurtByTarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAINearestAttackableTarget;
import com.github.alexthe666.iceandfire.enums.EnumTroll;
import javax.annotation.Nullable;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.ilexiconn.llibrary.server.animation.AnimationHandler;
import net.ilexiconn.llibrary.server.animation.IAnimatedEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;

/** Ice and Fire 1.7.1 EntityTroll. */
public class EntityTroll extends Monster implements IAnimatedEntity, IVillagerFear {
   private int animationTick;
   private Animation currentAnimation;
   private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(EntityTroll.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> WEAPON = SynchedEntityData.defineId(EntityTroll.class, EntityDataSerializers.INT);
   public static final Animation ANIMATION_STRIKE_HORIZONTAL = Animation.create(20);
   public static final Animation ANIMATION_STRIKE_VERTICAL = Animation.create(20);
   public static final Animation ANIMATION_SPEAK = Animation.create(10);
   public static final Animation ANIMATION_ROAR = Animation.create(25);
   public float stoneProgress;
   public static final ResourceKey<LootTable> FOREST_LOOT = ResourceKey.create(Registries.LOOT_TABLE, IafLand.rl("troll_forest"));
   public static final ResourceKey<LootTable> FROST_LOOT = ResourceKey.create(Registries.LOOT_TABLE, IafLand.rl("troll_frost"));
   public static final ResourceKey<LootTable> MOUNTAIN_LOOT = ResourceKey.create(Registries.LOOT_TABLE, IafLand.rl("troll_mountain"));

   public EntityTroll(EntityType<? extends EntityTroll> type, Level worldIn) {
      super(type, worldIn);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MOVEMENT_SPEED, 0.35)
         .add(Attributes.ATTACK_DAMAGE, IceAndFire.CONFIG.trollAttackStrength)
         .add(Attributes.MAX_HEALTH, IceAndFire.CONFIG.trollMaxHealth)
         .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
         .add(Attributes.ARMOR, 9.0);
   }

   @Override
   public boolean isNoAi() {
      return EntityGorgon.isStoneMob(this) || super.isNoAi();
   }

   /** getCanSpawnHere: the config roll, out of the sky, at Y 50 or below, then the 1.12 EntityMob rules. */
   public static boolean checkTrollSpawnRules(EntityType<? extends Monster> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
      return random.nextInt(IceAndFire.CONFIG.trollSpawnCheckChance + 1) == 0
         && !level.canSeeSky(pos)
         && pos.getY() <= 50
         && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(1, new TrollAIFleeSun(this, 1.0));
      this.goalSelector.addGoal(2, new EntityAIAttackMelee(this, 1.0, true));
      this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0));
      this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F, 1.0F));
      this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new EntityAIHurtByTarget(this, false));
      this.targetSelector.addGoal(2, new EntityAINearestAttackableTarget<>(this, Villager.class, false));
      this.targetSelector.addGoal(2, new EntityAINearestAttackableTarget<>(this, Player.class, false));
      ((GroundPathNavigation)this.getNavigation()).setAvoidSun(true);
   }

   @Override
   public boolean doHurtTarget(net.minecraft.world.entity.Entity entityIn) {
      if (this.getRandom().nextBoolean()) {
         this.setAnimation(ANIMATION_STRIKE_VERTICAL);
      } else {
         this.setAnimation(ANIMATION_STRIKE_HORIZONTAL);
      }

      return true;
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(VARIANT, 0);
      builder.define(WEAPON, 0);
   }

   private int getVariant() {
      return this.entityData.get(VARIANT);
   }

   private void setVariant(int variant) {
      this.entityData.set(VARIANT, variant);
   }

   public void setTrollType(EnumTroll variant) {
      this.setVariant(variant.ordinal());
   }

   /** 1.12 getType() (renamed: 1.21 Entity.getType() is the entity type) */
   public EnumTroll getTrollType() {
      return EnumTroll.values()[Mth.clamp(this.getVariant(), 0, EnumTroll.values().length - 1)];
   }

   private int getWeapon() {
      return this.entityData.get(WEAPON);
   }

   private void setWeapon(int variant) {
      this.entityData.set(WEAPON, variant);
   }

   public void setWeaponType(EnumTroll.Weapon variant) {
      this.setWeapon(variant.ordinal());
   }

   public EnumTroll.Weapon getWeaponType() {
      return EnumTroll.Weapon.values()[Mth.clamp(this.getWeapon(), 0, EnumTroll.Weapon.values().length - 1)];
   }

   @Override
   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      compound.putInt("Variant", this.getVariant());
      compound.putInt("Weapon", this.getWeapon());
      compound.putFloat("StoneProgress", this.stoneProgress);
   }

   @Override
   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      this.setVariant(compound.getInt("Variant"));
      this.setWeapon(compound.getInt("Weapon"));
      this.stoneProgress = compound.getFloat("StoneProgress");
   }

   @Nullable
   @Override
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData livingdata) {
      livingdata = super.finalizeSpawn(level, difficulty, spawnType, livingdata);
      this.setTrollType(EnumTroll.getBiomeType(level.getBiome(this.blockPosition())));
      this.setWeaponType(EnumTroll.getWeaponForType(this.getTrollType()));
      return livingdata;
   }

   @Override
   public boolean hurt(DamageSource source, float damage) {
      return source.getMsgId().contains("arrow") ? false : super.hurt(source, damage);
   }

   @Override
   protected ResourceKey<LootTable> getDefaultLootTable() {
      switch (this.getTrollType()) {
         case MOUNTAIN:
            return MOUNTAIN_LOOT;
         case FROST:
            return FROST_LOOT;
         case FOREST:
         default:
            return FOREST_LOOT;
      }
   }

   @Override
   protected int getBaseExperienceReward() {
      return 15 + this.level().random.nextInt(10);
   }

   @Override
   protected void tickDeath() {
      super.tickDeath();
      if (this.deathTime == 20 && !this.level().isClientSide && IceAndFire.CONFIG.trollsDropWeapon) {
         if (this.getRandom().nextInt(3) == 0) {
            ItemStack weaponStack = new ItemStack(this.getWeaponType().item == null ? Items.AIR : this.getWeaponType().item, 1);
            int dmg = this.getRandom().nextInt(250);
            if (weaponStack.isDamageableItem()) {
               weaponStack.setDamageValue(Math.min(weaponStack.getMaxDamage(), weaponStack.getDamageValue() + dmg));
            }
            this.dropItemAt(weaponStack, this.getX(), this.getY(), this.getZ());
         } else {
            ItemStack brokenDrop = new ItemStack(Blocks.STONE_BRICKS, this.getRandom().nextInt(2) + 1);
            ItemStack brokenDrop2 = new ItemStack(Blocks.STONE_BRICKS, this.getRandom().nextInt(2) + 1);
            if (this.getWeaponType() == EnumTroll.Weapon.AXE) {
               brokenDrop = new ItemStack(Items.STICK, this.getRandom().nextInt(2) + 1);
               brokenDrop2 = new ItemStack(Blocks.COBBLESTONE, this.getRandom().nextInt(2) + 1);
            }

            if (this.getWeaponType() == EnumTroll.Weapon.COLUMN) {
               brokenDrop = new ItemStack(Blocks.CRACKED_STONE_BRICKS, this.getRandom().nextInt(2) + 1);
               brokenDrop2 = new ItemStack(Blocks.CRACKED_STONE_BRICKS, this.getRandom().nextInt(2) + 1);
            }

            if (this.getWeaponType() == EnumTroll.Weapon.COLUMN_FOREST) {
               brokenDrop = new ItemStack(Blocks.MOSSY_STONE_BRICKS, this.getRandom().nextInt(2) + 1);
               brokenDrop2 = new ItemStack(Blocks.CRACKED_STONE_BRICKS, this.getRandom().nextInt(2) + 1);
            }

            if (this.getWeaponType() == EnumTroll.Weapon.COLUMN_FROST) {
               brokenDrop = new ItemStack(Blocks.STONE_BRICKS, this.getRandom().nextInt(2) + 1);
               brokenDrop2 = new ItemStack(Items.SNOWBALL, this.getRandom().nextInt(4) + 1);
            }

            if (this.getWeaponType() == EnumTroll.Weapon.HAMMER) {
               brokenDrop = new ItemStack(Items.BONE, this.getRandom().nextInt(2) + 1);
               brokenDrop2 = new ItemStack(Blocks.COBBLESTONE, this.getRandom().nextInt(2) + 1);
            }

            if (this.getWeaponType() == EnumTroll.Weapon.TRUNK) {
               brokenDrop = new ItemStack(Blocks.OAK_LOG, this.getRandom().nextInt(2) + 1);
               brokenDrop2 = new ItemStack(Blocks.OAK_LOG, this.getRandom().nextInt(2) + 1);
            }

            if (this.getWeaponType() == EnumTroll.Weapon.TRUNK_FROST) {
               brokenDrop = new ItemStack(Blocks.SPRUCE_LOG, this.getRandom().nextInt(4) + 1);
               brokenDrop2 = new ItemStack(Items.SNOWBALL, this.getRandom().nextInt(4) + 1);
            }

            this.dropItemAt(brokenDrop, this.getX(), this.getY(), this.getZ());
            this.dropItemAt(brokenDrop2, this.getX(), this.getY(), this.getZ());
         }
      }
   }

   @Nullable
   private ItemEntity dropItemAt(ItemStack stack, double x, double y, double z) {
      if (!stack.isEmpty()) {
         ItemEntity entityitem = new ItemEntity(this.level(), x, y, z, stack);
         entityitem.setDefaultPickUpDelay();
         this.level().addFreshEntity(entityitem);
         return entityitem;
      } else {
         return null;
      }
   }

   @Override
   public void aiStep() {
      super.aiStep();
      boolean stone = EntityGorgon.isStoneMob(this);
      if (stone && this.stoneProgress < 20.0F) {
         this.stoneProgress += 2.0F;
      } else if (!stone && this.stoneProgress > 0.0F) {
         this.stoneProgress -= 2.0F;
      }

      if (!stone && this.getAnimation() == NO_ANIMATION && this.getTarget() != null && this.getRandom().nextInt(100) == 0) {
         this.setAnimation(ANIMATION_ROAR);
      }

      if (this.getAnimation() == ANIMATION_ROAR && this.getAnimationTick() == 5) {
         this.playSound(IafLand.sound("troll_roar"), 1.0F, 1.0F);
      }

      if (!stone && this.getHealth() < this.getMaxHealth() && this.tickCount % 30 == 0) {
         this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 30, 1, false, false));
      }

      if (this.level().isDay() && !this.level().isClientSide) {
         float f = this.getLightLevelDependentMagicValue();
         BlockPos blockpos = this.getVehicle() instanceof Boat
            ? BlockPos.containing(this.getX(), (double)Math.round(this.getY()), this.getZ()).above()
            : BlockPos.containing(this.getX(), (double)Math.round(this.getY()), this.getZ());
         if (f > 0.5F && this.level().canSeeSky(blockpos)) {
            StoneEntityProperties properties = StoneEntityProperties.get(this);
            if (properties != null && !properties.isStone) {
               LandUtils.setStone(this, true);
               this.setDeltaMovement(Vec3.ZERO);
               this.setAnimation(NO_ANIMATION);
               this.playSound(IafLand.sound("gorgon_turn_stone"), 1.0F, 1.0F);
            }
         }
      }

      if (this.getAnimation() == ANIMATION_STRIKE_VERTICAL && this.getAnimationTick() == 10) {
         float weaponX = (float)(this.getX() + 1.9F * Math.cos((double)(this.yBodyRot + 90.0F) * Math.PI / 180.0));
         float weaponZ = (float)(this.getZ() + 1.9F * Math.sin((double)(this.yBodyRot + 90.0F) * Math.PI / 180.0));
         float weaponY = (float)(this.getY() + 0.2F);
         BlockState state = this.level().getBlockState(BlockPos.containing((double)weaponX, (double)(weaponY - 1.0F), (double)weaponZ));

         for (int i = 0; i < 20; i++) {
            double motionX = this.getRandom().nextGaussian() * 0.07;
            double motionY = this.getRandom().nextGaussian() * 0.07;
            double motionZ = this.getRandom().nextGaussian() * 0.07;
            if (state.blocksMotion() && this.level().isClientSide) {
               this.level()
                  .addParticle(
                     new BlockParticleOption(ParticleTypes.BLOCK, state.getBlock().defaultBlockState()),
                     (double)(weaponX + (this.getRandom().nextFloat() - 0.5F)),
                     (double)(weaponY + (this.getRandom().nextFloat() - 0.5F)),
                     (double)(weaponZ + (this.getRandom().nextFloat() - 0.5F)),
                     motionX,
                     motionY,
                     motionZ
                  );
            }
         }
      }

      if (this.getAnimation() == ANIMATION_STRIKE_VERTICAL
         && this.getTarget() != null
         && this.distanceToSqr(this.getTarget()) < 4.0
         && this.getAnimationTick() == 10
         && this.deathTime <= 0) {
         this.getTarget().hurt(this.damageSources().mobAttack(this), (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE));
      }

      if (this.getAnimation() == ANIMATION_STRIKE_HORIZONTAL
         && this.getTarget() != null
         && this.distanceToSqr(this.getTarget()) < 4.0
         && this.getAnimationTick() == 10
         && this.deathTime <= 0) {
         LivingEntity target = this.getTarget();
         target.hurt(this.damageSources().mobAttack(this), (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE));
         // the 1.12 code computes a knockback direction from the target's input and then throws the target along its own yaw
         float f5 = Mth.sin(this.getYRot() * (float)(Math.PI / 180.0));
         float f6 = Mth.cos(this.getYRot() * (float)(Math.PI / 180.0));
         target.setDeltaMovement((double)f5, 0.4F, (double)f6);
         target.hurtMarked = true;
      }

      if (this.getNavigation().isDone()
         && this.getTarget() != null
         && this.distanceToSqr(this.getTarget()) > 3.0
         && this.distanceToSqr(this.getTarget()) < 30.0
         && this.level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_MOBGRIEFING)) {
         this.lookAt(this.getTarget(), 30.0F, 30.0F);
         if (this.getAnimation() == NO_ANIMATION && this.random.nextInt(15) == 0) {
            this.setAnimation(ANIMATION_STRIKE_VERTICAL);
         }

         if (this.getAnimation() == ANIMATION_STRIKE_VERTICAL && this.getAnimationTick() == 10) {
            float weaponX = (float)(this.getX() + 1.9F * Math.cos((double)(this.yBodyRot + 90.0F) * Math.PI / 180.0));
            float weaponZ = (float)(this.getZ() + 1.9F * Math.sin((double)(this.yBodyRot + 90.0F) * Math.PI / 180.0));
            float weaponY = (float)(this.getY() + (double)(this.getEyeHeight() / 2.0F));
            BlockBreakExplosion explosion = new BlockBreakExplosion(this.level(), this, (double)weaponX, (double)weaponY, (double)weaponZ, 1.0F + this.getRandom().nextFloat());
            explosion.doExplosionA();
            explosion.doExplosionB(true);
            this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.0F, 1.0F);
         }
      }

      if (this.getAnimation() == ANIMATION_STRIKE_VERTICAL && this.getAnimationTick() == 10) {
         this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 2.5F, 0.5F);
      }

      if (this.getAnimation() == ANIMATION_STRIKE_HORIZONTAL && this.getAnimationTick() == 10) {
         this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 2.5F, 0.5F);
      }

      AnimationHandler.INSTANCE.updateAnimations(this);
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

   @Nullable
   @Override
   protected SoundEvent getAmbientSound() {
      return IafLand.sound("troll_idle");
   }

   @Nullable
   @Override
   protected SoundEvent getHurtSound(DamageSource source) {
      return IafLand.sound("troll_hurt");
   }

   @Nullable
   @Override
   protected SoundEvent getDeathSound() {
      return IafLand.sound("troll_die");
   }

   @Override
   public Animation[] getAnimations() {
      return new Animation[]{NO_ANIMATION, ANIMATION_STRIKE_HORIZONTAL, ANIMATION_STRIKE_VERTICAL, ANIMATION_SPEAK, ANIMATION_ROAR};
   }
}
