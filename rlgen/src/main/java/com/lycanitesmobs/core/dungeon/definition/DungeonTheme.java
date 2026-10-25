package com.lycanitesmobs.core.dungeon.definition;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import com.nukevscity.rlgen.lyc.dungeon.*;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.core.dungeon.instance.SectorInstance;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.commons.lang3.math.NumberUtils;

public class DungeonTheme {
   public String name = "";
   public String floorPattern = "random";
   public String wallPattern = "random";
   public String ceilingPattern = "random";
   public List<ThemeBlock> floorBlocks = new ArrayList<>();
   public List<ThemeBlock> wallBlocks = new ArrayList<>();
   public List<ThemeBlock> ceilingBlocks = new ArrayList<>();
   public List<ThemeBlock> lightBlocks = new ArrayList<>();
   public List<ThemeBlock> torchBlocks = new ArrayList<>();
   public List<ThemeBlock> stairBlocks = new ArrayList<>();
   public List<ThemeBlock> pitBlocks = new ArrayList<>();
   public List<ThemeBlock> airBlocks = new ArrayList<>();
   public boolean fillEntranceAndAboveWithAir = true;

   public void loadFromJSON(JsonObject json) {
      this.name = json.get("name").getAsString().toLowerCase();
      if (json.has("floorPattern")) {
         this.floorPattern = json.get("floorPattern").getAsString().toLowerCase();
      }

      if (json.has("wallPattern")) {
         this.wallPattern = json.get("wallPattern").getAsString().toLowerCase();
      }

      if (json.has("ceilingPattern")) {
         this.ceilingPattern = json.get("ceilingPattern").getAsString().toLowerCase();
      }

      if (json.has("floorBlocks")) {
         for (JsonElement jsonElement : json.get("floorBlocks").getAsJsonArray()) {
            ThemeBlock themeBlock = new ThemeBlock();
            themeBlock.loadFromJSON(jsonElement.getAsJsonObject());
            this.floorBlocks.add(themeBlock);
         }
      }

      if (json.has("wallBlocks")) {
         for (JsonElement jsonElement : json.get("wallBlocks").getAsJsonArray()) {
            ThemeBlock themeBlock = new ThemeBlock();
            themeBlock.loadFromJSON(jsonElement.getAsJsonObject());
            this.wallBlocks.add(themeBlock);
         }
      }

      if (json.has("ceilingBlocks")) {
         for (JsonElement jsonElement : json.get("ceilingBlocks").getAsJsonArray()) {
            ThemeBlock themeBlock = new ThemeBlock();
            themeBlock.loadFromJSON(jsonElement.getAsJsonObject());
            this.ceilingBlocks.add(themeBlock);
         }
      }

      if (json.has("lightBlocks")) {
         for (JsonElement jsonElement : json.get("lightBlocks").getAsJsonArray()) {
            ThemeBlock themeBlock = new ThemeBlock();
            themeBlock.loadFromJSON(jsonElement.getAsJsonObject());
            this.lightBlocks.add(themeBlock);
         }
      } else {
         this.lightBlocks.addAll(this.wallBlocks);
      }

      if (json.has("torchBlocks")) {
         for (JsonElement jsonElement : json.get("torchBlocks").getAsJsonArray()) {
            ThemeBlock themeBlock = new ThemeBlock();
            themeBlock.loadFromJSON(jsonElement.getAsJsonObject());
            this.torchBlocks.add(themeBlock);
         }
      } else {
         ThemeBlock themeBlock = new ThemeBlock();
         themeBlock.block = Blocks.AIR;
         this.torchBlocks.add(themeBlock);
      }

      if (json.has("stairBlocks")) {
         for (JsonElement jsonElement : json.get("stairBlocks").getAsJsonArray()) {
            ThemeBlock themeBlock = new ThemeBlock();
            themeBlock.loadFromJSON(jsonElement.getAsJsonObject());
            this.stairBlocks.add(themeBlock);
         }
      }

      if (json.has("pitBlocks")) {
         for (JsonElement jsonElement : json.get("pitBlocks").getAsJsonArray()) {
            ThemeBlock themeBlock = new ThemeBlock();
            themeBlock.loadFromJSON(jsonElement.getAsJsonObject());
            this.pitBlocks.add(themeBlock);
         }
      }

      if (json.has("airBlocks")) {
         for (JsonElement jsonElement : json.get("airBlocks").getAsJsonArray()) {
            ThemeBlock themeBlock = new ThemeBlock();
            themeBlock.loadFromJSON(jsonElement.getAsJsonObject());
            this.airBlocks.add(themeBlock);
         }
      } else {
         ThemeBlock themeBlock = new ThemeBlock();
         themeBlock.block = Blocks.AIR;
         this.airBlocks.add(themeBlock);
      }

      if (json.has("fillEntranceAndAboveWithAir")) {
         this.fillEntranceAndAboveWithAir = json.get("fillEntranceAndAboveWithAir").getAsBoolean();
      }
   }

   public BlockState getFloor(SectorInstance sectorInstance, char patternChar, Random random) {
      return this.getBlockState(sectorInstance, patternChar, random, this.floorBlocks);
   }

   public BlockState getWall(SectorInstance sectorInstance, char patternChar, Random random) {
      return this.getBlockState(sectorInstance, patternChar, random, this.wallBlocks);
   }

   public BlockState getCeiling(SectorInstance sectorInstance, char patternChar, Random random) {
      return this.getBlockState(sectorInstance, patternChar, random, this.ceilingBlocks);
   }

   public BlockState getLight(char patternChar, Random random) {
      return this.getBlockState(null, patternChar, random, this.lightBlocks);
   }

   public BlockState getTorch(char patternChar, Random random) {
      return this.getBlockState(null, patternChar, random, this.torchBlocks);
   }

   public BlockState getStairs(char patternChar, Random random) {
      return this.getBlockState(null, patternChar, random, this.stairBlocks);
   }

   public BlockState getPit(char patternChar, Random random) {
      return this.getBlockState(null, patternChar, random, this.pitBlocks);
   }

   public BlockState getAir(char patternChar, Random random) {
      return this.getBlockState(null, patternChar, random, this.airBlocks);
   }

   public BlockState getBlockState(SectorInstance sectorInstance, char patternChar, Random random, List<ThemeBlock> blockList) {
      if (patternChar == '0') {
         return null;
      } else {
         if (sectorInstance != null) {
            if (patternChar == 'L') {
               return sectorInstance.lightBlock;
            }

            if (patternChar == 'T') {
               return sectorInstance.torchBlock;
            }

            if (patternChar == 'R') {
               return sectorInstance.stairBlock;
            }

            if (patternChar == 'P') {
               return sectorInstance.pitBlock;
            }

            if (patternChar == 'A') {
               return sectorInstance.airBlock;
            }
         }

         if (patternChar == 'C') {
            return Blocks.CHEST.defaultBlockState();
         } else if (patternChar == 'S') {
            return Blocks.SPAWNER.defaultBlockState();
         } else if (blockList.isEmpty()) {
            return null;
         } else if (blockList.size() == 1) {
            return blockList.get(0).getBlockState();
         } else if (NumberUtils.isCreatable("" + patternChar)) {
            int blockIndex = NumberUtils.toInt("" + patternChar, 1) - 1;
            if (blockIndex >= blockList.size()) {
               blockIndex = 0;
            }

            return blockList.get(blockIndex % blockList.size()).getBlockState();
         } else {
            int totalWeights = 0;

            for (ThemeBlock themeBlock : blockList) {
               if (themeBlock != null && themeBlock.weight >= 1) {
                  totalWeights += themeBlock.weight;
               }
            }

            int randomWeight = random.nextInt(totalWeights) + 1;
            int searchedWeight = 0;

            for (ThemeBlock themeBlockx : blockList) {
               if (themeBlockx != null && themeBlockx.weight >= 1) {
                  if (randomWeight <= themeBlockx.weight + searchedWeight) {
                     return themeBlockx.getBlockState();
                  }

                  searchedWeight += themeBlockx.weight;
               }
            }

            return blockList.get(blockList.size() - 1).getBlockState();
         }
      }
   }
}
