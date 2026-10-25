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
import com.yungnickyoung.minecraft.bettermineshafts.integration.Integrations;
import com.yungnickyoung.minecraft.bettermineshafts.world.generator.BetterMineshaftGenerator;
import com.yungnickyoung.minecraft.bettermineshafts.world.generator.MineshaftVariantSettings;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import com.yungnickyoung.minecraft.bettermineshafts.util.BoxUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Random;

public class SmallTunnel extends MineshaftPiece {
    private final List<Integer> supports = Lists.newLinkedList(); // local z coords
    private static final int
        SECONDARY_AXIS_LEN = 5,
        Y_AXIS_LEN = 5,
        MAIN_AXIS_LEN = 8;
    private static final int
        LOCAL_X_END = SECONDARY_AXIS_LEN - 1,
        LOCAL_Y_END = Y_AXIS_LEN - 1,
        LOCAL_Z_END = MAIN_AXIS_LEN - 1;

    public SmallTunnel(int i, int chunkPieceLen, Random random, StructureBoundingBox blockBox, Direction direction, MineshaftVariantSettings settings) {
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
        Direction direction = this.getCoordBaseMode();
        if (direction == null) {
            return;
        }

        switch (direction) {
            case NORTH:
            default:
                BetterMineshaftGenerator.generateAndAddSmallTunnelPiece(structurePiece, list, random, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ - 1, direction, this.getComponentType(), pieceChainLen);
                break;
            case SOUTH:
                BetterMineshaftGenerator.generateAndAddSmallTunnelPiece(structurePiece, list, random, this.boundingBox.maxX, this.boundingBox.minY, this.boundingBox.maxZ + 1, direction, this.getComponentType(), pieceChainLen);
                break;
            case WEST:
                BetterMineshaftGenerator.generateAndAddSmallTunnelPiece(structurePiece, list, random, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.maxZ, direction, this.getComponentType(), pieceChainLen);
                break;
            case EAST:
                BetterMineshaftGenerator.generateAndAddSmallTunnelPiece(structurePiece, list, random, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ, direction, this.getComponentType(), pieceChainLen);
        }

        buildSupports(random);
    }

    @Override
    public boolean addComponentParts(World world, Random random, StructureBoundingBox box) {
        // Don't spawn if liquid in this box or if in ocean biome
        if (this.isLiquidInStructureBoundingBox(world, box)) return false;
        if (this.isInOcean(world, 0, 0) || this.isInOcean(world, LOCAL_X_END, LOCAL_Z_END)) return false;

        // Randomize blocks
        this.chanceReplaceNonAir(world, box, random, settings.replacementRate, 0, 1, 0, LOCAL_X_END, LOCAL_Y_END, LOCAL_Z_END, getMainSelector());

        // Randomize floor
        this.chanceReplaceNonAir(world, box, random, settings.replacementRate, 0, 0, 0, LOCAL_X_END, 0, LOCAL_Z_END, getFloorSelector());

        // Fill with air
        this.fill(world, box, 1, 1, 0, LOCAL_X_END - 1, LOCAL_Y_END - 1, LOCAL_Z_END, AIR);

        // Fill in any air in floor with main block
        this.replaceAir(world, box, 1, 0, 0, LOCAL_X_END - 1, 0, LOCAL_Z_END, getMainBlock());

        // Decorations
        generateSupports(world, box, random);
        generateRails(world, box, random);
        generateCobwebs(world, box, random);
        generateChestCarts(world, box, random, BuiltInLootTables.ABANDONED_MINESHAFT);
        generateTntCarts(world, box, random);
        this.addVines(world, box, random, 1, 0, 1, LOCAL_X_END - 1, LOCAL_Y_END, LOCAL_Z_END - 1);
        this.addBiomeDecorations(world, box, random, 1, 0, 0, LOCAL_X_END - 1, LOCAL_Y_END - 1, LOCAL_Z_END);
        generateTorches(world, box, random);

        return true;
    }

    private void generateCobwebs(World world, StructureBoundingBox box, Random random) {
        supports.forEach(z -> {
            this.chanceReplaceAir(world, box, random, Configuration.spawnRates.cobwebSpawnRate, 1, 3, z - 1, 1, 3, z + 1, Blocks.COBWEB.defaultBlockState());
            this.chanceReplaceAir(world, box, random, Configuration.spawnRates.cobwebSpawnRate, 3, 3, z - 1, 3, 3, z + 1, Blocks.COBWEB.defaultBlockState());
        });
    }

    private void generateChestCarts(World world, StructureBoundingBox box, Random random, net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> lootTableId) {
        for (int z = 0; z <= LOCAL_Z_END; z++) {
            if (random.nextFloat() < Configuration.spawnRates.smallShaftChestMinecartSpawnRate) {
                BlockPos blockPos = new BlockPos(this.getXWithOffset(LOCAL_X_END / 2, z), this.getYWithOffset(1), this.getZWithOffset(LOCAL_X_END / 2, z));
                if (box.isVecInside(blockPos) && world.getBlockState(blockPos.below()) != AIR) {
                    net.minecraft.world.entity.vehicle.MinecartChest chestMinecartEntity = new net.minecraft.world.entity.vehicle.MinecartChest(world.level.getLevel(), ((float) blockPos.getX() + 0.5F), ((float) blockPos.getY() + 0.5F), ((float) blockPos.getZ() + 0.5F));
                    chestMinecartEntity.setLootTable(lootTableId, random.nextLong());
                    world.spawnEntity(chestMinecartEntity);
                }
            }
        }
    }

    private void generateSupports(World world, StructureBoundingBox box, Random random) {
        BlockState supportBlock = getSupportBlock();
        if (supportBlock.getBlock() instanceof FenceBlock) {
            supportBlock = supportBlock.setValue(FenceBlock.WEST, true).setValue(FenceBlock.EAST, true);
        } else if (supportBlock.getBlock() instanceof WallBlock) {
            supportBlock = supportBlock.setValue(WallBlock.WEST_WALL, WallSide.LOW).setValue(WallBlock.EAST_WALL, WallSide.LOW);
        }

        for (int z : this.supports) {
            this.fill(world, box, 1, 1, z, 1, 2, z, getSupportBlock());
            this.fill(world, box, 3, 1, z, 3, 2, z, getSupportBlock());
            this.fill(world, box, 1, 3, z, 3, 3, z, getMainBlock());
            this.chanceReplaceNonAir(world, box, random, .25f, 1, 3, z, 3, 3, z, supportBlock);
        }
    }

    private void generateRails(World world, StructureBoundingBox box, Random random) {
        // Place rails in center
        for (int z = 0; z <= LOCAL_Z_END; z++) {
            if (random.nextFloat() < .3f) {
                this.fill(world, box, LOCAL_X_END / 2, 1, z, LOCAL_X_END / 2, 1, z + 1, Blocks.RAIL.defaultBlockState());
                z++;
            }
        }
    }

    private void generateTntCarts(World world, StructureBoundingBox box, Random random) {
        for (int z = 0; z <= LOCAL_Z_END; z++) {
            if (random.nextFloat() < Configuration.spawnRates.smallShaftTntMinecartSpawnRate) {
                BlockPos blockPos = new BlockPos(this.getXWithOffset(LOCAL_X_END / 2, z), this.getYWithOffset(1), this.getZWithOffset(LOCAL_X_END / 2, z));
                if (box.isVecInside(blockPos) && world.getBlockState(blockPos.below()) != AIR) {
                    net.minecraft.world.entity.vehicle.MinecartTNT tntMinecartEntity = new net.minecraft.world.entity.vehicle.MinecartTNT(world.level.getLevel(), ((float) blockPos.getX() + 0.5F), ((float) blockPos.getY() + 0.5F), ((float) blockPos.getZ() + 0.5F));
                    world.spawnEntity(tntMinecartEntity);
                }
            }
        }
    }

    private void generateTorches(World world, StructureBoundingBox box, Random random) {
        BlockState leftTorch = Integrations.getLeftTorch(random);
        BlockState rightTorch = Integrations.getRightTorch(random);
        float r;
        for (int z = 0; z <= LOCAL_Z_END; z++) {
            if (this.supports.contains(z)) continue;
            r = random.nextFloat();
            if (r < Configuration.spawnRates.torchSpawnRate / 2) {
                BlockPos pos = new BlockPos(getXWithOffset(1, z), getYWithOffset(2), getZWithOffset(1, z));
                BlockPos adjPos = new BlockPos(getXWithOffset(0, z), getYWithOffset(2), getZWithOffset(0, z));
                boolean canPlace = leftTorch != null && Integrations.canPlaceBlockAt(leftTorch, world, pos) && world.getBlockState(adjPos) != AIR;
                if (canPlace) {
                    this.replaceAir(world, box, 1, 2, z, 1, 2, z, leftTorch);
                    if (Integrations.VARIED_COMMODITIES.isTorchVariedCommoditiesCandle(leftTorch)) {
                        Direction direction = this.getCoordBaseMode();
                        if (direction == null) direction = Direction.NORTH;
                        int rotation;
                        switch (direction) {
                            case EAST:
                            case WEST:
                                rotation = 4;
                                break;
                            default:
                            case NORTH:
                            case SOUTH:
                                rotation = 2;
                                break;
                        }
                        Integrations.VARIED_COMMODITIES.spawnCandleTorchEntity(world, getXWithOffset(1, z), getYWithOffset(2), getZWithOffset(1, z), rotation);
                    }
                }
            } else if (r < Configuration.spawnRates.torchSpawnRate) {
                BlockPos pos = new BlockPos(getXWithOffset(LOCAL_X_END - 1, z), getYWithOffset(2), getZWithOffset(LOCAL_X_END - 1, z));
                BlockPos adjPos = new BlockPos(getXWithOffset(LOCAL_X_END , z), getYWithOffset(2), getZWithOffset(LOCAL_X_END, z));
                boolean canPlace = rightTorch != null && Integrations.canPlaceBlockAt(rightTorch, world, pos) && world.getBlockState(adjPos) != AIR;
                if (canPlace) {
                    this.replaceAir(world, box, LOCAL_X_END - 1, 2, z, LOCAL_X_END - 1, 2, z, rightTorch);
                    if (Integrations.VARIED_COMMODITIES.isTorchVariedCommoditiesCandle(rightTorch)) {
                        Direction direction = this.getCoordBaseMode();
                        if (direction == null) direction = Direction.NORTH;
                        int rotation;
                        switch (direction) {
                            case EAST:
                            case WEST:
                                rotation = 0;
                                break;
                            default:
                            case NORTH:
                            case SOUTH:
                                rotation = 6;
                                break;
                        }
                        Integrations.VARIED_COMMODITIES.spawnCandleTorchEntity(world, getXWithOffset(LOCAL_X_END - 1, z), getYWithOffset(2), getZWithOffset(LOCAL_X_END - 1 , z), rotation);
                    }
                }
            }
        }
    }

    private void buildSupports(Random random) {
        for (int z = 0; z <= LOCAL_Z_END; z++) {
            int r = random.nextInt(7);
            if (r == 0) { // Big support
                supports.add(z);
                z += 5;
            }
        }
    }
}
