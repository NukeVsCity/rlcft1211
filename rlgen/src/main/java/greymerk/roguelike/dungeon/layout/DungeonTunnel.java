package greymerk.roguelike.dungeon.layout;

import com.github.fnar.minecraft.block.SingleBlockBrush;
import greymerk.roguelike.dungeon.DungeonLevel;
import greymerk.roguelike.dungeon.segment.SegmentGenerator;
import greymerk.roguelike.dungeon.segment.part.SegmentBase;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.theme.Theme;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.BlockJumble;
import greymerk.roguelike.worldgen.Bounded;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.IShape;
import greymerk.roguelike.worldgen.shapes.RectHollow;
import greymerk.roguelike.worldgen.shapes.RectSolid;
import greymerk.roguelike.worldgen.shapes.Shape;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class DungeonTunnel implements Iterable<Coord>, Bounded {
   private final Coord start;
   private final Coord end;
   private final List<SegmentBase> segments;
   private final List<Coord> tunnel;

   public DungeonTunnel(Coord start, Coord end) {
      this.start = start;
      this.end = end;
      this.tunnel = RectSolid.newRect(start, end).get();
      this.segments = new ArrayList<>();
   }

   @Override
   public Iterator<Coord> iterator() {
      return this.tunnel.iterator();
   }

   public void encase(WorldEditor editor, Theme theme) {
      Direction dir = this.getDirection();
      Coord s = this.start.copy();
      Coord e = this.end.copy();
      s.translate(dir.antiClockwise(), 3);
      s.up(3);
      e.translate(dir.clockwise(), 3);
      e.down(3);
      RectSolid.newRect(s, e).fill(editor, theme.getPrimary().getWall());
   }

   public void construct(WorldEditor editor, LevelSettings settings) {
      BlockBrush wallBlocks = settings.getTheme().getPrimary().getWall();
      BlockBrush floor = settings.getTheme().getPrimary().getFloor();
      BlockJumble bridgeBlocks = new BlockJumble();
      bridgeBlocks.addBlock(floor);
      bridgeBlocks.addBlock(SingleBlockBrush.AIR);
      Coord s = this.start.copy();
      s.north();
      s.east();
      Coord e = this.end.copy();
      e.south();
      e.west();
      e.up(2);
      RectSolid.newRect(s, e).fill(editor, SingleBlockBrush.AIR);
      s.north();
      s.east();
      s.down();
      e.south();
      e.west();
      e.up();
      RectHollow.newRect(s, e).fill(editor, wallBlocks, false, true);
      s = this.start.copy();
      s.north();
      s.east();
      s.down();
      e = this.end.copy();
      e.south();
      e.west();
      e.down();
      RectSolid.newRect(s, e).fill(editor, floor, false, true);
      RectSolid.newRect(s, e).fill(editor, bridgeBlocks, true, false);
      Direction dir = this.getDirection();
      Coord location = this.end.copy();
      location.translate(dir, 1);
      Coord start = location.copy();
      Direction[] orth = dir.orthogonals();
      start.translate(orth[0], 2);
      start.up(2);
      Coord end = location.copy();
      end.translate(orth[1], 2);
      end.down(2);
      RectSolid.newRect(start, end).fill(editor, wallBlocks, false, true);
   }

   public Coord[] getEnds() {
      return new Coord[]{this.start.copy(), this.end.copy()};
   }

   public Direction getDirection() {
      return this.start.dirTo(this.end);
   }

   public void genSegments(WorldEditor editor, DungeonLevel level) {
      LevelSettings settings = level.getSettings();
      SegmentGenerator segGen = settings.getSegments();

      for (Coord c : this) {
         this.segments.addAll(segGen.genSegment(editor, level, this.getDirection(), c));
      }
   }

   public List<SegmentBase> getSegments() {
      return this.segments;
   }

   private Bounded getBoundingBox() {
      Direction dir = this.getDirection();
      Coord start = this.start.copy().translate(dir.left(), 2).down();
      Coord end = this.end.copy().translate(dir.right(), 2);
      return RectSolid.newRect(start, end).withHeight(10);
   }

   public boolean hasEnd(Coord pos) {
      return pos.equals(this.start) || pos.equals(this.end);
   }

   @Override
   public boolean collide(Bounded other) {
      return this.getBoundingBox().collide(other);
   }

   @Override
   public IShape getShape(Shape type) {
      return this.getBoundingBox().getShape(type);
   }

   @Override
   public Coord getStart() {
      return this.getBoundingBox().getStart();
   }

   @Override
   public Coord getEnd() {
      return this.getBoundingBox().getEnd();
   }

   public Optional<Direction> getEntrance(Coord vertex) {
      Coord start = this.start;
      Coord end = this.end;
      if (vertex.equals(start)) {
         return Optional.of(start.dirTo(end));
      } else {
         return vertex.equals(end) ? Optional.of(end.dirTo(start)) : Optional.empty();
      }
   }
}
