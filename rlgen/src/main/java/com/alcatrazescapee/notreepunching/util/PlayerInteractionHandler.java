package com.alcatrazescapee.notreepunching.util;

import com.alcatrazescapee.notreepunching.ModConfig;
import com.alcatrazescapee.notreepunching.NoTreePunching;
import com.alcatrazescapee.notreepunching.common.items.ItemMattock;
import com.alcatrazescapee.notreepunching.common.items.ItemSaw;
import com.alcatrazescapee.notreepunching.util.types.ToolType;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

/** 1.12 PlayerInteractionHandler (+ WoodRecipeHandler.isLog/isPlank/isAxe): flint knapping on stone, chopping logs and planks with an axe. */
public final class PlayerInteractionHandler {
   private PlayerInteractionHandler() {
   }

   /** 1.12 Material.ROCK and isNormalCube: a full pickaxe block that is not metal */
   private static boolean isRock(Level world, BlockPos pos, BlockState state) {
      return state.is(BlockTags.MINEABLE_WITH_PICKAXE) && state.isRedstoneConductor(world, pos) && state.getSoundType(world, pos, null) != SoundType.METAL
         && !state.is(BlockTags.ICE);
   }

   public static boolean hasAction(Level world, BlockPos pos, ItemStack stack, @Nullable Direction face) {
      BlockState state = world.getBlockState(pos);
      if (stack.is(Items.FLINT)) {
         return ModConfig.BALANCE.flintKnappingChance > 0.0 && isRock(world, pos, state) && face == Direction.UP;
      } else if (isAxe(stack)) {
         BlockState stateDown = world.getBlockState(pos.below());
         if (face == Direction.UP && stateDown.isSolidRender(world, pos.below())) {
            return isLog(state) && !isLog(stateDown) || isPlank(state);
         }
      }
      return false;
   }

   public static boolean performAction(Level world, BlockPos pos, Player player, ItemStack stack, @Nullable Direction face, InteractionHand hand) {
      if (stack.is(Items.FLINT)) {
         return handleFlint(world, pos, player, stack, hand);
      }
      return isAxe(stack) && handleChopping(world, pos, player, stack, hand);
   }

   private static boolean handleFlint(Level world, BlockPos pos, Player player, ItemStack stack, InteractionHand hand) {
      if (!world.isClientSide) {
         if ((double)HarvestBlockHandler.RNG.nextFloat() < ModConfig.BALANCE.flintKnappingChance) {
            if ((double)HarvestBlockHandler.RNG.nextFloat() < ModConfig.BALANCE.flintKnappingSuccessChance) {
               drop(world, pos, new ItemStack(NoTreePunching.FLINT_SHARD.get(), 2));
            }
            if (!player.isCreative()) {
               stack.shrink(1);
            }
         }
         world.playSound(null, pos, NoTreePunching.KNAPPING.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
      }
      return true;
   }

   private static boolean handleChopping(Level world, BlockPos pos, Player player, ItemStack stack, InteractionHand hand) {
      if (!world.isClientSide) {
         if ((double)HarvestBlockHandler.RNG.nextFloat() < ModConfig.BALANCE.logChoppingChance) {
            int amount = 1 + ((double)HarvestBlockHandler.RNG.nextFloat() < 0.75 ? 1 : 0);
            if (!isWeakAxe(stack)) {
               amount++;
            }
            BlockState state = world.getBlockState(pos);
            Block plank = getPlankForLog(state);
            if (plank != null) {
               drop(world, pos, new ItemStack(plank, amount));
               stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
               world.removeBlock(pos, false);
            } else if (isPlank(state)) {
               drop(world, pos, new ItemStack(Items.STICK, 1 + ((double)HarvestBlockHandler.RNG.nextFloat() < 0.25 ? 1 : 0)));
               stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
               world.removeBlock(pos, false);
            }
            world.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.PLAYERS, 1.2F, 1.0F);
         } else {
            world.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.PLAYERS, 0.5F, 1.0F);
         }
      }
      return true;
   }

   /** CoreHelpers.dropItemInWorldExact: at the top centre, no motion */
   private static void drop(Level world, BlockPos pos, ItemStack stack) {
      ItemEntity e = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, stack);
      e.setDeltaMovement(0, 0, 0);
      world.addFreshEntity(e);
   }

   static boolean isAxe(ItemStack stack) {
      return stack.getItem() instanceof AxeItem || stack.is(ItemTags.AXES) || stack.getItem() instanceof ItemSaw || stack.getItem() instanceof ItemMattock
         || isWeakAxe(stack);
   }

   static boolean isWeakAxe(ItemStack stack) {
      return stack.is(NoTreePunching.FLINT_TOOLS.get(ToolType.AXE).get());
   }

   /** WoodRecipeHandler: a log is a block whose 1-log crafting recipe gives planks (1.12 logWood) */
   static boolean isLog(BlockState state) {
      return getPlankForLog(state) != null;
   }

   static boolean isPlank(BlockState state) {
      return state.is(BlockTags.PLANKS);
   }

   @Nullable
   public static Block getPlankForLog(BlockState state) {
      if (!state.is(BlockTags.LOGS)) {
         return null;
      }
      ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
      String p = id.getPath().replace("stripped_", "").replace("_log", "_planks").replace("_wood", "_planks").replace("_stem", "_planks")
         .replace("_hyphae", "_planks");
      ResourceLocation plank = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), p);
      return BuiltInRegistries.BLOCK.containsKey(plank) && !plank.equals(id) ? BuiltInRegistries.BLOCK.get(plank) : null;
   }
}
