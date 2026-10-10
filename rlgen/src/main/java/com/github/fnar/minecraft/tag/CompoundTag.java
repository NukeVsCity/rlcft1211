package com.github.fnar.minecraft.tag;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class CompoundTag implements Tag {
   private final Map<String, Tag> tagMap = Maps.newHashMap();

   public Set<Entry<String, Tag>> getTags() {
      return this.tagMap.entrySet();
   }

   public void setTag(String name, Tag tag) {
      this.tagMap.put(name, tag);
   }

   public Tag getTag(String name) {
      return this.tagMap.get(name);
   }

   public CompoundTag getCompound(String name) {
      Tag tag = this.getTag(name);
      return tag == null ? null : (CompoundTag)tag;
   }

   public CompoundTag withTag(String name, Tag tag) {
      this.tagMap.put(name, tag);
      return this;
   }

   public CompoundTag withTag(String name, int value) {
      return this.withTag(name, new IntTag(value));
   }

   public CompoundTag withTag(String name, String value) {
      return this.withTag(name, new StringTag(value));
   }

   @Override
   public TagType getType() {
      return TagType.COMPOUND;
   }

   @Override
   public String toString() {
      return "CompoundTag(tagMap=" + this.tagMap + ")";
   }
}
