package ichttt.mods.firstaid.common.network;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.apiimpl.FirstAidRegistryImpl;
import ichttt.mods.firstaid.common.damagesystem.DamageablePart;
import ichttt.mods.firstaid.common.damagesystem.DamageablePart.PartHealer;
import ichttt.mods.firstaid.common.damagesystem.PlayerDamageModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * The 1.12 First Aid messages: MessageUpdatePart, MessageSyncDamageModel, MessageConfiguration, MessageApplyAbsorption,
 * MessageAddHealth, MessagePlayHurtSound (to the client) and MessageApplyHealingItem, MessageClientRequest (to the server).
 * Client handling lives in client.ClientHandlers (only loaded on the client).
 */
public final class FirstAidNetwork {
   private FirstAidNetwork() {
   }

   private static ResourceLocation rl(String s) {
      return ResourceLocation.fromNamespaceAndPath(FirstAid.MODID, s);
   }

   public static void register(RegisterPayloadHandlersEvent event) {
      PayloadRegistrar r = event.registrar(FirstAid.MODID).versioned("1");
      r.playToClient(UpdatePart.TYPE, UpdatePart.CODEC, (m, c) -> c.enqueueWork(() -> ichttt.mods.firstaid.client.ClientHandlers.updatePart(m)));
      r.playToClient(SyncDamageModel.TYPE, SyncDamageModel.CODEC, (m, c) -> c.enqueueWork(() -> ichttt.mods.firstaid.client.ClientHandlers.sync(m)));
      r.playToClient(Configuration.TYPE, Configuration.CODEC, (m, c) -> c.enqueueWork(() -> ichttt.mods.firstaid.client.ClientHandlers.configuration(m)));
      r.playToClient(ApplyAbsorption.TYPE, ApplyAbsorption.CODEC, (m, c) -> c.enqueueWork(() -> ichttt.mods.firstaid.client.ClientHandlers.absorption(m)));
      r.playToClient(AddHealth.TYPE, AddHealth.CODEC, (m, c) -> c.enqueueWork(() -> ichttt.mods.firstaid.client.ClientHandlers.addHealth(m)));
      r.playToClient(PlayHurtSound.TYPE, PlayHurtSound.CODEC, (m, c) -> c.enqueueWork(() -> ichttt.mods.firstaid.client.ClientHandlers.hurtSound(m)));
      r.playToServer(ApplyHealingItem.TYPE, ApplyHealingItem.CODEC, FirstAidNetwork::applyHealingItem);
      r.playToServer(ClientRequest.TYPE, ClientRequest.CODEC, FirstAidNetwork::clientRequest);
   }

   public static void updatePart(ServerPlayer player, DamageablePart part) {
      if (player.connection != null) {
         PacketDistributor.sendToPlayer(player, new UpdatePart(part.part.id, part.getMaxHealth(), part.getAbsorption(), part.currentHealth));
      }
   }

   public static void syncDamageModel(ServerPlayer player, PlayerDamageModel model, boolean scaleMaxHealth) {
      if (player.connection != null) {
         PacketDistributor.sendToPlayer(player, new SyncDamageModel(model.serializeNBT(player.registryAccess()), scaleMaxHealth));
      }
   }

   public static void configuration(ServerPlayer player, PlayerDamageModel model) {
      PacketDistributor.sendToPlayer(player, new Configuration(model.serializeNBT(player.registryAccess())));
   }

   public static void applyAbsorption(ServerPlayer player, float amount) {
      PacketDistributor.sendToPlayer(player, new ApplyAbsorption(amount));
   }

   public static void addHealth(ServerPlayer player, float[] table) {
      PacketDistributor.sendToPlayer(player, new AddHealth(table));
   }

   public static void playHurtSound(ServerPlayer player, SoundEvent sound, int duration) {
      PacketDistributor.sendToPlayer(player, new PlayHurtSound(BuiltInRegistries.SOUND_EVENT.getKey(sound), duration));
   }

   public static void sendApplyHealingItem(EnumPlayerPart part, InteractionHand hand) {
      PacketDistributor.sendToServer(new ApplyHealingItem(part.id, hand == InteractionHand.MAIN_HAND));
   }

   public static void sendClientRequest(int type) {
      PacketDistributor.sendToServer(new ClientRequest(type));
   }

   /** MessageApplyHealingItem.Handler */
   private static void applyHealingItem(ApplyHealingItem msg, IPayloadContext ctx) {
      ctx.enqueueWork(() -> {
         if (!(ctx.player() instanceof ServerPlayer player)) {
            return;
         }
         PlayerDamageModel damageModel = FirstAid.getDamageModel(player);
         ItemStack stack = player.getItemInHand(msg.mainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
         PartHealer healer = FirstAidRegistryImpl.INSTANCE.getPartHealer(stack);
         if (healer == null) {
            FirstAid.LOGGER.warn("Player {} has invalid item in hand {} while it should be an healing item", player.getName().getString(),
               BuiltInRegistries.ITEM.getKey(stack.getItem()));
            player.sendSystemMessage(Component.literal("Unable to apply healing item!"));
         } else {
            stack.shrink(1);
            damageModel.getFromEnum(EnumPlayerPart.fromID(msg.part)).activeHealer = healer;
         }
      });
   }

   /** MessageClientRequest.Handler: 0 = TUTORIAL_COMPLETE, 1 = REQUEST_REFRESH */
   private static void clientRequest(ClientRequest msg, IPayloadContext ctx) {
      ctx.enqueueWork(() -> {
         if (ctx.player() instanceof ServerPlayer player) {
            if (msg.kind == 0) {
               FirstAid.getDamageModel(player).hasTutorial = true;
            } else if (msg.kind == 1) {
               syncDamageModel(player, FirstAid.getDamageModel(player), true);
            }
         }
      });
   }

   public record UpdatePart(byte id, int maxHealth, float absorption, float currentHealth) implements CustomPacketPayload {
      public static final Type<UpdatePart> TYPE = new Type<>(rl("update_part"));
      public static final StreamCodec<FriendlyByteBuf, UpdatePart> CODEC = StreamCodec.of((b, m) -> {
         b.writeByte(m.id);
         b.writeInt(m.maxHealth);
         b.writeFloat(m.absorption);
         b.writeFloat(m.currentHealth);
      }, b -> new UpdatePart(b.readByte(), b.readInt(), b.readFloat(), b.readFloat()));

      @Override
      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public record SyncDamageModel(CompoundTag model, boolean scaleMaxHealth) implements CustomPacketPayload {
      public static final Type<SyncDamageModel> TYPE = new Type<>(rl("sync_damage_model"));
      public static final StreamCodec<FriendlyByteBuf, SyncDamageModel> CODEC = StreamCodec.of((b, m) -> {
         b.writeNbt(m.model);
         b.writeBoolean(m.scaleMaxHealth);
      }, b -> new SyncDamageModel(b.readNbt(), b.readBoolean()));

      @Override
      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public record Configuration(CompoundTag model) implements CustomPacketPayload {
      public static final Type<Configuration> TYPE = new Type<>(rl("configuration"));
      public static final StreamCodec<FriendlyByteBuf, Configuration> CODEC = StreamCodec.of((b, m) -> b.writeNbt(m.model), b -> new Configuration(b.readNbt()));

      @Override
      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public record ApplyAbsorption(float amount) implements CustomPacketPayload {
      public static final Type<ApplyAbsorption> TYPE = new Type<>(rl("apply_absorption"));
      public static final StreamCodec<FriendlyByteBuf, ApplyAbsorption> CODEC = StreamCodec.of((b, m) -> b.writeFloat(m.amount), b -> new ApplyAbsorption(b.readFloat()));

      @Override
      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public record AddHealth(float[] table) implements CustomPacketPayload {
      public static final Type<AddHealth> TYPE = new Type<>(rl("add_health"));
      public static final StreamCodec<FriendlyByteBuf, AddHealth> CODEC = StreamCodec.of((b, m) -> {
         for (float f : m.table) {
            b.writeFloat(f);
         }
      }, b -> {
         float[] t = new float[8];
         for (int i = 0; i < 8; i++) {
            t[i] = b.readFloat();
         }
         return new AddHealth(t);
      });

      @Override
      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public record PlayHurtSound(ResourceLocation sound, int duration) implements CustomPacketPayload {
      public static final Type<PlayHurtSound> TYPE = new Type<>(rl("play_hurt_sound"));
      public static final StreamCodec<FriendlyByteBuf, PlayHurtSound> CODEC = StreamCodec.of((b, m) -> {
         b.writeResourceLocation(m.sound);
         b.writeInt(m.duration);
      }, b -> new PlayHurtSound(b.readResourceLocation(), b.readInt()));

      @Override
      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public record ApplyHealingItem(byte part, boolean mainHand) implements CustomPacketPayload {
      public static final Type<ApplyHealingItem> TYPE = new Type<>(rl("apply_healing_item"));
      public static final StreamCodec<FriendlyByteBuf, ApplyHealingItem> CODEC = StreamCodec.of((b, m) -> {
         b.writeByte(m.part);
         b.writeBoolean(m.mainHand);
      }, b -> new ApplyHealingItem(b.readByte(), b.readBoolean()));

      @Override
      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public record ClientRequest(int kind) implements CustomPacketPayload {
      public static final Type<ClientRequest> TYPE = new Type<>(rl("client_request"));
      public static final StreamCodec<FriendlyByteBuf, ClientRequest> CODEC = StreamCodec.of((b, m) -> b.writeByte(m.kind), b -> new ClientRequest(b.readByte()));

      @Override
      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }
}
