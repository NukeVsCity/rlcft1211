package com.nukevscity.rlgen.village;

import com.mojang.logging.LogUtils;
import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.deco.DWorld;
import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.legacy.biome.BiomeDesert;
import com.nukevscity.rlgen.legacy.biome.BiomeSavanna;
import com.nukevscity.rlgen.legacy.biome.BiomeTaiga;
import com.nukevscity.rlgen.rc.RcEnv;
import com.nukevscity.rlgen.rc.RcGenerator;
import com.nukevscity.rlgen.rc.RcRegistry;
import com.nukevscity.rlgen.rc.RcStructureDef;
import com.nukevscity.rlgen.rc.RcTransform;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import com.nukevscity.rlgen.world.RcPiece;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import org.slf4j.Logger;

/**
 * 1.12.2 StructureVillagePieces (with the Forge 1.12 patches: extra village components, GetVillageBlockID, start biome),
 * ported to 1.21 structure pieces. Recurrent Complex's village pieces (GenericVillagePiece) are {@link RcVillagePiece}.
 */
public class VillagePieces {
   private static final Logger LOGGER = LogUtils.getLogger();
   static final int SEA_LEVEL = 63;
   private static final Direction[] HORIZONTAL = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
   /** 1.12 double_stone_slab (stone variant). */
   static final BlockState DOUBLE_STONE_SLAB = Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE);

   private static final Map<String, Function<CompoundTag, Village>> LOADERS = new HashMap<>();

   static {
      LOADERS.put("Church", Church::new);
      LOADERS.put("Field1", Field1::new);
      LOADERS.put("Field2", Field2::new);
      LOADERS.put("Hall", Hall::new);
      LOADERS.put("House1", House1::new);
      LOADERS.put("House2", House2::new);
      LOADERS.put("House3", House3::new);
      LOADERS.put("House4Garden", House4Garden::new);
      LOADERS.put("Path", Path::new);
      LOADERS.put("Start", Start::new);
      LOADERS.put("Torch", Torch::new);
      LOADERS.put("Well", Well::new);
      LOADERS.put("WoodHut", WoodHut::new);
      LOADERS.put("RcVillagePiece", RcVillagePiece::new);
      LOADERS.put("WaystonePiece", WaystonePiece::new);
      LOADERS.put("BountyBoardPiece", BountyBoardPiece::new);
   }

   /** StructurePieceType loader for every village piece. */
   public static StructurePiece load(StructurePieceSerializationContext context, CompoundTag tag) {
      Function<CompoundTag, Village> f = LOADERS.get(tag.getString("VId"));
      if (f == null) {
         throw new IllegalStateException("rlgen: unknown village piece " + tag.getString("VId"));
      }
      Village v = f.apply(tag);
      v.read(tag);
      return v;
   }

   /** MathHelper.getInt */
   static int randInt(Random random, int min, int max) {
      return min >= max ? min : random.nextInt(max - min + 1) + min;
   }

   /** MathHelper.nextDouble */
   static double randDouble(Random random, double min, double max) {
      return min >= max ? min : random.nextDouble() * (max - min) + min;
   }

   /** StructureComponent.findIntersecting */
   @Nullable
   static StructurePiece findIntersecting(List<StructurePiece> list, BoundingBox box) {
      for (StructurePiece p : list) {
         if (p.getBoundingBox() != null && p.getBoundingBox().intersects(box)) {
            return p;
         }
      }
      return null;
   }

   /** World.getTopSolidOrLiquidBlock (plants, leaves and logs do not count, like before 1.12 trees grew). */
   static BlockPos topSolidOrLiquid(WorldGenLevel world, BlockPos p) {
      int y = Math.min(256, world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, p.getX(), p.getZ()) + 16);
      BlockPos pos = new BlockPos(p.getX(), y, p.getZ());
      while (pos.getY() >= 0) {
         BlockPos below = pos.below();
         BlockState s = world.getBlockState(below);
         if (DWorld.blocksMovement(s) && !DWorld.isLeaves(s) && !DWorld.isFoliage(s) && !isTree(s)) {
            break;
         }
         pos = below;
      }
      return pos;
   }

   /**
    * In 1.12 villages were built before the chunk's grass and flowers were decorated; here a neighbour's decoration can come first,
    * so its plants count as air.
    */
   static boolean isAirOrLaterPlant(WorldGenLevel world, BlockPos pos) {
      BlockState s = world.getBlockState(pos);
      return s.isAir() || isLaterPlant(s);
   }

   private static boolean isLaterPlant(BlockState s) {
      return s.is(Blocks.SHORT_GRASS) || s.is(Blocks.TALL_GRASS) || s.is(Blocks.FERN) || s.is(Blocks.LARGE_FERN) || s.is(BlockTags.FLOWERS)
         || s.is(Blocks.DEAD_BUSH) || s.is(Blocks.BROWN_MUSHROOM) || s.is(Blocks.RED_MUSHROOM) || s.is(Blocks.PUMPKIN);
   }

   static void clearPlant(WorldGenLevel world, BlockPos pos) {
      BlockState s = world.getBlockState(pos);
      if (isLaterPlant(s)) {
         world.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
         if (s.is(BlockTags.TALL_FLOWERS) || s.is(Blocks.TALL_GRASS) || s.is(Blocks.LARGE_FERN)) {
            world.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 2);
         }
      }
   }

   /** Dynamic Trees branches and trunks (not rooty soil): trees grew after villages in 1.12. */
   private static boolean isTree(BlockState s) {
      ResourceLocation id = BuiltInRegistries.BLOCK.getKey(s.getBlock());
      String p = id.getPath();
      return s.is(BlockTags.LOGS) || !p.startsWith("rooty") && (p.endsWith("_branch") || p.equals("trunk_shell") || p.endsWith("_root"));
   }

   static BlockState crop(Block crop, int age) {
      return ((CropBlock)crop).getStateForAge(Math.min(age, ((CropBlock)crop).getMaxAge()));
   }

   // ------------------------------------------------------------------ Recurrent Complex village components (Forge VillagerRegistry)

   /** GenericVillageCreationHandler: one per RC structure with a vanilla generation, alphabetical (1.12 used a HashMap of classes). */
   record RcHandler(RcStructureDef def, RcStructureDef.VanillaGen gen) {
      PieceWeight weight(Random random, int villageSize) {
         int limit = (int)Math.floor(randDouble(random, this.gen.minBaseLimit() + villageSize * this.gen.minScaledLimit(),
            this.gen.maxBaseLimit() + villageSize * this.gen.maxScaledLimit()) + 0.5);
         PieceWeight w = new PieceWeight(RcVillagePiece.class, this.gen.vanillaWeight(), limit);
         w.rc = this;
         return w;
      }

      Village build(PieceWeight weight, Start start, List<StructurePiece> pieces, Random random, int x, int y, int z, Direction front, int depth) {
         RcTransform transform = RcVillagePiece.transform(this.gen.front(), this.def.mirrorable, this.def.rotatable, front.getOpposite(), random);
         if (!this.gen.biomeExpression().test(start.biome) || transform == null) {
            weight.villagePiecesSpawned = weight.villagePiecesLimit;
            return null;
         }
         int[] size = transform.applySize(this.def.size);
         BoundingBox bb = new BoundingBox(x, y, z, x + size[0] - 1, y + size[1] - 1, z + size[2] - 1);
         if (!Village.canVillageGoDeeper(bb) || findIntersecting(pieces, bb) != null) {
            return null;
         }
         RcVillagePiece piece = new RcVillagePiece(start, depth);
         piece.seed = random.nextLong();
         piece.structureID = this.def.id;
         piece.generationID = this.gen.id();
         piece.setOrientation(front);
         piece.transform = transform;
         piece.setBox(bb);
         return piece;
      }
   }

   private static volatile List<RcHandler> rcHandlers;

   static List<RcHandler> rcHandlers() {
      List<RcHandler> h = rcHandlers;
      if (h == null) {
         h = new ArrayList<>();
         for (RcStructureDef def : RcRegistry.get().structures()) {
            for (RcStructureDef.VanillaGen gen : def.vanilla) {
               h.add(new RcHandler(def, gen));
            }
         }
         rcHandlers = h;
      }
      return h;
   }

   public static List<VillagePieces.PieceWeight> getStructureVillageWeightedPieceList(Random random, int i) {
      List<PieceWeight> list = new ArrayList<>();
      list.add(new VillagePieces.PieceWeight(VillagePieces.House4Garden.class, 4, randInt(random, 2 + i, 4 + i * 2)));
      list.add(new VillagePieces.PieceWeight(VillagePieces.Church.class, 20, randInt(random, 0 + i, 1 + i)));
      list.add(new VillagePieces.PieceWeight(VillagePieces.House1.class, 20, randInt(random, 0 + i, 2 + i)));
      list.add(new VillagePieces.PieceWeight(VillagePieces.WoodHut.class, 3, randInt(random, 2 + i, 5 + i * 3)));
      list.add(new VillagePieces.PieceWeight(VillagePieces.Hall.class, 15, randInt(random, 0 + i, 2 + i)));
      list.add(new VillagePieces.PieceWeight(VillagePieces.Field1.class, 3, randInt(random, 1 + i, 4 + i)));
      list.add(new VillagePieces.PieceWeight(VillagePieces.Field2.class, 3, randInt(random, 2 + i, 4 + i * 2)));
      list.add(new VillagePieces.PieceWeight(VillagePieces.House2.class, 15, randInt(random, 0, 1 + i)));
      list.add(new VillagePieces.PieceWeight(VillagePieces.House3.class, 8, randInt(random, 0 + i, 3 + i * 2)));
      // the other mods' IVillageCreationHandlers (1.12 kept them in a HashMap by class; here alphabetical by class name):
      // Ice and Fire's animal farm (limit i + nextInt(1), 0 here, so it never builds), Bountiful's board, Recurrent Complex, Waystones
      random.nextInt(1);
      if (random.nextFloat() <= 1.0F) {
         list.add(new VillagePieces.PieceWeight(VillagePieces.BountyBoardPiece.class, 3, 1));
      }
      for (RcHandler handler : rcHandlers()) {
         list.add(handler.weight(random, i));
      }
      list.add(random.nextFloat() > 1.0F ? new VillagePieces.PieceWeight(VillagePieces.WaystonePiece.class, 0, 0)
         : new VillagePieces.PieceWeight(VillagePieces.WaystonePiece.class, 3, 1));
      Iterator<PieceWeight> iterator = list.iterator();

      while (iterator.hasNext()) {
         if (iterator.next().villagePiecesLimit == 0) {
            iterator.remove();
         }
      }

      return list;
   }

   private static int updatePieceWeight(List<VillagePieces.PieceWeight> list) {
      boolean flag = false;
      int i = 0;

      for (VillagePieces.PieceWeight structurevillagepieces$pieceweight : list) {
         if (structurevillagepieces$pieceweight.villagePiecesLimit > 0
            && structurevillagepieces$pieceweight.villagePiecesSpawned < structurevillagepieces$pieceweight.villagePiecesLimit) {
            flag = true;
         }

         i += structurevillagepieces$pieceweight.villagePieceWeight;
      }

      return flag ? i : -1;
   }

   private static VillagePieces.Village findAndCreateComponentFactory(
      VillagePieces.Start structurevillagepieces$start,
      VillagePieces.PieceWeight structurevillagepieces$pieceweight,
      List<StructurePiece> list,
      Random random,
      int i,
      int j,
      int k,
      Direction enumfacing,
      int l
   ) {
      Class oclass = structurevillagepieces$pieceweight.villagePieceClass;
      VillagePieces.Village structurevillagepieces$village = null;
      if (oclass == VillagePieces.House4Garden.class) {
         structurevillagepieces$village = VillagePieces.House4Garden.createPiece(structurevillagepieces$start, list, random, i, j, k, enumfacing, l);
      } else if (oclass == VillagePieces.Church.class) {
         structurevillagepieces$village = VillagePieces.Church.createPiece(structurevillagepieces$start, list, random, i, j, k, enumfacing, l);
      } else if (oclass == VillagePieces.House1.class) {
         structurevillagepieces$village = VillagePieces.House1.createPiece(structurevillagepieces$start, list, random, i, j, k, enumfacing, l);
      } else if (oclass == VillagePieces.WoodHut.class) {
         structurevillagepieces$village = VillagePieces.WoodHut.createPiece(structurevillagepieces$start, list, random, i, j, k, enumfacing, l);
      } else if (oclass == VillagePieces.Hall.class) {
         structurevillagepieces$village = VillagePieces.Hall.createPiece(structurevillagepieces$start, list, random, i, j, k, enumfacing, l);
      } else if (oclass == VillagePieces.Field1.class) {
         structurevillagepieces$village = VillagePieces.Field1.createPiece(structurevillagepieces$start, list, random, i, j, k, enumfacing, l);
      } else if (oclass == VillagePieces.Field2.class) {
         structurevillagepieces$village = VillagePieces.Field2.createPiece(structurevillagepieces$start, list, random, i, j, k, enumfacing, l);
      } else if (oclass == VillagePieces.House2.class) {
         structurevillagepieces$village = VillagePieces.House2.createPiece(structurevillagepieces$start, list, random, i, j, k, enumfacing, l);
      } else if (oclass == VillagePieces.House3.class) {
         structurevillagepieces$village = VillagePieces.House3.createPiece(structurevillagepieces$start, list, random, i, j, k, enumfacing, l);
      } else if (oclass == VillagePieces.WaystonePiece.class) {
         structurevillagepieces$village = VillagePieces.WaystonePiece.createPiece(structurevillagepieces$start, list, random, i, j, k, enumfacing, l);
      } else if (oclass == VillagePieces.BountyBoardPiece.class) {
         structurevillagepieces$village = VillagePieces.BountyBoardPiece.createPiece(structurevillagepieces$start, list, random, i, j, k, enumfacing, l);
      } else if (structurevillagepieces$pieceweight.rc != null) {
         structurevillagepieces$village = structurevillagepieces$pieceweight.rc.build(structurevillagepieces$pieceweight, structurevillagepieces$start, list, random, i, j, k, enumfacing, l);
      }

      return structurevillagepieces$village;
   }

   private static VillagePieces.Village generateComponent(
      VillagePieces.Start structurevillagepieces$start,
      List<StructurePiece> list,
      Random random,
      int i,
      int j,
      int k,
      Direction enumfacing,
      int l
   ) {
      int i1 = updatePieceWeight(structurevillagepieces$start.structureVillageWeightedPieceList);
      if (i1 <= 0) {
         return null;
      } else {
         int j1 = 0;

         while (j1 < 5) {
            j1++;
            int k1 = random.nextInt(i1);

            for (VillagePieces.PieceWeight structurevillagepieces$pieceweight : structurevillagepieces$start.structureVillageWeightedPieceList) {
               k1 -= structurevillagepieces$pieceweight.villagePieceWeight;
               if (k1 < 0) {
                  if (!structurevillagepieces$pieceweight.canSpawnMoreVillagePiecesOfType(l)
                     || structurevillagepieces$pieceweight == structurevillagepieces$start.lastPlaced
                        && structurevillagepieces$start.structureVillageWeightedPieceList.size() > 1) {
                     break;
                  }

                  VillagePieces.Village structurevillagepieces$village = findAndCreateComponentFactory(
                     structurevillagepieces$start, structurevillagepieces$pieceweight, list, random, i, j, k, enumfacing, l
                  );
                  if (structurevillagepieces$village != null) {
                     structurevillagepieces$pieceweight.villagePiecesSpawned++;
                     structurevillagepieces$start.lastPlaced = structurevillagepieces$pieceweight;
                     if (!structurevillagepieces$pieceweight.canSpawnMoreVillagePieces()) {
                        structurevillagepieces$start.structureVillageWeightedPieceList.remove(structurevillagepieces$pieceweight);
                     }

                     return structurevillagepieces$village;
                  }
               }
            }
         }

         BoundingBox structureboundingbox = VillagePieces.Torch.findPieceBox(structurevillagepieces$start, list, random, i, j, k, enumfacing);
         return structureboundingbox != null
            ? new VillagePieces.Torch(structurevillagepieces$start, l, random, structureboundingbox, enumfacing)
            : null;
      }
   }

   private static StructurePiece generateAndAddComponent(
      VillagePieces.Start structurevillagepieces$start,
      List<StructurePiece> list,
      Random random,
      int i,
      int j,
      int k,
      Direction enumfacing,
      int l
   ) {
      if (l > 50) {
         return null;
      } else if (Math.abs(i - structurevillagepieces$start.getBoundingBox().minX()) <= 112
         && Math.abs(k - structurevillagepieces$start.getBoundingBox().minZ()) <= 112) {
         StructurePiece structurecomponent = generateComponent(structurevillagepieces$start, list, random, i, j, k, enumfacing, l + 1);
         if (structurecomponent != null) {
            list.add(structurecomponent);
            structurevillagepieces$start.pendingHouses.add(structurecomponent);
            return structurecomponent;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static StructurePiece generateAndAddRoadPiece(
      VillagePieces.Start structurevillagepieces$start,
      List<StructurePiece> list,
      Random random,
      int i,
      int j,
      int k,
      Direction enumfacing,
      int l
   ) {
      if (l > 3 + structurevillagepieces$start.terrainType) {
         return null;
      } else if (Math.abs(i - structurevillagepieces$start.getBoundingBox().minX()) <= 112
         && Math.abs(k - structurevillagepieces$start.getBoundingBox().minZ()) <= 112) {
         BoundingBox structureboundingbox = VillagePieces.Path.findPieceBox(structurevillagepieces$start, list, random, i, j, k, enumfacing);
         if (structureboundingbox != null && structureboundingbox.minY() > 10) {
            StructurePiece structurecomponent = new VillagePieces.Path(structurevillagepieces$start, l, random, structureboundingbox, enumfacing);
            list.add(structurecomponent);
            structurevillagepieces$start.pendingRoads.add(structurecomponent);
            return structurecomponent;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }


   public static class Church extends VillagePieces.Village {
      Church(CompoundTag tag) {
         super(tag);
      }

      public Church(
         VillagePieces.Start structurevillagepieces$start, int i, Random random, BoundingBox structureboundingbox, Direction enumfacing
      ) {
         super(structurevillagepieces$start, i);
         this.setOrientation(enumfacing);
         this.boundingBox = structureboundingbox;
      }

      public static VillagePieces.Church createPiece(
         VillagePieces.Start structurevillagepieces$start,
         List<StructurePiece> list,
         Random random,
         int i,
         int j,
         int k,
         Direction enumfacing,
         int l
      ) {
         BoundingBox structureboundingbox = BoundingBox.orientBox(i, j, k, 0, 0, 0, 5, 12, 9, enumfacing);
         return canVillageGoDeeper(structureboundingbox) && findIntersecting(list, structureboundingbox) == null
            ? new VillagePieces.Church(structurevillagepieces$start, l, random, structureboundingbox, enumfacing)
            : null;
      }

      @Override
      public boolean addComponentParts(WorldGenLevel world, Random random, BoundingBox structureboundingbox) {
         if (this.averageGroundLvl < 0) {
            this.averageGroundLvl = this.getAverageGroundLevel(world, structureboundingbox);
            if (this.averageGroundLvl < 0) {
               return true;
            }

            this.boundingBox.move(0, this.averageGroundLvl - this.boundingBox.maxY() + 12 - 1, 0);
         }

         BlockState iblockstate = Blocks.COBBLESTONE.defaultBlockState();
         BlockState iblockstate1 = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
         BlockState iblockstate2 = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST));
         BlockState iblockstate3 = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST));
         this.generateBox(world, structureboundingbox, 1, 1, 1, 3, 3, 7, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(world, structureboundingbox, 1, 5, 1, 3, 9, 3, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(world, structureboundingbox, 1, 0, 0, 3, 0, 8, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 1, 1, 0, 3, 10, 0, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 0, 1, 1, 0, 10, 3, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 4, 1, 1, 4, 10, 3, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 0, 0, 4, 0, 4, 7, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 4, 0, 4, 4, 4, 7, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 1, 1, 8, 3, 4, 8, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 1, 5, 4, 3, 10, 4, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 1, 5, 5, 3, 5, 7, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 0, 9, 0, 4, 9, 4, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 0, 4, 0, 4, 4, 4, iblockstate, iblockstate, false);
         this.placeBlock(world, iblockstate, 0, 11, 2, structureboundingbox);
         this.placeBlock(world, iblockstate, 4, 11, 2, structureboundingbox);
         this.placeBlock(world, iblockstate, 2, 11, 0, structureboundingbox);
         this.placeBlock(world, iblockstate, 2, 11, 4, structureboundingbox);
         this.placeBlock(world, iblockstate, 1, 1, 6, structureboundingbox);
         this.placeBlock(world, iblockstate, 1, 1, 7, structureboundingbox);
         this.placeBlock(world, iblockstate, 2, 1, 7, structureboundingbox);
         this.placeBlock(world, iblockstate, 3, 1, 6, structureboundingbox);
         this.placeBlock(world, iblockstate, 3, 1, 7, structureboundingbox);
         this.placeBlock(world, iblockstate1, 1, 1, 5, structureboundingbox);
         this.placeBlock(world, iblockstate1, 2, 1, 6, structureboundingbox);
         this.placeBlock(world, iblockstate1, 3, 1, 5, structureboundingbox);
         this.placeBlock(world, iblockstate2, 1, 2, 7, structureboundingbox);
         this.placeBlock(world, iblockstate3, 3, 2, 7, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 0, 2, 2, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 0, 3, 2, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 4, 2, 2, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 4, 3, 2, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 0, 6, 2, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 0, 7, 2, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 4, 6, 2, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 4, 7, 2, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 2, 6, 0, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 2, 7, 0, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 2, 6, 4, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 2, 7, 4, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 0, 3, 6, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 4, 3, 6, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 2, 3, 8, structureboundingbox);
         this.placeTorch(world, Direction.SOUTH, 2, 4, 7, structureboundingbox);
         this.placeTorch(world, Direction.EAST, 1, 4, 6, structureboundingbox);
         this.placeTorch(world, Direction.WEST, 3, 4, 6, structureboundingbox);
         this.placeTorch(world, Direction.NORTH, 2, 4, 5, structureboundingbox);
         BlockState iblockstate4 = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.WEST);

         for (int i = 1; i <= 9; i++) {
            this.placeBlock(world, iblockstate4, 3, i, 3, structureboundingbox);
         }

         this.placeBlock(world, Blocks.AIR.defaultBlockState(), 2, 1, 0, structureboundingbox);
         this.placeBlock(world, Blocks.AIR.defaultBlockState(), 2, 2, 0, structureboundingbox);
         this.createVillageDoor(world, structureboundingbox, random, 2, 1, 0, Direction.NORTH);
         if (this.getBlock(world, 2, 0, -1, structureboundingbox).isAir()
            && this.getBlock(world, 2, -1, -1, structureboundingbox).isAir() == false) {
            this.placeBlock(world, iblockstate1, 2, 0, -1, structureboundingbox);
            if (this.getBlock(world, 2, -1, -1, structureboundingbox).is(Blocks.DIRT_PATH)) {
               this.placeBlock(world, Blocks.GRASS_BLOCK.defaultBlockState(), 2, -1, -1, structureboundingbox);
            }
         }

         for (int k = 0; k < 9; k++) {
            for (int j = 0; j < 5; j++) {
               this.clearCurrentPositionBlocksUpwards(world, j, 12, k, structureboundingbox);
               this.replaceAirAndLiquidDownwards(world, iblockstate, j, -1, k, structureboundingbox);
            }
         }

         this.spawnVillagers(world, structureboundingbox, 2, 1, 2, 1);
         return true;
      }

      @Override
      protected int chooseProfession(int i, int j) {
         return 2;
      }
   }

   public static class Field1 extends VillagePieces.Village {
      private Block cropTypeA;
      private Block cropTypeB;
      private Block cropTypeC;
      private Block cropTypeD;

      Field1(CompoundTag tag) {
         super(tag);
      }

      public Field1(
         VillagePieces.Start structurevillagepieces$start, int i, Random random, BoundingBox structureboundingbox, Direction enumfacing
      ) {
         super(structurevillagepieces$start, i);
         this.setOrientation(enumfacing);
         this.boundingBox = structureboundingbox;
         this.cropTypeA = this.getRandomCropType(random);
         this.cropTypeB = this.getRandomCropType(random);
         this.cropTypeC = this.getRandomCropType(random);
         this.cropTypeD = this.getRandomCropType(random);
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag nbttagcompound) {
         super.addAdditionalSaveData(context, nbttagcompound);
         nbttagcompound.putString("CA", BuiltInRegistries.BLOCK.getKey(this.cropTypeA).toString());
         nbttagcompound.putString("CB", BuiltInRegistries.BLOCK.getKey(this.cropTypeB).toString());
         nbttagcompound.putString("CC", BuiltInRegistries.BLOCK.getKey(this.cropTypeC).toString());
         nbttagcompound.putString("CD", BuiltInRegistries.BLOCK.getKey(this.cropTypeD).toString());
      }

      @Override
      void read(CompoundTag nbttagcompound) {
         super.read(nbttagcompound);
         this.cropTypeA = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(nbttagcompound.getString("CA")));
         this.cropTypeB = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(nbttagcompound.getString("CB")));
         this.cropTypeC = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(nbttagcompound.getString("CC")));
         this.cropTypeD = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(nbttagcompound.getString("CD")));
         if (!(this.cropTypeA instanceof CropBlock)) {
            this.cropTypeA = Blocks.WHEAT;
         }

         if (!(this.cropTypeB instanceof CropBlock)) {
            this.cropTypeB = Blocks.CARROTS;
         }

         if (!(this.cropTypeC instanceof CropBlock)) {
            this.cropTypeC = Blocks.POTATOES;
         }

         if (!(this.cropTypeD instanceof CropBlock)) {
            this.cropTypeD = Blocks.BEETROOTS;
         }
      }

      private Block getRandomCropType(Random random) {
         switch (random.nextInt(10)) {
            case 0:
            case 1:
               return Blocks.CARROTS;
            case 2:
            case 3:
               return Blocks.POTATOES;
            case 4:
               return Blocks.BEETROOTS;
            default:
               return Blocks.WHEAT;
         }
      }

      public static VillagePieces.Field1 createPiece(
         VillagePieces.Start structurevillagepieces$start,
         List<StructurePiece> list,
         Random random,
         int i,
         int j,
         int k,
         Direction enumfacing,
         int l
      ) {
         BoundingBox structureboundingbox = BoundingBox.orientBox(i, j, k, 0, 0, 0, 13, 4, 9, enumfacing);
         return canVillageGoDeeper(structureboundingbox) && findIntersecting(list, structureboundingbox) == null
            ? new VillagePieces.Field1(structurevillagepieces$start, l, random, structureboundingbox, enumfacing)
            : null;
      }

      @Override
      public boolean addComponentParts(WorldGenLevel world, Random random, BoundingBox structureboundingbox) {
         if (this.averageGroundLvl < 0) {
            this.averageGroundLvl = this.getAverageGroundLevel(world, structureboundingbox);
            if (this.averageGroundLvl < 0) {
               return true;
            }

            this.boundingBox.move(0, this.averageGroundLvl - this.boundingBox.maxY() + 4 - 1, 0);
         }

         BlockState iblockstate = this.getBiomeSpecificBlockState(Blocks.OAK_LOG.defaultBlockState());
         this.generateBox(world, structureboundingbox, 0, 1, 0, 12, 4, 8, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(world, structureboundingbox, 1, 0, 1, 2, 0, 7, Blocks.FARMLAND.defaultBlockState(), Blocks.FARMLAND.defaultBlockState(), false);
         this.generateBox(world, structureboundingbox, 4, 0, 1, 5, 0, 7, Blocks.FARMLAND.defaultBlockState(), Blocks.FARMLAND.defaultBlockState(), false);
         this.generateBox(world, structureboundingbox, 7, 0, 1, 8, 0, 7, Blocks.FARMLAND.defaultBlockState(), Blocks.FARMLAND.defaultBlockState(), false);
         this.generateBox(world, structureboundingbox, 10, 0, 1, 11, 0, 7, Blocks.FARMLAND.defaultBlockState(), Blocks.FARMLAND.defaultBlockState(), false);
         this.generateBox(world, structureboundingbox, 0, 0, 0, 0, 0, 8, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 6, 0, 0, 6, 0, 8, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 12, 0, 0, 12, 0, 8, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 1, 0, 0, 11, 0, 0, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 1, 0, 8, 11, 0, 8, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 3, 0, 1, 3, 0, 7, Blocks.WATER.defaultBlockState(), Blocks.WATER.defaultBlockState(), false);
         this.generateBox(world, structureboundingbox, 9, 0, 1, 9, 0, 7, Blocks.WATER.defaultBlockState(), Blocks.WATER.defaultBlockState(), false);

         for (int i = 1; i <= 7; i++) {
            int j = ((CropBlock)this.cropTypeA).getMaxAge();
            int k = j / 3;
            this.placeBlock(world, crop(this.cropTypeA, randInt(random, k, j)), 1, 1, i, structureboundingbox);
            this.placeBlock(world, crop(this.cropTypeA, randInt(random, k, j)), 2, 1, i, structureboundingbox);
            int l = ((CropBlock)this.cropTypeB).getMaxAge();
            int i1 = l / 3;
            this.placeBlock(world, crop(this.cropTypeB, randInt(random, i1, l)), 4, 1, i, structureboundingbox);
            this.placeBlock(world, crop(this.cropTypeB, randInt(random, i1, l)), 5, 1, i, structureboundingbox);
            int j1 = ((CropBlock)this.cropTypeC).getMaxAge();
            int k1 = j1 / 3;
            this.placeBlock(world, crop(this.cropTypeC, randInt(random, k1, j1)), 7, 1, i, structureboundingbox);
            this.placeBlock(world, crop(this.cropTypeC, randInt(random, k1, j1)), 8, 1, i, structureboundingbox);
            int l1 = ((CropBlock)this.cropTypeD).getMaxAge();
            int i2 = l1 / 3;
            this.placeBlock(world, crop(this.cropTypeD, randInt(random, i2, l1)), 10, 1, i, structureboundingbox);
            this.placeBlock(world, crop(this.cropTypeD, randInt(random, i2, l1)), 11, 1, i, structureboundingbox);
         }

         for (int j2 = 0; j2 < 9; j2++) {
            for (int k2 = 0; k2 < 13; k2++) {
               this.clearCurrentPositionBlocksUpwards(world, k2, 4, j2, structureboundingbox);
               this.replaceAirAndLiquidDownwards(world, Blocks.DIRT.defaultBlockState(), k2, -1, j2, structureboundingbox);
            }
         }

         return true;
      }
   }

   public static class Field2 extends VillagePieces.Village {
      private Block cropTypeA;
      private Block cropTypeB;

      Field2(CompoundTag tag) {
         super(tag);
      }

      public Field2(
         VillagePieces.Start structurevillagepieces$start, int i, Random random, BoundingBox structureboundingbox, Direction enumfacing
      ) {
         super(structurevillagepieces$start, i);
         this.setOrientation(enumfacing);
         this.boundingBox = structureboundingbox;
         this.cropTypeA = this.getRandomCropType(random);
         this.cropTypeB = this.getRandomCropType(random);
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag nbttagcompound) {
         super.addAdditionalSaveData(context, nbttagcompound);
         nbttagcompound.putString("CA", BuiltInRegistries.BLOCK.getKey(this.cropTypeA).toString());
         nbttagcompound.putString("CB", BuiltInRegistries.BLOCK.getKey(this.cropTypeB).toString());
      }

      @Override
      void read(CompoundTag nbttagcompound) {
         super.read(nbttagcompound);
         this.cropTypeA = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(nbttagcompound.getString("CA")));
         this.cropTypeB = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(nbttagcompound.getString("CB")));
      }

      private Block getRandomCropType(Random random) {
         switch (random.nextInt(10)) {
            case 0:
            case 1:
               return Blocks.CARROTS;
            case 2:
            case 3:
               return Blocks.POTATOES;
            case 4:
               return Blocks.BEETROOTS;
            default:
               return Blocks.WHEAT;
         }
      }

      public static VillagePieces.Field2 createPiece(
         VillagePieces.Start structurevillagepieces$start,
         List<StructurePiece> list,
         Random random,
         int i,
         int j,
         int k,
         Direction enumfacing,
         int l
      ) {
         BoundingBox structureboundingbox = BoundingBox.orientBox(i, j, k, 0, 0, 0, 7, 4, 9, enumfacing);
         return canVillageGoDeeper(structureboundingbox) && findIntersecting(list, structureboundingbox) == null
            ? new VillagePieces.Field2(structurevillagepieces$start, l, random, structureboundingbox, enumfacing)
            : null;
      }

      @Override
      public boolean addComponentParts(WorldGenLevel world, Random random, BoundingBox structureboundingbox) {
         if (this.averageGroundLvl < 0) {
            this.averageGroundLvl = this.getAverageGroundLevel(world, structureboundingbox);
            if (this.averageGroundLvl < 0) {
               return true;
            }

            this.boundingBox.move(0, this.averageGroundLvl - this.boundingBox.maxY() + 4 - 1, 0);
         }

         BlockState iblockstate = this.getBiomeSpecificBlockState(Blocks.OAK_LOG.defaultBlockState());
         this.generateBox(world, structureboundingbox, 0, 1, 0, 6, 4, 8, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(world, structureboundingbox, 1, 0, 1, 2, 0, 7, Blocks.FARMLAND.defaultBlockState(), Blocks.FARMLAND.defaultBlockState(), false);
         this.generateBox(world, structureboundingbox, 4, 0, 1, 5, 0, 7, Blocks.FARMLAND.defaultBlockState(), Blocks.FARMLAND.defaultBlockState(), false);
         this.generateBox(world, structureboundingbox, 0, 0, 0, 0, 0, 8, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 6, 0, 0, 6, 0, 8, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 1, 0, 0, 5, 0, 0, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 1, 0, 8, 5, 0, 8, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 3, 0, 1, 3, 0, 7, Blocks.WATER.defaultBlockState(), Blocks.WATER.defaultBlockState(), false);

         for (int i = 1; i <= 7; i++) {
            int j = ((CropBlock)this.cropTypeA).getMaxAge();
            int k = j / 3;
            this.placeBlock(world, crop(this.cropTypeA, randInt(random, k, j)), 1, 1, i, structureboundingbox);
            this.placeBlock(world, crop(this.cropTypeA, randInt(random, k, j)), 2, 1, i, structureboundingbox);
            int l = ((CropBlock)this.cropTypeB).getMaxAge();
            int i1 = l / 3;
            this.placeBlock(world, crop(this.cropTypeB, randInt(random, i1, l)), 4, 1, i, structureboundingbox);
            this.placeBlock(world, crop(this.cropTypeB, randInt(random, i1, l)), 5, 1, i, structureboundingbox);
         }

         for (int j1 = 0; j1 < 9; j1++) {
            for (int k1 = 0; k1 < 7; k1++) {
               this.clearCurrentPositionBlocksUpwards(world, k1, 4, j1, structureboundingbox);
               this.replaceAirAndLiquidDownwards(world, Blocks.DIRT.defaultBlockState(), k1, -1, j1, structureboundingbox);
            }
         }

         return true;
      }
   }

   public static class Hall extends VillagePieces.Village {
      Hall(CompoundTag tag) {
         super(tag);
      }

      public Hall(
         VillagePieces.Start structurevillagepieces$start, int i, Random random, BoundingBox structureboundingbox, Direction enumfacing
      ) {
         super(structurevillagepieces$start, i);
         this.setOrientation(enumfacing);
         this.boundingBox = structureboundingbox;
      }

      public static VillagePieces.Hall createPiece(
         VillagePieces.Start structurevillagepieces$start,
         List<StructurePiece> list,
         Random random,
         int i,
         int j,
         int k,
         Direction enumfacing,
         int l
      ) {
         BoundingBox structureboundingbox = BoundingBox.orientBox(i, j, k, 0, 0, 0, 9, 7, 11, enumfacing);
         return canVillageGoDeeper(structureboundingbox) && findIntersecting(list, structureboundingbox) == null
            ? new VillagePieces.Hall(structurevillagepieces$start, l, random, structureboundingbox, enumfacing)
            : null;
      }

      @Override
      public boolean addComponentParts(WorldGenLevel world, Random random, BoundingBox structureboundingbox) {
         if (this.averageGroundLvl < 0) {
            this.averageGroundLvl = this.getAverageGroundLevel(world, structureboundingbox);
            if (this.averageGroundLvl < 0) {
               return true;
            }

            this.boundingBox.move(0, this.averageGroundLvl - this.boundingBox.maxY() + 7 - 1, 0);
         }

         BlockState iblockstate = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE.defaultBlockState());
         BlockState iblockstate1 = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
         BlockState iblockstate2 = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH));
         BlockState iblockstate3 = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST));
         BlockState iblockstate4 = this.getBiomeSpecificBlockState(Blocks.OAK_PLANKS.defaultBlockState());
         BlockState iblockstate5 = this.getBiomeSpecificBlockState(Blocks.OAK_LOG.defaultBlockState());
         BlockState iblockstate6 = this.getBiomeSpecificBlockState(Blocks.OAK_FENCE.defaultBlockState());
         this.generateBox(world, structureboundingbox, 1, 1, 1, 7, 4, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(world, structureboundingbox, 2, 1, 6, 8, 4, 10, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(world, structureboundingbox, 2, 0, 6, 8, 0, 10, Blocks.DIRT.defaultBlockState(), Blocks.DIRT.defaultBlockState(), false);
         this.placeBlock(world, iblockstate, 6, 0, 6, structureboundingbox);
         this.generateBox(world, structureboundingbox, 2, 1, 6, 2, 1, 10, iblockstate6, iblockstate6, false);
         this.generateBox(world, structureboundingbox, 8, 1, 6, 8, 1, 10, iblockstate6, iblockstate6, false);
         this.generateBox(world, structureboundingbox, 3, 1, 10, 7, 1, 10, iblockstate6, iblockstate6, false);
         this.generateBox(world, structureboundingbox, 1, 0, 1, 7, 0, 4, iblockstate4, iblockstate4, false);
         this.generateBox(world, structureboundingbox, 0, 0, 0, 0, 3, 5, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 8, 0, 0, 8, 3, 5, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 1, 0, 0, 7, 1, 0, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 1, 0, 5, 7, 1, 5, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 1, 2, 0, 7, 3, 0, iblockstate4, iblockstate4, false);
         this.generateBox(world, structureboundingbox, 1, 2, 5, 7, 3, 5, iblockstate4, iblockstate4, false);
         this.generateBox(world, structureboundingbox, 0, 4, 1, 8, 4, 1, iblockstate4, iblockstate4, false);
         this.generateBox(world, structureboundingbox, 0, 4, 4, 8, 4, 4, iblockstate4, iblockstate4, false);
         this.generateBox(world, structureboundingbox, 0, 5, 2, 8, 5, 3, iblockstate4, iblockstate4, false);
         this.placeBlock(world, iblockstate4, 0, 4, 2, structureboundingbox);
         this.placeBlock(world, iblockstate4, 0, 4, 3, structureboundingbox);
         this.placeBlock(world, iblockstate4, 8, 4, 2, structureboundingbox);
         this.placeBlock(world, iblockstate4, 8, 4, 3, structureboundingbox);
         BlockState iblockstate7 = iblockstate1;
         BlockState iblockstate8 = iblockstate2;

         for (int i = -1; i <= 2; i++) {
            for (int j = 0; j <= 8; j++) {
               this.placeBlock(world, iblockstate7, j, 4 + i, i, structureboundingbox);
               this.placeBlock(world, iblockstate8, j, 4 + i, 5 - i, structureboundingbox);
            }
         }

         this.placeBlock(world, iblockstate5, 0, 2, 1, structureboundingbox);
         this.placeBlock(world, iblockstate5, 0, 2, 4, structureboundingbox);
         this.placeBlock(world, iblockstate5, 8, 2, 1, structureboundingbox);
         this.placeBlock(world, iblockstate5, 8, 2, 4, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 0, 2, 2, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 0, 2, 3, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 8, 2, 2, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 8, 2, 3, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 2, 2, 5, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 3, 2, 5, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 5, 2, 0, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 6, 2, 5, structureboundingbox);
         this.placeBlock(world, iblockstate6, 2, 1, 3, structureboundingbox);
         this.placeBlock(world, Blocks.OAK_PRESSURE_PLATE.defaultBlockState(), 2, 2, 3, structureboundingbox);
         this.placeBlock(world, iblockstate4, 1, 1, 4, structureboundingbox);
         this.placeBlock(world, iblockstate7, 2, 1, 4, structureboundingbox);
         this.placeBlock(world, iblockstate3, 1, 1, 3, structureboundingbox);
         this.generateBox(
            world, structureboundingbox, 5, 0, 1, 7, 0, 3, DOUBLE_STONE_SLAB, DOUBLE_STONE_SLAB, false
         );
         this.placeBlock(world, DOUBLE_STONE_SLAB, 6, 1, 1, structureboundingbox);
         this.placeBlock(world, DOUBLE_STONE_SLAB, 6, 1, 2, structureboundingbox);
         this.placeBlock(world, Blocks.AIR.defaultBlockState(), 2, 1, 0, structureboundingbox);
         this.placeBlock(world, Blocks.AIR.defaultBlockState(), 2, 2, 0, structureboundingbox);
         this.placeTorch(world, Direction.NORTH, 2, 3, 1, structureboundingbox);
         this.createVillageDoor(world, structureboundingbox, random, 2, 1, 0, Direction.NORTH);
         if (this.getBlock(world, 2, 0, -1, structureboundingbox).isAir()
            && this.getBlock(world, 2, -1, -1, structureboundingbox).isAir() == false) {
            this.placeBlock(world, iblockstate7, 2, 0, -1, structureboundingbox);
            if (this.getBlock(world, 2, -1, -1, structureboundingbox).is(Blocks.DIRT_PATH)) {
               this.placeBlock(world, Blocks.GRASS_BLOCK.defaultBlockState(), 2, -1, -1, structureboundingbox);
            }
         }

         this.placeBlock(world, Blocks.AIR.defaultBlockState(), 6, 1, 5, structureboundingbox);
         this.placeBlock(world, Blocks.AIR.defaultBlockState(), 6, 2, 5, structureboundingbox);
         this.placeTorch(world, Direction.SOUTH, 6, 3, 4, structureboundingbox);
         this.createVillageDoor(world, structureboundingbox, random, 6, 1, 5, Direction.SOUTH);

         for (int k = 0; k < 5; k++) {
            for (int l = 0; l < 9; l++) {
               this.clearCurrentPositionBlocksUpwards(world, l, 7, k, structureboundingbox);
               this.replaceAirAndLiquidDownwards(world, iblockstate, l, -1, k, structureboundingbox);
            }
         }

         this.spawnVillagers(world, structureboundingbox, 4, 1, 2, 2);
         return true;
      }

      @Override
      protected int chooseProfession(int i, int j) {
         return i == 0 ? 4 : super.chooseProfession(i, j);
      }
   }

   public static class House1 extends VillagePieces.Village {
      House1(CompoundTag tag) {
         super(tag);
      }

      public House1(
         VillagePieces.Start structurevillagepieces$start, int i, Random random, BoundingBox structureboundingbox, Direction enumfacing
      ) {
         super(structurevillagepieces$start, i);
         this.setOrientation(enumfacing);
         this.boundingBox = structureboundingbox;
      }

      public static VillagePieces.House1 createPiece(
         VillagePieces.Start structurevillagepieces$start,
         List<StructurePiece> list,
         Random random,
         int i,
         int j,
         int k,
         Direction enumfacing,
         int l
      ) {
         BoundingBox structureboundingbox = BoundingBox.orientBox(i, j, k, 0, 0, 0, 9, 9, 6, enumfacing);
         return canVillageGoDeeper(structureboundingbox) && findIntersecting(list, structureboundingbox) == null
            ? new VillagePieces.House1(structurevillagepieces$start, l, random, structureboundingbox, enumfacing)
            : null;
      }

      @Override
      public boolean addComponentParts(WorldGenLevel world, Random random, BoundingBox structureboundingbox) {
         if (this.averageGroundLvl < 0) {
            this.averageGroundLvl = this.getAverageGroundLevel(world, structureboundingbox);
            if (this.averageGroundLvl < 0) {
               return true;
            }

            this.boundingBox.move(0, this.averageGroundLvl - this.boundingBox.maxY() + 9 - 1, 0);
         }

         BlockState iblockstate = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE.defaultBlockState());
         BlockState iblockstate1 = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
         BlockState iblockstate2 = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH));
         BlockState iblockstate3 = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST));
         BlockState iblockstate4 = this.getBiomeSpecificBlockState(Blocks.OAK_PLANKS.defaultBlockState());
         BlockState iblockstate5 = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
         BlockState iblockstate6 = this.getBiomeSpecificBlockState(Blocks.OAK_FENCE.defaultBlockState());
         this.generateBox(world, structureboundingbox, 1, 1, 1, 7, 5, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(world, structureboundingbox, 0, 0, 0, 8, 0, 5, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 0, 5, 0, 8, 5, 5, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 0, 6, 1, 8, 6, 4, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 0, 7, 2, 8, 7, 3, iblockstate, iblockstate, false);

         for (int i = -1; i <= 2; i++) {
            for (int j = 0; j <= 8; j++) {
               this.placeBlock(world, iblockstate1, j, 6 + i, i, structureboundingbox);
               this.placeBlock(world, iblockstate2, j, 6 + i, 5 - i, structureboundingbox);
            }
         }

         this.generateBox(world, structureboundingbox, 0, 1, 0, 0, 1, 5, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 1, 1, 5, 8, 1, 5, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 8, 1, 0, 8, 1, 4, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 2, 1, 0, 7, 1, 0, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 0, 2, 0, 0, 4, 0, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 0, 2, 5, 0, 4, 5, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 8, 2, 5, 8, 4, 5, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 8, 2, 0, 8, 4, 0, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 0, 2, 1, 0, 4, 4, iblockstate4, iblockstate4, false);
         this.generateBox(world, structureboundingbox, 1, 2, 5, 7, 4, 5, iblockstate4, iblockstate4, false);
         this.generateBox(world, structureboundingbox, 8, 2, 1, 8, 4, 4, iblockstate4, iblockstate4, false);
         this.generateBox(world, structureboundingbox, 1, 2, 0, 7, 4, 0, iblockstate4, iblockstate4, false);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 4, 2, 0, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 5, 2, 0, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 6, 2, 0, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 4, 3, 0, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 5, 3, 0, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 6, 3, 0, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 0, 2, 2, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 0, 2, 3, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 0, 3, 2, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 0, 3, 3, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 8, 2, 2, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 8, 2, 3, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 8, 3, 2, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 8, 3, 3, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 2, 2, 5, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 3, 2, 5, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 5, 2, 5, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 6, 2, 5, structureboundingbox);
         this.generateBox(world, structureboundingbox, 1, 4, 1, 7, 4, 1, iblockstate4, iblockstate4, false);
         this.generateBox(world, structureboundingbox, 1, 4, 4, 7, 4, 4, iblockstate4, iblockstate4, false);
         this.generateBox(world, structureboundingbox, 1, 3, 4, 7, 3, 4, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
         this.placeBlock(world, iblockstate4, 7, 1, 4, structureboundingbox);
         this.placeBlock(world, iblockstate3, 7, 1, 3, structureboundingbox);
         this.placeBlock(world, iblockstate1, 6, 1, 4, structureboundingbox);
         this.placeBlock(world, iblockstate1, 5, 1, 4, structureboundingbox);
         this.placeBlock(world, iblockstate1, 4, 1, 4, structureboundingbox);
         this.placeBlock(world, iblockstate1, 3, 1, 4, structureboundingbox);
         this.placeBlock(world, iblockstate6, 6, 1, 3, structureboundingbox);
         this.placeBlock(world, Blocks.OAK_PRESSURE_PLATE.defaultBlockState(), 6, 2, 3, structureboundingbox);
         this.placeBlock(world, iblockstate6, 4, 1, 3, structureboundingbox);
         this.placeBlock(world, Blocks.OAK_PRESSURE_PLATE.defaultBlockState(), 4, 2, 3, structureboundingbox);
         this.placeBlock(world, Blocks.CRAFTING_TABLE.defaultBlockState(), 7, 1, 1, structureboundingbox);
         this.placeBlock(world, Blocks.AIR.defaultBlockState(), 1, 1, 0, structureboundingbox);
         this.placeBlock(world, Blocks.AIR.defaultBlockState(), 1, 2, 0, structureboundingbox);
         this.createVillageDoor(world, structureboundingbox, random, 1, 1, 0, Direction.NORTH);
         if (this.getBlock(world, 1, 0, -1, structureboundingbox).isAir()
            && this.getBlock(world, 1, -1, -1, structureboundingbox).isAir() == false) {
            this.placeBlock(world, iblockstate5, 1, 0, -1, structureboundingbox);
            if (this.getBlock(world, 1, -1, -1, structureboundingbox).is(Blocks.DIRT_PATH)) {
               this.placeBlock(world, Blocks.GRASS_BLOCK.defaultBlockState(), 1, -1, -1, structureboundingbox);
            }
         }

         for (int l = 0; l < 6; l++) {
            for (int k = 0; k < 9; k++) {
               this.clearCurrentPositionBlocksUpwards(world, k, 9, l, structureboundingbox);
               this.replaceAirAndLiquidDownwards(world, iblockstate, k, -1, l, structureboundingbox);
            }
         }

         this.spawnVillagers(world, structureboundingbox, 2, 1, 2, 1);
         return true;
      }

      @Override
      protected int chooseProfession(int i, int j) {
         return 1;
      }
   }

   public static class House2 extends VillagePieces.Village {
      private boolean hasMadeChest;

      House2(CompoundTag tag) {
         super(tag);
      }

      public House2(
         VillagePieces.Start structurevillagepieces$start, int i, Random random, BoundingBox structureboundingbox, Direction enumfacing
      ) {
         super(structurevillagepieces$start, i);
         this.setOrientation(enumfacing);
         this.boundingBox = structureboundingbox;
      }

      public static VillagePieces.House2 createPiece(
         VillagePieces.Start structurevillagepieces$start,
         List<StructurePiece> list,
         Random random,
         int i,
         int j,
         int k,
         Direction enumfacing,
         int l
      ) {
         BoundingBox structureboundingbox = BoundingBox.orientBox(i, j, k, 0, 0, 0, 10, 6, 7, enumfacing);
         return canVillageGoDeeper(structureboundingbox) && findIntersecting(list, structureboundingbox) == null
            ? new VillagePieces.House2(structurevillagepieces$start, l, random, structureboundingbox, enumfacing)
            : null;
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag nbttagcompound) {
         super.addAdditionalSaveData(context, nbttagcompound);
         nbttagcompound.putBoolean("Chest", this.hasMadeChest);
      }

      @Override
      void read(CompoundTag nbttagcompound) {
         super.read(nbttagcompound);
         this.hasMadeChest = nbttagcompound.getBoolean("Chest");
      }

      @Override
      public boolean addComponentParts(WorldGenLevel world, Random random, BoundingBox structureboundingbox) {
         if (this.averageGroundLvl < 0) {
            this.averageGroundLvl = this.getAverageGroundLevel(world, structureboundingbox);
            if (this.averageGroundLvl < 0) {
               return true;
            }

            this.boundingBox.move(0, this.averageGroundLvl - this.boundingBox.maxY() + 6 - 1, 0);
         }

         BlockState iblockstate = Blocks.COBBLESTONE.defaultBlockState();
         BlockState iblockstate1 = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
         BlockState iblockstate2 = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST));
         BlockState iblockstate3 = this.getBiomeSpecificBlockState(Blocks.OAK_PLANKS.defaultBlockState());
         BlockState iblockstate4 = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
         BlockState iblockstate5 = this.getBiomeSpecificBlockState(Blocks.OAK_LOG.defaultBlockState());
         BlockState iblockstate6 = this.getBiomeSpecificBlockState(Blocks.OAK_FENCE.defaultBlockState());
         this.generateBox(world, structureboundingbox, 0, 1, 0, 9, 4, 6, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(world, structureboundingbox, 0, 0, 0, 9, 0, 6, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 0, 4, 0, 9, 4, 6, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 0, 5, 0, 9, 5, 6, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
         this.generateBox(world, structureboundingbox, 1, 5, 1, 8, 5, 5, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(world, structureboundingbox, 1, 1, 0, 2, 3, 0, iblockstate3, iblockstate3, false);
         this.generateBox(world, structureboundingbox, 0, 1, 0, 0, 4, 0, iblockstate5, iblockstate5, false);
         this.generateBox(world, structureboundingbox, 3, 1, 0, 3, 4, 0, iblockstate5, iblockstate5, false);
         this.generateBox(world, structureboundingbox, 0, 1, 6, 0, 4, 6, iblockstate5, iblockstate5, false);
         this.placeBlock(world, iblockstate3, 3, 3, 1, structureboundingbox);
         this.generateBox(world, structureboundingbox, 3, 1, 2, 3, 3, 2, iblockstate3, iblockstate3, false);
         this.generateBox(world, structureboundingbox, 4, 1, 3, 5, 3, 3, iblockstate3, iblockstate3, false);
         this.generateBox(world, structureboundingbox, 0, 1, 1, 0, 3, 5, iblockstate3, iblockstate3, false);
         this.generateBox(world, structureboundingbox, 1, 1, 6, 5, 3, 6, iblockstate3, iblockstate3, false);
         this.generateBox(world, structureboundingbox, 5, 1, 0, 5, 3, 0, iblockstate6, iblockstate6, false);
         this.generateBox(world, structureboundingbox, 9, 1, 0, 9, 3, 0, iblockstate6, iblockstate6, false);
         this.generateBox(world, structureboundingbox, 6, 1, 4, 9, 4, 6, iblockstate, iblockstate, false);
         this.placeBlock(world, Blocks.LAVA.defaultBlockState(), 7, 1, 5, structureboundingbox);
         this.placeBlock(world, Blocks.LAVA.defaultBlockState(), 8, 1, 5, structureboundingbox);
         this.placeBlock(world, Blocks.IRON_BARS.defaultBlockState(), 9, 2, 5, structureboundingbox);
         this.placeBlock(world, Blocks.IRON_BARS.defaultBlockState(), 9, 2, 4, structureboundingbox);
         this.generateBox(world, structureboundingbox, 7, 2, 4, 8, 2, 5, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.placeBlock(world, iblockstate, 6, 1, 3, structureboundingbox);
         this.placeBlock(world, Blocks.FURNACE.defaultBlockState(), 6, 2, 3, structureboundingbox);
         this.placeBlock(world, Blocks.FURNACE.defaultBlockState(), 6, 3, 3, structureboundingbox);
         this.placeBlock(world, DOUBLE_STONE_SLAB, 8, 1, 1, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 0, 2, 2, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 0, 2, 4, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 2, 2, 6, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 4, 2, 6, structureboundingbox);
         this.placeBlock(world, iblockstate6, 2, 1, 4, structureboundingbox);
         this.placeBlock(world, Blocks.OAK_PRESSURE_PLATE.defaultBlockState(), 2, 2, 4, structureboundingbox);
         this.placeBlock(world, iblockstate3, 1, 1, 5, structureboundingbox);
         this.placeBlock(world, iblockstate1, 2, 1, 5, structureboundingbox);
         this.placeBlock(world, iblockstate2, 1, 1, 4, structureboundingbox);
         if (!this.hasMadeChest && structureboundingbox.isInside(new BlockPos(this.getWorldX(5, 5), this.getWorldY(1), this.getWorldZ(5, 5)))
            )
          {
            this.hasMadeChest = true;
            this.generateChest(world, structureboundingbox, random, 5, 1, 5, BuiltInLootTables.VILLAGE_WEAPONSMITH);
         }

         for (int i = 6; i <= 8; i++) {
            if (this.getBlock(world, i, 0, -1, structureboundingbox).isAir()
               && this.getBlock(world, i, -1, -1, structureboundingbox).isAir() == false) {
               this.placeBlock(world, iblockstate4, i, 0, -1, structureboundingbox);
               if (this.getBlock(world, i, -1, -1, structureboundingbox).is(Blocks.DIRT_PATH)) {
                  this.placeBlock(world, Blocks.GRASS_BLOCK.defaultBlockState(), i, -1, -1, structureboundingbox);
               }
            }
         }

         for (int k = 0; k < 7; k++) {
            for (int j = 0; j < 10; j++) {
               this.clearCurrentPositionBlocksUpwards(world, j, 6, k, structureboundingbox);
               this.replaceAirAndLiquidDownwards(world, iblockstate, j, -1, k, structureboundingbox);
            }
         }

         this.spawnVillagers(world, structureboundingbox, 7, 1, 1, 1);
         return true;
      }

      @Override
      protected int chooseProfession(int i, int j) {
         return 3;
      }
   }

   public static class House3 extends VillagePieces.Village {
      House3(CompoundTag tag) {
         super(tag);
      }

      public House3(
         VillagePieces.Start structurevillagepieces$start, int i, Random random, BoundingBox structureboundingbox, Direction enumfacing
      ) {
         super(structurevillagepieces$start, i);
         this.setOrientation(enumfacing);
         this.boundingBox = structureboundingbox;
      }

      public static VillagePieces.House3 createPiece(
         VillagePieces.Start structurevillagepieces$start,
         List<StructurePiece> list,
         Random random,
         int i,
         int j,
         int k,
         Direction enumfacing,
         int l
      ) {
         BoundingBox structureboundingbox = BoundingBox.orientBox(i, j, k, 0, 0, 0, 9, 7, 12, enumfacing);
         return canVillageGoDeeper(structureboundingbox) && findIntersecting(list, structureboundingbox) == null
            ? new VillagePieces.House3(structurevillagepieces$start, l, random, structureboundingbox, enumfacing)
            : null;
      }

      @Override
      public boolean addComponentParts(WorldGenLevel world, Random random, BoundingBox structureboundingbox) {
         if (this.averageGroundLvl < 0) {
            this.averageGroundLvl = this.getAverageGroundLevel(world, structureboundingbox);
            if (this.averageGroundLvl < 0) {
               return true;
            }

            this.boundingBox.move(0, this.averageGroundLvl - this.boundingBox.maxY() + 7 - 1, 0);
         }

         BlockState iblockstate = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE.defaultBlockState());
         BlockState iblockstate1 = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
         BlockState iblockstate2 = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH));
         BlockState iblockstate3 = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST));
         BlockState iblockstate4 = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST));
         BlockState iblockstate5 = this.getBiomeSpecificBlockState(Blocks.OAK_PLANKS.defaultBlockState());
         BlockState iblockstate6 = this.getBiomeSpecificBlockState(Blocks.OAK_LOG.defaultBlockState());
         this.generateBox(world, structureboundingbox, 1, 1, 1, 7, 4, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(world, structureboundingbox, 2, 1, 6, 8, 4, 10, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(world, structureboundingbox, 2, 0, 5, 8, 0, 10, iblockstate5, iblockstate5, false);
         this.generateBox(world, structureboundingbox, 1, 0, 1, 7, 0, 4, iblockstate5, iblockstate5, false);
         this.generateBox(world, structureboundingbox, 0, 0, 0, 0, 3, 5, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 8, 0, 0, 8, 3, 10, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 1, 0, 0, 7, 2, 0, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 1, 0, 5, 2, 1, 5, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 2, 0, 6, 2, 3, 10, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 3, 0, 10, 7, 3, 10, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 1, 2, 0, 7, 3, 0, iblockstate5, iblockstate5, false);
         this.generateBox(world, structureboundingbox, 1, 2, 5, 2, 3, 5, iblockstate5, iblockstate5, false);
         this.generateBox(world, structureboundingbox, 0, 4, 1, 8, 4, 1, iblockstate5, iblockstate5, false);
         this.generateBox(world, structureboundingbox, 0, 4, 4, 3, 4, 4, iblockstate5, iblockstate5, false);
         this.generateBox(world, structureboundingbox, 0, 5, 2, 8, 5, 3, iblockstate5, iblockstate5, false);
         this.placeBlock(world, iblockstate5, 0, 4, 2, structureboundingbox);
         this.placeBlock(world, iblockstate5, 0, 4, 3, structureboundingbox);
         this.placeBlock(world, iblockstate5, 8, 4, 2, structureboundingbox);
         this.placeBlock(world, iblockstate5, 8, 4, 3, structureboundingbox);
         this.placeBlock(world, iblockstate5, 8, 4, 4, structureboundingbox);
         BlockState iblockstate7 = iblockstate1;
         BlockState iblockstate8 = iblockstate2;
         BlockState iblockstate9 = iblockstate4;
         BlockState iblockstate10 = iblockstate3;

         for (int i = -1; i <= 2; i++) {
            for (int j = 0; j <= 8; j++) {
               this.placeBlock(world, iblockstate7, j, 4 + i, i, structureboundingbox);
               if ((i > -1 || j <= 1) && (i > 0 || j <= 3) && (i > 1 || j <= 4 || j >= 6)) {
                  this.placeBlock(world, iblockstate8, j, 4 + i, 5 - i, structureboundingbox);
               }
            }
         }

         this.generateBox(world, structureboundingbox, 3, 4, 5, 3, 4, 10, iblockstate5, iblockstate5, false);
         this.generateBox(world, structureboundingbox, 7, 4, 2, 7, 4, 10, iblockstate5, iblockstate5, false);
         this.generateBox(world, structureboundingbox, 4, 5, 4, 4, 5, 10, iblockstate5, iblockstate5, false);
         this.generateBox(world, structureboundingbox, 6, 5, 4, 6, 5, 10, iblockstate5, iblockstate5, false);
         this.generateBox(world, structureboundingbox, 5, 6, 3, 5, 6, 10, iblockstate5, iblockstate5, false);

         for (int k = 4; k >= 1; k--) {
            this.placeBlock(world, iblockstate5, k, 2 + k, 7 - k, structureboundingbox);

            for (int k1 = 8 - k; k1 <= 10; k1++) {
               this.placeBlock(world, iblockstate10, k, 2 + k, k1, structureboundingbox);
            }
         }

         this.placeBlock(world, iblockstate5, 6, 6, 3, structureboundingbox);
         this.placeBlock(world, iblockstate5, 7, 5, 4, structureboundingbox);
         this.placeBlock(world, iblockstate4, 6, 6, 4, structureboundingbox);

         for (int l = 6; l <= 8; l++) {
            for (int l1 = 5; l1 <= 10; l1++) {
               this.placeBlock(world, iblockstate9, l, 12 - l, l1, structureboundingbox);
            }
         }

         this.placeBlock(world, iblockstate6, 0, 2, 1, structureboundingbox);
         this.placeBlock(world, iblockstate6, 0, 2, 4, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 0, 2, 2, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 0, 2, 3, structureboundingbox);
         this.placeBlock(world, iblockstate6, 4, 2, 0, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 5, 2, 0, structureboundingbox);
         this.placeBlock(world, iblockstate6, 6, 2, 0, structureboundingbox);
         this.placeBlock(world, iblockstate6, 8, 2, 1, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 8, 2, 2, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 8, 2, 3, structureboundingbox);
         this.placeBlock(world, iblockstate6, 8, 2, 4, structureboundingbox);
         this.placeBlock(world, iblockstate5, 8, 2, 5, structureboundingbox);
         this.placeBlock(world, iblockstate6, 8, 2, 6, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 8, 2, 7, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 8, 2, 8, structureboundingbox);
         this.placeBlock(world, iblockstate6, 8, 2, 9, structureboundingbox);
         this.placeBlock(world, iblockstate6, 2, 2, 6, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 2, 2, 7, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 2, 2, 8, structureboundingbox);
         this.placeBlock(world, iblockstate6, 2, 2, 9, structureboundingbox);
         this.placeBlock(world, iblockstate6, 4, 4, 10, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 5, 4, 10, structureboundingbox);
         this.placeBlock(world, iblockstate6, 6, 4, 10, structureboundingbox);
         this.placeBlock(world, iblockstate5, 5, 5, 10, structureboundingbox);
         this.placeBlock(world, Blocks.AIR.defaultBlockState(), 2, 1, 0, structureboundingbox);
         this.placeBlock(world, Blocks.AIR.defaultBlockState(), 2, 2, 0, structureboundingbox);
         this.placeTorch(world, Direction.NORTH, 2, 3, 1, structureboundingbox);
         this.createVillageDoor(world, structureboundingbox, random, 2, 1, 0, Direction.NORTH);
         this.generateBox(world, structureboundingbox, 1, 0, -1, 3, 2, -1, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         if (this.getBlock(world, 2, 0, -1, structureboundingbox).isAir()
            && this.getBlock(world, 2, -1, -1, structureboundingbox).isAir() == false) {
            this.placeBlock(world, iblockstate7, 2, 0, -1, structureboundingbox);
            if (this.getBlock(world, 2, -1, -1, structureboundingbox).is(Blocks.DIRT_PATH)) {
               this.placeBlock(world, Blocks.GRASS_BLOCK.defaultBlockState(), 2, -1, -1, structureboundingbox);
            }
         }

         for (int i1 = 0; i1 < 5; i1++) {
            for (int i2 = 0; i2 < 9; i2++) {
               this.clearCurrentPositionBlocksUpwards(world, i2, 7, i1, structureboundingbox);
               this.replaceAirAndLiquidDownwards(world, iblockstate, i2, -1, i1, structureboundingbox);
            }
         }

         for (int j1 = 5; j1 < 11; j1++) {
            for (int j2 = 2; j2 < 9; j2++) {
               this.clearCurrentPositionBlocksUpwards(world, j2, 7, j1, structureboundingbox);
               this.replaceAirAndLiquidDownwards(world, iblockstate, j2, -1, j1, structureboundingbox);
            }
         }

         this.spawnVillagers(world, structureboundingbox, 4, 1, 2, 2);
         return true;
      }
   }

   public static class House4Garden extends VillagePieces.Village {
      private boolean isRoofAccessible;

      public boolean roofAccessible() {
         return this.isRoofAccessible;
      }

      House4Garden(CompoundTag tag) {
         super(tag);
      }

      public House4Garden(
         VillagePieces.Start structurevillagepieces$start, int i, Random random, BoundingBox structureboundingbox, Direction enumfacing
      ) {
         super(structurevillagepieces$start, i);
         this.setOrientation(enumfacing);
         this.boundingBox = structureboundingbox;
         this.isRoofAccessible = random.nextBoolean();
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag nbttagcompound) {
         super.addAdditionalSaveData(context, nbttagcompound);
         nbttagcompound.putBoolean("Terrace", this.isRoofAccessible);
      }

      @Override
      void read(CompoundTag nbttagcompound) {
         super.read(nbttagcompound);
         this.isRoofAccessible = nbttagcompound.getBoolean("Terrace");
      }

      public static VillagePieces.House4Garden createPiece(
         VillagePieces.Start structurevillagepieces$start,
         List<StructurePiece> list,
         Random random,
         int i,
         int j,
         int k,
         Direction enumfacing,
         int l
      ) {
         BoundingBox structureboundingbox = BoundingBox.orientBox(i, j, k, 0, 0, 0, 5, 6, 5, enumfacing);
         return findIntersecting(list, structureboundingbox) != null
            ? null
            : new VillagePieces.House4Garden(structurevillagepieces$start, l, random, structureboundingbox, enumfacing);
      }

      @Override
      public boolean addComponentParts(WorldGenLevel world, Random random, BoundingBox structureboundingbox) {
         if (this.averageGroundLvl < 0) {
            this.averageGroundLvl = this.getAverageGroundLevel(world, structureboundingbox);
            if (this.averageGroundLvl < 0) {
               return true;
            }

            this.boundingBox.move(0, this.averageGroundLvl - this.boundingBox.maxY() + 6 - 1, 0);
         }

         BlockState iblockstate = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE.defaultBlockState());
         BlockState iblockstate1 = this.getBiomeSpecificBlockState(Blocks.OAK_PLANKS.defaultBlockState());
         BlockState iblockstate2 = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
         BlockState iblockstate3 = this.getBiomeSpecificBlockState(Blocks.OAK_LOG.defaultBlockState());
         BlockState iblockstate4 = this.getBiomeSpecificBlockState(Blocks.OAK_FENCE.defaultBlockState());
         this.generateBox(world, structureboundingbox, 0, 0, 0, 4, 0, 4, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 0, 4, 0, 4, 4, 4, iblockstate3, iblockstate3, false);
         this.generateBox(world, structureboundingbox, 1, 4, 1, 3, 4, 3, iblockstate1, iblockstate1, false);
         this.placeBlock(world, iblockstate, 0, 1, 0, structureboundingbox);
         this.placeBlock(world, iblockstate, 0, 2, 0, structureboundingbox);
         this.placeBlock(world, iblockstate, 0, 3, 0, structureboundingbox);
         this.placeBlock(world, iblockstate, 4, 1, 0, structureboundingbox);
         this.placeBlock(world, iblockstate, 4, 2, 0, structureboundingbox);
         this.placeBlock(world, iblockstate, 4, 3, 0, structureboundingbox);
         this.placeBlock(world, iblockstate, 0, 1, 4, structureboundingbox);
         this.placeBlock(world, iblockstate, 0, 2, 4, structureboundingbox);
         this.placeBlock(world, iblockstate, 0, 3, 4, structureboundingbox);
         this.placeBlock(world, iblockstate, 4, 1, 4, structureboundingbox);
         this.placeBlock(world, iblockstate, 4, 2, 4, structureboundingbox);
         this.placeBlock(world, iblockstate, 4, 3, 4, structureboundingbox);
         this.generateBox(world, structureboundingbox, 0, 1, 1, 0, 3, 3, iblockstate1, iblockstate1, false);
         this.generateBox(world, structureboundingbox, 4, 1, 1, 4, 3, 3, iblockstate1, iblockstate1, false);
         this.generateBox(world, structureboundingbox, 1, 1, 4, 3, 3, 4, iblockstate1, iblockstate1, false);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 0, 2, 2, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 2, 2, 4, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 4, 2, 2, structureboundingbox);
         this.placeBlock(world, iblockstate1, 1, 1, 0, structureboundingbox);
         this.placeBlock(world, iblockstate1, 1, 2, 0, structureboundingbox);
         this.placeBlock(world, iblockstate1, 1, 3, 0, structureboundingbox);
         this.placeBlock(world, iblockstate1, 2, 3, 0, structureboundingbox);
         this.placeBlock(world, iblockstate1, 3, 3, 0, structureboundingbox);
         this.placeBlock(world, iblockstate1, 3, 2, 0, structureboundingbox);
         this.placeBlock(world, iblockstate1, 3, 1, 0, structureboundingbox);
         if (this.getBlock(world, 2, 0, -1, structureboundingbox).isAir()
            && this.getBlock(world, 2, -1, -1, structureboundingbox).isAir() == false) {
            this.placeBlock(world, iblockstate2, 2, 0, -1, structureboundingbox);
            if (this.getBlock(world, 2, -1, -1, structureboundingbox).is(Blocks.DIRT_PATH)) {
               this.placeBlock(world, Blocks.GRASS_BLOCK.defaultBlockState(), 2, -1, -1, structureboundingbox);
            }
         }

         this.generateBox(world, structureboundingbox, 1, 1, 1, 3, 3, 3, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         if (this.isRoofAccessible) {
            this.placeBlock(world, iblockstate4, 0, 5, 0, structureboundingbox);
            this.placeBlock(world, iblockstate4, 1, 5, 0, structureboundingbox);
            this.placeBlock(world, iblockstate4, 2, 5, 0, structureboundingbox);
            this.placeBlock(world, iblockstate4, 3, 5, 0, structureboundingbox);
            this.placeBlock(world, iblockstate4, 4, 5, 0, structureboundingbox);
            this.placeBlock(world, iblockstate4, 0, 5, 4, structureboundingbox);
            this.placeBlock(world, iblockstate4, 1, 5, 4, structureboundingbox);
            this.placeBlock(world, iblockstate4, 2, 5, 4, structureboundingbox);
            this.placeBlock(world, iblockstate4, 3, 5, 4, structureboundingbox);
            this.placeBlock(world, iblockstate4, 4, 5, 4, structureboundingbox);
            this.placeBlock(world, iblockstate4, 4, 5, 1, structureboundingbox);
            this.placeBlock(world, iblockstate4, 4, 5, 2, structureboundingbox);
            this.placeBlock(world, iblockstate4, 4, 5, 3, structureboundingbox);
            this.placeBlock(world, iblockstate4, 0, 5, 1, structureboundingbox);
            this.placeBlock(world, iblockstate4, 0, 5, 2, structureboundingbox);
            this.placeBlock(world, iblockstate4, 0, 5, 3, structureboundingbox);
         }

         if (this.isRoofAccessible) {
            BlockState iblockstate5 = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH);
            this.placeBlock(world, iblockstate5, 3, 1, 3, structureboundingbox);
            this.placeBlock(world, iblockstate5, 3, 2, 3, structureboundingbox);
            this.placeBlock(world, iblockstate5, 3, 3, 3, structureboundingbox);
            this.placeBlock(world, iblockstate5, 3, 4, 3, structureboundingbox);
         }

         this.placeTorch(world, Direction.NORTH, 2, 3, 1, structureboundingbox);

         for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 5; i++) {
               this.clearCurrentPositionBlocksUpwards(world, i, 6, j, structureboundingbox);
               this.replaceAirAndLiquidDownwards(world, iblockstate, i, -1, j, structureboundingbox);
            }
         }

         this.spawnVillagers(world, structureboundingbox, 1, 1, 2, 1);
         return true;
      }
   }

   public static class Path extends VillagePieces.Road {
      private int length;

      Path(CompoundTag tag) {
         super(tag);
      }

      public Path(
         VillagePieces.Start structurevillagepieces$start, int i, Random random, BoundingBox structureboundingbox, Direction enumfacing
      ) {
         super(structurevillagepieces$start, i);
         this.setOrientation(enumfacing);
         this.boundingBox = structureboundingbox;
         this.length = Math.max(structureboundingbox.getXSpan(), structureboundingbox.getZSpan());
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag nbttagcompound) {
         super.addAdditionalSaveData(context, nbttagcompound);
         nbttagcompound.putInt("Length", this.length);
      }

      @Override
      void read(CompoundTag nbttagcompound) {
         super.read(nbttagcompound);
         this.length = nbttagcompound.getInt("Length");
      }

      @Override
      public void buildComponent(StructurePiece structurecomponent, List<StructurePiece> list, Random random) {
         boolean flag = false;

         for (int i = random.nextInt(5); i < this.length - 8; i += 2 + random.nextInt(5)) {
            StructurePiece structurecomponent1 = this.getNextComponentNN((VillagePieces.Start)structurecomponent, list, random, 0, i);
            if (structurecomponent1 != null) {
               i += Math.max(structurecomponent1.getBoundingBox().getXSpan(), structurecomponent1.getBoundingBox().getZSpan());
               flag = true;
            }
         }

         for (int j = random.nextInt(5); j < this.length - 8; j += 2 + random.nextInt(5)) {
            StructurePiece structurecomponent2 = this.getNextComponentPP((VillagePieces.Start)structurecomponent, list, random, 0, j);
            if (structurecomponent2 != null) {
               j += Math.max(structurecomponent2.getBoundingBox().getXSpan(), structurecomponent2.getBoundingBox().getZSpan());
               flag = true;
            }
         }

         Direction enumfacing = this.getOrientation();
         if (flag && random.nextInt(3) > 0 && enumfacing != null) {
            switch (enumfacing) {
               case NORTH:
               default:
                  VillagePieces.generateAndAddRoadPiece(
                     (VillagePieces.Start)structurecomponent,
                     list,
                     random,
                     this.boundingBox.minX() - 1,
                     this.boundingBox.minY(),
                     this.boundingBox.minZ(),
                     Direction.WEST,
                     this.getGenDepth()
                  );
                  break;
               case SOUTH:
                  VillagePieces.generateAndAddRoadPiece(
                     (VillagePieces.Start)structurecomponent,
                     list,
                     random,
                     this.boundingBox.minX() - 1,
                     this.boundingBox.minY(),
                     this.boundingBox.maxZ() - 2,
                     Direction.WEST,
                     this.getGenDepth()
                  );
                  break;
               case WEST:
                  VillagePieces.generateAndAddRoadPiece(
                     (VillagePieces.Start)structurecomponent,
                     list,
                     random,
                     this.boundingBox.minX(),
                     this.boundingBox.minY(),
                     this.boundingBox.minZ() - 1,
                     Direction.NORTH,
                     this.getGenDepth()
                  );
                  break;
               case EAST:
                  VillagePieces.generateAndAddRoadPiece(
                     (VillagePieces.Start)structurecomponent,
                     list,
                     random,
                     this.boundingBox.maxX() - 2,
                     this.boundingBox.minY(),
                     this.boundingBox.minZ() - 1,
                     Direction.NORTH,
                     this.getGenDepth()
                  );
            }
         }

         if (flag && random.nextInt(3) > 0 && enumfacing != null) {
            switch (enumfacing) {
               case NORTH:
               default:
                  VillagePieces.generateAndAddRoadPiece(
                     (VillagePieces.Start)structurecomponent,
                     list,
                     random,
                     this.boundingBox.maxX() + 1,
                     this.boundingBox.minY(),
                     this.boundingBox.minZ(),
                     Direction.EAST,
                     this.getGenDepth()
                  );
                  break;
               case SOUTH:
                  VillagePieces.generateAndAddRoadPiece(
                     (VillagePieces.Start)structurecomponent,
                     list,
                     random,
                     this.boundingBox.maxX() + 1,
                     this.boundingBox.minY(),
                     this.boundingBox.maxZ() - 2,
                     Direction.EAST,
                     this.getGenDepth()
                  );
                  break;
               case WEST:
                  VillagePieces.generateAndAddRoadPiece(
                     (VillagePieces.Start)structurecomponent,
                     list,
                     random,
                     this.boundingBox.minX(),
                     this.boundingBox.minY(),
                     this.boundingBox.maxZ() + 1,
                     Direction.SOUTH,
                     this.getGenDepth()
                  );
                  break;
               case EAST:
                  VillagePieces.generateAndAddRoadPiece(
                     (VillagePieces.Start)structurecomponent,
                     list,
                     random,
                     this.boundingBox.maxX() - 2,
                     this.boundingBox.minY(),
                     this.boundingBox.maxZ() + 1,
                     Direction.SOUTH,
                     this.getGenDepth()
                  );
            }
         }
      }

      public static BoundingBox findPieceBox(
         VillagePieces.Start structurevillagepieces$start, List<StructurePiece> list, Random random, int i, int j, int k, Direction enumfacing
      ) {
         for (int l = 7 * randInt(random, 3, 5); l >= 7; l -= 7) {
            BoundingBox structureboundingbox = BoundingBox.orientBox(i, j, k, 0, 0, 0, 3, 3, l, enumfacing);
            if (findIntersecting(list, structureboundingbox) == null) {
               return structureboundingbox;
            }
         }

         return null;
      }

      @Override
      public boolean addComponentParts(WorldGenLevel world, Random random, BoundingBox structureboundingbox) {
         BlockState iblockstate = this.getBiomeSpecificBlockState(Blocks.DIRT_PATH.defaultBlockState());
         BlockState iblockstate1 = this.getBiomeSpecificBlockState(Blocks.OAK_PLANKS.defaultBlockState());
         BlockState iblockstate2 = this.getBiomeSpecificBlockState(Blocks.GRAVEL.defaultBlockState());
         BlockState iblockstate3 = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE.defaultBlockState());

         for (int i = this.boundingBox.minX(); i <= this.boundingBox.maxX(); i++) {
            for (int j = this.boundingBox.minZ(); j <= this.boundingBox.maxZ(); j++) {
               BlockPos blockpos = new BlockPos(i, 64, j);
               if (structureboundingbox.isInside(blockpos)) {
                  blockpos = topSolidOrLiquid(world, blockpos).below();
                  if (blockpos.getY() < SEA_LEVEL) {
                     blockpos = new BlockPos(blockpos.getX(), SEA_LEVEL - 1, blockpos.getZ());
                  }

                  while (blockpos.getY() >= SEA_LEVEL - 1) {
                     BlockState iblockstate4 = world.getBlockState(blockpos);
                     if (iblockstate4.is(Blocks.GRASS_BLOCK) && isAirOrLaterPlant(world, blockpos.above())) {
                        clearPlant(world, blockpos.above());
                        world.setBlock(blockpos, iblockstate, 2);
                        break;
                     }

                     if (iblockstate4.liquid()) {
                        world.setBlock(blockpos, iblockstate1, 2);
                        break;
                     }

                     if (iblockstate4.is(Blocks.SAND)
                        || iblockstate4.is(Blocks.SANDSTONE)
                        || iblockstate4.is(Blocks.RED_SANDSTONE)) {
                        world.setBlock(blockpos, iblockstate2, 2);
                        world.setBlock(blockpos.below(), iblockstate3, 2);
                        break;
                     }

                     blockpos = blockpos.below();
                  }
               }
            }
         }

         return true;
      }
   }

   public static class PieceWeight {
      public Class<? extends VillagePieces.Village> villagePieceClass;
      public final int villagePieceWeight;
      public int villagePiecesSpawned;
      public int villagePiecesLimit;
      RcHandler rc;

      public PieceWeight(Class<? extends VillagePieces.Village> oclass, int i, int j) {
         this.villagePieceClass = oclass;
         this.villagePieceWeight = i;
         this.villagePiecesLimit = j;
      }

      public boolean canSpawnMoreVillagePiecesOfType(int i) {
         return this.villagePiecesLimit == 0 || this.villagePiecesSpawned < this.villagePiecesLimit;
      }

      public boolean canSpawnMoreVillagePieces() {
         return this.villagePiecesLimit == 0 || this.villagePiecesSpawned < this.villagePiecesLimit;
      }
   }

   public abstract static class Road extends VillagePieces.Village {
      Road(CompoundTag tag) {
         super(tag);
      }

      protected Road(VillagePieces.Start structurevillagepieces$start, int i) {
         super(structurevillagepieces$start, i);
      }
   }


   public static class Start extends VillagePieces.Well {
      public int terrainType;
      public VillagePieces.PieceWeight lastPlaced;
      public List<VillagePieces.PieceWeight> structureVillageWeightedPieceList;
      public List<StructurePiece> pendingHouses = new ArrayList<>();
      public List<StructurePiece> pendingRoads = new ArrayList<>();
      /** Forge: the start biome (BiomeProvider.getBiome at the start position). */
      public Biome biome;

      Start(CompoundTag tag) {
         super(tag);
      }

      public Start(Biome biome, int i, Random random, int j, int k, List<VillagePieces.PieceWeight> list, int l) {
         super(null, 0, random, j, k);
         this.structureVillageWeightedPieceList = list;
         this.terrainType = l;
         this.biome = biome;
         this.startPiece = this;
         this.startBiome = biome.getRegistryName();
         if (biome instanceof BiomeDesert) {
            this.structureType = 1;
         } else if (biome instanceof BiomeSavanna) {
            this.structureType = 2;
         } else if (biome instanceof BiomeTaiga) {
            this.structureType = 3;
         }

         this.setStructureType(this.structureType);
         this.isZombieInfested = random.nextInt(50) == 0;
      }
   }

   public static class Torch extends VillagePieces.Village {
      Torch(CompoundTag tag) {
         super(tag);
      }

      public Torch(
         VillagePieces.Start structurevillagepieces$start, int i, Random random, BoundingBox structureboundingbox, Direction enumfacing
      ) {
         super(structurevillagepieces$start, i);
         this.setOrientation(enumfacing);
         this.boundingBox = structureboundingbox;
      }

      public static BoundingBox findPieceBox(
         VillagePieces.Start structurevillagepieces$start, List<StructurePiece> list, Random random, int i, int j, int k, Direction enumfacing
      ) {
         BoundingBox structureboundingbox = BoundingBox.orientBox(i, j, k, 0, 0, 0, 3, 4, 2, enumfacing);
         return findIntersecting(list, structureboundingbox) != null ? null : structureboundingbox;
      }

      @Override
      public boolean addComponentParts(WorldGenLevel world, Random random, BoundingBox structureboundingbox) {
         if (this.averageGroundLvl < 0) {
            this.averageGroundLvl = this.getAverageGroundLevel(world, structureboundingbox);
            if (this.averageGroundLvl < 0) {
               return true;
            }

            this.boundingBox.move(0, this.averageGroundLvl - this.boundingBox.maxY() + 4 - 1, 0);
         }

         BlockState iblockstate = this.getBiomeSpecificBlockState(Blocks.OAK_FENCE.defaultBlockState());
         this.generateBox(world, structureboundingbox, 0, 0, 0, 2, 3, 1, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.placeBlock(world, iblockstate, 1, 0, 0, structureboundingbox);
         this.placeBlock(world, iblockstate, 1, 1, 0, structureboundingbox);
         this.placeBlock(world, iblockstate, 1, 2, 0, structureboundingbox);
         this.placeBlock(world, Blocks.WHITE_WOOL.defaultBlockState(), 1, 3, 0, structureboundingbox);
         this.placeTorch(world, Direction.EAST, 2, 3, 0, structureboundingbox);
         this.placeTorch(world, Direction.NORTH, 1, 3, 1, structureboundingbox);
         this.placeTorch(world, Direction.WEST, 0, 3, 0, structureboundingbox);
         this.placeTorch(world, Direction.SOUTH, 1, 3, -1, structureboundingbox);
         return true;
      }
   }


   public abstract static class Village extends StructurePiece {
      protected int averageGroundLvl = -1;
      private int villagersSpawned;
      protected int structureType;
      protected boolean isZombieInfested;
      protected VillagePieces.Start startPiece;
      /** The start biome's 1.12 name, for GetVillageBlockID (kept when the piece is saved, unlike Forge's start piece). */
      protected String startBiome;

      Village(CompoundTag tag) {
         super(RLGen.VILLAGE_PIECE.get(), tag);
      }

      protected Village(VillagePieces.Start structurevillagepieces$start, int i) {
         super(RLGen.VILLAGE_PIECE.get(), i, new BoundingBox(0, 0, 0, 0, 0, 0));
         if (structurevillagepieces$start != null) {
            this.structureType = structurevillagepieces$start.structureType;
            this.isZombieInfested = structurevillagepieces$start.isZombieInfested;
            this.startPiece = structurevillagepieces$start;
            this.startBiome = structurevillagepieces$start.startBiome;
         }
      }

      void setBox(BoundingBox box) {
         this.boundingBox = box;
      }

      public boolean zombie() {
         return this.isZombieInfested;
      }

      /** StructureComponent.buildComponent */
      public void buildComponent(StructurePiece structurecomponent, List<StructurePiece> list, Random random) {
      }

      /** StructureComponent.addComponentParts */
      public abstract boolean addComponentParts(WorldGenLevel world, Random random, BoundingBox structureboundingbox);

      @Override
      public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource,
                              BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {
         try {
            this.addComponentParts(level, VillageRandom.forChunk(level.getSeed(), chunkPos), box);
            // Charm VillageDecorations (StructureEventBase.Post after each piece)
            if (chunkGenerator instanceof com.nukevscity.rlgen.world.LegacyChunkGenerator generator) {
               com.nukevscity.rlgen.charm.Inner.decorate(this, level, box, generator);
            }
         } catch (RuntimeException e) {
            LOGGER.error("rlgen: village piece {} failed in chunk {}", this.getClass().getSimpleName(), chunkPos, e);
         }
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag nbttagcompound) {
         nbttagcompound.putString("VId", this.getClass().getSimpleName());
         nbttagcompound.putInt("HPos", this.averageGroundLvl);
         nbttagcompound.putInt("VCount", this.villagersSpawned);
         nbttagcompound.putByte("Type", (byte)this.structureType);
         nbttagcompound.putBoolean("Zombie", this.isZombieInfested);
         if (this.startBiome != null) {
            nbttagcompound.putString("Biome", this.startBiome);
         }
      }

      void read(CompoundTag nbttagcompound) {
         this.averageGroundLvl = nbttagcompound.getInt("HPos");
         this.villagersSpawned = nbttagcompound.getInt("VCount");
         this.structureType = nbttagcompound.getByte("Type");
         this.isZombieInfested = nbttagcompound.getBoolean("Zombie");
         this.startBiome = nbttagcompound.contains("Biome") ? nbttagcompound.getString("Biome") : null;
      }

      @Nullable
      protected StructurePiece getNextComponentNN(
         VillagePieces.Start structurevillagepieces$start, List<StructurePiece> list, Random random, int i, int j
      ) {
         Direction enumfacing = this.getOrientation();
         if (enumfacing != null) {
            switch (enumfacing) {
               case NORTH:
               default:
                  return VillagePieces.generateAndAddComponent(
                     structurevillagepieces$start,
                     list,
                     random,
                     this.boundingBox.minX() - 1,
                     this.boundingBox.minY() + i,
                     this.boundingBox.minZ() + j,
                     Direction.WEST,
                     this.getGenDepth()
                  );
               case SOUTH:
                  return VillagePieces.generateAndAddComponent(
                     structurevillagepieces$start,
                     list,
                     random,
                     this.boundingBox.minX() - 1,
                     this.boundingBox.minY() + i,
                     this.boundingBox.minZ() + j,
                     Direction.WEST,
                     this.getGenDepth()
                  );
               case WEST:
                  return VillagePieces.generateAndAddComponent(
                     structurevillagepieces$start,
                     list,
                     random,
                     this.boundingBox.minX() + j,
                     this.boundingBox.minY() + i,
                     this.boundingBox.minZ() - 1,
                     Direction.NORTH,
                     this.getGenDepth()
                  );
               case EAST:
                  return VillagePieces.generateAndAddComponent(
                     structurevillagepieces$start,
                     list,
                     random,
                     this.boundingBox.minX() + j,
                     this.boundingBox.minY() + i,
                     this.boundingBox.minZ() - 1,
                     Direction.NORTH,
                     this.getGenDepth()
                  );
            }
         } else {
            return null;
         }
      }

      @Nullable
      protected StructurePiece getNextComponentPP(
         VillagePieces.Start structurevillagepieces$start, List<StructurePiece> list, Random random, int i, int j
      ) {
         Direction enumfacing = this.getOrientation();
         if (enumfacing != null) {
            switch (enumfacing) {
               case NORTH:
               default:
                  return VillagePieces.generateAndAddComponent(
                     structurevillagepieces$start,
                     list,
                     random,
                     this.boundingBox.maxX() + 1,
                     this.boundingBox.minY() + i,
                     this.boundingBox.minZ() + j,
                     Direction.EAST,
                     this.getGenDepth()
                  );
               case SOUTH:
                  return VillagePieces.generateAndAddComponent(
                     structurevillagepieces$start,
                     list,
                     random,
                     this.boundingBox.maxX() + 1,
                     this.boundingBox.minY() + i,
                     this.boundingBox.minZ() + j,
                     Direction.EAST,
                     this.getGenDepth()
                  );
               case WEST:
                  return VillagePieces.generateAndAddComponent(
                     structurevillagepieces$start,
                     list,
                     random,
                     this.boundingBox.minX() + j,
                     this.boundingBox.minY() + i,
                     this.boundingBox.maxZ() + 1,
                     Direction.SOUTH,
                     this.getGenDepth()
                  );
               case EAST:
                  return VillagePieces.generateAndAddComponent(
                     structurevillagepieces$start,
                     list,
                     random,
                     this.boundingBox.minX() + j,
                     this.boundingBox.minY() + i,
                     this.boundingBox.maxZ() + 1,
                     Direction.SOUTH,
                     this.getGenDepth()
                  );
            }
         } else {
            return null;
         }
      }

      protected int getAverageGroundLevel(WorldGenLevel world, BoundingBox structureboundingbox) {
         int i = 0;
         int j = 0;
         BlockPos.MutableBlockPos mutableblockpos = new BlockPos.MutableBlockPos();

         for (int k = this.boundingBox.minZ(); k <= this.boundingBox.maxZ(); k++) {
            for (int l = this.boundingBox.minX(); l <= this.boundingBox.maxX(); l++) {
               mutableblockpos.set(l, 64, k);
               if (structureboundingbox.isInside(mutableblockpos)) {
                  i += Math.max(topSolidOrLiquid(world, mutableblockpos).getY(), 64 - 1);
                  j++;
               }
            }
         }

         return j == 0 ? -1 : i / j;
      }

      protected static boolean canVillageGoDeeper(BoundingBox structureboundingbox) {
         return structureboundingbox != null && structureboundingbox.minY() > 10;
      }
      protected void spawnVillagers(WorldGenLevel world, BoundingBox structureboundingbox, int i, int j, int k, int l) {
         if (this.villagersSpawned < l) {
            for (int i1 = this.villagersSpawned; i1 < l; i1++) {
               int j1 = this.getWorldX(i + i1, k);
               int k1 = this.getWorldY(j);
               int l1 = this.getWorldZ(i + i1, k);
               if (!structureboundingbox.isInside(j1, k1, l1)) {
                  break;
               }

               this.villagersSpawned++;
               ServerLevel level = world.getLevel();
               BlockPos pos = new BlockPos(j1, k1, l1);
               if (this.isZombieInfested) {
                  ZombieVillager zombie = EntityType.ZOMBIE_VILLAGER.create(level);
                  if (zombie == null) {
                     continue;
                  }
                  zombie.moveTo(j1 + 0.5, k1, l1 + 0.5, 0.0F, 0.0F);
                  zombie.finalizeSpawn(world, world.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
                  zombie.setVillagerData(zombie.getVillagerData().setType(this.villagerType())
                     .setProfession(profession(this.chooseProfession(i1, 0), world.getRandom())));
                  zombie.setPersistenceRequired();
                  world.addFreshEntityWithPassengers(zombie);
               } else {
                  Villager villager = EntityType.VILLAGER.create(level);
                  if (villager == null) {
                     continue;
                  }
                  villager.moveTo(j1 + 0.5, k1, l1 + 0.5, 0.0F, 0.0F);
                  // Forge: VillagerRegistry.setRandomProfession(world.rand), then chooseForgeProfession
                  int prof = this.chooseProfession(i1, world.getRandom().nextInt(6));
                  villager.finalizeSpawn(world, world.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
                  villager.setVillagerData(villager.getVillagerData().setType(this.villagerType()).setProfession(profession(prof, world.getRandom())));
                  if (prof != 5) {
                     villager.setVillagerXp(1); // 1.12 professions were fixed; keep it without a job site block
                  }
                  world.addFreshEntityWithPassengers(villager);
               }
            }
         }
      }

      private VillagerType villagerType() {
         return switch (this.structureType) {
            case 1 -> VillagerType.DESERT;
            case 2 -> VillagerType.SAVANNA;
            case 3 -> VillagerType.TAIGA;
            default -> VillagerType.PLAINS;
         };
      }

      /** 1.12 profession ids and their careers. */
      static VillagerProfession profession(int id, RandomSource random) {
         return switch (id) {
            case 0 -> new VillagerProfession[]{VillagerProfession.FARMER, VillagerProfession.FISHERMAN, VillagerProfession.SHEPHERD,
               VillagerProfession.FLETCHER}[random.nextInt(4)];
            case 1 -> random.nextBoolean() ? VillagerProfession.LIBRARIAN : VillagerProfession.CARTOGRAPHER;
            case 2 -> VillagerProfession.CLERIC;
            case 3 -> new VillagerProfession[]{VillagerProfession.ARMORER, VillagerProfession.WEAPONSMITH, VillagerProfession.TOOLSMITH}[random.nextInt(3)];
            case 4 -> random.nextBoolean() ? VillagerProfession.BUTCHER : VillagerProfession.LEATHERWORKER;
            default -> VillagerProfession.NITWIT;
         };
      }

      protected int chooseProfession(int i, int j) {
         return j;
      }

      protected BlockState getBiomeSpecificBlockState(BlockState iblockstate) {
         return VillageBlocks.biomeSpecific(iblockstate, this.structureType, this.startBiome);
      }

      protected Block biomeDoor() {
         switch (this.structureType) {
            case 2:
               return Blocks.ACACIA_DOOR;
            case 3:
               return Blocks.SPRUCE_DOOR;
            default:
               return Blocks.OAK_DOOR;
         }
      }

      protected void createVillageDoor(WorldGenLevel world, BoundingBox structureboundingbox, Random random, int i, int j, int k, Direction enumfacing) {
         if (!this.isZombieInfested) {
            this.generateDoor(world, structureboundingbox, random, i, j, k, Direction.NORTH, this.biomeDoor());
         }
      }

      /** StructureComponent.generateDoor */
      protected void generateDoor(WorldGenLevel world, BoundingBox box, Random random, int i, int j, int k, Direction facing, Block door) {
         this.placeBlock(world, door.defaultBlockState().setValue(DoorBlock.FACING, facing), i, j, k, box);
         this.placeBlock(world, door.defaultBlockState().setValue(DoorBlock.FACING, facing).setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), i, j + 1, k, box);
      }

      protected void placeTorch(WorldGenLevel world, Direction enumfacing, int i, int j, int k, BoundingBox structureboundingbox) {
         if (!this.isZombieInfested) {
            BlockState torch = enumfacing == Direction.UP
               ? Blocks.TORCH.defaultBlockState()
               : Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, enumfacing);
            this.placeBlock(world, torch, i, j, k, structureboundingbox);
         }
      }

      /** StructureComponent.generateChest */
      protected boolean generateChest(WorldGenLevel world, BoundingBox box, Random random, int i, int j, int k, ResourceKey<LootTable> loot) {
         BlockPos pos = new BlockPos(this.getWorldX(i, k), this.getWorldY(j), this.getWorldZ(i, k));
         if (box.isInside(pos) && !world.getBlockState(pos).is(Blocks.CHEST)) {
            world.setBlock(pos, reorient(world, pos, Blocks.CHEST.defaultBlockState()), 2);
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof ChestBlockEntity chest) {
               chest.setLootTable(loot, random.nextLong());
            }
            return true;
         }
         return false;
      }

      /** StructureComponent.clearCurrentPositionBlocksUpwards */
      protected void clearCurrentPositionBlocksUpwards(WorldGenLevel world, int i, int j, int k, BoundingBox box) {
         BlockPos pos = new BlockPos(this.getWorldX(i, k), this.getWorldY(j), this.getWorldZ(i, k));
         if (box.isInside(pos)) {
            while (!world.isEmptyBlock(pos) && pos.getY() < 255) {
               world.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
               pos = pos.above();
            }
         }
      }

      /** Village.replaceAirAndLiquidDownwards: StructureComponent's, with the biome block. */
      protected void replaceAirAndLiquidDownwards(WorldGenLevel world, BlockState iblockstate, int i, int j, int k, BoundingBox box) {
         BlockState state = this.getBiomeSpecificBlockState(iblockstate);
         int x = this.getWorldX(i, k);
         int y = this.getWorldY(j);
         int z = this.getWorldZ(i, k);
         if (box.isInside(x, y, z)) {
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);
            while ((world.isEmptyBlock(pos) || world.getBlockState(pos).liquid()) && pos.getY() > 1) {
               world.setBlock(pos, state, 2);
               pos.move(Direction.DOWN);
            }
         }
      }

      protected void setStructureType(int i) {
         this.structureType = i;
      }
   }

   public static class Well extends VillagePieces.Village {
      Well(CompoundTag tag) {
         super(tag);
      }

      public Well(VillagePieces.Start structurevillagepieces$start, int i, Random random, int j, int k) {
         super(structurevillagepieces$start, i);
         this.setOrientation(HORIZONTAL[random.nextInt(HORIZONTAL.length)]);
         if (this.getOrientation().getAxis() == Direction.Axis.Z) {
            this.boundingBox = new BoundingBox(j, 64, k, j + 6 - 1, 78, k + 6 - 1);
         } else {
            this.boundingBox = new BoundingBox(j, 64, k, j + 6 - 1, 78, k + 6 - 1);
         }
      }

      @Override
      public void buildComponent(StructurePiece structurecomponent, List<StructurePiece> list, Random random) {
         VillagePieces.generateAndAddRoadPiece(
            (VillagePieces.Start)structurecomponent,
            list,
            random,
            this.boundingBox.minX() - 1,
            this.boundingBox.maxY() - 4,
            this.boundingBox.minZ() + 1,
            Direction.WEST,
            this.getGenDepth()
         );
         VillagePieces.generateAndAddRoadPiece(
            (VillagePieces.Start)structurecomponent,
            list,
            random,
            this.boundingBox.maxX() + 1,
            this.boundingBox.maxY() - 4,
            this.boundingBox.minZ() + 1,
            Direction.EAST,
            this.getGenDepth()
         );
         VillagePieces.generateAndAddRoadPiece(
            (VillagePieces.Start)structurecomponent,
            list,
            random,
            this.boundingBox.minX() + 1,
            this.boundingBox.maxY() - 4,
            this.boundingBox.minZ() - 1,
            Direction.NORTH,
            this.getGenDepth()
         );
         VillagePieces.generateAndAddRoadPiece(
            (VillagePieces.Start)structurecomponent,
            list,
            random,
            this.boundingBox.minX() + 1,
            this.boundingBox.maxY() - 4,
            this.boundingBox.maxZ() + 1,
            Direction.SOUTH,
            this.getGenDepth()
         );
      }

      @Override
      public boolean addComponentParts(WorldGenLevel world, Random random, BoundingBox structureboundingbox) {
         if (this.averageGroundLvl < 0) {
            this.averageGroundLvl = this.getAverageGroundLevel(world, structureboundingbox);
            if (this.averageGroundLvl < 0) {
               return true;
            }

            this.boundingBox.move(0, this.averageGroundLvl - this.boundingBox.maxY() + 3, 0);
         }

         BlockState iblockstate = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE.defaultBlockState());
         BlockState iblockstate1 = this.getBiomeSpecificBlockState(Blocks.OAK_FENCE.defaultBlockState());
         this.generateBox(world, structureboundingbox, 1, 0, 1, 4, 12, 4, iblockstate, Blocks.WATER.defaultBlockState(), false);
         this.placeBlock(world, Blocks.AIR.defaultBlockState(), 2, 12, 2, structureboundingbox);
         this.placeBlock(world, Blocks.AIR.defaultBlockState(), 3, 12, 2, structureboundingbox);
         this.placeBlock(world, Blocks.AIR.defaultBlockState(), 2, 12, 3, structureboundingbox);
         this.placeBlock(world, Blocks.AIR.defaultBlockState(), 3, 12, 3, structureboundingbox);
         this.placeBlock(world, iblockstate1, 1, 13, 1, structureboundingbox);
         this.placeBlock(world, iblockstate1, 1, 14, 1, structureboundingbox);
         this.placeBlock(world, iblockstate1, 4, 13, 1, structureboundingbox);
         this.placeBlock(world, iblockstate1, 4, 14, 1, structureboundingbox);
         this.placeBlock(world, iblockstate1, 1, 13, 4, structureboundingbox);
         this.placeBlock(world, iblockstate1, 1, 14, 4, structureboundingbox);
         this.placeBlock(world, iblockstate1, 4, 13, 4, structureboundingbox);
         this.placeBlock(world, iblockstate1, 4, 14, 4, structureboundingbox);
         this.generateBox(world, structureboundingbox, 1, 15, 1, 4, 15, 4, iblockstate, iblockstate, false);

         for (int i = 0; i <= 5; i++) {
            for (int j = 0; j <= 5; j++) {
               if (j == 0 || j == 5 || i == 0 || i == 5) {
                  this.placeBlock(world, iblockstate, j, 11, i, structureboundingbox);
                  this.clearCurrentPositionBlocksUpwards(world, j, 12, i, structureboundingbox);
               }
            }
         }

         return true;
      }
   }

   public static class WoodHut extends VillagePieces.Village {
      private boolean isTallHouse;
      private int tablePosition;

      WoodHut(CompoundTag tag) {
         super(tag);
      }

      public WoodHut(
         VillagePieces.Start structurevillagepieces$start, int i, Random random, BoundingBox structureboundingbox, Direction enumfacing
      ) {
         super(structurevillagepieces$start, i);
         this.setOrientation(enumfacing);
         this.boundingBox = structureboundingbox;
         this.isTallHouse = random.nextBoolean();
         this.tablePosition = random.nextInt(3);
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag nbttagcompound) {
         super.addAdditionalSaveData(context, nbttagcompound);
         nbttagcompound.putInt("T", this.tablePosition);
         nbttagcompound.putBoolean("C", this.isTallHouse);
      }

      @Override
      void read(CompoundTag nbttagcompound) {
         super.read(nbttagcompound);
         this.tablePosition = nbttagcompound.getInt("T");
         this.isTallHouse = nbttagcompound.getBoolean("C");
      }

      public static VillagePieces.WoodHut createPiece(
         VillagePieces.Start structurevillagepieces$start,
         List<StructurePiece> list,
         Random random,
         int i,
         int j,
         int k,
         Direction enumfacing,
         int l
      ) {
         BoundingBox structureboundingbox = BoundingBox.orientBox(i, j, k, 0, 0, 0, 4, 6, 5, enumfacing);
         return canVillageGoDeeper(structureboundingbox) && findIntersecting(list, structureboundingbox) == null
            ? new VillagePieces.WoodHut(structurevillagepieces$start, l, random, structureboundingbox, enumfacing)
            : null;
      }

      @Override
      public boolean addComponentParts(WorldGenLevel world, Random random, BoundingBox structureboundingbox) {
         if (this.averageGroundLvl < 0) {
            this.averageGroundLvl = this.getAverageGroundLevel(world, structureboundingbox);
            if (this.averageGroundLvl < 0) {
               return true;
            }

            this.boundingBox.move(0, this.averageGroundLvl - this.boundingBox.maxY() + 6 - 1, 0);
         }

         BlockState iblockstate = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE.defaultBlockState());
         BlockState iblockstate1 = this.getBiomeSpecificBlockState(Blocks.OAK_PLANKS.defaultBlockState());
         BlockState iblockstate2 = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
         BlockState iblockstate3 = this.getBiomeSpecificBlockState(Blocks.OAK_LOG.defaultBlockState());
         BlockState iblockstate4 = this.getBiomeSpecificBlockState(Blocks.OAK_FENCE.defaultBlockState());
         this.generateBox(world, structureboundingbox, 1, 1, 1, 3, 5, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(world, structureboundingbox, 0, 0, 0, 3, 0, 4, iblockstate, iblockstate, false);
         this.generateBox(world, structureboundingbox, 1, 0, 1, 2, 0, 3, Blocks.DIRT.defaultBlockState(), Blocks.DIRT.defaultBlockState(), false);
         if (this.isTallHouse) {
            this.generateBox(world, structureboundingbox, 1, 4, 1, 2, 4, 3, iblockstate3, iblockstate3, false);
         } else {
            this.generateBox(world, structureboundingbox, 1, 5, 1, 2, 5, 3, iblockstate3, iblockstate3, false);
         }

         this.placeBlock(world, iblockstate3, 1, 4, 0, structureboundingbox);
         this.placeBlock(world, iblockstate3, 2, 4, 0, structureboundingbox);
         this.placeBlock(world, iblockstate3, 1, 4, 4, structureboundingbox);
         this.placeBlock(world, iblockstate3, 2, 4, 4, structureboundingbox);
         this.placeBlock(world, iblockstate3, 0, 4, 1, structureboundingbox);
         this.placeBlock(world, iblockstate3, 0, 4, 2, structureboundingbox);
         this.placeBlock(world, iblockstate3, 0, 4, 3, structureboundingbox);
         this.placeBlock(world, iblockstate3, 3, 4, 1, structureboundingbox);
         this.placeBlock(world, iblockstate3, 3, 4, 2, structureboundingbox);
         this.placeBlock(world, iblockstate3, 3, 4, 3, structureboundingbox);
         this.generateBox(world, structureboundingbox, 0, 1, 0, 0, 3, 0, iblockstate3, iblockstate3, false);
         this.generateBox(world, structureboundingbox, 3, 1, 0, 3, 3, 0, iblockstate3, iblockstate3, false);
         this.generateBox(world, structureboundingbox, 0, 1, 4, 0, 3, 4, iblockstate3, iblockstate3, false);
         this.generateBox(world, structureboundingbox, 3, 1, 4, 3, 3, 4, iblockstate3, iblockstate3, false);
         this.generateBox(world, structureboundingbox, 0, 1, 1, 0, 3, 3, iblockstate1, iblockstate1, false);
         this.generateBox(world, structureboundingbox, 3, 1, 1, 3, 3, 3, iblockstate1, iblockstate1, false);
         this.generateBox(world, structureboundingbox, 1, 1, 0, 2, 3, 0, iblockstate1, iblockstate1, false);
         this.generateBox(world, structureboundingbox, 1, 1, 4, 2, 3, 4, iblockstate1, iblockstate1, false);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 0, 2, 2, structureboundingbox);
         this.placeBlock(world, Blocks.GLASS_PANE.defaultBlockState(), 3, 2, 2, structureboundingbox);
         if (this.tablePosition > 0) {
            this.placeBlock(world, iblockstate4, this.tablePosition, 1, 3, structureboundingbox);
            this.placeBlock(world, Blocks.OAK_PRESSURE_PLATE.defaultBlockState(), this.tablePosition, 2, 3, structureboundingbox);
         }

         this.placeBlock(world, Blocks.AIR.defaultBlockState(), 1, 1, 0, structureboundingbox);
         this.placeBlock(world, Blocks.AIR.defaultBlockState(), 1, 2, 0, structureboundingbox);
         this.createVillageDoor(world, structureboundingbox, random, 1, 1, 0, Direction.NORTH);
         if (this.getBlock(world, 1, 0, -1, structureboundingbox).isAir()
            && this.getBlock(world, 1, -1, -1, structureboundingbox).isAir() == false) {
            this.placeBlock(world, iblockstate2, 1, 0, -1, structureboundingbox);
            if (this.getBlock(world, 1, -1, -1, structureboundingbox).is(Blocks.DIRT_PATH)) {
               this.placeBlock(world, Blocks.GRASS_BLOCK.defaultBlockState(), 1, -1, -1, structureboundingbox);
            }
         }

         for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 4; j++) {
               this.clearCurrentPositionBlocksUpwards(world, j, 6, i, structureboundingbox);
               this.replaceAirAndLiquidDownwards(world, iblockstate, j, -1, i, structureboundingbox);
            }
         }

         this.spawnVillagers(world, structureboundingbox, 1, 1, 2, 1);
         return true;
      }
   }

   /** Recurrent Complex GenericVillagePiece. */
   /** A block of another mod if it is installed (its own, working version), else null. */
   @Nullable
   static Block modBlock(String id) {
      net.minecraft.resources.ResourceLocation rl = net.minecraft.resources.ResourceLocation.parse(id);
      return net.minecraft.core.registries.BuiltInRegistries.BLOCK.containsKey(rl) ? net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(rl) : null;
   }

   static BlockState withProperty(BlockState s, String name, String value) {
      net.minecraft.world.level.block.state.properties.Property<?> p = s.getBlock().getStateDefinition().getProperty(name);
      return p == null ? s : with(s, p, value);
   }

   private static <T extends Comparable<T>> BlockState with(BlockState s, net.minecraft.world.level.block.state.properties.Property<T> p, String value) {
      return p.getValue(value).map(v -> s.setValue(p, v)).orElse(s);
   }

   /**
    * Waystones 4.1.0 ComponentVillageWaystone: its 5x5 template, unrotated, at the corner of a 7x6x7 box on the ground, and a mossy
    * waystone on the "Waystone" data block. With Waystones (1.21) installed that is its own mossy waystone, otherwise a stand-in.
    */
   public static class WaystonePiece extends VillagePieces.Village {
      WaystonePiece(CompoundTag tag) {
         super(tag);
      }

      WaystonePiece(VillagePieces.Start start, int type, BoundingBox box, Direction facing) {
         super(start, type);
         this.setBox(box);
         this.setOrientation(facing);
      }

      @Nullable
      public static WaystonePiece createPiece(VillagePieces.Start start, List<StructurePiece> pieces, Random random, int x, int y, int z, Direction facing, int type) {
         BoundingBox box = BoundingBox.orientBox(x, y, z, 0, 0, 0, 7, 6, 7, facing);
         return canVillageGoDeeper(box) && findIntersecting(pieces, box) == null ? new WaystonePiece(start, type, box, facing) : null;
      }

      @Override
      public boolean addComponentParts(WorldGenLevel world, Random random, BoundingBox box) {
         if (this.averageGroundLvl < 0) {
            this.averageGroundLvl = this.getAverageGroundLevel(world, box);
            if (this.averageGroundLvl < 0) {
               return true;
            }
            this.boundingBox.move(0, this.averageGroundLvl - this.boundingBox.minY(), 0);
         }
         BlockPos pos = new BlockPos(this.boundingBox.minX(), this.boundingBox.minY(), this.boundingBox.minZ());
         var template = world.getLevel().getStructureManager().get(net.minecraft.resources.ResourceLocation.parse(
            this.structureType == 1 ? "rlgen:waystones/desert_village_waystone" : "rlgen:waystones/village_waystone")).orElse(null);
         if (template == null) {
            return true;
         }
         var settings = new net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings().setBoundingBox(box)
            .addProcessor(net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor.STRUCTURE_BLOCK);
         template.placeInWorld(world, pos, pos, settings, RandomSource.create(0L), 2);
         for (var e : com.nukevscity.rlgen.quark.Templates.dataBlocks(template, pos, settings).entrySet()) {
            if ("Waystone".equals(e.getValue())) {
               BlockPos at = e.getKey();
               Block waystone = modBlock("waystones:mossy_waystone");
               if (waystone != null) {
                  if (box.isInside(at)) {
                     world.setBlock(at, withProperty(waystone.defaultBlockState(), "half", "lower"), 3);
                  }
                  if (box.isInside(at.above())) {
                     world.setBlock(at.above(), withProperty(waystone.defaultBlockState(), "half", "upper"), 3);
                  }
               } else {
                  if (box.isInside(at)) {
                     world.setBlock(at, Blocks.MOSSY_STONE_BRICK_WALL.defaultBlockState(), 3);
                  }
                  if (box.isInside(at.above())) {
                     world.setBlock(at.above(), Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 3);
                  }
               }
            }
         }
         return true;
      }
   }

   /**
    * Bountiful 2.2.3 VillageBoardComponent: the village board template for the village type, rotated to the road, one below the
    * ground level; its grass paths look down for the ground (VillageBoardProcessor). The board is Bountiful's (1.21) when installed.
    */
   public static class BountyBoardPiece extends VillagePieces.Village {
      private Rotation boardRotation = Rotation.NONE;
      private BlockPos boardShift = BlockPos.ZERO;

      BountyBoardPiece(CompoundTag tag) {
         super(tag);
         this.setup(this.getOrientation());
      }

      BountyBoardPiece(VillagePieces.Start start, int type, BoundingBox box, Direction facing) {
         super(start, type);
         this.setBox(box);
         this.setOrientation(facing);
         this.setup(facing);
      }

      private void setup(@Nullable Direction facing) {
         if (facing == Direction.SOUTH) {
            this.boardRotation = Rotation.CLOCKWISE_180;
            this.boardShift = new BlockPos(6, 0, 6);
         } else if (facing == Direction.WEST) {
            this.boardRotation = Rotation.COUNTERCLOCKWISE_90;
            this.boardShift = new BlockPos(0, 0, 6);
         } else if (facing == Direction.EAST) {
            this.boardRotation = Rotation.CLOCKWISE_90;
            this.boardShift = new BlockPos(6, 0, 0);
         } else {
            this.boardRotation = Rotation.NONE;
            this.boardShift = BlockPos.ZERO;
         }
      }

      @Nullable
      public static BountyBoardPiece createPiece(VillagePieces.Start start, List<StructurePiece> pieces, Random random, int x, int y, int z, Direction facing, int type) {
         BoundingBox box = BoundingBox.orientBox(x, y, z, 0, 0, 0, 7, 6, 7, facing);
         return canVillageGoDeeper(box) && findIntersecting(pieces, box) == null ? new BountyBoardPiece(start, type, box, facing) : null;
      }

      @Override
      public boolean addComponentParts(WorldGenLevel world, Random random, BoundingBox box) {
         if (this.averageGroundLvl < 0) {
            this.averageGroundLvl = this.getAverageGroundLevel(world, box);
            if (this.averageGroundLvl < 0) {
               return true;
            }
            this.boundingBox.move(0, this.averageGroundLvl - this.boundingBox.minY() - 1, 0);
         }
         BlockState pathState = this.getBiomeSpecificBlockState(Blocks.DIRT_PATH.defaultBlockState());
         BlockState planksState = this.getBiomeSpecificBlockState(Blocks.OAK_PLANKS.defaultBlockState());
         BlockState gravelState = this.getBiomeSpecificBlockState(Blocks.GRAVEL.defaultBlockState());
         BlockState cobbleState = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE.defaultBlockState());
         String id = switch (this.structureType) {
            case 1 -> "rlgen:bountiful/village_board_desert";
            case 2 -> "rlgen:bountiful/village_board_savanna";
            case 3 -> "rlgen:bountiful/village_board_taiga";
            default -> "rlgen:bountiful/village_board";
         };
         var template = world.getLevel().getStructureManager().get(net.minecraft.resources.ResourceLocation.parse(id)).orElse(null);
         if (template == null) {
            return true;
         }
         BlockPos pos = new BlockPos(this.boundingBox.minX(), this.boundingBox.minY(), this.boundingBox.minZ()).offset(this.boardShift);
         var settings = new net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings().setBoundingBox(box).setRotation(this.boardRotation);
         List<BlockPos> paths = new ArrayList<>();
         for (var info : template.filterBlocks(pos, settings, Blocks.DIRT_PATH)) {
            paths.add(info.pos());
         }
         settings.addProcessor(new net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor(List.of(Blocks.DIRT_PATH, Blocks.STRUCTURE_VOID)));
         template.placeInWorld(world, pos, pos, settings, RandomSource.create(0L), 2);
         Block board = modBlock("bountiful:bountyboard");
         if (board != null) {
            for (var info : template.filterBlocks(pos, settings, com.nukevscity.rlgen.RLGen.BOUNTY_BOARD.get())) {
               if (box.isInside(info.pos())) {
                  world.setBlock(info.pos(), board.defaultBlockState(), 2);
               }
            }
         }
         // VillageBoardProcessor: a template path looks down from its spot (to sea level - 1) and sets the path spot itself
         for (BlockPos p : paths) {
            if (!box.isInside(p)) {
               continue;
            }
            for (BlockPos q = p; q.getY() >= SEA_LEVEL - 1; q = q.below()) {
               BlockState state = world.getBlockState(q);
               if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT)) {
                  world.setBlock(p.above(), Blocks.AIR.defaultBlockState(), 3);
                  world.setBlock(p, pathState, 3);
                  break;
               } else if (state.liquid()) {
                  world.setBlock(p, planksState, 3);
                  break;
               } else if (state.is(Blocks.SAND) || state.is(Blocks.SANDSTONE) || state.is(Blocks.RED_SANDSTONE)) {
                  BlockState below = world.getBlockState(p.below());
                  world.setBlock(p, below.isAir() || below.liquid() ? cobbleState : gravelState, 3);
                  break;
               }
            }
         }
         return true;
      }
   }

   public static class RcVillagePiece extends VillagePieces.Village {
      String structureID;
      String generationID;
      RcTransform transform;
      long seed;
      private RcGenerator.Instance instance;

      RcVillagePiece(CompoundTag tag) {
         super(tag);
      }

      RcVillagePiece(VillagePieces.Start start, int depth) {
         super(start, depth);
      }

      public String structure() {
         return this.structureID;
      }

      /** GenericVillagePiece.getTransform */
      @Nullable
      static RcTransform transform(@Nullable Direction front, boolean canMirror, boolean canRotate, Direction toFront, Random random) {
         boolean mirror = canMirror && random.nextBoolean();
         RcTransform t;
         if (front != null) {
            Integer r = RcTransform.clockwiseRotations(front, toFront, mirror);
            t = RcTransform.from(r == null ? 0 : r, mirror);
         } else {
            t = RcTransform.from(canRotate ? random.nextInt(4) : 0, mirror);
         }
         return !canRotate && t.rotation() != 0 ? null : t;
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
         super.addAdditionalSaveData(context, tag);
         tag.putString("RcSId", this.structureID);
         tag.putString("RcGtId", this.generationID);
         tag.putInt("RcRotation", this.transform.rotation());
         tag.putBoolean("RcMirror", this.transform.mirrorX());
         tag.putLong("seed", this.seed);
      }

      @Override
      void read(CompoundTag tag) {
         super.read(tag);
         this.structureID = tag.getString("RcSId");
         this.generationID = tag.getString("RcGtId");
         this.transform = RcTransform.from(tag.getInt("RcRotation"), tag.getBoolean("RcMirror"));
         this.seed = tag.getLong("seed");
      }

      @Nullable
      private RcStructureDef.VanillaGen gen(RcStructureDef def) {
         for (RcStructureDef.VanillaGen g : def.vanilla) {
            if (g.id().equals(this.generationID)) {
               return g;
            }
         }
         return null;
      }

      private RcEnv env() {
         Biome biome = this.startPiece != null ? this.startPiece.biome : this.startBiome != null ? Biome.byName(this.startBiome) : null;
         return new RcEnv(biome, "vanilla", this.generationID, this.structureType);
      }

      @Override
      public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource,
                              BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {
         if (chunkGenerator instanceof LegacyChunkGenerator generator) {
            this.generator = generator;
            super.postProcess(level, structureManager, chunkGenerator, randomSource, box, chunkPos, pivot);
         }
      }

      private LegacyChunkGenerator generator;

      @Override
      public boolean addComponentParts(WorldGenLevel world, Random random, BoundingBox box) {
         RcStructureDef def = RcRegistry.get().structure(this.structureID);
         RcStructureDef.VanillaGen gen = def == null ? null : this.gen(def);
         if (gen == null) {
            return false;
         }
         BlockPos shift = this.transform.apply(gen.spawnShift(), new int[]{1, 1, 1});
         RcGenerator.Instance inst;
         synchronized (this) {
            if (this.averageGroundLvl < 0) {
               this.averageGroundLvl = this.getAverageGroundLevel(world, box);
               if (this.averageGroundLvl < 0) {
                  return true;
               }
               this.boundingBox.move(0, this.averageGroundLvl - this.boundingBox.minY() + shift.getY(), 0);
            }
            if (this.instance == null) {
               BlockPos lower = new BlockPos(this.boundingBox.minX(), this.boundingBox.minY(), this.boundingBox.minZ());
               this.instance = RcGenerator.prepareVillage(def, this.generationID, this.seed, this.transform, lower, this.env(), this.generator.oracle(),
                  this.getGenDepth());
            }
            inst = this.instance;
         }
         if (inst != null) {
            RcPiece.LiveView view = new RcPiece.LiveView(world, new ChunkPos(box.getCenter()), this.generator.oracle());
            RcGenerator.spawn(inst, view, box, this.seed, this.env());
            view.finish();
         }
         return true;
      }
   }
}
