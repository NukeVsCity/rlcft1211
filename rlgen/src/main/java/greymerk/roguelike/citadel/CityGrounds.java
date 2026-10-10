package greymerk.roguelike.citadel;

import com.github.fnar.minecraft.block.SingleBlockBrush;
import greymerk.roguelike.dungeon.towers.Tower;
import greymerk.roguelike.dungeon.towers.TowerType;
import greymerk.roguelike.theme.Theme;
import greymerk.roguelike.theme.Themes;
import greymerk.roguelike.util.graph.Edge;
import greymerk.roguelike.util.graph.Graph;
import greymerk.roguelike.util.mst.MSTPoint;
import greymerk.roguelike.util.mst.MinimumSpanningTree;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.RectSolid;
import java.util.Random;

public class CityGrounds {
   public static void generate(WorldEditor editor, MinimumSpanningTree mst, Theme theme, Coord pos) {
      Coord start = pos.copy();
      start.translate(new Coord(-51, 10, -51));
      Coord end = pos.copy();
      end.translate(new Coord(51, 40, 51));
      RectSolid.newRect(start, end).fill(editor, SingleBlockBrush.AIR);
      start = pos.copy();
      start.translate(new Coord(-51, 10, -51));
      end = pos.copy();
      end.translate(new Coord(51, 20, 51));
      RectSolid.newRect(start, end).fill(editor, theme.getPrimary().getWall());
      start = pos.copy();
      start.translate(new Coord(-34, 20, -34));
      end = pos.copy();
      end.translate(new Coord(34, 30, 34));
      RectSolid.newRect(start, end).fill(editor, theme.getPrimary().getWall());
      start = pos.copy();
      start.translate(new Coord(-17, 30, -17));
      end = pos.copy();
      end.translate(new Coord(17, 40, 17));
      RectSolid.newRect(start, end).fill(editor, theme.getPrimary().getWall());
      Coord cursor = pos.copy();
      cursor.up(20);

      for (Edge<MSTPoint> e : mst.getEdges()) {
         start = e.getStart().getPosition();
         start.translate(cursor);
         end = e.getEnd().getPosition();
         end.translate(cursor);
         end.down(20);
         RectSolid.newRect(start, end).fill(editor, theme.getPrimary().getWall());
      }

      Graph<Coord> layout = mst.getGraph();

      for (Coord c : layout.getPoints()) {
         c.translate(pos);
         Random rand = editor.getRandom();
         Theme themeBase = Themes.values()[rand.nextInt(Themes.values().length)].getThemeBase();
         Tower tower = TowerType.instantiate(TowerType.values()[rand.nextInt(TowerType.values().length)], editor, themeBase);
         tower.generate(new Coord(c.getX(), 50, c.getZ()));
      }
   }
}
