package com.lycanitesmobs.core.spawner.trigger;

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
import net.minecraft.world.entity.boss.enderdragon.*;
import net.minecraft.world.entity.boss.wither.*;
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
import com.google.gson.JsonObject;
import com.lycanitesmobs.core.info.ObjectLists;
import com.lycanitesmobs.core.spawner.Spawner;


public class TreeBlockSpawnTrigger extends BlockSpawnTrigger {
   public TreeBlockSpawnTrigger(Spawner spawner) {
      super(spawner);
   }

   @Override
   public void loadFromJSON(JsonObject json) {
      super.loadFromJSON(json);
   }

   @Override
   public boolean isTriggerBlock(BlockState blockState, Level world, BlockPos blockPos, int fortune) {
      return this.isTreeLogBlock(blockState.getBlock(), world, blockPos) || this.isTreeLeavesBlock(blockState.getBlock(), world, blockPos);
   }

   public boolean isTreeLogBlock(Block block, Level world, BlockPos pos) {
      if (this.isLog(world.getBlockState(pos))) {
         int x = pos.getX();
         int y = pos.getY();
         int z = pos.getZ();

         for (int searchX = x - 1; searchX <= x + 1; searchX++) {
            for (int searchZ = z - 1; searchZ <= z + 1; searchZ++) {
               for (int searchY = y; searchY <= Math.min(world.getHeight(), y + 32); searchY++) {
                  if (this.isLeaves(world.getBlockState(LycPos.of(searchX, searchY, searchZ)))) {
                     return true;
                  }

                  if (!world.isEmptyBlock(LycPos.of(x, searchY, z))) {
                     break;
                  }
               }
            }
         }
      }

      String blockName = LycReg.key(block).toString();
      return (blockName.contains("tree") || blockName.contains("traverse")) && blockName.contains("branch");
   }

   public boolean isTreeLeavesBlock(Block block, Level world, BlockPos pos) {
      if (this.isLeaves(world.getBlockState(pos))) {
         int x = pos.getX();
         int y = pos.getY();
         int z = pos.getZ();

         for (int searchX = x - 1; searchX <= x + 1; searchX++) {
            for (int searchZ = z - 1; searchZ <= z + 1; searchZ++) {
               for (int searchY = y; searchY >= Math.max(0, y - 32); searchY--) {
                  if (this.isLog(world.getBlockState(LycPos.of(searchX, searchY, searchZ)))) {
                     return true;
                  }

                  if (!world.isEmptyBlock(LycPos.of(x, searchY, z))) {
                     break;
                  }
               }
            }
         }
      }

      String blockName = LycReg.key(block).toString();
      return (blockName.contains("tree") || blockName.contains("traverse")) && blockName.contains("leaves");
   }

   public boolean isLog(BlockState blockState) {
      Block block = blockState.getBlock();
      return blockState.is(BlockTags.LOGS) || ObjectLists.isInOreDictionary("logWood", block);
   }

   public boolean isLeaves(BlockState blockState) {
      Block block = blockState.getBlock();
      return block instanceof LeavesBlock || ObjectLists.isInOreDictionary("treeLeaves", block);
   }
}
