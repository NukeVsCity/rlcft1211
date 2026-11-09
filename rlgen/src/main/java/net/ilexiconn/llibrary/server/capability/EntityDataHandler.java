package net.ilexiconn.llibrary.server.capability;

import java.util.List;
import javax.annotation.Nullable;
import net.ilexiconn.llibrary.LLibrary;
import net.minecraft.world.entity.Entity;

/**
 * LLibrary 1.7.20 EntityDataHandler. The 1.12 capability is the attachment {@code llibrary:entity_data}
 * ({@link LLibrary#ENTITY_DATA_CAPABILITY}); it is created on first access with every EntityProperties registered for the
 * entity's class (1.12 attached it in AttachCapabilitiesEvent), saved with the entity and copied to the respawned player.
 */
public enum EntityDataHandler {
   INSTANCE;

   public IEntityDataCapability getCapability(Entity entity) {
      return entity.getData(LLibrary.ENTITY_DATA_CAPABILITY);
   }

   /** 1.12 deprecated registerExtendedEntityData: adds data to the entity's capability. */
   @Deprecated
   public <T extends Entity> void registerExtendedEntityData(T entity, IEntityData<T> entityData) {
      this.getCapability(entity).registerData(entityData);
   }

   @Nullable
   public <T extends Entity> IEntityData<T> getEntityData(T entity, String identifier) {
      return this.getCapability(entity).getData(identifier);
   }

   public <T extends Entity> List<IEntityData<T>> getEntityData(T entity) {
      return this.getCapability(entity).getData();
   }
}
