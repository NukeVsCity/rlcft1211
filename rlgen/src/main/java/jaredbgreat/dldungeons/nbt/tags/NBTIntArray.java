package jaredbgreat.dldungeons.nbt.tags;

import jaredbgreat.dldungeons.nbt.NBTType;
import java.util.ArrayList;
import java.util.StringTokenizer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;

public class NBTIntArray extends ITag {
   public final int[] data;

   NBTIntArray(String label, String name, String data) {
      super(label, name);
      ArrayList<Integer> tmp = this.parseData(data);
      this.data = new int[tmp.size()];

      for (int i = 0; i < this.data.length; i++) {
         this.data[i] = tmp.get(i);
      }
   }

   NBTIntArray(String label, String name, int[] data) {
      super(label, name);
      this.data = data;
   }

   @Override
   public void write(CompoundTag cmp) {
      cmp.putIntArray(this.name, this.data);
   }

   @Override
   public void write(ListTag cmp) {
      cmp.add(new IntArrayTag(this.data));
   }

   private ArrayList<Integer> parseData(String in) {
      StringTokenizer tokens = new StringTokenizer(in, "{,}");
      ArrayList<Integer> tmp = new ArrayList<>();

      while (tokens.hasMoreTokens()) {
         tmp.add(Integer.getInteger(tokens.nextToken()));
      }

      return tmp;
   }

   @Override
   public NBTType getType() {
      return NBTType.INT_ARRAY;
   }
}
