package com.charles445.simpledifficulty.api.temperature;

/** 1.12 TemperatureEnum. */
public enum TemperatureEnum {
   FREEZING(0, 5),
   COLD(6, 10),
   NORMAL(11, 14),
   HOT(15, 19),
   BURNING(20, 25);

   private final int lowerBound;
   private final int upperBound;

   TemperatureEnum(int lowerBound, int upperBound) {
      this.lowerBound = lowerBound;
      this.upperBound = upperBound;
   }

   public boolean matches(int temperature) {
      return temperature >= this.lowerBound && temperature <= this.upperBound;
   }

   public int getMiddle() {
      return (this.upperBound + this.lowerBound) / 2;
   }

   public int getLowerBound() {
      return this.lowerBound;
   }

   public int getUpperBound() {
      return this.upperBound;
   }
}
