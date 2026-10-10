package greymerk.roguelike.theme.builtin;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.normal.ColoredBlock;
import com.github.fnar.minecraft.block.normal.SlabBlock;
import com.github.fnar.minecraft.block.normal.StairsBlock;
import greymerk.roguelike.theme.BlockSet;
import greymerk.roguelike.theme.Theme;
import greymerk.roguelike.util.DyeColor;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.BlockStripes;
import greymerk.roguelike.worldgen.BlockWeightedRandom;

public class ThemeEniko2 extends Theme {
   public ThemeEniko2() {
      BlockStripes floor = new BlockStripes();
      floor.addBlock(ColoredBlock.stainedHardenedClay().setColor(DyeColor.BLUE));
      floor.addBlock(ColoredBlock.stainedHardenedClay().setColor(DyeColor.LIGHT_GRAY));
      BlockWeightedRandom walls = new BlockWeightedRandom();
      walls.addBlock(BlockType.STONE_BRICK.getBrush(), 20);
      walls.addBlock(BlockType.STONE_BRICK_CRACKED.getBrush(), 10);
      walls.addBlock(BlockType.STONE_BRICK_MOSSY.getBrush(), 5);
      walls.addBlock(BlockType.COBBLESTONE.getBrush(), 3);
      walls.addBlock(BlockType.GRAVEL.getBrush(), 1);
      StairsBlock stair = StairsBlock.stoneBrick();
      BlockBrush pillar = SlabBlock.stone().setTop(false).setFullBlock(true).setSeamless(true);
      this.primary = new BlockSet(floor, walls, stair, pillar);
      this.secondary = this.primary;
   }
}
