package jaredbgreat.dldungeons.nbt.tags;

import jaredbgreat.dldungeons.nbt.NBTType;
import jaredbgreat.dldungeons.util.parser.Tokenizer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class NBTList extends ITag {
   private final List<ITag> data = new ArrayList<>();
   private final int type;

   NBTList(String label, String name, String data) {
      super(label, name);
      this.parseData(data);
      this.type = this.data.get(0).getType().ordinal();
   }

   @Override
   public void write(CompoundTag in) {
      if (!in.contains(this.name, this.type)) {
         in.put(this.name, new ListTag());
      }

      for (ITag child : this.data) {
         ListTag list = in.getList(this.name, child.getType().ordinal());
         child.write(list);
      }
   }

   private void writeHelper(CompoundTag in, ITag child) {
   }

   @Override
   public void write(ListTag in) {
      for (ITag child : this.data) {
         child.write(in);
      }
   }

   private void parseData(String in) {
      Tokenizer tokens = new Tokenizer(in, ",");

      while (tokens.hasMoreTokens()) {
         this.data.add(Tags.registry.get(tokens.nextToken()));
      }
   }

   @Override
   public NBTType getType() {
      return NBTType.LIST;
   }
}
