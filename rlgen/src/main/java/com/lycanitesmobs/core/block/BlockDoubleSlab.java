package com.lycanitesmobs.core.block;

import com.lycanitesmobs.compat.Material;
import com.lycanitesmobs.core.info.ModInfo;

/** 1.12 BlockDoubleSlab (a pillar block that drops two of its slab; drops come from the loot table). */
public class BlockDoubleSlab extends BlockPillar {
   protected String slabName;

   public BlockDoubleSlab(Material material, ModInfo group, String name, String slabName) {
      super(material, group, name);
      this.slabName = slabName;
   }
}
