package jaredbgreat.dldungeons.nbt.tags;

import jaredbgreat.dldungeons.nbt.NBTType;
import jaredbgreat.dldungeons.util.parser.Tokenizer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class NBTCompound extends ITag {
   public final List<ITag> data = new ArrayList<>();

   NBTCompound(String label, String name, String data) {
      super(label, name);
      this.parseData(data);
   }

   @Override
   public void write(CompoundTag in) {
      CompoundTag sub = in.getCompound(this.name);
      in.put(this.name, sub);

      for (ITag child : this.data) {
         child.write(sub);
      }
   }

   @Override
   public void write(ListTag in) {
      CompoundTag sub = new CompoundTag();

      for (ITag child : this.data) {
         child.write(sub);
      }

      in.add(sub);
   }

   private void parseData(String in) {
      Tokenizer tokens = new Tokenizer(in, ",");

      while (tokens.hasMoreTokens()) {
         this.data.add(Tags.registry.get(tokens.nextToken()));
      }
   }

   @Override
   public NBTType getType() {
      return NBTType.COMPOUND;
   }
}
