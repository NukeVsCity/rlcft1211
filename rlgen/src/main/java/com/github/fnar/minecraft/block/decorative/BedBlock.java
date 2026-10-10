package com.github.fnar.minecraft.block.decorative;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.SingleBlockBrush;
import com.github.fnar.minecraft.block.normal.ColoredBlock;
import greymerk.roguelike.config.RogueConfig;
import greymerk.roguelike.util.DyeColor;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.WorldEditor;

public class BedBlock extends SingleBlockBrush {
   private DyeColor color = DyeColor.RED;
   private boolean isHead = true;

   public BedBlock() {
      super(BlockType.BED);
   }

   public BedBlock setColor(DyeColor color) {
      this.color = color;
      return this;
   }

   public DyeColor getColor() {
      return this.color;
   }

   public boolean isHead() {
      return this.isHead;
   }

   public BedBlock setHead(boolean head) {
      this.isHead = head;
      return this;
   }

   private BedBlock getFoot() {
      BedBlock bedBlock = bed().setColor(this.getColor()).setHead(false);
      bedBlock.setFacing(this.getFacing());
      return bedBlock;
   }

   private BedBlock getHead() {
      BedBlock bedBlock = bed().setColor(this.getColor()).setHead(true);
      bedBlock.setFacing(this.getFacing());
      return bedBlock;
   }

   @Override
   public boolean stroke(WorldEditor editor, Coord pos, boolean fillAir, boolean replaceSolid) {
      Coord cursor = pos.copy();
      if (!RogueConfig.FURNITURE.getBoolean()) {
         ColoredBlock.wool().setColor(DyeColor.WHITE).stroke(editor, cursor);
         cursor.translate(this.getFacing());
         ColoredBlock.wool().setColor(DyeColor.RED).stroke(editor, cursor);
         return true;
      } else {
         editor.setBlock(cursor, this, fillAir, replaceSolid);
         editor.setBedColorAt(cursor, this.color);
         cursor.translate(this.getFacing());
         editor.setBlock(cursor, this.getFoot(), fillAir, replaceSolid);
         editor.setBedColorAt(cursor, this.color);
         return true;
      }
   }

   public static BedBlock bed() {
      return new BedBlock();
   }

   public BedBlock copy() {
      BedBlock copy = new BedBlock();
      copy.setFacing(this.getFacing());
      copy.setColor(this.color);
      copy.setHead(this.isHead);
      return copy;
   }
}
