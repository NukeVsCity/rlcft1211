package com.alcatrazescapee.notreepunching.common.items;

import com.alcatrazescapee.notreepunching.NoTreePunching;
import com.alcatrazescapee.notreepunching.util.types.Stone;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** 1.12 ItemRock (rock/*): placed back as a loose rock on top of a normal cube (the clicked block, whatever face). */
public class ItemRock extends Item {
   private final Stone type;

   public ItemRock(Stone type, Properties props) {
      super(props);
      this.type = type;
   }

   @Override
   public InteractionResult useOn(UseOnContext ctx) {
      Level world = ctx.getLevel();
      BlockPos pos = ctx.getClickedPos();
      Player player = ctx.getPlayer();
      BlockState stateUnder = world.getBlockState(pos);
      BlockState stateAt = world.getBlockState(pos.above());
      if (stateUnder.isRedstoneConductor(world, pos) && stateAt.canBeReplaced()) {
         if (!world.isClientSide) {
            ItemStack stack = ctx.getItemInHand();
            if (player == null || !player.isCreative()) {
               stack.shrink(1);
            }
            world.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            world.setBlockAndUpdate(pos.above(), NoTreePunching.LOOSE_ROCKS.get(this.type).get().defaultBlockState());
         }
         return InteractionResult.sidedSuccess(world.isClientSide);
      }
      return InteractionResult.PASS;
   }
}
