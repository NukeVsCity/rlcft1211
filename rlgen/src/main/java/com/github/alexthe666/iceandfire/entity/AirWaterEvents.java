package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.message.AirWaterNetwork;
import java.util.List;
import java.util.Random;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** The Ice and Fire 1.7.1 EventLiving handlers of the air and water mobs. */
public final class AirWaterEvents {
   private static final Random rand = new Random();

   private AirWaterEvents() {
   }

   /** EventLiving.onEntityMount: an owner getting off a hippogryph puts it where the player is. */
   @SubscribeEvent
   public static void onEntityMount(EntityMountEvent event) {
      if (event.getEntityBeingMounted() instanceof EntityHippogryph hippogryph && event.isDismounting()
         && event.getEntityMounting() instanceof Player player && !player.level().isClientSide && hippogryph.isOwnedBy(player)) {
         hippogryph.absMoveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
      }
   }

   /** EventLiving.onLivingUpdate (siren part): charmed players and villagers walk/swim to the siren that sings to them. */
   @SubscribeEvent
   public static void onEntityUpdate(EntityTickEvent.Pre event) {
      if (!(event.getEntity() instanceof LivingEntity living)) {
         return;
      }
      if (living instanceof Player || living instanceof Villager || living instanceof IHearsSiren) {
         SirenEntityProperties sirenProps = SirenEntityProperties.get(living);
         if (sirenProps != null && sirenProps.sirenID != 0) {
            EntitySiren closestSiren = (EntitySiren)sirenProps.getSiren(living.level());
            if (closestSiren != null && closestSiren.isActuallySinging()) {
               if (!EntitySiren.isWearingEarplugs(living) && sirenProps.singTime <= com.github.alexthe666.iceandfire.IceAndFire.CONFIG.sirenMaxSingTime) {
                  sirenProps.isCharmed = true;
                  sirenProps.singTime++;
                  if (rand.nextInt(7) == 0) {
                     for (int i = 0; i < 5; i++) {
                        living.level().addParticle(ParticleTypes.HEART, living.getX() + (rand.nextDouble() - 0.5) * 3.0,
                           living.getY() + (rand.nextDouble() - 0.5) * 3.0, living.getZ() + (rand.nextDouble() - 0.5) * 3.0, 0.0, 0.0, 0.0);
                     }
                  }
                  if (living.horizontalCollision) {
                     if (living instanceof Mob mob) {
                        mob.getJumpControl().jump();
                     } else if (living.onGround()) {
                        Vec3 m = living.getDeltaMovement();
                        living.setDeltaMovement(m.x, 0.42F, m.z);
                     }
                  }
                  Vec3 m = living.getDeltaMovement();
                  double mx = m.x + (Math.signum(closestSiren.getX() - living.getX()) * 0.5 - m.x) * 0.100000000372529;
                  double my = m.y + (Math.signum(closestSiren.getY() - living.getY() + 1.0) * 0.5 - m.y) * 0.100000000372529;
                  double mz = m.z + (Math.signum(closestSiren.getZ() - living.getZ()) * 0.5 - m.z) * 0.100000000372529;
                  living.setDeltaMovement(mx, my, mz);
                  double d0 = closestSiren.getX() - living.getX();
                  double d2 = closestSiren.getZ() - living.getZ();
                  double d1 = closestSiren.getY() - 1.0 - living.getY();
                  if (living.isPassenger()) {
                     living.stopRiding();
                  }
                  double d3 = Math.sqrt(d0 * d0 + d2 * d2);
                  float f = (float)(Mth.atan2(d2, d0) * (180.0 / Math.PI)) - 90.0F;
                  float f1 = (float)(-(Mth.atan2(d1, d3) * (180.0 / Math.PI)));
                  living.setXRot(AirWaterUtils.updateRotation(living.getXRot(), f1, 30.0F));
                  living.setYRot(AirWaterUtils.updateRotation(living.getYRot(), f, 30.0F));
                  if ((double)living.distanceTo(closestSiren) < 5.0) {
                     sirenProps.isCharmed = false;
                     sirenProps.sirenID = 0;
                     sirenProps.singTime = 0;
                     closestSiren.singCooldown = com.github.alexthe666.iceandfire.IceAndFire.CONFIG.sirenTimeBetweenSongs;
                     closestSiren.setSinging(false);
                     closestSiren.setTarget(living);
                     closestSiren.setAggressive(true);
                     closestSiren.triggerOtherSirens(living);
                  }
                  if (!closestSiren.isAlive() || living.distanceTo(closestSiren) > 64.0F || sirenProps.getSiren(living.level()) == null
                     || living instanceof Player p && p.isCreative()) {
                     sirenProps.isCharmed = false;
                     sirenProps.sirenID = 0;
                     sirenProps.singTime = 0;
                  }
               } else {
                  sirenProps.isCharmed = false;
                  sirenProps.sirenID = 0;
                  sirenProps.singTime = 0;
                  closestSiren.singCooldown = com.github.alexthe666.iceandfire.IceAndFire.CONFIG.sirenTimeBetweenSongs;
               }
            }
         }
      }
   }

   /** EventLiving.onLivingAttacked: amphitheres defend villagers that are attacked. */
   @SubscribeEvent
   public static void onLivingAttacked(LivingIncomingDamageEvent event) {
      Entity attacker = event.getSource().getEntity();
      if (attacker instanceof LivingEntity livingAttacker && AirWaterUtils.isVillager(event.getEntity())) {
         signalAmphithereAlarm(event.getEntity(), livingAttacker);
      }
   }

   /** EventLiving.onLivingSetTarget */
   @SubscribeEvent
   public static void onLivingSetTarget(LivingChangeTargetEvent event) {
      if (event.getNewAboutToBeSetTarget() != null && AirWaterUtils.isVillager(event.getNewAboutToBeSetTarget())) {
         signalAmphithereAlarm(event.getNewAboutToBeSetTarget(), event.getEntity());
      }
   }

   private static void signalAmphithereAlarm(LivingEntity villager, LivingEntity attacker) {
      float d0 = com.github.alexthe666.iceandfire.IceAndFire.CONFIG.amphithereVillagerSearchLength;
      List<EntityAmphithere> list = villager.level().getEntitiesOfClass(EntityAmphithere.class,
         new AABB(villager.getX() - 1.0, villager.getY() - 1.0, villager.getZ() - 1.0, villager.getX() + 1.0, villager.getY() + 1.0,
            villager.getZ() + 1.0).inflate(d0, d0, d0));
      list.sort(new com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAINearestAttackableTarget.Sorter(attacker));
      for (EntityAmphithere amphithere : list) {
         if (!(attacker instanceof EntityAmphithere) && !AirWaterUtils.hasSameOwner(amphithere, attacker)) {
            if (attacker instanceof Player player) {
               if (!player.isCreative() && !amphithere.isOwnedBy(player)) {
                  amphithere.setTarget(player);
               }
            } else {
               amphithere.setTarget(attacker);
            }
         }
      }
   }
}
