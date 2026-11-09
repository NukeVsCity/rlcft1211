package com.github.alexthe666.iceandfire;

/**
 * STUB(iaf/core): the IceAndFireConfig fields the land mobs read, with the values the 1.12 code ends up with under RLCraft's
 * ice_and_fire.cfg (including the 1.12 key mix-ups: cockatriceEggChance reads "Cockatrice chicken Search Length" = 32 and
 * myrmexLarvaTicks reads "Myrmex Hatch Length" = 3000).
 */
public class IceAndFireConfig {
   public int dragonGoldSearchLength = 17;
   public boolean spawnGorgons = true;
   public int spawnGorgonsChance = 75;
   public double gorgonMaxHealth = 100.0;
   public boolean generateCyclopsCaves = true;
   public int spawnCyclopsChance = 170;
   public int cyclopesSheepSearchLength = 17;
   public double cyclopsMaxHealth = 200.0;
   public double cyclopsAttackStrength = 15.0;
   public double cyclopsBiteStrength = 40.0;
   public boolean cyclopsGriefing = true;
   public int deathWormTargetSearchLength = 64;
   public double deathWormMaxHealth = 10.0;
   public double deathWormAttackStrength = 3.0;
   public boolean spawnDeathWorm = true;
   public boolean deathWormAttackMonsters = true;
   public int deathWormSpawnRate = 2;
   public int deathWormSpawnCheckChance = 3;
   public int cockatriceChickenSearchLength = 32;
   public int cockatriceEggChance = 32;
   public boolean chickensLayRottenEggs = true;
   public boolean spawnCockatrices = true;
   public int cockatriceSpawnRate = 4;
   public int cockatriceSpawnCheckChance = 0;
   public boolean spawnTrolls = true;
   public int trollSpawnRate = 500;
   public int trollSpawnCheckChance = 1;
   public boolean trollsDropWeapon = true;
   public double trollMaxHealth = 100.0;
   public double trollAttackStrength = 10.0;
   public boolean villagersFearDragons = true;
   public boolean animalsFearDragons = true;
   public boolean generateMyrmexColonies = true;
   public int myrmexPregnantTicks = 2500;
   public int myrmexEggTicks = 3000;
   public int myrmexLarvaTicks = 3000;
   public int myrmexColonyGenChance = 150;
   public int myrmexColonySize = 80;
}
