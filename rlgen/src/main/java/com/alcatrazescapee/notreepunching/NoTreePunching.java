package com.alcatrazescapee.notreepunching;

import com.alcatrazescapee.notreepunching.common.blocks.BlockRock;
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
import net.minecraft.core.registries.Registries;
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
 * 1.12 registry names with "/" are kept: notreepunching:rock/stone, knife/flint ...). Not ported: the fire pit and the clay pottery
 * (worked clay, ceramic vessels/bucket); the clay tool only makes clay bricks.
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
   /** ItemClayTool: 60 uses, stays in the crafting grid (Forge default container item: a new clay tool); shaping clay into pottery is not ported */
   public static final DeferredItem<Item> CLAY_TOOL = ITEMS.register("clay_tool", () -> new Item(new Item.Properties().durability(60).setNoRepair()) {
      @Override
      public ItemStack getCraftingRemainingItem(ItemStack stack) {
         return new ItemStack(this);
      }

      @Override
      public boolean hasCraftingRemainingItem(ItemStack stack) {
         return true;
      }
   });
   public static final DeferredItem<Item> FIRE_STARTER = ITEMS.register("fire_starter", () -> new ItemFireStarter(new Item.Properties().durability(10)));

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
      NeoForge.EVENT_BUS.register(ModEventHandler.class);
   }
}
