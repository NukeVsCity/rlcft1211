package com.lycanitesmobs;

import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.CustomProjectileEntity;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * 1.12 EntityEntry registration (CreatureManager.registerEntities, ProjectileManager.registerEntities,
 * ObjectManager.registerSpecialEntities) on 1.21 entity types. 1.12 entities were created from their class with a World
 * constructor; here every entity class keeps that constructor and looks its type up with {@link #type(Class)}.
 * JSON projectiles all use CustomProjectileEntity, one type per projectile like the 1.12 entity entries.
 */
public final class LycEntities {
   public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, LycanitesMobs.modid);
   /** 1.12 BaseCreatureEntity.DEFENSE: RangedAttribute(null, "generic.defense", 4.0, 0.0, 1024.0).setShouldWatch(true) */
   public static final DeferredHolder<Attribute, Attribute> DEFENSE = ATTRIBUTES.register("generic.defense",
      () -> new RangedAttribute("attribute.name.generic.defense", 4.0, 0.0, 1024.0).setSyncable(true));
   /** 1.12 BaseCreatureEntity.RANGED_SPEED: RangedAttribute(null, "generic.rangedSpeed", 4.0, 0.0, 1024.0).setShouldWatch(true) */
   public static final DeferredHolder<Attribute, Attribute> RANGED_SPEED = ATTRIBUTES.register("generic.ranged_speed",
      () -> new RangedAttribute("attribute.name.generic.rangedSpeed", 4.0, 0.0, 1024.0).setSyncable(true));

   private static final Map<Class<?>, EntityType<?>> CLASS_TYPES = new HashMap<>();
   private static final Map<String, EntityType<?>> NAME_TYPES = new HashMap<>();
   private static final Map<EntityType<?>, ProjectileInfo> PROJECTILE_TYPES = new HashMap<>();

   private LycEntities() {
   }

   public static void init(IEventBus modBus) {
      ATTRIBUTES.register(modBus);
      modBus.addListener(LycEntities::onRegister);
      modBus.addListener(LycEntities::onAttributes);
      modBus.addListener(LycEntities::onSpawnPlacements);
   }

   /**
    * 1.12 EntitySpawnPlacementRegistry (Forge default ON_GROUND for unregistered mobs); the 1.12 getCanSpawnHere and
    * isNotColliding checks are the creature's checkSpawnRules and checkSpawnObstruction.
    */
   @SuppressWarnings({"unchecked", "rawtypes"})
   private static void onSpawnPlacements(net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent event) {
      for (CreatureInfo creatureInfo : CreatureManager.getInstance().creatures.values()) {
         EntityType type = creatureInfo.entityClass != null ? CLASS_TYPES.get(creatureInfo.entityClass) : null;
         if (type != null) {
            event.register(type, net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
               net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
               (t, level, reason, pos, random) -> true, net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
         }
      }
   }

   @SuppressWarnings("unchecked")
   public static <T extends Entity> EntityType<T> type(Class<T> cls) {
      EntityType<?> type = CLASS_TYPES.get(cls);
      if (type == null) {
         throw new IllegalStateException("[LycanitesMobs] No entity type registered for " + cls.getName());
      }
      return (EntityType<T>)type;
   }

   public static EntityType<?> type(String name) {
      return NAME_TYPES.get(name.toLowerCase());
   }

   public static EntityType<?> projectileType(ProjectileInfo info) {
      return NAME_TYPES.get(info.getName().toLowerCase());
   }

   /** The type of a JSON projectile, or the class's type when it has none. */
   public static EntityType<?> projectileTypeFor(ProjectileInfo info, Class<? extends Entity> cls) {
      EntityType<?> type = info == null ? null : projectileType(info);
      return type != null ? type : type(cls);
   }

   public static ProjectileInfo projectileInfo(EntityType<?> type) {
      return PROJECTILE_TYPES.get(type);
   }

   /** Entity factory calling the 1.12 (World) constructor. */
   private static <T extends Entity> EntityType.EntityFactory<T> worldConstructor(Class<T> cls) {
      try {
         Constructor<T> ctor = cls.getConstructor(Level.class);
         return (type, level) -> {
            try {
               return ctor.newInstance(level);
            } catch (ReflectiveOperationException e) {
               throw new RuntimeException("[LycanitesMobs] Unable to create " + cls.getName(), e);
            }
         };
      } catch (NoSuchMethodException e) {
         throw new RuntimeException("[LycanitesMobs] " + cls.getName() + " has no (World) constructor", e);
      }
   }

   private static <T extends Entity> EntityType<T> register(Registry<EntityType<?>> registry, String name, Class<T> cls,
         EntityType.EntityFactory<T> factory, MobCategory category, float width, float height, int trackingChunks, int updateInterval, boolean velocity) {
      ResourceLocation id = ResourceLocation.fromNamespaceAndPath(LycanitesMobs.modid, name.toLowerCase());
      EntityType.Builder<T> builder = EntityType.Builder.of(factory, category).sized(Math.max(width, 0.1F), Math.max(height, 0.1F))
         .clientTrackingRange(trackingChunks).updateInterval(updateInterval);
      if (!velocity) {
         builder.setShouldReceiveVelocityUpdates(false);
      }
      EntityType<T> type = builder.build(id.toString());
      Registry.register(registry, id, type);
      if (cls != null && !CLASS_TYPES.containsKey(cls)) {
         CLASS_TYPES.put(cls, type);
      }
      NAME_TYPES.put(name.toLowerCase(), type);
      return type;
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   private static void onRegister(RegisterEvent event) {
      LycanitesMobs.preInitOnce();
      if (event.getRegistryKey() != Registries.ENTITY_TYPE) {
         ObjectManager.onRegister(event);
         return;
      }
      Registry<EntityType<?>> registry = (Registry<EntityType<?>>)event.getRegistry();
      // CreatureManager.registerEntities: tracker(isBoss ? 160 : 80, 3, false)
      for (CreatureInfo creatureInfo : CreatureManager.getInstance().creatures.values()) {
         if (creatureInfo.entityClass == null) {
            continue;
         }
         Class cls = creatureInfo.entityClass;
         // 1.12 spawn caps counted IMob mobs as monsters; the others (not EntityAnimal) were counted as nothing, CREATURE is closest
         MobCategory category = net.minecraft.world.entity.monster.Enemy.class.isAssignableFrom(cls) ? MobCategory.MONSTER : MobCategory.CREATURE;
         register(registry, creatureInfo.getName(), cls, worldConstructor(cls), category, (float)creatureInfo.width,
            (float)creatureInfo.height, creatureInfo.isBoss() ? 10 : 5, 3, false);
      }
      // ProjectileManager.registerEntities: tracker(40, 3, true)
      ProjectileManager projectileManager = ProjectileManager.getInstance();
      for (ProjectileInfo projectileInfo : projectileManager.projectiles.values()) {
         EntityType<?> type;
         if (projectileInfo.entityClass == null || projectileInfo.entityClass == CustomProjectileEntity.class) {
            type = register(registry, projectileInfo.getName(), (Class)CustomProjectileEntity.class,
               (EntityType.EntityFactory)(t, level) -> new CustomProjectileEntity((EntityType)t, level), MobCategory.MISC, 0.25F, 0.25F, 3, 3, true);
         } else {
            Class cls = projectileInfo.entityClass;
            type = register(registry, projectileInfo.getName(), cls, worldConstructor(cls), MobCategory.MISC, 0.25F, 0.25F, 3, 3, true);
         }
         PROJECTILE_TYPES.put(type, projectileInfo);
      }
      for (Map.Entry<String, Class<? extends Entity>> e : projectileManager.oldSpriteProjectiles.entrySet()) {
         Class cls = e.getValue();
         register(registry, e.getKey(), cls, worldConstructor(cls), MobCategory.MISC, 0.25F, 0.25F, 3, 3, true);
      }
      for (Map.Entry<String, Class<? extends Entity>> e : projectileManager.oldModelProjectiles.entrySet()) {
         Class cls = e.getValue();
         register(registry, e.getKey(), cls, worldConstructor(cls), MobCategory.MISC, 0.25F, 0.25F, 3, 3, true);
      }
      // ObjectManager.registerSpecialEntities: tracker(80, 3, false)
      for (Map.Entry<String, Class<? extends Entity>> e : ObjectManager.specialEntities.entrySet()) {
         Class cls = e.getValue();
         register(registry, e.getKey(), cls, worldConstructor(cls), MobCategory.MISC, 0.5F, 0.5F, 5, 3, false);
      }
   }

   @SuppressWarnings("unchecked")
   private static void onAttributes(EntityAttributeCreationEvent event) {
      for (CreatureInfo creatureInfo : CreatureManager.getInstance().creatures.values()) {
         EntityType<?> type = creatureInfo.entityClass == null ? null : CLASS_TYPES.get(creatureInfo.entityClass);
         if (type != null) {
            event.put((EntityType<? extends net.minecraft.world.entity.LivingEntity>)type, Mob.createMobAttributes()
               .add(Attributes.ATTACK_DAMAGE)
               .add(Attributes.ATTACK_SPEED)
               .add(Attributes.FLYING_SPEED)
               .add(DEFENSE)
               .add(RANGED_SPEED)
               .build());
         }
      }
   }

   public static ResourceKey<EntityType<?>> key(String name) {
      return ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(LycanitesMobs.modid, name.toLowerCase()));
   }

   public static Holder<Attribute> defense() {
      return DEFENSE;
   }
}
