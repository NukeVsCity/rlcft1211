package com.alcatrazescapee.notreepunching.util.types;

/** 1.12 Pottery: the shapes the clay tool cycles through (worked clay -> large vessel -> small vessel -> bucket -> flower pot -> gone). */
public enum Pottery {
   WORKED,
   LARGE_VESSEL,
   SMALL_VESSEL,
   BUCKET,
   FLOWER_POT;

   private static final Pottery[] VALUES = values();

   public Pottery next() {
      return this == FLOWER_POT ? null : VALUES[this.ordinal() + 1];
   }
}
