package com.nukevscity.rlgen.village;

import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.legacy.biome.Biomes;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.world.level.levelgen.structure.StructurePiece;

/** MapGenVillage.Start. */
public final class VillageStart {
   /** MapGenVillage.VILLAGE_SPAWN_BIOMES plus the Traverse biomes that have villages (BiomeManager.addVillageBiome). */
   private static volatile Set<Biome> villageBiomes;

   private VillageStart() {
   }

   public static boolean isVillageBiome(@Nullable Biome biome) {
      Set<Biome> s = villageBiomes;
      if (s == null) {
         s = new java.util.HashSet<>(List.of(Biomes.PLAINS, Biomes.DESERT, Biomes.SAVANNA, Biomes.TAIGA));
         for (String name : List.of("woodlands", "meadow", "red_desert", "badlands", "rocky_plateau", "lush_hills", "desert_shrubland", "arid_highland",
            "rocky_plains")) {
            Biome b = Biome.byName("traverse:" + name);
            if (b != null) {
               s.add(b);
            }
         }
         villageBiomes = s;
      }
      return biome != null && s.contains(biome);
   }

   /** The village's pieces, or null when it has two houses or fewer (isSizeableStructure). */
   @Nullable
   public static List<StructurePiece> build(Biome startBiome, Random random, int x, int z, int size) {
      List<StructurePiece> components = new ArrayList<>();
      List<VillagePieces.PieceWeight> list = VillagePieces.getStructureVillageWeightedPieceList(random, size);
      VillagePieces.Start start = new VillagePieces.Start(startBiome, 0, random, (x << 4) + 2, (z << 4) + 2, list, size);
      components.add(start);
      start.buildComponent(start, components, random);
      List<StructurePiece> roads = start.pendingRoads;
      List<StructurePiece> houses = start.pendingHouses;

      while (!roads.isEmpty() || !houses.isEmpty()) {
         if (roads.isEmpty()) {
            int i = random.nextInt(houses.size());
            ((VillagePieces.Village)houses.remove(i)).buildComponent(start, components, random);
         } else {
            int i = random.nextInt(roads.size());
            ((VillagePieces.Village)roads.remove(i)).buildComponent(start, components, random);
         }
      }

      int houseCount = 0;
      for (StructurePiece c : components) {
         if (!(c instanceof VillagePieces.Road)) {
            houseCount++;
         }
      }
      return houseCount > 2 ? components : null;
   }
}
