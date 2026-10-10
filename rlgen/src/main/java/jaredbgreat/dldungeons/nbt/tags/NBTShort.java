package jaredbgreat.dldungeons.nbt.tags;

import jaredbgreat.dldungeons.nbt.NBTType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.ShortTag;

public class NBTShort extends ITag {
   public final short data;

   NBTShort(String label, String name, String data) {
      super(label, name);
      this.data = Short.parseShort(data);
   }

   NBTShort(String label, String name, short data) {
      super(label, name);
      this.data = data;
   }

   @Override
   public void write(CompoundTag cmp) {
      cmp.putShort(this.name, this.data);
   }

   @Override
   public void write(ListTag cmp) {
      cmp.add(ShortTag.valueOf(this.data));
   }

   @Override
   public NBTType getType() {
      return NBTType.SHORT;
   }
}
