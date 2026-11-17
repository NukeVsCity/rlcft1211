package com.lycanitesmobs.core.config;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
import com.lycanitesmobs.LycEntities;
import com.lycanitesmobs.compat.*;
import com.lycanitesmobs.compat.BiomeDictionary.Type;
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
import com.lycanitesmobs.core.info.ModInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;


public class ConfigSpawning extends ConfigBase {
   public static ConfigSpawning getConfig(ModInfo group, String configName) {
      String configFileName = configName.toLowerCase();
      if (!"lycanitesmobs".equalsIgnoreCase(group.modid)) {
         configFileName = group.modid + "-" + configFileName;
      }

      if (!configs.containsKey(configFileName)) {
         registerConfig(new ConfigSpawning(group, configName, configFileName));
      }

      ConfigBase config = ConfigBase.configs.get(configFileName);
      if (config instanceof ConfigSpawning) {
         return (ConfigSpawning)config;
      } else {
         LycanitesMobs.logWarning("", "[Config] Tried to access the Base Config: " + configName + " as a Spawning Config from group: " + group.name + "!");
         return null;
      }
   }

   public ConfigSpawning(ModInfo group, String name, String filename) {
      super(group, name, filename);
   }

   public Type[] getAllBiomeTypes() {
      return new Type[]{
         Type.HOT,
         Type.COLD,
         Type.SPARSE,
         Type.DENSE,
         Type.WET,
         Type.DRY,
         Type.SAVANNA,
         Type.CONIFEROUS,
         Type.JUNGLE,
         Type.SPOOKY,
         Type.DEAD,
         Type.LUSH,
         Type.NETHER,
         Type.END,
         Type.MUSHROOM,
         Type.MAGICAL,
         Type.RARE,
         Type.OCEAN,
         Type.RIVER,
         Type.WATER,
         Type.MESA,
         Type.FOREST,
         Type.PLAINS,
         Type.MOUNTAIN,
         Type.HILLS,
         Type.SWAMP,
         Type.SANDY,
         Type.SNOWY,
         Type.WASTELAND,
         Type.BEACH,
         Type.VOID
      };
   }

   public ConfigSpawning.SpawnTypeSet getTypes(String category, String key) {
      return this.getTypes(category, key, "");
   }

   public ConfigSpawning.SpawnTypeSet getTypes(String category, String key, String defaultValue) {
      return this.getTypes(category, key, defaultValue, null);
   }

   public ConfigSpawning.SpawnTypeSet getTypes(String category, String key, String defaultValue, String comment) {
      String spawnTypeEntries = this.getString(category, key, defaultValue);
      spawnTypeEntries = spawnTypeEntries.replace(" ", "");
      String[] spawners = spawnTypeEntries.split(",");
      List<MobCategory> creatureTypeList = new ArrayList<>();

      for (String spawnTypeEntry : spawnTypeEntries.split(",")) {
         if ("MONSTER".equalsIgnoreCase(spawnTypeEntry)) {
            creatureTypeList.add(MobCategory.MONSTER);
         } else if ("CREATURE".equalsIgnoreCase(spawnTypeEntry) || "ANIMAL".equalsIgnoreCase(spawnTypeEntry)) {
            creatureTypeList.add(MobCategory.CREATURE);
         } else if ("WATERCREATURE".equalsIgnoreCase(spawnTypeEntry)) {
            creatureTypeList.add(MobCategory.WATER_CREATURE);
         } else if ("AMBIENT".equalsIgnoreCase(spawnTypeEntry)) {
            creatureTypeList.add(MobCategory.AMBIENT);
         }
      }

      MobCategory[] creatureTypes = creatureTypeList.toArray(new MobCategory[creatureTypeList.size()]);
      return new ConfigSpawning.SpawnTypeSet(spawners, creatureTypes);
   }

   public ConfigSpawning.SpawnDimensionSet getDimensions(String category, String key) {
      return this.getDimensions(category, key, "");
   }

   public ConfigSpawning.SpawnDimensionSet getDimensions(String category, String key, String defaultValue) {
      return this.getDimensions(category, key, defaultValue, null);
   }

   public ConfigSpawning.SpawnDimensionSet getDimensions(String category, String key, String defaultValue, String comment) {
      String dimensionEntries = this.getString(category, key, defaultValue);
      dimensionEntries = dimensionEntries.replace(" ", "");
      List<Integer> dimensionIDList = new ArrayList<>();
      List<String> dimensionTypeList = new ArrayList<>();

      for (String dimensionEntry : dimensionEntries.split(",")) {
         if (NumberUtils.isCreatable(dimensionEntry)) {
            dimensionIDList.add(Integer.parseInt(dimensionEntry.replace("+", "")));
         } else {
            dimensionTypeList.add(dimensionEntry.replace("+", ""));
         }
      }

      int[] dimensionIDs = ArrayUtils.toPrimitive(dimensionIDList.toArray(new Integer[dimensionIDList.size()]));
      String[] dimensionTypes = dimensionTypeList.toArray(new String[dimensionTypeList.size()]);
      return new ConfigSpawning.SpawnDimensionSet(dimensionIDs, dimensionTypes);
   }

   public ResourceLocation[] getBiomes(String category, String key) {
      return this.getBiomes(category, key, "");
   }

   public ResourceLocation[] getBiomes(String category, String key, String defaultValue) {
      return this.getBiomes(category, key, defaultValue, null);
   }

   public ResourceLocation[] getBiomes(String category, String key, String defaultValue, String comment) {
      String biomeEntries = this.getString(category, key, defaultValue, comment);
      biomeEntries = biomeEntries.replace(" ", "");
      List<ResourceLocation> biomeList = new ArrayList<>();

      for (String biomeEntry : biomeEntries.split(",")) {
         if (!"".equals(biomeEntry)) {
            boolean additive = true;
            if (biomeEntry.charAt(0) == '-' || biomeEntry.charAt(0) == '+') {
               if (biomeEntry.charAt(0) == '-') {
                  additive = false;
               }

               biomeEntry = biomeEntry.substring(1);
            }

            ResourceLocation[] selectedBiomes = null;
            if ("ALL".equals(biomeEntry)) {
               for (Type biomeType : this.getAllBiomeTypes()) {
                  if (selectedBiomes == null) {
                     Set<ResourceLocation> selectedBiomesSet = BiomeDictionary.getBiomes(biomeType);
                     selectedBiomes = selectedBiomesSet.toArray(new ResourceLocation[selectedBiomesSet.size()]);
                  } else {
                     Set<ResourceLocation> typeBiomesSet = BiomeDictionary.getBiomes(biomeType);
                     ResourceLocation[] typeBiomes = typeBiomesSet.toArray(new ResourceLocation[typeBiomesSet.size()]);
                     if (typeBiomes != null) {
                        selectedBiomes = (ResourceLocation[])ArrayUtils.addAll(selectedBiomes, typeBiomes);
                     }
                  }
               }
            } else if ("GROUP".equals(biomeEntry)) {
               selectedBiomes = this.group.biomes;
            } else if (!"NONE".equals(biomeEntry)) {
               Type biomeTypex;
               try {
                  biomeTypex = Type.getType(biomeEntry, new Type[0]);
               } catch (Exception var19) {
                  biomeTypex = null;
                  LycanitesMobs.logWarning(
                     "", "[Config] Unknown biome type " + biomeEntry + " specified for " + defaultValue + "this will be ignored and treated as NONE."
                  );
               }

               if (biomeTypex != null) {
                  Set<ResourceLocation> selectedBiomesSet = BiomeDictionary.getBiomes(biomeTypex);
                  selectedBiomes = selectedBiomesSet.toArray(new ResourceLocation[selectedBiomesSet.size()]);
               }
            }

            if (selectedBiomes != null) {
               for (ResourceLocation biome : selectedBiomes) {
                  if (additive && !biomeList.contains(biome)) {
                     biomeList.add(biome);
                  } else if (!additive && biomeList.contains(biome)) {
                     biomeList.remove(biome);
                  }
               }
            }
         }
      }

      return biomeList.toArray(new ResourceLocation[biomeList.size()]);
   }

   public Type[] getBiomeTypes(boolean additive, String category, String key) {
      return this.getBiomeTypes(additive, category, key, "");
   }

   public Type[] getBiomeTypes(boolean additive, String category, String key, String defaultValue) {
      return this.getBiomeTypes(additive, category, key, defaultValue, null);
   }

   public Type[] getBiomeTypes(boolean additive, String category, String key, String defaultValue, String comment) {
      String biomeEntries = this.getString(category, key, defaultValue, comment);
      biomeEntries = biomeEntries.replace(" ", "");
      List<Type> biomeTypeList = new ArrayList<>();

      for (String biomeEntry : biomeEntries.split(",")) {
         if (!"".equals(biomeEntry)) {
            if (biomeEntry.charAt(0) != '-' && biomeEntry.charAt(0) != '+') {
               if (!additive) {
                  continue;
               }
            } else {
               if (biomeEntry.charAt(0) == '-' && additive || biomeEntry.charAt(0) == '+' && !additive) {
                  continue;
               }

               biomeEntry = biomeEntry.substring(1);
            }

            if ("ALL".equals(biomeEntry)) {
               biomeTypeList.addAll(Arrays.asList(this.getAllBiomeTypes()));
            } else if ("GROUP".equals(biomeEntry)) {
               if (additive) {
                  biomeTypeList.addAll(Arrays.asList(this.group.biomeTypesAllowed));
               } else {
                  biomeTypeList.addAll(Arrays.asList(this.group.biomeTypesDenied));
               }
            } else if (!"NONE".equals(biomeEntry)) {
               Type biomeType;
               try {
                  biomeType = Type.getType(biomeEntry, new Type[0]);
               } catch (Exception var14) {
                  biomeType = null;
                  LycanitesMobs.logWarning(
                     "", "[Config] Unknown biome type " + biomeEntry + " specified for " + defaultValue + "this will be ignored and treated as NONE."
                  );
               }

               if (biomeType != null) {
                  biomeTypeList.add(biomeType);
               }
            }
         }
      }

      return biomeTypeList.toArray(new Type[biomeTypeList.size()]);
   }

   public class SpawnDimensionSet {
      public int[] dimensionIDs;
      public String[] dimensionTypes;

      public SpawnDimensionSet(int[] dimensionIDs, String[] dimensionTypes) {
         this.dimensionIDs = dimensionIDs;
         this.dimensionTypes = dimensionTypes;
      }
   }

   public class SpawnTypeSet {
      public String[] spawners;
      public MobCategory[] creatureTypes;

      public SpawnTypeSet(String[] spawners, MobCategory[] creatureTypes) {
         this.spawners = spawners;
         this.creatureTypes = creatureTypes;
      }
   }
}
