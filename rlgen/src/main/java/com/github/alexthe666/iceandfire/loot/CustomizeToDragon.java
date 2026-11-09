package com.github.alexthe666.iceandfire.loot;

import com.github.alexthe666.iceandfire.core.ModItems;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Random;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Ice and Fire 1.7.1 CustomizeToDragon (loot function iceandfire:customize_to_dragon): turns the loot of the dead-dragon
 * tables into the looted dragon's colour and scales the counts with its age.
 */
public class CustomizeToDragon extends LootItemConditionalFunction {
   private static final DeferredRegister<LootItemFunctionType<?>> FUNCTIONS = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, "iceandfire");
   public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<CustomizeToDragon>> TYPE = FUNCTIONS.register(
      "customize_to_dragon", () -> new LootItemFunctionType<>(codec())
   );

   private static MapCodec<CustomizeToDragon> codec() {
      return com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(i -> commonFields(i).apply(i, CustomizeToDragon::new));
   }

   public static void register(IEventBus modBus) {
      FUNCTIONS.register(modBus);
   }

   protected CustomizeToDragon(List<LootItemCondition> conditions) {
      super(conditions);
   }

   @Override
   public LootItemFunctionType<CustomizeToDragon> getType() {
      return TYPE.get();
   }

   private static boolean isAny(ItemStack stack, Object... items) {
      for (Object item : items) {
         if (item instanceof net.minecraft.world.level.ItemLike like && stack.is(like.asItem())) {
            return true;
         }
      }
      return false;
   }

   @Override
   protected ItemStack run(ItemStack stack, LootContext context) {
      if (!stack.isEmpty() && context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof EntityDragonBase dragon) {
         Random random = new Random();
         if (stack.is(ModItems.dragonbone)) {
            stack.setCount(1 + random.nextInt(1 + dragon.getAgeInDays() / 25));
            return stack;
         } else if (isAny(stack, ModItems.dragonscales_red, ModItems.dragonscales_green, ModItems.dragonscales_bronze, ModItems.dragonscales_gray,
            ModItems.dragonscales_blue, ModItems.dragonscales_white, ModItems.dragonscales_sapphire, ModItems.dragonscales_silver)) {
            stack.setCount(dragon.getAgeInDays() / 25 + random.nextInt(1 + dragon.getAgeInDays() / 5));
            return new ItemStack(dragon.getVariantScale(dragon.getVariant()), stack.getCount());
         } else if (isAny(stack, ModItems.dragonegg_red, ModItems.dragonegg_green, ModItems.dragonegg_bronze, ModItems.dragonegg_gray,
            ModItems.dragonegg_blue, ModItems.dragonegg_white, ModItems.dragonegg_sapphire, ModItems.dragonegg_silver)) {
            if (dragon.isAdult()) {
               return new ItemStack(dragon.getVariantEgg(dragon.getVariant()), stack.getCount());
            } else {
               stack.setCount(1 + random.nextInt(1 + dragon.getAgeInDays() / 5));
               return new ItemStack(dragon.getVariantScale(dragon.getVariant()), stack.getCount());
            }
         } else if (isAny(stack, ModItems.fire_dragon_flesh, ModItems.ice_dragon_flesh)) {
            stack.setCount(1 + random.nextInt(1 + dragon.getAgeInDays() / 25));
            return new ItemStack(dragon.isFire ? ModItems.fire_dragon_flesh : ModItems.ice_dragon_flesh, stack.getCount());
         } else if (isAny(stack, ModItems.dragon_skull)) {
            ItemStack stack1 = stack.copy();
            return stack1;
         } else if (isAny(stack, ModItems.fire_dragon_blood, ModItems.ice_dragon_blood)) {
            return new ItemStack(dragon.isFire ? ModItems.fire_dragon_blood : ModItems.ice_dragon_blood, stack.getCount());
         } else {
            return !isAny(stack, ModItems.fire_dragon_heart, ModItems.ice_dragon_heart)
               ? stack
               : new ItemStack(dragon.isFire ? ModItems.fire_dragon_heart : ModItems.ice_dragon_heart, stack.getCount());
         }
      } else {
         return stack;
      }
   }
}
