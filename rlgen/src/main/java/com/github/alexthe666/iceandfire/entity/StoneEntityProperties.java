package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IafRegistries;
import com.github.alexthe666.iceandfire.message.MessageStoneStatue;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 1.12 StoneEntityProperties (an LLibrary EntityProperties on every EntityLiving), as a NeoForge data attachment. Saved with the
 * 1.12 tag names; the stone flag is sent to tracking clients when it changes and when a player starts tracking the mob
 * (LLibrary re-sent it every 20 ticks).
 */
public class StoneEntityProperties {
   public boolean isStone;
   public int breakLvl;

   public static final Supplier<AttachmentType<StoneEntityProperties>> TYPE = IafRegistries.ATTACHMENT_TYPES
      .register("stone", () -> AttachmentType.builder(StoneEntityProperties::new).serialize(new IAttachmentSerializer<CompoundTag, StoneEntityProperties>() {
         @Override
         public StoneEntityProperties read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
            StoneEntityProperties p = new StoneEntityProperties();
            p.loadNBTData(tag);
            return p;
         }

         @Override
         public CompoundTag write(StoneEntityProperties p, HolderLookup.Provider provider) {
            if (!p.isStone && p.breakLvl == 0) {
               return null;
            }
            CompoundTag tag = new CompoundTag();
            p.saveNBTData(tag);
            return tag;
         }
      }).build());

   public static void init() {
   }

   public void saveNBTData(CompoundTag compound) {
      compound.putBoolean("TurnedToStone", this.isStone);
      compound.putInt("StoneBreakLvl", this.breakLvl);
   }

   public void loadNBTData(CompoundTag compound) {
      this.isStone = compound.getBoolean("TurnedToStone");
      this.breakLvl = compound.getInt("StoneBreakLvl");
   }

   /** EntityPropertiesHandler.getProperties(entity, StoneEntityProperties.class): null unless the entity is an EntityLiving (Mob). */
   @Nullable
   public static StoneEntityProperties get(@Nullable Entity entity) {
      return entity instanceof Mob ? entity.getData(TYPE) : null;
   }

   public static boolean isStone(@Nullable Entity entity) {
      return entity instanceof Mob && entity.hasData(TYPE) && entity.getData(TYPE).isStone;
   }

   /** Sets the flag and sends MessageStoneStatue to every client tracking the entity (1.12 sent it to all). */
   public static void setStone(Entity entity, boolean stone) {
      StoneEntityProperties p = get(entity);
      if (p != null) {
         p.isStone = stone;
         if (!entity.level().isClientSide) {
            PacketDistributor.sendToPlayersTrackingEntity(entity, new MessageStoneStatue(entity.getId(), stone));
         }
      }
   }
}
