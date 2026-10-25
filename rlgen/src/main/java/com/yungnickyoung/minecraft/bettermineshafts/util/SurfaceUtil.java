package com.yungnickyoung.minecraft.bettermineshafts.util;

import com.yungnickyoung.minecraft.bettermineshafts.mc.World;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SurfaceUtil {
    /** The top non-air block of a column. 1.12 generated far chunks on demand here, so this reads the 1.12 terrain. */
    public static int getSurfaceHeight(World world, ColPos pos) {
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(pos.x, 255, pos.z);
        if (world.getTerrainState(new BlockPos(blockPos.getX(), 255, blockPos.getZ())) != Blocks.AIR.defaultBlockState())
            return 255;
        for (int y = 255; y >= 0; y--) {
            BlockState blockState = world.getTerrainState(blockPos);
            if (blockState != Blocks.AIR.defaultBlockState())
                return y;
            blockPos.move(Direction.DOWN);
        }
        return 1; // Surface somehow not found
    }
}
