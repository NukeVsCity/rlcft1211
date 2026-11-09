package net.ilexiconn.llibrary.server.network;

import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.animation.IAnimatedEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/** LLibrary 1.7.20 AnimationMessage (server -> client: entity id, index into getAnimations(), -1 = NO_ANIMATION). */
public class AnimationMessage extends AbstractMessage<AnimationMessage> {
   private int entityID;
   private int index;

   public AnimationMessage() {
   }

   public AnimationMessage(int entityID, int index) {
      this.entityID = entityID;
      this.index = index;
   }

   @Override
   public void onClientReceived(Minecraft client, AnimationMessage message, Player player, MessageContext messageContext) {
      Entity found = player.level().getEntity(message.entityID);
      IAnimatedEntity entity = (IAnimatedEntity)found;
      if (entity != null) {
         if (message.index == -1) {
            entity.setAnimation(IAnimatedEntity.NO_ANIMATION);
         } else {
            entity.setAnimation(entity.getAnimations()[message.index]);
         }

         entity.setAnimationTick(0);
      }
   }

   @Override
   public void onServerReceived(MinecraftServer server, AnimationMessage message, Player player, MessageContext messageContext) {
   }

   @Override
   public void fromBytes(ByteBuf byteBuf) {
      this.entityID = byteBuf.readInt();
      this.index = byteBuf.readInt();
   }

   @Override
   public void toBytes(ByteBuf byteBuf) {
      byteBuf.writeInt(this.entityID);
      byteBuf.writeInt(this.index);
   }
}
