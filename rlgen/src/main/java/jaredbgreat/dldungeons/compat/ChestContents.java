package jaredbgreat.dldungeons.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

/** rlgen: the TileEntityChest a dungeon chest was filled through. */
public final class ChestContents {
   private final World world;
   private final BlockPos pos;

   public ChestContents(World world, BlockPos pos) {
      this.world = world;
      this.pos = pos;
   }

   public void setInventorySlotContents(int slot, ItemStack stack) {
      this.world.setChestItem(this.pos, slot, stack);
   }
}
