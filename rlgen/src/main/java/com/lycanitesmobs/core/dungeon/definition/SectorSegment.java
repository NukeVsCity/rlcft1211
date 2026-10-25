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
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.math.NumberUtils;

public class SectorSegment {
   public Map<Integer, SectorLayer> layers = new HashMap<>();
   public int padding = 0;

   public void loadFromJSON(JsonElement json) {
      for (JsonElement jsonElement : json.getAsJsonArray()) {
         JsonObject layerJson = jsonElement.getAsJsonObject();
         if (layerJson.has("layer") && layerJson.has("pattern")) {
            String layerNumber = layerJson.get("layer").getAsString();
            if (NumberUtils.isCreatable(layerNumber)) {
               int layerIndex = NumberUtils.createInteger(layerNumber);
               if (layerIndex < 0 && -layerIndex > this.padding) {
                  this.padding = -layerIndex;
               }

               SectorLayer layer = new SectorLayer();
               layer.loadFromJSON(layerJson);
               this.layers.put(layerIndex, layer);
            }
         }
      }
   }
}
