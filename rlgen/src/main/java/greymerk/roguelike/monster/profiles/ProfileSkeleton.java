package greymerk.roguelike.monster.profiles;

import com.github.fnar.minecraft.block.spawner.MobType;
import greymerk.roguelike.monster.Mob;
import greymerk.roguelike.monster.MonsterProfile;
import greymerk.roguelike.monster.MonsterProfileType;
import java.util.Random;

public class ProfileSkeleton implements MonsterProfile {
   @Override
   public Mob apply(Mob mob, int level, int difficulty, Random rand) {
      mob.setMobType(MobType.SKELETON);
      if (level == 3 && rand.nextInt(40) == 0) {
         return MonsterProfileType.POISON_ARCHER.apply(mob, level, difficulty, rand).withMobType(MobType.STRAY);
      } else if (level > 1 && rand.nextInt(50) == 0) {
         return MonsterProfileType.MAGIC_ARCHER.apply(mob, level, difficulty, rand).withMobType(MobType.STRAY);
      } else if (level > 1 && rand.nextInt(10) == 0) {
         return MonsterProfileType.WITHER_SKELETON.apply(mob, level, difficulty, rand);
      } else {
         return level > 0 && rand.nextInt(20) == 0
            ? MonsterProfileType.SWORDSMAN.apply(mob, level, difficulty, rand)
            : MonsterProfileType.ARCHER.apply(mob, level, difficulty, rand);
      }
   }
}
