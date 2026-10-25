package com.nukevscity.rlgen.charm;

import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.village.VillagePieces;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Meson's MesonInnerDecorator + MesonDecoratorItems with Charm's VillageInnerDecorator and CharmDecoratorItems on top: decorates a
 * village piece right after it was placed in one chunk. 1.12 seeded its random with the piece object's identity hash (different every
 * run); here it is seeded from the piece's box, so every chunk of the piece makes the same choices.
 */
public abstract class InnerDecorator {
   protected final Random rand;
   protected final WorldGenLevel world;
   protected final VillagePieces.Village structure;
   protected final BoundingBox box;
   protected Theme theme;
   public final BlockPos pos;
   public final long villageSeed;
   public final com.nukevscity.rlgen.legacy.biome.Biome biome;
   public int floor = 1;
   public int roof = 5;

   protected InnerDecorator(VillagePieces.Village structure, WorldGenLevel world, BoundingBox box, LegacyChunkGenerator generator) {
      this.structure = structure;
      this.world = world;
      this.box = box;
      BoundingBox bb = structure.getBoundingBox();
      this.rand = new Random(((long)bb.minX() * 341873128712L + (long)bb.minZ() * 132897987541L) ^ ((long)bb.minY() << 20) ^ world.getSeed());
      this.theme = this.makeTheme();
      this.pos = new BlockPos(bb.minX(), bb.minY(), bb.minZ());
      this.villageSeed = Charm.villageSeed(generator.legacyGenerator(), world.getSeed(), this.pos);
      BlockPos nearest = Charm.nearestVillage(generator.legacyGenerator(), world.getSeed(), this.pos);
      this.biome = nearest != null ? generator.legacyBiome(nearest.getX(), nearest.getZ()) : com.nukevscity.rlgen.legacy.biome.Biomes.PLAINS;
   }

   /** VillageInnerDecorator.getThemeClass: a random profession unless the piece has its own. */
   protected Theme makeTheme() {
      switch (this.rand.nextInt(7)) {
         case 0:
            return new Theme.Smith(this);
         case 1:
            return new Theme.Priest(this);
         case 2:
            return new Theme.Librarian(this);
         case 3:
            return new Theme.Butcher(this);
         case 4:
            return new Theme.Carpenter(this);
         case 5:
            return new Theme.Shepherd(this);
         default:
            return new Theme.Fisherman(this);
      }
   }

   public abstract void generate();

   public Random getRand() {
      return this.rand;
   }

   public Random getVillageRand() {
      return new Random(this.villageSeed);
   }

   public boolean chance(float chance) {
      return this.rand.nextFloat() <= chance;
   }

   protected boolean common() {
      return this.chance(Charm.COMMON);
   }

   protected boolean uncommon() {
      return this.chance(Charm.UNCOMMON);
   }

   protected boolean valuable() {
      return this.chance(Charm.VALUABLE);
   }

   protected boolean rare() {
      return this.chance(Charm.RARE);
   }

   // ---- MesonInnerDecorator ----

   public void add(Block block, int x, int y, int z) {
      this.add(block.defaultBlockState(), x, y, z, Direction.NORTH);
   }

   public void add(BlockState state, int x, int y, int z) {
      this.add(state, x, y, z, Direction.NORTH);
   }

   public void add(BlockState state, int x, int y, int z, Direction facing) {
      this.setBlockState(state, new BlockPos(x, y, z), facing);
   }

   public void addRow(List<BlockState> states, int x, int y, int z, Direction direction, Direction facing) {
      BlockPos p = new BlockPos(x, y, z);
      int i = 0;
      for (BlockState state : states) {
         BlockPos offset = p.relative(direction, i++);
         this.add(state, offset.getX(), offset.getY(), offset.getZ(), facing);
      }
   }

   public void fill(Block block, int x0, int y0, int z0, int x1, int y1, int z1, boolean overwrite) {
      this.fill(block.defaultBlockState(), x0, y0, z0, x1, y1, z1, Direction.NORTH, overwrite);
   }

   public void fill(BlockState state, int x0, int y0, int z0, int x1, int y1, int z1, Direction facing, boolean overwrite) {
      for (int y = y0; y <= y1; y++) {
         for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
               BlockPos place = new BlockPos(x, y, z);
               if (overwrite || this.getBlockStateFromPos(place).isAir()) {
                  this.setBlockState(state, place, facing);
               }
            }
         }
      }
   }

   /** addTileEntity: places the block (if nothing with a block entity is there) and returns its block entity. */
   @Nullable
   public BlockEntity addTileEntity(BlockPos p, BlockState state, Direction facing) {
      BlockPos place = this.getRelativePos(p);
      if (this.isNotInBox(place)) {
         return null;
      }
      if (this.world.getBlockEntity(place) != null) {
         return null;
      }
      this.setBlockState(state, p, facing);
      return this.world.getBlockEntity(place);
   }

   public void setBlockState(BlockState state, BlockPos p, Direction facing) {
      BlockPos place = this.getRelativePos(p);
      if (this.box.isInside(place)) {
         state = rotated(facing, state);
         this.world.setBlock(place, rotated(this.structure.getOrientation(), state), 2);
      }
   }

   public BlockState getBlockStateFromPos(BlockPos p) {
      BlockPos place = this.getRelativePos(p);
      return this.box.isInside(place) || this.world.hasChunk(place.getX() >> 4, place.getZ() >> 4) ? this.world.getBlockState(place) : Blocks.AIR.defaultBlockState();
   }

   public int getXWithOffset(int x, int z) {
      Direction f = this.structure.getOrientation();
      BoundingBox bb = this.structure.getBoundingBox();
      if (f == null) {
         return x;
      }
      return switch (f) {
         case NORTH, SOUTH -> bb.minX() + x;
         case WEST -> bb.maxX() - z;
         case EAST -> bb.minX() + z;
         default -> x;
      };
   }

   public int getYWithOffset(int y) {
      return this.structure.getOrientation() == null ? y : y + this.structure.getBoundingBox().minY();
   }

   public int getZWithOffset(int x, int z) {
      Direction f = this.structure.getOrientation();
      BoundingBox bb = this.structure.getBoundingBox();
      if (f == null) {
         return z;
      }
      return switch (f) {
         case NORTH -> bb.maxZ() - z;
         case SOUTH -> bb.minZ() + z;
         case WEST, EAST -> bb.minZ() + x;
         default -> z;
      };
   }

   public BlockPos getRelativePos(BlockPos p) {
      return new BlockPos(this.getXWithOffset(p.getX(), p.getZ()), this.getYWithOffset(p.getY()), this.getZWithOffset(p.getX(), p.getZ()));
   }

   private static Mirror mirror(@Nullable Direction axis) {
      return axis == Direction.SOUTH || axis == Direction.WEST ? Mirror.LEFT_RIGHT : Mirror.NONE;
   }

   private static Rotation rotation(@Nullable Direction axis) {
      return axis == Direction.WEST || axis == Direction.EAST ? Rotation.CLOCKWISE_90 : Rotation.NONE;
   }

   /** getRotatedState: the mirror and rotation a piece facing "axis" applies. */
   public static BlockState rotated(@Nullable Direction axis, BlockState original) {
      return original.mirror(mirror(axis)).rotate(rotation(axis));
   }

   public Direction getRelativeFacing(Direction facing) {
      Direction axis = this.structure.getOrientation();
      return rotation(axis).getRotated(mirror(axis).getRotation(facing)).rotate(facing);
   }

   public float getRotatedAngle(@Nullable Direction axis, float angle) {
      if (axis == null) {
         return angle;
      }
      return switch (axis) {
         case SOUTH -> angle + 270.0F;
         case WEST -> angle + 180.0F;
         case EAST -> angle + 90.0F;
         default -> angle;
      };
   }

   public boolean isNotInBox(BlockPos p) {
      return !this.box.isInside(p);
   }

   // ---- MesonDecoratorItems / CharmDecoratorItems ----

   public void addStorageBlock(int x, int y, int z, Direction facing) {
      this.addStorageBlock(this.theme.getStorageBlock(), x, y, z, facing);
   }

   public void addStorageBlock(BlockState type, int x, int y, int z, Direction facing) {
      this.addStorageBlock(type, x, y, z, facing, this.theme.getLootTable(), 0);
   }

   public void addStorageBlock(BlockState type, int x, int y, int z, Direction facing, String loot, int lootSize) {
      BlockEntity tile = this.addTileEntity(new BlockPos(x, y, z), type, facing);
      if (tile != null) {
         CharmLoot.fill(this.world, tile, loot, lootSize, this.world.getRandom().nextLong());
      }
   }

   public void addFunctionalBlock(int x, int y, int z, Direction facing) {
      this.add(this.theme.getFunctionalBlock(), x, y, z, facing);
   }

   public void addDecorationBlock(int x, int y, int z, Direction facing) {
      this.add(this.theme.getDecorationBlock(), x, y, z, facing);
   }

   public void addRandomBlock(int x, int y, int z, Direction facing) {
      if (this.rand.nextFloat() <= 0.5F) {
         this.addFunctionalBlock(x, y, z, facing);
      } else {
         this.addDecorationBlock(x, y, z, facing);
      }
   }

   public void addArmorStand(int x, int y, int z, float angle) {
      this.addArmorStand(new BlockPos(x, y, z), angle, this.theme.getWearables());
   }

   public void addArmorStand(BlockPos p, float angle, @Nullable Map<EquipmentSlot, ItemStack> wearing) {
      BlockPos place = this.getRelativePos(p);
      if (this.isNotInBox(place)) {
         return;
      }
      ArmorStand stand = new ArmorStand(this.world.getLevel(), place.getX() + 0.5, place.getY(), place.getZ() + 0.5);
      if (wearing != null) {
         for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND,
            EquipmentSlot.OFFHAND}) {
            if (wearing.get(slot) != null) {
               stand.setItemSlot(slot, wearing.get(slot));
            }
         }
      }
      this.world.setBlock(place, Blocks.AIR.defaultBlockState(), 3);
      if (this.box.isInside(place.above())) {
         this.world.setBlock(place.above(), Blocks.AIR.defaultBlockState(), 3);
      }
      stand.moveTo(place.getX() + 0.5, place.getY(), place.getZ() + 0.5, this.getRotatedAngle(this.structure.getOrientation(), angle), 0.0F);
      this.world.addFreshEntity(stand);
   }

   public void addAnvil(int x, int y, int z, int damage, @Nullable Direction facing) {
      this.add(anvil(damage), x, y, z, facing);
   }

   static BlockState anvil(int damage) {
      return (damage == 0 ? Blocks.ANVIL : damage == 1 ? Blocks.CHIPPED_ANVIL : Blocks.DAMAGED_ANVIL).defaultBlockState();
   }

   public void addFramedItem(int x, int y, int z, Direction direction) {
      this.addFramedItem(new BlockPos(x, y, z), direction, this.theme.getFramedItem());
   }

   public void addFramedItem(BlockPos p, Direction facing, @Nullable ItemStack displayedItem) {
      BlockPos place = this.getRelativePos(p);
      if (this.isNotInBox(place)) {
         return;
      }
      ItemFrame frame = new ItemFrame(this.world.getLevel(), place, this.getRelativeFacing(facing));
      if (displayedItem != null) {
         frame.setItem(displayedItem, false);
      }
      this.world.addFreshEntity(frame);
   }

   /** Quark's glass (invisible) item frame, facing up, on top of the anvil. */
   public void addGlassFramedItem(int x, int y, int z, @Nullable ItemStack displayedItem) {
      BlockPos place = this.getRelativePos(new BlockPos(x, y, z));
      if (this.isNotInBox(place)) {
         return;
      }
      ItemFrame frame = new ItemFrame(this.world.getLevel(), place, this.getRelativeFacing(Direction.UP));
      frame.setInvisible(true);
      if (displayedItem != null) {
         frame.setItem(displayedItem, false);
      }
      this.world.addFreshEntity(frame);
   }

   public void addRug(int floor, int x0, int z0, int x1, int z1, @Nullable Random r) {
      if (r == null) {
         r = this.rand;
      }
      // EnumDyeColor.byDyeDamage: dye damage 0 is black, 15 white
      BlockState carpet1 = Theme.carpet(DyeColor.byId(15 - r.nextInt(15)));
      BlockState carpet2 = Theme.carpet(DyeColor.byId(15 - r.nextInt(15)));
      int style = r.nextInt(3) + 1;
      int i = 0;
      for (int xx = x0; xx <= x1; xx++) {
         for (int zz = z0; zz <= z1; zz++) {
            this.add(i % style == 0 ? carpet1 : carpet2, xx, floor, zz);
            i++;
         }
      }
   }

   /** CharmDecoratorItems.addStand: a plank with a cake, flower pot or (never, nextInt(2)) lantern on it. */
   public void addStand(int x, int y, int z, Direction facing, @Nullable BlockState onTop) {
      if (onTop == null) {
         switch (this.rand.nextInt(2)) {
            case 0:
               onTop = this.getCake();
               break;
            default:
               onTop = this.getFlowerPot();
         }
      }
      this.add(Blocks.OAK_PLANKS.defaultBlockState(), x, y, z, facing);
      this.add(onTop, x, y + 1, z, facing);
   }

   /** BlockTorch: facing null is the default (up), otherwise a wall torch facing that way. */
   public void addTorch(int x, int y, int z, @Nullable Direction facing) {
      BlockState torch = facing == null || facing == Direction.UP ? Blocks.TORCH.defaultBlockState()
         : Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, facing);
      this.add(torch, x, y, z);
   }

   /** Charm's iron lantern (Future MC is not in RLCraft). */
   public void addLantern(int x, int y, int z, boolean hanging) {
      this.add(Blocks.LANTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LanternBlock.HANGING, hanging), x, y, z, Direction.NORTH);
   }

   /** Head then foot of a (red, the default colour) bed. */
   public List<BlockState> getBed() {
      List<BlockState> bed = new ArrayList<>();
      bed.add(Blocks.RED_BED.defaultBlockState().setValue(BedBlock.PART, BedPart.HEAD));
      bed.add(Blocks.RED_BED.defaultBlockState().setValue(BedBlock.PART, BedPart.FOOT));
      return bed;
   }

   public List<BlockState> getDoor(Block door) {
      List<BlockState> d = new ArrayList<>();
      d.add(door.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
      d.add(door.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
      return d;
   }

   /** CharmDecoratorItems.getCake: one of the FlavoredCake cakes (they look like a plain cake here) with 0 or 1 bites. */
   public BlockState getCake() {
      this.rand.nextInt(FLAVORED_CAKES);
      return Blocks.CAKE.defaultBlockState().setValue(CakeBlock.BITES, this.rand.nextInt(2));
   }

   /** Charm FlavoredCake.cakeTypes: one per potion it registered. */
   private static final int FLAVORED_CAKES = 13;

   public BlockState getFlowerPot() {
      return getFlowerPot(this.rand.nextInt(14) + 1);
   }

   /** BlockFlowerPot legacy data (createNewTileEntity's item for each). */
   public static BlockState getFlowerPot(int flowerType) {
      Block b = switch (flowerType) {
         case 1 -> Blocks.POTTED_POPPY;
         case 2 -> Blocks.POTTED_DANDELION;
         case 3 -> Blocks.POTTED_OAK_SAPLING;
         case 4 -> Blocks.POTTED_SPRUCE_SAPLING;
         case 5 -> Blocks.POTTED_BIRCH_SAPLING;
         case 6 -> Blocks.POTTED_JUNGLE_SAPLING;
         case 7 -> Blocks.POTTED_RED_MUSHROOM;
         case 8 -> Blocks.POTTED_BROWN_MUSHROOM;
         case 9 -> Blocks.POTTED_CACTUS;
         case 10 -> Blocks.POTTED_DEAD_BUSH;
         case 11 -> Blocks.POTTED_FERN;
         case 12 -> Blocks.POTTED_ACACIA_SAPLING;
         case 13 -> Blocks.POTTED_DARK_OAK_SAPLING;
         default -> Blocks.FLOWER_POT;
      };
      return b.defaultBlockState();
   }

   static ResourceKey<LootTable> key(String id) {
      return ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(id));
   }

}
