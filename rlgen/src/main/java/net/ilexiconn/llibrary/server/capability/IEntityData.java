package net.ilexiconn.llibrary.server.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/** LLibrary 1.7.20 IEntityData. */
public interface IEntityData<T extends Entity> {
   void init(T entity, Level world);

   void saveNBTData(CompoundTag compound);

   void loadNBTData(CompoundTag compound);

   String getID();
}
