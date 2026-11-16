package com.lycanitesmobs.core.block.effect;

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
import com.lycanitesmobs.PotionBase;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.core.block.BlockFireBase;
import com.lycanitesmobs.core.config.ConfigBase;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.info.ElementManager;
import java.util.Random;


public class BlockShadowfire extends BlockFireBase {
   public boolean blindness;

   public BlockShadowfire() {
      super(Material.FIRE, LycanitesMobs.modInfo, "shadowfire");
      this.tickRate = 30;
      this.dieInRain = false;
      this.triggerTNT = false;
      this.agingRate = 3;
      this.spreadChance = 0.0F;
      this.removeOnTick = !ConfigBase.getConfig(this.group, "general").getBool("Features", "Enable Shadowfire", true);
      this.removeOnNoFireTick = ConfigBase.getConfig(this.group, "general").getBool("Features", "Remove Shadowfire on No Fire Tick", false);
      this.blindness = !ConfigBase.getConfig(this.group, "general").getBool("Features", "Shadowfire Blindness", true);
   }

   @Override
   protected boolean canNeighborCatchFire(Level worldIn, BlockPos pos) {
      return false;
   }

   @Override
   protected int getNeighborEncouragement(Level worldIn, BlockPos pos) {
      return 0;
   }

   @Override
   public boolean canCatchFire(BlockGetter world, BlockPos pos, Direction face) {
      return false;
   }

   @Override
   public boolean isBlockFireSource(Block block, Level world, BlockPos pos, Direction side) {
      return block == Blocks.OBSIDIAN;
   }

   @Override
   protected boolean canDie(Level world, BlockPos pos) {
      return false;
   }

   @Override
   public Item getItemDropped(BlockState state, RandomSource random, int zero) {
      return null;
   }

   @Override
   public void onEntityCollision(Level world, BlockPos pos, BlockState state, Entity entity) {
      super.onEntityCollision(world, pos, state, entity);
      if (!(entity instanceof ItemEntity)) {
         if (entity instanceof LivingEntity) {
            LivingEntity entityLiving = (LivingEntity)entity;
            PotionBase decay = ObjectManager.getEffect("decay");
            if (decay != null) {
               MobEffectInstance effectDecay = new MobEffectInstance(decay, 100, 0);
               if (entityLiving.canBeAffected(effectDecay)) {
                  entityLiving.addEffect(effectDecay);
               }
            }

            MobEffectInstance effectBlindness = new MobEffectInstance(MobEffects.BLINDNESS, 100, 0);
            if (this.blindness && entityLiving.canBeAffected(effectBlindness)) {
               entityLiving.addEffect(effectBlindness);
            }
         }

         if (!(entity instanceof BaseCreatureEntity) || !((BaseCreatureEntity)entity).hasElement(ElementManager.getInstance().getElement("shadow"))) {
            entity.hurt(LycDamage.src(DamageTypes.WITHER), 1.0F);
         }
      }
   }

   public void randomDisplayTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
      super.randomDisplayTick(state, world, pos, random);
      int x = pos.getX();
      int y = pos.getY();
      int z = pos.getZ();
      if (random.nextInt(24) == 0) {
         world.playLocalSound(
            (double)((float)x + 0.5F),
            (double)((float)y + 0.5F),
            (double)((float)z + 0.5F),
            AssetManager.getSound("shadowfire"),
            SoundSource.BLOCKS,
            0.5F + random.nextFloat(),
            random.nextFloat() * 0.7F + 0.3F,
            false
         );
      }

      for (int particleCount = 0; particleCount < 12; particleCount++) {
         float particleX = (float)x + random.nextFloat();
         float particleY = (float)y + random.nextFloat() * 0.5F;
         float particleZ = (float)z + random.nextFloat();
         world.addParticle(ParticleTypes.WITCH, (double)particleX, (double)particleY, (double)particleZ, 0.0, 0.0, 0.0);
      }
   }
}
