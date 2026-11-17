package com.lycanitesmobs.compat;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/** 1.12 dimension ids: overworld 0, nether -1, end 1, others a stable number from their name. */
public final class LycDim {
   private LycDim() {
   }

   public static int id(Level level) {
      return id(level.dimension());
   }

   public static int id(Entity e) {
      return id(e.level());
   }

   public static int id(ResourceKey<Level> key) {
      if (key == Level.OVERWORLD) return 0;
      if (key == Level.NETHER) return -1;
      if (key == Level.END) return 1;
      return 1000 + (key.location().toString().hashCode() & 0xFFFF);
   }
}
