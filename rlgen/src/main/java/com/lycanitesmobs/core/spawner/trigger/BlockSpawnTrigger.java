package com.lycanitesmobs.core.spawner.trigger;

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
import com.lycanitesmobs.core.spawner.Spawner;
import java.util.ArrayList;
import java.util.List;


public class BlockSpawnTrigger extends SpawnTrigger {
   public boolean playerOnly = false;
   public boolean onBreak = true;
   public boolean onHarvest = true;
   public boolean onPlace = false;
   public List<Block> blocks = new ArrayList<>();
   public String blocksListType = "whitelist";
   public List<Material> blockMaterials = new ArrayList<>();
   public String blockMaterialsListType = "whitelist";
   public String silkTouch = "ignore";
   public double fortuneMin = -1.0;
   public double fortuneMax = -1.0;

   public BlockSpawnTrigger(Spawner spawner) {
      super(spawner);
   }

   @Override
   public void loadFromJSON(JsonObject json) {
      if (json.has("playerOnly")) {
         this.playerOnly = json.get("playerOnly").getAsBoolean();
      }

      if (json.has("onBreak")) {
         this.onBreak = json.get("onBreak").getAsBoolean();
      }

      if (json.has("onHarvest")) {
         this.onHarvest = json.get("onHarvest").getAsBoolean();
      }

      if (json.has("onPlace")) {
         this.onPlace = json.get("onPlace").getAsBoolean();
      }

      this.blocks = JSONHelper.getJsonBlocks(json);
      if (json.has("blocksListType")) {
         this.blocksListType = json.get("blocksListType").getAsString();
      }

      this.blockMaterials = JSONHelper.getJsonMaterials(json);
      if (json.has("blockMaterialsListType")) {
         this.blockMaterialsListType = json.get("blockMaterialsListType").getAsString();
      }

      if (json.has("silkTouch")) {
         this.silkTouch = json.get("silkTouch").getAsString();
      }

      if (json.has("fortuneMin")) {
         this.fortuneMin = (double)json.get("fortuneMin").getAsInt();
      }

      if (json.has("fortuneMax")) {
         this.fortuneMax = (double)json.get("fortuneMax").getAsInt();
      }

      super.loadFromJSON(json);
   }

   public void onBlockBreak(Level world, Player player, BlockPos breakPos, BlockState blockState, int chain) {
      if (this.onBreak) {
         this.onBlockTriggered(world, player, breakPos, blockState, chain, 0);
      }
   }

   public void onBlockHarvest(Level world, Player player, BlockPos breakPos, BlockState blockState, int chain, int fortune, boolean silkTouch) {
      if (this.onHarvest) {
         if (!(this.fortuneMin >= 0.0) || !((double)fortune < this.fortuneMin)) {
            if (!(this.fortuneMax > this.fortuneMin) || !((double)fortune > this.fortuneMax)) {
               if (!silkTouch || !"deny".equals(this.silkTouch)) {
                  if (silkTouch || !"require".equals(this.silkTouch)) {
                     this.onBlockTriggered(world, player, breakPos, blockState, chain, fortune);
                  }
               }
            }
         }
      }
   }

   public void onBlockPlace(Level world, Player player, BlockPos breakPos, BlockState blockState, int chain) {
      if (this.onPlace) {
         this.onBlockTriggered(world, player, breakPos, blockState, chain, 0);
      }
   }

   public void onBlockTriggered(Level world, Player player, BlockPos blockPos, BlockState blockState, int chain, int fortune) {
      if (!this.playerOnly || player != null && !(player instanceof net.neoforged.neoforge.common.util.FakePlayer)) {
         if (this.isTriggerBlock(blockState, world, blockPos, fortune)) {
            if (!(this.chance < 1.0) || !(world.random.nextDouble() > this.chance)) {
               this.trigger(world, player, blockPos, this.getBlockLevel(blockState, world, blockPos), chain);
            }
         }
      }
   }

   public boolean isTriggerBlock(BlockState blockState, Level world, BlockPos blockPos, int fortune) {
      if (this.blocks.size() > 0) {
         Block block = blockState.getBlock();
         if (this.blocks.contains(block)) {
            return !"blacklist".equalsIgnoreCase(this.blocksListType);
         }
      }

      if (this.blockMaterials.size() > 0) {
         Material material = Material.of(blockState);
         if (this.blockMaterials.contains(material)) {
            return !"blacklist".equalsIgnoreCase(this.blockMaterialsListType);
         }
      }

      return this.blocks.isEmpty() && this.blockMaterials.isEmpty()
         ? true
         : "blacklist".equalsIgnoreCase(this.blocksListType) && "blacklist".equalsIgnoreCase(this.blockMaterialsListType);
   }

   public int getBlockLevel(BlockState blockState, Level world, BlockPos blockPos) {
      return 0;
   }
}
