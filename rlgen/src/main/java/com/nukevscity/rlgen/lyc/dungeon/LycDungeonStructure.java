package com.nukevscity.rlgen.lyc.dungeon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * Lycanites Mobs dungeons: every chunk (structure set spacing 1) gets one piece per dungeon whose sectors reach it. "dimension" is
 * the 1.12 dimension id the structure plans for: 0 on the 1.12 overworld, -1 and 1 on the 1.21 nether and End.
 */
public final class LycDungeonStructure extends Structure {
   public static final MapCodec<LycDungeonStructure> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
      settingsCodec(i), Codec.INT.optionalFieldOf("dimension", 0).forGetter(s -> s.dimension)).apply(i, LycDungeonStructure::new));
   private final int dimension;

   public LycDungeonStructure(Structure.StructureSettings settings, int dimension) {
      super(settings);
      this.dimension = dimension;
   }

   @Override
   public Optional<Structure.GenerationStub> findValidGenerationPoint(Structure.GenerationContext context) {
      return this.findGenerationPoint(context);
   }

   @Override
   protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
      LegacyChunkGenerator generator = context.chunkGenerator() instanceof LegacyChunkGenerator g ? g : null;
      if (generator == null && this.dimension == 0) {
         return Optional.empty();
      }
      ChunkPos cp = context.chunkPos();
      List<Integer> dungeons = LycDungeons.reaching(this.dimension, generator, context.seed(), cp);
      if (dungeons.isEmpty()) {
         return Optional.empty();
      }
      return Optional.of(new Structure.GenerationStub(new BlockPos(cp.getMinBlockX(), 64, cp.getMinBlockZ()),
         builder -> dungeons.forEach(i -> builder.addPiece(new LycDungeonPiece(cp, i, this.dimension)))));
   }

   @Override
   public StructureType<?> type() {
      return RLGen.LYC_DUNGEON.get();
   }
}
