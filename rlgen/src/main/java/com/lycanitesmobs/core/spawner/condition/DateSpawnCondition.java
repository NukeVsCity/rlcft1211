package com.lycanitesmobs.core.spawner.condition;

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
import com.google.gson.JsonObject;
import com.lycanitesmobs.Utilities;
import java.util.Calendar;


public class DateSpawnCondition extends SpawnCondition {
   public int monthMin = -1;
   public int monthMax = -1;
   public int dayMin = -1;
   public int dayMax = -1;
   public String season = "";

   @Override
   public void loadFromJSON(JsonObject json) {
      if (json.has("monthMin")) {
         this.monthMin = json.get("monthMin").getAsInt();
      }

      if (json.has("monthMax")) {
         this.monthMax = json.get("monthMax").getAsInt();
      }

      if (json.has("dayMin")) {
         this.dayMin = json.get("dayMin").getAsInt();
      }

      if (json.has("dayMax")) {
         this.dayMax = json.get("dayMax").getAsInt();
      }

      if (json.has("season")) {
         this.season = json.get("season").getAsString();
      }

      super.loadFromJSON(json);
   }

   @Override
   public boolean isMet(Level world, Player player, BlockPos position) {
      Calendar calendar = Calendar.getInstance();
      int month = calendar.get(2) + 1;
      if (this.monthMin >= 0 && month < this.monthMin) {
         return false;
      } else if (this.monthMax >= 0 && month > this.monthMax) {
         return false;
      } else {
         int day = calendar.get(5);
         if (this.dayMin >= 0 && day < this.dayMin) {
            return false;
         } else if (this.dayMax >= 0 && day > this.dayMax) {
            return false;
         } else {
            if (!"".equals(this.season)) {
               if ("valentines".equalsIgnoreCase(this.season) && !Utilities.isValentines()) {
                  return false;
               }

               if ("easter".equalsIgnoreCase(this.season) && !Utilities.isEaster()) {
                  return false;
               }

               if ("midsummer".equalsIgnoreCase(this.season) && !Utilities.isMidsummer()) {
                  return false;
               }

               if ("halloween".equalsIgnoreCase(this.season) && !Utilities.isHalloween()) {
                  return false;
               }

               if (("yuletide".equalsIgnoreCase(this.season) || "christmas".equalsIgnoreCase(this.season)) && !Utilities.isYuletide()) {
                  return false;
               }

               if ("newyear".equalsIgnoreCase(this.season) && !Utilities.isNewYear()) {
                  return false;
               }
            }

            return super.isMet(world, player, position);
         }
      }
   }
}
