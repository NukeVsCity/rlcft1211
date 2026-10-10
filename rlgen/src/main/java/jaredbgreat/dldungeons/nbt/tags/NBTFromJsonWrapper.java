package jaredbgreat.dldungeons.nbt.tags;

import jaredbgreat.dldungeons.nbt.NBTType;
import net.minecraft.nbt.TagParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class NBTFromJsonWrapper extends ITag {
   private CompoundTag wrapped;

   NBTFromJsonWrapper(String label, String data) {
      super(label, label);

      try {
         this.wrapped = TagParser.parseTag(data);
      } catch (CommandSyntaxException var4) {
         System.err.println("Exception reading json-nbt string: " + var4.getMessage());
         this.wrapped = new CompoundTag();
      }
   }

   @Override
   public void write(CompoundTag cmp) {
      cmp.merge(this.wrapped);
   }

   @Override
   public void write(ListTag in) {
      in.add(this.wrapped.copy());
   }

   @Override
   public NBTType getType() {
      return NBTType.COMPOUND;
   }
}
