package com.github.alexthe666.iceandfire.entity;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * The dragons Ice and Fire 1.7.1 world gen spawned, called by the rlgen planners where 1.12 did. The planners draw the structure
 * random's values (age, rotation) in the 1.12 order while planning and pass them here when the chunk is decorated; {@code level} is
 * the WorldGenRegion of that chunk.
 */
public final class DragonWorldGenSpawns {
   private DragonWorldGenSpawns() {
   }

   private static EntityDragonBase create(ServerLevelAccessor level, boolean fire) {
      return fire ? new EntityFireDragon(IafDragonEntities.FIRE_DRAGON.get(), level.getLevel()) : new EntityIceDragon(IafDragonEntities.ICE_DRAGON.get(), level.getLevel());
   }

   /**
    * WorldGenFire/IceDragonRoosts.generate: dragonAge = 50 + rand(25), awake, home at the roost, on top of the column
    * ({@code top} = World.getHeight(position).getY() after the roost was built), yaw = rand.nextFloat() * 360.
    */
   public static void roostDragon(ServerLevelAccessor level, float yaw, BlockPos position, int top, int dragonAge, boolean fire) {
      EntityDragonBase dragon = create(level, fire);
      dragon.setGender(dragon.getRNG().nextBoolean());
      dragon.growDragon(dragonAge);
      dragon.setAgingDisabled(true);
      dragon.setHealth(dragon.getMaxHealth());
      dragon.setVariant(new Random().nextInt(4));
      dragon.absMoveTo((double)position.getX() + 0.5, (double)top + 1.5, (double)position.getZ() + 0.5, yaw, 0.0F);
      dragon.homePos = position;
      dragon.hasHomePosition = true;
      dragon.setHunger(50);
      level.addFreshEntityWithPassengers(dragon);
   }

   /** WorldGenFire/IceDragonCave.generate: dragonAge = 75 + rand(50), asleep on its gold, homePos set but hasHomePosition false (1.12). */
   public static void caveDragon(ServerLevelAccessor level, float yaw, BlockPos position, int dragonAge, boolean fire) {
      EntityDragonBase dragon = create(level, fire);
      dragon.setGender(dragon.getRNG().nextBoolean());
      dragon.growDragon(dragonAge);
      dragon.setAgingDisabled(true);
      dragon.setHealth(dragon.getMaxHealth());
      dragon.setVariant(new Random().nextInt(4));
      dragon.absMoveTo((double)position.getX() + 0.5, (double)position.getY() + 0.5, (double)position.getZ() + 0.5, yaw, 0.0F);
      dragon.setSleeping(true);
      dragon.homePos = position;
      dragon.setHunger(50);
      level.addFreshEntityWithPassengers(dragon);
   }

   /**
    * StructureGenerator dragon skeletons (sandy+dry biomes fire, cold+snowy ice, 1 in generateDragonSkeletonChance + 1):
    * a dead dragon of age 10 + random(100), already half harvested (death stage age / 5 / 2).
    */
   public static void skeleton(ServerLevelAccessor level, int dragonage, int yaw, int x, int groundY, int z, boolean fire) {
      EntityDragonBase dragon = create(level, fire);
      dragon.setPos((double)x, (double)(groundY + 1), (double)z);
      dragon.growDragon(dragonage);
      dragon.modelDeadProgress = 20.0F;
      dragon.setModelDead(true);
      dragon.setDeathStage(dragonage / 5 / 2);
      dragon.setYRot((float)yaw);
      level.addFreshEntityWithPassengers(dragon);
   }
}
