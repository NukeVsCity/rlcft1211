package com.nukevscity.rlgen.ruins;

import com.mojang.serialization.MapCodec;
import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * Ruins: every chunk gets one piece per built ruin that reaches it (structure set spacing 1). "dimension" is the 1.12 DimensionType
 * name the structure stands for (overworld, the_nether, the_end); the nether and End ones only run in RLCraft Legacy worlds.
 */
public final class RuinsStructure extends Structure {
   public static final MapCodec<RuinsStructure> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
      settingsCodec(i), Codec.STRING.optionalFieldOf("dimension", "overworld").forGetter(s -> s.dim.name)
   ).apply(i, RuinsStructure::new));
   private final RuinsTerrain.Dim dim;

   public RuinsStructure(Structure.StructureSettings settings, String dimension) {
      super(settings);
      this.dim = RuinsTerrain.Dim.byName(dimension);
   }

   /** The ruins terrain of a chunk generator, or null where Ruins does not run. */
   static RuinsTerrain terrain(RuinsTerrain.Dim dim, long seed, ChunkGenerator gen, LevelHeightAccessor height, RandomState random) {
      if (dim == RuinsTerrain.Dim.OVERWORLD) {
         return gen instanceof LegacyChunkGenerator legacy ? RuinsTerrain.legacy(legacy) : null;
      }
      MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
      if (gen instanceof LegacyChunkGenerator || server == null || !(server.overworld().getChunkSource().getGenerator() instanceof LegacyChunkGenerator)) {
         return null;
      }
      return RuinsTerrain.vanilla(dim, seed, gen, height, random);
   }

   @Override
   public Optional<Structure.GenerationStub> findValidGenerationPoint(Structure.GenerationContext context) {
      return this.findGenerationPoint(context);
   }

   @Override
   protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
      RuinsTerrain generator = terrain(this.dim, context.seed(), context.chunkGenerator(), context.heightAccessor(), context.randomState());
      if (generator == null) {
         return Optional.empty();
      }
      ChunkPos cp = context.chunkPos();
      List<RuinsPiece> pieces = new ArrayList<>();
      for (RuinsGen.Candidate c : RuinsGen.reaching(generator, context.seed(), cp)) {
         int y = RuinsGen.decide(generator, c);
         if (y >= 0) {
            pieces.add(new RuinsPiece(cp, this.dim, c.chunkX(), c.chunkZ(), c.index(), y));
         }
      }
      if (pieces.isEmpty()) {
         return Optional.empty();
      }
      return Optional.of(new Structure.GenerationStub(new BlockPos(cp.getMinBlockX(), 64, cp.getMinBlockZ()), builder -> pieces.forEach(builder::addPiece)));
   }

   @Override
   public StructureType<?> type() {
      return RLGen.RUINS.get();
   }
}
