package com.github.alexthe666.iceandfire.loot;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
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
 * 1.12 loot functions iceandfire:customize_to_dragon (dragon drops become the looted dragon's colour, stack sizes scale with its age)
 * and iceandfire:customize_to_sea_serpent. The entity specific code (EntityDragonBase/EntitySeaSerpent fields) is set by the mob
 * branches into {@link #DRAGON} / {@link #SEA_SERPENT}: (stack, looted entity) -> new stack, the 1.12 apply body. Unset, the stack is
 * returned unchanged.
 */
public class CustomizeToDragon extends LootItemConditionalFunction {
   public static volatile BiFunction<ItemStack, Entity, ItemStack> DRAGON;
   public static volatile BiFunction<ItemStack, Entity, ItemStack> SEA_SERPENT;

   public static final DeferredRegister<LootItemFunctionType<?>> FUNCTIONS = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, IceAndFire.MODID);
   public static final MapCodec<CustomizeToDragon> DRAGON_CODEC = RecordCodecBuilder.mapCodec(i -> commonFields(i).apply(i, c -> new CustomizeToDragon(c, false)));
   public static final MapCodec<CustomizeToDragon> SERPENT_CODEC = RecordCodecBuilder.mapCodec(i -> commonFields(i).apply(i, c -> new CustomizeToDragon(c, true)));
   public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<CustomizeToDragon>> CUSTOMIZE_TO_DRAGON = FUNCTIONS.register("customize_to_dragon",
      () -> new LootItemFunctionType<>(DRAGON_CODEC));
   public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<CustomizeToDragon>> CUSTOMIZE_TO_SEA_SERPENT = FUNCTIONS.register(
      "customize_to_sea_serpent", () -> new LootItemFunctionType<>(SERPENT_CODEC));

   private final boolean seaSerpent;

   protected CustomizeToDragon(List<LootItemCondition> conditions, boolean seaSerpent) {
      super(conditions);
      this.seaSerpent = seaSerpent;
   }

   public static void register(IEventBus modBus) {
      FUNCTIONS.register(modBus);
   }

   @Override
   public LootItemFunctionType<? extends LootItemConditionalFunction> getType() {
      return this.seaSerpent ? CUSTOMIZE_TO_SEA_SERPENT.get() : CUSTOMIZE_TO_DRAGON.get();
   }

   @Override
   protected ItemStack run(ItemStack stack, LootContext context) {
      Entity looted = context.getParamOrNull(LootContextParams.THIS_ENTITY);
      BiFunction<ItemStack, Entity, ItemStack> f = this.seaSerpent ? SEA_SERPENT : DRAGON;
      if (stack.isEmpty() || looted == null || f == null) {
         return stack;
      }
      return f.apply(stack, looted);
   }
}
