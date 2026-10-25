package com.yungnickyoung.minecraft.bettermineshafts.config;

/** bettermineshafts-1_12_2.cfg as shipped with RLCraft 2.9.3. */
public class Configuration {
    public static double mineshaftSpawnRate = .003;
    public static SpawnRatesConfig spawnRates = new SpawnRatesConfig();
    public static OresConfig ores = new OresConfig();

    public static class SpawnRatesConfig {
        public float lanternSpawnRate = .015f;
        public float torchSpawnRate = .02f;
        public float workstationSpawnRate = .03f;
        public float workstationDungeonSpawnRate = .35f;
        public float smallShaftSpawnRate = .0715f;
        public float cobwebSpawnRate = .125f;
        public float smallShaftChestMinecartSpawnRate = .005f;
        public float mainShaftChestMinecartSpawnRate = .02f;
        public float smallShaftTntMinecartSpawnRate = .0025f;
        public float mainShaftTntMinecartSpawnRate = .003f;
        public int zombieVillagerRoomSpawnRate = 2;
        public int smallShaftPieceChainLength = 10;
    }

    public static class OresConfig {
        public boolean enabled = true;
        public int cobble = 50;
        public int coal = 20;
        public int iron = 9;
        public int redstone = 7;
        public int gold = 7;
        public int lapis = 3;
        public int emerald = 3;
        public int diamond = 1;
    }
}
