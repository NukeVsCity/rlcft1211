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
import com.lycanitesmobs.core.helpers.JSONHelper;
import com.lycanitesmobs.core.spawner.CoordSorterFurthest;
import com.lycanitesmobs.core.spawner.CoordSorterNearest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


public class SpawnLocation {
   public Vec3i rangeMin = new Vec3i(0, 0, 0);
   public Vec3i rangeMax = new Vec3i(0, 0, 0);
   public int yMin = -1;
   public int yMax = -1;
   public String sorting = "difficulty";

   public static SpawnLocation createFromJSON(JsonObject json) {
      String type = json.get("type").getAsString();
      SpawnLocation spawnLocation = null;
      if ("base".equalsIgnoreCase(type)) {
         spawnLocation = new SpawnLocation();
      } else if ("random".equalsIgnoreCase(type)) {
         spawnLocation = new RandomSpawnLocation();
      } else if ("block".equalsIgnoreCase(type)) {
         spawnLocation = new BlockSpawnLocation();
      } else if ("material".equalsIgnoreCase(type)) {
         spawnLocation = new MaterialSpawnLocation();
      } else if ("village".equalsIgnoreCase(type)) {
         spawnLocation = new VillageSpawnLocation();
      } else if ("structure".equalsIgnoreCase(type)) {
         spawnLocation = new StructureSpawnLocation();
      }

      spawnLocation.loadFromJSON(json);
      return spawnLocation;
   }

   public void loadFromJSON(JsonObject json) {
      this.rangeMin = JSONHelper.getVec3i(json, "rangeMin");
      this.rangeMax = JSONHelper.getVec3i(json, "rangeMax");
      if (json.has("yMin")) {
         this.yMin = json.get("yMin").getAsInt();
      }

      if (json.has("yMax")) {
         this.yMax = json.get("yMax").getAsInt();
      }

      if (json.has("sorting")) {
         this.sorting = json.get("sorting").getAsString();
      }
   }

   public List<BlockPos> getSpawnPositions(Level world, Player player, BlockPos triggerPos) {
      List<BlockPos> spawnPositions = new ArrayList<>();
      int yPos = this.getOffset(world.random, this.rangeMin.getY(), this.rangeMax.getY());
      if ((this.yMax < 0 || yPos <= this.yMax) && (this.yMin < 0 || yPos >= this.yMin)) {
         Vec3i offset = new Vec3i(
            this.getOffset(world.random, this.rangeMin.getX(), this.rangeMax.getX()),
            yPos,
            this.getOffset(world.random, this.rangeMin.getZ(), this.rangeMax.getZ())
         );
         spawnPositions.add(triggerPos.offset(offset));
      }

      return this.sortSpawnPositions(spawnPositions, world, triggerPos);
   }

   public int getOffset(RandomSource random, int min, int max) {
      if (max <= min) {
         return 0;
      } else {
         int offset = min + random.nextInt(max - min);
         if (random.nextBoolean()) {
            offset = -offset;
         }

         return offset;
      }
   }

   public List<BlockPos> sortSpawnPositions(List<BlockPos> spawnPositions, Level world, BlockPos triggerPos) {
      String sorting = this.sorting;
      if ("difficulty".equalsIgnoreCase(this.sorting)) {
         sorting = "random";
         if (world.getDifficulty().getId() <= 1) {
            sorting = "far";
         } else if (world.getDifficulty().getId() >= 3) {
            sorting = "near";
         }
      }

      if ("random".equalsIgnoreCase(sorting)) {
         Collections.shuffle(spawnPositions);
      } else if ("near".equalsIgnoreCase(sorting)) {
         Collections.sort(spawnPositions, new CoordSorterNearest(triggerPos));
      } else if ("far".equalsIgnoreCase(sorting)) {
         Collections.sort(spawnPositions, new CoordSorterFurthest(triggerPos));
      }

      return spawnPositions;
   }
}
