package net.ilexiconn.llibrary.server.capability;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/** LLibrary 1.7.20 IEntityDataCapability (now the value of the llibrary:entity_data attachment). */
public interface IEntityDataCapability {
   void init(Entity entity, Level world, boolean init);

   void saveToNBT(CompoundTag compound);

   void loadFromNBT(CompoundTag compound);

   <T extends Entity> void registerData(IEntityData<T> data);

   @Nullable
   <T extends Entity> IEntityData<T> getData(String identifier);

   <T extends Entity> List<IEntityData<T>> getData();
}
