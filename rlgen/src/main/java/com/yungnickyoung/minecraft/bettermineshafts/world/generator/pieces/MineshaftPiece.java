package com.yungnickyoung.minecraft.bettermineshafts.world.generator.pieces;

import com.yungnickyoung.minecraft.bettermineshafts.mc.*;
import com.nukevscity.rlgen.rc.BiomeDict;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

import com.google.common.collect.ImmutableSet;
import com.yungnickyoung.minecraft.bettermineshafts.util.BlockSetSelector;
import com.yungnickyoung.minecraft.bettermineshafts.world.generator.MineshaftVariantSettings;
import com.yungnickyoung.minecraft.bettermineshafts.world.generator.MineshaftVariants;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Random;
import java.util.Set;

public abstract class MineshaftPiece extends StructureComponent {
    protected MineshaftVariantSettings settings;
    protected int pieceChainLen;

    protected static final BlockState AIR = Blocks.AIR.defaultBlockState();

    public MineshaftPiece(int i, int pieceChainLen, MineshaftVariantSettings settings) {
        super(i);
        this.settings = settings;
        this.pieceChainLen = pieceChainLen;
    }

    public void setBoundingBox(StructureBoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    public MineshaftVariantSettings getSettings() {
        return settings;
    }

    /**
     * Adds new pieces to the list passed in.
     * Also resolves variables local to this piece like support positions.
     * Does not actually place any blocks.
     */
    @Override
    public void buildComponent(StructureComponent structurePiece, List<StructureComponent> list, Random random) {
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *                                     BLOCK SELECTORS                                     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    protected BlockSetSelector getMainSelector() {
        return settings.mainSelector;
    }

    protected BlockSetSelector getFloorSelector() {
        return settings.floorSelector;
    }

    protected BlockSetSelector getBrickSelector() {
        return settings.brickSelector;
    }

    protected BlockSetSelector getLegSelector() {
        return settings.legSelector;
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *                                BIOME VARIANT BLOCK GETTERS                              *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    protected BlockState getMainBlock() {
        return settings.mainBlock;
    }

    protected BlockState getSupportBlock() {
        return settings.supportBlock;
    }

    protected BlockState getMainSlab() {
        return settings.slabBlock;
    }

    protected BlockState getGravel() {
        return settings.gravelBlock;
    }

    protected BlockState getMainDoorwayWall() {
        return settings.stoneWallBlock;
    }

    protected BlockState getMainDoorwaySlab() {
        return settings.stoneSlabBlock;
    }

    protected BlockState getTrapdoor() {
        return Blocks.OAK_TRAPDOOR.defaultBlockState();
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *                                  GENERATION UTIL METHODS                                *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    /**
     * Randomly add vines with a given chance in a given area, facing the specified direction.
     */
    protected void addVines(World world, StructureBoundingBox boundingBox, Direction facing, Random random, float chance, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    mutable.set(x, y, z);
                    Direction dir = facing.getAxis() == Direction.Axis.X ? facing : facing.getOpposite();
                    if (
                        this.getBlockStateFromPos(world, x, y, z, boundingBox).getBlock() == Blocks.AIR
                            && this.getBlockStateFromPos(world, x + facing.getStepX(), y, z + facing.getStepZ(), boundingBox).isFaceSturdy(world.level, new BlockPos(x + dir.getStepZ(), y, z + dir.getStepZ()), dir.getOpposite())
                    ) {
                        if (random.nextFloat() < chance) {
                            this.setBlockState(world, Blocks.VINE.defaultBlockState().setValue(VineBlock.NORTH, dir == Direction.NORTH).setValue(VineBlock.EAST, dir == Direction.EAST).setValue(VineBlock.SOUTH, dir == Direction.SOUTH).setValue(VineBlock.WEST, dir == Direction.WEST), x, y, z, boundingBox);
                        }
                    }
                }
            }
        }
    }

    /**
     * Randomly add vines with a given chance in a given area, doing passes for all four horizontal directions.
     */
    protected void addVines(World world, StructureBoundingBox boundingBox, Random random, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        if (settings.vineChance <= 0) return;
        this.addVines(world, boundingBox, Direction.EAST, random, settings.vineChance, minX, minY, minZ, maxX, maxY, maxZ);
        this.addVines(world, boundingBox, Direction.WEST, random, settings.vineChance, minX, minY, minZ, maxX, maxY, maxZ);
        this.addVines(world, boundingBox, Direction.NORTH, random, settings.vineChance, minX, minY, minZ, maxX, maxY, maxZ);
        this.addVines(world, boundingBox, Direction.SOUTH, random, settings.vineChance, minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * Add decorations specific to a biome variant, such as snow.
     */
    protected void addBiomeDecorations(World world, StructureBoundingBox box, Random random, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos blockPos = new BlockPos(this.getXWithOffset(x, z), this.getYWithOffset(y), this.getZWithOffset(x, z));
                    BlockState state = this.getBlockStateFromPos(world, x, y, z, box);
                    BlockState stateBelow = this.getBlockStateFromPos(world, x, y - 1, z, box);
                    Block blockBelow = stateBelow.getBlock();

                    // Snow layers
                    if (settings.snowChance > 0) {
                        if (random.nextFloat() < settings.snowChance && state == AIR && blockBelow != Blocks.ICE && blockBelow != Blocks.PACKED_ICE && blockBelow != Blocks.BARRIER && stateBelow.isFaceSturdy(world.level, blockPos.below(), Direction.UP)) {
                            this.setBlockState(world, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, random.nextInt(2) + 1), x, y, z, box);
                        }
                    }

                    // Cactus
                    if (settings.cactusChance > 0) {
                        if (random.nextFloat() < settings.cactusChance) {
                            if (state == AIR && blockBelow == Blocks.SAND) {
                                this.setBlockState(world, Blocks.CACTUS.defaultBlockState().setValue(CactusBlock.AGE, 0), x, y, z, box);
                                if (random.nextFloat() < .5f && this.getBlockStateFromPos(world, x, y + 1, z, box) == AIR) {
                                    this.setBlockState(world, Blocks.CACTUS.defaultBlockState().setValue(CactusBlock.AGE, 0), x, y + 1, z, box);
                                }
                            }
                        }
                    }

                    // Deadbush
                    if (settings.deadBushChance > 0) {
                        if (random.nextFloat() < settings.deadBushChance) {
                            if (state == AIR && (blockBelow == Blocks.SAND || blockBelow.defaultBlockState().is(net.minecraft.tags.BlockTags.TERRACOTTA) && blockBelow != Blocks.TERRACOTTA || blockBelow == Blocks.TERRACOTTA || blockBelow == Blocks.DIRT)) {
                                this.setBlockState(world, Blocks.DEAD_BUSH.defaultBlockState(), x, y, z, box);
                            }
                        }
                    }

                    // Mushrooms
                    if (settings.mushroomChance > 0) {
                        if (state == AIR && (blockBelow == Blocks.MYCELIUM || blockBelow == Blocks.DIRT)) {
                            float r = random.nextFloat();
                            if (r < settings.mushroomChance / 2) {
                                this.setBlockState(world, Blocks.RED_MUSHROOM.defaultBlockState(), x, y, z, box);
                            } else if (r < settings.mushroomChance) {
                                this.setBlockState(world, Blocks.BROWN_MUSHROOM.defaultBlockState(), x, y, z, box);
                            }
                        }
                    }
                }
            }
        }
    }

    protected void generateLeg(World world, Random random, StructureBoundingBox box, int x, int z, BlockSetSelector selector) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(x, -1, z);
        BlockState state = this.getBlockStateFromPos(world, mutable, box);

        while (getYWithOffset(mutable.getY()) > 0 && (state == AIR || liquid(state))) {
            this.setBlockState(world, selector.get(random), x, mutable.getY(), z, box);
            mutable.move(Direction.DOWN);
            state = this.getBlockStateFromPos(world, mutable, box);
        }
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *                                       FILL METHODS                                      *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * Replaces each block in the provided area with the provided BlockState.
     */
    protected void fill(World world, StructureBoundingBox blockBox, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockState blockState) {
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    this.setBlockState(world, blockState, x, y, z, blockBox);
                }
            }
        }
    }

    /**
     * Replaces each block in the provided area with blocks determined by the provided BlockSelector.
     */
    protected void fill(World world, StructureBoundingBox blockBox, Random random, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockSetSelector selector) {
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    this.setBlockState(world, selector.get(random), x, y, z, blockBox);
                }
            }
        }
    }

    /**
     * Replaces each air block in the provided area with the provided BlockState.
     */
    protected void replaceAir(World world, StructureBoundingBox blockBox, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockState blockState) {
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    BlockState currState = this.getBlockStateFromPosFixed(world, x, y, z, blockBox);
                    if (currState != null && currState == AIR) {
                        this.setBlockState(world, blockState, x, y, z, blockBox);
                    }
                }
            }
        }
    }

    /**
     * Replaces each air block in the provided area with blocks determined by the provided BlockSelector.
     */
    protected void replaceAir(World world, StructureBoundingBox blockBox, Random random, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockSetSelector selector) {
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    BlockState currState = this.getBlockStateFromPosFixed(world, x, y, z, blockBox);
                    if (currState != null && currState == AIR) {
                        this.setBlockState(world, selector.get(random), x, y, z, blockBox);
                    }
                }
            }
        }
    }

    /**
     * Replaces each non-air block in the provided area with the provided BlockState.
     */
    protected void replaceNonAir(World world, StructureBoundingBox blockBox, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockState blockState) {
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    BlockState currState = this.getBlockStateFromPosFixed(world, x, y, z, blockBox);
                    if (currState != null && currState != AIR) {
                        this.setBlockState(world, blockState, x, y, z, blockBox);
                    }
                }
            }
        }
    }

    /**
     * Replaces each non-air block in the provided area with blocks determined by the provided BlockSelector.
     */
    protected void replaceNonAir(World world, StructureBoundingBox blockBox, Random random, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockSetSelector selector) {
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    BlockState currState = this.getBlockStateFromPosFixed(world, x, y, z, blockBox);
                    if (currState != null && currState != AIR) {
                        this.setBlockState(world, selector.get(random), x, y, z, blockBox);
                    }
                }
            }
        }
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *                                   RANDOM FILL METHODS                                   *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * Has a chance of replacing each block in the provided area with the provided BlockState.
     */
    protected void chanceFill(World world, StructureBoundingBox blockBox, Random random, float chance, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockState blockState) {
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    if (random.nextFloat() < chance) {
                        this.setBlockState(world, blockState, x, y, z, blockBox);
                    }
                }
            }
        }
    }

    /**
     * Has a chance of replacing each block in the provided area with a block determined by the provided BlockSelector.
     */
    protected void chanceFill(World world, StructureBoundingBox blockBox, Random random, float chance, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockSetSelector selector) {
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    if (random.nextFloat() < chance) {
                        this.setBlockState(world, selector.get(random), x, y, z, blockBox);
                    }
                }
            }
        }    }

    /**
     * Has a chance of replacing each air block in the provided area with the provided BlockState.
     */
    protected void chanceReplaceAir(World world, StructureBoundingBox blockBox, Random random, float chance, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockState blockState) {
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    if (random.nextFloat() < chance) {
                        BlockState currState = this.getBlockStateFromPosFixed(world, x, y, z, blockBox);
                        if (currState != null && currState == AIR) {
                            this.setBlockState(world, blockState, x, y, z, blockBox);
                        }
                    }
                }
            }
        }
    }

    /**
     * Has a chance of replacing each air block in the provided area with a block determined by the provided BlockSelector.
     */
    protected void chanceReplaceAir(World world, StructureBoundingBox blockBox, Random random, float chance, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockSetSelector selector) {
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    if (random.nextFloat() < chance) {
                        BlockState currState = this.getBlockStateFromPosFixed(world, x, y, z, blockBox);
                        if (currState != null && currState == AIR) {
                            this.setBlockState(world, selector.get(random), x, y, z, blockBox);
                        }
                    }
                }
            }
        }    }

    /**
     * Has a chance of replacing each non-air block in the provided area with the provided BlockState.
     */
    protected void chanceReplaceNonAir(World world, StructureBoundingBox blockBox, Random random, float chance, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockState blockState) {
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    if (random.nextFloat() < chance) {
                        BlockState currState = this.getBlockStateFromPosFixed(world, x, y, z, blockBox);
                        if (currState != null && currState != AIR) {
                            this.setBlockState(world, blockState, x, y, z, blockBox);
                        }
                    }
                }
            }
        }
    }

    /**
     * Has a chance of replacing each non-air block in the provided area with a block determined by the provided BlockSelector.
     */
    protected void chanceReplaceNonAir(World world, StructureBoundingBox blockBox, Random random, float chance, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockSetSelector selector) {
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    if (random.nextFloat() < chance) {
                        BlockState currState = this.getBlockStateFromPosFixed(world, x, y, z, blockBox);
                        if (currState != null && currState != AIR) {
                            this.setBlockState(world, selector.get(random), x, y, z, blockBox);
                        }
                    }
                }
            }
        }
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *                                      BLOCK SET/GET                                      *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    protected void chanceAddBlock(World world, Random random, float chance, BlockState block, int x, int y, int z, StructureBoundingBox blockBox) {
        if (random.nextFloat() < chance) {
            this.setBlockState(world, block, x, y, z, blockBox);
        }
    }

    /**
     * My "fixed" version of getBlockAt that returns null instead of air if block is out of bounds
     *
     * @return the block at the given position, or null if it is outside of the blockBox
     */
    protected BlockState getBlockStateFromPosFixed(World world, int x, int y, int z, StructureBoundingBox blockBox) {
        int i = this.getXWithOffset(x, z);
        int j = this.getYWithOffset(y);
        int k = this.getZWithOffset(x, z);
        BlockPos blockPos = new BlockPos(i, j, k);
        return !blockBox.isVecInside(blockPos) ? null : world.getBlockState(blockPos);
    }

    protected BlockState getBlockStateFromPos(World worldIn, BlockPos pos, StructureBoundingBox boundingboxIn) {
        return super.getBlockStateFromPos(worldIn, pos.getX(), pos.getY(), pos.getZ(), boundingboxIn);
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *                                  PLACEMENT METHODS                                      *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    protected boolean isInOcean(World world, int localX, int localZ) {
        BlockPos pos = new BlockPos.MutableBlockPos(getXWithOffset(localX, localZ), 1, getZWithOffset(localX, localZ));
        return BiomeDict.hasType(world.getBiome(pos), "OCEAN");
    }
}
