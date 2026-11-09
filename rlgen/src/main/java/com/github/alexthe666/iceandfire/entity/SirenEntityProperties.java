package com.github.alexthe666.iceandfire.entity;

import net.ilexiconn.llibrary.server.entity.EntityProperties;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import com.github.alexthe666.iceandfire.core.ModEntityHooks;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/** 1.12 SirenEntityProperties (EntityLivingBase), registered with LLibrary's EntityPropertiesHandler like 1.12. */
public class SirenEntityProperties extends EntityProperties<LivingEntity> {
   public boolean isCharmed;
   public int sirenID;
   public int singTime;

   @Nullable
   public static SirenEntityProperties get(Entity entity) {
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

   /** 1.12 getSiren(world): the entity with sirenID if it is a siren (cast it to EntitySiren). */
   @Nullable
   public Entity getSiren(Level world) {
      Entity entity = world.getEntity(this.sirenID);
      return ModEntityHooks.is(entity, "siren") ? entity : null;
   }

   @Override
   public void init() {
      this.isCharmed = false;
   }

   @Override
   public Class<LivingEntity> getEntityClass() {
      return LivingEntity.class;
   }

   @Override
   public String getID() {
      return "Ice And Fire - Siren Property Tracker";
   }
}
