package com.lycanitesmobs.core.info;

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
import com.lycanitesmobs.core.config.ConfigBase;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;


public class CreatureSpawnConfig {
   public int typeSpawnLimit = 64;
   public double spawnLimitRange = 32.0;
   public boolean disableAllSpawning = false;
   public boolean disableDungeonSpawners = false;
   public boolean enforceBlockCost = true;
   public boolean useSurfaceLightLevel = true;
   public double spawnWeightScale = 1.0;
   public double dungeonSpawnerWeightScale = 1.0;
   public boolean ignoreWorldGenSpawning = false;
   public boolean controlVanillaSpawns = false;
   public int[] dimensionList;
   public boolean dimensionListWhitelist = false;

   public void loadConfig(ConfigBase config) {
      config.setCategoryComment(
         "Global Spawning",
         "These settings are used by everything. It is recommended to leave them as they are however low end machines might benefit from a few tweaks here."
      );
      this.typeSpawnLimit = config.getInt(
         "Global Spawning",
         "Mob Type Limit",
         this.typeSpawnLimit,
         "The limit of how many mobs of the same type (peaceful or not peaceful) can spawn within the limit search range. For individual creature species limits, see the creature json configs."
      );
      this.spawnLimitRange = config.getDouble(
         "Global Spawning",
         "Mob Limit Search Range",
         this.spawnLimitRange,
         "When spawned from a vanilla spawner, this is how far a mob should search from in blocks when checking how many of its kind have already spawned. Custom Spawners have it defined in their json file instead."
      );
      this.disableAllSpawning = config.getBool(
         "Global Spawning", "Disable Spawning", this.disableAllSpawning, "If true, all mobs from this mod will not spawn at all."
      );
      this.enforceBlockCost = config.getBool(
         "Global Spawning",
         "Enforce Block Costs",
         this.enforceBlockCost,
         "If true, mobs will double check if their required blocks are nearby, such as Cinders needing so many blocks of fire."
      );
      this.spawnWeightScale = config.getDouble(
         "Global Spawning",
         "Weight Scale",
         this.spawnWeightScale,
         "Scales the spawn weights of all mobs from this mod. For example, you can use this to quickly half the spawn rates of mobs from this mod compared to vanilla/other mod mobs by setting it to 0.5."
      );
      this.useSurfaceLightLevel = config.getBool(
         "Global Spawning",
         "Use Surface Light Level",
         this.useSurfaceLightLevel,
         "If true, when water mobs spawn, instead of checking the light level of the block the mob is spawning at, the light level of the surface (if possible) is checked. This stops mobs like Jengus from spawning at the bottom of deep rivers during the day, set to false for the old way."
      );
      this.ignoreWorldGenSpawning = config.getBool(
         "Global Spawning",
         "Ignore WorldGen Spawning",
         this.ignoreWorldGenSpawning,
         "If true, when new world chunks are generated, no mobs from this mod will pre-spawn (mobs will still attempt to spawn randomly afterwards). Set this to true if you are removing mobs from vanilla dimensions as the vanilla WorldGen spawning ignores mob spawn conditions."
      );
      this.controlVanillaSpawns = config.getBool(
         "Global Spawning",
         "Edit Vanilla Spawning",
         this.controlVanillaSpawns,
         "If true, some vanilla spawns in various biomes will be removed, note that vanilla mobs should still be easy to find, only they will be more biome specific."
      );
      String dimensionListValue = config.getString(
         "Global Spawning",
         "Master Spawn Dimensions",
         "",
         "A global comma separated list of dimension ids that overrides every other spawn setting in both the configs and json spawners. Use this to quickly stop all mobs from spawning in certain dimensions, etc."
      );
      List<Integer> dimensionEntries = new ArrayList<>();

      for (String dimensionEntry : dimensionListValue.replace(" ", "").split(",")) {
         if (NumberUtils.isCreatable(dimensionEntry)) {
            dimensionEntries.add(Integer.parseInt(dimensionEntry));
         }
      }

      this.dimensionList = ArrayUtils.toPrimitive(dimensionEntries.toArray(new Integer[dimensionEntries.size()]));
      this.dimensionListWhitelist = config.getBool(
         "Global Spawning",
         "Master Spawn Dimensions Whitelist",
         this.dimensionListWhitelist,
         "If set to true the dimension list acts as a whitelist, otherwise it is a blacklist."
      );
      config.setCategoryComment("Dungeon Features", "Here you can set special features used in dungeon generation.");
      this.disableDungeonSpawners = config.getBool(
         "Dungeon Features",
         "Disable Dungeon Spawners",
         this.disableDungeonSpawners,
         "If true, newly generated dungeons wont create spawners with mobs from this mod."
      );
      this.dungeonSpawnerWeightScale = config.getDouble(
         "Dungeon Features",
         "Dungeon Spawner Weight Scale",
         this.dungeonSpawnerWeightScale,
         "Scales the weight of dungeons using spawners from this mod. For example, you can half the chances all dungeons having spawners with mobs from this mod in them by setting this to 0.5."
      );
   }

   public boolean isAllowedGlobal(Level world) {
      if (this.disableAllSpawning) {
         return false;
      } else {
         if (this.dimensionList.length > 0) {
            boolean inDimensionList = false;

            for (int dimensionId : this.dimensionList) {
               if (dimensionId == LycDim.id(world)) {
                  inDimensionList = true;
                  break;
               }
            }

            if (inDimensionList && !this.dimensionListWhitelist) {
               return false;
            }

            if (!inDimensionList && this.dimensionListWhitelist) {
               return false;
            }
         }

         return true;
      }
   }
}
