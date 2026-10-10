package atomicstryker.ruins.common;

import java.util.Random;

class RuinRuleProcess {
   private final RuinTemplateRule rule;
   private final int x;
   private final int y;
   private final int z;
   private final int rotate;

   public RuinRuleProcess(RuinTemplateRule r, int xbase, int ybase, int zbase, int rot) {
      this.rule = r;
      this.x = xbase;
      this.y = ybase;
      this.z = zbase;
      this.rotate = rot;
   }

   public void doBlock(RuinsWorld world, Random random) {
      this.rule.doBlock(world, random, this.x, this.y, this.z, this.rotate);
   }
}
