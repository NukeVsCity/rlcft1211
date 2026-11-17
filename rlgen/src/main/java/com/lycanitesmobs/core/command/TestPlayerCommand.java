package com.lycanitesmobs.core.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/**
 * Dev-server testing only (not part of Lycanites): with -Dlycanitesmobs.testcommands=true, "lyctestplayer x y z" puts a fake
 * player into the level so that natural spawning, Lycanites spawners and mob events have a player to work around.
 */
public class TestPlayerCommand {
   private static final GameProfile PROFILE = new GameProfile(UUID.fromString("6c7963e5-7465-7374-0000-000000000001"), "LycTester");

   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      if (!Boolean.getBoolean("lycanitesmobs.testcommands")) {
         return;
      }
      dispatcher.register(Commands.literal("lyctestplayer").requires(s -> s.hasPermission(2))
         .then(Commands.argument("x", DoubleArgumentType.doubleArg())
            .then(Commands.argument("y", DoubleArgumentType.doubleArg())
               .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                  .executes(ctx -> {
                     ServerLevel level = ctx.getSource().getLevel();
                     FakePlayer player = FakePlayerFactory.get(level, PROFILE);
                     player.moveTo(DoubleArgumentType.getDouble(ctx, "x"), DoubleArgumentType.getDouble(ctx, "y"), DoubleArgumentType.getDouble(ctx, "z"));
                     if (!level.players().contains(player)) {
                        level.addNewPlayer(player);
                        // fake players are not ticked: post their player tick events (Lycanites player spawn triggers)
                        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) -> {
                           if (!player.isRemoved()) {
                              net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre(player));
                              net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));
                           }
                        });
                     }
                     ctx.getSource().sendSuccess(() -> Component.literal("test player at " + player.blockPosition()), false);
                     return 1;
                  })))));
   }
}
