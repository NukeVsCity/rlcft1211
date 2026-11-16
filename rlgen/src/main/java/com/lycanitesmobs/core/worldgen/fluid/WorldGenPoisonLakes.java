package com.lycanitesmobs.core.worldgen.fluid;

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
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.IWorldGenBase;
import com.lycanitesmobs.core.config.ConfigSpawning;
import com.lycanitesmobs.core.info.ModInfo;
import java.util.Random;
import java.util.Set;


public class WorldGenPoisonLakes extends WorldGenLakes implements IWorldGenBase {
   public String name = "Poison Lakes";
   public ModInfo group = LycanitesMobs.modInfo;
   public double generateUndergroundChance = 0.04;
   public double generateSurfaceChance = 0.125;
   public String dimensionEntries = "-1, 1";
   public int[] dimensionBlacklist;
   public String[] dimensionTypes;
   public boolean dimensionWhitelist = false;

   public WorldGenPoisonLakes() {
      super(ObjectManager.getBlock("poison"));
      ConfigSpawning config = ConfigSpawning.getConfig(this.group, "worldgen");
      config.setCategoryComment(
         "WorldGen Chances",
         "The chance that each worldgen will generate. You can set this to 0 to disable the worldgen or use the dimension black/white list. 1 = all over, 0.04 fairly rare."
      );
      this.generateUndergroundChance = config.getDouble("WorldGen Chances", this.name + " Underground Chance", this.generateUndergroundChance);
      this.generateSurfaceChance = config.getDouble(
         "WorldGen Chances",
         this.name + " Surface Chance",
         this.generateSurfaceChance,
         "Poison only generates on the surface when in SWAMP and SPOOKY tagged biomes and not COLD or SNOWY."
      );
      ConfigSpawning.SpawnDimensionSet dimensions = config.getDimensions("WorldGen Dimensions", this.name + " Dimensions", this.dimensionEntries);
      this.dimensionBlacklist = dimensions.dimensionIDs;
      this.dimensionTypes = dimensions.dimensionTypes;
      this.dimensionWhitelist = config.getBool("WorldGen Dimensions", this.name + " Dimensions Whitelist Mode", this.dimensionWhitelist);
   }

   @Override
   public void onWorldGen(RandomSource random, int chunkX, int chunkZ, Level world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
      if (this.isValidDimension(world)) {
         if (this.generateSurfaceChance > 0.0) {
            Biome biome = world.getBiome(LycPos.of(chunkX * 16, 0, chunkZ * 16).value());
            Set<Type> biomeTypesSet = BiomeDictionary.getTypes(biome);
            Type[] biomeTypes = biomeTypesSet.toArray(new Type[biomeTypesSet.size()]);
            boolean typeValid = false;

            for (Type type : biomeTypes) {
               if (type == Type.SNOWY || type == Type.COLD) {
                  typeValid = false;
                  break;
               }

               if (type == Type.SWAMP || type == Type.SPOOKY) {
                  typeValid = true;
               }
            }

            if (typeValid && (this.generateSurfaceChance >= 1.0 || random.nextDouble() <= this.generateSurfaceChance)) {
               int x = chunkX * 16 + 16;
               int z = chunkZ * 16 + 16;
               int y = random.nextInt(128);
               this.generate(world, random, LycPos.of(x, y, z));
            }
         }

         if (this.generateUndergroundChance > 0.0 && (this.generateUndergroundChance >= 1.0 || random.nextDouble() <= this.generateUndergroundChance)) {
            int x = chunkX * 16 + 16;
            int z = chunkZ * 16 + 16;
            int top = Math.max(1, world.getTopSolidOrLiquidBlock(LycPos.of(x, 0, z)).getY() - 10);
            if (top > 0) {
               int y = random.nextInt(top);
               this.generate(world, random, LycPos.of(x, y, z));
            }
         }
      }
   }

   public boolean generate(Level world, RandomSource random, BlockPos pos) {
      return super.generate(world, random, pos);
   }

   public boolean isValidDimension(Level world) {
      boolean validDimension = false;

      for (String eventDimensionType : this.dimensionTypes) {
         if ("ALL".equalsIgnoreCase(eventDimensionType)) {
            validDimension = true;
         } else if ("VANILLA".equalsIgnoreCase(eventDimensionType)) {
            validDimension = LycDim.id(world) > -2 && LycDim.id(world) < 2;
         }
      }

      if (!validDimension) {
         validDimension = !this.dimensionWhitelist;

         for (int eventDimension : this.dimensionBlacklist) {
            if (LycDim.id(world) == eventDimension) {
               validDimension = this.dimensionWhitelist;
               break;
            }
         }
      }

      return validDimension;
   }
}
