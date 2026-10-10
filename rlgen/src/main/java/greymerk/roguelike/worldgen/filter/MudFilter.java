package greymerk.roguelike.worldgen.filter;

import com.github.fnar.minecraft.block.BlockType;
import com.google.common.collect.Lists;
import greymerk.roguelike.theme.Theme;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.BlockJumble;
import greymerk.roguelike.worldgen.Bounded;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.Shape;
import java.util.ArrayList;

public class MudFilter implements IFilter {
   @Override
   public void apply(WorldEditor editor, Theme theme, Bounded box) {
      for (Coord pos : box.getShape(Shape.RECTSOLID)) {
         if (editor.getRandom().nextInt(40) == 0 && this.validLocation(editor, pos)) {
            this.generate(editor, pos, editor.getRandom().nextInt(3) + 2);
         }
      }
   }

   private void generate(WorldEditor editor, Coord pos, int counter) {
      if (counter > 0) {
         for (Direction dir : Direction.CARDINAL) {
            if (!editor.getRandom().nextBoolean()) {
               Coord next = pos.copy();
               next.translate(dir);
               this.generate(editor, next, counter - 1);
            }
         }

         if (this.validLocation(editor, pos)) {
            label48: {
               ArrayList<BlockBrush> wetBlocks = Lists.newArrayList(
                  new BlockBrush[]{BlockType.CLAY.getBrush(), BlockType.SOUL_SAND.getBrush(), BlockType.MYCELIUM.getBrush()}
               );
               BlockJumble wet = new BlockJumble(wetBlocks);
               ArrayList<BlockBrush> dryBlocks = Lists.newArrayList(
                  new BlockBrush[]{BlockType.DIRT_PODZOL.getBrush(), BlockType.DIRT.getBrush(), BlockType.DIRT_COARSE.getBrush()}
               );
               new BlockJumble(dryBlocks);
               switch (counter) {
                  case 1:
                     if (!editor.getRandom().nextBoolean()) {
                        break label48;
                     }

                     wet.stroke(editor, pos);
                     break;
                  case 4:
                  case 5:
                     BlockType.DIRT.getBrush().stroke(editor, pos);
                  case 3:
                     if (editor.getRandom().nextBoolean()) {
                        BlockType.DIRT_COARSE.getBrush().stroke(editor, pos);
                        break;
                     }
                  case 2:
                     wet.stroke(editor, pos);
                     break;
                  default:
                     break label48;
               }

               if (editor.getRandom().nextInt(6) != 0) {
                  return;
               }

               BlockJumble plants = new BlockJumble();
               plants.addBlock(BlockType.BROWN_MUSHROOM.getBrush());
               plants.addBlock(BlockType.RED_MUSHROOM.getBrush());
               Coord cursor = pos.copy();
               cursor.up();
               plants.stroke(editor, cursor);
               return;
            }

            BlockType.WATER_FLOWING.getBrush().stroke(editor, pos);
         }
      }
   }

   private boolean validLocation(WorldEditor editor, Coord pos) {
      if (!editor.isOpaqueCubeBlock(pos)) {
         return false;
      } else {
         Coord cursor = pos.copy();
         cursor.up();
         if (!editor.isAirBlock(cursor)) {
            return false;
         } else {
            cursor.down(2);
            if (editor.isAirBlock(cursor)) {
               return false;
            } else {
               cursor.up();

               for (Direction dir : Direction.values()) {
                  cursor.translate(dir);
                  if (!editor.isOpaqueCubeBlock(pos)) {
                     return false;
                  }

                  cursor.translate(dir.reverse());
               }

               return true;
            }
         }
      }
   }
}
