package com.github.alexthe666.iceandfire.misc;

import com.github.alexthe666.iceandfire.IafRegistries;
import com.github.alexthe666.iceandfire.core.IafMeta;
import com.github.alexthe666.iceandfire.core.ModBlocks;
import com.github.alexthe666.iceandfire.core.ModItems;
import com.github.alexthe666.iceandfire.item.ItemBestiary;
import com.github.alexthe666.iceandfire.item.ItemHippogryphEgg;
import com.github.alexthe666.iceandfire.item.block.ItemBlockMeta;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

/** 1.12 CreativeTab "iceandfire" (icon: dragon skull, no search bar) with the 1.12 getSubItems of every item. */
public final class CreativeTab {
   private CreativeTab() {
   }

   /** Items 1.12 left out of the tab. */
   private static final Set<String> HIDDEN = Set.of("egginice", "dragon_horn_fire", "dragon_horn_ice", "stone_statue");

   public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = IafRegistries.CREATIVE_TABS.register("iceandfire", () -> CreativeModeTab.builder()
      .title(Component.translatable("itemGroup.iceandfire"))
      .icon(() -> new ItemStack(ModItems.dragon_skull))
      .displayItems((params, out) -> {
         for (String name : ModItems.NAMES) {
            if (HIDDEN.contains(name)) {
               continue;
            }
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("iceandfire", name));
            for (ItemStack s : subItems(item)) {
               out.accept(s);
            }
         }
         for (java.util.function.Supplier<? extends net.minecraft.world.level.ItemLike> extra : CreativeTab.EXTRA) {
            out.accept(extra.get());
         }
      }).build());

   /** Items of the other branches shown after the 1.12 items (spawn eggs etc.): {@code CreativeTab.EXTRA.add(MY_SPAWN_EGG)} at construction. */
   public static final java.util.List<java.util.function.Supplier<? extends net.minecraft.world.level.ItemLike>> EXTRA = new java.util.ArrayList<>();

   private static ItemStack[] metas(Item item, int n) {
      ItemStack[] r = new ItemStack[n];
      for (int i = 0; i < n; i++) {
         r[i] = IafMeta.stack(item, 1, i);
      }
      return r;
   }

   public static ItemStack[] subItems(Item item) {
      if (item instanceof ItemBlockMeta m) {
         return metas(item, m.subtypes);
      }
      if (item == ModItems.bestiary) {
         return new ItemStack[]{new ItemStack(item), ItemBestiary.allPages(item)};
      }
      if (item == ModItems.dragon_skull) {
         return metas(item, 2);
      }
      if (item == ModItems.dragon_armor_iron || item == ModItems.dragon_armor_gold || item == ModItems.dragon_armor_diamond) {
         return metas(item, 4);
      }
      if (item == ModItems.hippogryph_egg) {
         return metas(item, ItemHippogryphEgg.WILD_TYPES);
      }
      if (item == ModItems.deathworm_chitin) {
         return metas(item, 3);
      }
      if (item == ModItems.deathworm_egg) {
         return metas(item, 2);
      }
      if (item == ModItems.myrmex_desert_egg || item == ModItems.myrmex_jungle_egg) {
         return metas(item, 5);
      }
      return new ItemStack[]{new ItemStack(item)};
   }

   public static void init() {
      ModBlocks.class.getName();
   }
}
