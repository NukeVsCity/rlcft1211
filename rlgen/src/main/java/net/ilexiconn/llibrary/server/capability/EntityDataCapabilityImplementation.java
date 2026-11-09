package net.ilexiconn.llibrary.server.capability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/** LLibrary 1.7.20 EntityDataCapabilityImplementation. */
@SuppressWarnings({"rawtypes", "unchecked"})
public class EntityDataCapabilityImplementation implements IEntityDataCapability {
   private final Map<String, IEntityData> attachedData;
   private final List<IEntityData> attachedDataList;

   public EntityDataCapabilityImplementation(List<IEntityData> data) {
      this.attachedData = new LinkedHashMap<>(data.size());
      this.attachedDataList = new ArrayList<>(data.size());

      for (IEntityData d : data) {
         this.attachedData.put(d.getID(), d);
         this.attachedDataList.add(d);
      }
   }

   public EntityDataCapabilityImplementation() {
      this(new ArrayList<>());
   }

   @Override
   public void init(Entity entity, Level world, boolean init) {
      if (init) {
         for (IEntityData entityData : this.attachedData.values()) {
            entityData.init(entity, world);
         }
      }
   }

   @Override
   public void saveToNBT(CompoundTag compound) {
      for (IEntityData entityData : this.attachedData.values()) {
         CompoundTag managerTag = new CompoundTag();
         entityData.saveNBTData(managerTag);
         compound.put(entityData.getID(), managerTag);
      }
   }

   @Override
   public void loadFromNBT(CompoundTag compound) {
      for (IEntityData entityData : this.attachedData.values()) {
         CompoundTag managerTag = compound.getCompound(entityData.getID());
         entityData.loadNBTData(managerTag);
      }
   }

   @Override
   public <T extends Entity> void registerData(IEntityData<T> data) {
      this.attachedData.put(data.getID(), data);
      this.attachedDataList.add(data);
   }

   @Nullable
   @Override
   public <T extends Entity> IEntityData<T> getData(String identifier) {
      return this.attachedData.get(identifier);
   }

   @Override
   public <T extends Entity> List<IEntityData<T>> getData() {
      return (List)Collections.unmodifiableList(this.attachedDataList);
   }

   public boolean isEmpty() {
      return this.attachedDataList.isEmpty();
   }
}
