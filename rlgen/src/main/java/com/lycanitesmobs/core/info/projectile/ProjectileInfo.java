package com.lycanitesmobs.core.info.projectile;

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
import com.lycanitesmobs.core.dispenser.DispenserBehaviorBase;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.CustomProjectileEntity;
import com.lycanitesmobs.core.entity.CustomProjectileModelEntity;
import com.lycanitesmobs.core.helpers.JSONHelper;
import com.lycanitesmobs.core.info.ElementInfo;
import com.lycanitesmobs.core.info.ElementManager;
import com.lycanitesmobs.core.info.ModInfo;
import com.lycanitesmobs.core.info.projectile.behaviours.ProjectileBehaviour;
import com.lycanitesmobs.core.item.ChargeItem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ProjectileInfo {
   protected String name;
   public Class<? extends Entity> entityClass = CustomProjectileEntity.class;
   public String modelClassName;
   public ModInfo modInfo;
   public Item chargeItem;
   public String chargeItemName;
   public boolean noChargeItem = false;
   public DispenserBehaviorBase dispenserBehaviour;
   public float width = 0.75F;
   public float height = 0.75F;
   public float scale = 0.5F;
   public int lifetime = 200;
   public int damage = 1;
   public int pierce = 1;
   public double knockbackChance = 0.0;
   public int effectDuration = 1;
   public int effectAmplifier = 1;
   public double velocity = 1.1;
   public double weight = 1.0;
   public float rollSpeed = 0.0F;
   public List<ElementInfo> elements = new ArrayList<>();
   public List<ProjectileBehaviour> behaviours = new ArrayList<>();
   public boolean waterproof = false;
   public boolean lavaproof = false;
   public boolean cutGrass = false;
   public boolean ripper = false;
   public boolean pierceBlocks = false;
   public boolean impactSound = false;
   public int animationFrames = 1;
   public boolean glow = false;
   public boolean burningEffect = false;
   public int particleCount = 0;
   public ParticleOptions particleType = null;
   public ParticleOptions waterParticleType = null;

   public ProjectileInfo(ModInfo modInfo) {
      this.modInfo = modInfo;
   }

   public void loadFromJSON(JsonObject json) {
      this.name = json.get("name").getAsString();
      if (json.has("chargeItemName")) {
         this.chargeItemName = json.get("chargeItemName").getAsString();
      } else {
         this.chargeItemName = this.name + "charge";
      }

      if (json.has("noChargeItem")) {
         this.noChargeItem = json.get("noChargeItem").getAsBoolean();
      }

      if (json.has("entityClass")) {
         try {
            this.entityClass = (Class<? extends Entity>)Class.forName(json.get("entityClass").getAsString());
         } catch (Exception var7) {
            LycanitesMobs.logWarning(
               "", "[Projectile] Unable to find the Java Entity Class: " + json.get("entityClass").getAsString() + " for " + this.getName()
            );
         }
      }

      if (json.has("modelClass")) {
         this.modelClassName = json.get("modelClass").getAsString();
      }

      if (json.has("width")) {
         this.width = json.get("width").getAsFloat();
      }

      if (json.has("height")) {
         this.height = json.get("height").getAsFloat();
      }

      if (json.has("scale")) {
         this.scale = json.get("scale").getAsFloat();
      }

      if (json.has("damage")) {
         this.damage = json.get("damage").getAsInt();
      }

      if (json.has("pierce")) {
         this.pierce = json.get("pierce").getAsInt();
      }

      if (json.has("knockbackChance")) {
         this.knockbackChance = json.get("knockbackChance").getAsDouble();
      }

      if (json.has("effectDuration")) {
         this.effectDuration = json.get("effectDuration").getAsInt();
      }

      if (json.has("effectAmplifier")) {
         this.effectAmplifier = json.get("effectAmplifier").getAsInt();
      }

      if (json.has("velocity")) {
         this.velocity = json.get("velocity").getAsDouble();
      }

      if (json.has("weight")) {
         this.weight = json.get("weight").getAsDouble();
      }

      if (json.has("lifetime")) {
         this.lifetime = json.get("lifetime").getAsInt();
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
               "[Creature] Unable to initialise Projectile Info for " + this.getName() + " as the element " + elementName + " cannot be found."
            );
         }

         this.elements.add(element);
      }

      if (json.has("behaviours")) {
         JsonArray jsonArray = json.get("behaviours").getAsJsonArray();
         Iterator<JsonElement> jsonIterator = jsonArray.iterator();

         while (jsonIterator.hasNext()) {
            JsonObject behaviorJson = jsonIterator.next().getAsJsonObject();
            ProjectileBehaviour projectileBehaviour = ProjectileBehaviour.createFromJSON(behaviorJson);
            if (projectileBehaviour != null) {
               this.behaviours.add(projectileBehaviour);
            } else {
               LycanitesMobs.logWarning("", "Unable to load Projectile Behaviour: " + behaviorJson.get("type").getAsString());
            }
         }
      }

      if (json.has("waterproof")) {
         this.waterproof = json.get("waterproof").getAsBoolean();
      }

      if (json.has("lavaproof")) {
         this.lavaproof = json.get("lavaproof").getAsBoolean();
      }

      if (json.has("cutGrass")) {
         this.cutGrass = json.get("cutGrass").getAsBoolean();
      }

      if (json.has("ripper")) {
         this.ripper = json.get("ripper").getAsBoolean();
      }

      if (json.has("pierceBlocks")) {
         this.pierceBlocks = json.get("pierceBlocks").getAsBoolean();
      }

      if (json.has("impactSound")) {
         this.impactSound = json.get("impactSound").getAsBoolean();
      }

      if (json.has("animationFrames")) {
         this.animationFrames = json.get("animationFrames").getAsInt();
      }

      if (json.has("glow")) {
         this.glow = json.get("glow").getAsBoolean();
      }

      if (json.has("burningEffect")) {
         this.burningEffect = json.get("burningEffect").getAsBoolean();
      }

      if (json.has("particleCount")) {
         this.particleCount = json.get("particleCount").getAsInt();
      }

      if (json.has("particleType")) {
         String particleTypeName = json.get("particleType").getAsString();
         this.particleType = ParticleTypes.getByName(particleTypeName);
      }

      if (json.has("waterParticleType")) {
         String waterParticleTypeName = json.get("waterParticleType").getAsString();
         this.waterParticleType = ParticleTypes.getByName(waterParticleTypeName);
      }
   }

   public void load() {
      if (!this.noChargeItem) {
         this.chargeItem = ObjectManager.getItem(this.chargeItemName);
         if (this.chargeItem == null) {
            this.chargeItem = new ChargeItem(this);
            ObjectManager.addItem(this.chargeItemName, this.chargeItem);
         }

         this.dispenserBehaviour = new DispenserBehaviorBase(this);
         BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this.chargeItem, this.dispenserBehaviour);
      }

      AssetManager.addSound(this.name, this.modInfo, "projectile." + this.name);
      if (this.impactSound) {
         AssetManager.addSound(this.name + "_impact", this.modInfo, "projectile." + this.name + ".impact");
      }
   }

   public String getName() {
      return this.name;
   }

   public String getId() {
      return this.modInfo.modid + ":" + this.getName();
   }

   public ResourceLocation getResourceLocation() {
      return ResourceLocation.fromNamespaceAndPath(this.modInfo.modid, this.getName());
   }

   public String getLocalisationKey() {
      return this.getName();
   }

   public String getTitle() {
      return LanguageManager.translate("entity." + this.getLocalisationKey());
   }

   public BaseProjectileEntity createProjectile(Level world, LivingEntity entityLivingBase) {
      return (BaseProjectileEntity)(this.modelClassName != null
         ? new CustomProjectileModelEntity(world, entityLivingBase, this)
         : new CustomProjectileEntity(world, entityLivingBase, this));
   }

   public BaseProjectileEntity createProjectile(Level world, double x, double y, double z) {
      return (BaseProjectileEntity)(this.modelClassName != null
         ? new CustomProjectileModelEntity(world, x, y, z, this)
         : new CustomProjectileEntity(world, x, y, z, this));
   }

   public SoundEvent getLaunchSound() {
      return AssetManager.getSound(this.name);
   }

   public SoundEvent getImpactSound() {
      return AssetManager.getSound(this.name + "_impact");
   }
}
