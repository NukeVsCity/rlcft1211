package com.github.alexthe666.iceandfire.entity;

import net.ilexiconn.llibrary.server.entity.EntityProperties;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;

/** 1.12 ChickenEntityProperties (EntityAnimal), registered with LLibrary's EntityPropertiesHandler like 1.12. */
public class ChickenEntityProperties extends EntityProperties<Animal> {
   public int timeUntilNextEgg = 1;
   private static Random rand = new Random();

   @Nullable
   public static ChickenEntityProperties get(Entity entity) {
      return EntityPropertiesHandler.INSTANCE.getProperties(entity, ChickenEntityProperties.class);
   }

   @Override
   public int getTrackingTime() {
      return 20;
   }

   @Override
   public void saveNBTData(CompoundTag compound) {
      compound.putInt("TimeUntilNextEgg", this.timeUntilNextEgg);
   }

   @Override
   public void loadNBTData(CompoundTag compound) {
      this.timeUntilNextEgg = compound.getInt("TimeUntilNextEgg");
   }

   @Override
   public void init() {
      this.timeUntilNextEgg = this.generateTime();
   }

   @Override
   public Class<Animal> getEntityClass() {
      return Animal.class;
   }

   @Override
   public String getID() {
      return "Ice And Fire - Chicken Property Tracker";
   }

   public int generateTime() {
      return rand.nextInt(6000) + 6000;
   }
}
