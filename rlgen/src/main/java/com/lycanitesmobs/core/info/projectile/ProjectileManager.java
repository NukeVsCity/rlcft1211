package com.lycanitesmobs.core.info.projectile;

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
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.core.JSONLoader;
import com.lycanitesmobs.core.entity.EntityProjectileModel;
import com.lycanitesmobs.core.entity.LaserEndProjectileEntity;
import com.lycanitesmobs.core.entity.PortalEntity;
import com.lycanitesmobs.core.entity.projectile.EntityDevilGatling;
import com.lycanitesmobs.core.entity.projectile.EntityHellLaser;
import com.lycanitesmobs.core.entity.projectile.EntityHellLaserEnd;
import com.lycanitesmobs.core.entity.projectile.EntityHellShield;
import com.lycanitesmobs.core.entity.projectile.EntityHellfireBarrier;
import com.lycanitesmobs.core.entity.projectile.EntityHellfireBarrierPart;
import com.lycanitesmobs.core.entity.projectile.EntityHellfireOrb;
import com.lycanitesmobs.core.entity.projectile.EntityHellfireWall;
import com.lycanitesmobs.core.entity.projectile.EntityHellfireWave;
import com.lycanitesmobs.core.entity.projectile.EntityHellfireWavePart;
import com.lycanitesmobs.core.entity.projectile.EntityShadowfireBarrier;
import com.lycanitesmobs.core.info.ModInfo;
import java.util.HashMap;
import java.util.Map;


public class ProjectileManager extends JSONLoader {
   public static ProjectileManager INSTANCE;
   public Map<String, ProjectileInfo> projectiles = new HashMap<>();
   public Map<String, Class<? extends Entity>> oldSpriteProjectiles = new HashMap<>();
   public Map<String, Class<? extends Entity>> oldModelProjectiles = new HashMap<>();
   protected int nextProjectileNetworkId = 1000;

   public static ProjectileManager getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new ProjectileManager();
      }

      return INSTANCE;
   }

   public void startup(ModInfo modInfo) {
      this.loadAllFromJSON(modInfo);

      for (ProjectileInfo projectileInfo : this.projectiles.values()) {
         projectileInfo.load();
      }

      this.loadOldProjectiles();
   }

   public void loadAllFromJSON(ModInfo groupInfo) {
      try {
         this.loadAllJson(groupInfo, "Projectile", "projectiles", "name", true);
         LycanitesMobs.logDebug("Projectile", "Complete! " + this.projectiles.size() + " JSON Projectile Info Loaded In Total.");
      } catch (Exception var3) {
         LycanitesMobs.logWarning("", "No Projectiles loaded for: " + groupInfo.name);
      }
   }

   @Override
   public void parseJson(ModInfo modInfo, String name, JsonObject json) {
      ProjectileInfo projectileInfo = new ProjectileInfo(modInfo);
      projectileInfo.loadFromJSON(json);
      if (projectileInfo.name == null) {
         LycanitesMobs.logWarning("", "[Projectile] Unable to load " + name + " json due to missing name.");
      } else {
         if (this.projectiles.containsKey(projectileInfo.name)) {
            projectileInfo = this.projectiles.get(projectileInfo.name);
            projectileInfo.loadFromJSON(json);
         }

         this.projectiles.put(projectileInfo.name, projectileInfo);
      }
   }

   public int getNextProjectileNetworkId() {
      return this.nextProjectileNetworkId++;
   }

   @SubscribeEvent
   public void registerEntities(Register<EntityEntry> event) {
      ModInfo modInfo = LycanitesMobs.modInfo;
      LycanitesMobs.logDebug("Projectile", "Forge registering all " + this.projectiles.size() + " projectiles from the mod: " + modInfo.name + "...");

      for (ProjectileInfo projectileInfo : this.projectiles.values()) {
         if (projectileInfo.modInfo == modInfo) {
            EntityEntry entityEntry = EntityEntryBuilder.create()
               .entity(projectileInfo.entityClass)
               .id(projectileInfo.getId(), this.getNextProjectileNetworkId())
               .name(projectileInfo.getId())
               .tracker(40, 3, true)
               .build();
            event.getRegistry().register(entityEntry);
         }
      }

      for (String entityName : this.oldSpriteProjectiles.keySet()) {
         String registryName = LycanitesMobs.modInfo.modid + ":" + entityName;
         EntityEntry entityEntry = EntityEntryBuilder.create()
            .entity(this.oldSpriteProjectiles.get(entityName))
            .id(registryName, this.getNextProjectileNetworkId())
            .name(registryName)
            .tracker(40, 3, true)
            .build();
         event.getRegistry().register(entityEntry);
      }

      for (String entityName : this.oldModelProjectiles.keySet()) {
         String registryName = LycanitesMobs.modInfo.modid + ":" + entityName;
         EntityEntry entityEntry = EntityEntryBuilder.create()
            .entity(this.oldModelProjectiles.get(entityName))
            .id(registryName, this.getNextProjectileNetworkId())
            .name(registryName)
            .tracker(40, 3, true)
            .build();
         event.getRegistry().register(entityEntry);
      }
   }

   public ProjectileInfo getProjectile(String projectileName) {
      return !this.projectiles.containsKey(projectileName) ? null : this.projectiles.get(projectileName);
   }

   public void loadOldProjectiles() {
      this.addOldProjectile("summoningportal", PortalEntity.class);
      this.addOldProjectile("laserend", LaserEndProjectileEntity.class);
      this.addOldProjectile("shadowfirebarrier", EntityShadowfireBarrier.class, false);
      this.addOldProjectile("hellfirewall", EntityHellfireWall.class, false);
      this.addOldProjectile("hellfireorb", EntityHellfireOrb.class, false);
      this.addOldProjectile("hellfirewave", EntityHellfireWave.class, false);
      this.addOldProjectile("hellfirewavepart", EntityHellfireWavePart.class, false);
      this.addOldProjectile("hellfirebarrier", EntityHellfireBarrier.class, false);
      this.addOldProjectile("hellfirebarrierpart", EntityHellfireBarrierPart.class, false);
      this.addOldProjectile("devilgatling", EntityDevilGatling.class, false);
      this.addOldProjectile("hellshield", EntityHellShield.class, false);
      this.addOldProjectile("helllaser", EntityHellLaser.class, false);
      this.addOldProjectile("helllaserend", EntityHellLaserEnd.class, false);
   }

   public void addOldProjectile(String name, Class<? extends Entity> entityClass) {
      if (EntityProjectileModel.class.isAssignableFrom(entityClass)) {
         this.oldModelProjectiles.put(name, entityClass);
      } else {
         this.oldSpriteProjectiles.put(name, entityClass);
      }
   }

   public void addOldProjectile(String name, Class<? extends Entity> entityClass, boolean impactSound) {
      ModInfo modInfo = LycanitesMobs.modInfo;
      AssetManager.addSound(name, modInfo, "projectile." + name);
      if (impactSound) {
         AssetManager.addSound(name + "_impact", modInfo, "projectile." + name + ".impact");
      }

      this.addOldProjectile(name, entityClass);
   }
}
