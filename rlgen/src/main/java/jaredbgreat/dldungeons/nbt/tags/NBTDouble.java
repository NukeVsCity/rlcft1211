package jaredbgreat.dldungeons.nbt.tags;

import jaredbgreat.dldungeons.nbt.NBTType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;

public class NBTDouble extends ITag {
   public final double data;

   public NBTDouble(String label, String name, String data) {
      super(label, name);
      this.data = Double.parseDouble(data);
   }

   NBTDouble(String label, String name, double data) {
      super(label, name);
      this.data = data;
   }

   @Override
   public void write(CompoundTag cmp) {
      cmp.putDouble(this.name, this.data);
   }

   @Override
   public void write(ListTag cmp) {
      cmp.add(DoubleTag.valueOf(this.data));
   }

   @Override
   public NBTType getType() {
      return NBTType.DOUBLE;
   }
}
