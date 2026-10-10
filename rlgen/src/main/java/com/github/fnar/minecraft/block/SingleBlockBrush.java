package com.github.fnar.minecraft.block;

import com.google.gson.JsonElement;
import greymerk.roguelike.dungeon.settings.DungeonSettingParseException;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;

public class SingleBlockBrush implements BlockBrush {
   public static final SingleBlockBrush AIR = BlockType.AIR.getBrush();
   private BlockType blockType;
   private Direction facing = Direction.EAST;
   private JsonElement json;
   private Material material;

   public SingleBlockBrush(BlockType blockType) {
      this(blockType, Material.NONE);
   }

   public SingleBlockBrush(BlockType blockType, Material material) {
      this.blockType = blockType;
      this.material = material;
   }

   public SingleBlockBrush(JsonElement jsonElement) throws DungeonSettingParseException {
      this.json = jsonElement;
   }

   @Override
   public boolean stroke(WorldEditor editor, Coord pos, boolean fillAir, boolean replaceSolid) {
      return editor.setBlock(pos, this, fillAir, replaceSolid);
   }

   @Override
   public BlockBrush setFacing(Direction facing) {
      this.facing = facing;
      return this;
   }

   public SingleBlockBrush copy() {
      SingleBlockBrush copy = new SingleBlockBrush(this.blockType, this.material);
      copy.setFacing(this.facing);
      copy.setJson(this.json);
      return copy;
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof SingleBlockBrush)) {
         return false;
      } else {
         SingleBlockBrush other = (SingleBlockBrush)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$blockType = this.getBlockType();
            Object other$blockType = other.getBlockType();
            if (this$blockType == null ? other$blockType == null : this$blockType.equals(other$blockType)) {
               Object this$facing = this.getFacing();
               Object other$facing = other.getFacing();
               if (this$facing == null ? other$facing == null : this$facing.equals(other$facing)) {
                  Object this$json = this.getJson();
                  Object other$json = other.getJson();
                  if (this$json == null ? other$json == null : this$json.equals(other$json)) {
                     Object this$material = this.getMaterial();
                     Object other$material = other.getMaterial();
                     return this$material == null ? other$material == null : this$material.equals(other$material);
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         }
      }
   }

   protected boolean canEqual(Object other) {
      return other instanceof SingleBlockBrush;
   }

   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $blockType = this.getBlockType();
      result = result * 59 + ($blockType == null ? 43 : $blockType.hashCode());
      Object $facing = this.getFacing();
      result = result * 59 + ($facing == null ? 43 : $facing.hashCode());
      Object $json = this.getJson();
      result = result * 59 + ($json == null ? 43 : $json.hashCode());
      Object $material = this.getMaterial();
      return result * 59 + ($material == null ? 43 : $material.hashCode());
   }

   public BlockType getBlockType() {
      return this.blockType;
   }

   public Direction getFacing() {
      return this.facing;
   }

   public JsonElement getJson() {
      return this.json;
   }

   public Material getMaterial() {
      return this.material;
   }

   public void setBlockType(BlockType blockType) {
      this.blockType = blockType;
   }

   public void setJson(JsonElement json) {
      this.json = json;
   }

   public void setMaterial(Material material) {
      this.material = material;
   }

   @Override
   public String toString() {
      return "SingleBlockBrush(blockType="
         + this.getBlockType()
         + ", facing="
         + this.getFacing()
         + ", json="
         + this.getJson()
         + ", material="
         + this.getMaterial()
         + ")";
   }
}
