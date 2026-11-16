package com.lycanitesmobs;

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
import com.lycanitesmobs.core.block.BlockSlabCustom;
import com.lycanitesmobs.core.config.ConfigBase;
import com.lycanitesmobs.core.info.ModInfo;
import com.lycanitesmobs.core.info.ObjectLists;
import com.lycanitesmobs.core.item.GenericFoodItem;
import com.lycanitesmobs.core.item.ItemBlockBase;
import com.lycanitesmobs.core.item.ItemSlabCustom;
import java.util.HashMap;
import java.util.Map;


public class ObjectManager {
   public static Map<String, Block> blocks = new HashMap<>();
   public static Map<String, Item> items = new HashMap<>();
   public static Map<Item, ModInfo> itemGroups = new HashMap<>();
   public static Map<String, Fluid> fluids = new HashMap<>();
   public static Map<Block, Item> buckets = new HashMap<>();
   public static Map<String, Class> tileEntities = new HashMap<>();
   public static Map<String, PotionBase> effects = new HashMap<>();
   public static Map<String, Class<? extends Entity>> specialEntities = new HashMap<>();
   public static Map<String, DamageSource> damageSources = new HashMap<>();
   public static Map<String, StatBase> stats = new HashMap<>();
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
      if (block instanceof BlockSlabCustom) {
         BlockSlabCustom blockSlab = (BlockSlabCustom)block;
         ItemSlabCustom itemSlabCustom = new ItemSlabCustom(blockSlab, blockSlab, blockSlab.getDoubleBlock());
         items.put(name, itemSlabCustom);
         itemGroups.put(itemSlabCustom, currentModInfo);
      } else {
         BlockItem itemBlock = new ItemBlockBase(block);
         LycReg.setName(itemBlock, LycReg.key(block));
         items.put(name, itemBlock);
         itemGroups.put(itemBlock, currentModInfo);
      }

      return block;
   }

   public static Fluid addFluid(String fluidName) {
      ModInfo group = currentModInfo;
      Fluid fluid = new Fluid(
         fluidName, ResourceLocation.parse(group.modid + ":blocks/" + fluidName + "_still"), ResourceLocation.parse(group.modid + ":blocks/" + fluidName + "_flow")
      );
      fluids.put(fluidName, fluid);
      if (!FluidRegistry.registerFluid(fluid)) {
         LycanitesMobs.logWarning("", "Another fluid was registered as " + fluidName);
      }

      return fluid;
   }

   public static Item addBucket(Item bucket, Block block, Fluid fluid) {
      buckets.put(block, bucket);
      return bucket;
   }

   public static Item addItem(String name, Item item) {
      name = name.toLowerCase();
      items.put(name, item);
      itemGroups.put(item, currentModInfo);
      if (item instanceof ItemBucket) {
         IBehaviorDispenseItem ibehaviordispenseitem = new BehaviorDefaultDispenseItem() {
            private final BehaviorDefaultDispenseItem dispenseBehavior = new BehaviorDefaultDispenseItem();

            public ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
               ItemBucket itembucket = (ItemBucket)stack.getItem();
               BlockPos blockpos = source.getBlockPos().offset((Direction)source.getBlockState().getValue(BlockDispenser.FACING));
               return itembucket.tryPlaceContainedLiquid(null, source.getWorld(), blockpos)
                  ? new ItemStack(Items.BUCKET)
                  : this.dispenseBehavior.dispense(source, stack);
            }
         };
         BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(item, ibehaviordispenseitem);
      }

      return item;
   }

   public static Item addItem(String name, Item item, int weight, int minAmount, int maxAmount) {
      Utilities.addDungeonLoot(new ItemStack(item), minAmount, maxAmount, weight);
      Utilities.addStrongholdLoot(new ItemStack(item), minAmount, maxAmount, weight * 2);
      Utilities.addVillageLoot(new ItemStack(item), minAmount, maxAmount, weight);
      return addItem(name, item);
   }

   public static Class addTileEntity(String name, Class tileEntityClass) {
      name = name.toLowerCase();
      tileEntities.put(name, tileEntityClass);
      GameRegistry.registerTileEntity(tileEntityClass, "lycanitesmobs." + name);
      return tileEntityClass;
   }

   public static PotionBase addEffect(String name, ConfigBase config, boolean isBad, int color, boolean goodEffect) {
      if (!config.getBool("Effects", name + " enabled", true, "Set to false to disable this potion effect.")) {
         return null;
      } else {
         PotionBase effect = new PotionBase(name, isBad, color);
         effects.put(name, effect);
         ObjectLists.addEffect(goodEffect ? "buffs" : "debuffs", effect, name);
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

   public static void addStat(String name, StatBase stat) {
      name = name.toLowerCase();
      if (!stats.containsKey(name)) {
         stat.registerStat();
         stats.put(name, stat);
      }
   }

   public static Block getBlock(String name) {
      name = name.toLowerCase();
      return !blocks.containsKey(name) ? null : blocks.get(name);
   }

   public static Item getItem(String name) {
      name = name.toLowerCase();
      return !items.containsKey(name) ? null : items.get(name);
   }

   public static Class getTileEntity(String name) {
      name = name.toLowerCase();
      return !tileEntities.containsKey(name) ? null : tileEntities.get(name);
   }

   public static PotionBase getEffect(String name) {
      name = name.toLowerCase();
      return !effects.containsKey(name) ? null : effects.get(name);
   }

   public static DamageSource getDamageSource(String name) {
      name = name.toLowerCase();
      return !damageSources.containsKey(name) ? null : damageSources.get(name);
   }

   public static StatBase getStat(String name) {
      name = name.toLowerCase();
      return !stats.containsKey(name) ? null : stats.get(name);
   }

   public static void registerBlocks(Register<Block> event) {
      event.getRegistry().registerAll(blocks.values().toArray(new Block[blocks.size()]));

      for (Block block : blocks.values()) {
         if (LycReg.key(block) == null) {
            LycanitesMobs.logWarning("", "Block: " + block + " has no Registry Name!");
         }

         LycanitesMobs.proxy.addBlockRender(currentModInfo, block);
      }
   }

   public static void registerItems(Register<Item> event) {
      for (Item item : items.values()) {
         if (LycReg.key(item) == null) {
            LycanitesMobs.logWarning("", "Item: " + item + " has no Registry Name!");
         }

         event.getRegistry().register(item);
         LycanitesMobs.proxy.addItemRender(itemGroups.get(item), item);
         if (item instanceof GenericFoodItem) {
            String itemName = ((GenericFoodItem)item).itemName;
            if (item.getDescriptionId().contains("cooked_")) {
               OreDictionary.registerOre("listAllmeatcooked", getItem(itemName));
            } else if (itemName.contains("raw_")) {
               OreDictionary.registerOre("listAllmeatraw", getItem(itemName));
            }
         }
      }
   }

   public static void registerPotions(Register<Potion> event) {
      for (PotionBase potion : effects.values()) {
         event.getRegistry().register(potion);
      }
   }

   public static void registerSpecialEntities(Register<EntityEntry> event) {
      for (String entityName : specialEntities.keySet()) {
         String registryName = LycanitesMobs.modInfo.modid + ":" + entityName;
         EntityEntry entityEntry = EntityEntryBuilder.create()
            .entity(specialEntities.get(entityName))
            .id(registryName, getNextSpecialEntityNetworkId())
            .name(entityName)
            .tracker(80, 3, false)
            .build();
         event.getRegistry().register(entityEntry);
      }
   }
}
