package com.lycanitesmobs.core.mobevent;

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
import com.lycanitesmobs.LycanitesMobs;


public class MobEventPlayerServer {
   public static boolean testOnCreative = false;
   public MobEvent mobEvent;
   public int ticks = 0;
   public Level world;
   public long startedWorldTime = 0L;
   public Player player;
   public BlockPos origin = LycPos.of(0, 0, 0);
   public int level = 1;
   public int variant = -1;
   public boolean extended = false;

   public MobEventPlayerServer(MobEvent mobEvent, Level world) {
      this.mobEvent = mobEvent;
      this.world = world;
      if (world.isClientSide) {
         LycanitesMobs.logWarning("", "Created a MobEventServer with a client side world, this shouldn't happen, things are going to get weird!");
      }
   }

   public void onStart() {
      this.startedWorldTime = this.level().getGameTime();
      this.ticks = 0;
      LycanitesMobs.logInfo(
         "",
         "Mob Event "
            + (this.extended ? "Extended" : "Started")
            + ": "
            + this.mobEvent.getTitle()
            + " In Dimension: "
            + LycDim.id(this.level())
            + " Duration: "
            + this.mobEvent.duration / 20
            + "secs"
      );
   }

   public void changeStartedWorldTime(long newStartedTime) {
      this.startedWorldTime = newStartedTime;
      LycanitesMobs.logInfo(
         "",
         "Mob Event Start Time Changed: "
            + this.mobEvent.getTitle()
            + " In Dimension: "
            + LycDim.id(this.level())
            + " Duration: "
            + this.mobEvent.duration / 20
            + "secs Time Remaining: "
            + ((long)this.mobEvent.duration - (this.level().getGameTime() - this.startedWorldTime)) / 20L
            + "secs"
      );
   }

   public void onFinish() {
      LycanitesMobs.logInfo("", "Mob Event Finished: " + this.mobEvent.getTitle());
   }

   public void onUpdate() {
      if (this.world == null) {
         LycanitesMobs.logWarning("", "MobEventBase was trying to update without a world object, stopped!");
      } else if (this.level().isClientSide) {
         LycanitesMobs.logWarning("", "MobEventBase was trying to update with a client side world, stopped!");
      } else {
         this.mobEvent.onUpdate(this.level(), this.player, this.origin, this.level, this.ticks, this.variant);
         this.ticks++;
         if (this.ticks >= this.mobEvent.duration) {
            ExtendedWorld worldExt = ExtendedWorld.getForWorld(this.level());
            if ("world".equalsIgnoreCase(this.mobEvent.channel)) {
               worldExt.stopWorldEvent();
            } else {
               worldExt.stopMobEvent(this.mobEvent.name);
            }
         }
      }
   }
}
