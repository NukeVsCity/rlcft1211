package net.ilexiconn.llibrary.server;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import net.ilexiconn.llibrary.LLibrary;
import net.ilexiconn.llibrary.server.capability.EntityDataCapabilityImplementation;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.ilexiconn.llibrary.server.entity.PropertiesTracker;
import net.ilexiconn.llibrary.server.network.PropertiesMessage;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** LLibrary 1.7.20 ServerEventHandler: entity data creation/saving and the EntityProperties tracking sync. */
public enum ServerEventHandler {
   INSTANCE;

   private int updateTimer;

   /** Default value of the llibrary:entity_data attachment (1.12 AttachCapabilitiesEvent + CollectEntityDataEvent). */
   public EntityDataCapabilityImplementation createEntityData(IAttachmentHolder holder) {
      return new EntityDataCapabilityImplementation(EntityPropertiesHandler.INSTANCE.createProperties((Entity)holder));
   }

   /** 1.12 EntityDataCapabilityStorage; entities without properties write nothing. */
   public static final IAttachmentSerializer<CompoundTag, EntityDataCapabilityImplementation> SERIALIZER = new IAttachmentSerializer<>() {
      @Override
      public EntityDataCapabilityImplementation read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
         EntityDataCapabilityImplementation data = INSTANCE.createEntityData(holder);
         data.loadFromNBT(tag);
         return data;
      }

      @Override
      public CompoundTag write(EntityDataCapabilityImplementation attachment, HolderLookup.Provider provider) {
         if (attachment.isEmpty()) {
            return null;
         }
         CompoundTag compound = new CompoundTag();
         attachment.saveToNBT(compound);
         return compound;
      }
   };

   /** 1.12 LivingUpdateEvent for EntityPlayerMP (fired from the player's living update). */
   @SubscribeEvent
   public void onEntityUpdate(PlayerTickEvent.Pre event) {
      if (!event.getEntity().level().isClientSide && event.getEntity() instanceof ServerPlayer player) {
         List<PropertiesTracker<?>> trackers = EntityPropertiesHandler.INSTANCE.getEntityTrackers(player);
         if (trackers != null && trackers.size() > 0) {
            boolean hasPlayer = false;

            for (PropertiesTracker<?> tracker : trackers) {
               if (hasPlayer = tracker.getEntity() == player) {
                  break;
               }
            }

            if (!hasPlayer) {
               EntityPropertiesHandler.INSTANCE.addTracker(player, player);
            }

            for (PropertiesTracker<?> tracker : trackers) {
               tracker.updateTracker();
               if (tracker.isTrackerReady()) {
                  tracker.onSync();
                  PropertiesMessage message = new PropertiesMessage(tracker, tracker.getEntity());
                  LLibrary.NETWORK_WRAPPER.sendTo(message, player);
                  tracker.reset();
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void onJoinWorld(EntityJoinLevelEvent event) {
      if (!event.getLevel().isClientSide && event.getEntity() instanceof ServerPlayer player) {
         EntityPropertiesHandler.INSTANCE.addTracker(player, player);
      }
   }

   @SubscribeEvent
   public void onEntityStartTracking(PlayerEvent.StartTracking event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         EntityPropertiesHandler.INSTANCE.addTracker(player, event.getTarget());
      }
   }

   @SubscribeEvent
   public void onEntityStopTracking(PlayerEvent.StopTracking event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         EntityPropertiesHandler.INSTANCE.removeTracker(player, event.getTarget());
      }
   }

   @SubscribeEvent
   public void onServerTickEvent(ServerTickEvent.Post event) {
      this.updateTimer++;
      if (this.updateTimer > 20) {
         this.updateTimer = 0;
         Iterator<Entry<ServerPlayer, List<PropertiesTracker<?>>>> iterator = EntityPropertiesHandler.INSTANCE.getTrackerIterator();

         while (iterator.hasNext()) {
            Entry<ServerPlayer, List<PropertiesTracker<?>>> trackerEntry = iterator.next();
            ServerPlayer player = trackerEntry.getKey();
            if (player != null && isLoaded(player)) {
               Iterator<PropertiesTracker<?>> it = trackerEntry.getValue().iterator();

               while (it.hasNext()) {
                  PropertiesTracker<?> tracker = it.next();
                  Entity entity = tracker.getEntity();
                  if (entity == null || !isLoaded(entity)) {
                     it.remove();
                     tracker.removeTracker();
                  }
               }
            } else {
               trackerEntry.getValue().forEach(PropertiesTracker::removeTracker);
               iterator.remove();
            }
         }
      }
   }

   /** 1.12: !isDead and world.loadedEntityList.contains(entity). */
   private static boolean isLoaded(Entity entity) {
      return !entity.isRemoved() && entity.isAlive() && entity.level().getEntity(entity.getId()) == entity;
   }
}
