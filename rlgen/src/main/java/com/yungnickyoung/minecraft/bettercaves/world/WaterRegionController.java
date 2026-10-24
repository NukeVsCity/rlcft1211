package com.yungnickyoung.minecraft.bettercaves.world;

import com.yungnickyoung.minecraft.bettercaves.config.util.ConfigHolder;
import com.yungnickyoung.minecraft.bettercaves.enums.RegionSize;
import com.yungnickyoung.minecraft.bettercaves.noise.FastNoise;
import com.yungnickyoung.minecraft.bettercaves.noise.NoiseUtils;
import com.yungnickyoung.minecraft.bettercaves.util.BetterCavesUtils;
import com.nukevscity.rlgen.legacy.LBlock;
import com.yungnickyoung.minecraft.bettercaves.mc.Blocks;
import com.yungnickyoung.minecraft.bettercaves.mc.BlockPos;
import com.yungnickyoung.minecraft.bettercaves.mc.World;

import java.util.Random;

public class WaterRegionController {
    private FastNoise waterRegionController;
    private long worldSeed;
    private int dimensionID;
    private String dimensionName;
    private Random rand;

    // Vars determined from config
    private LBlock lavaBlock;
    private LBlock waterBlock;
    private float waterRegionThreshold;

    // Constants
    private static final float SMOOTH_RANGE = .04f;
    private static final float SMOOTH_DELTA = .01f;

    public WaterRegionController(World world, ConfigHolder config) {
        this.worldSeed = world.getSeed();
        this.rand = new Random();

        // Vars from config
        this.lavaBlock = getLavaBlockFromString(config.lavaBlock.get());
        this.waterBlock = getWaterBlockFromString(config.waterBlock.get());
        this.waterRegionThreshold = NoiseUtils.simplexNoiseOffsetByPercent(-1f, config.waterRegionSpawnChance.get() / 100f);

        // Water region controller
        float waterRegionSize = calcWaterRegionSize(config.waterRegionSize.get(), config.waterRegionCustomSize.get());
        this.waterRegionController = new FastNoise();
        this.waterRegionController.SetSeed((int)world.getSeed() + 444);
        this.waterRegionController.SetFrequency(waterRegionSize);
    }

    public LBlock[][] getLiquidBlocksForChunk(int chunkX, int chunkZ) {
        rand.setSeed(worldSeed ^ chunkX ^ chunkZ);
        LBlock[][] blocks = new LBlock[16][16];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int realX = chunkX * 16 + x;
                int realZ = chunkZ * 16 + z;
                BlockPos pos = new BlockPos(realX, 1, realZ);
                blocks[x][z] = getLiquidBlockAtPos(rand, pos);
            }
        }
        return blocks;
    }

    private LBlock getLiquidBlockAtPos(Random rand, BlockPos blockPos) {
        LBlock liquidBlock = lavaBlock;
        if (waterRegionThreshold > -1f) { // Don't bother calculating noise if water regions are disabled
            float waterRegionNoise = waterRegionController.GetNoise(blockPos.getX(), blockPos.getZ());

            // If water region threshold check is passed, change liquid block to water
            float randOffset = rand.nextFloat() * SMOOTH_DELTA + SMOOTH_RANGE;
            if (waterRegionNoise < waterRegionThreshold - randOffset)
                liquidBlock = waterBlock;
            else if (waterRegionNoise < waterRegionThreshold + randOffset)
                liquidBlock = null;
        }
        return liquidBlock;
    }

    private LBlock getLavaBlockFromString(String lavaString) {
        LBlock lavaBlock;
        try {
            lavaBlock = LBlock.LAVA; // RLCraft: minecraft:lava
            com.yungnickyoung.minecraft.bettercaves.mc.Log.LOGGER.info("Using block '" + lavaString + "' as lava in cave generation for dimension " +
                    BetterCavesUtils.dimensionAsString(dimensionID, dimensionName) + " ...");
        } catch (Exception e) {
            com.yungnickyoung.minecraft.bettercaves.mc.Log.LOGGER.warn("Unable to use block '" + lavaString + "': " + e);
            com.yungnickyoung.minecraft.bettercaves.mc.Log.LOGGER.warn("Using vanilla lava instead...");
            lavaBlock = Blocks.LAVA;
        }
        if (lavaBlock == null) {
            com.yungnickyoung.minecraft.bettercaves.mc.Log.LOGGER.warn("Unable to use block '" + lavaString + "': null block returned.\n Using vanilla lava instead...");
            lavaBlock = Blocks.LAVA;
        }
        return lavaBlock;
    }

    private LBlock getWaterBlockFromString(String waterString) {
        LBlock waterBlock;
        try {
            waterBlock = LBlock.WATER; // RLCraft: minecraft:water
            com.yungnickyoung.minecraft.bettercaves.mc.Log.LOGGER.info("Using block '" + waterString + "' as water in cave generation for dimension " +
                    BetterCavesUtils.dimensionAsString(dimensionID, dimensionName) + " ...");
        } catch (Exception e) {
            com.yungnickyoung.minecraft.bettercaves.mc.Log.LOGGER.warn("Unable to use block '" + waterString + "': " + e);
            com.yungnickyoung.minecraft.bettercaves.mc.Log.LOGGER.warn("Using vanilla water instead...");
            waterBlock = Blocks.WATER;
        }

        if (waterBlock == null) {
            com.yungnickyoung.minecraft.bettercaves.mc.Log.LOGGER.warn("Unable to use block '" + waterString + "': null block returned.\n Using vanilla water instead...");
            waterBlock = Blocks.WATER;
        }

        return waterBlock;
    }

    /**
     * @return frequency value for water region controller
     */
    private float calcWaterRegionSize(RegionSize waterRegionSize, float waterRegionCustomSize) {
        switch (waterRegionSize) {
            case Small:
                return .008f;
            case Large:
                return .0028f;
            case ExtraLarge:
                return .001f;
            case Custom:
                return waterRegionCustomSize;
            default: // Medium
                return .004f;
        }
    }
}
