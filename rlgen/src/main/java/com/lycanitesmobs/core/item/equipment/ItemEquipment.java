package com.lycanitesmobs.core.item.equipment;

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
import net.minecraft.world.entity.boss.enderdragon.*;
import net.minecraft.world.entity.boss.wither.*;
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
import com.google.common.collect.Multimap;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.entity.ExtendedEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.item.ItemBase;
import com.lycanitesmobs.core.item.equipment.features.DamageEquipmentFeature;
import com.lycanitesmobs.core.item.equipment.features.EffectEquipmentFeature;
import com.lycanitesmobs.core.item.equipment.features.EquipmentFeature;
import com.lycanitesmobs.core.item.equipment.features.HarvestEquipmentFeature;
import com.lycanitesmobs.core.item.equipment.features.ProjectileEquipmentFeature;
import com.lycanitesmobs.core.item.equipment.features.SummonEquipmentFeature;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ItemEquipment extends ItemBase {
   public static ItemStack ITEMSTACK_TO_RENDER;
   public static int PART_LIMIT = 20;
   public static int SHARPNESS_MAX = 1500;
   public static int MANA_MAX = 1500;

   public ItemEquipment() {
      this.itemName = "equipment";
      this.modInfo = LycanitesMobs.modInfo;
      LycReg.setName(this, this.modInfo.modid, this.itemName);
      this.setTranslationKey(this.itemName);
      this.setMaxStackSize(1);
   }

      public void addInformation(ItemStack itemStack, Level world, List<String> tooltip, TooltipFlag tooltipFlag) {
      super.addInformation(itemStack, world, tooltip, tooltipFlag);
      // 1.12 FontRenderer.listFormattedStringToWidth: the client proxy wraps with the font
      for (String description : this.getAdditionalDescriptions(itemStack, world, tooltipFlag)) {
         for (String formattedDescription : LycanitesMobs.proxy.wrapText(description, DESCRIPTION_WIDTH + 100)) {
            tooltip.add("§3" + formattedDescription);
         }
      }
   }

   @Override
   public String getDescription(ItemStack stack, Level worldIn, List<String> tooltip, TooltipFlag flagIn) {
      return LanguageManager.translate("item.equipment.description");
   }

   public List<String> getAdditionalDescriptions(ItemStack itemStack, Level world, TooltipFlag tooltipFlag) {
      List<String> descriptions = new ArrayList<>();
      descriptions.add(LanguageManager.translate("equipment.sharpness") + " " + this.getSharpness(itemStack) + "/" + SHARPNESS_MAX);
      descriptions.add(LanguageManager.translate("equipment.mana") + " " + this.getMana(itemStack) + "/" + MANA_MAX);
      descriptions.add("-------------------");

      for (ItemStack equipmentPartStack : this.getEquipmentPartStacks(itemStack)) {
         ItemEquipmentPart equipmentPart = this.getEquipmentPart(equipmentPartStack);
         if (equipmentPart != null) {
            int partLevel = equipmentPart.getLevel(equipmentPartStack);
            descriptions.add(equipmentPart.getItemStackDisplayName(itemStack) + " " + LanguageManager.translate("entity.level") + " " + partLevel);
         }
      }

      descriptions.add("-------------------\n");
      String damageDescription = LanguageManager.translate("equipment.feature.damage") + " " + String.format("%.0f", this.getDamageAmount(itemStack) + 1.0);
      damageDescription = damageDescription
         + "\n"
         + LanguageManager.translate("equipment.feature.damage.cooldown")
         + " "
         + String.format("%.1f", this.getDamageCooldown(itemStack));
      damageDescription = damageDescription
         + "\n"
         + LanguageManager.translate("equipment.feature.damage.knockback")
         + " "
         + String.format("%.0f", this.getDamageKnockback(itemStack));
      damageDescription = damageDescription
         + "\n"
         + LanguageManager.translate("equipment.feature.damage.range")
         + " "
         + String.format("%.1f", this.getDamageRange(itemStack));
      damageDescription = damageDescription
         + "\n"
         + LanguageManager.translate("equipment.feature.damage.sweep")
         + " "
         + String.format("%.0f", Math.min(this.getDamageSweep(itemStack), 360.0));
      descriptions.add(damageDescription);
      String harvestSummaries = this.getFeatureSummaries(itemStack, "harvest");
      String effectSummaries = this.getFeatureSummaries(itemStack, "effect");
      String projectileSummaries = this.getFeatureSummaries(itemStack, "projectile");
      String summonSummaries = this.getFeatureSummaries(itemStack, "summon");
      if (!"".equals(harvestSummaries) || !"".equals(effectSummaries) || !"".equals(projectileSummaries) || !"".equals(summonSummaries)) {
         descriptions.add("-------------------\n");
         if (!"".equals(harvestSummaries)) {
            descriptions.add(LanguageManager.translate("equipment.feature.harvest") + " " + harvestSummaries);
         }

         if (!"".equals(effectSummaries)) {
            descriptions.add(LanguageManager.translate("equipment.feature.effect") + " " + effectSummaries);
         }

         if (!"".equals(projectileSummaries)) {
            descriptions.add(LanguageManager.translate("equipment.feature.projectile") + " " + projectileSummaries);
         }

         if (!"".equals(summonSummaries)) {
            descriptions.add(LanguageManager.translate("equipment.feature.summon") + " " + summonSummaries);
         }
      }

      return descriptions;
   }

   public String getFeatureSummaries(ItemStack itemStack, String featureType) {
      Map<EquipmentFeature, ItemStack> effectFeatures = this.getFeaturesByTypeWithPartStack(itemStack, featureType);
      String featureSummaries = "";
      boolean first = true;

      for (EquipmentFeature equipmentFeature : effectFeatures.keySet()) {
         if (!first) {
            featureSummaries = featureSummaries + ", ";
         }

         first = false;
         String featureSummary = equipmentFeature.getSummary(effectFeatures.get(equipmentFeature), this.getPartLevel(effectFeatures.get(equipmentFeature)));
         if (featureSummary != null) {
            featureSummaries = featureSummaries + featureSummary;
         }
      }

      return featureSummaries;
   }

   public int getMetadata(ItemStack stack) {
      ITEMSTACK_TO_RENDER = stack;
      return 0;
   }


   public boolean showDurabilityBar(ItemStack itemStack) {
      return (double)this.getSharpness(itemStack) < (double)SHARPNESS_MAX;
   }

   public double getDurabilityForDisplay(ItemStack itemStack) {
      return 1.0 - (double)this.getSharpness(itemStack) / (double)SHARPNESS_MAX;
   }

   public NonNullList<ItemStack> getEquipmentPartStacks(ItemStack itemStack) {
      NonNullList<ItemStack> itemStacks = NonNullList.withSize(PART_LIMIT, ItemStack.EMPTY);
      CompoundTag nbt = this.getTagCompound(itemStack);
      if (nbt.contains("Items")) {
         ContainerHelper.loadAllItems(nbt, itemStacks, LycReg.access());
      }

      return itemStacks;
   }

   public int getEquipmentPartCount(ItemStack itemStack) {
      int partCount = 0;

      for (ItemStack partStack : this.getEquipmentPartStacks(itemStack)) {
         if (!partStack.isEmpty()) {
            partCount++;
         }
      }

      return partCount;
   }

   public ItemEquipmentPart getEquipmentPart(ItemStack itemStack) {
      if (itemStack.isEmpty()) {
         return null;
      } else {
         return !(itemStack.getItem() instanceof ItemEquipmentPart) ? null : (ItemEquipmentPart)itemStack.getItem();
      }
   }

   public void addEquipmentPart(ItemStack equipmentStack, ItemStack equipmentPartStack, int slotIndex) {
      if (slotIndex < PART_LIMIT) {
         NonNullList<ItemStack> itemStacks = this.getEquipmentPartStacks(equipmentStack);
         itemStacks.set(slotIndex, equipmentPartStack);
         CompoundTag nbt = this.getTagCompound(equipmentStack);
         ContainerHelper.saveAllItems(nbt, itemStacks, LycReg.access());
         LycNBT.setTag(equipmentStack, nbt);
      }
   }

   public List<EquipmentFeature> getFeaturesByType(ItemStack equipmentStack, String featureType) {
      List<EquipmentFeature> features = new ArrayList<>();

      for (ItemStack equipmentPartStack : this.getEquipmentPartStacks(equipmentStack)) {
         ItemEquipmentPart equipmentPart = this.getEquipmentPart(equipmentPartStack);
         if (equipmentPart != null) {
            for (EquipmentFeature feature : equipmentPart.features) {
               if (feature.isActive(equipmentPartStack, equipmentPart.getLevel(equipmentPartStack)) && feature.featureType.equalsIgnoreCase(featureType)) {
                  features.add(feature);
               }
            }
         }
      }

      return features;
   }

   public Map<EquipmentFeature, ItemStack> getFeaturesByTypeWithPartStack(ItemStack equipmentStack, String featureType) {
      Map<EquipmentFeature, ItemStack> features = new HashMap<>();

      for (ItemStack equipmentPartStack : this.getEquipmentPartStacks(equipmentStack)) {
         ItemEquipmentPart equipmentPart = this.getEquipmentPart(equipmentPartStack);
         if (equipmentPart != null) {
            for (EquipmentFeature feature : equipmentPart.features) {
               if (feature.isActive(equipmentPartStack, equipmentPart.getLevel(equipmentPartStack)) && feature.featureType.equalsIgnoreCase(featureType)) {
                  features.put(feature, equipmentPartStack);
               }
            }
         }
      }

      return features;
   }

   public void applyLevelCap(ItemStack equipmentStack, int levelCap) {
      for (ItemStack equipmentPartStack : this.getEquipmentPartStacks(equipmentStack)) {
         ItemEquipmentPart equipmentPart = this.getEquipmentPart(equipmentPartStack);
         if (equipmentPart != null) {
            equipmentPart.setLevel(equipmentPartStack, Math.min(levelCap, equipmentPart.getLevel(equipmentPartStack)));
         }
      }
   }

   public int getHighestLevel(ItemStack equipmentStack) {
      int highestLevel = 0;

      for (ItemStack equipmentPartStack : this.getEquipmentPartStacks(equipmentStack)) {
         ItemEquipmentPart equipmentPart = this.getEquipmentPart(equipmentPartStack);
         if (equipmentPart != null) {
            int partLevel = equipmentPart.getLevel(equipmentPartStack);
            if (partLevel > highestLevel) {
               highestLevel = partLevel;
            }
         }
      }

      return highestLevel;
   }

   public int getPartLevel(ItemStack partStack) {
      Item featureItem = partStack.getItem();
      if (!(featureItem instanceof ItemEquipmentPart)) {
         return 1;
      } else {
         ItemEquipmentPart featurePart = (ItemEquipmentPart)featureItem;
         return featurePart.getLevel(partStack);
      }
   }

   public int getSharpness(ItemStack equipmentStack) {
      int lowestSharpness = SHARPNESS_MAX;

      for (ItemStack equipmentPartStack : this.getEquipmentPartStacks(equipmentStack)) {
         ItemEquipmentPart equipmentPart = this.getEquipmentPart(equipmentPartStack);
         if (equipmentPart != null) {
            int partSharpness = equipmentPart.getSharpness(equipmentPartStack);
            if (partSharpness < lowestSharpness) {
               lowestSharpness = partSharpness;
            }
         }
      }

      return lowestSharpness;
   }

   public boolean addSharpness(ItemStack equipmentStack, int sharpness) {
      boolean sharpnessIncreased = false;
      NonNullList<ItemStack> itemStacks = this.getEquipmentPartStacks(equipmentStack);

      for (ItemStack equipmentPartStack : itemStacks) {
         ItemEquipmentPart equipmentPart = this.getEquipmentPart(equipmentPartStack);
         if (equipmentPart != null) {
            sharpnessIncreased = equipmentPart.addSharpness(equipmentPartStack, sharpness) || sharpnessIncreased;
         }
      }

      CompoundTag nbt = this.getTagCompound(equipmentStack);
      ContainerHelper.saveAllItems(nbt, itemStacks, LycReg.access());
      LycNBT.setTag(equipmentStack, nbt);
      return sharpnessIncreased;
   }

   public boolean removeSharpness(ItemStack equipmentStack, int sharpness) {
      boolean sharpnessDecreased = false;
      NonNullList<ItemStack> itemStacks = this.getEquipmentPartStacks(equipmentStack);

      for (ItemStack equipmentPartStack : itemStacks) {
         ItemEquipmentPart equipmentPart = this.getEquipmentPart(equipmentPartStack);
         if (equipmentPart != null) {
            sharpnessDecreased = equipmentPart.removeSharpness(equipmentPartStack, sharpness) || sharpnessDecreased;
         }
      }

      CompoundTag nbt = this.getTagCompound(equipmentStack);
      ContainerHelper.saveAllItems(nbt, itemStacks, LycReg.access());
      LycNBT.setTag(equipmentStack, nbt);
      return sharpnessDecreased;
   }

   public int getMana(ItemStack equipmentStack) {
      int lowestMana = MANA_MAX;

      for (ItemStack equipmentPartStack : this.getEquipmentPartStacks(equipmentStack)) {
         ItemEquipmentPart equipmentPart = this.getEquipmentPart(equipmentPartStack);
         if (equipmentPart != null) {
            int partMana = equipmentPart.getMana(equipmentPartStack);
            if (partMana < lowestMana) {
               lowestMana = partMana;
            }
         }
      }

      return lowestMana;
   }

   public boolean addMana(ItemStack equipmentStack, int mana) {
      boolean manaIncreased = false;
      NonNullList<ItemStack> itemStacks = this.getEquipmentPartStacks(equipmentStack);

      for (ItemStack equipmentPartStack : itemStacks) {
         ItemEquipmentPart equipmentPart = this.getEquipmentPart(equipmentPartStack);
         if (equipmentPart != null) {
            manaIncreased = equipmentPart.addMana(equipmentPartStack, mana) || manaIncreased;
         }
      }

      CompoundTag nbt = this.getTagCompound(equipmentStack);
      ContainerHelper.saveAllItems(nbt, itemStacks, LycReg.access());
      LycNBT.setTag(equipmentStack, nbt);
      return manaIncreased;
   }

   public boolean removeMana(ItemStack equipmentStack, int mana) {
      boolean manaDecreased = false;
      NonNullList<ItemStack> itemStacks = this.getEquipmentPartStacks(equipmentStack);

      for (ItemStack equipmentPartStack : itemStacks) {
         ItemEquipmentPart equipmentPart = this.getEquipmentPart(equipmentPartStack);
         if (equipmentPart != null) {
            manaDecreased = equipmentPart.removeMana(equipmentPartStack, mana) || manaDecreased;
         }
      }

      CompoundTag nbt = this.getTagCompound(equipmentStack);
      ContainerHelper.saveAllItems(nbt, itemStacks, LycReg.access());
      LycNBT.setTag(equipmentStack, nbt);
      return manaDecreased;
   }

   @Override
   public UseAnim getItemUseAction(ItemStack itemStack) {
      return UseAnim.BOW;
   }

   public int getMaxItemUseDuration(ItemStack stack) {
      return 18000;
   }

   @Override
   public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
      ItemStack itemStack = player.getItemInHand(hand);
      if (this.getMana(itemStack) <= 0) {
         return new InteractionResultHolder(InteractionResult.FAIL, itemStack);
      } else {
         boolean active = false;

         for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "projectile")) {
            ProjectileEquipmentFeature projectileFeature = (ProjectileEquipmentFeature)equipmentFeature;
            if (projectileFeature.onUseSecondary(world, player, hand)) {
               active = true;
            }
         }

         if (active) {
            player.startUsingItem(hand);
            return new InteractionResultHolder(InteractionResult.SUCCESS, itemStack);
         } else {
            return new InteractionResultHolder(InteractionResult.FAIL, itemStack);
         }
      }
   }

   @Override
   public void onUsingTick(ItemStack itemStack, LivingEntity user, int count) {
      if (user.isUsingItem() && this.getMana(itemStack) > 0) {
         boolean usedMana = false;

         for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "projectile")) {
            ProjectileEquipmentFeature projectileFeature = (ProjectileEquipmentFeature)equipmentFeature;
            usedMana = projectileFeature.onHoldSecondary(user, count) || usedMana;
         }

         if (usedMana) {
            this.removeMana(itemStack, 1);
         }
      }
   }

   public void onItemLeftClick(Level world, Player player, InteractionHand hand) {
      ItemStack itemStack = player.getItemInHand(hand);
      if (this.getMana(itemStack) > 0) {
         boolean usedMana = false;

         for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "projectile")) {
            ProjectileEquipmentFeature projectileFeature = (ProjectileEquipmentFeature)equipmentFeature;
            usedMana = projectileFeature.onUsePrimary(world, player, hand) || usedMana;
         }

         if (usedMana) {
            this.removeMana(itemStack, 1);
         }
      }
   }

   @Override
   public InteractionResult onItemUse(Player player, Level world, BlockPos pos, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
      ItemStack itemStack = player.getItemInHand(hand);
      if (this.getMana(itemStack) <= 0) {
         return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
      } else {
         boolean active = false;

         for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "harvest")) {
            HarvestEquipmentFeature harvestFeature = (HarvestEquipmentFeature)equipmentFeature;
            if (harvestFeature.onBlockUsed(world, player, pos, itemStack, facing)) {
               active = true;
            }
         }

         if (active) {
            player.startUsingItem(hand);
            this.removeSharpness(itemStack, 1);
            return InteractionResult.SUCCESS;
         } else {
            return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
         }
      }
   }

   public boolean itemInteractionForEntity(ItemStack itemStack, Player player, LivingEntity target, InteractionHand hand) {
      boolean entityInteraction = false;
      if (this.getMana(itemStack) <= 0) {
         return false;
      } else {
         for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "harvest")) {
            HarvestEquipmentFeature harvestFeature = (HarvestEquipmentFeature)equipmentFeature;
            if (harvestFeature.onEntityInteraction(player, target, itemStack)) {
               entityInteraction = true;
            }
         }

         if (entityInteraction) {
            this.removeSharpness(itemStack, 1);
            return true;
         } else {
            return false;
         }
      }
   }

   public Set<String> getToolClasses(ItemStack itemStack) {
      Map<String, Boolean> toolTypes = new HashMap<>();

      for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "harvest")) {
         HarvestEquipmentFeature harvestFeature = (HarvestEquipmentFeature)equipmentFeature;
         String toolType = harvestFeature.getToolType();
         if (toolType != null) {
            toolTypes.put(toolType, true);
         }
      }

      return toolTypes.keySet();
   }

   public int getHarvestLevel(ItemStack itemStack, String toolClass, Player player, BlockState blockState) {
      if (this.getSharpness(itemStack) <= 0) {
         return -1;
      } else {
         int harvestLevel = -1;

         for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "harvest")) {
            HarvestEquipmentFeature harvestFeature = (HarvestEquipmentFeature)equipmentFeature;
            if (harvestLevel < harvestFeature.harvestLevel && toolClass.equals(harvestFeature.getToolType())) {
               harvestLevel = harvestFeature.harvestLevel;
            }
         }

         return harvestLevel;
      }
   }

   public boolean canHarvestBlock(BlockState blockState, ItemStack itemStack) {
      if (this.getSharpness(itemStack) <= 0) {
         return false;
      } else {
         for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "harvest")) {
            HarvestEquipmentFeature harvestFeature = (HarvestEquipmentFeature)equipmentFeature;
            if (harvestFeature.canHarvestBlock(blockState)) {
               return true;
            }
         }

         return false;
      }
   }

   public float getDestroySpeed(ItemStack itemStack, BlockState blockState) {
      if (this.getSharpness(itemStack) <= 0) {
         return 1.0F;
      } else {
         float speed = 1.0F;

         for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "harvest")) {
            HarvestEquipmentFeature harvestFeature = (HarvestEquipmentFeature)equipmentFeature;
            speed += harvestFeature.getHarvestSpeed(blockState);
         }

         return speed;
      }
   }

   /** 1.12 onBlockDestroyed */
   @Override
   public boolean mineBlock(ItemStack itemStack, Level worldIn, BlockState blockState, BlockPos pos, LivingEntity entityLiving) {
      if (this.getSharpness(itemStack) <= 0) {
         return super.mineBlock(itemStack, worldIn, blockState, pos, entityLiving);
      } else if (worldIn.isClientSide) {
         return super.mineBlock(itemStack, worldIn, blockState, pos, entityLiving);
      } else {
         for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "harvest")) {
            HarvestEquipmentFeature harvestFeature = (HarvestEquipmentFeature)equipmentFeature;
            harvestFeature.onBlockDestroyed(worldIn, blockState, pos, entityLiving);
         }

         this.removeSharpness(itemStack, 1);
         return super.mineBlock(itemStack, worldIn, blockState, pos, entityLiving);
      }
   }

   public boolean hitEntity(ItemStack itemStack, LivingEntity primaryTarget, LivingEntity attacker) {
      if (this.getSharpness(itemStack) <= 0) {
         return true;
      } else {
         ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(attacker);
         boolean attackOnCooldown = false;
         if (extendedEntity != null) {
            int currentCooldown = extendedEntity.getProjectileCooldown(1, "equipment_melee");
            int weaponCooldown = 9 * (int)this.getDamageCooldown(itemStack);
            attackOnCooldown = currentCooldown > 0;
            if (currentCooldown < weaponCooldown) {
               extendedEntity.setProjectileCooldown(1, "equipment_melee", weaponCooldown);
            }
         }

         List<LivingEntity> targets = new ArrayList<>();
         targets.add(primaryTarget);
         if (attacker != null && !attacker.level().isClientSide && !attacker.isShiftKeyDown() && !attackOnCooldown) {
            double sweepAngle = this.getDamageSweep(itemStack) / 2.0;
            if (sweepAngle > 0.0) {
               float sweepDamage = (float)this.getDamageAmount(itemStack);
               double sweepRange = 1.0 + this.getDamageRange(itemStack);

               for (LivingEntity possibleTarget : attacker.level()
                  .getEntitiesOfClass(LivingEntity.class, attacker.getBoundingBox().inflate(sweepRange, sweepRange, sweepRange))) {
                  if (possibleTarget != attacker
                     && possibleTarget != primaryTarget
                     && possibleTarget.isAlive()
                     && !(possibleTarget instanceof Player)) {
                     if (possibleTarget instanceof TamableAnimal) {
                        TamableAnimal possibleTameableTarget = (TamableAnimal)possibleTarget;
                        if (possibleTameableTarget.getOwner() != null && !attacker.level().getServer().isPvpAllowed()
                           || possibleTameableTarget.getOwner() == attacker) {
                           continue;
                        }
                     }

                     if (possibleTarget instanceof TameableCreatureEntity) {
                        TameableCreatureEntity possibleTameableTarget = (TameableCreatureEntity)possibleTarget;
                        if (possibleTameableTarget.getPlayerOwner() != null && !attacker.level().getServer().isPvpAllowed()
                           || possibleTameableTarget.getPlayerOwner() == attacker) {
                           continue;
                        }
                     }

                     double targetXDist = possibleTarget.getX() - attacker.getX();
                     double targetZDist = attacker.getZ() - possibleTarget.getZ();
                     double targetAngleAbsolute = 180.0 + Math.toDegrees(Math.atan2(targetXDist, targetZDist));
                     double targetAngle = Math.abs(targetAngleAbsolute - (double)attacker.getYRot());
                     if (targetAngle > 180.0) {
                        targetAngle = 180.0 - (targetAngle - 180.0);
                     }

                     if (!(targetAngle > sweepAngle)) {
                        targets.add(possibleTarget);
                        DamageSource sweepSource = LycDamage.src(DamageTypes.GENERIC);
                        if (attacker instanceof Player) {
                           sweepSource = EntityDamageSource.causePlayerDamage((Player)attacker);
                        }

                        possibleTarget.hurt(sweepSource, sweepDamage);
                     }
                  }
               }
            }
         }

         if (attacker instanceof Player && targets.size() > 1) {
            Player playerAttacker = (Player)attacker;
            playerAttacker.sweepAttack();
            attacker.level()
               .playLocalSound(
                  null,
                  attacker.getX(),
                  attacker.getY(),
                  attacker.getZ(),
                  SoundEvents.PLAYER_ATTACK_SWEEP,
                  attacker.getSoundSource(),
                  1.0F,
                  1.0F
               );
         }

         boolean usedMana = false;

         for (LivingEntity target : targets) {
            double knockback = this.getDamageKnockback(itemStack);
            if (knockback != 0.0 && attacker != null && target != null) {
               double xDist = attacker.getX() - target.getX();
               double zDist = attacker.getZ() - target.getZ();
               double xzDist = Math.max((double)(float)Math.sqrt(xDist * xDist + zDist * zDist), 0.01);
               double motionCap = 10.0;
               if (target.getDeltaMovement().x < motionCap
                  && target.getDeltaMovement().x > -motionCap
                  && target.getDeltaMovement().z < motionCap
                  && target.getDeltaMovement().z > -motionCap) {
                  target.push(
                     -(xDist / xzDist * knockback + target.getDeltaMovement().x * knockback), 0.0, -(zDist / xzDist * knockback + target.getDeltaMovement().z * knockback)
                  );
               }
            }

            for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "effect")) {
               EffectEquipmentFeature effectFeature = (EffectEquipmentFeature)equipmentFeature;
               effectFeature.onHitEntity(itemStack, target, attacker);
            }

            for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "summon")) {
               SummonEquipmentFeature summonFeature = (SummonEquipmentFeature)equipmentFeature;
               usedMana = summonFeature.onHitEntity(itemStack, target, attacker) || usedMana;
            }

            for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "projectile")) {
               ProjectileEquipmentFeature projectileFeature = (ProjectileEquipmentFeature)equipmentFeature;
               usedMana = projectileFeature.onHitEntity(itemStack, target, attacker) || usedMana;
            }
         }

         if (usedMana) {
            this.removeMana(itemStack, 1);
         }

         this.removeSharpness(itemStack, 1);
         return true;
      }
   }

   /** 1.12 getAttributeModifiers(MAINHAND, stack): the weapon modifiers of this stack's parts. */
   @Override
   public net.minecraft.world.item.component.ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack itemStack) {
      return net.minecraft.world.item.component.ItemAttributeModifiers.builder()
         .add(Attributes.ATTACK_DAMAGE,
            new AttributeModifier(BASE_ATTACK_DAMAGE_ID, this.getDamageAmount(itemStack), AttributeModifier.Operation.ADD_VALUE),
            net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
         .add(Attributes.ATTACK_SPEED,
            new AttributeModifier(BASE_ATTACK_SPEED_ID, -this.getDamageCooldown(itemStack), AttributeModifier.Operation.ADD_VALUE),
            net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
         .build();
   }

   public double getDamageAmount(ItemStack itemStack) {
      if (this.getSharpness(itemStack) <= 0) {
         return 0.0;
      } else {
         double damage = 0.0;

         for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "damage")) {
            DamageEquipmentFeature damageFeature = (DamageEquipmentFeature)equipmentFeature;
            damage += (double)damageFeature.damageAmount;
         }

         return damage;
      }
   }

   public double getDamageCooldown(ItemStack itemStack) {
      double cooldown = 0.0;
      double i = 0.0;

      for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "damage")) {
         DamageEquipmentFeature damageFeature = (DamageEquipmentFeature)equipmentFeature;
         cooldown += damageFeature.damageCooldown;
         i++;
      }

      if (i == 0.0) {
         return 2.4;
      } else {
         cooldown = 2.4 * (cooldown / i);
         return Math.min(3.5, cooldown);
      }
   }

   public double getDamageRange(ItemStack itemStack) {
      double range = 0.0;

      for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "damage")) {
         DamageEquipmentFeature damageFeature = (DamageEquipmentFeature)equipmentFeature;
         range += damageFeature.damageRange;
      }

      return range;
   }

   public double getDamageKnockback(ItemStack itemStack) {
      if (this.getSharpness(itemStack) <= 0) {
         return 0.0;
      } else {
         double knockback = 0.0;

         for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "damage")) {
            DamageEquipmentFeature damageFeature = (DamageEquipmentFeature)equipmentFeature;
            knockback += damageFeature.damageKnockback;
         }

         return knockback;
      }
   }

   public double getDamageSweep(ItemStack itemStack) {
      if (this.getSharpness(itemStack) <= 0) {
         return 0.0;
      } else {
         double sweep = 0.0;

         for (EquipmentFeature equipmentFeature : this.getFeaturesByType(itemStack, "damage")) {
            DamageEquipmentFeature damageFeature = (DamageEquipmentFeature)equipmentFeature;
            sweep += damageFeature.damageSweep;
         }

         return sweep;
      }
   }


   public ResourceLocation getTexture(ItemStack itemStack) {
      return AssetManager.getTexture(this.itemName);
   }
}
