package greymerk.roguelike.worldgen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BlockJumble implements BlockBrush {
   private final List<BlockBrush> blocks = new ArrayList<>();

   public BlockJumble() {
   }

   public BlockJumble(List<BlockBrush> blocks) {
      this.blocks.addAll(blocks);
   }

   public BlockJumble(JsonElement data) {
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
      BlockBrush block = this.blocks.get(editor.getRandom().nextInt(this.blocks.size()));
      return block.stroke(editor, origin, fillAir, replaceSolid);
   }

   public BlockJumble copy() {
      return new BlockJumble(this.blocks.stream().map(BlockBrush::copy).collect(Collectors.toList()));
   }
}
