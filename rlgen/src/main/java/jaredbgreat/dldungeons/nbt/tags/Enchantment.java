package jaredbgreat.dldungeons.nbt.tags;

import jaredbgreat.dldungeons.nbt.NBTType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class Enchantment extends ITag {
   private short id;
   private short lvl;

   protected Enchantment(String label, String name, String data) {
      super(label, name);
      this.id = Short.parseShort(name);
      this.lvl = Short.parseShort(data);
   }

   @Override
   public void write(CompoundTag cmp) {
      if (!cmp.contains("ench", 9)) {
         cmp.put("ench", new ListTag());
      }

      ListTag list = cmp.getList("ench", 10);
      CompoundTag sub = new CompoundTag();
      sub.putShort("id", this.id);
      sub.putShort("lvl", this.lvl);
      list.add(sub);
   }

   @Override
   public void write(ListTag in) {
      CompoundTag sub = new CompoundTag();
      sub.putShort("id", this.id);
      sub.putShort("lvl", this.lvl);
      in.add(sub);
   }

   @Override
   public NBTType getType() {
      return NBTType.COMPOUND;
   }
}
