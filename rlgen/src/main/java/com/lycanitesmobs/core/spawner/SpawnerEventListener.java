package com.lycanitesmobs.core.spawner;

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
import com.lycanitesmobs.core.spawner.trigger.BlockSpawnTrigger;
import com.lycanitesmobs.core.spawner.trigger.ChunkSpawnTrigger;
import com.lycanitesmobs.core.spawner.trigger.EntitySpawnedSpawnTrigger;
import com.lycanitesmobs.core.spawner.trigger.ExplosionSpawnTrigger;
import com.lycanitesmobs.core.spawner.trigger.FishingSpawnTrigger;
import com.lycanitesmobs.core.spawner.trigger.KillSpawnTrigger;
import com.lycanitesmobs.core.spawner.trigger.MixBlockSpawnTrigger;
import com.lycanitesmobs.core.spawner.trigger.MobEventSpawnTrigger;
import com.lycanitesmobs.core.spawner.trigger.PlayerSpawnTrigger;
import com.lycanitesmobs.core.spawner.trigger.SleepSpawnTrigger;
import com.lycanitesmobs.core.spawner.trigger.SpawnTrigger;
import com.lycanitesmobs.core.spawner.trigger.WorldSpawnTrigger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;


public class SpawnerEventListener {
   protected static SpawnerEventListener INSTANCE;
   public static boolean testOnCreative = false;
   public List<WorldSpawnTrigger> worldSpawnTriggers = new ArrayList<>();
   public List<PlayerSpawnTrigger> playerSpawnTriggers = new ArrayList<>();
   public List<KillSpawnTrigger> killSpawnTriggers = new ArrayList<>();
   public List<EntitySpawnedSpawnTrigger> entitySpawnedSpawnTriggers = new ArrayList<>();
   public List<ChunkSpawnTrigger> chunkSpawnTriggers = new ArrayList<>();
   public List<BlockSpawnTrigger> blockSpawnTriggers = new ArrayList<>();
   public List<SleepSpawnTrigger> sleepSpawnTriggers = new ArrayList<>();
   public List<FishingSpawnTrigger> fishingSpawnTriggers = new ArrayList<>();
   public List<ExplosionSpawnTrigger> explosionSpawnTriggers = new ArrayList<>();
   public List<MobEventSpawnTrigger> mobEventSpawnTriggers = new ArrayList<>();
   public List<MixBlockSpawnTrigger> mixBlockSpawnTriggers = new ArrayList<>();
   private final Map<Player, Long> playerUpdateTicks = new WeakHashMap<>();
   public boolean chunkSpawnTriggersActive = false;

   public static SpawnerEventListener getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new SpawnerEventListener();
      }

      return INSTANCE;
   }

   public boolean addTrigger(SpawnTrigger spawnTrigger) {
      if (spawnTrigger instanceof WorldSpawnTrigger && !this.worldSpawnTriggers.contains(spawnTrigger)) {
         this.worldSpawnTriggers.add((WorldSpawnTrigger)spawnTrigger);
         return true;
      } else if (spawnTrigger instanceof PlayerSpawnTrigger && !this.playerSpawnTriggers.contains(spawnTrigger)) {
         this.playerSpawnTriggers.add((PlayerSpawnTrigger)spawnTrigger);
         return true;
      } else if (spawnTrigger instanceof KillSpawnTrigger && !this.killSpawnTriggers.contains(spawnTrigger)) {
         this.killSpawnTriggers.add((KillSpawnTrigger)spawnTrigger);
         return true;
      } else if (spawnTrigger instanceof EntitySpawnedSpawnTrigger && !this.entitySpawnedSpawnTriggers.contains(spawnTrigger)) {
         this.entitySpawnedSpawnTriggers.add((EntitySpawnedSpawnTrigger)spawnTrigger);
         return true;
      } else if (spawnTrigger instanceof ChunkSpawnTrigger && !this.chunkSpawnTriggers.contains(spawnTrigger)) {
         this.chunkSpawnTriggers.add((ChunkSpawnTrigger)spawnTrigger);
         return true;
      } else if (spawnTrigger instanceof MixBlockSpawnTrigger && !this.mixBlockSpawnTriggers.contains(spawnTrigger)) {
         this.mixBlockSpawnTriggers.add((MixBlockSpawnTrigger)spawnTrigger);
         return true;
      } else if (spawnTrigger instanceof SleepSpawnTrigger && !this.sleepSpawnTriggers.contains(spawnTrigger)) {
         this.sleepSpawnTriggers.add((SleepSpawnTrigger)spawnTrigger);
         return true;
      } else if (spawnTrigger instanceof BlockSpawnTrigger && !this.blockSpawnTriggers.contains(spawnTrigger)) {
         this.blockSpawnTriggers.add((BlockSpawnTrigger)spawnTrigger);
         return true;
      } else if (spawnTrigger instanceof FishingSpawnTrigger && !this.fishingSpawnTriggers.contains(spawnTrigger)) {
         this.fishingSpawnTriggers.add((FishingSpawnTrigger)spawnTrigger);
         return true;
      } else if (spawnTrigger instanceof ExplosionSpawnTrigger && !this.explosionSpawnTriggers.contains(spawnTrigger)) {
         this.explosionSpawnTriggers.add((ExplosionSpawnTrigger)spawnTrigger);
         return true;
      } else if (spawnTrigger instanceof MobEventSpawnTrigger && !this.mobEventSpawnTriggers.contains(spawnTrigger)) {
         this.mobEventSpawnTriggers.add((MobEventSpawnTrigger)spawnTrigger);
         return true;
      } else {
         return false;
      }
   }

   public void removeTrigger(SpawnTrigger spawnTrigger) {
      if (this.worldSpawnTriggers.contains(spawnTrigger)) {
         this.worldSpawnTriggers.remove(spawnTrigger);
      }

      if (this.playerSpawnTriggers.contains(spawnTrigger)) {
         this.playerSpawnTriggers.remove(spawnTrigger);
      }

      if (this.killSpawnTriggers.contains(spawnTrigger)) {
         this.killSpawnTriggers.remove(spawnTrigger);
      }

      if (this.entitySpawnedSpawnTriggers.contains(spawnTrigger)) {
         this.entitySpawnedSpawnTriggers.remove(spawnTrigger);
      }

      if (this.chunkSpawnTriggers.contains(spawnTrigger)) {
         this.chunkSpawnTriggers.remove(spawnTrigger);
      }

      if (this.blockSpawnTriggers.contains(spawnTrigger)) {
         this.blockSpawnTriggers.remove(spawnTrigger);
      }

      if (this.sleepSpawnTriggers.contains(spawnTrigger)) {
         this.sleepSpawnTriggers.remove(spawnTrigger);
      }

      if (this.fishingSpawnTriggers.contains(spawnTrigger)) {
         this.fishingSpawnTriggers.remove(spawnTrigger);
      }

      if (this.explosionSpawnTriggers.contains(spawnTrigger)) {
         this.explosionSpawnTriggers.remove(spawnTrigger);
      }

      if (this.mobEventSpawnTriggers.contains(spawnTrigger)) {
         this.mobEventSpawnTriggers.remove(spawnTrigger);
      }

      if (this.mixBlockSpawnTriggers.contains(spawnTrigger)) {
         this.mixBlockSpawnTriggers.remove(spawnTrigger);
      }
   }

   /** 1.12 PopulateChunkEvent.Post: new chunks, handled on the next level tick (spawning inside a chunk load could load more chunks). */
   private final Map<Level, List<ChunkPos>> populatedChunks = new WeakHashMap<>();

   @SubscribeEvent
   public void onChunkLoad(net.neoforged.neoforge.event.level.ChunkEvent.Load event) {
      if (event.isNewChunk() && event.getLevel() instanceof ServerLevel world) {
         this.populatedChunks.computeIfAbsent(world, k -> new ArrayList<>()).add(event.getChunk().getPos());
      }
   }

   /** 1.12 WorldTickEvent (both phases; lastSpawnerTime runs it once a tick) */
   @SubscribeEvent
   public void onWorldUpdate(LevelTickEvent.Post event) {
      Level world = event.getLevel();
      if (!world.isClientSide) {
         List<ChunkPos> newChunks = this.populatedChunks.remove(world);
         if (newChunks != null) {
            for (ChunkPos chunkPos : newChunks) {
               this.onChunkPopulate(world, chunkPos);
            }
         }

         ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);
         if (worldExt != null) {
            if (worldExt.lastSpawnerTime != world.getGameTime()) {
               worldExt.lastSpawnerTime = world.getGameTime();
               List<BlockPos> triggerPositions = new ArrayList<>();

               for (Player player : world.players()) {
                  if (triggerPositions.isEmpty()) {
                     triggerPositions.add(player.blockPosition());
                  } else {
                     boolean nearOtherPlayers = false;

                     for (BlockPos triggerPosition : triggerPositions) {
                        if (LycMath.distSq(player, triggerPosition) <= 10000.0) {
                           nearOtherPlayers = true;
                        }
                     }

                     if (!nearOtherPlayers) {
                        triggerPositions.add(player.blockPosition());
                     }
                  }
               }

               for (BlockPos triggerPositionx : triggerPositions) {
                  for (WorldSpawnTrigger spawnTrigger : this.worldSpawnTriggers) {
                     spawnTrigger.onTick(world, triggerPositionx, worldExt.lastSpawnerTime);
                  }
               }

               if (worldExt.getWorldEvent() != null) {
                  for (MobEventSpawnTrigger spawnTrigger : this.mobEventSpawnTriggers) {
                     spawnTrigger.onTick(world, worldExt.serverWorldEventPlayer);
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void onPlayerTickEvent(PlayerTickEvent.Pre event) {
      {
         Player player = event.getEntity();
         if (!player.level().isClientSide) {
            long entityUpdateTick = this.playerUpdateTicks.compute(player, (k, v) -> v != null ? v + 1L : 1L) - 1L;
            int tickOffset = 0;

            for (PlayerSpawnTrigger spawnTrigger : this.playerSpawnTriggers) {
               spawnTrigger.onTick(player, entityUpdateTick - (long)tickOffset);
               tickOffset += 105;
            }
         }
      }
   }

   @SubscribeEvent
   public void onEntityDeath(LivingDeathEvent event) {
      LivingEntity killedEntity = event.getEntity();
      if (killedEntity != null
         && killedEntity.level() != null
         && !killedEntity.level().isClientSide
         && !event.isCanceled()
         && killedEntity instanceof Mob) {
         Entity killerEntity = event.getSource().getEntity();
         if (killerEntity instanceof Player) {
            for (KillSpawnTrigger spawnTrigger : this.killSpawnTriggers) {
               spawnTrigger.onKill((Player)killerEntity, (Mob)killedEntity);
            }
         }
      }
   }

   /** 1.12 LivingSpawnEvent (CheckSpawn/SpecialSpawn/AllowDespawn): no RLCraft spawner uses this trigger. */
   @SubscribeEvent
   public void onEntitySpawn(FinalizeSpawnEvent event) {
      LivingEntity spawnedEntity = event.getEntity();
      if (spawnedEntity != null
         && spawnedEntity.level() != null
         && !spawnedEntity.level().isClientSide
         && !event.isCanceled()
         && spawnedEntity instanceof Mob) {
         for (EntitySpawnedSpawnTrigger spawnTrigger : this.entitySpawnedSpawnTriggers) {
            spawnTrigger.onEntitySpawned((Mob)spawnedEntity);
         }
      }
   }

   public void onChunkPopulate(Level world, ChunkPos chunkPos) {
      if (!this.chunkSpawnTriggersActive) {
         for (ChunkSpawnTrigger spawnTrigger : this.chunkSpawnTriggers) {
            if (spawnTrigger.onChunkPopulate(world, chunkPos)) {
               this.chunkSpawnTriggersActive = true;
            }
         }

         this.chunkSpawnTriggersActive = false;
      }
   }

   /** 1.12 HarvestDropsEvent: 1.21 BlockDropsEvent (fortune and silk touch from the tool) */
   @SubscribeEvent
   public void onHarvestDrops(BlockDropsEvent event) {
      Player player = event.getBreaker() instanceof Player breaker ? breaker : null;
      if (event.getState() != null && event.getLevel() != null && !event.getLevel().isClientSide && !event.isCanceled()) {
         if (player == null || testOnCreative || !player.getAbilities().instabuild) {
            Level world = event.getLevel();
            BlockPos blockPos = event.getPos();
            BlockState blockState = event.getState();
            net.minecraft.core.HolderLookup.RegistryLookup<net.minecraft.world.item.enchantment.Enchantment> enchantments =
               world.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
            int fortune = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
               enchantments.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FORTUNE), event.getTool());
            boolean silkTouch = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
               enchantments.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH), event.getTool()) > 0;

            for (BlockSpawnTrigger spawnTrigger : this.blockSpawnTriggers) {
               spawnTrigger.onBlockHarvest(world, player, blockPos, blockState, 0, fortune, silkTouch);
            }
         }
      }
   }

   @SubscribeEvent
   public void onBlockBreak(BlockEvent.BreakEvent event) {
      if (event.getState() != null && event.getLevel() instanceof Level world && !world.isClientSide && !event.isCanceled()) {
         this.onBlockBreak(world, event.getPos(), event.getState(), event.getPlayer(), 0);
      }
   }

   public void onBlockBreak(Level world, BlockPos blockPos, BlockState blockState, Player player, int chain) {
      if (player == null || testOnCreative || !player.getAbilities().instabuild) {
         for (BlockSpawnTrigger spawnTrigger : this.blockSpawnTriggers) {
            spawnTrigger.onBlockBreak(world, player, blockPos, blockState, chain);
         }
      }
   }

   @SubscribeEvent
   public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
      if (event.getPlacedBlock() != null && event.getLevel() instanceof Level world && !world.isClientSide && !event.isCanceled()) {
         this.onBlockPlace(world, event.getPos(), event.getPlacedBlock(), event.getEntity() instanceof Player placer ? placer : null, 0);
      }
   }

   public void onBlockPlace(Level world, BlockPos blockPos, BlockState blockState, Player player, int chain) {
      if (player == null || testOnCreative || !player.getAbilities().instabuild) {
         for (BlockSpawnTrigger spawnTrigger : this.blockSpawnTriggers) {
            spawnTrigger.onBlockPlace(world, player, blockPos, blockState, chain);
         }
      }
   }

   @SubscribeEvent
   public void onSleep(CanPlayerSleepEvent event) {
      Player player = event.getEntity();
      if (player != null && player.level() != null && !player.level().isClientSide) {
         Level world = player.level();
         BlockPos spawnPos = event.getPos().offset(0, 0, 1);
         if (world != null && !world.isClientSide && !world.isDay()) {
            boolean interrupted = false;

            for (SleepSpawnTrigger spawnTrigger : this.sleepSpawnTriggers) {
               if (spawnTrigger.onSleep(world, player, spawnPos, world.getBlockState(event.getPos()))) {
                  interrupted = true;
               }
            }

            if (interrupted) {
               event.setProblem(Player.BedSleepingProblem.NOT_SAFE);
            }
         }
      }
   }

   @SubscribeEvent
   public void onFished(ItemFishedEvent event) {
      Player player = event.getEntity();
      if (player != null && player.level() != null && !player.level().isClientSide && !event.isCanceled()) {
         if (player == null || testOnCreative || !player.getAbilities().instabuild) {
            Level world = player.level();
            Entity hookEntity = event.getHookEntity();

            for (FishingSpawnTrigger spawnTrigger : this.fishingSpawnTriggers) {
               spawnTrigger.onFished(world, player, hookEntity);
            }
         }
      }
   }

   @SubscribeEvent
   public void onExplosion(ExplosionEvent.Detonate event) {
      Explosion explosion = event.getExplosion();
      if (explosion != null) {
         Level world = event.getLevel();
         Player player = null;
         if (explosion.getIndirectSourceEntity() instanceof Player) {
            player = (Player)explosion.getIndirectSourceEntity();
         }

         if (player == null || testOnCreative || !player.getAbilities().instabuild) {
            for (ExplosionSpawnTrigger spawnTrigger : this.explosionSpawnTriggers) {
               spawnTrigger.onExplosion(world, player, explosion);
            }
         }
      }
   }

   @SubscribeEvent
   public void onLavaMix(BlockEvent.FluidPlaceBlockEvent event) {
      if (event.getLevel() instanceof Level world) {
         if (event.getOriginalState().getBlock() == Blocks.LAVA && event.getNewState().getBlock() == Blocks.OBSIDIAN) {
            for (MixBlockSpawnTrigger spawnTrigger : this.mixBlockSpawnTriggers) {
               spawnTrigger.onMix(world, event.getState(), event.getLiquidPos());
            }
         }
      }
   }
}
