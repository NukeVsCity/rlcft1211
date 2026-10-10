package greymerk.roguelike.theme.builtin;

import com.github.fnar.minecraft.block.normal.ColoredBlock;
import com.github.fnar.minecraft.block.normal.StairsBlock;
import greymerk.roguelike.theme.BlockSet;
import greymerk.roguelike.theme.Theme;
import greymerk.roguelike.util.DyeColor;
import greymerk.roguelike.worldgen.BlockBrush;

public class ThemeBumbo extends Theme {
   public ThemeBumbo() {
      BlockBrush green = ColoredBlock.stainedHardenedClay().setColor(DyeColor.GREEN);
      BlockBrush moustache = ColoredBlock.stainedHardenedClay().setColor(DyeColor.BLACK);
      BlockBrush black = ColoredBlock.concrete().setColor(DyeColor.BLACK);
      BlockBrush white = ColoredBlock.concrete().setColor(DyeColor.WHITE);
      BlockBrush yellow = ColoredBlock.concrete().setColor(DyeColor.YELLOW);
      BlockBrush red = ColoredBlock.concrete().setColor(DyeColor.RED);
      this.primary = new BlockSet(moustache, green, StairsBlock.acacia(), white);
      this.secondary = new BlockSet(red, yellow, StairsBlock.acacia(), black);
   }
}
