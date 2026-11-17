package com.lycanitesmobs.compat;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/** 1.12 FMLCommonHandler.getMinecraftServerInstance / DimensionManager.getWorld(id). */
public final class LycServer {
   private LycServer() {
   }

   public static MinecraftServer get() {
      return ServerLifecycleHooks.getCurrentServer();
   }

   public static ServerLevel level(int dimension) {
      MinecraftServer server = get();
      if (server == null) {
         return null;
      }
      for (ServerLevel level : server.getAllLevels()) {
         if (LycDim.id(level) == dimension) {
            return level;
         }
      }
      return null;
   }
}
