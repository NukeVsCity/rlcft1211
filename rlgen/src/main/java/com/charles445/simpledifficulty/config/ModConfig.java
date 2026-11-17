package com.charles445.simpledifficulty.config;

/**
 * 1.12 ModConfig (+ ServerConfig/QuickConfig of the api): same field names, values from RLCraft 2.9.3 simpledifficulty.cfg.
 * Not read from a file; edit here. The Serene Seasons / Harvest Festival / Armor Underwear / Baubles / Inspirations compat stays off.
 */
public class ModConfig {
   public static final Server server = new Server();
   public static final Client client = new Client();

   public static class Server {
      public int canteenDoses = 3;
      public boolean debugMode = false;
      public boolean infinitePurifiedWater = false;
      public int ironCanteenDoses = 6;
      public boolean peacefulDanger = false;
      public boolean strictHeaters = true;
      public boolean temperatureEnabled = true;
      public boolean temperatureTileEntities = true;
      public boolean thirstDrinkBlocks = true;
      public boolean thirstDrinkRain = true;
      public boolean thirstEnabled = true;
      public final Miscellaneous miscellaneous = new Miscellaneous();
      public final Temperature temperature = new Temperature();
      public final Thirst thirst = new Thirst();
   }

   public static class Miscellaneous {
      public int campfireDecayChance = 2;
      public String[] campfireSpitBlacklist = new String[0];
      public boolean campfireSpitBlacklistIsWhitelist = false;
      public int campfireSpitDelay = 20;
      public boolean campfireSpitExperience = true;
      public int campfireSpitSize = 8;
      public int campfireStickIgniteChance = 5;
      public boolean goldenAppleJuiceEffect = true;
      public boolean iceDropsChunks = true;
      public boolean magmaDropsChunks = true;
      public int rainCollectorFillChance = 2;
      public boolean registerEnchantments = true;
      public int resistancePotionDurationLong = 9600;
      public int resistancePotionDurationShort = 3600;
      public int routinePacketDelay = 30;
   }

   public static class Temperature {
      public int altitudeMultiplier = 3;
      public int biomeMultiplier = 10;
      public boolean blocksTilesSeparate = true;
      public int enchantmentTemperature = 2;
      public double heaterFullPowerRange = 16.0;
      public double heaterMaxRange = 32.0;
      public int heaterTemperature = 10;
      public int snowValue = -10;
      public int sprintingValue = 3;
      public boolean stackingTemperature = true;
      public double stackingTemperatureLimit = 3.0;
      public int temperatureDamageDuration = 400;
      public double temperatureDamageScaling = 0.025;
      public int temperatureTickDangerBoost = 50;
      public int temperatureTickMax = 400;
      public int temperatureTickMin = 20;
      public double timeBiomeMultiplier = 1.25;
      public int timeMultiplier = 3;
      public boolean timeTemperatureDay = true;
      public boolean timeTemperatureNight = true;
      public int timeTemperatureShade = -2;
      public boolean undergroundEffect = true;
      public int undergroundEffectCutoff = 32;
      public int wetValue = -7;
   }

   public static class Thirst {
      public double thirstAttacking = 0.2;
      public double thirstBaseMovement = 0.01;
      public double thirstBreakBlock = 0.025;
      public double thirstDamageScaling = 0.025;
      public double thirstExhaustionLimit = 5.0;
      public double thirstJump = 0.15;
      public boolean thirstParasites = true;
      public double thirstParasitesChance = 0.1;
      public double thirstParasitesDamage = 0.25;
      public int thirstParasitesDuration = 1200;
      public double thirstParasitesHunger = 0.02;
      public double thirstSprintJump = 0.2;
      public double thirstSprintingMovement = 0.05;
      public double thirstSwimmingMovement = 0.015;
      public double thirstWalkingMovement = 0.01;
      public double thirstyStrength = 0.025;
   }

   public static class Client {
      public boolean alternateTemp = true;
      public boolean classicHUDTemperature = false;
      public boolean classicHUDThirst = false;
      public boolean classicHud = false;
      public boolean debugMode = false;
      public boolean drawThirstSaturation = false;
      public boolean heaterParticles = true;
      public boolean temperatureReadout = false;
      public final Thermometer thermometer = new Thermometer();
   }

   public static class Thermometer {
      public boolean enableThermometer = true;
      public boolean hudThermometer = true;
      public int hudThermometerX = 0;
      public int hudThermometerY = 0;
   }

   /** 1.12 QuickConfig */
   public static boolean isThirstEnabled() {
      return server.thirstEnabled;
   }

   public static boolean isTemperatureEnabled() {
      return server.temperatureEnabled;
   }
}
