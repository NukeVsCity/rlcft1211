package com.lycanitesmobs.core.pets;

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
import com.google.gson.JsonParser;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.Variant;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.io.IOUtils;


public class PlayerFamiliars {
   public static PlayerFamiliars INSTANCE = new PlayerFamiliars();
   public Map<UUID, Map<UUID, PetEntry>> playerFamiliars = new HashMap<>();
   public Map<UUID, Long> playerFamiliarLoadedTimes = new HashMap<>();
   public List<String> familiarBlacklist = new ArrayList<>();
   public SSLContext sslContext;

   public PlayerFamiliars() {
      TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
         @Override
         public X509Certificate[] getAcceptedIssuers() {
            return null;
         }

         @Override
         public void checkClientTrusted(X509Certificate[] certs, String authType) {
         }

         @Override
         public void checkServerTrusted(X509Certificate[] certs, String authType) {
         }
      }};

      try {
         this.sslContext = SSLContext.getInstance("SSL");
         this.sslContext.init(null, trustAllCerts, new SecureRandom());
      } catch (Exception var3) {
         var3.printStackTrace();
      }
   }

   public void parseFamiliarJSON(String jsonString) {
      try {
         JsonParser jsonParser = new JsonParser();
         JsonObject json = jsonParser.parse(jsonString).getAsJsonObject();
         JsonArray jsonArray = json.getAsJsonArray("data");
         Iterator<JsonElement> jsonIterator = jsonArray.iterator();

         while (jsonIterator.hasNext()) {
            try {
               JsonObject familiarJson = jsonIterator.next().getAsJsonObject();
               UUID minecraft_uuid = UUID.fromString(familiarJson.get("minecraft_uuid").getAsString());
               if (!this.familiarBlacklist.contains(minecraft_uuid.toString())) {
                  UUID familiar_uuid = UUID.fromString(familiarJson.get("familiar_uuid").getAsString());
                  String familiar_species = familiarJson.get("familiar_species").getAsString();
                  CreatureInfo creatureInfo = CreatureManager.getInstance().getCreature(familiar_species);
                  int familiar_subspecies = familiarJson.get("familiar_subspecies").getAsInt();
                  familiar_species = this.handleLegacySpecies(familiar_species);
                  int familiar_variant = familiarJson.get("familiar_variant").getAsInt();
                  Variant creatureVariant = creatureInfo != null ? creatureInfo.getSubspecies(familiar_subspecies).getVariant(familiar_variant) : null;
                  String familiar_name = familiarJson.get("familiar_name").getAsString();
                  String familiar_color = familiarJson.get("familiar_color").getAsString();
                  double familiar_size = familiarJson.get("familiar_size").getAsDouble();
                  if (familiar_size <= 0.0) {
                     familiar_size = 0.5;
                     if (creatureVariant != null && creatureVariant.scale != 1.0) {
                        familiar_size *= 1.0 / creatureVariant.scale;
                     }
                  }

                  PetEntryFamiliar familiarEntry = new PetEntryFamiliar(familiar_uuid, null, familiar_species.toLowerCase());
                  familiarEntry.setEntitySubspecies(familiar_subspecies);
                  familiarEntry.setEntityVariant(familiar_variant);
                  familiarEntry.setEntitySize(familiar_size);
                  if (!"".equals(familiar_name)) {
                     familiarEntry.setEntityName(familiar_name);
                  }

                  familiarEntry.setColor(familiar_color);
                  if (!this.playerFamiliars.containsKey(minecraft_uuid)) {
                     this.playerFamiliars.put(minecraft_uuid, new HashMap<>());
                  }

                  if (!this.playerFamiliars.get(minecraft_uuid).containsKey(familiar_uuid)) {
                     this.playerFamiliars.get(minecraft_uuid).put(familiar_uuid, familiarEntry);
                  } else {
                     PetEntry existingEntry = this.playerFamiliars.get(minecraft_uuid).get(familiar_uuid);
                     existingEntry.copy(familiarEntry);
                  }
               }
            } catch (Exception var20) {
            }
         }
      } catch (Exception var21) {
      }
   }

   public Map<UUID, PetEntry> getFamiliarsForPlayer(Player player) {
      long currentTime = System.currentTimeMillis() / 1000L;
      long loadedTime = this.getPlayerFamiliarLoadedTime(player);
      if (loadedTime < 0L || currentTime - loadedTime > 1800L) {
         this.updatePlayerFamiliarLoadedTime(player);
         PlayerFamiliars.FamiliarLoader familiarLoader = new PlayerFamiliars.FamiliarLoader(player);
         Thread thread = new Thread(familiarLoader);
         thread.start();
      }

      Map<UUID, PetEntry> playerFamiliarEntries = new HashMap<>();
      if (this.playerFamiliars.containsKey(player.getUUID())) {
         playerFamiliarEntries = this.playerFamiliars.get(player.getUUID());

         for (PetEntry familiarEntry : playerFamiliarEntries.values()) {
            if (familiarEntry.host == null) {
               familiarEntry.host = player;
            }
         }
      }

      return playerFamiliarEntries;
   }

   public long getPlayerFamiliarLoadedTime(Player player) {
      UUID uuid = player.getUUID();
      return !this.playerFamiliarLoadedTimes.containsKey(uuid) ? -1L : this.playerFamiliarLoadedTimes.get(uuid);
   }

   public void updatePlayerFamiliarLoadedTime(Player player) {
      this.playerFamiliarLoadedTimes.put(player.getUUID(), System.currentTimeMillis() / 1000L);
   }

   public String handleLegacySpecies(String species) {
      if (species.equalsIgnoreCase("apollyon")) {
         return "archvile";
      } else if (species.equalsIgnoreCase("belphegor")) {
         return "belph";
      } else if (species.equalsIgnoreCase("behemophet")) {
         return "behemoth";
      } else if (species.equalsIgnoreCase("malwrath")) {
         return "cacodemon";
      } else if (species.equalsIgnoreCase("naxiris")) {
         return "beholder";
      } else if (species.equalsIgnoreCase("zephyr")) {
         return "djinn";
      } else if (species.equalsIgnoreCase("raidra")) {
         return "zephyr";
      } else if (species.equalsIgnoreCase("ningen")) {
         return "dweller";
      } else if (species.equalsIgnoreCase("cherufe")) {
         return "lobber";
      } else if (species.equalsIgnoreCase("ostimien")) {
         return "lurker";
      } else if (species.equalsIgnoreCase("kathoga")) {
         return "pinky";
      } else if (species.equalsIgnoreCase("brucha")) {
         return "quillbeast";
      } else if (species.equalsIgnoreCase("stryder")) {
         return "strider";
      } else if (species.equalsIgnoreCase("lycosa")) {
         return "tarantula";
      } else {
         return species.equalsIgnoreCase("jouste") ? "joust" : species;
      }
   }

   public static class FamiliarLoader implements Runnable {
      Player player;

      public FamiliarLoader(Player player) {
         this.player = player;
      }

      @Override
      public void run() {
         this.getForPlayerUUID(this.player.getUUID());
         ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(this.player);
         if (extendedPlayer != null) {
            extendedPlayer.loadFamiliars();
         }

         PlayerFamiliars.INSTANCE.updatePlayerFamiliarLoadedTime(this.player);
      }

      private void getForPlayerUUID(UUID uuid) {
         String jsonString;
         try {
            URL url = new URL("https://service.lycanitesmobs.com/api/v1/familiars?minecraft_uuid=" + uuid.toString());
            HttpsURLConnection urlConnection = (HttpsURLConnection)url.openConnection();
            urlConnection.setSSLSocketFactory(PlayerFamiliars.INSTANCE.sslContext.getSocketFactory());
            urlConnection.setRequestProperty("Authorization", "Bearer 7ed1f44cbc1aff693e604075f23d56402983a4a0");
            String osName = System.getProperty("os.name");
            urlConnection.setRequestProperty("User-Agent", "Minecraft 1.12.2 (" + osName + ") LycanitesMobs " + "2.0.8.9");
            InputStream inputStream = urlConnection.getInputStream();

            try {
               jsonString = IOUtils.toString(inputStream, (Charset)null);
            } catch (Exception var12) {
               throw var12;
            } finally {
               inputStream.close();
            }

            LycanitesMobs.logInfo("", "Online familiars loaded successfully for " + uuid + ".");
         } catch (Throwable var14) {
            LycanitesMobs.logInfo("", "Unable to access the online familiars service.");
            var14.printStackTrace();
            return;
         }

         PlayerFamiliars.INSTANCE.parseFamiliarJSON(jsonString);
      }
   }
}
