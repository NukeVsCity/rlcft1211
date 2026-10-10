package jaredbgreat.dldungeons.nbt.tags;

import jaredbgreat.dldungeons.nbt.NBTType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;

public class NBTLong extends ITag {
   public final long data;

   NBTLong(String label, String name, String data) {
      super(label, name);
      this.data = Long.parseLong(data);
   }

   NBTLong(String label, String name, long data) {
      super(label, name);
      this.data = data;
   }

   @Override
   public void write(CompoundTag cmp) {
      cmp.putLong(this.name, this.data);
   }

   @Override
   public void write(ListTag cmp) {
      cmp.add(LongTag.valueOf(this.data));
   }

   @Override
   public NBTType getType() {
      return NBTType.LONG;
   }
}
