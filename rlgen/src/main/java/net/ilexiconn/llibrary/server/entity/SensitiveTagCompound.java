package net.ilexiconn.llibrary.server.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

/**
 * LLibrary 1.7.20 SensitiveTagCompound: remembers which keys changed. Same comparisons as 1.12 (array values compare by
 * reference, so arrays always count as changed).
 */
public class SensitiveTagCompound extends CompoundTag {
   private boolean changed = false;
   private final Set<String> changedTags = new HashSet<>();

   @Override
   public void putByte(String key, byte value) {
      Tag b = super.put(key, ByteTag.valueOf(value));
      if (b == null || b instanceof ByteTag t && t.getAsByte() != value) {
         this.updateChanged(key);
      }
   }

   @Override
   public void putBoolean(String key, boolean value) {
      this.putByte(key, (byte)(value ? 1 : 0));
   }

   @Override
   public void putByteArray(String key, byte[] value) {
      Tag b = super.put(key, new ByteArrayTag(value));
      if (b == null || b instanceof ByteArrayTag t && t.getAsByteArray() != value) {
         this.updateChanged(key);
      }
   }

   @Override
   public void putDouble(String key, double value) {
      Tag b = super.put(key, DoubleTag.valueOf(value));
      if (b == null || b instanceof DoubleTag t && t.getAsDouble() != value) {
         this.updateChanged(key);
      }
   }

   @Override
   public void putFloat(String key, float value) {
      Tag b = super.put(key, FloatTag.valueOf(value));
      if (b == null || b instanceof FloatTag t && t.getAsFloat() != value) {
         this.updateChanged(key);
      }
   }

   @Override
   public void putIntArray(String key, int[] value) {
      Tag b = super.put(key, new IntArrayTag(value));
      if (b == null || b instanceof IntArrayTag t && t.getAsIntArray() != value) {
         this.updateChanged(key);
      }
   }

   @Override
   public void putLongArray(String key, long[] value) {
      Tag b = super.put(key, new LongArrayTag(value));
      if (b == null || b instanceof LongArrayTag t && t.getAsLongArray() != value) {
         this.updateChanged(key);
      }
   }

   @Override
   public void putInt(String key, int value) {
      Tag b = super.put(key, IntTag.valueOf(value));
      if (b == null || b instanceof IntTag t && t.getAsInt() != value) {
         this.updateChanged(key);
      }
   }

   @Override
   public void putLong(String key, long value) {
      Tag b = super.put(key, LongTag.valueOf(value));
      if (b == null || b instanceof LongTag t && t.getAsLong() != value) {
         this.updateChanged(key);
      }
   }

   @Override
   public void putShort(String key, short value) {
      Tag b = super.put(key, ShortTag.valueOf(value));
      if (b == null || b instanceof ShortTag t && t.getAsShort() != value) {
         this.updateChanged(key);
      }
   }

   @Override
   public void putString(String key, String value) {
      Tag b = super.put(key, StringTag.valueOf(value));
      if (b == null || b instanceof StringTag t && !t.getAsString().equals(value)) {
         this.updateChanged(key);
      }
   }

   @Override
   public void putUUID(String key, UUID value) {
      this.put(key, NbtUtils.createUUID(value));
   }

   @Override
   public Tag put(String key, Tag value) {
      Tag b = super.put(key, value);
      if (b == null || !b.equals(value)) {
         this.updateChanged(key);
      }
      return b;
   }

   public void updateChanged(String key) {
      this.changedTags.add(key);
      this.changed = true;
   }

   public CompoundTag getChangedCopy() {
      CompoundTag tag = new CompoundTag();
      for (String s : this.changedTags) {
         Tag value = this.get(s);
         if (value != null) {
            tag.put(s, value);
         }
      }
      return tag;
   }

   public boolean hasChanged() {
      return this.changed;
   }

   public void reset() {
      this.changed = false;
      this.changedTags.clear();
   }
}
