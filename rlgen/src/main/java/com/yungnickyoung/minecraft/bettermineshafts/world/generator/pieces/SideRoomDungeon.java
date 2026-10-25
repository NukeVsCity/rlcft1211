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

import com.yungnickyoung.minecraft.bettermineshafts.world.MapGenBetterMineshaft;
import com.yungnickyoung.minecraft.bettermineshafts.world.generator.MineshaftVariantSettings;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Random;

public class SideRoomDungeon extends MineshaftPiece {
    private static final int
        SECONDARY_AXIS_LEN = 9,
        Y_AXIS_LEN = 4,
        MAIN_AXIS_LEN = 9;
    private static final int
        LOCAL_X_END = SECONDARY_AXIS_LEN - 1,
        LOCAL_Y_END = Y_AXIS_LEN - 1,
        LOCAL_Z_END = MAIN_AXIS_LEN - 1;

    public SideRoomDungeon(int i, int pieceChainLen, Random random, StructureBoundingBox blockBox, Direction direction, MineshaftVariantSettings settings) {
        super(i, pieceChainLen, settings);
        this.setCoordBaseMode(direction);
        this.boundingBox = blockBox;
    }

    public static StructureBoundingBox determineBoxPosition(List<StructureComponent> list, Random random, int x, int y, int z, Direction direction) {
        StructureBoundingBox blockBox = new StructureBoundingBox(x, y, z, x, y + Y_AXIS_LEN - 1, z);

        switch (direction) {
            case NORTH:
            default:
                blockBox.maxX = x + 4;
                blockBox.minX = x - 4;
                blockBox.minZ = z - (MAIN_AXIS_LEN - 1);
                break;
            case SOUTH:
                blockBox.maxX = x + 4;
                blockBox.minX = x - 4;
                blockBox.maxZ = z + (MAIN_AXIS_LEN - 1);
                break;
            case WEST:
                blockBox.minX = x - (MAIN_AXIS_LEN - 1);
                blockBox.maxZ = z + 4;
                blockBox.minZ = z - 4;
                break;
            case EAST:
                blockBox.maxX = x + (MAIN_AXIS_LEN - 1);
                blockBox.maxZ = z + 4;
                blockBox.minZ = z - 4;
        }

        // The following func call returns null if this new blockbox does not intersect with any pieces in the list.
        // If there is an intersection, the following func call returns the piece that intersects.
        StructureComponent intersectingPiece = StructureComponent.findIntersecting(list, blockBox);

        // Thus, this function returns null if blackBox intersects with an existing piece. Otherwise, we return blackbox
        return intersectingPiece != null ? null : blockBox;
    }

    @Override
    public void buildComponent(StructureComponent structurePiece, List<StructureComponent> list, Random random) {
    }

    @Override
    public boolean addComponentParts(World world, Random random, StructureBoundingBox box) {
        // Don't spawn if liquid in this box or if in ocean biome
        if (this.isLiquidInStructureBoundingBox(world, box)) return false;
        if (this.isInOcean(world, 0, 0) || this.isInOcean(world, LOCAL_X_END, LOCAL_Z_END)) return false;

         // Fill with stone then clean out with air
        this.fill(world, box, random, 0, 0, 0, LOCAL_X_END, LOCAL_Y_END, LOCAL_Z_END, getBrickSelector());
        this.fill(world, box, 1, 1, 1, LOCAL_X_END - 1, LOCAL_Y_END - 1, LOCAL_Z_END - 1, AIR);

        generateLegs(world, box, random);

        // Ladders
        BlockState LADDER = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH);
        this.fill(world, box, 4, 1, 1, 4, 3, 1, LADDER);

        // Spawner
        BlockPos spawnerPos = new BlockPos(this.getXWithOffset(4,5), this.getYWithOffset(1), this.getZWithOffset(4, 5));
        world.setBlockState(spawnerPos, Blocks.SPAWNER.defaultBlockState(), 2);
        world.withTileEntity(spawnerPos, be -> {
            if (be instanceof net.minecraft.world.level.block.entity.SpawnerBlockEntity sp) sp.setEntityId(net.minecraft.world.entity.EntityType.CAVE_SPIDER, net.minecraft.util.RandomSource.create(0L));
        });

        // Cobwebs immediately surrounding chests
        this.chanceReplaceAir(world, box, random, .9f, 3, 1, 4, 5, 2, 6, Blocks.COBWEB.defaultBlockState());

        // Fill room randomly with cobwebs
        this.chanceReplaceAir(world, box, random, .1f, 1, 1, 1, LOCAL_X_END - 1, 2, LOCAL_Z_END, Blocks.COBWEB.defaultBlockState());

        // Chests
        this.generateChest(world, box, random, 1, 1, LOCAL_Z_END - 1, BuiltInLootTables.ABANDONED_MINESHAFT);
        if (random.nextInt(2) == 0) { // Chance of second chest
            this.generateChest(world, box, random, LOCAL_X_END - 1, 1, LOCAL_Z_END - 1, BuiltInLootTables.ABANDONED_MINESHAFT);
        }

        // Decorations
        this.addBiomeDecorations(world, box, random, 0, 0, 0, LOCAL_X_END, LOCAL_Y_END - 1, LOCAL_Z_END);

        return true;
    }

    private void generateLegs(World world, StructureBoundingBox box, Random random) {
        generateLeg(world, random, box, 1, 1, getBrickSelector());
        generateLeg(world, random, box, 1, LOCAL_Z_END - 1, getBrickSelector());
        generateLeg(world, random, box, LOCAL_X_END - 1, 1, getBrickSelector());
        generateLeg(world, random, box, LOCAL_X_END - 1, LOCAL_Z_END - 1, getBrickSelector());
    }
}
