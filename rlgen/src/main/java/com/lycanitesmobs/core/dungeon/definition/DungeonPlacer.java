package com.lycanitesmobs.core.dungeon.definition;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import com.nukevscity.rlgen.lyc.dungeon.*;

import com.google.gson.JsonObject;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.yungnickyoung.minecraft.bettermineshafts.mc.World;

public abstract class DungeonPlacer {
   public static DungeonPlacer createFromJson(JsonObject json) {
      String type = json.get("type").getAsString();
      DungeonPlacer spawnCondition = null;
      if ("random".equalsIgnoreCase(type)) {
         spawnCondition = new DungeonPlacer.RandomPlacer();
      } else if ("surface".equalsIgnoreCase(type)) {
         spawnCondition = new DungeonPlacer.SurfacePlacer();
      }

      spawnCondition.loadFromJSON(json);
      return spawnCondition;
   }

   public void loadFromJSON(JsonObject json) {
   }

   public abstract BlockPos calculatePosition(World var1, BlockPos var2, Random var3);

   public static class RandomPlacer extends DungeonPlacer {
      private int minY = 64;
      private int maxY = 64;

      @Override
      public void loadFromJSON(JsonObject json) {
         if (json.has("minY")) {
            this.minY = json.get("minY").getAsInt();
         }

         if (json.has("maxY")) {
            this.maxY = json.get("maxY").getAsInt();
         }
      }

      @Override
      public BlockPos calculatePosition(World world, BlockPos pos, Random random) {
         int y = this.minY;
         if (this.minY < this.maxY) {
            y += random.nextInt(this.maxY - this.minY);
         }

         return new BlockPos(pos.getX(), y, pos.getZ());
      }
   }

   public static class SurfacePlacer extends DungeonPlacer {
      private Set<String> materialsToIgnore = Set.of("air", "wood", "leaves", "plants", "vine", "snow", "cactus");

      @Override
      public void loadFromJSON(JsonObject json) {
         this.materialsToIgnore = new HashSet<>(JSONHelper.getJsonMaterials(json));
      }

      /** Down from the top of the chunk's highest section; 1.12 generated the far chunk on demand, so this reads the 1.12 terrain. */
      @Override
      public BlockPos calculatePosition(World world, BlockPos pos, Random random) {
         MutableBlockPos mutablePos = new MutableBlockPos(pos.getX(), world.topFilledSegment(pos) + 15, pos.getZ());
         for (String material = JSONHelper.material(world.getTerrainState(mutablePos));
            mutablePos.getY() > 0 && this.materialsToIgnore.contains(material);
            material = JSONHelper.material(world.getTerrainState(mutablePos))
         ) {
            mutablePos.setY(mutablePos.getY() - 1);
         }
         return new BlockPos(pos.getX(), mutablePos.getY(), pos.getZ());
      }
   }
}
