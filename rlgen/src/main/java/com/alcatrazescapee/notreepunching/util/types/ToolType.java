package com.alcatrazescapee.notreepunching.util.types;

/** 1.12 ToolType */
public enum ToolType {
   PICKAXE(true, false),
   AXE(true, false),
   SHOVEL(true, false),
   HOE(true, false),
   KNIFE(true, true),
   MATTOCK(false, true),
   SAW(false, true);

   public final boolean isFlintTool;
   public final boolean isNewTool;

   ToolType(boolean isFlintTool, boolean isNewTool) {
      this.isFlintTool = isFlintTool;
      this.isNewTool = isNewTool;
   }
}
