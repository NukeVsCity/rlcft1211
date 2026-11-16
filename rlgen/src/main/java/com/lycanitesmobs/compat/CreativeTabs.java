package com.lycanitesmobs.compat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * 1.12 CreativeTabs used by Lycanites (itemsTab, blocksTab, creaturesTab, chargesTab, equipmentPartsTab): a name, an icon and
 * the items that called setCreativeTab with it, registered as 1.21 creative mode tabs in that order.
 */
public class CreativeTabs {
   public static final Map<String, CreativeTabs> TABS = new LinkedHashMap<>();
   public final String name;
   public final List<ItemLike> items = new ArrayList<>();
   public Supplier<ItemStack> icon = () -> ItemStack.EMPTY;

   public CreativeTabs(String name) {
      this.name = name;
      TABS.put(name, this);
   }

   public static void add(CreativeTabs tab, ItemLike item) {
      if (tab != null && !tab.items.contains(item)) {
         tab.items.add(item);
      }
   }

   public static void register(RegisterEvent event) {
      event.register(Registries.CREATIVE_MODE_TAB, helper -> {
         for (CreativeTabs tab : TABS.values()) {
            helper.register(ResourceLocation.fromNamespaceAndPath("lycanitesmobs", tab.name.replace("lycanitesmobs.", "").toLowerCase()),
               CreativeModeTab.builder()
                  .title(Component.translatable("itemGroup." + tab.name))
                  .icon(() -> {
                     ItemStack stack = tab.icon.get();
                     if (stack.isEmpty() && !tab.items.isEmpty()) {
                        stack = new ItemStack(tab.items.get(0));
                     }
                     return stack.isEmpty() ? new ItemStack(net.minecraft.world.item.Items.BONE) : stack;
                  })
                  .displayItems((params, output) -> {
                     for (ItemLike item : tab.items) {
                        Item i = item.asItem();
                        if (i != null && i != net.minecraft.world.item.Items.AIR) {
                           output.accept(i);
                        }
                     }
                  })
                  .build());
         }
      });
   }
}
