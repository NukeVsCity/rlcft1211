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
import java.util.Collections;
import java.util.List;

public class SectorLayer {
   public List<List<Character>> rows = new ArrayList<>();
   public boolean tileHorizontal = true;
   public boolean tileVertical = true;
   public boolean centerHorizontal = false;
   public boolean centerVertical = false;
   public boolean tileSpawners = false;
   public boolean tileChests = false;

   public void loadFromJSON(JsonObject json) {
      if (json.has("tileHorizontal")) {
         this.tileHorizontal = json.get("tileHorizontal").getAsBoolean();
      }

      if (json.has("tileVertical")) {
         this.tileVertical = json.get("tileVertical").getAsBoolean();
      }

      if (json.has("centerHorizontal")) {
         this.centerHorizontal = json.get("centerHorizontal").getAsBoolean();
      }

      if (json.has("centerVertical")) {
         this.centerVertical = json.get("centerVertical").getAsBoolean();
      }

      if (json.has("tileSpawners")) {
         this.tileSpawners = json.get("tileSpawners").getAsBoolean();
      }

      if (json.has("tileChests")) {
         this.tileChests = json.get("tileChests").getAsBoolean();
      }

      this.rows.clear();

      for (JsonElement rowElement : json.get("pattern").getAsJsonArray()) {
         List<Character> columns = new ArrayList<>();

         for (char patternChar : rowElement.getAsString().toCharArray()) {
            columns.add(patternChar);
         }

         this.rows.add(columns);
      }

      Collections.reverse(this.rows);
   }

   public List<Character> getRow(int vertical, int verticalLength) {
      if (this.rows.isEmpty()) {
         return new ArrayList<>();
      } else {
         int offset = 0;
         if (this.centerVertical) {
            offset += Math.round((float)verticalLength / 2.0F);
            offset -= Math.round((float)this.rows.size() / 2.0F);
         }

         List<Character> row = this.rows.get(Math.floorMod(vertical - offset, this.rows.size()));
         if (!this.tileVertical) {
            if (vertical < offset) {
               return this.rows.get(0);
            }

            if (vertical > offset + this.rows.size() - 1) {
               return this.rows.get(this.rows.size() - 1);
            }
         }

         return row;
      }
   }

   public Character getColumn(int vertical, int verticalLength, int horizontal, int horizontalLength, List<Character> row) {
      if (row == null) {
         row = this.getRow(vertical, verticalLength);
      }

      if (row.isEmpty()) {
         return '0';
      } else {
         int verticalOffset = 0;
         int horizontalOffset = 0;
         if (this.centerVertical) {
            verticalOffset += Math.round((float)verticalLength / 2.0F);
            verticalOffset -= Math.round((float)this.rows.size() / 2.0F);
         }

         if (this.centerHorizontal) {
            horizontalOffset += Math.round((float)horizontalLength / 2.0F);
            horizontalOffset -= Math.round((float)row.size() / 2.0F);
         }

         Character column = row.get(Math.floorMod(horizontal - horizontalOffset, row.size()));
         if (vertical < verticalOffset
            || vertical > verticalOffset + this.rows.size() - 1
            || horizontal < horizontalOffset
            || horizontal > horizontalOffset + row.size() - 1) {
            if (!this.tileHorizontal) {
               if (horizontal < horizontalOffset) {
                  column = row.get(0);
               }

               if (horizontal > horizontalOffset + row.size()) {
                  column = row.get(row.size() - 1);
               }
            }

            if (!this.tileSpawners && column == 'S') {
               return '0';
            }

            if (!this.tileChests && column == 'C') {
               return '0';
            }
         }

         return column;
      }
   }
}
