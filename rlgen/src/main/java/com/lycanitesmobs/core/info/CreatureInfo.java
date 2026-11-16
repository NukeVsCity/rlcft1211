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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.entity.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.helpers.JSONHelper;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CreatureInfo {
   protected String name;
   public CreatureType creatureType;
   public List<CreatureGroup> groups = new ArrayList<>();
   public Class<? extends Mob> entityClass;
   public Class<? extends ModelBase> modelClass;
   public ModInfo modInfo;
   public boolean enabled = true;
   public boolean dummy = false;
   public CreatureSpawn creatureSpawn;
   public double width = 0.8;
   public double height = 1.8;
   public int experience = 5;
   public double health = 20.0;
   public double defense = 0.0;
   public double armor = 0.0;
   public double speed = 24.0;
   public double damage = 2.0;
   public double attackSpeed = 1.0;
   public double rangedSpeed = 0.5;
   public double effectDuration = 1.0;
   public double effectAmplifier = -1.0;
   public double pierce = 1.0;
   public double sight = 16.0;
   public double knockbackResistance = 0.0;
   public int packSize = 3;
   public int tamingReputation = 500;
   public int bagSize = 5;
   public int eggBackColor;
   public int eggForeColor;
   public boolean boss = false;
   public Map<Integer, Subspecies> subspecies = new HashMap<>();
   public List<String> loadedSubspeciesSkins = new ArrayList<>();
   public List<ElementInfo> elements = new ArrayList<>();
   public List<String> diets = new ArrayList<>();
   public boolean peaceful = false;
   public boolean farmable = false;
   public boolean summonable = false;
   public boolean tameable = false;
   public boolean mountable = false;
   public boolean perchable = false;
   public int summonCost = 1;
   public int dungeonLevel = -1;
   public int bossNearbyRange = 30;
   public List<ItemDrop> drops = new ArrayList<>();
   protected JsonArray dropsJson;
   public double sizeScale = 1.0;
   public double hitboxScale = 1.0;
   public Vec3 mountOffset = new Vec3(0.0, 1.0, 0.0);
   protected Map<String, Boolean> boolFlags = new HashMap<>();
   protected Map<String, Double> doubleFlags = new HashMap<>();
   protected Map<String, String> stringFlags = new HashMap<>();

   public CreatureInfo(ModInfo modInfo) {
      this.modInfo = modInfo;
      this.creatureSpawn = new CreatureSpawn();
   }

   public void loadFromJSON(JsonObject json) {
      this.name = json.get("name").getAsString();

      try {
         this.entityClass = (Class<? extends Mob>)Class.forName(json.get("entityClass").getAsString());
      } catch (Exception var11) {
         LycanitesMobs.logWarning("", "[Creature] Unable to find the Java Entity Class: " + json.get("entityClass").getAsString() + " for " + this.getName());
         throw new RuntimeException(
            "Creature config loading error, clear your config if updating from an older version of the mod. Missing Class: "
               + json.get("entityClass").getAsString()
               + " Creature: "
               + this.getName()
         );
      }

      if (json.has("enabled")) {
         this.enabled = json.get("enabled").getAsBoolean();
      }

      if (json.has("dummy")) {
         this.dummy = json.get("dummy").getAsBoolean();
      }

      if (!this.dummy) {
         try {
            LycanitesMobs.proxy.loadCreatureModel(this, json.get("modelClass").getAsString());
         } catch (Exception var10) {
            LycanitesMobs.logWarning(
               "", "[Creature] Unable to find a valid Java Model Class: " + json.get("modelClass").getAsString() + " for creature: " + this.getTitle()
            );
         }

         if (json.has("creatureType")) {
            this.creatureType = CreatureManager.getInstance().getCreatureType(json.get("creatureType").getAsString());
            if (this.creatureType == null) {
               LycanitesMobs.logWarning("", "Unable to find the creature type: " + json.get("creatureType").getAsString() + " when load creature: " + this.name);
            }
         }

         if (this.creatureType == null) {
            this.creatureType = CreatureManager.getInstance().getCreatureType("beast");
         }

         if (json.has("groups")) {
            for (String groupName : JSONHelper.getJsonStrings(json.getAsJsonArray("groups"))) {
               CreatureGroup group = CreatureManager.getInstance().getCreatureGroup(groupName);
               if (group != null) {
                  this.groups.add(group);
                  group.addCreature(this);
               } else {
                  LycanitesMobs.logWarning("", "[Creature] Unable to find the Creature Group: " + groupName + " for Creature: " + this.name);
               }
            }
         }

         this.creatureSpawn.loadFromJSON(this, json.get("spawning").getAsJsonObject());
         if (json.has("width")) {
            this.width = json.get("width").getAsDouble();
         }

         if (json.has("height")) {
            this.height = json.get("height").getAsDouble();
         }

         if (json.has("sizeScale")) {
            this.sizeScale = json.get("sizeScale").getAsDouble();
         }

         if (json.has("hitboxScale")) {
            this.hitboxScale = json.get("hitboxScale").getAsDouble();
         }

         this.mountOffset = JSONHelper.getVec3d(json, "mountOffset", this.mountOffset);
         if (json.has("experience")) {
            this.experience = json.get("experience").getAsInt();
         }

         if (json.has("health")) {
            this.health = json.get("health").getAsDouble();
         }

         if (json.has("defense")) {
            this.defense = json.get("defense").getAsDouble();
         }

         if (json.has("armor")) {
            this.armor = json.get("armor").getAsDouble();
         }

         if (json.has("speed")) {
            this.speed = json.get("speed").getAsDouble();
         }

         if (json.has("damage")) {
            this.damage = json.get("damage").getAsDouble();
         }

         if (json.has("attackSpeed")) {
            this.attackSpeed = json.get("attackSpeed").getAsDouble();
         }

         if (json.has("rangedSpeed")) {
            this.rangedSpeed = json.get("rangedSpeed").getAsDouble();
         }

         if (json.has("effectDuration")) {
            this.effectDuration = json.get("effectDuration").getAsDouble();
         }

         if (json.has("effectAmplifier")) {
            this.effectAmplifier = json.get("effectAmplifier").getAsDouble();
         }

         if (json.has("pierce")) {
            this.pierce = json.get("pierce").getAsDouble();
         }

         if (json.has("knockbackResistance")) {
            this.knockbackResistance = json.get("knockbackResistance").getAsDouble();
         }

         if (json.has("sight")) {
            this.sight = json.get("sight").getAsDouble();
         }

         if (json.has("packSize")) {
            this.packSize = json.get("packSize").getAsInt();
         }

         if (json.has("tamingReputation")) {
            this.tamingReputation = json.get("tamingReputation").getAsInt();
         }

         if (json.has("bagSize")) {
            this.bagSize = json.get("bagSize").getAsInt();
         }

         this.eggBackColor = Color.decode(json.get("eggBackColor").getAsString()).getRGB();
         this.eggForeColor = Color.decode(json.get("eggForeColor").getAsString()).getRGB();

         for (JsonElement jsonElement : json.get("subspecies").getAsJsonArray()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Subspecies subspecies = Subspecies.createFromJSON(this, jsonObject);
            this.subspecies.put(subspecies.index, subspecies);
         }

         if (this.subspecies.isEmpty()) {
            throw new RuntimeException(
               "No subspecies were found for " + this.getName() + " there should always be at least 1 default subspecies for the base normal subspecies."
            );
         } else {
            List<String> elementNames = new ArrayList<>();
            if (json.has("element")) {
               elementNames.add(json.get("element").getAsString());
            }

            if (json.has("elements")) {
               elementNames = JSONHelper.getJsonStrings(json.get("elements").getAsJsonArray());
            }

            this.elements.clear();

            for (String elementName : elementNames) {
               ElementInfo element = ElementManager.getInstance().getElement(elementName);
               if (element == null) {
                  throw new RuntimeException("[Creature] The element " + elementName + " cannot be found for: " + this.getName());
               }

               this.elements.add(element);
            }

            if (json.has("diets")) {
               this.diets = JSONHelper.getJsonStrings(json.get("diets").getAsJsonArray());
            }

            if (json.has("boss")) {
               this.boss = json.get("boss").getAsBoolean();
            }

            if (json.has("peaceful")) {
               this.peaceful = json.get("peaceful").getAsBoolean();
            }

            if (json.has("farmable")) {
               this.farmable = json.get("farmable").getAsBoolean();
            }

            if (json.has("summonable")) {
               this.summonable = json.get("summonable").getAsBoolean();
            }

            if (json.has("tameable")) {
               this.tameable = json.get("tameable").getAsBoolean();
            }

            if (json.has("mountable")) {
               this.mountable = json.get("mountable").getAsBoolean();
            }

            if (json.has("perchable")) {
               this.perchable = json.get("perchable").getAsBoolean();
            }

            if (json.has("summonCost")) {
               this.summonCost = json.get("summonCost").getAsInt();
            }

            if (json.has("dungeonLevel")) {
               this.dungeonLevel = json.get("dungeonLevel").getAsInt();
            }

            if (json.has("bossNearbyRange")) {
               this.bossNearbyRange = json.get("bossNearbyRange").getAsInt();
            }

            if (json.has("drops")) {
               for (JsonElement mobDropJson : json.getAsJsonArray("drops")) {
                  ItemDrop itemDrop = ItemDrop.createFromJSON(mobDropJson.getAsJsonObject());
                  if (itemDrop != null) {
                     this.drops.add(itemDrop);
                  } else {
                     LycanitesMobs.logWarning("", "[Creature] Unable to add item drop to creature: " + this.name + ".");
                  }
               }
            }

            if (json.has("flags")) {
               for (JsonElement flagJson : json.getAsJsonArray("flags")) {
                  JsonObject flagJsonObject = flagJson.getAsJsonObject();
                  if (flagJsonObject.has("name") && flagJsonObject.has("value")) {
                     try {
                        this.addFlag(flagJsonObject.get("name").getAsString(), flagJsonObject.get("value").getAsBoolean());
                     } catch (Exception var9) {
                     }

                     try {
                        this.addFlag(flagJsonObject.get("name").getAsString(), flagJsonObject.get("value").getAsDouble());
                     } catch (Exception var8) {
                     }

                     try {
                        this.addFlag(flagJsonObject.get("name").getAsString(), flagJsonObject.get("value").getAsString());
                     } catch (Exception var7) {
                     }
                  } else {
                     LycanitesMobs.logWarning("", "Invalid creature json flag, make sure that the flag has both a name and value, skipping this flag.");
                  }
               }
            }

            if (this.creatureType != null) {
               this.creatureType.addCreature(this);
            }
         }
      }
   }

   public void load() {
      if (!this.dummy) {
         ItemStack achievementStack = new ItemStack(ObjectManager.getItem("mobtoken"));
         achievementStack.setTagInfo("Mob", new StringTag(this.getName()));
         ObjectManager.addStat(this.getName() + ".kill", new StatBase(this.getName() + ".kill", Component.literal(this.getName() + ".kill")));
         ObjectManager.addStat(this.getName() + ".learn", new StatBase(this.getName() + ".learn", Component.literal(this.getName() + ".learn")));
         if (this.isSummonable()) {
            ObjectManager.addStat(this.getName() + ".summon", new StatBase(this.getName() + ".summon", Component.literal(this.getName() + ".summon")));
         }

         if (this.isTameable()) {
            ObjectManager.addStat(this.getName() + ".tame", new StatBase(this.getName() + ".tame", Component.literal(this.getName() + ".tame")));
         }

         this.addSounds("");

         for (Subspecies subspecies : this.subspecies.values()) {
            subspecies.load(this);
         }

         LycanitesMobs.logDebug("Creature", "Creature Loaded: " + this.getName() + " - " + this.entityClass + " (" + this.modInfo.name + ")");
      }
   }

   public void lateLoad() {
      if (!this.dummy) {
         this.creatureSpawn.registerVanillaSpawns(this);
         LycanitesMobs.logDebug("Creature", "Creature Late Loaded: " + this.getName() + " - " + this.entityClass + " (" + this.modInfo.name + ")");
      }
   }

   public void addSounds(String suffix) {
      AssetManager.addSound(this.name + suffix + "_say", this.modInfo, "entity." + this.name + suffix + ".say");
      AssetManager.addSound(this.name + suffix + "_hurt", this.modInfo, "entity." + this.name + suffix + ".hurt");
      AssetManager.addSound(this.name + suffix + "_death", this.modInfo, "entity." + this.name + suffix + ".death");
      AssetManager.addSound(this.name + suffix + "_step", this.modInfo, "entity." + this.name + suffix + ".step");
      AssetManager.addSound(this.name + suffix + "_attack", this.modInfo, "entity." + this.name + suffix + ".attack");
      AssetManager.addSound(this.name + suffix + "_jump", this.modInfo, "entity." + this.name + suffix + ".jump");
      AssetManager.addSound(this.name + suffix + "_fly", this.modInfo, "entity." + this.name + suffix + ".fly");
      if (this.isSummonable() || this.isTameable() || TameableCreatureEntity.class.isAssignableFrom(this.entityClass)) {
         AssetManager.addSound(this.name + suffix + "_tame", this.modInfo, "entity." + this.name + suffix + ".tame");
         AssetManager.addSound(this.name + suffix + "_beg", this.modInfo, "entity." + this.name + suffix + ".beg");
      }

      if (this.isTameable()) {
         AssetManager.addSound(this.name + suffix + "_eat", this.modInfo, "entity." + this.name + suffix + ".eat");
      }

      if (this.isMountable()) {
         AssetManager.addSound(this.name + suffix + "_mount", this.modInfo, "entity." + this.name + suffix + ".mount");
      }

      if (this.isBoss()) {
         AssetManager.addSound(this.name + suffix + "_phase", this.modInfo, "entity." + this.name + suffix + ".phase");
      }
   }

   public String getName() {
      return this.name;
   }

   public String getId() {
      return this.modInfo.modid + ":" + this.getName();
   }

   public Class<? extends Entity> getEntityClass() {
      return this.entityClass;
   }

   public List<CreatureGroup> getGroups() {
      return this.groups;
   }

   public ResourceLocation getResourceLocation() {
      return ResourceLocation.fromNamespaceAndPath(this.modInfo.modid, this.getName());
   }

   public String getLocalisationKey() {
      return this.getName();
   }

   public String getTitle() {
      return LanguageManager.translate("entity." + this.getLocalisationKey() + ".name");
   }

   public String getDescription() {
      return LanguageManager.translate("entity." + this.getLocalisationKey() + ".description");
   }

   public String getHabitatDescription() {
      return LanguageManager.translate("entity." + this.getLocalisationKey() + ".habitat");
   }

   public String getCombatDescription() {
      return LanguageManager.translate("entity." + this.getLocalisationKey() + ".combat");
   }

   public ResourceLocation getIcon() {
      ResourceLocation texture = AssetManager.getTexture(this.getName() + "_icon");
      if (texture == null) {
         AssetManager.addTexture(this.getName() + "_icon", this.modInfo, "textures/guis/creatures/" + this.getName() + "_icon.png");
         texture = AssetManager.getTexture(this.getName() + "_icon");
      }

      return texture;
   }

   public String getElementNames(Subspecies subspecies) {
      List<ElementInfo> elements = this.elements;
      if (subspecies != null && !subspecies.elements.isEmpty()) {
         elements = subspecies.elements;
      }

      if (elements.isEmpty()) {
         return "None";
      } else {
         String elementNames = "";
         boolean firstElement = true;

         for (ElementInfo element : elements) {
            if (!firstElement) {
               elementNames = elementNames + ", ";
            }

            firstElement = false;
            elementNames = elementNames + element.getTitle();
         }

         return elementNames;
      }
   }

   public String getDietNames() {
      if (this.diets.isEmpty()) {
         return "None";
      } else {
         String dietNames = "";
         boolean firstDiet = true;

         for (String diet : this.diets) {
            if (!firstDiet) {
               dietNames = dietNames + ", ";
            }

            firstDiet = false;
            dietNames = dietNames + LanguageManager.translate("diet." + diet);
         }

         return dietNames;
      }
   }

   public String getBiomeNames() {
      List<ResourceLocation> biomes = new ArrayList<>();
      if (this.creatureSpawn.biomesFromTags != null) {
         biomes.addAll(this.creatureSpawn.biomesFromTags);
      }

      if (this.creatureSpawn.biomes != null) {
         biomes.addAll(this.creatureSpawn.biomes);
      }

      if (biomes.isEmpty()) {
         return LanguageManager.translate("gui.beastiary.biomes.none");
      } else {
         String biomeNames = "";
         boolean firstBiome = true;

         for (ResourceLocation biome : biomes) {
            if (!firstBiome) {
               biomeNames = biomeNames + ", ";
            }

            firstBiome = false;
            biomeNames = biomeNames + biome.getBiomeName();
         }

         return biomeNames;
      }
   }

   public String getDropNames() {
      if (this.drops.isEmpty()) {
         return "";
      } else {
         String dropNames = "";
         boolean firstDrop = true;

         for (ItemDrop drop : this.drops) {
            if (!firstDrop) {
               dropNames = dropNames + "\n";
            }

            firstDrop = false;
            dropNames = dropNames + drop.getItemStack().getHoverName();
            dropNames = dropNames + " (";
            if (drop.maxAmount > drop.minAmount) {
               dropNames = dropNames + drop.minAmount + "-" + drop.maxAmount + "X";
            } else {
               dropNames = dropNames + drop.minAmount + "X";
            }

            dropNames = dropNames + " " + drop.chance * 100.0F + "%";
            Subspecies subspecies = this.getSubspecies(0);
            if (drop.subspeciesIndex > 0) {
               subspecies = this.getSubspecies(drop.subspeciesIndex);
               if (subspecies.name != null) {
                  dropNames = dropNames + " ";
                  dropNames = dropNames + subspecies.getTitle();
               }
            }

            if (drop.variantIndex >= 0) {
               dropNames = dropNames + " ";
               Variant variant = subspecies.getVariant(drop.variantIndex);
               if (variant == null) {
                  dropNames = dropNames + LanguageManager.translate("subspecies.normal");
               } else {
                  dropNames = dropNames + variant.getTitle();
               }
            }

            dropNames = dropNames + ")";
         }

         return dropNames;
      }
   }

   public boolean isFarmable() {
      return this.farmable && AgeableCreatureEntity.class.isAssignableFrom(this.entityClass);
   }

   public boolean isSummonable() {
      return this.summonable && TameableCreatureEntity.class.isAssignableFrom(this.entityClass);
   }

   public boolean isTameable() {
      return this.tameable && TameableCreatureEntity.class.isAssignableFrom(this.entityClass);
   }

   public boolean isMountable() {
      return this.mountable && RideableCreatureEntity.class.isAssignableFrom(this.entityClass);
   }

   public boolean isPerchable() {
      return this.perchable;
   }

   public boolean isBoss() {
      return this.boss;
   }

   public Subspecies getSubspecies(int index) {
      return !this.subspecies.containsKey(index) ? this.subspecies.get(0) : this.subspecies.get(index);
   }

   public Subspecies getRandomSubspecies(LivingEntity entity) {
      LycanitesMobs.logDebug("Subspecies", "~0===== Subspecies =====0~");
      LycanitesMobs.logDebug("Subspecies", "Selecting subspecies for: " + entity);
      LycanitesMobs.logDebug("Subspecies", "Subspecies Available: " + this.subspecies.size());
      List<Subspecies> possibleSubspecies = new ArrayList<>();
      int highestPriority = 0;

      for (Subspecies subspeciesEntry : this.subspecies.values()) {
         if (subspeciesEntry.canSpawn(entity)) {
            possibleSubspecies.add(subspeciesEntry);
            if (subspeciesEntry.priority > highestPriority) {
               highestPriority = subspeciesEntry.priority;
            }
         }
      }

      if (possibleSubspecies.isEmpty()) {
         LycanitesMobs.logWarning(
            "",
            "[Subspecies] No subspecies allowed for "
               + this.getName()
               + ", there should always be a default subspecies, returning the first subspecies found for now."
         );
         return this.subspecies.get(0);
      } else {
         if (highestPriority > 0) {
            for (Subspecies subspeciesEntryx : possibleSubspecies.toArray(new Subspecies[possibleSubspecies.size()])) {
               if (subspeciesEntryx.priority < highestPriority) {
                  possibleSubspecies.remove(subspeciesEntryx);
               }
            }
         }

         LycanitesMobs.logDebug("Subspecies", "Subspecies Allowed: " + possibleSubspecies.size() + " Highest Priority: " + highestPriority);
         if (possibleSubspecies.size() == 1) {
            return possibleSubspecies.get(0);
         } else {
            int randomIndex = entity.getRandom().nextInt(possibleSubspecies.size());
            return possibleSubspecies.get(randomIndex);
         }
      }
   }

   public int getFriendlyReputation() {
      return Math.round((float)this.tamingReputation / 2.0F);
   }

   public int getTamingReputation() {
      return this.tamingReputation;
   }

   public boolean canEat(ItemStack itemStack) {
      if (this.diets.isEmpty()) {
         return false;
      } else {
         for (String diet : this.diets) {
            if (ObjectLists.inItemList("diet_" + diet, itemStack)) {
               return true;
            }
         }

         return false;
      }
   }

   public Mob createEntity(Level world) {
      try {
         return this.entityClass == null ? null : this.entityClass.getConstructor(Level.class).newInstance(world);
      } catch (Exception var3) {
         var3.printStackTrace();
         return null;
      }
   }

   public void addFlag(String flagName, boolean flagValue) {
      this.boolFlags.put(flagName, flagValue);
   }

   public void addFlag(String flagName, double flagValue) {
      this.doubleFlags.put(flagName, flagValue);
   }

   public void addFlag(String flagName, String flagValue) {
      this.stringFlags.put(flagName, flagValue);
   }

   public boolean getFlag(String flagName, boolean defaultValue) {
      return !this.boolFlags.containsKey(flagName) ? defaultValue : this.boolFlags.get(flagName);
   }

   public int getFlag(String flagName, int defaultValue) {
      return !this.doubleFlags.containsKey(flagName) ? defaultValue : Math.round(this.doubleFlags.get(flagName).floatValue());
   }

   public double getFlag(String flagName, double defaultValue) {
      return !this.doubleFlags.containsKey(flagName) ? defaultValue : this.doubleFlags.get(flagName);
   }

   public String getFlag(String flagName, String defaultValue) {
      return !this.stringFlags.containsKey(flagName) ? defaultValue : this.stringFlags.get(flagName);
   }
}
