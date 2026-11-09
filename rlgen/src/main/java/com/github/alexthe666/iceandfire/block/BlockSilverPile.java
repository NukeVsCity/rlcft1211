package com.github.alexthe666.iceandfire.block;

import com.github.alexthe666.iceandfire.core.ModBlocks;
import net.minecraft.world.level.block.Block;

/** 1.12 BlockSilverPile: BlockGoldPile with silver piles and silver nugget drops (loot table). */
public class BlockSilverPile extends BlockGoldPile {
   public BlockSilverPile(Properties props) {
      super(props, "iceandfire.silverpile");
   }

   @Override
   protected Block pileBlock() {
      return ModBlocks.silverPile;
   }
}
