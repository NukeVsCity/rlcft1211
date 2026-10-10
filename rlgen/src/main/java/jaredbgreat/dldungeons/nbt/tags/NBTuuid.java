package jaredbgreat.dldungeons.nbt.tags;

import jaredbgreat.dldungeons.nbt.NBTType;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;

public class NBTuuid extends ITag {
   public final UUID data;

   NBTuuid(String label, String name, String data) {
      super(label, name);
      this.data = UUID.fromString(data);
   }

   NBTuuid(String label, String name, UUID data) {
      super(label, name);
      this.data = data;
   }

   @Override
   public void write(CompoundTag cmp) {
      cmp.putUUID(this.name, this.data);
   }

   @Override
   public void write(ListTag cmp) {
      cmp.add(LongTag.valueOf(this.data.getLeastSignificantBits()));
      cmp.add(LongTag.valueOf(this.data.getMostSignificantBits()));
   }

   @Override
   public NBTType getType() {
      return NBTType.INT;
   }
}
