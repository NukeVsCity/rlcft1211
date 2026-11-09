package net.ilexiconn.llibrary.server.entity.multipart;

import net.minecraft.world.entity.Mob;

/**
 * LLibrary 1.7.20 IMultipartEntity. 1.12 {@code Entity[] getParts()} overrode vanilla Entity.getParts(); in 1.21 the
 * implementing entity overrides NeoForge's {@code PartEntity<?>[] getParts()} (same name, covariant array) and must also
 * override {@code isMultipartEntity()} to return true.
 */
public interface IMultipartEntity {
   net.neoforged.neoforge.entity.PartEntity<?>[] getParts();

   default void onUpdateParts() {
      for (net.neoforged.neoforge.entity.PartEntity<?> entity : this.getParts()) {
         entity.tick();
      }
   }

   default PartEntity create(float radius, float angleYaw, float offsetY, float sizeX, float sizeY) {
      return this.create(radius, angleYaw, offsetY, sizeX, sizeY, 1.0F);
   }

   default PartEntity create(float radius, float angleYaw, float offsetY, float sizeX, float sizeY, float damageMultiplier) {
      return new PartEntity(this.getEntity(), radius, angleYaw, offsetY, sizeX, sizeY, damageMultiplier);
   }

   default Mob getEntity() {
      return (Mob)this;
   }
}
