package com.lycanitesmobs.core.info;

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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.item.GenericFoodItem;
import com.lycanitesmobs.core.item.GenericItem;
import java.util.Iterator;


public class ItemInfo {
   public Item item;
   protected String name;
   public ModInfo group;

   public ItemInfo(ModInfo group) {
      this.group = group;
   }

   public void loadFromJSON(JsonObject json) {
      this.name = json.get("name").getAsString();
      LycanitesMobs.logDebug("", "Loading item " + this.name + " from JSON...");
      String modelName = null;
      if (json.has("model")) {
         modelName = json.get("model").getAsString();
      }

      CreativeTabs group = LycanitesMobs.itemsTab;
      if (json.has("group")) {
         String groupName = json.get("group").getAsString();
         if ("blocks".equalsIgnoreCase(groupName)) {
            group = LycanitesMobs.blocksTab;
         }

         if ("creatures".equalsIgnoreCase(groupName)) {
            group = LycanitesMobs.creaturesTab;
         }
      }

      int maxStackSize = 64;
      if (json.has("maxStackSize")) {
         maxStackSize = json.get("maxStackSize").getAsInt();
      }

      FoodInfo food = null;
      if (json.has("food")) {
         JsonObject foodJson = json.get("food").getAsJsonObject();
         food = new FoodInfo();
         food.hunger(foodJson.get("hunger").getAsInt());
         food.saturation(foodJson.get("saturation").getAsFloat());
         if (!foodJson.has("alwaysEdible") || foodJson.get("alwaysEdible").getAsBoolean()) {
            food.setAlwaysEdible();
         }

         if (foodJson.has("fast") && foodJson.get("fast").getAsBoolean()) {
            food.fastToEat();
         }

         if (foodJson.has("meat") && foodJson.get("meat").getAsBoolean()) {
            food.meat();
         }

         if (foodJson.has("effects")) {
            JsonArray effectsJson = foodJson.getAsJsonArray("effects");
            Iterator<JsonElement> jsonIterator = effectsJson.iterator();

            while (jsonIterator.hasNext()) {
               JsonObject foodEffectJson = jsonIterator.next().getAsJsonObject();
               String effectId = foodEffectJson.get("effectId").getAsString();
               float chance = 1.0F;
               if (foodEffectJson.has("chance")) {
                  chance = foodEffectJson.get("chance").getAsFloat();
               }

               EffectEntry effectEntry = new EffectEntry(
                  effectId, foodEffectJson.get("duration").getAsInt() * 20, foodEffectJson.get("amplifier").getAsInt(), chance
               );
               food.effect(effectEntry);
            }
         }
      }

      ItemProperties properties = new ItemProperties();
      properties.group(group);
      properties.maxStackSize(maxStackSize);
      if (food != null) {
         properties.food(food);
         this.item = new GenericFoodItem(properties, this.name);
      } else {
         this.item = new GenericItem(properties, this.name);
         ((GenericItem)this.item).modelName = modelName;
      }
   }

   public Item getItem() {
      return this.item;
   }

   public String getName() {
      return this.name;
   }
}
