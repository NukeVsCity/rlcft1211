package com.nukevscity.rlgen.lyc.dungeon;

import com.mojang.serialization.MapCodec;
import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/** Lycanites Mobs dungeons: every chunk (structure set spacing 1) gets one piece per dungeon whose sectors reach it. */
public final class LycDungeonStructure extends Structure {
   public static final MapCodec<LycDungeonStructure> CODEC = simpleCodec(LycDungeonStructure::new);

   public LycDungeonStructure(Structure.StructureSettings settings) {
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
      List<Integer> dungeons = LycDungeons.reaching(generator, context.seed(), cp);
      if (dungeons.isEmpty()) {
         return Optional.empty();
      }
      return Optional.of(new Structure.GenerationStub(new BlockPos(cp.getMinBlockX(), 64, cp.getMinBlockZ()),
         builder -> dungeons.forEach(i -> builder.addPiece(new LycDungeonPiece(cp, i)))));
   }

   @Override
   public StructureType<?> type() {
      return RLGen.LYC_DUNGEON.get();
   }
}
