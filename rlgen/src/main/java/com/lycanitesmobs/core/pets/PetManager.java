package com.lycanitesmobs.core.pets;

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
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class PetManager {
   public LivingEntity host;
   public Map<UUID, PetEntry> entries = new HashMap<>();
   public List<PetEntry> newEntries = new ArrayList<>();
   public List<PetEntry> removedEntries = new ArrayList<>();
   public Map<UUID, CompoundTag> entryNBTs = new HashMap<>();

   public PetManager(LivingEntity host) {
      this.host = host;
   }

   public boolean hasEntry(PetEntry petEntry) {
      return this.entries.containsKey(petEntry.petEntryID);
   }

   public void addEntry(PetEntry petEntry) {
      if (this.entries.containsKey(petEntry.petEntryID)) {
         LycanitesMobs.logWarning("", "[Pet Manager] Tried to add a Pet Entry that is already added!");
      } else {
         if (this.entryNBTs.containsKey(petEntry.petEntryID)) {
            petEntry.readFromNBT(this.entryNBTs.get(petEntry.petEntryID));
         }

         this.entries.put(petEntry.petEntryID, petEntry);
         petEntry.onAdd(this);
         this.newEntries.add(petEntry);
      }
   }

   public void removeEntry(PetEntry petEntry) {
      if (!this.entries.containsValue(petEntry)) {
         LycanitesMobs.logWarning("", "[Pet Manager] Tried to remove a pet entry that isn't added!");
      } else {
         this.entries.remove(petEntry.petEntryID);
      }
   }

   public PetEntry getEntry(UUID id) {
      return this.entries.get(id);
   }

   public List<PetEntry> createEntryListByType(String type) {
      List<PetEntry> filteredEntries = new ArrayList<>();

      for (PetEntry petEntry : this.entries.values()) {
         if (type.equalsIgnoreCase(petEntry.getType())) {
            filteredEntries.add(petEntry);
         }
      }

      return filteredEntries;
   }

   public void onUpdate(Level world) {
      if (!this.entryNBTs.isEmpty()) {
         for (PetEntry petEntry : this.entries.values()) {
            if (this.entryNBTs.containsKey(petEntry.petEntryID)) {
               petEntry.readFromNBT(this.entryNBTs.get(petEntry.petEntryID));
               this.entryNBTs.remove(petEntry.petEntryID);
            }
         }

         for (CompoundTag nbtEntry : this.entryNBTs.values()) {
            if (!(this.host instanceof Player)
               || !nbtEntry.getString("Type").equalsIgnoreCase("familiar")
               || PlayerFamiliars.INSTANCE.playerFamiliars.containsKey(this.host.getUUID())
                  && PlayerFamiliars.INSTANCE.playerFamiliars.get(this.host.getUUID()).containsKey(nbtEntry.getUUID("UUID"))) {
               PetEntry petEntryx = new PetEntry(nbtEntry.getUUID("UUID"), nbtEntry.getString("Type"), this.host, nbtEntry.getString("SummonType"));
               petEntryx.readFromNBT(nbtEntry);
               if (petEntryx.active) {
                  this.addEntry(petEntryx);
               }
            }
         }

         this.entryNBTs.clear();
      }

      if (this.newEntries.size() > 0) {
         if (!world.isClientSide && this.host instanceof Player) {
            for (PetEntry petEntryx : this.newEntries) {
               ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer((Player)this.host);
               if (playerExt != null) {
                  playerExt.sendPetEntryToPlayer(petEntryx);
               }
            }
         }

         this.newEntries = new ArrayList<>();
      }

      int newSpiritReserved = 0;

      for (PetEntry petEntryxx : this.entries.values()) {
         if (petEntryxx.host != this.host) {
            petEntryxx.setOwner(this.host);
         }

         if (this.host instanceof Player) {
            ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer((Player)this.host);
            if (playerExt != null && petEntryxx.usesSpirit()) {
               int spiritCost = petEntryxx.getSpiritCost();
               if (petEntryxx.spawningActive && petEntryxx.active) {
                  newSpiritReserved += spiritCost;
                  if (playerExt.spirit + playerExt.spiritReserved < newSpiritReserved || newSpiritReserved > playerExt.spiritMax) {
                     petEntryxx.spawningActive = false;
                     newSpiritReserved -= spiritCost;
                  }
               }
            }
         }

         if (petEntryxx.active) {
            petEntryxx.onUpdate(world);
         } else {
            this.removedEntries.add(petEntryxx);
         }
      }

      if (this.removedEntries.size() > 0) {
         for (PetEntry petEntryxx : this.removedEntries) {
            this.removeEntry(petEntryxx);
         }

         this.removedEntries = new ArrayList<>();
      }

      if (this.host instanceof Player) {
         ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer((Player)this.host);
         if (playerExt != null) {
            playerExt.spiritReserved = newSpiritReserved;
         }
      }
   }

   public void readFromNBT(CompoundTag nbtTagCompound) {
      if (nbtTagCompound.contains("PetManager")) {
         ListTag entryList = nbtTagCompound.getList("PetManager", 10);

         for (int i = 0; i < entryList.size(); i++) {
            CompoundTag nbtEntry = entryList.getCompound(i);
            if (!nbtEntry.hasUUID("UUID") && nbtEntry.contains("EntryName")) {
               LycanitesMobs.logInfo("", "[Pets] Converting Pet Entry NBT from older mod version: " + nbtEntry.getString("EntryName") + "...");
               nbtEntry.putUUID("UUID", UUID.randomUUID());
            }

            if (nbtEntry.hasUUID("UUID")) {
               this.entryNBTs.put(nbtEntry.getUUID("UUID"), nbtEntry);
            } else {
               LycanitesMobs.logWarning(
                  "", "[Pets] A Pet Entry was missing a UUID and EntryName, this is either a bug or NBT data has been tampered with, please report this!"
               );
            }
         }
      }
   }

   public void writeToNBT(CompoundTag nbtTagCompound) {
      ListTag entryList = new ListTag();

      for (PetEntry petEntry : this.entries.values()) {
         CompoundTag nbtEntry = new CompoundTag();
         petEntry.writeToNBT(nbtEntry);
         entryList.add(nbtEntry);
      }

      nbtTagCompound.put("PetManager", entryList);
   }
}
