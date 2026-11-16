package com.lycanitesmobs.core.tileentity;

import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.compat.LegacyInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.12 TileEntityBase (TileEntity, ITickable, ISidedInventory). 1.21: the type comes from ObjectManager (registered for this
 * class), update() is ticked by the block's ticker, readFromNBT/writeToNBT keep their 1.12 names and the 1.12 update packet
 * (getUpdateTag / onDataPacket) is the 1.21 block entity data packet.
 */
public abstract class TileEntityBase extends BlockEntity implements LegacyInventory.Sided {
   protected boolean destroyed = false;
   /** 1.12 TileEntity.world/pos fields (1.21: level / worldPosition). */

   /** The type being constructed (set by ObjectManager's block entity factory: 1.12 tile entities had no type). */
   public static final ThreadLocal<BlockEntityType<?>> CONSTRUCTING = new ThreadLocal<>();

   public TileEntityBase(BlockPos pos, BlockState state) {
      super(CONSTRUCTING.get(), pos, state);
   }

   protected TileEntityBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }

   @Override
   public BlockEntityType<?> getType() {
      BlockEntityType<?> type = ObjectManager.getTileEntityType(this.getClass());
      return type != null ? type : super.getType();
   }

   public Level getWorld() {
      return this.level;
   }

   public BlockPos getPos() {
      return this.worldPosition;
   }

   public void onRemove() {
      this.destroyed = true;
   }

   @Override
   public void setRemoved() {
      super.setRemoved();
      this.onRemove();
   }

   /** 1.12 ITickable.update */
   public void update() {
   }

   public boolean receiveClientEvent(int eventID, int eventArg) {
      return false;
   }

   @Override
   public boolean triggerEvent(int eventID, int eventArg) {
      return this.receiveClientEvent(eventID, eventArg);
   }

   /** 1.12 getUpdatePacket: the tag sent to clients (null for none). */
   public CompoundTag getUpdateTag112() {
      return null;
   }

   /** 1.12 onDataPacket: the tag received by the client. */
   public void onDataPacket112(CompoundTag syncData) {
   }

   @Override
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return this.getUpdateTag112() == null ? null : ClientboundBlockEntityDataPacket.create(this, (be, registries) -> this.getUpdateTag112());
   }

   @Override
   public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
      this.onDataPacket112(pkt.getTag());
   }

   @Override
   public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
      CompoundTag tag = this.getUpdateTag112();
      return tag != null ? tag : super.getUpdateTag(registries);
   }

   public void onGuiButton(byte buttonId) {
   }

   public void readFromNBT(CompoundTag nbtTagCompound) {
   }

   public CompoundTag writeToNBT(CompoundTag nbtTagCompound) {
      return nbtTagCompound;
   }

   @Override
   protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
      super.loadAdditional(tag, registries);
      this.readFromNBT(tag);
   }

   @Override
   protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
      super.saveAdditional(tag, registries);
      this.writeToNBT(tag);
   }

   public void markDirty() {
      this.setChanged();
   }

   /** 1.12 getGUI(player): the container (1.21 menu) on the server; the client screen is made by lyc/client. */
   public Object getGUI(Player player) {
      return null;
   }

   @Override
   public boolean isUsableByPlayer(Player player) {
      return this.destroyed ? false : this.getPos().distSqr(player.blockPosition()) < 16.0;
   }

   @Override
   public int[] getSlotsForFace(Direction side) {
      return new int[0];
   }

   @Override
   public boolean canInsertItem(int index, ItemStack itemStackIn, Direction direction) {
      return false;
   }

   @Override
   public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
      return false;
   }
}
