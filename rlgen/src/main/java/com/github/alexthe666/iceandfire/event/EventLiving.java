package com.github.alexthe666.iceandfire.event;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.core.ModBlocks;
import com.github.alexthe666.iceandfire.core.ModEntityHooks;
import com.github.alexthe666.iceandfire.core.ModItems;
import com.github.alexthe666.iceandfire.entity.ChickenEntityProperties;
import com.github.alexthe666.iceandfire.entity.FrozenEntityProperties;
import com.github.alexthe666.iceandfire.entity.MiscPlayerProperties;
import com.github.alexthe666.iceandfire.item.ItemSeaSerpentArmor;
import com.github.alexthe666.iceandfire.item.ItemTrollArmor;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * The iaf/core part of 1.12 EventLiving: projectile friendly fire on ridden tamed mounts, no fall damage after leaving a dragon, troll
 * armor projectile reduction, wither bones from wither skeletons, tide armor (water breathing + strength in water), chickens laying
 * rotten eggs, the frozen state (dragon ice / ice sword), manuscripts in dungeon chests, dismount on logout. The mob parts of
 * EventLiving (stone statues, siren charm, multipart hits, dragon/gold aggro, villager/animal fear goals, mount offsets) belong to the
 * mob branches and live in their own handler classes.
 */
public class EventLiving {
   Random rand = new Random();

   @SubscribeEvent
   public void onArrowCollide(ProjectileImpactEvent event) {
      if (event.getProjectile() instanceof AbstractArrow arrow && arrow.getOwner() != null && event.getRayTraceResult() instanceof EntityHitResult hit) {
         Entity shootingEntity = arrow.getOwner();
         Entity shotEntity = hit.getEntity();
         if (shootingEntity instanceof LivingEntity && (shootingEntity.hasIndirectPassenger(shotEntity) || shotEntity.hasIndirectPassenger(shootingEntity))) {
            if (shotEntity instanceof TamableAnimal tame && tame.isTame() && shotEntity.isAlliedTo(shootingEntity)) {
               event.setCanceled(true);
            }
         }
      }
   }

   @SubscribeEvent
   public void onEntityFall(LivingFallEvent event) {
      if (event.getEntity() instanceof Player) {
         MiscPlayerProperties properties = MiscPlayerProperties.get(event.getEntity());
         if (properties != null && properties.hasDismountedDragon) {
            event.setDamageMultiplier(0.0F);
            properties.hasDismountedDragon = false;
         }
      }
   }

   /** 1.12 LivingHurtEvent on projectiles: each troll armor piece takes 10/30/20/10 % off. */
   @SubscribeEvent
   public void onEntityDamage(LivingDamageEvent.Pre event) {
      if (event.getSource().is(DamageTypeTags.IS_PROJECTILE)) {
         float multi = 1.0F;
         LivingEntity e = event.getEntity();
         if (e.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof ItemTrollArmor) {
            multi = (float)((double)multi - 0.1);
         }
         if (e.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ItemTrollArmor) {
            multi = (float)((double)multi - 0.3);
         }
         if (e.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof ItemTrollArmor) {
            multi = (float)((double)multi - 0.2);
         }
         if (e.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof ItemTrollArmor) {
            multi = (float)((double)multi - 0.1);
         }
         event.setNewDamage(event.getNewDamage() * multi);
      }
   }

   @SubscribeEvent
   public void onEntityDrop(LivingDropsEvent event) {
      LivingEntity e = event.getEntity();
      if (e instanceof WitherSkeleton) {
         int n = e.getRandom().nextInt(2);
         if (n > 0) {
            event.getDrops().add(new ItemEntity(e.level(), e.getX(), e.getY(), e.getZ(), new ItemStack(ModItems.witherbone, n)));
         }
      }
   }

   @SubscribeEvent
   public void onEntityUpdate(EntityTickEvent.Post event) {
      if (!(event.getEntity() instanceof LivingEntity living)) {
         return;
      }
      boolean head = living.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof ItemSeaSerpentArmor;
      boolean chest = living.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ItemSeaSerpentArmor;
      boolean legs = living.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof ItemSeaSerpentArmor;
      boolean feet = living.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof ItemSeaSerpentArmor;
      if (head || chest || legs || feet) {
         living.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 50, 0, false, false));
         if (living.isInWaterOrRain()) {
            int mod = (head ? 1 : 0) + (chest ? 1 : 0) + (legs ? 1 : 0) + (feet ? 1 : 0);
            living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 50, mod - 1, false, false));
         }
      }
      if (IceAndFire.CONFIG.chickensLayRottenEggs && !living.level().isClientSide && isAnimaniaChicken(living) && !living.isBaby() && living instanceof Animal) {
         ChickenEntityProperties chickenProps = ChickenEntityProperties.get(living);
         if (chickenProps != null) {
            if (chickenProps.timeUntilNextEgg < 0) {
               chickenProps.timeUntilNextEgg = 0;
            }
            if (chickenProps.timeUntilNextEgg == 0) {
               if (living.getRandom().nextInt(IceAndFire.CONFIG.cockatriceEggChance + 1) == 0 && living.tickCount > 30) {
                  living.playSound(SoundEvents.CHICKEN_HURT, 2.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
                  living.playSound(SoundEvents.CHICKEN_EGG, 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
                  living.spawnAtLocation(ModItems.rotten_egg, 1);
               }
               chickenProps.timeUntilNextEgg = chickenProps.generateTime();
            } else if (chickenProps.timeUntilNextEgg > 0) {
               chickenProps.timeUntilNextEgg--;
            }
         }
      }
      FrozenEntityProperties frozenProps = FrozenEntityProperties.get(living);
      if (frozenProps != null) {
         boolean prevFrozen = frozenProps.isFrozen;
         if (ModEntityHooks.is(living, "icedragon")) {
            frozenProps.isFrozen = false;
         }
         if (!living.level().isClientSide) {
            if (frozenProps.isFrozen && living.isOnFire()) {
               frozenProps.isFrozen = false;
               living.clearFire();
            }
            if (living.deathTime > 0) {
               frozenProps.isFrozen = false;
            }
            if (frozenProps.ticksUntilUnfrozen > 0) {
               frozenProps.ticksUntilUnfrozen--;
            } else {
               frozenProps.ticksUntilUnfrozen = 0;
               frozenProps.isFrozen = false;
            }
         }
         if (frozenProps.isFrozen && !(living instanceof Player p && p.isCreative())) {
            Vec3 m = living.getDeltaMovement();
            living.setDeltaMovement(m.x * 0.25, living instanceof EnderDragon ? m.y : m.y - 0.1, m.z * 0.25);
         }
         if (prevFrozen != frozenProps.isFrozen) {
            if (frozenProps.isFrozen) {
               living.playSound(SoundEvents.GLASS_PLACE, 1.0F, 1.0F);
            } else {
               for (int i = 0; i < 15; i++) {
                  living.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, ModBlocks.dragon_ice.defaultBlockState()),
                     living.getX() + (this.rand.nextDouble() - 0.5) * (double)living.getBbWidth(), living.getY() + this.rand.nextDouble() * (double)living.getBbHeight(),
                     living.getZ() + (this.rand.nextDouble() - 0.5) * (double)living.getBbWidth(), 0.0, 0.0, 0.0);
               }
               living.playSound(SoundEvents.GLASS_BREAK, 3.0F, 1.0F);
            }
         }
      }
   }

   /** 1.12 onChestGenerated: manuscripts (weight 10, quality 5) in the second pool ("pool1") of the dungeon chest. */
   @SubscribeEvent
   public void onChestGenerated(LootTableLoadEvent event) {
      if (event.getName().equals(ResourceLocation.withDefaultNamespace("chests/simple_dungeon"))) {
         try {
            List<LootPool> pools = ObfuscationReflectionHelper.getPrivateValue(LootTable.class, event.getTable(), "pools");
            if (pools != null && pools.size() > 1) {
               LootPool pool1 = pools.get(1);
               List<LootPoolEntryContainer> entries = new ArrayList<>(ObfuscationReflectionHelper.getPrivateValue(LootPool.class, pool1, "entries"));
               entries.add(LootItem.lootTableItem(ModItems.manuscript).setWeight(10).setQuality(5).build());
               ObfuscationReflectionHelper.setPrivateValue(LootPool.class, pool1, List.copyOf(entries), "entries");
            }
         } catch (RuntimeException e) {
            IceAndFire.LOGGER.warn("Could not add manuscripts to dungeon chests", e);
         }
      }
   }

   @SubscribeEvent
   public void onPlayerLeaveEvent(PlayerEvent.PlayerLoggedOutEvent event) {
      if (event.getEntity() != null && !event.getEntity().getPassengers().isEmpty()) {
         for (Entity e : new ArrayList<>(event.getEntity().getPassengers())) {
            e.stopRiding();
         }
      }
   }

   public static boolean isAnimaniaSheep(Entity entity) {
      String className = entity.getClass().getName();
      return className.contains("sheep") || entity instanceof net.minecraft.world.entity.animal.Sheep;
   }

   /** 1.12 isAnimaniaChicken: class name contains "chicken" or a vanilla chicken, a Mob, not a monster. */
   public static boolean isAnimaniaChicken(Entity entity) {
      String className = entity.getClass().getName();
      return (className.contains("chicken") || entity instanceof Chicken) && entity instanceof Mob && entity.getType().getCategory() != MobCategory.MONSTER;
   }

   public static boolean isAnimaniaFerret(Entity entity) {
      String className = entity.getClass().getName();
      return className.contains("ferret") || className.contains("polecat");
   }

}
