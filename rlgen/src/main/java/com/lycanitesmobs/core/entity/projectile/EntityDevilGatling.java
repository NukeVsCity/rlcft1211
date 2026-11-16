package com.lycanitesmobs.core.entity.projectile;

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
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.info.ObjectLists;
import java.util.ArrayList;
import java.util.List;


public class EntityDevilGatling extends BaseProjectileEntity {
   public Entity shootingEntity;
   public int expireTime = 15;

   public EntityDevilGatling(Level world) {
      super(LycEntities.type(EntityDevilGatling.class), world);
   }

   public EntityDevilGatling(Level world, LivingEntity entityLivingBase) {
      super(LycEntities.type(EntityDevilGatling.class), world, entityLivingBase);
   }

   public EntityDevilGatling(Level world, double x, double y, double z) {
      super(LycEntities.type(EntityDevilGatling.class), world, x, y, z);
   }

   @Override
   public void setup() {
      this.entityName = "devilgatling";
      this.modInfo = LycanitesMobs.modInfo;
      this.setDamage(4);
      this.ripper = true;
   }

   @Override
   public void tick() {
      super.tick();
      if (this.getY() > (double)(this.level().getHeight() + 20)) {
         this.discard();
      }

      if (this.tickCount >= this.expireTime * 20) {
         this.discard();
      }
   }

   @Override
   protected float getGravityVelocity() {
      return 0.0F;
   }

   @Override
   public void onDamage(LivingEntity target, float damage, boolean attackSuccess) {
      super.onDamage(target, damage, attackSuccess);
      if (this.random.nextBoolean()) {
         List<Holder<MobEffect>> goodEffects = new ArrayList<>();

         for (Object potionEffectObj : target.getActiveEffects()) {
            if (potionEffectObj instanceof MobEffectInstance) {
               Holder<MobEffect> potion = ((MobEffectInstance)potionEffectObj).getEffect();
               if (potion != null && ObjectLists.inEffectList("buffs", potion)) {
                  goodEffects.add(potion);
               }
            }
         }

         if (!goodEffects.isEmpty()) {
            if (goodEffects.size() > 1) {
               target.removeEffect(goodEffects.get(this.random.nextInt(goodEffects.size())));
            } else {
               target.removeEffect(goodEffects.get(0));
            }
         }
      }

      if (ObjectManager.getEffect("decay") != null) {
         target.addEffect(new MobEffectInstance(ObjectManager.getEffect("decay"), this.getEffectDuration(60), 0));
      }
   }

   @Override
   public void onImpactVisuals() {
      for (int i = 0; i < 8; i++) {
         this.level()
            .addParticle(ParticleTypes.POOF, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
      }
   }

   @Override
   public SoundEvent getLaunchSound() {
      return AssetManager.getSound("devilgatling");
   }

   public float getBrightness() {
      return 1.0F;
   }

   public int getBrightnessForRender() {
      return 15728880;
   }
}
