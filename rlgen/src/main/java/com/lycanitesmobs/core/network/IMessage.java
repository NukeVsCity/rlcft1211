package com.lycanitesmobs.core.network;

import io.netty.buffer.ByteBuf;

/** 1.12 net.minecraftforge.fml.common.network.simpleimpl.IMessage: messages keep their no-arg constructor and fromBytes/toBytes. */
public interface IMessage {
   void fromBytes(ByteBuf buf);

   void toBytes(ByteBuf buf);
}
