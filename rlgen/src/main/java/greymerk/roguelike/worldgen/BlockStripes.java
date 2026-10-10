package greymerk.roguelike.worldgen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BlockStripes implements BlockBrush {
   private final List<BlockBrush> blocks = new ArrayList<>();

   public BlockStripes() {
   }

   public BlockStripes(List<BlockBrush> blocks) {
      this.blocks.addAll(blocks);
   }

   public BlockStripes(JsonElement data) {
      this();

      for (JsonElement jsonElement : (JsonArray)data) {
         if (!jsonElement.isJsonNull()) {
            this.addBlock(BlockProvider.create(jsonElement.getAsJsonObject()));
         }
      }
   }

   public void addBlock(BlockBrush toAdd) {
      this.blocks.add(toAdd);
   }

   @Override
   public boolean stroke(WorldEditor editor, Coord origin, boolean fillAir, boolean replaceSolid) {
      int size = this.blocks.size();
      int choice = Math.abs(origin.getX() % size + origin.getY() % size + origin.getZ() % size) % size;
      BlockBrush block = this.blocks.get(choice);
      return block.stroke(editor, origin, fillAir, replaceSolid);
   }

   public BlockStripes copy() {
      return new BlockStripes(this.blocks.stream().map(BlockBrush::copy).collect(Collectors.toList()));
   }
}
