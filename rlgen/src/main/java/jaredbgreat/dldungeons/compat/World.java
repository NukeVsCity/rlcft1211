package jaredbgreat.dldungeons.compat;

import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * rlgen: the 1.12 World as Doomlike Dungeons used it. While a chunk is built it is bound to that chunk's WorldGenLevel; writes outside
 * the 3x3 chunks the level can change are dropped (1.12 would have generated those chunks), reads there see the 1.12 terrain.
 */
public final class World {
   public static final int HEIGHT = 256;
   private final long seed;
   private final LegacyChunkGenerator generator;
   private WorldGenLevel level;
   private int minX;
   private int minZ;
   private int maxX;
   private int maxZ;
   private final List<BlockPos> shaped = new ArrayList<>();

   public World(long seed, LegacyChunkGenerator generator) {
      this.seed = seed;
      this.generator = generator;
   }

   public void bind(WorldGenLevel level, int chunkX, int chunkZ) {
      this.level = level;
      this.minX = (chunkX - 1) << 4;
      this.minZ = (chunkZ - 1) << 4;
      this.maxX = ((chunkX + 2) << 4) - 1;
      this.maxZ = ((chunkZ + 2) << 4) - 1;
      this.shaped.clear();
   }

   /** Fences, bars, walls, stairs and chests take their shape from their neighbours (1.12 worked it out when drawing them). */
   public void unbind() {
      if (this.level != null) {
         for (BlockPos p : this.shaped) {
            BlockState s = this.level.getBlockState(p);
            BlockState u = Block.updateFromNeighbourShapes(s, this.level, p);
            if (u != s && !u.isAir()) {
               this.level.setBlock(p, u, 2 | 16);
            }
         }
      }
      this.shaped.clear();
      this.level = null;
   }

   private boolean inLevel(BlockPos pos) {
      return this.level != null && pos.getX() >= this.minX && pos.getX() <= this.maxX && pos.getZ() >= this.minZ && pos.getZ() <= this.maxZ;
   }

   public long getSeed() {
      return this.seed;
   }

   public int getActualHeight() {
      return HEIGHT;
   }

   public BlockPos getSpawnPoint() {
      return BlockPos.ZERO;
   }

   public Biome getBiome(BlockPos pos) {
      return this.generator.legacyBiome(pos.getX(), pos.getZ());
   }

   public BlockState getBlockState(BlockPos pos) {
      if (pos.getY() < 0 || pos.getY() >= HEIGHT) {
         return Blocks.AIR.defaultBlockState(); // 1.12 world: nothing outside 0..255
      }
      return this.inLevel(pos) ? this.level.getBlockState(pos) : this.generator.legacyState(pos);
   }

   public boolean isAirBlock(BlockPos pos) {
      return this.getBlockState(pos).isAir();
   }

   public void setBlockState(BlockPos pos, BlockState state) {
      if (pos.getY() < 0 || pos.getY() >= HEIGHT || !this.inLevel(pos)) {
         return;
      }
      this.level.setBlock(pos, state, 2);
      if (!state.getFluidState().isEmpty()) {
         this.level.scheduleTick(pos, state.getFluidState().getType(), 0); // 1.12 setBlockState (flags 3) let placed liquids flow
      }
      Block b = state.getBlock();
      if (b instanceof FenceBlock || b instanceof WallBlock || b instanceof IronBarsBlock || b instanceof StairBlock || b instanceof ChestBlock) {
         this.shaped.add(pos.immutable());
      }
   }

   public void setBlockToAir(BlockPos pos) {
      this.setBlockState(pos, Blocks.AIR.defaultBlockState());
   }

   public void setChestItem(BlockPos pos, int slot, ItemStack stack) {
      if (stack != null && !stack.isEmpty() && this.inLevel(pos) && this.level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
         chest.setItem(slot, stack);
      }
   }

   /** TileEntityMobSpawner with one WeightedSpawnerEntity {id: mob} (and the 1.12 default delays). */
   public void setSpawner(BlockPos pos, String mob) {
      if (!this.inLevel(pos)) {
         return;
      }
      BlockEntity be = this.level.getBlockEntity(pos);
      ResourceLocation id = ResourceLocation.tryParse(com.github.fnar.minecraft.block.spawner.SpawnPotentialMapper1_12.entityId(mob));
      if (id == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
         id = ResourceLocation.withDefaultNamespace(standIn(mob)); // Lycanites / Ice and Fire are not ported yet
      }
      if (be instanceof SpawnerBlockEntity spawner && id != null && BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
         spawner.setEntityId(BuiltInRegistries.ENTITY_TYPE.get(id), this.level.getRandom());
         be.setChanged();
      }
   }

   private static String standIn(String mob) {
      return mob.startsWith("iceandfire:") ? "wither_skeleton" : "zombie";
   }
}
