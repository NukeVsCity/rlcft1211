package jaredbgreat.dldungeons.planner.mapping;

import jaredbgreat.dldungeons.DoomlikeDungeons;
import jaredbgreat.dldungeons.builder.BlockFamily;
import jaredbgreat.dldungeons.builder.RegisteredBlock;
import jaredbgreat.dldungeons.pieces.Spawner;
import jaredbgreat.dldungeons.pieces.chests.BasicChest;
import jaredbgreat.dldungeons.pieces.entrances.AbstractEntrance;
import jaredbgreat.dldungeons.planner.Dungeon;
import jaredbgreat.dldungeons.planner.astar.Step;
import jaredbgreat.dldungeons.rooms.Room;
import jaredbgreat.dldungeons.themes.Sizes;
import jaredbgreat.dldungeons.themes.ThemeFlags;
import jaredbgreat.dldungeons.util.cache.Coords;
import jaredbgreat.dldungeons.util.cache.IHaveCoords;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import jaredbgreat.dldungeons.compat.World;

public class MapMatrix implements IHaveCoords {
   private static final Block lapis = Blocks.LAPIS_BLOCK;
   private static final Block slab = Blocks.SMOOTH_STONE;
   private static final Block gold = Blocks.GOLD_BLOCK;
   private static final Block glass = Blocks.GLASS;
   private static boolean drawFlyingMap = false;
   public final World world;
   public final Coords coords;
   public final int chunkX;
   public final int chunkZ;
   public final int origenX;
   public final int origenZ;
   public final int lowCX;
   public final int lowCZ;
   public final int shiftX;
   public final int shiftZ;
   public byte[][] ceilY;
   public byte[][] floorY;
   public byte[][] nCeilY;
   public byte[][] nFloorY;
   public int[][] ceiling;
   public int[][] wall;
   public int[][] floor;
   public int[][] room;
   public boolean[][] isWall;
   public boolean[][] isFence;
   public boolean[][] hasLiquid;
   public boolean[][] isDoor;
   public Step[][] nodedge;
   public boolean[][] astared;
   public final ChunkFeatures[][] features;

   public MapMatrix(Sizes size, World world, Coords coords) {
      this.world = world;
      this.coords = coords;
      this.chunkX = coords.getX();
      this.chunkZ = coords.getZ();
      this.lowCX = this.chunkX - size.chunkRadius;
      this.lowCZ = this.chunkZ - size.chunkRadius;
      this.origenX = this.chunkX * 16 - size.width / 2 + 8;
      this.origenZ = this.chunkZ * 16 - size.width / 2 + 8;
      this.ceilY = new byte[size.width][size.width];
      this.floorY = new byte[size.width][size.width];
      this.nCeilY = new byte[size.width][size.width];
      this.nFloorY = new byte[size.width][size.width];
      this.room = new int[size.width][size.width];
      this.ceiling = new int[size.width][size.width];
      this.wall = new int[size.width][size.width];
      this.floor = new int[size.width][size.width];
      this.isWall = new boolean[size.width][size.width];
      this.isFence = new boolean[size.width][size.width];
      this.hasLiquid = new boolean[size.width][size.width];
      this.isDoor = new boolean[size.width][size.width];
      this.nodedge = new Step[size.width][size.width];
      this.astared = new boolean[size.width][size.width];
      this.features = new ChunkFeatures[size.chunkWidth][size.chunkWidth];

      for (int i = 0; i < size.chunkWidth; i++) {
         for (int j = 0; j < size.chunkWidth; j++) {
            this.features[i][j] = new ChunkFeatures();
         }
      }

      this.shiftX = this.chunkX * 16 - this.room.length / 2 + 16;
      this.shiftZ = this.chunkZ * 16 - this.room.length / 2 + 16;
   }

   public void addSpawner(Spawner spawner) {
      this.features[spawner.getX() / 16][spawner.getZ() / 16].addSpawner(spawner);
   }

   public void addChest(BasicChest chest) {
      this.features[chest.mx / 16][chest.mz / 16].addChest(chest);
   }

   public void addEntrance(AbstractEntrance entrance) {
      this.features[entrance.x / 16][entrance.z / 16].addEntrance(entrance);
   }

   public void buildByChunks(Dungeon dungeon) {
      int i = this.lowCX;

      for (int i0 = 0; i < this.lowCX + dungeon.size.chunkWidth; i0++) {
         int j = this.lowCZ;

         for (int j0 = 0; j < this.lowCZ + dungeon.size.chunkWidth; j0++) {
            this.buildInChunk(dungeon, i, j);
            j++;
         }

         i++;
      }
   }

   public void buildInChunk(Dungeon dungeon, int cx0, int cz0) {
      int cx1 = cx0 - this.lowCX;
      int cz1 = cz0 - this.lowCZ;
      if (cx1 >= 0 && cx1 < dungeon.size.chunkWidth && cz1 >= 0 && cz1 < dungeon.size.chunkWidth) {
         DoomlikeDungeons.profiler.startTask("Building Dungeon in Chunk");
         DoomlikeDungeons.profiler.startTask("Building Dungeon architecture");
         BlockFamily.setRadnom(dungeon.random);
         boolean flooded = dungeon.theme.flags.contains(ThemeFlags.WATER);
         int sx = (cx0 - this.lowCX) * 16;
         int ex = sx + 16;
         int sz = (cz0 - this.lowCZ) * 16;
         int ez = sz + 16;

         for (int i = sx; i < ex; i++) {
            for (int j = sz; j < ez; j++) {
               if (this.room[i][j] != 0) {
                  Room theRoom = dungeon.rooms.get(this.room[i][j]);
                  if (drawFlyingMap) {
                     if (this.astared[i][j]) {
                        RegisteredBlock.placeBlock(this.world, this.shiftX + i, 96, this.shiftZ + j, lapis);
                     } else if (this.isDoor[i][j]) {
                        RegisteredBlock.placeBlock(this.world, this.shiftX + i, 96, this.shiftZ + j, slab);
                     } else if (this.isWall[i][j]) {
                        RegisteredBlock.placeBlock(this.world, this.shiftX + i, 96, this.shiftZ + j, gold);
                     } else {
                        RegisteredBlock.placeBlock(this.world, this.shiftX + i, 96, this.shiftZ + j, glass);
                     }
                  }

                  if (this.nFloorY[i][j] < 1) {
                     this.nFloorY[i][j] = (byte)dungeon.baseHeight;
                  }

                  if (this.floorY[i][j] < 1) {
                     this.floorY[i][j] = (byte)dungeon.baseHeight;
                  }

                  if (this.nFloorY[i][j] < this.floorY[i][j] && this.nFloorY[i][j] > 0) {
                     for (int k = this.nFloorY[i][j]; k < this.floorY[i][j]; k++) {
                        if (this.noLowDegenerate(theRoom, this.shiftX + i, k, this.shiftZ + j, i, j)) {
                           RegisteredBlock.place(this.world, this.shiftX + i, k, this.shiftZ + j, this.wall[i][j]);
                        }
                     }
                  }

                  if (this.nFloorY[i][j] > this.floorY[i][j] && this.floorY[i][j] > 0) {
                     for (int kx = this.floorY[i][j]; kx < this.nFloorY[i][j]; kx++) {
                        if (this.noLowDegenerate(theRoom, this.shiftX + i, kx, this.shiftZ + j, i, j)) {
                           RegisteredBlock.place(this.world, this.shiftX + i, kx, this.shiftZ + j, this.wall[i][j]);
                        }
                     }
                  }

                  if (this.noLowDegenerate(theRoom, this.shiftX + i, this.floorY[i][j] - 1, this.shiftZ + j, i, j)) {
                     RegisteredBlock.place(this.world, this.shiftX + i, this.floorY[i][j] - 1, this.shiftZ + j, this.floor[i][j]);
                     if (dungeon.theme.buildFoundation) {
                        int below = this.nFloorY[i][j] < this.floorY[i][j] ? this.nFloorY[i][j] - 1 : this.floorY[i][j] - 2;

                        while (!RegisteredBlock.isGroundBlock(this.world, this.shiftX + i, below, this.shiftZ + j)) {
                           RegisteredBlock.place(this.world, this.shiftX + i, below, this.shiftZ + j, dungeon.floorBlock);
                           if (--below < 1) {
                              break;
                           }
                        }
                     }
                  }

                  if (!theRoom.sky && this.noHighDegenerate(theRoom, this.shiftX + i, this.ceilY[i][j] + 1, this.shiftZ + j)) {
                     RegisteredBlock.place(this.world, this.shiftX + i, this.ceilY[i][j] + 1, this.shiftZ + j, this.ceiling[i][j]);
                  }

                  for (int kxx = this.roomBottom(i, j); kxx <= this.ceilY[i][j]; kxx++) {
                     if (!this.isWall[i][j]) {
                        RegisteredBlock.deleteBlock(this.world, this.shiftX + i, kxx, this.shiftZ + j, theRoom.airBlock);
                     } else if (this.noHighDegenerate(theRoom, this.shiftX + i, kxx, this.shiftZ + j)) {
                        RegisteredBlock.place(this.world, this.shiftX + i, kxx, this.shiftZ + j, this.wall[i][j]);
                     }
                  }

                  for (int kxxx = this.nCeilY[i][j]; kxxx < this.ceilY[i][j]; kxxx++) {
                     if (this.noHighDegenerate(theRoom, this.shiftX + i, kxxx, this.shiftZ + j)) {
                        RegisteredBlock.place(this.world, this.shiftX + i, kxxx, this.shiftZ + j, this.wall[i][j]);
                     }
                  }

                  if (this.isFence[i][j]) {
                     RegisteredBlock.place(this.world, this.shiftX + i, this.floorY[i][j], this.shiftZ + j, theRoom.fenceBlock);
                  }

                  if (this.isDoor[i][j]) {
                     RegisteredBlock.deleteBlock(this.world, this.shiftX + i, this.floorY[i][j], this.shiftZ + j, flooded);
                     RegisteredBlock.deleteBlock(this.world, this.shiftX + i, this.floorY[i][j] + 1, this.shiftZ + j, flooded);
                     RegisteredBlock.deleteBlock(this.world, this.shiftX + i, this.floorY[i][j] + 2, this.shiftZ + j, flooded);
                  }

                  if (this.hasLiquid[i][j]
                     && !this.isWall[i][j]
                     && !this.isDoor[i][j]
                     && !this.world.isAirBlock(new BlockPos(this.shiftX + i, this.floorY[i][j] - 1, this.shiftZ + j))) {
                     RegisteredBlock.place(this.world, this.shiftX + i, this.floorY[i][j], this.shiftZ + j, theRoom.liquidBlock);
                  }
               }
            }
         }

         DoomlikeDungeons.profiler.endTask("Building Dungeon architecture");
         this.features[cx1][cz1].buildFeatures(dungeon, this, this.shiftX, this.shiftZ, this.world);
         DoomlikeDungeons.profiler.endTask("Building Dungeon in Chunk");
      }
   }

   private boolean noHighDegenerate(Room theRoom, int x, int y, int z) {
      return !theRoom.degenerate || !this.world.isAirBlock(new BlockPos(x, y, z));
   }

   private boolean noLowDegenerate(Room theRoom, int x, int y, int z, int i, int j) {
      return !theRoom.degenerateFloors || !this.world.isAirBlock(new BlockPos(x, y, z)) || this.astared[i][j];
   }

   private int roomBottom(int i, int j) {
      int b = this.floorY[i][j];
      if (this.isWall[i][j] && !this.isDoor[i][j]) {
         b--;
      }

      return b;
   }

   public static void setDrawFlyingMap(boolean value) {
      drawFlyingMap = value;
   }

   @Override
   public Coords getCoords() {
      return this.coords;
   }
}
