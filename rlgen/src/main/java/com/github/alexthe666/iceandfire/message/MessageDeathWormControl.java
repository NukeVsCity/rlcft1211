package com.github.alexthe666.iceandfire.message;

import com.github.alexthe666.iceandfire.entity.EntityDeathWorm;
import com.github.alexthe666.iceandfire.entity.IafLand;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** The death worm branch of 1.12 MessageDragonControl (control bits and position from the rider's client). */
public record MessageDeathWormControl(int entityId, byte controlState, double posX, double posY, double posZ) implements CustomPacketPayload {
   public static final Type<MessageDeathWormControl> TYPE = new Type<>(IafLand.rl("deathworm_control"));
   public static final StreamCodec<ByteBuf, MessageDeathWormControl> CODEC = StreamCodec.composite(
      ByteBufCodecs.INT,
      MessageDeathWormControl::entityId,
      ByteBufCodecs.BYTE,
      MessageDeathWormControl::controlState,
      ByteBufCodecs.DOUBLE,
      MessageDeathWormControl::posX,
      ByteBufCodecs.DOUBLE,
      MessageDeathWormControl::posY,
      ByteBufCodecs.DOUBLE,
      MessageDeathWormControl::posZ,
      MessageDeathWormControl::new
   );

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(MessageDeathWormControl message, IPayloadContext context) {
      Entity entity = context.player().level().getEntity(message.entityId);
      if (entity instanceof EntityDeathWorm deathworm) {
         deathworm.setControlState(message.controlState);
         deathworm.setPos(message.posX, message.posY, message.posZ);
      }
   }
}
