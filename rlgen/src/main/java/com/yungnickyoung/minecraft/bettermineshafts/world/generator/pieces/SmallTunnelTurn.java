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

import com.yungnickyoung.minecraft.bettermineshafts.world.generator.BetterMineshaftGenerator;
import com.yungnickyoung.minecraft.bettermineshafts.util.BoxUtil;
import com.yungnickyoung.minecraft.bettermineshafts.world.generator.MineshaftVariantSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.Direction;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SmallTunnelTurn extends MineshaftPiece {
    public enum TurnDirection {
        LEFT(0), RIGHT(1);

        private final int value;

        TurnDirection(int value) {
            this.value = value;
        }

        public static TurnDirection valueOf(int value) {
            return Arrays.stream(values())
                .filter(dir -> dir.value == value)
                .findFirst().get();
        }
    }

    private TurnDirection turnDirection;
    private static final int
        SECONDARY_AXIS_LEN = 5,
        Y_AXIS_LEN = 5,
        MAIN_AXIS_LEN = 5;
    private static final int
        LOCAL_X_END = SECONDARY_AXIS_LEN - 1,
        LOCAL_Y_END = Y_AXIS_LEN - 1,
        LOCAL_Z_END = MAIN_AXIS_LEN - 1;

    public SmallTunnelTurn(int i, int chunkPieceLen, Random random, StructureBoundingBox blockBox, Direction direction, MineshaftVariantSettings settings) {
        super(i, chunkPieceLen, settings);
        this.setCoordBaseMode(direction);
        this.turnDirection = random.nextBoolean() ? TurnDirection.LEFT : TurnDirection.RIGHT;
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
        Direction direction = this.getCoordBaseMode();
        if (direction == null) {
            return;
        }

        Direction nextDirection = this.turnDirection == TurnDirection.LEFT ? direction.getCounterClockWise() : direction.getClockWise();
        switch (nextDirection) {
            case NORTH:
            default:
                BetterMineshaftGenerator.generateAndAddSmallTunnelPiece(structurePiece, list, random, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ - 1, nextDirection, this.getComponentType(), pieceChainLen);
                break;
            case SOUTH:
                BetterMineshaftGenerator.generateAndAddSmallTunnelPiece(structurePiece, list, random, this.boundingBox.maxX, this.boundingBox.minY, this.boundingBox.maxZ + 1, nextDirection, this.getComponentType(), pieceChainLen);
                break;
            case WEST:
                BetterMineshaftGenerator.generateAndAddSmallTunnelPiece(structurePiece, list, random, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.maxZ, nextDirection, this.getComponentType(), pieceChainLen);
                break;
            case EAST:
                BetterMineshaftGenerator.generateAndAddSmallTunnelPiece(structurePiece, list, random, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ, nextDirection, this.getComponentType(), pieceChainLen);
        }
    }

    @Override
    public boolean addComponentParts(World world, Random random, StructureBoundingBox box) {
        // Don't spawn if liquid in this box or if in ocean biome
        if (this.isLiquidInStructureBoundingBox(world, box)) return false;
        if (this.isInOcean(world, 0, 0) || this.isInOcean(world, LOCAL_X_END, LOCAL_Z_END)) return false;

        Direction direction = this.getCoordBaseMode();

        // Randomize blocks
        this.chanceReplaceNonAir(world, box, random, settings.replacementRate, 0, 1, 0, LOCAL_X_END, LOCAL_Y_END, LOCAL_Z_END, getMainSelector());

        // Randomize floor
        this.chanceReplaceNonAir(world, box, random, settings.replacementRate, 0, 0, 0, LOCAL_X_END, 0, LOCAL_Z_END, getFloorSelector());

        // Fill with air
        this.fill(world, box, 1, 1, 0, LOCAL_X_END - 1, LOCAL_Y_END - 1, LOCAL_Z_END - 1, AIR);

        // Fill in any air in floor with main block
        this.replaceAir(world, box, 1, 0, 0, LOCAL_X_END - 1, 0, LOCAL_Z_END, getMainBlock());

        // Rails
        this.fill(world, box, 2, 1, 0, 2, 1, 1, Blocks.RAIL.defaultBlockState());

        if (this.turnDirection == TurnDirection.LEFT) {
            if (direction == Direction.NORTH || direction == Direction.EAST) {
                generateLeftTurn(world, box);
            }
            else {
                generateRightTurn(world, box);
            }
        }
        else {
            if (direction == Direction.NORTH || direction == Direction.EAST) {
                generateRightTurn(world, box);
            }
            else {
                generateLeftTurn(world, box);
            }
        }

        // Decorations
        this.addBiomeDecorations(world, box, random, 0, 0, 0, LOCAL_X_END, LOCAL_Y_END - 1, LOCAL_Z_END);
        this.addVines(world, box, random, 1, 0, 1, LOCAL_X_END - 1, LOCAL_Y_END, LOCAL_Z_END - 1);

        return true;
    }

    private void generateLeftTurn(World world, StructureBoundingBox box) {
        this.fill(world, box, 0, 1, 1, 0, LOCAL_Y_END - 1, LOCAL_Z_END - 1, AIR);
        this.fill(world, box, 0, 0, 0, 0, 0, LOCAL_Z_END - 1, getMainBlock());
        this.fill(world, box, 2, 1, 2, 2, 1, 2, Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.SOUTH_WEST));
        this.fill(world, box, 0, 1, 2, 1, 1, 2, Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.EAST_WEST));
    }

    private void generateRightTurn(World world, StructureBoundingBox box) {
        this.fill(world, box, LOCAL_X_END, 1, 1, LOCAL_X_END, LOCAL_Y_END - 1, LOCAL_Z_END - 1, AIR);
        this.fill(world, box, LOCAL_X_END, 0, 0, LOCAL_X_END, 0, LOCAL_Z_END - 1, getMainBlock());
        this.fill(world, box, 2, 1, 2, 2, 1, 2, Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.SOUTH_EAST));
        this.fill(world, box, LOCAL_X_END - 1, 1, 2, LOCAL_X_END, 1, 2, Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.EAST_WEST));
    }
}
