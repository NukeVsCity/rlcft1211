package com.lycanitesmobs.core.info;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
import com.lycanitesmobs.LycEntities;
import com.lycanitesmobs.compat.*;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.*;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.*;
import net.minecraft.network.chat.*;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.*;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.ambient.*;
import net.minecraft.world.entity.boss.enderdragon.*;
import net.minecraft.world.entity.boss.wither.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.item.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.*;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.*;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.common.*;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.*;
import net.neoforged.neoforge.event.tick.*;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.config.ConfigBase;
import com.lycanitesmobs.core.item.ItemCustom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ObjectLists {
   public static String[] itemListNames = new String[]{
      "RawMeat", "CookedMeat", "Vegetables", "RawFish", "CookedFish", "CactusFood", "Mushrooms", "Sweets", "Fuel"
   };
   public static Map<String, List<ItemStack>> itemLists = new HashMap<>();
   public static Map<String, List<Class>> entityLists = new HashMap<>();
   public static Map<String, List<Holder<MobEffect>>> effectLists = new HashMap<>();
   public static Map<String, Holder<MobEffect>> allEffects = new HashMap<>();

   public static void addItem(String list, Object object) {
      if (object instanceof Item || object instanceof Block || object instanceof ItemStack || object instanceof String) {
         list = list.toLowerCase();
         if (!itemLists.containsKey(list)) {
            itemLists.put(list, new ArrayList<>());
         }

         ItemStack itemStack = null;
         if (object instanceof Item) {
            itemStack = new ItemStack((Item)object);
         } else if (object instanceof Block) {
            itemStack = new ItemStack((Block)object);
         } else if (object instanceof ItemStack) {
            itemStack = (ItemStack)object;
         } else if (object instanceof String) {
            if (ObjectManager.getItem((String)object) != null) {
               itemStack = new ItemStack(ObjectManager.getItem((String)object));
            } else if (ObjectManager.getBlock((String)object) != null) {
               itemStack = new ItemStack(ObjectManager.getBlock((String)object));
            }
         }

         if (itemStack != null) {
            itemLists.get(list).add(itemStack);
         }
      }
   }

   public static void addEntity(String list, Object object) {
      if (object instanceof Entity || object instanceof String) {
         list = list.toLowerCase();
         if (!entityLists.containsKey(list)) {
            entityLists.put(list, new ArrayList<>());
         }

         Class entity = null;
         if (object instanceof String) {
            CreatureInfo creatureInfo = CreatureManager.getInstance().getCreature((String)object);
            if (creatureInfo != null) {
               entity = creatureInfo.entityClass;
            }
         }

         if (entity != null) {
            entityLists.get(list).add(entity);
         }
      }
   }

   public static void addEffect(String list, Holder<MobEffect> effect, String effectName) {
      if (effect != null) {
         allEffects.put(effectName, effect);
         list = list.toLowerCase();
         if (!effectLists.containsKey(list)) {
            effectLists.put(list, new ArrayList<>());
         }

         effectLists.get(list).add(effect);
      }
   }

   public static ItemStack[] getItems(String list) {
      list = list.toLowerCase();
      return !itemLists.containsKey(list) ? new ItemStack[0] : itemLists.get(list).toArray(new ItemStack[itemLists.get(list).size()]);
   }

   public static Class[] getEntites(String list) {
      list = list.toLowerCase();
      return !entityLists.containsKey(list) ? new Class[0] : entityLists.get(list).toArray(new Class[entityLists.get(list).size()]);
   }

   public static Holder<MobEffect>[] getEffects(String list) {
      list = list.toLowerCase();
      return !effectLists.containsKey(list) ? new Holder<MobEffect>[0] : effectLists.get(list).toArray(new Holder<MobEffect>[effectLists.get(list).size()]);
   }

   public static boolean inItemList(String list, ItemStack testStack) {
      list = list.toLowerCase();
      if (testStack.isEmpty()) {
         return false;
      } else {
         testStack.getItem();
         if (!itemLists.containsKey(list)) {
            return false;
         } else {
            for (ItemStack listStack : itemLists.get(list)) {
               if (testStack.getItem() == listStack.getItem() && testStack.getDamageValue() == listStack.getDamageValue()) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   public static boolean inEntityList(String list, Class testClass) {
      list = list.toLowerCase();
      return !entityLists.containsKey(list) ? false : false;
   }

   public static boolean inEffectList(String list, Holder<MobEffect> testPotion) {
      list = list.toLowerCase();
      return !effectLists.containsKey(list) ? false : effectLists.get(list).contains(testPotion);
   }

   public static boolean isInOreDictionary(String oreEntry, Item item) {
      return isInOreDictionary(oreEntry, new ItemStack(item));
   }

   public static boolean isInOreDictionary(String oreEntry, Block block) {
      return isInOreDictionary(oreEntry, new ItemStack(block));
   }

   public static boolean isInOreDictionary(String oreEntry, ItemStack itemStack) {
      if (itemStack == null) {
         return false;
      } else {
         List<ItemStack> ores = OreDictionary.getOres(oreEntry);
         if (ores == null) {
            return false;
         } else {
            for (ItemStack ore : ores) {
               if (ore.getItem() == itemStack.getItem()) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   public static void createCustomItems() {
      ConfigBase config = ConfigBase.getConfig(LycanitesMobs.modInfo, "itemlists");
      config.setCategoryComment(
         "Custom Objects",
         "here you can add your own custom items! These items wont do anything and will have no crafting recipes you can however have them drop from mobs by adding them to the custom mob drops. To add items just create a comma seperated list of names (spaces will be removed), you will need to use the item.youritem.name and item.youritem.description lang file entries in your resource pack to name your item and you will also need to save a texture for your item as: assets/lycanitesmobs/items/youritem.png"
      );
      String customItems = config.getString("Custom Objects", "Custom Items", "");
      if (!"".equals(customItems)) {
         for (String itemEntry : customItems.replace(" ", "").split(",")) {
            ObjectManager.addItem(itemEntry, new ItemCustom(itemEntry, LycanitesMobs.modInfo));
         }
      }
   }

   public static void createVanillaLists() {
      addItem("diet_carnivore", Items.BEEF);
      addItem("diet_carnivore", Items.PORKCHOP);
      addItem("diet_carnivore", Items.CHICKEN);
      addItem("diet_carnivore", Items.RABBIT);
      addItem("diet_carnivore", Items.MUTTON);
      addItem("diet_carnivore", Items.COOKED_BEEF);
      addItem("diet_carnivore", Items.COOKED_PORKCHOP);
      addItem("diet_carnivore", Items.COOKED_CHICKEN);
      addItem("diet_carnivore", Items.COOKED_RABBIT);
      addItem("diet_carnivore", Items.COOKED_MUTTON);
      addItem("diet_piscivore", Items.COD);
      addItem("diet_piscivore", new ItemStack(Items.COD, 1, 1));
      addItem("diet_piscivore", new ItemStack(Items.COD, 1, 2));
      addItem("diet_piscivore", new ItemStack(Items.COD, 1, 3));
      addItem("diet_piscivore", Items.COOKED_COD);
      addItem("diet_piscivore", new ItemStack(Items.COOKED_COD, 1, 1));
      addItem("diet_herbivore", Items.WHEAT);
      addItem("diet_herbivore", Items.CARROT);
      addItem("diet_herbivore", Items.POTATO);
      addItem("diet_frugivore", Items.APPLE);
      addItem("diet_frugivore", Items.MELON_SLICE);
      addItem("diet_frugivore", Blocks.PUMPKIN);
      addItem("diet_frugivore", Items.SUGAR);
      addItem("diet_frugivore", new ItemStack(Items.INK_SAC, 1, 15));
      addItem("diet_frugivore", Items.COOKIE);
      addItem("diet_frugivore", Blocks.CAKE);
      addItem("diet_frugivore", Items.PUMPKIN_PIE);
      addItem("diet_exudativore", new ItemStack(Items.INK_SAC, 1, 2));
      addItem("diet_fungivore", Items.MUSHROOM_STEW);
      addItem("diet_fungivore", Blocks.RED_MUSHROOM);
      addItem("diet_fungivore", Blocks.BROWN_MUSHROOM);
      addItem("diet_fungivore", Blocks.NETHER_WART);
      addItem("diet_detritivore", Items.ROTTEN_FLESH);
      addItem("diet_detritivore", Items.FERMENTED_SPIDER_EYE);
      addItem("diet_geovore", Items.COAL);
      addItem("diet_geovore", new ItemStack(Items.INK_SAC, 1, 4));
      addItem("diet_geovore", Items.DIAMOND);
      addItem("diet_geovore", Items.EMERALD);
      addItem("diet_insectivore", Items.SPIDER_EYE);
      addItem("diet_insectivore", Items.FERMENTED_SPIDER_EYE);

      for (String itemListName : itemListNames) {
         addFromConfig(itemListName.toLowerCase());
      }

      addEffect("buffs", MobEffects.DAMAGE_BOOST, "strength");
      addEffect("buffs", MobEffects.DIG_SPEED, "haste");
      addEffect("buffs", MobEffects.FIRE_RESISTANCE, "fire_resistance");
      addEffect("buffs", MobEffects.HEAL, "instant_health");
      addEffect("buffs", MobEffects.INVISIBILITY, "invisibility");
      addEffect("buffs", MobEffects.JUMP, "jump_boost");
      addEffect("buffs", MobEffects.MOVEMENT_SPEED, "speed");
      addEffect("buffs", MobEffects.NIGHT_VISION, "night_vision");
      addEffect("buffs", MobEffects.REGENERATION, "regeneration");
      addEffect("buffs", MobEffects.DAMAGE_RESISTANCE, "resistance");
      addEffect("buffs", MobEffects.WATER_BREATHING, "water_breathing");
      addEffect("buffs", MobEffects.HEALTH_BOOST, "health_boost");
      addEffect("buffs", MobEffects.ABSORPTION, "absorption");
      addEffect("buffs", MobEffects.SATURATION, "saturation");
      addEffect("buffs", MobEffects.GLOWING, "glowing");
      addEffect("buffs", MobEffects.LEVITATION, "levitation");
      addEffect("buffs", MobEffects.LUCK, "luck");
      addEffect("debuffs", MobEffects.BLINDNESS, "blindness");
      addEffect("debuffs", MobEffects.CONFUSION, "nausea");
      addEffect("debuffs", MobEffects.DIG_SLOWDOWN, "mining_fatigue");
      addEffect("debuffs", MobEffects.HARM, "instant_damage");
      addEffect("debuffs", MobEffects.HUNGER, "hunger");
      addEffect("debuffs", MobEffects.MOVEMENT_SLOWDOWN, "slowness");
      addEffect("debuffs", MobEffects.POISON, "poison");
      addEffect("debuffs", MobEffects.WEAKNESS, "weakness");
      addEffect("debuffs", MobEffects.WITHER, "wither");
      addEffect("debuffs", MobEffects.UNLUCK, "unluck");
   }

   public static void addFromConfig(String listName) {
      ConfigBase config = ConfigBase.getConfig(LycanitesMobs.modInfo, "itemlists");
      config.setCategoryComment(
         "item lists",
         "Here you can add items from vanilla Minecraft or other mods to various lists used by this mod. These are mostly food items that can be fed to farmable/tameable mobs. Format is: mod:itemname,metadata Multiple entries should be semicolon separated, be sure to use a colon and semicolon in the correct place."
      );
      String customDropsString = config.getString("Item Lists", listName).replace(" ", "");
      LycanitesMobs.logDebug("ItemSetup", "~O========== Custom " + listName + " ==========O~");
      if (customDropsString != null && customDropsString.length() > 0) {
         for (String customDropEntryString : customDropsString.replace(" ", "").split(";")) {
            LycanitesMobs.logDebug("ItemSetup", "Adding: " + customDropEntryString);
            String[] customDropValues = customDropEntryString.split(",");
            String dropName = customDropValues[0];
            int dropMeta = 0;
            if (customDropValues.length > 1) {
               dropMeta = Integer.parseInt(customDropValues[1]);
            }

            if (Item.getByNameOrId(dropName) != null) {
               Item customItem = Item.getByNameOrId(dropName);
               addItem(listName, new ItemStack(customItem, 1, dropMeta));
               LycanitesMobs.logDebug("ItemSetup", "As Item: " + customItem);
            } else if (Block.getBlockFromName(dropName) != null) {
               Block customBlock = Block.getBlockFromName(dropName);
               addItem(listName, new ItemStack(customBlock, 1, dropMeta));
               LycanitesMobs.logDebug("ItemSetup", "As Block: " + customBlock);
            }
         }
      }
   }

   public static boolean isPickaxe(ItemStack itemStack) {
      if (itemStack.isEmpty()) {
         return false;
      } else {
         try {
            String[] toolNameParts = itemStack.getDescriptionId().split("\\.");
            if (toolNameParts.length >= 3 && "InfiTool".equalsIgnoreCase(toolNameParts[1])) {
               String toolName = toolNameParts[2];
               return "Pickaxe".equalsIgnoreCase(toolName) || "Hammer".equalsIgnoreCase(toolName);
            } else if (itemStack.getItem() instanceof ItemPickaxe) {
               return true;
            } else {
               return itemStack.getItem().getHarvestLevel(itemStack, "pickaxe", null, null) != -1
                  ? true
                  : itemStack.getDestroySpeed(Blocks.STONE.defaultBlockState()) > 1.0F;
            }
         } catch (Exception var3) {
            return false;
         }
      }
   }

   public static boolean isAxe(ItemStack itemStack) {
      if (itemStack.isEmpty()) {
         return false;
      } else {
         try {
            String[] toolNameParts = itemStack.getDescriptionId().split("\\.");

            for (String toolNamePart : toolNameParts) {
               if (toolNameParts.length >= 3 && "InfiTool".equalsIgnoreCase(toolNameParts[1])) {
                  String toolName = toolNameParts[2];
                  if (!"Axe".equalsIgnoreCase(toolName)
                     && !"LumberAxe".equalsIgnoreCase(toolName)
                     && !"Mattock".equalsIgnoreCase(toolName)
                     && !"Battleaxe".equalsIgnoreCase(toolName)) {
                     return false;
                  }

                  return true;
               }
            }

            if (itemStack.getItem() instanceof ItemAxe) {
               return true;
            } else {
               return itemStack.getItem().getHarvestLevel(itemStack, "axe", null, null) != -1
                  ? true
                  : itemStack.getDestroySpeed(Blocks.OAK_LOG.defaultBlockState()) > 1.0F;
            }
         } catch (Exception var7) {
            return false;
         }
      }
   }

   public static boolean isShovel(ItemStack itemStack) {
      if (itemStack.isEmpty()) {
         return false;
      } else {
         try {
            String[] toolNameParts = itemStack.getDescriptionId().split("\\.");
            if (toolNameParts.length >= 3 && "InfiTool".equalsIgnoreCase(toolNameParts[1])) {
               String toolName = toolNameParts[2];
               return "Shovel".equalsIgnoreCase(toolName) || "Excavator".equalsIgnoreCase(toolName) || "Mattock".equalsIgnoreCase(toolName);
            } else if (itemStack.getItem() instanceof ItemSpade) {
               return true;
            } else {
               return itemStack.getItem().getHarvestLevel(itemStack, "shovel", null, null) != -1
                  ? true
                  : itemStack.getDestroySpeed(Blocks.DIRT.defaultBlockState()) > 1.0F;
            }
         } catch (Exception var3) {
            return false;
         }
      }
   }

   public static boolean isName(Item item, String name) {
      if (item == null) {
         return false;
      } else {
         String itemName = item.getDescriptionId().toLowerCase();
         return itemName.contains(name);
      }
   }

   public static boolean isName(Block block, String name) {
      if (block == null) {
         return false;
      } else {
         name = name.toLowerCase();
         String blockName = block.getDescriptionId().toLowerCase();
         return blockName.contains(name);
      }
   }
}
