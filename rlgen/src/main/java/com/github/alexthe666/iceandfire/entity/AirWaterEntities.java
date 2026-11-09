package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IafRegistries;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.core.ModEntityHooks;
import com.github.alexthe666.iceandfire.enums.EnumHippogryphTypes;
import com.github.alexthe666.iceandfire.inventory.ContainerHippocampus;
import com.github.alexthe666.iceandfire.inventory.ContainerHippogryph;
import com.github.alexthe666.iceandfire.message.AirWaterNetwork;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Registers the Ice and Fire 1.7.1 air and water mobs (hippogryph, amphithere, stymphalian bird, siren, hippocampus, sea serpent, pixie,
 * snow villager) with their 1.12 registry names (CommonProxy.registerEntities: tracker 64 blocks, every tick), attributes, menus,
 * network and events. Client-only work goes through {@link #updateClientControls} and friends to the client package.
 */
public final class AirWaterEntities {
   private AirWaterEntities() {
   }

   public static final DeferredHolder<EntityType<?>, EntityType<EntityHippogryph>> HIPPOGRYPH = IafRegistries.ENTITY_TYPES.register("hippogryph",
      () -> EntityType.Builder.<EntityHippogryph>of(EntityHippogryph::new, MobCategory.CREATURE).sized(1.7F, 1.6F).clientTrackingRange(4)
         .updateInterval(1).build("hippogryph"));
   public static final DeferredHolder<EntityType<?>, EntityType<EntityHippogryphEgg>> HIPPOGRYPH_EGG = IafRegistries.ENTITY_TYPES.register(
      "hippogryphegg", () -> EntityType.Builder.<EntityHippogryphEgg>of(EntityHippogryphEgg::new, MobCategory.MISC).sized(0.25F, 0.25F)
         .clientTrackingRange(4).updateInterval(1).build("hippogryphegg"));

   public static final DeferredHolder<EntityType<?>, EntityType<EntityStymphalianBird>> STYMPHALIAN_BIRD = IafRegistries.ENTITY_TYPES.register(
      "stymphalianbird", () -> EntityType.Builder.<EntityStymphalianBird>of(EntityStymphalianBird::new, MobCategory.MONSTER).sized(1.3F, 1.2F)
         .clientTrackingRange(4).updateInterval(1).build("stymphalianbird"));
   public static final DeferredHolder<EntityType<?>, EntityType<EntityStymphalianFeather>> STYMPHALIAN_FEATHER = IafRegistries.ENTITY_TYPES.register(
      "stymphalianfeather", () -> EntityType.Builder.<EntityStymphalianFeather>of(EntityStymphalianFeather::new, MobCategory.MISC).sized(0.5F, 0.5F)
         .clientTrackingRange(4).updateInterval(1).build("stymphalianfeather"));
   public static final DeferredHolder<EntityType<?>, EntityType<EntityStymphalianArrow>> STYMPHALIAN_ARROW = IafRegistries.ENTITY_TYPES.register(
      "stymphalianarrow", () -> EntityType.Builder.<EntityStymphalianArrow>of(EntityStymphalianArrow::new, MobCategory.MISC).sized(0.5F, 0.5F)
         .clientTrackingRange(4).updateInterval(1).build("stymphalianarrow"));

   public static final DeferredHolder<EntityType<?>, EntityType<EntitySiren>> SIREN = IafRegistries.ENTITY_TYPES.register("siren",
      () -> EntityType.Builder.<EntitySiren>of(EntitySiren::new, MobCategory.MONSTER).sized(1.6F, 0.9F).clientTrackingRange(4).updateInterval(1)
         .build("siren"));

   public static final DeferredHolder<EntityType<?>, EntityType<EntityPixie>> PIXIE = IafRegistries.ENTITY_TYPES.register("if_pixie",
      () -> EntityType.Builder.<EntityPixie>of(EntityPixie::new, MobCategory.CREATURE).sized(0.4F, 0.8F).clientTrackingRange(4).updateInterval(1)
         .build("if_pixie"));

   public static final DeferredHolder<EntityType<?>, EntityType<EntityAmphithere>> AMPHITHERE = IafRegistries.ENTITY_TYPES.register("amphithere",
      () -> EntityType.Builder.<EntityAmphithere>of(EntityAmphithere::new, MobCategory.CREATURE).sized(2.5F, 1.25F).clientTrackingRange(4)
         .updateInterval(1).build("amphithere"));

   public static final DeferredHolder<EntityType<?>, EntityType<EntityHippocampus>> HIPPOCAMPUS = IafRegistries.ENTITY_TYPES.register("hippocampus",
      () -> EntityType.Builder.<EntityHippocampus>of(EntityHippocampus::new, MobCategory.WATER_CREATURE).sized(1.95F, 0.95F).clientTrackingRange(4)
         .updateInterval(1).build("hippocampus"));

   public static final DeferredHolder<EntityType<?>, EntityType<EntitySeaSerpent>> SEA_SERPENT = IafRegistries.ENTITY_TYPES.register("seaserpent",
      () -> EntityType.Builder.<EntitySeaSerpent>of(EntitySeaSerpent::new, MobCategory.CREATURE).sized(0.5F, 0.5F).clientTrackingRange(4)
         .updateInterval(1).build("seaserpent"));
   public static final DeferredHolder<EntityType<?>, EntityType<EntitySeaSerpentBubbles>> SEA_SERPENT_BUBBLES = IafRegistries.ENTITY_TYPES.register(
      "seaserpentbubble", () -> EntityType.Builder.<EntitySeaSerpentBubbles>of(EntitySeaSerpentBubbles::new, MobCategory.MISC).sized(0.8F, 0.8F)
         .clientTrackingRange(4).updateInterval(1).build("seaserpentbubble"));
   public static final DeferredHolder<EntityType<?>, EntityType<EntitySeaSerpentArrow>> SEA_SERPENT_ARROW = IafRegistries.ENTITY_TYPES.register(
      "seaserpentarrow", () -> EntityType.Builder.<EntitySeaSerpentArrow>of(EntitySeaSerpentArrow::new, MobCategory.MISC).sized(0.5F, 0.5F)
         .clientTrackingRange(4).updateInterval(1).build("seaserpentarrow"));

   public static final DeferredHolder<EntityType<?>, EntityType<EntitySnowVillager>> SNOW_VILLAGER = IafRegistries.ENTITY_TYPES.register("snowvillager",
      () -> EntityType.Builder.<EntitySnowVillager>of(EntitySnowVillager::new, MobCategory.MISC).sized(0.6F, 1.95F).eyeHeight(1.62F)
         .clientTrackingRange(4).updateInterval(1).build("snowvillager"));

   public static final DeferredHolder<EntityType<?>, EntityType<EntityAmphithereArrow>> AMPHITHERE_ARROW = IafRegistries.ENTITY_TYPES.register(
      "amphitherearrow", () -> EntityType.Builder.<EntityAmphithereArrow>of(EntityAmphithereArrow::new, MobCategory.MISC).sized(0.5F, 0.5F)
         .clientTrackingRange(4).updateInterval(1).build("amphitherearrow"));

   public static final DeferredHolder<MenuType<?>, MenuType<ContainerHippocampus>> HIPPOCAMPUS_MENU = IafRegistries.MENUS.register("hippocampus",
      () -> IMenuTypeExtension.create(ContainerHippocampus::fromNetwork));
   public static final DeferredHolder<MenuType<?>, MenuType<ContainerHippogryph>> HIPPOGRYPH_MENU = IafRegistries.MENUS.register("hippogryph",
      () -> IMenuTypeExtension.create(ContainerHippogryph::fromNetwork));

   public static void register(IEventBus modBus) {
      EntitySnowVillager.register(modBus);
      modBus.addListener(AirWaterEntities::attributes);
      modBus.addListener(AirWaterEntities::spawnPlacements);
      hooks();
      modBus.addListener(AirWaterNetwork::register);
      NeoForge.EVENT_BUS.register(AirWaterEvents.class);
      if (FMLEnvironment.dist.isClient()) {
         com.github.alexthe666.iceandfire.client.AirWaterClient.register(modBus);
      }
   }

   /** the item/block hooks of the core part (ItemHippogryphEgg, the arrows, TileEntityJar / TileEntityPixieHouse.releasePixie) */
   private static void hooks() {
      ModEntityHooks.throwHippogryphEgg = (level, player, stack) -> {
         EntityHippogryphEgg egg = new EntityHippogryphEgg(level, player, stack);
         egg.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
         level.addFreshEntity(egg);
      };
      ModEntityHooks.stymphalianArrow = (level, shooter, x, y, z, ammo) -> shooter != null
         ? new EntityStymphalianArrow(level, shooter, ammo.copyWithCount(1), null) : new EntityStymphalianArrow(level, x, y, z, ammo.copyWithCount(1), null);
      ModEntityHooks.amphithereArrow = (level, shooter, x, y, z, ammo) -> shooter != null
         ? new EntityAmphithereArrow(level, shooter, ammo.copyWithCount(1), null) : new EntityAmphithereArrow(level, x, y, z, ammo.copyWithCount(1), null);
      ModEntityHooks.seaSerpentArrow = (level, shooter, x, y, z, ammo) -> shooter != null
         ? new EntitySeaSerpentArrow(level, shooter, ammo.copyWithCount(1), null) : new EntitySeaSerpentArrow(level, x, y, z, ammo.copyWithCount(1), null);
      ModEntityHooks.releasePixie = (level, x, y, z, held, color, tamed, owner) -> {
         EntityPixie pixie = PIXIE.get().create(level);
         if (pixie == null) {
            return;
         }
         pixie.moveTo(x, y, z, (float)new java.util.Random().nextInt(360), 0.0F);
         pixie.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, held);
         pixie.setColor(color);
         pixie.ticksUntilHouseAI = 500;
         pixie.setTame(tamed, false);
         pixie.setOwnerUUID(owner);
         if (!level.isClientSide) {
            level.addFreshEntity(pixie);
         }
      };
      ModEntityHooks.PIXIE_PARTICLE_RGB = EntityPixie.PARTICLE_RGB;
      if (FMLEnvironment.dist.isClient()) {
         ModEntityHooks.pixieParticle = (level, x, y, z, r, g, b) -> spawnParticle("if_pixie", level, x, y, z, r, g, b);
      }
   }

   /**
    * natural spawns (biome modifiers spawn_hippogryph / spawn_amphithere): hippogryphs use EntityAnimal.getCanSpawnHere (grass, light),
    * amphitheres EntityAmphithere.getCanSpawnHere (the block above sees the sky).
    */
   private static void spawnPlacements(net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent event) {
      event.register(HIPPOGRYPH.get(), net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND, net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
         net.minecraft.world.entity.animal.Animal::checkAnimalSpawnRules, net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
      event.register(AMPHITHERE.get(), net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND, net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
         (type, level, reason, pos, random) -> level.canSeeSky(pos.above()), net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
   }

   private static void attributes(EntityAttributeCreationEvent event) {
      event.put(HIPPOGRYPH.get(), EntityHippogryph.createAttributes().build());
      event.put(STYMPHALIAN_BIRD.get(), EntityStymphalianBird.createAttributes().build());
      event.put(SIREN.get(), EntitySiren.createAttributes().build());
      event.put(PIXIE.get(), EntityPixie.createAttributes().build());
      event.put(AMPHITHERE.get(), EntityAmphithere.createAttributes().build());
      event.put(HIPPOCAMPUS.get(), EntityHippocampus.createAttributes().build());
      event.put(SEA_SERPENT.get(), EntitySeaSerpent.createAttributes().build());
      event.put(SNOW_VILLAGER.get(), net.minecraft.world.entity.npc.Villager.createAttributes().build());
   }

   public static ResourceKey<LootTable> loot(String name) {
      return ResourceKey.create(Registries.LOOT_TABLE, AirWaterRefs.id(name));
   }

   /** ItemHippogryphEgg.createEggStack: new ItemStack(hippogryph_egg, 1, type ordinal) */
   public static ItemStack hippogryphEgg(EnumHippogryphTypes type) {
      return com.github.alexthe666.iceandfire.core.IafMeta.stack(AirWaterRefs.item("hippogryph_egg"), 1, type.ordinal());
   }

   public static int hippogryphEggVariant(ItemStack stack) {
      return com.github.alexthe666.iceandfire.core.IafMeta.getMetadata(stack);
   }

   /** new ItemStack(ModBlocks.jar_pixie, 1, color) */
   public static ItemStack pixieJar(int color) {
      return com.github.alexthe666.iceandfire.core.IafMeta.stack(AirWaterRefs.item("jar_pixie"), 1, color);
   }

   /** client: new IFChainBuffer() */
   public static com.github.alexthe666.iceandfire.client.model.IFChainBuffer clientChainBuffer() {
      return FMLEnvironment.dist.isClient() ? new com.github.alexthe666.iceandfire.client.model.IFChainBuffer() : null;
   }

   /** client: new ChainBuffer() (llibrary) */
   public static net.ilexiconn.llibrary.client.model.tools.ChainBuffer clientLLibraryChainBuffer() {
      return FMLEnvironment.dist.isClient() ? new net.ilexiconn.llibrary.client.model.tools.ChainBuffer() : null;
   }

   /** client: IFChainBuffer.calculateChainFlapBuffer */
   public static void flapBuffer(Object buffer, float maxAngle, int bufferTime, float angleDecrement, LivingEntity entity) {
      if (buffer != null) {
         com.github.alexthe666.iceandfire.client.AirWaterClient.flapBuffer(buffer, maxAngle, bufferTime, angleDecrement, entity);
      }
   }

   /** client: ChainBuffer.calculateChainSwingBuffer */
   public static void swingBuffer(Object buffer, float maxAngle, int bufferTime, float angleDecrement, LivingEntity entity) {
      if (buffer != null) {
         com.github.alexthe666.iceandfire.client.AirWaterClient.swingBuffer(buffer, maxAngle, bufferTime, angleDecrement, entity);
      }
   }

   /** client: ChainBuffer.calculateChainWaveBuffer */
   public static void waveBuffer(Object buffer, float maxAngle, int bufferTime, float angleDecrement, LivingEntity entity) {
      waveBuffer(buffer, maxAngle, bufferTime, angleDecrement, 1.0F, entity);
   }

   public static void waveBuffer(Object buffer, float maxAngle, int bufferTime, float angleDecrement, float divisor, LivingEntity entity) {
      if (buffer != null) {
         com.github.alexthe666.iceandfire.client.AirWaterClient.waveBuffer(buffer, maxAngle, bufferTime, angleDecrement, divisor, entity);
      }
   }

   public static void flapBuffer(Object buffer, float maxAngle, int bufferTime, float angleDecrement, float divisor, LivingEntity entity) {
      if (buffer != null) {
         com.github.alexthe666.iceandfire.client.AirWaterClient.flapBuffer(buffer, maxAngle, bufferTime, angleDecrement, divisor, entity);
      }
   }

   /** IceAndFire.PROXY.spawnParticle(name, ...): the Ice and Fire particles, client side only (no-op on a server, as in 1.12). */
   public static void spawnParticle(String name, net.minecraft.world.level.Level level, double x, double y, double z, double vx, double vy, double vz) {
      if (level.isClientSide) {
         com.github.alexthe666.iceandfire.client.AirWaterClient.spawnParticle(name, level, x, y, z, vx, vy, vz);
      }
   }

   /** client: updateClientControls of the rideable mobs */
   public static void updateClientControls(Entity entity) {
      if (FMLEnvironment.dist.isClient()) {
         com.github.alexthe666.iceandfire.client.AirWaterClient.updateControls(entity);
      }
   }
}
