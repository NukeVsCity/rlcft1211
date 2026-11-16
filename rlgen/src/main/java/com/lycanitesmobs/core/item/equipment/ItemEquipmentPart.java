package com.lycanitesmobs.core.item.equipment;

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
import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.helpers.JSONHelper;
import com.lycanitesmobs.core.info.ElementInfo;
import com.lycanitesmobs.core.info.ElementManager;
import com.lycanitesmobs.core.info.ModInfo;
import com.lycanitesmobs.core.item.ChargeItem;
import com.lycanitesmobs.core.item.ItemBase;
import com.lycanitesmobs.core.item.equipment.features.EquipmentFeature;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.vecmath.Vector4f;


public class ItemEquipmentPart extends ItemBase {
   public static int BASE_LEVELUP_EXPERIENCE = 500;
   public static ItemStack ITEMSTACK_TO_RENDER;
   public static Map<String, List<ItemEquipmentPart>> MOB_PART_DROPS = new HashMap<>();
   public List<EquipmentFeature> features = new ArrayList<>();
   public List<ElementInfo> elements = new ArrayList<>();
   public String slotType;
   public String dropMobId;
   public float dropChance = 1.0F;
   public int levelMin = 1;
   public int levelMax = 3;

   public ItemEquipmentPart(ModInfo groupInfo) {
      this.modInfo = groupInfo;
      this.setMaxStackSize(1);
      this.setCreativeTab(LycanitesMobs.equipmentPartsTab);
   }

   public void loadFromJSON(JsonObject json) {
      this.itemName = json.get("itemName").getAsString();
      this.slotType = json.get("slotType").getAsString();
      if (json.has("dropMobId")) {
         this.dropMobId = json.get("dropMobId").getAsString();
         if (!"".equals(this.dropMobId)) {
            if (!MOB_PART_DROPS.containsKey(this.dropMobId)) {
               MOB_PART_DROPS.put(this.dropMobId, new ArrayList<>());
            }

            MOB_PART_DROPS.get(this.dropMobId).add(this);
         }
      }

      if (json.has("dropChance")) {
         this.dropChance = json.get("dropChance").getAsFloat();
      }

      if (json.has("levelMin")) {
         this.levelMin = json.get("levelMin").getAsInt();
      }

      if (json.has("levelMax")) {
         this.levelMax = json.get("levelMax").getAsInt();
      }

      List<String> elementNames = new ArrayList<>();
      if (json.has("elements")) {
         elementNames = JSONHelper.getJsonStrings(json.get("elements").getAsJsonArray());
      }

      this.elements.clear();

      for (String elementName : elementNames) {
         ElementInfo element = ElementManager.getInstance().getElement(elementName);
         if (element == null) {
            throw new RuntimeException(
               "[Equipment] Unable to initialise Equipment Part " + this.getDescriptionId() + " as the element " + elementName + " cannot be found."
            );
         }

         this.elements.add(element);
      }

      if (json.has("features")) {
         JsonArray jsonArray = json.get("features").getAsJsonArray();
         Iterator<JsonElement> jsonIterator = jsonArray.iterator();

         while (jsonIterator.hasNext()) {
            JsonObject featureJson = jsonIterator.next().getAsJsonObject();
            if (!this.addFeature(EquipmentFeature.createFromJSON(featureJson))) {
               LycanitesMobs.logWarning("", "[Equipment] The feature " + featureJson.toString() + " was unable to be added, check the JSON format.");
            }
         }
      }

      this.sortFeatures();
      LycReg.setName(this, this.modInfo.modid, this.itemName);
      this.setTranslationKey(this.itemName);
      AssetManager.addTexture(this.itemName, this.modInfo, "textures/equipment/" + this.itemName + ".png");
   }

   @Override
   public String getItemStackDisplayName(ItemStack itemStack) {
      String displayName = LanguageManager.translate(this.getTranslationKey(itemStack) + ".name");
      return displayName + " " + LanguageManager.translate("equipment.level") + " " + this.getLevel(itemStack);
   }

   @Override
   public void addInformation(ItemStack itemStack, Level world, List<String> tooltip, TooltipFlag tooltipFlag) {
      super.addInformation(itemStack, world, tooltip, tooltipFlag);
      FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

      for (String description : this.getAdditionalDescriptions(itemStack, world, tooltipFlag)) {
         for (Object formattedDescription : fontRenderer.listFormattedStringToWidth(description, DESCRIPTION_WIDTH)) {
            if (formattedDescription instanceof String) {
               tooltip.add("§3" + formattedDescription);
            }
         }
      }
   }

   @Override
   public String getDescription(ItemStack stack, Level worldIn, List<String> tooltip, TooltipFlag flagIn) {
      return LanguageManager.translate("item.equipmentpart.description");
   }

   public List<String> getAdditionalDescriptions(ItemStack itemStack, Level world, TooltipFlag tooltipFlag) {
      List<String> descriptions = new ArrayList<>();
      int level = this.getLevel(itemStack);
      int experience = this.getExperience(itemStack);
      int experienceMax = this.getExperienceForNextLevel(itemStack);
      String baseFeature = LanguageManager.translate("equipment.slottype") + " " + LanguageManager.translate("equipment.slot." + this.slotType);
      baseFeature = baseFeature + "\n" + LanguageManager.translate("equipment.level") + " " + level + "/" + this.levelMax;
      if (level < this.levelMax) {
         baseFeature = baseFeature + "\n" + LanguageManager.translate("entity.experience") + ": " + experience + "/" + experienceMax;
      }

      descriptions.add("-------------------\n");
      descriptions.add(baseFeature);
      if (!this.elements.isEmpty()) {
         String elementFeature = LanguageManager.translate("equipment.element") + " " + this.getElementNames();
         descriptions.add(elementFeature);
      }

      descriptions.add("-------------------\n");

      for (EquipmentFeature feature : this.features) {
         String featureDescription = feature.getDescription(itemStack, level);
         if (featureDescription != null && !"".equals(featureDescription)) {
            descriptions.add(featureDescription);
         }
      }

      return descriptions;
   }

   @Override
   public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
      return super.getAttributeModifiers(slot, stack);
   }

   public int getMetadata(ItemStack stack) {
      ITEMSTACK_TO_RENDER = stack;
      return super.getMetadata(stack);
   }

   public FontRenderer getFontRenderer(ItemStack stack) {
      return LycanitesMobs.proxy.getFontRenderer();
   }

   public void randomizeLevel(Level world, ItemStack itemStack) {
      int level = this.levelMax;
      if (this.levelMin < this.levelMax) {
         level = this.levelMin + world.random.nextInt(this.levelMax - this.levelMin + 1);
      }

      this.setLevel(itemStack, level);
   }

   public boolean addFeature(EquipmentFeature feature) {
      if (feature == null) {
         LycanitesMobs.logWarning("", "[Equipment] Unable to add a null feature to " + this);
         return false;
      } else if (feature.featureType == null) {
         LycanitesMobs.logWarning("", "[Equipment] Feature type not set for part " + this);
         return false;
      } else {
         this.features.add(feature);
         return true;
      }
   }

   public void sortFeatures() {
      Comparator<EquipmentFeature> comparator = (o1, o2) -> o1.featureType.compareToIgnoreCase(o2.featureType);
      this.features.sort(comparator);
   }

   public void setLevel(ItemStack itemStack, int level) {
      CompoundTag nbt = LycNBT.get(this);
      nbt.putInt("equipmentLevel", level);
      LycNBT.put(itemStack, nbt);
   }

   public int getLevel(ItemStack itemStack) {
      CompoundTag nbt = LycNBT.get(this);
      int level = 1;
      if (nbt.contains("equipmentLevel")) {
         level = nbt.getInt("equipmentLevel");
      }

      return level;
   }

   public void setExperience(ItemStack itemStack, int experience) {
      CompoundTag nbt = LycNBT.get(this);
      nbt.putInt("equipmentExperience", experience);
      LycNBT.put(itemStack, nbt);
   }

   public void addExperience(ItemStack itemStack, int experience) {
      int currentLevel = this.getLevel(itemStack);
      if (currentLevel >= this.levelMax) {
         this.setExperience(itemStack, 0);
      }

      int increasedExperience = this.getExperience(itemStack) + experience;
      int nextLevelExperience = this.getExperienceForNextLevel(itemStack);
      if (increasedExperience >= nextLevelExperience) {
         increasedExperience -= nextLevelExperience;
         this.setLevel(itemStack, currentLevel + 1);
      }

      this.setExperience(itemStack, increasedExperience);
   }

   public int getExperience(ItemStack itemStack) {
      CompoundTag nbt = LycNBT.get(this);
      int experience = 0;
      if (nbt.contains("equipmentExperience")) {
         experience = nbt.getInt("equipmentExperience");
      }

      return experience;
   }

   public int getExperienceForNextLevel(ItemStack itemStack) {
      return BASE_LEVELUP_EXPERIENCE + Math.round((float)(BASE_LEVELUP_EXPERIENCE * (this.getLevel(itemStack) - 1)) * 0.25F);
   }

   public boolean isLevelingChargeItem(ItemStack itemStack) {
      if (itemStack.getItem() instanceof ChargeItem) {
         ChargeItem chargeItem = (ChargeItem)itemStack.getItem();

         for (ElementInfo elementInfo : this.elements) {
            if (chargeItem.getElements().contains(elementInfo)) {
               return true;
            }
         }
      }

      return false;
   }

   public int getExperienceFromChargeItem(ItemStack itemStack) {
      int experience = 0;
      if (itemStack.getItem() instanceof ChargeItem) {
         ChargeItem chargeItem = (ChargeItem)itemStack.getItem();

         for (ElementInfo elementInfo : this.elements) {
            if (chargeItem.getElements().contains(elementInfo)) {
               experience += ChargeItem.CHARGE_EXPERIENCE;
            }
         }
      }

      return experience;
   }

   public int getSharpness(ItemStack itemStack) {
      CompoundTag nbt = LycNBT.get(this);
      int sharpness = ItemEquipment.SHARPNESS_MAX;
      if (nbt.contains("equipmentSharpness")) {
         sharpness = nbt.getInt("equipmentSharpness");
      }

      return sharpness;
   }

   public void setSharpness(ItemStack itemStack, int sharpness) {
      CompoundTag nbt = LycNBT.get(this);
      nbt.putInt("equipmentSharpness", Math.max(Math.min(sharpness, ItemEquipment.SHARPNESS_MAX), 0));
      LycNBT.put(itemStack, nbt);
   }

   public boolean addSharpness(ItemStack itemStack, int sharpness) {
      int currentSharpness = this.getSharpness(itemStack);
      if (currentSharpness >= ItemEquipment.SHARPNESS_MAX) {
         return false;
      } else {
         this.setSharpness(itemStack, this.getSharpness(itemStack) + sharpness);
         return true;
      }
   }

   public boolean removeSharpness(ItemStack itemStack, int sharpness) {
      int currentSharpness = this.getSharpness(itemStack);
      if (currentSharpness <= 0) {
         return false;
      } else {
         this.setSharpness(itemStack, this.getSharpness(itemStack) - sharpness);
         return true;
      }
   }

   public int getMana(ItemStack itemStack) {
      CompoundTag nbt = LycNBT.get(this);
      int mana = ItemEquipment.MANA_MAX;
      if (nbt.contains("equipmentMana")) {
         mana = nbt.getInt("equipmentMana");
      }

      return mana;
   }

   public void setMana(ItemStack itemStack, int mana) {
      CompoundTag nbt = LycNBT.get(this);
      nbt.putInt("equipmentMana", Math.max(Math.min(mana, ItemEquipment.MANA_MAX), 0));
      LycNBT.put(itemStack, nbt);
   }

   public boolean addMana(ItemStack itemStack, int mana) {
      int currentMana = this.getMana(itemStack);
      if (currentMana >= ItemEquipment.MANA_MAX) {
         return false;
      } else {
         this.setMana(itemStack, this.getMana(itemStack) + mana);
         return true;
      }
   }

   public boolean removeMana(ItemStack itemStack, int mana) {
      int currentMana = this.getMana(itemStack);
      if (currentMana <= 0) {
         return false;
      } else {
         this.setMana(itemStack, this.getMana(itemStack) - mana);
         return true;
      }
   }

   public Vector4f getColor(ItemStack itemStack) {
      CompoundTag nbt = LycNBT.get(this);
      float r = 1.0F;
      float g = 1.0F;
      float b = 1.0F;
      if (nbt.contains("equipmentColorR")) {
         r = nbt.getFloat("equipmentColorR");
      }

      if (nbt.contains("equipmentColorG")) {
         g = nbt.getFloat("equipmentColorG");
      }

      if (nbt.contains("equipmentColorB")) {
         b = nbt.getFloat("equipmentColorB");
      }

      return new Vector4f(r, g, b, 1.0F);
   }

   public void setColor(ItemStack itemStack, float red, float green, float blue) {
      CompoundTag nbt = LycNBT.get(this);
      nbt.putFloat("equipmentColorR", red);
      nbt.putFloat("equipmentColorG", green);
      nbt.putFloat("equipmentColorB", blue);
      LycNBT.put(itemStack, nbt);
   }

   public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
      if (this.isInCreativeTab(tab)) {
         for (int level = 1; level <= this.levelMax; level++) {
            ItemStack itemStack = new ItemStack(this, 1);
            this.setLevel(itemStack, level);
            items.add(itemStack);
         }
      }
   }

   public boolean hasElement(ElementInfo element) {
      return this.elements.contains(element);
   }

   public String getElementNames() {
      String elementNames = "";
      boolean firstElement = true;

      for (ElementInfo element : this.elements) {
         if (!firstElement) {
            elementNames = elementNames + ", ";
         }

         firstElement = false;
         elementNames = elementNames + element.getTitle();
      }

      return elementNames;
   }

   @Override
   public ModelResourceLocation getModelResourceLocation() {
      return new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath("lycanitesmobs", "equipmentpart"), "inventory");
   }

   public ResourceLocation getTexture(ItemStack itemStack, String suffix) {
      if (AssetManager.getTexture(this.itemName + suffix) == null) {
         AssetManager.addTexture(this.itemName + suffix, this.modInfo, "textures/equipment/" + this.itemName.toLowerCase() + suffix + ".png");
      }

      return AssetManager.getTexture(this.itemName + suffix);
   }
}
