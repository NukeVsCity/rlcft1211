package net.ilexiconn.llibrary.server.entity;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import net.ilexiconn.llibrary.LLibrary;
import net.ilexiconn.llibrary.server.capability.EntityDataHandler;
import net.ilexiconn.llibrary.server.capability.IEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * LLibrary 1.7.20 EntityPropertiesHandler. Register property classes once (IaF does it in preInit; any time before the
 * first entity is created works), read them with {@code getProperties(entity, Class)}. Tracked properties
 * (getTrackingTime() >= 0) are sent to tracking players like 1.12 (see ServerEventHandler).
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public enum EntityPropertiesHandler {
   INSTANCE;

   private final Map<Class<? extends EntityProperties>, String> propertiesIDMap = new HashMap<>();
   // LinkedHashMap: 1.12 used a HashMap keyed by Class (identity hash order); the order only decides the order of the
   // properties inside one entity's data, which nothing depends on.
   private final Map<Class<? extends Entity>, List<Class<? extends EntityProperties<?>>>> registeredProperties = new LinkedHashMap<>();
   private final Map<Class<? extends Entity>, List<String>> entityPropertiesCache = new HashMap<>();
   private final Map<ServerPlayer, List<PropertiesTracker<?>>> trackerMap = new IdentityHashMap<>();

   public <E extends Entity, T extends EntityProperties<E>> void registerProperties(Class<T> propertiesClass) {
      T properties;
      try {
         Constructor<T> constructor = propertiesClass.getConstructor();
         properties = constructor.newInstance();
      } catch (Exception e) {
         LLibrary.LOGGER.fatal("Failed to register entity properties", e);
         return;
      }

      if (this.propertiesIDMap.containsValue(properties.getID())) {
         LLibrary.LOGGER.fatal("Duplicate entity properties with ID {}", properties.getID());
      } else {
         this.propertiesIDMap.put(propertiesClass, properties.getID());
         Class<E> entityClass = properties.getEntityClass();
         List<Class<? extends EntityProperties<?>>> list = this.registeredProperties.computeIfAbsent(entityClass, k -> new ArrayList<>());
         list.add((Class)propertiesClass);
      }
   }

   public <T extends EntityProperties<?>> T getProperties(Entity entity, Class<T> propertiesClass) {
      if (entity != null) {
         String identifier = this.propertiesIDMap.get(propertiesClass);
         if (identifier != null) {
            return (T)EntityDataHandler.INSTANCE.getEntityData(entity, identifier);
         }
      }
      return null;
   }

   /** 1.12 ServerEventHandler.onCollectEntityData: new instances of every property class registered for a superclass of the entity. */
   public List<IEntityData> createProperties(Entity entity) {
      boolean cached = this.hasEntityInCache(entity.getClass());
      List<String> entityPropertiesIDCache = !cached ? new ArrayList<>() : null;
      List<IEntityData> collected = new ArrayList<>();
      this.getRegisteredProperties().filter(propEntry -> propEntry.getKey().isAssignableFrom(entity.getClass())).forEach(propEntry -> {
         for (Class<? extends EntityProperties> propClass : propEntry.getValue()) {
            try {
               Constructor<? extends EntityProperties> constructor = propClass.getConstructor();
               EntityProperties prop = constructor.newInstance();
               collected.add(prop);
               if (!cached) {
                  entityPropertiesIDCache.add(prop.getID());
               }
            } catch (Exception e) {
               LLibrary.LOGGER.error("Failed to create entity properties {}", propClass, e);
            }
         }
      });
      if (!cached) {
         this.addEntityToCache(entity.getClass(), entityPropertiesIDCache);
      }
      return collected;
   }

   private boolean hasAnyProperties(Class<? extends Entity> entityClass) {
      return this.registeredProperties.keySet().stream().anyMatch(c -> c.isAssignableFrom(entityClass));
   }

   public <T extends Entity> void addTracker(ServerPlayer player, T entity) {
      if (!this.hasEntityInCache(entity.getClass())) {
         if (!this.hasAnyProperties(entity.getClass())) {
            return;
         }
         // 1.12 created the data (and the cache entry) when the entity was constructed; here it is created on first access
         EntityDataHandler.INSTANCE.getCapability(entity);
      }
      List<String> entityProperties = this.entityPropertiesCache.get(entity.getClass());
      if (entityProperties != null) {
         List<PropertiesTracker<?>> trackerList = this.getEntityTrackers(player);

         for (String propID : entityProperties) {
            IEntityData extendedProperties = EntityDataHandler.INSTANCE.getEntityData(entity, propID);
            if (extendedProperties instanceof EntityProperties properties) {
               if (properties.getTrackingTime() >= 0) {
                  PropertiesTracker<T> tracker = properties.createTracker(entity);
                  tracker.setReady();
                  trackerList.add(tracker);
               }
            }
         }
      }
   }

   public void removeTracker(ServerPlayer player, Entity entity) {
      List<PropertiesTracker<?>> trackerList = this.getEntityTrackers(player);
      if (trackerList != null && !trackerList.isEmpty()) {
         Iterator<PropertiesTracker<?>> iterator = trackerList.iterator();

         while (iterator.hasNext()) {
            PropertiesTracker<?> tracker = iterator.next();
            if (tracker.getEntity().equals(entity)) {
               iterator.remove();
               tracker.removeTracker();
            }
         }
      }
   }

   public boolean hasEntityInCache(Class<? extends Entity> entityClass) {
      return this.entityPropertiesCache.containsKey(entityClass);
   }

   public void addEntityToCache(Class<? extends Entity> entityClass, List<String> propertyIDs) {
      this.entityPropertiesCache.put(entityClass, propertyIDs);
   }

   public Stream<Entry<Class<? extends Entity>, List<Class<? extends EntityProperties<?>>>>> getRegisteredProperties() {
      return this.registeredProperties.entrySet().stream();
   }

   public List<PropertiesTracker<?>> getEntityTrackers(ServerPlayer player) {
      return this.trackerMap.computeIfAbsent(player, p -> new ArrayList<>());
   }

   public Iterator<Entry<ServerPlayer, List<PropertiesTracker<?>>>> getTrackerIterator() {
      return this.trackerMap.entrySet().iterator();
   }

   @SubscribeEvent
   public void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         this.trackerMap.remove(player);
      }
   }

   @SubscribeEvent
   public void onPlayerClone(PlayerEvent.Clone event) {
      if (event.getOriginal() instanceof ServerPlayer player) {
         this.trackerMap.remove(player);
      }
   }
}
