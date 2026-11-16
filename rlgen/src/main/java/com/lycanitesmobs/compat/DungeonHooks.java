package com.lycanitesmobs.compat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.resources.ResourceLocation;

/**
 * 1.12 net.minecraftforge.common.DungeonHooks: the weighted mob list of vanilla dungeon spawners (Forge default skeleton 100,
 * zombie 200, spider 100, then the mods' mobs in registration order). rlgen's 1.12 dungeons read it when Lycanites is loaded.
 */
public final class DungeonHooks {
   public record DungeonMob(ResourceLocation type, int weight) {
   }

   private static final List<DungeonMob> MOBS = new ArrayList<>();

   static {
      addDungeonMob(ResourceLocation.withDefaultNamespace("skeleton"), 100);
      addDungeonMob(ResourceLocation.withDefaultNamespace("zombie"), 200);
      addDungeonMob(ResourceLocation.withDefaultNamespace("spider"), 100);
   }

   private DungeonHooks() {
   }

   /** 1.12 addDungeonMob: an existing entry gets the weight added. */
   public static float addDungeonMob(ResourceLocation type, int rarity) {
      if (rarity <= 0) {
         throw new IllegalArgumentException("Rarity must be greater then zero");
      }
      for (int i = 0; i < MOBS.size(); i++) {
         DungeonMob mob = MOBS.get(i);
         if (mob.type().equals(type)) {
            MOBS.set(i, new DungeonMob(type, mob.weight() + rarity));
            return mob.weight() + rarity;
         }
      }
      MOBS.add(new DungeonMob(type, rarity));
      return rarity;
   }

   /** 1.12 getRandomDungeonMob: WeightedRandom.getRandomItem (one nextInt of the total weight). */
   public static ResourceLocation getRandomDungeonMob(Random rand) {
      int total = 0;
      for (DungeonMob mob : MOBS) {
         total += mob.weight();
      }
      int roll = rand.nextInt(total);
      for (DungeonMob mob : MOBS) {
         roll -= mob.weight();
         if (roll < 0) {
            return mob.type();
         }
      }
      return MOBS.get(0).type();
   }
}
