package net.ilexiconn.llibrary.server.network;

import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.capability.EntityDataHandler;
import net.ilexiconn.llibrary.server.capability.IEntityData;
import net.ilexiconn.llibrary.server.entity.EntityProperties;
import net.ilexiconn.llibrary.server.entity.PropertiesTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/** LLibrary 1.7.20 PropertiesMessage (server -> client sync of the changed tracking tags of an EntityProperties). */
public class PropertiesMessage extends AbstractMessage<PropertiesMessage> {
   private String propertyID;
   private CompoundTag compound;
   private int entityID;

   public PropertiesMessage() {
   }

   public PropertiesMessage(PropertiesTracker<?> tracker, Entity entity) {
      this.propertyID = tracker.getProperties().getID();
      this.compound = tracker.getTrackingTag().getChangedCopy();
      this.entityID = entity.getId();
   }

   @Override
   public void onClientReceived(Minecraft client, PropertiesMessage message, Player player, MessageContext messageContext) {
      Entity entity = player.level().getEntity(message.entityID);
      if (entity != null) {
         IEntityData<?> extendedProperties = EntityDataHandler.INSTANCE.getEntityData(entity, message.propertyID);
         if (extendedProperties instanceof EntityProperties<?> properties) {
            properties.loadTrackingSensitiveData(message.compound);
            properties.onSync();
         }
      }
   }

   @Override
   public void onServerReceived(MinecraftServer server, PropertiesMessage message, Player player, MessageContext messageContext) {
   }

   @Override
   public void fromBytes(ByteBuf buf) {
      this.propertyID = ByteBufUtils.readUTF8String(buf);
      this.compound = ByteBufUtils.readTag(buf);
      this.entityID = buf.readInt();
   }

   @Override
   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.propertyID);
      ByteBufUtils.writeTag(buf, this.compound);
      buf.writeInt(this.entityID);
   }
}
