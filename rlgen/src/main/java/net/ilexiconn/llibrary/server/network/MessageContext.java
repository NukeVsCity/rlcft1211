package net.ilexiconn.llibrary.server.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Stand-in for Forge 1.12 MessageContext: the side that received the message and the 1.21 payload context. */
public class MessageContext {
   public final LogicalSide side;
   private final IPayloadContext context;

   public MessageContext(IPayloadContext context) {
      this.context = context;
      this.side = context.flow().getReceptionSide();
   }

   public IPayloadContext getContext() {
      return this.context;
   }

   /** 1.12 messageContext.getServerHandler().player (server side only). */
   public ServerPlayer getServerPlayer() {
      return (ServerPlayer)this.context.player();
   }
}
