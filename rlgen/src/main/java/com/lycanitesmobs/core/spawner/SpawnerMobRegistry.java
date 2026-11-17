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
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.info.CreatureInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class SpawnerMobRegistry {
   public static Map<String, SpawnerMobRegistry> SPAWNER_MOB_REGISTRIES = new HashMap<>();
   public Map<CreatureInfo, MobSpawn> mobSpawns = new HashMap<>();

   public static Collection<MobSpawn> getMobSpawns(String spawnerName) {
      LycanitesMobs.logDebug("JSONSpawner", "Getting Global Mobs For Spawner: " + spawnerName);
      if (SPAWNER_MOB_REGISTRIES.containsKey(spawnerName.toLowerCase())) {
         Collection<MobSpawn> mobs = SPAWNER_MOB_REGISTRIES.get(spawnerName.toLowerCase()).mobSpawns.values();
         LycanitesMobs.logDebug("JSONSpawner", "Found " + mobs.size() + " Global Mobs For Spawner: " + spawnerName);
         return mobs;
      } else {
         LycanitesMobs.logDebug("JSONSpawner", "No Global Mobs Found For Spawner: " + spawnerName);
         return null;
      }
   }

   public static void createSpawn(CreatureInfo creatureInfo, String spawnerName) {
      if (creatureInfo == null) {
         LycanitesMobs.logWarning("", "Tried to create a global Mob Spawn to " + spawnerName + " from a null Creature Info!");
      }

      if (spawnerName == null) {
         LycanitesMobs.logWarning("", "Tried to create a global Mob Spawn for " + creatureInfo.getName() + " with a null Spawner Name!");
      }

      MobSpawn mobSpawn = new MobSpawn(creatureInfo);
      if (!SPAWNER_MOB_REGISTRIES.containsKey(spawnerName)) {
         SPAWNER_MOB_REGISTRIES.put(spawnerName, new SpawnerMobRegistry());
      }

      if (!SPAWNER_MOB_REGISTRIES.get(spawnerName).addMobSpawn(mobSpawn)) {
         LycanitesMobs.logWarning("", "Tried to create a duplicate global MobSpawn for " + creatureInfo.getName() + " in Spawner: " + spawnerName + "!");
      }

      LycanitesMobs.logDebug("JSONSpawner", "Added " + creatureInfo.getName() + " to " + spawnerName + " global spawn list.");
   }

   public boolean addMobSpawn(MobSpawn mobSpawn) {
      if (this.mobSpawns.containsKey(mobSpawn.creatureInfo)) {
         return false;
      } else {
         this.mobSpawns.put(mobSpawn.creatureInfo, mobSpawn);
         return true;
      }
   }
}
