package com.nukevscity.rlgen.lyc.dungeon;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

/**
 * The parts of Lycanites' MobSpawn a dungeon uses: the weight (Lycanites creatures without one used their creature spawn weight,
 * 8 here) and the dungeon level/boss filters. Lycanites mobs are not ported, so spawners get a vanilla stand-in.
 */
public class MobSpawn {
   public final String mobId;
   protected int weight = -1;
   public int dungeonLevelMin = -1;
   public int dungeonLevelMax = -1;
   public boolean dungeonBoss = false;

   private MobSpawn(String mobId) {
      this.mobId = mobId;
   }

   public static MobSpawn createFromJSON(JsonObject json) {
      if (!json.has("mobId")) {
         return null;
      }
      MobSpawn m = new MobSpawn(json.get("mobId").getAsString());
      if (json.has("weight")) {
         m.weight = json.get("weight").getAsInt();
      }
      if (json.has("dungeonLevelMin")) {
         m.dungeonLevelMin = json.get("dungeonLevelMin").getAsInt();
      }
      if (json.has("dungeonLevelMax")) {
         m.dungeonLevelMax = json.get("dungeonLevelMax").getAsInt();
      }
      if (json.has("dungeonBoss")) {
         m.dungeonBoss = json.get("dungeonBoss").getAsBoolean();
      }
      return m;
   }

   public int getWeight() {
      return this.weight > -1 ? this.weight : 8;
   }

   /** The entity a spawner gets: vanilla ids as they are, Lycanites mobs as zombies until Lycanites is ported. */
   public EntityType<?> entityType() {
      ResourceLocation id = ResourceLocation.tryParse(this.mobId);
      if (id != null && id.getNamespace().equals("minecraft") && BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
         return BuiltInRegistries.ENTITY_TYPE.get(id);
      }
      return EntityType.ZOMBIE;
   }
}
