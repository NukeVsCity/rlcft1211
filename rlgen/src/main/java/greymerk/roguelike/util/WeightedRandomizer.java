package greymerk.roguelike.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WeightedRandomizer<T> implements IWeighted<T> {
   private int weight;
   private int weightSum = 0;
   private final List<IWeighted<T>> items = new ArrayList<>();

   public WeightedRandomizer() {
   }

   public WeightedRandomizer(int weight) {
      this.weight = weight;
   }

   public WeightedRandomizer(WeightedRandomizer<T> toCopy) {
      this.weight = toCopy.weight;
      this.weightSum = toCopy.weightSum;
      this.items.addAll(toCopy.items);
   }

   public WeightedRandomizer(WeightedRandomizer<T> base, WeightedRandomizer<T> other) {
      for (IWeighted<T> item : base.items) {
         this.add(item);
      }

      for (IWeighted<T> item : other.items) {
         this.add(item);
      }
   }

   @Override
   public int getWeight() {
      return this.weight;
   }

   public boolean isEmpty() {
      return this.items.isEmpty();
   }

   public void add(IWeighted<T> toAdd) {
      this.weightSum = this.weightSum + toAdd.getWeight();
      this.items.add(toAdd);
   }

   public void add(T entryway, int weight) {
      this.add(new WeightedChoice<>(entryway, weight));
   }

   public WeightedRandomizer<T> with(IWeighted<T> toAdd) {
      this.add(toAdd);
      return this;
   }

   public WeightedRandomizer<T> with(WeightedChoice<T> toAdd) {
      this.add(toAdd);
      return this;
   }

   @Override
   public T get(Random rand) {
      if (this.weightSum == 0) {
         return null;
      } else if (this.items.isEmpty()) {
         return null;
      } else {
         int roll = rand.nextInt(this.weightSum);

         for (IWeighted<T> i : this.items) {
            roll -= i.getWeight();
            if (roll < 0) {
               return i.get(rand);
            }
         }

         return null;
      }
   }

   public void merge(WeightedRandomizer<T> toMerge) {
      for (IWeighted<T> item : toMerge.items) {
         this.add(item);
      }
   }

   public WeightedRandomizer<T> copy() {
      return new WeightedRandomizer<>(this);
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof WeightedRandomizer)) {
         return false;
      } else {
         WeightedRandomizer<?> other = (WeightedRandomizer<?>)o;
         if (!other.canEqual(this)) {
            return false;
         } else if (this.getWeight() != other.getWeight()) {
            return false;
         } else if (this.weightSum != other.weightSum) {
            return false;
         } else {
            Object this$items = this.items;
            Object other$items = other.items;
            return this$items == null ? other$items == null : this$items.equals(other$items);
         }
      }
   }

   protected boolean canEqual(Object other) {
      return other instanceof WeightedRandomizer;
   }

   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + this.getWeight();
      result = result * 59 + this.weightSum;
      Object $items = this.items;
      return result * 59 + ($items == null ? 43 : $items.hashCode());
   }

   @Override
   public String toString() {
      return "WeightedRandomizer(weight=" + this.getWeight() + ", weightSum=" + this.weightSum + ", items=" + this.items + ")";
   }
}
