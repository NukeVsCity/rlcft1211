package ichttt.mods.firstaid.api.enums;

/** 1.12 EnumDebuffSlot */
public enum EnumDebuffSlot {
   HEAD(EnumPlayerPart.HEAD),
   BODY(EnumPlayerPart.BODY),
   ARMS(EnumPlayerPart.LEFT_ARM, EnumPlayerPart.RIGHT_ARM),
   LEGS_AND_FEET(EnumPlayerPart.LEFT_LEG, EnumPlayerPart.RIGHT_LEG, EnumPlayerPart.LEFT_FOOT, EnumPlayerPart.RIGHT_FOOT);

   public final EnumPlayerPart[] playerParts;

   EnumDebuffSlot(EnumPlayerPart... playerParts) {
      this.playerParts = playerParts;
   }
}
