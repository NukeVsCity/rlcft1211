package com.lycanitesmobs.core.spawner.location;

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
import com.google.gson.JsonObject;
import com.lycanitesmobs.core.helpers.JSONHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BlockSpawnLocation extends SpawnLocation {
   public List<String> blockIds = new ArrayList<>();
   public String listType = "whitelist";
   public boolean surface = true;
   public boolean underground = true;
   public int blockCost = -1;
   public int requiredBlockTypes = 0;
   public Vec3i offset = new Vec3i(0, 0, 0);

   @Override
   public void loadFromJSON(JsonObject json) {
      if (json.has("blocks")) {
         this.blockIds = JSONHelper.getJsonStrings(json.get("blocks").getAsJsonArray());
      }

      if (json.has("listType")) {
         this.listType = json.get("listType").getAsString();
      }

      if (json.has("surface")) {
         this.surface = json.get("surface").getAsBoolean();
      }

      if (json.has("underground")) {
         this.underground = json.get("underground").getAsBoolean();
      }

      if (json.has("blockCost")) {
         this.blockCost = json.get("blockCost").getAsInt();
      }

      if (json.has("requiredBlockTypes")) {
         this.requiredBlockTypes = json.get("requiredBlockTypes").getAsInt();
      }

      this.offset = JSONHelper.getVec3i(json, "offset");
      super.loadFromJSON(json);
   }

   @Override
   public List<BlockPos> getSpawnPositions(Level world, Player player, BlockPos triggerPos) {
      List<BlockPos> spawnPositions = new ArrayList<>();
      Map<Block, Integer> validBlocksFound = new HashMap<>();

      for (int y = triggerPos.getY() - this.rangeMax.getY(); y <= triggerPos.getY() + this.rangeMax.getY(); y++) {
         int yMin = 0;
         if (this.yMin >= 0) {
            yMin = this.yMin;
         }

         if (y < yMin) {
            y = yMin;
         }

         int yMax = world.getActualHeight();
         if (this.yMax >= 0) {
            yMax = Math.min(this.yMax, yMax);
         }

         if (y >= yMax) {
            break;
         }

         for (int x = triggerPos.getX() - this.rangeMax.getX(); x <= triggerPos.getX() + this.rangeMax.getX(); x++) {
            for (int z = triggerPos.getZ() - this.rangeMax.getZ(); z <= triggerPos.getZ() + this.rangeMax.getZ(); z++) {
               BlockPos spawnPos = LycPos.of(x, y, z);
               BlockState blockState = world.getBlockState(spawnPos);
               if (blockState.getBlock() instanceof IFluidBlock) {
                  float filled = ((IFluidBlock)blockState.getBlock()).getFilledPercentage(world, spawnPos);
                  if (filled != 1.0F && filled != -1.0F) {
                     continue;
                  }
               }

               if ((!(blockState.getBlock() instanceof BlockLiquid) || blockState.getBlock().getMetaFromState(blockState) == 0)
                  && this.isValidBlock(world, spawnPos)) {
                  spawnPositions.add(spawnPos.add(this.offset));
                  if (this.requiredBlockTypes > 0) {
                     Block block = world.getBlockState(spawnPos).getBlock();
                     if (!validBlocksFound.containsKey(block)) {
                        validBlocksFound.put(block, 1);
                     } else {
                        validBlocksFound.put(block, validBlocksFound.get(block) + 1);
                     }
                  }
               }
            }
         }
      }

      if (this.blockCost > 0 && spawnPositions.size() < this.blockCost) {
         return new ArrayList<>();
      } else {
         if (this.requiredBlockTypes > 0) {
            if (validBlocksFound.size() < this.blockIds.size()) {
               return new ArrayList<>();
            }

            for (int blocksFoundOfType : validBlocksFound.values()) {
               if (blocksFoundOfType < this.requiredBlockTypes) {
                  return new ArrayList<>();
               }
            }
         }

         return this.sortSpawnPositions(spawnPositions, world, triggerPos);
      }
   }

   public boolean isValidBlock(Level world, BlockPos blockPos) {
      if (!this.surface || !this.underground) {
         if (world.canSeeSky(blockPos)) {
            if (!this.surface) {
               return false;
            }
         } else if (!this.underground) {
            return false;
         }
      }

      if (this.blockIds.isEmpty()) {
         return true;
      } else {
         Block block = world.getBlockState(blockPos).getBlock();
         return "blacklist".equalsIgnoreCase(this.listType)
            ? !this.blockIds.contains(LycReg.key(block).toString())
            : this.blockIds.contains(LycReg.key(block).toString());
      }
   }
}
