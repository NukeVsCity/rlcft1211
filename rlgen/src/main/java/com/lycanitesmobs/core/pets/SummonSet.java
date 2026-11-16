package com.lycanitesmobs.core.pets;

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
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureKnowledge;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.Subspecies;
import com.lycanitesmobs.core.info.Variant;


public class SummonSet {
   public ExtendedPlayer playerExt;
   public String summonType = "";
   public int subspecies = 0;
   public int variant = 0;
   public boolean summonableOnly = true;
   public boolean sitting = false;
   public boolean following = true;
   public boolean passive = false;
   public boolean assist = true;
   public boolean aggressive = false;
   public boolean pvp = false;

   public static boolean isSummonableCreature(String creatureName) {
      CreatureInfo creatureInfo = CreatureManager.getInstance().getCreature(creatureName);
      if (creatureInfo == null) {
         return false;
      } else {
         return creatureInfo.dummy ? false : creatureInfo.isSummonable();
      }
   }

   public SummonSet(ExtendedPlayer playerExt) {
      this.playerExt = playerExt;
   }

   public void setSummonType(String summonType) {
      if (summonType != null) {
         this.summonType = summonType.toLowerCase();
      } else {
         this.summonType = null;
      }
   }

   public void setSubspecies(int subspecies) {
      this.subspecies = subspecies;
   }

   public int getSubspecies() {
      return this.subspecies;
   }

   public void setVariant(int variant) {
      this.variant = variant;
   }

   public int getVariant() {
      return this.variant;
   }

   public boolean getSitting() {
      return this.sitting;
   }

   public boolean getFollowing() {
      return this.following;
   }

   public boolean getPassive() {
      return this.passive;
   }

   public boolean getAssist() {
      return this.assist;
   }

   public boolean getAggressive() {
      return this.aggressive;
   }

   public boolean getPVP() {
      return this.pvp;
   }

   public CreatureInfo getCreatureInfo() {
      return CreatureManager.getInstance().getCreature(this.summonType.toLowerCase());
   }

   public void applyBehaviour(TameableCreatureEntity minion) {
      minion.setSitting(this.getSitting());
      minion.setFollowing(this.getFollowing());
      minion.setPassive(this.getPassive());
      minion.setAssist(this.getAssist());
      minion.setAggressive(this.getAggressive());
      minion.setPVP(this.getPVP());
   }

   public void updateBehaviour(TameableCreatureEntity minion) {
      this.sitting = minion.isSitting();
      this.following = minion.isFollowing();
      this.passive = minion.isPassive();
      this.assist = minion.isAssisting();
      this.aggressive = minion.isAggressive();
      this.pvp = minion.isPVP();
   }

   public boolean isUseable() {
      if (this.summonType == null
         || "".equals(this.summonType)
         || CreatureManager.getInstance().getCreature(this.summonType) == null
         || !isSummonableCreature(this.summonType)) {
         return false;
      } else if (this.playerExt != null) {
         CreatureInfo creatureInfo = this.getCreatureInfo();
         CreatureKnowledge creatureKnowledge = this.playerExt.getBeastiary().getCreatureKnowledge(creatureInfo.getName());
         if (creatureKnowledge == null) {
            return false;
         } else {
            Subspecies subspecies = creatureInfo.getSubspecies(this.subspecies);
            Variant variant = subspecies.getVariant(this.variant);
            return variant == null ? true : this.playerExt.getBeastiary().hasKnowledgeRank(creatureInfo.getName(), 2);
         }
      } else {
         return true;
      }
   }

   public Class getCreatureClass() {
      CreatureInfo creatureInfo = CreatureManager.getInstance().getCreature(this.summonType);
      return creatureInfo == null ? null : creatureInfo.entityClass;
   }

   public void readFromPacket(String summonType, int subspecies, int variant, byte behaviour) {
      this.setSummonType(summonType);
      this.setSubspecies(subspecies);
      this.setVariant(variant);
      this.setBehaviourByte(behaviour);
   }

   public void setBehaviourByte(byte behaviour) {
      this.sitting = (behaviour & SummonSet.BEHAVIOUR_ID.SITTING.id) > 0;
      this.following = (behaviour & SummonSet.BEHAVIOUR_ID.FOLLOWING.id) > 0;
      this.passive = (behaviour & SummonSet.BEHAVIOUR_ID.PASSIVE.id) > 0;
      this.assist = (behaviour & SummonSet.BEHAVIOUR_ID.ASSIST.id) > 0;
      this.aggressive = (behaviour & SummonSet.BEHAVIOUR_ID.AGGRESSIVE.id) > 0;
      this.pvp = (behaviour & SummonSet.BEHAVIOUR_ID.PVP.id) > 0;
   }

   public byte getBehaviourByte() {
      byte behaviour = 0;
      if (this.getSitting()) {
         behaviour += SummonSet.BEHAVIOUR_ID.SITTING.id;
      }

      if (this.getFollowing()) {
         behaviour += SummonSet.BEHAVIOUR_ID.FOLLOWING.id;
      }

      if (this.getPassive()) {
         behaviour += SummonSet.BEHAVIOUR_ID.PASSIVE.id;
      }

      if (this.getAssist()) {
         behaviour += SummonSet.BEHAVIOUR_ID.ASSIST.id;
      }

      if (this.getAggressive()) {
         behaviour += SummonSet.BEHAVIOUR_ID.AGGRESSIVE.id;
      }

      if (this.getPVP()) {
         behaviour += SummonSet.BEHAVIOUR_ID.PVP.id;
      }

      return behaviour;
   }

   public void readFromNBT(CompoundTag nbtTagCompound) {
      if (nbtTagCompound.contains("SummonType")) {
         this.setSummonType(nbtTagCompound.getString("SummonType"));
      }

      if (nbtTagCompound.contains("Subspecies")) {
         this.setSubspecies(nbtTagCompound.getInt("Subspecies"));
      }

      if (nbtTagCompound.contains("Variant")) {
         this.setVariant(nbtTagCompound.getInt("Variant"));
      }

      if (nbtTagCompound.contains("Sitting")) {
         this.sitting = nbtTagCompound.getBoolean("Sitting");
      }

      if (nbtTagCompound.contains("Following")) {
         this.following = nbtTagCompound.getBoolean("Following");
      }

      if (nbtTagCompound.contains("Passive")) {
         this.passive = nbtTagCompound.getBoolean("Passive");
      }

      if (nbtTagCompound.contains("Assist")) {
         this.assist = nbtTagCompound.getBoolean("Assist");
      }

      if (nbtTagCompound.contains("Aggressive")) {
         this.aggressive = nbtTagCompound.getBoolean("Aggressive");
      }

      if (nbtTagCompound.contains("PVP")) {
         this.pvp = nbtTagCompound.getBoolean("PVP");
      }
   }

   public void writeToNBT(CompoundTag nbtTagCompound) {
      nbtTagCompound.putString("SummonType", this.summonType);
      nbtTagCompound.putInt("Subspecies", this.getSubspecies());
      nbtTagCompound.putInt("Variant", this.getVariant());
      nbtTagCompound.putBoolean("Sitting", this.sitting);
      nbtTagCompound.putBoolean("Following", this.following);
      nbtTagCompound.putBoolean("Passive", this.passive);
      nbtTagCompound.putBoolean("Assist", this.assist);
      nbtTagCompound.putBoolean("Aggressive", this.aggressive);
      nbtTagCompound.putBoolean("PVP", this.pvp);
   }

   public static enum BEHAVIOUR_ID {
      SITTING((byte)1),
      FOLLOWING((byte)2),
      PASSIVE((byte)4),
      ASSIST((byte)8),
      AGGRESSIVE((byte)16),
      PVP((byte)32);

      public byte id;

      private BEHAVIOUR_ID(byte i) {
         this.id = i;
      }
   }
}
