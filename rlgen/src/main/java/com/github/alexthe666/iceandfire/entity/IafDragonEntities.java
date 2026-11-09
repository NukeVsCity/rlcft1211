package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IafRegistries;
import com.github.alexthe666.iceandfire.event.DragonEvents;
import com.github.alexthe666.iceandfire.inventory.ContainerDragon;
import com.github.alexthe666.iceandfire.message.DragonNetwork;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * The dragon entities of Ice and Fire 1.7.1 CommonProxy (ids iceandfire:firedragon, icedragon, dragonegg, dragonskull,
 * dragonfire, dragonice, dragonfirecharge, dragonicecharge; 1.12 tracker range 64 blocks, update every tick), the dragon
 * inventory menu, the dragon messages and events.
 */
public final class IafDragonEntities {
   private IafDragonEntities() {
   }

   public static final DeferredHolder<EntityType<?>, EntityType<EntityFireDragon>> FIRE_DRAGON = IafRegistries.ENTITY_TYPES.register(
      "firedragon",
      () -> EntityType.Builder.<EntityFireDragon>of(EntityFireDragon::new, MobCategory.CREATURE)
         .sized(0.78F, 1.2F)
         .fireImmune()
         .clientTrackingRange(4)
         .updateInterval(1)
         .build("iceandfire:firedragon")
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityIceDragon>> ICE_DRAGON = IafRegistries.ENTITY_TYPES.register(
      "icedragon",
      () -> EntityType.Builder.<EntityIceDragon>of(EntityIceDragon::new, MobCategory.CREATURE)
         .sized(0.78F, 1.2F)
         .clientTrackingRange(4)
         .updateInterval(1)
         .build("iceandfire:icedragon")
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityDragonEgg>> DRAGON_EGG = IafRegistries.ENTITY_TYPES.register(
      "dragonegg",
      () -> EntityType.Builder.<EntityDragonEgg>of(EntityDragonEgg::new, MobCategory.MISC)
         .sized(0.45F, 0.55F)
         .fireImmune()
         .clientTrackingRange(4)
         .updateInterval(1)
         .build("iceandfire:dragonegg")
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityDragonSkull>> DRAGON_SKULL = IafRegistries.ENTITY_TYPES.register(
      "dragonskull",
      () -> EntityType.Builder.<EntityDragonSkull>of(EntityDragonSkull::new, MobCategory.MISC)
         .sized(0.9F, 0.65F)
         .clientTrackingRange(4)
         .updateInterval(1)
         .build("iceandfire:dragonskull")
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityDragonFire>> DRAGON_FIRE = IafRegistries.ENTITY_TYPES.register(
      "dragonfire",
      () -> EntityType.Builder.<EntityDragonFire>of(EntityDragonFire::new, MobCategory.MISC)
         .sized(1.0F, 1.0F)
         .clientTrackingRange(4)
         .updateInterval(1)
         .build("iceandfire:dragonfire")
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityDragonIceProjectile>> DRAGON_ICE = IafRegistries.ENTITY_TYPES.register(
      "dragonice",
      () -> EntityType.Builder.<EntityDragonIceProjectile>of(EntityDragonIceProjectile::new, MobCategory.MISC)
         .sized(1.0F, 1.0F)
         .clientTrackingRange(4)
         .updateInterval(1)
         .build("iceandfire:dragonice")
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityDragonFireCharge>> DRAGON_FIRE_CHARGE = IafRegistries.ENTITY_TYPES.register(
      "dragonfirecharge",
      () -> EntityType.Builder.<EntityDragonFireCharge>of(EntityDragonFireCharge::new, MobCategory.MISC)
         .sized(1.0F, 1.0F)
         .clientTrackingRange(4)
         .updateInterval(1)
         .build("iceandfire:dragonfirecharge")
   );
   public static final DeferredHolder<EntityType<?>, EntityType<EntityDragonIceCharge>> DRAGON_ICE_CHARGE = IafRegistries.ENTITY_TYPES.register(
      "dragonicecharge",
      () -> EntityType.Builder.<EntityDragonIceCharge>of(EntityDragonIceCharge::new, MobCategory.MISC)
         .sized(1.0F, 1.0F)
         .clientTrackingRange(4)
         .updateInterval(1)
         .build("iceandfire:dragonicecharge")
   );
   /** 1.12 GuiHandler id 0 (ContainerDragon). */
   public static final DeferredHolder<MenuType<?>, MenuType<ContainerDragon>> DRAGON_MENU = IafRegistries.MENUS.register(
      "dragon", () -> IMenuTypeExtension.create(ContainerDragon::new)
   );

   /** Called from the IceAndFire constructor. */
   public static void init(IEventBus modBus) {
      modBus.addListener(IafDragonEntities::attributes);
      modBus.addListener(DragonNetwork::register);
      NeoForge.EVENT_BUS.register(DragonEvents.class);
   }

   private static void attributes(EntityAttributeCreationEvent event) {
      event.put(FIRE_DRAGON.get(), EntityDragonBase.createAttributes().build());
      event.put(ICE_DRAGON.get(), EntityDragonBase.createAttributes().build());
      event.put(DRAGON_EGG.get(), EntityDragonEgg.createAttributes().build());
      event.put(DRAGON_SKULL.get(), EntityDragonSkull.createAttributes().build());
   }
}
