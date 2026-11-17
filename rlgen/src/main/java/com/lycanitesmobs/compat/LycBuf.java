package com.lycanitesmobs.compat;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.RegistryAccess;

/** 1.12 new PacketBuffer(buf): payload buffers already are RegistryFriendlyByteBufs. */
public final class LycBuf {
   private LycBuf() {
   }

   public static RegistryFriendlyByteBuf of(ByteBuf buf) {
      if (buf instanceof RegistryFriendlyByteBuf r) {
         return r;
      }
      return new RegistryFriendlyByteBuf(buf, RegistryAccess.EMPTY);
   }
}
