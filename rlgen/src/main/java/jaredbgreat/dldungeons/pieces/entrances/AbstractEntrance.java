package jaredbgreat.dldungeons.pieces.entrances;

import jaredbgreat.dldungeons.planner.Dungeon;
import jaredbgreat.dldungeons.compat.World;

public abstract class AbstractEntrance {
   protected static final String LADDER = "minecraft:ladder";
   protected static final String STAIR_SLAB = "minecraft:stone_slab";
   public final int x;
   public final int z;

   public AbstractEntrance(int x, int z) {
      this.x = x;
      this.z = z;
   }

   public abstract void build(Dungeon var1, World var2);
}
