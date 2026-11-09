package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.entity.tile.TileEntityPixieHouse;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/** The 1.7.1 TileEntityPixieHouse fields the pixie reads and writes (EntityPixie.findAHouse / onLivingUpdate). */
public final class PixieHouses {
   private PixieHouses() {
   }

   public static boolean isPixieHouse(@Nullable BlockEntity be) {
      return be instanceof TileEntityPixieHouse;
   }

   public static boolean hasPixie(BlockEntity be) {
      return ((TileEntityPixieHouse)be).hasPixie;
   }

   /** EntityPixie.onLivingUpdate: the pixie moves into the house (MessageUpdatePixieHouse is the block entity sync here). */
   public static void enter(BlockEntity be, int type, ItemStack item, boolean tamed, @Nullable UUID owner) {
      TileEntityPixieHouse house = (TileEntityPixieHouse)be;
      house.hasPixie = true;
      house.pixieType = type;
      house.pixieItems.set(0, item);
      house.tamedPixie = tamed;
      house.pixieOwnerUUID = owner;
      house.setChanged();
      house.sync();
   }
}
