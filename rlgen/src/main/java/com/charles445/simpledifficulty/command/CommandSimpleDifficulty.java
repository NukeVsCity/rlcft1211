package com.charles445.simpledifficulty.command;

import com.charles445.simpledifficulty.SimpleDifficulty;
import com.charles445.simpledifficulty.api.SDCapabilities;
import com.charles445.simpledifficulty.config.JsonConfigInternal;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * 1.12 CommandSimpleDifficulty: help, reloadJson, nbt, setThirst, setTemperature (admin = permission 4 and a player). The in-game
 * json editing commands (addArmor, addBlock, ..., exportJson) are not ported: edit config/simpledifficulty/*.json and reloadJson.
 */
public class CommandSimpleDifficulty {
   private static final String HELP = "   help <command>\n   reloadJson\n   nbt\n   setThirst <thirst> <saturation>\n   setTemperature <temperature>";

   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      dispatcher.register(Commands.literal("simpledifficulty")
         .executes(c -> msg(c.getSource(), "/simpledifficulty help"))
         .then(Commands.literal("help").executes(c -> msg(c.getSource(), HELP))
            .then(Commands.argument("command", StringArgumentType.word()).executes(c -> msg(c.getSource(), help(StringArgumentType.getString(c, "command"))))))
         .then(Commands.literal("reloadjson").requires(s -> s.hasPermission(4)).executes(c -> {
            JsonConfigInternal.postInit();
            return msg(c.getSource(), "Reloaded JSON");
         }))
         .then(Commands.literal("nbt").requires(s -> s.hasPermission(4)).executes(c -> {
            ServerPlayer player = c.getSource().getPlayerOrException();
            ItemStack stack = player.getMainHandItem();
            if (stack.isEmpty()) {
               return msg(c.getSource(), "Not holding an item!");
            }
            CustomData data = stack.get(DataComponents.CUSTOM_DATA);
            return msg(c.getSource(), data == null ? "Item has no NBT! " + SimpleDifficulty.getMetadata(stack) : data.copyTag().toString());
         }))
         .then(Commands.literal("setthirst").requires(s -> s.hasPermission(4))
            .then(Commands.argument("thirst", IntegerArgumentType.integer()).executes(c -> {
               SDCapabilities.getThirstData(c.getSource().getPlayerOrException()).setThirstLevel(IntegerArgumentType.getInteger(c, "thirst"));
               return 1;
            }).then(Commands.argument("saturation", FloatArgumentType.floatArg()).executes(c -> {
               var cap = SDCapabilities.getThirstData(c.getSource().getPlayerOrException());
               cap.setThirstLevel(IntegerArgumentType.getInteger(c, "thirst"));
               cap.setThirstSaturation(FloatArgumentType.getFloat(c, "saturation"));
               return 1;
            }))))
         .then(Commands.literal("settemperature").requires(s -> s.hasPermission(4))
            .then(Commands.argument("temperature", IntegerArgumentType.integer()).executes(c -> {
               SDCapabilities.getTemperatureData(c.getSource().getPlayerOrException()).setTemperatureLevel(IntegerArgumentType.getInteger(c, "temperature"));
               return 1;
            }))));
   }

   private static String help(String cmd) {
      return switch (cmd.toLowerCase()) {
         case "help" -> "If you need more help, you can contact the mod author on CurseForge or GitHub";
         case "reloadjson" -> "Discards any unexported in-game JSON changes.\nReloads the JSON from the config folder";
         case "nbt" -> "Displays the NBT of the held item";
         case "setthirst" -> "Sets the thirst (and saturation) of the player";
         case "settemperature" -> "Sets the temperature of the player";
         default -> HELP;
      };
   }

   private static int msg(CommandSourceStack source, String s) {
      source.sendSystemMessage(Component.literal(s));
      return 1;
   }
}
