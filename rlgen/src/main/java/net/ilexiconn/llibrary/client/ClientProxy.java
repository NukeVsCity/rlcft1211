package net.ilexiconn.llibrary.client;

import net.ilexiconn.llibrary.server.ServerProxy;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.ilexiconn.llibrary.server.network.MessageContext;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/** LLibrary 1.7.20 ClientProxy (patron list, update checker, language download, snackbars and survival tabs not ported). */
@OnlyIn(Dist.CLIENT)
public class ClientProxy extends ServerProxy {
   @Override
   public <T extends AbstractMessage<T>> void handleMessage(T message, MessageContext messageContext) {
      if (messageContext.getContext().flow().isServerbound()) {
         super.handleMessage(message, messageContext);
      } else {
         Minecraft mc = Minecraft.getInstance();
         message.onClientReceived(mc, message, mc.player, messageContext);
      }
   }

   /** 1.12 Timer.renderPartialTicks. */
   @Override
   public float getPartialTicks() {
      return Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
   }
}
