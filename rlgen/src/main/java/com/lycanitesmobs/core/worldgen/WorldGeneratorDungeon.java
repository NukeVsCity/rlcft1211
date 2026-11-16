package com.lycanitesmobs.core.worldgen;

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
import com.lycanitesmobs.ExtendedWorld;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.config.ConfigBase;
import com.lycanitesmobs.core.dungeon.instance.DungeonInstance;
import java.util.List;
import java.util.Random;
import java.util.UUID;


public class WorldGeneratorDungeon implements IWorldGenerator {
   public static int DUNGEON_DISTANCE = 170;
   public static double TOWER_CHANCE = 0.25;
   public boolean enabled = true;

   public WorldGeneratorDungeon() {
      ConfigBase config = ConfigBase.getConfig(LycanitesMobs.modInfo, "general");
      this.enabled = config.getBool(
         "Dungeons",
         "Dungeons Enabled",
         this.enabled,
         "If false, all Lycanites Mobs Dungeons are disabled, set to true to enable the Dungeon System. (The JSON files are still loaded but don't do anything.)"
      );
      DUNGEON_DISTANCE = config.getInt(
         "Dungeons", "Dungeon Distance", DUNGEON_DISTANCE, "The average distance in chunks that dungeons are spaced apart from each other."
      );
      TOWER_CHANCE = config.getDouble("Dungeons", "Tower Chance", TOWER_CHANCE, "The chance of a dungeon generating a tower on top (0.25 by default).");
   }

   public void generate(RandomSource random, int chunkX, int chunkZ, Level world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
      if (this.enabled) {
         ExtendedWorld extendedWorld = ExtendedWorld.getForWorld(world);
         if (extendedWorld != null) {
            try {
               ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
               int dungeonSizeMax = DUNGEON_DISTANCE;
               List<DungeonInstance> nearbyDungeons = extendedWorld.getNearbyDungeonInstances(chunkPos, dungeonSizeMax * 2);
               if (nearbyDungeons.isEmpty()) {
                  for (int x = -1; x <= 1; x++) {
                     for (int z = -1; z <= 1; z++) {
                        if ((x != 0 || z != 0) && (x == 0 || z == 0)) {
                           LycanitesMobs.logDebug(
                              "Dungeon", "Creating A New Dungeon At Chunk: X" + (chunkX + dungeonSizeMax * x) + " Z" + (chunkZ + dungeonSizeMax * z)
                           );
                           DungeonInstance dungeonInstance = new DungeonInstance();
                           int yPos = world.getSeaLevel();
                           if (yPos < 64) {
                              yPos = 64;
                           }

                           BlockPos dungeonPos = new ChunkPos(chunkX + dungeonSizeMax * x, chunkZ + dungeonSizeMax * z).getBlock(7, yPos, 7);
                           dungeonInstance.setOrigin(dungeonPos);
                           if (dungeonInstance.init(world)) {
                              extendedWorld.addDungeonInstance(dungeonInstance, new UUID(world.random.nextLong(), world.random.nextLong()));
                           }
                        }
                     }
                  }

                  return;
               }

               for (DungeonInstance dungeonInstancex : extendedWorld.getNearbyDungeonInstances(chunkPos, 0)) {
                  dungeonInstancex.buildChunk(world, chunkPos);
               }
            } catch (Exception var16) {
               LycanitesMobs.logWarning("Dungeon", "An exception occurred when trying to generate a dungeon.");
               if (LycanitesMobs.config.getBool("Debug", "Dungeon", false)) {
                  var16.printStackTrace();
               }
            }
         }
      }
   }
}
