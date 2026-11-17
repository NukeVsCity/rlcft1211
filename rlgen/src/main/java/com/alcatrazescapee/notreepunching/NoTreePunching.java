package com.alcatrazescapee.notreepunching;

import com.alcatrazescapee.notreepunching.common.blocks.BlockFirePit;
import com.alcatrazescapee.notreepunching.common.blocks.BlockLargeVessel;
import com.alcatrazescapee.notreepunching.common.blocks.BlockPottery;
import com.alcatrazescapee.notreepunching.common.blocks.BlockRock;
import com.alcatrazescapee.notreepunching.common.container.ContainerFirePit;
import com.alcatrazescapee.notreepunching.common.container.ContainerLargeVessel;
import com.alcatrazescapee.notreepunching.common.container.ContainerSmallVessel;
import com.alcatrazescapee.notreepunching.common.items.ItemCeramicBucket;
import com.alcatrazescapee.notreepunching.common.items.ItemClayTool;
import com.alcatrazescapee.notreepunching.common.items.ItemSmallVessel;
import com.alcatrazescapee.notreepunching.common.tile.TileFirePit;
import com.alcatrazescapee.notreepunching.common.tile.TileLargeVessel;
import com.alcatrazescapee.notreepunching.util.types.Pottery;
import com.alcatrazescapee.notreepunching.common.items.ItemFireStarter;
import com.alcatrazescapee.notreepunching.common.items.ItemKnife;
import com.alcatrazescapee.notreepunching.common.items.ItemMattock;
import com.alcatrazescapee.notreepunching.common.items.ItemRock;
import com.alcatrazescapee.notreepunching.common.items.ItemSaw;
import com.alcatrazescapee.notreepunching.util.types.Stone;
import com.alcatrazescapee.notreepunching.util.types.ToolType;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.tags.TagKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * No Tree Punching 2.0.21 as in RLCraft 2.9.3, ported from the 1.12 code (RegistryHelper of alcatrazcore -> DeferredRegisters, the
 * 1.12 registry names with "/" are kept: notreepunching:rock/stone, knife/flint, pottery/worked ...). The ModGuiHandler GUIs are
 * menus: large vessel, small vessel, fire pit.
 */
@Mod(NoTreePunching.MOD_ID)
public final class NoTreePunching {
   public static final String MOD_ID = "notreepunching";
   public static final String VERSION = "2.0.21";
   public static final Logger LOG = LogManager.getLogger("notreepunching");

   public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
   public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
   public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, MOD_ID);
   public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);
   public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
   public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MOD_ID);
   public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MOD_ID);

   /** the ceramic bucket's fluid (1.12 FluidHandlerItemStackSimple NBT "Fluid") */
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<SimpleFluidContent>> FLUID_CONTENT = COMPONENTS.registerComponentType("fluid",
      b -> b.persistent(SimpleFluidContent.CODEC).networkSynchronized(SimpleFluidContent.STREAM_CODEC));

   public static final DeferredHolder<SoundEvent, SoundEvent> KNAPPING = SOUNDS.register("knapping",
      () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "knapping")));

   /** ModMaterials.TOOL_FLINT: harvest level 0, 45 uses, efficiency 2.5, damage 0.5, enchantability 0, no repair material */
   public static final Tier TOOL_FLINT = new Tier() {
      @Override
      public int getUses() {
         return 45;
      }

      @Override
      public float getSpeed() {
         return 2.5F;
      }

      @Override
      public float getAttackDamageBonus() {
         return 0.5F;
      }

      @Override
      public TagKey<Block> getIncorrectBlocksForDrops() {
         return BlockTags.INCORRECT_FOR_WOODEN_TOOL;
      }

      @Override
      public int getEnchantmentValue() {
         return 0;
      }

      @Override
      public Ingredient getRepairIngredient() {
         return Ingredient.EMPTY;
      }
   };
   /** Metal: IRON, GOLD, DIAMOND (copper/tin/bronze/steel tools are off in RLCraft) */
   public static final Map<String, Tier> METALS = new LinkedHashMap<>();

   static {
      METALS.put("iron", Tiers.IRON);
      METALS.put("gold", Tiers.GOLD);
      METALS.put("diamond", Tiers.DIAMOND);
   }

   public static final DeferredItem<Item> GRASS_FIBER = ITEMS.registerSimpleItem("grass_fiber");
   public static final DeferredItem<Item> GRASS_STRING = ITEMS.registerSimpleItem("grass_string");
   public static final DeferredItem<Item> FLINT_SHARD = ITEMS.registerSimpleItem("flint_shard");
   public static final DeferredItem<Item> CLAY_BRICK = ITEMS.registerSimpleItem("clay_brick");
   public static final DeferredItem<Item> CLAY_TOOL = ITEMS.register("clay_tool", () -> new ItemClayTool(new Item.Properties()));
   public static final DeferredItem<Item> FIRE_STARTER = ITEMS.register("fire_starter", () -> new ItemFireStarter(new Item.Properties().durability(10)));
   public static final DeferredItem<Item> CERAMIC_SMALL_VESSEL = ITEMS.register("ceramic_small_vessel", () -> new ItemSmallVessel(new Item.Properties()));
   public static final DeferredItem<ItemCeramicBucket> CERAMIC_BUCKET = ITEMS.register("ceramic_bucket", () -> new ItemCeramicBucket(new Item.Properties()));

   /** ModBlocks: pottery/<type> (item blocks except worked clay), ceramic_large_vessel, fire_pit (no item) */
   public static final Map<Pottery, DeferredBlock<BlockPottery>> POTTERY = new EnumMap<>(Pottery.class);

   static {
      for (Pottery type : Pottery.values()) {
         // BlockPottery: Material.CLAY, hardness 0.8 - 0.1 * ordinal, SoundType.GROUND (= 1.21 gravel), shovel 0
         DeferredBlock<BlockPottery> b = BLOCKS.registerBlock("pottery/" + type.name().toLowerCase(), p -> new BlockPottery(type, p), BlockBehaviour.Properties.of()
            .mapColor(MapColor.CLAY).strength(0.8F - 0.1F * (float)type.ordinal()).sound(SoundType.GRAVEL).noOcclusion());
         POTTERY.put(type, b);
         if (type != Pottery.WORKED) {
            ITEMS.registerSimpleBlockItem(b);
         }
      }
   }

   /** BlockLargeVessel: Material.GROUND, hardness 1, pickaxe 0 */
   public static final DeferredBlock<BlockLargeVessel> CERAMIC_LARGE_VESSEL = BLOCKS.registerBlock("ceramic_large_vessel", BlockLargeVessel::new,
      BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(1.0F).sound(SoundType.GRAVEL).noOcclusion());
   public static final DeferredItem<BlockItem> CERAMIC_LARGE_VESSEL_ITEM = ITEMS.registerSimpleBlockItem(CERAMIC_LARGE_VESSEL);
   /** BlockFirePit: Material.CIRCUITS (hardness 0, destroyed by pistons), wood sound, light 15 when lit */
   public static final DeferredBlock<BlockFirePit> FIRE_PIT = BLOCKS.registerBlock("fire_pit", BlockFirePit::new, BlockBehaviour.Properties.of()
      .instabreak().sound(SoundType.WOOD).noCollission().noOcclusion().pushReaction(PushReaction.DESTROY)
      .lightLevel(st -> st.getValue(BlockFirePit.LIT) ? 15 : 0));

   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileLargeVessel>> LARGE_VESSEL_TILE = TILES.register("ceramic_large_vessel",
      () -> BlockEntityType.Builder.of(TileLargeVessel::new, CERAMIC_LARGE_VESSEL.get()).build(null));
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileFirePit>> FIRE_PIT_TILE = TILES.register("fire_pit",
      () -> BlockEntityType.Builder.of(TileFirePit::new, FIRE_PIT.get()).build(null));

   public static final DeferredHolder<MenuType<?>, MenuType<ContainerLargeVessel>> LARGE_VESSEL_MENU = MENUS.register("large_vessel",
      () -> IMenuTypeExtension.create((id, inv, buf) -> {
         BlockPos pos = buf.readBlockPos();
         return new ContainerLargeVessel(id, inv, inv.player.level().getBlockEntity(pos) instanceof TileLargeVessel te ? te
            : new TileLargeVessel(pos, CERAMIC_LARGE_VESSEL.get().defaultBlockState()));
      }));
   public static final DeferredHolder<MenuType<?>, MenuType<ContainerSmallVessel>> SMALL_VESSEL_MENU = MENUS.register("small_vessel",
      () -> IMenuTypeExtension.create((id, inv, buf) -> new ContainerSmallVessel(id, inv, ContainerSmallVessel.heldVessel(inv.player))));
   public static final DeferredHolder<MenuType<?>, MenuType<ContainerFirePit>> FIRE_PIT_MENU = MENUS.register("fire_pit",
      () -> IMenuTypeExtension.create((id, inv, buf) -> {
         BlockPos pos = buf.readBlockPos();
         return new ContainerFirePit(id, inv, inv.player.level().getBlockEntity(pos) instanceof TileFirePit te ? te
            : new TileFirePit(pos, FIRE_PIT.get().defaultBlockState()));
      }));

   public static final Map<Stone, DeferredBlock<BlockRock>> LOOSE_ROCKS = new EnumMap<>(Stone.class);
   public static final Map<Stone, DeferredItem<Item>> ROCKS = new EnumMap<>(Stone.class);
   public static final Map<Stone, DeferredBlock<Block>> COBBLESTONE = new EnumMap<>(Stone.class);
   public static final Map<ToolType, DeferredItem<Item>> FLINT_TOOLS = new EnumMap<>(ToolType.class);
   public static final Map<String, DeferredItem<Item>> METAL_TOOLS = new LinkedHashMap<>();

   static {
      for (Stone type : Stone.values()) {
         if (!type.isEnabled()) {
            continue;
         }
         String n = type.name().toLowerCase();
         if (type.hasCobblestone()) {
            // BlockCobble: Material.ROCK, hardness 2.5, pickaxe 0
            DeferredBlock<Block> cobble = BLOCKS.registerSimpleBlock("cobblestone/" + n, BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
               .strength(2.5F, 12.5F).requiresCorrectToolForDrops().sound(SoundType.STONE));
            COBBLESTONE.put(type, cobble);
            ITEMS.registerSimpleBlockItem(cobble);
         }
         // BlockRock: Material.GROUND, hardness 0.15, no collision, no item block
         LOOSE_ROCKS.put(type, BLOCKS.registerBlock("loose_rock/" + n, p -> new BlockRock(type, p), BlockBehaviour.Properties.of().mapColor(MapColor.DIRT)
            .strength(0.15F).sound(SoundType.STONE).noCollission().noOcclusion().pushReaction(PushReaction.DESTROY)));
         ROCKS.put(type, ITEMS.register("rock/" + n, () -> new ItemRock(type, new Item.Properties())));
      }
      for (ToolType t : ToolType.values()) {
         if (t.isFlintTool) {
            FLINT_TOOLS.put(t, ITEMS.register(t.name().toLowerCase() + "/flint", () -> createFlint(t)));
         }
         if (t.isNewTool) {
            for (Map.Entry<String, Tier> m : METALS.entrySet()) {
               METAL_TOOLS.put(t.name().toLowerCase() + "/" + m.getKey(), ITEMS.register(t.name().toLowerCase() + "/" + m.getKey(), () -> createTool(t, m.getValue())));
            }
         }
      }
   }

   public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB_ITEMS = TABS.register("items", () -> CreativeModeTab.builder()
      .title(Component.translatable("itemGroup.notreepunching.items")).icon(() -> new ItemStack(FLINT_SHARD.get()))
      .displayItems((p, out) -> {
         for (DeferredItem<Item> i : List.of(GRASS_FIBER, GRASS_STRING, FLINT_SHARD, CLAY_BRICK)) {
            out.accept(i.get());
         }
         ROCKS.values().forEach(r -> out.accept(r.get()));
         COBBLESTONE.values().forEach(c -> out.accept(c.get()));
         POTTERY.forEach((t, b) -> {
            if (t != Pottery.WORKED) {
               out.accept(b.get());
            }
         });
         out.accept(CERAMIC_LARGE_VESSEL.get());
         out.accept(CERAMIC_SMALL_VESSEL.get());
         out.accept(CERAMIC_BUCKET.get());
         ItemStack water = new ItemStack(CERAMIC_BUCKET.get());
         water.set(FLUID_CONTENT.get(), SimpleFluidContent.copyOf(new FluidStack(Fluids.WATER, ItemCeramicBucket.CAPACITY)));
         out.accept(water);
      }).build());
   public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB_TOOLS = TABS.register("tools", () -> CreativeModeTab.builder()
      .title(Component.translatable("itemGroup.notreepunching.tools")).icon(() -> new ItemStack(FLINT_TOOLS.get(ToolType.KNIFE).get()))
      .displayItems((p, out) -> {
         out.accept(CLAY_TOOL.get());
         out.accept(FIRE_STARTER.get());
         FLINT_TOOLS.values().forEach(t -> out.accept(t.get()));
         METAL_TOOLS.values().forEach(t -> out.accept(t.get()));
      }).build());

   /** ToolType.createFlint with the alcatrazcore stats (ItemPickCore 1.0/-2.8, ItemAxeCore dmg/-3.0, ItemSpadeCore 0/0, ItemHoe, ItemKnife) */
   private static Item createFlint(ToolType t) {
      Tier m = TOOL_FLINT;
      return switch (t) {
         case PICKAXE -> new PickaxeItem(m, new Item.Properties().attributes(PickaxeItem.createAttributes(m, 1.0F, -2.8F)));
         case AXE -> new DiggerItem(m, BlockTags.MINEABLE_WITH_AXE, new Item.Properties().attributes(DiggerItem.createAttributes(m, m.getAttackDamageBonus(), -3.0F)));
         case SHOVEL -> new ShovelItem(m, new Item.Properties().attributes(ShovelItem.createAttributes(m, 0.0F, 0.0F)));
         case HOE -> new HoeItem(m, new Item.Properties().attributes(HoeItem.createAttributes(m, -m.getAttackDamageBonus(), m.getAttackDamageBonus() - 3.0F)));
         case KNIFE -> new ItemKnife(m, true);
         default -> throw new IllegalStateException("This type does not support flint tools");
      };
   }

   /** ToolType.createTool: knife, saw, mattock in iron/gold/diamond */
   private static Item createTool(ToolType t, Tier m) {
      return switch (t) {
         case KNIFE -> new ItemKnife(m, false);
         case SAW -> new ItemSaw(m);
         case MATTOCK -> new ItemMattock(m);
         default -> throw new IllegalStateException("This type does not support new tools");
      };
   }

   public NoTreePunching(IEventBus modBus) {
      BLOCKS.register(modBus);
      ITEMS.register(modBus);
      SOUNDS.register(modBus);
      TABS.register(modBus);
      TILES.register(modBus);
      MENUS.register(modBus);
      COMPONENTS.register(modBus);
      modBus.addListener(NoTreePunching::registerCapabilities);
      NeoForge.EVENT_BUS.register(ModEventHandler.class);
   }

   /** 1.12 capabilities: the ceramic bucket's fluid handler, TileLargeVessel on every side, TileInventory (fire pit) only for side null */
   private static void registerCapabilities(RegisterCapabilitiesEvent event) {
      event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new ItemCeramicBucket.FluidHandlerCeramicBucket(stack, ItemCeramicBucket.CAPACITY),
         CERAMIC_BUCKET.get());
      event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, LARGE_VESSEL_TILE.get(), (te, side) -> te.getInventory());
      event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FIRE_PIT_TILE.get(), (te, side) -> side == null ? te.getInventory() : null);
   }
}
