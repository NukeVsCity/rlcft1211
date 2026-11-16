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
import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class ItemDrop {
   public static List<ItemDrop> allDrops = new ArrayList<>();
   public String itemId;
   protected int metadata;
   protected String burningItemId;
   protected int burningMetadata;
   protected Map<Integer, String> effectItemIds = new HashMap<>();
   protected Map<Integer, Integer> effectItemMetadata = new HashMap<>();
   public int minAmount = 1;
   public int maxAmount = 1;
   public boolean bonusAmount = true;
   public boolean amountMultiplier = true;
   public float chance = 0.0F;
   public int subspeciesIndex = -1;
   public int variantIndex = -1;
   public boolean adultOnly = true;

   public static ItemDrop createFromJSON(JsonObject json) {
      ItemDrop itemDrop = null;
      int itemMetadata = 0;
      if (json.has("item")) {
         if (json.has("metadata")) {
            itemMetadata = json.get("metadata").getAsInt();
         }

         String itemId = json.get("item").getAsString();
         itemDrop = new ItemDrop(itemId, itemMetadata, 1.0F);
         itemDrop.loadFromJSON(json);
      } else {
         LycanitesMobs.logWarning("", "[JSON] Unable to load item drop from json as it has no item id!");
      }

      allDrops.add(itemDrop);
      return itemDrop;
   }

   public static ItemDrop createFromConfigString(String itemDropString) {
      if (itemDropString != null && itemDropString.length() > 0) {
         String[] customDropValues = itemDropString.split(",");
         String itemId = customDropValues[0];
         int itemMetadata = 0;
         if (customDropValues.length > 1) {
            itemMetadata = Integer.parseInt(customDropValues[1]);
         }

         int amountMin = 1;
         if (customDropValues.length > 2) {
            amountMin = Integer.parseInt(customDropValues[2]);
         }

         int amountMax = 1;
         if (customDropValues.length > 3) {
            amountMax = Integer.parseInt(customDropValues[3]);
         }

         float chance = 1.0F;
         if (customDropValues.length > 4) {
            chance = Float.parseFloat(customDropValues[4]);
         }

         ItemDrop itemDrop = new ItemDrop(itemId, itemMetadata, chance);
         itemDrop.setMinAmount(amountMin);
         itemDrop.setMaxAmount(amountMax);
         return itemDrop;
      } else {
         return null;
      }
   }

   public ItemDrop(String itemId, int metadata, float chance) {
      this.itemId = itemId;
      this.metadata = metadata;
      this.chance = chance;
   }

   public ItemDrop(CompoundTag nbtTagCompound) {
      this.readFromNBT(nbtTagCompound);
   }

   public ItemDrop(ItemDrop copyDrop) {
      this.itemId = copyDrop.itemId;
      this.metadata = copyDrop.metadata;
      this.minAmount = copyDrop.minAmount;
      this.maxAmount = copyDrop.maxAmount;
      this.bonusAmount = copyDrop.bonusAmount;
      this.amountMultiplier = copyDrop.amountMultiplier;
      this.chance = copyDrop.chance;
      this.subspeciesIndex = copyDrop.subspeciesIndex;
      this.variantIndex = copyDrop.variantIndex;
      this.adultOnly = copyDrop.adultOnly;
      this.burningItemId = copyDrop.burningItemId;
      this.burningMetadata = copyDrop.burningMetadata;
      this.effectItemIds = copyDrop.effectItemIds;
      this.effectItemMetadata = copyDrop.effectItemMetadata;
   }

   public void loadFromJSON(JsonObject json) {
      if (json.has("minAmount")) {
         this.minAmount = json.get("minAmount").getAsInt();
      }

      if (json.has("maxAmount")) {
         this.maxAmount = json.get("maxAmount").getAsInt();
      }

      if (json.has("bonusAmount")) {
         this.bonusAmount = json.get("bonusAmount").getAsBoolean();
      }

      if (json.has("amountMultiplier")) {
         this.amountMultiplier = json.get("amountMultiplier").getAsBoolean();
      }

      if (json.has("chance")) {
         this.chance = json.get("chance").getAsFloat();
      }

      if (json.has("subspecies")) {
         this.subspeciesIndex = json.get("subspecies").getAsInt();
      }

      if (json.has("variant")) {
         this.variantIndex = json.get("variant").getAsInt();
      }

      if (json.has("adultOnly")) {
         this.adultOnly = json.get("adultOnly").getAsBoolean();
      }

      if (json.has("burningItem")) {
         this.burningItemId = json.get("burningItem").getAsString();
         if (json.has("burningMetadata")) {
            this.burningMetadata = json.get("burningMetadata").getAsInt();
         }
      }
   }

   public ItemDrop setDrop(ItemStack itemStack) {
      this.itemId = LycReg.key(itemStack.getItem()).toString();
      this.metadata = itemStack.getMetadata();
      return this;
   }

   public ItemDrop setBurningDrop(ItemStack itemStack) {
      this.burningItemId = LycReg.key(itemStack.getItem()).toString();
      this.burningMetadata = itemStack.getMetadata();
      return this;
   }

   public ItemDrop setEffectDrop(int effectID, ItemStack itemStack) {
      this.effectItemIds.put(effectID, LycReg.key(itemStack.getItem()).toString());
      this.effectItemMetadata.put(effectID, itemStack.getMetadata());
      return this;
   }

   public ItemDrop setMinAmount(int amount) {
      this.minAmount = amount;
      return this;
   }

   public ItemDrop setMaxAmount(int amount) {
      this.maxAmount = amount;
      return this;
   }

   public ItemDrop setChance(float chance) {
      this.chance = chance;
      return this;
   }

   public ItemDrop setSubspecies(int subspeciesIndex) {
      this.subspeciesIndex = subspeciesIndex;
      return this;
   }

   public ItemDrop setVariant(int variantIndex) {
      this.variantIndex = variantIndex;
      return this;
   }

   public int getQuantity(RandomSource random, int bonus, int multiplier) {
      float roll = random.nextFloat();
      roll = Math.max(roll, 0.0F);
      if (roll > this.chance) {
         return 0;
      } else {
         if (!this.amountMultiplier) {
            multiplier = 1;
         }

         int min = this.minAmount;
         int max = this.maxAmount + (this.bonusAmount ? bonus : 0);
         if (max <= min) {
            return min * multiplier;
         } else {
            roll /= this.chance;
            float dropRange = (float)(max - min) * roll;
            int dropAmount = min + Math.round(dropRange);
            return Math.min(dropAmount * multiplier, this.getItemStack().getMaxStackSize());
         }
      }
   }

   public ItemStack getItemStack() {
      if (this.itemId == null) {
         return ItemStack.EMPTY;
      } else {
         Item item = (Item)GameRegistry.findRegistry(Item.class).getValue(ResourceLocation.parse(this.itemId));
         return item != null ? new ItemStack(item, 1, this.metadata) : ItemStack.EMPTY;
      }
   }

   public ItemStack getBurningItemStack() {
      if (this.burningItemId == null) {
         return this.getItemStack();
      } else {
         Item item = (Item)GameRegistry.findRegistry(Item.class).getValue(ResourceLocation.parse(this.burningItemId));
         return item != null ? new ItemStack(item, 1, this.burningMetadata) : this.getItemStack();
      }
   }

   public ItemStack getEffectItemStack(int effectId) {
      if (this.effectItemIds.containsKey(effectId) && this.effectItemMetadata.containsKey(effectId)) {
         Item item = (Item)GameRegistry.findRegistry(Item.class).getValue(ResourceLocation.parse(this.effectItemIds.get(effectId)));
         return item != null ? new ItemStack(item, 1, this.effectItemMetadata.get(effectId)) : ItemStack.EMPTY;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public ItemStack getEntityDropItemStack(LivingEntity entity, int quantity) {
      ItemStack itemStack = this.getItemStack();
      if (entity != null) {
         if (entity.isOnFire()) {
            itemStack = this.getBurningItemStack();
         }

         for (Object potionEffect : entity.getActiveEffects()) {
            if (potionEffect instanceof MobEffectInstance) {
               int effectId = LycEffects.id(((MobEffectInstance)potionEffect).getEffect());
               ItemStack effectStack = this.getEffectItemStack(effectId);
               if (!effectStack.isEmpty()) {
                  itemStack = effectStack;
               }
            }
         }
      }

      itemStack.setCount(quantity);
      return itemStack;
   }

   public void readFromNBT(CompoundTag nbtTagCompound) {
      if (nbtTagCompound.contains("ItemId")) {
         this.itemId = nbtTagCompound.getString("ItemId");
      }

      if (nbtTagCompound.contains("Metadata")) {
         this.metadata = nbtTagCompound.getInt("Metadata");
      }

      this.minAmount = nbtTagCompound.getInt("MinAmount");
      this.maxAmount = nbtTagCompound.getInt("MaxAmount");
      if (nbtTagCompound.contains("BonusAmount")) {
         this.bonusAmount = nbtTagCompound.getBoolean("BonusAmount");
      }

      this.chance = nbtTagCompound.getFloat("Chance");
      if (nbtTagCompound.contains("AmountMultiplier")) {
         this.amountMultiplier = nbtTagCompound.getBoolean("AmountMultiplier");
      }

      if (nbtTagCompound.contains("Subspecies")) {
         this.subspeciesIndex = nbtTagCompound.getInt("Subspecies");
      }

      if (nbtTagCompound.contains("Variant")) {
         this.variantIndex = nbtTagCompound.getInt("Variant");
      }

      if (nbtTagCompound.contains("AdultOnly")) {
         this.adultOnly = nbtTagCompound.getBoolean("AdultOnly");
      }
   }

   public boolean writeToNBT(CompoundTag nbtTagCompound) {
      if (this.itemId == null) {
         return false;
      } else {
         nbtTagCompound.putString("ItemId", this.itemId);
         nbtTagCompound.putInt("Metadata", this.metadata);
         nbtTagCompound.putInt("MinAmount", this.minAmount);
         nbtTagCompound.putInt("MaxAmount", this.maxAmount);
         nbtTagCompound.putBoolean("BonusAmount", this.bonusAmount);
         nbtTagCompound.putFloat("Chance", this.chance);
         nbtTagCompound.putBoolean("AmountMultiplier", this.amountMultiplier);
         nbtTagCompound.putInt("Subspecies", this.subspeciesIndex);
         nbtTagCompound.putInt("Variant", this.variantIndex);
         nbtTagCompound.putBoolean("AdultOnly", this.adultOnly);
         return true;
      }
   }

   public String toConfigString() {
      return this.itemId + "," + this.metadata + "," + this.minAmount + "," + this.maxAmount + "," + this.chance;
   }
}
