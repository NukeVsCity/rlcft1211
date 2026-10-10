package greymerk.roguelike.worldgen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nukevscity.rlgen.legacy.LegacyStates;
import greymerk.roguelike.dungeon.settings.DungeonSettingParseException;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

/** rlgen: the 1.12 MetaBlock, now holding a 1.21 block state (1.12 name + meta from settings go through LegacyStates). */
public class MetaBlock1_12 {
   private BlockState state;
   private int flag = 2;

   public MetaBlock1_12(Block block) {
      this.state = block.defaultBlockState();
   }

   public MetaBlock1_12(BlockState state) {
      this.state = state;
   }

   public MetaBlock1_12(JsonElement e) throws DungeonSettingParseException {
      JsonObject json = (JsonObject)e;
      String name = json.get("name").getAsString();
      int meta = json.has("meta") ? json.get("meta").getAsInt() : 0;
      BlockState s = LegacyStates.block(name, meta);
      if (s == null) {
         throw new DungeonSettingParseException("No such block: " + name);
      }
      this.state = s;
      if (name.contains("leaves")) {
         this.withProperty(LeavesBlock.PERSISTENT, true);
      }
      this.flag = json.has("flag") ? json.get("flag").getAsInt() : 2;
   }

   /** IBlockState.withProperty; properties the 1.21 block does not have (or values it refuses) are ignored. */
   public <T extends Comparable<T>, V extends T> MetaBlock1_12 withProperty(Property<T> property, V value) {
      if (this.state.hasProperty(property) && property.getPossibleValues().contains(value)) {
         this.state = this.state.setValue(property, value);
      }
      return this;
   }

   public BlockState getState() {
      return this.state;
   }

   public Block getBlock() {
      return this.state.getBlock();
   }

   public int getFlag() {
      return this.flag;
   }

   @Override
   public String toString() {
      return this.state.getBlock().getDescriptionId();
   }

   @Override
   public boolean equals(Object other) {
      return other == this || other instanceof MetaBlock1_12 o && this.state.equals(o.state);
   }

   @Override
   public int hashCode() {
      return this.state.hashCode();
   }

   public Item asItem() {
      return this.getBlock().asItem();
   }
}
