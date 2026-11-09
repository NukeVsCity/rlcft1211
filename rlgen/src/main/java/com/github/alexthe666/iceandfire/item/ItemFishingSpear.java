package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.IafNames;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 1.12 ItemFishingSpear (64 uses): drawn 20+ ticks at water within 5 blocks, it spears a random raw fish (1.12 fish meta 0..3: cod,
 * salmon, clownfish = tropical fish, pufferfish). RLCraft renames it "Ancient Fishing Spear" (only used for crafting) via the lang file.
 */
public class ItemFishingSpear extends Item {
   private static final Item[] FISH = new Item[]{Items.COD, Items.SALMON, Items.TROPICAL_FISH, Items.PUFFERFISH};

   public ItemFishingSpear() {
      super(new Properties().stacksTo(1).durability(64));
      IafNames.item(this, "iceandfire.fishing_spear");
   }

   @Override
   public UseAnim getUseAnimation(ItemStack stack) {
      return UseAnim.BOW;
   }

   private static BlockHitResult rayTrace(Level worldIn, Player p) {
      float f1 = p.getXRot();
      float f2 = p.getYRot();
      Vec3 vec3d = new Vec3(p.getX(), p.getY() + (double)p.getEyeHeight(), p.getZ());
      float f3 = Mth.cos(-f2 * (float)(Math.PI / 180.0) - (float)Math.PI);
      float f4 = Mth.sin(-f2 * (float)(Math.PI / 180.0) - (float)Math.PI);
      float f5 = -Mth.cos(-f1 * (float)(Math.PI / 180.0));
      float f6 = Mth.sin(-f1 * (float)(Math.PI / 180.0));
      Vec3 vec3d1 = vec3d.add((double)(f4 * f5) * 5.0, (double)f6 * 5.0, (double)(f3 * f5) * 5.0);
      return worldIn.clip(new ClipContext(vec3d, vec3d1, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, p));
   }

   @Override
   public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
      if (entityLiving instanceof Player entityplayer) {
         int i = this.getUseDuration(stack, entityLiving) - timeLeft;
         if (i < 20) {
            return;
         }
         BlockHitResult raytraceresult = rayTrace(worldIn, entityplayer);
         if (raytraceresult.getType() != HitResult.Type.BLOCK) {
            return;
         }
         BlockPos pos = raytraceresult.getBlockPos();
         if (!worldIn.getBlockState(pos).is(Blocks.WATER)) {
            return;
         }
         if (!entityplayer.getAbilities().instabuild) {
            stack.hurtAndBreak(1, entityplayer, entityplayer.getUsedItemHand() == InteractionHand.OFF_HAND ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND);
         }
         entityplayer.awardStat(Stats.ITEM_USED.get(this));
         if (!worldIn.isClientSide) {
            ItemEntity item = new ItemEntity(worldIn, (double)pos.getX() + 0.5, (double)pos.getY() + 1.5, (double)pos.getZ() + 0.5,
               new ItemStack(FISH[worldIn.random.nextInt(4)]));
            worldIn.addFreshEntity(item);
         }
         worldIn.playSound(null, entityplayer.getX(), entityplayer.getY(), entityplayer.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL, 1.0F,
            1.0F / (worldIn.random.nextFloat() * 0.4F + 1.2F) + 0.1F);
         entityplayer.awardStat(Stats.ITEM_USED.get(this));
      }
   }

   @Override
   public int getUseDuration(ItemStack stack, LivingEntity entity) {
      return 72000;
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level worldIn, Player entityplayer, InteractionHand hand) {
      ItemStack itemStackIn = entityplayer.getItemInHand(hand);
      entityplayer.startUsingItem(hand);
      BlockHitResult raytraceresult = rayTrace(worldIn, entityplayer);
      if (raytraceresult.getType() == HitResult.Type.BLOCK && worldIn.getBlockState(raytraceresult.getBlockPos()).is(Blocks.WATER)) {
         entityplayer.awardStat(Stats.ITEM_USED.get(this));
      }
      return InteractionResultHolder.pass(itemStackIn);
   }
}
