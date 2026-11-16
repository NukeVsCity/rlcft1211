package com.lycanitesmobs.core.entity.goals.actions.abilities;

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
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.goals.BaseGoal;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureManager;


public class SummonMinionsGoal extends BaseGoal {
   protected int summonTime = 0;
   protected int summonRate = 60;
   protected int summonCap = 5;
   protected CreatureInfo minionInfo;
   protected boolean perPlayer = false;
   protected boolean antiFlight = false;
   protected double sizeScale = 1.0;

   public SummonMinionsGoal(BaseCreatureEntity setHost) {
      super(setHost);
   }

   public SummonMinionsGoal setSummonRate(int summonRate) {
      this.summonRate = summonRate;
      return this;
   }

   public SummonMinionsGoal setSummonCap(int summonCap) {
      this.summonCap = summonCap;
      return this;
   }

   public SummonMinionsGoal setPerPlayer(boolean perPlayer) {
      this.perPlayer = perPlayer;
      return this;
   }

   public SummonMinionsGoal setAntiFlight(boolean antiFlight) {
      this.antiFlight = antiFlight;
      return this;
   }

   public SummonMinionsGoal setMinionInfo(String creatureName) {
      this.minionInfo = CreatureManager.getInstance().getCreature(creatureName);
      return this;
   }

   public SummonMinionsGoal setSizeScale(double sizeScale) {
      this.sizeScale = sizeScale;
      return this;
   }

   @Override
   public boolean shouldExecute() {
      return this.host.isPetType("familiar") ? false : super.shouldExecute() && this.minionInfo != null;
   }

   public void startExecuting() {
      this.summonTime = 1;
   }

   public void updateTask() {
      if (this.summonTime++ % this.summonRate == 0) {
         if (this.host.getMinions(this.minionInfo.getEntityClass()).size() < this.summonCap) {
            if (this.antiFlight) {
               for (Player target : this.host.playerTargets) {
                  if (!target.isCreative()
                     && !target.isSpectator()
                     && CreatureManager.getInstance().config.bossAntiFlight > 0.0
                     && target.getY() > this.host.getY() + CreatureManager.getInstance().config.bossAntiFlight + 1.0) {
                     this.summonMinion(target);
                  }
               }
            } else {
               this.summonMinion(this.host.getTarget());
            }
         }
      }
   }

   protected void summonMinion(LivingEntity target) {
      LivingEntity minion = this.minionInfo.createEntity(this.host.level());
      this.host.summonMinion(minion, this.host.getRandom().nextDouble() * 360.0, (double)(this.host.getBbWidth() + 1.0F));
      if (minion instanceof BaseCreatureEntity) {
         BaseCreatureEntity minionCreature = (BaseCreatureEntity)minion;
         minionCreature.setTarget(target);
         minionCreature.setSizeScale(minionCreature.sizeScale * this.sizeScale);
      }
   }
}
