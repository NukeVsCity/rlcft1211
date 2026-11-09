package net.ilexiconn.llibrary.server.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

/** Stand-in for Forge 1.12 ByteBufUtils for {@link AbstractMessage#fromBytes}/{@link AbstractMessage#toBytes} buffers. */
public final class ByteBufUtils {
   private ByteBufUtils() {
   }

   public static String readUTF8String(ByteBuf buf) {
      return new FriendlyByteBuf(buf).readUtf();
   }

   public static void writeUTF8String(ByteBuf buf, String string) {
      new FriendlyByteBuf(buf).writeUtf(string);
   }

   public static CompoundTag readTag(ByteBuf buf) {
      return new FriendlyByteBuf(buf).readNbt();
   }

   public static void writeTag(ByteBuf buf, CompoundTag tag) {
      new FriendlyByteBuf(buf).writeNbt(tag);
   }

   public static int readVarInt(ByteBuf buf, int maxSize) {
      return new FriendlyByteBuf(buf).readVarInt();
   }

   public static void writeVarInt(ByteBuf buf, int value, int maxSize) {
      new FriendlyByteBuf(buf).writeVarInt(value);
   }

   /** Needs the RegistryFriendlyByteBuf the payload codec passes in. */
   public static ItemStack readItemStack(ByteBuf buf) {
      return ItemStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf)buf);
   }

   public static void writeItemStack(ByteBuf buf, ItemStack stack) {
      ItemStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf)buf, stack);
   }
}
