package com.alcatrazescapee.notreepunching.util;

import com.alcatrazescapee.notreepunching.ModConfig;
import com.alcatrazescapee.notreepunching.NoTreePunching;
import com.alcatrazescapee.notreepunching.common.items.ItemKnife;
import com.alcatrazescapee.notreepunching.common.items.ItemMattock;
import com.alcatrazescapee.notreepunching.common.items.ItemSaw;
import com.alcatrazescapee.notreepunching.util.types.Stone;
import com.alcatrazescapee.notreepunching.util.types.ToolType;
import com.nukevscity.rlgen.legacy.LegacyStates;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.12 HarvestBlockHandler. The rules work on 1.12 harvest tools: a block needs a tool only if 1.12 gave it one (Forge initTools:
 * the ItemPickaxe/ItemSpade/ItemAxe effective lists plus the ore levels), read through rlgen's 1.12 block names. Blocks without a 1.12
 * counterpart (new 1.21 blocks, mod blocks) use their 1.21 tags: logs/planks and Dynamic Trees branches = axe, mineable/pickaxe =
 * pickaxe with the needs_*_tool level.
 */
public final class HarvestBlockHandler {
   public static final Random RNG = new Random();
   private static final Map<String, Object[]> LEGACY_TOOLS = new HashMap<>();
   private static Set<Block> exceptions;

   static {
      for (String b : new String[]{"activator_rail", "coal_ore", "cobblestone", "detector_rail", "double_stone_slab", "golden_rail", "ice", "mossy_cobblestone",
         "netherrack", "packed_ice", "rail", "sandstone", "red_sandstone", "stone", "stone_slab", "stone_button", "stone_pressure_plate", "enchanting_table",
         "quartz_ore"}) {
         LEGACY_TOOLS.put("minecraft:" + b, new Object[]{"pickaxe", 0});
      }
      for (String b : new String[]{"iron_ore", "iron_block", "lapis_ore", "lapis_block"}) {
         LEGACY_TOOLS.put("minecraft:" + b, new Object[]{"pickaxe", 1});
      }
      for (String b : new String[]{"emerald_ore", "emerald_block", "diamond_ore", "diamond_block", "gold_ore", "gold_block", "redstone_ore", "lit_redstone_ore"}) {
         LEGACY_TOOLS.put("minecraft:" + b, new Object[]{"pickaxe", 2});
      }
      LEGACY_TOOLS.put("minecraft:obsidian", new Object[]{"pickaxe", 3});
      for (String b : new String[]{"clay", "dirt", "farmland", "grass", "gravel", "mycelium", "sand", "snow", "snow_layer", "soul_sand", "grass_path",
         "concrete_powder"}) {
         LEGACY_TOOLS.put("minecraft:" + b, new Object[]{"shovel", 0});
      }
      for (String b : new String[]{"planks", "bookshelf", "log", "log2", "chest", "pumpkin", "lit_pumpkin", "melon_block", "ladder", "wooden_button",
         "wooden_pressure_plate"}) {
         LEGACY_TOOLS.put("minecraft:" + b, new Object[]{"axe", 0});
      }
   }

   private HarvestBlockHandler() {
   }

   /** reloadWhitelist: the alwaysBreakable 1.12 names, every metadata */
   private static synchronized Set<Block> exceptions() {
      if (exceptions == null) {
         Set<Block> set = new HashSet<>();
         for (String s : ModConfig.GENERAL.alwaysBreakable) {
            ResourceLocation id = ResourceLocation.tryParse(s);
            if (id == null) {
               continue;
            }
            if (id.getNamespace().equals("minecraft")) {
               for (int m = 0; m < 16; m++) {
                  BlockState st = LegacyStates.block(s, m);
                  if (st != null && !st.isAir()) {
                     set.add(st.getBlock());
                  }
               }
            } else {
               ResourceLocation r = id.getNamespace().equals("defiledlands") ? ResourceLocation.fromNamespaceAndPath("rlgen", id.getPath()) : id;
               if (BuiltInRegistries.BLOCK.containsKey(r)) {
                  set.add(BuiltInRegistries.BLOCK.get(r));
               } else {
                  NoTreePunching.LOG.warn("Problem loading entry in alwaysBreakable: {}", s);
               }
            }
         }
         exceptions = set;
      }
      return exceptions;
   }

   public static boolean isWhitelisted(BlockState state) {
      return exceptions().contains(state.getBlock());
   }

   /** Block.getHarvestTool / getHarvestLevel of 1.12: {tool class, level} or null */
   @Nullable
   public static Object[] getHarvestTool(BlockState state) {
      Block block = state.getBlock();
      String legacy = LegacyStates.legacyName(block);
      Object[] t = LEGACY_TOOLS.get(legacy);
      if (t != null) {
         return t;
      }
      if (legacy.startsWith("minecraft:") && LegacyStates.block(legacy, 0) != null) {
         return null; // a 1.12 vanilla block without a harvest tool
      }
      if (block.getClass().getName().contains("Branch") || state.is(BlockTags.LOGS) || state.is(BlockTags.PLANKS)) {
         return new Object[]{"axe", 0};
      }
      if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
         int level = state.is(BlockTags.NEEDS_DIAMOND_TOOL) ? 3 : state.is(BlockTags.NEEDS_IRON_TOOL) ? 2 : state.is(BlockTags.NEEDS_STONE_TOOL) ? 1 : 0;
         return new Object[]{"pickaxe", level};
      }
      return null;
   }

   /** Item.getToolClasses with Item.getHarvestLevel */
   private static Map<String, Integer> toolClasses(ItemStack stack) {
      Map<String, Integer> out = new HashMap<>();
      Item item = stack.getItem();
      int level = item instanceof TieredItem t ? level(t.getTier()) : 0;
      if (item instanceof PickaxeItem || stack.is(ItemTags.PICKAXES)) {
         out.put("pickaxe", level);
      }
      if (item instanceof AxeItem || stack.is(ItemTags.AXES) || item instanceof ItemSaw || item instanceof ItemMattock
         || item == NoTreePunching.FLINT_TOOLS.get(ToolType.AXE).get()) {
         out.put("axe", level);
      }
      if (item instanceof ShovelItem || stack.is(ItemTags.SHOVELS) || item instanceof ItemMattock) {
         out.put("shovel", level);
      }
      if (item instanceof ItemMattock) {
         out.put("mattock", level);
      }
      return out;
   }

   /** ToolMaterial harvest levels: wood/gold/flint 0, stone 1, iron 2, diamond 3 (netherite 4) */
   private static int level(Tier tier) {
      if (tier instanceof Tiers t) {
         return switch (t) {
            case WOOD, GOLD -> 0;
            case STONE -> 1;
            case IRON -> 2;
            case DIAMOND -> 3;
            case NETHERITE -> 4;
         };
      }
      var tag = tier.getIncorrectBlocksForDrops();
      if (tag == BlockTags.INCORRECT_FOR_STONE_TOOL) {
         return 1;
      } else if (tag == BlockTags.INCORRECT_FOR_IRON_TOOL) {
         return 2;
      } else if (tag == BlockTags.INCORRECT_FOR_DIAMOND_TOOL) {
         return 3;
      } else if (tag == BlockTags.INCORRECT_FOR_NETHERITE_TOOL) {
         return 4;
      }
      return 0;
   }

   public static boolean isInvalidTool(@Nullable ItemStack stack, @Nullable Player player, BlockState state) {
      if (player == null || stack == null) {
         return true;
      } else if (isWhitelisted(state)) {
         return false;
      }
      Object[] needed = getHarvestTool(state);
      if (needed == null) {
         return false;
      } else if (stack.isEmpty()) {
         return true;
      }
      String neededToolClass = (String)needed[0];
      int neededHarvestLevel = (Integer)needed[1];
      for (Map.Entry<String, Integer> e : toolClasses(stack).entrySet()) {
         String toolClass = e.getKey();
         if (neededToolClass.equals(toolClass) && e.getValue() >= neededHarvestLevel) {
            return false;
         }
         if (neededToolClass.equals("shovel") && toolClass.equals("pickaxe") && e.getValue() >= 1) {
            return false;
         }
         if (toolClass.equals("mattock") && (neededToolClass.equals("axe") || neededToolClass.equals("shovel"))) {
            return false;
         }
      }
      return true;
   }

   public static float getSpeedModifier(String toolClass) {
      return switch (toolClass) {
         case "axe", "mattock" -> 0.2F;
         case "pickaxe" -> 0.125F;
         default -> 0.33F;
      };
   }

   /** addExtraDrops: stone gives 2-4 rocks, leaves sticks, tall grass plant fiber when cut with a knife */
   public static void addExtraDrops(List<ItemStack> drops, BlockState state, Player player, ItemStack stack, boolean isSilkTouch) {
      Stone stone = Stone.getFromBlock(state);
      if (stone != null && stone.isEnabled() && ModConfig.GENERAL.enableStoneDropChanges && !isSilkTouch) {
         drops.clear();
         drops.add(new ItemStack(NoTreePunching.ROCKS.get(stone).get(), 2 + RNG.nextInt(3)));
      }
      if (state.getBlock() instanceof LeavesBlock && !isSilkTouch) {
         float stickChance = (float)ModConfig.BALANCE.leavesStickDropChance;
         if (stack.getItem() instanceof ItemKnife k && !k.isWeak()) {
            stickChance += 0.3F; // ore "toolKnife": only the metal knives
         }
         if (RNG.nextFloat() < stickChance) {
            drops.add(new ItemStack(Items.STICK, 1 + RNG.nextInt(1)));
         }
      }
      if ((state.is(Blocks.SHORT_GRASS) || state.is(Blocks.FERN) || state.is(Blocks.TALL_GRASS) || state.is(Blocks.LARGE_FERN)) && stack.getItem() instanceof ItemKnife) {
         if ((double)RNG.nextFloat() < ModConfig.BALANCE.tallGrassDropPlantFiberChance) {
            drops.add(new ItemStack(NoTreePunching.GRASS_FIBER.get(), RNG.nextBoolean() ? 1 : 2));
         }
         ItemStack held = player.getMainHandItem();
         if (held.getItem() instanceof ItemKnife) {
            held.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
         }
      }
   }

   public static List<ItemStack> list() {
      return new ArrayList<>();
   }
}
