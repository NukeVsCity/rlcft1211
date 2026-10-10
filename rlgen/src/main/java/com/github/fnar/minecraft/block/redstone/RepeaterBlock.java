package com.github.fnar.minecraft.block.redstone;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.SingleBlockBrush;

public class RepeaterBlock extends SingleBlockBrush {
   private boolean isPowered;
   private RepeaterBlock.Delay delay = RepeaterBlock.Delay.ONE;

   public RepeaterBlock() {
      super(BlockType.REPEATER);
   }

   public RepeaterBlock setDelay(RepeaterBlock.Delay delay) {
      this.delay = delay;
      return this;
   }

   public RepeaterBlock.Delay getDelay() {
      return this.delay;
   }

   public RepeaterBlock setPowered(boolean isPowered) {
      this.isPowered = isPowered;
      return this;
   }

   public boolean isPowered() {
      return this.isPowered;
   }

   public static RepeaterBlock repeater() {
      return new RepeaterBlock();
   }

   public RepeaterBlock copy() {
      RepeaterBlock copy = new RepeaterBlock();
      copy.setFacing(this.getFacing());
      copy.setPowered(this.isPowered);
      copy.setDelay(this.delay);
      return copy;
   }

   public static enum Delay {
      ONE(1),
      TWO(2),
      THREE(3),
      FOUR(4);

      private final int delay;

      private Delay(int delay) {
         this.delay = delay;
      }

      public int asInt() {
         return this.delay;
      }
   }
}
