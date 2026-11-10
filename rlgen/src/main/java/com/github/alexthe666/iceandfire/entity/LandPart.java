package com.github.alexthe666.iceandfire.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

/**
 * LLibrary PartEntity as the land mobs used it (cyclops eye, death worm segments): created by the mob, updated every tick, but
 * never added to the world, so nothing can hit it. It only follows the parent and pushes entities that touch it on the server.
 */
public class LandPart {
   protected final Mob parent;
   protected float radius;
   protected final float angleYaw;
   protected final float offsetY;
   public float width;
   public float height;
   protected final float damageMultiplier;
   public double posX;
   public double posY;
   public double posZ;
   public double prevPosX;
   public double prevPosY;
   public double prevPosZ;

   public LandPart(Mob parent, float radius, float angleYaw, float offsetY, float sizeX, float sizeY, float damageMultiplier) {
      this.parent = parent;
      this.radius = radius;
      this.angleYaw = (angleYaw + 90.0F) * (float)(Math.PI / 180.0);
      this.offsetY = offsetY;
      this.width = sizeX;
      this.height = sizeY;
      this.damageMultiplier = damageMultiplier;
   }

   /** PartEntity.onUpdate */
   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      this.posX = this.parent.getX() + (double)this.radius * Math.cos((double)this.parent.yBodyRot * (Math.PI / 180.0) + (double)this.angleYaw);
      this.posY = this.parent.getY() + (double)this.offsetY;
      this.posZ = this.parent.getZ() + (double)this.radius * Math.sin((double)this.parent.yBodyRot * (Math.PI / 180.0) + (double)this.angleYaw);
      if (!this.parent.level().isClientSide) {
         this.collideWithNearbyEntities();
      }
   }

   public AABB getBoundingBox() {
      double w = this.width / 2.0;
      return new AABB(this.posX - w, this.posY, this.posZ - w, this.posX + w, this.posY + this.height, this.posZ + w);
   }

   /** PartEntity.collideWithNearbyEntities (1.12 expand(0.2, 0, 0.2) grows towards +x/+z only). */
   public void collideWithNearbyEntities() {
      for (Entity entity : this.parent.level().getEntities(this.parent, this.getBoundingBox().expandTowards(0.2F, 0.0, 0.2F))) {
         if (entity != this.parent && entity.isPushable()) {
            entity.push(this.parent);
         }
      }
   }

   public boolean attackEntityFrom(DamageSource source, float damage) {
      return this.parent.hurt(source, damage * this.damageMultiplier);
   }
}
