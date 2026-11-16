package com.lycanitesmobs.core.entity;

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
import com.lycanitesmobs.core.block.building.BlockPropolis;
import com.lycanitesmobs.core.block.building.BlockVeswax;
import com.lycanitesmobs.core.dungeon.definition.DungeonTheme;
import com.lycanitesmobs.core.dungeon.definition.ThemeBlock;
import java.util.HashMap;
import java.util.Map;


public class CreatureStructure {
   protected BaseCreatureEntity owner;
   protected DungeonTheme dungeonTheme;
   protected Map<Integer, Map<BlockPos, CreatureBuildTask>> buildTasks = new HashMap<>();
   protected BlockPos origin;
   protected BlockPos startPos;

   public CreatureStructure(BaseCreatureEntity creatureEntity, DungeonTheme dungeonTheme) {
      this.owner = creatureEntity;
      this.dungeonTheme = dungeonTheme;
      this.origin = creatureEntity.blockPosition();
      if (this.dungeonTheme == null) {
         throw new RuntimeException("Unable to find the vespid_hive dungeon theme!");
      }
   }

   public BlockPos getOrigin() {
      return this.origin;
   }

   public void setOrigin(BlockPos blockPos) {
      this.origin = blockPos;
      this.startPos = blockPos.offset(0, 8, 0);
   }

   public void refreshBuildTasks() {
      this.buildTasks.clear();
      int radius = 8;

      for (int x = -radius; x <= radius; x++) {
         for (int y = -radius; y <= radius; y++) {
            for (int z = -radius; z <= radius; z++) {
               if (Math.abs(x) != radius || Math.abs(z) != radius) {
                  BlockPos blockPos = this.origin.offset(x, y, z);
                  if (this.shouldBuildAt(blockPos)) {
                     BlockState blockState;
                     if (Math.abs(y) > 1 || Math.abs(x) > 1 && Math.abs(z) > 1) {
                        if (Math.abs(x) < radius && Math.abs(y) < radius && Math.abs(z) < radius) {
                           blockState = Blocks.AIR.defaultBlockState();
                        } else if (y == radius) {
                           blockState = this.dungeonTheme.getCeiling(null, '1', this.owner.getRandom());
                        } else if (y == -radius) {
                           blockState = this.dungeonTheme.getFloor(null, '1', this.owner.getRandom());
                        } else {
                           blockState = this.dungeonTheme.getWall(null, '1', this.owner.getRandom());
                        }
                     } else {
                        blockState = Blocks.AIR.defaultBlockState();
                     }

                     this.createBuildTask(blockState, blockPos, 1);
                  }
               }
            }
         }
      }

      for (int x = -radius; x <= radius; x++) {
         for (int y = -radius; y <= radius; y++) {
            for (int zx = -radius; zx <= radius; zx++) {
               if (Math.abs(x) >= radius / 4 + 1 || Math.abs(zx) >= radius / 4 + 1) {
                  BlockPos blockPos = this.origin.offset(x, y, zx);
                  if (this.shouldBuildAt(blockPos)) {
                     for (int pitLayer = 1; pitLayer <= radius / 2; pitLayer++) {
                        if (y == radius - pitLayer && Math.abs(x) < radius - pitLayer && Math.abs(zx) < radius - pitLayer) {
                           BlockState blockState = this.dungeonTheme.getPit('1', this.owner.getRandom());
                           this.createBuildTask(blockState, blockPos, 1);
                        }
                     }

                     for (int pitLayerx = 1; pitLayerx <= radius / 2; pitLayerx++) {
                        if (y == -radius + pitLayerx && Math.abs(x) < radius - pitLayerx && Math.abs(zx) < radius - pitLayerx) {
                           BlockState blockState = this.dungeonTheme.getPit('1', this.owner.getRandom());
                           this.createBuildTask(blockState, blockPos, 1);
                        }
                     }
                  }
               }
            }
         }
      }

      for (int x = -radius; x <= radius; x++) {
         for (int y = -radius; y <= radius; y++) {
            for (int zxx = -radius; zxx <= radius; zxx++) {
               if (Math.abs(x) < radius / 4 + 1 && y <= -(radius / 2) && y > -radius && Math.abs(zxx) < radius / 4 + 1) {
                  BlockPos blockPos = this.origin.offset(x, y, zxx);
                  if (this.shouldBuildAt(blockPos)) {
                     BlockState blockState = this.dungeonTheme.getPit('2', this.owner.getRandom());
                     this.createBuildTask(blockState, blockPos, 1);
                  }
               }
            }
         }
      }
   }

   public void createBuildTask(BlockState blockState, BlockPos blockPos, int phase) {
      BlockState targetBlockState = this.owner.level().getBlockState(blockPos);
      if (blockState == targetBlockState
         || !(blockState.getBlock() instanceof IFluidBlock) && blockState.getBlock() == targetBlockState.getBlock()) {
         for (Map<BlockPos, CreatureBuildTask> phasedBuildTasks : this.buildTasks.values()) {
            phasedBuildTasks.remove(blockPos);
         }
      } else {
         CreatureBuildTask buildTask = new CreatureBuildTask(blockState, blockPos, phase);
         if (!this.buildTasks.containsKey(phase)) {
            this.buildTasks.put(phase, new HashMap<>());
         }

         this.buildTasks.get(phase).put(blockPos, buildTask);
      }
   }

   protected boolean shouldBuildAt(BlockPos pos) {
      BlockState targetState = this.owner.level().getBlockState(pos);
      Block targetBlock = targetState.getBlock();
      return targetBlock == Blocks.AIR ? true : this.owner.level().getBlockEntity(pos) == null;
   }

   @Nullable
   public CreatureBuildTask takeBuildTask(LivingEntity builder) {
      if (this.buildTasks.isEmpty()) {
         return null;
      } else {
         for (int phase : this.buildTasks.keySet()) {
            if (!this.buildTasks.get(phase).isEmpty()) {
               CreatureBuildTask[] phasedBuildTasks = this.buildTasks.get(phase).values().toArray(new CreatureBuildTask[0]);
               if (phasedBuildTasks.length == 1) {
                  return phasedBuildTasks[0];
               }

               return phasedBuildTasks[builder.getRandom().nextInt(phasedBuildTasks.length)];
            }
         }

         return null;
      }
   }

   public void completeBuildTask(CreatureBuildTask buildTask) {
      for (int phase : this.buildTasks.keySet()) {
         if (!this.buildTasks.get(phase).isEmpty()) {
            this.buildTasks.get(phase).remove(buildTask.pos);
            this.owner.onBuildTaskComplete(buildTask);
         }
      }
   }

   public boolean isPhaseComplete(int phase) {
      return this.getBuildTaskSize(phase) == 0;
   }

   public int getBuildTaskSize(int phase) {
      return !this.buildTasks.containsKey(phase) ? 0 : this.buildTasks.get(phase).size();
   }

   public int getFinalPhaseBuildTaskSize() {
      return this.getBuildTaskSize(this.buildTasks.size() - 1);
   }

   public void removePitBLocks() {
      int radius = 10;

      for (int x = -radius; x <= radius; x++) {
         for (int y = -radius; y <= radius; y++) {
            for (int z = -radius; z <= radius; z++) {
               BlockPos blockPos = this.getOrigin().offset(x, y, z);
               BlockState blockState = this.owner.level().getBlockState(blockPos);
               if (!(blockState.getBlock() instanceof BlockPropolis) && !(blockState.getBlock() instanceof BlockVeswax)) {
                  if (blockState.getBlock() == this.dungeonTheme.getPit('1', this.owner.getRandom()).getBlock()) {
                     this.owner.level().removeBlock(blockPos, false);
                  } else if (blockState.getBlock() == this.dungeonTheme.getPit('2', this.owner.getRandom()).getBlock()) {
                     this.owner.level().removeBlock(blockPos, false);
                  }
               }
            }
         }
      }
   }

   protected boolean isStructureBlock(BlockPos blockPos) {
      BlockState targetState = this.owner.level().getBlockState(blockPos);
      Block targetBlock = targetState.getBlock();
      if (targetBlock == Blocks.AIR) {
         return false;
      } else {
         for (ThemeBlock themeBlock : this.dungeonTheme.ceilingBlocks) {
            if (targetBlock == themeBlock.getBlockState().getBlock()) {
               return true;
            }
         }

         for (ThemeBlock themeBlockx : this.dungeonTheme.floorBlocks) {
            if (targetBlock == themeBlockx.getBlockState().getBlock()) {
               return true;
            }
         }

         for (ThemeBlock themeBlockxx : this.dungeonTheme.wallBlocks) {
            if (targetBlock == themeBlockxx.getBlockState().getBlock()) {
               return true;
            }
         }

         return false;
      }
   }
}
