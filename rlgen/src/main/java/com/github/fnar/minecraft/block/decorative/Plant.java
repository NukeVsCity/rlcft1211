package com.github.fnar.minecraft.block.decorative;

import greymerk.roguelike.worldgen.BlockBrush;
import java.util.Random;

public enum Plant {
   DANDELION,
   POPPY,
   ORCHID,
   ALLIUM,
   BLUET,
   REDTULIP,
   ORANGETULIP,
   WHITETULIP,
   PINKTULIP,
   DAISY,
   RED_MUSHROOM,
   BROWN_MUSHROOM,
   CACTUS,
   OAK_SAPLING,
   BIRCH_SAPLING,
   SPRUCE_SAPLING,
   JUNGLE_SAPLING,
   ACACIA_SAPLING,
   DARKOAK_SAPLING,
   SHRUB,
   FERN;

   public BlockBrush getBrush() {
      return PlantBlock.plant().setPlant(this);
   }

   public static Plant chooseRandom(Random random) {
      return values()[random.nextInt(values().length)];
   }
}
