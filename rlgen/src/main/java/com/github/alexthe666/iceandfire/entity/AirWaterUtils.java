package com.github.alexthe666.iceandfire.entity;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The Ice and Fire 1.7.1 DragonUtils / EntityGorgon helpers the air and water mobs use (copies, so these mobs do not depend on the
 * dragon port), plus 1.12 World helpers.
 */
public final class AirWaterUtils {
   private AirWaterUtils() {
   }

   /** DragonUtils.isAlive */
   public static boolean isAlive(LivingEntity entity) {
      return !(entity instanceof IDeadMob dead) || !dead.isMobDead();
   }

   /** DragonUtils.canTameDragonAttack */
   public static boolean canTameDragonAttack(TamableAnimal dragon, Entity entity) {
      String className = entity.getClass().getSimpleName();
      if (className.contains("VillagerMCA") || className.contains("MillVillager") || className.contains("Citizen")) {
         return false;
      } else if (entity instanceof AbstractVillager || entity instanceof AbstractGolem || entity instanceof Player) {
         return false;
      } else {
         return entity instanceof TamableAnimal t ? !t.isTame() : true;
      }
   }

   /** DragonUtils.isVillager */
   public static boolean isVillager(Entity entity) {
      String className = entity.getClass().getSimpleName();
      return entity instanceof Npc || className.contains("VillagerMCA") || className.contains("MillVillager") || className.contains("Citizen");
   }

   /** DragonUtils.isLivestock */
   public static boolean isLivestock(Entity entity) {
      String n = entity.getClass().getSimpleName();
      return entity instanceof Cow || entity instanceof Sheep || entity instanceof Pig || entity instanceof Chicken || entity instanceof Rabbit
         || entity instanceof AbstractHorse || n.contains("Cow") || n.contains("Sheep") || n.contains("Pig") || n.contains("Chicken")
         || n.contains("Rabbit") || n.contains("Peacock") || n.contains("Goat") || n.contains("Ferret") || n.contains("Hedgehog")
         || n.contains("Peahen") || n.contains("Peafowl") || n.contains("Sow") || n.contains("Hog");
   }

   /** DragonUtils.hasSameOwner */
   public static boolean hasSameOwner(TamableAnimal a, Entity entity) {
      return entity instanceof TamableAnimal t && t.getOwnerUUID() != null && a.getOwnerUUID() != null && t.getOwnerUUID().equals(a.getOwnerUUID());
   }

   /**
    * StoneEntityProperties.isStone / EntityGorgon.isStoneMob (gorgon stone statues).
    * STUB(iaf/mobs-land): hook the gorgon port's stone attachment here; no mob is ever stone until then.
    */
   public static boolean isStone(Entity entity) {
      return false;
   }

   /** 1.12 World.getHeight(pos): the column top (blocks that stop light/motion), as a BlockPos with that y. */
   public static BlockPos getHeight(Level level, BlockPos pos) {
      return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(pos.getX(), 0, pos.getZ()));
   }

   /** 1.12 Entity.getDistanceSqToCenter */
   public static double distanceSqToCenter(Entity e, BlockPos pos) {
      double dx = pos.getX() + 0.5 - e.getX();
      double dy = pos.getY() + 0.5 - e.getY();
      double dz = pos.getZ() + 0.5 - e.getZ();
      return dx * dx + dy * dy + dz * dz;
   }

   /** 1.12 World.rayTraceBlocks(from, to, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock); null on a miss. */
   @Nullable
   public static BlockHitResult rayTraceBlocks(Entity entity, Vec3 from, Vec3 to, boolean stopOnLiquid) {
      BlockHitResult hit = entity.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER,
         stopOnLiquid ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, entity));
      return hit.getType() == HitResult.Type.MISS ? null : hit;
   }

   /** 1.12 new BlockPos(Entity) / Entity.getPosition() */
   public static BlockPos pos(Entity e) {
      return BlockPos.containing(e.getX(), e.getY(), e.getZ());
   }

   /** DragonUtils.riderLookingAtEntity */
   @Nullable
   public static LivingEntity riderLookingAtEntity(LivingEntity dragon, LivingEntity rider, double dist) {
      Vec3 eyes = rider.getEyePosition(1.0F);
      Vec3 look = rider.getViewVector(1.0F);
      Vec3 end = eyes.add(look.x * dist, look.y * dist, look.z * dist);
      Entity pointedEntity = null;
      List<Entity> list = rider.level().getEntities(rider, rider.getBoundingBox().expandTowards(look.x * dist, look.y * dist, look.z * dist).inflate(1.0),
         e -> (!(e instanceof Player p) || !p.isSpectator()) && e.isPickable() && e instanceof LivingEntity && e != dragon && !e.isAlliedTo(dragon)
            && (!(e instanceof IDeadMob d) || !d.isMobDead()));
      double d2 = dist;
      for (Entity entity1 : list) {
         AABB box = entity1.getBoundingBox().inflate(entity1.getPickRadius() + 2.0);
         var intercept = box.clip(eyes, end);
         if (box.contains(eyes)) {
            if (d2 >= 0.0) {
               pointedEntity = entity1;
               d2 = 0.0;
            }
         } else if (intercept.isPresent()) {
            double d3 = eyes.distanceTo(intercept.get());
            if (d3 < d2 || d2 == 0.0) {
               if (entity1.getRootVehicle() != rider.getRootVehicle()) {
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

   /** DragonUtils.getBlockInViewHippogryph */
   @Nullable
   public static BlockPos getBlockInViewHippogryph(EntityHippogryph hippo) {
      float radius = -12.599999F - (float)hippo.getRandom().nextInt(48);
      float neg = hippo.getRandom().nextBoolean() ? 1.0F : -1.0F;
      float angle = (float)(Math.PI / 180.0) * hippo.yBodyRot + 3.15F + hippo.getRandom().nextFloat() * neg;
      double extraX = (double)(radius * Mth.sin((float)(Math.PI + (double)angle)));
      double extraZ = (double)(radius * Mth.cos(angle));
      if (hippo.hasHomePosition && hippo.homePos != null) {
         BlockPos ground = getHeight(hippo.level(), pos(hippo));
         int distFromGround = (int)hippo.getY() - ground.getY();
         for (int i = 0; i < 10; i++) {
            int home = AirWaterConfig.dragonWanderFromHomeDistance;
            BlockPos p = new BlockPos(hippo.homePos.getX() + hippo.getRandom().nextInt(home) - home,
               distFromGround > 16 ? (int)Math.min((double)AirWaterConfig.maxDragonFlight, hippo.getY() + (double)hippo.getRandom().nextInt(16) - 8.0)
                  : (int)hippo.getY() + hippo.getRandom().nextInt(16) + 1,
               hippo.homePos.getZ() + hippo.getRandom().nextInt(home * 2) - home);
            if (!hippo.isTargetBlocked(Vec3.atLowerCornerOf(p)) && distanceSqToCenter(hippo, p) > 6.0) {
               return p;
            }
         }
      }

      BlockPos radialPos = BlockPos.containing(hippo.getX() + extraX, 0.0, hippo.getZ() + extraZ);
      BlockPos ground = getHeight(hippo.level(), radialPos);
      int distFromGround = (int)hippo.getY() - ground.getY();
      BlockPos newPos = radialPos.above(distFromGround > 16
         ? (int)Math.min((double)AirWaterConfig.maxDragonFlight, hippo.getY() + (double)hippo.getRandom().nextInt(16) - 8.0)
         : (int)hippo.getY() + hippo.getRandom().nextInt(16) + 1);
      return !hippo.isTargetBlocked(Vec3.atLowerCornerOf(newPos)) && distanceSqToCenter(hippo, newPos) > 6.0 ? newPos : null;
   }

   /** DragonUtils.getBlockInViewStymphalian */
   @Nullable
   public static BlockPos getBlockInViewStymphalian(EntityStymphalianBird bird) {
      float radius = -9.45F - (float)bird.getRandom().nextInt(24);
      float neg = bird.getRandom().nextBoolean() ? 1.0F : -1.0F;
      float renderYawOffset = bird.flock != null && !bird.flock.isLeader(bird) ? getStymphalianFlockDirection(bird) : bird.yBodyRot;
      float angle = (float)(Math.PI / 180.0) * renderYawOffset + 3.15F + bird.getRandom().nextFloat() * neg;
      double extraX = (double)(radius * Mth.sin((float)(Math.PI + (double)angle)));
      double extraZ = (double)(radius * Mth.cos(angle));
      BlockPos radialPos = getStymphalianFearPos(bird, BlockPos.containing(bird.getX() + extraX, 0.0, bird.getZ() + extraZ));
      BlockPos ground = getHeight(bird.level(), radialPos);
      int distFromGround = (int)bird.getY() - ground.getY();
      int flightHeight = Math.min(AirWaterConfig.stymphalianBirdFlightHeight,
         bird.flock != null && !bird.flock.isLeader(bird) ? ground.getY() + bird.getRandom().nextInt(16) : ground.getY() + bird.getRandom().nextInt(16));
      BlockPos newPos = radialPos.above(distFromGround > 16 ? flightHeight : (int)bird.getY() + bird.getRandom().nextInt(16) + 1);
      return !bird.isTargetBlocked(Vec3.atLowerCornerOf(newPos)) && distanceSqToCenter(bird, newPos) > 6.0 ? newPos : null;
   }

   private static BlockPos getStymphalianFearPos(EntityStymphalianBird bird, BlockPos fallback) {
      if (bird.getVictor() != null && bird.getVictor() instanceof PathfinderMob victor) {
         Vec3 vec3d = com.github.alexthe666.iceandfire.entity.ai.legacy.RandomPositionGenerator.findRandomTargetBlockAwayFrom(victor, 16,
            AirWaterConfig.stymphalianBirdFlightHeight, victor.position());
         if (vec3d != null) {
            BlockPos p = BlockPos.containing(vec3d);
            return new BlockPos(p.getX(), 0, p.getZ());
         }
      }
      return fallback;
   }

   private static float getStymphalianFlockDirection(EntityStymphalianBird bird) {
      EntityStymphalianBird leader = bird.flock.getLeader();
      if (bird.distanceToSqr(leader) > 2.0) {
         double d0 = leader.getX() - bird.getX();
         double d2 = leader.getZ() - bird.getZ();
         float f = (float)(Mth.atan2(d2, d0) * (180.0 / Math.PI)) - 90.0F;
         float degrees = Mth.wrapDegrees(f - bird.getYRot());
         return bird.getYRot() + degrees;
      } else {
         return leader.yBodyRot;
      }
   }

   /** DragonUtils.getBlockInTargetsViewSeaSerpent */
   public static BlockPos getBlockInTargetsViewSeaSerpent(EntitySeaSerpent serpent, LivingEntity target) {
      float radius = 10.0F * serpent.getSeaSerpentScale() + (float)serpent.getRandom().nextInt(10);
      serpent.getRandom().nextBoolean();
      float angle = (float)(Math.PI / 180.0) * target.yHeadRot;
      double extraX = (double)(radius * Mth.sin((float)(Math.PI + (double)angle)));
      double extraZ = (double)(radius * Mth.cos(angle));
      BlockPos radialPos = BlockPos.containing(target.getX() + extraX, 0.0, target.getZ() + extraZ);
      BlockPos ground = getHeight(target.level(), radialPos);
      return distanceSqToCenter(serpent, ground) > 30.0 ? ground : pos(target);
   }

   /** EventLiving.updateRotation */
   public static float updateRotation(float angle, float targetAngle, float maxIncrease) {
      float f = Mth.wrapDegrees(targetAngle - angle);
      if (f > maxIncrease) {
         f = maxIncrease;
      }
      if (f < -maxIncrease) {
         f = -maxIncrease;
      }
      return angle + f;
   }

   /** 1.12 MathHelper.sqrt */
   public static float sqrt(double d) {
      return (float)Math.sqrt(d);
   }

   public static boolean isOwner(Mob mob, LivingEntity e) {
      return mob instanceof TamableAnimal t && t.isOwnedBy(e);
   }
}
