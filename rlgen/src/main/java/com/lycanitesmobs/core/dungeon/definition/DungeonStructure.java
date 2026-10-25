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

public class DungeonStructure {
   public String name = "";
   public String type = "decoration";
   public int weight = 8;
   public String nbt = "";

   public void loadFromJSON(JsonObject json) {
      this.name = json.get("name").getAsString().toLowerCase();
      if (json.has("type")) {
         this.type = json.get("type").getAsString().toLowerCase();
      }

      if (json.has("nbt")) {
         this.nbt = json.get("nbt").getAsString().toLowerCase();
      }
   }
}
