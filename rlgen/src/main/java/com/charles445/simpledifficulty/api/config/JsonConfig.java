package com.charles445.simpledifficulty.api.config;

import com.charles445.simpledifficulty.SimpleDifficulty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * 1.12 api.config.JsonConfig and its json entry classes. 1.12 keyed the maps by registry name; here they are keyed by the 1.21 item
 * or block the 1.12 name+metadata turned into (see config.JsonConfigInternal), metadata only survives for items that still have
 * subtypes (simpledifficulty:juice, stored in the simpledifficulty:metadata component).
 */
public class JsonConfig {
   public static Map<Item, List<JsonTemperatureIdentity>> armorTemperatures = new HashMap<>();
   public static Map<Block, List<JsonPropertyTemperature>> blockTemperatures = new HashMap<>();
   public static Map<Item, List<JsonConsumableTemperature>> consumableTemperature = new HashMap<>();
   public static Map<Item, List<JsonConsumableThirst>> consumableThirst = new HashMap<>();
   public static Map<String, JsonTemperature> dimensionTemperature = new HashMap<>();
   public static Map<String, JsonTemperature> fluidTemperatures = new HashMap<>();
   public static Map<Item, List<JsonTemperatureIdentity>> heldItemTemperatures = new HashMap<>();

   public static void registerArmorTemperature(Item item, float temperature, JsonItemIdentity identity) {
      registerIdentity(armorTemperatures, item, new JsonTemperatureIdentity(temperature, identity));
   }

   public static void registerHeldItem(Item item, float temperature, JsonItemIdentity identity) {
      registerIdentity(heldItemTemperatures, item, new JsonTemperatureIdentity(temperature, identity));
   }

   private static void registerIdentity(Map<Item, List<JsonTemperatureIdentity>> map, Item item, JsonTemperatureIdentity result) {
      List<JsonTemperatureIdentity> currentList = map.computeIfAbsent(item, k -> new ArrayList<>());
      for (int i = 0; i < currentList.size(); i++) {
         if (currentList.get(i).identity.matches(result.identity)) {
            currentList.set(i, result);
            return;
         }
      }
      currentList.add(result);
   }

   /** 1.12 registerBlockTemperature, including its refusal to add a property-less entry next to property entries. */
   public static boolean registerBlockTemperature(Block block, float temperature, Map<String, String> properties) {
      List<JsonPropertyTemperature> currentList = blockTemperatures.computeIfAbsent(block, k -> new ArrayList<>());
      JsonPropertyTemperature result = new JsonPropertyTemperature(temperature, properties);
      if (!properties.isEmpty()) {
         for (int i = 0; i < currentList.size(); i++) {
            if (currentList.get(i).properties.equals(properties)) {
               currentList.set(i, result);
               return true;
            }
         }
         currentList.add(result);
         return true;
      } else {
         for (JsonPropertyTemperature jpt : currentList) {
            if (!jpt.properties.isEmpty()) {
               return false;
            }
         }
         for (int i = 0; i < currentList.size(); i++) {
            if (currentList.get(i).properties.isEmpty()) {
               currentList.set(i, result);
               return true;
            }
         }
         currentList.add(result);
         return true;
      }
   }

   public static void registerFluidTemperature(String fluidName, float temperature) {
      fluidTemperatures.put(fluidName, new JsonTemperature(temperature));
   }

   public static void registerConsumableTemperature(String group, Item item, JsonItemIdentity identity, float temperature, int duration) {
      List<JsonConsumableTemperature> currentList = consumableTemperature.computeIfAbsent(item, k -> new ArrayList<>());
      JsonConsumableTemperature result = new JsonConsumableTemperature(group, temperature, duration, identity);
      for (int i = 0; i < currentList.size(); i++) {
         if (currentList.get(i).identity.matches(identity)) {
            currentList.set(i, result);
            return;
         }
      }
      currentList.add(result);
   }

   public static void registerConsumableThirst(Item item, JsonItemIdentity identity, int amount, float saturation, float thirstyChance) {
      List<JsonConsumableThirst> currentList = consumableThirst.computeIfAbsent(item, k -> new ArrayList<>());
      JsonConsumableThirst result = new JsonConsumableThirst(amount, saturation, thirstyChance, identity);
      for (int i = 0; i < currentList.size(); i++) {
         if (currentList.get(i).identity.matches(identity)) {
            currentList.set(i, result);
            return;
         }
      }
      currentList.add(result);
   }

   public static void registerDimensionTemperature(int dim, float temperature) {
      dimensionTemperature.put("" + dim, new JsonTemperature(temperature));
   }

   /** 1.12 JsonTemperature */
   public static class JsonTemperature {
      public float temperature;

      public JsonTemperature(float temperature) {
         this.temperature = temperature;
      }
   }

   /** 1.12 JsonItemIdentity: metadata -1/32767 = any, nbt = a compound that must be contained in the stack's (custom data) tag. */
   public static class JsonItemIdentity {
      public int metadata;
      @Nullable
      public String nbt;
      @Nullable
      private CompoundTag nbtCompound;

      public JsonItemIdentity(int metadata) {
         this(metadata, null);
      }

      public JsonItemIdentity(int metadata, @Nullable String nbt) {
         this.metadata = metadata;
         this.nbt = nbt;
         if (nbt != null) {
            try {
               this.nbtCompound = TagParser.parseTag(nbt);
            } catch (Exception e) {
               this.nbtCompound = null;
               this.nbt = null;
            }
         }
      }

      public boolean matches(ItemStack stack) {
         if (this.nbtCompound == null) {
            return this.matches(SimpleDifficulty.getMetadata(stack), null);
         }
         return this.matches(SimpleDifficulty.getMetadata(stack), legacyTag(stack));
      }

      /** The stack's 1.12 NBT as far as the json can ask for it: custom data, plus the 1.12 "Potion" string of potions. */
      @Nullable
      private static CompoundTag legacyTag(ItemStack stack) {
         CustomData data = stack.get(DataComponents.CUSTOM_DATA);
         PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);
         if (data == null && (potion == null || potion.potion().isEmpty())) {
            return null;
         }
         CompoundTag tag = data == null ? new CompoundTag() : data.copyTag();
         if (potion != null && potion.potion().isPresent()) {
            potion.potion().get().unwrapKey().ifPresent(k -> tag.putString("Potion", k.location().toString()));
         }
         return tag;
      }

      public boolean matches(JsonItemIdentity sent) {
         return this.matches(sent.metadata, sent.nbtCompound);
      }

      public boolean matches(int stackMetadata, @Nullable CompoundTag stackCompound) {
         if (this.metadata != -1 && this.metadata != 32767 && this.metadata != stackMetadata) {
            return false;
         } else if (this.nbt != null && !this.nbt.isEmpty() && this.nbtCompound != null) {
            return checkNestedCompound(this.nbtCompound, stackCompound);
         } else {
            return true;
         }
      }

      private static boolean checkNestedCompound(CompoundTag self, @Nullable CompoundTag stack) {
         if (stack == null) {
            return false;
         }
         for (String key : self.getAllKeys()) {
            Tag a = self.get(key);
            Tag b = stack.get(key);
            if (a != null) {
               if (a instanceof CompoundTag ca && b instanceof CompoundTag cb) {
                  if (!checkNestedCompound(ca, cb)) {
                     return false;
                  }
               } else if (!a.equals(b)) {
                  return false;
               }
            } else if (b != null) {
               return false;
            }
         }
         return true;
      }
   }

   /** 1.12 JsonTemperatureIdentity */
   public static class JsonTemperatureIdentity {
      public float temperature;
      public JsonItemIdentity identity;

      public JsonTemperatureIdentity(float temperature, JsonItemIdentity identity) {
         this.temperature = temperature;
         this.identity = identity;
      }

      public boolean matches(ItemStack stack) {
         return this.identity.matches(stack);
      }
   }

   /** 1.12 JsonConsumableThirst */
   public static class JsonConsumableThirst {
      public JsonItemIdentity identity;
      public int amount;
      public float saturation;
      public float thirstyChance;

      public JsonConsumableThirst(int amount, float saturation, float thirstyChance, JsonItemIdentity identity) {
         this.amount = amount;
         this.saturation = saturation;
         this.thirstyChance = thirstyChance;
         this.identity = identity;
      }

      public boolean matches(ItemStack stack) {
         return this.identity.matches(stack);
      }
   }

   /** 1.12 JsonConsumableTemperature */
   public static class JsonConsumableTemperature {
      public JsonItemIdentity identity;
      public String group;
      public float temperature;
      public int duration;

      public JsonConsumableTemperature(String group, float temperature, int duration, JsonItemIdentity identity) {
         this.group = group;
         this.temperature = temperature;
         this.duration = duration;
         this.identity = identity;
      }

      public boolean matches(ItemStack stack) {
         return this.identity.matches(stack);
      }
   }

   /** 1.12 JsonPropertyTemperature: every property of the state named in the entry must have that value (toString of the value). */
   public static class JsonPropertyTemperature {
      public Map<String, String> properties;
      public float temperature;

      public JsonPropertyTemperature(float temperature, Map<String, String> properties) {
         this.temperature = temperature;
         this.properties = new HashMap<>(properties);
      }

      public boolean matchesState(BlockState blockstate) {
         for (Property<?> property : blockstate.getProperties()) {
            String want = this.properties.get(property.getName());
            if (want != null && !want.equals(valueName(blockstate, property))) {
               return false;
            }
         }
         return true;
      }

      private static <T extends Comparable<T>> String valueName(BlockState state, Property<T> property) {
         return property.getName(state.getValue(property));
      }
   }
}
