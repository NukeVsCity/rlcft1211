package com.github.alexthe666.iceandfire.enums;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.ChatFormatting;

/** 1.12 EnumDragonEgg. */
public enum EnumDragonEgg {
   RED(0, ChatFormatting.DARK_RED, true),
   GREEN(1, ChatFormatting.DARK_GREEN, true),
   BRONZE(2, ChatFormatting.GOLD, true),
   GRAY(3, ChatFormatting.GRAY, true),
   BLUE(4, ChatFormatting.AQUA, false),
   WHITE(5, ChatFormatting.WHITE, false),
   SAPPHIRE(6, ChatFormatting.BLUE, false),
   SILVER(7, ChatFormatting.DARK_GRAY, false);

   private static final Map<Integer, EnumDragonEgg> META_LOOKUP = Maps.newHashMap();
   public int meta;
   public ChatFormatting color;
   public boolean isFire;

   private EnumDragonEgg(int meta, ChatFormatting color, boolean isFire) {
      this.meta = meta;
      this.color = color;
      this.isFire = isFire;
   }

   public static EnumDragonEgg byMetadata(int meta) {
      EnumDragonEgg i = META_LOOKUP.get(meta);
      return i == null ? RED : i;
   }

   static {
      for (EnumDragonEgg e : values()) {
         META_LOOKUP.put(e.meta, e);
      }
   }
}
