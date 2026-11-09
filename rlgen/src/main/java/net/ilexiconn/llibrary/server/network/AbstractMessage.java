package net.ilexiconn.llibrary.server.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;

/**
 * LLibrary 1.7.20 AbstractMessage as a 1.21 {@link CustomPacketPayload}. Keep the 1.12 shape: a public no-arg constructor,
 * fromBytes/toBytes (the buffer is a RegistryFriendlyByteBuf, see {@link ByteBufUtils} for strings, tags and stacks), and
 * the two handlers, which run on the main thread of the receiving side. Register the class with a
 * {@link SimpleNetworkWrapper} (or the {@link NetworkWrapper} annotation) during mod construction.
 * <p>
 * Dedicated server safety: onClientReceived bodies are verified on the server too, so keep client-only code (ClientLevel,
 * LocalPlayer, screens, ClientProxy fields) in a separate client class called from there.
 */
public abstract class AbstractMessage<T extends AbstractMessage<T>> implements CustomPacketPayload {
   public abstract void fromBytes(ByteBuf buf);

   public abstract void toBytes(ByteBuf buf);

   public abstract void onClientReceived(Minecraft client, T message, Player player, MessageContext messageContext);

   public abstract void onServerReceived(MinecraftServer server, T message, Player player, MessageContext messageContext);

   public boolean registerOnSide(LogicalSide side) {
      return true;
   }

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return NetworkHandler.INSTANCE.getType(this.getClass());
   }
}
