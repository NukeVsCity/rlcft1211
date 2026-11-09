package com.github.alexthe666.iceandfire.message;

import com.github.alexthe666.iceandfire.entity.IafLand;
import com.github.alexthe666.iceandfire.entity.StoneEntityProperties;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** 1.12 MessageStoneStatue (server to client only here). */
public record MessageStoneStatue(int entityId, boolean isStone) implements CustomPacketPayload {
   public static final Type<MessageStoneStatue> TYPE = new Type<>(IafLand.rl("stone_statue"));
   public static final StreamCodec<ByteBuf, MessageStoneStatue> CODEC = StreamCodec.composite(
      ByteBufCodecs.INT, MessageStoneStatue::entityId, ByteBufCodecs.BOOL, MessageStoneStatue::isStone, MessageStoneStatue::new
   );

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(MessageStoneStatue message, IPayloadContext context) {
      Entity entity = context.player().level().getEntity(message.entityId);
      StoneEntityProperties properties = StoneEntityProperties.get(entity);
      if (properties != null) {
         properties.isStone = message.isStone;
      }
   }
}
