package com.lycanitesmobs.core.block;

import com.lycanitesmobs.compat.Material;
import com.lycanitesmobs.compat.LycReg;
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.info.ModInfo;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 1.12 BlockBase. 1.12 blocks set hardness, resistance, light, sound and opacity with setters after construction; 1.21 blocks
 * take them in their Properties. The setters are kept (they store the values) and the 1.21 getters are overridden to return
 * them; what 1.21 needs at construction (collision, occlusion, replaceable) comes from the 1.12 Material ({@link #props}).
 */
public class BlockBase extends Block {
   public ModInfo group;
   public String blockName = "BlockBase";
   public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 15);
   public int tickRate = 0;
   public boolean removeOnTick = false;
   public boolean loopTicks = true;
   public boolean canBeCrushed = false;
   public boolean noEntityCollision = false;
   public boolean noBreakCollision = false;
   public boolean isOpaque = true;
   public final Material material;
   protected float blockHardness = 0.0F;
   protected float blockResistance = 0.0F;
   protected int lightValue = -1;
   protected int lightOpacity = -1;
   protected SoundType legacySoundType;
   protected boolean tickRandomly112 = false;

   /** 1.21 Properties for a 1.12 material (Block(Material) defaults: hardness 0, no light; sounds of 1.12 Block.blockSoundType). */
   public static BlockBehaviour.Properties props(Material material) {
      BlockBehaviour.Properties p = BlockBehaviour.Properties.of().mapColor(MapColor.STONE);
      if (!material.blocksMovement() && !material.isLiquid()) {
         p.noCollission();
      }
      if (!material.isOpaque()) {
         p.noOcclusion();
      }
      if (material.isReplaceable()) {
         p.replaceable();
      }
      if (material == Material.FIRE || material == Material.PLANTS || material == Material.VINE || material == Material.WEB) {
         p.pushReaction(PushReaction.DESTROY);
      }
      if (material == Material.FIRE || material == Material.AIR) {
         p.noLootTable();
      }
      return p;
   }

   public BlockBase(Material material, ModInfo group, String name) {
      this(props(material), material, group, name);
   }

   public BlockBase(BlockBehaviour.Properties properties, Material material, ModInfo group, String name) {
      super(properties);
      this.material = material;
      this.group = group;
      this.blockName = name;
      this.setup();
   }

   public BlockBase(Material material) {
      super(props(material));
      this.material = material;
   }

   public void setup() {
      LycReg.setName(this, this.group.modid, this.blockName.toLowerCase());
   }

   // ==================== 1.12 Block setters ====================
   public BlockBase setHardness(float hardness) {
      this.blockHardness = hardness;
      if (this.blockResistance < hardness * 5.0F) {
         this.blockResistance = hardness * 5.0F;
      }
      return this;
   }

   public BlockBase setResistance(float resistance) {
      this.blockResistance = resistance * 3.0F;
      return this;
   }

   public BlockBase setBlockUnbreakable() {
      this.setHardness(-1.0F);
      return this;
   }

   public BlockBase setLightLevel(float value) {
      this.lightValue = (int)(15.0F * value);
      return this;
   }

   public BlockBase setLightOpacity(int opacity) {
      this.lightOpacity = opacity;
      return this;
   }

   public BlockBase setSoundType(SoundType sound) {
      this.legacySoundType = sound;
      return this;
   }

   public BlockBase setTickRandomly(boolean tickRandomly) {
      this.tickRandomly112 = tickRandomly;
      return this;
   }

   public BlockBase setHarvestLevel(String toolClass, int level) {
      return this;
   }

   public BlockBase disableStats() {
      return this;
   }

   public BlockBase setTranslationKey(String key) {
      return this;
   }

   public BlockBase setCreativeTab(Object tab) {
      if (tab instanceof com.lycanitesmobs.compat.CreativeTabs tabs) {
         com.lycanitesmobs.compat.CreativeTabs.add(tabs, this);
      }
      return this;
   }

   public float getBlockHardness() {
      return this.blockHardness;
   }

   public void copyAttributesTo(Block block) {
      if (block instanceof BlockBase base) {
         base.blockHardness = this.blockHardness;
         base.blockResistance = this.blockResistance;
      }
   }

   // ==================== 1.21 getters answering the 1.12 values ====================
   @Override
   protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
      // 1.12 ForgeHooks.blockStrength with this block's hardness
      float hardness = this.blockHardness;
      if (hardness < 0.0F) {
         return 0.0F;
      }
      if (hardness == 0.0F) {
         return 1.0F;
      }
      int i = net.neoforged.neoforge.event.EventHooks.doPlayerHarvestCheck(player, state, level, pos) ? 30 : 100;
      return player.getDigSpeed(state, pos) / hardness / (float)i;
   }

   @Override
   public float getExplosionResistance() {
      // 1.12 getExplosionResistance(Entity) = blockResistance / 5
      return this.blockResistance / 5.0F;
   }

   @Override
   public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
      return this.getExplosionResistance();
   }

   @Override
   public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
      return this.lightValue >= 0 ? this.lightValue : super.getLightEmission(state, level, pos);
   }

   @Override
   protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
      if (this.lightOpacity >= 0) {
         return Math.min(this.lightOpacity, 15);
      }
      return this.isOpaque ? 15 : super.getLightBlock(state, level, pos);
   }

   @Override
   public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, Entity entity) {
      return this.legacySoundType != null ? this.legacySoundType : super.getSoundType(state, level, pos, entity);
   }

   @Override
   protected boolean isRandomlyTicking(BlockState state) {
      return this.getTickRandomly();
   }

   /** 1.12 Block.randomTick called updateTick. */
   @Override
   protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      this.tick(state, level, pos, random);
   }

   public String getLocalizedName() {
      return LanguageManager.translate("tile." + this.blockName + ".name");
   }

   /** 1.12 Block.addInformation (called by ItemBlockBase). */
   public void addInformation(ItemStack stack, Level world, List<String> tooltip, TooltipFlag advanced) {
      tooltip.add(this.getDescription(stack, world));
   }

   public String getDescription(ItemStack itemStack, Level world) {
      return "§a" + LanguageManager.translate("tile." + this.blockName + ".description");
   }

   @Override
   protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean moved) {
      if (this.tickRate > 0) {
         world.scheduleTick(pos, this, this.tickRate(world));
      }
   }

   public BlockState getStateFromMeta(int meta) {
      return this.defaultBlockState();
   }

   public int getMetaFromState(BlockState state) {
      return 0;
   }

   @Override
   protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos triggerPos, boolean moving) {
      if (this.canBeCrushed && (block == Blocks.SAND || block == Blocks.GRAVEL)) {
         world.removeBlock(pos, false);
      }

      super.neighborChanged(state, world, pos, block, triggerPos, moving);
   }

   public int tickRate(Level world) {
      return this.tickRate;
   }

   @Override
   public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand) {
      if (!world.isClientSide) {
         if (this.removeOnTick && this.canRemove(world, pos, state, rand)) {
            world.removeBlock(pos, false);
         } else if (this.tickRate > 0 && this.loopTicks) {
            world.scheduleTick(pos, this, this.tickRate(world));
         }
      }
   }

   public boolean getTickRandomly() {
      return this.tickRandomly112 || this.tickRate > 0;
   }

   public boolean canRemove(Level world, BlockPos pos, BlockState state, RandomSource rand) {
      return true;
   }

   @Override
   protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      return this.noEntityCollision ? Shapes.empty() : super.getCollisionShape(state, world, pos, context);
   }

   /** 1.12 isCollidable false: no outline to click on (noBreakCollision). */
   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      return this.noBreakCollision ? Shapes.empty() : super.getShape(state, world, pos, context);
   }

   public boolean isOpaqueCube(BlockState state) {
      return this.isOpaque;
   }

   /** 1.12 onEntityCollision */
   @Override
   protected void entityInside(BlockState blockState, Level world, BlockPos pos, Entity entity) {
      super.entityInside(blockState, world, pos, entity);
   }

   public BlockBase setBlockStepSound(SoundType soundType) {
      return this.setSoundType(soundType);
   }

   public static enum RENDER_TYPE {
      NONE(-1),
      NORMAL(0),
      CROSS(1),
      TORCH(2),
      FIRE(3),
      FLUID(4);

      public final int id;

      private RENDER_TYPE(int value) {
         this.id = value;
      }

      public int getValue() {
         return this.id;
      }
   }
}
