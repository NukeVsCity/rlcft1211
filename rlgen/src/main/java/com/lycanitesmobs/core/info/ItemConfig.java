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
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.config.ConfigBase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ItemConfig {
   public static double seasonalItemDropChance = 0.1F;
   public static boolean removeOnNoFireTick = true;
   public static boolean chargesToXPBottles = true;
   public static int lowEquipmentRepairAmount = 50;
   public static int mediumEquipmentRepairAmount = 100;
   public static int highEquipmentRepairAmount = 500;
   public static List<? extends String> lowEquipmentSharpnessItems = new ArrayList<>();
   public static List<? extends String> mediumEquipmentSharpnessItems = new ArrayList<>();
   public static List<? extends String> highEquipmentSharpnessItems = new ArrayList<>();
   public static List<? extends String> maxEquipmentSharpnessItems = new ArrayList<>();
   public static List<? extends String> lowEquipmentManaItems = new ArrayList<>();
   public static List<? extends String> mediumEquipmentManaItems = new ArrayList<>();
   public static List<? extends String> highEquipmentManaItems = new ArrayList<>();
   public static List<? extends String> maxEquipmentManaItems = new ArrayList<>();

   public static void loadGlobalSettings() {
      ConfigBase config = ConfigBase.getConfig(LycanitesMobs.modInfo, "general");
      seasonalItemDropChance = config.getDouble(
         "Seasonal Item Drop Chance",
         "Seasonal",
         seasonalItemDropChance,
         "The chance of seasonal items dropping such as Winter Gifts. Can be 0-1, 0.25 would be 25%. Set to 0 to disable these drops all together."
      );
      config.setCategoryComment("Fire", "Special settings for fire blocks, etc.");
      removeOnNoFireTick = config.getBool(
         "Fire",
         "Remove On No Fire Tick",
         removeOnNoFireTick,
         "If set to false, when the doFireTick gamerule is set to false, instead of removing all custom fire such as Hellfire, the fire simply stops spreading instead, this is useful for decorative fire on adventure maps and servers."
      );
      config.setCategoryComment("Charge", "Settings for Charges");
      chargesToXPBottles = config.getBool(
         "Charge",
         "Enable Charge to XP Bottle Infusion",
         chargesToXPBottles,
         "If true, Charge items can be placed into the Infuser with an Empty Bottle to make XP Bottles."
      );
      config.setCategoryComment("Equipment", "Settings for Crafted Equipment");
      lowEquipmentRepairAmount = config.getInt(
         "Equipment", "Equipment Repair Amount Low", lowEquipmentRepairAmount, "The amount of Sharpness or Mana Low tier Equipment repair items give."
      );
      mediumEquipmentRepairAmount = config.getInt(
         "Equipment", "Equipment Repair Amount Medium", mediumEquipmentRepairAmount, "The amount of Sharpness or Mana Medium tier Equipment repair items give."
      );
      highEquipmentRepairAmount = config.getInt(
         "Equipment", "Equipment Repair Amount High", highEquipmentRepairAmount, "The amount of Sharpness or Mana High tier Equipment repair items give."
      );
      lowEquipmentSharpnessItems = Arrays.asList(
         config.getStringList(
            "Equipment",
            "Equipment Sharpness Items Low",
            new String[]{"minecraft:flint", "minecraft:bone"},
            "A list of ids for items that grant a low amount of Equipment Sharpness."
         )
      );
      mediumEquipmentSharpnessItems = Arrays.asList(
         config.getStringList(
            "Equipment",
            "Equipment Sharpness Items Medium",
            new String[]{"minecraft:iron_ingot", "minecraft:quartz"},
            "A list of ids for items that grant a medium amount of Equipment Sharpness."
         )
      );
      highEquipmentSharpnessItems = Arrays.asList(
         config.getStringList(
            "Equipment",
            "Equipment Sharpness Items High",
            new String[]{"minecraft:diamond", "minecraft:emerald", "minecraft:prismarine_shard"},
            "A list of ids for items that grant a high amount of Equipment Sharpness."
         )
      );
      maxEquipmentSharpnessItems = Arrays.asList(
         config.getStringList(
            "Equipment",
            "Equipment Sharpness Items Max",
            new String[]{"minecraft:prismarine_crystal"},
            "A list of ids for items that grant a max amount of Equipment Sharpness."
         )
      );
      lowEquipmentManaItems = Arrays.asList(
         config.getStringList(
            "Equipment",
            "Equipment Mana Items Low",
            new String[]{"minecraft:redstone", "minecraft:slime_ball"},
            "A list of ids for items that grant a low amount of Equipment Mana."
         )
      );
      mediumEquipmentManaItems = Arrays.asList(
         config.getStringList(
            "Equipment",
            "Equipment Mana Items Medium",
            new String[]{
               "minecraft:lapis_lazuli",
               "minecraft:blaze_powder",
               "minecraft:gunpowder",
               "lycanitesmobs:frostyfur",
               "lycanitesmobs:poisongland",
               "lycanitesmobs:geistliver"
            },
            "A list of ids for items that grant a medium amount of Equipment Mana."
         )
      );
      highEquipmentManaItems = Arrays.asList(
         config.getStringList(
            "Equipment",
            "Equipment Mana Items High",
            new String[]{"minecraft:experience_bottle", "minecraft:magma_cream"},
            "A list of ids for items that grant a high amount of Equipment Mana."
         )
      );
      maxEquipmentManaItems = Arrays.asList(
         config.getStringList(
            "Equipment",
            "Equipment Mana Items Max",
            new String[]{"minecraft:nether_star"},
            "A list of ids for items that grant a max amount of Equipment Mana."
         )
      );
   }
}
