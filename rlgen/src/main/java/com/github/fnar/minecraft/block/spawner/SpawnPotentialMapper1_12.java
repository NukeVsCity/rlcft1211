package com.github.fnar.minecraft.block.spawner;

import com.github.fnar.minecraft.EffectType;
import com.github.fnar.minecraft.item.RldItem;
import com.github.fnar.minecraft.item.mapper.ItemMapper1_12;
import com.github.fnar.minecraft.item.mapper.Mc;
import greymerk.roguelike.config.RogueConfig;
import greymerk.roguelike.monster.Mob;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;

/** rlgen: SpawnPotentials for a 1.21 spawner (same 12 rolled mobs per potential, same equipment and the mining fatigue mark). */
public class SpawnPotentialMapper1_12 {
   /** 1.12 entity ids that 1.21 renamed. */
   private static final Map<String, String> RENAMED = Map.of("minecraft:zombie_pigman", "minecraft:zombified_piglin", "minecraft:villager_golem",
      "minecraft:iron_golem", "minecraft:snowman", "minecraft:snow_golem", "minecraft:evocation_illager", "minecraft:evoker", "minecraft:vindication_illager",
      "minecraft:vindicator", "minecraft:illusion_illager", "minecraft:illusioner", "minecraft:ender_crystal", "minecraft:end_crystal");

   public static String entityId(String name) {
      String id = name.contains(":") ? name.toLowerCase() : "minecraft:" + name.toLowerCase();
      return RENAMED.getOrDefault(id, id);
   }

   public static ListTag mapToNbt(List<SpawnPotential> spawnerPotentials, Random random, int level) {
      ListTag potentials = new ListTag();
      spawnerPotentials.stream().map(potential -> mapToNbt(potential, random, level)).forEach(potentials::addAll);
      return potentials;
   }

   private static ListTag mapToNbt(SpawnPotential spawnPotential, Random random, int level) {
      ListTag potentials = new ListTag();
      IntStream.range(0, 12).mapToObj(i -> spawnPotential.generateMob(random, level)).map(mob -> mapToNbt(spawnPotential, level, mob))
         .filter(java.util.Objects::nonNull).forEach(potentials::add);
      return potentials;
   }

   private static CompoundTag mapToNbt(SpawnPotential spawnPotential, int level, Mob mob) {
      String id = entityId(spawnPotential.getName());
      ResourceLocation rl = ResourceLocation.tryParse(id);
      if (rl == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(rl)) {
         return null; // a mob from a mod we do not have
      }
      CompoundTag entity = jsonToNbt(spawnPotential.getNbt()).copy();
      entity.putString("id", id);
      ListTag effects = new ListTag();
      CompoundTag fatigue = new CompoundTag();
      fatigue.putString("id", "minecraft:" + Mc.effectName(EffectType.FATIGUE.getEffectID()));
      fatigue.putByte("amplifier", (byte)level);
      fatigue.putInt("duration", 10);
      fatigue.putBoolean("ambient", false);
      effects.add(fatigue);
      entity.put("active_effects", effects);
      setLootingRateTags(entity);
      ListTag hands = new ListTag();
      appendItemTag(hands, mob.getMainhand());
      appendItemTag(hands, mob.getOffhand());
      entity.put("HandItems", hands);
      ListTag armour = new ListTag();
      appendItemTag(armour, mob.getBoots());
      appendItemTag(armour, mob.getLeggings());
      appendItemTag(armour, mob.getChestplate());
      appendItemTag(armour, mob.getHelmet());
      entity.put("ArmorItems", armour);
      CompoundTag data = new CompoundTag();
      data.put("entity", entity);
      CompoundTag potential = new CompoundTag();
      potential.put("data", data);
      potential.putInt("weight", Math.max(1, spawnPotential.getWeight()));
      return potential;
   }

   /** Only the item id went into the spawner (no enchantments); missing items shortened the list, shifting later slots. */
   private static void appendItemTag(ListTag parent, RldItem item) {
      if (item == null) {
         return;
      }
      try {
         String itemName = BuiltInRegistries.ITEM.getKey(new ItemMapper1_12().map(item.asStack()).getItem()).toString();
         CompoundTag tag = new CompoundTag();
         tag.putString("id", itemName);
         tag.putInt("count", 1);
         parent.add(tag);
      } catch (RuntimeException ignored) {
      }
   }

   private static void setLootingRateTags(CompoundTag entityNbt) {
      float chance = (float)RogueConfig.MOBS_ITEMS_DROP_CHANCE.getDouble();
      ListTag hands = new ListTag();
      hands.add(FloatTag.valueOf(chance));
      hands.add(FloatTag.valueOf(chance));
      entityNbt.put("HandDropChances", hands);
      ListTag armor = new ListTag();
      for (int i = 0; i < 4; i++) {
         armor.add(FloatTag.valueOf(chance));
      }
      entityNbt.put("ArmorDropChances", armor);
   }

   private static CompoundTag jsonToNbt(String entityNbt) {
      try {
         return entityNbt == null ? new CompoundTag() : TagParser.parseTag(entityNbt);
      } catch (Exception e) {
         return new CompoundTag();
      }
   }
}
