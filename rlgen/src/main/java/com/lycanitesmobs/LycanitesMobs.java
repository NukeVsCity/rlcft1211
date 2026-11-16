package com.lycanitesmobs;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
import com.lycanitesmobs.compat.*;
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
import com.lycanitesmobs.core.VersionChecker;
import com.lycanitesmobs.core.capabilities.ExtendedEntityStorage;
import com.lycanitesmobs.core.capabilities.ExtendedPlayerStorage;
import com.lycanitesmobs.core.command.CommandMain;
import com.lycanitesmobs.core.config.ConfigBase;
import com.lycanitesmobs.core.dungeon.DungeonManager;
import com.lycanitesmobs.core.entity.EntityHitArea;
import com.lycanitesmobs.core.entity.ExtendedEntity;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.helpers.LMReflectionHelper;
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
import com.lycanitesmobs.core.worldgen.WorldGeneratorFluids;
import java.util.ArrayList;
import java.util.Arrays;


@Mod(
   modid = "lycanitesmobs",
   name = "Lycanites Mobs",
   version = "2.0.8.9 - MC 1.12.2",
   useMetadata = false,
   acceptedMinecraftVersions = "[1.12,1.13)",
   dependencies = "after:thaumcraft"
)
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
   public static final PacketHandler packetHandler = new PacketHandler();
   public static ModInfo modInfo;
   public static ConfigBase config;
   @Instance("lycanitesmobs")
   public static LycanitesMobs instance;
   @SidedProxy(
      clientSide = "com.lycanitesmobs.client.ClientProxy",
      serverSide = "com.lycanitesmobs.CommonProxy"
   )
   public static CommonProxy proxy;
   @CapabilityInject(ExtendedEntity.class)
   public static final Capability<ExtendedEntity> EXTENDED_ENTITY = null;
   @CapabilityInject(ExtendedPlayer.class)
   public static final Capability<ExtendedPlayer> EXTENDED_PLAYER = null;
   public static final CreativeTabs itemsTab = new CreativeTabItems(CreativeTabs.getNextID(), "lycanitesmobs.items");
   public static final CreativeTabs blocksTab = new CreativeTabBlocks(CreativeTabs.getNextID(), "lycanitesmobs.blocks");
   public static final CreativeTabs creaturesTab = new CreativeTabCreatures(CreativeTabs.getNextID(), "lycanitesmobs.creatures");
   public static final CreativeTabs chargesTab = new CreativeTabCharges(CreativeTabs.getNextID(), "lycanitesmobs.charges");
   public static final CreativeTabs equipmentPartsTab = new CreativeTabEquipmentParts(CreativeTabs.getNextID(), "lycanitesmobs.equipmentparts");
   public static String texturePath = "mods/lycanitesmobs/";
   public PotionEffects potionEffects;
   public static WorldGeneratorDungeon dungeonGenerator;

   @EventHandler
   public void preInit(FMLPreInitializationEvent event) {
      modInfo = new ModInfo(this, "Lycanites Mobs", 1000);
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
      packetHandler.init();
      NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
      LMReflectionHelper.setPrivateFinalValue(
         RangedAttribute.class, (RangedAttribute)Attributes.MAX_HEALTH, 100000, "maximumValue", "maximumValue"
      );
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
         "How many ticks it takes for an entity to be forcefully removed (1 second = 20 ticks). This only applies to Mob, other entities are instantly removed."
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
      CapabilityManager.INSTANCE.register(ExtendedPlayer.class, new ExtendedPlayerStorage(), ExtendedPlayer::new);
      CapabilityManager.INSTANCE.register(ExtendedEntity.class, new ExtendedEntityStorage(), ExtendedEntity::new);
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
      GameRegistry.registerWorldGenerator(new WorldGeneratorFluids(), 0);
      DungeonManager.getInstance().loadAllFromJSON();
      dungeonGenerator = new WorldGeneratorDungeon();
      GameRegistry.registerWorldGenerator(dungeonGenerator, 1000);
      ItemHalloweenTreat.createObjectLists();
      ItemWinterGift.createObjectLists();
      proxy.registerEvents();
      proxy.registerRenders(modInfo);
      proxy.registerTextures();
   }

   @EventHandler
   public void init(FMLInitializationEvent event) {
      proxy.registerModels(modInfo);
   }

   @EventHandler
   public void postInit(FMLPostInitializationEvent event) {
      CreatureManager.getInstance().lateStartup(modInfo);
   }

   @EventHandler
   public void serverLoad(FMLServerStartingEvent event) {
      event.registerServerCommand(new CommandMain());
   }

   public static void logInfo(String key, String message) {
      if ("".equals(key) || config.getBool("Debug", key, false)) {
         System.out.println("[LycanitesMobs] [Info] [" + key + "] " + message);
      }
   }

   public static void logDebug(String key, String message) {
      if ("".equals(key) || config.getBool("Debug", key, false)) {
         System.out.println("[LycanitesMobs] [Debug] [" + key + "] " + message);
      }
   }

   public static void logWarning(String key, String message) {
      if ("".equals(key) || config.getBool("Debug", key, false)) {
         System.err.println("[LycanitesMobs] [WARNING] [" + key + "] " + message);
      }
   }

   static {
      FluidRegistry.enableUniversalBucket();
   }
}
