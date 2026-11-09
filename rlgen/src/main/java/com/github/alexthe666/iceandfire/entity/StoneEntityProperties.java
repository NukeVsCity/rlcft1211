package com.github.alexthe666.iceandfire.entity;

import net.ilexiconn.llibrary.server.entity.EntityProperties;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

/** 1.12 StoneEntityProperties (LLibrary EntityProperties of EntityLiving = 1.21 Mob), registered with LLibrary's EntityPropertiesHandler like 1.12. */
public class StoneEntityProperties extends EntityProperties<Mob> {
   public boolean isStone;
   public int breakLvl;

   /** 1.12 EntityPropertiesHandler.INSTANCE.getProperties(entity, StoneEntityProperties.class); null for non-Mob entities like 1.12. */
   @Nullable
   public static StoneEntityProperties get(Entity entity) {
      return EntityPropertiesHandler.INSTANCE.getProperties(entity, StoneEntityProperties.class);
   }

   @Override
   public int getTrackingTime() {
      return 20;
   }

   @Override
   public void saveNBTData(CompoundTag compound) {
      compound.putBoolean("TurnedToStone", this.isStone);
      compound.putInt("StoneBreakLvl", this.breakLvl);
   }

   @Override
   public void loadNBTData(CompoundTag compound) {
      this.isStone = compound.getBoolean("TurnedToStone");
      this.breakLvl = compound.getInt("StoneBreakLvl");
   }

   @Override
   public void init() {
      this.isStone = false;
      this.breakLvl = 0;
   }

   @Override
   public Class<Mob> getEntityClass() {
      return Mob.class;
   }

   @Override
   public String getID() {
      return "Ice And Fire - Stone Property Tracker";
   }
}
