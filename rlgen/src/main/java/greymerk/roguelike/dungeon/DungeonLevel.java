package greymerk.roguelike.dungeon;

import com.github.fnar.roguelike.worldgen.generatables.BaseGeneratable;
import com.github.fnar.roguelike.worldgen.generatables.LadderPillar;
import com.github.fnar.roguelike.worldgen.generatables.SpiralStaircase;
import greymerk.roguelike.dungeon.base.BaseRoom;
import greymerk.roguelike.dungeon.base.RoomIterator;
import greymerk.roguelike.dungeon.base.RoomType;
import greymerk.roguelike.dungeon.layout.DungeonNode;
import greymerk.roguelike.dungeon.layout.LevelLayout;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.theme.Theme;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.filter.Filter;
import greymerk.roguelike.worldgen.filter.IFilter;

public class DungeonLevel {
   private final LevelSettings settings;
   public LevelLayout layout;

   public DungeonLevel(LevelSettings settings) {
      this.settings = settings;
   }

   public LevelSettings getSettings() {
      return this.settings;
   }

   public LevelLayout getLayout() {
      return this.layout;
   }

   public void encase(WorldEditor editor) {
      this.encaseNodes(editor);
      this.encaseTunnels(editor);
   }

   private void encaseNodes(WorldEditor editor) {
      DungeonNode start = this.layout.getStart();
      DungeonNode end = this.layout.getEnd();
      this.layout.getNodes().stream().filter(node -> node != start && node != end).forEach(node -> node.encase(editor, this.settings.getTheme()));
   }

   private void encaseTunnels(WorldEditor editor) {
      this.layout.getTunnels().forEach(t -> t.encase(editor, this.settings.getTheme()));
   }

   public void applyFilters(WorldEditor editor) {
      this.settings.getFilters().stream().map(Filter::get).forEach(filter -> this.filter(editor, filter));
   }

   public void filter(WorldEditor editor, IFilter filter) {
      this.layout.getBoundingBoxes().forEach(box -> filter.apply(editor, this.settings.getTheme(), box));
   }

   public Coord generateLayout(WorldEditor editor, Coord start) {
      this.layout = this.settings.getLayoutGenerator().generate(start, editor.getRandom(start));
      Coord end = this.layout.getEnd().getPosition().copy();
      this.assignRooms(editor);
      return end;
   }

   private void assignRooms(WorldEditor editor) {
      RoomIterator roomIterator = new RoomIterator(this.settings, editor);

      for (int count = 0; this.layout.hasEmptyRooms(); count++) {
         BaseRoom toGenerate = count < this.settings.getNumRooms()
            ? roomIterator.getDungeonRoom()
            : RoomType.CORNER.newSingleRoomSetting().instantiate(this.settings, editor);
         DungeonNode node = this.layout.getBestFit(toGenerate);
         node.setDungeon(toGenerate);
      }

      this.layout.getStart().setDungeon(RoomType.LINKER.newSingleRoomSetting().instantiate(this.settings, editor));
      this.layout.getEnd().setDungeon(RoomType.LINKERTOP.newSingleRoomSetting().instantiate(this.settings, editor));
   }

   public DungeonLevel generateLinkers(WorldEditor editor, DungeonLevel previous) {
      DungeonNode lower = this.getLayout().getStart();
      lower.generate();
      if (previous != null) {
         DungeonNode upper = previous.getLayout().getEnd();
         upper.generate();
         this.generateStairsOrLadder(editor, lower, upper);
      }

      return this;
   }

   private void generateStairsOrLadder(WorldEditor editor, DungeonNode lower, DungeonNode upper) {
      Theme theme = this.getSettings().getTheme();
      int height = upper.getPosition().getY() - lower.getPosition().getY();
      BaseGeneratable linker = (BaseGeneratable)(editor.getRandom().nextDouble() < 0.75
         ? SpiralStaircase.newStaircase(editor).withHeight(height)
         : LadderPillar.newLadderPillar(editor).withHeight(height));
      linker.withTheme(theme).generate(lower.getPosition());
   }

   public void generateRooms() {
      LevelLayout layout = this.getLayout();
      layout.getNodes().stream().filter(node -> !layout.isStartOrEnd(node)).forEach(DungeonNode::generate);
   }

   public void tunnel(WorldEditor editor) {
      this.getLayout().getTunnels().forEach(tunnel -> tunnel.construct(editor, this.getSettings()));
   }

   public void generateSegments(WorldEditor editor) {
      this.getLayout().getTunnels().forEach(tunnel -> tunnel.genSegments(editor, this));
   }

   public boolean containsRoomAt(Coord coord) {
      return this.layout.containsRoomAt(coord);
   }
}
