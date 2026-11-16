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
import com.lycanitesmobs.core.entity.creature.EntityAsmodeus;
import com.lycanitesmobs.core.entity.projectile.EntityHellfireWall;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.mobevent.MobEventPlayerServer;
import com.lycanitesmobs.core.mobevent.effects.StructureBuilder;


public class AsmodeusStructureBuilder extends StructureBuilder {
   public AsmodeusStructureBuilder() {
      this.name = "asmodeus";
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
            BaseProjectileEntity baseProjectileEntity = new EntityHellfireWall(world, (double)originX, (double)(originY + 10 * i), (double)originZ);
            baseProjectileEntity.projectileLife = 400;
            world.addFreshEntity(baseProjectileEntity);
            if (worldExt != null) {
               worldExt.bossUpdate(baseProjectileEntity);
               worldExt.overrideBossRange(baseProjectileEntity, CreatureManager.getInstance().getCreature("asmodeus").bossNearbyRange);
            }
         }
      }

      if (ticks == 60) {
         this.buildArenaFloor(world, originX, originY, originZ);
      }

      if (ticks == 100) {
         this.buildArenaWalls(world, originX, originY, originZ);
      }

      if (ticks == 140) {
         this.buildObstacles(world, originX, originY, originZ);
      }

      if (ticks >= 200 && ticks % 10 == 0) {
         world.createExplosion(
            null,
            (double)(originX - 20 + world.random.nextInt(40)),
            (double)(originY + 25 + world.random.nextInt(10)),
            (double)(originZ - 20 + world.random.nextInt(40)),
            2.0F,
            true
         );
      }

      if (ticks == 400) {
         BaseCreatureEntity baseCreatureEntity = new EntityAsmodeus(world);
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
      int radius = 80;
      int height = 30;
      int minX = originX - radius;
      int maxX = originX + radius;
      int minY = originY;
      int maxY = originY + height;
      int minZ = originZ - radius;
      int maxZ = originZ + radius;
      BlockState floor = ObjectManager.getBlock("aberrantstonetile").defaultBlockState();
      BlockState light = ObjectManager.getBlock("aberrantcrystal").defaultBlockState();
      BlockState trimming = ObjectManager.getBlock("aberrantstonechiseled").defaultBlockState();

      for (int x = minX; x <= maxX; x++) {
         for (int z = minZ; z <= maxZ; z++) {
            int topY = world.getTopSolidOrLiquidBlock(LycPos.of(x, 0, z)).getY();

            for (int y = minY; y <= maxY; y++) {
               BlockPos buildPos = LycPos.of(x, y, z);
               if (y == minY) {
                  if (x == minX || x == maxX || z == minZ || z == maxZ) {
                     world.setBlock(buildPos, trimming, 2);
                  } else if (x % 6 == 0 && z % 6 == 0) {
                     world.setBlock(buildPos, light, 2);
                  } else {
                     world.setBlock(buildPos, floor, 2);
                  }
               } else {
                  if (y > minY + 5 && y >= topY) {
                     break;
                  }

                  world.setBlock(buildPos, Blocks.AIR.defaultBlockState(), 2);
               }
            }
         }
      }
   }

   public void buildArenaWalls(Level world, int originX, int originY, int originZ) {
      int radius = 80;
      int thickness = 4;
      int height = 20;
      int minX = originX - (radius + thickness);
      int maxX = originX + radius + thickness;
      int minY = originY;
      int maxY = originY + height;
      int minZ = originZ - (radius + thickness);
      int maxZ = originZ + radius + thickness;
      BlockState base = ObjectManager.getBlock("aberrantstonebrick").defaultBlockState();
      BlockState light = ObjectManager.getBlock("aberrantcrystal").defaultBlockState();
      BlockState trimming = ObjectManager.getBlock("aberrantstonechiseled").defaultBlockState();
      BlockState top = ObjectManager.getBlock("aberrantstonepolished").defaultBlockState();
      BlockState fireBase = Blocks.OBSIDIAN.defaultBlockState();
      BlockState fire = ObjectManager.getBlock("hellfire").defaultBlockState().setValue(BlockFireBase.STATIC, true);

      for (int x = minX; x <= maxX; x++) {
         for (int z = minZ; z <= maxZ; z++) {
            if (x <= minX + thickness || x >= maxX - thickness || z <= minZ + thickness || z >= maxZ - thickness) {
               for (int y = minY; y <= maxY; y++) {
                  BlockPos buildPos = LycPos.of(x, y, z);
                  if (y < maxY - 2) {
                     if (y - 1 == 0) {
                        world.setBlock(buildPos, light, 2);
                     } else if (y % 8 == 0) {
                        world.setBlock(buildPos, trimming, 2);
                     } else {
                        world.setBlock(buildPos, base, 2);
                     }
                  } else if (y == maxY - 2) {
                     world.setBlock(buildPos, top, 2);
                  } else if (y == maxY - 1) {
                     world.setBlock(buildPos, fireBase, 2);
                  } else {
                     world.setBlock(buildPos, fire, 2);
                  }
               }
            }
         }
      }
   }

   public void buildObstacles(Level world, int originX, int originY, int originZ) {
      int gap = 20;

      for (int x = -2; x <= 2; x++) {
         for (int z = -2; z <= 2; z++) {
            if (x < -1 || x > 1 || z < -1 || z > 1) {
               this.buildPillar(world, originX + x * gap, originY, originZ + z * gap);
            }
         }
      }
   }

   public void buildPillar(Level world, int originX, int originY, int originZ) {
      int radius = 2;
      int height = 30;
      int minX = originX - radius;
      int maxX = originX + radius;
      int minY = originY;
      int maxY = originY + height;
      int minZ = originZ - radius;
      int maxZ = originZ + radius;
      BlockState base = ObjectManager.getBlock("aberrantstonepillar").defaultBlockState();
      BlockState light = ObjectManager.getBlock("aberrantcrystal").defaultBlockState();
      BlockState trimming = ObjectManager.getBlock("aberrantstonechiseled").defaultBlockState();
      BlockState top = ObjectManager.getBlock("aberrantstonepolished").defaultBlockState();
      BlockState fireBase = Blocks.OBSIDIAN.defaultBlockState();
      BlockState fire = ObjectManager.getBlock("hellfire").defaultBlockState().setValue(BlockFireBase.STATIC, true);

      for (int x = minX; x <= maxX; x++) {
         for (int z = minZ; z <= maxZ; z++) {
            for (int y = minY; y <= maxY; y++) {
               BlockPos buildPos = LycPos.of(x, y, z);
               if (y < maxY - 2) {
                  if (y % 7 == 0) {
                     world.setBlock(buildPos, light, 2);
                  } else if (y % 8 == 0) {
                     world.setBlock(buildPos, trimming, 2);
                  } else {
                     world.setBlock(buildPos, base, 2);
                  }
               } else if (y == maxY - 2) {
                  world.setBlock(buildPos, top, 2);
               } else if (y == maxY - 1) {
                  world.setBlock(buildPos, fireBase, 2);
               } else {
                  world.setBlock(buildPos, fire, 2);
               }
            }
         }
      }
   }
}
