package com.github.alexthe666.iceandfire.core;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.mojang.serialization.Codec;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 1.12 item metadata and stack NBT on 1.21 stacks. 1.7.1 keeps subtypes (dragon_skull 0/1, deathworm_chitin 0..2, podium 0..5,
 * jar_pixie 0..4, ...) as metadata of one item, so the port keeps one item and stores the metadata in the iceandfire:meta component.
 * Use {@link #stack(ItemLike, int, int)} for 1.12 {@code new ItemStack(item, count, meta)} and {@link #getMetadata(ItemStack)} for
 * {@code stack.getMetadata()}. Stack NBT (1.12 getTagCompound) lives in minecraft:custom_data: {@link #getTag}/{@link #setTag}.
 */
public final class IafMeta {
   private IafMeta() {
   }

   public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, IceAndFire.MODID);
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> META = COMPONENTS.registerComponentType("meta",
      b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

   public static void register(IEventBus modBus) {
      COMPONENTS.register(modBus);
   }

   /** 1.12 {@code new ItemStack(item, count, meta)}. */
   public static ItemStack stack(ItemLike item, int count, int meta) {
      ItemStack stack = new ItemStack(item, count);
      if (meta != 0) {
         stack.set(META.get(), meta);
      }
      if (stack.getItem() instanceof IMetaItem m) {
         m.onMetaSet(stack, meta);
      }
      return stack;
   }

   /** 1.12 {@code stack.getMetadata()}. */
   public static int getMetadata(ItemStack stack) {
      return stack.getOrDefault(META.get(), 0);
   }

   public static void setMetadata(ItemStack stack, int meta) {
      if (meta == 0) {
         stack.remove(META.get());
      } else {
         stack.set(META.get(), meta);
      }
      if (stack.getItem() instanceof IMetaItem m) {
         m.onMetaSet(stack, meta);
      }
   }

   /** 1.12 {@code stack.getTagCompound()}: a copy of the stack's custom data, or null if it has none. Write changes back with setTag. */
   @Nullable
   public static CompoundTag getTag(ItemStack stack) {
      CustomData data = stack.get(DataComponents.CUSTOM_DATA);
      return data == null ? null : data.copyTag();
   }

   /** 1.12 {@code stack.setTagCompound(tag)} (null removes it). */
   public static void setTag(ItemStack stack, @Nullable CompoundTag tag) {
      if (tag == null) {
         stack.remove(DataComponents.CUSTOM_DATA);
      } else {
         stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
      }
   }

   /** Edits the stack's 1.12 NBT in place (creating it when missing). */
   public static void editTag(ItemStack stack, Consumer<CompoundTag> edit) {
      CustomData.update(DataComponents.CUSTOM_DATA, stack, edit);
   }

   /** Items whose metadata changes other components (banner patterns of skulls, ...). */
   public interface IMetaItem {
      default void onMetaSet(ItemStack stack, int meta) {
      }
   }
}
