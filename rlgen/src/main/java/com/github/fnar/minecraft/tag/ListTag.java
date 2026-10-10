package com.github.fnar.minecraft.tag;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;

public class ListTag implements Tag {
   private final List<Tag> tags = Lists.newLinkedList();

   @Override
   public TagType getType() {
      return TagType.LIST;
   }

   public List<Tag> getTags() {
      return this.tags;
   }

   public ListTag withTags(Tag... tags) {
      this.tags.addAll(Arrays.asList(tags));
      return this;
   }

   public ListTag withTags(String... values) {
      Arrays.stream(values).forEach(value -> this.withTags(new StringTag(value)));
      return this;
   }

   @Override
   public String toString() {
      return "ListTag(tags=" + this.getTags() + ")";
   }
}
