package com.github.alexthe666.iceandfire.message;

import com.github.alexthe666.iceandfire.entity.AirWaterRefs;
import com.github.alexthe666.iceandfire.entity.IAirWaterRideable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** The Ice and Fire 1.7.1 messages of the air and water mobs. */
public final class AirWaterNetwork {
   private AirWaterNetwork() {
   }

   public static void register(RegisterPayloadHandlersEvent event) {
      PayloadRegistrar r = event.registrar("iceandfire_airwater").versioned("1");
      r.playToServer(MobControl.TYPE, MobControl.CODEC, AirWaterNetwork::handleControl);
      r.playToClient(SirenSong.TYPE, SirenSong.CODEC, (msg, ctx) -> ctx.enqueueWork(() -> ClientHandlers.sirenSong(msg)));
   }

   /**
    * MessageDragonControl for the hippogryph, hippocampus and amphithere: the rider's key state, and the client's position of the mob.
    */
   public record MobControl(int entityId, byte controlState, double x, double y, double z) implements CustomPacketPayload {
      public static final Type<MobControl> TYPE = new Type<>(AirWaterRefs.id("mob_control"));
      public static final StreamCodec<FriendlyByteBuf, MobControl> CODEC = StreamCodec.of((buf, m) -> {
         buf.writeInt(m.entityId);
         buf.writeByte(m.controlState);
         buf.writeDouble(m.x);
         buf.writeDouble(m.y);
         buf.writeDouble(m.z);
      }, buf -> new MobControl(buf.readInt(), buf.readByte(), buf.readDouble(), buf.readDouble(), buf.readDouble()));

      @Override
      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   private static void handleControl(MobControl msg, IPayloadContext ctx) {
      ctx.enqueueWork(() -> {
         Player player = ctx.player();
         Entity entity = player.level().getEntity(msg.entityId);
         if (entity instanceof IAirWaterRideable rideable && entity instanceof TamableAnimal tame) {
            if (tame.isOwnedBy(player)) {
               rideable.setControlState(msg.controlState);
            }
            entity.setPos(msg.x, msg.y, msg.z);
         }
      });
   }

   /** MessageSirenSong */
   public record SirenSong(int sirenId, boolean singing) implements CustomPacketPayload {
      public static final Type<SirenSong> TYPE = new Type<>(AirWaterRefs.id("siren_song"));
      public static final StreamCodec<FriendlyByteBuf, SirenSong> CODEC = StreamCodec.of((buf, m) -> {
         buf.writeInt(m.sirenId);
         buf.writeBoolean(m.singing);
      }, buf -> new SirenSong(buf.readInt(), buf.readBoolean()));

      @Override
      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   /** Client-side handlers (only loaded on the client). */
   static final class ClientHandlers {
      static void sirenSong(SirenSong msg) {
         com.github.alexthe666.iceandfire.client.AirWaterClient.onSirenSong(msg.sirenId(), msg.singing());
      }
   }
}
