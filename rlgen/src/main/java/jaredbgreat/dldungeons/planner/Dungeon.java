package jaredbgreat.dldungeons.planner;

import jaredbgreat.dldungeons.ConfigHandler;
import jaredbgreat.dldungeons.Difficulty;
import jaredbgreat.dldungeons.DoomlikeDungeons;
import jaredbgreat.dldungeons.builder.RegisteredBlock;
import jaredbgreat.dldungeons.pieces.Spawner;
import jaredbgreat.dldungeons.pieces.chests.BasicChest;
import jaredbgreat.dldungeons.pieces.chests.LootCategory;
import jaredbgreat.dldungeons.pieces.chests.LootHandler;
import jaredbgreat.dldungeons.pieces.entrances.SimpleEntrance;
import jaredbgreat.dldungeons.pieces.entrances.SpiralStair;
import jaredbgreat.dldungeons.pieces.entrances.TopRoom;
import jaredbgreat.dldungeons.planner.astar.DoorChecker;
import jaredbgreat.dldungeons.planner.mapping.MapMatrix;
import jaredbgreat.dldungeons.rooms.Cave;
import jaredbgreat.dldungeons.rooms.Room;
import jaredbgreat.dldungeons.rooms.RoomList;
import jaredbgreat.dldungeons.themes.BiomeSets;
import jaredbgreat.dldungeons.themes.Degree;
import jaredbgreat.dldungeons.themes.Sizes;
import jaredbgreat.dldungeons.themes.Theme;
import jaredbgreat.dldungeons.util.cache.Coords;
import jaredbgreat.dldungeons.util.cache.IHaveCoords;
import jaredbgreat.dldungeons.util.math.SpatialHash;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import net.minecraft.core.BlockPos;
import jaredbgreat.dldungeons.compat.World;
import com.nukevscity.rlgen.legacy.biome.Biome;

public class Dungeon implements IHaveCoords {
   private final Coords coords;
   public Theme theme;
   public Random random;
   public Biome biome;
   public MapMatrix map;
   public Node[] nodes;
   public int numNodes;
   public int roomCount;
   public int entrancePref;
   public int baseHeight;
   public int numEntrances = 0;
   public RoomList rooms;
   public SpawnerCounter spawners;
   public ArrayList<Room> planter;
   public ArrayList<Room> grower;
   public Sizes size;
   public Degree outside;
   public Degree liquids;
   public Degree subrooms;
   public Degree islands;
   public Degree pillars;
   public Degree fences;
   public Degree symmetry;
   public Degree variability;
   public Degree degeneracy;
   public Degree complexity;
   public Degree verticle;
   public Degree entrances;
   public Degree bigRooms;
   public Degree naturals;
   public int airBlock;
   public int wallBlock1;
   public int floorBlock;
   public int cielingBlock;
   public int stairBlock;
   public int stairSlab;
   public int fenceBlock;
   public int cornerBlock;
   public int liquidBlock;
   public int caveBlock;
   public LootCategory lootCat;
   int shiftX;
   int shiftZ;

   public void preFinalize() {
      if (this.theme != null) {
         for (int i = 0; i < this.nodes.length; i++) {
            this.nodes[i] = null;
         }

         for (Room room : this.rooms) {
            room.preFinalize();
            Object var4 = null;
         }

         this.rooms.clear();
      }

      this.rooms = null;
      this.planter = this.grower = null;
      this.nodes = null;
      this.theme = null;
      this.random = null;
      this.biome = null;
      this.map = null;
      this.size = null;
      this.outside = null;
      this.liquids = null;
      this.subrooms = null;
      this.islands = null;
      this.pillars = null;
      this.symmetry = null;
      this.variability = null;
      this.degeneracy = null;
      this.complexity = null;
      this.verticle = null;
      this.entrances = null;
      this.bigRooms = null;
      this.naturals = null;
   }

   public Dungeon(Biome biome, World world, int chunkX, int chunkZ) throws Throwable {
      this.coords = new Coords(chunkX, chunkZ, 0);
      DoomlikeDungeons.profiler.startTask("Planning Dungeon");
      DoomlikeDungeons.profiler.startTask("Layout dungeon (rough draft)");
      this.random = SpatialHash.randomFromSeed(world.getSeed(), chunkX, chunkZ, 0);
      this.biome = biome;
      this.theme = BiomeSets.getTheme(biome, this.random, 0);
      if (this.theme != null) {
         this.applyTheme();
         this.entrancePref = this.random.nextInt(3);
         this.airBlock = this.theme.air[this.random.nextInt(this.theme.air.length)];
         this.wallBlock1 = this.theme.walls[this.random.nextInt(this.theme.walls.length)];
         this.floorBlock = this.theme.floors[this.random.nextInt(this.theme.floors.length)];
         this.cielingBlock = this.theme.ceilings[this.random.nextInt(this.theme.ceilings.length)];
         this.fenceBlock = this.theme.fencing[this.random.nextInt(this.theme.fencing.length)];
         this.cornerBlock = this.theme.pillarBlock[this.random.nextInt(this.theme.pillarBlock.length)];
         this.liquidBlock = this.theme.liquid[this.random.nextInt(this.theme.liquid.length)];
         this.caveBlock = this.theme.caveWalls[this.random.nextInt(this.theme.caveWalls.length)];
         this.rooms = new RoomList(this.size.maxRooms + 1);
         this.planter = new ArrayList<>();
         this.map = new MapMatrix(this.size, world, this.coords);
         this.numNodes = this.random.nextInt(this.size.maxNodes - this.size.minNodes + 1) + this.size.minNodes + 1;
         this.nodes = new Node[this.numNodes];
         this.spawners = new SpawnerCounter();
         this.shiftX = this.map.chunkX * 16 - this.map.room.length / 2 + 8;
         this.shiftZ = this.map.chunkZ * 16 - this.map.room.length / 2 + 8;
         this.makeNodes();
         if (this.numEntrances < 1 && (ConfigHandler.easyFind || ConfigHandler.singleEntrance)) {
            this.addAnEntrance();
         }

         this.connectNodes();
         this.growthCycle();
         if (ConfigHandler.thinSpawners && ConfigHandler.difficulty != Difficulty.NONE) {
            this.spawners.fixSpawners(this, this.random);

            for (Room room : this.rooms) {
               room.addChests(this);
            }
         }

         DoomlikeDungeons.profiler.endTask("Layout dungeon (rough draft)");
         DoomlikeDungeons.profiler.startTask("Fixing room contents");
         this.fixRoomContents();
         DoomlikeDungeons.profiler.endTask("Fixing room contents");
         DoomlikeDungeons.profiler.endTask("Planning Dungeon");
      }
   }

   public Dungeon(World world, Coords coords) throws Throwable {
      this.coords = coords;
      int chunkX = coords.getX();
      int chunkZ = coords.getZ();
      DoomlikeDungeons.profiler.startTask("Planning Dungeon");
      DoomlikeDungeons.profiler.startTask("Layout dungeon (rough draft)");
      this.random = SpatialHash.randomFromSeed(world.getSeed(), chunkX, chunkZ, 0);
      this.biome = world.getBiome(new BlockPos(chunkX * 16, 64, chunkZ * 16));
      this.theme = BiomeSets.getTheme(this.biome, this.random, 0);
      if (this.theme != null) {
         this.applyTheme();
         this.entrancePref = this.random.nextInt(3);
         this.airBlock = this.theme.air[this.random.nextInt(this.theme.air.length)];
         this.wallBlock1 = this.theme.walls[this.random.nextInt(this.theme.walls.length)];
         this.floorBlock = this.theme.floors[this.random.nextInt(this.theme.floors.length)];
         this.cielingBlock = this.theme.ceilings[this.random.nextInt(this.theme.ceilings.length)];
         this.fenceBlock = this.theme.fencing[this.random.nextInt(this.theme.fencing.length)];
         this.cornerBlock = this.theme.pillarBlock[this.random.nextInt(this.theme.pillarBlock.length)];
         this.liquidBlock = this.theme.liquid[this.random.nextInt(this.theme.liquid.length)];
         this.caveBlock = this.theme.caveWalls[this.random.nextInt(this.theme.caveWalls.length)];
         this.rooms = new RoomList(this.size.maxRooms + 1);
         this.planter = new ArrayList<>();
         this.map = new MapMatrix(this.size, world, coords);
         this.numNodes = this.random.nextInt(this.size.maxNodes - this.size.minNodes + 1) + this.size.minNodes + 1;
         this.nodes = new Node[this.numNodes];
         this.spawners = new SpawnerCounter();
         this.shiftX = this.map.chunkX * 16 - this.map.room.length / 2 + 8;
         this.shiftZ = this.map.chunkZ * 16 - this.map.room.length / 2 + 8;
         this.makeNodes();
         if (this.numEntrances < 1 && (ConfigHandler.easyFind || ConfigHandler.singleEntrance)) {
            this.addAnEntrance();
         }

         this.connectNodes();
         this.growthCycle();
         if (ConfigHandler.thinSpawners && ConfigHandler.difficulty != Difficulty.NONE) {
            this.spawners.fixSpawners(this, this.random);

            for (Room room : this.rooms) {
               room.addChests(this);
            }
         }

         DoomlikeDungeons.profiler.endTask("Layout dungeon (rough draft)");
         DoomlikeDungeons.profiler.startTask("Fixing room contents");
         this.fixRoomContents();
         DoomlikeDungeons.profiler.endTask("Fixing room contents");
         DoomlikeDungeons.profiler.endTask("Planning Dungeon");
      }
   }

   private void applyTheme() {
      if (ConfigHandler.announceCommands) {
         this.size = Sizes.HUGE;
      } else {
         this.size = this.theme.sizes.select(this.random);
      }

      this.outside = this.theme.outside.select(this.random);
      this.liquids = this.theme.liquids.select(this.random);
      this.subrooms = this.theme.subrooms.select(this.random);
      this.islands = this.theme.islands.select(this.random);
      this.pillars = this.theme.pillars.select(this.random);
      this.symmetry = this.theme.symmetry.select(this.random);
      this.variability = this.theme.variability.select(this.random);
      this.degeneracy = this.theme.degeneracy.select(this.random);
      this.complexity = this.theme.complexity.select(this.random);
      this.verticle = this.theme.verticle.select(this.random);
      this.entrances = this.theme.entrances.select(this.random);
      this.fences = this.theme.fences.select(this.random);
      this.naturals = this.theme.naturals.select(this.random);
      this.baseHeight = this.random.nextInt(this.theme.maxY - this.theme.minY) + this.theme.minY;
      this.lootCat = LootHandler.getLootHandler().getCategory(this.theme.lootCat);
      if (this.lootCat == null) {
         this.lootCat = LootHandler.getLootHandler().getCategory("chest.cfg");
      }
   }

   private void makeNodes() {
      int i = 0;

      while (i < this.numNodes) {
         this.nodes[i] = new Node(this.random.nextInt(this.size.width), this.baseHeight, this.random.nextInt(this.size.width), this.random, this);
         if (this.nodes[i].hubRoom != null) {
            i++;
         }
      }
   }

   private void connectNodes() {
      if (this.random.nextBoolean()) {
         this.connectNodesDensely();
      } else {
         this.connectNodesSparcely();
      }
   }

   private void connectNodesSparcely() {
      ArrayList<Node> connected = new ArrayList<>(this.nodes.length);
      ArrayList<Node> disconnected = new ArrayList<>(this.nodes.length);
      connected.add(this.nodes[0]);

      for (int i = 1; i < this.nodes.length; i++) {
         disconnected.add(this.nodes[i]);
      }

      while (!disconnected.isEmpty()) {
         if (this.rooms.realSize() >= this.size.maxRooms) {
            return;
         }

         Node first = connected.get(this.random.nextInt(connected.size()));
         Node other = disconnected.get(this.random.nextInt(disconnected.size()));
         new Route(first, other).drawConnections(this);
         connected.add(other);
         disconnected.remove(other);
      }
   }

   private void connectNodesDensely() {
      for (int i = 0; i < this.nodes.length; i++) {
         Node first = this.nodes[i];

         for (int j = i + 1; j < this.nodes.length; j++) {
            Node other = this.nodes[j];
            if (this.rooms.realSize() >= this.size.maxRooms) {
               return;
            }

            if (other != first) {
               new Route(first, other).drawConnections(this);
            }
         }
      }
   }

   private void growthCycle() {
      boolean doMore = true;

      do {
         doMore = false;
         this.grower = this.planter;
         Collections.shuffle(this.grower, this.random);
         this.planter = new ArrayList<>();

         for (Room room : this.grower) {
            if (this.rooms.realSize() >= this.size.maxRooms) {
               return;
            }

            if (room.plantChildren(this)) {
               doMore = true;
            }
         }
      } while (doMore);
   }

   private void fixRoomContents() {
      for (Room room : this.rooms) {
         this.addTEsToChunks(room);
         DoorChecker.processDoors1(this, room);
      }

      for (Room room : this.rooms) {
         DoorChecker.processDoors2(this, room);
      }

      for (Room room : this.rooms) {
         DoorChecker.processDoors3(this, room);
         if (room instanceof Cave) {
            DoorChecker.caveConnector(this, room);
         }
      }

      DoorChecker.checkConnectivity(this);
   }

   public void addChestBlocks(Room room) {
      if (true) {
         for (BasicChest chest : room.chests) {
            RegisteredBlock.placeChest(this.map.world, this.shiftX + chest.mx, chest.my, this.shiftZ + chest.mz);
         }
      }
   }

   public void addTEsToChunks(Room room) {
      for (BasicChest chest : room.chests) {
         this.map.addChest(chest);
      }

      for (Spawner spawner : room.spawners) {
         this.map.addSpawner(spawner);
      }

      if (room.entrance != null) {
         this.map.addEntrance(room.entrance);
      }
   }

   public void addTileEntities() {
      for (Room room : this.rooms) {
         this.addTileEntitesToRoom(room);
      }
   }

   private void addTileEntitesToRoom(Room room) {
      if (true) {
         for (Spawner spawner : room.spawners) {
            RegisteredBlock.placeSpawner(this.map.world, this.shiftX + spawner.getX(), spawner.getY(), this.shiftZ + spawner.getZ(), spawner.getMob());
         }

         for (BasicChest chest : room.chests) {
            chest.place(this.map.world, this.shiftX + chest.mx, chest.my, this.shiftZ + chest.mz, this.random);
         }
      }
   }

   public void addEntrances() {
      for (int i = 0; i < this.nodes.length; i++) {
         if (this.nodes[i].hubRoom != null) {
            this.addEntrance(this.nodes[i].hubRoom);
         }
      }
   }

   private void addEntrance(Room room) {
      if (room.hasEntrance) {
         int entrance;
         if (this.variability.use(this.random)) {
            entrance = this.random.nextInt(3);
         } else {
            entrance = this.entrancePref;
         }

         if (ConfigHandler.easyFind) {
            entrance = 1;
         }

         if (true) {
            switch (entrance) {
               case 0:
                  new SpiralStair((int)room.realX, (int)room.realZ).build(this, this.map.world);
                  break;
               case 1:
                  new TopRoom((int)room.realX, (int)room.realZ).build(this, this.map.world);
                  break;
               case 2:
               default:
                  new SimpleEntrance((int)room.realX, (int)room.realZ).build(this, this.map.world);
            }
         }
      }
   }

   private void addAnEntrance() {
      if (!this.theme.entrances.never()) {
         int which = this.random.nextInt(this.nodes.length);
         Room it = this.nodes[which].hubRoom;
         it.chests.clear();
         it.spawners.clear();
         it.hasEntrance = true;
         it.hasSpawners = false;
         this.numEntrances = 1;

         for (int i = (int)it.realX - 2; i < (int)it.realX + 2; i++) {
            for (int j = (int)it.realZ - 2; j < (int)it.realZ + 2; j++) {
               this.map.floorY[i][j] = (byte)it.floorY;
               this.map.hasLiquid[i][j] = false;
               this.map.isWall[i][j] = false;
            }
         }

         it.addEntrance(this);
      }
   }

   @Override
   public Coords getCoords() {
      return this.coords;
   }
}
