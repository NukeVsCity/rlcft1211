package com.github.fnar.minecraft.item.mapper;

import com.github.fnar.minecraft.tag.CompoundTag;
import com.github.fnar.minecraft.tag.IntTag;
import com.github.fnar.minecraft.tag.ListTag;
import com.github.fnar.minecraft.tag.StringTag;
import com.github.fnar.minecraft.tag.Tag;
import com.nukevscity.rlgen.legacy.LegacyStates;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantment;

/** rlgen: the bits of 1.21 the item mappers share (registries, 1.12 effect ids, the NBT tags RLD put on items). */
public final class Mc {
   private static volatile RegistryAccess access;

   private Mc() {
   }

   /** Set before a dungeon is generated (enchantments are data driven in 1.21). */
   public static void setAccess(RegistryAccess registryAccess) {
      access = registryAccess;
   }

   public static RegistryAccess access() {
      return access;
   }

   @Nullable
   public static Item item(String name) {
      ResourceLocation id = ResourceLocation.tryParse(name.contains(":") ? name : "minecraft:" + name);
      return id != null && BuiltInRegistries.ITEM.containsKey(id) ? BuiltInRegistries.ITEM.get(id) : null;
   }

   @Nullable
   public static Holder<Enchantment> enchantment(String name) {
      if (access == null) {
         return null;
      }
      return access.registryOrThrow(Registries.ENCHANTMENT).getHolder(ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.withDefaultNamespace(name)))
         .map(h -> (Holder<Enchantment>)h).orElse(null);
   }

   private static final String[] EFFECTS = {"", "speed", "slowness", "haste", "mining_fatigue", "strength", "instant_health", "instant_damage",
      "jump_boost", "nausea", "regeneration", "resistance", "fire_resistance", "water_breathing", "invisibility", "blindness", "night_vision", "hunger",
      "weakness", "poison", "wither", "health_boost", "absorption", "saturation", "glowing", "levitation", "luck", "unluck", "slow_falling",
      "conduit_power", "dolphins_grace", "bad_omen", "hero_of_the_village"};

   public static String effectName(int id) {
      return id > 0 && id < EFFECTS.length ? EFFECTS[id] : "speed";
   }

   @Nullable
   public static Holder<MobEffect> effect(int id) {
      return BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.withDefaultNamespace(effectName(id))).map(h -> (Holder<MobEffect>)h).orElse(null);
   }

   /** ItemStack.setItemDamage: durability for tools and armour, otherwise 1.12 metadata (charcoal, enchanted golden apple, ...). */
   public static ItemStack withDamage(ItemStack stack, int damage) {
      if (damage == 0 || stack.isEmpty()) {
         return stack;
      }
      if (stack.isDamageableItem()) {
         stack.setDamageValue(damage);
         return stack;
      }
      Item meta = LegacyStates.item(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath(), damage);
      if (meta != null && meta != stack.getItem()) {
         ItemStack copy = new ItemStack(meta, stack.getCount());
         copy.applyComponents(stack.getComponentsPatch());
         return copy;
      }
      return stack;
   }

   /** RLD's item tags: display name and lore become components, anything else is kept as custom data. */
   public static void applyTags(ItemStack stack, CompoundTag tags) {
      net.minecraft.nbt.CompoundTag rest = new net.minecraft.nbt.CompoundTag();
      for (Map.Entry<String, Tag> e : tags.getTags()) {
         if (e.getKey().equals("display") && e.getValue() instanceof CompoundTag display) {
            for (Map.Entry<String, Tag> d : display.getTags()) {
               if (d.getKey().equals("Name") && d.getValue() instanceof StringTag name) {
                  stack.set(DataComponents.CUSTOM_NAME, Component.literal(name.getValue()));
               } else if (d.getKey().equals("Lore") && d.getValue() instanceof ListTag lore) {
                  List<Component> lines = new ArrayList<>();
                  for (Tag t : lore.getTags()) {
                     if (t instanceof StringTag s) {
                        lines.add(Component.literal(s.getValue()));
                     }
                  }
                  stack.set(DataComponents.LORE, new ItemLore(lines));
               } else if (d.getKey().equals("color") && d.getValue() instanceof IntTag color) {
                  stack.set(DataComponents.DYED_COLOR, new net.minecraft.world.item.component.DyedItemColor(color.getValue(), true));
               }
            }
         } else if (!e.getKey().equals("HideFlags")) {
            rest.put(e.getKey(), toNbt(e.getValue()));
         }
      }
      if (!rest.isEmpty()) {
         CustomData.update(DataComponents.CUSTOM_DATA, stack, t -> t.merge(rest));
      }
   }

   /** TagMapper1_12 */
   public static net.minecraft.nbt.Tag toNbt(Tag tag) {
      if (tag instanceof StringTag s) {
         return net.minecraft.nbt.StringTag.valueOf(s.getValue());
      } else if (tag instanceof ListTag l) {
         net.minecraft.nbt.ListTag out = new net.minecraft.nbt.ListTag();
         l.getTags().forEach(t -> out.add(toNbt(t)));
         return out;
      } else if (tag instanceof CompoundTag c) {
         net.minecraft.nbt.CompoundTag out = new net.minecraft.nbt.CompoundTag();
         c.getTags().forEach(e -> out.put(e.getKey(), toNbt(e.getValue())));
         return out;
      }
      return net.minecraft.nbt.IntTag.valueOf(((IntTag)tag).getValue());
   }

   static <T> Optional<T> opt(T t) {
      return Optional.ofNullable(t);
   }
}
