package com.lycanitesmobs.core;

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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;


public class VersionChecker {
   public static VersionChecker INSTANCE = new VersionChecker();
   public boolean enabled = true;
   protected VersionChecker.VersionInfo currentVersion = new VersionChecker.VersionInfo("2.0.8.9", "1.12.2");
   protected VersionChecker.VersionInfo latestVersion = null;
   protected long lastChecked = -1L;
   public SSLContext sslContext;

   public VersionChecker() {
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

   public VersionChecker.VersionInfo getLatestVersion() {
      if (this.latestVersion != null) {
         return this.latestVersion;
      } else {
         long currentTime = System.currentTimeMillis() / 1000L;
         if (this.lastChecked < 0L || currentTime - this.lastChecked > 3600L) {
            this.lastChecked = currentTime;
            VersionChecker.VersionLoader versionLoader = new VersionChecker.VersionLoader();
            Thread thread = new Thread(versionLoader);
            thread.start();
         }

         return this.latestVersion != null ? this.latestVersion : this.currentVersion;
      }
   }

   public static class VersionInfo {
      public String versionNumber;
      public String mcVersion;
      public String name = "";
      public String newFeatures = "";
      public String configChanges = "";
      public String majorFixes = "";
      public String changes = "";
      public String balancing = "";
      public String minorFixes = "";
      public boolean isNewer = false;

      public VersionInfo(String versionNumber, String mcVersion) {
         this.versionNumber = versionNumber;
         this.mcVersion = mcVersion;
      }

      public void loadFromJSON(JsonObject versionJson) {
         this.versionNumber = versionJson.get("version").getAsString();
         this.mcVersion = versionJson.get("mcversion").getAsString();
         this.name = versionJson.get("name").getAsString();
         this.newFeatures = versionJson.get("new").getAsString();
         this.configChanges = versionJson.get("config_changes").getAsString();
         this.majorFixes = versionJson.get("major_fixes").getAsString();
         this.changes = versionJson.get("changes").getAsString();
         this.balancing = versionJson.get("balancing").getAsString();
         this.minorFixes = versionJson.get("minor_fixes").getAsString();
      }

      public void checkIfNewer(VersionChecker.VersionInfo compareVersion) {
         this.isNewer = false;
         String[] versions = this.versionNumber.split("\\.");
         String[] compareVersions = compareVersion.versionNumber.split("\\.");

         for (int i = 0; i < 4; i++) {
            int versionNumber = NumberUtils.isCreatable(versions[i].replaceAll("[^\\d.]", "")) ? Integer.parseInt(versions[i].replaceAll("[^\\d.]", "")) : 0;
            int compareVersionNumber = NumberUtils.isCreatable(compareVersions[i].replaceAll("[^\\d.]", ""))
               ? Integer.parseInt(compareVersions[i].replaceAll("[^\\d.]", ""))
               : 0;
            if (versionNumber > compareVersionNumber) {
               this.isNewer = true;
               return;
            }

            if (versionNumber != compareVersionNumber) {
               return;
            }
         }
      }
   }

   public static class VersionLoader implements Runnable {
      @Override
      public void run() {
         VersionChecker.INSTANCE.lastChecked = System.currentTimeMillis() / 1000L;

         try {
            URL url = new URL("https://service.lycanitesmobs.com/api/v1/versions?limit=1&sort_by=created_at:desc&mcversion=1.12.2");
            HttpsURLConnection urlConnection = (HttpsURLConnection)url.openConnection();
            urlConnection.setSSLSocketFactory(VersionChecker.INSTANCE.sslContext.getSocketFactory());
            urlConnection.setRequestProperty("Authorization", "Bearer 7ed1f44cbc1aff693e604075f23d56402983a4a0");
            String osName = System.getProperty("os.name");
            urlConnection.setRequestProperty("User-Agent", "Minecraft 1.12.2 (" + osName + ") LycanitesMobs " + "2.0.8.9");
            InputStream inputStream = urlConnection.getInputStream();

            String jsonString;
            try {
               jsonString = IOUtils.toString(inputStream, (Charset)null);
               jsonString = jsonString.replace("\\r", "");
            } finally {
               inputStream.close();
            }

            JsonParser jsonParser = new JsonParser();
            JsonObject json = jsonParser.parse(jsonString).getAsJsonObject();
            JsonArray jsonArray = json.getAsJsonArray("data");
            JsonObject versionJson = jsonArray.get(0).getAsJsonObject();
            if (!versionJson.has("version") && !versionJson.has("mcversion")) {
               return;
            }

            String versionNumber = versionJson.get("version").getAsString();
            String mcVersion = versionJson.get("mcversion").getAsString();
            VersionChecker.INSTANCE.latestVersion = new VersionChecker.VersionInfo(versionNumber, mcVersion);
            VersionChecker.INSTANCE.latestVersion.loadFromJSON(versionJson);
            VersionChecker.INSTANCE.latestVersion.checkIfNewer(VersionChecker.INSTANCE.currentVersion);
         } catch (Throwable var15) {
            var15.printStackTrace();
         }
      }
   }
}
