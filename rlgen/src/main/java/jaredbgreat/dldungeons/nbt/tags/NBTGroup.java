package jaredbgreat.dldungeons.nbt.tags;

import jaredbgreat.dldungeons.nbt.NBTType;
import jaredbgreat.dldungeons.util.parser.Tokenizer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class NBTGroup extends ITag {
   public final List<ITag> data = new ArrayList<>();

   NBTGroup(String label, String name, String data) {
      super(label, name);
      this.parseData(data);
   }

   @Override
   public void write(CompoundTag cmp) {
      for (ITag child : this.data) {
         child.write(cmp);
      }
   }

   @Override
   public void write(ListTag cmp) {
      for (ITag child : this.data) {
         child.write(cmp);
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
      return NBTType.GROUP;
   }
}
