package com.yungnickyoung.minecraft.bettercaves.world.ravine;

import com.nukevscity.rlgen.legacy.ChunkPrimer;
import com.nukevscity.rlgen.legacy.LBlock;
import com.nukevscity.rlgen.legacy.LegacyWorld;
import com.nukevscity.rlgen.legacy.gen.MapGenRavine;
import com.yungnickyoung.minecraft.bettercaves.config.RLCraftConfig;
import com.yungnickyoung.minecraft.bettercaves.config.util.ConfigHolder;
import com.yungnickyoung.minecraft.bettercaves.mc.BiomeDictionary;
import com.yungnickyoung.minecraft.bettercaves.mc.BlockPos;
import com.yungnickyoung.minecraft.bettercaves.mc.Blocks;
import com.yungnickyoung.minecraft.bettercaves.mc.World;
import com.yungnickyoung.minecraft.bettercaves.util.BetterCavesUtils;
import com.yungnickyoung.minecraft.bettercaves.world.WaterRegionController;
import com.yungnickyoung.minecraft.bettercaves.world.carver.CarverUtils;

/** Better Caves 1.12.2-2.0.4 MapGenBetterRavine: vanilla ravines carved with Better Caves' digBlock (lava/water regions, no gravel fall). */
public class MapGenBetterRavine extends MapGenRavine {
    private ConfigHolder config;
    private WaterRegionController waterRegionController;
    private World bcWorld;
    LBlock[][] currChunkLiquidBlocks;
    int currChunkX, currChunkZ;

    @Override
    public void generate(LegacyWorld worldIn, int x, int z, ChunkPrimer primer) {
        if (config == null) {
            this.initialize(worldIn);
        }
        if (config.enableVanillaRavines.get()) {
            super.generate(worldIn, x, z, primer);
        }
    }

    @Override
    protected void digBlock(ChunkPrimer primer, int x, int y, int z, int chunkX, int chunkZ, boolean foundTop) {
        BlockPos pos = new BlockPos(x + chunkX * 16, y, z + chunkZ * 16);
        LBlock liquidBlockState;
        if (currChunkLiquidBlocks != null && chunkX == currChunkX && chunkZ == currChunkZ) {
            try {
                liquidBlockState = currChunkLiquidBlocks[BetterCavesUtils.getLocal(x)][BetterCavesUtils.getLocal(z)];
            } catch (Exception e) {
                liquidBlockState = Blocks.LAVA;
            }
        } else {
            try {
                currChunkLiquidBlocks = waterRegionController.getLiquidBlocksForChunk(chunkX, chunkZ);
                liquidBlockState = currChunkLiquidBlocks[BetterCavesUtils.getLocal(x)][BetterCavesUtils.getLocal(z)];
                currChunkX = chunkX;
                currChunkZ = chunkZ;
            } catch (Exception e) {
                liquidBlockState = Blocks.LAVA;
            }
        }

        boolean flooded = config.enableFloodedRavines.get() && BiomeDictionary.hasType(bcWorld.getBiome(pos), BiomeDictionary.Type.OCEAN)
            && y < bcWorld.getSeaLevel();
        if (flooded) {
            float smoothAmpFactor = BetterCavesUtils.biomeDistanceFactor(bcWorld, pos, 2, b -> !BiomeDictionary.hasType(b, BiomeDictionary.Type.OCEAN));
            if (smoothAmpFactor <= .25f) {
                return;
            }
        }

        LBlock airBlockState = flooded ? Blocks.WATER : AIR;
        CarverUtils.digBlock(bcWorld, primer, pos, airBlockState, liquidBlockState, config.liquidAltitude.get(), config.replaceFloatingGravel.get());
    }

    @Override
    protected boolean isOceanBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ) {
        return false;
    }

    private void initialize(LegacyWorld worldIn) {
        RLCraftConfig.apply();
        this.bcWorld = new World(worldIn);
        this.config = new ConfigHolder();
        this.waterRegionController = new WaterRegionController(bcWorld, config);
    }
}
