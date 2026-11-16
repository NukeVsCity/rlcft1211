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
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.pets.PetEntry;
import com.lycanitesmobs.core.pets.PetManager;
import com.lycanitesmobs.core.pets.SummonSet;
import java.util.UUID;


public class MessagePetEntry implements IMessage {
   public UUID petEntryID;
   public String petEntryType;
   public boolean spawningActive;
   public boolean teleportEntity;
   public String summonType;
   public int subspecies;
   public int variant;
   public byte behaviour;
   public int petEntryEntityID = -1;
   public String petEntryEntityName;
   public int respawnTime;
   public int respawnTimeMax;
   public int entityLevel;
   public int entityExperience;
   public boolean isRespawning;

   public MessagePetEntry() {
   }

   public MessagePetEntry(ExtendedPlayer playerExt, PetEntry petEntry) {
      this.petEntryID = petEntry.petEntryID;
      this.petEntryType = petEntry.getType();
      this.spawningActive = petEntry.spawningActive;
      this.teleportEntity = petEntry.teleportEntity;
      SummonSet summonSet = petEntry.summonSet;
      this.summonType = summonSet.summonType;
      this.subspecies = petEntry.subspeciesIndex;
      this.variant = petEntry.variantIndex;
      this.behaviour = summonSet.getBehaviourByte();
      this.petEntryEntityID = petEntry.entity != null ? petEntry.entity.getId() : -1;
      this.petEntryEntityName = petEntry.entityName;
      this.respawnTime = petEntry.respawnTime;
      this.respawnTimeMax = petEntry.respawnTimeMax;
      this.entityLevel = petEntry.entityLevel;
      this.entityExperience = petEntry.entityExperience;
      this.isRespawning = petEntry.isRespawning;
   }

   public static void onMessage(MessagePetEntry message, MessageContext ctx, Player player) {
      if (ctx.side == Side.SERVER) {
         ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
         PetManager petManager = playerExt.petManager;
         PetEntry petEntry = petManager.getEntry(message.petEntryID);
         if (petEntry == null) {
            return;
         }

         petEntry.setSpawningActive(message.spawningActive);
         petEntry.teleportEntity = message.teleportEntity;
         SummonSet summonSet = petEntry.summonSet;
         summonSet.readFromPacket(message.summonType, message.subspecies, message.variant, message.behaviour);
         petEntry.onBehaviourUpdate();
      } else {
         ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
         if (playerExt == null) {
            return;
         }

         PetManager petManager = playerExt.petManager;
         PetEntry petEntry = petManager.getEntry(message.petEntryID);
         if (petEntry == null) {
            petEntry = new PetEntry(message.petEntryID, message.petEntryType, player, message.summonType);
            petManager.addEntry(petEntry);
         }

         petEntry.setSpawningActive(message.spawningActive);
         petEntry.teleportEntity = message.teleportEntity;
         petEntry.subspeciesIndex = message.subspecies;
         petEntry.variantIndex = message.variant;
         SummonSet summonSet = petEntry.summonSet;
         summonSet.readFromPacket(message.summonType, message.subspecies, message.variant, message.behaviour);
         Entity entity = null;
         if (message.petEntryEntityID != -1) {
            entity = player.level().getEntity(message.petEntryEntityID);
         }

         petEntry.entity = entity;
         petEntry.entityName = message.petEntryEntityName;
         petEntry.respawnTime = message.respawnTime;
         petEntry.respawnTimeMax = message.respawnTimeMax;
         petEntry.entityLevel = message.entityLevel;
         petEntry.entityExperience = message.entityExperience;
         petEntry.isRespawning = message.isRespawning;
      }
   }

   public void fromBytes(ByteBuf buf) {
      RegistryFriendlyByteBuf packet = LycBuf.of(buf);
      this.petEntryID = packet.readUUID();
      this.petEntryType = packet.readUtf(256);
      this.spawningActive = packet.readBoolean();
      this.teleportEntity = packet.readBoolean();
      this.summonType = packet.readUtf(512);
      this.subspecies = packet.readInt();
      this.variant = packet.readInt();
      this.behaviour = packet.readByte();
      this.petEntryEntityID = packet.readInt();
      this.petEntryEntityName = packet.readUtf(1024);
      this.respawnTime = packet.readInt();
      this.respawnTimeMax = packet.readInt();
      this.entityLevel = packet.readInt();
      this.entityExperience = packet.readInt();
      this.isRespawning = packet.readBoolean();
   }

   public void toBytes(ByteBuf buf) {
      RegistryFriendlyByteBuf packet = LycBuf.of(buf);
      packet.writeUUID(this.petEntryID);
      packet.writeUtf(this.petEntryType);
      packet.writeBoolean(this.spawningActive);
      packet.writeBoolean(this.teleportEntity);
      packet.writeUtf(this.summonType);
      packet.writeInt(this.subspecies);
      packet.writeInt(this.variant);
      packet.writeByte(this.behaviour);
      packet.writeInt(this.petEntryEntityID);
      packet.writeUtf(this.petEntryEntityName);
      packet.writeInt(this.respawnTime);
      packet.writeInt(this.respawnTimeMax);
      packet.writeInt(this.entityLevel);
      packet.writeInt(this.entityExperience);
      packet.writeBoolean(this.isRespawning);
   }
}
