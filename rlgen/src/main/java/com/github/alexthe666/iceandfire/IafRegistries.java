package com.github.alexthe666.iceandfire;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Shared registers of the iceandfire mod. Every part of the port adds its own entries from its own holder classes. */
public final class IafRegistries {
   private IafRegistries() {
   }

   public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(IceAndFire.MODID);
   public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(IceAndFire.MODID);
   public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, IceAndFire.MODID);
   public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, IceAndFire.MODID);
   public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, IceAndFire.MODID);
   public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, IceAndFire.MODID);
   public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, IceAndFire.MODID);
   public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, IceAndFire.MODID);
   public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, IceAndFire.MODID);
   public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, IceAndFire.MODID);
   public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, IceAndFire.MODID);

   static void register(IEventBus modBus) {
      BLOCKS.register(modBus);
      ITEMS.register(modBus);
      ENTITY_TYPES.register(modBus);
      BLOCK_ENTITY_TYPES.register(modBus);
      SOUND_EVENTS.register(modBus);
      MENUS.register(modBus);
      CREATIVE_TABS.register(modBus);
      PARTICLE_TYPES.register(modBus);
      MOB_EFFECTS.register(modBus);
      RECIPE_SERIALIZERS.register(modBus);
      ATTACHMENT_TYPES.register(modBus);
   }
}
