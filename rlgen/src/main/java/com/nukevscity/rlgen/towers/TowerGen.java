package com.nukevscity.rlgen.towers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.SlabType;

/** Battle Towers (AtomicStryker, 1.12.2) AS_WorldGenTower. */
final class TowerGen {
   /** Where the tower is drawn: the live chunk, everything else dropped (every chunk of the tower replays the same build). */
   interface View {
      BlockState get(int x, int y, int z);

      void set(int x, int y, int z, BlockState state);

      boolean inside(int x, int y, int z);

      BlockEntity blockEntity(int x, int y, int z);

      ServerLevel level();
   }

   enum Type {
      NULL(Blocks.AIR, Blocks.AIR, Blocks.AIR.defaultBlockState(), Blocks.AIR),
      COBBLESTONE(Blocks.COBBLESTONE, Blocks.TORCH, doubleSlab(Blocks.SMOOTH_STONE_SLAB), Blocks.COBBLESTONE_STAIRS),
      COBBLESTONE_MOSSY(Blocks.MOSSY_COBBLESTONE, Blocks.TORCH, doubleSlab(Blocks.SMOOTH_STONE_SLAB), Blocks.COBBLESTONE_STAIRS),
      SANDSTONE(Blocks.SANDSTONE, Blocks.TORCH, doubleSlab(Blocks.SANDSTONE_SLAB), Blocks.SANDSTONE_STAIRS),
      ICE(Blocks.ICE, Blocks.AIR, Blocks.CLAY.defaultBlockState(), Blocks.OAK_STAIRS),
      SMOOTHSTONE(Blocks.STONE, Blocks.TORCH, doubleSlab(Blocks.COBBLESTONE_SLAB), Blocks.COBBLESTONE_STAIRS),
      NETHERRACK(Blocks.NETHERRACK, Blocks.GLOWSTONE, Blocks.SOUL_SAND.defaultBlockState(), Blocks.NETHER_BRICK_STAIRS),
      JUNGLE(Blocks.MOSSY_COBBLESTONE, Blocks.COBWEB, Blocks.DIRT.defaultBlockState(), Blocks.JUNGLE_STAIRS);

      final Block wall;
      final Block light;
      /** double_stone_slab with the type's meta (0 stone, 1 sandstone, 3 cobblestone); clay and soul sand for ice and nether. */
      final BlockState floor;
      final Block stairs;

      Type(Block wall, Block light, BlockState floor, Block stairs) {
         this.wall = wall;
         this.light = light;
         this.floor = floor;
         this.stairs = stairs;
      }

      /** towerFloorBlockID.getDefaultState(): double_stone_slab meta 0 is the stone one whatever the tower. */
      BlockState floorDefault() {
         return this.floor.getBlock() instanceof SlabBlock ? doubleSlab(Blocks.SMOOTH_STONE_SLAB) : this.floor.getBlock().defaultBlockState();
      }

      /** getBlock() == towerFloorBlockID (any double_stone_slab variant counts) */
      boolean isFloorBlock(BlockState s) {
         if (this.floor.getBlock() instanceof SlabBlock) {
            return (s.is(Blocks.SMOOTH_STONE_SLAB) || s.is(Blocks.SANDSTONE_SLAB) || s.is(Blocks.COBBLESTONE_SLAB)) && s.getValue(SlabBlock.TYPE) == SlabType.DOUBLE;
         }
         return s.is(this.floor.getBlock());
      }

      private static BlockState doubleSlab(Block slab) {
         return slab.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE);
      }
   }

   private static final int[][] CANDIDATES = {{4, -5}, {4, 0}, {4, 5}, {0, -5}, {0, 0}, {0, 5}, {-4, -5}, {-4, 0}, {-4, 5}};

   private TowerGen() {
   }

   interface Reader {
      BlockState get(int x, int y, int z);
   }

   /** WorldGenHandler.getSurfaceBlockHeight: the first air above Y 50, minus one. */
   static int surfaceHeight(Reader world, int x, int z) {
      int h = 50;
      while (!world.get(x, ++h, z).isAir() && h < 320) {
      }
      return h - 1;
   }

   private static boolean isLog(BlockState s) {
      Block b = s.getBlock();
      return b == Blocks.OAK_LOG || b == Blocks.SPRUCE_LOG || b == Blocks.BIRCH_LOG || b == Blocks.JUNGLE_LOG || b == Blocks.ACACIA_LOG
         || b == Blocks.DARK_OAK_LOG;
   }

   private static boolean isLeaves(BlockState s) {
      Block b = s.getBlock();
      return b == Blocks.OAK_LEAVES || b == Blocks.SPRUCE_LEAVES || b == Blocks.BIRCH_LEAVES || b == Blocks.JUNGLE_LEAVES;
   }

   /** AS_WorldGenTower.isFoliageBlockID (snow layer, tall grass, dead bush, logs, leaves). */
   private static boolean isFoliage(BlockState s) {
      return s.is(Blocks.SNOW) || s.is(Blocks.SHORT_GRASS) || s.is(Blocks.FERN) || s.is(Blocks.DEAD_BUSH) || isLog(s) || isLeaves(s);
   }

   private static boolean isBuildable(BlockState s) {
      return s.is(Blocks.STONE) || s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.SAND) || s.is(Blocks.SANDSTONE) || s.is(Blocks.GRAVEL) || s.is(Blocks.DIRT);
   }

   /** yellow/red flowers, mushroom blocks, cactus, pumpkin, lava */
   static boolean isBanned(BlockState s) {
      return s.is(Blocks.DANDELION) || s.is(net.minecraft.tags.BlockTags.SMALL_FLOWERS) && !s.is(Blocks.WITHER_ROSE) || s.is(Blocks.BROWN_MUSHROOM_BLOCK)
         || s.is(Blocks.RED_MUSHROOM_BLOCK) || s.is(Blocks.CACTUS) || s.is(Blocks.PUMPKIN) || s.is(Blocks.CARVED_PUMPKIN) || s.is(Blocks.LAVA);
   }

   private static int candidateSurface(Reader world, int x, int z) {
      int h = 50;
      BlockState s;
      do {
         h++;
         s = world.get(x, h, z);
      } while (!s.isAir() && !isFoliage(s) && h < 320);
      return h - 1;
   }

   /**
    * getChosenTowerOrdinal. {@code checkOnly}: only the site tests (null = rejected). Otherwise the tower type, never rejected: the
    * site was already tested on the 1.12 terrain when the structure was planned.
    */
   static Type choose(Reader world, Random random, int ix, int jy, int kz, boolean checkOnly) {
      boolean rejected = false;
      int countWater = 0;
      int countSand = 0;
      int countSnow = 0;
      int countFoliage = 0;
      int countElse = 0;
      for (int[] pair : CANDIDATES) {
         int checkY = candidateSurface(world, ix + pair[0], kz + pair[1]);
         BlockState id = world.get(ix + pair[0], checkY, kz + pair[1]);
         if (world.get(ix + pair[0], checkY + 1, kz + pair[1]).is(Blocks.SNOW) || id.is(Blocks.ICE)) {
            countSnow++;
         } else if (id.is(Blocks.SAND) || id.is(Blocks.SANDSTONE)) {
            countSand++;
         } else if (id.is(Blocks.WATER)) {
            countWater++;
         } else if (!isLeaves(id) && !id.is(Blocks.LILY_PAD) && !isLog(id)) {
            countElse++;
         } else {
            countFoliage++;
         }
         if (Math.abs(checkY - jy) > 22) {
            rejected = true;
            break;
         }
         for (int y = 1; y <= 3; y++) {
            if (isBanned(world.get(ix + pair[0], checkY + y, kz + pair[1]))) {
               rejected = true;
            }
         }
         for (int y = 1; y <= 5; y++) {
            BlockState s = world.get(ix + pair[0], checkY - y, kz + pair[1]);
            if (s.isAir() || isBanned(s)) {
               rejected = true;
            }
         }
         if (rejected) {
            break;
         }
      }
      if (checkOnly) {
         return rejected ? null : Type.NULL;
      }
      int[] nums = {countWater, countSnow, countSand, countFoliage, countElse};
      Arrays.sort(nums);
      int result = nums[nums.length - 1];
      if (countSand == result) {
         return Type.SANDSTONE;
      } else if (countSnow == result) {
         return Type.ICE;
      } else if (countWater == result || countFoliage == result) {
         return Type.COBBLESTONE_MOSSY;
      } else if (random.nextInt(10) == 0) {
         return Type.NETHERRACK;
      }
      return random.nextInt(5) == 0 ? Type.SMOOTHSTONE : Type.COBBLESTONE;
   }

   /** AS_WorldGenTower.generate. {@code rand} stands in for both the generator random and world.rand. */
   static void generate(View world, Random rand, int ix, int jy, int kz, Type tower, boolean underground) {
      Block wall = tower.wall;
      Block light = tower.light;
      BlockState floorState = tower.floor;
      BlockState air = Blocks.AIR.defaultBlockState();
      int startingHeight = underground ? Math.max(jy - 70, 15) : jy - 6;
      int maximumHeight = underground ? jy + 7 : 120;
      int floor = 1;
      boolean topFloor = false;

      for (int builderHeight = startingHeight; builderHeight < maximumHeight; builderHeight += 7) {
         if (builderHeight + 7 >= maximumHeight) {
            topFloor = true;
         }
         for (int fi = 0; fi < 7; fi++) {
            if (floor == 1 && fi < 4) {
               fi = 4;
            }
            for (int xi = -7; xi < 7; xi++) {
               for (int zi = -7; zi < 7; zi++) {
                  int x = xi + ix;
                  int y = fi + builderHeight;
                  int z = zi + kz;
                  if (zi == -7) {
                     if (xi > -5 && xi < 4) {
                        wallPiece(world, x, y, z, wall, floor, fi);
                     }
                  } else if (zi != -6 && zi != -5) {
                     if (zi != -4 && zi != -3 && zi != 2 && zi != 3) {
                        if (zi > -3 && zi < 2) {
                           if (xi != -7 && xi != 6) {
                              if (xi > -7 && xi < 6) {
                                 if (fi == 5) {
                                    world.set(x, y, z, floorState);
                                 } else {
                                    world.set(x, y, z, air);
                                 }
                              }
                           } else if (fi >= 0 && fi <= 3 && !underground && (zi == -1 || zi == 0)) {
                              world.set(x, y, z, air);
                           } else {
                              wallPiece(world, x, y, z, wall, floor, fi);
                           }
                        } else if (zi == 4) {
                           if (xi == -5 || xi == 4) {
                              wallPiece(world, x, y, z, wall, floor, fi);
                           } else if (xi > -5 && xi < 4) {
                              world.set(x, y, z, fi == 5 ? floorState : air);
                           }
                        } else if (zi == 5) {
                           if (xi == -4 || xi == -3 || xi == 2 || xi == 3) {
                              wallPiece(world, x, y, z, wall, floor, fi);
                           } else if (xi > -3 && xi < 2) {
                              if (fi == 5) {
                                 world.set(x, y, z, floorState);
                              } else {
                                 wallPiece(world, x, y, z, wall, floor, fi);
                              }
                           }
                        } else if (zi == 6 && xi > -3 && xi < 2) {
                           wallPiece(world, x, y, z, wall, floor, fi);
                        }
                     } else if (xi == -6 || xi == 5) {
                        wallPiece(world, x, y, z, wall, floor, fi);
                     } else if (xi > -6 && xi < 5) {
                        if (fi == 5) {
                           world.set(x, y, z, floorState);
                        } else if (!world.get(x, y, z).is(Blocks.CHEST)) {
                           world.set(x, y, z, air);
                        }
                     }
                  } else if (xi == -5 || xi == 4) {
                     wallPiece(world, x, y, z, wall, floor, fi);
                  } else if (zi == -6) {
                     if (xi == (fi + 1) % 7 - 3) {
                        if (!underground || floor != 1) {
                           // stairs metadata 0: facing east, bottom half
                           world.set(x, y, z, tower.stairs.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST));
                        }
                        if (fi == 5) {
                           world.set(x - 7, y, z, tower.floorDefault());
                        }
                        if (fi == 6 && topFloor) {
                           wallPiece(world, x, y, z, wall, floor, fi);
                        }
                     } else if (xi < 4 && xi > -5) {
                        world.set(x, y, z, air);
                     }
                  } else if (zi == -5 && xi > -5 && xi < 5) {
                     if (fi != 0 && fi != 6 || xi != -4 && xi != 3) {
                        if (fi != 5 || xi != 3 && xi != -4) {
                           wallPiece(world, x, y, z, wall, floor, fi);
                        } else {
                           world.set(x, y, z, floorState);
                        }
                     } else {
                        world.set(x, y, z, air);
                     }
                  }
               }
            }
         }

         if (floor == 2) {
            world.set(ix + 3, builderHeight, kz - 5, wall.defaultBlockState());
            world.set(ix + 3, builderHeight - 1, kz - 5, wall.defaultBlockState());
         }

         if (!underground && topFloor || underground && floor == 1) {
            // the Battle Tower golem sat here (no 1.21 golem yet); its random yaw is still drawn
            rand.nextFloat();
         } else if (tower != Type.NULL) {
            spawner(world, ix + 2, builderHeight + 6, kz + 2, rand);
            spawner(world, ix - 3, builderHeight + 6, kz + 2, rand);
         } else {
            world.set(ix + 2, builderHeight + 6, kz + 2, air);
            world.set(ix - 3, builderHeight + 6, kz + 2, air);
         }

         world.set(ix, builderHeight + 6, kz + 3, tower.floorDefault());
         world.set(ix - 1, builderHeight + 6, kz + 3, tower.floorDefault());
         if (builderHeight + 56 >= 120 && floor == 1) {
            floor = 2;
         }

         TowerLoot loot = !underground ? TowerLoot.forFloor(topFloor ? 10 : floor) : TowerLoot.forFloor(floor == 1 ? 10 : Math.abs(11 - floor));
         for (int chest = 0; chest < 2; chest++) {
            int cx = ix - chest;
            int cy = builderHeight + 7;
            int cz = kz + 3;
            // two meta-2 chests side by side: 1.12 joined them into one double chest facing north
            world.set(cx, cy, cz, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH)
               .setValue(ChestBlock.TYPE, chest == 0 ? ChestType.RIGHT : ChestType.LEFT));
            if (tower != Type.NULL) {
               boolean here = world.inside(cx, cy, cz);
               int count = underground ? TowerLoot.itemGenerationsPerFloor * 2 : TowerLoot.itemGenerationsPerFloor;
               List<ItemStack> stacks = loot.stacks(here ? world.level() : null, rand, count);
               List<Integer> freeSlots = new ArrayList<>(27);
               for (int i = 0; i < 27; i++) {
                  freeSlots.add(i);
               }
               BlockEntity be = here ? world.blockEntity(cx, cy, cz) : null;
               Iterator<ItemStack> it = stacks.iterator();
               while (it.hasNext() && !freeSlots.isEmpty()) {
                  Integer slot = freeSlots.get(rand.nextInt(freeSlots.size()));
                  freeSlots.remove(slot);
                  ItemStack stack = it.next();
                  if (be instanceof ChestBlockEntity c) {
                     c.setItem(slot, stack);
                  }
               }
            }
         }

         if (light == Blocks.TORCH) {
            BlockState torch = Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.SOUTH);
            world.set(ix + 3, builderHeight + 2, kz - 6, torch);
            world.set(ix - 4, builderHeight + 2, kz - 6, torch);
            world.set(ix + 1, builderHeight + 2, kz - 4, torch);
            world.set(ix - 2, builderHeight + 2, kz - 4, torch);
         } else {
            world.set(ix + 3, builderHeight + 2, kz - 6, light.defaultBlockState());
            world.set(ix - 4, builderHeight + 2, kz - 6, light.defaultBlockState());
            world.set(ix + 1, builderHeight + 2, kz - 4, light.defaultBlockState());
            world.set(ix - 2, builderHeight + 2, kz - 4, light.defaultBlockState());
         }

         if (tower != Type.NULL) {
            for (int l3 = 0; l3 < floor * 4 + tower.ordinal() - 8 && !topFloor; l3++) {
               int k4 = 5 - rand.nextInt(12);
               int k5 = builderHeight + 5;
               int j6 = 5 - rand.nextInt(10);
               if (j6 >= -2 || k4 >= 4 || k4 <= -5 || k4 == 1 || k4 == -2) {
                  k4 += ix;
                  j6 += kz;
                  if (world.inside(k4, k5, j6) && tower.isFloorBlock(world.get(k4, k5, j6))
                     && !world.get(k4, k5 + 1, j6).is(Blocks.SPAWNER)) {
                     world.set(k4, k5, j6, air);
                  }
               }
            }
         }
         floor++;
      }
   }

   private static void spawner(View world, int x, int y, int z, Random rand) {
      world.set(x, y, z, Blocks.SPAWNER.defaultBlockState());
      EntityType<?> type = mobType(rand);
      if (world.inside(x, y, z) && world.blockEntity(x, y, z) instanceof SpawnerBlockEntity spawner) {
         spawner.setEntityId(type, world.level().getRandom());
      }
   }

   /** getMobType, with its fall-through: 7 and 8 give cave spiders too. */
   private static EntityType<?> mobType(Random random) {
      return switch (random.nextInt(10)) {
         case 0, 1, 2 -> EntityType.SKELETON;
         case 3, 4, 5, 6 -> EntityType.ZOMBIE;
         default -> EntityType.CAVE_SPIDER;
      };
   }

   private static void wallPiece(View world, int x, int y, int z, Block wall, int floor, int fi) {
      world.set(x, y, z, wall.defaultBlockState());
      if (floor == 1 && fi == 4) {
         for (int yy = y - 1; yy > 0 && !isBuildable(world.get(x, yy, z)); yy--) {
            world.set(x, yy, z, wall.defaultBlockState());
         }
      }
   }
}
