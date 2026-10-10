package greymerk.roguelike.monster;

import java.util.Random;

public interface MonsterProfile {
   Mob apply(Mob var1, int var2, int var3, Random var4);
}
