package com.github.alexthe666.iceandfire.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The 1.12 DragonUtils / EventLiving helpers the land mobs use, plus reflective calls into classes other parts of the port
 * own (EntityDragonBase, EntityHippogryph), so this part compiles without them.
 */
public final class LandUtils {
   private LandUtils() {
   }

   /** EntityGorgon.isStoneMob without the Mob check: the StoneEntityProperties flag. */
   public static boolean isStone(@Nullable Entity entity) {
      StoneEntityProperties p = entity == null ? null : StoneEntityProperties.get(entity);
      return p != null && p.isStone;
   }

   /** properties.isStone = stone; then MessageStoneStatue to the tracking clients (LLibrary also re-syncs every 20 ticks). */
   public static void setStone(Entity entity, boolean stone) {
      StoneEntityProperties p = StoneEntityProperties.get(entity);
      if (p != null) {
         p.isStone = stone;
         if (!entity.level().isClientSide) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntity(entity, new com.github.alexthe666.iceandfire.message.MessageStoneStatue(entity.getId(), stone));
         }
      }
   }

   /** DragonUtils.isAlive */
   public static boolean isAlive(@Nullable LivingEntity entity) {
      return !(entity instanceof IDeadMob) || !((IDeadMob)entity).isMobDead();
   }

   /** DragonUtils.hasSameOwner */
   public static boolean hasSameOwner(TamableAnimal cockatrice, Entity entity) {
      if (entity instanceof TamableAnimal tameable) {
         return tameable.getOwnerUUID() != null && cockatrice.getOwnerUUID() != null && tameable.getOwnerUUID().equals(cockatrice.getOwnerUUID());
      }
      return false;
   }

   /** DragonUtils.isVillager */
   public static boolean isVillager(Entity entity) {
      String className = entity.getClass().getSimpleName();
      return entity instanceof Npc || className.contains("VillagerMCA") || className.contains("MillVillager") || className.contains("Citizen");
   }

   /** DragonUtils.isLivestock */
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

   /** EventLiving.isAnimaniaSheep */
   public static boolean isAnimaniaSheep(Entity entity) {
      return entity.getClass().getName().contains("sheep") || entity instanceof Sheep;
   }

   /** EventLiving.isAnimaniaChicken */
   public static boolean isAnimaniaChicken(Entity entity) {
      return (entity.getClass().getName().contains("chicken") || entity instanceof Chicken)
         && entity instanceof net.minecraft.world.entity.Mob
         && !(entity instanceof net.minecraft.world.entity.monster.Enemy);
   }

   /** EventLiving.isAnimaniaFerret */
   public static boolean isAnimaniaFerret(Entity entity) {
      String className = entity.getClass().getName();
      return className.contains("ferret") || className.contains("polecat");
   }

   /** FoodUtils.isSeeds: ItemSeeds (not nether wart) or the seed ore dictionary names; here the c:seeds tag. */
   public static boolean isSeeds(net.minecraft.world.item.ItemStack stack) {
      return stack.getItem() != net.minecraft.world.item.Items.NETHER_WART && stack.is(net.neoforged.neoforge.common.Tags.Items.SEEDS);
   }

   /** DragonUtils.riderLookingAtEntity */
   @Nullable
   public static LivingEntity riderLookingAtEntity(final LivingEntity dragon, LivingEntity rider, double dist) {
      Vec3 vec3d = rider.getEyePosition(1.0F);
      Vec3 vec3d1 = rider.getViewVector(1.0F);
      Vec3 vec3d2 = vec3d.add(vec3d1.x * dist, vec3d1.y * dist, vec3d1.z * dist);
      Entity pointedEntity = null;
      List<Entity> list = rider.level()
         .getEntities(
            rider,
            rider.getBoundingBox().expandTowards(vec3d1.x * dist, vec3d1.y * dist, vec3d1.z * dist).inflate(1.0, 1.0, 1.0),
            entity -> entity != null
               && !entity.isSpectator()
               && entity.isPickable()
               && entity instanceof LivingEntity
               && entity != dragon
               && !entity.isAlliedTo(dragon)
               && (!(entity instanceof IDeadMob) || !((IDeadMob)entity).isMobDead())
         );
      double d2 = dist;

      for (Entity entity1 : list) {
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

   /** 1.12 World.getHeight(pos): the first air block above the top of the column (MOTION_BLOCKING-like height map). */
   public static BlockPos getHeight(net.minecraft.world.level.Level level, BlockPos pos) {
      return new BlockPos(pos.getX(), level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()), pos.getZ());
   }

   // ---- reflective bridges to classes owned by other parts of the port ----

   private static boolean isA(Object o, String simpleName) {
      for (Class<?> c = o == null ? null : o.getClass(); c != null; c = c.getSuperclass()) {
         if (c.getSimpleName().equals(simpleName)) {
            return true;
         }
      }
      return false;
   }

   public static boolean isDragon(@Nullable Entity e) {
      return isA(e, "EntityDragonBase");
   }

   public static boolean isHippogryph(@Nullable Entity e) {
      return isA(e, "EntityHippogryph");
   }

   /** EntityDragonBase.isModelDead() */
   public static boolean isModelDead(Entity dragon) {
      Object r = call(dragon, "isModelDead");
      return r instanceof Boolean b && b;
   }

   /** Gorgon: dragon.setFlying(false); dragon.setHovering(false); dragon.airTarget = null (dragons and hippogryphs). */
   public static void stopFlying(Entity e) {
      if (isDragon(e) || isHippogryph(e)) {
         call(e, "setFlying", false);
         call(e, "setHovering", false);
         try {
            Field f = findField(e.getClass(), "airTarget");
            if (f != null) {
               f.setAccessible(true);
               f.set(e, null);
            }
         } catch (ReflectiveOperationException ignored) {
         }
      }
   }

   /** IDropArmor.dropArmor() (dragons) */
   public static void dropArmor(Entity e) {
      for (Class<?> i : e.getClass().getInterfaces()) {
         if (i.getSimpleName().equals("IDropArmor")) {
            call(e, "dropArmor");
            return;
         }
      }
      if (isDragon(e)) {
         call(e, "dropArmor");
      }
   }

   @Nullable
   private static Field findField(Class<?> c, String name) {
      for (; c != null; c = c.getSuperclass()) {
         try {
            return c.getDeclaredField(name);
         } catch (NoSuchFieldException ignored) {
         }
      }
      return null;
   }

   @Nullable
   private static Object call(Object o, String name, Object... args) {
      for (Class<?> c = o.getClass(); c != null; c = c.getSuperclass()) {
         for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == args.length) {
               try {
                  m.setAccessible(true);
                  return m.invoke(o, args);
               } catch (ReflectiveOperationException ignored) {
                  return null;
               }
            }
         }
      }
      return null;
   }
}
