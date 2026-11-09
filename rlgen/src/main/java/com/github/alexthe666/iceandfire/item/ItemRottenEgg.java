package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.core.ModEntityHooks;

/** 1.12 ItemRottenEgg: thrown, it becomes an EntityCockatriceEgg (iaf/mobs-land). */
public class ItemRottenEgg extends ItemIafThrowable {
   public ItemRottenEgg() {
      super(new Properties(), () -> ModEntityHooks.throwRottenEgg);
      IafNames.item(this, "iceandfire.rotten_egg");
   }
}
