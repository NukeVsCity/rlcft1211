package com.github.alexthe666.iceandfire.message;

import com.github.alexthe666.iceandfire.entity.IafLand;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** 1.12 MessageSetMyrmexHiveNull: clears ClientProxy.referedClientHive. */
public record MessageSetMyrmexHiveNull() implements CustomPacketPayload {
   public static final MessageSetMyrmexHiveNull INSTANCE = new MessageSetMyrmexHiveNull();
   public static final Type<MessageSetMyrmexHiveNull> TYPE = new Type<>(IafLand.rl("set_myrmex_hive_null"));
   public static final StreamCodec<ByteBuf, MessageSetMyrmexHiveNull> CODEC = StreamCodec.unit(INSTANCE);

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(MessageSetMyrmexHiveNull message, IPayloadContext context) {
      com.github.alexthe666.iceandfire.client.MyrmexStaffClient.setReferedClientHive(null);
   }
}
