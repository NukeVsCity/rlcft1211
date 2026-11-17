package com.alcatrazescapee.notreepunching.common.recipe;

import com.alcatrazescapee.notreepunching.NoTreePunching;
import com.alcatrazescapee.notreepunching.common.blocks.BlockPottery;
import com.alcatrazescapee.notreepunching.util.types.Pottery;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;

/**
 * 1.12 ModRecipes.FIRE_PIT (FirePitRecipe, one input item -> output): every pottery block but worked clay -> its fired type, every
 * furnace recipe whose output is food (1.12 took a copy of FurnaceRecipes at init; here the smelting recipes are searched), and
 * "brickClay" (the clay brick) -> brick. The first matching recipe wins, in that order.
 */
public final class FirePitRecipes {
   private FirePitRecipes() {
   }

   /** the output for one input item, EMPTY if the fire pit cannot cook it */
   public static ItemStack getOutput(Level level, ItemStack input) {
      if (input.isEmpty()) {
         return ItemStack.EMPTY;
      }
      for (Pottery type : Pottery.values()) {
         if (type != Pottery.WORKED) {
            BlockPottery block = BlockPottery.get(type);
            if (input.is(block.asItem())) {
               return block.getFiredType();
            }
         }
      }
      SingleRecipeInput in = new SingleRecipeInput(input.copyWithCount(1));
      for (RecipeHolder<SmeltingRecipe> r : level.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING)) {
         ItemStack out = r.value().getResultItem(level.registryAccess());
         if (out.has(DataComponents.FOOD) && r.value().matches(in, level)) {
            return out.copy();
         }
      }
      if (input.is(NoTreePunching.CLAY_BRICK.get())) {
         return new ItemStack(Items.BRICK);
      }
      return ItemStack.EMPTY;
   }
}
