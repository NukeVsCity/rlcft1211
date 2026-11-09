package com.github.alexthe666.iceandfire.entity;

import net.ilexiconn.llibrary.server.entity.EntityProperties;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/** 1.12 MiscPlayerProperties (EntityPlayer), registered with LLibrary's EntityPropertiesHandler like 1.12. */
public class MiscPlayerProperties extends EntityProperties<Player> {
   public boolean hasDismountedDragon;

   @Nullable
   public static MiscPlayerProperties get(Entity entity) {
      return EntityPropertiesHandler.INSTANCE.getProperties(entity, MiscPlayerProperties.class);
   }

   @Override
   public int getTrackingTime() {
      return 20;
   }

   @Override
   public void saveNBTData(CompoundTag compound) {
      compound.putBoolean("DismountedDragon", this.hasDismountedDragon);
   }

   @Override
   public void loadNBTData(CompoundTag compound) {
      this.hasDismountedDragon = compound.getBoolean("DismountedDragon");
   }

   @Override
   public void init() {
      this.hasDismountedDragon = false;
   }

   @Override
   public Class<Player> getEntityClass() {
      return Player.class;
   }

   @Override
   public String getID() {
      return "Ice And Fire - Player Property Tracker";
   }
}
