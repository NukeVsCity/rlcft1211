package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IafRegistries;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.event.EventLivingLand;
import com.github.alexthe666.iceandfire.message.MessageDeathWormControl;
import com.github.alexthe666.iceandfire.message.MessageStoneStatue;
import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registration of the Ice and Fire land mobs (1.12 CommonProxy.registerEntities ids 13..33: stone statue, gorgon, cyclops,
 * death worm, cockatrice, troll, myrmex). Every 1.12 entry used tracker(64, 1, true).
 */
public final class IafLandMobs {
   private IafLandMobs() {
   }

   private static <T extends net.minecraft.world.entity.Entity> Supplier<EntityType<T>> register(String name, EntityType.Builder<T> builder) {
      return IafRegistries.ENTITY_TYPES.register(name, () -> builder.clientTrackingRange(4).updateInterval(1).build(IceAndFire.MODID + ":" + name));
   }

   public static final Supplier<EntityType<EntityStoneStatue>> STONE_STATUE = register(
      "stonestatue", EntityType.Builder.of(EntityStoneStatue::new, MobCategory.MISC).sized(0.8F, 1.9F)
   );
   public static final Supplier<EntityType<EntityGorgon>> GORGON = register(
      "gorgon", EntityType.Builder.of(EntityGorgon::new, MobCategory.MONSTER).sized(0.8F, 1.99F)
   );
   public static final Supplier<EntityType<EntityCyclops>> CYCLOPS = register(
      "cyclops", EntityType.Builder.of(EntityCyclops::new, MobCategory.MONSTER).sized(1.95F, 7.4F)
   );

   public static final Supplier<EntityType<EntityDeathWorm>> DEATHWORM = register(
      "deathworm", EntityType.Builder.of(EntityDeathWorm::new, MobCategory.CREATURE).sized(0.8F, 0.8F)
   );
   public static final Supplier<EntityType<EntityDeathWormEgg>> DEATHWORM_EGG = register(
      "deathwormegg", EntityType.Builder.<EntityDeathWormEgg>of(EntityDeathWormEgg::new, MobCategory.MISC).sized(0.25F, 0.25F)
   );
   public static final Supplier<EntityType<EntityCockatrice>> COCKATRICE = register(
      "if_cockatrice", EntityType.Builder.of(EntityCockatrice::new, MobCategory.CREATURE).sized(0.95F, 0.95F)
   );
   public static final Supplier<EntityType<EntityCockatriceEgg>> COCKATRICE_EGG = register(
      "if_cockatriceegg", EntityType.Builder.<EntityCockatriceEgg>of(EntityCockatriceEgg::new, MobCategory.MISC).sized(0.25F, 0.25F)
   );

   static {
      egg("gorgon", GORGON, 13687199, 6833456);
      egg("cyclops", CYCLOPS, 11567726, 3809039);
      egg("deathworm", DEATHWORM, 13749667, 4340282);
      egg("if_cockatrice", COCKATRICE, 9392133, 5200419);
   }

   /** 1.12 builder.egg(main, sub): the forge spawn egg, here an item spawn_egg_<name> (STUB(iaf/core) if core makes spawn eggs). */
   private static void egg(String name, Supplier<? extends EntityType<? extends net.minecraft.world.entity.Mob>> type, int main, int sub) {
      IafRegistries.ITEMS.register("spawn_egg_" + name, () -> new DeferredSpawnEggItem(type, main, sub, new Item.Properties()));
   }

   public static void init(IEventBus modBus) {
      StoneEntityProperties.init();
      ChickenEntityProperties.init();
      modBus.addListener(IafLandMobs::spawnPlacements);
      modBus.addListener(IafLandMobs::attributes);
      modBus.addListener(IafLandMobs::payloads);
      NeoForge.EVENT_BUS.register(new EventLivingLand());
   }

   private static void attributes(EntityAttributeCreationEvent event) {
      event.put(STONE_STATUE.get(), EntityStoneStatue.createAttributes().build());
      event.put(GORGON.get(), EntityGorgon.createAttributes().build());
      event.put(CYCLOPS.get(), EntityCyclops.createAttributes().build());
      event.put(DEATHWORM.get(), EntityDeathWorm.createAttributes().build());
      event.put(COCKATRICE.get(), EntityCockatrice.createAttributes().build());
   }

   /** 1.12 getCanSpawnHere of each naturally spawning land mob. */
   private static void spawnPlacements(RegisterSpawnPlacementsEvent event) {
      event.register(DEATHWORM.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EntityDeathWorm::checkDeathWormSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
      event.register(COCKATRICE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EntityCockatrice::checkCockatriceSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
   }

   private static void payloads(RegisterPayloadHandlersEvent event) {
      PayloadRegistrar registrar = event.registrar(IceAndFire.MODID + "_land").versioned("1");
      registrar.playToClient(MessageStoneStatue.TYPE, MessageStoneStatue.CODEC, MessageStoneStatue::handle);
      registrar.playToServer(MessageDeathWormControl.TYPE, MessageDeathWormControl.CODEC, MessageDeathWormControl::handle);
   }
}
