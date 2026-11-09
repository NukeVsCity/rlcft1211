package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.IafNames;
import net.minecraft.world.item.Item;

/** 1.12 ItemGeneric: a plain item with a 1.12 translation key. */
public class ItemGeneric extends Item {
   public ItemGeneric(String name) {
      this(name, new Properties());
   }

   public ItemGeneric(String name, Properties props) {
      super(props);
      IafNames.item(this, name);
   }
}
