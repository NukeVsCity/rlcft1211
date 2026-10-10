package jaredbgreat.dldungeons.util.debug;

public class DoNothing implements IProfiler {
   @Override
   public void startTask(String name) {
   }

   @Override
   public void endTask(String name) {
   }

   @Override
   public void infoOut(String info) {
   }
}
