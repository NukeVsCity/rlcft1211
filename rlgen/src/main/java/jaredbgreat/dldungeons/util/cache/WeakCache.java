package jaredbgreat.dldungeons.util.cache;

public class WeakCache<T extends IHaveCoords> {
   private CacheReference<T>[] data;
   private final int minSize;
   private int capacity;
   private int lowLimit;
   private int length;
   private boolean altered;

   public WeakCache(int size) {
      this.data = new CacheReference[size];
      this.minSize = size;
      this.capacity = size * 3 / 4;
      this.lowLimit = (size - this.minSize) * 3 / 16;
      this.length = 0;
      this.altered = false;
   }

   public WeakCache() {
      this.data = new CacheReference[16];
      this.minSize = 16;
      this.capacity = 12;
      this.lowLimit = 0;
      this.length = 0;
      this.altered = false;
   }

   public void add(T item) {
      int bucket = (item.getCoords().hashCode() & 2147483647) % this.data.length;

      for (int offset = 0; offset < this.data.length; offset++) {
         int slot = (bucket + offset) % this.data.length;
         if (this.data[slot] == null || this.data[slot].get() == null) {
            this.data[slot] = new CacheReference<>(item, this);
            if (++this.length > this.capacity) {
               this.grow();
            }

            return;
         }

         if (this.data[slot].get().equals(item)) {
            return;
         }
      }
   }

   public T get(Coords coords) {
      if (this.altered) {
         this.rebucketAll();
      }

      int bucket = (coords.hashCode() & 2147483647) % this.data.length;

      for (int offset = 0; offset < this.data.length; offset++) {
         int slot = (bucket + offset) % this.data.length;
         if (this.data[slot] == null || this.data[slot].get() == null) {
            return null;
         }

         if (this.data[slot].get().getCoords().equals(coords)) {
            return this.data[slot].get();
         }
      }

      return null;
   }

   public T get(int x, int z, int d) {
      if (this.altered) {
         this.rebucketAll();
      }

      int bucket = (Coords.hashCoords(x, z, d) & 2147483647) % this.data.length;

      for (int offset = 0; offset < this.data.length; offset++) {
         int slot = (bucket + offset) % this.data.length;
         if (this.data[slot] == null || this.data[slot].get() == null) {
            return null;
         }

         if (this.data[slot].get().getCoords().equals(x, z)) {
            return this.data[slot].get();
         }
      }

      return null;
   }

   public boolean contains(Coords coords) {
      if (this.altered) {
         this.rebucketAll();
      }

      int bucket = (coords.hashCode() & 2147483647) % this.data.length;

      for (int offset = 0; offset < this.data.length; offset++) {
         int slot = (bucket + offset) % this.data.length;
         if (this.data[slot] == null || this.data[slot].get() == null) {
            return false;
         }

         if (this.data[slot].get().getCoords().equals(coords)) {
            return true;
         }
      }

      return false;
   }

   public boolean contains(T in) {
      if (this.altered) {
         this.rebucketAll();
      }

      Coords coords = in.getCoords();
      int bucket = (coords.hashCode() & 2147483647) % this.data.length;

      for (int offset = 0; offset <= this.data.length; offset++) {
         int slot = (bucket + offset) % this.data.length;
         if (this.data[slot] == null || this.data[slot].get() == null) {
            return false;
         }

         if (this.data[slot].get().getCoords().equals(coords)) {
            return true;
         }
      }

      return false;
   }

   private void grow() {
      CacheReference<T>[] old = this.data;
      this.data = new CacheReference[old.length * 3 / 2];

      for (int i = 0; i < old.length; i++) {
         if (old[i] != null && old[i].get() != null) {
            this.rebucket(old[i].get());
         }
      }

      this.capacity = this.data.length * 3 / 4;
      this.lowLimit = (this.data.length - this.minSize) * 3 / 16;
      this.altered = false;
   }

   private void shrink() {
      CacheReference<T>[] old = this.data;
      this.data = new CacheReference[Math.max(old.length / 2, this.minSize)];

      for (int i = 0; i < old.length; i++) {
         if (old[i] != null && old[i].get() != null) {
            this.rebucket(old[i].get());
         }
      }

      this.capacity = this.data.length * 3 / 4;
      this.lowLimit = (this.data.length - this.minSize) * 3 / 16;
      this.altered = false;
   }

   private void rebucketAll() {
      if (this.length < this.lowLimit) {
         this.shrink();
      } else {
         CacheReference<T>[] old = this.data;
         this.data = new CacheReference[old.length];

         for (int i = 0; i < old.length; i++) {
            if (old[i] != null && old[i].get() != null) {
               this.rebucket(old[i].get());
            }
         }
      }

      this.altered = false;
   }

   private void rebucket(T item) {
      int bucket = (item.getCoords().hashCode() & 2147483647) % this.data.length;

      for (int offset = 0; offset <= this.data.length; offset++) {
         int slot = (bucket + offset) % this.data.length;
         if (this.data[slot] == null || this.data[slot].get() != null && this.data[slot].get().equals(item)) {
            this.data[slot] = new CacheReference<>(item, this);
            return;
         }
      }
   }

   public void reduce() {
      this.length--;
      this.altered = true;
   }
}
