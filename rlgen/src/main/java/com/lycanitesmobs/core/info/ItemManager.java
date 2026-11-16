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
import com.google.gson.JsonObject;
import com.lycanitesmobs.BlockMaker;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.core.JSONLoader;
import com.lycanitesmobs.core.block.BlockEquipmentForge;
import com.lycanitesmobs.core.block.BlockFluidBase;
import com.lycanitesmobs.core.block.BlockSoulcube;
import com.lycanitesmobs.core.block.BlockSummoningPedestal;
import com.lycanitesmobs.core.block.EquipmentInfuserBlock;
import com.lycanitesmobs.core.block.EquipmentStationBlock;
import com.lycanitesmobs.core.block.building.BlockPropolis;
import com.lycanitesmobs.core.block.building.BlockVeswax;
import com.lycanitesmobs.core.block.effect.BlockDoomfire;
import com.lycanitesmobs.core.block.effect.BlockFrostCloud;
import com.lycanitesmobs.core.block.effect.BlockFrostfire;
import com.lycanitesmobs.core.block.effect.BlockFrostweb;
import com.lycanitesmobs.core.block.effect.BlockHellfire;
import com.lycanitesmobs.core.block.effect.BlockIcefire;
import com.lycanitesmobs.core.block.effect.BlockPoisonCloud;
import com.lycanitesmobs.core.block.effect.BlockPoopCloud;
import com.lycanitesmobs.core.block.effect.BlockPrimefire;
import com.lycanitesmobs.core.block.effect.BlockQuickWeb;
import com.lycanitesmobs.core.block.effect.BlockScorchfire;
import com.lycanitesmobs.core.block.effect.BlockShadowfire;
import com.lycanitesmobs.core.block.fluid.BlockFluidAcid;
import com.lycanitesmobs.core.block.fluid.BlockFluidMoglava;
import com.lycanitesmobs.core.block.fluid.BlockFluidOoze;
import com.lycanitesmobs.core.block.fluid.BlockFluidPoison;
import com.lycanitesmobs.core.block.fluid.BlockFluidRabbitooze;
import com.lycanitesmobs.core.block.fluid.BlockFluidSharacid;
import com.lycanitesmobs.core.block.fluid.BlockFluidVeshoney;
import com.lycanitesmobs.core.block.fluid.BlockFluidVesspoison;
import com.lycanitesmobs.core.item.ChargeItem;
import com.lycanitesmobs.core.item.GenericBucketItem;
import com.lycanitesmobs.core.item.ItemMobToken;
import com.lycanitesmobs.core.item.consumable.ItemCleansingCrystal;
import com.lycanitesmobs.core.item.consumable.ItemHalloweenTreat;
import com.lycanitesmobs.core.item.consumable.ItemImmunizer;
import com.lycanitesmobs.core.item.consumable.ItemWinterGift;
import com.lycanitesmobs.core.item.consumable.ItemWinterGiftLarge;
import com.lycanitesmobs.core.item.equipment.ItemEquipment;
import com.lycanitesmobs.core.item.special.ItemFrostyFur;
import com.lycanitesmobs.core.item.special.ItemGeistLiver;
import com.lycanitesmobs.core.item.special.ItemPoisonGland;
import com.lycanitesmobs.core.item.special.ItemSoulContract;
import com.lycanitesmobs.core.item.special.ItemSoulgazer;
import com.lycanitesmobs.core.item.special.ItemSoulkey;
import com.lycanitesmobs.core.item.special.ItemSoulstone;
import com.lycanitesmobs.core.item.special.ItemWraithSigil;
import com.lycanitesmobs.core.item.temp.ItemStaffBlood;
import com.lycanitesmobs.core.item.temp.ItemStaffSavage;
import com.lycanitesmobs.core.item.temp.ItemStaffStable;
import com.lycanitesmobs.core.item.temp.ItemStaffSturdy;
import com.lycanitesmobs.core.item.temp.ItemStaffSummoning;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ItemManager extends JSONLoader {
   public static ItemManager INSTANCE;
   public Map<String, ItemInfo> items = new HashMap<>();
   public List<ModInfo> loadedGroups = new ArrayList<>();
   public ItemConfig config;

   public static ItemManager getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new ItemManager();
      }

      return INSTANCE;
   }

   public void startup(ModInfo modInfo) {
      this.loadConfig();
      this.loadAllFromJson(modInfo);

      for (ItemInfo itemInfo : this.items.values()) {
         if (itemInfo.name.contains("raw_")) {
            String cookedName = itemInfo.name.replace("raw_", "cooked_");
            if (this.items.containsKey(cookedName)) {
               // 1.12 GameRegistry.addSmelting(itemInfo.item, new ItemStack(this.items.get(cookedName).item, 1), 0.5F): smelting recipes are data/lycanitesmobs/recipe JSON
            }
         }
      }

      this.loadItems();
   }

   public void loadAllFromJson(ModInfo modInfo) {
      if (!this.loadedGroups.contains(modInfo)) {
         this.loadedGroups.add(modInfo);
      }

      this.loadAllJson(modInfo, "Items", "items", "name", true);
      LycanitesMobs.logDebug("Items", "Complete! " + this.items.size() + " JSON Items Loaded In Total.");
   }

   @Override
   public void parseJson(ModInfo modInfo, String loadGroup, JsonObject json) {
      ItemInfo itemInfo = new ItemInfo(modInfo);
      itemInfo.loadFromJSON(json);
      this.items.put(itemInfo.name, itemInfo);
      ObjectManager.addItem(itemInfo.name, itemInfo.getItem());
   }

   public void loadConfig() {
      ItemConfig.loadGlobalSettings();
   }

   public void loadItems() {
      ModInfo group = LycanitesMobs.modInfo;
      ObjectManager.addItem("soulgazer", new ItemSoulgazer());
      ObjectManager.addItem("soulstone", new ItemSoulstone(group));
      ObjectManager.addItem("soul_contract", new ItemSoulContract());
      ObjectManager.addItem("equipment", new ItemEquipment());
      ObjectManager.addItem("mobtoken", new ItemMobToken(group));
      ObjectManager.addItem("soulkey", new ItemSoulkey("soulkey", 0));
      ObjectManager.addItem("soulkeydiamond", new ItemSoulkey("soulkeydiamond", 1));
      ObjectManager.addItem("soulkeyemerald", new ItemSoulkey("soulkeyemerald", 2));
      ObjectManager.addBlock("summoningpedestal", new BlockSummoningPedestal(group));
      ObjectManager.addBlock("equipmentforge_lesser", new BlockEquipmentForge(group, 1));
      ObjectManager.addBlock("equipmentforge_greater", new BlockEquipmentForge(group, 2));
      ObjectManager.addBlock("equipmentforge_master", new BlockEquipmentForge(group, 3));
      ObjectManager.addBlock("equipment_infuser", new EquipmentInfuserBlock(group));
      ObjectManager.addBlock("equipment_station", new EquipmentStationBlock(group));
      ObjectManager.addItem("immunizer", new ItemImmunizer());
      ObjectManager.addItem("cleansingcrystal", new ItemCleansingCrystal());
      ObjectManager.addItem("halloweentreat", new ItemHalloweenTreat());
      ObjectManager.addItem("wintergift", new ItemWinterGift());
      ObjectManager.addItem("wintergiftlarge", new ItemWinterGiftLarge());
      ObjectManager.addItem("frostyfur", new ItemFrostyFur());
      ObjectManager.addItem("poisongland", new ItemPoisonGland());
      ObjectManager.addItem("geistliver", new ItemGeistLiver());
      ObjectLists.addItem("diet_detritivore", ObjectManager.getItem("geistliver"));
      ObjectManager.addItem("wraithsigil", new ItemWraithSigil());
      ForgeFluid fluid = ObjectManager.addFluid("ooze");
      this.addFluid("ooze", fluid, new BlockFluidOoze(fluid, "ooze"), 10, 3000, 5000, 0, true, true);
      fluid = ObjectManager.addFluid("rabbitooze");
      this.addFluid("rabbitooze", fluid, new BlockFluidRabbitooze(fluid, "rabbitooze"), 10, 3000, 5000, 0, true, true);
      fluid = ObjectManager.addFluid("moglava");
      this.addFluid("moglava", fluid, new BlockFluidMoglava(fluid), 15, 3000, 5000, 1100, false, false);
      fluid = ObjectManager.addFluid("acid");
      this.addFluid("acid", fluid, new BlockFluidAcid(fluid, "acid"), 10, 1000, 10, 40, false, true);
      fluid = ObjectManager.addFluid("sharacid");
      this.addFluid("sharacid", fluid, new BlockFluidSharacid(fluid, "sharacid"), 10, 1000, 10, 40, false, true);
      fluid = ObjectManager.addFluid("poison");
      this.addFluid("poison", fluid, new BlockFluidPoison(fluid, "poison"), 0, 1000, 8, 20, false, true);
      fluid = ObjectManager.addFluid("vesspoison");
      this.addFluid("vesspoison", fluid, new BlockFluidVesspoison(fluid, "vesspoison"), 0, 1000, 8, 20, false, true);
      fluid = ObjectManager.addFluid("veshoney");
      this.addFluid("veshoney", fluid, new BlockFluidVeshoney(fluid, "veshoney"), 0, 1000, 8, 20, false, true);
      ObjectManager.addItem("summoningstaff", new ItemStaffSummoning("summoningstaff", "summoningstaff"));
      ObjectManager.addItem("stablesummoningstaff", new ItemStaffStable("stablesummoningstaff", "staffstable"));
      ObjectManager.addItem("bloodsummoningstaff", new ItemStaffBlood("bloodsummoningstaff", "staffblood"));
      ObjectManager.addItem("sturdysummoningstaff", new ItemStaffSturdy("sturdysummoningstaff", "staffsturdy"));
      ObjectManager.addItem("savagesummoningstaff", new ItemStaffSavage("savagesummoningstaff", "staffsavage"));
      ObjectManager.addBlock("frostweb", new BlockFrostweb());
      BlockMaker.addStoneBlocks(group, "lush");
      BlockMaker.addStoneBlocks(group, "desert");
      BlockMaker.addStoneBlocks(group, "shadow");
      BlockMaker.addStoneBlocks(group, "demon");
      BlockMaker.addStoneBlocks(group, "aberrant");
      BlockMaker.addStoneBlocks(group, "ashen");
      BlockMaker.addStoneBlocks(group, "stream");
      ObjectManager.addBlock("soulcubedemonic", new BlockSoulcube(group, "soulcubedemonic"));
      ObjectManager.addBlock("soulcubeundead", new BlockSoulcube(group, "soulcubeundead"));
      ObjectManager.addBlock("soulcubeaberrant", new BlockSoulcube(group, "soulcubeaberrant"));
      ObjectManager.addBlock("propolis", new BlockPropolis());
      // 1.12 GameRegistry.addSmelting(ObjectManager.getBlock("propolis"), new ItemStack(Blocks.TERRACOTTA, 1), 0.5F): smelting recipes are data/lycanitesmobs/recipe JSON
      ObjectManager.addBlock("veswax", new BlockVeswax());
      // 1.12 GameRegistry.addSmelting(ObjectManager.getBlock("veswax"), new ItemStack(Items.SUGAR, 6), 0.5F): smelting recipes are data/lycanitesmobs/recipe JSON
      AssetManager.addSound("frostcloud", group, "block.frostcloud");
      ObjectManager.addBlock("frostcloud", new BlockFrostCloud());
      AssetManager.addSound("frostfire", group, "block.frostfire");
      ObjectManager.addBlock("frostfire", new BlockFrostfire());
      AssetManager.addSound("icefire", group, "block.icefire");
      ObjectManager.addBlock("icefire", new BlockIcefire());
      AssetManager.addSound("hellfire", group, "block.hellfire");
      ObjectManager.addBlock("hellfire", new BlockHellfire());
      AssetManager.addSound("doomfire", group, "block.doomfire");
      ObjectManager.addBlock("doomfire", new BlockDoomfire());
      AssetManager.addSound("scorchfire", group, "block.scorchfire");
      ObjectManager.addBlock("scorchfire", new BlockScorchfire());
      AssetManager.addSound("primefire", group, "block.primefire");
      ObjectManager.addBlock("primefire", new BlockPrimefire());
      AssetManager.addSound("shadowfire", group, "block.shadowfire");
      ObjectManager.addBlock("shadowfire", new BlockShadowfire());
      AssetManager.addSound("poisoncloud", group, "block.poisoncloud");
      ObjectManager.addBlock("poisoncloud", new BlockPoisonCloud());
      AssetManager.addSound("poopcloud", group, "block.poopcloud");
      ObjectManager.addBlock("poopcloud", new BlockPoopCloud());
      ObjectManager.addBlock("quickweb", new BlockQuickWeb());
   }

   public void addFluid(
      String name, ForgeFluid fluid, BlockFluidBase block, int luminosity, int density, int viscosity, int temperature, boolean addSound, boolean addDamageSource
   ) {
      fluid.setLuminosity(luminosity).setDensity(density).setViscosity(viscosity).setTemperature(temperature);
      ObjectManager.addBlock(name, block);
      ObjectManager.addItem("bucket" + name, new GenericBucketItem(fluid, block, name).setContainerItem(Items.BUCKET));
      if (addSound) {
         AssetManager.addSound(name, LycanitesMobs.modInfo, "block." + name);
      }

      if (addDamageSource) {
         ObjectManager.addDamageSource(name, EntityDamageSource.named(name));
      }
   }

   public void registerItemOres() {
      OreDictionary.registerOre("listAllporkraw", Items.PORKCHOP);
      OreDictionary.registerOre("listAllporkcooked", Items.COOKED_PORKCHOP);
      OreDictionary.registerOre("listAllbeefraw", Items.BEEF);
      OreDictionary.registerOre("listAllbeefcooked", Items.COOKED_BEEF);
      OreDictionary.registerOre("listAllchickenraw", Items.CHICKEN);
      OreDictionary.registerOre("listAllchickencooked", Items.COOKED_CHICKEN);
      OreDictionary.registerOre("listAllmuttonraw", Items.MUTTON);
      OreDictionary.registerOre("listAllmuttoncooked", Items.COOKED_MUTTON);
      OreDictionary.registerOre("listAllrabbitraw", Items.RABBIT);
      OreDictionary.registerOre("listAllrabbitcooked", Items.COOKED_RABBIT);
      OreDictionary.registerOre("listAllfishraw", Items.COD);
      OreDictionary.registerOre("listAllfishcooked", Items.COOKED_COD);
      OreDictionary.registerOre("listAllVegetables", Items.WHEAT);
      OreDictionary.registerOre("listAllVegetables", Items.BREAD);
      OreDictionary.registerOre("listAllVegetables", Items.CARROT);
      OreDictionary.registerOre("listAllVegetables", Items.POTATO);
      OreDictionary.registerOre("listAllVegetables", Items.BAKED_POTATO);
      OreDictionary.registerOre("listAllVegetables", Items.BEETROOT);
      OreDictionary.registerOre("listAllFungi", Items.MUSHROOM_STEW);
      OreDictionary.registerOre("listAllFruit", Items.APPLE);
      OreDictionary.registerOre("listAllFruit", Items.MELON_SLICE);
      OreDictionary.registerOre("listAllSweet", Items.CAKE);
      OreDictionary.registerOre("listAllSweet", Items.PUMPKIN_PIE);
      OreDictionary.registerOre("listAllSweet", Items.COOKIE);
      OreDictionary.registerOre("listAllSweet", Items.SUGAR);
      OreDictionary.registerOre("listAllEntomo", Items.SPIDER_EYE);
      OreDictionary.registerOre("listAllEntomo", Items.FERMENTED_SPIDER_EYE);
      OreDictionary.registerOre("listAllRotten", Items.ROTTEN_FLESH);
      OreDictionary.registerOre("listAllporkraw", ObjectManager.getItem("raw_bobeko_meat"));
      OreDictionary.registerOre("listAllporkcooked", ObjectManager.getItem("cooked_bobeko_meat"));
      OreDictionary.registerOre("listAllbeefraw", ObjectManager.getItem("raw_pinky_meat"));
      OreDictionary.registerOre("listAllbeefcooked", ObjectManager.getItem("cooked_pinky_meat"));
      OreDictionary.registerOre("listAllchickenraw", ObjectManager.getItem("raw_joust_meat"));
      OreDictionary.registerOre("listAllchickencooked", ObjectManager.getItem("cooked_joust_meat"));
      OreDictionary.registerOre("listAllfishraw", ObjectManager.getItem("raw_silex_meat"));
      OreDictionary.registerOre("listAllfishcooked", ObjectManager.getItem("cooked_silex_meat"));
      OreDictionary.registerOre("listAllrabbitraw", ObjectManager.getItem("raw_krake_meat"));
      OreDictionary.registerOre("listAllrabbitcooked", ObjectManager.getItem("cooked_krake_meat"));
      OreDictionary.registerOre("listAllfishcooked", ObjectManager.getItem("cooked_cephignis_meat"));
      OreDictionary.registerOre("listAllchickenraw", ObjectManager.getItem("raw_concapede_meat"));
      OreDictionary.registerOre("listAllchickencooked", ObjectManager.getItem("cooked_concapede_meat"));
      OreDictionary.registerOre("listAllEntomoraw", ObjectManager.getItem("raw_concapede_meat"));
      OreDictionary.registerOre("listAllEntomocooked", ObjectManager.getItem("cooked_concapede_meat"));
      OreDictionary.registerOre("listAllmuttonraw", ObjectManager.getItem("raw_yale_meat"));
      OreDictionary.registerOre("listAllmuttoncooked", ObjectManager.getItem("cooked_yale_meat"));
      OreDictionary.registerOre("listAllporkraw", ObjectManager.getItem("raw_maka_meat"));
      OreDictionary.registerOre("listAllporkcooked", ObjectManager.getItem("cooked_maka_meat"));
      OreDictionary.registerOre("listAllfishraw", ObjectManager.getItem("raw_ika_meat"));
      OreDictionary.registerOre("listAllfishcooked", ObjectManager.getItem("cooked_ika_meat"));
      OreDictionary.registerOre("listAllmuttonraw", ObjectManager.getItem("raw_chupacabra_meat"));
      OreDictionary.registerOre("listAllmuttoncooked", ObjectManager.getItem("cooked_chupacabra_meat"));
      OreDictionary.registerOre("listAllbeefraw", ObjectManager.getItem("raw_aspid_meat"));
      OreDictionary.registerOre("listAllbeefcooked", ObjectManager.getItem("cooked_aspid_meat"));
      OreDictionary.registerOre("listAllRotten", ObjectManager.getItem("geistliver"));
   }

   public void registerBlockOres() {
      OreDictionary.registerOre("listAllFungi", Blocks.RED_MUSHROOM);
      OreDictionary.registerOre("listAllFungi", Blocks.RED_MUSHROOM_BLOCK);
      OreDictionary.registerOre("listAllFungi", Blocks.BROWN_MUSHROOM);
      OreDictionary.registerOre("listAllFungi", Blocks.BROWN_MUSHROOM_BLOCK);
      OreDictionary.registerOre("listAllVegetables", Blocks.PUMPKIN);
   }

   public int getEquipmentSharpnessRepair(ItemStack itemStack) {
      if (itemStack.isEmpty()) {
         return 0;
      } else {
         Item item = itemStack.getItem();
         ResourceLocation checkId = LycReg.key(item);
         if (item == Items.INK_SAC && item.getMetadata(itemStack) == 4) {
            checkId = ResourceLocation.parse("minecraft:lapis_lazuli");
         }

         for (String itemId : ItemConfig.lowEquipmentSharpnessItems) {
            if (ResourceLocation.parse(itemId).equals(checkId)) {
               return ItemConfig.lowEquipmentRepairAmount;
            }
         }

         for (String itemIdx : ItemConfig.mediumEquipmentSharpnessItems) {
            if (ResourceLocation.parse(itemIdx).equals(checkId)) {
               return ItemConfig.mediumEquipmentRepairAmount;
            }
         }

         for (String itemIdxx : ItemConfig.highEquipmentSharpnessItems) {
            if (ResourceLocation.parse(itemIdxx).equals(checkId)) {
               return ItemConfig.highEquipmentRepairAmount;
            }
         }

         for (String itemIdxxx : ItemConfig.maxEquipmentSharpnessItems) {
            if (ResourceLocation.parse(itemIdxxx).equals(checkId)) {
               return ItemEquipment.SHARPNESS_MAX;
            }
         }

         return 0;
      }
   }

   public int getEquipmentManaRepair(ItemStack itemStack) {
      if (itemStack.isEmpty()) {
         return 0;
      } else {
         Item item = itemStack.getItem();
         ResourceLocation checkId = LycReg.key(item);
         if (item == Items.INK_SAC && item.getMetadata(itemStack) == 4) {
            checkId = ResourceLocation.parse("minecraft:lapis_lazuli");
         }

         if (item instanceof ChargeItem) {
            return ItemConfig.highEquipmentRepairAmount;
         } else {
            for (String itemId : ItemConfig.lowEquipmentManaItems) {
               if (ResourceLocation.parse(itemId).equals(checkId)) {
                  return ItemConfig.lowEquipmentRepairAmount;
               }
            }

            for (String itemIdx : ItemConfig.mediumEquipmentManaItems) {
               if (ResourceLocation.parse(itemIdx).equals(checkId)) {
                  return ItemConfig.mediumEquipmentRepairAmount;
               }
            }

            for (String itemIdxx : ItemConfig.highEquipmentManaItems) {
               if (ResourceLocation.parse(itemIdxx).equals(checkId)) {
                  return ItemConfig.highEquipmentRepairAmount;
               }
            }

            for (String itemIdxxx : ItemConfig.maxEquipmentManaItems) {
               if (ResourceLocation.parse(itemIdxxx).equals(checkId)) {
                  return ItemEquipment.MANA_MAX;
               }
            }

            return 0;
         }
      }
   }
}
