package greymerk.roguelike.util;

import java.util.Objects;
import java.util.Random;

public class WeightedChoice<T> implements IWeighted<T> {
   private final T item;
   private final int weight;

   public WeightedChoice(T toAdd, int weight) {
      this.item = toAdd;
      this.weight = weight;
   }

   @Override
   public int getWeight() {
      return this.weight;
   }

   @Override
   public T get(Random rand) {
      return this.item;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         WeightedChoice<?> that = (WeightedChoice<?>)o;
         return this.weight == that.weight && Objects.equals(this.item, that.item);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.item, this.weight);
   }

   @Override
   public String toString() {
      return "WeightedChoice(item=" + this.item + ", weight=" + this.getWeight() + ")";
   }
}
