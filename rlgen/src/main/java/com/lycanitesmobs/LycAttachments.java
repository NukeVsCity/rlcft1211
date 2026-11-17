package com.lycanitesmobs;

import com.lycanitesmobs.core.entity.ExtendedEntity;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * 1.12 capabilities lycanitesmobs:IExtendedEntity (every living entity) and lycanitesmobs:IExtendedPlayer as 1.21 data
 * attachments, saved with the same writeNBT/readNBT (ExtendedEntityStorage / ExtendedPlayerStorage). Players keep theirs on
 * death like the 1.12 PlayerEvent.Clone copy.
 */
public final class LycAttachments {
   private static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, LycanitesMobs.modid);

   public static final Supplier<AttachmentType<ExtendedEntity>> EXTENDED_ENTITY = TYPES.register("extended_entity",
      () -> AttachmentType.builder(holder -> new ExtendedEntity()).serialize(new IAttachmentSerializer<CompoundTag, ExtendedEntity>() {
         @Override
         public ExtendedEntity read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
            ExtendedEntity ext = new ExtendedEntity();
            ext.readNBT(tag);
            return ext;
         }

         @Override
         public CompoundTag write(ExtendedEntity ext, HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            ext.writeNBT(tag);
            return tag;
         }
      }).build());

   public static final Supplier<AttachmentType<ExtendedPlayer>> EXTENDED_PLAYER = TYPES.register("extended_player",
      () -> AttachmentType.builder(holder -> new ExtendedPlayer()).serialize(new IAttachmentSerializer<CompoundTag, ExtendedPlayer>() {
         @Override
         public ExtendedPlayer read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
            ExtendedPlayer ext = new ExtendedPlayer();
            ext.readNBT(tag);
            return ext;
         }

         @Override
         public CompoundTag write(ExtendedPlayer ext, HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            ext.writeNBT(tag);
            return tag;
         }
      }).copyOnDeath().build());

   private LycAttachments() {
   }

   public static void init(IEventBus modBus) {
      TYPES.register(modBus);
   }
}
