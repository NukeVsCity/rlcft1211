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
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.config.ConfigBase;
import com.lycanitesmobs.core.info.altar.AltarInfoAmalgalich;
import com.lycanitesmobs.core.info.altar.AltarInfoAsmodeus;
import com.lycanitesmobs.core.info.altar.AltarInfoCelestialGeonach;
import com.lycanitesmobs.core.info.altar.AltarInfoCrimsonEpion;
import com.lycanitesmobs.core.info.altar.AltarInfoEbonCacodemon;
import com.lycanitesmobs.core.info.altar.AltarInfoLunarGrue;
import com.lycanitesmobs.core.info.altar.AltarInfoMottleAbaia;
import com.lycanitesmobs.core.info.altar.AltarInfoPhosphorescentChupacabra;
import com.lycanitesmobs.core.info.altar.AltarInfoRahovart;
import com.lycanitesmobs.core.info.altar.AltarInfoRoyalArchvile;
import com.lycanitesmobs.core.info.altar.AltarInfoUmberLobber;
import com.lycanitesmobs.core.mobevent.effects.StructureBuilder;
import com.lycanitesmobs.core.mobevent.trigger.AltarMobEventTrigger;
import com.lycanitesmobs.core.worldgen.mobevents.AmalgalichStructureBuilder;
import com.lycanitesmobs.core.worldgen.mobevents.AsmodeusStructureBuilder;
import com.lycanitesmobs.core.worldgen.mobevents.RahovartStructureBuilder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class AltarInfo {
   public static boolean altarsEnabled = true;
   public static boolean checkDimensions = false;
   public static Map<String, Double> rareSubspeciesMutlipliers = new HashMap<>();
   public static Map<String, AltarInfo> altars = new HashMap<>();
   public String name = "Altar";
   public AltarMobEventTrigger mobEventTrigger;

   public static void loadGlobalSettings() {
      ConfigBase config = ConfigBase.getConfig(LycanitesMobs.modInfo, "general");
      config.setCategoryComment(
         "Altars",
         "Altars are block arrangements that can be activated using Soulkeys to summon rare subspecies as mini bosses or trigger events including boss events."
      );
      altarsEnabled = config.getBool(
         "Altars", "Altars Enabled", altarsEnabled, "Set to false to disable altars, Soulkeys can still be crafted but wont work on Altars."
      );
      checkDimensions = config.getBool(
         "Altars",
         "Check Dimension",
         checkDimensions,
         "If set to true, Altars will only activate in dimensions that the monster spawned or event started is allowed in."
      );
      String[] statNames = new String[]{"Health", "Defense", "Speed", "Damage", "Haste", "Effect", "Pierce"};
      rareSubspeciesMutlipliers = new HashMap<>();

      for (String statName : statNames) {
         double defaultValue = 4.0;
         if ("Health".equalsIgnoreCase(statName)) {
            defaultValue = 10.0;
         }

         if ("Speed".equalsIgnoreCase(statName)) {
            defaultValue = 1.5;
         }

         rareSubspeciesMutlipliers.put(statName.toUpperCase(Locale.ENGLISH), config.getDouble("Altars", statName + " Altar Stat Multiplier", defaultValue));
      }
   }

   public static boolean checkAltarsEnabled() {
      return altars.size() < 1 ? false : altarsEnabled;
   }

   public static void addAltar(AltarInfo altarInfo) {
      altars.put(altarInfo.name.toLowerCase(), altarInfo);
   }

   public static AltarInfo getAltar(String name) {
      return altars.containsKey(name.toLowerCase()) ? altars.get(name.toLowerCase()) : null;
   }

   public static void createAltars() {
      AltarInfo ebonCacodemonAltar = new AltarInfoEbonCacodemon("EbonCacodemonAltar");
      addAltar(ebonCacodemonAltar);
      AltarInfo rahovartAltar = new AltarInfoRahovart("RahovartAltar");
      addAltar(rahovartAltar);
      StructureBuilder.addStructureBuilder(new RahovartStructureBuilder());
      AltarInfo asmodeusAltar = new AltarInfoAsmodeus("AsmodeusAltar");
      addAltar(asmodeusAltar);
      StructureBuilder.addStructureBuilder(new AsmodeusStructureBuilder());
      AltarInfo amalgalichAltar = new AltarInfoAmalgalich("AmalgalichAltar");
      addAltar(amalgalichAltar);
      StructureBuilder.addStructureBuilder(new AmalgalichStructureBuilder());
      AltarInfo umberLobberAltar = new AltarInfoUmberLobber("UmberLobberAltar");
      addAltar(umberLobberAltar);
      AltarInfo celestialGeonachAltar = new AltarInfoCelestialGeonach("CelestialGeonachAltar");
      addAltar(celestialGeonachAltar);
      AltarInfo lunarGrueAltar = new AltarInfoLunarGrue("LunarGrueAltar");
      addAltar(lunarGrueAltar);
      AltarInfo mottledAbaiaAltar = new AltarInfoMottleAbaia("MottledAbaia");
      addAltar(mottledAbaiaAltar);
      AltarInfo crimsonEpionAltar = new AltarInfoCrimsonEpion("CrimsonEpion");
      addAltar(crimsonEpionAltar);
      AltarInfo royalArchvileAltar = new AltarInfoRoyalArchvile("RoyalArchvile");
      addAltar(royalArchvileAltar);
      AltarInfo phosphorescentChupacabraAltar = new AltarInfoPhosphorescentChupacabra("PhosphorescentChupacabra");
      addAltar(phosphorescentChupacabraAltar);
   }

   public AltarInfo(String name) {
      this.name = name;
   }

   public boolean checkBlockEvent(Entity entity, Level world, BlockPos pos) {
      return true;
   }

   public boolean quickCheck(Entity entity, Level world, BlockPos pos) {
      return false;
   }

   public boolean fullCheck(Entity entity, Level world, BlockPos pos) {
      return false;
   }

   public boolean activate(Entity entity, Level world, BlockPos pos, int variant) {
      return this.mobEventTrigger == null ? false : this.mobEventTrigger.onActivate(entity, world, pos, variant);
   }

   public BlockPos getFacingPosition(BlockPos pos, double distance, double angle) {
      double xAmount = -Math.sin(Math.toRadians(angle));
      double zAmount = Math.cos(Math.toRadians(angle));
      return LycPos.of(
         (double)Math.round((double)pos.getX() + distance * xAmount),
         (double)pos.getY(),
         (double)Math.round((double)pos.getZ() + distance * zAmount)
      );
   }
}
