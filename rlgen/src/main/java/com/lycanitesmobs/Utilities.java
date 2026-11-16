package com.lycanitesmobs;

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
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;


public class Utilities {
   protected static Calendar easterCalendar;

   public static void addDungeonLoot(ItemStack itemStack, int minAmount, int maxAmount, int weight) {
      ConfigBase config = LycanitesMobs.config;
      config.setCategoryComment("Dungeon Loot Enabled", "Here you can enable/disable dungeon loot for various types of dungeons.");
      if (config.getBool(
         "Dungeon Loot Enabled", "Dungeons", true, "These are most dungeons from underground mob spawner dungeons to pyramids, mineshafts and jungle temples."
      )) {
      }
   }

   public static void addStrongholdLoot(ItemStack itemStack, int minAmount, int maxAmount, int weight) {
      ConfigBase config = LycanitesMobs.config;
      if (config.getBool("Dungeon Loot Enabled", "Strongholds", true, "Stronghold dungeons including corridors, libraries and other parts.")) {
      }
   }

   public static void addVillageLoot(ItemStack itemStack, int minAmount, int maxAmount, int weight) {
      ConfigBase config = LycanitesMobs.config;
      if (config.getBool("Dungeon Loot Enabled", "Blacksmiths", true, "These are the chests found in village blacksmiths homes.")) {
      }
   }

   public static HitResult raytrace(
      Level world,
      Vec3 start,
      Vec3 end,
      boolean stopOnLiquid,
      boolean ignoreBlockWithoutBoundingBox,
      float rayWidth,
      @Nullable Predicate<Entity> entityPredicate
   ) {
      HitResult blockResult = raytraceBlocks(world, start, end, stopOnLiquid, ignoreBlockWithoutBoundingBox);
      HitResult entityResult = raytraceEntities(world, start, end, rayWidth, entityPredicate);
      if (blockResult != null && blockResult.getType() != HitResult.Type.MISS) {
         if (entityResult != null && entityResult.getType() != HitResult.Type.MISS) {
            return start.distanceToSqr(blockResult.getLocation()) <= start.distanceToSqr(entityResult.getLocation()) ? blockResult : entityResult;
         } else {
            return blockResult;
         }
      } else {
         return entityResult != null && entityResult.getType() != HitResult.Type.MISS ? entityResult : net.minecraft.world.phys.BlockHitResult.miss(end, null, LycPos.of(end));
      }
   }

   public static HitResult raytraceBlocks(Level world, Vec3 start, Vec3 end, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox) {
      HitResult result = LycWorld.rayTraceBlocks(world, start, end, stopOnLiquid, ignoreBlockWithoutBoundingBox, false);
      return result != null && result.getType() != HitResult.Type.MISS ? result : net.minecraft.world.phys.BlockHitResult.miss(end, null, LycPos.of(end));
   }

   public static <T extends Entity> HitResult raytraceEntities(Level world, Vec3 start, Vec3 end, float rayWidth, @Nullable Predicate<Entity> predicate) {
      AABB aabb = new AABB(
         start.x, start.y, start.z, end.x, end.y, end.z
      );
      if (rayWidth != 0.0F) {
         aabb = aabb.inflate((double)rayWidth);
      }

      List<Entity> possibleEntities = world.getEntitiesOfClass(Entity.class, aabb, predicate != null ? predicate::test : null);
      Entity closestHitEntity = null;
      Vec3 closestHitLocation = null;
      double closestHitDistSqr = Double.POSITIVE_INFINITY;

      for (Entity entity : possibleEntities) {
         AABB entityAabb = entity.getBoundingBox();
         if (rayWidth + entity.getPickRadius() != 0.0F) {
            entityAabb = entityAabb.inflate((double)(rayWidth + entity.getPickRadius()));
         }

         java.util.Optional<Vec3> currentHit = entityAabb.clip(start, end);
         if (currentHit.isPresent()) {
            double currentHitDistSqr = start.distanceToSqr(currentHit.get());
            if (currentHitDistSqr < closestHitDistSqr) {
               closestHitEntity = entity;
               closestHitLocation = currentHit.get();
               closestHitDistSqr = currentHitDistSqr;
            }
         }
      }

      return closestHitEntity != null && closestHitLocation != null
         ? new net.minecraft.world.phys.EntityHitResult(closestHitEntity, closestHitLocation)
         : net.minecraft.world.phys.BlockHitResult.miss(end, null, LycPos.of(end));
   }

   public static Predicate<Entity> collidable() {
      return entity -> net.minecraft.world.entity.EntitySelector.NO_SPECTATORS.test(entity) && entity.isPickable();
   }

   public static Predicate<Entity> collidableExlcuding(Entity entityToIgnore) {
      return entityToIgnore == null ? collidable() : entity -> net.minecraft.world.entity.EntitySelector.NO_SPECTATORS.test(entity) && entity.isPickable() && entity != entityToIgnore;
   }

   public static Predicate<Entity> collidableExlcuding(Entity entityToIgnore1, Entity entityToIgnore2) {
      if (entityToIgnore1 == null) {
         return collidableExlcuding(entityToIgnore2);
      } else {
         return entityToIgnore2 == null
            ? collidableExlcuding(entityToIgnore1)
            : entity -> net.minecraft.world.entity.EntitySelector.NO_SPECTATORS.test(entity) && entity.isPickable() && entity != entityToIgnore1 && entity != entityToIgnore2;
      }
   }

   public static Predicate<Entity> collidableExlcuding(Entity entityToIgnore1, Entity entityToIgnore2, Entity entityToIgnore3) {
      if (entityToIgnore1 == null) {
         return collidableExlcuding(entityToIgnore2, entityToIgnore3);
      } else if (entityToIgnore2 == null) {
         return collidableExlcuding(entityToIgnore1, entityToIgnore3);
      } else {
         return entityToIgnore3 == null
            ? collidableExlcuding(entityToIgnore1, entityToIgnore2)
            : entity -> net.minecraft.world.entity.EntitySelector.NO_SPECTATORS.test(entity)
                  && entity.isPickable()
                  && entity != entityToIgnore1
                  && entity != entityToIgnore2
                  && entity != entityToIgnore3;
      }
   }

   public static Predicate<Entity> collidableExlcuding(Entity... entities) {
      if (entities.length >= 0 && entities.length < 4) {
         if (entities.length == 0) {
            return collidable();
         }

         if (entities.length == 1) {
            return collidableExlcuding(entities[0]);
         }

         if (entities.length == 2) {
            return collidableExlcuding(entities[0], entities[1]);
         }

         if (entities.length == 3) {
            return collidableExlcuding(entities[0], entities[1], entities[2]);
         }
      }

      Set<Entity> entitySet = Arrays.stream(entities).filter(Objects::nonNull).collect(Collectors.toSet());
      return entity -> net.minecraft.world.entity.EntitySelector.NO_SPECTATORS.test(entity) && entity.isPickable() && !entitySet.contains(entity);
   }

   public static boolean isValentines() {
      Calendar calendar = Calendar.getInstance();
      return calendar.get(2) == 1 && calendar.get(5) >= 7 && calendar.get(5) <= 14;
   }

   public static boolean isEaster() {
      Calendar calendar = Calendar.getInstance();
      if (easterCalendar == null) {
         int Y = calendar.get(1);
         int a = Y % 19;
         int b = Y / 100;
         int c = Y % 100;
         int d = b / 4;
         int e = b % 4;
         int f = (b + 8) / 25;
         int g = (b - f + 1) / 3;
         int h = (19 * a + b - d - g + 15) % 30;
         int i = c / 4;
         int k = c % 4;
         int L = (32 + 2 * e + 2 * i - h - k) % 7;
         int m = (a + 11 * h + 22 * L) / 451;
         int easterMonth = (h + L - 7 * m + 114) / 31;
         int easterDay = (h + L - 7 * m + 114) % 31 + 1;
         easterCalendar = new GregorianCalendar(Y, easterMonth, easterDay);
      }

      long daysUntilEaster = ChronoUnit.DAYS.between(calendar.toInstant(), easterCalendar.toInstant());
      return daysUntilEaster <= 7L && daysUntilEaster >= 0L;
   }

   public static boolean isMidsummer() {
      Calendar calendar = Calendar.getInstance();
      return calendar.get(2) == 6 && calendar.get(5) >= 10 && calendar.get(5) <= 20;
   }

   public static boolean isHalloween() {
      Calendar calendar = Calendar.getInstance();
      return calendar.get(5) >= 25 && calendar.get(2) == 9 || calendar.get(5) == 1 && calendar.get(2) == 10;
   }

   public static boolean isYuletide() {
      Calendar calendar = Calendar.getInstance();
      return calendar.get(2) == 11 && calendar.get(5) >= 10 && calendar.get(5) <= 25;
   }

   public static boolean isYuletidePeak() {
      Calendar calendar = Calendar.getInstance();
      return calendar.get(2) == 11 && calendar.get(5) == 25;
   }

   public static boolean isNewYear() {
      Calendar calendar = Calendar.getInstance();
      return calendar.get(2) == 0 && calendar.get(5) == 1;
   }

   public static int daysBetween(Date d1, Date d2) {
      return (int)((d2.getTime() - d1.getTime()) / 86400000L);
   }

   public static Path getAssetPath(Class clazz, String assetDomain, String assetPath) {
      Path path = null;
      String assetDir = "/assets/" + assetDomain + (!"".equals(assetPath) ? "/" + assetPath : "");

      try {
         URL url = clazz.getResource("/assets/" + assetDomain + "/.root");
         URI uri = url.toURI();
         if ("file".equals(uri.getScheme())) {
            path = Paths.get(clazz.getResource(assetDir).toURI());
         } else {
            if (!"jar".equals(uri.getScheme())) {
               LycanitesMobs.logWarning("", "Unsupported file scheme: " + uri.getScheme());
               return null;
            }

            FileSystem filesystem;
            try {
               filesystem = FileSystems.getFileSystem(uri);
            } catch (Exception var9) {
               filesystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
            }

            path = filesystem.getPath(assetDir);
         }
      } catch (Exception var10) {
         LycanitesMobs.logWarning("", "No data found in: " + assetDir);
      }

      return path;
   }

   public static List<ResourceLocation> getPathResourceLocations(Path path, String assetDomain, String fileType) {
      List<ResourceLocation> resourceLocations = new ArrayList<>();

      try {
         Iterator<Path> iterator = Files.walk(path).iterator();

         while (iterator.hasNext()) {
            Path filePath = iterator.next();
            if (fileType == null || fileType.equals(FilenameUtils.getExtension(filePath.toString()))) {
               Path relativePath = path.relativize(filePath);
               String resourceLocationPath = FilenameUtils.removeExtension(relativePath.toString()).replaceAll("\\\\", "/");
               ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(assetDomain, resourceLocationPath);
               resourceLocations.add(resourceLocation);
            }
         }
      } catch (Exception var9) {
         LycanitesMobs.logWarning("", "There was a problem getting ResourceLocations for: " + path + ", " + fileType + ",  \n" + var9.toString());
      }

      return resourceLocations;
   }
}
