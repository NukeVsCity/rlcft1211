package net.ilexiconn.llibrary.server.network;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * Stand-in for Forge 1.12 SimpleNetworkWrapper on top of NeoForge payloads. Every registered {@link AbstractMessage} class
 * becomes a bidirectional payload {@code <channel>:<lowercase simple class name>}; the payloads are registered in
 * RegisterPayloadHandlersEvent by {@link NetworkHandler}, so create the wrapper and register its messages during mod
 * construction.
 */
public class SimpleNetworkWrapper {
   final String channel;
   final List<Class<? extends AbstractMessage<?>>> messages = new ArrayList<>();

   public SimpleNetworkWrapper(String channel) {
      this.channel = channel;
      NetworkHandler.INSTANCE.addWrapper(this);
   }

   public String getChannel() {
      return this.channel;
   }

   public <T extends AbstractMessage<T>> void registerMessage(Class<T> messageClass) {
      NetworkHandler.INSTANCE.registerMessage(this, messageClass);
   }

   /** Client -> server. */
   public void sendToServer(AbstractMessage<?> message) {
      PacketDistributor.sendToServer(message);
   }

   public void sendTo(AbstractMessage<?> message, ServerPlayer player) {
      PacketDistributor.sendToPlayer(player, message);
   }

   public void sendToAll(AbstractMessage<?> message) {
      if (ServerLifecycleHooks.getCurrentServer() != null) {
         PacketDistributor.sendToAllPlayers(message);
      }
   }

   public void sendToAllAround(AbstractMessage<?> message, TargetPoint point) {
      MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
      ServerLevel level = server == null ? null : server.getLevel(point.dimension);
      if (level != null) {
         PacketDistributor.sendToPlayersNear(level, null, point.x, point.y, point.z, point.range, message);
      }
   }

   /** Every player tracking the entity (not the entity itself), like 1.12 sendToAllTracking(message, entity). */
   public void sendToAllTracking(AbstractMessage<?> message, Entity entity) {
      PacketDistributor.sendToPlayersTrackingEntity(entity, message);
   }

   public void sendToDimension(AbstractMessage<?> message, ResourceKey<Level> dimension) {
      MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
      ServerLevel level = server == null ? null : server.getLevel(dimension);
      if (level != null) {
         PacketDistributor.sendToPlayersInDimension(level, message);
      }
   }

   /** 1.12 NetworkRegistry.TargetPoint with the dimension id replaced by a level key. */
   public static class TargetPoint {
      public final ResourceKey<Level> dimension;
      public final double x;
      public final double y;
      public final double z;
      public final double range;

      public TargetPoint(ResourceKey<Level> dimension, double x, double y, double z, double range) {
         this.dimension = dimension;
         this.x = x;
         this.y = y;
         this.z = z;
         this.range = range;
      }
   }
}
