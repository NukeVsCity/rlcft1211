package com.nukevscity.rlgen.world;

import it.unimi.dsi.fastutil.HashCommon;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

/**
 * MapGenStructure.generateStructure as 1.12 ChunkGeneratorOverworld.populate called it: the starts of a structure whose box meets the
 * populate area (chunk * 16 + 8 .. + 23) get their pieces built there, with the populate random. Found once per decorated chunk before
 * the decoration steps run; while a chunk is decorated, the 1.21 postProcess of these pieces does nothing (the populate steps build them).
 */
public final class PopulateStarts {
   private static final ResourceLocation MINESHAFT = ResourceLocation.fromNamespaceAndPath("rlgen", "better_mineshaft");
   private static final ResourceLocation VILLAGE = ResourceLocation.fromNamespaceAndPath("rlgen", "village");
   /** Long2ObjectOpenHashMap(1024) of 1.12 MapGenStructure.structureMap: 2048 slots until it holds 1536 starts. */
   private static final int MAP_MASK = 2047;

   public record Starts(long chunk, LegacyChunkGenerator generator, List<StructureStart> mineshafts, List<StructureStart> villages) {
   }

   private static final ThreadLocal<Starts> CURRENT = new ThreadLocal<>();
   private static final ThreadLocal<Boolean> DECORATING = new ThreadLocal<>();

   private PopulateStarts() {
   }

   /** Whether the calling thread is decorating a chunk of a LegacyChunkGenerator (pieces are then built by the populate steps). */
   public static boolean decorating() {
      return Boolean.TRUE.equals(DECORATING.get());
   }

   static void begin(Starts starts) {
      CURRENT.set(starts);
      DECORATING.set(Boolean.TRUE);
   }

   static void end() {
      CURRENT.remove();
      DECORATING.remove();
   }

   /** The starts found for the chunk being decorated, or null. */
   public static Starts get(ChunkPos cp) {
      Starts s = CURRENT.get();
      return s != null && s.chunk == cp.toLong() ? s : null;
   }

   static Starts collect(WorldGenLevel level, ChunkPos cp, StructureManager structures, LegacyChunkGenerator generator) {
      var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
      return new Starts(cp.toLong(), generator, find(level, registry.get(MINESHAFT), cp, structures), find(level, registry.get(VILLAGE), cp, structures));
   }

   private static List<StructureStart> find(WorldGenLevel level, Structure structure, ChunkPos cp, StructureManager structures) {
      List<StructureStart> out = new ArrayList<>();
      if (structure == null) {
         return out;
      }
      int minX = cp.getMinBlockX() + 8;
      int minZ = cp.getMinBlockZ() + 8;
      Map<Long, StructureStart> found = new LinkedHashMap<>();
      // the populate area lies in this chunk and the ones +1 east/south; their references name every start reaching it
      for (int dx = 0; dx <= 1; dx++) {
         for (int dz = 0; dz <= 1; dz++) {
            // the neighbours' references can name a start one chunk beyond what this chunk's WorldGenRegion holds: skipped
            it.unimi.dsi.fastutil.longs.LongSet refs = level.getChunk(cp.x + dx, cp.z + dz,
               net.minecraft.world.level.chunk.status.ChunkStatus.STRUCTURE_REFERENCES).getReferencesForStructure(structure);
            it.unimi.dsi.fastutil.longs.LongSet reachable = new it.unimi.dsi.fastutil.longs.LongOpenHashSet();
            for (long r : refs) {
               if (level.hasChunk(ChunkPos.getX(r), ChunkPos.getZ(r))) {
                  reachable.add(r);
               }
            }
            structures.fillStartsForStructure(structure, reachable, start -> {
               if (start.isValid() && start.getBoundingBox().intersects(minX, minZ, minX + 15, minZ + 15)) {
                  found.putIfAbsent(start.getChunkPos().toLong(), start);
               }
            });
         }
      }
      out.addAll(found.values());
      // 1.12 walked structureMap.values(): open hash map slots from the last one down
      out.sort(Comparator.comparingInt((StructureStart s) -> (int)HashCommon.mix(s.getChunkPos().toLong()) & MAP_MASK).reversed());
      return out;
   }
}
