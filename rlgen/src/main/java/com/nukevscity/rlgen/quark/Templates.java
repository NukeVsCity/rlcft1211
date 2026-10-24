package com.nukevscity.rlgen.quark;

import com.nukevscity.rlgen.deco.DWorld;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/** 1.12 Template.addBlocksToWorld / getDataBlocks for the converted templates in data/rlgen/structure. */
public final class Templates {
   private Templates() {
   }

   public static Optional<StructureTemplate> get(StructureTemplateManager manager, String id) {
      return manager.get(ResourceLocation.parse(id));
   }

   /** PlacementSettings with a rotation: structure blocks are left out (1.12 ignoreStructureBlock), and nothing is written outside clip. */
   public static StructurePlaceSettings settings(Rotation rotation, Mirror mirror, BoundingBox clip) {
      StructurePlaceSettings s = new StructurePlaceSettings().setRotation(rotation).setMirror(mirror).addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
      return clip == null ? s : s.setBoundingBox(clip);
   }

   /** The 3x3 chunks a decoration step may write to. */
   public static BoundingBox region(DWorld w, int centerChunkX, int centerChunkZ) {
      return new BoundingBox((centerChunkX - 1) * 16, 0, (centerChunkZ - 1) * 16, (centerChunkX + 1) * 16 + 15, 255, (centerChunkZ + 1) * 16 + 15);
   }

   public static void place(DWorld w, StructureTemplate t, BlockPos pos, StructurePlaceSettings settings, long seed) {
      t.placeInWorld(w.level, pos, pos, settings, RandomSource.create(seed), 2);
   }

   /** Template.getDataBlocks: data structure blocks' metadata by world position, in 1.12's HashMap order. */
   public static Map<BlockPos, String> dataBlocks(StructureTemplate t, BlockPos pos, StructurePlaceSettings settings) {
      Map<BlockPos, String> out = new HashMap<>();
      for (StructureTemplate.StructureBlockInfo info : t.filterBlocks(pos, settings, Blocks.STRUCTURE_BLOCK)) {
         if (info.nbt() != null) {
            out.put(info.pos(), info.nbt().getString("metadata"));
         }
      }
      return out;
   }
}
