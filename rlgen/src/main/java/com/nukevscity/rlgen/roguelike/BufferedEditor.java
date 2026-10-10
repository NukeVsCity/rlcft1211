package com.nukevscity.rlgen.roguelike;

import com.github.fnar.minecraft.block.BlockMapper1_12;
import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.Material;
import com.github.fnar.minecraft.block.SingleBlockBrush;
import com.github.fnar.minecraft.block.decorative.Plant;
import com.github.fnar.minecraft.block.decorative.Skull;
import com.github.fnar.minecraft.block.normal.StairsBlock;
import com.github.fnar.minecraft.block.redstone.DoorBlock;
import com.github.fnar.minecraft.block.spawner.SpawnPotentialMapper1_12;
import com.github.fnar.minecraft.block.spawner.Spawner;
import com.github.fnar.minecraft.item.RldItemStack;
import com.github.fnar.minecraft.item.mapper.ItemMapper1_12;
import com.google.gson.JsonElement;
import com.nukevscity.rlgen.legacy.biome.Biome;
import greymerk.roguelike.dungeon.Dungeon;
import greymerk.roguelike.treasure.TreasureChest;
import greymerk.roguelike.treasure.TreasureManager;
import greymerk.roguelike.util.DyeColor;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.MetaBlock1_12;
import greymerk.roguelike.worldgen.PositionInfo;
import greymerk.roguelike.worldgen.VanillaStructure;
import greymerk.roguelike.worldgen.WorldEditor;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Roguelike Dungeons' WorldEditor1_12, writing into a buffer over the 1.12 terrain instead of the live world: a dungeon spans
 * hundreds of blocks, so it is built once and each chunk later pastes its part ({@link RoguelikePiece}).
 */
final class BufferedEditor implements WorldEditor {
   /** A block entity as Roguelike sees it while building: inventory, loot table, spawner data, skull. */
   static final class Tile {
      final ItemStack[] items;
      String lootTable;
      long lootSeed;
      CompoundTag spawner;
      int skullRotation = -1;

      Tile(int size) {
         this.items = new ItemStack[size];
      }
   }

   private final Function<BlockPos, BlockState> terrain;
   private final Function<BlockPos, Biome> biomes;
   private final long seed;
   final Map<Long, BlockState> blocks = new LinkedHashMap<>();
   final Map<Long, Tile> tiles = new HashMap<>();
   private final Map<BlockType, Integer> stats = new HashMap<>();
   private final Random random;
   private final TreasureManager treasureManager;

   BufferedEditor(Function<BlockPos, BlockState> terrain, Function<BlockPos, Biome> biomes, long seed) {
      this.terrain = terrain;
      this.biomes = biomes;
      this.seed = seed;
      this.random = new Random((long)Objects.hash(seed));
      this.treasureManager = new TreasureManager(this.random);
   }

   private static long key(Coord c) {
      return BlockPos.asLong(c.getX(), c.getY(), c.getZ());
   }

   BlockState state(Coord c) {
      BlockState s = this.blocks.get(key(c));
      return s != null ? s : this.terrain.apply(new BlockPos(c.getX(), c.getY(), c.getZ()));
   }

   private void put(Coord c, BlockState s) {
      long k = key(c);
      this.blocks.put(k, s);
      this.tiles.remove(k);
   }

   // ------------------------------------------------------------------ reads

   @Override
   public boolean isSolidBlock(Coord coord) {
      BlockState s = this.state(coord);
      return s.isSolid() && !s.liquid();
   }

   @Override
   public boolean isOpaqueBlock(Coord coord) {
      return this.state(coord).canOcclude();
   }

   @Override
   public boolean isOpaqueCubeBlock(Coord coord) {
      return this.state(coord).isSolidRender(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
   }

   @Override
   public boolean isBlockOfTypeAt(BlockType blockType, Coord coord) {
      return BlockMapper1_12.map(blockType).getBlock() == this.state(coord).getBlock();
   }

   @Override
   public boolean isMaterialAt(Material material, Coord coord) {
      return false;
   }

   @Override
   public boolean isAirBlock(Coord pos) {
      return this.state(pos).isAir();
   }

   /** Material GRASS, GROUND, ROCK, SAND, ICE, PACKED_ICE, SNOW (layer) or CLAY. */
   @Override
   public boolean isValidGroundBlock(Coord pos) {
      BlockState s = this.state(pos);
      Block b = s.getBlock();
      if (b == Blocks.GRASS_BLOCK || b == Blocks.MYCELIUM || b == Blocks.PODZOL || b == Blocks.DIRT || b == Blocks.COARSE_DIRT || b == Blocks.FARMLAND
         || b == Blocks.SAND || b == Blocks.RED_SAND || b == Blocks.GRAVEL || b == Blocks.SOUL_SAND || b == Blocks.CLAY || b == Blocks.ICE
         || b == Blocks.PACKED_ICE || b == Blocks.SNOW) {
         return true;
      }
      ResourceLocation id = BuiltInRegistries.BLOCK.getKey(b);
      if (id.getNamespace().equals("rlgen") && (id.getPath().contains("grass") || id.getPath().contains("dirt") || id.getPath().contains("sand"))) {
         return true;
      }
      return s.is(BlockTags.MINEABLE_WITH_PICKAXE) && s.isSolidRender(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
   }

   @Override
   public Random getRandom() {
      return this.random;
   }

   @Override
   public Random getRandom(Coord pos) {
      this.random.setSeed((long)this.getSeed(pos));
      return this.random;
   }

   @Override
   public long getSeed() {
      return this.seed;
   }

   @Override
   public PositionInfo getInfo(Coord pos) {
      return new PositionInfo() {
         @Override
         public int getDimension() {
            return 0;
         }

         @Override
         public Biome getBiome() {
            return BufferedEditor.this.biomes.apply(new BlockPos(pos.getX(), pos.getY(), pos.getZ()));
         }
      };
   }

   /** Vanilla structures are 1.21 ones here; Roguelike only kept 8 blocks away from them. */
   @Override
   public Coord findNearestStructure(VanillaStructure type, Coord pos) {
      return null;
   }

   @Override
   public Map<BlockType, Integer> getStats() {
      return this.stats;
   }

   // ------------------------------------------------------------------ writes

   @Override
   public boolean setBlock(Coord coord, SingleBlockBrush brush, boolean fillAir, boolean replaceSolid) {
      if (this.isBlockOfTypeAt(BlockType.CHEST, coord) || this.isBlockOfTypeAt(BlockType.TRAPPED_CHEST, coord)
         || this.isBlockOfTypeAt(BlockType.MOB_SPAWNER, coord)) {
         return false;
      }
      boolean isAir = this.isBlockOfTypeAt(BlockType.AIR, coord);
      if (!fillAir && isAir || !replaceSolid && !isAir) {
         return false;
      }
      if (coord.getY() < 0 || coord.getY() > 255) {
         return false;
      }
      MetaBlock1_12 metaBlock = metaBlock(brush);
      this.put(coord, metaBlock.getState());
      BlockType blockType = brush.getBlockType();
      if (blockType != null) {
         this.stats.merge(blockType, 1, Integer::sum);
      }
      return true;
   }

   private static MetaBlock1_12 metaBlock(SingleBlockBrush brush) {
      JsonElement json = brush.getJson();
      if (json == null) {
         return BlockMapper1_12.map(brush);
      } else if (brush instanceof StairsBlock stairs) {
         return BlockMapper1_12.mapStairs(stairs);
      } else if (brush instanceof DoorBlock door) {
         return BlockMapper1_12.mapDoor(door);
      }
      try {
         return new MetaBlock1_12(json);
      } catch (Exception e) {
         return new MetaBlock1_12(Blocks.AIR);
      }
   }

   @Override
   public void fillDown(Coord origin, BlockBrush blocks) {
      Coord cursor = origin.copy();
      while (!this.isOpaqueCubeBlock(cursor) && cursor.getY() > 1) {
         blocks.stroke(this, cursor);
         cursor.down();
      }
   }

   @Override
   public boolean canPlace(SingleBlockBrush block, Coord pos, Direction dir) {
      return this.isAirBlock(pos) && this.isSolidBlock(pos.copy().add(dir));
   }

   private static int containerSize(Block b) {
      if (b == Blocks.CHEST || b == Blocks.TRAPPED_CHEST) {
         return 27;
      } else if (b == Blocks.DISPENSER || b == Blocks.DROPPER) {
         return 9;
      } else if (b == Blocks.HOPPER || b == Blocks.BREWING_STAND) {
         return 5;
      } else if (b == Blocks.FURNACE) {
         return 3;
      }
      return 0;
   }

   @Override
   public Object getTileEntity(Coord pos) {
      BlockState s = this.state(pos);
      if (!s.hasBlockEntity()) {
         return null;
      }
      return this.tiles.computeIfAbsent(key(pos), k -> new Tile(containerSize(s.getBlock())));
   }

   @Override
   public void setItem(Coord pos, int slot, RldItemStack itemStack) {
      ItemStack stack;
      try {
         stack = new ItemMapper1_12().map(itemStack);
      } catch (RuntimeException e) {
         return; // an item from a mod we do not have
      }
      this.setItem(pos, slot, stack);
   }

   @Override
   public void setItem(Coord pos, int slot, ItemStack itemStack) {
      if (this.getTileEntity(pos) instanceof Tile t && slot >= 0 && slot < t.items.length) {
         t.items[slot] = itemStack;
      }
   }

   @Override
   public void setLootTable(Coord pos, String table) {
      if (this.getTileEntity(pos) instanceof Tile t) {
         t.lootTable = table;
         t.lootSeed = this.getSeed(pos);
      }
   }

   @Override
   public int getCapacity(TreasureChest treasureChest) {
      return this.getTileEntity(treasureChest.getCoord()) instanceof Tile t ? t.items.length : 0;
   }

   @Override
   public boolean isEmptySlot(TreasureChest treasureChest, int slot) {
      return this.getTileEntity(treasureChest.getCoord()) instanceof Tile t && slot >= 0 && slot < t.items.length && t.items[slot] == null;
   }

   @Override
   public void generateSpawner(Spawner spawner, Coord cursor) {
      Coord pos = cursor.copy();
      spawner.stroke(this, pos);
      if (this.getTileEntity(pos) instanceof Tile t) {
         CompoundTag nbt = new CompoundTag();
         nbt.put("SpawnPotentials", SpawnPotentialMapper1_12.mapToNbt(spawner.getPotentials(), this.getRandom(), Dungeon.getLevel(pos.getY())));
         t.spawner = nbt;
      }
   }

   @Override
   public void setSkull(WorldEditor editor, Coord cursor, Direction dir, Skull type) {
      SingleBlockBrush skullBlock = BlockType.SKULL.getBrush();
      skullBlock.setFacing(Direction.UP);
      if (skullBlock.stroke(editor, cursor)) {
         int rotation = switch (dir) {
            case EAST -> 4;
            case SOUTH -> 8;
            case WEST -> 12;
            default -> 0;
         };
         rotation = (rotation - 1 + editor.getRandom().nextInt(3)) % 16;
         Block skull = switch (type) {
            case WITHER -> Blocks.WITHER_SKELETON_SKULL;
            case ZOMBIE -> Blocks.ZOMBIE_HEAD;
            case STEVE -> Blocks.PLAYER_HEAD;
            case CREEPER -> Blocks.CREEPER_HEAD;
            default -> Blocks.SKELETON_SKULL;
         };
         // 1.12 stored -1 % 16 = -1 as the rotation, which read back as 15
         this.put(cursor, skull.defaultBlockState().setValue(SkullBlock.ROTATION, Math.floorMod(rotation, 16)));
      }
   }

   /** TileEntityBed.setColor, with 1.12's colour names (light gray fell back to white). */
   @Override
   public void setBedColorAt(Coord cursor, DyeColor color) {
      BlockState s = this.state(cursor);
      if (s.getBlock() instanceof BedBlock) {
         String name = (color == DyeColor.LIGHT_GRAY ? "white" : color.name().toLowerCase(Locale.ROOT)) + "_bed";
         Block bed = BuiltInRegistries.BLOCK.get(ResourceLocation.withDefaultNamespace(name));
         this.blocks.put(key(cursor), bed.defaultBlockState().setValue(BedBlock.FACING, s.getValue(BedBlock.FACING)).setValue(BedBlock.PART, s.getValue(BedBlock.PART)));
      }
   }

   @Override
   public void setFlowerPotContent(Coord pos, Plant choice) {
      if (!this.state(pos).is(Blocks.FLOWER_POT)) {
         return;
      }
      Block potted = switch (choice) {
         case POPPY -> Blocks.POTTED_POPPY;
         case ORCHID -> Blocks.POTTED_BLUE_ORCHID;
         case ALLIUM -> Blocks.POTTED_ALLIUM;
         case BLUET -> Blocks.POTTED_AZURE_BLUET;
         case REDTULIP -> Blocks.POTTED_RED_TULIP;
         case ORANGETULIP -> Blocks.POTTED_ORANGE_TULIP;
         case WHITETULIP -> Blocks.POTTED_WHITE_TULIP;
         case PINKTULIP -> Blocks.POTTED_PINK_TULIP;
         case DAISY -> Blocks.POTTED_OXEYE_DAISY;
         case RED_MUSHROOM -> Blocks.POTTED_RED_MUSHROOM;
         case BROWN_MUSHROOM -> Blocks.POTTED_BROWN_MUSHROOM;
         case CACTUS -> Blocks.POTTED_CACTUS;
         case OAK_SAPLING -> Blocks.POTTED_OAK_SAPLING;
         case SPRUCE_SAPLING -> Blocks.POTTED_SPRUCE_SAPLING;
         case BIRCH_SAPLING -> Blocks.POTTED_BIRCH_SAPLING;
         case JUNGLE_SAPLING -> Blocks.POTTED_JUNGLE_SAPLING;
         case ACACIA_SAPLING -> Blocks.POTTED_ACACIA_SAPLING;
         case DARKOAK_SAPLING -> Blocks.POTTED_DARK_OAK_SAPLING;
         case SHRUB -> Blocks.POTTED_DEAD_BUSH;
         case FERN -> Blocks.POTTED_FERN;
         default -> Blocks.POTTED_DANDELION;
      };
      this.blocks.put(key(pos), potted.defaultBlockState());
   }

   @Override
   public TreasureManager getTreasureManager() {
      return this.treasureManager;
   }

   static boolean hasRotation(BlockState s) {
      return s.hasProperty(BlockStateProperties.ROTATION_16);
   }
}
