package jaredbgreat.dldungeons.nbt.tags;

import jaredbgreat.dldungeons.nbt.NBTType;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class NBTByte extends ITag {
   public final byte data;

   NBTByte(String label, String name, String data) {
      super(label, name);
      this.data = Byte.parseByte(data);
   }

   NBTByte(String label, String name, byte data) {
      super(label, name);
      this.data = data;
   }

   @Override
   public void write(CompoundTag cmp) {
      cmp.putByte(this.name, this.data);
   }

   @Override
   public void write(ListTag cmp) {
      cmp.add(ByteTag.valueOf(this.data));
   }

   @Override
   public NBTType getType() {
      return NBTType.BYTE;
   }
}
