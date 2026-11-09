package com.github.alexthe666.iceandfire.entity;

import java.lang.reflect.Field;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Access to the 1.7.1 TileEntityPixieHouse / TileEntityJar fields (hasPixie, pixieType, pixieItems, tamedPixie, pixieOwnerUUID) the pixie
 * reads and writes. The block entities belong to the items/blocks part of the port; they are reached by their 1.12 class and field names
 * so the pixie does not depend on how that part is built.
 */
public final class PixieHouses {
   private PixieHouses() {
   }

   public static boolean isPixieHouse(@Nullable BlockEntity be) {
      return be != null && be.getClass().getSimpleName().equals("TileEntityPixieHouse");
   }

   private static Field field(Object o, String name) throws NoSuchFieldException {
      Class<?> c = o.getClass();
      while (c != null) {
         try {
            Field f = c.getDeclaredField(name);
            f.setAccessible(true);
            return f;
         } catch (NoSuchFieldException e) {
            c = c.getSuperclass();
         }
      }
      throw new NoSuchFieldException(name);
   }

   public static boolean hasPixie(BlockEntity be) {
      try {
         return field(be, "hasPixie").getBoolean(be);
      } catch (ReflectiveOperationException e) {
         return true;
      }
   }

   /** EntityPixie.onLivingUpdate: the pixie moves into the house. */
   @SuppressWarnings("unchecked")
   public static void enter(BlockEntity be, int type, ItemStack item, boolean tamed, @Nullable UUID owner) {
      try {
         field(be, "hasPixie").setBoolean(be, true);
         field(be, "pixieType").setInt(be, type);
         ((NonNullList<ItemStack>)field(be, "pixieItems").get(be)).set(0, item);
         field(be, "tamedPixie").setBoolean(be, tamed);
         field(be, "pixieOwnerUUID").set(be, owner);
         be.setChanged();
         if (be.getLevel() != null) {
            be.getLevel().sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 3);
         }
      } catch (ReflectiveOperationException ignored) {
      }
   }
}
