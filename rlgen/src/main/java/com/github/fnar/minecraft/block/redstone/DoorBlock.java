package com.github.fnar.minecraft.block.redstone;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.Material;
import com.github.fnar.minecraft.block.SingleBlockBrush;
import com.google.gson.JsonElement;
import greymerk.roguelike.dungeon.settings.DungeonSettingParseException;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.WorldEditor;

public class DoorBlock extends SingleBlockBrush {
   private boolean isOpen;
   private boolean isTop;
   private boolean isHingeLeft;

   public DoorBlock(BlockType blockType, Material material) {
      super(blockType, material);
   }

   public DoorBlock(JsonElement jsonElement) throws DungeonSettingParseException {
      super(jsonElement);
   }

   public DoorBlock setOpen() {
      this.isOpen = true;
      return this;
   }

   public DoorBlock setTop() {
      this.isTop = true;
      return this;
   }

   public boolean isTop() {
      return this.isTop;
   }

   public DoorBlock setHingeLeft() {
      this.isHingeLeft = true;
      return this;
   }

   public static DoorBlock iron() {
      return new DoorBlock(BlockType.IRON_DOOR, Material.METAL);
   }

   public static DoorBlock oak() {
      return new DoorBlock(BlockType.OAK_DOOR, Material.WOOD);
   }

   public static DoorBlock birch() {
      return new DoorBlock(BlockType.BIRCH_DOOR, Material.WOOD);
   }

   public static DoorBlock spruce() {
      return new DoorBlock(BlockType.SPRUCE_DOOR, Material.WOOD);
   }

   public static DoorBlock jungle() {
      return new DoorBlock(BlockType.JUNGLE_DOOR, Material.WOOD);
   }

   public static DoorBlock acacia() {
      return new DoorBlock(BlockType.ACACIA_DOOR, Material.WOOD);
   }

   public static DoorBlock darkOak() {
      return new DoorBlock(BlockType.DARK_OAK_DOOR, Material.WOOD);
   }

   public static DoorBlock warpedDoor() {
      return new DoorBlock(BlockType.WARPED_DOOR, Material.WOOD);
   }

   private DoorBlock getTop() {
      DoorBlock topBlock = new DoorBlock(this.getBlockType(), this.getMaterial()).setTop();
      topBlock.setJson(this.getJson());
      if (this.isOpen()) {
         topBlock.setOpen();
      }

      if (this.isHingeLeft()) {
         topBlock.setHingeLeft();
      }

      topBlock.setFacing(this.getFacing());
      return topBlock;
   }

   @Override
   public boolean stroke(WorldEditor worldEditor, Coord pos, boolean fillAir, boolean replaceSolid) {
      worldEditor.setBlock(pos, this, fillAir, replaceSolid);
      if (!this.isTop()) {
         Coord above = pos.copy().up();
         worldEditor.setBlock(above, this.getTop(), fillAir, replaceSolid);
      }

      return true;
   }

   public DoorBlock copy() {
      JsonElement json = this.getJson();
      DoorBlock copy = json != null ? new DoorBlock(json) : new DoorBlock(this.getBlockType(), this.getMaterial());
      copy.setFacing(this.getFacing());
      copy.setHingeLeft(this.isHingeLeft);
      copy.setOpen(this.isOpen);
      copy.setTop(this.isTop);
      return copy;
   }

   public boolean isOpen() {
      return this.isOpen;
   }

   public boolean isHingeLeft() {
      return this.isHingeLeft;
   }

   public void setOpen(boolean isOpen) {
      this.isOpen = isOpen;
   }

   public void setTop(boolean isTop) {
      this.isTop = isTop;
   }

   public void setHingeLeft(boolean isHingeLeft) {
      this.isHingeLeft = isHingeLeft;
   }
}
