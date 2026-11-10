package com.github.alexthe666.iceandfire.message;

import com.github.alexthe666.iceandfire.entity.IafLand;
import com.github.alexthe666.iceandfire.entity.MyrmexHive;
import com.github.alexthe666.iceandfire.world.MyrmexWorldData;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 1.12 MessageGetMyrmexHive: the hive's village NBT, both ways. To the client it becomes ClientProxy.referedClientHive; to the
 * server it overwrites the server hive with the same UUID (no permission check, like 1.12).
 */
public record MessageGetMyrmexHive(CompoundTag tag) implements CustomPacketPayload {
   public static final Type<MessageGetMyrmexHive> TYPE = new Type<>(IafLand.rl("get_myrmex_hive"));
   public static final StreamCodec<RegistryFriendlyByteBuf, MessageGetMyrmexHive> CODEC =
      ByteBufCodecs.COMPOUND_TAG.<RegistryFriendlyByteBuf>cast().map(MessageGetMyrmexHive::new, MessageGetMyrmexHive::tag);

   public static MessageGetMyrmexHive of(@Nullable MyrmexHive hive) {
      CompoundTag tag = new CompoundTag();
      if (hive != null) {
         hive.writeVillageDataToNBT(tag);
      }
      return new MessageGetMyrmexHive(tag);
   }

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   /** fromBytes: a new hive read from the tag. */
   private MyrmexHive hive() {
      MyrmexHive hive = new MyrmexHive();
      hive.readVillageDataFromNBT(this.tag);
      return hive;
   }

   public static void handle(MessageGetMyrmexHive message, IPayloadContext context) {
      if (context.flow().isClientbound()) {
         com.github.alexthe666.iceandfire.client.MyrmexStaffClient.setReferedClientHive(message.hive());
      } else {
         MyrmexHive hive = message.hive();
         MyrmexHive serverHive = MyrmexWorldData.get(context.player().level()).getHiveFromUUID(hive.hiveUUID);
         if (serverHive != null) {
            CompoundTag tag = new CompoundTag();
            hive.writeVillageDataToNBT(tag);
            serverHive.readVillageDataFromNBT(tag);
            MyrmexWorldData.markDirty(context.player().level());
         }
      }
   }
}
