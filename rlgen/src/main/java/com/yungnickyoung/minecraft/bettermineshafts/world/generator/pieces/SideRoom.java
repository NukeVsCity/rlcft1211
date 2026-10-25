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

import com.google.common.collect.Lists;
import com.yungnickyoung.minecraft.bettermineshafts.config.Configuration;
import com.yungnickyoung.minecraft.bettermineshafts.world.generator.BetterMineshaftGenerator;
import com.yungnickyoung.minecraft.bettermineshafts.util.BoxUtil;
import com.yungnickyoung.minecraft.bettermineshafts.world.generator.MineshaftVariantSettings;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Random;

public class SideRoom extends MineshaftPiece {
    private boolean hasDownstairs;
    private static final int
        SECONDARY_AXIS_LEN = 10,
        Y_AXIS_LEN = 5,
        MAIN_AXIS_LEN = 5;
    private static final int
        LOCAL_X_END = SECONDARY_AXIS_LEN - 1,
        LOCAL_Y_END = Y_AXIS_LEN - 1,
        LOCAL_Z_END = MAIN_AXIS_LEN - 1;

    public SideRoom(int i, int pieceChainLen, Random random, StructureBoundingBox blockBox, Direction direction, MineshaftVariantSettings settings) {
        super(i, pieceChainLen, settings);
        this.setCoordBaseMode(direction);
        this.boundingBox = blockBox;
    }

    public static StructureBoundingBox determineBoxPosition(List<StructureComponent> list, Random random, int x, int y, int z, Direction direction) {
        StructureBoundingBox blockBox = BoxUtil.boxFromCoordsWithRotation(x, y, z, SECONDARY_AXIS_LEN, Y_AXIS_LEN, MAIN_AXIS_LEN, direction);

        // The following func call returns null if this new blockbox does not intersect with any pieces in the list.
        // If there is an intersection, the following func call returns the piece that intersects.
        StructureComponent intersectingPiece = StructureComponent.findIntersecting(list, blockBox);

        // Thus, this function returns null if blackBox intersects with an existing piece. Otherwise, we return blackbox
        return intersectingPiece != null ? null : blockBox;
    }

    @Override
    public void buildComponent(StructureComponent structurePiece, List<StructureComponent> list, Random random) {
        // Chance of generating side room dungeon downstairs
        if (random.nextFloat() < Configuration.spawnRates.workstationDungeonSpawnRate) {
            Direction direction = this.getCoordBaseMode();
            if (direction == null) {
                return;
            }

            StructureComponent newDungeonPiece = null;
            switch (direction) {
                case NORTH:
                    newDungeonPiece = BetterMineshaftGenerator.generateAndAddSideRoomDungeonPiece(structurePiece, list, random, this.boundingBox.minX + 6, this.boundingBox.minY - 4, this.boundingBox.maxZ, this.getCoordBaseMode(), this.getComponentType(), 0);
                    break;
                case SOUTH:
                    newDungeonPiece = BetterMineshaftGenerator.generateAndAddSideRoomDungeonPiece(structurePiece, list, random, this.boundingBox.minX + 6, this.boundingBox.minY - 4, this.boundingBox.minZ, this.getCoordBaseMode(), this.getComponentType(), 0);
                    break;
                case WEST:
                    newDungeonPiece = BetterMineshaftGenerator.generateAndAddSideRoomDungeonPiece(structurePiece, list, random, this.boundingBox.maxX, this.boundingBox.minY - 4, this.boundingBox.minZ + 6, this.getCoordBaseMode(), this.getComponentType(), 0);
                    break;
                case EAST:
                    newDungeonPiece = BetterMineshaftGenerator.generateAndAddSideRoomDungeonPiece(structurePiece, list, random, this.boundingBox.minX, this.boundingBox.minY - 4, this.boundingBox.minZ + 6, this.getCoordBaseMode(), this.getComponentType(), 0);
            }

            if (newDungeonPiece != null) {
                this.hasDownstairs = true;
            }
        }
    }

    @Override
    public boolean addComponentParts(World world, Random random, StructureBoundingBox box) {
        // Don't spawn if liquid in this box or if in ocean biome
        if (this.isLiquidInStructureBoundingBox(world, box)) return false;
        if (this.isInOcean(world, 0, 0) || this.isInOcean(world, LOCAL_X_END, LOCAL_Z_END)) return false;

        // Fill with stone then clean out with air. Track ceiling positions to see where we can place iron bar supports
        this.fill(world, box, random, 0, 0, 0, LOCAL_X_END, 1, LOCAL_Z_END, getBrickSelector()); // Floor
        this.replaceNonAir(world, box, random, 0, 2, 0, LOCAL_X_END, LOCAL_Y_END - 1, LOCAL_Z_END, getBrickSelector());
        this.fill(world, box, 1, 1, 1, LOCAL_X_END - 1, LOCAL_Y_END - 1, LOCAL_Z_END, AIR);
        boolean[][] ceiling = new boolean[SECONDARY_AXIS_LEN][MAIN_AXIS_LEN];
        for (int x = 0; x <= LOCAL_X_END; ++x) {
            for (int z = 0; z <= LOCAL_Z_END; ++z) {
                BlockState currState = this.getBlockStateFromPosFixed(world, x, LOCAL_Y_END, z, box);
                if (currState != null && currState != AIR) {
                    this.setBlockState(world, getBrickSelector().get(random), x, LOCAL_Y_END, z, box);
                    ceiling[x][z] = true;
                }
            }
        }

        if (!hasDownstairs)
            generateLegs(world, box, random);

        // Furnace 1
        if (random.nextInt(2) == 0) {
            this.setBlockState(world, Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING, Direction.NORTH), 2, 1, 1, box);
            BlockPos furnacePos = new BlockPos(this.getXWithOffset(2, 1), this.getYWithOffset(1), this.getZWithOffset(2, 1));
            if (box.isVecInside(furnacePos) || world.getBlockState(furnacePos).is(Blocks.FURNACE)) {
                int coal = random.nextInt(33);
                world.withTileEntity(furnacePos, be -> {
                    if (be instanceof net.minecraft.world.level.block.entity.FurnaceBlockEntity f) f.setItem(1, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COAL, coal));
                });
            }
        }

        // Furnace 2
        if (random.nextInt(2) == 0) {
            this.setBlockState(world, Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING, Direction.NORTH), 1, 1, 1, box);
            BlockPos furnacePos = new BlockPos(this.getXWithOffset(1, 1), this.getYWithOffset(1), this.getZWithOffset(1, 1));
            if (box.isVecInside(furnacePos) || world.getBlockState(furnacePos).is(Blocks.FURNACE)) {
                int coal = random.nextInt(33);
                world.withTileEntity(furnacePos, be -> {
                    if (be instanceof net.minecraft.world.level.block.entity.FurnaceBlockEntity f) f.setItem(1, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COAL, coal));
                });
            }
        }

        // Crafting table
        this.chanceAddBlock(world, random, .5f, Blocks.CRAFTING_TABLE.defaultBlockState(), 3, 1, 1, box);

        // Chest with loot
        if (random.nextInt(4) == 0) {
            this.generateChest(world, box, random, LOCAL_X_END - 1, 1, 1, BuiltInLootTables.ABANDONED_MINESHAFT);
        }

        // Entrance to spider lair
        if (this.hasDownstairs) {
            this.setBlockState(world, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH), 6, 0, 1, box);
            this.setBlockState(world, getTrapdoor().setValue(TrapDoorBlock.FACING, Direction.NORTH), 6, 1, 1, box);
        }

        // Decorations
        generateIronBarSupports(world, box, random,ceiling);
        this.addBiomeDecorations(world, box, random, 0, 0, 0, LOCAL_X_END, LOCAL_Y_END - 1, LOCAL_Z_END);
        this.addVines(world, box, random, 1, 0, 1, LOCAL_X_END - 1, LOCAL_Y_END, LOCAL_Z_END - 1);

        return true;
    }

    private void generateLegs(World world, StructureBoundingBox box, Random random) {
        generateLeg(world, random, box, 1, 1, getBrickSelector());
        generateLeg(world, random, box, 1, LOCAL_Z_END - 1, getBrickSelector());
        generateLeg(world, random, box, LOCAL_X_END - 1, 1, getBrickSelector());
        generateLeg(world, random, box, LOCAL_X_END - 1, LOCAL_Z_END - 1, getBrickSelector());
    }

    private void generateIronBarSupports(World world, StructureBoundingBox box, Random random, boolean[][] ceiling) {
        List<Integer> invalidXs = Lists.newLinkedList(); // Prevent columns of bars from spawning adjacent to eachother
        for (int z = 2; z <= 3; z++) {
            for (int x = 2; x <= 7; x++) {
                if (invalidXs.contains(x)) continue;
                if (!ceiling[x][z]) continue;
                if (random.nextInt(5) == 0) {
                    this.fill(world, box, x, 1, z, x, 3, z, Blocks.IRON_BARS.defaultBlockState());
                    invalidXs.add(x);
                    x++;
                }
            }
        }
    }
}
