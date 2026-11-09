package com.github.alexthe666.iceandfire.client.model;

import net.minecraft.client.Minecraft;

/** LLibrary.PROXY.getPartialTicks() of 1.12 (the frame's render partial ticks). Client only. */
public final class PartialTicks {
   private PartialTicks() {
   }

   public static float get() {
      return Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
   }
}
