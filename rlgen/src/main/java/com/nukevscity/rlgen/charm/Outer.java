package com.nukevscity.rlgen.charm;

import com.nukevscity.rlgen.deco.DWorld;
import com.nukevscity.rlgen.legacy.biome.Biome;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Markings;
import net.minecraft.world.entity.animal.horse.Variant;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * VillageDecorations.onDecorate: in a chunk a village generated in, at the FLOWERS decorate event, the outer decorators run with
 * either the village-seeded random or the decoration random (eventRand), which the rest of 1.12's decoration then continued with.
 * RLCraft sets flowers, crops, pumpkins, trees and mushrooms to 0 and lights to 1, mobs 0.5, barrels 0.4.
 */
public final class Outer {
   private Outer() {
   }

   public static void decorate(DWorld w, BlockPos pos, Random eventRand, long villageSeed, boolean zombie) {
      if (villageSeed == 0L) {
         return;
      }
      Random rand = new Random(villageSeed);
      boolean flowers = rand.nextFloat() <= Charm.FLOWERS;
      boolean lights = rand.nextFloat() <= Charm.LIGHTS;
      boolean mobs = rand.nextFloat() <= Charm.MOBS;
      boolean crops = rand.nextFloat() <= Charm.CROPS;
      boolean barrels = rand.nextFloat() <= Charm.BARRELS;
      boolean pumpkins = rand.nextFloat() <= Charm.PUMPKINS;
      boolean trees = rand.nextFloat() <= Charm.TREES;
      boolean mushrooms = rand.nextFloat() <= Charm.MUSHROOMS;
      if (flowers || crops || pumpkins || trees || mushrooms) {
         throw new IllegalStateException("a Charm outer decorator RLCraft turned off");
      }
      if (lights) {
         lights(w, pos, eventRand);
      }
      if (mobs) {
         mobs(w, pos, eventRand, zombie);
      }
      if (barrels) {
         barrels(w, pos, eventRand);
      }
      if (Charm.ZOMBIE_EROSION && zombie) {
         erosion(w, pos, rand);
      }
   }

   private static boolean isAir(DWorld w, BlockPos p) {
      return w.get(p) == Blocks.AIR.defaultBlockState();
   }

   private static boolean normalCube(DWorld w, BlockPos p) {
      return w.get(p).isSolidRender(w.level, p);
   }

   private static void lights(DWorld w, BlockPos pos, Random rand) {
      int max = 1 + rand.nextInt(4);
      for (int i = 0; i < max; i++) {
         int xx = rand.nextInt(16) + 8;
         int zz = rand.nextInt(16) + 8;
         BlockPos p = w.height(pos.offset(xx, 0, zz));
         if (isAir(w, p) && normalCube(w, p.below())) {
            float f = rand.nextFloat();
            BlockState light;
            if (f > 0.93F) {
               light = Blocks.LANTERN.defaultBlockState();
            } else if (f > 0.84F) {
               light = Blocks.REDSTONE_TORCH.defaultBlockState();
            } else {
               light = Blocks.TORCH.defaultBlockState();
            }
            w.set(p, light);
         }
      }
   }

   private static <T extends Mob> T spawn(DWorld w, EntityType<T> type, BlockPos p) {
      T e = type.create(w.level.getLevel());
      if (e == null) {
         return null;
      }
      e.moveTo(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, 0.0F, 0.0F);
      e.finalizeSpawn(w.level, new DifficultyInstance(w.level.getDifficulty(), 0L, 0L, 0.0F), MobSpawnType.STRUCTURE, null);
      e.setPersistenceRequired();
      w.level.addFreshEntity(e);
      return e;
   }

   private static void mobs(DWorld w, BlockPos pos, Random rand, boolean zombie) {
      Biome biome = w.biome(pos);
      if ((double)rand.nextFloat() <= Charm.MOBS_WEIGHT) {
         return;
      }
      int xx = rand.nextInt(16) + 8;
      int zz = rand.nextInt(16) + 8;
      BlockPos p = w.height(pos.offset(xx, 0, zz));
      boolean airAbove = isAir(w, p);
      boolean grassBelow = w.get(p.below()).is(Blocks.GRASS_BLOCK);
      if (!(airAbove || grassBelow)) {
         return;
      }
      if (zombie) {
         if ((double)rand.nextFloat() < 0.5) {
            spawn(w, EntityType.ZOMBIE_HORSE, p);
         } else {
            spawn(w, EntityType.SKELETON_HORSE, p);
         }
         return;
      }
      int mobType = rand.nextInt(4);
      if (biome.isSnowyBiome() && (double)rand.nextFloat() <= Charm.GOLEMS_WEIGHT) {
         spawn(w, EntityType.SNOW_GOLEM, p);
      }
      if (mobType == 0) {
         // 1.12 ocelot skins: 0 wild ocelot, 1 black (tuxedo), 2 red, 3 siamese
         int skin = rand.nextInt(4);
         boolean tamed = rand.nextFloat() < 0.2F;
         if (skin == 0) {
            spawn(w, EntityType.OCELOT, p);
         } else {
            Cat cat = spawn(w, EntityType.CAT, p);
            if (cat != null) {
               cat.setVariant(w.level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.CAT_VARIANT)
                  .getHolderOrThrow(skin == 1 ? CatVariant.BLACK : skin == 2 ? CatVariant.RED : CatVariant.SIAMESE));
               if (tamed) {
                  cat.setTame(true, false);
                  cat.setOwnerUUID(cat.getUUID());
               }
            }
         }
      } else if (mobType == 1) {
         Wolf wolf = spawn(w, EntityType.WOLF, p);
         if (wolf != null && rand.nextFloat() < 0.2F) {
            wolf.setTame(true, false);
            wolf.setOwnerUUID(wolf.getUUID());
         }
      } else if (mobType == 2 && (double)rand.nextFloat() <= Charm.GOLEMS_WEIGHT) {
         spawn(w, EntityType.IRON_GOLEM, p);
      } else if (mobType == 3) {
         Horse horse = spawn(w, EntityType.HORSE, p);
         int variant = rand.nextInt(7);
         if (horse != null) {
            // setHorseVariant(0..6): a colour, no markings
            net.minecraft.nbt.CompoundTag t = new net.minecraft.nbt.CompoundTag();
            horse.addAdditionalSaveData(t);
            t.putInt("Variant", Variant.byId(variant).getId() | Markings.NONE.getId() << 8);
            horse.readAdditionalSaveData(t);
         }
      }
   }

   private static void barrels(DWorld w, BlockPos pos, Random rand) {
      if ((double)rand.nextFloat() < Charm.BARRELS_WEIGHT) {
         return;
      }
      int xx = rand.nextInt(16) + 8;
      int zz = rand.nextInt(16) + 8;
      BlockPos p = w.height(pos.offset(xx, 0, zz));
      if (isAir(w, p) && w.get(p.below()).is(Blocks.GRASS_BLOCK)) {
         if (rand.nextFloat() < 0.5F) {
            w.set(p, Blocks.BARREL.defaultBlockState());
            BlockEntity be = w.level.getBlockEntity(p);
            if (be instanceof RandomizableContainerBlockEntity c) {
               c.setLootTable(CharmLoot.key("charm:village/farmer"), rand.nextLong());
            }
         } else {
            w.set(p, Blocks.COMPOSTER.defaultBlockState());
         }
      }
   }

   private static void erosion(DWorld w, BlockPos pos, Random rand) {
      int max = Charm.EROSION_DAMAGE + rand.nextInt(60);
      for (int i = 0; i < max; i++) {
         int xx = rand.nextInt(16) + 8;
         int zz = rand.nextInt(16) + 8;
         BlockPos p = w.height(pos.offset(xx, 0, zz)).below(1 + rand.nextInt(12));
         BlockState state = w.get(p);
         BlockState newState = null;
         if (state.is(net.minecraft.tags.BlockTags.PLANKS) || state.getBlock() instanceof StairBlock || state.getBlock() instanceof FenceBlock) {
            newState = rand.nextFloat() < 0.92F ? Blocks.AIR.defaultBlockState() : Blocks.COBWEB.defaultBlockState();
         }
         if (state.getBlock() instanceof SlabBlock) {
            newState = Blocks.AIR.defaultBlockState();
         }
         if (state.is(Blocks.COBBLESTONE)) {
            newState = rand.nextFloat() < 0.5F ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : Blocks.GRAVEL.defaultBlockState();
         }
         if (newState != null) {
            w.set(p, newState);
         }
      }
   }
}
