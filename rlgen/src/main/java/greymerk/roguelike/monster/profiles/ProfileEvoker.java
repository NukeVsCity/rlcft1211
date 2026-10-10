package greymerk.roguelike.monster.profiles;

import com.github.fnar.minecraft.block.spawner.MobType;
import greymerk.roguelike.monster.Mob;
import greymerk.roguelike.monster.MonsterProfile;
import java.util.Random;

public class ProfileEvoker implements MonsterProfile {
   @Override
   public Mob apply(Mob mob, int level, int difficulty, Random rand) {
      mob.setMobType(MobType.EVOKER);
      return mob;
   }
}
