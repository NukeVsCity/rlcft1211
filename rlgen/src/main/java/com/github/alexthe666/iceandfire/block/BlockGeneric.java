package com.github.alexthe666.iceandfire.block;

import com.github.alexthe666.iceandfire.core.IafNames;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.12 BlockGeneric. dragon_ice (the only translucent one) hides faces between identical states (1.12 shouldSideBeRendered) and is not
 * opaque. Beacon base (silver/sapphire block) is the minecraft:beacon_base_blocks tag. Harvest tool/level are block tags.
 */
public class BlockGeneric extends Block {
   private final boolean translucent;

   public BlockGeneric(Properties props, String name, boolean translucent) {
      super(translucent ? props.noOcclusion().isRedstoneConductor((s, l, p) -> false).isSuffocating((s, l, p) -> false).isViewBlocking((s, l, p) -> false) : props);
      this.translucent = translucent;
      IafNames.block(this, name);
   }

   public BlockGeneric(Properties props, String name) {
      this(props, name, false);
   }

   @Override
   protected boolean skipRendering(BlockState state, BlockState adjacent, Direction side) {
      if (this.translucent) {
         return adjacent == state || super.skipRendering(state, adjacent, side);
      }
      return super.skipRendering(state, adjacent, side);
   }

   @Override
   protected float getShadeBrightness(BlockState state, net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos pos) {
      return this.translucent ? 1.0F : super.getShadeBrightness(state, level, pos);
   }

   @Override
   protected boolean propagatesSkylightDown(BlockState state, net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos pos) {
      return this.translucent || super.propagatesSkylightDown(state, level, pos);
   }
}
