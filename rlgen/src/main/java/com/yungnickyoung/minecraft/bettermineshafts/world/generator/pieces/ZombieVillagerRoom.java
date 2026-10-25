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

import com.yungnickyoung.minecraft.bettermineshafts.util.BoxUtil;
import com.yungnickyoung.minecraft.bettermineshafts.world.generator.MineshaftVariantSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Random;

public class ZombieVillagerRoom extends MineshaftPiece {
    private static final int
        SECONDARY_AXIS_LEN = 7,
        Y_AXIS_LEN = 5,
        MAIN_AXIS_LEN = 7;
    private static final int
        LOCAL_X_END = SECONDARY_AXIS_LEN - 1,
        LOCAL_Y_END = Y_AXIS_LEN - 1,
        LOCAL_Z_END = MAIN_AXIS_LEN - 1;

    public ZombieVillagerRoom(int i, int chunkPieceLen, Random random, StructureBoundingBox blockBox, Direction direction, MineshaftVariantSettings settings) {
        super(i, chunkPieceLen, settings);
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
    }

    @Override
    public boolean addComponentParts(World world, Random random, StructureBoundingBox box) {
        // Don't spawn if liquid in this box or if in ocean biome
        if (this.isLiquidInStructureBoundingBox(world, box)) return false;
        if (this.isInOcean(world, 0, 0) || this.isInOcean(world, LOCAL_X_END, LOCAL_Z_END)) return false;

        // Outermost walls
        this.fill(world, box, 1, 0, 0, 5, 2, 0, Blocks.STONE.defaultBlockState());
        this.fill(world, box, 0, 0, 1, 0, 2, 5, Blocks.STONE.defaultBlockState());
        this.fill(world, box, LOCAL_X_END, 0, 1, LOCAL_X_END, 2, 5, Blocks.STONE.defaultBlockState());
        this.fill(world, box, 1, 0, LOCAL_Z_END, 5, 2, LOCAL_Z_END, Blocks.STONE.defaultBlockState());
        // Randomize
        // Cobble
        this.chanceReplaceNonAir(world, box, random, .4f, 1, 0, 0, 5, 2, 0, Blocks.COBBLESTONE.defaultBlockState());
        this.chanceReplaceNonAir(world, box, random, .4f, 0, 0, 1, 0, 2, 5, Blocks.COBBLESTONE.defaultBlockState());
        this.chanceReplaceNonAir(world, box, random, .4f, LOCAL_X_END, 0, 1, LOCAL_X_END, 2, 5, Blocks.COBBLESTONE.defaultBlockState());
        this.chanceReplaceNonAir(world, box, random, .4f, 1, 0, LOCAL_Z_END, 5, 2, LOCAL_Z_END, Blocks.COBBLESTONE.defaultBlockState());
        // Stone brick
        this.chanceReplaceNonAir(world, box, random, .1f, 1, 0, 0, 5, 2, 0, Blocks.STONE_BRICKS.defaultBlockState());
        this.chanceReplaceNonAir(world, box, random, .1f, 0, 0, 1, 0, 2, 5, Blocks.STONE_BRICKS.defaultBlockState());
        this.chanceReplaceNonAir(world, box, random, .1f, LOCAL_X_END, 0, 1, LOCAL_X_END, 2, 5, Blocks.STONE_BRICKS.defaultBlockState());
        this.chanceReplaceNonAir(world, box, random, .1f, 1, 0, LOCAL_Z_END, 5, 2, LOCAL_Z_END, Blocks.STONE_BRICKS.defaultBlockState());

        // Slabs on top of outermost walls
        this.fill(world, box, 2, 3, 0, 4, 3, 0, Blocks.STONE_SLAB.defaultBlockState());
        this.fill(world, box, 0, 3, 2, 0, 3, 4, Blocks.STONE_SLAB.defaultBlockState());
        this.fill(world, box, LOCAL_X_END, 3, 2, LOCAL_X_END, 3, 4, Blocks.STONE_SLAB.defaultBlockState());
        this.fill(world, box, 2, 3, LOCAL_Z_END, 4, 3, LOCAL_Z_END, Blocks.STONE_SLAB.defaultBlockState());
        // Randomize
        this.chanceFill(world, box, random, .5f, 2, 3, 0, 4, 3, 0, Blocks.COBBLESTONE_SLAB.defaultBlockState());
        this.chanceFill(world, box, random, .5f, 0, 3, 2, 0, 3, 4, Blocks.COBBLESTONE_SLAB.defaultBlockState());
        this.chanceFill(world, box, random, .5f, LOCAL_X_END, 3, 2, LOCAL_X_END, 3, 4, Blocks.COBBLESTONE_SLAB.defaultBlockState());
        this.chanceFill(world, box, random, .5f, 2, 3, LOCAL_Z_END, 4, 3, LOCAL_Z_END, Blocks.COBBLESTONE_SLAB.defaultBlockState());

        // Second wall/ceiling layer, formed with upside-down stairs
        // Cardinal directions
        this.fill(world, box, 2, 3, 1, 4, 3, 1, Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH).setValue(StairBlock.HALF, Half.TOP));
        this.fill(world, box, 1, 3, 2, 1, 3, 4, Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST).setValue(StairBlock.HALF, Half.TOP));
        this.fill(world, box, 2, 3, 5, 4, 3, 5, Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH).setValue(StairBlock.HALF, Half.TOP));
        this.fill(world, box, 5, 3, 2, 5, 3, 4, Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST).setValue(StairBlock.HALF, Half.TOP));
        // Corners
        this.setBlockState(world, Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.SHAPE, StairsShape.INNER_RIGHT).setValue(StairBlock.HALF, Half.TOP).setValue(StairBlock.FACING, Direction.SOUTH), 1, 3, 1, box);
        this.setBlockState(world, Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.SHAPE, StairsShape.INNER_LEFT).setValue(StairBlock.HALF, Half.TOP).setValue(StairBlock.FACING, Direction.NORTH), 1, 3, 5, box);
        this.setBlockState(world, Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.SHAPE, StairsShape.INNER_LEFT).setValue(StairBlock.HALF, Half.TOP).setValue(StairBlock.FACING, Direction.SOUTH), 5, 3, 1, box);
        this.setBlockState(world, Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.SHAPE, StairsShape.INNER_RIGHT).setValue(StairBlock.HALF, Half.TOP).setValue(StairBlock.FACING, Direction.NORTH), 5, 3, 5, box);

        // Third ceiling layer, formed with bottom-half slabs
        this.fill(world, box, 2, 4, 2, 4, 4, 4, Blocks.STONE_SLAB.defaultBlockState());
        this.chanceFill(world, box, random, .5f, 2, 4, 2, 4, 4, 4, Blocks.COBBLESTONE_SLAB.defaultBlockState());
        this.setBlockState(world, AIR, 3, 4, 3, box);

        // Top middle roof block
        this.setBlockState(world, Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP), 3, 4,3, box);

        // Floor
        this.fill(world, box, 1, 0, 1, 5, 0, 5, Blocks.STONE.defaultBlockState());
        // Randomize
        this.chanceFill(world, box, random, .4f, 1, 0, 1, 5, 0, 5, Blocks.COBBLESTONE.defaultBlockState());
        this.chanceFill(world, box, random, .1f, 1, 0, 1, 5, 0, 5, Blocks.STONE_BRICKS.defaultBlockState());

        // Fill with air
        this.fill(world, box, 1, 1, 1, 5, 2, 5, AIR);
        this.fill(world, box, 2, 3, 2, 4, 3, 4, AIR);

        // Place door
        this.fill(world, box, 3, 1, 0, 3, 2, 0, AIR);
        this.setBlockState(world, Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH).setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), 3, 1, 0, box);
        this.setBlockState(world, Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH).setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), 3, 2, 0, box);

        // Window
        this.fill(world, box, 6, 2, 2, 6, 2, 4, Blocks.IRON_BARS.defaultBlockState());

        // Beds
        this.setBlockState(world, Blocks.BLACK_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.NORTH).setValue(BedBlock.PART, BedPart.FOOT), 1, 1, 4, box);
        this.setBlockState(world, Blocks.BLACK_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.NORTH).setValue(BedBlock.PART, BedPart.HEAD), 1, 1, 5, box);

        this.setBlockState(world, Blocks.BLACK_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.NORTH).setValue(BedBlock.PART, BedPart.FOOT), 5, 1, 4, box);
        this.setBlockState(world, Blocks.BLACK_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.NORTH).setValue(BedBlock.PART, BedPart.HEAD), 5, 1, 5, box);

        // Mob spawner
        BlockPos spawnerPos = new BlockPos(this.getXWithOffset(3,3), this.getYWithOffset(0), this.getZWithOffset(3, 3));
        world.setBlockState(spawnerPos, Blocks.SPAWNER.defaultBlockState(), 2);
        world.withTileEntity(spawnerPos, be -> {
            if (be instanceof net.minecraft.world.level.block.entity.SpawnerBlockEntity sp) sp.setEntityId(net.minecraft.world.entity.EntityType.ZOMBIE_VILLAGER, net.minecraft.util.RandomSource.create(0L));
        });

        // Wall with redstone torch in corner
        this.setBlockState(world, Blocks.COBBLESTONE_WALL.defaultBlockState(), 1, 1, 1, box);
        this.setBlockState(world, Blocks.REDSTONE_TORCH.defaultBlockState(), 1, 2, 1, box);

        // Chest
        this.generateChest(world, box, random, 5, 1, 1, BuiltInLootTables.ABANDONED_MINESHAFT);

        // Button for door (inside)
        this.setBlockState(world, Blocks.STONE_BUTTON.defaultBlockState(), 2, 2, 1, box);

        // Anvil
        this.setBlockState(world, Blocks.ANVIL.defaultBlockState(), 5, 1, 2, box);

        // Decoration block (smithing table, crafting table, blast furnace)
        if (random.nextFloat() < .33f)
            this.setBlockState(world, Blocks.BOOKSHELF.defaultBlockState(), 2, 1, 5, box);
        else if (random.nextFloat() < .67f)
            this.setBlockState(world, Blocks.CRAFTING_TABLE.defaultBlockState(), 2, 1, 5, box);
        else
            this.setBlockState(world, Blocks.FURNACE.defaultBlockState(), 2, 1, 5, box);

        // Cobwebs
        this.chanceFill(world, box, random, .3f, 2, 3, 2, 4, 3, 4, Blocks.COBWEB.defaultBlockState());

        return true;
    }
}
