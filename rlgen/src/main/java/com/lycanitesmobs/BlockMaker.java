package com.lycanitesmobs;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
import com.lycanitesmobs.LycEntities;
import com.lycanitesmobs.compat.*;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.*;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.*;
import net.minecraft.network.chat.*;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.*;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.ambient.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.item.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.*;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.*;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.common.*;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.*;
import net.neoforged.neoforge.event.tick.*;
import com.lycanitesmobs.core.block.BlockBase;
import com.lycanitesmobs.core.block.BlockDoubleSlab;
import com.lycanitesmobs.core.block.BlockFenceCustom;
import com.lycanitesmobs.core.block.BlockPillar;
import com.lycanitesmobs.core.block.BlockSlabCustom;
import com.lycanitesmobs.core.block.BlockStairsCustom;
import com.lycanitesmobs.core.block.BlockWallCustom;
import com.lycanitesmobs.core.info.ModInfo;


public class BlockMaker {
   public static void addStoneBlocks(ModInfo group, String stoneName) {
      float hardness = 2.0F;
      float resistance = 2000.0F;
      BlockBase stoneBlock = (BlockBase)new BlockBase(Material.ROCK, group, stoneName + "stone")
         .setHardness(hardness)
         .setResistance(resistance)
         .setCreativeTab(LycanitesMobs.blocksTab);
      ObjectManager.addBlock(stoneName + "stone", stoneBlock);
      ObjectManager.addBlock(stoneName + "stone_stairs", new BlockStairsCustom(stoneBlock).setCreativeTab(LycanitesMobs.blocksTab));
      Block stoneSlabDoubleBlock = new BlockDoubleSlab(Material.ROCK, group, stoneName + "stone_slab_double", stoneName + "stone_slab")
         .setHardness(hardness)
         .setResistance(resistance);
      ObjectManager.addBlock(stoneName + "stone_slab_double", stoneSlabDoubleBlock);
      ObjectManager.addBlock(stoneName + "stone_slab", new BlockSlabCustom(stoneBlock, stoneSlabDoubleBlock).setCreativeTab(LycanitesMobs.blocksTab));
      BlockBase stoneBrickBlock = (BlockBase)new BlockBase(Material.ROCK, group, stoneName + "stonebrick")
         .setHardness(hardness)
         .setResistance(resistance)
         .setCreativeTab(LycanitesMobs.blocksTab);
      ObjectManager.addBlock(stoneName + "stonebrick", stoneBrickBlock);
      ObjectManager.addBlock(stoneName + "stonebrick_stairs", new BlockStairsCustom(stoneBrickBlock).setCreativeTab(LycanitesMobs.blocksTab));
      ObjectManager.addBlock(stoneName + "stonebrick_slab", new BlockSlabCustom(stoneBrickBlock, stoneBrickBlock).setCreativeTab(LycanitesMobs.blocksTab));
      ObjectManager.addBlock(stoneName + "stonebrick_fence", new BlockFenceCustom(stoneBrickBlock).setCreativeTab(LycanitesMobs.blocksTab));
      ObjectManager.addBlock(stoneName + "stonebrick_wall", new BlockWallCustom(stoneBrickBlock).setCreativeTab(LycanitesMobs.blocksTab));
      BlockBase stoneTileBlock = (BlockBase)new BlockBase(Material.ROCK, group, stoneName + "stonetile")
         .setHardness(hardness)
         .setResistance(resistance)
         .setCreativeTab(LycanitesMobs.blocksTab);
      ObjectManager.addBlock(stoneName + "stonetile", stoneTileBlock);
      ObjectManager.addBlock(stoneName + "stonetile_stairs", new BlockStairsCustom(stoneTileBlock).setCreativeTab(LycanitesMobs.blocksTab));
      ObjectManager.addBlock(stoneName + "stonetile_slab", new BlockSlabCustom(stoneTileBlock, stoneTileBlock).setCreativeTab(LycanitesMobs.blocksTab));
      ObjectManager.addBlock(
         stoneName + "stonepolished",
         new BlockBase(Material.ROCK, group, stoneName + "stonepolished")
            .setHardness(hardness)
            .setResistance(resistance)
            .setCreativeTab(LycanitesMobs.blocksTab)
      );
      ObjectManager.addBlock(
         stoneName + "stonechiseled",
         new BlockBase(Material.ROCK, group, stoneName + "stonechiseled")
            .setHardness(hardness)
            .setResistance(resistance)
            .setCreativeTab(LycanitesMobs.blocksTab)
      );
      ObjectManager.addBlock(
         stoneName + "stonepillar",
         new BlockPillar(Material.ROCK, group, stoneName + "stonepillar")
            .setHardness(hardness)
            .setResistance(resistance)
            .setCreativeTab(LycanitesMobs.blocksTab)
      );
      ObjectManager.addBlock(
         stoneName + "crystal",
         new BlockBase(Material.GLASS, group, stoneName + "crystal")
            .setBlockStepSound(SoundType.GLASS)
            .setHardness(0.5F)
            .setResistance(resistance)
            .setLightLevel(1.0F)
            .setCreativeTab(LycanitesMobs.blocksTab)
      );
   }
}
