package com.charles445.simpledifficulty.api;

import com.charles445.simpledifficulty.SimpleDifficulty;
import com.charles445.simpledifficulty.config.JsonConfigInternal;
import com.charles445.simpledifficulty.item.ItemArmorTemperature;
import com.charles445.simpledifficulty.item.ItemCanteen;
import com.charles445.simpledifficulty.item.ItemDragonCanteen;
import com.charles445.simpledifficulty.item.ItemIronCanteen;
import com.charles445.simpledifficulty.item.ItemJuice;
import com.charles445.simpledifficulty.item.ItemPurifiedWaterBottle;
import com.charles445.simpledifficulty.item.ItemThermometer;
import java.util.EnumMap;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** 1.12 SDItems + RegisterItems (and the item blocks of RegisterBlocks / RegisterFluids). */
public class SDItems {
   public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SimpleDifficulty.MODID);
   public static final DeferredRegister<ArmorMaterial> MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, SimpleDifficulty.MODID);

   // EnumHelper.addArmorMaterial("SDWOOL", "simpledifficulty:wool", 2, {1,1,1,1}, 5, ITEM_ARMOR_EQUIP_GENERIC, 0); repair = white wool (meta 0)
   public static final DeferredHolder<ArmorMaterial, ArmorMaterial> woolArmorMaterial = MATERIALS.register("wool",
      () -> material("wool", () -> Ingredient.of(Items.WHITE_WOOL)));
   public static final DeferredHolder<ArmorMaterial, ArmorMaterial> iceArmorMaterial = MATERIALS.register("ice",
      () -> material("ice", () -> Ingredient.of(SDItems.ice_chunk.get())));

   public static final DeferredItem<Item> purifiedWaterBottle = ITEMS.register("purified_water_bottle", () -> new ItemPurifiedWaterBottle(new Item.Properties()));
   public static final DeferredItem<Item> juice = ITEMS.register("juice", () -> new ItemJuice(new Item.Properties()));
   public static final DeferredItem<Item> canteen = ITEMS.register("canteen", () -> new ItemCanteen(new Item.Properties()));
   public static final DeferredItem<Item> ironCanteen = ITEMS.register("iron_canteen", () -> new ItemIronCanteen(new Item.Properties()));
   public static final DeferredItem<Item> charcoalFilter = ITEMS.registerSimpleItem("charcoal_filter");
   public static final DeferredItem<Item> ice_chunk = ITEMS.registerSimpleItem("ice_chunk");
   public static final DeferredItem<Item> magma_chunk = ITEMS.registerSimpleItem("magma_chunk");
   public static final DeferredItem<Item> thermometer = ITEMS.register("thermometer", () -> new ItemThermometer(new Item.Properties()));
   public static final DeferredItem<Item> wool_helmet = armor("wool_helmet", woolArmorMaterial, ArmorItem.Type.HELMET);
   public static final DeferredItem<Item> wool_chestplate = armor("wool_chestplate", woolArmorMaterial, ArmorItem.Type.CHESTPLATE);
   public static final DeferredItem<Item> wool_leggings = armor("wool_leggings", woolArmorMaterial, ArmorItem.Type.LEGGINGS);
   public static final DeferredItem<Item> wool_boots = armor("wool_boots", woolArmorMaterial, ArmorItem.Type.BOOTS);
   public static final DeferredItem<Item> ice_helmet = armor("ice_helmet", iceArmorMaterial, ArmorItem.Type.HELMET);
   public static final DeferredItem<Item> ice_chestplate = armor("ice_chestplate", iceArmorMaterial, ArmorItem.Type.CHESTPLATE);
   public static final DeferredItem<Item> ice_leggings = armor("ice_leggings", iceArmorMaterial, ArmorItem.Type.LEGGINGS);
   public static final DeferredItem<Item> ice_boots = armor("ice_boots", iceArmorMaterial, ArmorItem.Type.BOOTS);
   // extra items (extraItems.json, all enabled in RLCraft)
   public static final DeferredItem<Item> dragon_canteen = ITEMS.register("dragon_canteen",
      () -> new ItemDragonCanteen(new Item.Properties(), JsonConfigInternal.extraItems.get("dragon_canteen")));
   public static final DeferredItem<Item> frost_rod = ITEMS.registerSimpleItem("frost_rod");
   public static final DeferredItem<Item> frost_powder = ITEMS.registerSimpleItem("frost_powder");

   public static final DeferredItem<BlockItem> campfire = ITEMS.registerSimpleBlockItem(SDBlocks.campfire);
   public static final DeferredItem<BlockItem> rain_collector = ITEMS.registerSimpleBlockItem(SDBlocks.rainCollector);
   public static final DeferredItem<BlockItem> heater = ITEMS.registerSimpleBlockItem(SDBlocks.heater);
   public static final DeferredItem<BlockItem> chiller = ITEMS.registerSimpleBlockItem(SDBlocks.chiller);
   public static final DeferredItem<BlockItem> spit = ITEMS.registerSimpleBlockItem(SDBlocks.spit);
   /** forge:bucketfilled with purifiedwater (1.12 universal bucket) */
   public static final DeferredItem<Item> purifiedWaterBucket = ITEMS.register("purifiedwater_bucket",
      () -> new BucketItem(SDFluids.purifiedWater.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

   private static ArmorMaterial material(String name, java.util.function.Supplier<Ingredient> repair) {
      EnumMap<ArmorItem.Type, Integer> def = new EnumMap<>(ArmorItem.Type.class);
      for (ArmorItem.Type t : ArmorItem.Type.values()) {
         def.put(t, 1);
      }
      return new ArmorMaterial(def, 5, SoundEvents.ARMOR_EQUIP_GENERIC, repair,
         List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(SimpleDifficulty.MODID, name))), 0.0F, 0.0F);
   }

   private static DeferredItem<Item> armor(String name, Holder<ArmorMaterial> material, ArmorItem.Type type) {
      return ITEMS.register(name, () -> new ItemArmorTemperature(material, type, new Item.Properties().durability(type.getDurability(2))));
   }

   public static void fillTab(CreativeModeTab.Output out) {
      out.accept(purifiedWaterBottle.get());
      for (ItemJuice.JuiceEnum j : ItemJuice.JuiceEnum.values()) {
         out.accept(ItemJuice.stack(j));
      }
      ((ItemCanteen)canteen.get()).fillTab(out);
      ((ItemCanteen)ironCanteen.get()).fillTab(out);
      out.accept(charcoalFilter.get());
      out.accept(ice_chunk.get());
      out.accept(magma_chunk.get());
      out.accept(thermometer.get());
      for (DeferredItem<Item> a : List.of(wool_helmet, wool_chestplate, wool_leggings, wool_boots, ice_helmet, ice_chestplate, ice_leggings, ice_boots)) {
         out.accept(a.get());
      }
      ((ItemCanteen)dragon_canteen.get()).fillTab(out);
      out.accept(frost_rod.get());
      out.accept(frost_powder.get());
      out.accept(campfire.get());
      out.accept(rain_collector.get());
      out.accept(heater.get());
      out.accept(chiller.get());
      out.accept(spit.get());
      out.accept(purifiedWaterBucket.get());
   }

   public static void register(IEventBus bus) {
      MATERIALS.register(bus);
      ITEMS.register(bus);
   }
}
