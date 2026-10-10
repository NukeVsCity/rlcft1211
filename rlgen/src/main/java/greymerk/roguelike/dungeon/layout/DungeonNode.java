package greymerk.roguelike.dungeon.layout;

import com.github.fnar.util.ReportThisIssueException;
import greymerk.roguelike.dungeon.base.BaseRoom;
import greymerk.roguelike.theme.Theme;
import greymerk.roguelike.worldgen.Bounded;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.IShape;
import greymerk.roguelike.worldgen.shapes.Shape;
import java.util.List;

public class DungeonNode implements Bounded {
   public static final int ENCASING_SIZE = 1;
   private final Coord pos;
   private BaseRoom toGenerate;
   private final List<Direction> entrances;

   public DungeonNode(List<Direction> entrances, Coord origin) {
      this.entrances = entrances;
      this.pos = origin.copy();
   }

   public void setDungeon(BaseRoom toGenerate) {
      this.toGenerate = toGenerate;
   }

   public int getSize() {
      return this.toGenerate == null ? 10 : this.toGenerate.getSize() + 1;
   }

   public void encase(WorldEditor editor, Theme theme) {
      int size = this.getSize();
      int height = 9;
      int depth = 4;
      IShape caseRect = this.getPosition().copy().newHollowRect(size).withHeight(height + depth).down(depth);
      theme.getPrimary().getWall().fill(editor, caseRect);
   }

   public List<Direction> getEntrances() {
      return this.entrances;
   }

   public Coord getPosition() {
      return this.pos.copy();
   }

   public BaseRoom getRoom() {
      return this.toGenerate;
   }

   private Bounded getBoundingBox(int size) {
      return this.pos.copy().down().newRect(size).withHeight(10);
   }

   private Bounded getBoundingBox() {
      return this.getBoundingBox(this.getSize());
   }

   public boolean connectsTo(DungeonTunnel tunnel) {
      return tunnel.hasEnd(this.pos);
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

   public boolean overlaps(int size, DungeonNode other) {
      return this != other && this.getBoundingBox(size).collide(other);
   }

   public boolean overlaps(int size, DungeonTunnel tunnel) {
      return !this.connectsTo(tunnel) && this.getBoundingBox(size).collide(tunnel);
   }

   public boolean isNotYetGenerated() {
      return this.getRoom() == null;
   }

   public void generate() {
      try {
         this.getRoom().generate(this.getPosition(), this.getEntrances());
      } catch (Exception var2) {
         new ReportThisIssueException(var2).printStackTrace();
      }
   }

   public boolean hasOverlappingNode(int size, List<DungeonNode> nodes) {
      return nodes.stream().anyMatch(other -> this.overlaps(size, other));
   }

   public boolean contains(Coord pos) {
      return (int)this.getPosition().distance(pos) < this.getSize();
   }

   public double getDistance(DungeonNode end) {
      return this.getPosition().distance(end.getPosition());
   }
}
