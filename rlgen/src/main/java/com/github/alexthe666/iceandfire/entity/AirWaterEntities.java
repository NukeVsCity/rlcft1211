package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IafRegistries;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.enums.EnumHippogryphTypes;
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

   public static final DeferredHolder<MenuType<?>, MenuType<ContainerHippogryph>> HIPPOGRYPH_MENU = IafRegistries.MENUS.register("hippogryph",
      () -> IMenuTypeExtension.create(ContainerHippogryph::fromNetwork));

   public static void register(IEventBus modBus) {
      modBus.addListener(AirWaterEntities::attributes);
      modBus.addListener(AirWaterNetwork::register);
      NeoForge.EVENT_BUS.register(AirWaterEvents.class);
      if (FMLEnvironment.dist.isClient()) {
         com.github.alexthe666.iceandfire.client.AirWaterClient.register(modBus);
      }
   }

   private static void attributes(EntityAttributeCreationEvent event) {
      event.put(HIPPOGRYPH.get(), EntityHippogryph.createAttributes().build());
      event.put(STYMPHALIAN_BIRD.get(), EntityStymphalianBird.createAttributes().build());
      event.put(SIREN.get(), EntitySiren.createAttributes().build());
   }

   public static ResourceKey<LootTable> loot(String name) {
      return ResourceKey.create(Registries.LOOT_TABLE, AirWaterRefs.id(name));
   }

   /** ItemHippogryphEgg stack of a type (1.12 metadata = type ordinal, kept as custom data "Variant"). */
   public static ItemStack hippogryphEgg(EnumHippogryphTypes type) {
      ItemStack stack = new ItemStack(AirWaterRefs.item("hippogryph_egg"));
      CompoundTag tag = new CompoundTag();
      tag.putInt("Variant", type.ordinal());
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
      return stack;
   }

   public static int hippogryphEggVariant(ItemStack stack) {
      CustomData data = stack.get(DataComponents.CUSTOM_DATA);
      return data == null ? 0 : data.copyTag().getInt("Variant");
   }

   /** client: new IFChainBuffer() */
   public static Object clientChainBuffer() {
      return FMLEnvironment.dist.isClient() ? com.github.alexthe666.iceandfire.client.AirWaterClient.newChainBuffer() : null;
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
      if (buffer != null) {
         com.github.alexthe666.iceandfire.client.AirWaterClient.waveBuffer(buffer, maxAngle, bufferTime, angleDecrement, entity);
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
