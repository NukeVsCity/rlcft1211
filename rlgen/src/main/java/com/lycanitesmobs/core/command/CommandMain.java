package com.lycanitesmobs.core.command;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
import com.lycanitesmobs.LycEntities;
import com.lycanitesmobs.compat.*;
import net.minecraft.server.MinecraftServer;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.*;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.*;
import net.minecraft.network.chat.*;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.*;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.ambient.*;
import net.minecraft.world.entity.boss.enderdragon.*;
import net.minecraft.world.entity.boss.wither.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.item.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.*;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.*;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.common.*;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.*;
import net.neoforged.neoforge.event.tick.*;
import com.lycanitesmobs.ExtendedWorld;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.config.ConfigBase;
import com.lycanitesmobs.core.dungeon.DungeonManager;
import com.lycanitesmobs.core.dungeon.instance.DungeonInstance;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.info.Beastiary;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureKnowledge;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.item.equipment.EquipmentPartManager;
import com.lycanitesmobs.core.mobevent.MobEvent;
import com.lycanitesmobs.core.mobevent.MobEventListener;
import com.lycanitesmobs.core.mobevent.MobEventManager;
import com.lycanitesmobs.core.mobevent.MobEventPlayerServer;
import com.lycanitesmobs.core.network.MessageSummonSetSelection;
import com.lycanitesmobs.core.spawner.SpawnerEventListener;
import com.lycanitesmobs.core.spawner.SpawnerManager;
import com.lycanitesmobs.core.worldgen.WorldGeneratorDungeon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.math.NumberUtils;


/**
 * 1.12 CommandMain (/lycanitesmobs, aliases lm, lycan, lycmobs, lycanmobs): the 1.12 String[] handling is kept and called
 * from a brigadier command with one greedy argument.
 */
public class CommandMain {
   public static void register(com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack> dispatcher) {
      CommandMain command = new CommandMain();
      for (Object alias : java.util.List.of("lycanitesmobs", "lm", "lycan", "lycmobs", "lycanmobs")) {
         dispatcher.register(net.minecraft.commands.Commands.literal((String)alias)
            .requires(src -> !src.isPlayer() || src.hasPermission(command.getRequiredPermissionLevel()))
            .executes(ctx -> {
               command.execute(ctx.getSource().getServer(), new ICommandSender(ctx.getSource()), new String[0]);
               return 1;
            })
            .then(net.minecraft.commands.Commands.argument("args", com.mojang.brigadier.arguments.StringArgumentType.greedyString())
               .suggests((ctx, builder) -> {
                  String[] typed = builder.getRemaining().split(" ", -1);
                  for (String option : command.getTabCompletions(ctx.getSource().getServer(), new ICommandSender(ctx.getSource()), typed, null)) {
                     builder.suggest(option);
                  }
                  return builder.buildFuture();
               })
               .executes(ctx -> {
                  command.execute(ctx.getSource().getServer(), new ICommandSender(ctx.getSource()),
                     com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "args").trim().split(" +"));
                  return 1;
               })));
      }
   }

   /** 1.12 CommandBase.getListOfStringsMatchingLastWord */
   public static List<String> getListOfStringsMatchingLastWord(String[] args, String... options) {
      return getListOfStringsMatchingLastWord(args, java.util.Arrays.asList(options));
   }

   public static List<String> getListOfStringsMatchingLastWord(String[] args, java.util.Collection<?> options) {
      String last = args.length == 0 ? "" : args[args.length - 1];
      List<String> list = new ArrayList<>();
      for (Object o : options) {
         String option = String.valueOf(o);
         if (option.regionMatches(true, 0, last, 0, last.length())) {
            list.add(option);
         }
      }
      return list;
   }

   private List aliases = new ArrayList();

   public CommandMain() {
      this.aliases.add("lm");
      this.aliases.add("lycan");
      this.aliases.add("lycmobs");
      this.aliases.add("lycanmobs");
      this.aliases.add("lycanitesmobs");
   }

   public String getName() {
      return "lycanitesmobs";
   }

   public String getUsage(ICommandSender commandSender) {
      return commandSender.getPlayer() != null
         ? "/lycanitesmobs <sub-commands: mobevent [start <event name>, stop, list, enable, disable]>"
         : "/lycanitesmobs <sub-commands: mobevent [start <event name> dimensionID, stop, list, enable, disable]>";
   }

   public List getAliases() {
      return this.aliases;
   }

   public List<String> getTabCompletions(MinecraftServer server, ICommandSender commandSender, String[] args, BlockPos pos) {
      if (args.length == 1) {
         return getListOfStringsMatchingLastWord(args, new String[]{"debug", "spawner", "dungeon", "player", "creature", "equipment", "beastiary", "mobevent"});
      } else {
         if (args.length > 1) {
            if ("debug".equalsIgnoreCase(args[0])) {
               if (args.length == 2) {
                  return getListOfStringsMatchingLastWord(args, LycanitesMobs.config.config.getCategory("debug").keySet());
               }

               if (args.length == 3) {
                  return getListOfStringsMatchingLastWord(args, new String[]{Boolean.toString(false), Boolean.toString(true)});
               }
            } else if ("spawner".equalsIgnoreCase(args[0])) {
               if (args.length == 2) {
                  return getListOfStringsMatchingLastWord(args, new String[]{"reload", "creative", "test"});
               }

               if (args.length > 2 && "test".equalsIgnoreCase(args[1])) {
                  if (args.length == 3) {
                     return getListOfStringsMatchingLastWord(args, SpawnerManager.getInstance().spawners.keySet());
                  }

                  if (args.length == 4) {
                     return getListOfStringsMatchingLastWord(args, IntStream.rangeClosed(1, 9).mapToObj(Integer::toString).collect(Collectors.toList()));
                  }
               }
            } else if ("dungeon".equalsIgnoreCase(args[0])) {
               if (args.length == 2) {
                  return getListOfStringsMatchingLastWord(args, new String[]{"reload", "enable", "disable", "locate"});
               }
            } else if ("player".equalsIgnoreCase(args[0])) {
               if (args.length == 2) {
                  return getListOfStringsMatchingLastWord(args, new String[]{"spirit", "focus"});
               }
            } else if ("creature".equalsIgnoreCase(args[0])) {
               if (args.length == 2) {
                  return getListOfStringsMatchingLastWord(args, new String[]{"reload"});
               }
            } else if ("equipment".equalsIgnoreCase(args[0])) {
               if (args.length == 2) {
                  return getListOfStringsMatchingLastWord(args, new String[]{"reload"});
               }
            } else if ("beastiary".equalsIgnoreCase(args[0])) {
               if (args.length == 2) {
                  return getListOfStringsMatchingLastWord(args, new String[]{"add", "complete", "clear", "packet"});
               }

               if (args.length > 2) {
                  if ("add".equalsIgnoreCase(args[1])) {
                     if (args.length == 3) {
                        return getListOfStringsMatchingLastWord(args, CreatureManager.getInstance().creatures.keySet());
                     }

                     if (args.length == 4) {
                        return getListOfStringsMatchingLastWord(args, IntStream.rangeClosed(1, 3).mapToObj(Integer::toString).collect(Collectors.toList()));
                     }
                  } else if ("complete".equalsIgnoreCase(args[1]) && args.length == 3) {
                     return getListOfStringsMatchingLastWord(args, IntStream.rangeClosed(1, 3).mapToObj(Integer::toString).collect(Collectors.toList()));
                  }
               }
            } else if ("mobevent".equalsIgnoreCase(args[0])) {
               if (args.length == 2) {
                  return getListOfStringsMatchingLastWord(args, new String[]{"reload", "creative", "start", "random", "stop", "list", "enable", "disable"});
               }

               if (args.length > 2) {
                  if ("start".equalsIgnoreCase(args[1])) {
                     if (args.length == 3) {
                        return getListOfStringsMatchingLastWord(args, MobEventManager.getInstance().mobEvents.keySet());
                     }

                     if (args.length == 4) {
                        return getListOfStringsMatchingLastWord(
                           args, java.util.List.of("0", "-1", "1")
                        );
                     }

                     if (args.length == 5) {
                        return getListOfStringsMatchingLastWord(args, IntStream.rangeClosed(1, 5).mapToObj(Integer::toString).collect(Collectors.toList()));
                     }

                     if (args.length == 6) {
                        return getListOfStringsMatchingLastWord(args, IntStream.of(-1).mapToObj(Integer::toString).collect(Collectors.toList()));
                     }

                     if (args.length == 7) {
                        return getListOfStringsMatchingLastWord(args, new String[]{Boolean.toString(false), Boolean.toString(true)});
                     }
                  } else if ("random".equalsIgnoreCase(args[1])) {
                     if (args.length == 3) {
                        return getListOfStringsMatchingLastWord(
                           args, java.util.List.of("0", "-1", "1")
                        );
                     }
                  } else if ("stop".equalsIgnoreCase(args[1])) {
                     if (args.length == 3) {
                        return getListOfStringsMatchingLastWord(
                           args, java.util.List.of("0", "-1", "1")
                        );
                     }
                  } else if ("enable".equalsIgnoreCase(args[1])) {
                     if (args.length == 3) {
                        return getListOfStringsMatchingLastWord(args, new String[]{"all", "random"});
                     }
                  } else if ("disable".equalsIgnoreCase(args[1]) && args.length == 3) {
                     return getListOfStringsMatchingLastWord(args, new String[]{"all", "random"});
                  }
               }
            }
         }

         return Collections.emptyList();
      }
   }

   public boolean isUsernameIndex(String[] args, int index) {
      return false;
   }


   public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) {
      String reply = LanguageManager.translate("lyc.command.invalid");
      if (args.length < 1) {
         commandSender.sendSystemMessage(Component.literal(reply));
         commandSender.sendSystemMessage(Component.literal(this.getUsage(commandSender)));
      } else if ("debug".equalsIgnoreCase(args[0])) {
         reply = LanguageManager.translate("lyc.command.debug.invalid");
         if (args.length < 3) {
            commandSender.sendSystemMessage(Component.literal(reply));
         } else {
            String debugValue = args[1];
            reply = LanguageManager.translate("lyc.command.debug.set");
            reply = reply.replace("%debug%", debugValue);
            LycanitesMobs.config.setBool("Debug", debugValue, "true".equalsIgnoreCase(args[2]));
            commandSender.sendSystemMessage(Component.literal(reply));
         }
      } else {
         if ("spawners".equalsIgnoreCase(args[0]) || "spawner".equalsIgnoreCase(args[0])) {
            reply = LanguageManager.translate("lyc.command.spawners.invalid");
            if (args.length < 2) {
               commandSender.sendSystemMessage(Component.literal(reply));
               return;
            }

            if ("reload".equalsIgnoreCase(args[1])) {
               reply = LanguageManager.translate("lyc.command.spawners.reload");
               SpawnerManager.getInstance().reload();
               commandSender.sendSystemMessage(Component.literal(reply));
               return;
            }

            if ("creative".equalsIgnoreCase(args[1])) {
               reply = LanguageManager.translate("lyc.command.spawners.creative");
               SpawnerEventListener.testOnCreative = !SpawnerEventListener.testOnCreative;
               reply = reply.replace("%value%", "" + SpawnerEventListener.testOnCreative);
               commandSender.sendSystemMessage(Component.literal(reply));
               return;
            }

            if ("test".equalsIgnoreCase(args[1])) {
               reply = LanguageManager.translate("lyc.command.spawners.test");
               if (!(commandSender.getPlayer() != null)) {
                  return;
               }

               if (args.length < 3 || !SpawnerManager.getInstance().spawners.containsKey(args[2])) {
                  reply = LanguageManager.translate("lyc.command.spawner.test.unknown");
               }

               String spawnerName = args[2];
               Level world = commandSender.level();
               Player player = commandSender.getPlayer();
               BlockPos pos = player.blockPosition();
               int level = 1;
               if (args.length > 3) {
                  level = Math.max(1, NumberUtils.isCreatable(args[3]) ? Integer.parseInt(args[3]) : 1);
               }

               SpawnerManager.getInstance().spawners.get(spawnerName).trigger(world, player, null, pos, level, 1, 0);
               commandSender.sendSystemMessage(Component.literal(reply));
               return;
            }
         }

         if ("dungeon".equalsIgnoreCase(args[0]) || "dungeons".equalsIgnoreCase(args[0])) {
            reply = LanguageManager.translate("lyc.command.dungeon.invalid");
            if (args.length < 2) {
               commandSender.sendSystemMessage(Component.literal(reply));
               return;
            }

            if ("reload".equalsIgnoreCase(args[1])) {
               reply = LanguageManager.translate("lyc.command.dungeon.reload");
               DungeonManager.getInstance().reload();
               commandSender.sendSystemMessage(Component.literal(reply));
               return;
            }

            if ("enable".equalsIgnoreCase(args[1])) {
               reply = LanguageManager.translate("lyc.command.dungeon.enable");
               ConfigBase config = ConfigBase.getConfig(LycanitesMobs.modInfo, "general");
               config.setBool("Dungeons", "Dungeons Enabled", true);
               LycanitesMobs.dungeonGenerator.enabled = true;
               commandSender.sendSystemMessage(Component.literal(reply));
               return;
            }

            if ("disable".equalsIgnoreCase(args[1])) {
               reply = LanguageManager.translate("lyc.command.dungeon.disable");
               ConfigBase config = ConfigBase.getConfig(LycanitesMobs.modInfo, "general");
               config.setBool("Dungeons", "Dungeons Enabled", false);
               LycanitesMobs.dungeonGenerator.enabled = false;
               commandSender.sendSystemMessage(Component.literal(reply));
               return;
            }

            if ("locate".equalsIgnoreCase(args[1])) {
               reply = LanguageManager.translate("lyc.command.dungeon.locate");
               commandSender.sendSystemMessage(Component.literal(reply));
               Level world = commandSender.level();
               ExtendedWorld extendedWorld = ExtendedWorld.getForWorld(world);
               List<DungeonInstance> nearbyDungeons = extendedWorld.getNearbyDungeonInstances(
                  new ChunkPos(commandSender.blockPosition()), WorldGeneratorDungeon.DUNGEON_DISTANCE * 2
               );
               if (nearbyDungeons.isEmpty()) {
                  commandSender.sendSystemMessage(Component.literal(LanguageManager.translate("common.none")));
                  return;
               }

               for (DungeonInstance dungeonInstance : nearbyDungeons) {
                  commandSender.sendSystemMessage(Component.literal(dungeonInstance.toString()));
               }

               return;
            }
         }

         if ("player".equalsIgnoreCase(args[0])) {
            reply = "Invalid command arguments, valid arguments are: spirit, focus";
            if (args.length < 2) {
               commandSender.sendSystemMessage(Component.literal(reply));
               return;
            }

            Player player = commandSender.getPlayer();
            ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(player);
            if ("spirit".equalsIgnoreCase(args[1])) {
               reply = "Restored Player Spirit.";
               extendedPlayer.spirit = extendedPlayer.spiritMax;
               commandSender.sendSystemMessage(Component.literal(reply));
               return;
            }

            if ("focus".equalsIgnoreCase(args[1])) {
               reply = "Restored Player Focus.";
               extendedPlayer.summonFocus = extendedPlayer.summonFocusMax;
               CreatureManager.getInstance().reload();
               commandSender.sendSystemMessage(Component.literal(reply));
               return;
            }
         }

         if ("creature".equalsIgnoreCase(args[0]) || "creatures".equalsIgnoreCase(args[0])) {
            reply = LanguageManager.translate("lyc.command.creatures.invalid");
            if (args.length < 2) {
               commandSender.sendSystemMessage(Component.literal(reply));
               return;
            }

            if ("reload".equalsIgnoreCase(args[1])) {
               reply = LanguageManager.translate("lyc.command.creatures.reload");
               CreatureManager.getInstance().reload();
               commandSender.sendSystemMessage(Component.literal(reply));
               return;
            }
         }

         if ("equipment".equalsIgnoreCase(args[0])) {
            reply = LanguageManager.translate("lyc.command.equipment.invalid");
            if (args.length < 2) {
               commandSender.sendSystemMessage(Component.literal(reply));
               return;
            }

            if ("reload".equalsIgnoreCase(args[1])) {
               reply = LanguageManager.translate("lyc.command.equipment.reload");
               EquipmentPartManager.getInstance().reload();
               commandSender.sendSystemMessage(Component.literal(reply));
               return;
            }
         }

         if ("beastiary".equalsIgnoreCase(args[0])) {
            reply = LanguageManager.translate("lyc.command.beastiary.invalid");
            if (args.length < 2) {
               commandSender.sendSystemMessage(Component.literal(reply));
               return;
            }

            if (!(commandSender.getPlayer() != null)) {
               reply = LanguageManager.translate("lyc.command.playeronly");
               commandSender.sendSystemMessage(Component.literal(reply));
               return;
            }

            Player playerx = commandSender.getPlayer();
            ExtendedPlayer extendedPlayerx = ExtendedPlayer.getForPlayer(playerx);
            Beastiary beastiary = extendedPlayerx.getBeastiary();
            if (extendedPlayerx == null || beastiary == null) {
               return;
            }

            if ("add".equalsIgnoreCase(args[1])) {
               reply = LanguageManager.translate("lyc.command.beastiary.add.invalid");
               if (args.length < 3) {
                  commandSender.sendSystemMessage(Component.literal(reply));
                  return;
               }

               int rank = 3;
               if (args.length >= 4) {
                  rank = NumberUtils.isCreatable(args[3]) ? Integer.parseInt(args[3]) : 3;
               }

               String creatureName = args[2].toLowerCase();
               CreatureInfo creatureInfo = CreatureManager.getInstance().getCreature(creatureName);
               if (creatureInfo == null) {
                  reply = LanguageManager.translate("lyc.command.beastiary.add.unknown");
                  commandSender.sendSystemMessage(Component.literal(reply));
                  return;
               }

               CreatureKnowledge creatureKnowledge = new CreatureKnowledge(beastiary, creatureInfo.getName(), rank, 0);
               if (beastiary.addCreatureKnowledge(creatureKnowledge, true)) {
                  beastiary.sendAddedMessage(creatureKnowledge);
                  beastiary.sendToClient(creatureKnowledge);
               } else {
                  beastiary.sendKnownMessage(creatureKnowledge);
               }

               return;
            }

            if ("complete".equalsIgnoreCase(args[1])) {
               int rankx = 3;
               if (args.length >= 3) {
                  rankx = NumberUtils.isCreatable(args[2]) ? Integer.parseInt(args[2]) : 3;
               }

               for (CreatureInfo creatureInfox : CreatureManager.getInstance().creatures.values()) {
                  beastiary.addCreatureKnowledge(new CreatureKnowledge(beastiary, creatureInfox.getName(), rankx, 0), true);
               }

               beastiary.sendAllToClient();
               reply = LanguageManager.translate("lyc.command.beastiary.complete");
               commandSender.sendSystemMessage(Component.literal(reply));
               return;
            }

            if ("clear".equalsIgnoreCase(args[1])) {
               beastiary.creatureKnowledgeList.clear();
               beastiary.sendAllToClient();
               reply = LanguageManager.translate("lyc.command.beastiary.clear");
               commandSender.sendSystemMessage(Component.literal(reply));
               return;
            }

            if ("packet".equalsIgnoreCase(args[1])) {
               beastiary.sendAllToClient();
               extendedPlayerx.sendAllSummonSetsToPlayer();
               MessageSummonSetSelection message = new MessageSummonSetSelection(extendedPlayerx);
               LycanitesMobs.packetHandler.sendToPlayer(message, (ServerPlayer)playerx);
               extendedPlayerx.sendPetEntriesToPlayer("");
               commandSender.sendSystemMessage(Component.literal("Force sent a full Beastiary update packet."));
               return;
            }
         }

         if ("mobevent".equalsIgnoreCase(args[0]) || "mobevents".equalsIgnoreCase(args[0])) {
            reply = LanguageManager.translate("lyc.command.mobevent.invalid");
            if (args.length < 2) {
               commandSender.sendSystemMessage(Component.literal(reply));
               return;
            }

            if ("reload".equalsIgnoreCase(args[1])) {
               reply = LanguageManager.translate("lyc.command.mobevent.reload");
               MobEventManager.getInstance().reload();
               commandSender.sendSystemMessage(Component.literal(reply));
               return;
            }

            if ("creative".equalsIgnoreCase(args[1])) {
               reply = LanguageManager.translate("lyc.command.mobevent.creative");
               MobEventPlayerServer.testOnCreative = !MobEventPlayerServer.testOnCreative;
               reply = reply.replace("%value%", "" + MobEventPlayerServer.testOnCreative);
               commandSender.sendSystemMessage(Component.literal(reply));
               return;
            }

            if ("start".equalsIgnoreCase(args[1])) {
               reply = LanguageManager.translate("lyc.command.mobevent.start.invalid");
               if (args.length < 3) {
                  commandSender.sendSystemMessage(Component.literal(reply));
                  return;
               }

               String mobEventName = args[2].toLowerCase();
               if (!MobEventManager.getInstance().mobEvents.containsKey(mobEventName)) {
                  reply = LanguageManager.translate("lyc.command.mobevent.start.unknown");
                  commandSender.sendSystemMessage(Component.literal(reply));
                  return;
               }

               Level world = null;
               if (args.length >= 4 && NumberUtils.isNumber(args[3])) {
                  world = LycServer.level(Integer.parseInt(args[3]));
               } else {
                  world = commandSender.level();
               }

               if (world == null) {
                  reply = LanguageManager.translate("lyc.command.mobevent.start.noworld");
                  commandSender.sendSystemMessage(Component.literal(reply));
                  return;
               }

               ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);
               if (!MobEventManager.getInstance().mobEventsEnabled) {
                  reply = LanguageManager.translate("lyc.command.mobevent.enable");
                  commandSender.sendSystemMessage(Component.literal(reply));
                  MobEventManager.getInstance().mobEventsEnabled = true;
                  ConfigBase config = ConfigBase.getConfig(LycanitesMobs.modInfo, "mobevents");
                  config.setBool("Global", "Mob Events Enabled", true);
               }

               Player playerxx = null;
               BlockPos pos = LycPos.of(0, 0, 0);
               if (commandSender.getPlayer() != null) {
                  playerxx = commandSender.getPlayer();
                  pos = playerxx.blockPosition();
               }

               boolean forced = false;
               if (args.length >= 7) {
                  forced = Boolean.parseBoolean(args[6]);
               }

               MobEvent mobEvent = MobEventManager.getInstance().getMobEvent(mobEventName);
               if (!forced && !mobEvent.canStart(world, playerxx)) {
                  reply = LanguageManager.translate("lyc.command.mobevent.start.conditions");
                  commandSender.sendSystemMessage(Component.literal(reply));
                  return;
               }

               int level = 1;
               if (args.length >= 5 && NumberUtils.isNumber(args[4])) {
                  level = Integer.parseInt(args[4]);
               }

               int subspecies = -1;
               if (args.length >= 6 && NumberUtils.isNumber(args[5])) {
                  subspecies = Integer.parseInt(args[5]);
               }

               reply = LanguageManager.translate("lyc.command.mobevent.start");
               commandSender.sendSystemMessage(Component.literal(reply));
               worldExt.startMobEvent(mobEventName, playerxx, pos, level, subspecies);
               return;
            }

            Level worldx;
            if (args.length >= 3 && NumberUtils.isNumber(args[2])) {
               worldx = LycServer.level(Integer.parseInt(args[2]));
            } else {
               worldx = commandSender.level();
            }

            if (worldx == null) {
               reply = LanguageManager.translate("lyc.command.mobevent.start.noworld");
               commandSender.sendSystemMessage(Component.literal(reply));
               return;
            }

            LycanitesMobs.logDebug("", "Getting Extended Level for Dimension: " + LycDim.id(worldx) + " Level: " + worldx);
            ExtendedWorld worldExtx = ExtendedWorld.getForWorld(worldx);
            LycanitesMobs.logDebug("", "Got Extended Level for Dimension: " + LycDim.id(worldExtx.world) + " Level: " + worldExtx.world);
            if (worldExtx == null) {
               return;
            }

            if ("random".equalsIgnoreCase(args[1])) {
               reply = LanguageManager.translate("lyc.command.mobevent.random");
               commandSender.sendSystemMessage(Component.literal(reply));
               worldExtx.stopWorldEvent();
               MobEventListener.getInstance().triggerRandomMobEvent(worldx, worldExtx);
               return;
            }

            if ("stop".equalsIgnoreCase(args[1])) {
               reply = LanguageManager.translate("lyc.command.mobevent.stop");
               commandSender.sendSystemMessage(Component.literal(reply));
               worldExtx.stopWorldEvent();
               return;
            }

            if ("list".equalsIgnoreCase(args[1])) {
               reply = LanguageManager.translate("lyc.command.mobevent.list");
               commandSender.sendSystemMessage(Component.literal(reply));

               for (MobEvent mobEventx : MobEventManager.getInstance().mobEvents.values()) {
                  String eventName = mobEventx.name + " (" + mobEventx.getTitle() + ")";
                  commandSender.sendSystemMessage(Component.literal(eventName));
               }

               return;
            }

            if ("enable".equalsIgnoreCase(args[1])) {
               if (args.length >= 3 && "random".equalsIgnoreCase(args[2])) {
                  reply = LanguageManager.translate("lyc.command.mobevent.enable.random");
                  commandSender.sendSystemMessage(Component.literal(reply));
                  MobEventManager.getInstance().mobEventsRandom = true;
                  ConfigBase config = ConfigBase.getConfig(LycanitesMobs.modInfo, "mobevents");
                  config.setBool("Global", "Random Mob Events", true);
                  return;
               }

               reply = LanguageManager.translate("lyc.command.mobevent.enable");
               commandSender.sendSystemMessage(Component.literal(reply));
               MobEventManager.getInstance().mobEventsEnabled = true;
               ConfigBase config = ConfigBase.getConfig(LycanitesMobs.modInfo, "mobevents");
               config.setBool("Global", "Mob Events Enabled", true);
               return;
            }

            if ("disable".equalsIgnoreCase(args[1])) {
               if (args.length >= 3 && "random".equalsIgnoreCase(args[2])) {
                  reply = LanguageManager.translate("lyc.command.mobevent.disable.random");
                  commandSender.sendSystemMessage(Component.literal(reply));
                  MobEventManager.getInstance().mobEventsRandom = false;
                  ConfigBase config = ConfigBase.getConfig(LycanitesMobs.modInfo, "mobevents");
                  config.setBool("Global", "Random Mob Events", false);
                  return;
               }

               reply = LanguageManager.translate("lyc.command.mobevent.disable");
               commandSender.sendSystemMessage(Component.literal(reply));
               MobEventManager.getInstance().mobEventsEnabled = false;
               ConfigBase config = ConfigBase.getConfig(LycanitesMobs.modInfo, "mobevents");
               config.setBool("Global", "Mob Events Enabled", false);
               return;
            }
         }

         commandSender.sendSystemMessage(Component.literal(reply));
         commandSender.sendSystemMessage(Component.literal(this.getUsage(commandSender)));
      }
   }

   public int getRequiredPermissionLevel() {
      return 4;
   }

   public boolean checkPermission(MinecraftServer server, ICommandSender commandSender) {
      return !(commandSender.getPlayer() != null) || commandSender.canUseCommand(this.getRequiredPermissionLevel(), this.getName());
   }
}
