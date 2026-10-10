package jaredbgreat.dldungeons.planner.mapping;

import jaredbgreat.dldungeons.builder.RegisteredBlock;
import jaredbgreat.dldungeons.pieces.Spawner;
import jaredbgreat.dldungeons.pieces.chests.BasicChest;
import jaredbgreat.dldungeons.pieces.entrances.AbstractEntrance;
import jaredbgreat.dldungeons.planner.Dungeon;
import java.util.ArrayList;
import java.util.List;
import jaredbgreat.dldungeons.compat.World;

public class ChunkFeatures {
   volatile AbstractEntrance entrance;
   final List<Spawner> spawners = new ArrayList<>();
   final List<BasicChest> chests = new ArrayList<>();

   public void addSpawner(Spawner spawner) {
      this.spawners.add(spawner);
   }

   public void addChest(BasicChest chest) {
      this.chests.add(chest);
   }

   public void addEntrance(AbstractEntrance entrance) {
      this.entrance = entrance;
   }

   public void buildTileEntites(Dungeon dungeon, MapMatrix map, int shiftX, int shiftZ) {
      for (BasicChest chest : this.chests) {
         RegisteredBlock.placeChest(map.world, shiftX + chest.mx, chest.my, shiftZ + chest.mz);
         chest.place(map.world, shiftX + chest.mx, chest.my, shiftZ + chest.mz, dungeon.random);
      }

      for (Spawner spawner : this.spawners) {
         RegisteredBlock.placeSpawner(map.world, shiftX + spawner.getX(), spawner.getY(), shiftZ + spawner.getZ(), spawner.getMob());
      }
   }

   private void buildEntrance(Dungeon dungeon, World world) {
      if (this.entrance != null) {
         this.entrance.build(dungeon, world);
      }
   }

   public void buildFeatures(Dungeon dungeon, MapMatrix map, int shiftX, int shiftZ, World world) {
      this.buildTileEntites(dungeon, map, shiftX, shiftZ);
      this.buildEntrance(dungeon, world);
   }
}
