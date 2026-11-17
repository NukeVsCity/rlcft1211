package com.charles445.simpledifficulty.capability;

import com.charles445.simpledifficulty.api.SDDamageSources;
import com.charles445.simpledifficulty.config.ModConfig;
import com.charles445.simpledifficulty.util.DamageUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;

/** 1.12 ThirstCapability + ThirstStorage. */
public class ThirstCapability implements INBTSerializable<CompoundTag> {
   private float exhaustion = 0.0F;
   private int thirst = 20;
   private float saturation = 5.0F;
   private int ticktimer = 0;
   private int damagecounter = 0;
   private int oldthirst = 0;
   private float oldsaturation = 0.0F;
   private double[] position = null;
   private int packetTimer = 0;

   /** start = TickEvent.Phase.START */
   public void tickUpdate(Player player, Level world, boolean start) {
      if (start) {
         this.packetTimer++;
      } else {
         if (this.position == null) {
            this.position = new double[]{player.getX(), player.getY(), player.getZ()};
         }
         double dx = Math.abs(this.position[0] - player.getX());
         double dy = Math.abs(this.position[1] - player.getY());
         double dz = Math.abs(this.position[2] - player.getZ());
         int moveDistance = (int)Math.round(Math.sqrt(dx * dx + dy * dy + dz * dz) * 100.0);
         this.position = new double[]{player.getX(), player.getY(), player.getZ()};
         if (moveDistance > 1000) {
            moveDistance = 0;
         }
         if (moveDistance > 0) {
            float moveSensitivity = (float)ModConfig.server.thirst.thirstBaseMovement;
            if (player.isInWater() || player.isEyeInFluid(FluidTags.WATER)) {
               moveSensitivity = (float)ModConfig.server.thirst.thirstSwimmingMovement;
            } else if (player.onGround()) {
               if (player.isSprinting()) {
                  moveSensitivity = (float)ModConfig.server.thirst.thirstSprintingMovement;
               } else {
                  moveSensitivity = (float)ModConfig.server.thirst.thirstWalkingMovement;
               }
            }
            this.addThirstExhaustion(moveSensitivity * 0.01F * (float)moveDistance);
         }
         if (this.getThirstExhaustion() > (float)ModConfig.server.thirst.thirstExhaustionLimit) {
            this.addThirstExhaustion(-1.0F * (float)ModConfig.server.thirst.thirstExhaustionLimit);
            if (this.getThirstSaturation() > 0.0F) {
               this.addThirstSaturation(-1.0F);
            } else if (DamageUtil.isModDangerous(world)) {
               this.addThirstLevel(-1);
            }
         }
         if (this.getThirstLevel() <= 0) {
            this.addThirstTickTimer(1);
            if (this.getThirstTickTimer() >= 80) {
               this.setThirstTickTimer(0);
               if (DamageUtil.isModDangerous(world) && DamageUtil.healthAboveDifficulty(world, player)) {
                  float thirstDamageToApply = 1.0F + 1.0F * (float)this.getThirstDamageCounter() * (float)ModConfig.server.thirst.thirstDamageScaling;
                  player.hurt(SDDamageSources.source(player, SDDamageSources.DEHYDRATION), thirstDamageToApply);
                  this.addThirstDamageCounter(1);
               }
            }
         } else {
            this.setThirstTickTimer(0);
            this.setThirstDamageCounter(0);
         }
      }
   }

   public boolean isDirty() {
      return this.thirst != this.oldthirst || this.saturation != this.oldsaturation;
   }

   public void setClean() {
      this.oldthirst = this.thirst;
      this.oldsaturation = this.saturation;
   }

   public float getThirstExhaustion() {
      return this.exhaustion;
   }

   public int getThirstLevel() {
      return this.thirst;
   }

   public float getThirstSaturation() {
      return this.saturation;
   }

   public int getThirstTickTimer() {
      return this.ticktimer;
   }

   public int getThirstDamageCounter() {
      return this.damagecounter;
   }

   public void setThirstExhaustion(float exhaustion) {
      this.exhaustion = Math.max(exhaustion, 0.0F);
      if (!Float.isFinite(this.exhaustion)) {
         this.exhaustion = 0.0F;
      }
   }

   public void setThirstLevel(int thirst) {
      this.thirst = Mth.clamp(thirst, 0, 20);
   }

   public void setThirstSaturation(float saturation) {
      this.saturation = Mth.clamp(saturation, 0.0F, 20.0F);
      if (!Float.isFinite(this.saturation)) {
         this.saturation = 0.0F;
      }
   }

   public void setThirstTickTimer(int ticktimer) {
      this.ticktimer = ticktimer;
   }

   public void setThirstDamageCounter(int damagecounter) {
      this.damagecounter = damagecounter;
   }

   public void addThirstExhaustion(float exhaustion) {
      this.setThirstExhaustion(this.getThirstExhaustion() + exhaustion);
   }

   public void addThirstLevel(int thirst) {
      this.setThirstLevel(this.getThirstLevel() + thirst);
   }

   public void addThirstSaturation(float saturation) {
      this.setThirstSaturation(this.getThirstSaturation() + saturation);
   }

   public void addThirstTickTimer(int ticktimer) {
      this.setThirstTickTimer(this.getThirstTickTimer() + ticktimer);
   }

   public void addThirstDamageCounter(int damagecounter) {
      this.setThirstDamageCounter(this.getThirstDamageCounter() + damagecounter);
   }

   public boolean isThirsty() {
      return this.getThirstLevel() < 20;
   }

   public int getPacketTimer() {
      return this.packetTimer;
   }

   /** 1.12 ThirstStorage.writeNBT */
   @Override
   public CompoundTag serializeNBT(HolderLookup.Provider provider) {
      CompoundTag compound = new CompoundTag();
      compound.putFloat("thirstExhaustion", this.getThirstExhaustion());
      compound.putInt("thirstLevel", this.getThirstLevel());
      compound.putFloat("thirstSaturation", this.getThirstSaturation());
      compound.putInt("thirstTickTimer", this.getThirstTickTimer());
      compound.putInt("thirstDamageCounter", this.getThirstDamageCounter());
      return compound;
   }

   /** 1.12 ThirstStorage.readNBT */
   @Override
   public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compound) {
      if (compound.contains("thirstExhaustion")) {
         this.setThirstExhaustion(compound.getFloat("thirstExhaustion"));
      }
      if (compound.contains("thirstLevel")) {
         this.setThirstLevel(compound.getInt("thirstLevel"));
      }
      if (compound.contains("thirstSaturation")) {
         this.setThirstSaturation(compound.getFloat("thirstSaturation"));
      }
      if (compound.contains("thirstTickTimer")) {
         this.setThirstTickTimer(compound.getInt("thirstTickTimer"));
      }
      if (compound.contains("thirstDamageCounter")) {
         this.setThirstDamageCounter(compound.getInt("thirstDamageCounter"));
      }
   }
}
