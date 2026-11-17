package com.charles445.simpledifficulty.capability;

import com.charles445.simpledifficulty.api.SDPotions;
import com.charles445.simpledifficulty.api.temperature.TemperatureEnum;
import com.charles445.simpledifficulty.api.temperature.TemperatureRegistry;
import com.charles445.simpledifficulty.api.temperature.TemperatureRegistry.TemporaryModifier;
import com.charles445.simpledifficulty.config.ModConfig;
import com.charles445.simpledifficulty.util.TemperatureUtil;
import com.charles445.simpledifficulty.util.WorldUtil;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;

/** 1.12 TemperatureCapability + TemperatureStorage. */
public class TemperatureCapability implements INBTSerializable<CompoundTag> {
   private int temperature = 12;
   private int ticktimer = 0;
   private int damagecounter = 0;
   private Map<String, TemporaryModifier> temporaryModifiers = new HashMap<>();
   private int oldtemperature = 0;
   private int updatetimer = 500;
   private int targettemp = 0;
   private int debugtimer = 0;
   private boolean manualDirty = false;
   private int oldmodifiersize = 0;
   private int packetTimer = 0;

   public int getTemperatureLevel() {
      return this.temperature;
   }

   public int getTemperatureTickTimer() {
      return this.ticktimer;
   }

   public int getTemperatureDamageCounter() {
      return this.damagecounter;
   }

   public void setTemperatureLevel(int temperature) {
      this.temperature = TemperatureUtil.clampTemperature(temperature);
   }

   public void setTemperatureTickTimer(int ticktimer) {
      this.ticktimer = ticktimer;
   }

   public void setTemperatureDamageCounter(int damagecounter) {
      this.damagecounter = damagecounter;
   }

   public void addTemperatureLevel(int temperature) {
      this.setTemperatureLevel(this.getTemperatureLevel() + temperature);
   }

   public void addTemperatureTickTimer(int ticktimer) {
      this.setTemperatureTickTimer(this.getTemperatureTickTimer() + ticktimer);
   }

   public void addTemperatureDamageCounter(int damagecounter) {
      this.setTemperatureDamageCounter(this.getTemperatureDamageCounter() + damagecounter);
   }

   public void tickUpdate(Player player, Level world, boolean start) {
      if (start) {
         this.packetTimer++;
      } else {
         this.debugtimer++;
         if (this.debugtimer >= 40 && ModConfig.server.debugMode) {
            this.debugtimer = 0;
            this.debugRoutine(player, world);
         }
         this.updatetimer++;
         if (this.updatetimer >= 5) {
            this.updatetimer = 0;
            this.targettemp = TemperatureUtil.getPlayerTargetTemperature(player);
         }
         this.addTemperatureTickTimer(1);
         boolean appliedEffect = false;
         if (this.getTemperatureTickTimer() >= this.getTemperatureTickLimit()) {
            this.setTemperatureTickTimer(0);
            int destinationTemp = TemperatureUtil.clampTemperature(this.targettemp);
            if (this.getTemperatureLevel() != destinationTemp) {
               if (this.getTemperatureLevel() > destinationTemp) {
                  this.addTemperatureLevel(-1);
               } else {
                  this.addTemperatureLevel(1);
               }
            }
            TemperatureEnum tempEnum = this.getTemperatureEnum();
            if (tempEnum == TemperatureEnum.BURNING) {
               if (TemperatureEnum.BURNING.getMiddle() < this.getTemperatureLevel() && !player.hasEffect(SDPotions.heat_resist) && !player.isSpectator()
                  && !player.isCreative()) {
                  player.removeEffect(SDPotions.hyperthermia);
                  player.addEffect(new MobEffectInstance(SDPotions.hyperthermia, ModConfig.server.temperature.temperatureDamageDuration, 0));
                  appliedEffect = true;
               }
            } else if (tempEnum == TemperatureEnum.FREEZING && TemperatureEnum.FREEZING.getMiddle() >= this.getTemperatureLevel()
               && !player.hasEffect(SDPotions.cold_resist) && !player.isSpectator() && !player.isCreative()) {
               player.removeEffect(SDPotions.hypothermia);
               player.addEffect(new MobEffectInstance(SDPotions.hypothermia, ModConfig.server.temperature.temperatureDamageDuration, 0));
               appliedEffect = true;
            }
         }
         if (!appliedEffect && this.getTemperatureDamageCounter() != 0) {
            boolean hasHypothermia = player.hasEffect(SDPotions.hypothermia);
            boolean hasHyperthermia = player.hasEffect(SDPotions.hyperthermia);
            if (!hasHypothermia && !hasHyperthermia) {
               this.setTemperatureDamageCounter(0);
            }
         }
         Map<String, TemporaryModifier> tweaks = new HashMap<>();
         for (Entry<String, TemporaryModifier> entry : this.temporaryModifiers.entrySet()) {
            TemporaryModifier tm = entry.getValue();
            if (tm.duration > 0) {
               tweaks.put(entry.getKey(), new TemporaryModifier(tm.temperature, tm.duration - 1));
            }
         }
         this.temporaryModifiers.clear();
         this.temporaryModifiers.putAll(tweaks);
         if (this.oldmodifiersize != this.temporaryModifiers.size()) {
            this.manualDirty = true;
         }
         this.oldmodifiersize = this.temporaryModifiers.size();
      }
   }

   private void debugRoutine(Player player, Level world) {
      player.sendSystemMessage(Component.literal("----------------"));
      BlockPos pos = WorldUtil.getSidedBlockPos(world, player);
      for (TemperatureRegistry.ITemperatureModifier modifier : TemperatureRegistry.modifiers.values()) {
         float modsum = modifier.getWorldInfluence(world, pos) + modifier.getPlayerInfluence(player);
         player.sendSystemMessage(Component.literal(modifier.getName() + " - " + modsum));
         com.charles445.simpledifficulty.SimpleDifficulty.logger.info("SDDEBUG {} {}", modifier.getName(), modsum);
      }
      player.sendSystemMessage(Component.literal("( " + TemperatureUtil.getPlayerTargetTemperature(player) + " )"));
      com.charles445.simpledifficulty.SimpleDifficulty.logger.info("SDDEBUG target {} temp {} thirst {} sat {} pos {}", TemperatureUtil.getPlayerTargetTemperature(player),
         this.temperature, com.charles445.simpledifficulty.api.SDCapabilities.getThirstData(player).getThirstLevel(),
         com.charles445.simpledifficulty.api.SDCapabilities.getThirstData(player).getThirstSaturation(), pos);
      player.sendSystemMessage(Component.literal("TempTickLimit: " + this.getTemperatureTickLimit()));
   }

   private int getTemperatureTickLimit() {
      int tickrange = ModConfig.server.temperature.temperatureTickMax - ModConfig.server.temperature.temperatureTickMin;
      int temprange = TemperatureEnum.BURNING.getUpperBound() - TemperatureEnum.FREEZING.getLowerBound();
      int currentrange = Math.abs(this.getTemperatureLevel() - this.targettemp);
      boolean escapingDanger = this.getTemperatureLevel() <= this.targettemp
         ? this.getTemperatureEnum() == TemperatureEnum.FREEZING
         : this.getTemperatureEnum() == TemperatureEnum.BURNING;
      return Math.max(ModConfig.server.temperature.temperatureTickMin,
         ModConfig.server.temperature.temperatureTickMax - currentrange * tickrange / temprange
            - (escapingDanger ? ModConfig.server.temperature.temperatureTickDangerBoost : 0));
   }

   public boolean isDirty() {
      return this.manualDirty || this.temperature != this.oldtemperature;
   }

   public void setClean() {
      this.oldtemperature = this.temperature;
      this.manualDirty = false;
   }

   public TemperatureEnum getTemperatureEnum() {
      return TemperatureUtil.getTemperatureEnum(this.getTemperatureLevel());
   }

   public ImmutableMap<String, TemporaryModifier> getTemporaryModifiers() {
      return ImmutableMap.copyOf(this.temporaryModifiers);
   }

   public void setTemporaryModifier(String name, float temp, int duration) {
      if (temp != 0.0F && Float.isFinite(temp)) {
         if (this.temporaryModifiers.containsKey(name)) {
            this.manualDirty = true;
         }
         this.temporaryModifiers.put(name, new TemporaryModifier(temp, duration));
      }
   }

   public void clearTemporaryModifiers() {
      this.temporaryModifiers.clear();
   }

   public int getPacketTimer() {
      return this.packetTimer;
   }

   /** 1.12 TemperatureStorage.writeNBT */
   @Override
   public CompoundTag serializeNBT(HolderLookup.Provider provider) {
      CompoundTag compound = new CompoundTag();
      ListTag temporaryList = new ListTag();
      for (Entry<String, TemporaryModifier> entry : this.getTemporaryModifiers().entrySet()) {
         CompoundTag c = new CompoundTag();
         c.putString("name", entry.getKey());
         c.putFloat("temperature", entry.getValue().temperature);
         c.putInt("duration", entry.getValue().duration);
         temporaryList.add(c);
      }
      compound.putInt("temperatureLevel", this.getTemperatureLevel());
      compound.putInt("temperatureTickTimer", this.getTemperatureTickTimer());
      compound.putInt("temperatureDamageCounter", this.getTemperatureDamageCounter());
      compound.put("temporaryModifiers", temporaryList);
      return compound;
   }

   /** 1.12 TemperatureStorage.readNBT */
   @Override
   public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compound) {
      if (compound.contains("temperatureLevel")) {
         this.setTemperatureLevel(compound.getInt("temperatureLevel"));
      }
      if (compound.contains("temperatureTickTimer")) {
         this.setTemperatureTickTimer(compound.getInt("temperatureTickTimer"));
      }
      if (compound.contains("temperatureDamageCounter")) {
         this.setTemperatureDamageCounter(compound.getInt("temperatureDamageCounter"));
      }
      if (compound.contains("temporaryModifiers")) {
         this.clearTemporaryModifiers();
         ListTag list = compound.getList("temporaryModifiers", Tag.TAG_COMPOUND);
         for (int i = 0; i < list.size(); i++) {
            CompoundTag c = list.getCompound(i);
            if (c.contains("name") && c.contains("temperature") && c.contains("duration")) {
               this.setTemporaryModifier(c.getString("name"), c.getFloat("temperature"), c.getInt("duration"));
            }
         }
      }
   }
}
