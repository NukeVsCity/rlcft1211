package com.lycanitesmobs;

import com.lycanitesmobs.compat.CreativeTabs;
import com.lycanitesmobs.compat.ForgeFluid;
import com.lycanitesmobs.compat.LycReg;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.core.config.ConfigBase;
import com.lycanitesmobs.core.info.ModInfo;
import com.lycanitesmobs.core.info.ObjectLists;
import com.lycanitesmobs.core.item.ItemBlockBase;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * 1.12 ObjectManager: the maps of everything Lycanites creates in preInit, registered when each 1.21 registry opens
 * ({@link #onRegister}; 1.12 registered them in the matching RegistryEvent.Register). Registry names are the map keys.
 */
public class ObjectManager {
   public static Map<String, Block> blocks = new LinkedHashMap<>();
   public static Map<String, Item> items = new LinkedHashMap<>();
   public static Map<Item, ModInfo> itemGroups = new HashMap<>();
   public static Map<String, ForgeFluid> fluids = new LinkedHashMap<>();
   public static Map<Block, Item> buckets = new HashMap<>();
   public static Map<String, Class> tileEntities = new LinkedHashMap<>();
   public static Map<String, BlockEntityType<?>> tileEntityTypes = new HashMap<>();
   public static Map<String, PotionBase> effects = new LinkedHashMap<>();
   public static Map<String, Holder<MobEffect>> effectHolders = new HashMap<>();
   public static Map<String, Class<? extends Entity>> specialEntities = new LinkedHashMap<>();
   public static Map<String, DamageSource> damageSources = new HashMap<>();
   public static Map<String, Object> stats = new HashMap<>();
   public static ModInfo currentModInfo;
   protected static int nextSpecialEntityNetworkId = 0;

   public static void setCurrentModInfo(ModInfo group) {
      currentModInfo = group;
   }

   public static int getNextSpecialEntityNetworkId() {
      return nextSpecialEntityNetworkId++;
   }

   public static Block addBlock(String name, Block block) {
      name = name.toLowerCase();
      blocks.put(name, block);
      LycReg.setName(block, "lycanitesmobs", name);
      Item itemBlock = block instanceof com.lycanitesmobs.core.block.BlockFluidBase ? null : new ItemBlockBase(block);
      if (itemBlock != null) {
         LycReg.setName(itemBlock, "lycanitesmobs", name);
         items.put(name, itemBlock);
         itemGroups.put(itemBlock, currentModInfo);
      }

      return block;
   }

   /** 1.12 addFluid: a Forge fluid with the still/flow textures blocks/<name>_still/_flow. */
   public static ForgeFluid addFluid(String fluidName) {
      ModInfo group = currentModInfo;
      ForgeFluid fluid = new ForgeFluid(
         fluidName,
         ResourceLocation.fromNamespaceAndPath(group.modid, "blocks/" + fluidName + "_still"),
         ResourceLocation.fromNamespaceAndPath(group.modid, "blocks/" + fluidName + "_flow")
      );
      if (fluids.containsKey(fluidName)) {
         LycanitesMobs.logWarning("", "Another fluid was registered as " + fluidName);
      }
      fluids.put(fluidName, fluid);
      return fluid;
   }

   public static Item addBucket(Item bucket, Block block, ForgeFluid fluid) {
      buckets.put(block, bucket);
      return bucket;
   }

   public static Item addItem(String name, Item item) {
      name = name.toLowerCase();
      items.put(name, item);
      LycReg.setName(item, "lycanitesmobs", name);
      itemGroups.put(item, currentModInfo);
      return item;
   }

   public static Item addItem(String name, Item item, int weight, int minAmount, int maxAmount) {
      // 1.12 also added the item to the dungeon, stronghold and village chest loot (Utilities.addDungeonLoot...): not used by
      // anything Lycanites registers in 2.0.8.9.
      return addItem(name, item);
   }

   public static Class addTileEntity(String name, Class tileEntityClass) {
      name = name.toLowerCase();
      tileEntities.put(name, tileEntityClass);
      return tileEntityClass;
   }

   public static PotionBase addEffect(String name, ConfigBase config, boolean isBad, int color, boolean goodEffect) {
      if (!config.getBool("Effects", name + " enabled", true, "Set to false to disable this potion effect.")) {
         return null;
      } else {
         PotionBase effect = new PotionBase(name, isBad, color);
         effects.put(name, effect);
         // the effect's registry holder (registered at RegisterEvent)
         ObjectLists.addEffect(goodEffect ? "buffs" : "debuffs",
            net.neoforged.neoforge.registries.DeferredHolder.create(net.minecraft.core.registries.Registries.MOB_EFFECT,
               ResourceLocation.fromNamespaceAndPath(currentModInfo != null ? currentModInfo.modid : "lycanitesmobs", name.toLowerCase())), name);
         return effect;
      }
   }

   public static void addSpecialEntity(String name, Class<? extends Entity> entityClass) {
      specialEntities.put(name, entityClass);
   }

   public static void addDamageSource(String name, DamageSource damageSource) {
      name = name.toLowerCase();
      damageSources.put(name, damageSource);
   }

   /** 1.12 addStat(name, StatBase): Lycanites kill/learn statistics are not registered in 1.21 (statistics screen only). */
   public static void addStat(String name, Object stat) {
      name = name.toLowerCase();
      if (!stats.containsKey(name)) {
         stats.put(name, stat);
      }
   }

   public static Block getBlock(String name) {
      name = name.toLowerCase();
      return blocks.get(name);
   }

   public static Item getItem(String name) {
      name = name.toLowerCase();
      return items.get(name);
   }

   public static Class getTileEntity(String name) {
      name = name.toLowerCase();
      return tileEntities.get(name);
   }

   public static BlockEntityType<?> getTileEntityType(String name) {
      return tileEntityTypes.get(name.toLowerCase());
   }

   public static BlockEntityType<?> getTileEntityType(Class tileEntityClass) {
      for (Map.Entry<String, Class> e : tileEntities.entrySet()) {
         if (e.getValue() == tileEntityClass) {
            return tileEntityTypes.get(e.getKey());
         }
      }
      return null;
   }

   /** 1.12 getEffect returned the Potion; 1.21 effects are used through their registry holder. */
   public static Holder<MobEffect> getEffect(String name) {
      name = name.toLowerCase();
      return effectHolders.get(name);
   }

   public static PotionBase getEffectObject(String name) {
      return effects.get(name.toLowerCase());
   }

   public static DamageSource getDamageSource(String name) {
      name = name.toLowerCase();
      return damageSources.get(name);
   }

   public static Object getStat(String name) {
      name = name.toLowerCase();
      return stats.get(name);
   }

   /** Called for every RegisterEvent after preInit (LycEntities handles ENTITY_TYPE). */
   @SuppressWarnings({"unchecked", "rawtypes"})
   public static void onRegister(RegisterEvent event) {
      CreativeTabs.register(event);
      event.register(Registries.SOUND_EVENT, helper -> {
         for (SoundEvent sound : AssetManager.sounds.values()) {
            helper.register(sound.getLocation(), sound);
         }
         AssetManager.soundsLocked = true;
      });
      event.register(Registries.MOB_EFFECT, helper -> {
         for (Map.Entry<String, PotionBase> e : effects.entrySet()) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(LycanitesMobs.modid, e.getKey().toLowerCase());
            helper.register(id, e.getValue());
         }
      });
      if (event.getRegistryKey() == Registries.MOB_EFFECT) {
         Registry<MobEffect> registry = (Registry<MobEffect>)event.getRegistry();
         for (Map.Entry<String, PotionBase> e : effects.entrySet()) {
            effectHolders.put(e.getKey().toLowerCase(), registry.wrapAsHolder(e.getValue()));
         }
      }
      event.register(NeoForgeRegistries.Keys.FLUID_TYPES, helper -> {
         for (ForgeFluid fluid : fluids.values()) {
            fluid.fluidType = createFluidType(fluid);
            helper.register(ResourceLocation.fromNamespaceAndPath(LycanitesMobs.modid, fluid.fluidName), fluid.fluidType);
         }
      });
      event.register(Registries.FLUID, helper -> {
         for (ForgeFluid fluid : fluids.values()) {
            Item bucket = buckets.get(fluid.block);
            if (bucket != null) {
               fluid.bucket = bucket;
            }
            helper.register(ResourceLocation.fromNamespaceAndPath(LycanitesMobs.modid, fluid.fluidName), fluid.sourceFluid);
            helper.register(ResourceLocation.fromNamespaceAndPath(LycanitesMobs.modid, "flowing_" + fluid.fluidName), fluid.flowingFluid);
         }
      });
      event.register(Registries.BLOCK, helper -> {
         for (Map.Entry<String, Block> e : blocks.entrySet()) {
            helper.register(ResourceLocation.fromNamespaceAndPath(LycanitesMobs.modid, e.getKey()), e.getValue());
            LycanitesMobs.proxy.addBlockRender(currentModInfo, e.getValue());
         }
      });
      event.register(Registries.ITEM, helper -> {
         for (Map.Entry<String, Item> e : items.entrySet()) {
            helper.register(ResourceLocation.fromNamespaceAndPath(LycanitesMobs.modid, e.getKey()), e.getValue());
            LycanitesMobs.proxy.addItemRender(itemGroups.get(e.getValue()), e.getValue());
         }
      });
      event.register(Registries.BLOCK_ENTITY_TYPE, helper -> {
         for (Map.Entry<String, Class> e : tileEntities.entrySet()) {
            BlockEntityType<?> type = createBlockEntityType(e.getValue());
            tileEntityTypes.put(e.getKey(), type);
            helper.register(ResourceLocation.fromNamespaceAndPath(LycanitesMobs.modid, e.getKey()), type);
         }
      });
   }

   /** 1.12 Forge Fluid properties as a 1.21 FluidType (density/viscosity/temperature/luminosity, lava-like when hot). */
   private static FluidType createFluidType(ForgeFluid fluid) {
      boolean hot = fluid.temperature >= 1000;
      return new FluidType(FluidType.Properties.create()
         .descriptionId("block.lycanitesmobs." + fluid.fluidName)
         .density(fluid.density)
         .viscosity(fluid.viscosity)
         .temperature(fluid.temperature)
         .lightLevel(fluid.luminosity)
         .canSwim(!hot)
         .canDrown(!hot)
         .canExtinguish(!hot)
         .supportsBoating(!hot)
         .pathType(hot ? net.minecraft.world.level.pathfinder.PathType.LAVA : net.minecraft.world.level.pathfinder.PathType.WATER)
         .adjacentPathType(hot ? null : net.minecraft.world.level.pathfinder.PathType.WATER_BORDER));
   }

   /** BlockEntityType for a 1.12 tile entity class: blocks are those whose newBlockEntity creates this class. */
   @SuppressWarnings({"unchecked", "rawtypes"})
   private static BlockEntityType<?> createBlockEntityType(Class cls) {
      try {
         Constructor ctor = cls.getConstructor(net.minecraft.core.BlockPos.class, net.minecraft.world.level.block.state.BlockState.class);
         Block[] validBlocks = blocks.values().stream()
            .filter(b -> b instanceof com.lycanitesmobs.core.block.ITileEntityBlock teb && teb.getTileEntityClass() == cls)
            .toArray(Block[]::new);
         BlockEntityType<?>[] typeRef = new BlockEntityType<?>[1];
         typeRef[0] = BlockEntityType.Builder.of((pos, state) -> {
            com.lycanitesmobs.core.tileentity.TileEntityBase.CONSTRUCTING.set(typeRef[0]);
            try {
               return (BlockEntity)ctor.newInstance(pos, state);
            } catch (ReflectiveOperationException ex) {
               throw new RuntimeException(ex);
            } finally {
               com.lycanitesmobs.core.tileentity.TileEntityBase.CONSTRUCTING.remove();
            }
         }, validBlocks).build(null);
         return typeRef[0];
      } catch (NoSuchMethodException ex) {
         throw new RuntimeException("[LycanitesMobs] " + cls.getName() + " needs a (BlockPos, BlockState) constructor", ex);
      }
   }
}
