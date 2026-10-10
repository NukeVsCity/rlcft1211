package jaredbgreat.dldungeons.builder;

import jaredbgreat.dldungeons.compat.World;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public abstract class AbstractBlock implements IBlockPlacer {
   public static final Block spawner = Blocks.SPAWNER;
   public static final Block chest = Blocks.CHEST;
   public static final Block portal1 = Blocks.END_PORTAL_FRAME;
   public static final Block portal2 = Blocks.END_PORTAL;
   public static final Block bedrock = Blocks.BEDROCK;

   public static boolean isProtectedBlock(World world, int x, int y, int z) {
      Block block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
      return block == chest || block == spawner || block == portal1 || block == portal2 || block == bedrock;
   }
}
