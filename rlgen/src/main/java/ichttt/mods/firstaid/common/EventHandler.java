package ichttt.mods.firstaid.common;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.apiimpl.FirstAidRegistryImpl;
import ichttt.mods.firstaid.common.damagesystem.DamageablePart;
import ichttt.mods.firstaid.common.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.common.damagesystem.distribution.DamageDistribution;
import ichttt.mods.firstaid.api.IDamageDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.EqualDamageDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.HealthDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.RandomDamageDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.StandardDamageDistribution;
import ichttt.mods.firstaid.common.network.FirstAidNetwork;
import ichttt.mods.firstaid.common.util.CommonUtils;
import ichttt.mods.firstaid.common.util.PlayerSizeHelper;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.apache.commons.lang3.tuple.Pair;

/** 1.12 common.EventHandler (the LivingHurtEvent part is {@link #onLivingHurt}, called from the Player.actuallyHurt mixin). */
public class EventHandler {
   public static final Map<Player, Pair<Entity, EntityHitResult>> HIT_LIST = new WeakHashMap<>();

   /**
    * 1.12 onLivingHurt (LOWEST): replaces vanilla armor/absorption/setHealth with the part damage. Returns true when the vanilla
    * damage has to be skipped (1.12 event.setCanceled).
    */
   public static boolean onLivingHurt(Player player, DamageSource source, float amountToDamage) {
      if (player.level().isClientSide || player instanceof FakePlayer) {
         return false;
      }
      PlayerDamageModel damageModel = FirstAid.getDamageModel(player);
      if (amountToDamage != Float.MAX_VALUE && !Float.isNaN(amountToDamage) && amountToDamage != Float.POSITIVE_INFINITY) {
         boolean addStat = amountToDamage < 3.4028235E37F;
         IDamageDistribution damageDistribution = FirstAidRegistryImpl.INSTANCE.getDamageDistributionForSource(source);
         if (source.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)) {
            Pair<Entity, EntityHitResult> rayTraceResult = HIT_LIST.remove(player);
            if (rayTraceResult != null) {
               EquipmentSlot slot = PlayerSizeHelper.getSlotTypeForProjectileHit(rayTraceResult.getLeft(), player);
               if (slot != null) {
                  damageDistribution = new StandardDamageDistribution(Collections.singletonList(Pair.of(slot, CommonUtils.getPartArrayForSlot(slot))), false, true);
               }
            }
         }
         if (damageDistribution == null) {
            damageDistribution = PlayerSizeHelper.getMeleeDistribution(player, source);
            if (damageDistribution == null) {
               damageDistribution = RandomDamageDistribution.getDefault();
            }
         }
         DamageDistribution.handleDamageTaken(damageDistribution, damageModel, amountToDamage, player, source, addStat, true);
         HIT_LIST.remove(player);
      } else {
         for (DamageablePart part : damageModel) {
            part.currentHealth = 0.0F;
         }
         if (player instanceof ServerPlayer sp) {
            FirstAidNetwork.syncDamageModel(sp, damageModel, false);
         }
         CommonUtils.killPlayer(damageModel, player, source);
      }
      return true;
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void onProjectileImpact(ProjectileImpactEvent event) {
      if (event.getRayTraceResult() instanceof EntityHitResult hit && hit.getEntity() instanceof Player player && !player.level().isClientSide) {
         HIT_LIST.put(player, Pair.of(event.getEntity(), hit));
      }
   }

   @SubscribeEvent
   public static void tickPlayers(PlayerTickEvent.Post event) {
      Player player = event.getEntity();
      if (CommonUtils.isSurvivalOrAdventure(player)) {
         FirstAid.getDamageModel(player).tick(player.level(), player);
         HIT_LIST.remove(player);
      }
   }

   /** 1.12 tickWorld (START): sleeping heals when every player is fully asleep */
   @SubscribeEvent
   public static void tickWorld(LevelTickEvent.Pre event) {
      if (FirstAidConfig.externalHealing.sleepHealPercentage > 0.0 && event.getLevel() instanceof ServerLevel world && !world.players().isEmpty()) {
         boolean all = world.players().stream().allMatch(p -> p.isSpectator() || p.isSleepingLongEnough());
         if (all && world.players().stream().anyMatch(p -> !p.isSpectator())) {
            for (Player player : world.players()) {
               FirstAid.getDamageModel(player).sleepHeal(player);
            }
         }
      }
   }

   /** 1.12 onHeal (LOW): every heal of a player is cancelled and spread on the parts (food regen only if natural regeneration). */
   @SubscribeEvent(priority = EventPriority.LOW)
   public static void onHeal(LivingHealEvent event) {
      if (event.getEntity() instanceof Player player && !(player instanceof FakePlayer)) {
         event.setCanceled(true);
         if (!player.level().isClientSide && FirstAidConfig.externalHealing.allowOtherHealingItems) {
            float amount = event.getAmount();
            if (Arrays.stream(Thread.currentThread().getStackTrace()).anyMatch(e -> e.getClassName().equals(net.minecraft.world.food.FoodData.class.getName()))) {
               if (FirstAidConfig.externalHealing.allowNaturalRegeneration) {
                  amount *= (float)FirstAidConfig.externalHealing.naturalRegenMultiplier;
               }
            } else {
               amount *= (float)FirstAidConfig.externalHealing.otherRegenMultiplier;
            }
            HealthDistribution.distributeHealth(amount, player, true);
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGH)
   public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer sp) {
         FirstAidNetwork.configuration(sp, FirstAid.getDamageModel(sp));
      }
   }

   @SubscribeEvent(priority = EventPriority.LOW)
   public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
      HIT_LIST.remove(event.getEntity());
   }

   /** 1.12 onWorldLoad: the naturalRegeneration gamerule follows allowNaturalRegeneration (false in RLCraft) */
   @SubscribeEvent
   public static void onWorldLoad(LevelEvent.Load event) {
      if (event.getLevel() instanceof ServerLevel world) {
         world.getGameRules().getRule(GameRules.RULE_NATURAL_REGENERATION).set(FirstAidConfig.externalHealing.allowNaturalRegeneration, world.getServer());
      }
   }

   @SubscribeEvent
   public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
      if (event.getEntity() instanceof ServerPlayer sp) {
         FirstAidNetwork.syncDamageModel(sp, FirstAid.getDamageModel(sp), true);
      }
   }

   @SubscribeEvent(priority = EventPriority.LOW)
   public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
      if (!event.isEndConquered() && event.getEntity() instanceof ServerPlayer sp) {
         PlayerDamageModel damageModel = FirstAid.getDamageModel(sp);
         damageModel.runScaleLogic(sp);
         for (DamageablePart part : damageModel) {
            part.heal((float)part.getMaxHealth(), sp, false);
         }
         damageModel.scheduleResync();
      }
   }

   @SubscribeEvent
   public static void onServerStop(ServerStoppedEvent event) {
      HIT_LIST.clear();
   }

   @SubscribeEvent
   public static void registerCommands(RegisterCommandsEvent event) {
      registerDebugCommand(event.getDispatcher());
   }

   /** 1.12 DebugDamageCommand: /damagePlayerPart <part|ALL> <damage> [debuff] (negative damage heals) */
   private static void registerDebugCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
      dispatcher.register(Commands.literal("damagePlayerPart").requires(s -> s.hasPermission(2))
         .then(Commands.argument("part", StringArgumentType.word()).then(Commands.argument("damage", FloatArgumentType.floatArg())
            .executes(c -> debugDamage(c.getSource(), StringArgumentType.getString(c, "part"), FloatArgumentType.getFloat(c, "damage"), true))
            .then(Commands.argument("debuff", BoolArgumentType.bool()).executes(c -> debugDamage(c.getSource(), StringArgumentType.getString(c, "part"),
               FloatArgumentType.getFloat(c, "damage"), BoolArgumentType.getBool(c, "debuff")))))));
   }

   private static int debugDamage(CommandSourceStack source, String part, float damage, boolean debuff) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
      ServerPlayer player = source.getPlayerOrException();
      if (part.equalsIgnoreCase("ALL")) {
         for (EnumPlayerPart p : EnumPlayerPart.VALUES) {
            debugDamage(p, damage, debuff, player);
         }
      } else {
         debugDamage(EnumPlayerPart.valueOf(part.toUpperCase(Locale.ENGLISH)), damage, debuff, player);
      }
      FirstAidNetwork.syncDamageModel(player, FirstAid.getDamageModel(player), false);
      return 1;
   }

   private static void debugDamage(EnumPlayerPart part, float damage, boolean debuff, Player player) {
      if (damage != 0.0F) {
         PlayerDamageModel damageModel = FirstAid.getDamageModel(player);
         if (damage > 0.0F) {
            DamageDistribution.handleDamageTaken(new EqualDamageDistribution.DirectDamageDistribution(part, debuff), damageModel, damage, player,
               player.damageSources().fellOutOfWorld(), false, false);
         } else {
            damageModel.getFromEnum(part).heal(-damage, player, debuff);
         }
      }
   }
}
