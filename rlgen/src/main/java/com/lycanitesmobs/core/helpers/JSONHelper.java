package com.lycanitesmobs.core.helpers;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;


public class JSONHelper {
   public static Vec3i getVec3i(JsonObject json, String memberName) {
      if (!json.has(memberName)) {
         return new Vec3i(0, 0, 0);
      } else {
         JsonArray jsonArray = json.get(memberName).getAsJsonArray();
         Iterator<JsonElement> jsonIterator = jsonArray.iterator();
         int[] coords = new int[3];

         for (int i = 0; jsonIterator.hasNext() && i < coords.length; i++) {
            coords[i] = jsonIterator.next().getAsInt();
         }

         return new Vec3i(coords[0], coords[1], coords[2]);
      }
   }

   public static Vec3 getVec3d(JsonObject json, String memberName, Vec3 defaultVec) {
      if (!json.has(memberName)) {
         return defaultVec;
      } else {
         JsonArray jsonArray = json.get(memberName).getAsJsonArray();
         Iterator<JsonElement> jsonIterator = jsonArray.iterator();
         double[] coords = new double[3];

         for (int i = 0; jsonIterator.hasNext() && i < coords.length; i++) {
            coords[i] = jsonIterator.next().getAsDouble();
         }

         return new Vec3(coords[0], coords[1], coords[2]);
      }
   }

   public static List<String> getJsonStrings(JsonArray jsonArray) {
      List<String> strings = new ArrayList<>();
      Iterator<JsonElement> jsonIterator = jsonArray.iterator();

      while (jsonIterator.hasNext()) {
         String string = jsonIterator.next().getAsString();
         strings.add(string);
      }

      return strings;
   }

   public static List<Block> getJsonBlocks(JsonObject json) {
      List<Block> blocks = new ArrayList<>();
      if (json.has("blocks")) {
         blocks = getJsonBlocks(json.get("blocks").getAsJsonArray());
      }

      return blocks;
   }

   public static List<Block> getJsonBlocks(JsonArray jsonArray) {
      List<Block> blocks = new ArrayList<>();
      Iterator<JsonElement> jsonIterator = jsonArray.iterator();

      while (jsonIterator.hasNext()) {
         Block block = (Block)GameRegistry.findRegistry(Block.class).getValue(ResourceLocation.parse(jsonIterator.next().getAsString()));
         if (block != null) {
            blocks.add(block);
         }
      }

      return blocks;
   }

   public static List<Item> getJsonItems(JsonArray jsonArray) {
      List<Item> items = new ArrayList<>();
      Iterator<JsonElement> jsonIterator = jsonArray.iterator();

      while (jsonIterator.hasNext()) {
         Item item = (Item)GameRegistry.findRegistry(Item.class).getValue(ResourceLocation.parse(jsonIterator.next().getAsString()));
         if (item != null) {
            items.add(item);
         }
      }

      return items;
   }

   public static List<Material> getJsonMaterials(JsonObject json) {
      List<Material> materials = new ArrayList<>();
      if (json.has("materials")) {
         JsonArray jsonArray = json.get("materials").getAsJsonArray();
         Iterator<JsonElement> jsonIterator = jsonArray.iterator();

         while (jsonIterator.hasNext()) {
            Material material = null;
            String materialName = jsonIterator.next().getAsString();
            if ("air".equalsIgnoreCase(materialName)) {
               material = Material.AIR;
            } else if ("lava".equalsIgnoreCase(materialName)) {
               material = Material.LAVA;
            } else if ("fire".equalsIgnoreCase(materialName)) {
               material = Material.FIRE;
            } else if ("water".equalsIgnoreCase(materialName)) {
               material = Material.WATER;
            } else if ("ground".equalsIgnoreCase(materialName)) {
               material = Material.GROUND;
            } else if ("sand".equalsIgnoreCase(materialName)) {
               material = Material.SAND;
            } else if ("claw".equalsIgnoreCase(materialName)) {
               material = Material.CLAY;
            } else if ("wood".equalsIgnoreCase(materialName)) {
               material = Material.WOOD;
            } else if ("rock".equalsIgnoreCase(materialName)) {
               material = Material.ROCK;
            } else if ("grass".equalsIgnoreCase(materialName)) {
               material = Material.GRASS;
            } else if ("plants".equalsIgnoreCase(materialName)) {
               material = Material.PLANTS;
            } else if ("leaves".equalsIgnoreCase(materialName)) {
               material = Material.LEAVES;
            } else if ("vine".equalsIgnoreCase(materialName)) {
               material = Material.VINE;
            } else if ("cactus".equalsIgnoreCase(materialName)) {
               material = Material.CACTUS;
            } else if ("snow".equalsIgnoreCase(materialName)) {
               material = Material.SNOW;
            } else if ("ice".equalsIgnoreCase(materialName)) {
               material = Material.ICE;
            } else if ("iron".equalsIgnoreCase(materialName)) {
               material = Material.IRON;
            } else if ("web".equalsIgnoreCase(materialName)) {
               material = Material.WEB;
            }

            if (material != null) {
               materials.add(material);
            }
         }
      }

      return materials;
   }

   public static List<Biome> getBiomesFromTags(List<String> biomeTags) {
      List<Biome> biomeList = new ArrayList<>();

      for (String biomeEntry : biomeTags) {
         boolean additive = true;
         if (biomeEntry.charAt(0) == '-' || biomeEntry.charAt(0) == '+') {
            if (biomeEntry.charAt(0) == '-') {
               additive = false;
            }

            biomeEntry = biomeEntry.substring(1);
         }

         List<Biome> selectedBiomes = new ArrayList<>();
         if ("ALL".equalsIgnoreCase(biomeEntry)) {
            for (Type biomeType : Type.getAll()) {
               Set<Biome> selectedBiomesSet = BiomeDictionary.getBiomes(biomeType);
               selectedBiomes.addAll(selectedBiomesSet);
            }
         } else if (!"NONE".equalsIgnoreCase(biomeEntry)) {
            Type biomeType = null;

            try {
               biomeType = Type.getType(biomeEntry.toUpperCase(Locale.ENGLISH), new Type[0]);
            } catch (Exception var9) {
               LycanitesMobs.logWarning("", "[Spawning] Unknown biome type " + biomeEntry + " this will be ignored and treated as NONE.");
            }

            if (biomeType != null) {
               Set<Biome> selectedBiomesSet = BiomeDictionary.getBiomes(biomeType);
               selectedBiomes.addAll(selectedBiomesSet);
            }
         }

         for (Biome biome : selectedBiomes) {
            if (additive) {
               if (!biomeList.contains(biome)) {
                  biomeList.add(biome);
               }
            } else {
               biomeList.remove(biome);
            }
         }
      }

      return biomeList;
   }

   public static List<Biome> getBiomes(List<String> biomeIds) {
      List<Biome> biomes = new ArrayList<>();

      for (String biomeId : biomeIds) {
         Biome biome = (Biome)GameRegistry.findRegistry(Biome.class).getValue(ResourceLocation.parse(biomeId));
         if (biome != null) {
            biomes.add(biome);
         }
      }

      return biomes;
   }
}
