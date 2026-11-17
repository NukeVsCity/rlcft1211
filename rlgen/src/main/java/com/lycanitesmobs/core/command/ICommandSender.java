package com.lycanitesmobs.core.command;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

/** 1.12 ICommandSender for CommandMain: the 1.21 command source. */
public class ICommandSender {
   public final CommandSourceStack source;

   public ICommandSender(CommandSourceStack source) {
      this.source = source;
   }

   public void sendSystemMessage(Component message) {
      this.source.sendSystemMessage(message);
   }

   public ServerLevel level() {
      return this.source.getLevel();
   }

   public BlockPos blockPosition() {
      return BlockPos.containing(this.source.getPosition());
   }

   public Player getPlayer() {
      return this.source.getEntity() instanceof Player player ? player : null;
   }

   public boolean canUseCommand(int level, String name) {
      return this.source.hasPermission(level);
   }
}
