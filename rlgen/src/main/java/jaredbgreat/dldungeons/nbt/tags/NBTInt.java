package jaredbgreat.dldungeons.nbt.tags;

import jaredbgreat.dldungeons.nbt.NBTType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;

public class NBTInt extends ITag {
   public final int data;

   NBTInt(String label, String name, String data) {
      super(label, name);
      this.data = Integer.parseInt(data);
   }

   NBTInt(String label, String name, int data) {
      super(label, name);
      this.data = data;
   }

   @Override
   public void write(CompoundTag cmp) {
      cmp.putInt(this.name, this.data);
   }

   @Override
   public void write(ListTag cmp) {
      cmp.add(IntTag.valueOf(this.data));
   }

   @Override
   public NBTType getType() {
      return NBTType.INT;
   }
}
