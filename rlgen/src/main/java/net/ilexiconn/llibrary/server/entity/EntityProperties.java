package net.ilexiconn.llibrary.server.entity;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.ilexiconn.llibrary.server.capability.IEntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * LLibrary 1.7.20 EntityProperties. 1.12 quirk kept: LLibrary 1.7.20 never calls IEntityDataCapability.init, so
 * {@link #init(Entity, Level)} / {@link #init()} never run: getEntity() and getWorld() stay null and the fields keep their
 * Java initialisers (e.g. IaF FrozenEntityProperties.setFrozenFor also freezes ice dragons, chickens start with
 * timeUntilNextEgg = 1).
 */
public abstract class EntityProperties<T extends Entity> implements IEntityData<T> {
   private Level world;
   private T entity;
   private final Set<PropertiesTracker<?>> trackers = Collections.newSetFromMap(new WeakHashMap<>());

   @Override
   public final void init(T entity, Level world) {
      this.entity = entity;
      this.world = world;
      this.init();
   }

   public Set<PropertiesTracker<?>> getTrackers() {
      return this.trackers;
   }

   public void sync() {
      this.trackers.forEach(PropertiesTracker::setReady);
   }

   public final Level getWorld() {
      return this.world;
   }

   public final T getEntity() {
      return this.entity;
   }

   public abstract void init();

   @Override
   public abstract String getID();

   public abstract Class<T> getEntityClass();

   public int getTrackingTime() {
      return -1;
   }

   public int getTrackingUpdateTime() {
      return 0;
   }

   public void saveTrackingSensitiveData(CompoundTag compound) {
      this.saveNBTData(compound);
   }

   public void loadTrackingSensitiveData(CompoundTag compound) {
      this.loadNBTData(compound);
   }

   public void onSync() {
   }

   public PropertiesTracker<T> createTracker(T entity) {
      PropertiesTracker<T> tracker = new PropertiesTracker<>(entity, this);
      this.trackers.add(tracker);
      return tracker;
   }
}
