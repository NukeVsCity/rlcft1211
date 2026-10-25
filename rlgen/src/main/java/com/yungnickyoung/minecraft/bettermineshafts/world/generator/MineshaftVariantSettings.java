package com.yungnickyoung.minecraft.bettermineshafts.world.generator;

import com.yungnickyoung.minecraft.bettermineshafts.mc.*;
import com.nukevscity.rlgen.rc.BiomeDict;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

import com.yungnickyoung.minecraft.bettermineshafts.util.BlockSetSelector;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class MineshaftVariantSettings {

    public List<List<String>> biomeTags;
    public BlockSetSelector mainSelector;
    public BlockSetSelector floorSelector;
    public BlockSetSelector brickSelector;
    public BlockSetSelector legSelector;
    public BlockState mainBlock;
    public BlockState supportBlock;
    public BlockState slabBlock;
    public BlockState gravelBlock;
    public BlockState stoneWallBlock;
    public BlockState stoneSlabBlock;
    public int minY;
    public int maxY;
    public float vineChance;
    public float snowChance;
    public float cactusChance;
    public float deadBushChance;
    public float mushroomChance;
    public int legVariant;
    public float replacementRate;

    /** Builder-style setters to make it more obvious which settings are being set when creating a new object **/

    public MineshaftVariantSettings setBiomeTags(List<List<String>> biomeTags) {
        this.biomeTags = biomeTags;
        return this;
    }

    public MineshaftVariantSettings setMainSelector(BlockSetSelector mainSelector) {
        this.mainSelector = mainSelector;
        if (this.mainBlock != null) this.mainSelector.setDefaultBlock(mainBlock);
        return this;
    }

    public MineshaftVariantSettings setFloorSelector(BlockSetSelector floorSelector) {
        this.floorSelector = floorSelector;
        if (this.mainBlock != null) this.floorSelector.setDefaultBlock(mainBlock);
        return this;
    }

    public MineshaftVariantSettings setBrickSelector(BlockSetSelector brickSelector) {
        this.brickSelector = brickSelector;
        if (this.mainBlock != null) this.brickSelector.setDefaultBlock(mainBlock);
        return this;
    }

    public MineshaftVariantSettings setLegSelector(BlockSetSelector legSelector) {
        this.legSelector = legSelector;
        if (this.mainBlock != null) this.legSelector.setDefaultBlock(mainBlock);
        return this;
    }

    public MineshaftVariantSettings setMainBlock(BlockState mainBlock) {
        this.mainBlock = mainBlock;
        if (this.mainSelector != null) this.mainSelector.setDefaultBlock(mainBlock);
        if (this.floorSelector != null) this.floorSelector.setDefaultBlock(mainBlock);
        if (this.brickSelector != null) this.brickSelector.setDefaultBlock(mainBlock);
        if (this.legSelector != null) this.legSelector.setDefaultBlock(mainBlock);
        return this;
    }

    public MineshaftVariantSettings setSupportBlock(BlockState supportBlock) {
        this.supportBlock = supportBlock;
        return this;
    }

    public MineshaftVariantSettings setSlabBlock(BlockState slabBlock) {
        this.slabBlock = slabBlock;
        return this;
    }

    public MineshaftVariantSettings setGravelBlock(BlockState gravelBlock) {
        this.gravelBlock = gravelBlock;
        return this;
    }

    public MineshaftVariantSettings setStoneWallBlock(BlockState stoneWallBlock) {
        this.stoneWallBlock = stoneWallBlock;
        return this;
    }

    public MineshaftVariantSettings setStoneSlabBlock(BlockState stoneSlabBlock) {
        this.stoneSlabBlock = stoneSlabBlock;
        return this;
    }

    public MineshaftVariantSettings setMinY(int minY) {
        this.minY = minY;
        return this;
    }

    public MineshaftVariantSettings setMaxY(int maxY) {
        this.maxY = maxY;
        return this;
    }

    public MineshaftVariantSettings setLegVariant(int legVariant) {
        this.legVariant = legVariant;
        return this;
    }

    public MineshaftVariantSettings setVineChance(float vineChance) {
        this.vineChance = vineChance;
        return this;
    }

    public MineshaftVariantSettings setSnowChance(float snowChance) {
        this.snowChance = snowChance;
        return this;
    }

    public MineshaftVariantSettings setCactusChance(float cactusChance) {
        this.cactusChance = cactusChance;
        return this;
    }

    public MineshaftVariantSettings setDeadBushChance(float deadBushChance) {
        this.deadBushChance = deadBushChance;
        return this;
    }

    public MineshaftVariantSettings setMushroomChance(float mushroomChance) {
        this.mushroomChance = mushroomChance;
        return this;
    }

    public MineshaftVariantSettings setReplacementRate(float replacementRate) {
        this.replacementRate = replacementRate;
        return this;
    }
}
