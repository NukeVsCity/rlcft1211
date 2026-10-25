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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DungeonSector {
   public String name = "";
   public String type = "room";
   public int weight = 8;
   public boolean changeTheme = false;
   public Vec3i sizeMin = new Vec3i(8, 8, 8);
   public Vec3i sizeMax = new Vec3i(10, 10, 10);
   public Vec3i padding = new Vec3i(1, 0, 1);
   public List<String> structures = new ArrayList<>();
   public SectorSegment floor;
   public SectorSegment wall;
   public SectorSegment ceiling;

   public void loadFromJSON(JsonObject json) {
      this.name = json.get("name").getAsString().toLowerCase();
      if (json.has("type")) {
         this.type = json.get("type").getAsString().toLowerCase();
      }

      if (json.has("changeTheme")) {
         this.changeTheme = json.get("changeTheme").getAsBoolean();
      }

      this.sizeMin = JSONHelper.getVec3i(json, "sizeMin");
      this.sizeMax = JSONHelper.getVec3i(json, "sizeMax");
      if (json.has("weight")) {
         this.weight = json.get("weight").getAsInt();
      }

      if (json.has("structures")) {
         for (JsonElement jsonElement : json.get("structures").getAsJsonArray()) {
            String structureName = jsonElement.getAsString();
            if (!this.structures.contains(structureName)) {
               this.structures.add(structureName);
            }
         }
      }

      if (json.has("floor")) {
         this.floor = new SectorSegment();
         this.floor.loadFromJSON(json.get("floor"));
      }

      if (json.has("wall")) {
         this.wall = new SectorSegment();
         this.wall.loadFromJSON(json.get("wall"));
      }

      if (json.has("ceiling")) {
         this.ceiling = new SectorSegment();
         this.ceiling.loadFromJSON(json.get("ceiling"));
      }

      this.padding = JSONHelper.getVec3i(json, "padding");
      if (this.padding.getX() <= 0) {
         this.padding = new Vec3i(this.wall.padding, this.padding.getY(), this.padding.getZ());
      }

      if (this.padding.getZ() <= 0) {
         this.padding = new Vec3i(this.padding.getX(), this.padding.getY(), this.wall.padding);
      }
   }

   public Vec3i getRandomSize(Random random) {
      int x = this.sizeMin.getX();
      if (this.sizeMax.getX() > x) {
         x += random.nextInt(this.sizeMax.getX() - x + 1);
      }

      int y = this.sizeMin.getY();
      if (this.sizeMax.getY() > y) {
         y += random.nextInt(this.sizeMax.getY() - y + 1);
      }

      int z = this.sizeMin.getZ();
      if (this.sizeMax.getZ() > z) {
         z += random.nextInt(this.sizeMax.getZ() - z + 1);
      }

      return new Vec3i(x, y, z);
   }
}
