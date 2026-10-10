package atomicstryker.ruins.common;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * rlgen: the 1.12 World as a Ruin template saw it while building. Reads fall through to the 1.12 terrain; writes and tile entities are
 * kept here and later copied into the chunks they land in.
 */
public final class RuinsWorld {
   public static final int HEIGHT = 256;
   private final Function<BlockPos, BlockState> terrain;
   public final Map<Long, BlockState> blocks = new LinkedHashMap<>();
   public final Map<Long, Tile> tiles = new HashMap<>();

   /** What a tile entity was given: chest contents, a loot table to roll, the mob of a spawner, a skull. */
   public static final class Tile {
      public final ItemStack[] items = new ItemStack[27];
      public String lootTable;
      public int lootCount;
      public long lootSeed;
      public String spawner;
      public int skullType = -1;
      public int skullRotation;

      public int getSizeInventory() {
         return this.items.length;
      }

      public ItemStack getStackInSlot(int slot) {
         ItemStack s = this.items[slot];
         return s == null ? ItemStack.EMPTY : s;
      }

      public void setInventorySlotContents(int slot, ItemStack stack) {
         this.items[slot] = stack == null || stack.isEmpty() ? null : stack;
      }

      public void clear() {
         java.util.Arrays.fill(this.items, null);
         this.lootTable = null;
      }
   }

   public RuinsWorld(Function<BlockPos, BlockState> terrain) {
      this.terrain = terrain;
   }

   public BlockState getBlockState(BlockPos pos) {
      BlockState s = this.blocks.get(pos.asLong());
      return s != null ? s : this.terrain.apply(pos);
   }

   public boolean setBlockState(BlockPos pos, BlockState state) {
      if (pos.getY() < 0 || pos.getY() >= HEIGHT) {
         return false;
      }
      long k = pos.asLong();
      this.blocks.remove(k); // keep the order of the last write
      this.blocks.put(k, state);
      this.tiles.remove(k);
      return true;
   }

   /** The tile entity at pos (made on first use, like World.getTileEntity), or null when the block has none. */
   public Tile getTileEntity(BlockPos pos) {
      if (!(this.getBlockState(pos).getBlock() instanceof EntityBlock) || !this.blocks.containsKey(pos.asLong())) {
         return null;
      }
      return this.tiles.computeIfAbsent(pos.asLong(), k -> new Tile());
   }

   public int getActualHeight() {
      return HEIGHT;
   }
}
