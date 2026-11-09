package com.github.alexthe666.iceandfire.block;

import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.core.ModBlocks;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.12 BlockDragonOre (silver_ore, sapphire_ore). Drops are loot tables: silver ore drops itself, sapphire ore a gem with the 1.12
 * quantityDroppedWithBonus formula (apply_bonus ore_drops). XP: sapphire 3..7 always (1.12 compared getItemDropped, which ignores
 * silk touch), silver 0.
 */
public class BlockDragonOre extends Block {
   public BlockDragonOre(Properties props, String name) {
      super(props);
      IafNames.block(this, name);
   }

   @Override
   public int getExpDrop(BlockState state, LevelAccessor level, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity breaker, ItemStack tool) {
      return this == ModBlocks.sapphireOre ? Mth.nextInt(level.getRandom(), 3, 7) : 0;
   }
}
