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
import com.lycanitesmobs.GuiHandler;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.VersionChecker;
import com.lycanitesmobs.core.info.Beastiary;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureKnowledge;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.CreatureType;
import com.lycanitesmobs.core.item.equipment.ItemEquipment;
import com.lycanitesmobs.core.item.temp.ItemStaffSummoning;
import com.lycanitesmobs.core.network.MessagePetEntry;
import com.lycanitesmobs.core.network.MessagePetEntryRemove;
import com.lycanitesmobs.core.network.MessagePlayerStats;
import com.lycanitesmobs.core.network.MessageSummonSet;
import com.lycanitesmobs.core.network.MessageSummonSetSelection;
import com.lycanitesmobs.core.pets.PetEntry;
import com.lycanitesmobs.core.pets.PetManager;
import com.lycanitesmobs.core.pets.PlayerFamiliars;
import com.lycanitesmobs.core.pets.SummonSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class ExtendedPlayer {
   public Player player;
   public Beastiary beastiary;
   public PetManager petManager;
   public long timePlayed = 0L;
   public CreatureType selectedCreatureType;
   public CreatureInfo selectedCreature;
   public int selectedSubspecies = 0;
   public int selectedVariant = 0;
   public int selectedPetType = 0;
   public PetEntry selectedPet;
   public long currentTick = 0L;
   public boolean needsFullSync = true;
   public BlockState justBrokenBlock;
   protected int justBrokenClearTime;
   public byte controlStates = 0;
   public boolean hasAttacked = false;
   public int spiritCharge = 100;
   public int spiritMax = this.spiritCharge * 10;
   public int spirit = this.spiritMax;
   public int spiritReserved = 0;
   public int spiritRecharge = 10;
   public int selectedSummonSet = 1;
   public int summonFocusCharge = 600;
   public int summonFocusMax = this.summonFocusCharge * 10;
   public int summonFocus = this.summonFocusMax;
   public int summonFocusRecharge = 10;
   public Map<Integer, SummonSet> summonSets = new HashMap<>();
   public int summonSetMax = 5;
   public PortalEntity staffPortal;
   public int creatureStudyCooldown = 0;
   public int creatureStudyCooldownMax = 200;
   private boolean initialSetup = false;

   public static ExtendedPlayer getForPlayer(Player player) {
      if (player == null) {
         return null;
      } else {
         ExtendedPlayer extendedPlayer = player.getData(com.lycanitesmobs.LycAttachments.EXTENDED_PLAYER);
         if (extendedPlayer.player != player) {
            extendedPlayer.setPlayer(player);
         }

         return extendedPlayer;
      }
   }

   public ExtendedPlayer() {
      this.beastiary = new Beastiary(this);
      this.petManager = new PetManager(this.player);
      this.spiritRecharge = LycanitesMobs.config
         .getInt("Player", "Spirit Recharge", this.spiritRecharge, "How much spirit a player regains per second. Default is 10, was 1 in earlier versions.");
      this.summonFocusRecharge = LycanitesMobs.config
         .getInt(
            "Player",
            "Summoning Focus Recharge",
            this.summonFocusRecharge,
            "How much summoning focus a player regains per second. Default is 10, was 1 in earlier versions."
         );
      this.creatureStudyCooldownMax = CreatureManager.getInstance().config.creatureStudyCooldown;
   }

   public void setPlayer(Player player) {
      this.player = player;
      this.petManager.host = player;
   }

   public Player getPlayer() {
      return this.player;
   }

   public boolean canMeleeBigEntity(Entity targetEntity) {
      if (!(targetEntity instanceof LivingEntity)) {
         return false;
      } else {
         float targetWidth = targetEntity.getBbWidth();
         float targetHeight = targetEntity.getBbHeight();
         if (targetEntity instanceof BaseCreatureEntity) {
            BaseCreatureEntity targetCreature = (BaseCreatureEntity)targetEntity;
            targetWidth *= targetCreature.hitAreaWidthScale;
            targetHeight *= targetCreature.hitAreaHeightScale;
         }

         if (targetWidth <= 4.0F && targetHeight <= 4.0F) {
            return false;
         } else {
            double heightOffset = this.player.getY() - targetEntity.getY();
            double heightCompensation = 0.0;
            if (heightOffset > 0.0) {
               heightCompensation = Math.min(heightOffset, (double)targetHeight);
            }

            double distance = Math.sqrt((double)this.player.distanceTo(targetEntity));
            double range = 6.0 + heightCompensation + (double)(targetWidth / 2.0F);
            return distance <= range;
         }
      }
   }

   public void meleeAttack(Entity targetEntity) {
      if (!this.hasAttacked && !this.player.getMainHandItem().isEmpty() && this.canMeleeBigEntity(targetEntity)) {
         this.player.attack(targetEntity);
         this.player.resetAttackStrengthTicker();
         this.player.swing(InteractionHand.MAIN_HAND);
      }
   }

   public void onUpdate() {
      this.timePlayed++;
      this.hasAttacked = false;
      boolean creative = this.player.getAbilities().instabuild;
      boolean sync = false;
      if (this.justBrokenClearTime > 0 && --this.justBrokenClearTime <= 0) {
         this.justBrokenBlock = null;
      }

      this.spirit = Math.min(Math.max(this.spirit, 0), this.spiritMax - this.spiritReserved);
      if (this.spirit < this.spiritMax - this.spiritReserved) {
         this.spirit = this.spirit + this.spiritRecharge;
         if (!this.player.level().isClientSide && this.currentTick % 20L == 0L || this.spirit == this.spiritMax - this.spiritReserved) {
            sync = true;
         }
      }

      this.summonFocus = Math.min(Math.max(this.summonFocus, 0), this.summonFocusMax);
      if (this.summonFocus < this.summonFocusMax) {
         this.summonFocus = this.summonFocus + this.summonFocusRecharge;
         if (!this.player.level().isClientSide && !creative && this.currentTick % 20L == 0L
            || this.summonFocus < this.summonFocusMax
            || !this.player.getMainHandItem().isEmpty() && this.player.getMainHandItem().getItem() instanceof ItemStaffSummoning
            || !this.player.getOffhandItem().isEmpty() && this.player.getOffhandItem().getItem() instanceof ItemStaffSummoning) {
            sync = true;
         }
      }

      if (this.creatureStudyCooldown > 0) {
         this.creatureStudyCooldown--;
         sync = true;
      }

      if (!this.player.level().isClientSide && sync) {
         MessagePlayerStats message = new MessagePlayerStats(this);
         LycanitesMobs.packetHandler.sendToPlayer(message, (ServerPlayer)this.player);
      }

      if (!this.initialSetup) {
         if (!this.player.level().isClientSide) {
            this.loadFamiliars();
         }

         if (this.player.level().isClientSide) {
            VersionChecker.VersionInfo latestVersion = VersionChecker.INSTANCE.getLatestVersion();
            if (latestVersion != null && latestVersion.isNewer && VersionChecker.INSTANCE.enabled) {
               this.player
                  .sendSystemMessage(
                     Component.literal(
                        LanguageManager.translate("lyc.version.newer").replace("{current}", "2.0.8.9").replace("{latest}", latestVersion.versionNumber)
                     )
                  );
            }
         }

         this.initialSetup = true;
      }

      if (!this.player.level().isClientSide && this.needsFullSync) {
         this.beastiary.sendAllToClient();
         this.sendAllSummonSetsToPlayer();
         MessageSummonSetSelection message = new MessageSummonSetSelection(this);
         LycanitesMobs.packetHandler.sendToPlayer(message, (ServerPlayer)this.player);
         this.sendPetEntriesToPlayer("");
      }

      this.petManager.onUpdate(this.player.level());
      this.currentTick++;
      this.needsFullSync = false;
      if (!this.isControlActive(ExtendedPlayer.CONTROL_ID.RIGHT_MOUSE)
         && (this.player.getMainHandItem().getItem() instanceof ItemEquipment || this.player.getOffhandItem().getItem() instanceof ItemEquipment)) {
         this.player.stopUsingItem();
      }
   }

   public void setJustBrokenBlock(BlockState blockState) {
      this.justBrokenBlock = blockState;
      this.justBrokenClearTime = 60;
   }

   public SummonSet getSummonSet(int setID) {
      if (setID <= 0) {
         LycanitesMobs.logWarning("", "Attempted to access set " + setID + " but the minimum ID is 1. Player: " + this.player);
         return this.getSummonSet(1);
      } else if (setID > this.summonSetMax) {
         LycanitesMobs.logWarning("", "Attempted to access set " + setID + " but the maximum set ID is " + this.summonSetMax + ". Player: " + this.player);
         return this.getSummonSet(this.summonSetMax);
      } else {
         if (!this.summonSets.containsKey(setID)) {
            this.summonSets.put(setID, new SummonSet(this));
         }

         return this.summonSets.get(setID);
      }
   }

   public SummonSet getSelectedSummonSet() {
      return this.getSummonSet(this.selectedSummonSet);
   }

   public void setSelectedSummonSet(int targetSetID) {
      this.selectedSummonSet = targetSetID;
   }

   public int validateSummonSetID(int targetSetID) {
      targetSetID = Math.max(Math.min(targetSetID, this.summonSetMax), 1);

      while (!this.getSummonSet(targetSetID).isUseable() && targetSetID > 1 && !"".equals(this.getSummonSet(targetSetID).summonType)) {
         targetSetID--;
      }

      return targetSetID;
   }

   public Beastiary getBeastiary() {
      return this.beastiary;
   }

   public void loadFamiliars() {
      Map<UUID, PetEntry> playerFamiliars = PlayerFamiliars.INSTANCE.getFamiliarsForPlayer(this.player);
      if (!playerFamiliars.isEmpty()) {
         for (PetEntry petEntry : playerFamiliars.values()) {
            if (this.petManager.hasEntry(petEntry)) {
               PetEntry currentFamiliarEntry = this.petManager.getEntry(petEntry.petEntryID);
               currentFamiliarEntry.copy(petEntry);
            } else {
               this.petManager.addEntry(petEntry);
               petEntry.entity = null;
            }
         }

         this.sendPetEntriesToPlayer("familiar");
      }
   }

   public boolean studyCreature(Entity entity, int experience, boolean useCooldown, boolean alwaysShowMessage) {
      if (!(entity instanceof BaseCreatureEntity)) {
         if (useCooldown && !this.player.level().isClientSide) {
            this.player.displayClientMessage(Component.literal(LanguageManager.translate("message.beastiary.unknown")), true);
         }

         return false;
      } else if (useCooldown && this.creatureStudyCooldown > 0) {
         if (!this.player.level().isClientSide) {
            this.player.displayClientMessage(Component.literal(LanguageManager.translate("message.beastiary.study.recharging")), true);
         }

         return false;
      } else {
         BaseCreatureEntity creature = (BaseCreatureEntity)entity;
         if (creature.isTamed()) {
            return false;
         } else {
            experience = creature.scaleKnowledgeExperience(experience);
            CreatureKnowledge newKnowledge = this.beastiary.addCreatureKnowledge(creature, experience);
            if (newKnowledge != null) {
               if (useCooldown) {
                  this.creatureStudyCooldown = this.creatureStudyCooldownMax;
               }

               if (!this.player.level().isClientSide) {
                  if (newKnowledge.getMaxExperience() == 0) {
                     this.player
                        .displayClientMessage(
                           Component.literal(LanguageManager.translate("message.beastiary.study.full") + " " + creature.creatureInfo.getTitle()), true
                        );
                  } else if (experience > 0) {
                     boolean showMessage = alwaysShowMessage;
                     if (!alwaysShowMessage) {
                        float messageThreshold = (float)newKnowledge.getMaxExperience() * 0.25F;
                        int fromExperience = Math.max(0, newKnowledge.experience - experience);
                        int wrappedExperience = fromExperience % (int)messageThreshold;
                        showMessage = (float)(wrappedExperience + experience) >= messageThreshold;
                     }

                     if (showMessage) {
                        this.player
                           .displayClientMessage(
                              Component.literal(
                                 " "
                                    + LanguageManager.translate("message.beastiary.study")
                                    + newKnowledge.getCreatureInfo().getTitle()
                                    + " "
                                    + newKnowledge.experience
                                    + "/"
                                    + newKnowledge.getMaxExperience()
                                    + " (+"
                                    + experience
                                    + ")"
                              ),
                              true
                           );
                     }
                  }
               }

               return true;
            } else {
               if (useCooldown && !this.player.level().isClientSide) {
                  this.player
                     .displayClientMessage(
                        Component.literal(LanguageManager.translate("message.beastiary.study.full") + " " + creature.creatureInfo.getTitle()), true
                     );
               }

               return false;
            }
         }
      }
   }

   public void onDeath() {
   }

   public void sendPetEntriesToPlayer(String entryType) {
      if (!this.player.level().isClientSide) {
         LycanitesMobs.logDebug("Packets", "Sending all pet entries to client.");

         for (PetEntry petEntry : this.petManager.entries.values()) {
            if (entryType.equals(petEntry.getType()) || "".equals(entryType)) {
               MessagePetEntry message = new MessagePetEntry(this, petEntry);
               LycanitesMobs.packetHandler.sendToPlayer(message, (ServerPlayer)this.player);
            }
         }
      }
   }

   public void sendPetEntryToPlayer(PetEntry petEntry) {
      if (!this.player.level().isClientSide) {
         MessagePetEntry message = new MessagePetEntry(this, petEntry);
         LycanitesMobs.packetHandler.sendToPlayer(message, (ServerPlayer)this.player);
      }
   }

   public void sendPetEntryRemoveToPlayer(PetEntry petEntry) {
      if (!this.player.level().isClientSide) {
         MessagePetEntryRemove message = new MessagePetEntryRemove(this, petEntry);
         LycanitesMobs.packetHandler.sendToPlayer(message, (ServerPlayer)this.player);
      }
   }

   public void sendPetEntryToServer(PetEntry petEntry) {
      if (this.player.level().isClientSide) {
         MessagePetEntry message = new MessagePetEntry(this, petEntry);
         LycanitesMobs.packetHandler.sendToServer(message);
      }
   }

   public void sendPetEntryRemoveRequest(PetEntry petEntry) {
      if (this.player.level().isClientSide) {
         petEntry.remove();
         MessagePetEntryRemove message = new MessagePetEntryRemove(this, petEntry);
         LycanitesMobs.packetHandler.sendToServer(message);
      }
   }

   public void sendAllSummonSetsToPlayer() {
      if (!this.player.level().isClientSide) {
         LycanitesMobs.logDebug("Packets", "Sending all summoning sets to client.");

         for (byte setID = 1; setID <= this.summonSetMax; setID++) {
            MessageSummonSet message = new MessageSummonSet(this, setID);
            LycanitesMobs.packetHandler.sendToPlayer(message, (ServerPlayer)this.player);
         }
      }
   }

   public void sendSummonSetToServer(byte setID) {
      if (this.player.level().isClientSide) {
         MessageSummonSet message = new MessageSummonSet(this, setID);
         LycanitesMobs.packetHandler.sendToServer(message);
      }
   }

   public void updateControlStates(byte controlStates) {
      this.controlStates = controlStates;
   }

   public boolean isControlActive(ExtendedPlayer.CONTROL_ID controlID) {
      return (this.controlStates & controlID.id) > 0;
   }

   public void requestGUI(byte guiID) {
      if (guiID == GuiHandler.Beastiary.PETS.id) {
         this.sendPetEntriesToPlayer("pet");
      }

      if (guiID == GuiHandler.Beastiary.PETS.id) {
         this.sendPetEntriesToPlayer("mount");
      }

      if (guiID == GuiHandler.Beastiary.PETS.id) {
         this.sendPetEntriesToPlayer("familiar");
      }
   }

   public void readNBT(CompoundTag nbtTagCompound) {
      CompoundTag extTagCompound = nbtTagCompound.getCompound("LycanitesMobsPlayer");
      this.beastiary.readFromNBT(extTagCompound);
      this.petManager.readFromNBT(extTagCompound);
      if (extTagCompound.contains("SummonFocus")) {
         this.summonFocus = extTagCompound.getInt("SummonFocus");
      }

      if (extTagCompound.contains("Spirit")) {
         this.spirit = extTagCompound.getInt("Spirit");
      }

      if (extTagCompound.contains("CreatureStudyCooldown")) {
         this.creatureStudyCooldown = extTagCompound.getInt("CreatureStudyCooldown");
      }

      if (extTagCompound.contains("SelectedSummonSet")) {
         this.selectedSummonSet = extTagCompound.getInt("SelectedSummonSet");
      }

      if (extTagCompound.contains("SummonSets")) {
         ListTag nbtSummonSets = extTagCompound.getList("SummonSets", 10);

         for (int setID = 0; setID < this.summonSetMax; setID++) {
            CompoundTag nbtSummonSet = nbtSummonSets.getCompound(setID);
            SummonSet summonSet = new SummonSet(this);
            summonSet.readFromNBT(nbtSummonSet);
            this.summonSets.put(setID + 1, summonSet);
         }
      }

      if (extTagCompound.contains("TimePlayed")) {
         this.timePlayed = extTagCompound.getLong("TimePlayed");
      }
   }

   public void writeNBT(CompoundTag nbtTagCompound) {
      CompoundTag extTagCompound = new CompoundTag();
      this.beastiary.writeToNBT(extTagCompound);
      this.petManager.writeToNBT(extTagCompound);
      extTagCompound.putInt("SummonFocus", this.summonFocus);
      extTagCompound.putInt("Spirit", this.spirit);
      extTagCompound.putInt("CreatureStudyCooldown", this.creatureStudyCooldown);
      extTagCompound.putInt("SelectedSummonSet", this.selectedSummonSet);
      extTagCompound.putLong("TimePlayed", this.timePlayed);
      ListTag nbtSummonSets = new ListTag();

      for (int setID = 0; setID < this.summonSetMax; setID++) {
         CompoundTag nbtSummonSet = new CompoundTag();
         SummonSet summonSet = this.getSummonSet(setID + 1);
         summonSet.writeToNBT(nbtSummonSet);
         nbtSummonSets.add(nbtSummonSet);
      }

      extTagCompound.put("SummonSets", nbtSummonSets);
      nbtTagCompound.put("LycanitesMobsPlayer", extTagCompound);
   }

   public static enum CONTROL_ID {
      JUMP((byte)1),
      MOUNT_DISMOUNT((byte)2),
      MOUNT_ABILITY((byte)4),
      MOUNT_INVENTORY((byte)8),
      ATTACK((byte)16),
      DESCEND((byte)32),
      RIGHT_MOUSE((byte)64);

      public byte id;

      private CONTROL_ID(byte i) {
         this.id = i;
      }
   }
}
