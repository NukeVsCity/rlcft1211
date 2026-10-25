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

public class ThemeBlock {
   protected Block block = null;
   public String blockId;
   public int metadata = 0;
   public int weight = 8;

   public void loadFromJSON(JsonObject json) {
      if (json.has("blockId")) {
         this.blockId = json.get("blockId").getAsString();
      }

      if (json.has("metadata")) {
         this.metadata = json.get("metadata").getAsInt();
      }

      if (json.has("weight")) {
         this.weight = json.get("weight").getAsInt();
      }
   }

   public BlockState getBlockState() {
      if (this.block == null) {
         this.block = LycBlocks.block(this.blockId);
      }

      if (this.block == null) {
         return Blocks.AIR.defaultBlockState();
      } else {
         return this.block.defaultBlockState();
      }
   }
}
