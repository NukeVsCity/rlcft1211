package com.github.alexthe666.iceandfire.entity;

import net.ilexiconn.llibrary.server.entity.EntityProperties;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import com.github.alexthe666.iceandfire.core.ModEntityHooks;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/** 1.12 FrozenEntityProperties (LLibrary EntityProperties of EntityLivingBase), registered with LLibrary's EntityPropertiesHandler like 1.12. */
public class FrozenEntityProperties extends EntityProperties<LivingEntity> {
   public boolean isFrozen;
   public int ticksUntilUnfrozen;

   /** 1.12 EntityPropertiesHandler.INSTANCE.getProperties(entity, FrozenEntityProperties.class). */
   @Nullable
   public static FrozenEntityProperties get(Entity entity) {
      return EntityPropertiesHandler.INSTANCE.getProperties(entity, FrozenEntityProperties.class);
   }

   @Override
   public int getTrackingTime() {
      return 20;
   }

   @Override
   public void saveNBTData(CompoundTag compound) {
      compound.putBoolean("IsFrozen", this.isFrozen);
      compound.putInt("TicksUntilUnfrozen", this.ticksUntilUnfrozen);
   }

   @Override
   public void loadNBTData(CompoundTag compound) {
      this.isFrozen = compound.getBoolean("IsFrozen");
      this.ticksUntilUnfrozen = compound.getInt("TicksUntilUnfrozen");
   }

   @Override
   public void init() {
      this.isFrozen = false;
      this.ticksUntilUnfrozen = 0;
   }

   @Override
   public Class<LivingEntity> getEntityClass() {
      return LivingEntity.class;
   }

   @Override
   public String getID() {
      return "Ice And Fire - Frozen Property Tracker";
   }

   public void setFrozenFor(int frozenFor) {
      if (!ModEntityHooks.is(this.getEntity(), "icedragon")) {
         this.isFrozen = true;
         this.ticksUntilUnfrozen = frozenFor;
      }
   }
}
