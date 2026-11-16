package com.lycanitesmobs.compat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** 1.12 Entity.motionX/Y/Z and posX/Y/Z field writes on the 1.21 API. */
public final class LycMotion {
   private LycMotion() {
   }

   public static void setX(Entity e, double v) { Vec3 m = e.getDeltaMovement(); e.setDeltaMovement(v, m.y, m.z); }
   public static void setY(Entity e, double v) { Vec3 m = e.getDeltaMovement(); e.setDeltaMovement(m.x, v, m.z); }
   public static void setZ(Entity e, double v) { Vec3 m = e.getDeltaMovement(); e.setDeltaMovement(m.x, m.y, v); }
   public static void addX(Entity e, double v) { setX(e, e.getDeltaMovement().x + v); }
   public static void addY(Entity e, double v) { setY(e, e.getDeltaMovement().y + v); }
   public static void addZ(Entity e, double v) { setZ(e, e.getDeltaMovement().z + v); }
   public static void subX(Entity e, double v) { setX(e, e.getDeltaMovement().x - v); }
   public static void subY(Entity e, double v) { setY(e, e.getDeltaMovement().y - v); }
   public static void subZ(Entity e, double v) { setZ(e, e.getDeltaMovement().z - v); }
   public static void mulX(Entity e, double v) { setX(e, e.getDeltaMovement().x * v); }
   public static void mulY(Entity e, double v) { setY(e, e.getDeltaMovement().y * v); }
   public static void mulZ(Entity e, double v) { setZ(e, e.getDeltaMovement().z * v); }
   public static void divX(Entity e, double v) { setX(e, e.getDeltaMovement().x / v); }
   public static void divY(Entity e, double v) { setY(e, e.getDeltaMovement().y / v); }
   public static void divZ(Entity e, double v) { setZ(e, e.getDeltaMovement().z / v); }

   public static void setPosX(Entity e, double v) { e.setPosRaw(v, e.getY(), e.getZ()); }
   public static void setPosY(Entity e, double v) { e.setPosRaw(e.getX(), v, e.getZ()); }
   public static void setPosZ(Entity e, double v) { e.setPosRaw(e.getX(), e.getY(), v); }
   public static void addPosX(Entity e, double v) { setPosX(e, e.getX() + v); }
   public static void addPosY(Entity e, double v) { setPosY(e, e.getY() + v); }
   public static void addPosZ(Entity e, double v) { setPosZ(e, e.getZ() + v); }
   public static void subPosX(Entity e, double v) { setPosX(e, e.getX() - v); }
   public static void subPosY(Entity e, double v) { setPosY(e, e.getY() - v); }
   public static void subPosZ(Entity e, double v) { setPosZ(e, e.getZ() - v); }
}
