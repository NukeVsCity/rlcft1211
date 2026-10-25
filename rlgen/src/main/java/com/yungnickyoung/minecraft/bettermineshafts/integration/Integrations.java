package com.yungnickyoung.minecraft.bettermineshafts.integration;

import com.yungnickyoung.minecraft.bettermineshafts.mc.World;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The compat lists as RLCraft had them: Rustic (iron and golden lanterns, hanging), Charm (iron and gold lanterns), Varied Commodities (lamp and candle). Rustic's lanterns are 1.21 hanging lanterns, the lamp a standing lantern, the
 * candle a lit candle.
 */
public class Integrations {
    private static final BlockState RUSTIC_LANTERN = Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
    /** Charm's iron and gold lanterns (on in RLCraft: Future MC isn't installed), hanging. */
    private static final BlockState CHARM_LANTERN = Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
    private static final BlockState VC_LAMP = Blocks.LANTERN.defaultBlockState();
    private static final BlockState VC_CANDLE = Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.LIT, true);
    private static final List<BlockState> LANTERNS = List.of(RUSTIC_LANTERN, RUSTIC_LANTERN, CHARM_LANTERN, CHARM_LANTERN, VC_LAMP, VC_CANDLE);
    private static final List<BlockState> LEFT_TORCHES = List.of(Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST), VC_CANDLE);
    private static final List<BlockState> RIGHT_TORCHES = List.of(Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST), VC_CANDLE);
    public static final VariedCommodities VARIED_COMMODITIES = new VariedCommodities();

    private static BlockState pick(List<BlockState> list, Random random) {
        float delta = 1f / list.size();
        float currBottom = 0f;
        float r = random.nextFloat();
        for (BlockState s : list) {
            if (r >= currBottom && r < currBottom + delta) return s;
            currBottom += delta;
        }
        return list.get(list.size() - 1);
    }

    public static BlockState getLantern(Random random) {
        return pick(LANTERNS, random);
    }

    public static BlockState getLeftTorch(Random random) {
        return pick(LEFT_TORCHES, random);
    }

    public static BlockState getRightTorch(Random random) {
        return pick(RIGHT_TORCHES, random);
    }

    /** Block.canPlaceBlockAt: a torch needs a side (or the floor) it can stand on, the candle (default Block) a replaceable spot. */
    public static boolean canPlaceBlockAt(BlockState state, World world, BlockPos pos) {
        if (state.getBlock() instanceof WallTorchBlock) {
            if (Blocks.TORCH.defaultBlockState().canSurvive(world.level, pos)) return true;
            for (Direction d : Direction.Plane.HORIZONTAL) {
                if (Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, d).canSurvive(world.level, pos)) return true;
            }
            return false;
        }
        return world.getBlockState(pos).canBeReplaced();
    }

    public static class VariedCommodities {
        public boolean isTorchVariedCommoditiesCandle(BlockState s) {
            return s == VC_CANDLE;
        }

        public boolean isLanternVariedCommoditiesCandle(BlockState s) {
            return s == VC_CANDLE;
        }

        public boolean isLanternVariedCommoditiesLamp(BlockState s) {
            return s == VC_LAMP;
        }

        /** Candle and lamp rotations/colours lived in VC's tile entities; 1.21 candles have none. */
        public void spawnCandleTorchEntity(World world, int x, int y, int z, int rotation) {
        }

        public void spawnCandleLanternEntity(World world, int x, int y, int z) {
        }

        public void spawnLampLanternEntity(World world, int x, int y, int z) {
        }
    }
}
