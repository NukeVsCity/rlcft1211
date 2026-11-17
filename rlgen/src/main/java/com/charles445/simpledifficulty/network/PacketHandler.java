package com.charles445.simpledifficulty.network;

import com.charles445.simpledifficulty.SimpleDifficulty;
import com.charles445.simpledifficulty.api.SDCapabilities;
import com.charles445.simpledifficulty.api.thirst.ThirstEnum;
import com.charles445.simpledifficulty.util.DamageUtil;
import com.charles445.simpledifficulty.util.ThirstUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** 1.12 PacketHandler: MessageUpdateThirst, MessageUpdateTemperature (capability NBT to the client) and MessageDrinkWater. */
public class PacketHandler {
   public static void register(RegisterPayloadHandlersEvent event) {
      PayloadRegistrar r = event.registrar(SimpleDifficulty.MODID).versioned("1");
      r.playToClient(UpdateThirst.TYPE, UpdateThirst.CODEC, (msg, ctx) -> ctx.enqueueWork(() -> {
         Player p = ctx.player();
         if (p != null) {
            SDCapabilities.getThirstData(p).deserializeNBT(p.registryAccess(), msg.tag());
         }
      }));
      r.playToClient(UpdateTemperature.TYPE, UpdateTemperature.CODEC, (msg, ctx) -> ctx.enqueueWork(() -> {
         Player p = ctx.player();
         if (p != null) {
            SDCapabilities.getTemperatureData(p).deserializeNBT(p.registryAccess(), msg.tag());
         }
      }));
      r.playToServer(DrinkWater.TYPE, DrinkWater.CODEC, PacketHandler::handleDrink);
   }

   public static void sendThirstUpdate(ServerPlayer player) {
      PacketDistributor.sendToPlayer(player, new UpdateThirst(SDCapabilities.getThirstData(player).serializeNBT(player.registryAccess())));
   }

   public static void sendTemperatureUpdate(ServerPlayer player) {
      PacketDistributor.sendToPlayer(player, new UpdateTemperature(SDCapabilities.getTemperatureData(player).serializeNBT(player.registryAccess())));
   }

   public static void sendDrinkWater() {
      PacketDistributor.sendToServer(DrinkWater.INSTANCE);
   }

   /** MessageDrinkWater.Handler */
   private static void handleDrink(DrinkWater msg, IPayloadContext ctx) {
      ctx.enqueueWork(() -> {
         if (ctx.player() instanceof ServerPlayer player) {
            ThirstEnum.BlockPosResult traceResult = ThirstUtil.traceWaterToDrink(player);
            if (traceResult != null) {
               ThirstEnum result = traceResult.thirstEnum;
               ThirstUtil.takeDrink(player, result.getThirst(), result.getSaturation(), result.getThirstyChance());
               DamageUtil.commonPlayPlayerSound(player, SoundEvents.GENERIC_DRINK);
            }
         }
      });
   }

   private static ResourceLocation id(String s) {
      return ResourceLocation.fromNamespaceAndPath(SimpleDifficulty.MODID, s);
   }

   public record UpdateThirst(CompoundTag tag) implements CustomPacketPayload {
      public static final Type<UpdateThirst> TYPE = new Type<>(id("update_thirst"));
      public static final StreamCodec<FriendlyByteBuf, UpdateThirst> CODEC = ByteBufCodecs.COMPOUND_TAG.map(UpdateThirst::new, UpdateThirst::tag).cast();

      @Override
      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public record UpdateTemperature(CompoundTag tag) implements CustomPacketPayload {
      public static final Type<UpdateTemperature> TYPE = new Type<>(id("update_temperature"));
      public static final StreamCodec<FriendlyByteBuf, UpdateTemperature> CODEC = ByteBufCodecs.COMPOUND_TAG.map(UpdateTemperature::new, UpdateTemperature::tag).cast();

      @Override
      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public record DrinkWater() implements CustomPacketPayload {
      public static final DrinkWater INSTANCE = new DrinkWater();
      public static final Type<DrinkWater> TYPE = new Type<>(id("drink_water"));
      public static final StreamCodec<FriendlyByteBuf, DrinkWater> CODEC = StreamCodec.unit(INSTANCE);

      @Override
      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }
}
