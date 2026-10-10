package greymerk.roguelike.worldgen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.List;

public class BlockCheckers implements BlockBrush {
   private final BlockBrush fillOne;
   private final BlockBrush fillTwo;
   private Coord offset;

   public BlockCheckers(BlockBrush fillOne, BlockBrush fillTwo, Coord offset) {
      this.fillOne = fillOne;
      this.fillTwo = fillTwo;
      this.offset = offset.copy();
   }

   public BlockCheckers(BlockBrush fillOne, BlockBrush fillTwo) {
      this(fillOne, fillTwo, new Coord(0, 0, 0));
   }

   public BlockCheckers(JsonElement json) {
      JsonArray arr = (JsonArray)json;
      List<BlockBrush> blocks = new ArrayList<>();

      for (JsonElement jsonElement : arr) {
         if (!jsonElement.isJsonNull()) {
            blocks.add(BlockProvider.create(jsonElement.getAsJsonObject()));
         }
      }

      this.fillOne = blocks.get(0);
      this.fillTwo = blocks.get(1);
   }

   @Override
   public boolean stroke(WorldEditor editor, Coord origin, boolean fillAir, boolean replaceSolid) {
      int x = origin.getX() - this.offset.getX();
      int y = origin.getY() - this.offset.getY();
      int z = origin.getZ() - this.offset.getY();
      if (x % 2 == 0) {
         if (z % 2 == 0) {
            return y % 2 == 0
               ? this.fillOne.stroke(editor, origin.copy(), fillAir, replaceSolid)
               : this.fillTwo.stroke(editor, origin.copy(), fillAir, replaceSolid);
         } else {
            return y % 2 == 0
               ? this.fillTwo.stroke(editor, origin.copy(), fillAir, replaceSolid)
               : this.fillOne.stroke(editor, origin.copy(), fillAir, replaceSolid);
         }
      } else if (z % 2 == 0) {
         return y % 2 == 0
            ? this.fillTwo.stroke(editor, origin.copy(), fillAir, replaceSolid)
            : this.fillOne.stroke(editor, origin.copy(), fillAir, replaceSolid);
      } else {
         return y % 2 == 0
            ? this.fillOne.stroke(editor, origin.copy(), fillAir, replaceSolid)
            : this.fillTwo.stroke(editor, origin.copy(), fillAir, replaceSolid);
      }
   }

   public BlockCheckers copy() {
      return new BlockCheckers(this.fillOne.copy(), this.fillTwo.copy(), this.offset.copy());
   }
}
