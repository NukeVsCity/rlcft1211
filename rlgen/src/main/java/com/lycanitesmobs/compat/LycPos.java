package com.lycanitesmobs.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** 1.12 BlockPos constructors (the double ones floor like 1.12). */
public final class LycPos {
   private LycPos() {
   }

   public static BlockPos of(int x, int y, int z) { return new BlockPos(x, y, z); }
   public static BlockPos of(double x, double y, double z) { return BlockPos.containing(x, y, z); }
   public static BlockPos of(Entity e) { return BlockPos.containing(e.getX(), e.getY(), e.getZ()); }
   public static BlockPos of(Vec3 v) { return BlockPos.containing(v.x, v.y, v.z); }
   public static BlockPos of(Vec3i v) { return new BlockPos(v.getX(), v.getY(), v.getZ()); }
}
