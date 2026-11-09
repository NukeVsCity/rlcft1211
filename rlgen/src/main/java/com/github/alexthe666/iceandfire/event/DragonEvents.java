package com.github.alexthe666.iceandfire.event;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.core.ModBlocks;
import com.github.alexthe666.iceandfire.entity.DragonCompat;
import com.github.alexthe666.iceandfire.entity.DragonUtils;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.IAnimalFear;
import com.github.alexthe666.iceandfire.entity.IVillagerFear;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.ChestBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/** The dragon parts of Ice and Fire 1.7.1 EventLiving. */
public final class DragonEvents {
   private DragonEvents() {
   }

   /** Harvesting a dragon corpse (1.21's Mob.interact ignores dead mobs, 1.12 did not). */
   @SubscribeEvent
   public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
      if (event.getTarget() instanceof EntityDragonBase dragon && dragon.isModelDead()) {
         boolean done = dragon.processDeadInteract(event.getEntity(), event.getHand());
         if (done) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
         }
      }
   }

   @SubscribeEvent
   public static void onEntityFall(LivingFallEvent event) {
      if (event.getEntity() instanceof Player player && DragonCompat.hasDismountedDragon(player)) {
         event.setDamageMultiplier(0.0F);
         DragonCompat.setDismountedDragon(player, false);
      }
   }

   @SubscribeEvent
   public static void onEntityMount(EntityMountEvent event) {
      if (event.getEntityBeingMounted() instanceof EntityDragonBase dragon) {
         if (event.isDismounting() && event.getEntityMounting() instanceof Player player && !player.level().isClientSide) {
            if (dragon.isOwner(player)) {
               dragon.absMoveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
               player.fallDistance = -dragon.getBbHeight();
            } else {
               dragon.yBodyRot = dragon.getYRot();
               float modTick_0 = (float)(dragon.getAnimationTick() - 25);
               float modTick_1 = dragon.getAnimationTick() > 25 && dragon.getAnimationTick() < 55
                  ? 8.0F * Mth.clamp(Mth.sin((float)(Math.PI + (double)modTick_0 * 0.25)), -0.8F, 0.8F)
                  : 0.0F;
               float modTick_2 = dragon.getAnimationTick() > 30 ? 10.0F : (float)Math.max(0, dragon.getAnimationTick() - 20);
               float radius = 0.75F * (0.6F * dragon.getRenderSize() / 3.0F) * -3.0F;
               float angle = (float) (Math.PI / 180.0) * dragon.yBodyRot + 3.15F + modTick_1 * 2.0F * 0.015F;
               double extraX = (double)(radius * Mth.sin((float)(Math.PI + (double)angle)));
               double extraZ = (double)(radius * Mth.cos(angle));
               double extraY = modTick_2 == 0.0F ? 0.0 : 0.035F * ((double)(dragon.getRenderSize() / 3.0F) + (double)modTick_2 * 0.5 * (double)(dragon.getRenderSize() / 3.0F));
               player.setPos(dragon.getX() + extraX, dragon.getY() + extraY, dragon.getZ() + extraZ);
            }
         }
      }
   }

   /** Looking straight down while riding a dragon interacts with it (opens the inventory when sneaking). */
   @SubscribeEvent
   public static void onEntityUseItem(PlayerInteractEvent.RightClickItem event) {
      Player player = event.getEntity();
      if (player.getXRot() > 87.0F && player.getVehicle() instanceof EntityDragonBase dragon) {
         dragon.mobInteract(player, event.getHand());
      }
   }

   private static void alertDragons(Player player) {
      float dist = (float)IceAndFire.CONFIG.dragonGoldSearchLength;
      List<Entity> list = new ArrayList<>(player.level().getEntities(player, player.getBoundingBox().expandTowards((double)dist, (double)dist, (double)dist)));
      list.sort(Comparator.comparingDouble(player::distanceToSqr));
      for (Entity entity : list) {
         if (entity instanceof EntityDragonBase dragon) {
            if (!dragon.isTame() && !dragon.isModelDead() && !dragon.isOwner(player) && !player.isCreative()) {
               dragon.setSleeping(false);
               dragon.setSitting(false);
               dragon.setTarget(player);
            }
         }
      }
   }

   /** Opening a chest wakes untamed dragons within the gold search length (32) and makes them attack (1.12 box grows to +x/+y/+z only). */
   @SubscribeEvent
   public static void onPlayerRightClick(PlayerInteractEvent.RightClickBlock event) {
      if (event.getLevel().getBlockState(event.getPos()).getBlock() instanceof ChestBlock) {
         alertDragons(event.getEntity());
      }
   }

   /** Breaking a gold or silver pile does the same. */
   @SubscribeEvent
   public static void onBreakBlock(BlockEvent.BreakEvent event) {
      if (event.getPlayer() != null
         && (event.getState().getBlock() == DragonCompat.block(ModBlocks.goldPile) || event.getState().getBlock() == DragonCompat.block(ModBlocks.silverPile))) {
         alertDragons(event.getPlayer());
      }
   }

   @SubscribeEvent
   public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
      for (Entity passenger : new ArrayList<>(event.getEntity().getPassengers())) {
         passenger.stopRiding();
      }
   }

   /** 1.12 VillagerAIFearUntamed (EntityAIAvoidEntity, priority 1) given to villagers and livestock; tamed mobs are not feared. */
   @SubscribeEvent
   public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
      Entity entity = event.getEntity();
      if (event.getLevel().isClientSide) {
         return;
      }
      if (DragonUtils.isVillager(entity) && entity instanceof PathfinderMob villager && IceAndFire.CONFIG.villagersFearDragons) {
         villager.goalSelector.addGoal(1, new AvoidEntityGoal<>(villager, LivingEntity.class, 12.0F, 0.8, 0.8,
            e -> e instanceof IVillagerFear && !(e instanceof TamableAnimal t && t.isTame())));
      }

      if (DragonUtils.isLivestock(entity) && entity instanceof PathfinderMob animal && IceAndFire.CONFIG.animalsFearDragons) {
         animal.goalSelector.addGoal(1, new AvoidEntityGoal<>(animal, LivingEntity.class, 12.0F, 1.2, 1.5,
            e -> e instanceof IAnimalFear fear && fear.shouldAnimalsFear(animal) && !(e instanceof TamableAnimal t && t.isTame())));
      }
   }
}
