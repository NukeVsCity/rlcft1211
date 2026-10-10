package jaredbgreat.dldungeons.nbt.tags;

import jaredbgreat.dldungeons.nbt.NBTType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

public class NBTString extends ITag {
   public final String data;

   NBTString(String label, String name, String data) {
      super(label, name);
      this.data = data;
   }

   @Override
   public void write(CompoundTag cmp) {
      cmp.putString(this.name, this.data);
   }

   @Override
   public void write(ListTag cmp) {
      cmp.add(StringTag.valueOf(this.data));
   }

   @Override
   public NBTType getType() {
      return NBTType.STRING;
   }
}
