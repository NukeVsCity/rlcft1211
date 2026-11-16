package com.lycanitesmobs.core.info;

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
import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.item.ItemCustomSpawnEgg;
import com.lycanitesmobs.core.item.consumable.CreatureTreatItem;
import com.lycanitesmobs.core.item.equipment.CreatureSaddleItem;
import com.lycanitesmobs.core.item.special.ItemSoulstone;
import com.lycanitesmobs.core.item.special.ItemSoulstoneFilled;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CreatureType {
   protected String name;
   public ModInfo modInfo;
   public Map<String, CreatureInfo> creatures = new HashMap<>();
   public List<CreatureInfo> tameableCreatures = new ArrayList<>();
   protected Item treat;
   public Item saddle;
   protected ItemSoulstone soulstone;
   protected Item spawnEgg;
   public String dietProvided = "none";

   public CreatureType(ModInfo group) {
      this.modInfo = group;
   }

   public void loadFromJSON(JsonObject json) {
      this.name = json.get("name").getAsString();
      if (json.has("dietProvided")) {
         this.dietProvided = json.get("dietProvided").getAsString();
      }
   }

   public void load() {
      this.createItems();
      LycanitesMobs.logDebug("Creature Type", "Loaded Creature Type: " + this.getTitle());
   }

   public String getName() {
      return this.name;
   }

   public String getTitle() {
      return LanguageManager.translate("creaturetype." + this.getName() + ".name");
   }

   public String getTreatName() {
      return this.getName() + "treat";
   }

   public Item getTreatItem() {
      return this.treat;
   }

   public String getSaddleName() {
      return "saddle_" + this.getName();
   }

   public Item getSaddleItem() {
      return this.saddle;
   }

   public String getSoulstoneName() {
      return "soulstone_" + this.getName();
   }

   public ItemSoulstone getSoulstoneItem() {
      return this.soulstone;
   }

   public String getSpawnEggName() {
      return this.getName() + "spawn";
   }

   public void addCreature(CreatureInfo creatureInfo) {
      if (!this.creatures.containsKey(creatureInfo.getName())) {
         this.creatures.put(creatureInfo.getName(), creatureInfo);
         if (creatureInfo.isTameable()) {
            this.tameableCreatures.add(creatureInfo);
         }
      }
   }

   public Item getSpawnEgg() {
      return this.spawnEgg;
   }

   public void createItems() {
      this.spawnEgg = ObjectManager.getItem(this.getSpawnEggName());
      if (this.spawnEgg != null) {
         if (this.spawnEgg instanceof ItemCustomSpawnEgg) {
            ((ItemCustomSpawnEgg)this.spawnEgg).creatureType = this;
         }
      } else {
         this.spawnEgg = new ItemCustomSpawnEgg(this.getSpawnEggName(), this);
         ObjectManager.addItem(this.getSpawnEggName(), this.spawnEgg);
         this.treat = ObjectManager.getItem(this.getTreatName());
         if (this.treat == null) {
            this.treat = new CreatureTreatItem(this);
            ObjectManager.addItem(this.getTreatName(), this.treat);
            this.saddle = ObjectManager.getItem(this.getSaddleName());
            if (this.saddle == null) {
               this.saddle = new CreatureSaddleItem(this);
               ObjectManager.addItem(this.getSaddleName(), this.saddle);
               this.soulstone = (ItemSoulstone)ObjectManager.getItem(this.getSoulstoneName());
               if (this.soulstone == null) {
                  this.soulstone = new ItemSoulstoneFilled(this.modInfo, this);
                  ObjectManager.addItem(this.getSoulstoneName(), this.soulstone);
               }
            }
         }
      }
   }
}
