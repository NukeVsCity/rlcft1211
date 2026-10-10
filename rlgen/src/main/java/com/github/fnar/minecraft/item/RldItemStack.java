package com.github.fnar.minecraft.item;

import com.github.fnar.minecraft.tag.CompoundTag;
import com.github.fnar.minecraft.tag.ListTag;
import greymerk.roguelike.treasure.loot.ItemHideFlags;

public class RldItemStack {
   private RldItem item;
   private int count;
   private int damage;
   private CompoundTag tags;
   private boolean isPlzNbt = false;
   private String plzNbt;

   public RldItemStack(RldItem item, int count) {
      this.item = item;
      this.count = count;
   }

   public RldItem getItem() {
      return this.item;
   }

   public RldItemStack withItem(RldItem item) {
      this.item = item;
      return this;
   }

   public int getCount() {
      return this.count;
   }

   public RldItemStack withCount(int count) {
      this.count = count;
      return this;
   }

   public int getDamage() {
      return this.damage;
   }

   public RldItemStack withDamage(int damage) {
      this.damage = damage;
      return this;
   }

   public RldItemStack withTag(String name, CompoundTag value) {
      this.ensureTags().withTag(name, value);
      return this;
   }

   public RldItemStack withTag(String name, int value) {
      this.ensureTags().withTag(name, value);
      return this;
   }

   public RldItemStack withTag(String name, String value) {
      this.ensureTags().withTag(name, value);
      return this;
   }

   public CompoundTag getTags() {
      return this.tags;
   }

   private CompoundTag ensureTags() {
      if (this.tags == null) {
         this.tags = new CompoundTag();
      }

      return this.tags;
   }

   public RldItemStack withDisplayName(String name) {
      this.ensureCompoundTag("display").withTag("Name", name);
      return this;
   }

   public CompoundTag ensureCompoundTag(String name) {
      CompoundTag tag = this.ensureTags().getCompound(name);
      if (tag != null) {
         return tag;
      } else {
         tag = new CompoundTag();
         this.withTag(name, tag);
         return tag;
      }
   }

   public RldItemStack withDisplayLore(String... lore) {
      this.ensureCompoundTag("display").withTag("Lore", new ListTag().withTags(lore));
      return this;
   }

   public RldItemStack withHideFlag(ItemHideFlags... hideFlags) {
      ItemHideFlags.reduce(hideFlags).ifPresent(integer -> this.withTag("HideFlags", integer));
      return this;
   }

   public boolean isPlzNbt() {
      return this.isPlzNbt;
   }

   public RldItemStack plzNbt(String json) {
      this.isPlzNbt = true;
      this.plzNbt = json;
      return this;
   }

   public String getPlzNbt() {
      return this.plzNbt;
   }

   @Override
   public String toString() {
      return "RldItemStack{item="
         + this.item
         + ", count="
         + this.count
         + ", damage="
         + this.damage
         + ", tags="
         + this.tags
         + ", isPlzNbt="
         + this.isPlzNbt
         + ", plzNbt='"
         + this.plzNbt
         + '\''
         + '}';
   }
}
