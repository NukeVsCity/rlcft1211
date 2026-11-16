package com.lycanitesmobs.core.network;

import net.minecraft.world.entity.player.Player;

/** 1.12 MessageContext: the side the message arrived on and the player (sender on the server, the local player on the client). */
public class MessageContext {
   public final boolean client;
   public final Player player;

   public MessageContext(boolean client, Player player) {
      this.client = client;
      this.player = player;
   }
}
