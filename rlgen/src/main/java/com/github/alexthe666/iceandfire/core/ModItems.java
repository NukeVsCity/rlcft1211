package com.github.alexthe666.iceandfire.core;

import com.github.alexthe666.iceandfire.IafRegistries;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.block.BlockPodium;
import com.github.alexthe666.iceandfire.enums.EnumDragonArmor;
import com.github.alexthe666.iceandfire.enums.EnumDragonEgg;
import com.github.alexthe666.iceandfire.enums.EnumSeaSerpent;
import com.github.alexthe666.iceandfire.enums.EnumTroll;
import com.github.alexthe666.iceandfire.item.*;
import com.github.alexthe666.iceandfire.item.block.ItemBlockMeta;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.SimpleTier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 1.12 ModItems: the same public static fields and registry names (fields are assigned when the item registry fires), the 1.12 armor
 * and tool materials with their exact stats (1.12 EnumHelper.addArmorMaterial / addToolMaterial values, repair items from
 * ModRecipes.preInit) and the item blocks of ModBlocks (registered first, like the 1.12 creative tab).
 * Tool harvest levels: 2 iron, 3 diamond, 4 (dragonbone) netherite.
 */
public class ModItems {
   public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, IceAndFire.MODID);
   /** Item registry names in creative tab order. */
   public static final List<String> NAMES = new ArrayList<>();
   public static final TagKey<Item> INGOTS_SILVER = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ingots/silver"));

   /** 1.12 EnumHelper.addArmorMaterial(name, texture, durability, reductionAmounts {feet, legs, chest, head}, enchantability, sound, toughness). */
   private static DeferredHolder<ArmorMaterial, ArmorMaterial> armor(String name, String texture, int[] reduction, int ench, Holder<SoundEvent> sound,
                                                                     float toughness, Supplier<Ingredient> repair) {
      return ARMOR_MATERIALS.register(name, () -> {
         EnumMap<ArmorItem.Type, Integer> map = new EnumMap<>(ArmorItem.Type.class);
         map.put(ArmorItem.Type.BOOTS, reduction[0]);
         map.put(ArmorItem.Type.LEGGINGS, reduction[1]);
         map.put(ArmorItem.Type.CHESTPLATE, reduction[2]);
         map.put(ArmorItem.Type.HELMET, reduction[3]);
         map.put(ArmorItem.Type.BODY, reduction[2]);
         return new ArmorMaterial(map, ench, sound, repair, List.of(new ArmorMaterial.Layer(ResourceLocation.parse(texture))), toughness, 0.0F);
      });
   }

   private static Supplier<Ingredient> item(Supplier<Item> item) {
      return () -> Ingredient.of(item.get());
   }

   public static final Holder<ArmorMaterial> silverMetal = armor("silver", "iceandfire:armor_silver_metal", new int[]{1, 4, 5, 2}, 20, SoundEvents.ARMOR_EQUIP_CHAIN, 0.0F,
      () -> Ingredient.of(INGOTS_SILVER));
   public static final Holder<ArmorMaterial> blindfoldArmor = armor("blindfold", "iceandfire:blindfold", new int[]{1, 1, 1, 1}, 10, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F,
      () -> Ingredient.of(Items.STRING));
   public static final Holder<ArmorMaterial> sheep = armor("sheep", "iceandfire:sheep_disguise", new int[]{1, 2, 3, 1}, 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F,
      () -> Ingredient.of(Items.WHITE_WOOL));
   public static final Holder<ArmorMaterial> myrmexDesert = armor("myrmexdesert", "iceandfire:myrmex_desert", new int[]{3, 5, 8, 4}, 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F,
      () -> Ingredient.EMPTY);
   public static final Holder<ArmorMaterial> myrmexJungle = armor("myrmexjungle", "iceandfire:myrmex_jungle", new int[]{3, 5, 8, 4}, 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F,
      () -> Ingredient.EMPTY);
   public static final Holder<ArmorMaterial> earplugsArmor = armor("earplugs", "iceandfire:earplugs", new int[]{1, 1, 1, 1}, 10, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F,
      () -> Ingredient.of(Items.OAK_BUTTON));
   public static final Holder<ArmorMaterial> yellow_deathworm = armor("yellow_deathworm", "iceandfire:yellow_deathworm", new int[]{2, 5, 7, 3}, 5,
      SoundEvents.ARMOR_EQUIP_LEATHER, 1.5F, () -> Ingredient.of(IafMeta.stack(ModItems.deathworm_chitin, 1, 0)));
   public static final Holder<ArmorMaterial> white_deathworm = armor("white_deathworm", "iceandfire:white_deathworm", new int[]{2, 5, 7, 3}, 5,
      SoundEvents.ARMOR_EQUIP_LEATHER, 1.5F, () -> Ingredient.of(IafMeta.stack(ModItems.deathworm_chitin, 1, 1)));
   public static final Holder<ArmorMaterial> red_deathworm = armor("red_deathworm", "iceandfire:red_deathworm", new int[]{2, 5, 7, 3}, 5,
      SoundEvents.ARMOR_EQUIP_LEATHER, 1.5F, () -> Ingredient.of(IafMeta.stack(ModItems.deathworm_chitin, 1, 2)));
   public static final Holder<ArmorMaterial> troll_mountain = armor("mountain_troll", "iceandfire:troll_mountain", new int[]{4, 5, 7, 3}, 10,
      SoundEvents.ARMOR_EQUIP_LEATHER, 1.0F, () -> Ingredient.of(EnumTroll.MOUNTAIN.leather));
   public static final Holder<ArmorMaterial> troll_forest = armor("forest_troll", "iceandfire:troll_forest", new int[]{2, 5, 7, 3}, 10,
      SoundEvents.ARMOR_EQUIP_LEATHER, 1.0F, () -> Ingredient.of(EnumTroll.FOREST.leather));
   public static final Holder<ArmorMaterial> troll_frost = armor("frost_troll", "iceandfire:troll_frost", new int[]{2, 5, 7, 3}, 10,
      SoundEvents.ARMOR_EQUIP_LEATHER, 1.0F, () -> Ingredient.of(EnumTroll.FROST.leather));

   /** 1.12 EnumHelper.addToolMaterial(name, harvestLevel, maxUses, efficiency, damage, enchantability). */
   public static final Tier silverTools = new SimpleTier(BlockTags.INCORRECT_FOR_IRON_TOOL, 460, 11.0F, 1.0F, 18, () -> Ingredient.of(INGOTS_SILVER));
   public static final Tier boneTools = new SimpleTier(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 1660, 10.0F, 4.0F, 22, () -> Ingredient.of(ModItems.dragonbone));
   public static final Tier fireBoneTools = new SimpleTier(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2000, 10.0F, 5.5F, 22, () -> Ingredient.of(ModItems.dragonbone));
   public static final Tier iceBoneTools = new SimpleTier(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2000, 10.0F, 5.5F, 22, () -> Ingredient.of(ModItems.dragonbone));
   public static final Tier trollWeapon = new SimpleTier(BlockTags.INCORRECT_FOR_IRON_TOOL, 300, 11.0F, 1.0F, 1, () -> Ingredient.of(Items.STONE));
   public static final Tier myrmexChitin = new SimpleTier(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 600, 6.0F, -1.0F, 8, () -> Ingredient.EMPTY);

   public static Item bestiary;
   public static Item manuscript;
   public static Item sapphireGem;
   public static Item silverIngot;
   public static Item silverNugget;
   public static Item silver_helmet;
   public static Item silver_chestplate;
   public static Item silver_leggings;
   public static Item silver_boots;
   public static Item silver_sword;
   public static Item silver_shovel;
   public static Item silver_pickaxe;
   public static Item silver_axe;
   public static Item silver_hoe;
   public static Item fire_stew;
   public static Item frost_stew;
   public static Item dragonegg_red;
   public static Item dragonegg_green;
   public static Item dragonegg_bronze;
   public static Item dragonegg_gray;
   public static Item dragonegg_blue;
   public static Item dragonegg_white;
   public static Item dragonegg_sapphire;
   public static Item dragonegg_silver;
   public static Item dragonscales_red;
   public static Item dragonscales_green;
   public static Item dragonscales_bronze;
   public static Item dragonscales_gray;
   public static Item dragonscales_blue;
   public static Item dragonscales_white;
   public static Item dragonscales_sapphire;
   public static Item dragonscales_silver;
   public static Item dragonbone;
   public static Item witherbone;
   public static Item fishing_spear;
   public static Item wither_shard;
   public static Item dragonbone_sword;
   public static Item dragonbone_shovel;
   public static Item dragonbone_pickaxe;
   public static Item dragonbone_axe;
   public static Item dragonbone_hoe;
   public static Item dragonbone_sword_fire;
   public static Item dragonbone_sword_ice;
   public static Item dragonbone_arrow;
   public static Item dragonbone_bow;
   public static Item dragon_skull;
   public static Item dragon_armor_iron;
   public static Item dragon_armor_gold;
   public static Item dragon_armor_diamond;
   public static Item dragon_meal;
   public static Item sickly_dragon_meal;
   public static Item fire_dragon_flesh;
   public static Item ice_dragon_flesh;
   public static Item fire_dragon_heart;
   public static Item ice_dragon_heart;
   public static Item fire_dragon_blood;
   public static Item ice_dragon_blood;
   public static Item dragon_stick;
   public static Item dragon_horn;
   public static Item dragon_horn_fire;
   public static Item dragon_horn_ice;
   public static Item dragon_flute;
   public static Item hippogryph_egg;
   public static Item iron_hippogryph_armor;
   public static Item gold_hippogryph_armor;
   public static Item diamond_hippogryph_armor;
   public static Item gorgon_head;
   public static Item stone_statue;
   public static Item blindfold;
   public static Item pixie_dust;
   public static Item ambrosia;
   public static Item sheep_helmet;
   public static Item sheep_chestplate;
   public static Item sheep_leggings;
   public static Item sheep_boots;
   public static Item shiny_scales;
   public static Item earplugs;
   public static Item deathworm_chitin;
   public static Item deathworm_yellow_helmet;
   public static Item deathworm_yellow_chestplate;
   public static Item deathworm_yellow_leggings;
   public static Item deathworm_yellow_boots;
   public static Item deathworm_white_helmet;
   public static Item deathworm_white_chestplate;
   public static Item deathworm_white_leggings;
   public static Item deathworm_white_boots;
   public static Item deathworm_red_helmet;
   public static Item deathworm_red_chestplate;
   public static Item deathworm_red_leggings;
   public static Item deathworm_red_boots;
   public static Item deathworm_egg;
   public static Item rotten_egg;
   public static Item stymphalian_bird_feather;
   public static Item stymphalian_arrow;
   public static Item troll_tusk;
   public static Item myrmex_desert_egg;
   public static Item myrmex_jungle_egg;
   public static Item myrmex_desert_resin;
   public static Item myrmex_jungle_resin;
   public static Item myrmex_desert_chitin;
   public static Item myrmex_jungle_chitin;
   public static Item myrmex_stinger;
   public static Item myrmex_desert_sword;
   public static Item myrmex_desert_sword_venom;
   public static Item myrmex_desert_shovel;
   public static Item myrmex_desert_pickaxe;
   public static Item myrmex_desert_axe;
   public static Item myrmex_desert_hoe;
   public static Item myrmex_jungle_sword;
   public static Item myrmex_jungle_sword_venom;
   public static Item myrmex_jungle_shovel;
   public static Item myrmex_jungle_pickaxe;
   public static Item myrmex_jungle_axe;
   public static Item myrmex_jungle_hoe;
   public static Item myrmex_desert_staff;
   public static Item myrmex_jungle_staff;
   public static Item myrmex_desert_helmet;
   public static Item myrmex_desert_chestplate;
   public static Item myrmex_desert_leggings;
   public static Item myrmex_desert_boots;
   public static Item myrmex_jungle_helmet;
   public static Item myrmex_jungle_chestplate;
   public static Item myrmex_jungle_leggings;
   public static Item myrmex_jungle_boots;
   public static Item amphithere_feather;
   public static Item amphithere_arrow;
   public static Item sea_serpent_fang;
   public static Item sea_serpent_arrow;


   static {
      itemBlocks();
      reg("bestiary", () -> bestiary = new ItemBestiary());
      reg("manuscript", () -> manuscript = new ItemGeneric("iceandfire.manuscript", new Item.Properties()));
      reg("sapphire_gem", () -> sapphireGem = new ItemGeneric("iceandfire.sapphireGem", new Item.Properties()));
      reg("silver_ingot", () -> silverIngot = new ItemGeneric("iceandfire.silverIngot", new Item.Properties()));
      reg("silver_nugget", () -> silverNugget = new ItemGeneric("iceandfire.silverNugget", new Item.Properties()));
      reg("armor_silver_metal_helmet", () -> silver_helmet = new ItemModArmor(silverMetal, 15, ArmorItem.Type.HELMET, "iceandfire.silver_helmet"));
      reg("armor_silver_metal_chestplate", () -> silver_chestplate = new ItemModArmor(silverMetal, 15, ArmorItem.Type.CHESTPLATE, "iceandfire.silver_chestplate"));
      reg("armor_silver_metal_leggings", () -> silver_leggings = new ItemModArmor(silverMetal, 15, ArmorItem.Type.LEGGINGS, "iceandfire.silver_leggings"));
      reg("armor_silver_metal_boots", () -> silver_boots = new ItemModArmor(silverMetal, 15, ArmorItem.Type.BOOTS, "iceandfire.silver_boots"));
      reg("silver_sword", () -> silver_sword = new ItemModSword(silverTools, "iceandfire.silver_sword"));
      reg("silver_shovel", () -> silver_shovel = new ItemModShovel(silverTools, "iceandfire.silver_shovel"));
      reg("silver_pickaxe", () -> silver_pickaxe = new ItemModPickaxe(silverTools, "iceandfire.silver_pickaxe"));
      reg("silver_axe", () -> silver_axe = new ItemModAxe(silverTools, "iceandfire.silver_axe"));
      reg("silver_hoe", () -> silver_hoe = new ItemModHoe(silverTools, "iceandfire.silver_hoe"));
      reg("fire_stew", () -> fire_stew = new ItemGeneric("iceandfire.fire_stew", new Item.Properties()));
      reg("frost_stew", () -> frost_stew = new ItemGeneric("iceandfire.frost_stew", new Item.Properties()));
      reg("dragonegg_red", () -> dragonegg_red = new ItemDragonEgg(EnumDragonEgg.RED));
      reg("dragonegg_green", () -> dragonegg_green = new ItemDragonEgg(EnumDragonEgg.GREEN));
      reg("dragonegg_bronze", () -> dragonegg_bronze = new ItemDragonEgg(EnumDragonEgg.BRONZE));
      reg("dragonegg_gray", () -> dragonegg_gray = new ItemDragonEgg(EnumDragonEgg.GRAY));
      reg("dragonegg_blue", () -> dragonegg_blue = new ItemDragonEgg(EnumDragonEgg.BLUE));
      reg("dragonegg_white", () -> dragonegg_white = new ItemDragonEgg(EnumDragonEgg.WHITE));
      reg("dragonegg_sapphire", () -> dragonegg_sapphire = new ItemDragonEgg(EnumDragonEgg.SAPPHIRE));
      reg("dragonegg_silver", () -> dragonegg_silver = new ItemDragonEgg(EnumDragonEgg.SILVER));
      reg("dragonscales_red", () -> dragonscales_red = new ItemDragonScales(EnumDragonEgg.RED));
      reg("dragonscales_green", () -> dragonscales_green = new ItemDragonScales(EnumDragonEgg.GREEN));
      reg("dragonscales_bronze", () -> dragonscales_bronze = new ItemDragonScales(EnumDragonEgg.BRONZE));
      reg("dragonscales_gray", () -> dragonscales_gray = new ItemDragonScales(EnumDragonEgg.GRAY));
      reg("dragonscales_blue", () -> dragonscales_blue = new ItemDragonScales(EnumDragonEgg.BLUE));
      reg("dragonscales_white", () -> dragonscales_white = new ItemDragonScales(EnumDragonEgg.WHITE));
      reg("dragonscales_sapphire", () -> dragonscales_sapphire = new ItemDragonScales(EnumDragonEgg.SAPPHIRE));
      reg("dragonscales_silver", () -> dragonscales_silver = new ItemDragonScales(EnumDragonEgg.SILVER));
      reg("dragonbone", () -> dragonbone = new ItemGeneric("iceandfire.dragonbone", new Item.Properties().stacksTo(8)));
      reg("witherbone", () -> witherbone = new ItemGeneric("iceandfire.witherbone", new Item.Properties()));
      reg("fishing_spear", () -> fishing_spear = new ItemFishingSpear());
      reg("wither_shard", () -> wither_shard = new ItemGeneric("iceandfire.wither_shard", new Item.Properties()));
      reg("dragonbone_sword", () -> dragonbone_sword = new ItemModSword(boneTools, "iceandfire.dragonbone_sword"));
      reg("dragonbone_shovel", () -> dragonbone_shovel = new ItemModShovel(boneTools, "iceandfire.dragonbone_shovel"));
      reg("dragonbone_pickaxe", () -> dragonbone_pickaxe = new ItemModPickaxe(boneTools, "iceandfire.dragonbone_pickaxe"));
      reg("dragonbone_axe", () -> dragonbone_axe = new ItemModAxe(boneTools, "iceandfire.dragonbone_axe"));
      reg("dragonbone_hoe", () -> dragonbone_hoe = new ItemModHoe(boneTools, "iceandfire.dragonbone_hoe"));
      reg("dragonbone_sword_fire", () -> dragonbone_sword_fire = new ItemAlchemySword(fireBoneTools, "iceandfire.dragonbone_sword_fire"));
      reg("dragonbone_sword_ice", () -> dragonbone_sword_ice = new ItemAlchemySword(iceBoneTools, "iceandfire.dragonbone_sword_ice"));
      reg("dragonbone_arrow", () -> dragonbone_arrow = new ItemIafArrow("iceandfire.dragonbone_arrow", () -> ModEntityHooks.dragonArrow, null));
      reg("dragonbone_bow", () -> dragonbone_bow = new ItemDragonBow());
      reg("dragon_skull", () -> dragon_skull = new ItemDragonSkull());
      reg("dragonarmor_iron", () -> dragon_armor_iron = new ItemDragonArmor(0, "dragonarmor_iron"));
      reg("dragonarmor_gold", () -> dragon_armor_gold = new ItemDragonArmor(1, "dragonarmor_gold"));
      reg("dragonarmor_diamond", () -> dragon_armor_diamond = new ItemDragonArmor(2, "dragonarmor_diamond"));
      reg("dragon_meal", () -> dragon_meal = new ItemGeneric("iceandfire.dragon_meal", new Item.Properties()));
      reg("sickly_dragon_meal", () -> sickly_dragon_meal = new ItemGenericDesc("iceandfire.sickly_dragon_meal"));
      reg("fire_dragon_flesh", () -> fire_dragon_flesh = new ItemDragonFlesh(true));
      reg("ice_dragon_flesh", () -> ice_dragon_flesh = new ItemDragonFlesh(false));
      reg("fire_dragon_heart", () -> fire_dragon_heart = new ItemGeneric("iceandfire.fire_dragon_heart", new Item.Properties()));
      reg("ice_dragon_heart", () -> ice_dragon_heart = new ItemGeneric("iceandfire.ice_dragon_heart", new Item.Properties()));
      reg("fire_dragon_blood", () -> fire_dragon_blood = new ItemGeneric("iceandfire.fire_dragon_blood", new Item.Properties()));
      reg("ice_dragon_blood", () -> ice_dragon_blood = new ItemGeneric("iceandfire.ice_dragon_blood", new Item.Properties()));
      reg("dragon_stick", () -> dragon_stick = new ItemGeneric("iceandfire.dragon_stick", new Item.Properties().stacksTo(1)));
      reg("dragon_horn", () -> dragon_horn = new ItemDragonHornStatic());
      reg("dragon_horn_fire", () -> dragon_horn_fire = new ItemDragonHornActive("dragon_horn_fire"));
      reg("dragon_horn_ice", () -> dragon_horn_ice = new ItemDragonHornActive("dragon_horn_ice"));
      reg("dragon_flute", () -> dragon_flute = new ItemDragonFlute());
      reg("hippogryph_egg", () -> hippogryph_egg = new ItemHippogryphEgg());
      reg("iron_hippogryph_armor", () -> iron_hippogryph_armor = new ItemGeneric("iceandfire.iron_hippogryph_armor", new Item.Properties().stacksTo(1)));
      reg("gold_hippogryph_armor", () -> gold_hippogryph_armor = new ItemGeneric("iceandfire.gold_hippogryph_armor", new Item.Properties().stacksTo(1)));
      reg("diamond_hippogryph_armor", () -> diamond_hippogryph_armor = new ItemGeneric("iceandfire.diamond_hippogryph_armor", new Item.Properties().stacksTo(1)));
      reg("gorgon_head", () -> gorgon_head = new ItemGorgonHead());
      reg("stone_statue", () -> stone_statue = new ItemStoneStatue());
      reg("blindfold", () -> blindfold = new ItemBlindfold(blindfoldArmor));
      reg("pixie_dust", () -> pixie_dust = new ItemPixieDust());
      reg("ambrosia", () -> ambrosia = new ItemAmbrosia());
      reg("sheep_helmet", () -> sheep_helmet = new ItemModArmor(sheep, 5, ArmorItem.Type.HELMET, "iceandfire.sheep_helmet"));
      reg("sheep_chestplate", () -> sheep_chestplate = new ItemModArmor(sheep, 5, ArmorItem.Type.CHESTPLATE, "iceandfire.sheep_chestplate"));
      reg("sheep_leggings", () -> sheep_leggings = new ItemModArmor(sheep, 5, ArmorItem.Type.LEGGINGS, "iceandfire.sheep_leggings"));
      reg("sheep_boots", () -> sheep_boots = new ItemModArmor(sheep, 5, ArmorItem.Type.BOOTS, "iceandfire.sheep_boots"));
      reg("shiny_scales", () -> shiny_scales = new ItemGeneric("iceandfire.shiny_scales", new Item.Properties()));
      reg("earplugs", () -> earplugs = new ItemModArmor(earplugsArmor, 5, ArmorItem.Type.HELMET, "iceandfire.earplugs"));
      reg("deathworm_chitin", () -> deathworm_chitin = new ItemDeathWormChitin());
      reg("deathworm_yellow_helmet", () -> deathworm_yellow_helmet = new ItemDeathwormArmor(yellow_deathworm, 0, "iceandfire.deathworm_yellow_helmet"));
      reg("deathworm_yellow_chestplate", () -> deathworm_yellow_chestplate = new ItemDeathwormArmor(yellow_deathworm, 1, "iceandfire.deathworm_yellow_chestplate"));
      reg("deathworm_yellow_leggings", () -> deathworm_yellow_leggings = new ItemDeathwormArmor(yellow_deathworm, 2, "iceandfire.deathworm_yellow_leggings"));
      reg("deathworm_yellow_boots", () -> deathworm_yellow_boots = new ItemDeathwormArmor(yellow_deathworm, 3, "iceandfire.deathworm_yellow_boots"));
      reg("deathworm_white_helmet", () -> deathworm_white_helmet = new ItemDeathwormArmor(white_deathworm, 0, "iceandfire.deathworm_white_helmet"));
      reg("deathworm_white_chestplate", () -> deathworm_white_chestplate = new ItemDeathwormArmor(white_deathworm, 1, "iceandfire.deathworm_white_chestplate"));
      reg("deathworm_white_leggings", () -> deathworm_white_leggings = new ItemDeathwormArmor(white_deathworm, 2, "iceandfire.deathworm_white_leggings"));
      reg("deathworm_white_boots", () -> deathworm_white_boots = new ItemDeathwormArmor(white_deathworm, 3, "iceandfire.deathworm_white_boots"));
      reg("deathworm_red_helmet", () -> deathworm_red_helmet = new ItemDeathwormArmor(red_deathworm, 0, "iceandfire.deathworm_red_helmet"));
      reg("deathworm_red_chestplate", () -> deathworm_red_chestplate = new ItemDeathwormArmor(red_deathworm, 1, "iceandfire.deathworm_red_chestplate"));
      reg("deathworm_red_leggings", () -> deathworm_red_leggings = new ItemDeathwormArmor(red_deathworm, 2, "iceandfire.deathworm_red_leggings"));
      reg("deathworm_red_boots", () -> deathworm_red_boots = new ItemDeathwormArmor(red_deathworm, 3, "iceandfire.deathworm_red_boots"));
      reg("iceandfire.deathworm_egg", () -> deathworm_egg = new ItemDeathwormEgg());
      reg("rotten_egg", () -> rotten_egg = new ItemRottenEgg());
      reg("stymphalian_bird_feather", () -> stymphalian_bird_feather = new ItemGeneric("iceandfire.stymphalian_bird_feather", new Item.Properties()));
      reg("stymphalian_arrow", () -> stymphalian_arrow = new ItemIafArrow("iceandfire.stymphalian_arrow", () -> ModEntityHooks.stymphalianArrow, "item.iceandfire.stymphalian_arrow.desc"));
      reg("troll_tusk", () -> troll_tusk = new ItemGeneric("iceandfire.troll_tusk", new Item.Properties()));
      reg("myrmex_desert_egg", () -> myrmex_desert_egg = new ItemMyrmexEgg(false));
      reg("myrmex_jungle_egg", () -> myrmex_jungle_egg = new ItemMyrmexEgg(true));
      reg("myrmex_desert_resin", () -> myrmex_desert_resin = new ItemGeneric("iceandfire.myrmex_desert_resin", new Item.Properties()));
      reg("myrmex_jungle_resin", () -> myrmex_jungle_resin = new ItemGeneric("iceandfire.myrmex_jungle_resin", new Item.Properties()));
      reg("myrmex_desert_chitin", () -> myrmex_desert_chitin = new ItemGeneric("iceandfire.myrmex_desert_chitin", new Item.Properties()));
      reg("myrmex_jungle_chitin", () -> myrmex_jungle_chitin = new ItemGeneric("iceandfire.myrmex_jungle_chitin", new Item.Properties()));
      reg("myrmex_stinger", () -> myrmex_stinger = new ItemGeneric("iceandfire.myrmex_stinger", new Item.Properties()));
      reg("myrmex_desert_sword", () -> myrmex_desert_sword = new ItemModSword(myrmexChitin, "iceandfire.myrmex_desert_sword"));
      reg("myrmex_desert_sword_venom", () -> myrmex_desert_sword_venom = new ItemModSword(myrmexChitin, "iceandfire.myrmex_desert_sword_venom"));
      reg("myrmex_desert_shovel", () -> myrmex_desert_shovel = new ItemModShovel(myrmexChitin, "iceandfire.myrmex_desert_shovel"));
      reg("myrmex_desert_pickaxe", () -> myrmex_desert_pickaxe = new ItemModPickaxe(myrmexChitin, "iceandfire.myrmex_desert_pickaxe"));
      reg("myrmex_desert_axe", () -> myrmex_desert_axe = new ItemModAxe(myrmexChitin, "iceandfire.myrmex_desert_axe"));
      reg("myrmex_desert_hoe", () -> myrmex_desert_hoe = new ItemModHoe(myrmexChitin, "iceandfire.myrmex_desert_hoe"));
      reg("myrmex_jungle_sword", () -> myrmex_jungle_sword = new ItemModSword(myrmexChitin, "iceandfire.myrmex_jungle_sword"));
      reg("myrmex_jungle_sword_venom", () -> myrmex_jungle_sword_venom = new ItemModSword(myrmexChitin, "iceandfire.myrmex_jungle_sword_venom"));
      reg("myrmex_jungle_shovel", () -> myrmex_jungle_shovel = new ItemModShovel(myrmexChitin, "iceandfire.myrmex_jungle_shovel"));
      reg("myrmex_jungle_pickaxe", () -> myrmex_jungle_pickaxe = new ItemModPickaxe(myrmexChitin, "iceandfire.myrmex_jungle_pickaxe"));
      reg("myrmex_jungle_axe", () -> myrmex_jungle_axe = new ItemModAxe(myrmexChitin, "iceandfire.myrmex_jungle_axe"));
      reg("myrmex_jungle_hoe", () -> myrmex_jungle_hoe = new ItemModHoe(myrmexChitin, "iceandfire.myrmex_jungle_hoe"));
      reg("myrmex_desert_staff", () -> myrmex_desert_staff = new ItemMyrmexStaff(false));
      reg("myrmex_jungle_staff", () -> myrmex_jungle_staff = new ItemMyrmexStaff(true));
      reg("myrmex_desert_helmet", () -> myrmex_desert_helmet = new ItemModArmor(myrmexDesert, 20, ArmorItem.Type.HELMET, "iceandfire.myrmex_desert_helmet"));
      reg("myrmex_desert_chestplate", () -> myrmex_desert_chestplate = new ItemModArmor(myrmexDesert, 20, ArmorItem.Type.CHESTPLATE, "iceandfire.myrmex_desert_chestplate"));
      reg("myrmex_desert_leggings", () -> myrmex_desert_leggings = new ItemModArmor(myrmexDesert, 20, ArmorItem.Type.LEGGINGS, "iceandfire.myrmex_desert_leggings"));
      reg("myrmex_desert_boots", () -> myrmex_desert_boots = new ItemModArmor(myrmexDesert, 20, ArmorItem.Type.BOOTS, "iceandfire.myrmex_desert_boots"));
      reg("myrmex_jungle_helmet", () -> myrmex_jungle_helmet = new ItemModArmor(myrmexJungle, 20, ArmorItem.Type.HELMET, "iceandfire.myrmex_jungle_helmet"));
      reg("myrmex_jungle_chestplate", () -> myrmex_jungle_chestplate = new ItemModArmor(myrmexJungle, 20, ArmorItem.Type.CHESTPLATE, "iceandfire.myrmex_jungle_chestplate"));
      reg("myrmex_jungle_leggings", () -> myrmex_jungle_leggings = new ItemModArmor(myrmexJungle, 20, ArmorItem.Type.LEGGINGS, "iceandfire.myrmex_jungle_leggings"));
      reg("myrmex_jungle_boots", () -> myrmex_jungle_boots = new ItemModArmor(myrmexJungle, 20, ArmorItem.Type.BOOTS, "iceandfire.myrmex_jungle_boots"));
      reg("amphithere_feather", () -> amphithere_feather = new ItemGeneric("iceandfire.amphithere_feather", new Item.Properties()));
      reg("amphithere_arrow", () -> amphithere_arrow = new ItemIafArrow("iceandfire.amphithere_arrow", () -> ModEntityHooks.amphithereArrow, "item.iceandfire.amphithere_arrow.desc"));
      reg("sea_serpent_fang", () -> sea_serpent_fang = new ItemGeneric("iceandfire.sea_serpent_fang", new Item.Properties()));
      reg("sea_serpent_arrow", () -> sea_serpent_arrow = new ItemIafArrow("iceandfire.sea_serpent_arrow", () -> ModEntityHooks.seaSerpentArrow, "item.iceandfire.sea_serpent_arrow.desc"));
      enumItems();
   }

   private static void reg(String name, Supplier<Item> item) {
      NAMES.add(name);
      IafRegistries.ITEMS.register(name, item);
   }

   private static String[] podiumKeys = new String[]{"tile.iceandfire.podium_oak", "tile.iceandfire.podium_spruce", "tile.iceandfire.podium_birch",
      "tile.iceandfire.podium_jungle", "tile.iceandfire.podium_acacia", "tile.iceandfire.podium_dark_oak"};

   /** 1.12 CommonProxy.registerItemBlocks: an ItemBlock for every ModBlocks block (the jar, pixie house, podium and resin ones with subtypes). */
   private static void itemBlocks() {
      for (String name : ModBlocks.NAMES) {
         reg(name, () -> {
            Block block = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(IceAndFire.MODID, name));
            if (block == ModBlocks.jar_pixie) {
               return new ItemBlockMeta(block, new Item.Properties().stacksTo(1), 5, i -> "tile.iceandfire.jar_" + i);
            } else if (block == ModBlocks.pixieHouse) {
               return new ItemBlockMeta(block, new Item.Properties().stacksTo(1), 6, i -> "tile.iceandfire.pixie_house_" + i);
            } else if (block == ModBlocks.podium) {
               return new ItemBlockMeta(block, new Item.Properties(), BlockPodium.EnumType.values().length, i -> podiumKeys[i < 0 || i >= podiumKeys.length ? 0 : i]);
            } else if (block == ModBlocks.myrmex_resin) {
               return new ItemBlockMeta(block, new Item.Properties(), 2, i -> i == 1 ? "tile.iceandfire.jungle_myrmex_resin" : "tile.iceandfire.desert_myrmex_resin");
            } else if (block == ModBlocks.myrmex_resin_sticky) {
               return new ItemBlockMeta(block, new Item.Properties(), 2,
                  i -> i == 1 ? "tile.iceandfire.jungle_myrmex_resin_sticky" : "tile.iceandfire.desert_myrmex_resin_sticky");
            }
            return new BlockItem(block, new Item.Properties());
         });
      }
   }

   /** 1.12 EnumDragonArmor.initArmors / EnumSeaSerpent.initArmors / EnumTroll constructors. */
   private static void enumItems() {
      for (int i = 0; i < EnumDragonArmor.values().length; i++) {
         EnumDragonArmor a = EnumDragonArmor.values()[i];
         a.armorMaterial = armor("armor_dragon_scales" + (i + 1), "iceandfire:armor_dragon_scales" + (i + 1), new int[]{4, 7, 9, 4}, 15,
            SoundEvents.ARMOR_EQUIP_CHAIN, 2.0F, () -> Ingredient.of(EnumDragonArmor.getScaleItem(a)));
      }
      for (EnumSeaSerpent color : EnumSeaSerpent.values()) {
         color.armorMaterial = armor("sea_serpent_scales_" + color.resourceName, "iceandfire:sea_serpent_scales_" + color.resourceName, new int[]{4, 8, 7, 4}, 25,
            SoundEvents.ARMOR_EQUIP_GOLD, 2.5F, () -> Ingredient.EMPTY);
      }
      for (EnumDragonArmor a : EnumDragonArmor.values()) {
         reg(a.name() + "_helmet", () -> a.helmet = new ItemScaleArmor(a.eggType, a, a.armorMaterial, 0, "iceandfire.dragonHelmet"));
         reg(a.name() + "_chestplate", () -> a.chestplate = new ItemScaleArmor(a.eggType, a, a.armorMaterial, 1, "iceandfire.dragonChestplate"));
         reg(a.name() + "_leggings", () -> a.leggings = new ItemScaleArmor(a.eggType, a, a.armorMaterial, 2, "iceandfire.dragonLeggings"));
         reg(a.name() + "_boots", () -> a.boots = new ItemScaleArmor(a.eggType, a, a.armorMaterial, 3, "iceandfire.dragonBoots"));
      }
      for (EnumSeaSerpent c : EnumSeaSerpent.values()) {
         reg("sea_serpent_scales_" + c.resourceName, () -> c.scale = new ItemSeaSerpentScales(c.resourceName, c.color));
         reg("tide_" + c.resourceName + "_helmet", () -> c.helmet = new ItemSeaSerpentArmor(c, c.armorMaterial, 0, "iceandfire.sea_serpent_helmet"));
         reg("tide_" + c.resourceName + "_chestplate", () -> c.chestplate = new ItemSeaSerpentArmor(c, c.armorMaterial, 1, "iceandfire.sea_serpent_chestplate"));
         reg("tide_" + c.resourceName + "_leggings", () -> c.leggings = new ItemSeaSerpentArmor(c, c.armorMaterial, 2, "iceandfire.sea_serpent_leggings"));
         reg("tide_" + c.resourceName + "_boots", () -> c.boots = new ItemSeaSerpentArmor(c, c.armorMaterial, 3, "iceandfire.sea_serpent_boots"));
      }
      for (EnumTroll.Weapon w : EnumTroll.Weapon.values()) {
         reg("troll_weapon." + w.name().toLowerCase(), () -> w.item = new ItemTrollWeapon(w));
      }
      for (EnumTroll t : EnumTroll.values()) {
         String n = t.name().toLowerCase();
         reg("troll_leather_" + n, () -> t.leather = new ItemGeneric("iceandfire.troll_leather_" + n));
         reg(n + "_troll_leather_helmet", () -> t.helmet = new ItemTrollArmor(t, t.material(), 0));
         reg(n + "_troll_leather_chestplate", () -> t.chestplate = new ItemTrollArmor(t, t.material(), 1));
         reg(n + "_troll_leather_leggings", () -> t.leggings = new ItemTrollArmor(t, t.material(), 2));
         reg(n + "_troll_leather_boots", () -> t.boots = new ItemTrollArmor(t, t.material(), 3));
      }
   }

   public static void init(IEventBus modBus) {
      ARMOR_MATERIALS.register(modBus);
   }
}
