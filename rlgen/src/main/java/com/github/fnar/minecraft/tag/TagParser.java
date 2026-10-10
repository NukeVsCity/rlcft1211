package com.github.fnar.minecraft.tag;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map.Entry;

public class TagParser {
   public Tag parse(JsonElement jsonElement) {
      if (jsonElement.isJsonNull()) {
         return null;
      } else if (jsonElement.isJsonArray()) {
         return this.parse(jsonElement.getAsJsonArray());
      } else if (jsonElement.isJsonObject()) {
         return this.parse(jsonElement.getAsJsonObject());
      } else {
         return jsonElement.isJsonPrimitive() ? new StringTag(jsonElement.getAsString()) : null;
      }
   }

   public CompoundTag parse(JsonObject jsonObject) {
      CompoundTag compoundTag = new CompoundTag();

      for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
         String key = entry.getKey();
         JsonElement value = entry.getValue();
         compoundTag.withTag(key, this.parse(value));
      }

      return compoundTag;
   }

   public ListTag parse(JsonArray array) {
      ListTag listTag = new ListTag();

      for (int i = 0; i < array.size(); i++) {
         listTag.withTags(this.parse(array.get(i)));
      }

      return listTag;
   }
}
