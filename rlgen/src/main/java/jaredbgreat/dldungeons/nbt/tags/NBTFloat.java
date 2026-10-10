package jaredbgreat.dldungeons.nbt.tags;

import jaredbgreat.dldungeons.nbt.NBTType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;

public class NBTFloat extends ITag {
   public final float data;

   NBTFloat(String label, String name, String data) {
      super(label, name);
      this.data = Float.parseFloat(data);
   }

   NBTFloat(String label, String name, float data) {
      super(label, name);
      this.data = data;
   }

   @Override
   public void write(CompoundTag cmp) {
      cmp.putFloat(this.name, this.data);
   }

   @Override
   public void write(ListTag cmp) {
      cmp.add(FloatTag.valueOf(this.data));
   }

   @Override
   public NBTType getType() {
      return NBTType.FLOAT;
   }
}
