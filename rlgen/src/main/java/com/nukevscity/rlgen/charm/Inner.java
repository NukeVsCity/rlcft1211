package com.nukevscity.rlgen.charm;

import com.nukevscity.rlgen.village.VillagePieces;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Charm's VillageInnerDecorator subclasses and VillageDecorations.onAddComponentParts, which picked one for each vanilla village piece
 * right after it was built (Field1 and Field2 swapped, as in Charm).
 */
public final class Inner {
   private Inner() {
   }

   public static void decorate(VillagePieces.Village piece, WorldGenLevel world, BoundingBox box, LegacyChunkGenerator generator) {
      InnerDecorator d = null;
      if (piece.getClass() == VillagePieces.Church.class) {
         d = new Church(piece, world, box, generator);
      } else if (piece.getClass() == VillagePieces.Field2.class) {
         d = new Field1(piece, world, box, generator);
      } else if (piece.getClass() == VillagePieces.Field1.class) {
         d = new Field2(piece, world, box, generator);
      } else if (piece.getClass() == VillagePieces.Hall.class) {
         d = new Hall(piece, world, box, generator);
      } else if (piece.getClass() == VillagePieces.House1.class) {
         d = new House1(piece, world, box, generator);
      } else if (piece.getClass() == VillagePieces.House2.class) {
         d = new House2(piece, world, box, generator);
      } else if (piece.getClass() == VillagePieces.House3.class) {
         d = new House3(piece, world, box, generator);
      } else if (piece.getClass() == VillagePieces.House4Garden.class) {
         d = new House4(piece, world, box, generator);
      } else if (piece.getClass() == VillagePieces.WoodHut.class) {
         d = new WoodHut(piece, world, box, generator);
      }
      if (piece instanceof VillagePieces.Well) {
         d = new Well(piece, world, box, generator);
      }
      if (d != null) {
         d.generate();
      }
   }

   static class Church extends InnerDecorator {
      Church(VillagePieces.Village s, WorldGenLevel w, BoundingBox b, LegacyChunkGenerator g) {
         super(s, w, b, g);
      }

      @Override
      protected Theme makeTheme() {
         return new Theme.Priest(this);
      }

      @Override
      public void generate() {
         if (Charm.FUNCTIONAL_BLOCKS && this.valuable()) {
            this.add(Blocks.ENCHANTING_TABLE, 2, 2, 7);
         }
         if (Charm.TORCHES) {
            if (this.common()) {
               this.addLantern(2, this.floor + 3, 6, true);
            }
            this.addTorch(3, 7, 1, Direction.WEST);
            this.addTorch(2, 10, 2, null);
         }
         if (Charm.DECORATIVE_BLOCKS) {
            if (this.common()) {
               this.addRow(this.getBed(), 3, 5, 2, Direction.NORTH, Direction.NORTH);
            }
            if (this.common()) {
               this.addRow(this.getBed(), 2, 5, 2, Direction.NORTH, Direction.NORTH);
            }
         }
         if (Charm.STORAGE && this.common()) {
            this.addStorageBlock(3, 10, 1, Direction.NORTH);
         }
      }
   }

   static class Field1 extends InnerDecorator {
      Field1(VillagePieces.Village s, WorldGenLevel w, BoundingBox b, LegacyChunkGenerator g) {
         super(s, w, b, g);
      }

      @Override
      public void generate() {
         if (Charm.TORCHES) {
            this.addTorch(3, 1, 0, null);
            this.addTorch(3, 1, 8, null);
         }
      }
   }

   static class Field2 extends InnerDecorator {
      Field2(VillagePieces.Village s, WorldGenLevel w, BoundingBox b, LegacyChunkGenerator g) {
         super(s, w, b, g);
      }

      @Override
      public void generate() {
         if (Charm.TORCHES) {
            this.addTorch(3, 1, 0, null);
            this.addTorch(3, 1, 8, null);
            this.addTorch(9, 1, 0, null);
            this.addTorch(9, 1, 8, null);
         }
      }
   }

   static class Hall extends InnerDecorator {
      Hall(VillagePieces.Village s, WorldGenLevel w, BoundingBox b, LegacyChunkGenerator g) {
         super(s, w, b, g);
      }

      @Override
      protected Theme makeTheme() {
         return new Theme.Butcher(this);
      }

      @Override
      public void generate() {
         if (Charm.STORAGE && this.uncommon()) {
            this.addStorageBlock(3, this.floor, 4, Direction.SOUTH);
         }
         if (Charm.FUNCTIONAL_BLOCKS && this.common()) {
            if (this.chance(0.5F)) {
               this.addFunctionalBlock(4, this.floor, 4, Direction.SOUTH);
            } else {
               this.addStand(4, this.floor, 4, Direction.SOUTH, null);
            }
         }
         if (Charm.ITEM_FRAMES) {
            if (this.common()) {
               this.addFramedItem(5, this.floor + 2, 4, Direction.SOUTH);
            }
            if (this.common()) {
               this.addFramedItem(7, this.floor + 2, 1, Direction.WEST);
            }
            if (this.common()) {
               this.addFramedItem(3, this.floor + 2, 1, Direction.NORTH);
            }
         }
         if (Charm.ARMOR_STANDS && this.uncommon()) {
            this.addArmorStand(7, this.floor, 4, 215.0F);
         }
         if (Charm.DECORATIVE_BLOCKS) {
            this.fill(Blocks.FARMLAND.defaultBlockState(), 3, 0, 6, 7, 0, 9, Direction.UP, true);
            for (int xx = 3; xx <= 7; xx++) {
               for (int zz = 6; zz <= 9; zz++) {
                  if (this.rand.nextFloat() < 0.4F) {
                     this.add(((CropBlock)Blocks.WHEAT).getStateForAge(6 + this.rand.nextInt(1)), xx, this.floor, zz);
                  }
               }
            }
            this.add(Blocks.WATER.defaultBlockState(), 5, 0, 8);
            this.add(Blocks.AIR.defaultBlockState(), 5, 1, 8);
         }
         if (Charm.TORCHES) {
            this.addTorch(2, this.floor + 1, 10, null);
            this.addTorch(8, this.floor + 1, 10, null);
         }
      }
   }

   static class House1 extends InnerDecorator {
      House1(VillagePieces.Village s, WorldGenLevel w, BoundingBox b, LegacyChunkGenerator g) {
         super(s, w, b, g);
      }

      @Override
      protected Theme makeTheme() {
         return new Theme.Librarian(this);
      }

      @Override
      public void generate() {
         if (Charm.STORAGE) {
            if (this.common()) {
               this.addStorageBlock(7, 1, 2, Direction.WEST);
            }
            // BookshelfChest (9 book slots) is a 1.21 chiseled bookshelf
            BlockState type = Blocks.CHISELED_BOOKSHELF.defaultBlockState();
            for (int i = 0; i < 7; i++) {
               if (this.uncommon()) {
                  int numberOfBooks = this.rand.nextInt(4) + 1;
                  this.addStorageBlock(type, i + 1, 3, 4, Direction.SOUTH, "charm:treasure/common_books", numberOfBooks);
               }
            }
         }
         if (Charm.TORCHES) {
            this.addTorch(1, 4, 2, Direction.EAST);
            this.addTorch(1, 4, 3, Direction.EAST);
            this.addTorch(7, 4, 2, Direction.WEST);
            this.addTorch(7, 4, 3, Direction.WEST);
         }
         if (Charm.ITEM_FRAMES) {
            if (this.common()) {
               this.addFramedItem(7, 2, 1, Direction.WEST);
            }
            if (this.common()) {
               this.addFramedItem(1, 3, 1, Direction.NORTH);
            }
         }
      }
   }

   static class House2 extends InnerDecorator {
      House2(VillagePieces.Village s, WorldGenLevel w, BoundingBox b, LegacyChunkGenerator g) {
         super(s, w, b, g);
      }

      @Override
      protected Theme makeTheme() {
         return new Theme.Smith(this);
      }

      @Override
      public void generate() {
         if (Charm.FUNCTIONAL_BLOCKS) {
            this.addAnvil(8, this.floor, 1, this.chance(0.5F) ? 1 : 0, Direction.EAST);
            // Quark's glass item frame with a tool on the anvil
            if (this.common()) {
               List<ItemStack> anvilItems = List.of(new ItemStack(Items.IRON_SWORD), new ItemStack(Items.IRON_AXE), new ItemStack(Items.IRON_SHOVEL),
                  new ItemStack(Items.IRON_PICKAXE));
               this.addGlassFramedItem(8, this.floor + 1, 1, anvilItems.get(this.rand.nextInt(anvilItems.size())));
            }
         }
         if (Charm.DECORATIVE_BLOCKS && this.common()) {
            this.fill(Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM), 6, 5, 4, 8, 5, 6, Direction.NORTH, true);
            this.add(Blocks.AIR, 7, 4, 5);
            this.add(Blocks.AIR, 8, 4, 5);
            this.add(Blocks.AIR, 7, 5, 5);
            this.add(Blocks.AIR, 8, 5, 5);
            this.add(Blocks.MAGMA_BLOCK, 7, 3, 5);
            this.add(Blocks.MAGMA_BLOCK, 8, 3, 5);
         }
         if (Charm.TORCHES) {
            this.addTorch(1, 3, 3, Direction.EAST);
            this.addTorch(3, 3, 5, Direction.SOUTH);
         }
         if (Charm.ARMOR_STANDS && this.uncommon()) {
            this.addArmorStand(1, this.floor, 1, 40.0F);
         }
      }
   }

   static class House3 extends InnerDecorator {
      House3(VillagePieces.Village s, WorldGenLevel w, BoundingBox b, LegacyChunkGenerator g) {
         super(s, w, b, g);
      }

      @Override
      public void generate() {
         float r = this.rand.nextFloat();
         if (r <= 0.25F) {
            if (Charm.BEDS && Charm.DECORATIVE_BLOCKS) {
               this.addRow(this.getBed(), 4, this.floor, 9, Direction.NORTH, Direction.NORTH);
               this.addRow(this.getBed(), 6, this.floor, 9, Direction.NORTH, Direction.NORTH);
               this.add(Blocks.CRAFTING_TABLE.defaultBlockState(), 5, this.floor, 9);
               this.add(Blocks.BOOKSHELF.defaultBlockState(), 3, this.floor, 9);
               this.add(Blocks.BOOKSHELF.defaultBlockState(), 7, this.floor, 9);
            }
         } else if (r <= 0.5F) {
            if (Charm.BEDS && Charm.DECORATIVE_BLOCKS) {
               this.addRow(this.getBed(), 4, this.floor, 9, Direction.WEST, Direction.EAST);
               this.add(Blocks.BOOKSHELF.defaultBlockState(), 5, this.floor, 9);
               this.addRow(this.getBed(), 6, this.floor, 9, Direction.EAST, Direction.WEST);
               if (this.common()) {
                  this.addRandomBlock(6, this.floor, 1, Direction.SOUTH);
               }
               if (this.common()) {
                  this.addRandomBlock(7, this.floor, 1, Direction.SOUTH);
               }
            }
         } else if (r <= 0.75F) {
            if (Charm.FUNCTIONAL_BLOCKS && Charm.DECORATIVE_BLOCKS) {
               if (this.common()) {
                  this.addFunctionalBlock(3, this.floor, 9, Direction.SOUTH);
               }
               if (this.common()) {
                  this.addFunctionalBlock(4, this.floor, 9, Direction.SOUTH);
               }
               if (this.uncommon()) {
                  this.addStorageBlock(5, this.floor, 9, Direction.SOUTH);
               }
               if (this.common()) {
                  this.addFunctionalBlock(6, this.floor, 9, Direction.SOUTH);
               }
               if (this.common()) {
                  this.addFunctionalBlock(7, this.floor, 9, Direction.SOUTH);
               }
            }
         } else if (Charm.STORAGE) {
            if (this.common()) {
               this.addStorageBlock(5, this.floor, 9, Direction.SOUTH);
            }
            if (this.common()) {
               this.addStorageBlock(6, this.floor, 9, Direction.SOUTH);
            }
         }
         if (Charm.DECORATIVE_BLOCKS && Charm.FUNCTIONAL_BLOCKS) {
            if (this.common()) {
               this.addStand(7, this.floor, 5, Direction.WEST, null);
            }
            if (this.common()) {
               this.addRandomBlock(7, this.floor, 6, Direction.WEST);
            }
            if (this.common()) {
               this.addStand(4, this.floor, 1, Direction.NORTH, null);
            }
            if (this.common()) {
               this.addRandomBlock(5, this.floor, 1, Direction.NORTH);
            }
         }
         if (Charm.STORAGE) {
            if (this.uncommon()) {
               this.addStorageBlock(1, this.floor, 2, Direction.EAST);
            }
            if (this.uncommon()) {
               this.addStorageBlock(1, this.floor, 3, Direction.EAST);
            }
         }
         if (Charm.ITEM_FRAMES) {
            if (this.uncommon()) {
               this.addFramedItem(6, this.floor + 1, 1, Direction.NORTH);
            }
            if (this.uncommon()) {
               this.addFramedItem(4, this.floor + 1, 9, Direction.SOUTH);
            }
            if (this.uncommon()) {
               this.addFramedItem(6, this.floor + 1, 9, Direction.SOUTH);
            }
         }
         if (Charm.FUNCTIONAL_BLOCKS) {
            if (this.common()) {
               this.addFunctionalBlock(7, this.floor, 2, Direction.WEST);
            }
            if (this.common()) {
               this.addFunctionalBlock(7, this.floor, 3, Direction.WEST);
            }
         }
         if (Charm.CARPET) {
            this.addRug(this.floor, 3, 3, 6, 6, null);
         }
         if (Charm.TORCHES) {
            this.addTorch(5, 3, 9, Direction.SOUTH);
            this.addTorch(7, 3, 5, Direction.WEST);
            this.addTorch(3, 3, 5, Direction.EAST);
            if (this.uncommon()) {
               this.addLantern(5, 5, 5, true);
            }
         }
      }
   }

   static class House4 extends InnerDecorator {
      House4(VillagePieces.Village s, WorldGenLevel w, BoundingBox b, LegacyChunkGenerator g) {
         super(s, w, b, g);
      }

      @Override
      public void generate() {
         if (this.chance(0.5F)) {
            if (Charm.BEDS) {
               this.addRow(this.getBed(), 2, 1, 3, Direction.WEST, Direction.EAST);
            }
            if (Charm.CARPET) {
               this.addRug(1, 1, 2, 2, 2, null);
            }
         } else if (Charm.STORAGE) {
            if (this.uncommon()) {
               this.addStorageBlock(1, 1, 3, Direction.SOUTH);
            }
            if (this.uncommon()) {
               this.addStorageBlock(2, 1, 3, Direction.SOUTH);
            }
         }
         // MoreVillageBiomes (doors) is off in RLCraft: common() is still drawn
         this.common();
         if (((VillagePieces.House4Garden)this.structure).roofAccessible()) {
            if (Charm.STORAGE) {
               if (this.uncommon()) {
                  this.addStorageBlock(1, 5, 3, Direction.EAST);
               }
               if (this.uncommon()) {
                  this.addStorageBlock(3, 5, 1, Direction.NORTH);
               }
            }
         } else if (Charm.ITEM_FRAMES && this.common()) {
            this.addFramedItem(3, 3, 3, Direction.SOUTH);
         }
         if (Charm.TORCHES) {
            this.addTorch(0, 6, 0, null);
            this.addTorch(4, 6, 4, null);
            if (this.uncommon()) {
               this.addLantern(2, this.floor + 2, 2, true);
            }
         }
         if (Charm.ITEM_FRAMES) {
            if (this.common()) {
               this.addFramedItem(1, 3, 3, Direction.SOUTH);
            }
            if (this.common()) {
               this.addFramedItem(1, 3, 1, Direction.NORTH);
            }
            if (this.common()) {
               this.addFramedItem(3, 3, 1, Direction.NORTH);
            }
         }
         if (Charm.ARMOR_STANDS && this.valuable()) {
            this.addArmorStand(3, this.floor, 1, 135.0F);
         }
      }
   }

   static class Well extends InnerDecorator {
      Well(VillagePieces.Village s, WorldGenLevel w, BoundingBox b, LegacyChunkGenerator g) {
         super(s, w, b, g);
      }

      @Override
      public void generate() {
         if (Charm.CARPET) {
            this.addRug(16, 1, 1, 4, 4, this.getVillageRand());
         }
         if (this.common()) {
            BlockState ore = switch (this.getRand().nextInt(4)) {
               case 0 -> Blocks.IRON_ORE.defaultBlockState();
               case 1 -> Blocks.GOLD_ORE.defaultBlockState();
               case 2 -> Blocks.EMERALD_ORE.defaultBlockState();
               default -> Blocks.DIAMOND_ORE.defaultBlockState();
            };
            this.add(ore, 2, 0, 2, Direction.NORTH);
         }
      }
   }

   static class WoodHut extends InnerDecorator {
      WoodHut(VillagePieces.Village s, WorldGenLevel w, BoundingBox b, LegacyChunkGenerator g) {
         super(s, w, b, g);
         this.roof = 5;
      }

      @Override
      public void generate() {
         if (this.common()) {
            this.fill(Blocks.COBBLESTONE, 1, 0, 1, 2, 0, 3, true);
         }
         if (this.chance(0.5F)) {
            if (Charm.BEDS) {
               this.addRow(this.getBed(), 2, 1, 3, Direction.WEST, Direction.EAST);
               this.add(Blocks.AIR, 1, 2, 3);
               this.add(Blocks.AIR, 2, 2, 3);
            }
            if (Charm.CARPET) {
               this.addRug(1, 1, 2, 2, 2, null);
            }
         } else if (Charm.STORAGE && this.uncommon()) {
            this.addStorageBlock(1, 1, 3, Direction.SOUTH);
            this.add(Blocks.AIR, 1, 2, 3);
         }
         if (this.chance(0.5F)) {
            if (Charm.FUNCTIONAL_BLOCKS && this.common()) {
               if (this.chance(0.5F)) {
                  this.addFunctionalBlock(2, 1, 1, Direction.EAST);
               } else {
                  this.addStand(2, 1, 1, Direction.EAST, null);
               }
            }
         } else if (Charm.ARMOR_STANDS && this.valuable()) {
            this.addArmorStand(2, this.floor, 1, 135.0F);
         }
         if (Charm.ITEM_FRAMES && this.common()) {
            this.addFramedItem(1, this.floor + 2, 3, Direction.SOUTH);
         }
         if (Charm.TORCHES) {
            this.addTorch(2, this.floor + 2, 3, Direction.SOUTH);
         }
      }
   }
}
