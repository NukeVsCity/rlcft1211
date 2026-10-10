package greymerk.roguelike.monster.profiles;

import com.github.fnar.minecraft.Difficulty;
import greymerk.roguelike.monster.Mob;
import greymerk.roguelike.monster.MonsterProfile;
import greymerk.roguelike.monster.MonsterProfileType;
import java.util.Random;

public class ProfileBaby implements MonsterProfile {
   @Override
   public Mob apply(Mob mob, int level, int difficulty, Random random) {
      mob.setChild(true);
      if (random.nextBoolean()) {
         return MonsterProfileType.ZOMBIE_VILLAGER.apply(mob, level, difficulty, random);
      } else {
         mob.equipTool(random, level, Difficulty.fromInt(difficulty));
         return mob;
      }
   }
}
