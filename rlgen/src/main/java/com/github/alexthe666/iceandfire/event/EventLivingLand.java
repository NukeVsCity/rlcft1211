package com.github.alexthe666.iceandfire.event;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityCockatrice;
import com.github.alexthe666.iceandfire.entity.EntityCyclops;
import com.github.alexthe666.iceandfire.entity.EntityStoneStatue;
import com.github.alexthe666.iceandfire.entity.IafLand;
import com.github.alexthe666.iceandfire.entity.LandUtils;
import com.github.alexthe666.iceandfire.entity.StoneEntityProperties;
import com.github.alexthe666.iceandfire.entity.ai.EntitySheepAIFollowCyclops;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAINearestAttackableTarget;
import com.github.alexthe666.iceandfire.message.MessageStoneStatue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobDespawnEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The parts of Ice and Fire 1.7.1 EventLiving that belong to the land mobs: stone mobs (gorgon/cockatrice petrification),
 * cockatrice chicken alarm and rotten eggs, cyclops shepherd sheep.
 */
public class EventLivingLand {
   Random rand = new Random();

   @SubscribeEvent
   public void onEntityDrop(LivingDropsEvent event) {
      if (LandUtils.isStone(event.getEntity())) {
         event.setCanceled(true);
      }
   }

   /** EntityDeathWorm.onDeath: the drops are spawned at the surface above them. */
   @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST)
   public void onDeathWormDrops(LivingDropsEvent event) {
      if (event.getEntity() instanceof com.github.alexthe666.iceandfire.entity.EntityDeathWorm worm && !event.isCanceled()) {
         for (net.minecraft.world.entity.item.ItemEntity item : event.getDrops()) {
            worm.moveDropToSurface(item);
         }
      }
   }

   @SubscribeEvent
   public void onEntityDespawn(MobDespawnEvent event) {
      if (LandUtils.isStone(event.getEntity())) {
         event.setResult(MobDespawnEvent.Result.DENY);
      }
   }

   /** LivingAttackEvent */
   @SubscribeEvent
   public void onLivingAttacked(LivingIncomingDamageEvent event) {
      if (event.getSource().getEntity() != null) {
         Entity attacker = event.getSource().getEntity();
         if (LandUtils.isAnimaniaChicken(event.getEntity()) && attacker instanceof LivingEntity) {
            signalChickenAlarm(event.getEntity(), (LivingEntity)attacker);
         }
      }
   }

   /** LivingSetAttackTargetEvent */
   @SubscribeEvent
   public void onLivingSetTarget(LivingChangeTargetEvent event) {
      if (event.getNewAboutToBeSetTarget() != null) {
         LivingEntity attacker = event.getEntity();
         if (LandUtils.isAnimaniaChicken(event.getNewAboutToBeSetTarget())) {
            signalChickenAlarm(event.getNewAboutToBeSetTarget(), attacker);
         }
      }
   }

   private static void signalChickenAlarm(LivingEntity chicken, LivingEntity attacker) {
      float d0 = (float)IceAndFire.CONFIG.cockatriceChickenSearchLength;
      List<Entity> list = new ArrayList<>(
         chicken.level()
            .getEntitiesOfClass(
               EntityCockatrice.class,
               new AABB(chicken.getX(), chicken.getY(), chicken.getZ(), chicken.getX() + 1.0, chicken.getY() + 1.0, chicken.getZ() + 1.0).inflate((double)d0, 10.0, (double)d0)
            )
      );
      Collections.sort(list, new EntityAINearestAttackableTarget.Sorter(attacker));
      for (Entity entity : list) {
         if (entity instanceof EntityCockatrice && !(attacker instanceof EntityCockatrice)) {
            EntityCockatrice cockatrice = (EntityCockatrice)entity;
            if (!LandUtils.hasSameOwner(cockatrice, attacker)) {
               if (attacker instanceof Player player) {
                  if (!player.isCreative() && !cockatrice.isOwnedBy(player)) {
                     cockatrice.setTarget(player);
                  }
               } else {
                  cockatrice.setTarget(attacker);
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void onPlayerAttack(AttackEntityEvent event) {
      Player player = event.getEntity();
      Entity target = event.getTarget();
      if (target != null && LandUtils.isAnimaniaSheep(target)) {
         float dist = (float)IceAndFire.CONFIG.cyclopesSheepSearchLength;
         // 1.12 expand(dist, dist, dist): grows only towards the positive axes
         List<Entity> list = new ArrayList<>(target.level().getEntities(player, player.getBoundingBox().expandTowards((double)dist, (double)dist, (double)dist)));
         Collections.sort(list, new EntityAINearestAttackableTarget.Sorter(player));
         for (Entity entity : list) {
            if (entity instanceof EntityCyclops cyclops) {
               if (!cyclops.isBlinded() && !player.getAbilities().instabuild) {
                  cyclops.setTarget(player);
               }
            }
         }
      }

      if (target instanceof Mob living) {
         boolean stonePlayer = target instanceof EntityStoneStatue;
         StoneEntityProperties properties = StoneEntityProperties.get(living);
         if (properties != null && properties.isStone || stonePlayer) {
            living.setHealth(living.getMaxHealth());
            if (player != null) {
               ItemStack stack = player.getMainHandItem();
               if (!stack.isEmpty()
                  && (stack.isCorrectToolForDrops(Blocks.STONE.defaultBlockState()) || stack.getItem().getDescriptionId().contains("pickaxe"))) {
                  boolean silkTouch = EnchantmentHelper.getItemEnchantmentLevel(
                        player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), stack
                     )
                     > 0;
                  boolean ready = false;
                  if (properties != null && !stonePlayer) {
                     properties.breakLvl++;
                     ready = properties.breakLvl > 9;
                  }

                  if (stonePlayer) {
                     EntityStoneStatue statue = (EntityStoneStatue)target;
                     statue.setCrackAmount(statue.getCrackAmount() + 1);
                     ready = statue.getCrackAmount() > 9;
                  }

                  if (ready) {
                     target.discard();
                     if (silkTouch) {
                        ItemStack statuette = new ItemStack(IafLand.item("stone_statue"));
                        CompoundTag tag = new CompoundTag();
                        tag.putBoolean("IAFStoneStatueEntityPlayer", stonePlayer);
                        // 1.12 stored the numeric entity id (90 for the player statue); 1.21 has none, so the type name is stored
                        tag.putInt("IAFStoneStatueEntityID", stonePlayer ? 90 : -1);
                        tag.putString("IAFStoneStatueEntityType", BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString());
                        living.addAdditionalSaveData(tag);
                        if (!target.level().isClientSide) {
                           CustomData.set(DataComponents.CUSTOM_DATA, statuette, tag);
                           target.spawnAtLocation(statuette, 1.0F);
                        }
                     } else if (!living.level().isClientSide) {
                        target.spawnAtLocation(new ItemStack(Blocks.COBBLESTONE, 2 + player.getRandom().nextInt(4)), 0.0F);
                     }
                  }
               }
            }
         }
      }
   }

   /** LivingUpdateEvent: stone mobs stay still. */
   @SubscribeEvent
   public void onEntityUpdate(EntityTickEvent.Pre event) {
      if (!(event.getEntity() instanceof LivingEntity entity)) {
         return;
      }

      // rotten eggs from chickens: iaf/core EventLiving

      if (entity instanceof Mob living) {
         boolean stonePlayer = living instanceof EntityStoneStatue;
         if (LandUtils.isStone(living) || stonePlayer) {
            if (!living.getPassengers().isEmpty()) {
               for (Entity e : new ArrayList<>(living.getPassengers())) {
                  e.stopRiding();
               }
            }

            Vec3 m = living.getDeltaMovement();
            living.setDeltaMovement(m.x * 0.0, m.y - 0.1, m.z * 0.0);
            living.attackAnim = 0.0F;
            living.walkAnimation.setSpeed(0.0F);
            living.setInvisible(!stonePlayer);
            living.ambientSoundTime = 0;
            living.hurtTime = 0;
            living.invulnerableTime = 20 - 1;
            living.clearFire();
            if (living instanceof Animal animal) {
               animal.resetLove();
            }

            if (!living.isNoAi()) {
               living.setNoAi(true);
            }

            if (living.getTarget() != null) {
               living.setTarget(null);
            }

            if (living instanceof AbstractHorse horse) {
               horse.setEating(false);
            }
         }
      }
   }

   // 1.12 onEntityInteract (EntityInteract / EntityInteractSpecific) tested event.getEntityLiving(), which is the player and never
   // an EntityLiving, so interacting with stone mobs was never cancelled: not ported.

   @SubscribeEvent
   public void onEntityJoinWorld(EntityJoinLevelEvent event) {
      if (event.getEntity() != null && LandUtils.isAnimaniaSheep(event.getEntity()) && event.getEntity() instanceof Animal animal) {
         animal.goalSelector.addGoal(8, new EntitySheepAIFollowCyclops(animal, 1.2));
      }
   }

   /** LLibrary sent tracked EntityProperties to a player starting to track the entity. */
   @SubscribeEvent
   public void onStartTracking(PlayerEvent.StartTracking event) {
      if (event.getEntity() instanceof ServerPlayer player && LandUtils.isStone(event.getTarget())) {
         PacketDistributor.sendToPlayer(player, new MessageStoneStatue(event.getTarget().getId(), true));
      }
   }
}
