package com.lycanitesmobs.core.entity;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
import com.lycanitesmobs.LycEntities;
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
import net.minecraft.world.entity.ambient.*;
import net.minecraft.world.entity.boss.enderdragon.*;
import net.minecraft.world.entity.boss.wither.*;
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
import net.minecraft.world.entity.vehicle.*;
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
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.entity.damagesources.MinionEntityDamageSource;
import com.lycanitesmobs.core.entity.goals.actions.BegGoal;
import com.lycanitesmobs.core.entity.goals.actions.FollowOwnerGoal;
import com.lycanitesmobs.core.entity.goals.actions.StayGoal;
import com.lycanitesmobs.core.entity.goals.targeting.CopyOwnerAttackTargetGoal;
import com.lycanitesmobs.core.entity.goals.targeting.DefendOwnerGoal;
import com.lycanitesmobs.core.entity.goals.targeting.RevengeOwnerGoal;
import com.lycanitesmobs.core.info.CreatureKnowledge;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.ElementInfo;
import com.lycanitesmobs.core.item.ChargeItem;
import com.lycanitesmobs.core.item.consumable.CreatureTreatItem;
import com.lycanitesmobs.core.item.special.ItemSoulstone;
import java.util.HashMap;
import java.util.UUID;


public class TameableCreatureEntity extends AgeableCreatureEntity implements OwnableEntity {
   public float hunger = this.getCreatureHungerMax();
   public float stamina = this.getStaminaMax();
   public float staminaRecovery = 0.5F;
   public float sittingGuardRange = 16.0F;
   public UUID ownerUUID;
   public boolean fromSummoningPedestal = false;
   protected static final EntityDataAccessor<Byte> TAMED = SynchedEntityData.defineId(TameableCreatureEntity.class, EntityDataSerializers.BYTE);
   protected static final EntityDataAccessor<Optional<UUID>> OWNER_ID = SynchedEntityData.defineId(TameableCreatureEntity.class, EntityDataSerializers.OPTIONAL_UUID);
   protected static final EntityDataAccessor<Float> HUNGER = SynchedEntityData.defineId(TameableCreatureEntity.class, EntityDataSerializers.FLOAT);
   protected static final EntityDataAccessor<Float> STAMINA = SynchedEntityData.defineId(TameableCreatureEntity.class, EntityDataSerializers.FLOAT);
   protected static final EntityDataAccessor<Integer> LOYALTY = SynchedEntityData.defineId(TameableCreatureEntity.class, EntityDataSerializers.INT);

   public TameableCreatureEntity(EntityType<? extends BaseCreatureEntity> type, Level world) {
      super(type, world);
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(TAMED, (byte)0);
      builder.define(OWNER_ID, Optional.empty());
      builder.define(HUNGER, this.getCreatureHungerMax());
      builder.define(STAMINA, this.getStaminaMax());
   }

   @Override
   protected void initEntityAI() {
      this.targetTasks.addTask(this.nextReactTargetIndex++, new RevengeOwnerGoal(this));
      this.targetTasks.addTask(this.nextReactTargetIndex++, new CopyOwnerAttackTargetGoal(this));
      this.tasks.addTask(this.nextTravelGoalIndex++, new StayGoal(this));
      super.initEntityAI();
      this.targetTasks.addTask(this.nextSpecialTargetIndex++, new DefendOwnerGoal(this));
      this.tasks
         .addTask(
            this.nextTravelGoalIndex++,
            new FollowOwnerGoal(this).setStrayDistance((double)CreatureManager.getInstance().config.petFollowDistance).setLostDistance(32.0).setSpeed(1.0)
         );
      this.tasks.addTask(this.nextIdleGoalIndex++, new BegGoal(this));
   }

   @Override
   public Component getName() {
      if (this.isTamed() && CreatureManager.getInstance().config.ownerTags) {
         String ownerName = this.getOwnerName();
         String ownerSuffix = "'s ";
         if (ownerName != null
            && ownerName.length() > 0
            && ("s".equals(ownerName.substring(ownerName.length() - 1)) || "S".equals(ownerName.substring(ownerName.length() - 1)))) {
            ownerSuffix = "' ";
         }

         String ownedName = ownerName + ownerSuffix + this.getFullName();
         return this.hasCustomName() ? super.getName() : Component.literal(ownedName);
      } else {
         return super.getName();
      }
   }

   public boolean hasCustomName() {
      return !CreatureManager.getInstance().config.ownerTags ? false : super.hasCustomName();
   }

   @Override
   protected boolean canDespawn() {
      return this.isTamed() ? false : super.canDespawn();
   }

   @Override
   public boolean despawnCheck() {
      if (this.level().isClientSide) {
         return false;
      } else {
         if (this.getPetEntry() != null) {
            if (this.getPetEntry().entity != this && this.getPetEntry().entity != null) {
               return true;
            }

            if (this.getPetEntry().host == null || !this.getPetEntry().host.isAlive()) {
               this.getPetEntry().saveEntityNBT();
               return true;
            }
         }

         return this.isTamed() && !this.isTemporary ? false : super.despawnCheck();
      }
   }

   @Override
   public boolean isPersistant() {
      return this.isTamed() || super.isPersistant();
   }

   @Override
   public boolean canBeLeashedTo(Player player) {
      return this.isTamed() && player == this.getPlayerOwner() ? true : super.canBeLeashedTo(player);
   }

   @Override
   public void testLeash(float distance) {
      if (this.isSitting() && distance > 10.0F) {
         this.dropLeash(true, true);
      } else {
         super.testLeash(distance);
      }
   }

   @Override
   public void aiStep() {
      super.aiStep();
      this.staminaUpdate();
      if (!this.level().isClientSide && this.isPet() && this.distanceToSqr(this.getPlayerOwner()) <= 64.0) {
         this.ownerEffects();
      }
   }

   public void staminaUpdate() {
      if (!this.level().isClientSide) {
         if (this.stamina < this.getStaminaMax() && this.staminaRecovery >= this.getStaminaRecoveryMax() / 2.0F) {
            this.setStamina(Math.min(this.stamina + this.staminaRecovery, this.getStaminaMax()));
         }

         if (this.staminaRecovery < this.getStaminaRecoveryMax()) {
            this.staminaRecovery = Math.min(
               this.staminaRecovery + this.getStaminaRecoveryMax() / (float)this.getStaminaRecoveryWarmup(), this.getStaminaRecoveryMax()
            );
         }
      }
   }

   public void ownerEffects() {
      if (!this.canBurn()) {
         this.getPlayerOwner().addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 105, 1));
      }

      for (Object possibleEffect : this.getPlayerOwner().getActiveEffects().toArray(new Object[0])) {
         if (possibleEffect instanceof MobEffectInstance) {
            MobEffectInstance effectInstance = (MobEffectInstance)possibleEffect;
            if (!this.canBeAffected(effectInstance)) {
               this.getPlayerOwner().removeEffect(effectInstance.getEffect());
            }
         }
      }
   }

   public boolean isPet() {
      if (!this.isTamed() || this.getPlayerOwner() == null) {
         return false;
      } else if (this.creatureInfo.isMountable()) {
         return false;
      } else {
         return this.isTemporary ? false : this.getPetEntry() == null || this.isPetType("pet");
      }
   }

   public void copyPetBehaviourTo(TameableCreatureEntity target) {
      target.setPVP(this.isPVP());
      target.setPassive(this.isPassive());
      target.setAssist(this.isAssisting());
      target.setAggressive(this.isAggressive());
      target.setSitting(this.isSitting());
      target.setFollowing(this.isFollowing());
   }

   public boolean canPerch(LivingEntity target) {
      return !this.creatureInfo.isPerchable() ? false : this.getPlayerOwner() == target;
   }

   @Override
   public HashMap<Integer, String> getInteractCommands(Player player, InteractionHand hand, ItemStack itemStack) {
      HashMap<Integer, String> commands = new HashMap<>();
      commands.putAll(super.getInteractCommands(player, hand, itemStack));
      if (this.canPerch(player) && !player.isShiftKeyDown() && !this.level().isClientSide) {
         commands.put(BaseCreatureEntity.COMMAND_PIORITIES.MAIN.id, "Perch");
      } else if (!this.level().isClientSide && this.isTamed() && (itemStack == null || player.isShiftKeyDown()) && player == this.getPlayerOwner()) {
         commands.put(BaseCreatureEntity.COMMAND_PIORITIES.MAIN.id, "GUI");
      }

      if (!this.level().isClientSide && itemStack != null && !player.isShiftKeyDown()) {
         if (!this.isTamed() && this.isTamingItem(itemStack) && CreatureManager.getInstance().config.tamingEnabled) {
            commands.put(BaseCreatureEntity.COMMAND_PIORITIES.IMPORTANT.id, "Tame");
         }

         if (this.isTamed() && this.isHealingItem(itemStack) && this.getHealth() < this.getMaxHealth()) {
            commands.put(BaseCreatureEntity.COMMAND_PIORITIES.ITEM_USE.id, "Feed");
         }

         if (this.isTamed() && !this.isTemporary && !this.isPetType("minion") && itemStack.getItem() instanceof ChargeItem) {
            if (this.isLevelingChargeItem(itemStack)) {
               commands.put(BaseCreatureEntity.COMMAND_PIORITIES.ITEM_USE.id, "Charge");
            } else {
               player.sendSystemMessage(Component.literal(LanguageManager.translate("item.lycanitesmobs.charge.creature.invalid")));
            }
         }

         if (this.isTamed() && !this.isBaby() && this.canEquip() && player == this.getPlayerOwner()) {
            String equipSlot = this.inventory.getSlotForEquipment(itemStack);
            if (equipSlot != null
               && (
                  this.inventory.getEquipmentStack(equipSlot) == null || this.inventory.getEquipmentStack(equipSlot).getItem() != itemStack.getItem()
               )) {
               commands.put(BaseCreatureEntity.COMMAND_PIORITIES.EQUIPPING.id, "Equip Item");
            }
         }

         if (itemStack.getItem() instanceof ItemSoulstone && this.isTamed()) {
            commands.put(BaseCreatureEntity.COMMAND_PIORITIES.ITEM_USE.id, "Soulstone");
         }
      }

      return commands;
   }

   @Override
   public boolean performCommand(String command, Player player, InteractionHand hand, ItemStack itemStack) {
      if (command.equals("GUI")) {
         this.playTameSound();
         this.openGUI(player);
         return true;
      } else if (command.equals("Tame")) {
         this.tame(player);
         this.consumePlayersItem(player, hand, itemStack);
         return true;
      } else if (command.equals("Feed")) {
         int healAmount = 4;
         // 1.12 ItemFood.getHealAmount
         net.minecraft.world.food.FoodProperties itemFood = itemStack.getFoodProperties(this);
         if (itemFood != null) {
            healAmount = itemFood.nutrition();
         }

         this.heal((float)healAmount);
         this.playEatSound();
         if (this.level().isClientSide) {
            ParticleOptions particle = ParticleTypes.HEART;
            double d0 = this.random.nextGaussian() * 0.02;
            double d1 = this.random.nextGaussian() * 0.02;
            double d2 = this.random.nextGaussian() * 0.02;

            for (int i = 0; i < 25; i++) {
               this.level()
                  .addParticle(particle, this.getX() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(), this.getY() + 0.5 + (double)(this.random.nextFloat() * this.getBbHeight()), this.getZ() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(), d0, d1, d2);
            }
         }

         this.consumePlayersItem(player, hand, itemStack);
         return true;
      } else if (command.equals("Charge")) {
         this.addExperience(this.getExperienceFromChargeItem(itemStack));
         this.consumePlayersItem(player, hand, itemStack);
         return true;
      } else if (command.equals("Equip Item")) {
         ItemStack equippedItem = this.inventory.getEquipmentStack(this.inventory.getSlotForEquipment(itemStack));
         if (equippedItem != null) {
            this.spawnAtLocation(equippedItem);
         }

         ItemStack equipStack = itemStack.copy();
         equipStack.setCount(1);
         this.inventory.setEquipmentStack(equipStack.copy());
         this.consumePlayersItem(player, hand, itemStack);
         return true;
      } else if (command.equals("Soulstone")) {
         return false;
      } else if (command.equals("Sit")) {
         this.playTameSound();
         this.setTarget(null);
         this.clearMovement();
         this.setSitting(!this.isSitting());
         this.jumping = false;
         return true;
      } else if (command.equals("Perch")) {
         this.playTameSound();
         this.perchOnEntity(player);
         return true;
      } else {
         return super.performCommand(command, player, hand, itemStack);
      }
   }

   @Override
   public boolean canNameTag(Player player) {
      if (!this.isTamed()) {
         return super.canNameTag(player);
      } else {
         return this.isTamed() && player == this.getPlayerOwner() ? super.canNameTag(player) : false;
      }
   }

   @Override
   public void performGUICommand(Player player, byte guiCommandID) {
      if (this.petControlsEnabled()) {
         if (player == this.getOwner()) {
            if (guiCommandID == BaseCreatureEntity.PET_COMMAND_ID.PVP.id) {
               this.setPVP(!this.isPVP());
            } else if (guiCommandID == BaseCreatureEntity.PET_COMMAND_ID.PASSIVE.id) {
               this.setPassive(true);
            } else if (guiCommandID == BaseCreatureEntity.PET_COMMAND_ID.DEFENSIVE.id) {
               this.setPassive(false);
               this.setAssist(false);
               this.setAggressive(false);
            } else if (guiCommandID == BaseCreatureEntity.PET_COMMAND_ID.ASSIST.id) {
               this.setPassive(false);
               this.setAssist(true);
               this.setAggressive(false);
            } else if (guiCommandID == BaseCreatureEntity.PET_COMMAND_ID.AGGRESSIVE.id) {
               this.setPassive(false);
               this.setAssist(true);
               this.setAggressive(true);
            } else if (guiCommandID == BaseCreatureEntity.PET_COMMAND_ID.FOLLOW.id) {
               this.setSitting(false);
               this.setFollowing(true);
            } else if (guiCommandID == BaseCreatureEntity.PET_COMMAND_ID.WANDER.id) {
               this.setSitting(false);
               this.setFollowing(false);
            } else if (guiCommandID == BaseCreatureEntity.PET_COMMAND_ID.SIT.id) {
               this.setSitting(true);
               this.setFollowing(false);
            }

            this.playTameSound();
            if (this.petEntry != null && this.petEntry.summonSet != null) {
               this.petEntry.summonSet.updateBehaviour(this);
            }

            super.performGUICommand(player, guiCommandID);
         }
      }
   }

   public net.minecraft.world.scores.PlayerTeam getTeam() {
      if (this.isTamed()) {
         Entity owner = this.getOwner();
         if (owner != null) {
            return owner.getTeam();
         }
      }

      return super.getTeam();
   }

   public boolean isOnSameTeam(Entity target) {
      if (target == null) {
         return false;
      } else if (this.level().isClientSide) {
         return super.isAlliedTo(target);
      } else if (!this.isTamed()) {
         return super.isAlliedTo(target);
      } else if (target != this.getPlayerOwner() && target != this.getOwner()) {
         if (!(target instanceof Player) || this.level().getServer().isPvpAllowed() && this.isPVP()) {
            if (target instanceof TameableCreatureEntity) {
               TameableCreatureEntity tamedTarget = (TameableCreatureEntity)target;
               if (tamedTarget.isTamed()
                  && (!this.level().getServer().isPvpAllowed() || !this.isPVP() || tamedTarget.getPlayerOwner() == this.getPlayerOwner())) {
                  return true;
               }
            } else if (target instanceof OwnableEntity) {
               OwnableEntity tamedTarget = (OwnableEntity)target;
               if (tamedTarget.getOwner() != null
                  && (!this.level().getServer().isPvpAllowed() || !this.isPVP() || tamedTarget.getOwner() == this.getOwner())) {
                  return true;
               }
            }

            if (this.getPlayerOwner() != null) {
               return this.getPlayerOwner().getVehicle() == target ? true : this.getPlayerOwner().isAlliedTo(target);
            } else if (this.getOwner() == null) {
               return false;
            } else {
               return this.getOwner().getVehicle() == target ? true : this.getOwner().isAlliedTo(target);
            }
         } else {
            return true;
         }
      } else {
         return true;
      }
   }

   @Override
   public boolean doRangedDamage(Entity target, ThrowableProjectile projectile, float damage, boolean noPierce) {
      float totalDamage = damage * ((float)this.creatureStats.getDamage() / 2.0F);
      if (target instanceof Mob) {
         Mob livingTarget = (Mob)target;
         if (this.getPlayerOwner() != null && livingTarget.getHealth() > 0.0F && livingTarget.getHealth() - totalDamage <= 0.0F) {
            EntityDamageSource creditSource = this.getDamageSource(null, true);
            creditSource.setDamageBypassesArmor().setDamageIsAbsolute();
            return target.hurt(creditSource, totalDamage);
         }
      }

      return super.doRangedDamage(target, projectile, damage, noPierce);
   }

   @Override
   public boolean attackEntityAsMob(Entity target, double damageScale) {
      if (!this.isAlive()) {
         return false;
      } else if (target == null) {
         return false;
      } else if (!this.hasLineOfSight(target)) {
         return false;
      } else {
         float damage = this.getAttackDamage(damageScale);
         if (target instanceof Mob) {
            Mob livingTarget = (Mob)target;
            if (this.getPlayerOwner() != null && livingTarget.getHealth() > 0.0F && livingTarget.getHealth() - damage <= 0.0F) {
               EntityDamageSource creditSource = this.getDamageSource(null, true);
               creditSource.setDamageBypassesArmor().setDamageIsAbsolute();
               return target.hurt(creditSource, damage);
            }
         }

         return super.attackEntityAsMob(target, damageScale);
      }
   }

   @Override
   public boolean canAttackClass(Class targetClass) {
      if (this.isPassive()) {
         return false;
      } else {
         return this.isTamed() ? true : super.canAttackClass(targetClass);
      }
   }

   @Override
   public boolean canAttackEntity(LivingEntity targetEntity) {
      if (this.isPassive()) {
         return false;
      } else if (!this.isTamed()) {
         return super.canAttackEntity(targetEntity);
      } else if (this.getOwner() != targetEntity && this.getPlayerOwner() != targetEntity) {
         if (!this.level().isClientSide) {
            boolean canPVP = this.level().getServer().isPvpAllowed() && this.isPVP();
            if (targetEntity instanceof Player && !canPVP) {
               return false;
            }

            if (targetEntity instanceof TameableCreatureEntity) {
               TameableCreatureEntity targetTameable = (TameableCreatureEntity)targetEntity;
               if (targetTameable.isTamed()) {
                  if (!canPVP) {
                     return false;
                  }

                  if (targetTameable.getPlayerOwner() == this.getPlayerOwner()) {
                     return false;
                  }
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public EntityDamageSource getDamageSource(EntityDamageSource nestedDamageSource, boolean playerCredit) {
      if (this.isTamed() && this.getOwner() != null) {
         if (nestedDamageSource == null) {
            nestedDamageSource = (EntityDamageSource)EntityDamageSource.causeMobDamage(this);
         }

         return new MinionEntityDamageSource(nestedDamageSource, this.getOwner(), playerCredit);
      } else {
         return super.getDamageSource(nestedDamageSource, playerCredit);
      }
   }

   @Override
   public boolean hurt(DamageSource damageSrc, float damageAmount) {
      if (this.isInvulnerableTo(damageSrc)) {
         return false;
      } else {
         if (!this.isPassive()) {
            this.setSitting(false);
         }

         Entity entity = damageSrc.getDirectEntity();
         if (entity instanceof ThrowableProjectile) {
            entity = ((ThrowableProjectile)entity).getOwner();
         }

         return this.isTamed() && this.getOwner() == entity ? false : super.hurt(damageSrc, damageAmount);
      }
   }

   @Override
   public boolean isDamageTypeApplicable(String type, DamageSource source, float damage) {
      return "inWall".equals(type) && this.isTamed() ? false : super.isDamageTypeApplicable(type, source, damage);
   }

   @Override
   public boolean isDamageEntityApplicable(Entity entity) {
      if (this.isTamed()) {
         if (entity instanceof Player && this.level().getServer() != null && !this.level().getServer().isPvpAllowed()) {
            return false;
         }

         if (entity == this.getPlayerOwner()) {
            return false;
         }
      }

      return super.isDamageEntityApplicable(entity);
   }

   public void setOwnerId(UUID ownerUUID) {
      this.ownerUUID = ownerUUID;
      this.entityData.set(OWNER_ID, Optional.ofNullable(ownerUUID));
      this.setTamed(ownerUUID != null);
   }

   public LivingEntity getOwner() {
      UUID uuid = this.getOwnerUUID();
      return (LivingEntity)(uuid == null ? super.getOwner() : this.level().getPlayerByUUID(uuid));
   }

   public void setPlayerOwner(Player player) {
      this.setOwnerId(player.getUUID());
   }

   public UUID getOwnerUUID() {
      if (this.level().isClientSide) {
         try {
            return (UUID)this.getUUIDFromDataManager(OWNER_ID).orElse(null);
         } catch (Exception var2) {
         }
      }

      return this.ownerUUID;
   }

   public Player getPlayerOwner() {
      if (this.level().isClientSide) {
         Entity owner = this.getOwner();
         return owner instanceof Player ? (Player)owner : null;
      } else {
         return this.ownerUUID == null ? null : this.level().getPlayerByUUID(this.ownerUUID);
      }
   }

   public String getOwnerName() {
      Entity owner = this.getOwner();
      if (owner != null) {
         return owner instanceof Player ? ((Player)owner).getName().getString() : owner.getName().getString();
      } else {
         return "";
      }
   }

   @Override
   public boolean isTamed() {
      try {
         return (this.getByteFromDataManager(TAMED) & TameableCreatureEntity.TAMED_ID.IS_TAMED.id) != 0;
      } catch (Exception var2) {
         return false;
      }
   }

   public void setTamed(boolean isTamed) {
      byte tamed = this.getByteFromDataManager(TAMED);
      if (isTamed) {
         this.entityData.set(TAMED, (byte)(tamed | TameableCreatureEntity.TAMED_ID.IS_TAMED.id));
         this.spawnEventType = "";
      } else {
         this.entityData.set(TAMED, (byte)(tamed - (tamed & TameableCreatureEntity.TAMED_ID.IS_TAMED.id)));
      }

      this.setCustomNameVisible(isTamed);
   }

   public boolean isTamingItem(ItemStack itemstack) {
      if (!itemstack.isEmpty() && this.creatureInfo.creatureType != null && !this.isBoss()) {
         if (itemstack.getItem() instanceof CreatureTreatItem) {
            CreatureTreatItem itemTreat = (CreatureTreatItem)itemstack.getItem();
            if (itemTreat.getCreatureType() == this.creatureInfo.creatureType) {
               return this.creatureInfo.isTameable();
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public boolean tame(Player player) {
      if (!this.level().isClientSide && !this.isRareVariant() && !this.isBoss()) {
         ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(player);
         if (extendedPlayer == null) {
            return this.isTamed();
         } else {
            extendedPlayer.studyCreature(this, CreatureManager.getInstance().config.creatureTreatKnowledge, false, true);
            if (this.isTamed()) {
               return true;
            } else {
               CreatureKnowledge creatureKnowledge = extendedPlayer.getBeastiary().getCreatureKnowledge(this.creatureInfo.getName());
               if (creatureKnowledge != null && creatureKnowledge.rank >= 2) {
                  CreatureRelationshipEntry relationshipEntry = this.relationships.getOrCreateEntry(player);
                  int reputationAmount = 50 + this.getRandom().nextInt(50);
                  relationshipEntry.increaseReputation(reputationAmount);
                  if (this.creatureInfo.isTameable() && relationshipEntry.getReputation() >= this.creatureInfo.getTamingReputation()) {
                     this.setPlayerOwner(player);
                     this.onTamedByPlayer();
                     this.unsetTemporary();
                     String tameMessage = LanguageManager.translate("message.pet.tamed");
                     tameMessage = tameMessage.replace("%creature%", this.getSpeciesName());
                     player.sendSystemMessage(Component.literal(tameMessage));
                     this.playTameEffect(this.isTamed());
                     // 1.12 player.addStat(...): Lycanites statistics are not registered in 1.21
                     if (this.getPortalCooldown() > this.getDimensionChangingDelay()) {
                        this.setPortalCooldown(this.getDimensionChangingDelay());
                     }
                  }

                  this.playTameEffect(this.isTamed());
                  return this.isTamed();
               } else {
                  return this.isTamed();
               }
            }
         }
      } else {
         return super.isTamed();
      }
   }

   public void onTamedByPlayer() {
      this.refreshAttributes();
      this.clearMovement();
      this.setTarget(null);
      this.setSitting(false);
      this.setFollowing(true);
      this.setPassive(false);
      this.setAggressive(false);
      this.setPVP(true);
      this.playTameSound();
      this.spawnEventType = "";
   }

   @Override
   public void onCreateBaby(AgeableCreatureEntity partner, AgeableCreatureEntity baby) {
      if (this.isTamed() && this.getOwner() instanceof Player && partner instanceof TameableCreatureEntity && baby instanceof TameableCreatureEntity) {
         TameableCreatureEntity partnerTameable = (TameableCreatureEntity)partner;
         TameableCreatureEntity babyTameable = (TameableCreatureEntity)baby;
         if (partnerTameable.getPlayerOwner() == this.getPlayerOwner()) {
            babyTameable.setPlayerOwner((Player)this.getOwner());
         }
      }

      super.onCreateBaby(partner, baby);
   }

   public void handleEntityEvent(byte status) {
      if (status == 7) {
         this.playTameEffect(true);
      } else if (status == 6) {
         this.playTameEffect(false);
      } else {
         super.handleEntityEvent(status);
      }
   }

   public boolean isHealingItem(ItemStack itemStack) {
      return this.creatureInfo.canEat(itemStack);
   }

   public boolean isLevelingChargeItem(ItemStack itemStack) {
      if (itemStack.getItem() instanceof ChargeItem) {
         ChargeItem chargeItem = (ChargeItem)itemStack.getItem();

         for (ElementInfo elementInfo : this.getElements()) {
            if (chargeItem.getElements().contains(elementInfo)) {
               return true;
            }
         }
      }

      return false;
   }

   public int getExperienceFromChargeItem(ItemStack itemStack) {
      int experience = 0;
      if (itemStack.getItem() instanceof ChargeItem) {
         ChargeItem chargeItem = (ChargeItem)itemStack.getItem();

         for (ElementInfo elementInfo : this.getElements()) {
            if (chargeItem.getElements().contains(elementInfo)) {
               experience += ChargeItem.CHARGE_EXPERIENCE;
            }
         }
      }

      return experience;
   }

   @Override
   public boolean isBreedingItem(ItemStack itemStack) {
      return this.creatureInfo.isFarmable()
            || this.isTamed() && !this.isPetType("minion") && !this.isPetType("familiar") && !(this.getHealth() < this.getMaxHealth())
         ? super.isBreedingItem(itemStack)
         : false;
   }

   @Override
   public void summonMinion(LivingEntity minion, double angle, double distance) {
      if (this.getPlayerOwner() != null && minion instanceof TameableCreatureEntity) {
         ((TameableCreatureEntity)minion).setPlayerOwner(this.getPlayerOwner());
         this.copyPetBehaviourTo((TameableCreatureEntity)minion);
      }

      super.summonMinion(minion, angle, distance);
   }

   public boolean petControlsEnabled() {
      return true;
   }

   public byte behaviourBitMask() {
      return this.getByteFromDataManager(TAMED);
   }

   public boolean isSitting() {
      return !this.isTamed() ? false : (this.getByteFromDataManager(TAMED) & TameableCreatureEntity.TAMED_ID.MOVE_SIT.id) != 0;
   }

   public void setSitting(boolean set) {
      if (this.isTamed()) {
         if (!this.petControlsEnabled()) {
            set = false;
         }

         byte tamedStatus = this.getByteFromDataManager(TAMED);
         if (set) {
            this.entityData.set(TAMED, (byte)(tamedStatus | TameableCreatureEntity.TAMED_ID.MOVE_SIT.id));
            this.setHome((int)this.getX(), (int)this.getY(), (int)this.getZ(), this.sittingGuardRange);
         } else {
            this.entityData.set(TAMED, (byte)(tamedStatus - (tamedStatus & TameableCreatureEntity.TAMED_ID.MOVE_SIT.id)));
            this.detachHome();
         }
      }
   }

   public boolean isFollowing() {
      return !this.isTamed() ? false : (this.getByteFromDataManager(TAMED) & TameableCreatureEntity.TAMED_ID.MOVE_FOLLOW.id) != 0;
   }

   public void setFollowing(boolean set) {
      if (!this.petControlsEnabled()) {
         set = false;
      }

      byte tamedStatus = this.getByteFromDataManager(TAMED);
      if (set) {
         this.entityData.set(TAMED, (byte)(tamedStatus | TameableCreatureEntity.TAMED_ID.MOVE_FOLLOW.id));
      } else {
         this.entityData.set(TAMED, (byte)(tamedStatus - (tamedStatus & TameableCreatureEntity.TAMED_ID.MOVE_FOLLOW.id)));
      }
   }

   public boolean isPassive() {
      return !this.isTamed() ? false : (this.getByteFromDataManager(TAMED) & TameableCreatureEntity.TAMED_ID.STANCE_PASSIVE.id) != 0;
   }

   public void setPassive(boolean set) {
      if (!this.petControlsEnabled()) {
         set = false;
      }

      byte tamedStatus = this.getByteFromDataManager(TAMED);
      if (set) {
         this.entityData.set(TAMED, (byte)(tamedStatus | TameableCreatureEntity.TAMED_ID.STANCE_PASSIVE.id));
         this.setTarget(null);
         this.setStealth(0.0F);
      } else {
         this.entityData.set(TAMED, (byte)(tamedStatus - (tamedStatus & TameableCreatureEntity.TAMED_ID.STANCE_PASSIVE.id)));
      }
   }

   @Override
   public boolean isAggressive() {
      return !this.isTamed() ? super.isAggressive() : (this.getByteFromDataManager(TAMED) & TameableCreatureEntity.TAMED_ID.STANCE_AGGRESSIVE.id) != 0;
   }

   public void setAggressive(boolean set) {
      if (!this.petControlsEnabled()) {
         set = false;
      }

      byte tamedStatus = this.getByteFromDataManager(TAMED);
      if (set) {
         this.entityData.set(TAMED, (byte)(tamedStatus | TameableCreatureEntity.TAMED_ID.STANCE_AGGRESSIVE.id));
      } else {
         this.entityData.set(TAMED, (byte)(tamedStatus - (tamedStatus & TameableCreatureEntity.TAMED_ID.STANCE_AGGRESSIVE.id)));
      }
   }

   public boolean isAssisting() {
      return !this.isTamed() ? false : (this.getByteFromDataManager(TAMED) & TameableCreatureEntity.TAMED_ID.STANCE_ASSIST.id) != 0;
   }

   public void setAssist(boolean set) {
      if (!this.petControlsEnabled()) {
         set = true;
      }

      byte tamedStatus = this.getByteFromDataManager(TAMED);
      if (set) {
         this.entityData.set(TAMED, (byte)(tamedStatus | TameableCreatureEntity.TAMED_ID.STANCE_ASSIST.id));
      } else {
         this.entityData.set(TAMED, (byte)(tamedStatus - (tamedStatus & TameableCreatureEntity.TAMED_ID.STANCE_ASSIST.id)));
      }
   }

   public boolean isPVP() {
      return (this.getByteFromDataManager(TAMED) & TameableCreatureEntity.TAMED_ID.PVP.id) != 0;
   }

   public void setPVP(boolean set) {
      if (!this.petControlsEnabled()) {
         set = false;
      }

      byte tamedStatus = this.getByteFromDataManager(TAMED);
      if (set) {
         this.entityData.set(TAMED, (byte)(tamedStatus | TameableCreatureEntity.TAMED_ID.PVP.id));
      } else {
         this.setTarget(null);
         this.entityData.set(TAMED, (byte)(tamedStatus - (tamedStatus & TameableCreatureEntity.TAMED_ID.PVP.id)));
      }
   }

   public float getCreatureHunger() {
      if (this.level() == null) {
         return this.getCreatureHungerMax();
      } else if (!this.level().isClientSide) {
         return this.hunger;
      } else {
         try {
            return this.getFloatFromDataManager(HUNGER);
         } catch (Exception var2) {
            return 0.0F;
         }
      }
   }

   public void setCreatureHunger(float setHunger) {
      this.hunger = setHunger;
   }

   public float getCreatureHungerMax() {
      return 20.0F;
   }

   public float getStamina() {
      if (this.level() != null && this.level().isClientSide) {
         try {
            this.stamina = this.getFloatFromDataManager(STAMINA);
         } catch (Exception var2) {
         }
      }

      return this.stamina;
   }

   public void setStamina(float setStamina) {
      this.stamina = setStamina;
      if (this.level() != null && !this.level().isClientSide) {
         this.entityData.set(STAMINA, setStamina);
      }
   }

   public float getStaminaMax() {
      return 100.0F;
   }

   public float getStaminaRecoveryMax() {
      return 1.0F;
   }

   public int getStaminaRecoveryWarmup() {
      return 200;
   }

   public float getStaminaCost() {
      return 1.0F;
   }

   public void applyStaminaCost() {
      float newStamina = this.getStamina() - this.getStaminaCost();
      if (newStamina < 0.0F) {
         newStamina = 0.0F;
      }

      this.setStamina(newStamina);
      this.staminaRecovery = 0.0F;
   }

   public float getStaminaPercent() {
      return this.getStamina() / this.getStaminaMax();
   }

   public String getStaminaType() {
      return "energy";
   }

   @Override
   public AgeableCreatureEntity createChild(AgeableCreatureEntity baby) {
      AgeableCreatureEntity spawnedBaby = super.createChild(baby);
      UUID ownerId = this.getOwnerUUID();
      if (ownerId != null && spawnedBaby != null && spawnedBaby instanceof TameableCreatureEntity) {
         TameableCreatureEntity tamedBaby = (TameableCreatureEntity)spawnedBaby;
         tamedBaby.setOwnerId(ownerId);
      }

      return spawnedBaby;
   }

   @Override
   public boolean isCreatureType(MobCategory type, boolean forSpawnCount) {
      return forSpawnCount && this.isTamed() ? false : super.isCreatureType(type, forSpawnCount);
   }

   @Override
   public boolean canBeTempted() {
      return super.canBeTempted();
   }

   public boolean shouldDismountInWater(Entity rider) {
      return false;
   }

   @Override
   public boolean canBeSteered() {
      return this.isTamed();
   }

   protected void playTameEffect(boolean success) {
      ParticleOptions particle = ParticleTypes.HEART;
      if (!success) {
         particle = ParticleTypes.SMOKE;
      }

      for (int i = 0; i < 7; i++) {
         double d0 = this.random.nextGaussian() * 0.02;
         double d1 = this.random.nextGaussian() * 0.02;
         double d2 = this.random.nextGaussian() * 0.02;
         this.level()
            .addParticle(particle, this.getX() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(), this.getY() + 0.5 + (double)(this.random.nextFloat() * this.getBbHeight()), this.getZ() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(), d0, d1, d2);
      }
   }

   @Override
   public boolean canBeColored(Player player) {
      return player == null ? true : this.isTamed() && player == this.getPlayerOwner();
   }

   @Override
   public boolean showBossInfo() {
      return this.isTamed() ? false : super.showBossInfo();
   }

   @Override
   public void readAdditionalSaveData(CompoundTag nbtTagCompound) {
      super.readAdditionalSaveData(nbtTagCompound);
      if (nbtTagCompound.hasUUID("OwnerId")) {
         this.setOwnerId(nbtTagCompound.getUUID("OwnerId"));
      } else {
         this.setOwnerId(null);
      }

      if (nbtTagCompound.contains("FromSummoningPedestal")) {
         this.fromSummoningPedestal = nbtTagCompound.getBoolean("FromSummoningPedestal");
      }

      if (nbtTagCompound.contains("Sitting")) {
         this.setSitting(nbtTagCompound.getBoolean("Sitting"));
      } else {
         this.setSitting(false);
      }

      if (nbtTagCompound.contains("Following")) {
         this.setFollowing(nbtTagCompound.getBoolean("Following"));
      } else {
         this.setFollowing(true);
      }

      if (nbtTagCompound.contains("Passive")) {
         this.setPassive(nbtTagCompound.getBoolean("Passive"));
      } else {
         this.setPassive(false);
      }

      if (nbtTagCompound.contains("Aggressive")) {
         this.setAggressive(nbtTagCompound.getBoolean("Aggressive"));
      } else {
         this.setAggressive(false);
      }

      if (nbtTagCompound.contains("PVP")) {
         this.setPVP(nbtTagCompound.getBoolean("PVP"));
      } else {
         this.setPVP(true);
      }

      if (nbtTagCompound.contains("Hunger")) {
         this.setCreatureHunger(nbtTagCompound.getFloat("Hunger"));
      } else {
         this.setCreatureHunger(this.getCreatureHungerMax());
      }

      if (nbtTagCompound.contains("Stamina")) {
         this.setStamina(nbtTagCompound.getFloat("Stamina"));
      }
   }

   @Override
   public void addAdditionalSaveData(CompoundTag nbtTagCompound) {
      super.addAdditionalSaveData(nbtTagCompound);
      if (this.getOwnerUUID() != null) {
         nbtTagCompound.putUUID("OwnerId", this.getOwnerUUID());
      }

      nbtTagCompound.putBoolean("FromSummoningPedestal", this.fromSummoningPedestal);
      nbtTagCompound.putBoolean("Sitting", this.isSitting());
      nbtTagCompound.putBoolean("Following", this.isFollowing());
      nbtTagCompound.putBoolean("Passive", this.isPassive());
      nbtTagCompound.putBoolean("Aggressive", this.isAggressive());
      nbtTagCompound.putBoolean("PVP", this.isPVP());
      nbtTagCompound.putFloat("Hunger", this.getCreatureHunger());
      nbtTagCompound.putFloat("Stamina", this.getStamina());
   }

   @Override
   public int getAmbientSoundInterval() {
      return this.isTamed() ? 600 : super.getAmbientSoundInterval();
   }

   @Override
   protected SoundEvent getAmbientSound() {
      String sound = "_say";
      if (this.isTamed() && this.getHealth() < this.getMaxHealth()) {
         sound = "_beg";
      }

      return AssetManager.getSound(this.getSoundName() + sound);
   }

   public void playTameSound() {
      this.playSound(AssetManager.getSound(this.getSoundName() + "_tame"), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
   }

   public void playEatSound() {
      this.playSound(AssetManager.getSound(this.getSoundName() + "_eat"), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
   }

   public static enum TAMED_ID {
      IS_TAMED((byte)1),
      MOVE_SIT((byte)2),
      MOVE_FOLLOW((byte)4),
      STANCE_PASSIVE((byte)8),
      STANCE_AGGRESSIVE((byte)16),
      STANCE_ASSIST((byte)32),
      PVP((byte)64);

      public final byte id;

      private TAMED_ID(byte value) {
         this.id = value;
      }

      public byte getValue() {
         return this.id;
      }
   }
}
