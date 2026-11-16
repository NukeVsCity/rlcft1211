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
import com.lycanitesmobs.core.config.ConfigBase;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;


public class CreatureConfig {
   public boolean subspeciesTags = true;
   public int idleSoundTicks = 100;
   public boolean disableModelAlpha = false;
   public boolean disableBlockParticles = false;
   public int startingLevelMin = 1;
   public int startingLevelMax = 5;
   public double levelPerDay = 0.0;
   public int levelPerDayMax = 100;
   public double levelPerLocalDifficulty = 1.5;
   public boolean ownerTags = true;
   public boolean tamingEnabled = true;
   public boolean mountingEnabled = true;
   public boolean mountingFlightEnabled = true;
   public boolean friendlyFire = true;
   public int petRespawnTime = 3600;
   public int petFollowDistance = 8;
   public int[] soulboundDimensionList;
   public boolean soulboundDimensionListWhitelist = false;
   public int[] summonDimensionList;
   public boolean summonDimensionListWhitelist = false;
   public boolean beastiaryKnowledgeMessages = true;
   public int creatureProximityKnowledge = 0;
   public int creatureKillKnowledge = 50;
   public int creatureBreedKnowledge = 400;
   public int creatureTreatKnowledge = 100;
   public int creatureStudyKnowledge = 100;
   public int creatureStudyCooldown = 200;
   public double creatureVariantKnowledgeScale = 2.0;
   public double creatureBossKnowledgeScale = 5.0;
   public double bossAntiFlight = 10.0;
   public int bossDamageCap = 50;
   public boolean predatorsAttackAnimals = true;
   public boolean mobsAttackVillagers = true;
   public boolean animalsFightBack = false;
   public boolean elementalFusion = true;
   public String elementalFusionLevelMix = "both";
   public double elementalFusionLevelMultiplier = 10.0;
   public boolean disablePickupOffsets = false;
   public boolean suffocationImmunity = false;
   public boolean drownImmunity = false;
   public boolean packTreatLuring = false;
   public boolean variantSpawn = true;
   public boolean randomSizes = true;
   public double randomSizeMin = 0.85;
   public double randomSizeMax = 1.15;
   public String globalDropsString = "";
   protected List<ItemDrop> globalDrops;

   public void loadConfig(ConfigBase config) {
      config.setCategoryComment("Client", "Mostly client side settings that affect visuals or sounds such as mob names or inventory tabs, etc.");
      this.subspeciesTags = config.getBool(
         "Client", "Subspecies Tags", this.subspeciesTags, "If true, all mobs that are a subspecies will always show their nametag."
      );
      this.idleSoundTicks = config.getInt("Client", "Idle Sound Ticks", this.idleSoundTicks, "The minimum interval in ticks between random idle sounds.");
      this.disableModelAlpha = config.getBool(
         "Client",
         "Disable Model Alpha",
         this.disableModelAlpha,
         "If true, alpha is disabled on mob textures, this can make them look undesirable but can increase performance on low end systems."
      );
      this.disableBlockParticles = config.getBool(
         "Client",
         "Disable Block Particles",
         this.disableBlockParticles,
         "If true, block particles are not spawned by mobs (useful for visual mods that create 3D block particles which can cause lag in high numbers)."
      );
      config.setCategoryComment(
         "Base Starting Level",
         "The base starting level is the level every mob will start at. Mob Events, Special Spawners and other things will then add onto this base level."
      );
      this.startingLevelMin = config.getInt(
         "Base Starting Level", "Starting Level Min", this.startingLevelMin, "The minimum base starting level of every mob. Cannot be less than 1."
      );
      this.startingLevelMax = config.getInt(
         "Base Starting Level",
         "Starting Level Max",
         this.startingLevelMax,
         "The maximum base starting level of every mob. Ignored when not greater than the min level."
      );
      this.levelPerDay = config.getDouble(
         "Base Starting Level",
         "Level Gain Per Day",
         this.levelPerDay,
         "Increases the base start level by this amount of every world day that has gone by, use this to slowly level up mobs as the world gets older. Fractions can be used such as 0.05 levels per day. The levels are rounded down so +0.9 would be +0 levels."
      );
      this.levelPerDayMax = config.getInt(
         "Base Starting Level", "Level Gain Per Day Max", this.levelPerDayMax, "The maximum level to be able gain from levels per day."
      );
      this.levelPerLocalDifficulty = config.getDouble(
         "Base Starting Level",
         "Level Gain Per Local Difficulty",
         this.levelPerLocalDifficulty,
         "How many levels a mob gains multiplied by the local area difficulty level. Staying in an area for a while slowly increases the difficulty of that area ranging from 0.00 to 6.75. So 1.5 means level 10 at full local area difficulty."
      );
      config.setCategoryComment("Pets", "Here you can control all settings related to taming and mounting.");
      this.ownerTags = config.getBool("Pets", "Owner Tags", this.ownerTags, "If true, tamed mobs will display their owner's name in their name tag.");
      this.tamingEnabled = config.getBool("Pets", "Taming", this.tamingEnabled, "Set to false to disable pet/mount taming.");
      this.mountingEnabled = config.getBool("Pets", "Mounting", this.mountingEnabled, "Set to false to disable mounts.");
      this.mountingFlightEnabled = config.getBool(
         "Pets", "Flying Mounting", this.mountingFlightEnabled, "Set to false to disable flying mounts, if all mounts are disable this option doesn't matter."
      );
      this.friendlyFire = config.getBool(
         "Pets", "Friendly Fire", this.friendlyFire, "If true, pets, minions, etc can't harm their owners (with ranged attacks, etc)."
      );
      this.petRespawnTime = config.getInt("Pets", "Respawn Time", this.petRespawnTime, "The time in tics that it takes for a pet to respawn.");
      this.petFollowDistance = config.getInt(
         "Pets", "Follow Distance", this.petFollowDistance, "How far in blocks pets stray from their owner when set to follow."
      );
      String dimensionListValue = config.getString(
         "Pets", "Soulbound Dimensions", "", "A global list of dimension ids for restricting where soulbound pets and mounts are allowed."
      );
      List<Integer> dimensionEntries = new ArrayList<>();

      for (String dimensionEntry : dimensionListValue.replace(" ", "").split(",")) {
         if (NumberUtils.isCreatable(dimensionEntry)) {
            dimensionEntries.add(Integer.parseInt(dimensionEntry));
         }
      }

      this.soulboundDimensionList = ArrayUtils.toPrimitive(dimensionEntries.toArray(new Integer[dimensionEntries.size()]));
      this.soulboundDimensionListWhitelist = config.getBool(
         "Pets",
         "Soulbound Dimensions Whitelist",
         this.soulboundDimensionListWhitelist,
         "If set to true the soulbound dimension list acts as a whitelist, otherwise it is a blacklist."
      );
      dimensionListValue = config.getString(
         "Pets", "Summoning Dimensions", "", "A global list of dimension ids for restricting where summoning minions is allowed."
      );
      dimensionEntries = new ArrayList<>();

      for (String dimensionEntryx : dimensionListValue.replace(" ", "").split(",")) {
         if (NumberUtils.isCreatable(dimensionEntryx)) {
            dimensionEntries.add(Integer.parseInt(dimensionEntryx));
         }
      }

      this.summonDimensionList = ArrayUtils.toPrimitive(dimensionEntries.toArray(new Integer[dimensionEntries.size()]));
      this.summonDimensionListWhitelist = config.getBool(
         "Pets",
         "Summoning Dimensions Whitelist",
         this.summonDimensionListWhitelist,
         "If set to true the summon dimension list acts as a whitelist, otherwise it is a blacklist."
      );
      config.setCategoryComment("Beastiary", "Here you can control all settings related to the player's Beastiary.");
      this.beastiaryKnowledgeMessages = config.getBool(
         "Beastiary",
         "Beastiary Knowledge Messages",
         this.beastiaryKnowledgeMessages,
         "If true, a chat message will be displayed when gaining Beastiary Knowledge."
      );
      this.creatureProximityKnowledge = config.getInt(
         "Beastiary",
         "Creature Proximity Knowledge",
         this.creatureProximityKnowledge,
         "How much knowledge experience standing near a creature gives per second."
      );
      this.creatureKillKnowledge = config.getInt(
         "Beastiary", "Creature Kill Knowledge", this.creatureKillKnowledge, "How much knowledge experience killing a creature gives."
      );
      this.creatureBreedKnowledge = config.getInt(
         "Beastiary", "Creature Breed Knowledge", this.creatureBreedKnowledge, "How much knowledge experience breeding a creature gives."
      );
      this.creatureTreatKnowledge = config.getInt(
         "Beastiary", "Creature Treat Knowledge", this.creatureTreatKnowledge, "How much knowledge experience feeding a treat to a creature gives."
      );
      this.creatureStudyKnowledge = config.getInt(
         "Beastiary",
         "Creature Study Knowledge",
         this.creatureStudyKnowledge,
         "How much knowledge experience studying (using a Soulgazer on) a creature gives."
      );
      this.creatureStudyCooldown = config.getInt(
         "Beastiary",
         "Creature Study Cooldown",
         this.creatureStudyCooldown,
         "The time in ticks it takes to be able to use a Soulgazer for knowledge again. Default is 200 (10 seconds)."
      );
      this.creatureVariantKnowledgeScale = config.getDouble(
         "Beastiary", "Creature Variant Knowledge Scale", this.creatureVariantKnowledgeScale, "The knowledge experience scale for variant creatures."
      );
      this.creatureBossKnowledgeScale = config.getDouble(
         "Beastiary", "Creature Boss Knowledge Scale", this.creatureBossKnowledgeScale, "The knowledge experience scale for boss creatures."
      );
      config.setCategoryComment("Bosses", "Here you can control all settings related to boss creatures, this does not include rare subspecies (mini bosses).");
      this.bossAntiFlight = config.getDouble(
         "Bosses", "How much higher players must be relative to a boss' y position (feet) to trigger anti flight measures.", this.bossAntiFlight
      );
      this.bossDamageCap = config.getInt(
         "Bosses",
         "Boss Damage Cap",
         this.bossDamageCap,
         "Caps how much damage a boss can take per tick, this also affects Rare Variants and Dungeon Bosses. Set to 0 to disable the cap."
      );
      BaseCreatureEntity.BOSS_DAMAGE_LIMIT = this.bossDamageCap;
      config.setCategoryComment("Mob Interaction", "Here you can control how mobs interact with other mobs.");
      this.predatorsAttackAnimals = config.getBool(
         "Mob Interaction",
         "Predators Attack Animals",
         this.predatorsAttackAnimals,
         "Set to false to prevent predator mobs from attacking animals/farmable mobs."
      );
      this.mobsAttackVillagers = config.getBool(
         "Mob Interaction",
         "Mobs Attack Villagers",
         this.mobsAttackVillagers,
         "Set to false to prevent mobs that attack players from also attacking villagers."
      );
      this.animalsFightBack = config.getBool(
         "Mob Interaction", "Animals Fight Back", this.animalsFightBack, "If true, passive mobs will fight back when hit instead of running away."
      );
      this.elementalFusion = config.getBool(
         "Mob Interaction",
         "Elemental Fusion",
         this.elementalFusion,
         "If true, some elemental mobs will fuse with each other on sight into a stronger different elemental."
      );
      this.elementalFusionLevelMix = config.getString(
         "Mob Interaction",
         "Elemental Fusion",
         this.elementalFusionLevelMix,
         "Controls how fused mobs combine their levels. Can be 'both' (default) where both levels are added together, 'highest' where the higher level is used and 'lowest' where the lowest level is used."
      );
      this.elementalFusionLevelMultiplier = config.getDouble(
         "Mob Interaction",
         "Elemental Fusion",
         this.elementalFusionLevelMultiplier,
         "The level of a mob created via fusion is multiplied by this value, set to 1 for no changes. Tamed fusions aren't multiplied by this value."
      );
      this.disablePickupOffsets = config.getBool(
         "Mob Interaction",
         "Disable Pickup Offset",
         this.disablePickupOffsets,
         "If true, when a mob picks up a player, the player will be positioned where the mob is rather than offset to where the mob is holding the player at."
      );
      this.suffocationImmunity = config.getBool(
         "Mob Interaction", "Global Suffocation Immunity", this.suffocationImmunity, "If true, all mobs will be immune to suffocation (inWall) damage."
      );
      this.drownImmunity = config.getBool(
         "Mob Interaction", "Global Drown Immunity", this.drownImmunity, "If true, all mobs will be immune to damage from running out of air (drown damage)."
      );
      this.packTreatLuring = config.getBool(
         "Mob Interaction", "Pack Treat Luring", this.packTreatLuring, "If true, mobs can be lured with treats even if they are in a pack."
      );
      config.setCategoryComment(
         "Mob Variations",
         "Settings for how mobs randomly vary such as subspecies. Subspecies are uncommon and rare variants of regular mobs, uncommon subspecies tend to be a bit tougher and rare subspecies are quite powerful and can be considered as mini bosses.."
      );
      this.variantSpawn = config.getBool(
         "Mob Variations",
         "Variants Can Spawn",
         this.variantSpawn,
         "Set to false to prevent variants from spawning, this will not affect mobs that have already spawned as variants."
      );
      this.randomSizes = config.getBool(
         "Mob Variations",
         "Random Sizes",
         this.randomSizes,
         "Set to false to prevent mobs from having a random size variation when spawning, this will not affect mobs that have already spawned."
      );
      this.randomSizeMin = config.getDouble("Mob Variations", "Random Size Min", this.randomSizeMin, "The minimum size scale mobs can randomly spawn at.");
      this.randomSizeMax = config.getDouble("Mob Variations", "Random Size Max", this.randomSizeMax, "The maximum size scale mobs can randomly spawn at.");
      Variant.loadGlobalSettings(config);
      config.setCategoryComment(
         "Custom Item Drops",
         "Here you can add a global list of item drops to add to every mob from Lycanites Mobs. Format is: mod:item,metadata,chance,min,max Multiple drops should be semicolon separated and chances are in decimal format. You can also add an additional comma and then a subspecies ID to restrict that drop to a certain subspecies like so: mod:item,metadata,chance,min,max,subspecies. minecraft:wool,2,0.25,0,3 is Green Wool with a 25% drop rate and will drop 0 to 3 blocks. Be sure to use a colon for mod:item and commas for everything else in an entry. Semicolons can be used to separate multiple entries."
      );
      this.globalDropsString = config.getString("Default Item Drops", "Global Drops", this.globalDropsString, "");
   }

   public List<ItemDrop> getGlobalDrops() {
      if (this.globalDrops == null) {
         this.globalDrops = new ArrayList<>();
         if (this.globalDropsString != null && this.globalDropsString.length() > 0) {
            for (String customDropEntryString : this.globalDropsString.replace(" ", "").split(";")) {
               ItemDrop itemDrop = ItemDrop.createFromConfigString(customDropEntryString);
               if (itemDrop != null) {
                  this.globalDrops.add(itemDrop);
               }
            }
         }
      }

      return this.globalDrops;
   }

   public boolean isSoulboundAllowed(Level world) {
      if (this.soulboundDimensionList.length > 0) {
         boolean inDimensionList = false;

         for (int dimensionId : this.soulboundDimensionList) {
            if (dimensionId == LycDim.id(world)) {
               inDimensionList = true;
               break;
            }
         }

         if (inDimensionList && !this.soulboundDimensionListWhitelist) {
            return false;
         }

         if (!inDimensionList && this.soulboundDimensionListWhitelist) {
            return false;
         }
      }

      return true;
   }

   public boolean isSummoningAllowed(Level world) {
      if (this.summonDimensionList.length > 0) {
         boolean inDimensionList = false;

         for (int dimensionId : this.summonDimensionList) {
            if (dimensionId == LycDim.id(world)) {
               inDimensionList = true;
               break;
            }
         }

         if (inDimensionList && !this.summonDimensionListWhitelist) {
            return false;
         }

         if (!inDimensionList && this.summonDimensionListWhitelist) {
            return false;
         }
      }

      return true;
   }
}
