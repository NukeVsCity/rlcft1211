package com.nukevscity.rlgen.charm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;

/** MesonDecoratorTheme / CharmDecoratorTheme and Charm's village profession themes (1.12 blocks as their 1.21 states). */
public abstract class Theme {
   protected final InnerDecorator decorator;

   protected Theme(InnerDecorator decorator) {
      this.decorator = decorator;
   }

   protected java.util.Random getRand() {
      return this.decorator.getRand();
   }

   protected boolean common() {
      return this.decorator.common();
   }

   protected boolean uncommon() {
      return this.decorator.uncommon();
   }

   protected boolean valuable() {
      return this.decorator.valuable();
   }

   protected boolean rare() {
      return this.decorator.rare();
   }

   protected <T> T pick(List<T> list) {
      return list.get(this.getRand().nextInt(list.size()));
   }

   static BlockState carpet(DyeColor c) {
      return net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.withDefaultNamespace(c.getName() + "_carpet")).defaultBlockState();
   }

   static BlockState wool(DyeColor c) {
      return net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.withDefaultNamespace(c.getName() + "_wool")).defaultBlockState();
   }

   /** BlockCauldron LEVEL 0..3 */
   static BlockState cauldron(int level) {
      return level <= 0 ? Blocks.CAULDRON.defaultBlockState() : Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, level);
   }

   static List<BlockState> woolColours() {
      List<BlockState> states = new ArrayList<>();
      for (int i = 0; i < 16; i++) {
         states.add(wool(DyeColor.byId(15 - i)));
      }
      return states;
   }

   /** CharmDecoratorTheme: a crate (Crate), a barrel (Barrel) or a chest. */
   public BlockState getStorageBlock() {
      List<BlockState> states = new ArrayList<>();
      states.add(com.nukevscity.rlgen.RLGen.CRATES.get(0).get().defaultBlockState());
      states.add(Blocks.BARREL.defaultBlockState());
      states.add(Blocks.CHEST.defaultBlockState());
      return this.pick(states);
   }

   public BlockState getFunctionalBlock() {
      return this.pick(List.of(Blocks.CRAFTING_TABLE.defaultBlockState(), Blocks.FURNACE.defaultBlockState()));
   }

   public BlockState getDecorationBlock() {
      return Blocks.STONE.defaultBlockState();
   }

   public ItemStack getFramedItem() {
      return new ItemStack(Items.IRON_SWORD);
   }

   public String getLootTable() {
      return "minecraft:simple_dungeon";
   }

   public Map<EquipmentSlot, ItemStack> getWearables() {
      List<Item> head = new ArrayList<>(), chest = new ArrayList<>(), legs = new ArrayList<>(), feet = new ArrayList<>();
      if (this.common()) {
         head.add(Items.LEATHER_HELMET);
      }
      if (this.common()) {
         chest.add(Items.LEATHER_CHESTPLATE);
      }
      if (this.common()) {
         legs.add(Items.LEATHER_LEGGINGS);
      }
      if (this.common()) {
         feet.add(Items.LEATHER_BOOTS);
      }
      return this.wearables(head, chest, legs, feet, new ArrayList<>(), new ArrayList<>());
   }

   protected Map<EquipmentSlot, ItemStack> wearables(List<Item> head, List<Item> chest, List<Item> legs, List<Item> feet, List<Item> mainhand, List<Item> offhand) {
      Map<EquipmentSlot, ItemStack> map = new HashMap<>();
      this.put(map, head, EquipmentSlot.HEAD);
      this.put(map, chest, EquipmentSlot.CHEST);
      this.put(map, legs, EquipmentSlot.LEGS);
      this.put(map, feet, EquipmentSlot.FEET);
      this.put(map, mainhand, EquipmentSlot.MAINHAND);
      this.put(map, offhand, EquipmentSlot.OFFHAND);
      return map;
   }

   private void put(Map<EquipmentSlot, ItemStack> map, List<Item> wearables, EquipmentSlot slot) {
      if (!wearables.isEmpty()) {
         Item item = wearables.get(this.getRand().nextInt(wearables.size()));
         if (item != null) {
            map.put(slot, new ItemStack(item));
         }
      }
   }

   public static class Smith extends Theme {
      public Smith(InnerDecorator d) {
         super(d);
      }

      @Override
      public BlockState getFunctionalBlock() {
         List<BlockState> states = new ArrayList<>();
         states.add(Blocks.CRAFTING_TABLE.defaultBlockState());
         states.add(Blocks.FURNACE.defaultBlockState());
         states.add(Blocks.HOPPER.defaultBlockState());
         states.add(InnerDecorator.anvil(this.getRand().nextInt(2) + 1));
         states.add(cauldron(this.getRand().nextInt(3)));
         return this.pick(states);
      }

      @Override
      public BlockState getDecorationBlock() {
         List<BlockState> states = new ArrayList<>();
         if (this.uncommon()) {
            states.add(Blocks.IRON_BLOCK.defaultBlockState());
         }
         if (this.uncommon()) {
            states.add(Blocks.GOLD_BLOCK.defaultBlockState());
         }
         if (this.common()) {
            states.add(Blocks.IRON_ORE.defaultBlockState());
         }
         if (this.common()) {
            states.add(Blocks.GOLD_ORE.defaultBlockState());
         }
         states.add(Blocks.ANDESITE.defaultBlockState());
         states.add(Blocks.POLISHED_ANDESITE.defaultBlockState());
         states.add(Blocks.DIORITE.defaultBlockState());
         states.add(Blocks.POLISHED_DIORITE.defaultBlockState());
         states.add(Blocks.STONE.defaultBlockState());
         return this.pick(states);
      }

      @Override
      public ItemStack getFramedItem() {
         List<Item> items = new ArrayList<>();
         if (this.valuable()) {
            items.add(Items.DIAMOND);
         }
         if (this.uncommon()) {
            items.add(Items.CHAINMAIL_HELMET);
         }
         if (this.uncommon()) {
            items.add(Items.CHAINMAIL_CHESTPLATE);
         }
         if (this.uncommon()) {
            items.add(Items.CHAINMAIL_LEGGINGS);
         }
         if (this.uncommon()) {
            items.add(Items.CHAINMAIL_HELMET);
         }
         if (Charm.VANILLA_TOOLS) {
            items.add(Items.IRON_SWORD);
         }
         if (Charm.VANILLA_TOOLS) {
            items.add(Items.IRON_AXE);
         }
         items.add(Items.IRON_HELMET);
         items.add(Items.IRON_CHESTPLATE);
         items.add(Items.IRON_LEGGINGS);
         items.add(Items.IRON_BOOTS);
         items.add(Items.CLOCK);
         return new ItemStack(this.pick(items));
      }

      @Override
      public Map<EquipmentSlot, ItemStack> getWearables() {
         List<Item> head = new ArrayList<>(), chest = new ArrayList<>(), legs = new ArrayList<>(), feet = new ArrayList<>(), main = new ArrayList<>(),
            off = new ArrayList<>();
         if (this.valuable()) {
            head.add(Items.DIAMOND_HELMET);
         }
         if (this.valuable()) {
            chest.add(Items.DIAMOND_CHESTPLATE);
         }
         if (this.valuable()) {
            legs.add(Items.DIAMOND_LEGGINGS);
         }
         if (this.valuable()) {
            feet.add(Items.DIAMOND_BOOTS);
         }
         if (this.uncommon()) {
            head.add(Items.CHAINMAIL_HELMET);
         }
         if (this.uncommon()) {
            chest.add(Items.CHAINMAIL_CHESTPLATE);
         }
         if (this.uncommon()) {
            legs.add(Items.CHAINMAIL_LEGGINGS);
         }
         if (this.uncommon()) {
            feet.add(Items.CHAINMAIL_BOOTS);
         }
         if (this.common()) {
            head.add(Items.IRON_HELMET);
         }
         if (this.common()) {
            chest.add(Items.IRON_CHESTPLATE);
         }
         if (this.common()) {
            legs.add(Items.IRON_LEGGINGS);
         }
         if (this.common()) {
            feet.add(Items.IRON_BOOTS);
         }
         if (this.common() && Charm.VANILLA_TOOLS) {
            off.add(Items.SHIELD);
         }
         if (Charm.VANILLA_TOOLS) {
            main.add(Items.STONE_SWORD);
            main.add(Items.STONE_AXE);
            main.add(Items.IRON_SWORD);
            main.add(Items.IRON_AXE);
         }
         return this.wearables(head, chest, legs, feet, main, off);
      }

      @Override
      public String getLootTable() {
         List<String> l = new ArrayList<>();
         if (this.valuable()) {
            l.add("minecraft:chests/abandoned_mineshaft");
         }
         l.add("charm:village/smith");
         return this.pick(l);
      }
   }

   public static class Priest extends Theme {
      public Priest(InnerDecorator d) {
         super(d);
      }

      @Override
      public BlockState getStorageBlock() {
         List<BlockState> states = new ArrayList<>();
         states.add(super.getStorageBlock());
         if (this.valuable()) {
            states.add(Blocks.ENDER_CHEST.defaultBlockState());
         }
         return this.pick(states);
      }

      @Override
      public BlockState getFunctionalBlock() {
         List<BlockState> states = new ArrayList<>();
         if (this.valuable()) {
            states.add(Blocks.ENCHANTING_TABLE.defaultBlockState());
         }
         states.add(Blocks.CRAFTING_TABLE.defaultBlockState());
         states.add(Blocks.BOOKSHELF.defaultBlockState());
         states.add(Blocks.FURNACE.defaultBlockState());
         return this.pick(states);
      }

      @Override
      public BlockState getDecorationBlock() {
         List<BlockState> states = new ArrayList<>();
         if (this.valuable()) {
            states.add(Blocks.QUARTZ_BLOCK.defaultBlockState());
         }
         if (this.valuable()) {
            states.add(Blocks.LAPIS_BLOCK.defaultBlockState());
         }
         if (this.common()) {
            states.add(Blocks.OBSIDIAN.defaultBlockState());
         }
         for (Block b : new Block[]{Blocks.BOOKSHELF, Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA, Blocks.BLUE_GLAZED_TERRACOTTA, Blocks.LIME_GLAZED_TERRACOTTA,
            Blocks.GREEN_GLAZED_TERRACOTTA, Blocks.RED_GLAZED_TERRACOTTA, Blocks.PURPLE_GLAZED_TERRACOTTA, Blocks.ORANGE_GLAZED_TERRACOTTA,
            Blocks.YELLOW_GLAZED_TERRACOTTA}) {
            states.add(b.defaultBlockState());
         }
         return this.pick(states);
      }

      @Override
      public ItemStack getFramedItem() {
         List<Item> items = new ArrayList<>();
         if (this.valuable()) {
            items.add(Items.CHORUS_FRUIT);
         }
         if (this.valuable()) {
            items.add(Items.DIAMOND);
         }
         if (this.valuable()) {
            items.add(Items.ENDER_PEARL);
         }
         if (this.uncommon()) {
            items.add(Items.EXPERIENCE_BOTTLE);
         }
         if (this.uncommon()) {
            items.add(Items.BLAZE_POWDER);
         }
         if (this.common()) {
            items.add(Items.GOLD_INGOT);
         }
         if (Charm.VANILLA_TOOLS) {
            items.add(Items.GOLDEN_AXE);
            items.add(Items.GOLDEN_SHOVEL);
         }
         items.add(Items.GLASS_BOTTLE);
         items.add(Items.CLOCK);
         return new ItemStack(this.pick(items));
      }

      @Override
      public String getLootTable() {
         List<String> l = new ArrayList<>();
         if (this.valuable()) {
            l.add("minecraft:chests/stronghold_crossing");
         }
         l.add("charm:village/priest");
         return this.pick(l);
      }
   }

   public static class Librarian extends Theme {
      public Librarian(InnerDecorator d) {
         super(d);
      }

      @Override
      public BlockState getFunctionalBlock() {
         List<BlockState> states = new ArrayList<>();
         if (this.valuable()) {
            states.add(Blocks.ENCHANTING_TABLE.defaultBlockState());
         }
         states.add(Blocks.CRAFTING_TABLE.defaultBlockState());
         states.add(Blocks.FURNACE.defaultBlockState());
         states.add(Blocks.CHISELED_BOOKSHELF.defaultBlockState());
         return this.pick(states);
      }

      @Override
      public BlockState getDecorationBlock() {
         return this.pick(List.of(Blocks.BOOKSHELF.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_STAIRS.defaultBlockState()));
      }

      @Override
      public ItemStack getFramedItem() {
         // TotemOfReturning is off in RLCraft
         return new ItemStack(this.pick(List.of(Items.WRITABLE_BOOK, Items.BOOK, Items.COMPASS, Items.CLOCK, Items.PAPER, Items.MAP, Items.FEATHER)));
      }

      @Override
      public String getLootTable() {
         List<String> l = new ArrayList<>();
         if (this.valuable()) {
            l.add("minecraft:chests/stronghold_library");
         }
         if (this.valuable()) {
            l.add("minecraft:chests/woodland_mansion");
         }
         l.add("charm:village/librarian");
         return this.pick(l);
      }
   }

   public static class Butcher extends Theme {
      public Butcher(InnerDecorator d) {
         super(d);
      }

      @Override
      public BlockState getFunctionalBlock() {
         List<BlockState> states = new ArrayList<>();
         states.add(Blocks.CRAFTING_TABLE.defaultBlockState());
         states.add(Blocks.FURNACE.defaultBlockState());
         states.add(cauldron(this.getRand().nextInt(3)));
         states.add(Blocks.HOPPER.defaultBlockState());
         return this.pick(states);
      }

      @Override
      public BlockState getDecorationBlock() {
         return this.pick(List.of(Blocks.POLISHED_DIORITE.defaultBlockState(), Blocks.COAL_BLOCK.defaultBlockState()));
      }

      @Override
      public ItemStack getFramedItem() {
         List<Item> items = new ArrayList<>();
         if (this.valuable()) {
            items.add(Items.SADDLE);
         }
         if (this.valuable()) {
            items.add(Items.RABBIT_FOOT);
         }
         if (this.uncommon() && Charm.VANILLA_TOOLS) {
            items.add(Items.IRON_AXE);
         }
         if (this.uncommon() && Charm.VANILLA_TOOLS) {
            items.add(Items.IRON_SWORD);
         }
         if (this.common()) {
            items.add(Items.LEATHER_HELMET);
         }
         if (this.common()) {
            items.add(Items.LEATHER_CHESTPLATE);
         }
         if (this.common()) {
            items.add(Items.LEATHER_LEGGINGS);
         }
         if (this.common()) {
            items.add(Items.LEATHER_BOOTS);
         }
         items.add(Items.LEATHER);
         items.add(Items.RABBIT_HIDE);
         items.add(Items.BEEF);
         items.add(Items.PORKCHOP);
         items.add(Items.COAL);
         return new ItemStack(this.pick(items));
      }

      @Override
      public String getLootTable() {
         return this.pick(List.of("charm:village/butcher"));
      }
   }

   public static class Carpenter extends Theme {
      public Carpenter(InnerDecorator d) {
         super(d);
      }

      @Override
      public BlockState getFunctionalBlock() {
         List<BlockState> states = new ArrayList<>();
         if (this.rare()) {
            states.add(Blocks.STICKY_PISTON.defaultBlockState());
         }
         if (this.uncommon()) {
            states.add(Blocks.NOTE_BLOCK.defaultBlockState());
         }
         if (this.uncommon()) {
            states.add(Blocks.JUKEBOX.defaultBlockState());
         }
         states.add(Blocks.CRAFTING_TABLE.defaultBlockState());
         states.add(Blocks.BOOKSHELF.defaultBlockState());
         states.add(Blocks.PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.UP));
         return this.pick(states);
      }

      @Override
      public BlockState getDecorationBlock() {
         List<BlockState> states = new ArrayList<>();
         states.add(Blocks.BOOKSHELF.defaultBlockState());
         states.add(Blocks.OAK_SLAB.defaultBlockState().setValue(net.minecraft.world.level.block.SlabBlock.TYPE, net.minecraft.world.level.block.state.properties.SlabType.DOUBLE));
         for (Block b : new Block[]{Blocks.OAK_PLANKS, Blocks.SPRUCE_PLANKS, Blocks.JUNGLE_PLANKS, Blocks.DARK_OAK_PLANKS, Blocks.BIRCH_PLANKS, Blocks.ACACIA_PLANKS,
            Blocks.SPRUCE_SLAB, Blocks.JUNGLE_SLAB, Blocks.DARK_OAK_SLAB, Blocks.BIRCH_SLAB, Blocks.ACACIA_SLAB}) {
            states.add(b.defaultBlockState());
         }
         return this.pick(states);
      }

      @Override
      public ItemStack getFramedItem() {
         List<Item> items = new ArrayList<>();
         if (this.common() && Charm.VANILLA_TOOLS) {
            items.add(Items.IRON_AXE);
         }
         if (this.common() && Charm.VANILLA_TOOLS) {
            items.add(Items.IRON_SHOVEL);
         }
         if (Charm.VANILLA_TOOLS) {
            items.add(Items.STONE_AXE);
            items.add(Items.STONE_SHOVEL);
            items.add(Items.WOODEN_SHOVEL);
            items.add(Items.WOODEN_AXE);
         }
         return new ItemStack(this.pick(items));
      }

      @Override
      public String getLootTable() {
         return this.pick(List.of("charm:village/carpenter"));
      }
   }

   public static class Shepherd extends Theme {
      public Shepherd(InnerDecorator d) {
         super(d);
      }

      @Override
      public BlockState getFunctionalBlock() {
         return this.pick(List.of(Blocks.CRAFTING_TABLE.defaultBlockState(), Blocks.FURNACE.defaultBlockState(), Blocks.DISPENSER.defaultBlockState()));
      }

      @Override
      public BlockState getDecorationBlock() {
         return this.pick(woolColours());
      }

      @Override
      public ItemStack getFramedItem() {
         List<Item> items = new ArrayList<>(List.of(Items.WHITE_WOOL, Items.STRING, Items.MUTTON, Items.COOKED_MUTTON));
         if (this.common() && Charm.VANILLA_TOOLS) {
            items.add(Items.SHEARS);
         }
         return new ItemStack(this.pick(items));
      }

      @Override
      public String getLootTable() {
         return this.pick(List.of("charm:village/shepherd"));
      }
   }

   public static class Fisherman extends Theme {
      public Fisherman(InnerDecorator d) {
         super(d);
      }

      @Override
      public BlockState getFunctionalBlock() {
         List<BlockState> states = new ArrayList<>();
         states.add(Blocks.CRAFTING_TABLE.defaultBlockState());
         states.add(Blocks.FURNACE.defaultBlockState());
         states.add(cauldron(this.getRand().nextInt(3)));
         return this.pick(states);
      }

      @Override
      public BlockState getDecorationBlock() {
         return this.pick(woolColours());
      }

      @Override
      public ItemStack getFramedItem() {
         List<Item> items = new ArrayList<>(List.of(Items.COD, Items.COOKED_COD, Items.STRING, Items.LEATHER_BOOTS));
         if (this.common() && Charm.VANILLA_TOOLS) {
            items.add(Items.FISHING_ROD);
         }
         return new ItemStack(this.pick(items));
      }

      @Override
      public String getLootTable() {
         return this.pick(List.of("charm:village/fisherman"));
      }
   }
}
