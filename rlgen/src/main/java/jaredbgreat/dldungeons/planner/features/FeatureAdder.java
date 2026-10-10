package jaredbgreat.dldungeons.planner.features;

import jaredbgreat.dldungeons.planner.Dungeon;
import jaredbgreat.dldungeons.rooms.Room;
import jaredbgreat.dldungeons.themes.Degree;

public abstract class FeatureAdder {
   Degree chance;

   public FeatureAdder(Degree chance) {
      this.chance = chance;
   }

   public boolean addFeature(Dungeon dungeon, Room room) {
      boolean built = this.chance.use(dungeon.random);
      if (built) {
         this.buildFeature(dungeon, room);
      }

      return built;
   }

   public abstract void buildFeature(Dungeon var1, Room var2);
}
