package com.nukevscity.rlgen.legacy;

import com.github.fnar.minecraft.item.mapper.Mc;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/** 1.12 item NBT (ench, StoredEnchantments, Potion, display, Unbreakable) as 1.21 item components; anything else becomes custom data. */
public final class LegacyItemNbt {
   private static final Map<Integer, String> ENCHANTMENTS = Map.ofEntries(Map.entry(0, "protection"), Map.entry(1, "fire_protection"),
      Map.entry(2, "feather_falling"), Map.entry(3, "blast_protection"), Map.entry(4, "projectile_protection"), Map.entry(5, "respiration"),
      Map.entry(6, "aqua_affinity"), Map.entry(7, "thorns"), Map.entry(8, "depth_strider"), Map.entry(9, "frost_walker"), Map.entry(10, "binding_curse"),
      Map.entry(16, "sharpness"), Map.entry(17, "smite"), Map.entry(18, "bane_of_arthropods"), Map.entry(19, "knockback"), Map.entry(20, "fire_aspect"),
      Map.entry(21, "looting"), Map.entry(22, "sweeping_edge"), Map.entry(32, "efficiency"), Map.entry(33, "silk_touch"), Map.entry(34, "unbreaking"),
      Map.entry(35, "fortune"), Map.entry(48, "power"), Map.entry(49, "punch"), Map.entry(50, "flame"), Map.entry(51, "infinity"),
      Map.entry(61, "luck_of_the_sea"), Map.entry(62, "lure"), Map.entry(70, "mending"), Map.entry(71, "vanishing_curse"));

   private LegacyItemNbt() {
   }

   public static String enchantmentName(int id) {
      return ENCHANTMENTS.get(id);
   }

   public static void apply(ItemStack stack, CompoundTag nbt) {
      if (stack.isEmpty() || nbt == null || nbt.isEmpty()) {
         return;
      }
      CompoundTag rest = new CompoundTag();
      for (String key : nbt.getAllKeys()) {
         Tag value = nbt.get(key);
         switch (key) {
            case "ench" -> enchant(stack, nbt.getList(key, Tag.TAG_COMPOUND), false);
            case "StoredEnchantments" -> enchant(stack, nbt.getList(key, Tag.TAG_COMPOUND), true);
            case "Potion" -> {
               ResourceLocation id = ResourceLocation.tryParse(nbt.getString(key));
               if (id != null) {
                  BuiltInRegistries.POTION.getHolder(id).ifPresent(p -> stack.set(DataComponents.POTION_CONTENTS, new PotionContents(p)));
               }
            }
            case "Unbreakable" -> {
               if (nbt.getBoolean(key)) {
                  stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
               }
            }
            case "display" -> display(stack, nbt.getCompound(key));
            case "HideFlags" -> {
            }
            default -> rest.put(key, value.copy());
         }
      }
      if (!rest.isEmpty()) {
         CustomData.update(DataComponents.CUSTOM_DATA, stack, t -> t.merge(rest));
      }
   }

   private static void enchant(ItemStack stack, ListTag list, boolean stored) {
      for (int i = 0; i < list.size(); i++) {
         CompoundTag e = list.getCompound(i);
         String name = ENCHANTMENTS.get((int)e.getShort("id"));
         Holder<Enchantment> holder = name == null ? null : Mc.enchantment(name);
         if (holder == null) {
            continue;
         }
         int level = Math.max(1, e.getShort("lvl"));
         if (stored) {
            ItemEnchantments.Mutable m = new ItemEnchantments.Mutable(stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY));
            m.set(holder, level);
            stack.set(DataComponents.STORED_ENCHANTMENTS, m.toImmutable());
         } else {
            EnchantmentHelper.updateEnchantments(stack, m -> m.set(holder, level));
         }
      }
   }

   private static void display(ItemStack stack, CompoundTag display) {
      if (display.contains("Name", Tag.TAG_STRING)) {
         stack.set(DataComponents.CUSTOM_NAME, Component.literal(display.getString("Name")));
      }
      if (display.contains("Lore", Tag.TAG_LIST)) {
         ListTag lore = display.getList("Lore", Tag.TAG_STRING);
         List<Component> lines = new ArrayList<>();
         for (int i = 0; i < lore.size(); i++) {
            lines.add(Component.literal(lore.getString(i)));
         }
         stack.set(DataComponents.LORE, new ItemLore(lines));
      }
      if (display.contains("color", Tag.TAG_ANY_NUMERIC)) {
         stack.set(DataComponents.DYED_COLOR, new DyedItemColor(display.getInt("color"), true));
      }
   }
}
