package net.ilexiconn.llibrary.server;

import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.ilexiconn.llibrary.server.network.MessageContext;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;

/** LLibrary 1.7.20 ServerProxy (LLibrary.PROXY on a dedicated server; ClientProxy on the client). */
public class ServerProxy {
   public void onPreInit() {
      NeoForge.EVENT_BUS.register(ServerEventHandler.INSTANCE);
      NeoForge.EVENT_BUS.register(EntityPropertiesHandler.INSTANCE);
   }

   /** Runs on the main thread (NeoForge payload handlers default to it, like 1.12 addScheduledTask). */
   public <T extends AbstractMessage<T>> void handleMessage(T message, MessageContext messageContext) {
      if (messageContext.getContext().flow().isServerbound()) {
         Player player = messageContext.getContext().player();
         message.onServerReceived(player.getServer(), message, player, messageContext);
      }
   }

   public float getPartialTicks() {
      return 0.0F;
   }
}
