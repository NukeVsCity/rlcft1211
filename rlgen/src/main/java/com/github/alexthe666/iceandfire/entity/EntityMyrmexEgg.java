package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.structures.WorldGenMyrmexHive;
import com.github.alexthe666.iceandfire.world.MyrmexWorldData;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Ice and Fire 1.7.1 EntityMyrmexEgg: hatches after myrmexEggTicks out of the sky; a queen egg founds a new hive. */
public class EntityMyrmexEgg extends Mob implements IBlacklistedFromStatues, IDeadMob {
   private static final EntityDataAccessor<Boolean> MYRMEX_TYPE = SynchedEntityData.defineId(EntityMyrmexEgg.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> MYRMEX_AGE = SynchedEntityData.defineId(EntityMyrmexEgg.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> MYRMEX_CASTE = SynchedEntityData.defineId(EntityMyrmexEgg.class, EntityDataSerializers.INT);
   public UUID hiveUUID;

   public EntityMyrmexEgg(EntityType<? extends EntityMyrmexEgg> type, Level worldIn) {
      super(type, worldIn);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.0).add(Attributes.MAX_HEALTH, 10.0);
   }

   @Override
   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putBoolean("Jungle", this.isJungle());
      tag.putInt("MyrmexAge", this.getMyrmexAge());
      tag.putInt("MyrmexCaste", this.getMyrmexCaste());
      tag.putUUID("HiveUUID", this.hiveUUID == null ? (this.hiveUUID = UUID.randomUUID()) : this.hiveUUID);
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setJungle(tag.getBoolean("Jungle"));
      this.setMyrmexAge(tag.getInt("MyrmexAge"));
      this.setMyrmexCaste(tag.getInt("MyrmexCaste"));
      // 1.12 read "hiveUUID" (lower case) while writing "HiveUUID": eggs forget their hive when reloaded
      this.hiveUUID = tag.hasUUID("hiveUUID") ? tag.getUUID("hiveUUID") : new UUID(0L, 0L);
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(MYRMEX_TYPE, false);
      builder.define(MYRMEX_AGE, 0);
      builder.define(MYRMEX_CASTE, 0);
   }

   public boolean isJungle() {
      return this.entityData.get(MYRMEX_TYPE);
   }

   public void setJungle(boolean jungle) {
      this.entityData.set(MYRMEX_TYPE, jungle);
   }

   public int getMyrmexAge() {
      return this.entityData.get(MYRMEX_AGE);
   }

   public void setMyrmexAge(int i) {
      this.entityData.set(MYRMEX_AGE, i);
   }

   public int getMyrmexCaste() {
      return this.entityData.get(MYRMEX_CASTE);
   }

   public void setMyrmexCaste(int i) {
      this.entityData.set(MYRMEX_CASTE, i);
   }

   public boolean canSeeSky() {
      return this.level().canSeeSky(this.blockPosition());
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.canSeeSky()) {
         this.setMyrmexAge(this.getMyrmexAge() + 1);
      }

      if (this.getMyrmexAge() > IceAndFire.CONFIG.myrmexEggTicks && this.isAlive()) {
         this.discard();
         EntityMyrmexBase myrmex;
         switch (this.getMyrmexCaste()) {
            case 1:
               myrmex = IafLandMobs.MYRMEX_SOLDIER.get().create(this.level());
               break;
            case 2:
               myrmex = IafLandMobs.MYRMEX_ROYAL.get().create(this.level());
               break;
            case 3:
               myrmex = IafLandMobs.MYRMEX_SENTINEL.get().create(this.level());
               break;
            case 4:
               myrmex = IafLandMobs.MYRMEX_QUEEN.get().create(this.level());
               break;
            default:
               myrmex = IafLandMobs.MYRMEX_WORKER.get().create(this.level());
         }

         myrmex.setJungleVariant(this.isJungle());
         myrmex.setGrowthStage(0);
         myrmex.moveTo(this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F);
         if (myrmex instanceof EntityMyrmexQueen) {
            MyrmexHive hive = new MyrmexHive(this.level(), this.blockPosition(), 100);
            Player player = this.level().getNearestPlayer(this, 30.0);
            if (player != null) {
               hive.hasOwner = true;
               hive.ownerUUID = player.getUUID();
               if (!this.level().isClientSide) {
                  hive.modifyPlayerReputation(player.getUUID(), 100);
               }
            }

            MyrmexWorldData.addHive(this.level(), hive);
            myrmex.setHive(hive);
         } else {
            MyrmexHive hive = MyrmexWorldData.get(this.level()).getHiveFromUUID(this.hiveUUID);
            if (!this.level().isClientSide && hive != null && this.distanceToSqr(Vec3.atLowerCornerOf(hive.getCenter())) < 2000.0) {
               myrmex.setHive(hive);
            }
         }

         if (!this.level().isClientSide) {
            this.level().addFreshEntity(myrmex);
         }

         this.level().playLocalSound(this.getX(), this.getY() + (double)this.getEyeHeight(), this.getZ(), IafLand.sound("dragon_hatch"), this.getSoundSource(), 2.5F, 1.0F, false);
      }
   }

   @Override
   public boolean isNoAi() {
      return false;
   }

   @Nullable
   @Override
   public SoundEvent getHurtSound(DamageSource damageSourceIn) {
      return null;
   }

   @Override
   public boolean hurt(DamageSource dmg, float var2) {
      if (dmg.is(DamageTypes.IN_WALL)) {
         return false;
      } else {
         if (!this.level().isClientSide && !dmg.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            this.spawnAtLocation(this.getItem(), 0.0F);
         }

         this.discard();
         return super.hurt(dmg, var2);
      }
   }

   private ItemStack getItem() {
      return MyrmexTrades.egg(this.isJungle(), this.getMyrmexCaste());
   }

   @Override
   public boolean isPushable() {
      return false;
   }

   @Override
   protected void doPush(Entity entity) {
   }

   @Override
   public boolean fireImmune() {
      return true;
   }

   @Override
   public boolean canBeTurnedToStone() {
      return false;
   }

   public void onPlayerPlace(Player player) {
   }

   @Override
   public boolean isMobDead() {
      return true;
   }

   public boolean isInNursery() {
      MyrmexHive hive = MyrmexWorldData.get(this.level()).getNearestHive(this.blockPosition(), 100);
      if (hive != null
         && hive.getRooms(WorldGenMyrmexHive.RoomType.NURSERY).isEmpty()
         && hive.getRandomRoom(WorldGenMyrmexHive.RoomType.NURSERY, this.getRandom(), this.blockPosition()) != null) {
         return false;
      } else if (hive != null) {
         BlockPos nursery = hive.getRandomRoom(WorldGenMyrmexHive.RoomType.NURSERY, this.getRandom(), this.blockPosition());
         return this.distanceToSqr(Vec3.atCenterOf(nursery)) < 45.0;
      } else {
         return false;
      }
   }
}
