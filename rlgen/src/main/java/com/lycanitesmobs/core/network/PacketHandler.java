package com.lycanitesmobs.core.network;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
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
import net.minecraft.world.level.pathfinder.Path;
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
import org.apache.logging.log4j.util.TriConsumer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;


/**
 * 1.12 PacketHandler (SimpleNetworkWrapper channel "lycanitesmobs") on NeoForge payloads: every message class is one payload type
 * (lycanitesmobs:message_<index in the 1.12 registration order>), encoded with its 1.12 toBytes/fromBytes. Handlers run on the
 * main thread with the receiving side's player, like the 1.12 scheduled tasks.
 */
public class PacketHandler {
   private final List<Entry<?>> entries = new ArrayList<>();
   private final Map<Class<?>, Entry<?>> byClass = new HashMap<>();

   private static final class Entry<T extends IMessage> {
      final Class<T> cls;
      final CustomPacketPayload.Type<Payload> type;
      TriConsumer<T, MessageContext, Player> client;
      TriConsumer<T, MessageContext, Player> server;

      Entry(Class<T> cls, int id) {
         this.cls = cls;
         this.type = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("lycanitesmobs", "message_" + id));
      }
   }

   /** The wrapper payload around a 1.12 message. */
   public static final class Payload implements CustomPacketPayload {
      final IMessage message;
      final CustomPacketPayload.Type<Payload> type;

      Payload(IMessage message, CustomPacketPayload.Type<Payload> type) {
         this.message = message;
         this.type = type;
      }

      @Override
      public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
         return this.type;
      }
   }

   public void init() {
      this.register(MessageBeastiary.class, MessageBeastiary::onMessage, true);
      this.register(MessageCreature.class, MessageCreature::onMessage, true);
      this.register(MessageCreatureKnowledge.class, MessageCreatureKnowledge::onMessage, true);
      this.register(MessagePlayerStats.class, MessagePlayerStats::onMessage, true);
      this.register(MessagePetEntry.class, MessagePetEntry::onMessage, true);
      this.register(MessagePetEntryRemove.class, MessagePetEntryRemove::onMessage, true);
      this.register(MessageSummonSet.class, MessageSummonSet::onMessage, true);
      this.register(MessageSummonSetSelection.class, MessageSummonSetSelection::onMessage, true);
      this.register(MessageEntityPickedUp.class, MessageEntityPickedUp::onMessage, true);
      this.register(MessageEntityPerched.class, MessageEntityPerched::onMessage, true);
      this.register(MessageWorldEvent.class, MessageWorldEvent::onMessage, true);
      this.register(MessageMobEvent.class, MessageMobEvent::onMessage, true);
      this.register(MessageSummoningPedestalStats.class, MessageSummoningPedestalStats::onMessage, true);
      this.register(MessageEntityVelocity.class, MessageEntityVelocity::onMessage, true);
      this.register(MessageEntityGUICommand.class, MessageEntityGUICommand::onMessage, false);
      this.register(MessageSyncRequest.class, MessageSyncRequest::onMessage, false);
      this.register(MessageGUIRequest.class, MessageGUIRequest::onMessage, false);
      this.register(MessagePlayerControl.class, MessagePlayerControl::onMessage, false);
      this.register(MessagePlayerLeftClick.class, MessagePlayerLeftClick::onMessage, false);
      this.register(MessagePlayerAttack.class, MessagePlayerAttack::onMessage, false);
      this.register(MessagePetEntry.class, MessagePetEntry::onMessage, false);
      this.register(MessagePetEntryRemove.class, MessagePetEntryRemove::onMessage, false);
      this.register(MessageSummonSet.class, MessageSummonSet::onMessage, false);
      this.register(MessageSummonSetSelection.class, MessageSummonSetSelection::onMessage, false);
      this.register(MessageSummoningPedestalSummonSet.class, MessageSummoningPedestalSummonSet::onMessage, false);
      this.register(MessageTileEntityButton.class, MessageTileEntityButton::onMessage, false);
   }

   @SuppressWarnings("unchecked")
   private <T extends IMessage> void register(Class<T> cls, TriConsumer<T, MessageContext, Player> handler, boolean toClient) {
      Entry<T> entry = (Entry<T>)this.byClass.get(cls);
      if (entry == null) {
         entry = new Entry<>(cls, this.entries.size());
         this.entries.add(entry);
         this.byClass.put(cls, entry);
      }
      if (toClient) {
         entry.client = handler;
      } else {
         entry.server = handler;
      }
   }

   /** RegisterPayloadHandlersEvent (mod bus). */
   public void registerPayloads(RegisterPayloadHandlersEvent event) {
      PayloadRegistrar registrar = event.registrar("lycanitesmobs").optional();
      for (Entry<?> entry : this.entries) {
         this.registerEntry(registrar, entry);
      }
   }

   private <T extends IMessage> void registerEntry(PayloadRegistrar registrar, Entry<T> entry) {
      StreamCodec<RegistryFriendlyByteBuf, Payload> codec = StreamCodec.of((buf, payload) -> payload.message.toBytes(buf), buf -> {
         try {
            T message = entry.cls.getDeclaredConstructor().newInstance();
            message.fromBytes(buf);
            return new Payload(message, entry.type);
         } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
         }
      });
      IPayloadHandler<Payload> handler = (payload, ctx) -> {
         @SuppressWarnings("unchecked")
         T message = (T)payload.message;
         boolean client = ctx.flow().isClientbound();
         TriConsumer<T, MessageContext, Player> c = client ? entry.client : entry.server;
         if (c != null) {
            ctx.enqueueWork(() -> c.accept(message, new MessageContext(client, ctx.player()), ctx.player()));
         }
      };
      if (entry.client != null && entry.server != null) {
         registrar.playBidirectional(entry.type, codec, handler);
      } else if (entry.client != null) {
         registrar.playToClient(entry.type, codec, handler);
      } else {
         registrar.playToServer(entry.type, codec, handler);
      }
   }

   private Payload wrap(IMessage message) {
      Entry<?> entry = this.byClass.get(message.getClass());
      return new Payload(message, entry.type);
   }

   public void sendToAll(IMessage message) {
      PacketDistributor.sendToAllPlayers(this.wrap(message));
   }

   public void sendToPlayer(IMessage message, ServerPlayer player) {
      PacketDistributor.sendToPlayer(player, this.wrap(message));
   }

   public void sendToAllAround(IMessage message, TargetPoint point) {
      ServerLevel level = LycServer.level(point.dimension);
      if (level != null) {
         PacketDistributor.sendToPlayersNear(level, null, point.x, point.y, point.z, point.range, this.wrap(message));
      }
   }

   public void sendToDimension(IMessage message, int dimensionID) {
      ServerLevel level = LycServer.level(dimensionID);
      if (level != null) {
         PacketDistributor.sendToPlayersInDimension(level, this.wrap(message));
      }
   }

   public void sendToServer(IMessage message) {
      PacketDistributor.sendToServer(this.wrap(message));
   }
}
