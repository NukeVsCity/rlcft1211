package jaredbgreat.dldungeons.nbt;

import jaredbgreat.dldungeons.nbt.tags.ITag;
import jaredbgreat.dldungeons.nbt.tags.Tags;
import jaredbgreat.dldungeons.util.parser.Tokenizer;
import net.minecraft.nbt.CompoundTag;

/** rlgen: tags are written into the 1.12 item NBT compound, which LegacyItemNbt turns into 1.21 components. */
public class NBTHelper {
   public static void setNbtTag(CompoundTag item, ITag tag) {
      if (tag == null) {
         System.err.println("ERROR! null tag on item");
      } else {
         tag.write(item);
      }
   }

   public static ITag getTagFromLabel(String label) {
      return Tags.getTag(label);
   }

   public static ITag parseNBTLine(String line) {
      Tokenizer tokens = new Tokenizer(line, " \t");
      return Tags.makeITag(line, tokens);
   }
}
