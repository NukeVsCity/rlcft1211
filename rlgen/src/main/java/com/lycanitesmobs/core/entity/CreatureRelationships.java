package com.lycanitesmobs.core.entity;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class CreatureRelationships {
   protected BaseCreatureEntity creatureEntity;
   protected Map<UUID, CreatureRelationshipEntry> relationships = new HashMap<>();

   public CreatureRelationships(BaseCreatureEntity creatureEntity) {
      this.creatureEntity = creatureEntity;
   }

   @Nullable
   public CreatureRelationshipEntry getEntry(Entity entity) {
      return this.relationships.containsKey(entity.getUUID()) ? this.relationships.get(entity.getUUID()) : null;
   }

   public List<Player> getPlayers() {
      List<Player> players = new ArrayList<>();
      if (this.getCreatureEntity().level().isClientSide) {
         return players;
      } else {
         for (CreatureRelationshipEntry relationshipEntry : this.relationships.values()) {
            Player player = this.getCreatureEntity().level().getPlayerEntityByUUID(relationshipEntry.getTargetEntityUUID());
            if (player != null) {
               players.add(player);
            }
         }

         return players;
      }
   }

   public CreatureRelationshipEntry getOrCreateEntry(Entity entity) {
      CreatureRelationshipEntry relationshipEntry = this.getEntry(entity);
      if (relationshipEntry == null) {
         relationshipEntry = new CreatureRelationshipEntry(this);
         relationshipEntry.setTarget(entity);
         this.relationships.put(relationshipEntry.getTargetEntityUUID(), relationshipEntry);
      }

      return relationshipEntry;
   }

   public BaseCreatureEntity getCreatureEntity() {
      return this.creatureEntity;
   }

   public void load(CompoundTag nbt) {
      if (nbt.contains("CreatureRelationships")) {
         this.relationships.clear();
         ListTag relationshipNbtList = nbt.getList("CreatureRelationships", 10);

         for (int i = 0; i < relationshipNbtList.size(); i++) {
            CompoundTag relationshipNbt = relationshipNbtList.getCompound(i);
            CreatureRelationshipEntry creatureRelationshipEntry = new CreatureRelationshipEntry(this);
            creatureRelationshipEntry.load(relationshipNbt);
            this.relationships.put(creatureRelationshipEntry.getTargetEntityUUID(), creatureRelationshipEntry);
         }
      }
   }

   public void save(CompoundTag nbt) {
      ListTag relationshipNbtList = new ListTag();
      int i = 0;

      for (CreatureRelationshipEntry creatureRelationshipEntry : this.relationships.values()) {
         CompoundTag relationshipNbt = new CompoundTag();
         creatureRelationshipEntry.save(relationshipNbt);
         relationshipNbtList.add(relationshipNbt);
      }

      nbt.put("CreatureRelationships", relationshipNbtList);
   }
}
