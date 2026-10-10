package jaredbgreat.dldungeons.builder;

import jaredbgreat.dldungeons.planner.Dungeon;

/** rlgen: dungeons are planned by DoomlikeStructure and built one populate area at a time, as Builder.buildDungeonChunk did. */
public class Builder {
   public static void buildDungeonChunk(int cx, int cz, Dungeon dungeon) {
      if (dungeon != null && dungeon.map != null) {
         dungeon.map.buildInChunk(dungeon, cx, cz);
      }
   }
}
