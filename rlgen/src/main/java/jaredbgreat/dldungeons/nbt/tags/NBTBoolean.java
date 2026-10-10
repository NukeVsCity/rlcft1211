package jaredbgreat.dldungeons.nbt.tags;

import jaredbgreat.dldungeons.nbt.NBTType;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class NBTBoolean extends ITag {
   public final boolean data;

   NBTBoolean(String label, String name, String data) {
      super(label, name);
      this.data = Boolean.parseBoolean(data);
   }

   NBTBoolean(String label, String name, boolean data) {
      super(label, name);
      this.data = data;
   }

   @Override
   public void write(CompoundTag cmp) {
      cmp.putBoolean(this.name, this.data);
   }

   @Override
   public void write(ListTag cmp) {
      byte out = 0;
      if (this.data) {
         out = 1;
      }

      ByteTag sub = ByteTag.valueOf(out);
      cmp.add(sub);
   }

   @Override
   public NBTType getType() {
      return NBTType.BOOLEAN;
   }
}
