package com.lycanitesmobs.core.spawner.location;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
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
import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import java.util.ArrayList;
import java.util.List;


public class RandomSpawnLocation extends BlockSpawnLocation {
   protected int limit = 32;
   protected boolean solidGround = false;
   protected double easyDifficultyRangeScale = 1.5;
   protected double normalDifficultyRangeScale = 1.0;
   protected double hardDifficultyRangeScale = 0.5;

   @Override
   public void loadFromJSON(JsonObject json) {
      if (json.has("limit")) {
         this.limit = json.get("limit").getAsInt();
      }

      if (json.has("solidGround")) {
         this.solidGround = json.get("solidGround").getAsBoolean();
      }

      if (json.has("easyDifficultyRangeScale")) {
         this.easyDifficultyRangeScale = json.get("easyDifficultyRangeScale").getAsDouble();
      }

      if (json.has("normalDifficultyRangeScale")) {
         this.normalDifficultyRangeScale = json.get("normalDifficultyRangeScale").getAsDouble();
      }

      if (json.has("hardDifficultyRangeScale")) {
         this.hardDifficultyRangeScale = json.get("hardDifficultyRangeScale").getAsDouble();
      }

      this.listType = "whitelist";
      this.blockIds.add(LycReg.key(Blocks.AIR).toString());
      this.blockIds.add(LycReg.key(Blocks.SHORT_GRASS).toString());
      super.loadFromJSON(json);
   }

   @Override
   public List<BlockPos> getSpawnPositions(Level world, Player player, BlockPos triggerPos) {
      List<BlockPos> spawnPositions = new ArrayList<>();
      LycanitesMobs.logDebug("JSONSpawner", "Getting " + this.limit + " RandomSource Spawn Positions");

      for (int i = 0; i < this.limit; i++) {
         BlockPos randomPos = this.getRandomPosition(world, player, triggerPos);
         if (randomPos != null) {
            spawnPositions.add(randomPos);
         }
      }

      return this.sortSpawnPositions(spawnPositions, world, triggerPos);
   }

   public BlockPos getRandomPosition(Level world, Player player, BlockPos triggerPos) {
      int[] xz = this.getRandomXZCoord(world, triggerPos);
      int x = xz[0];
      int z = xz[1];
      int y = this.getRandomYCoord(world, LycPos.of(x, triggerPos.getY(), z));
      return y > -1 ? LycPos.of(x, y, z) : null;
   }

   public int[] getRandomXZCoord(Level world, BlockPos triggerPos) {
      double difficultyScale = this.normalDifficultyRangeScale;
      if (world.getDifficulty().getId() <= 1) {
         difficultyScale = this.easyDifficultyRangeScale;
      } else if (world.getDifficulty().getId() >= 3) {
         difficultyScale = this.hardDifficultyRangeScale;
      }

      int xPos = 0;
      int rangeMaxX = Math.round((float)this.rangeMax.getX() * (float)difficultyScale);
      int rangeMinX = Math.round((float)this.rangeMin.getX() * (float)difficultyScale);
      if ((double)rangeMaxX * difficultyScale > 0.0) {
         xPos = world.random.nextInt(rangeMaxX);
         if (world.random.nextBoolean()) {
            xPos += rangeMinX;
         } else {
            xPos = -xPos - rangeMinX;
         }
      }

      int zPos = 0;
      int rangeMaxZ = Math.round((float)this.rangeMax.getZ() * (float)difficultyScale);
      int rangeMinZ = Math.round((float)this.rangeMin.getZ() * (float)difficultyScale);
      if ((double)rangeMaxZ * difficultyScale > 0.0) {
         zPos = world.random.nextInt(rangeMaxZ);
         if (world.random.nextBoolean()) {
            zPos += rangeMinZ;
         } else {
            zPos = -zPos - rangeMinZ;
         }
      }

      return new int[]{triggerPos.getX() + xPos, triggerPos.getZ() + zPos};
   }

   public int getRandomYCoord(Level world, BlockPos triggerPos) {
      double difficultyScale = this.normalDifficultyRangeScale;
      if (world.getDifficulty().getId() <= 1) {
         difficultyScale = this.easyDifficultyRangeScale;
      } else if (world.getDifficulty().getId() >= 3) {
         difficultyScale = this.hardDifficultyRangeScale;
      }

      int rangeMaxY = Math.round((float)this.rangeMax.getY() * (float)difficultyScale);
      int rangeMinY = Math.round((float)this.rangeMin.getY() * (float)difficultyScale);
      int originX = triggerPos.getX();
      int originY = triggerPos.getY();
      int originZ = triggerPos.getZ();
      int minY = Math.max(originY - rangeMaxY, 0);
      if (this.yMin >= 0) {
         minY = Math.max(minY, this.yMin);
      }

      int maxY = Math.min(originY + rangeMaxY, world.getHeight() - 1);
      if (this.yMax >= 0) {
         maxY = Math.min(maxY, this.yMax);
      }

      List<Integer> yCoordsLow = new ArrayList<>();
      List<Integer> yCoordsHigh = new ArrayList<>();

      for (int nextY = minY; nextY <= maxY; nextY++) {
         if (nextY > originY - rangeMinY && nextY < originY + rangeMinY) {
            nextY = originY + rangeMinY;
         }

         BlockPos spawnPos = LycPos.of(originX, nextY, originZ);
         if (this.isValidBlock(world, spawnPos)) {
            boolean lastYPos = false;
            if (world.canSeeSky(spawnPos)) {
               if (!this.solidGround) {
                  int floatRange = maxY - nextY;
                  if (floatRange > 1) {
                     if (world.getBlockState(spawnPos).getBlock() != Blocks.AIR) {
                        floatRange = this.getValidBlockHeight(world, spawnPos, maxY);
                     }

                     nextY += world.random.nextInt(floatRange + 1) - 1;
                  }
               }

               lastYPos = true;
            }

            if (super.isValidBlock(world, spawnPos.above())) {
               if (nextY <= 64) {
                  yCoordsLow.add(nextY);
               } else {
                  yCoordsHigh.add(nextY);
               }
            }

            if (lastYPos) {
               break;
            }
         }
      }

      int y = -1;
      if (yCoordsHigh.size() > 0 && (yCoordsLow.size() <= 0 || world.random.nextFloat() > 0.25F)) {
         if (yCoordsHigh.size() == 1) {
            y = yCoordsHigh.get(0);
         } else {
            y = yCoordsHigh.get(world.random.nextInt(yCoordsHigh.size() - 1));
         }
      } else if (yCoordsLow.size() > 0) {
         if (yCoordsLow.size() == 1) {
            y = yCoordsLow.get(0);
         } else {
            y = yCoordsLow.get(world.random.nextInt(yCoordsLow.size() - 1));
         }
      }

      return y;
   }

   @Override
   public boolean isValidBlock(Level world, BlockPos blockPos) {
      if (!super.isValidBlock(world, blockPos)) {
         return false;
      } else {
         return this.solidGround ? this.posHasGround(world, blockPos) : true;
      }
   }

   public boolean posHasGround(Level world, BlockPos pos) {
      if (pos != null && pos.getY() != 0) {
         BlockState possibleGroundBlockState = world.getBlockState(pos.below());

         try {
            if (possibleGroundBlockState.isNormalCube()) {
               return true;
            }
         } catch (Exception var6) {
         }

         try {
            if (possibleGroundBlockState.getBlock() == Blocks.SNOW_BLOCK) {
               return true;
            }

            if (possibleGroundBlockState.isSideSolid(world, pos.below(), Direction.UP)) {
               return true;
            }

            if (possibleGroundBlockState.isSideSolid(world, pos.below(), Direction.DOWN)) {
               return true;
            }
         } catch (Exception var5) {
         }

         return false;
      } else {
         return false;
      }
   }

   public int getValidBlockHeight(Level world, BlockPos startPos, int maxY) {
      int y;
      for (y = startPos.getY(); y <= maxY; y++) {
         BlockPos checkPos = LycPos.of(startPos.getX(), y, startPos.getZ());
         if (!this.isValidBlock(world, checkPos)) {
            break;
         }
      }

      return y - startPos.getY();
   }
}
