package com.nukevscity.rlgen.charm;

import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.deco.DWorld;
import com.nukevscity.rlgen.quark.QuarkGen;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Charm AbandonedCrateGenerator (IWorldGenerator weight 1000) with RLCraft's settings: in 10% of chunks, up to 50 tries for an air
 * block between y 28 and 48 above a full block, then a sealed crate of a random wood with loot of a random crate type for its rarity.
 * The rarity, wood and type came from unseeded randoms (world.rand, new Random()); here from one seeded by the chunk.
 */
public final class AbandonedCrates {
   private static final double GENERATE_CHANCE = 0.1;
   private static final int LOWER = 28, UPPER = 48, MAX_TRIES = 50;
   private static final float RARE = 0.005F, VALUABLE = 0.06F, UNCOMMON = 0.2F;
   /** Crate.types: LootHelper's MISC tables per rarity (vanilla first, then Charm's) plus "dangerous" and "explosive". */
   private static final List<String> COMMON_TYPES = List.of("minecraft:chests/village/village_weaponsmith", "minecraft:chests/igloo_chest", "charm:treasure/common",
      "charm:treasure/junk", "charm:village/smith", "charm:village/librarian", "charm:village/priest", "charm:village/carpenter", "charm:village/butcher",
      "charm:village/farmer", "charm:village/shepherd", "charm:village/fisherman");
   private static final List<String> UNCOMMON_TYPES = List.of("minecraft:chests/simple_dungeon", "minecraft:chests/jungle_temple",
      "minecraft:chests/woodland_mansion", "minecraft:chests/desert_pyramid", "minecraft:chests/abandoned_mineshaft", "charm:treasure/uncommon");
   private static final List<String> VALUABLE_TYPES = List.of("minecraft:chests/stronghold_library", "minecraft:chests/stronghold_crossing",
      "minecraft:chests/stronghold_corridor", "minecraft:chests/nether_bridge", "charm:treasure/valuable", "charm:treasure/dangerous");
   private static final List<String> RARE_TYPES = List.of("minecraft:chests/end_city_treasure", "charm:treasure/rare", "charm:treasure/explosive");

   private AbandonedCrates() {
   }

   public static void generate(DWorld w, int chunkX, int chunkZ) {
      Random random = QuarkGen.fmlRandom(w.seed(), chunkX, chunkZ);
      float f = random.nextFloat();
      if ((double)f > GENERATE_CHANCE) {
         return;
      }
      int x = chunkX * 16 + 8;
      int z = chunkZ * 16 + 8;
      int range = UPPER - LOWER;
      for (int i = 0; i < MAX_TRIES; i++) {
         BlockPos pos = new BlockPos(x + random.nextInt(10), LOWER + random.nextInt(range), z + random.nextInt(10));
         if (w.get(pos).isAir()) {
            BlockState state;
            do {
               pos = pos.below();
               state = w.get(pos);
            } while (!state.isSolidRender(w.level, pos) && pos.getY() > LOWER);
            if (state.isSolidRender(w.level, pos)) {
               placeCrate(w, pos.above(), new Random(w.seed() ^ pos.asLong() * 0x9E3779B97F4A7C15L));
               break;
            }
         }
      }
   }

   private static void placeCrate(DWorld w, BlockPos pos, Random worldRand) {
      float f = worldRand.nextFloat();
      List<String> types = COMMON_TYPES;
      if (f <= RARE) {
         types = RARE_TYPES;
      } else if (f <= VALUABLE) {
         types = VALUABLE_TYPES;
      } else if (f <= UNCOMMON) {
         types = UNCOMMON_TYPES;
      }
      BlockState state = RLGen.SEALED_CRATES.get(worldRand.nextInt(RLGen.SEALED_CRATES.size())).get().defaultBlockState();
      String loot = types.get(worldRand.nextInt(types.size()));
      w.set(pos, state);
      BlockEntity be = w.level.getBlockEntity(pos);
      if (be instanceof RandomizableContainerBlockEntity c) {
         c.setLootTable(CharmLoot.key(loot), worldRand.nextLong());
      }
   }
}
