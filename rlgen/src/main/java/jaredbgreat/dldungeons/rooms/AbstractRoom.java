package jaredbgreat.dldungeons.rooms;

import jaredbgreat.dldungeons.planner.Dungeon;

public abstract class AbstractRoom {
   public int airBlock;
   public int wallBlock1;
   public int floorBlock;
   public int cielingBlock;
   public int fenceBlock;
   public int pillarBlock;
   public int liquidBlock;
   public int caveBlock;
   public boolean degenerate;
   public boolean degenerateFloors;
   public boolean sky;
   public boolean fenced;

   protected AbstractRoom() {
      this.wallBlock1 = 0;
      this.floorBlock = 0;
      this.cielingBlock = 0;
      this.fenceBlock = 0;
      this.pillarBlock = 0;
      this.liquidBlock = 0;
   }

   protected AbstractRoom(Dungeon dungeon, AbstractRoom previous) {
      if (previous != null && !dungeon.variability.use(dungeon.random)) {
         this.airBlock = previous.airBlock;
         this.wallBlock1 = previous.wallBlock1;
         this.floorBlock = previous.floorBlock;
         this.cielingBlock = previous.cielingBlock;
         this.fenceBlock = previous.fenceBlock;
         this.pillarBlock = previous.pillarBlock;
         this.liquidBlock = previous.liquidBlock;
         this.caveBlock = previous.caveBlock;
      } else if (dungeon.variability.use(dungeon.random)) {
         if (dungeon.variability.use(dungeon.random)) {
            this.airBlock = dungeon.theme.air[dungeon.random.nextInt(dungeon.theme.air.length)];
            this.wallBlock1 = dungeon.theme.walls[dungeon.random.nextInt(dungeon.theme.walls.length)];
            this.floorBlock = dungeon.theme.floors[dungeon.random.nextInt(dungeon.theme.floors.length)];
            this.cielingBlock = dungeon.theme.ceilings[dungeon.random.nextInt(dungeon.theme.ceilings.length)];
            this.fenceBlock = dungeon.theme.fencing[dungeon.random.nextInt(dungeon.theme.fencing.length)];
            this.pillarBlock = dungeon.theme.pillarBlock[dungeon.random.nextInt(dungeon.theme.pillarBlock.length)];
            this.liquidBlock = dungeon.theme.liquid[dungeon.random.nextInt(dungeon.theme.liquid.length)];
            this.caveBlock = dungeon.theme.caveWalls[dungeon.random.nextInt(dungeon.theme.caveWalls.length)];
         } else {
            if (!dungeon.variability.use(dungeon.random)) {
               this.airBlock = dungeon.airBlock;
            } else {
               this.airBlock = dungeon.theme.air[dungeon.random.nextInt(dungeon.theme.air.length)];
            }

            if (!dungeon.variability.use(dungeon.random)) {
               this.wallBlock1 = dungeon.wallBlock1;
            } else {
               this.wallBlock1 = dungeon.theme.walls[dungeon.random.nextInt(dungeon.theme.walls.length)];
            }

            if (!dungeon.variability.use(dungeon.random)) {
               this.floorBlock = dungeon.floorBlock;
            } else {
               this.floorBlock = dungeon.theme.floors[dungeon.random.nextInt(dungeon.theme.floors.length)];
            }

            if (!dungeon.variability.use(dungeon.random)) {
               this.cielingBlock = dungeon.cielingBlock;
            } else {
               this.cielingBlock = dungeon.theme.ceilings[dungeon.random.nextInt(dungeon.theme.ceilings.length)];
            }

            if (!dungeon.variability.use(dungeon.random)) {
               this.fenceBlock = dungeon.fenceBlock;
            } else {
               this.fenceBlock = dungeon.theme.fencing[dungeon.random.nextInt(dungeon.theme.fencing.length)];
            }

            if (!dungeon.variability.use(dungeon.random)) {
               this.pillarBlock = dungeon.cornerBlock;
            } else {
               this.pillarBlock = dungeon.theme.pillarBlock[dungeon.random.nextInt(dungeon.theme.pillarBlock.length)];
            }

            if (!dungeon.variability.use(dungeon.random)) {
               this.liquidBlock = dungeon.liquidBlock;
            } else {
               this.liquidBlock = dungeon.theme.liquid[dungeon.random.nextInt(dungeon.theme.liquid.length)];
            }

            if (!dungeon.variability.use(dungeon.random)) {
               this.caveBlock = dungeon.caveBlock;
            } else {
               this.caveBlock = dungeon.theme.caveWalls[dungeon.random.nextInt(dungeon.theme.caveWalls.length)];
            }
         }
      } else {
         this.airBlock = dungeon.airBlock;
         this.wallBlock1 = dungeon.wallBlock1;
         this.floorBlock = dungeon.floorBlock;
         this.cielingBlock = dungeon.cielingBlock;
         this.fenceBlock = dungeon.fenceBlock;
         this.pillarBlock = dungeon.cornerBlock;
         this.liquidBlock = dungeon.liquidBlock;
         this.caveBlock = dungeon.caveBlock;
      }

      if (dungeon.outside.value > 0) {
         this.sky = dungeon.outside.use(dungeon.random);
      }

      if (dungeon.degeneracy.value > 0) {
         this.degenerate = dungeon.degeneracy.use(dungeon.random);
         this.degenerateFloors = this.degenerate && dungeon.degeneracy.use(dungeon.random) && dungeon.random.nextBoolean();
      }

      if (this.sky) {
         this.fenced = dungeon.fences.use(dungeon.random);
      } else {
         this.fenced = false;
      }
   }

   public static Room makeRoom(int beginX, int endX, int beginZ, int endZ, int floorY, int ceilY, Dungeon dungeon, Room parent, Room previous) {
      RoomType type = RoomType.ROOM;
      if (dungeon.naturals.use(dungeon.random)) {
         type = RoomType.CAVE;
      }

      switch (type) {
         case CAVE: {
            Cave base = new Cave(beginX, endX, beginZ, endZ, floorY, ceilY, dungeon, parent, previous);
            return base.plan(dungeon, parent);
         }
         case ROOM:
         default: {
            Room base = new Room(beginX, endX, beginZ, endZ, floorY, ceilY, dungeon, parent, previous);
            return base.plan(dungeon, parent);
         }
      }
   }

   public static Room makeRoom(int beginX, int endX, int beginZ, int endZ, int floorY, int ceilY, Dungeon dungeon, Room parent, Room previous, RoomType type) {
      switch (type) {
         case CAVE: {
            Cave base = new Cave(beginX, endX, beginZ, endZ, floorY, ceilY, dungeon, parent, previous);
            return base.plan(dungeon, parent);
         }
         case ROOM:
         default: {
            Room base = new Room(beginX, endX, beginZ, endZ, floorY, ceilY, dungeon, parent, previous);
            return base.plan(dungeon, parent);
         }
      }
   }
}
