package com.charles445.simpledifficulty.register;

import com.charles445.simpledifficulty.SimpleDifficulty;
import com.charles445.simpledifficulty.api.SDItems;
import com.charles445.simpledifficulty.api.SDPotions;
import com.charles445.simpledifficulty.api.thirst.ThirstEnum;
import com.charles445.simpledifficulty.item.ItemCanteen;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 1.12 RegisterRecipes (brewing as vanilla mixes, POTION_RECIPES_AS_VANILLA) with RLCraft's Shiv.zs changes: the awkward + ice/magma
 * chunk mixes are removed, mundane + blaze powder = heat resistance and mundane + frost powder = cold resistance instead. The two
 * smelting recipes and the crafting recipes are data (data/simpledifficulty/recipe); canteen_charcoal_recipe is CanteenCharcoalRecipe.
 */
public class RegisterRecipes {
   public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, SimpleDifficulty.MODID);
   public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CanteenCharcoalRecipe>> CANTEEN_CHARCOAL = SERIALIZERS.register(
      "canteen_charcoal_recipe", CanteenCharcoalSerializer::new);

   public static void register(IEventBus bus) {
      SERIALIZERS.register(bus);
      SDParticles.register(bus);
   }

   public static void registerBrewing(RegisterBrewingRecipesEvent event) {
      var b = event.getBuilder();
      b.addMix(Potions.MUNDANE, Items.BLAZE_POWDER, SDPotions.heat_resist_type);
      b.addMix(SDPotions.heat_resist_type, Items.REDSTONE, SDPotions.long_heat_resist_type);
      b.addMix(Potions.MUNDANE, SDItems.frost_powder.get(), SDPotions.cold_resist_type);
      b.addMix(SDPotions.cold_resist_type, Items.REDSTONE, SDPotions.long_cold_resist_type);
   }

   /** 1.12 CanteenCharcoalRecipe: shapeless, the result canteen gets the input canteen's doses as purified water. */
   public static class CanteenCharcoalRecipe extends ShapelessRecipe {
      public CanteenCharcoalRecipe(ShapelessRecipe r) {
         super(r.getGroup(), r.category(), r.getResultItem(null), r.getIngredients());
      }

      @Override
      public ItemStack assemble(CraftingInput invcraft, HolderLookup.Provider registries) {
         ItemStack output = super.assemble(invcraft, registries);
         if (!output.isEmpty()) {
            for (int i = 0; i < invcraft.size(); i++) {
               ItemStack ingredient = invcraft.getItem(i);
               if (!ingredient.isEmpty() && (ingredient.getItem() == SDItems.canteen.get() || ingredient.getItem() == SDItems.ironCanteen.get())) {
                  ItemCanteen canteen = (ItemCanteen)ingredient.getItem();
                  canteen.setDoses(output, ThirstEnum.PURIFIED, canteen.getDoses(ingredient));
                  break;
               }
            }
         }
         return output;
      }

      @Override
      public RecipeSerializer<?> getSerializer() {
         return CANTEEN_CHARCOAL.get();
      }
   }

   public static class CanteenCharcoalSerializer implements RecipeSerializer<CanteenCharcoalRecipe> {
      private static final MapCodec<CanteenCharcoalRecipe> CODEC = RecipeSerializer.SHAPELESS_RECIPE.codec()
         .xmap(r -> new CanteenCharcoalRecipe((ShapelessRecipe)r), r -> r);
      private static final StreamCodec<RegistryFriendlyByteBuf, CanteenCharcoalRecipe> STREAM = RecipeSerializer.SHAPELESS_RECIPE.streamCodec()
         .map(r -> new CanteenCharcoalRecipe((ShapelessRecipe)r), r -> r);

      @Override
      public MapCodec<CanteenCharcoalRecipe> codec() {
         return CODEC;
      }

      @Override
      public StreamCodec<RegistryFriendlyByteBuf, CanteenCharcoalRecipe> streamCodec() {
         return STREAM;
      }
   }
}
