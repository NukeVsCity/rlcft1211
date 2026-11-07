package com.nukevscity.rlgen.nether;

import com.mojang.logging.LogUtils;
import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.doomlike.DoomlikeDungeonsGen;
import com.nukevscity.rlgen.quark.SpeleothemBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

/**
 * The 1.12 IWorldGenerators RLCraft ran in the nether (dimension -1), on the 1.21 nether terrain: one feature per chunk at the end of
 * decoration (1.12 ran them after ChunkGeneratorHell.populate), in GameRegistry weight order. Equal weights keep Quark's registration order
 * (1.12 kept them in a HashSet).
 */
public final class NetherGen {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RLGen.MODID);
   private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RLGen.MODID);
   private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, RLGen.MODID);
   private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, RLGen.MODID);

   // Quark Basalt (stone_basalt), BlockSmoker, BlazeLantern and the netherrack / basalt speleothems
   public static final DeferredBlock<Block> BASALT = BLOCKS.registerSimpleBlock("basalt",
      BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundType.STONE));
   public static final DeferredBlock<SmokerBlock> SMOKER = BLOCKS.registerBlock("smoker", SmokerBlock::new,
      BlockBehaviour.Properties.of().mapColor(MapColor.NETHER).requiresCorrectToolForDrops().strength(0.4F).sound(SoundType.STONE));
   public static final DeferredBlock<Block> BLAZE_LANTERN = BLOCKS.registerSimpleBlock("blaze_lantern",
      BlockBehaviour.Properties.ofFullCopy(Blocks.GLOWSTONE).mapColor(MapColor.COLOR_YELLOW).lightLevel(s -> 15));
   public static final DeferredBlock<SpeleothemBlock> NETHERRACK_SPELEOTHEM = BLOCKS.registerBlock("netherrack_speleothem", SpeleothemBlock::new,
      BlockBehaviour.Properties.of().mapColor(MapColor.NETHER).requiresCorrectToolForDrops().strength(1.5F).sound(SoundType.NETHERRACK).noOcclusion());
   public static final DeferredBlock<SpeleothemBlock> BASALT_SPELEOTHEM = BLOCKS.registerBlock("basalt_speleothem", SpeleothemBlock::new,
      BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops().strength(1.5F).sound(SoundType.STONE).noOcclusion());
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SmokerBlockEntity>> SMOKER_BE = BLOCK_ENTITIES.register("smoker",
      () -> BlockEntityType.Builder.of(SmokerBlockEntity::new, SMOKER.get()).build(null));

   static {
      for (DeferredBlock<?> b : new DeferredBlock<?>[]{BASALT, SMOKER, BLAZE_LANTERN, NETHERRACK_SPELEOTHEM, BASALT_SPELEOTHEM}) {
         ITEMS.registerSimpleBlockItem(b);
      }
      FEATURES.register("nether_world_generators", NetherFeature::new);
   }

   private NetherGen() {
   }

   public static void register(IEventBus modBus) {
      BLOCKS.register(modBus);
      ITEMS.register(modBus);
      BLOCK_ENTITIES.register(modBus);
      FEATURES.register(modBus);
   }

   /** GameRegistry.generateWorld for a nether chunk. */
   static void generate(NetherWorld w, int cx, int cz) {
      QuarkNether.basalt(w, cx, cz);        // 0
      QuarkNether.smokers(w, cx, cz);       // 0
      QuarkNether.fossils(w, cx, cz);       // 0
      QuarkNether.spikes(w, cx, cz);        // 0
      QuarkNether.brokenPortals(w, cx, cz); // 1
      DoomlikeDungeonsGen.netherChunk(w.level, cx, cz); // 100
      QuarkNether.speleothems(w, cx, cz);   // 1000
   }

   static final class NetherFeature extends Feature<NoneFeatureConfiguration> {
      NetherFeature() {
         super(NoneFeatureConfiguration.CODEC);
      }

      @Override
      public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
         ChunkPos cp = new ChunkPos(context.origin());
         try {
            generate(new NetherWorld(context.level(), cp), cp.x, cp.z);
         } catch (RuntimeException e) {
            LOGGER.error("rlgen: 1.12 nether world generators failed in chunk {}", cp, e);
         }
         return true;
      }
   }
}
