package com.github.alexthe666.iceandfire.entity;

import net.ilexiconn.llibrary.server.entity.EntityProperties;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/** Ice and Fire 1.7.1 SirenEntityProperties (llibrary entity properties, synced to trackers every 20 ticks). */
public class SirenEntityProperties extends EntityProperties<LivingEntity> {
   public boolean isCharmed;
   public int sirenID;
   public int singTime;

   public static SirenEntityProperties get(LivingEntity entity) {
      return EntityPropertiesHandler.INSTANCE.getProperties(entity, SirenEntityProperties.class);
   }

   @Override
   public int getTrackingTime() {
      return 20;
   }

   @Override
   public void saveNBTData(CompoundTag compound) {
      compound.putBoolean("CharmedBySiren", this.isCharmed);
      compound.putInt("SirenID", this.sirenID);
   }

   @Override
   public void loadNBTData(CompoundTag compound) {
      this.isCharmed = compound.getBoolean("CharmedBySiren");
      this.sirenID = compound.getInt("SirenID");
   }

   public EntitySiren getSiren(Level world) {
      Entity entity = world.getEntity(this.sirenID);
      return entity instanceof EntitySiren siren ? siren : null;
   }

   @Override
   public void init() {
      this.isCharmed = false;
   }

   @Override
   public String getID() {
      return "Ice And Fire - Siren Property Tracker";
   }

   @Override
   public Class<LivingEntity> getEntityClass() {
      return LivingEntity.class;
   }
}
