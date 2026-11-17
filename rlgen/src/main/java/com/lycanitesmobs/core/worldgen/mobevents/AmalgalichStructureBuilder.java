package com.lycanitesmobs.core.worldgen.mobevents;

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
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.block.BlockFireBase;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.creature.EntityAmalgalich;
import com.lycanitesmobs.core.entity.projectile.EntityShadowfireBarrier;
import com.lycanitesmobs.core.mobevent.MobEventPlayerServer;
import com.lycanitesmobs.core.mobevent.effects.StructureBuilder;
import java.util.ArrayList;
import java.util.List;


public class AmalgalichStructureBuilder extends StructureBuilder {
   public AmalgalichStructureBuilder() {
      this.name = "amalgalich";
   }

   @Override
   public void build(Level world, Player player, BlockPos pos, int level, int ticks, int variant) {
      ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);
      int originX = pos.getX();
      int originY = pos.getY();
      int originZ = pos.getZ();
      originX += 20;
      int height = 40;
      if (originY < 5) {
         originY = 5;
      }

      if (world.getHeight() <= height) {
         originY = 5;
      } else if (originY + height >= world.getHeight()) {
         originY = Math.max(5, world.getHeight() - height - 1);
      }

      if (ticks == 1) {
         for (int i = 0; i < 5; i++) {
            BaseProjectileEntity baseProjectileEntity = new EntityShadowfireBarrier(world, (double)originX, (double)(originY + 10 * i), (double)originZ);
            baseProjectileEntity.projectileLife = 400;
            world.addFreshEntity(baseProjectileEntity);
            if (worldExt != null) {
               worldExt.bossUpdate(baseProjectileEntity);
            }
         }
      }

      if (ticks == 60) {
         this.buildArenaFloor(world, originX, originY, originZ);
      }

      if (ticks == 100) {
         this.buildObstacles(world, originX, originY, originZ);
      }

      if (ticks >= 200 && ticks % 10 == 0) {
         LycWorld.createExplosion(world, 
            null,
            (double)(originX - 20 + world.random.nextInt(40)),
            (double)(originY + 25 + world.random.nextInt(10)),
            (double)(originZ - 20 + world.random.nextInt(40)),
            2.0F,
            true
         );
      }

      if (ticks == 400) {
         BaseCreatureEntity baseCreatureEntity = new EntityAmalgalich(world);
         baseCreatureEntity.moveTo((double)originX, (double)(originY + 1), (double)originZ, 0.0F, 0.0F);
         world.addFreshEntity(baseCreatureEntity);
         baseCreatureEntity.setArenaCenter(LycPos.of(originX, originY + 1, originZ));
         if (worldExt != null) {
            MobEventPlayerServer mobEventPlayerServer = worldExt.getMobEventPlayerServer(this.name);
            if (mobEventPlayerServer != null) {
               mobEventPlayerServer.mobEvent.onSpawn(baseCreatureEntity, world, player, pos, level, ticks, variant);
            }
         }
      }
   }

   public void buildArenaFloor(Level world, int originX, int originY, int originZ) {
      int radius = 60;
      int height = 40;
      Block primaryBlock = ObjectManager.getBlock("shadowstonetile");
      Block secondaryBlock = ObjectManager.getBlock("shadowcrystal");
      double secondaryChance = 0.05;
      int stripNumber = 1;

      for (int x = originX - radius; x < originX + radius; x++) {
         float stripNormal = (float)stripNumber / (float)radius;
         if (stripNumber > radius) {
            stripNormal = (float)(radius - (stripNumber - radius)) / (float)radius;
         }

         int stripRadius = Math.round((float)radius * (float)Math.sin(Math.toRadians((double)(90.0F * stripNormal))));

         for (int z = originZ - stripRadius; z < originZ + stripRadius; z++) {
            Block buildBlock = primaryBlock;
            if (world.random.nextDouble() <= secondaryChance) {
               buildBlock = secondaryBlock;
            }

            world.setBlock(LycPos.of(x, originY, z), buildBlock.defaultBlockState(), 2);
            world.setBlock(LycPos.of(x, originY - 1, z), buildBlock.defaultBlockState(), 2);
            world.setBlock(LycPos.of(x, originY - 2, z), buildBlock.defaultBlockState(), 2);

            for (int y = originY + 1; y <= originY + height && y < world.getHeight(); y++) {
               world.setBlock(LycPos.of(x, y, z), Blocks.AIR.defaultBlockState(), 2);
            }
         }

         stripNumber++;
      }
   }

   public void buildObstacles(Level world, int originX, int originY, int originZ) {
      double angle = 0.0;
      int radius = 50;
      List<int[]> decorationCoords = new ArrayList<>();

      while (angle < 360.0) {
         angle += 5.0 + 5.0 * world.random.nextDouble();
         double angleRadians = Math.toRadians(angle);
         double x = (double)radius * Math.cos(angleRadians) - Math.sin(angleRadians);
         double z = (double)radius * Math.sin(angleRadians) + Math.cos(angleRadians);
         decorationCoords.add(this.buildPillar(world, originX + (int)Math.ceil(x), originY, originZ + (int)Math.ceil(z)));
      }
   }

   public int[] buildPillar(Level world, int originX, int originY, int originZ) {
      int radiusMax = 6;
      int height = 30;
      Block primaryBlock = ObjectManager.getBlock("shadowstonebrick");
      Block secondaryBlock = ObjectManager.getBlock("shadowstone");
      Block tetriaryBlock = ObjectManager.getBlock("shadowstonechiseled");
      Block capBlock = Blocks.OBSIDIAN;
      Block hazardBlock = ObjectManager.getBlock("shadowfire");
      double secondaryChance = 0.4;
      double tetriaryChance = 0.05;
      int[] decorationCoord = new int[]{originX, originY, originZ};
      int radius = radiusMax;
      int radiusHeight = height - 10;

      for (int y = originY; y <= originY + height; y++) {
         radiusHeight--;
         boolean lowerRadius = radiusHeight <= 0;
         int stripNumber = 1;

         for (int x = originX - radius; x <= originX + radius; x++) {
            float stripNormal = (float)stripNumber / (float)radius;
            if (stripNumber > radius) {
               stripNormal = (float)(radius - (stripNumber - radius)) / (float)radius;
            }

            int stripRadius = Math.round((float)radius * (float)Math.sin(Math.toRadians((double)(90.0F * stripNormal))));

            for (int z = originZ - stripRadius; z <= originZ + stripRadius; z++) {
               if (!lowerRadius && y != originY + height) {
                  if (radiusHeight - 1 <= 0 || y == originY + height - 1) {
                     world.setBlock(LycPos.of(x, y, z), capBlock.defaultBlockState(), 2);
                  } else if (world.random.nextDouble() > secondaryChance) {
                     world.setBlock(LycPos.of(x, y, z), primaryBlock.defaultBlockState(), 2);
                  } else if (world.random.nextDouble() > tetriaryChance) {
                     world.setBlock(LycPos.of(x, y, z), secondaryBlock.defaultBlockState(), 2);
                  } else {
                     world.setBlock(LycPos.of(x, y, z), tetriaryBlock.defaultBlockState(), 2);
                  }
               } else {
                  world.setBlock(LycPos.of(x, y, z), hazardBlock.defaultBlockState().setValue(BlockFireBase.STATIC, true), 2);
               }
            }

            stripNumber++;
         }

         if (lowerRadius) {
            radiusHeight = radiusMax;
            radius--;
         }
      }

      return decorationCoord;
   }

   public void buildDecoration(Level world, int originX, int originY, int originZ) {
      Block primaryBlock = Blocks.OBSIDIAN;
      Block hazardBlock = ObjectManager.getBlock("shadowfire");
      world.setBlock(LycPos.of(originX, originY + 1, originZ), primaryBlock.defaultBlockState(), 2);
      world.setBlock(LycPos.of(originX, originY + 2, originZ), primaryBlock.defaultBlockState(), 2);
      world.setBlock(LycPos.of(originX, originY + 3, originZ), hazardBlock.defaultBlockState().setValue(BlockFireBase.STATIC, true), 2);
      world.setBlock(LycPos.of(originX + 1, originY + 1, originZ), primaryBlock.defaultBlockState(), 2);
      world.setBlock(LycPos.of(originX + 1, originY + 2, originZ), hazardBlock.defaultBlockState().setValue(BlockFireBase.STATIC, true), 2);
      world.setBlock(LycPos.of(originX - 1, originY + 1, originZ), primaryBlock.defaultBlockState(), 2);
      world.setBlock(LycPos.of(originX - 1, originY + 2, originZ), hazardBlock.defaultBlockState().setValue(BlockFireBase.STATIC, true), 2);
      world.setBlock(LycPos.of(originX, originY + 1, originZ + 1), primaryBlock.defaultBlockState(), 2);
      world.setBlock(LycPos.of(originX, originY + 2, originZ + 1), hazardBlock.defaultBlockState().setValue(BlockFireBase.STATIC, true), 2);
      world.setBlock(LycPos.of(originX, originY + 1, originZ - 1), primaryBlock.defaultBlockState(), 2);
      world.setBlock(LycPos.of(originX, originY + 2, originZ - 1), hazardBlock.defaultBlockState().setValue(BlockFireBase.STATIC, true), 2);
   }
}
