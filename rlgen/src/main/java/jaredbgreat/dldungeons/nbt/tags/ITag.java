package jaredbgreat.dldungeons.nbt.tags;

import jaredbgreat.dldungeons.nbt.NBTType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public abstract class ITag {
   protected final String label;
   protected final String name;

   protected ITag(String label, String name) {
      this.label = label;
      this.name = name;
   }

   public abstract void write(CompoundTag var1);

   public abstract void write(ListTag var1);

   public abstract NBTType getType();

   @Override
   public int hashCode() {
      return this.label.hashCode();
   }

   @Override
   public boolean equals(Object other) {
      if (!(other instanceof ITag)) {
         return false;
      } else {
         ITag o = (ITag)other;
         return o.label.equals(this.label);
      }
   }
}
