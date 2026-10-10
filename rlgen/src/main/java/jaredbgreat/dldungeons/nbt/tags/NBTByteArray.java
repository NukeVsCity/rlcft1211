package jaredbgreat.dldungeons.nbt.tags;

import jaredbgreat.dldungeons.nbt.NBTType;
import java.util.ArrayList;
import java.util.StringTokenizer;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class NBTByteArray extends ITag {
   public final byte[] data;

   NBTByteArray(String label, String name, String data) {
      super(label, name);
      ArrayList<Byte> tmp = this.parseData(data);
      this.data = new byte[tmp.size()];

      for (int i = 0; i < this.data.length; i++) {
         this.data[i] = tmp.get(i);
      }
   }

   NBTByteArray(String label, String name, byte[] data) {
      super(label, name);
      this.data = data;
   }

   @Override
   public void write(CompoundTag cmp) {
      cmp.putByteArray(this.name, this.data);
   }

   @Override
   public void write(ListTag cmp) {
      cmp.add(new ByteArrayTag(this.data));
   }

   private ArrayList<Byte> parseData(String in) {
      StringTokenizer tokens = new StringTokenizer(in, "{,}");
      ArrayList<Byte> tmp = new ArrayList<>();

      while (tokens.hasMoreTokens()) {
         tmp.add(Byte.parseByte(tokens.nextToken()));
      }

      return tmp;
   }

   @Override
   public NBTType getType() {
      return NBTType.BYTE_ARRAY;
   }
}
