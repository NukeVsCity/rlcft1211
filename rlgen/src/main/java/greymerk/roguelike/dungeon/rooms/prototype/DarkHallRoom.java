package greymerk.roguelike.dungeon.rooms.prototype;

import com.github.fnar.minecraft.block.SingleBlockBrush;
import greymerk.roguelike.dungeon.base.BaseRoom;
import greymerk.roguelike.dungeon.rooms.RoomSetting;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.RectHollow;
import greymerk.roguelike.worldgen.shapes.RectSolid;
import java.util.List;

public class DarkHallRoom extends BaseRoom {
   public DarkHallRoom(RoomSetting roomSetting, LevelSettings levelSettings, WorldEditor worldEditor) {
      super(roomSetting, levelSettings, worldEditor);
      this.wallDist = 7;
   }

   @Override
   public BaseRoom generate(Coord at, List<Direction> entrances) {
      RectHollow r1 = RectHollow.newRect(at.copy().north(7).west(7).down(), at.copy().south(7).east(7).up(7));
      this.primaryWallBrush().fill(this.worldEditor, r1, false, true);
      RectHollow r2 = RectHollow.newRect(at.copy().north(4).west(4).up(6), at.copy().south(4).east(4).up(9));
      this.primaryWallBrush().fill(this.worldEditor, r2, false, true);
      RectSolid r3 = RectSolid.newRect(at.copy().north(6).west(6).down(), at.copy().south(6).east(6).down());
      this.primaryFloorBrush().fill(this.worldEditor, r3, false, true);

      for (Direction entrance : entrances) {
         RectSolid r4 = RectSolid.newRect(at.copy().translate(entrance.left()), at.copy().translate(entrance.right()).translate(entrance, 7));
         this.secondaryFloorBrush().fill(this.worldEditor, r4, false, true);
      }

      for (Direction side : Direction.CARDINAL) {
         this.generateAccentPillar(at.copy().translate(side, 6).translate(side.left(), 6), 5);
         this.generateBeam(side, at.copy().translate(side, 6).up(6), 13);
         this.generateBeam(side, at.copy().translate(side, 3).up(6), 7);
         this.generateBeam(side, at.copy().translate(side, 3).up(8), 7);
         this.generateBeam(side, at.copy().up(8), 7);
         this.secondaryPillarBrush().stroke(this.worldEditor, at.copy().translate(side, 3).up(7));
         if (!entrances.contains(side)) {
            this.pillar(side.reverse(), at.copy().translate(side, 6));
         } else {
            this.generateEntranceArchway(at.copy().translate(side, 7), side);
         }

         this.secondaryWallBrush().fill(this.worldEditor, RectSolid.newRect(at.copy().translate(side, 6).up(6), at.copy().translate(side, 4).up(6)));

         for (Direction orthogonal : side.orthogonals()) {
            Coord cursor = at.copy().translate(side, 6).translate(orthogonal, 3);
            this.pillar(side.reverse(), cursor);
            this.secondaryWallBrush().fill(this.worldEditor, RectSolid.newRect(cursor.copy().up(6), cursor.copy().up(6).translate(side.reverse(), 6)));
         }
      }

      this.generateDoorways(at, entrances);
      return this;
   }

   private void generateEntranceArchway(Coord origin, Direction facing) {
      Coord aboveOrigin = origin.copy().up(2);
      this.secondaryWallBrush()
         .fill(this.worldEditor, RectSolid.newRect(aboveOrigin.copy().translate(facing.left(), 2), aboveOrigin.copy().translate(facing.right(), 2).up(3)));
      SingleBlockBrush.AIR.stroke(this.worldEditor, aboveOrigin);

      for (Direction orthogonal : facing.orthogonals()) {
         this.secondaryStairBrush().setUpsideDown(true).setFacing(orthogonal.reverse()).stroke(this.worldEditor, aboveOrigin.copy().translate(orthogonal));
         this.pillar(orthogonal.reverse(), origin.copy().translate(facing.back()).translate(orthogonal, 3));
         Coord cursor = origin.copy().translate(orthogonal, 2);
         this.secondaryPillarBrush().stroke(this.worldEditor, cursor);
         this.secondaryPillarBrush().stroke(this.worldEditor, cursor.up());
      }
   }

   private void generateBeam(Direction dir, Coord origin, int width) {
      BlockBrush wall = this.secondaryWallBrush();
      int left = width / 2;
      int right = width - left - 1;
      Coord beamLeftAnchor = origin.copy().translate(dir.left(), left);
      Coord beamRightAnchor = origin.copy().translate(dir.right(), right);
      RectSolid beam = RectSolid.newRect(beamLeftAnchor, beamRightAnchor);
      wall.fill(this.worldEditor, beam);
   }

   public void generateAccentPillar(Coord origin, int height) {
      this.primaryPillarBrush().fill(this.worldEditor, RectSolid.newRect(origin.copy(), origin.copy().up(height)));
   }

   private void pillar(Direction dir, Coord origin) {
      this.generateAccentPillar(origin, 5);
      Coord cursor = origin.copy().up(3).translate(dir);
      this.secondaryStairBrush().setUpsideDown(true).setFacing(dir).stroke(this.worldEditor, cursor);
      this.secondaryStairBrush().setUpsideDown(false).setFacing(dir.reverse()).stroke(this.worldEditor, cursor.up());
      this.secondaryStairBrush().setUpsideDown(true).setFacing(dir).stroke(this.worldEditor, cursor.translate(dir));
      this.secondaryStairBrush().setUpsideDown(false).setFacing(dir.reverse()).stroke(this.worldEditor, cursor.up());
      cursor.translate(dir);
      if (this.worldEditor.isAirBlock(cursor)) {
         this.secondaryStairBrush().setUpsideDown(true).setFacing(dir).stroke(this.worldEditor, cursor);
      } else {
         this.secondaryWallBrush().stroke(this.worldEditor, cursor);
      }
   }
}
