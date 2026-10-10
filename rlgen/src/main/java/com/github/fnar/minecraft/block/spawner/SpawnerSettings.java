package com.github.fnar.minecraft.block.spawner;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import greymerk.roguelike.util.WeightedChoice;
import greymerk.roguelike.util.WeightedRandomizer;
import java.util.List;
import java.util.Random;

public class SpawnerSettings {
   private WeightedRandomizer<Spawner> spawners = new WeightedRandomizer<>();

   public SpawnerSettings() {
   }

   public SpawnerSettings(SpawnerSettings toCopy) {
      this.spawners.merge(toCopy.spawners);
   }

   public SpawnerSettings(SpawnerSettings base, SpawnerSettings other) {
      this.spawners.merge(base.spawners);
      this.spawners.merge(other.spawners);
   }

   public WeightedRandomizer<Spawner> getSpawners() {
      return this.spawners;
   }

   public boolean isEmpty() {
      return this.spawners.isEmpty();
   }

   public static Spawner parseSpawnPotentials(JsonObject entry) throws Exception {
      JsonElement spawnPotentialsJson = entry.get("potentials");
      List<SpawnPotential> spawnPotentials = SpawnPotentialParser.parse(spawnPotentialsJson);
      return new Spawner(spawnPotentials);
   }

   public static int parseWeight(JsonObject entry) {
      return entry.has("weight") ? entry.get("weight").getAsInt() : 1;
   }

   public void add(Spawner spawner, int weight) {
      this.spawners.add(new WeightedChoice<>(spawner, weight));
   }

   @Override
   public String toString() {
      return this.spawners.toString();
   }

   public Spawner chooseOneAtRandom(Random random) {
      return this.getSpawners().get(random);
   }
}
