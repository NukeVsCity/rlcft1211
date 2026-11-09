package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IceAndFire;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Ice and Fire 1.7.1 DragonUtils: the dragon helpers (the hippogryph, stymphalian bird, cockatrice and sea serpent ones are added by their ports). */
public class DragonUtils {
   /** 1.12 World.getHeight(BlockPos): the column's height map (first light-blocking block from the top, plus one). */
   public static BlockPos getHeight(Entity entity, BlockPos pos) {
      return entity.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos);
   }

   /** 1.12 Entity.getDistanceSqToCenter. */
   public static double getDistanceSqToCenter(Entity entity, BlockPos pos) {
      return entity.distanceToSqr(Vec3.atCenterOf(pos));
   }

   public static BlockPos getBlockInView(EntityDragonBase dragon) {
      float radius = 0.75F * (0.7F * dragon.getRenderSize() / 3.0F) * -7.0F - (float)dragon.getRNG().nextInt(dragon.getDragonStage() * 6);
      float neg = dragon.getRNG().nextBoolean() ? 1.0F : -1.0F;
      float renderYawOffset = dragon.yBodyRot;
      if (dragon.hasHomePosition && dragon.homePos != null) {
         BlockPos dragonPos = dragon.blockPosition();
         BlockPos ground = getHeight(dragon, dragonPos);
         int distFromGround = (int)dragon.getY() - ground.getY();

         for (int i = 0; i < 10; i++) {
            BlockPos pos = new BlockPos(
               dragon.homePos.getX()
                  + dragon.getRNG().nextInt(IceAndFire.CONFIG.dragonWanderFromHomeDistance)
                  - IceAndFire.CONFIG.dragonWanderFromHomeDistance,
               distFromGround > 16
                  ? (int)Math.min((double)IceAndFire.CONFIG.maxDragonFlight, dragon.getY() + (double)dragon.getRNG().nextInt(16) - 8.0)
                  : (int)dragon.getY() + dragon.getRNG().nextInt(16) + 1,
               dragon.homePos.getZ()
                  + dragon.getRNG().nextInt(IceAndFire.CONFIG.dragonWanderFromHomeDistance * 2)
                  - IceAndFire.CONFIG.dragonWanderFromHomeDistance
            );
            if (!dragon.isTargetBlocked(Vec3.atLowerCornerOf(pos)) && getDistanceSqToCenter(dragon, pos) > 6.0) {
               return pos;
            }
         }
      }

      float angle = (float) (Math.PI / 180.0) * renderYawOffset + 3.15F + dragon.getRNG().nextFloat() * neg;
      double extraX = (double)(radius * Mth.sin((float)(Math.PI + (double)angle)));
      double extraZ = (double)(radius * Mth.cos(angle));
      BlockPos radialPos = BlockPos.containing(dragon.getX() + extraX, 0.0, dragon.getZ() + extraZ);
      BlockPos ground = getHeight(dragon, radialPos);
      int distFromGround = (int)dragon.getY() - ground.getY();
      BlockPos newPos = radialPos.above(
         distFromGround > 16
            ? (int)Math.min((double)IceAndFire.CONFIG.maxDragonFlight, dragon.getY() + (double)dragon.getRNG().nextInt(16) - 8.0)
            : (int)dragon.getY() + dragon.getRNG().nextInt(16) + 1
      );
      BlockPos pos = dragon.doesWantToLand() ? ground : newPos;
      return !dragon.isTargetBlocked(Vec3.atLowerCornerOf(newPos)) && getDistanceSqToCenter(dragon, newPos) > 6.0 ? newPos : null;
   }

   public static BlockPos getWaterBlockInView(EntityDragonBase dragon) {
      float radius = 0.75F * (0.7F * dragon.getRenderSize() / 3.0F) * -7.0F - (float)dragon.getRNG().nextInt(dragon.getDragonStage() * 6);
      float neg = dragon.getRNG().nextBoolean() ? 1.0F : -1.0F;
      float angle = (float) (Math.PI / 180.0) * dragon.yBodyRot + 3.15F + dragon.getRNG().nextFloat() * neg;
      double extraX = (double)(radius * Mth.sin((float)(Math.PI + (double)angle)));
      double extraZ = (double)(radius * Mth.cos(angle));
      BlockPos radialPos = BlockPos.containing(dragon.getX() + extraX, 0.0, dragon.getZ() + extraZ);
      BlockPos ground = getHeight(dragon, radialPos);
      int distFromGround = (int)dragon.getY() - ground.getY();
      BlockPos newPos = radialPos.above(
         distFromGround > 16
            ? (int)Math.min((double)IceAndFire.CONFIG.maxDragonFlight, dragon.getY() + (double)dragon.getRNG().nextInt(16) - 8.0)
            : (int)dragon.getY() + dragon.getRNG().nextInt(16) + 1
      );
      BlockPos pos = dragon.doesWantToLand() ? ground : newPos;
      BlockPos surface = DragonCompat.material(dragon.level(), newPos.below(2)) != DragonCompat.Material.WATER
         ? newPos.below(dragon.getRNG().nextInt(10) + 1)
         : newPos;
      return getDistanceSqToCenter(dragon, surface) > 6.0 && DragonCompat.material(dragon.level(), surface) == DragonCompat.Material.WATER ? surface : null;
   }

   public static LivingEntity riderLookingAtEntity(final LivingEntity dragon, LivingEntity rider, double dist) {
      Vec3 vec3d = rider.getEyePosition(1.0F);
      Vec3 vec3d1 = rider.getViewVector(1.0F);
      Vec3 vec3d2 = vec3d.add(vec3d1.x * dist, vec3d1.y * dist, vec3d1.z * dist);
      Entity pointedEntity = null;
      List<Entity> list = rider.level()
         .getEntities(
            rider,
            rider.getBoundingBox().expandTowards(vec3d1.x * dist, vec3d1.y * dist, vec3d1.z * dist).inflate(1.0, 1.0, 1.0),
            entity -> EntitySelector.NO_SPECTATORS.test(entity)
               && entity != null
               && entity.isPickable()
               && entity instanceof LivingEntity
               && !entity.is(dragon)
               && !entity.isAlliedTo(dragon)
               && (!(entity instanceof IDeadMob) || !((IDeadMob)entity).isMobDead())
         );
      double d2 = dist;

      for (int j = 0; j < list.size(); j++) {
         Entity entity1 = list.get(j);
         AABB axisalignedbb = entity1.getBoundingBox().inflate((double)entity1.getPickRadius() + 2.0);
         Optional<Vec3> raytraceresult = axisalignedbb.clip(vec3d, vec3d2);
         if (axisalignedbb.contains(vec3d)) {
            if (d2 >= 0.0) {
               pointedEntity = entity1;
               d2 = 0.0;
            }
         } else if (raytraceresult.isPresent()) {
            double d3 = vec3d.distanceTo(raytraceresult.get());
            if (d3 < d2 || d2 == 0.0) {
               if (entity1.getRootVehicle() != rider.getRootVehicle() || rider.canRiderInteract()) {
                  pointedEntity = entity1;
                  d2 = d3;
               } else if (d2 == 0.0) {
                  pointedEntity = entity1;
               }
            }
         }
      }

      return (LivingEntity)pointedEntity;
   }

   public static boolean canTameDragonAttack(TamableAnimal dragon, Entity entity) {
      String className = entity.getClass().getSimpleName();
      if (className.contains("VillagerMCA") || className.contains("MillVillager") || className.contains("Citizen")) {
         return false;
      } else if (entity instanceof AbstractVillager || entity instanceof AbstractGolem || entity instanceof Player) {
         return false;
      } else {
         return entity instanceof TamableAnimal ? !((TamableAnimal)entity).isTame() : true;
      }
   }

   public static boolean isVillager(Entity entity) {
      String className = entity.getClass().getSimpleName();
      return entity instanceof Npc || className.contains("VillagerMCA") || className.contains("MillVillager") || className.contains("Citizen");
   }

   public static boolean isAnimaniaMob(Entity entity) {
      String className = entity.getClass().getCanonicalName() == null ? "" : entity.getClass().getCanonicalName().toLowerCase();
      return className.contains("animania");
   }

   public static boolean isLivestock(Entity entity) {
      String className = entity.getClass().getSimpleName();
      return entity instanceof Cow
         || entity instanceof Sheep
         || entity instanceof Pig
         || entity instanceof Chicken
         || entity instanceof Rabbit
         || entity instanceof AbstractHorse
         || className.contains("Cow")
         || className.contains("Sheep")
         || className.contains("Pig")
         || className.contains("Chicken")
         || className.contains("Rabbit")
         || className.contains("Peacock")
         || className.contains("Goat")
         || className.contains("Ferret")
         || className.contains("Hedgehog")
         || className.contains("Peahen")
         || className.contains("Peafowl")
         || className.contains("Sow")
         || className.contains("Hog");
   }

   public static boolean canDragonBreak(Block block) {
      return block != Blocks.BARRIER
         && block != Blocks.OBSIDIAN
         && block != Blocks.END_STONE
         && block != Blocks.BEDROCK
         && block != Blocks.END_PORTAL
         && block != Blocks.END_PORTAL_FRAME
         && block != Blocks.COMMAND_BLOCK
         && block != Blocks.REPEATING_COMMAND_BLOCK
         && block != Blocks.CHAIN_COMMAND_BLOCK
         && block != Blocks.IRON_BARS
         && block != Blocks.END_GATEWAY;
   }

   public static boolean hasSameOwner(TamableAnimal cockatrice, Entity entity) {
      if (entity instanceof TamableAnimal) {
         TamableAnimal tameable = (TamableAnimal)entity;
         if (tameable.getOwnerUUID() != null && cockatrice.getOwnerUUID() != null && tameable.getOwnerUUID().equals(cockatrice.getOwnerUUID())) {
            return true;
         }
      }

      return false;
   }

   public static boolean isAlive(LivingEntity entity) {
      return !(entity instanceof IDeadMob) || !((IDeadMob)entity).isMobDead();
   }

   public static boolean canGrief(boolean weak) {
      return weak ? IceAndFire.CONFIG.dragonGriefing == 0 : IceAndFire.CONFIG.dragonGriefing < 2;
   }
}
