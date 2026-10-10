package jaredbgreat.dldungeons.planner;

import jaredbgreat.dldungeons.ConfigHandler;
import jaredbgreat.dldungeons.pieces.Spawner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class SpawnerCounter {
   final ArrayList<Spawner> list = new ArrayList<>();
   int dungeonSize = 0;

   public void addRoom(int size) {
      this.dungeonSize += size;
   }

   public void addSpawner(Spawner s) {
      this.list.add(s);
   }

   public void fixSpawners(Dungeon dungeon, Random random) {
      int targetNum = this.dungeonSize / ConfigHandler.difficulty.blocksPerSpawner;
      targetNum = (int)((double)targetNum + (double)targetNum * Math.max(5.0, Math.min(0.0, random.nextGaussian() + 2.0)) / 10.0);
      int existing = this.list.size();
      if (existing > targetNum) {
         Collections.shuffle(this.list, random);

         for (int i = targetNum; i < existing; i++) {
            Spawner s = this.list.get(i);
            dungeon.rooms.get(s.getRoom()).spawners.remove(s);
         }
      }
   }
}
