package greymerk.roguelike.treasure.loot.books;

import com.github.fnar.minecraft.block.BlockType;
import greymerk.roguelike.treasure.loot.Book;
import greymerk.roguelike.worldgen.WorldEditor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;

public class BookStatistics extends Book {
   public BookStatistics(WorldEditor editor) {
      super("greymerk", "Statistics");

      for (String page : this.getPages(editor)) {
         this.addPage(page);
      }
   }

   private List<String> getPages(WorldEditor editor) {
      List<String> pages = new ArrayList<>();
      Map<BlockType, Integer> stats = editor.getStats();
      int[] counter = new int[]{0};
      StringBuilder[] page = new StringBuilder[]{new StringBuilder()};
      Set<Entry<BlockType, Integer>> entries = stats.entrySet();
      entries.stream().sorted(Comparator.comparingInt((Entry<BlockType, Integer> e) -> e.getValue()).reversed()).forEach(entry -> {
         BlockType blockType = entry.getKey();
         Integer count = entry.getValue();
         String name = StringUtils.abbreviate(blockType.toString(), 16);
         String line = name + ": " + count + "\n";
         page[0].append(line);
         counter[0]++;
         if (counter[0] == 12) {
            counter[0] = 0;
            pages.add(page[0].toString());
            page[0] = new StringBuilder();
         }
      });
      pages.add(page[0].toString());
      return pages;
   }
}
