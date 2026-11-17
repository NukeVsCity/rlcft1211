package com.charles445.simpledifficulty.api.thirst;

import net.minecraft.core.BlockPos;

/** 1.12 ThirstEnum (name, thirst, saturation, thirsty chance). */
public enum ThirstEnum {
   NORMAL("normal", 3, 0.3F, 0.75F),
   RAIN("rain", 1, 0.05F, 0.0F),
   POTION("potion", 2, 0.2F, 0.0F),
   PURIFIED("purified", 6, 3.0F, 0.0F);

   private final String name;
   private final int thirst;
   private final float saturation;
   private final float dirty;

   ThirstEnum(String name, int thirst, float saturation, float dirty) {
      this.name = name;
      this.thirst = thirst;
      this.saturation = saturation;
      this.dirty = dirty;
   }

   public String getName() {
      return this.name;
   }

   public int getThirst() {
      return this.thirst;
   }

   public float getSaturation() {
      return this.saturation;
   }

   public float getThirstyChance() {
      return this.dirty;
   }

   @Override
   public String toString() {
      return this.getName();
   }

   /** 1.12 ThirstEnumBlockPos. */
   public static class BlockPosResult {
      public final ThirstEnum thirstEnum;
      public final BlockPos pos;

      public BlockPosResult(ThirstEnum thirstEnum, BlockPos pos) {
         this.thirstEnum = thirstEnum;
         this.pos = pos;
      }
   }
}
