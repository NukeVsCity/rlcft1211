package com.github.alexthe666.iceandfire.entity;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * The dragons Ice and Fire 1.7.1 world gen spawned, for the rlgen planners to call where 1.12 did (they currently only
 * consume the matching random call). {@code rand} is the structure's random, as in 1.12.
 */
public final class DragonWorldGenSpawns {
   private DragonWorldGenSpawns() {
   }

   private static EntityDragonBase create(ServerLevelAccessor level, boolean fire) {
      return fire ? new EntityFireDragon(IafDragonEntities.FIRE_DRAGON.get(), level.getLevel()) : new EntityIceDragon(IafDragonEntities.ICE_DRAGON.get(), level.getLevel());
   }

   /** WorldGenFire/IceDragonRoosts.generate: dragonAge = 50 + rand(25), awake, home at the roost, on top of the column. */
   public static void roostDragon(ServerLevelAccessor level, Random rand, BlockPos position, int dragonAge, boolean fire) {
      EntityDragonBase dragon = create(level, fire);
      dragon.setGender(dragon.getRNG().nextBoolean());
      dragon.growDragon(dragonAge);
      dragon.setAgingDisabled(true);
      dragon.setHealth(dragon.getMaxHealth());
      dragon.setVariant(new Random().nextInt(4));
      int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, position.getX(), position.getZ());
      dragon.absMoveTo((double)position.getX() + 0.5, (double)top + 1.5, (double)position.getZ() + 0.5, rand.nextFloat() * 360.0F, 0.0F);
      dragon.homePos = position;
      dragon.hasHomePosition = true;
      dragon.setHunger(50);
      level.addFreshEntityWithPassengers(dragon);
   }

   /** WorldGenFire/IceDragonCave.generate: dragonAge = 75 + rand(50), asleep on its gold, homePos set but hasHomePosition false (1.12). */
   public static void caveDragon(ServerLevelAccessor level, Random rand, BlockPos position, int dragonAge, boolean fire) {
      EntityDragonBase dragon = create(level, fire);
      dragon.setGender(dragon.getRNG().nextBoolean());
      dragon.growDragon(dragonAge);
      dragon.setAgingDisabled(true);
      dragon.setHealth(dragon.getMaxHealth());
      dragon.setVariant(new Random().nextInt(4));
      dragon.absMoveTo((double)position.getX() + 0.5, (double)position.getY() + 0.5, (double)position.getZ() + 0.5, rand.nextFloat() * 360.0F, 0.0F);
      dragon.setSleeping(true);
      dragon.homePos = position;
      dragon.setHunger(50);
      level.addFreshEntityWithPassengers(dragon);
   }

   /**
    * StructureGenerator dragon skeletons (sandy+dry biomes fire, cold+snowy ice, 1 in generateDragonSkeletonChance + 1):
    * a dead dragon of age 10 + random(100), already half harvested (death stage age / 5 / 2).
    */
   public static void skeleton(ServerLevelAccessor level, Random random, int x, int groundY, int z, boolean fire) {
      EntityDragonBase dragon = create(level, fire);
      dragon.setPos((double)x, (double)(groundY + 1), (double)z);
      int dragonage = 10 + random.nextInt(100);
      dragon.growDragon(dragonage);
      dragon.modelDeadProgress = 20.0F;
      dragon.setModelDead(true);
      dragon.setDeathStage(dragonage / 5 / 2);
      dragon.setYRot((float)random.nextInt(360));
      level.addFreshEntityWithPassengers(dragon);
   }
}
