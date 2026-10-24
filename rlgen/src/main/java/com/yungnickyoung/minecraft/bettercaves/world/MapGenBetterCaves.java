package com.yungnickyoung.minecraft.bettercaves.world;

import com.nukevscity.rlgen.legacy.ChunkPrimer;
import com.nukevscity.rlgen.legacy.LBlock;
import com.nukevscity.rlgen.legacy.LegacyWorld;
import com.nukevscity.rlgen.legacy.gen.MapGenCaves;
import com.yungnickyoung.minecraft.bettercaves.config.BCSettings;
import com.yungnickyoung.minecraft.bettercaves.config.RLCraftConfig;
import com.yungnickyoung.minecraft.bettercaves.config.util.ConfigHolder;
import com.yungnickyoung.minecraft.bettercaves.mc.World;
import com.yungnickyoung.minecraft.bettercaves.util.BetterCavesUtils;
import com.yungnickyoung.minecraft.bettercaves.world.bedrock.FlattenBedrock;

/** Better Caves 1.12.2-2.0.4 MapGenBetterCaves for the overworld (dimension 0 is whitelisted in RLCraft's config). */
public class MapGenBetterCaves extends MapGenCaves {
    public WaterRegionController waterRegionController;
    private CaveCarverController caveCarverController;
    private CavernCarverController cavernCarverController;
    public ConfigHolder config;
    private World bcWorld;

    @Override
    public void generate(LegacyWorld worldIn, int chunkX, int chunkZ, ChunkPrimer primer) {
        if (bcWorld == null) {
            this.initialize(worldIn);
        }

        if (config.flattenBedrock.get())
            FlattenBedrock.flattenBedrock(primer, config.bedrockWidth.get());

        int[][] surfaceAltitudes = new int[16][16];
        for (int subX = 0; subX < 16 / BCSettings.SUB_CHUNK_SIZE; subX++) {
            for (int subZ = 0; subZ < 16 / BCSettings.SUB_CHUNK_SIZE; subZ++) {
                int startX = subX * BCSettings.SUB_CHUNK_SIZE;
                int startZ = subZ * BCSettings.SUB_CHUNK_SIZE;
                for (int offsetX = 0; offsetX < BCSettings.SUB_CHUNK_SIZE; offsetX++) {
                    for (int offsetZ = 0; offsetZ < BCSettings.SUB_CHUNK_SIZE; offsetZ++) {
                        int surfaceHeight;
                        if (config.overrideSurfaceDetection.get()) {
                            surfaceHeight = 1;
                        } else {
                            surfaceHeight = BetterCavesUtils.getSurfaceAltitudeForColumn(primer, startX + offsetX, startZ + offsetZ);
                        }
                        surfaceAltitudes[startX + offsetX][startZ + offsetZ] = surfaceHeight;
                    }
                }
            }
        }

        LBlock[][] liquidBlocks = waterRegionController.getLiquidBlocksForChunk(chunkX, chunkZ);

        caveCarverController.carveChunk(primer, chunkX, chunkZ, surfaceAltitudes, liquidBlocks);
        cavernCarverController.carveChunk(primer, chunkX, chunkZ, surfaceAltitudes, liquidBlocks);
    }

    private void initialize(LegacyWorld worldIn) {
        RLCraftConfig.apply();
        this.world = worldIn;
        this.bcWorld = new World(worldIn);
        this.config = new ConfigHolder();
        this.waterRegionController = new WaterRegionController(bcWorld, config);
        this.caveCarverController = new CaveCarverController(bcWorld, config);
        this.cavernCarverController = new CavernCarverController(bcWorld, config);
    }
}
