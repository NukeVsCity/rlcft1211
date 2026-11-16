package com.lycanitesmobs.core.info;

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
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.FearEntity;
import com.lycanitesmobs.core.network.MessageBeastiary;
import com.lycanitesmobs.core.network.MessageCreatureKnowledge;
import com.lycanitesmobs.core.pets.SummonSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


public class Beastiary {
   public ExtendedPlayer extendedPlayer;
   public Map<String, CreatureKnowledge> creatureKnowledgeList = new HashMap<>();

   public Beastiary(ExtendedPlayer extendedPlayer) {
      this.extendedPlayer = extendedPlayer;
   }

   public boolean addCreatureKnowledge(CreatureKnowledge newKnowledge, boolean sendToClient) {
      CreatureInfo creatureInfo = CreatureManager.getInstance().getCreature(newKnowledge.creatureName);
      if (creatureInfo == null) {
         return false;
      } else if (creatureInfo.dummy) {
         return false;
      } else {
         CreatureKnowledge currentKnowledge = this.getCreatureKnowledge(creatureInfo.getName());
         if (currentKnowledge != null) {
            currentKnowledge.rank = newKnowledge.rank;
            currentKnowledge.experience = newKnowledge.experience;
            if (sendToClient) {
               this.sendToClient(currentKnowledge);
            }

            return true;
         } else {
            this.creatureKnowledgeList.put(newKnowledge.creatureName, newKnowledge);
            if (sendToClient) {
               this.sendAddedMessage(newKnowledge);
               this.sendToClient(newKnowledge);
            }

            return true;
         }
      }
   }

   public CreatureKnowledge addCreatureKnowledge(Entity entity, int experience) {
      if (entity instanceof BaseCreatureEntity && !(entity instanceof FearEntity)) {
         CreatureInfo creatureInfo = ((BaseCreatureEntity)entity).creatureInfo;
         CreatureKnowledge newKnowledge = this.getCreatureKnowledge(creatureInfo.getName());
         if (newKnowledge == null) {
            newKnowledge = new CreatureKnowledge(this.extendedPlayer.getBeastiary(), creatureInfo.getName(), 1, experience);
            newKnowledge.getMaxExperience();
         } else {
            if (newKnowledge.getMaxExperience() <= 0) {
               return null;
            }

            newKnowledge.addExperience(experience);
         }

         this.addCreatureKnowledge(newKnowledge, true);
         return newKnowledge;
      } else {
         return null;
      }
   }

   public void sendAddedMessage(CreatureKnowledge creatureKnowledge) {
      if (!this.extendedPlayer.player.level().isClientSide && CreatureManager.getInstance().config.beastiaryKnowledgeMessages) {
         CreatureInfo creatureInfo = creatureKnowledge.getCreatureInfo();
         String message = LanguageManager.translate("message.beastiary.rank");
         if (creatureKnowledge.rank == 1) {
            message = LanguageManager.translate("message.beastiary.new");
         }

         message = message.replace("%creature%", creatureInfo.getTitle());
         message = message.replace("%rank%", "" + creatureKnowledge.rank);
         this.extendedPlayer.player.sendSystemMessage(Component.literal(message));
         if (creatureInfo.isSummonable() && creatureKnowledge.rank == 2) {
            String summonMessage = LanguageManager.translate("message.beastiary.summonable");
            summonMessage = summonMessage.replace("%creature%", creatureInfo.getTitle());
            this.extendedPlayer.player.sendSystemMessage(Component.literal(summonMessage));
         }

         if (creatureInfo.isTameable() && creatureKnowledge.rank == 2) {
            String tameMessage = LanguageManager.translate("message.beastiary.tameable");
            tameMessage = tameMessage.replace("%creature%", creatureInfo.getTitle());
            this.extendedPlayer.player.sendSystemMessage(Component.literal(tameMessage));
         }
      }
   }

   public void sendKnownMessage(CreatureKnowledge creatureKnowledge) {
      if (!this.extendedPlayer.player.level().isClientSide) {
         CreatureInfo creatureInfo = creatureKnowledge.getCreatureInfo();
         CreatureKnowledge currentKnowledge = this.extendedPlayer.getBeastiary().getCreatureKnowledge(creatureInfo.getName());
         String message = LanguageManager.translate("message.beastiary.known");
         message = message.replace("%creature%", creatureInfo.getTitle());
         message = message.replace("%rank%", "" + currentKnowledge.rank);
         this.extendedPlayer.player.sendSystemMessage(Component.literal(message));
      }
   }

   public CreatureKnowledge getCreatureKnowledge(String creatureName) {
      return !this.creatureKnowledgeList.containsKey(creatureName) ? null : this.creatureKnowledgeList.get(creatureName);
   }

   public boolean hasKnowledgeRank(String creatureName, int rank) {
      CreatureKnowledge creatureKnowledge = this.getCreatureKnowledge(creatureName);
      return creatureKnowledge == null ? false : creatureKnowledge.rank >= rank;
   }

   public int getCreaturesDiscovered(CreatureType creatureType) {
      if (this.creatureKnowledgeList.size() == 0) {
         return 0;
      } else {
         int creaturesDescovered = 0;

         for (Entry<String, CreatureKnowledge> creatureKnowledgeEntry : this.creatureKnowledgeList.entrySet()) {
            if (creatureKnowledgeEntry.getValue() != null && creatureKnowledgeEntry.getValue().getCreatureInfo().creatureType == creatureType) {
               creaturesDescovered++;
            }
         }

         return creaturesDescovered;
      }
   }

   public Map<Integer, String> getSummonableList() {
      Map<Integer, String> minionList = new HashMap<>();
      int minionIndex = 0;

      for (String minionName : this.creatureKnowledgeList.keySet()) {
         CreatureKnowledge creatureKnowledge = this.creatureKnowledgeList.get(minionName);
         if (creatureKnowledge.rank >= 2 && SummonSet.isSummonableCreature(minionName)) {
            minionList.put(minionIndex++, minionName);
         }
      }

      return minionList;
   }

   public void sendToClient(CreatureKnowledge newKnowledge) {
      if (!this.extendedPlayer.player.level().isClientSide) {
         MessageCreatureKnowledge message = new MessageCreatureKnowledge(newKnowledge);
         LycanitesMobs.packetHandler.sendToPlayer(message, (ServerPlayer)this.extendedPlayer.getPlayer());
      }
   }

   public void sendAllToClient() {
      LycanitesMobs.logDebug("Packets", "Sending all beastiary to client.");
      MessageBeastiary message = new MessageBeastiary(this);
      LycanitesMobs.packetHandler.sendToPlayer(message, (ServerPlayer)this.extendedPlayer.getPlayer());
   }

   public void readFromNBT(CompoundTag nbtTagCompound) {
      if (nbtTagCompound.contains("CreatureKnowledge")) {
         this.creatureKnowledgeList.clear();
         ListTag knowledgeList = nbtTagCompound.getList("CreatureKnowledge", 10);

         for (int i = 0; i < knowledgeList.size(); i++) {
            CompoundTag nbtKnowledge = knowledgeList.getCompound(i);
            if (nbtKnowledge.contains("CreatureName")) {
               String creatureName = nbtKnowledge.getString("CreatureName");
               int rank = 0;
               if (nbtKnowledge.contains("Rank")) {
                  rank = nbtKnowledge.getInt("Rank");
               }

               int experience = 0;
               if (nbtKnowledge.contains("Experience")) {
                  experience = nbtKnowledge.getInt("Experience");
               }

               CreatureKnowledge creatureKnowledge = new CreatureKnowledge(this, creatureName, rank, experience);
               this.addCreatureKnowledge(creatureKnowledge, false);
            }
         }
      }
   }

   public void writeToNBT(CompoundTag nbtTagCompound) {
      ListTag knowledgeList = new ListTag();

      for (Entry<String, CreatureKnowledge> creatureKnowledgeEntry : this.creatureKnowledgeList.entrySet()) {
         CreatureKnowledge creatureKnowledge = creatureKnowledgeEntry.getValue();
         CompoundTag nbtKnowledge = new CompoundTag();
         nbtKnowledge.putString("CreatureName", creatureKnowledge.creatureName);
         nbtKnowledge.putInt("Rank", creatureKnowledge.rank);
         nbtKnowledge.putInt("Experience", creatureKnowledge.experience);
         knowledgeList.add(nbtKnowledge);
      }

      nbtTagCompound.put("CreatureKnowledge", knowledgeList);
   }
}
