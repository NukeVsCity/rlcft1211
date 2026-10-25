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
import com.yungnickyoung.minecraft.bettermineshafts.util.Pair;
import com.yungnickyoung.minecraft.bettermineshafts.world.generator.BetterMineshaftGenerator;
import com.yungnickyoung.minecraft.bettermineshafts.util.BoxUtil;
import com.yungnickyoung.minecraft.bettermineshafts.world.generator.MineshaftVariantSettings;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Random;

public class BigTunnel extends MineshaftPiece {
    private final List<BlockPos> smallShaftLeftEntrances = Lists.newLinkedList();
    private final List<BlockPos> smallShaftRightEntrances = Lists.newLinkedList();
    private final List<StructureBoundingBox> sideRoomEntrances = Lists.newLinkedList();
    private final List<Integer> bigSupports = Lists.newLinkedList(); // local z coords
    private final List<Integer> smallSupports = Lists.newLinkedList(); // local z coords
    private final List<Pair<Integer, Integer>> gravelDeposits = Lists.newLinkedList(); // Pair<z coordinate, side> where side 0 = left, 1 = right
    private static final int
        SECONDARY_AXIS_LEN = 9,
        Y_AXIS_LEN = 8,
        MAIN_AXIS_LEN = 24;
    private static final int
        LOCAL_X_END = SECONDARY_AXIS_LEN - 1,
        LOCAL_Y_END = Y_AXIS_LEN - 1,
        LOCAL_Z_END = MAIN_AXIS_LEN - 1;

    public BigTunnel(int i, int pieceChainLen, Random random, StructureBoundingBox blockBox, Direction direction, MineshaftVariantSettings settings) {
        super(i, pieceChainLen, settings);
        this.setCoordBaseMode(direction);
        this.boundingBox = blockBox;
    }

    public static StructureBoundingBox determineBoxPosition(int x, int y, int z, Direction direction) {
        return BoxUtil.boxFromCoordsWithRotation(x, y, z, SECONDARY_AXIS_LEN, Y_AXIS_LEN, MAIN_AXIS_LEN, direction);
    }

    @Override
    public void buildComponent(StructureComponent structurePiece, List<StructureComponent> list, Random random) {
        Direction direction = this.getCoordBaseMode();
        if (direction == null) {
            return;
        }

        // Extend big tunnel in same direction
        switch (direction) {
            case NORTH:
            default:
                BetterMineshaftGenerator.generateAndAddBigTunnelPiece(structurePiece, list, random, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ - 1, direction, this.getComponentType(), pieceChainLen);
                break;
            case SOUTH:
                BetterMineshaftGenerator.generateAndAddBigTunnelPiece(structurePiece, list, random, this.boundingBox.maxX, this.boundingBox.minY, this.boundingBox.maxZ + 1, direction, this.getComponentType(), pieceChainLen);
                break;
            case WEST:
                BetterMineshaftGenerator.generateAndAddBigTunnelPiece(structurePiece, list, random, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.maxZ, direction, this.getComponentType(), pieceChainLen);
                break;
            case EAST:
                BetterMineshaftGenerator.generateAndAddBigTunnelPiece(structurePiece, list, random, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ, direction, this.getComponentType(), pieceChainLen);
        }

        // Get the length of the main axis. This SHOULD be equal to the MAIN_AXIS_LEN variable.
        int pieceLen = this.getCoordBaseMode().getAxis() == Direction.Axis.Z ? this.boundingBox.getZSize() : this.boundingBox.getXSize();

        // Build side rooms
        buildSideRoomsLeft(structurePiece, list, random, direction, pieceLen);
        buildSideRoomsRight(structurePiece, list, random, direction, pieceLen);

        // Build small shafts and mark their entrances
        buildSmallShaftsLeft(structurePiece, list, random, direction, pieceLen);
        buildSmallShaftsRight(structurePiece, list, random, direction, pieceLen);

        // Decorations
        buildSupports(random);
        buildGravelDeposits(random);
    }

    @Override
    public boolean addComponentParts(World world, Random random, StructureBoundingBox box) {
        // Don't spawn if in ocean biome
        if (this.isInOcean(world, 0, 0) || this.isInOcean(world, LOCAL_X_END, LOCAL_Z_END)) return false;

        // Randomize blocks
        this.chanceReplaceNonAir(world, box, random, settings.replacementRate, 0, 1, 0, LOCAL_X_END, LOCAL_Y_END, LOCAL_Z_END, getMainSelector());

        // Randomize floor
        this.chanceReplaceNonAir(world, box, random, settings.replacementRate, 0, 0, 0, LOCAL_X_END, 0, LOCAL_Z_END, getFloorSelector());

        // Fill with air
        this.fill(world, box, 1, 1, 0, LOCAL_X_END - 1, LOCAL_Y_END - 3, LOCAL_Z_END, AIR);
        this.fill(world, box, 2, LOCAL_Y_END - 3, 0, LOCAL_X_END - 2, LOCAL_Y_END - 2, LOCAL_Z_END, AIR);
        this.fill(world, box, 3, LOCAL_Y_END - 1, 0, LOCAL_X_END - 3, LOCAL_Y_END - 1, LOCAL_Z_END, AIR);

        // Fill in any air in floor with main block
        this.replaceAir(world, box, 1, 0, 0, LOCAL_X_END - 1, 0, LOCAL_Z_END, getMainBlock());

        // Core structure
        generateSmallShaftEntrances(world, box, random);
        generateSideRoomOpenings(world, box, random);
        generateLegs(world, box, random);
        generateBigSupports(world, box, random);
        generateSmallSupports(world, box, random);

        // Decorations
        generateRails(world, box, random);
        generateChestCarts(world, box, random, BuiltInLootTables.ABANDONED_MINESHAFT);
        generateTntCarts(world, box, random);
        generateGravelDeposits(world, box, random);
        this.addBiomeDecorations(world, box, random, 1, 0, 0, LOCAL_X_END - 1, LOCAL_Y_END - 1, LOCAL_Z_END);
        this.addVines(world, box, random, 1, 0, 1, LOCAL_X_END - 1, LOCAL_Y_END, LOCAL_Z_END - 1);
        generateLanterns(world, box, random);
        generateCobwebs(world, box, random);

        return true;
    }

    private void generateLegs(World world, StructureBoundingBox box, Random random) {
        // Ice and mushroom biome variants have different legs
        if (settings.legVariant == 1) {
            generateLegsVariant1(world, box, random);
        } else {
            generateLegsVariant2(world, box, random);
        }
    }

    private void generateLegsVariant1(World world, StructureBoundingBox box, Random random) {
        BlockState supportBlock = getSupportBlock();
        if (supportBlock.getBlock() instanceof FenceBlock) {
            supportBlock = supportBlock.setValue(FenceBlock.NORTH, true).setValue(FenceBlock.SOUTH, true);
        } else if (supportBlock.getBlock() instanceof WallBlock) {
            supportBlock = supportBlock.setValue(WallBlock.NORTH_WALL, WallSide.LOW).setValue(WallBlock.SOUTH_WALL, WallSide.LOW);
        }

        // Left side
        generateLeg(world, random, box, 1, 0, getLegSelector());
        this.replaceAir(world, box, 1, -1, 1, 1, -1, 5, supportBlock);
        this.replaceAir(world, box, 1, -2, 1, 1, -2, 3, supportBlock);
        this.replaceAir(world, box, 1, -3, 1, 1, -3, 2, supportBlock);
        this.replaceAir(world, box, 1, -5, 1, 1, -4, 1, supportBlock);
        this.replaceAir(world, box, 1, -1, 6, 1, -1, 10, supportBlock);
        this.replaceAir(world, box, 1, -2, 8, 1, -2, 10, supportBlock);
        this.replaceAir(world, box, 1, -3, 9, 1, -3, 10, supportBlock);
        this.replaceAir(world, box, 1, -5, 10, 1, -4, 10, supportBlock);
        generateLeg(world, random, box, 1, 11, getLegSelector());
        generateLeg(world, random, box, 1, 12, getLegSelector());
        this.replaceAir(world, box, 1, -1, 13, 1, -1, 17, supportBlock);
        this.replaceAir(world, box, 1, -2, 13, 1, -2, 15, supportBlock);
        this.replaceAir(world, box, 1, -3, 13, 1, -3, 14, supportBlock);
        this.replaceAir(world, box, 1, -5, 13, 1, -4, 13, supportBlock);
        this.replaceAir(world, box, 1, -1, 18, 1, -1, 22, supportBlock);
        this.replaceAir(world, box, 1, -2, 20, 1, -2, 22, supportBlock);
        this.replaceAir(world, box, 1, -3, 21, 1, -3, 22, supportBlock);
        this.replaceAir(world, box, 1, -5, 22, 1, -4, 22, supportBlock);
        generateLeg(world, random, box, 1, LOCAL_Z_END, getLegSelector());

        // Right side
        generateLeg(world, random, box, LOCAL_X_END - 1, 0, getLegSelector());
        this.replaceAir(world, box, LOCAL_X_END - 1, -1, 1, LOCAL_X_END - 1, -1, 5, supportBlock);
        this.replaceAir(world, box, LOCAL_X_END - 1, -2, 1, LOCAL_X_END - 1, -2, 3, supportBlock);
        this.replaceAir(world, box, LOCAL_X_END - 1, -3, 1, LOCAL_X_END - 1, -3, 2, supportBlock);
        this.replaceAir(world, box, LOCAL_X_END - 1, -5, 1, LOCAL_X_END - 1, -4, 1, supportBlock);
        this.replaceAir(world, box, LOCAL_X_END - 1, -1, 6, LOCAL_X_END - 1, -1, 10, supportBlock);
        this.replaceAir(world, box, LOCAL_X_END - 1, -2, 8, LOCAL_X_END - 1, -2, 10, supportBlock);
        this.replaceAir(world, box, LOCAL_X_END - 1, -3, 9, LOCAL_X_END - 1, -3, 10, supportBlock);
        this.replaceAir(world, box, LOCAL_X_END - 1, -5, 10, LOCAL_X_END - 1, -4, 10, supportBlock);
        generateLeg(world, random, box, LOCAL_X_END - 1, 11, getLegSelector());
        generateLeg(world, random, box, LOCAL_X_END - 1, 12, getLegSelector());
        this.replaceAir(world, box, LOCAL_X_END - 1, -1, 13, LOCAL_X_END - 1, -1, 17, supportBlock);
        this.replaceAir(world, box, LOCAL_X_END - 1, -2, 13, LOCAL_X_END - 1, -2, 15, supportBlock);
        this.replaceAir(world, box, LOCAL_X_END - 1, -3, 13, LOCAL_X_END - 1, -3, 14, supportBlock);
        this.replaceAir(world, box, LOCAL_X_END - 1, -5, 13, LOCAL_X_END - 1, -4, 13, supportBlock);
        this.replaceAir(world, box, LOCAL_X_END - 1, -1, 18, LOCAL_X_END - 1, -1, 22, supportBlock);
        this.replaceAir(world, box, LOCAL_X_END - 1, -2, 20, LOCAL_X_END - 1, -2, 22, supportBlock);
        this.replaceAir(world, box, LOCAL_X_END - 1, -3, 21, LOCAL_X_END - 1, -3, 22, supportBlock);
        this.replaceAir(world, box, LOCAL_X_END - 1, -5, 22, LOCAL_X_END - 1, -4, 22, supportBlock);
        generateLeg(world, random, box, LOCAL_X_END - 1, LOCAL_Z_END, getLegSelector());
    }

    private void generateLegsVariant2(World world, StructureBoundingBox box, Random random) {
        for (int z = 0; z <= LOCAL_Z_END; z += 7) {
            generateLeg(world, random, box, 2, z + 1, getLegSelector());
            generateLeg(world, random, box, LOCAL_X_END - 2, z + 1, getLegSelector());

            this.replaceAir(world, box, random, 1, -1, z, LOCAL_X_END - 1, -1, z + 2, getLegSelector());

            this.replaceAir(world, box, random, 2, -1, z + 3, 2, -1, z + 3, getLegSelector());
            this.replaceAir(world, box, random, LOCAL_X_END - 2, -1, z + 3, LOCAL_X_END - 2, -1, z + 3, getLegSelector());

            this.replaceAir(world, box, random, 3, -1, z + 3, LOCAL_X_END - 3, -1, z + 6, getLegSelector());

            this.replaceAir(world, box, random, 2, -1, z + 6, 2, -1, z + 6, getLegSelector());
            this.replaceAir(world, box, random, LOCAL_X_END - 2, -1, z + 6, LOCAL_X_END - 2, -1, z + 6, getLegSelector());

            this.replaceAir(world, box, random, 2, -2, z, 2, -2, z, getLegSelector());
            this.replaceAir(world, box, random, LOCAL_X_END - 2, -2, z, LOCAL_X_END - 2, -2, z, getLegSelector());

            this.replaceAir(world, box, random, 2, -2, z + 2, 2, -2, z + 2, getLegSelector());
            this.replaceAir(world, box, random, LOCAL_X_END - 2, -2, z + 2, LOCAL_X_END - 2, -2, z + 2, getLegSelector());

            this.replaceAir(world, box, random, 1, -2, z + 1, 1, -2, z + 1, getLegSelector());
            this.replaceAir(world, box, random, LOCAL_X_END - 1, -2, z + 1, LOCAL_X_END - 1, -2, z + 1, getLegSelector());

            this.replaceAir(world, box, random, 3, -2, z + 1, 3, -2, z + 1, getLegSelector());
            this.replaceAir(world, box, random, LOCAL_X_END - 3, -2, z + 1, LOCAL_X_END - 3, -2, z + 1, getLegSelector());
        }
    }

    private void generateGravelDeposits(World world, StructureBoundingBox box, Random random) {
        gravelDeposits.forEach(pair -> {
            int z = pair.getLeft();
            int side = pair.getRight();
            switch (side) {
                case 0: // Left side
                default:
                    // Row closest to wall
                    this.replaceAir(world, box, 1, 1, z, 1, 2, z + 2, getGravel());
                    this.replaceAir(world, box, 1, 3, z + 1, 1, 3 + random.nextInt(2), z + 1, getGravel());
                    this.chanceReplaceAir(world, box, random, .5f, 1, 3, z, 1, 3, z + 2, getGravel());
                    // Middle row
                    this.replaceAir(world, box, 2, 1, z + 1, 2, 2 + random.nextInt(2), z + 1, getGravel());
                    this.replaceAir(world, box, 2, 1, z, 2, 1 + random.nextInt(2), z + 2, getGravel());
                    // Innermost row
                    this.chanceReplaceAir(world, box, random, .5f, 3, 1, z, 3, 1, z + 2, getGravel());
                    break;
                case 1: // Right side
                    // Row closest to wall
                    this.replaceAir(world, box, LOCAL_X_END - 1, 1, z, LOCAL_X_END - 1, 2, z + 2, getGravel());
                    this.replaceAir(world, box, LOCAL_X_END - 1, 3, z + 1, LOCAL_X_END - 1, 3 + random.nextInt(2), z + 1, getGravel());
                    this.chanceReplaceAir(world, box, random, .5f, LOCAL_X_END - 1, 3, z, LOCAL_X_END - 1, 3, z + 2, getGravel());
                    // Middle row
                    this.replaceAir(world, box, LOCAL_X_END - 2, 1, z + 1, LOCAL_X_END - 2, 2 + random.nextInt(2), z + 1, getGravel());
                    this.replaceAir(world, box, LOCAL_X_END - 2, 1, z, LOCAL_X_END - 2, 1 + random.nextInt(2), z + 2, getGravel());
                    // Innermost row
                    this.chanceReplaceAir(world, box, random, .5f, LOCAL_X_END - 3, 1, z, LOCAL_X_END - 3, 1, z + 2, getGravel());
            }
        });
    }

    private void generateCobwebs(World world, StructureBoundingBox box, Random random) {
        smallSupports.forEach(z -> {
            this.chanceReplaceAir(world, box, random, Configuration.spawnRates.cobwebSpawnRate, 2, 3, z - 1, LOCAL_X_END - 2,  4, z + 1, Blocks.COBWEB.defaultBlockState());
            this.chanceReplaceAir(world, box, random, Configuration.spawnRates.cobwebSpawnRate, 3, 5, z, LOCAL_X_END - 3,  5, z, Blocks.COBWEB.defaultBlockState());
        });

        bigSupports.forEach(z -> {
            this.chanceReplaceAir(world, box, random, Configuration.spawnRates.cobwebSpawnRate, 1, 1, z, 1,  4, z + 2, Blocks.COBWEB.defaultBlockState());
            this.chanceReplaceAir(world, box, random, Configuration.spawnRates.cobwebSpawnRate, LOCAL_X_END - 1, 1, z, LOCAL_X_END - 1,  4, z + 2, Blocks.COBWEB.defaultBlockState());
            this.chanceReplaceAir(world, box, random, Configuration.spawnRates.cobwebSpawnRate, 2, 5, z, LOCAL_X_END - 2,  5, z + 2, Blocks.COBWEB.defaultBlockState());
            this.chanceReplaceAir(world, box, random, Configuration.spawnRates.cobwebSpawnRate, 2, 4, z + 1, LOCAL_X_END - 2,  4, z + 1, Blocks.COBWEB.defaultBlockState());
            this.chanceReplaceAir(world, box, random, Configuration.spawnRates.cobwebSpawnRate, 3, 6, z + 1, LOCAL_X_END - 3,  6, z + 1, Blocks.COBWEB.defaultBlockState());
        });
    }

    private void generateChestCarts(World world, StructureBoundingBox box, Random random, net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> lootTableId) {
        for (int z = 0; z <= LOCAL_Z_END; z++) {
            if (random.nextFloat() < Configuration.spawnRates.mainShaftChestMinecartSpawnRate) {
                BlockPos blockPos = new BlockPos(this.getXWithOffset(LOCAL_X_END / 2, z), getYWithOffset(1), this.getZWithOffset(LOCAL_X_END / 2, z));
                if (box.isVecInside(blockPos) && world.getBlockState(blockPos.below()) != AIR) {
                    net.minecraft.world.entity.vehicle.MinecartChest chestMinecartEntity = new net.minecraft.world.entity.vehicle.MinecartChest(world.level.getLevel(), ((float) blockPos.getX() + 0.5F), ((float) blockPos.getY() + 0.5F), ((float) blockPos.getZ() + 0.5F));
                    chestMinecartEntity.setLootTable(lootTableId, random.nextLong());
                    world.spawnEntity(chestMinecartEntity);
                }
            }
        }
    }

    private void generateTntCarts(World world, StructureBoundingBox box, Random random) {
        for (int z = 0; z <= LOCAL_Z_END; z++) {
            if (random.nextFloat() < Configuration.spawnRates.mainShaftTntMinecartSpawnRate) {
                BlockPos blockPos = new BlockPos(this.getXWithOffset(LOCAL_X_END / 2, z), getYWithOffset(1), this.getZWithOffset(LOCAL_X_END / 2, z));
                if (box.isVecInside(blockPos) && world.getBlockState(blockPos.below()) != AIR) {
                    net.minecraft.world.entity.vehicle.MinecartTNT tntMinecartEntity = new net.minecraft.world.entity.vehicle.MinecartTNT(world.level.getLevel(), ((float) blockPos.getX() + 0.5F), ((float) blockPos.getY() + 0.5F), ((float) blockPos.getZ() + 0.5F));
                    world.spawnEntity(tntMinecartEntity);
                }
            }
        }
    }

    private void generateBigSupports(World world, StructureBoundingBox box, Random random) {
        bigSupports.forEach(z -> {
            // Bottom slabs
            this.chanceFill(world, box, random, .6f, 1, 1, z, 2, 1, z + 2, getMainSlab());
            this.chanceFill(world, box, random, .6f, LOCAL_X_END - 2, 1, z, LOCAL_X_END - 1, 1, z + 2, getMainSlab());
            // Main blocks
            this.setBlockState(world, getMainBlock(), 1, 1, z + 1, box);
            this.setBlockState(world, getMainBlock(), LOCAL_X_END - 1, 1, z + 1, box);
            this.setBlockState(world, getMainBlock(), 1, 4, z + 1, box);
            this.setBlockState(world, getMainBlock(), LOCAL_X_END - 1, 4, z + 1, box);
            this.fill(world, box, 2, 5, z + 1, LOCAL_X_END - 2, 5, z + 1, getMainBlock());
            // Supports
            this.fill(world, box, 1, 2, z + 1, 1, 3, z + 1, getSupportBlock());
            this.fill(world, box, LOCAL_X_END - 1, 2, z + 1, LOCAL_X_END - 1, 3, z + 1, getSupportBlock());
            this.chanceReplaceNonAir(world, box, random, .4f, 2, 5, z + 1, LOCAL_X_END - 2, 5, z + 1, getSupportBlock());
        });
    }

    private void generateSmallSupports(World world, StructureBoundingBox box, Random random) {
        BlockState supportBlock = getSupportBlock();
        if (supportBlock.getBlock() instanceof FenceBlock)
            supportBlock = getSupportBlock().setValue(FenceBlock.WEST, true).setValue(FenceBlock.EAST, true);
        else if (supportBlock.getBlock() instanceof WallBlock)
            supportBlock = getSupportBlock().setValue(WallBlock.WEST_WALL, WallSide.LOW).setValue(WallBlock.EAST_WALL, WallSide.LOW);

        for (int z : smallSupports) {
            this.setBlockState(world, getMainBlock(), 2, 1, z, box);
            this.setBlockState(world, getMainBlock(), LOCAL_X_END - 2, 1, z, box);
            this.setBlockState(world, getSupportBlock(), 2, 2, z, box);
            this.setBlockState(world, getSupportBlock(), LOCAL_X_END - 2, 2, z, box);
            this.setBlockState(world, getMainBlock(), 2, 3, z, box);
            this.setBlockState(world, getMainBlock(), LOCAL_X_END - 2, 3, z, box);
            this.fill(world, box, 3, 4, z, LOCAL_X_END - 3, 4, z, getMainBlock());
            this.chanceReplaceNonAir(world, box, random, .5f, 3, 4, z, LOCAL_X_END - 3, 4, z, supportBlock);
            this.chanceFill(world, box, random, .4f, 2, 3, z, LOCAL_X_END - 2, 3, z, supportBlock);
            this.setBlockState(world, supportBlock, 3, 3, z, box);
            this.setBlockState(world, supportBlock, LOCAL_X_END - 3, 3, z, box);
        }
    }

    private void generateLanterns(World world, StructureBoundingBox box, Random random) {
        BlockState lantern = Integrations.getLantern(random);
        if (lantern == null) return;
        for (int z = 0; z <= LOCAL_Z_END; z++) {
            // check rate * 3 because this used to spawn in any of the 3 middle blocks, so the * 3 matches the spawn rate for the previous logic
            if (random.nextFloat() < Configuration.spawnRates.lanternSpawnRate * 3) {
                if (this.getBlockStateFromPos(world, 4, LOCAL_Y_END, z, box) != AIR) {
                    this.setBlockState(world, lantern, 4, LOCAL_Y_END - 1, z, box);
                    if (Integrations.VARIED_COMMODITIES.isLanternVariedCommoditiesCandle(lantern)) {
                        Integrations.VARIED_COMMODITIES.spawnCandleLanternEntity(world, getXWithOffset(4, z), getYWithOffset(LOCAL_Y_END - 1), getZWithOffset(4 , z));
                    } else if (Integrations.VARIED_COMMODITIES.isLanternVariedCommoditiesLamp(lantern)) {
                        Integrations.VARIED_COMMODITIES.spawnLampLanternEntity(world, getXWithOffset(4, z), getYWithOffset(LOCAL_Y_END - 1), getZWithOffset(4 , z));
                    }
                    z += 20;
                }
            }
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

    private void generateSmallShaftEntrances(World world, StructureBoundingBox box, Random random) {
        smallShaftLeftEntrances.forEach(entrancePos -> {
            int x = entrancePos.getX();
            int y = entrancePos.getY();
            int z = entrancePos.getZ();

            this.fill(world, box, x, y, z, x, y, z + 2, AIR);
            this.setBlockState(world, getSupportBlock(), x, y + 1, z, box);
            this.setBlockState(world, getSupportBlock(), x, y + 1, z + 2, box);
            this.fill(world, box, x + 1, y, z, x + 1, y + 1, z, getSupportBlock());
            this.fill(world, box, x + 1, y, z + 2, x + 1, y + 1, z + 2, getSupportBlock());
            this.chanceFill(world, box, random, .75f, x, y + 2, z, x + 1, y + 2, z + 2, getMainBlock());
            this.fill(world, box, x, y + 1, z + 1, x + 1, y + 1, z + 1, AIR);
            this.replaceAir(world, box, x, y - 1, z, x + 1, y - 1, z + 2, getMainBlock());
        });

        smallShaftRightEntrances.forEach(entrancePos -> {
            int x = entrancePos.getX();
            int y = entrancePos.getY();
            int z = entrancePos.getZ();

            this.replaceAir(world, box, x, y - 1, z, x + 1, y - 1, z + 2, getMainBlock());
            this.fill(world, box, x + 1, y, z, x + 1, y, z + 2, AIR);
            this.setBlockState(world, getSupportBlock(), x + 1, y + 1, z, box);
            this.setBlockState(world, getSupportBlock(), x + 1, y + 1, z + 2, box);
            this.fill(world, box, x, y, z, x, y + 1, z, getSupportBlock());
            this.fill(world, box, x, y, z + 2, x, y + 1, z + 2, getSupportBlock());
            this.chanceFill(world, box, random, .75f, x, y + 2, z, x + 1, y + 2, z + 2, getMainBlock());
            this.fill(world, box, x, y + 1, z + 1, x + 1, y + 1, z + 1, AIR);
        });
    }

    private void generateSideRoomOpenings(World world, StructureBoundingBox box, Random random) {
        sideRoomEntrances.forEach(entranceBox -> {
            // Ensure floor in gap between tunnel and room
            this.replaceAir(world, box, random, entranceBox.minX, 0, entranceBox.minZ, entranceBox.maxX, 0, entranceBox.maxZ, getBrickSelector());
            switch (random.nextInt(3)) {
                case 0:
                    // Completely open
                    this.fill(world, box, entranceBox.minX, entranceBox.minY, entranceBox.minZ + 2, entranceBox.maxX, entranceBox.maxY, entranceBox.maxZ - 2, AIR);
                    return;
                case 1:
                    // A few columns for openings
                    this.fill(world, box, entranceBox.minX, entranceBox.minY, entranceBox.minZ + 2, entranceBox.maxX, entranceBox.maxY - 1, entranceBox.minZ + 2, AIR);
                    this.fill(world, box, entranceBox.minX, entranceBox.minY, entranceBox.minZ + 4, entranceBox.maxX, entranceBox.maxY - 1, entranceBox.minZ + 5, AIR);
                    this.fill(world, box, entranceBox.minX, entranceBox.minY, entranceBox.minZ + 7, entranceBox.maxX, entranceBox.maxY - 1, entranceBox.minZ + 7, AIR);
                    return;
                case 2:
                    // No openings - random block removal will expose these, probably
            }
        });
    }

    private void buildGravelDeposits(Random random) {
        for (int z = 0; z <= LOCAL_Z_END - 2; z++) {
            int r = random.nextInt(20);
            int currPos = z;
            if (r == 0) { // Left side
                gravelDeposits.add(new Pair<>(currPos, 0));
                z += 5;
            }
            else if (r == 1) { // Right side
                gravelDeposits.add(new Pair<>(currPos, 1));
                z += 5;
            }
        }
    }

    private void buildSupports(Random random) {
        int counter = 0;
        final int MAX_COUNT = 10; // max number of blocks before force spawning a support

        for (int z = 0; z <= LOCAL_Z_END - 2; z++) {
            counter++;

            // Make sure we arent overlapping with small shaft entrances
            boolean blockingEntrance = false;
            for (BlockPos entrancePos : smallShaftLeftEntrances) {
                if (entrancePos.getZ() <= z + 2 && z <= entrancePos.getZ() + 2) {
                    blockingEntrance = true;
                    break;
                }
            }
            for (BlockPos entrancePos : smallShaftRightEntrances) {
                if (entrancePos.getZ() <= z + 2 && z <= entrancePos.getZ() + 2) {
                    blockingEntrance = true;
                    break;
                }
            }
            if (blockingEntrance) continue;

            int r = random.nextInt(8);
            if (r == 0 || counter >= MAX_COUNT) { // Big support
                bigSupports.add(z);
                counter = 0;
                z += 3;
            }
            else if (r == 1) { // Small support
                smallSupports.add(z);
                counter = 0;
                z += 3;
            }
        }
    }

    private void buildSideRoomsLeft(StructureComponent structurePiece, List<StructureComponent> list, Random random, Direction direction, int pieceLen) {
        Direction nextPieceDirection;
        StructureComponent newPiece;
        for (int n = 0; n < (pieceLen - 1) - 10; n++) {
            if (random.nextFloat() < Configuration.spawnRates.workstationSpawnRate) {
                switch (direction) {
                    case NORTH:
                    default:
                        nextPieceDirection = Direction.EAST;
                        newPiece = BetterMineshaftGenerator.generateAndAddSideRoomPiece(structurePiece, list, random, this.boundingBox.minX - 5, this.boundingBox.minY, this.boundingBox.maxZ - n - 9, nextPieceDirection, this.getComponentType(), pieceChainLen);
                        if (newPiece != null) {
                            sideRoomEntrances.add(new StructureBoundingBox(0, 1, n, 0, 3, n + 9));
                        }
                        break;
                    case SOUTH:
                        nextPieceDirection = Direction.WEST;
                        newPiece = BetterMineshaftGenerator.generateAndAddSideRoomPiece(structurePiece, list, random, this.boundingBox.maxX + 5, this.boundingBox.minY, this.boundingBox.minZ + n + 9, nextPieceDirection, this.getComponentType(), pieceChainLen);
                        if (newPiece != null) {
                            sideRoomEntrances.add(new StructureBoundingBox(SECONDARY_AXIS_LEN - 1, 1, n, SECONDARY_AXIS_LEN - 1, 3, n + 9));
                        }
                        break;
                    case WEST:
                        nextPieceDirection = Direction.NORTH;
                        newPiece = BetterMineshaftGenerator.generateAndAddSideRoomPiece(structurePiece, list, random, this.boundingBox.maxX - n - 9, this.boundingBox.minY, this.boundingBox.maxZ + 5, nextPieceDirection, this.getComponentType(), pieceChainLen);
                        if (newPiece != null) {
                            sideRoomEntrances.add(new StructureBoundingBox(SECONDARY_AXIS_LEN - 1, 1, n, SECONDARY_AXIS_LEN - 1, 3, n + 9));
                        }
                        break;
                    case EAST:
                        nextPieceDirection = Direction.SOUTH;
                        newPiece = BetterMineshaftGenerator.generateAndAddSideRoomPiece(structurePiece, list, random, this.boundingBox.minX + n + 9, this.boundingBox.minY, this.boundingBox.minZ - 5, nextPieceDirection, this.getComponentType(), pieceChainLen);
                        if (newPiece != null) {
                            sideRoomEntrances.add(new StructureBoundingBox(0, 1, n, 0, 3, n + 9));
                        }
                        break;
                }
                n += 10;
            }
        }
    }

    private void buildSideRoomsRight(StructureComponent structurePiece, List<StructureComponent> list, Random random, Direction direction, int pieceLen) {
        Direction nextPieceDirection;
        StructureComponent newPiece;
        for (int n = 0; n < (pieceLen - 1) - 10; n++) {
            if (random.nextFloat() < Configuration.spawnRates.workstationSpawnRate) {
                switch (direction) {
                    case NORTH:
                    default:
                        nextPieceDirection = Direction.WEST;
                        newPiece = BetterMineshaftGenerator.generateAndAddSideRoomPiece(structurePiece, list, random, this.boundingBox.maxX + 5, this.boundingBox.minY, this.boundingBox.maxZ - n, nextPieceDirection, this.getComponentType(), pieceChainLen);
                        if (newPiece != null) {
                            sideRoomEntrances.add(new StructureBoundingBox(SECONDARY_AXIS_LEN - 1, 1, n, SECONDARY_AXIS_LEN - 1, 3, n + 9));
                        }
                        break;
                    case SOUTH:
                        nextPieceDirection = Direction.EAST;
                        newPiece = BetterMineshaftGenerator.generateAndAddSideRoomPiece(structurePiece, list, random, this.boundingBox.minX - 5, this.boundingBox.minY, this.boundingBox.minZ + n, nextPieceDirection, this.getComponentType(), pieceChainLen);
                        if (newPiece != null) {
                            sideRoomEntrances.add(new StructureBoundingBox(0, 1, n, 0, 3, n + 9));
                        }
                        break;
                    case WEST:
                        nextPieceDirection = Direction.SOUTH;
                        newPiece = BetterMineshaftGenerator.generateAndAddSideRoomPiece(structurePiece, list, random, this.boundingBox.maxX - n, this.boundingBox.minY, this.boundingBox.minZ - 5, nextPieceDirection, this.getComponentType(), pieceChainLen);
                        if (newPiece != null) {
                            sideRoomEntrances.add(new StructureBoundingBox(0, 1, n, 0, 3, n + 9));
                        }
                        break;
                    case EAST:
                        nextPieceDirection = Direction.NORTH;
                        newPiece = BetterMineshaftGenerator.generateAndAddSideRoomPiece(structurePiece, list, random, this.boundingBox.minX + n, this.boundingBox.minY, this.boundingBox.maxZ + 5, nextPieceDirection, this.getComponentType(), pieceChainLen);
                        if (newPiece != null) {
                            sideRoomEntrances.add(new StructureBoundingBox(SECONDARY_AXIS_LEN - 1, 1, n, SECONDARY_AXIS_LEN - 1, 3, n + 9));
                        }
                        break;
                }
                n += 10;
            }
        }
    }

    private void buildSmallShaftsLeft(StructureComponent structurePiece, List<StructureComponent> list, Random random, Direction direction, int pieceLen) {
        Direction nextPieceDirection;
        StructureComponent newPiece;
        for (int n = 0; n < (pieceLen - 1) - 4; n++) {
            if (random.nextFloat() < Configuration.spawnRates.smallShaftSpawnRate) {
                switch (direction) {
                    case NORTH:
                    default:
                        nextPieceDirection = Direction.WEST;
                        newPiece = BetterMineshaftGenerator.generateAndAddSmallTunnelPiece(structurePiece, list, random, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.maxZ - n, nextPieceDirection, this.getComponentType(), 0);
                        if (newPiece != null) {
                            this.smallShaftLeftEntrances.add(new BlockPos(0, 1, n + 1));
                        }
                        break;
                    case SOUTH:
                        nextPieceDirection = Direction.EAST;
                        newPiece = BetterMineshaftGenerator.generateAndAddSmallTunnelPiece(structurePiece, list, random, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ + n, nextPieceDirection, this.getComponentType(), 0);
                        if (newPiece != null) {
                            this.smallShaftRightEntrances.add(new BlockPos(SECONDARY_AXIS_LEN - 2, 1, n + 1));
                        }
                        break;
                    case WEST:
                        nextPieceDirection = Direction.SOUTH;
                        newPiece = BetterMineshaftGenerator.generateAndAddSmallTunnelPiece(structurePiece, list, random, this.boundingBox.maxX - n, this.boundingBox.minY, this.boundingBox.maxZ + 1, nextPieceDirection, this.getComponentType(), 0);
                        if (newPiece != null) {
                            this.smallShaftRightEntrances.add(new BlockPos(SECONDARY_AXIS_LEN - 2, 1, n + 1));
                        }
                        break;
                    case EAST:
                        nextPieceDirection = Direction.NORTH;
                        newPiece = BetterMineshaftGenerator.generateAndAddSmallTunnelPiece(structurePiece, list, random, this.boundingBox.minX + n, this.boundingBox.minY, this.boundingBox.minZ - 1, nextPieceDirection, this.getComponentType(), 0);
                        if (newPiece != null) {
                            this.smallShaftLeftEntrances.add(new BlockPos(0, 1, n + 1));
                        }
                        break;
                }

                n += random.nextInt(7) + 5;
            }
        }
    }

    private void buildSmallShaftsRight(StructureComponent structurePiece, List<StructureComponent> list, Random random, Direction direction, int pieceLen) {
        Direction nextPieceDirection;
        StructureComponent newPiece;
        for (int n = 5; n < pieceLen; n++) {
            if (random.nextFloat() < Configuration.spawnRates.smallShaftSpawnRate) {
                switch (direction) {
                    case NORTH:
                    default:
                        nextPieceDirection = Direction.EAST;
                        newPiece = BetterMineshaftGenerator.generateAndAddSmallTunnelPiece(structurePiece, list, random, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.maxZ - n, nextPieceDirection, this.getComponentType(), 0);
                        if (newPiece != null) {
                            this.smallShaftRightEntrances.add(new BlockPos(SECONDARY_AXIS_LEN - 2, 1, n - 3));
                        }
                        break;
                    case SOUTH:
                        nextPieceDirection = Direction.WEST;
                        newPiece = BetterMineshaftGenerator.generateAndAddSmallTunnelPiece(structurePiece, list, random, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.minZ + n, nextPieceDirection, this.getComponentType(), 0);
                        if (newPiece != null) {
                            this.smallShaftLeftEntrances.add(new BlockPos(0, 1, n - 3));
                        }
                        break;
                    case WEST:
                        nextPieceDirection = Direction.NORTH;
                        newPiece = BetterMineshaftGenerator.generateAndAddSmallTunnelPiece(structurePiece, list, random, this.boundingBox.maxX - n, this.boundingBox.minY, this.boundingBox.minZ - 1, nextPieceDirection, this.getComponentType(), 0);
                        if (newPiece != null) {
                            this.smallShaftLeftEntrances.add(new BlockPos(0, 1, n - 3));
                        }
                        break;
                    case EAST:
                        nextPieceDirection = Direction.SOUTH;
                        newPiece = BetterMineshaftGenerator.generateAndAddSmallTunnelPiece(structurePiece, list, random, this.boundingBox.minX + n, this.boundingBox.minY, this.boundingBox.maxZ + 1, nextPieceDirection, this.getComponentType(), 0);
                        if (newPiece != null) {
                            this.smallShaftRightEntrances.add(new BlockPos(SECONDARY_AXIS_LEN - 2, 1, n - 3));
                        }
                        break;
                }

                n += random.nextInt(7) + 5;
            }
        }
    }
}