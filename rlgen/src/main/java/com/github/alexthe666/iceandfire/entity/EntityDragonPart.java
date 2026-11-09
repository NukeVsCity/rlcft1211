package com.github.alexthe666.iceandfire.entity;

/**
 * Ice and Fire 1.7.1 EntityDragonPart: head (x1.5 damage), neck, wings (x0.5), tail (tip x1.5). In 1.12 these parts were
 * created and moved every tick but never added to the world, and EntityDragonBase did not override getParts() (vanilla
 * returned null), so nothing could hit them: players hit the dragon's main box only. Kept that way.
 */
public class EntityDragonPart extends EntityMutlipartPart {
   private EntityDragonBase dragon;

   public EntityDragonPart(EntityDragonBase dragon, float radius, float angleYaw, float offsetY, float sizeX, float sizeY, float damageMultiplier) {
      super(dragon, radius, angleYaw, offsetY, sizeX, sizeY, damageMultiplier);
      this.dragon = dragon;
   }

   @Override
   public boolean fireImmune() {
      return this.dragon instanceof EntityFireDragon;
   }

   @Override
   public void collideWithNearbyEntities() {
   }
}
