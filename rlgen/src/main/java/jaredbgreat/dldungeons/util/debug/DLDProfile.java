package jaredbgreat.dldungeons.util.debug;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class DLDProfile implements IProfiler {
   int depth;
   BufferedWriter save;
   ArrayList<Task> tasks = new ArrayList<>();

   public DLDProfile() {
      try {
         this.save = new BufferedWriter(new FileWriter("DLDProfile.log"));
      } catch (IOException var2) {
         var2.printStackTrace();
      }
   }

   @Override
   public void finalize() {
      try {
         this.save.close();
         super.finalize();
      } catch (Throwable var2) {
         var2.printStackTrace();
      }
   }

   @Override
   public void startTask(String name) {
      try {
         String out = this.depth + ": Starting task " + name + "\n";
         this.save.write(out);
         this.save.flush();
         System.out.print(out);
         this.tasks.add(new Task(name));
         this.depth++;
      } catch (IOException var3) {
         var3.printStackTrace();
      }
   }

   @Override
   public void endTask(String name) {
      try {
         Task task = this.tasks.remove(this.tasks.size() - 1);
         this.depth--;
         String out = this.depth + ": Finishing task " + name + " in " + task.complete() + " ms\n";
         this.save.write(out);
         this.save.flush();
         System.out.print(out);
      } catch (IOException var4) {
         var4.printStackTrace();
      }
   }

   @Override
   public void infoOut(String info) {
      try {
         this.save.write(info);
         this.save.flush();
      } catch (IOException var3) {
         var3.printStackTrace();
      }
   }
}
