package com.lycanitesmobs.core.item;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.compat.CreativeTabs;
import com.lycanitesmobs.compat.LycNBT;
import com.lycanitesmobs.compat.LycReg;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.info.ModInfo;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * 1.12 ItemBase. The 1.12 Item methods keep their names here (onItemRightClick, onItemUse, onUpdate, onUsingTick,
 * onPlayerStoppedUsing, getItemUseAction, getMaxItemUseDuration, addInformation, getIsRepairable, hitEntity...) and the
 * 1.21 methods call them, so subclasses port unchanged. 1.12 setters (setMaxStackSize, setMaxDamage) change the item's
 * default components (1.21 takes them at construction; Lycanites set them in its constructors).
 */
public class ItemBase extends Item {
   public static int DESCRIPTION_WIDTH = 200;
   /** 1.12 Item.itemRand */
   public static final net.minecraft.util.RandomSource ITEM_RAND = net.minecraft.util.RandomSource.create();
   public String itemName = "unamed_item";
   public ModInfo modInfo;

   public ItemBase() {
      this(new Item.Properties());
   }

   public ItemBase(Item.Properties properties) {
      super(properties);
      this.modInfo = LycanitesMobs.modInfo;
      this.setMaxStackSize(64);
   }

   public void setup() {
      LycReg.setName(this, this.modInfo.modid, this.itemName);
      CreativeTabs.add(LycanitesMobs.itemsTab, this);
   }

   // ==================== 1.12 Item setters ====================
   private static Field componentsField;

   /** Replaces a default component of this item (1.21 Item.components is set in the constructor). */
   protected static <T> void setDefaultComponent(Item item, net.minecraft.core.component.DataComponentType<T> type, T value) {
      try {
         if (componentsField == null) {
            componentsField = Item.class.getDeclaredField("components");
            componentsField.setAccessible(true);
         }
         DataComponentMap old = (DataComponentMap)componentsField.get(item);
         DataComponentMap.Builder builder = DataComponentMap.builder().addAll(old);
         if (value == null) {
            builder.set(type, null);
         } else {
            builder.set(type, value);
         }
         componentsField.set(item, builder.build());
      } catch (ReflectiveOperationException e) {
         throw new RuntimeException(e);
      }
   }

   public Item setMaxStackSize(int size) {
      setDefaultComponent(this, DataComponents.MAX_STACK_SIZE, size);
      return this;
   }

   public Item setMaxDamage(int damage) {
      setDefaultComponent(this, DataComponents.MAX_STACK_SIZE, 1);
      setDefaultComponent(this, DataComponents.MAX_DAMAGE, damage);
      setDefaultComponent(this, DataComponents.DAMAGE, 0);
      return this;
   }

   public Item setTranslationKey(String key) {
      return this;
   }

   public Item setCreativeTab(CreativeTabs tab) {
      CreativeTabs.add(tab, this);
      return this;
   }

   public Item setContainerItem(Item item) {
      return this;
   }

   public Item setHasSubtypes(boolean subtypes) {
      return this;
   }

   public Item setFull3D() {
      return this;
   }

   public int getItemStackLimit() {
      return this.getDefaultMaxStackSize();
   }

   // ==================== names and tooltips ====================
   public String getItemStackDisplayName(ItemStack stack) {
      return LanguageManager.translate("item." + this.itemName + ".name").trim();
   }

   @Override
   public Component getName(ItemStack stack) {
      String name = this.getItemStackDisplayName(stack);
      return name.isEmpty() || name.endsWith(".name") ? super.getName(stack) : Component.literal(name);
   }

   /** 1.12 addInformation; 1.21 appendHoverText. The 1.12 text was wrapped to DESCRIPTION_WIDTH by the client font (kept as one line). */
   public void addInformation(ItemStack stack, Level worldIn, List<String> tooltip, TooltipFlag flagIn) {
      String description = this.getDescription(stack, worldIn, tooltip, flagIn);
      if (!"".equalsIgnoreCase(description)) {
         tooltip.add("§a" + description);
      }
   }

   @Override
   public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag flag) {
      List<String> tooltip = new ArrayList<>();
      this.addInformation(stack, LycanitesMobs.proxy.getClientWorld(), tooltip, flag);
      for (String line : tooltip) {
         tooltipComponents.add(Component.literal(line));
      }
   }

   public String getDescription(ItemStack stack, Level worldIn, List<String> tooltip, TooltipFlag flagIn) {
      return LanguageManager.translate("item." + this.itemName + ".description");
   }

   public CompoundTag getTagCompound(ItemStack itemStack) {
      return LycNBT.hasTag(itemStack) ? LycNBT.getTag(itemStack) : new CompoundTag();
   }

   // ==================== 1.12 behaviour methods ====================
   /** 1.12 onUpdate */
   public void onUpdate(ItemStack itemStack, Level world, Entity entity, int par4, boolean par5) {
   }

   @Override
   public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
      this.onUpdate(stack, level, entity, slot, selected);
   }

   /** 1.12 onItemUse */
   public InteractionResult onItemUse(Player playerIn, Level worldIn, BlockPos pos, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
      return InteractionResult.PASS;
   }

   @Override
   public InteractionResult useOn(UseOnContext context) {
      BlockPos pos = context.getClickedPos();
      return this.onItemUse(context.getPlayer(), context.getLevel(), pos, context.getHand(), context.getClickedFace(),
         (float)(context.getClickLocation().x - pos.getX()), (float)(context.getClickLocation().y - pos.getY()), (float)(context.getClickLocation().z - pos.getZ()));
   }

   /** 1.12 onItemRightClick */
   public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
      return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      return this.onItemRightClick(world, player, hand);
   }

   public void onItemLeftClick(ItemStack itemStackIn, Level worldIn, Player playerIn, InteractionHand hand) {
   }

   /** 1.12 onUsingTick (1.21 onUseTick passes the remaining ticks too). */
   public void onUsingTick(ItemStack itemStack, LivingEntity entity, int useRemaining) {
   }

   @Override
   public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
      this.onUsingTick(stack, entity, remainingUseDuration);
   }

   /** 1.12 onPlayerStoppedUsing */
   public void onPlayerStoppedUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
   }

   @Override
   public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
      this.onPlayerStoppedUsing(stack, level, entity, timeLeft);
   }

   /** 1.12 onItemUseFinish */
   public ItemStack onItemUseFinish(ItemStack stack, Level worldIn, LivingEntity entityLiving) {
      return super.finishUsingItem(stack, worldIn, entityLiving);
   }

   @Override
   public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
      return this.onItemUseFinish(stack, level, entity);
   }

   /** 1.12 getItemUseAction */
   public UseAnim getItemUseAction(ItemStack itemStack) {
      return super.getUseAnimation(itemStack);
   }

   @Override
   public UseAnim getUseAnimation(ItemStack stack) {
      return this.getItemUseAction(stack);
   }

   /** 1.12 getMaxItemUseDuration */
   public int getMaxItemUseDuration(ItemStack stack) {
      return 0;
   }

   @Override
   public int getUseDuration(ItemStack stack, LivingEntity entity) {
      return this.getMaxItemUseDuration(stack);
   }

   /** 1.12 hitEntity */
   public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      return false;
   }

   @Override
   public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      return this.hitEntity(stack, target, attacker);
   }

   /** 1.12 itemInteractionForEntity */
   public boolean itemInteractionForEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
      return false;
   }

   @Override
   public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
      return this.itemInteractionForEntity(stack, player, entity, hand) ? InteractionResult.sidedSuccess(player.level().isClientSide) : InteractionResult.PASS;
   }

   public boolean onItemRightClickOnEntity(Player player, Entity entity, ItemStack itemStack) {
      return false;
   }

   public int getItemEnchantability() {
      return 0;
   }

   @Override
   public int getEnchantmentValue() {
      return this.getItemEnchantability();
   }

   /** 1.12 getIsRepairable */
   public boolean getIsRepairable(ItemStack itemStack, ItemStack repairStack) {
      return false;
   }

   @Override
   public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
      return this.getIsRepairable(stack, repairCandidate);
   }

   public void playSound(Level world, double x, double y, double z, SoundEvent sound, SoundSource category, float volume, float pitch) {
      world.playSound(null, x, y, z, sound, category, volume, pitch);
   }

   public void playSound(Level world, BlockPos pos, SoundEvent sound, SoundSource category, float volume, float pitch) {
      world.playSound(null, pos, sound, category, volume, pitch);
   }

   public boolean isFull3D() {
      return false;
   }

   public boolean useItemColors() {
      return false;
   }

   public int getColorFromItemstack(ItemStack itemStack, int tintIndex) {
      return 16777215;
   }
}
