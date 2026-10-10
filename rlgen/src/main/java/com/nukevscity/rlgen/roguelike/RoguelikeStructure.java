package com.nukevscity.rlgen.roguelike;

import com.mojang.serialization.MapCodec;
import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/** Roguelike Dungeons: every chunk asks which nearby dungeons have blocks in it and gets one piece per dungeon (structure set spacing 1). */
public final class RoguelikeStructure extends Structure {
   public static final MapCodec<RoguelikeStructure> CODEC = simpleCodec(RoguelikeStructure::new);

   public RoguelikeStructure(Structure.StructureSettings settings) {
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
      List<int[]> hits = new ArrayList<>();
      for (int[] g : RoguelikeDungeons.cornersNear(cp.x, cp.z)) {
         if (RoguelikeDungeons.chunks(generator, context.seed(), g[0], g[1], context.registryAccess()).contains(cp.toLong())) {
            hits.add(g);
         }
      }
      if (hits.isEmpty()) {
         return Optional.empty();
      }
      return Optional.of(new Structure.GenerationStub(new BlockPos(cp.getMinBlockX(), 64, cp.getMinBlockZ()), builder -> {
         for (int[] g : hits) {
            builder.addPiece(new RoguelikePiece(cp, g[0], g[1]));
         }
      }));
   }

   @Override
   public StructureType<?> type() {
      return RLGen.ROGUELIKE.get();
   }
}
