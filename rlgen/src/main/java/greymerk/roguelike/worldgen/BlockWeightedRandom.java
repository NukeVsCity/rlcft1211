package greymerk.roguelike.worldgen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import greymerk.roguelike.util.WeightedChoice;
import greymerk.roguelike.util.WeightedRandomizer;

public class BlockWeightedRandom implements BlockBrush {
   private WeightedRandomizer<BlockBrush> blocks = new WeightedRandomizer<>();

   public BlockWeightedRandom() {
   }

   public BlockWeightedRandom(JsonElement data) {
      for (JsonElement jsonElement : (JsonArray)data) {
         if (!jsonElement.isJsonNull()) {
            JsonObject d = jsonElement.getAsJsonObject();
            int weight = d.get("weight").getAsInt();
            BlockBrush toAdd = BlockProvider.create(d);
            this.addBlock(toAdd, weight);
         }
      }
   }

   public BlockWeightedRandom(WeightedRandomizer<BlockBrush> blocks) {
      this.blocks = blocks;
   }

   public void addBlock(BlockBrush toAdd, int weight) {
      this.blocks.add(new WeightedChoice<>(toAdd, weight));
   }

   @Override
   public boolean stroke(WorldEditor editor, Coord origin, boolean fillAir, boolean replaceSolid) {
      BlockBrush block = this.blocks.get(editor.getRandom());
      return block.stroke(editor, origin, fillAir, replaceSolid);
   }

   public BlockWeightedRandom copy() {
      return new BlockWeightedRandom(this.blocks.copy());
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof BlockWeightedRandom)) {
         return false;
      } else {
         BlockWeightedRandom other = (BlockWeightedRandom)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$blocks = this.blocks;
            Object other$blocks = other.blocks;
            return this$blocks == null ? other$blocks == null : this$blocks.equals(other$blocks);
         }
      }
   }

   protected boolean canEqual(Object other) {
      return other instanceof BlockWeightedRandom;
   }

   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $blocks = this.blocks;
      return result * 59 + ($blocks == null ? 43 : $blocks.hashCode());
   }

   @Override
   public String toString() {
      return "BlockWeightedRandom(blocks=" + this.blocks + ")";
   }
}
