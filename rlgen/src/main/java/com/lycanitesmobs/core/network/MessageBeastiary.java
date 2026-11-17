package com.lycanitesmobs.core.network;

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
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.info.Beastiary;
import com.lycanitesmobs.core.info.CreatureKnowledge;


public class MessageBeastiary implements IMessage {
   public int entryAmount = 0;
   public String[] creatureNames;
   public int[] ranks;
   public int[] experience;

   public MessageBeastiary() {
   }

   public MessageBeastiary(Beastiary beastiary) {
      this.entryAmount = Math.min(201, beastiary.creatureKnowledgeList.size());
      if (this.entryAmount > 0) {
         this.creatureNames = new String[this.entryAmount];
         this.ranks = new int[this.entryAmount];
         this.experience = new int[this.entryAmount];
         int i = 0;

         for (CreatureKnowledge creatureKnowledge : beastiary.creatureKnowledgeList.values()) {
            this.creatureNames[i] = creatureKnowledge.creatureName;
            this.ranks[i] = creatureKnowledge.rank;
            this.experience[i] = creatureKnowledge.experience;
            i++;
         }
      }
   }

   public static void onMessage(MessageBeastiary message, MessageContext ctx, Player player) {
      LycanitesMobs.logDebug("Packets", "Received Beastiary packet from server.");
      ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
      if (playerExt == null) {
         LycanitesMobs.logDebug("Packets", "Unable to find extended player for client player: " + player);
      } else {
         LycanitesMobs.logDebug("Packets", "Beastiary packet is valid.");
         playerExt.getBeastiary().creatureKnowledgeList.clear();

         for (int i = 0; i < message.entryAmount; i++) {
            String creatureName = message.creatureNames[i];
            int rank = message.ranks[i];
            int experience = message.experience[i];
            CreatureKnowledge creatureKnowledge = new CreatureKnowledge(playerExt.getBeastiary(), creatureName, rank, experience);
            playerExt.getBeastiary().creatureKnowledgeList.put(creatureKnowledge.creatureName, creatureKnowledge);
         }

         LycanitesMobs.logDebug("Packets", "Added " + message.entryAmount + " entries from the Beastairy Packet.");
      }
   }

   public void fromBytes(ByteBuf buf) {
      RegistryFriendlyByteBuf packet = LycBuf.of(buf);
      this.entryAmount = Math.min(200, packet.readInt());
      if (this.entryAmount == 200) {
         LycanitesMobs.logWarning(
            "", "Received 200 or more creature entries, something went wrong with the Beastiary packet! Additional entries will be skipped to prevent OOM!"
         );
      }

      if (this.entryAmount > 0) {
         this.creatureNames = new String[this.entryAmount];
         this.ranks = new int[this.entryAmount];
         this.experience = new int[this.entryAmount];

         for (int i = 0; i < this.entryAmount; i++) {
            this.creatureNames[i] = packet.readUtf(32767);
            this.ranks[i] = packet.readInt();
            this.experience[i] = packet.readInt();
         }
      }
   }

   public void toBytes(ByteBuf buf) {
      RegistryFriendlyByteBuf packet = LycBuf.of(buf);
      packet.writeInt(this.entryAmount);
      if (this.entryAmount > 0) {
         for (int i = 0; i < this.entryAmount; i++) {
            packet.writeUtf(this.creatureNames[i]);
            packet.writeInt(this.ranks[i]);
            packet.writeInt(this.experience[i]);
         }
      }
   }
}
