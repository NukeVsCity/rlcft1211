package greymerk.roguelike.citadel;

import greymerk.roguelike.theme.Themes;
import greymerk.roguelike.util.mst.MinimumSpanningTree;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.WorldEditor;
import java.util.Random;

public class Citadel {
   public static final int EDGE_LENGTH = 17;

   public static void generate(WorldEditor editor, int x, int z) {
      Random rand = editor.getRandom();
      MinimumSpanningTree mst = new MinimumSpanningTree(rand, 7, 17);
      CityGrounds.generate(editor, mst, Themes.OAK.getThemeBase(), new Coord(x, 50, z));
   }
}
