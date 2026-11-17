package com.charles445.simpledifficulty.tileentity;

import com.charles445.simpledifficulty.api.SDBlocks;
import com.charles445.simpledifficulty.block.BlockCampfire;
import com.charles445.simpledifficulty.config.ModConfig;
import com.charles445.simpledifficulty.util.DamageUtil;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

/** 1.12 TileEntitySpit: CampfireSpitSize single-item slots, cooks every edible smelting result after CampfireSpitDelay seconds. */
public class TileEntitySpit extends BlockEntity {
   public final ItemHandler items;
   private int progress = 0;
   private double experience = 0.0;
   private int timer = 0;

   public TileEntitySpit(BlockPos pos, BlockState state) {
      super(SDBlocks.SPIT_TILE.get(), pos, state);
      this.items = new ItemHandler(ModConfig.server.miscellaneous.campfireSpitSize);
   }

   public void update() {
      if (this.timer % 20 == 0) {
         this.timer = 0;
         this.secondUpdate();
      }
      this.timer++;
   }

   private void secondUpdate() {
      if (this.shouldCook()) {
         this.progress++;
         if (this.progress >= ModConfig.server.miscellaneous.campfireSpitDelay) {
            this.cookFood();
            this.progress = 0;
         }
      }
   }

   private Optional<RecipeHolder<SmeltingRecipe>> recipe(ItemStack stack) {
      return this.level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), this.level);
   }

   private ItemStack smeltingResult(ItemStack stack) {
      return this.recipe(stack).map(r -> r.value().getResultItem(this.level.registryAccess())).orElse(ItemStack.EMPTY);
   }

   private void cookFood() {
      for (int i = 0; i < this.items.getSlots(); i++) {
         ItemStack stack = this.items.getStackInSlot(i);
         if (this.isCookable(stack)) {
            Optional<RecipeHolder<SmeltingRecipe>> r = this.recipe(stack);
            ItemStack result = r.map(h -> h.value().getResultItem(this.level.registryAccess())).orElse(ItemStack.EMPTY).copy();
            if (ModConfig.server.miscellaneous.campfireSpitExperience) {
               this.experience = this.experience + (double)r.map(h -> h.value().getExperience()).orElse(0.0F);
            }
            this.items.setStackInSlot(i, result);
         }
      }
   }

   private void playWorldSound(Level world, BlockPos pos) {
      DamageUtil.serverPlayBlockSound(world, pos, SoundEvents.PLAYER_ATTACK_NODAMAGE, 0.4F, 0.9F);
   }

   public void handleRightClick(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand) {
      ItemStack heldItemStack = player.getItemInHand(hand);
      boolean rawWithdraw = heldItemStack.isEmpty();
      boolean playedSound = false;
      boolean found = false;
      boolean withdrewToHand = false;
      for (int i = 0; i < this.items.getSlots(); i++) {
         if (this.isCooked(this.items.getStackInSlot(i))) {
            withdrewToHand = this.withdrawFromSlot(player, hand, i);
            this.dumpExperience(world, pos);
            this.playWorldSound(world, pos);
            playedSound = true;
            found = true;
            break;
         }
      }
      if (!withdrewToHand && this.isCookable(heldItemStack)) {
         String heldItemName = BuiltInRegistries.ITEM.getKey(heldItemStack.getItem()).toString();
         boolean isBlacklisted = false;
         for (String s : ModConfig.server.miscellaneous.campfireSpitBlacklist) {
            if (s.equals(heldItemName)) {
               isBlacklisted = true;
               break;
            }
         }
         if (isBlacklisted == ModConfig.server.miscellaneous.campfireSpitBlacklistIsWhitelist) {
            for (int i = 0; i < this.items.getSlots(); i++) {
               if (this.items.getStackInSlot(i).isEmpty()) {
                  this.items.insertItem(i, heldItemStack.copyWithCount(1), false);
                  heldItemStack.shrink(1);
                  this.progress = 0;
                  if (!playedSound) {
                     this.playWorldSound(world, pos);
                     playedSound = true;
                  }
                  found = true;
                  break;
               }
            }
         }
      }
      if (!found && rawWithdraw && player.isShiftKeyDown()) {
         for (int i = 0; i < this.items.getSlots(); i++) {
            if (!this.items.getStackInSlot(i).isEmpty()) {
               this.withdrawFromSlot(player, hand, i);
               if (!playedSound) {
                  this.playWorldSound(world, pos);
               }
               break;
            }
         }
      }
   }

   private boolean withdrawFromSlot(Player player, InteractionHand hand, int slot) {
      ItemStack stack = this.items.extractItem(slot, 1, false);
      if (player.getItemInHand(hand).isEmpty()) {
         player.setItemInHand(hand, stack);
         return true;
      } else {
         if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
         }
         return false;
      }
   }

   private boolean isCookable(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      }
      ItemStack result = this.smeltingResult(stack);
      return !result.isEmpty() && result.has(DataComponents.FOOD);
   }

   private boolean isCooked(ItemStack stack) {
      return !stack.isEmpty() && !this.isCookable(stack);
   }

   private boolean shouldCook() {
      boolean hasItem = false;
      for (int i = 0; i < this.items.getSlots(); i++) {
         if (this.isCookable(this.items.getStackInSlot(i))) {
            hasItem = true;
            break;
         }
      }
      if (!hasItem) {
         return false;
      }
      BlockState state = this.level.getBlockState(this.worldPosition.below());
      return state.getBlock() == SDBlocks.campfire.get() && state.getValue(BlockCampfire.BURNING);
   }

   public void dumpItems(Level world, BlockPos pos) {
      for (int i = 0; i < this.items.getSlots(); i++) {
         ItemStack itemstack = this.items.getStackInSlot(i);
         if (!itemstack.isEmpty()) {
            Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), itemstack.copy());
         }
         this.items.setStackInSlot(i, ItemStack.EMPTY);
      }
   }

   public void dumpExperience(Level world, BlockPos pos) {
      int convExp = (int)this.experience;
      this.experience -= convExp;
      if (convExp > 0 && world instanceof ServerLevel sl) {
         ExperienceOrb.award(sl, net.minecraft.world.phys.Vec3.atCenterOf(pos), convExp);
      }
   }

   @Override
   protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
      super.loadAdditional(compound, registries);
      this.progress = compound.getInt("progress");
      this.items.deserializeNBT(registries, compound.getCompound("items"));
      this.experience = compound.getDouble("experience");
   }

   @Override
   protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
      super.saveAdditional(compound, registries);
      compound.putInt("progress", this.progress);
      compound.put("items", this.items.serializeNBT(registries));
      compound.putDouble("experience", this.experience);
   }

   @Override
   public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
      return this.saveWithoutMetadata(registries);
   }

   @Override
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public void updateClients() {
      if (this.level != null) {
         BlockState state = this.level.getBlockState(this.worldPosition);
         this.level.sendBlockUpdated(this.worldPosition, state, state, 2);
      }
   }

   public NonNullList<ItemStack> stacks() {
      NonNullList<ItemStack> l = NonNullList.withSize(this.items.getSlots(), ItemStack.EMPTY);
      for (int i = 0; i < l.size(); i++) {
         l.set(i, this.items.getStackInSlot(i));
      }
      return l;
   }

   public class ItemHandler extends ItemStackHandler {
      public ItemHandler(int slots) {
         super(slots);
      }

      @Override
      protected void onContentsChanged(int slot) {
         super.onContentsChanged(slot);
         TileEntitySpit.this.setChanged();
         TileEntitySpit.this.updateClients();
      }

      @Override
      public int getSlotLimit(int slot) {
         return 1;
      }
   }
}
