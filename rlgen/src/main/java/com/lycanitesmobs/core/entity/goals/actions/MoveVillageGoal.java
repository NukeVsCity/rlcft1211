package com.lycanitesmobs.core.entity.goals.actions;

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
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.navigate.CreaturePathNavigate;
import java.util.ArrayList;
import java.util.List;


public class MoveVillageGoal extends EntityAIBase {
   private BaseCreatureEntity host;
   private double speed;
   private boolean isNocturnal = true;
   private Path entityPathNavigate;
   private VillageDoorInfo doorInfo;
   private List doorList = new ArrayList();

   public MoveVillageGoal(BaseCreatureEntity setHost) {
      this.host = setHost;
      this.setMutexBits(1);
   }

   public MoveVillageGoal setSpeed(double setSpeed) {
      this.speed = setSpeed;
      return this;
   }

   public MoveVillageGoal setNocturnal(boolean flag) {
      this.isNocturnal = flag;
      return this;
   }

   public boolean shouldExecute() {
      this.shuffleDoorList();
      if (this.isNocturnal && this.host.level().isDay()) {
         return false;
      } else {
         Object village = this.host
            .level()
            .getVillageCollection()
            .getNearestVillage(
               LycPos.of(
                  Mth.floor(this.host.getX()),
                  Mth.floor(this.host.getY()),
                  Mth.floor(this.host.getZ())
               ),
               0
            );
         if (village == null) {
            return false;
         } else {
            this.doorInfo = this.getDoorInfo(village);
            if (this.doorInfo == null) {
               return false;
            } else if (!(this.host.getNavigation() instanceof CreaturePathNavigate)) {
               return false;
            } else {
               CreaturePathNavigate pathNavigate = (CreaturePathNavigate)this.host.getNavigation();
               boolean flag = pathNavigate.getEnterDoors();
               pathNavigate.setCanOpenDoors(false);
               this.entityPathNavigate = this.host
                  .getNavigation()
                  .createPath(
                     (double)this.doorInfo.getInsideOffsetX(), (double)this.doorInfo.getInsideBlockPos().getY(), (double)this.doorInfo.getInsideOffsetZ()
                  , 0);
               pathNavigate.setCanOpenDoors(flag);
               if (this.entityPathNavigate != null) {
                  return true;
               } else {
                  Vec3 vec3 = RandomPositionGenerator.findRandomTargetTowards(
                     this.host,
                     10,
                     7,
                     new Vec3(
                        (double)this.doorInfo.getInsideOffsetX(), (double)this.doorInfo.getInsideBlockPos().getY(), (double)this.doorInfo.getInsideOffsetZ()
                     )
                  );
                  if (vec3 == null) {
                     return false;
                  } else {
                     pathNavigate.setCanOpenDoors(false);
                     this.entityPathNavigate = this.host.getNavigation().createPath(vec3.x, vec3.y, vec3.z, 0);
                     pathNavigate.setCanOpenDoors(flag);
                     return this.entityPathNavigate != null;
                  }
               }
            }
         }
      }
   }

   public boolean shouldContinueExecuting() {
      if (this.host.getNavigation().isDone()) {
         return false;
      } else {
         float f = this.host.getBbWidth() + 4.0F;
         return this.host
               .distanceToSqr(
                  (double)this.doorInfo.getInsideOffsetX(), (double)this.doorInfo.getInsideBlockPos().getY(), (double)this.doorInfo.getInsideOffsetZ()
               )
            > (double)(f * f);
      }
   }

   public void startExecuting() {
      this.host.getNavigation().moveTo(this.entityPathNavigate, this.speed);
   }

   public void resetTask() {
      if (this.host.getNavigation().isDone()
         || this.host
               .distanceToSqr(
                  (double)this.doorInfo.getInsideOffsetX(), (double)this.doorInfo.getInsideBlockPos().getY(), (double)this.doorInfo.getInsideOffsetZ()
               )
            < 16.0) {
         this.doorList.add(this.doorInfo);
      }
   }

   private VillageDoorInfo getDoorInfo(Object par1Village) {
      VillageDoorInfo villagedoorinfo = null;
      int i = Integer.MAX_VALUE;

      for (VillageDoorInfo villagedoorinfo1 : par1Village.getVillageDoorInfoList()) {
         int j = villagedoorinfo1.getDistanceSquared(
            Mth.floor(this.host.getX()),
            Mth.floor(this.host.getY()),
            Mth.floor(this.host.getZ())
         );
         if (j < i && !this.doesDoorListContain(villagedoorinfo1)) {
            villagedoorinfo = villagedoorinfo1;
            i = j;
         }
      }

      return villagedoorinfo;
   }

   private boolean doesDoorListContain(VillageDoorInfo par1VillageDoorInfo) {
      for (VillageDoorInfo villagedoorinfo1 : this.doorList) {
         if (par1VillageDoorInfo.getInsideOffsetX() == villagedoorinfo1.getInsideOffsetX()
            && par1VillageDoorInfo.getInsideBlockPos().getY() == villagedoorinfo1.getInsideBlockPos().getY()
            && par1VillageDoorInfo.getInsideOffsetZ() == villagedoorinfo1.getInsideOffsetZ()) {
            return true;
         }
      }

      return false;
   }

   private void shuffleDoorList() {
      if (this.doorList.size() > 15) {
         this.doorList.remove(0);
      }
   }
}
