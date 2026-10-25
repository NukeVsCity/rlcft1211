package com.yungnickyoung.minecraft.bettermineshafts.util;

import com.yungnickyoung.minecraft.bettermineshafts.mc.*;
import com.nukevscity.rlgen.rc.BiomeDict;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

import com.yungnickyoung.minecraft.bettermineshafts.BetterMineshafts;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;

import java.util.*;

/**
 * Describes a set of blocks and the probability of each block in the set being chosen.
 */
public class BlockSetSelector {
    /**
     * Map of block to its corresponding probability.
     * The total sum of all the probabilities should not exceed 1.
     */
    private List<Map.Entry<BlockState, Float>> entries = new ArrayList<>();

    /**
     * The default block is used for any leftover probability ranges.
     * For example, if the total sum of all the probabilities of the entries is 0.6, then
     * there is a 0.4 chance of the defaultBlock being selected.
     */
    private BlockState defaultBlock = Blocks.AIR.defaultBlockState();

    public BlockSetSelector() {
    }

    public BlockSetSelector(BlockState defaultBlock) {
        this.defaultBlock = defaultBlock;
    }

    public static BlockSetSelector from(BlockState... blockStates) {
        BlockSetSelector selector = new BlockSetSelector();
        float chance = 1f / blockStates.length;

        for (BlockState state : blockStates) {
            selector.addBlock(state, chance);
        }

        return selector;
    }

    public BlockSetSelector addBlock(BlockState blockState, float chance) {
        // Abort if blockState already a part of this selector
        for (Map.Entry<BlockState, Float> entry : entries) {
            if (entry.getKey() == blockState) {
                BetterMineshafts.LOGGER.warn(String.format("WARNING: duplicate block %s added to BlockSelector!", blockState.toString()));
                return this;
            }
        }

        // Attempt to add blockState to entries
        float currTotal = entries.stream().map(Map.Entry::getValue).reduce(Float::sum).orElse(0f);
        float newTotal = currTotal + chance;
        if (newTotal > 1) { // Total probability cannot exceed 1
            BetterMineshafts.LOGGER.warn(String.format("WARNING: block %s added to BlockSelector exceeds max probabiltiy of 1!", blockState.toString()));
            return this;
        }
        entries.add(Map.entry(blockState, chance));
        return this;
    }

    /** An entry as Gson read it from variants.json (no checks, JSON order). */
    public void put(BlockState blockState, float chance) {
        entries.add(Map.entry(blockState, chance));
    }

    public BlockState get(Random random) {
        float target = random.nextFloat();
        float currBottom = 0;

        for (Map.Entry<BlockState, Float> entry : entries) {
            float chance = entry.getValue();
            if (currBottom <= target && target < currBottom + chance) {
                return entry.getKey();
            }

            currBottom += chance;
        }

        // No match found
        return this.defaultBlock;
    }

    public void setDefaultBlock(BlockState blockState) {
        this.defaultBlock = blockState;
    }
}
