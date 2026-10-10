package jaredbgreat.dldungeons.builder;

import com.nukevscity.rlgen.legacy.LegacyStates;
import jaredbgreat.dldungeons.compat.World;
import jaredbgreat.dldungeons.util.debug.Logging;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class DBlock extends AbstractBlock {
   protected BlockState block;

   DBlock(BlockState block) {
      this.block = block;
   }

   @Deprecated
   @Override
   public void placeNoMeta(World world, int x, int y, int z) {
      if (!isProtectedBlock(world, x, y, z)) {
         world.setBlockState(new BlockPos(x, y, z), this.block);
      }
   }

   @Override
   public void place(World world, int x, int y, int z) {
      if (!isProtectedBlock(world, x, y, z)) {
         world.setBlockState(new BlockPos(x, y, z), this.block);
      }
   }

   @Override
   public boolean equals(Object other) {
      return !(other instanceof DBlock) ? false : this.block.equals(((DBlock)other).block);
   }

   @Override
   public int hashCode() {
      return this.block.hashCode();
   }

   /** rlgen: "modid:name(meta)" read as a 1.12 block + metadata and flattened to its 1.21 state. */
   public static DBlock makeDBlock(String id) {
      try {
         StringTokenizer nums = new StringTokenizer(id, ":({[]})");
         String modid = nums.nextToken();
         String name = modid + ":" + nums.nextToken();
         int meta = nums.hasMoreElements() ? Integer.parseInt(nums.nextToken()) : 0;
         BlockState theBlock = LegacyStates.block(name, meta);
         if (theBlock == null) {
            String error = "[DLDUNGEONS] ERROR! Block read as \"" + id + "\" was was not in registry (returned null).";
            Logging.logError(error);
            throw new NoSuchElementException(error);
         }
         return new DBlock(theBlock);
      } catch (NoSuchElementException var7) {
         throw new NoSuchElementException("Something was wrong with " + id + "; could not find all elements.");
      }
   }

   @Override
   public Object getContents() {
      return this.block;
   }
}
