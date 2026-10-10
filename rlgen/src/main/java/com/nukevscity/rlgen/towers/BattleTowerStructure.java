package com.nukevscity.rlgen.towers;

import com.mojang.serialization.MapCodec;
import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.rc.RcGenerator;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * Battle Towers. In 1.12 the first chunk generated at least 800 blocks from spawn and from every other tower got one (if the site
 * passed its tests), so towers depended on the order you explored. Here the structure set (spacing 60, separation 56 chunks) gives each
 * 960-block cell one area; the first of the chunks around it whose corner passes the 1.12 site tests on the 1.12 terrain gets the tower.
 * Neighbouring towers stay at least 816 blocks apart.
 */
public final class BattleTowerStructure extends Structure {
   public static final MapCodec<BattleTowerStructure> CODEC = simpleCodec(BattleTowerStructure::new);
   private static final int MIN_DISTANCE_FROM_SPAWN = 800;
   private static final int SEARCH = 3;

   public BattleTowerStructure(Structure.StructureSettings settings) {
      super(settings);
   }

   @Override
   public Optional<Structure.GenerationStub> findValidGenerationPoint(Structure.GenerationContext context) {
      return this.findGenerationPoint(context);
   }

   @Override
   protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
      if (!(context.chunkGenerator() instanceof LegacyChunkGenerator generator)) {
         return Optional.empty();
      }
      ChunkPos cp = context.chunkPos();
      Random random = new Random(context.seed() ^ (long)cp.x * 341873128712L ^ (long)cp.z * 132897987541L ^ 0x4261747454L);
      List<ChunkPos> candidates = new ArrayList<>();
      for (int dx = -SEARCH; dx <= SEARCH; dx++) {
         for (int dz = -SEARCH; dz <= SEARCH; dz++) {
            candidates.add(new ChunkPos(cp.x + dx, cp.z + dz));
         }
      }
      Collections.shuffle(candidates, random);
      RcGenerator.Oracle oracle = generator.oracle();
      TowerGen.Reader terrain = (x, y, z) -> oracle.get(new BlockPos(x, y, z));
      for (ChunkPos c : candidates) {
         int x = c.getMinBlockX();
         int z = c.getMinBlockZ();
         if (Math.sqrt((double)x * x + (double)z * z) < MIN_DISTANCE_FROM_SPAWN) {
            continue;
         }
         int y = TowerGen.surfaceHeight(terrain, x, z);
         if (y > 49 && TowerGen.choose(terrain, random, x, y, z, true) != null) {
            long seed = random.nextLong();
            return Optional.of(new Structure.GenerationStub(new BlockPos(x, y, z), builder -> builder.addPiece(new BattleTowerPiece(x, z, seed))));
         }
      }
      return Optional.empty();
   }

   @Override
   public StructureType<?> type() {
      return RLGen.BATTLE_TOWER.get();
   }
}
