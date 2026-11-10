package com.github.alexthe666.iceandfire.client;

import com.github.alexthe666.iceandfire.client.gui.GuiMyrmexAddRoom;
import com.github.alexthe666.iceandfire.client.gui.GuiMyrmexStaff;
import com.github.alexthe666.iceandfire.entity.MyrmexHive;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

/** The myrmex staff part of 1.12 ClientProxy: referedClientHive and the two staff screens. */
public final class MyrmexStaffClient {
   private static MyrmexHive referedClientHive = null;

   private MyrmexStaffClient() {
   }

   @Nullable
   public static MyrmexHive getReferedClientHive() {
      return referedClientHive;
   }

   public static void setReferedClientHive(@Nullable MyrmexHive hive) {
      referedClientHive = hive;
   }

   public static void openMyrmexStaffGui(ItemStack staff) {
      Minecraft.getInstance().setScreen(new GuiMyrmexStaff(staff));
   }

   public static void openMyrmexAddRoomGui(ItemStack staff, BlockPos pos, Direction facing) {
      Minecraft.getInstance().setScreen(new GuiMyrmexAddRoom(staff, pos, facing));
   }
}
