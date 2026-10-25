package com.nukevscity.rlgen.bms;

import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.rc.BiomeDict;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import com.mojang.serialization.MapCodec;
import com.yungnickyoung.minecraft.bettermineshafts.config.Configuration;
import com.yungnickyoung.minecraft.bettermineshafts.mc.StructureBoundingBox;
import com.yungnickyoung.minecraft.bettermineshafts.mc.StructureComponent;
import com.yungnickyoung.minecraft.bettermineshafts.world.MapGenBetterMineshaft;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * YUNG's Better Mineshafts 2.2.1 (MapGenBetterMineshaft, which replaced 1.12's mineshafts through InitMapGenEvent): every chunk
 * (structure set spacing 1) is asked canSpawnStructureAtCoords with the MapGenBase random, and the start is built like 1.12.
 */
public final class MineshaftStructure extends Structure {
   public static final MapCodec<MineshaftStructure> CODEC = simpleCodec(MineshaftStructure::new);

   public MineshaftStructure(Structure.StructureSettings settings) {
      super(settings);
   }

   /** MapGenBase.generate / MapGenStructure.recursiveGenerate up to the start, or null when 1.12 had no mineshaft here. */
   static List<StructureComponent> start(LegacyChunkGenerator generator, long seed, int cx, int cz) {
      Random rand = new Random(seed);
      long j = rand.nextLong();
      long k = rand.nextLong();
      rand.setSeed((long)cx * j ^ (long)cz * k ^ seed);
      rand.nextInt();
      Biome biome = generator.legacyBiome((cx << 4) + 8, (cz << 4) + 8);
      if (BiomeDict.hasType(biome, "OCEAN") || BiomeDict.hasType(biome, "BEACH")) {
         return null;
      }
      if (!(rand.nextDouble() < Configuration.mineshaftSpawnRate)) {
         return null;
      }
      return MapGenBetterMineshaft.getStructureStart(biome, rand, cx, cz);
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
      List<StructureComponent> components = start(generator, context.seed(), cp.x, cp.z);
      if (components == null) {
         return Optional.empty();
      }
      StructureBoundingBox box = StructureBoundingBox.getNewBoundingBox();
      for (StructureComponent c : components) {
         box.expandTo(c.getBoundingBox());
      }
      MineshaftHolderPiece piece = new MineshaftHolderPiece(cp.x, cp.z, box);
      return Optional.of(new Structure.GenerationStub(new BlockPos((cp.x << 4) + 2, box.minY, (cp.z << 4) + 2), builder -> builder.addPiece(piece)));
   }

   @Override
   public StructureType<?> type() {
      return RLGen.MINESHAFT.get();
   }
}
