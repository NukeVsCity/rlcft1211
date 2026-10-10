package jaredbgreat.dldungeons.util.debug;

public class Task {
   String name;
   long start;
   long stop;

   Task(String name) {
      this.name = name;
      this.start = System.currentTimeMillis();
   }

   long complete() {
      this.stop = System.currentTimeMillis();
      return this.stop - this.start;
   }
}
