package com.github.alexthe666.iceandfire.core;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

/**
 * 1.12 ModRecipes. Crafting/smelting recipes, ore dictionary (c: tags) and banner patterns are data (data/iceandfire/...).
 * Dispenser behaviours for the arrows come from ArrowItem (ItemIafArrow.asProjectile). Here: the brewing recipe water bottle +
 * shiny scales -> water breathing (1.12 BrewingRecipeRegistry: a plain water bottle, not awkward). The RLCraft CraftTweaker brews with
 * dragon hearts/flesh/blood, sea serpent fangs and wither bones make Potion Core / Better Survival potions, which are not in 1.21.
 */
public final class ModRecipes {
   private ModRecipes() {
   }

   public static void init(IEventBus modBus) {
      NeoForge.EVENT_BUS.addListener(ModRecipes::brewing);
   }

   private static void brewing(RegisterBrewingRecipesEvent event) {
      ItemStack water = PotionContents.createItemStack(Items.POTION, Potions.WATER);
      ItemStack breathing = PotionContents.createItemStack(Items.POTION, Potions.WATER_BREATHING);
      event.getBuilder().addRecipe(DataComponentIngredient.of(false, DataComponents.POTION_CONTENTS, water.get(DataComponents.POTION_CONTENTS), Items.POTION),
         Ingredient.of(ModItems.shiny_scales), breathing);
   }
}
