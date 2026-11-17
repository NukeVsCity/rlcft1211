package com.alcatrazescapee.notreepunching.common.items;

import com.alcatrazescapee.notreepunching.ModConfig;
import com.alcatrazescapee.notreepunching.NoTreePunching;
import com.alcatrazescapee.notreepunching.common.tile.TileFirePit;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 1.12 ItemFireStarter: drawn like a bow for 30 ticks. If 3 sticks, 1 log and 1 kindling (grass string, paper, hay bale) lie on the
 * looked-at block, they become a lit fire pit (every item entity there is removed, 1.12 quirk); otherwise it lights a fire with
 * fireStarterFireStartChance (RLCraft 0.5). RLCraft removed its crafting recipe.
 */
public class ItemFireStarter extends Item {
   private static final TagKey<Item> STICKS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "rods/wooden"));

   public ItemFireStarter(Properties props) {
      super(props);
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      player.startUsingItem(hand);
      return InteractionResultHolder.pass(player.getItemInHand(hand));
   }

   @Override
   public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
      if (entity instanceof Player player) {
         BlockHitResult result = getPlayerPOVHitResult(world, player, ClipContext.Fluid.NONE);
         if (result.getType() == HitResult.Type.BLOCK && !world.isClientSide) {
            BlockPos pos = result.getBlockPos();
            stack.hurtAndBreak(1, entity, LivingEntity.getSlotForHand(player.getUsedItemHand()));
            List<ItemEntity> entities = world.getEntitiesOfClass(ItemEntity.class, new AABB(pos.above()).setMaxY(pos.getY() + 2));
            int sticks = 3;
            int logs = 1;
            int thatch = 1;
            for (ItemEntity drop : entities) {
               ItemStack dropStack = drop.getItem();
               if (dropStack.is(NoTreePunching.GRASS_STRING.get()) || dropStack.is(Items.PAPER) || dropStack.is(Items.HAY_BLOCK)) {
                  thatch -= dropStack.getCount();
               } else if (dropStack.is(ItemTags.LOGS)) {
                  logs -= dropStack.getCount();
               } else if (dropStack.is(STICKS)) {
                  sticks -= dropStack.getCount();
               }
            }
            if (sticks <= 0 && logs <= 0 && thatch <= 0) {
               world.setBlockAndUpdate(pos.above(), NoTreePunching.FIRE_PIT.get().defaultBlockState());
               if (world.getBlockEntity(pos.above()) instanceof TileFirePit te) {
                  te.light(true);
               }
               entities.forEach(Entity::discard);
            } else if ((double)world.random.nextFloat() < ModConfig.BALANCE.fireStarterFireStartChance) {
               world.setBlockAndUpdate(pos.above(), Blocks.FIRE.defaultBlockState());
            }
         }
      }
      return stack;
   }

   @Override
   public UseAnim getUseAnimation(ItemStack stack) {
      return UseAnim.BOW;
   }

   @Override
   public int getUseDuration(ItemStack stack, LivingEntity entity) {
      return 30;
   }

   /** onUsingTick: smoke at the looked-at point (ParticleManager.generateFireStarterSmoke) */
   @Override
   public void onUseTick(Level world, LivingEntity entity, ItemStack stack, int count) {
      if (world.isClientSide && world.random.nextFloat() < 0.3F && entity instanceof Player player) {
         BlockHitResult result = getPlayerPOVHitResult(world, player, ClipContext.Fluid.NONE);
         if (result.getType() == HitResult.Type.BLOCK) {
            Vec3 v = result.getLocation();
            world.addParticle(ParticleTypes.SMOKE, v.x, v.y, v.z, 0.0, 0.05, 0.0);
         }
      }
   }

   @Override
   public boolean isEnchantable(ItemStack stack) {
      return false;
   }
}
