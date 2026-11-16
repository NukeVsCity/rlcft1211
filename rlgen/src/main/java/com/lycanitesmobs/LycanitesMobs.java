package com.lycanitesmobs;

import com.lycanitesmobs.compat.CreativeTabs;
import com.lycanitesmobs.compat.LycDamage;
import com.lycanitesmobs.core.VersionChecker;
import com.lycanitesmobs.core.command.CommandMain;
import com.lycanitesmobs.core.config.ConfigBase;
import com.lycanitesmobs.core.dungeon.DungeonManager;
import com.lycanitesmobs.core.entity.EntityHitArea;
import com.lycanitesmobs.core.entity.ExtendedEntity;
import com.lycanitesmobs.core.info.AltarInfo;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.ElementManager;
import com.lycanitesmobs.core.info.ItemManager;
import com.lycanitesmobs.core.info.ModInfo;
import com.lycanitesmobs.core.info.ObjectLists;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;
import com.lycanitesmobs.core.item.CreativeTabBlocks;
import com.lycanitesmobs.core.item.CreativeTabCharges;
import com.lycanitesmobs.core.item.CreativeTabCreatures;
import com.lycanitesmobs.core.item.CreativeTabEquipmentParts;
import com.lycanitesmobs.core.item.CreativeTabItems;
import com.lycanitesmobs.core.item.consumable.ItemHalloweenTreat;
import com.lycanitesmobs.core.item.consumable.ItemWinterGift;
import com.lycanitesmobs.core.item.equipment.EquipmentPartManager;
import com.lycanitesmobs.core.mobevent.MobEventListener;
import com.lycanitesmobs.core.mobevent.MobEventManager;
import com.lycanitesmobs.core.network.PacketHandler;
import com.lycanitesmobs.core.pets.PlayerFamiliars;
import com.lycanitesmobs.core.spawner.SpawnerEventListener;
import com.lycanitesmobs.core.spawner.SpawnerManager;
import com.lycanitesmobs.core.tileentity.EquipmentInfuserTileEntity;
import com.lycanitesmobs.core.tileentity.EquipmentStationTileEntity;
import com.lycanitesmobs.core.tileentity.TileEntityEquipmentForge;
import com.lycanitesmobs.core.tileentity.TileEntitySummoningPedestal;
import com.lycanitesmobs.core.worldgen.WorldGeneratorDungeon;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Lycanites Mobs 2.0.8.9 (RLCraft 2.9.3) ported to 1.21.1. 1.12 preInit runs once, at the first registry event (1.21 objects
 * can only be created while the registries are open), then each registry event registers what 1.12 registered in its
 * RegistryEvent.Register. Config: <game>/config/lycanitesmobs, written from RLCraft's files on first start (see
 * {@link #installDefaultConfigs}), then read by the 1.12 loaders (cfg + JSON, custom JSON replacing the jar JSON like 1.12).
 */
@Mod(LycanitesMobs.modid)
public class LycanitesMobs {
   public static final String modid = "lycanitesmobs";
   public static final String name = "Lycanites Mobs";
   public static final String versionNumber = "2.0.8.9";
   public static final String versionMC = "1.12.2";
   public static final String version = "2.0.8.9 - MC 1.12.2";
   public static final String website = "https://lycanitesmobs.com";
   public static final String serviceAPI = "https://service.lycanitesmobs.com/api/v1";
   public static final String twitter = "https://twitter.com/Lycanite05";
   public static final String patreon = "https://www.patreon.com/lycanite";
   public static final String guilded = "https://www.guilded.gg/i/jpLvd6J2";
   public static final String discord = "https://discord.gg/bFpV3z4";
   public static final String acceptedMinecraftVersions = "[1.12,1.13)";
   public static final Logger LOGGER = LogManager.getLogger("LycanitesMobs");
   public static final PacketHandler packetHandler = new PacketHandler();
   public static ModInfo modInfo;
   public static ConfigBase config;
   public static LycanitesMobs instance;
   public static CommonProxy proxy = new CommonProxy();
   public static final CreativeTabs itemsTab = new CreativeTabItems("lycanitesmobs.items");
   public static final CreativeTabs blocksTab = new CreativeTabBlocks("lycanitesmobs.blocks");
   public static final CreativeTabs creaturesTab = new CreativeTabCreatures("lycanitesmobs.creatures");
   public static final CreativeTabs chargesTab = new CreativeTabCharges("lycanitesmobs.charges");
   public static final CreativeTabs equipmentPartsTab = new CreativeTabEquipmentParts("lycanitesmobs.equipmentparts");
   public static String texturePath = "mods/lycanitesmobs/";
   public PotionEffects potionEffects;
   public static WorldGeneratorDungeon dungeonGenerator;
   private static boolean preInitDone;

   public LycanitesMobs(IEventBus modBus) {
      instance = this;
      if (FMLEnvironment.dist.isClient()) {
         try {
            proxy = (CommonProxy)Class.forName("com.lycanitesmobs.client.ClientProxy").getDeclaredConstructor().newInstance();
         } catch (ClassNotFoundException e) {
            LOGGER.warn("[LycanitesMobs] No client proxy yet (lyc/client), using the common one.");
         } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
         }
      }
      modInfo = new ModInfo(this, "Lycanites Mobs", 1000);
      installDefaultConfigs();
      LycEntities.init(modBus);
      modBus.addListener(packetHandler::registerPayloads);
      packetHandler.init();
      NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> CommandMain.register(event.getDispatcher()));
      NeoForge.EVENT_BUS.addListener((ServerAboutToStartEvent event) -> LycDamage.setRegistries(event.getServer().registryAccess()));
      NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> LycDamage.setRegistries(null));
   }

   /** 1.12 preInit, run at the first RegisterEvent. */
   public static void preInitOnce() {
      if (preInitDone) {
         return;
      }
      preInitDone = true;
      try {
         instance.preInit();
      } catch (RuntimeException e) {
         LOGGER.error("[LycanitesMobs] preInit failed", e);
         throw e;
      }
   }

   /** 1.12 preInit(FMLPreInitializationEvent) */
   public void preInit() {
      ObjectManager.setCurrentModInfo(modInfo);
      proxy.initLanguageManager();
      ConfigBase.versionCheck("2.0.0.0", "2.0.8.9 - MC 1.12.2");
      config = ConfigBase.getConfig(modInfo, "general");
      config.setCategoryComment("Debug", "Set debug options to true to show extra debugging information in the console.");
      config.setCategoryComment("Extras", "Other extra config settings, some of the aren't necessarily specific to Lycanites Mobs.");
      config.getBool(
         "Extras",
         "Disable Sneak Dismount",
         true,
         "Set to true to prevent players from dismounting from Lycanites Mobs mounts when pressing shift (useful for shift to fly/swim down)."
      );
      config.setCategoryComment("Player", "Settings for player related stats and features.");
      NeoForge.EVENT_BUS.register(new GameEventListener());
      NeoForge.EVENT_BUS.register(CreatureManager.getInstance());
      NeoForge.EVENT_BUS.register(ProjectileManager.getInstance());
      PlayerFamiliars.INSTANCE.familiarBlacklist = new ArrayList<>();
      String[] familiarBlacklist = config.getStringList(
         "Extras",
         "Familiar Username Blacklist",
         new String[0],
         "Donation Familiars help support the development of this mod but can be turned of for individual players be adding their username to this list."
      );
      PlayerFamiliars.INSTANCE.familiarBlacklist.addAll(Arrays.asList(familiarBlacklist));
      VersionChecker.INSTANCE.enabled = config.getBool(
         "Extras", "Version Checker", VersionChecker.INSTANCE.enabled, "Set to false to disable the version checker."
      );
      // 1.12: RangedAttribute MAX_HEALTH maximumValue raised to 100000 by reflection (1.21: see LycAttributeLimits mixin-free hook)
      LycAttributeLimits.raiseMaxHealth(100000.0);
      config.setCategoryComment("Admin", "Special tools for server admins.");
      ExtendedEntity.FORCE_REMOVE_ENTITY_IDS = config.getStringList(
         "Admin", "Force Remove Entity Names", new String[0], "Here you can add a list of entity IDs for entity that you want to be forcefully removed."
      );
      if (ExtendedEntity.FORCE_REMOVE_ENTITY_IDS != null && ExtendedEntity.FORCE_REMOVE_ENTITY_IDS.length > 0) {
         logInfo("", "Lycanites Mobs will forcefully remove the following entities based on their registered IDs:");

         for (String removeEntityID : ExtendedEntity.FORCE_REMOVE_ENTITY_IDS) {
            logInfo("", removeEntityID);
         }
      }

      ExtendedEntity.FORCE_REMOVE_ENTITY_TICKS = config.getInt(
         "Admin",
         "Force Remove Entity Ticks",
         40,
         "How many ticks it takes for an entity to be forcefully removed (1 second = 20 ticks). This only applies to EntityLiving, other entities are instantly removed."
      );
      ElementManager.getInstance().loadConfig();
      ElementManager.getInstance().loadAllFromJSON(modInfo);
      this.potionEffects = new PotionEffects();
      this.potionEffects.init(config);
      ObjectLists.createVanillaLists();
      ObjectLists.createCustomItems();
      ItemManager.getInstance().startup(modInfo);
      EquipmentPartManager.getInstance().loadAllFromJSON(modInfo);
      ObjectManager.addTileEntity("summoningpedestal", TileEntitySummoningPedestal.class);
      ObjectManager.addTileEntity("equipmentforge", TileEntityEquipmentForge.class);
      ObjectManager.addTileEntity("equipment_infuser", EquipmentInfuserTileEntity.class);
      ObjectManager.addTileEntity("equipment_station", EquipmentStationTileEntity.class);
      ObjectManager.addSpecialEntity("hitarea", EntityHitArea.class);
      CreatureManager.getInstance().startup(modInfo);
      ProjectileManager.getInstance().startup(modInfo);
      NeoForge.EVENT_BUS.register(SpawnerEventListener.getInstance());
      SpawnerManager.getInstance().loadAllFromJSON();
      AltarInfo.loadGlobalSettings();
      AltarInfo.createAltars();
      NeoForge.EVENT_BUS.register(MobEventManager.getInstance());
      NeoForge.EVENT_BUS.register(MobEventListener.getInstance());
      MobEventManager.getInstance().loadConfig();
      MobEventManager.getInstance().loadAllFromJSON(modInfo);
      // 1.12 GameRegistry.registerWorldGenerator(WorldGeneratorFluids, 0) and (WorldGeneratorDungeon, 1000): Lycanites world
      // gen runs from rlgen's legacy populate (com.nukevscity.rlgen.lyc).
      DungeonManager.getInstance().loadAllFromJSON();
      dungeonGenerator = new WorldGeneratorDungeon();
      ItemHalloweenTreat.createObjectLists();
      ItemWinterGift.createObjectLists();
      proxy.registerEvents();
      proxy.registerRenders(modInfo);
      proxy.registerTextures();
      // 1.12 init + postInit
      proxy.registerModels(modInfo);
      CreatureManager.getInstance().lateStartup(modInfo);
   }

   /** Writes RLCraft 2.9.3's config/lycanitesmobs files (bundled in lycanitesmobs_defaults) when that folder has no general.cfg. */
   public static void installDefaultConfigs() {
      File configDir = new File(proxy.getMinecraftDir(), "config/lycanitesmobs");
      if (new File(configDir, "general.cfg").exists()) {
         return;
      }
      try (InputStream index = LycanitesMobs.class.getResourceAsStream("/lycanitesmobs_defaults/index.txt")) {
         if (index == null) {
            LOGGER.warn("[LycanitesMobs] No bundled RLCraft config found.");
            return;
         }
         for (String line : new String(index.readAllBytes(), StandardCharsets.UTF_8).split("\n")) {
            line = line.trim();
            if (line.isEmpty()) {
               continue;
            }
            try (InputStream in = LycanitesMobs.class.getResourceAsStream("/lycanitesmobs_defaults/" + line)) {
               if (in == null) {
                  continue;
               }
               File out = new File(configDir, line);
               out.getParentFile().mkdirs();
               Files.write(out.toPath(), in.readAllBytes());
            }
         }
         LOGGER.info("[LycanitesMobs] Installed the RLCraft 2.9.3 Lycanites Mobs config into " + configDir);
      } catch (IOException e) {
         LOGGER.error("[LycanitesMobs] Unable to install the RLCraft config", e);
      }
   }

   public static void logInfo(String key, String message) {
      if ("".equals(key) || config.getBool("Debug", key, false)) {
         LOGGER.info("[LycanitesMobs] [Info] [" + key + "] " + message);
      }
   }

   public static void logDebug(String key, String message) {
      if ("".equals(key) || config.getBool("Debug", key, false)) {
         LOGGER.info("[LycanitesMobs] [Debug] [" + key + "] " + message);
      }
   }

   public static void logWarning(String key, String message) {
      if ("".equals(key) || config.getBool("Debug", key, false)) {
         LOGGER.warn("[LycanitesMobs] [WARNING] [" + key + "] " + message);
      }
   }
}
