package com.yungnickyoung.minecraft.bettercaves.config;


/**
 * Configuration options for Better Caves.
 * <p>
 * Fields not annotated with @Ignore are visible to users.
 * Some @Ignore fields are not used at all, while the rest are used but annotated as such
 * in order to keep them hidden from the user, as they are too delicate to allow the user
 * to mess with.
 * <p>
 * This class and all of its fields provide default config values, as well as an in-game config GUI.
 * The values here are not actually used directly - they are baked into a ConfigHolder each time a new
 * ConfigHolder is created. Separate ConfigHolders are created for each dimension. This allows any or all
 * config values to be overridden differently for each dimension.
 */
public class Configuration {
    public static ConfigUndergroundGen caveSettings = new ConfigUndergroundGen();

    public static ConfigBedrockGen bedrockSettings = new ConfigBedrockGen();

    public static ConfigDebug debugsettings = new ConfigDebug();

    public static int[] whitelistedDimensionIDs = {0};

    public static boolean enableGlobalWhitelist = false;
}
