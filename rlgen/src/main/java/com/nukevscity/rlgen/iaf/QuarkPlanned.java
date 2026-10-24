package com.nukevscity.rlgen.iaf;

import com.nukevscity.rlgen.RLGen;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.Vec3;

/**
 * Quark r1.6 IWorldGenerators that reach too far for one decoration step, planned on the 1.12 terrain like Ice and Fire's:
 * PirateShipGenerator (weight 0) and CrystalCaveGenerator (weight 1). Each gets its own chunk random, as Forge re-seeded it per generator.
 */
final class QuarkPlanned {
   static final String PIRATE_SHIP = "rlgen:quark/pirate_ship";
   private static final String PIRATE_LOOT = "rlgen:quark/pirate_chest";

   private QuarkPlanned() {
   }

   /** PirateShipGenerator: 1 in 4000 ocean chunks, a ship sunk 3 blocks into the water if nothing but water and air is in its way. */
   static void pirateShip(IafWorld world, StructureTemplateManager templates, Random random, int chunkX, int chunkZ) {
      int x = chunkX * 16 + random.nextInt(16);
      int z = chunkZ * 16 + random.nextInt(16);
      String biome = world.biome(new BlockPos(x, 1, z)).getRegistryName();
      if ((biome.equals("ocean") || biome.equals("deep_ocean")) && random.nextInt(4000) == 0) {
         BlockPos pos = topLiquid(world, x, z);
         if (!world.get(pos.below()).is(Blocks.WATER)) {
            return;
         }
         pos = new BlockPos(pos.getX(), pos.getY() - 3, pos.getZ());
         StructureTemplate t = templates.get(ResourceLocation.parse(PIRATE_SHIP)).orElse(null);
         if (t == null) {
            return;
         }
         Rotation rotation = Rotation.values()[random.nextInt(Rotation.values().length)];
         StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation);
         Vec3i size = t.getSize();
         for (int i = 0; i < size.getX(); i++) {
            for (int j = 0; j < size.getY(); j++) {
               for (int k = 0; k < size.getZ(); k++) {
                  BlockState s = world.get(pos.offset(StructureTemplate.calculateRelativePosition(settings, new BlockPos(i, j, k))));
                  if (!s.isAir() && !s.is(Blocks.WATER)) {
                     return;
                  }
               }
            }
         }
         world.found.add("pirate ship " + pos.toShortString());
         world.template(pos, PIRATE_SHIP, rotation.ordinal(), 0);
         Map<BlockPos, String> data = new HashMap<>();
         for (StructureTemplate.StructureBlockInfo info : t.filterBlocks(pos, settings, Blocks.STRUCTURE_BLOCK)) {
            if (info.nbt() != null) {
               data.put(info.pos(), info.nbt().getString("metadata"));
            }
         }
         for (Map.Entry<BlockPos, String> e : data.entrySet()) {
            String[] tokens = e.getValue().split(" ");
            if (tokens.length == 0) {
               return;
            }
            BlockPos dataPos = e.getKey();
            switch (tokens[0]) {
               case "bow_pirate", "sword_pirate" -> world.set(dataPos, Blocks.AIR.defaultBlockState()); // pirates are not spawned
               case "booty" -> {
                  float chance = tokens.length == 3 ? 1.0F : 0.75F;
                  if (random.nextFloat() > chance) {
                     world.set(dataPos, Blocks.AIR.defaultBlockState());
                  } else {
                     Direction facing = tokens.length > 1 && Direction.byName(tokens[1]) != null ? Direction.byName(tokens[1]) : Direction.NORTH;
                     facing = rotation.rotate(facing);
                     world.chest(dataPos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing.getAxis().isHorizontal() ? facing : Direction.NORTH),
                        PIRATE_LOOT, random.nextLong());
                  }
               }
               case "cannon" -> {
                  Direction facing = tokens.length > 1 && Direction.byName(tokens[1]) != null ? Direction.byName(tokens[1]) : Direction.NORTH;
                  facing = rotation.rotate(facing);
                  int slot = random.nextInt(9);
                  int count = 2 + random.nextInt(4);
                  world.blockEntity(dataPos, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, facing), be -> {
                     if (be instanceof DispenserBlockEntity d) {
                        d.setItem(slot, new ItemStack(Items.FIRE_CHARGE, count));
                     }
                  });
               }
               default -> {
               }
            }
         }
      }
   }

   /** PirateShipGenerator.getTopLiquidBlock: above the highest liquid (or the bottom of the world). */
   private static BlockPos topLiquid(IafWorld world, int x, int z) {
      BlockPos check = new BlockPos(x, 255, z);
      while (check.getY() >= 0) {
         BlockPos next = check.below();
         if (world.get(next).liquid()) {
            break;
         }
         check = next;
      }
      return check;
   }

   // ------------------------------------------------------------------ crystal caves

   /** CrystalCaveGenerator: 1 in 150 chunks, from the first air above stone between Y 15 and 49. */
   static void crystalCave(IafWorld world, Random random, int chunkX, int chunkZ) {
      if (random.nextDouble() > 0.006666666666666667) {
         return;
      }
      int x = chunkX * 16 + random.nextInt(16);
      int z = chunkZ * 16 + random.nextInt(16);
      for (int i = 15; i < 50; i++) {
         BlockPos pos = new BlockPos(x, i, z);
         BlockState below = world.get(pos.below());
         if (world.isAir(pos) && (below.is(Blocks.STONE) || below.is(Blocks.GRANITE) || below.is(Blocks.DIORITE) || below.is(Blocks.ANDESITE))) {
            world.found.add("crystal cave " + pos.toShortString());
            makeCrystalCave(world, pos, random);
            return;
         }
      }
   }

   private static void makeCrystalCave(IafWorld world, BlockPos source, Random rand) {
      double expandX = (rand.nextDouble() - 0.5) * 2.0;
      double expandY = (rand.nextDouble() - 0.5) * 0.1F;
      double expandZ = (rand.nextDouble() - 0.5) * 2.0;
      double curveAngle = rand.nextDouble() * Math.PI * 2.0;
      double curveRatio = rand.nextDouble() * 0.25 + 0.1;
      double curveX = Math.cos(curveAngle) * curveRatio;
      double curveY = (double)((rand.nextFloat() - 0.5F) * 0.05F);
      double curveZ = Math.sin(curveAngle) * curveRatio;
      BlockPos center = source;
      Vec3 expansion = normalize(new Vec3(expandX, expandY, expandZ));
      Vec3 curvature = new Vec3(curveX, curveY, curveZ);
      int color1 = rand.nextInt(RLGen.CRYSTALS.size());
      int color2;
      do {
         color2 = rand.nextInt(RLGen.CRYSTALS.size());
      } while (color2 == color1);
      BlockState crystal1 = RLGen.CRYSTALS.get(color1).get().defaultBlockState();
      BlockState crystal2 = RLGen.CRYSTALS.get(color2).get().defaultBlockState();
      int length = 12 + rand.nextInt(10);
      int size = 4 + rand.nextInt(3);
      for (int i = 0; i < length; i++) {
         hollowOut(world, center, rand, size, crystal1, crystal2);
         // BlockPos.add(double, double, double)
         center = BlockPos.containing(center.getX() + expansion.x * size * 0.5, center.getY() + expansion.y * size * 0.5, center.getZ() + expansion.z * size * 0.5);
         if (center.getY() < 10) {
            expansion = new Vec3(expansion.x, -expansion.y, expansion.z);
            curvature = new Vec3(curvature.x, -curvature.y, curvature.z);
         }
         expansion = normalize(expansion.add(curvature));
      }
   }

   /** Vec3d.normalize (1.12: zero below a length of 1.0E-4) */
   private static Vec3 normalize(Vec3 v) {
      double d = Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
      return d < 1.0E-4 ? Vec3.ZERO : new Vec3(v.x / d, v.y / d, v.z / d);
   }

   private static boolean isCrystal(BlockState s) {
      for (var c : RLGen.CRYSTALS) {
         if (s.is(c.get())) {
            return true;
         }
      }
      return false;
   }

   private static void hollowOut(IafWorld world, BlockPos source, Random rand, int width, BlockState crystal1, BlockState crystal2) {
      List<BlockPos> crystals = new ArrayList<>();
      int max = width * width;
      for (int i = -width; i <= width; i++) {
         for (int j = -width; j <= width; j++) {
            for (int k = -width; k <= width; k++) {
               BlockPos pos = source.offset(i, j, k);
               int dist = i * i + j * j + k * k;
               if (dist < max) {
                  if (!world.bedrock(pos)) {
                     world.set(pos, Blocks.AIR.defaultBlockState());
                  }
               } else if (dist - 1 < max) {
                  crystals.add(pos);
               }
            }
         }
      }
      for (BlockPos pos : crystals) {
         if (rand.nextDouble() < 0.2) {
            makeCrystal(world, pos, rand, rand.nextBoolean() ? crystal1 : crystal2);
         } else if (rand.nextDouble() < 0.5) {
            BlockState at = world.get(pos);
            if (!at.isAir() && !isCrystal(at) && !world.bedrock(pos)) {
               BlockState ore = Blocks.GOLD_ORE.defaultBlockState();
               if (rand.nextInt(3) == 0) {
                  ore = rand.nextInt(3) == 0 ? Blocks.DIAMOND_ORE.defaultBlockState() : Blocks.EMERALD_ORE.defaultBlockState();
               }
               world.set(pos, ore);
            }
         }
      }
   }

   private static void makeCrystal(IafWorld world, BlockPos source, Random rand, BlockState crystal) {
      boolean up = rand.nextBoolean();
      Direction shift = up ? Direction.UP : Direction.DOWN;
      BlockPos startPos = source;
      BlockState state = world.get(source);
      if (isCrystal(state)) {
         return;
      }
      int tests = 0;
      while (state.isAir()) {
         startPos = startPos.relative(shift.getOpposite());
         state = world.get(startPos);
         if (++tests >= 10) {
            return;
         }
      }
      int size = 3 + rand.nextInt(4);
      BlockPos pos = startPos;
      for (int i = 0; i < size; i++) {
         if (world.bedrock(pos)) {
            break;
         }
         world.set(pos, crystal);
         pos = pos.relative(shift);
      }
   }
}
