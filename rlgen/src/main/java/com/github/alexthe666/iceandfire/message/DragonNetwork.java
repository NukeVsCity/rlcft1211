package com.github.alexthe666.iceandfire.message;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Ice and Fire 1.7.1 MessageDragonControl (rider key state + position, client to server). Other rideable Ice and Fire mobs
 * (hippogryph, hippocampus, death worm, amphithere) implement {@link Controllable} to receive the same message.
 * 1.12 MessageDragonArmor (client to server armor ints) is not needed: the 1.21 menu changes the armor on the server.
 */
public final class DragonNetwork {
   private DragonNetwork() {
   }

   /** A mob steered with MessageDragonControl. */
   public interface Controllable {
      void setControlState(byte state);

      /** Whether this player's control state is accepted (1.12: the owner; the death worm accepted anyone). */
      boolean acceptsControlFrom(Player player);
   }

   public record MessageDragonControl(int dragonId, byte controlState, double posX, double posY, double posZ) implements CustomPacketPayload {
      public static final Type<MessageDragonControl> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("iceandfire", "dragon_control"));
      public static final StreamCodec<ByteBuf, MessageDragonControl> CODEC = StreamCodec.composite(
         ByteBufCodecs.VAR_INT,
         MessageDragonControl::dragonId,
         ByteBufCodecs.BYTE,
         MessageDragonControl::controlState,
         ByteBufCodecs.DOUBLE,
         MessageDragonControl::posX,
         ByteBufCodecs.DOUBLE,
         MessageDragonControl::posY,
         ByteBufCodecs.DOUBLE,
         MessageDragonControl::posZ,
         MessageDragonControl::new
      );

      @Override
      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public static void register(RegisterPayloadHandlersEvent event) {
      event.registrar("1").playToServer(MessageDragonControl.TYPE, MessageDragonControl.CODEC, DragonNetwork::onServerReceived);
   }

   private static void onServerReceived(MessageDragonControl message, IPayloadContext context) {
      Player player = context.player();
      Entity entity = player.level().getEntity(message.dragonId());
      if (entity instanceof EntityDragonBase dragon) {
         if (dragon.isOwner(player)) {
            dragon.setControlState(message.controlState());
         }

         dragon.setPos(message.posX(), message.posY(), message.posZ());
      } else if (entity instanceof Controllable controllable) {
         if (controllable.acceptsControlFrom(player)) {
            controllable.setControlState(message.controlState());
         }

         entity.setPos(message.posX(), message.posY(), message.posZ());
      }
   }

   /** Client side: sends the rider's control state. */
   public static void sendControl(Entity mount, byte controlState) {
      PacketDistributor.sendToServer(new MessageDragonControl(mount.getId(), controlState, mount.getX(), mount.getY(), mount.getZ()));
   }
}
