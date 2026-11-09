package com.github.alexthe666.iceandfire.structures;

import java.util.Random;

/**
 * The part of 1.12 WorldGenMyrmexHive the myrmex AI uses (room types). The hive generator itself lives in rlgen
 * (com.nukevscity.rlgen.iaf.IafHive); a queen digging a new nest (small = true) is not generated yet.
 */
public final class WorldGenMyrmexHive {
   private WorldGenMyrmexHive() {
   }

   public enum RoomType {
      DEFAULT(false),
      TUNNEL(false),
      ENTERANCE(false),
      QUEEN(false),
      FOOD(true),
      EMPTY(true),
      NURSERY(true),
      SHINY(true),
      TRASH(true);

      final boolean random;

      RoomType(boolean random) {
         this.random = random;
      }

      public static RoomType random(Random rand) {
         java.util.List<RoomType> list = new java.util.ArrayList<>();
         for (RoomType type : values()) {
            if (type.random) {
               list.add(type);
            }
         }
         return list.get(rand.nextInt(list.size()));
      }
   }
}
