package com.alcatrazescapee.notreepunching.common.items;

import com.alcatrazescapee.notreepunching.NoTreePunching;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.12 ItemMattock: axe + shovel + "mattock" tool classes (damage max(material - 2, 1) + material, speed -2.7); tills grass/path
 * and dirt to farmland, coarse dirt to dirt.
 */
public class ItemMattock extends DiggerItem {
   /** axe and shovel blocks */
   public static final TagKey<Block> MINEABLE = BlockTags.create(ResourceLocation.fromNamespaceAndPath(NoTreePunching.MOD_ID, "mineable/mattock"));

   public ItemMattock(Tier material) {
      super(material, MINEABLE, new Item.Properties().attributes(DiggerItem.createAttributes(material, Math.max(material.getAttackDamageBonus() - 2.0F, 1.0F), -2.7F)));
   }

   @Override
   public InteractionResult useOn(UseOnContext ctx) {
      Player player = ctx.getPlayer();
      Level world = ctx.getLevel();
      BlockPos pos = ctx.getClickedPos();
      Direction facing = ctx.getClickedFace();
      ItemStack itemstack = ctx.getItemInHand();
      if (player == null || !player.mayUseItemAt(pos.relative(facing), facing, itemstack)) {
         return InteractionResult.FAIL;
      }
      BlockState iblockstate = world.getBlockState(pos);
      if (facing != Direction.DOWN && world.isEmptyBlock(pos.above())) {
         if (iblockstate.is(Blocks.GRASS_BLOCK) || iblockstate.is(Blocks.DIRT_PATH) || iblockstate.is(Blocks.DIRT)) {
            this.setBlock(itemstack, player, world, pos, Blocks.FARMLAND.defaultBlockState(), ctx);
            return InteractionResult.sidedSuccess(world.isClientSide);
         }
         if (iblockstate.is(Blocks.COARSE_DIRT)) {
            this.setBlock(itemstack, player, world, pos, Blocks.DIRT.defaultBlockState(), ctx);
            return InteractionResult.sidedSuccess(world.isClientSide);
         }
      }
      return InteractionResult.PASS;
   }

   private void setBlock(ItemStack stack, Player player, Level world, BlockPos pos, BlockState state, UseOnContext ctx) {
      world.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
      if (!world.isClientSide) {
         world.setBlock(pos, state, 11);
         stack.hurtAndBreak(1, player, net.minecraft.world.entity.LivingEntity.getSlotForHand(ctx.getHand()));
      }
   }
}
