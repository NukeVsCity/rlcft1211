package net.ilexiconn.llibrary.server.entity;

import net.minecraft.world.entity.Entity;

/** LLibrary 1.7.20 PropertiesTracker. */
public class PropertiesTracker<T extends Entity> {
   private int trackingTimer = 0;
   private int trackingUpdateTimer = 0;
   private boolean trackerReady = false;
   private boolean trackerDataChanged = false;
   private final SensitiveTagCompound trackingTag = new SensitiveTagCompound();
   private final T entity;
   private final EntityProperties<T> properties;

   public PropertiesTracker(T entity, EntityProperties<T> properties) {
      this.entity = entity;
      this.properties = properties;
   }

   public void updateTracker() {
      int trackingFrequency = this.properties.getTrackingTime();
      if (trackingFrequency >= 0 && !this.trackerReady) {
         this.trackingTimer++;
         if (this.trackingTimer >= trackingFrequency) {
            this.trackerReady = true;
         }
      }

      int trackingUpdateFrequency = this.properties.getTrackingUpdateTime();
      if (this.trackingUpdateTimer < trackingUpdateFrequency) {
         this.trackingUpdateTimer++;
      }

      if (this.trackingUpdateTimer >= trackingUpdateFrequency && !this.trackerDataChanged) {
         this.trackingUpdateTimer = 0;
         if (!this.trackingTag.hasChanged()) {
            this.properties.saveTrackingSensitiveData(this.trackingTag);
         }

         if (this.trackingTag.hasChanged()) {
            this.trackerDataChanged = true;
         }
      }
   }

   public void setReady() {
      this.trackerReady = true;
      this.trackerDataChanged = true;
   }

   public boolean isTrackerReady() {
      return this.properties.getTrackingTime() >= 0 && this.trackerReady && this.trackerDataChanged;
   }

   public void onSync() {
      this.properties.onSync();
   }

   public EntityProperties<T> getProperties() {
      return this.properties;
   }

   public T getEntity() {
      return this.entity;
   }

   public void removeTracker() {
      this.properties.getTrackers().remove(this);
   }

   public SensitiveTagCompound getTrackingTag() {
      return this.trackingTag;
   }

   public void reset() {
      this.trackingTimer = 0;
      this.trackerReady = false;
      this.trackerDataChanged = false;
      this.trackingTag.reset();
   }
}
