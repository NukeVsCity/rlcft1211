package greymerk.roguelike.dungeon.segment;

import com.github.fnar.roguelike.worldgen.generatables.Pillar;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import greymerk.roguelike.dungeon.DungeonLevel;
import greymerk.roguelike.dungeon.segment.part.SegmentBase;
import greymerk.roguelike.theme.Theme;
import greymerk.roguelike.util.WeightedChoice;
import greymerk.roguelike.util.WeightedRandomizer;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SegmentGenerator {
   protected Segment arch;
   protected WeightedRandomizer<Segment> segments;

   public SegmentGenerator() {
      this(Segment.ARCH);
   }

   public SegmentGenerator(Segment arch) {
      this.segments = new WeightedRandomizer<>();
      this.arch = arch;
   }

   public SegmentGenerator(JsonObject json) {
      String archType = json.get("arch").getAsString();
      this.arch = Segment.valueOf(archType);
      this.segments = new WeightedRandomizer<>();

      for (JsonElement jsonElement : json.get("segments").getAsJsonArray()) {
         if (!jsonElement.isJsonNull()) {
            JsonObject segData = jsonElement.getAsJsonObject();
            this.add(segData);
         }
      }
   }

   public void add(JsonObject entry) {
      String segType = entry.get("type").getAsString();
      Segment segment = Segment.valueOf(segType);
      if (entry.has("arch")) {
         boolean a = entry.get("arch").getAsBoolean();
         if (a) {
            this.arch = segment;
         }
      } else {
         int weight = entry.has("weight") ? entry.get("weight").getAsInt() : 1;
         this.segments.add(new WeightedChoice<>(segment, weight));
      }
   }

   public SegmentGenerator inherit(SegmentGenerator toInherit) {
      SegmentGenerator segmentGenerator = new SegmentGenerator();
      segmentGenerator.add(toInherit);
      segmentGenerator.add(this);
      return segmentGenerator;
   }

   public void add(SegmentGenerator segmentGenerator) {
      this.arch = segmentGenerator.arch;
      this.segments.merge(segmentGenerator.segments);
   }

   public void add(Segment segment, int weight) {
      this.segments.add(new WeightedChoice<>(segment, weight));
   }

   public Segment getArch() {
      return this.arch;
   }

   public WeightedRandomizer<Segment> getSegments() {
      return this.segments;
   }

   public List<SegmentBase> genSegment(WorldEditor editor, DungeonLevel level, Direction dir, Coord pos) {
      List<SegmentBase> segments = new ArrayList<>();

      for (Direction orthogonals : dir.orthogonals()) {
         SegmentBase segment = this.pickSegment(editor.getRandom(), dir, pos);
         if (segment == null) {
            return segments;
         }

         segment.generate(editor, level, orthogonals, level.getSettings().getTheme(), pos.copy());
         segments.add(segment);
      }

      if (!level.containsRoomAt(pos) && editor.getRandom().nextInt(3) == 0) {
         this.addSupport(editor, level.getSettings().getTheme(), pos.copy().down(2));
      }

      return segments;
   }

   private SegmentBase pickSegment(Random random, Direction dir, Coord pos) {
      int z = pos.getZ();
      if ((dir == Direction.NORTH || dir == Direction.SOUTH) && z % 3 == 0) {
         return this.pickSegment(z % 6 == 0, random);
      } else {
         int x = pos.getX();
         return (dir == Direction.WEST || dir == Direction.EAST) && x % 3 == 0 ? this.pickSegment(x % 6 == 0, random) : null;
      }
   }

   private SegmentBase pickSegment(boolean isArch, Random random) {
      return isArch
         ? Segment.getSegment(this.getArch())
         : (this.getSegments().isEmpty() ? Segment.getSegment(Segment.WALL) : Segment.getSegment(this.getSegments().get(random)));
   }

   private void addSupport(WorldEditor editor, Theme theme, Coord origin) {
      if (editor.isAirBlock(origin)) {
         Pillar.newPillar(editor).withPillar(theme.getPrimary().getPillar()).withStairs(theme.getPrimary().getStair()).withHeight(1).generate(origin);
      }
   }

   public SegmentGenerator with(Segment segment, int weight) {
      this.add(segment, weight);
      return this;
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof SegmentGenerator)) {
         return false;
      } else {
         SegmentGenerator other = (SegmentGenerator)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$arch = this.getArch();
            Object other$arch = other.getArch();
            if (this$arch == null ? other$arch == null : this$arch.equals(other$arch)) {
               Object this$segments = this.getSegments();
               Object other$segments = other.getSegments();
               return this$segments == null ? other$segments == null : this$segments.equals(other$segments);
            } else {
               return false;
            }
         }
      }
   }

   protected boolean canEqual(Object other) {
      return other instanceof SegmentGenerator;
   }

   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $arch = this.getArch();
      result = result * 59 + ($arch == null ? 43 : $arch.hashCode());
      Object $segments = this.getSegments();
      return result * 59 + ($segments == null ? 43 : $segments.hashCode());
   }

   @Override
   public String toString() {
      return "SegmentGenerator(arch=" + this.getArch() + ", segments=" + this.getSegments() + ")";
   }
}
