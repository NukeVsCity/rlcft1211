package com.nukevscity.rlgen.iaf;

import com.github.alexthe666.iceandfire.entity.AirWaterEntities;
import com.github.alexthe666.iceandfire.entity.EntityCyclops;
import com.github.alexthe666.iceandfire.entity.EntityHippocampus;
import com.github.alexthe666.iceandfire.entity.EntityMyrmexBase;
import com.github.alexthe666.iceandfire.entity.EntityPixie;
import com.github.alexthe666.iceandfire.entity.EntitySeaSerpent;
import com.github.alexthe666.iceandfire.entity.EntitySiren;
import com.github.alexthe666.iceandfire.entity.EntitySnowVillager;
import com.github.alexthe666.iceandfire.entity.EntityStymphalianBird;
import com.github.alexthe666.iceandfire.entity.IafLandMobs;
import com.github.alexthe666.iceandfire.entity.MyrmexHive;
import com.github.alexthe666.iceandfire.world.MyrmexWorldData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;

/**
 * The mobs Ice and Fire 1.7.1 spawned during world generation, created on the WorldGenRegion of the chunk that holds them. The
 * planners drew every random value in the 1.12 order; these only build the entity. 1.12 added them with World.spawnEntity and called
 * onInitialSpawn only for pixies, snow villagers and myrmexes (here finalizeSpawn, STRUCTURE).
 */
public final class IafSpawns {
   private IafSpawns() {
   }

   /** World.spawnEntity (no persistence: the mob's own despawn rules apply, as in 1.12). */
   private static void add(WorldGenLevel level, Mob mob) {
      level.addFreshEntityWithPassengers(mob);
   }

   private static DifficultyInstance difficulty(WorldGenLevel level, BlockPos pos) {
      return level.getCurrentDifficultyAt(pos);
   }

   /** World.getHeight: above the highest block with a light opacity (1.12 heightmap: leaves and water count, plants and piles not). */
   static int legacyHeight(WorldGenLevel level, int x, int z) {
      BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos(x, 255, z);
      for (int y = 255; y >= 0; y--) {
         p.setY(y);
         BlockState s = level.getBlockState(p);
         if (!s.isAir() && (s.getLightBlock(level, p) > 0 || !s.getFluidState().isEmpty())) {
            return y + 1;
         }
      }
      return 0;
   }

   // ------------------------------------------------------------------ cyclops cave

   /** WorldGenCyclopsCave.generate: variant rand(3), yaw rand.nextFloat() * 360, at the centre + (0.5, 1.5, 0.5). */
   static void cyclops(WorldGenLevel level, BlockPos position, int variant, float yaw) {
      EntityCyclops cyclops = IafLandMobs.CYCLOPS.get().create(level.getLevel());
      if (cyclops == null) {
         return;
      }
      cyclops.setVariant(variant);
      cyclops.moveTo((double)position.getX() + 0.5, (double)position.getY() + 1.5, (double)position.getZ() + 0.5, yaw, 0.0F);
      add(level, cyclops);
   }

   /** WorldGenCyclopsCave.genSheepPen: a sheep in the middle of the block next to the fence, yellow 1 in 4. */
   static void sheep(WorldGenLevel level, BlockPos sheepPos, boolean yellow) {
      Sheep sheep = EntityType.SHEEP.create(level.getLevel());
      if (sheep == null) {
         return;
      }
      sheep.setPos((double)((float)sheepPos.getX() + 0.5F), (double)((float)sheepPos.getY() + 0.5F), (double)((float)sheepPos.getZ() + 0.5F));
      sheep.setColor(yellow ? DyeColor.YELLOW : DyeColor.WHITE);
      add(level, sheep);
   }

   // ------------------------------------------------------------------ siren island

   /** WorldGenSirenIsland.spawnSiren: singing, hair colour rand(2), singing pose rand(2), yaw, at position + (0.5, 1, 0.5). */
   static void siren(WorldGenLevel level, BlockPos position, int hair, int pose, float yaw) {
      EntitySiren siren = AirWaterEntities.SIREN.get().create(level.getLevel());
      if (siren == null) {
         return;
      }
      siren.setSinging(true);
      siren.setHairColor(hair);
      siren.setSingingPose(pose);
      siren.moveTo((double)position.getX() + 0.5, (double)(position.getY() + 1), (double)position.getZ() + 0.5, yaw, 0.0F);
      add(level, siren);
   }

   // ------------------------------------------------------------------ StructureGenerator ocean / swamp mobs

   /** A hippocampus of variant rand(5) in the middle of a water block (isNotColliding only tested other entities: done when planned). */
   static void hippocampus(WorldGenLevel level, BlockPos pos, int variant) {
      EntityHippocampus campus = AirWaterEntities.HIPPOCAMPUS.get().create(level.getLevel());
      if (campus == null) {
         return;
      }
      campus.setVariant(variant);
      campus.moveTo((double)((float)pos.getX() + 0.5F), (double)((float)pos.getY() + 0.5F), (double)((float)pos.getZ() + 0.5F), 0.0F, 0.0F);
      add(level, campus);
   }

   /** EntitySeaSerpent.onWorldSpawn with its three draws (variant rand(6), ancient rand(15) == 1, scale nextFloat). */
   static void seaSerpent(WorldGenLevel level, BlockPos pos, int variant, boolean ancient, float scaleRoll) {
      EntitySeaSerpent serpent = AirWaterEntities.SEA_SERPENT.get().create(level.getLevel());
      if (serpent == null) {
         return;
      }
      serpent.setVariant(variant);
      if (ancient) {
         serpent.setAncient(true);
         serpent.setSeaSerpentScale(6.0F + scaleRoll * 3.0F);
      } else {
         serpent.setSeaSerpentScale(1.5F + scaleRoll * 4.0F);
      }
      serpent.moveTo((double)((float)pos.getX() + 0.5F), (double)((float)pos.getY() + 0.5F), (double)((float)pos.getZ() + 0.5F), 0.0F, 0.0F);
      add(level, serpent);
   }

   /** A stymphalian bird on an opaque block; EntityLiving.isNotColliding: no liquid and no block collision in its box. */
   static void stymphalianBird(WorldGenLevel level, BlockPos pos) {
      EntityStymphalianBird bird = AirWaterEntities.STYMPHALIAN_BIRD.get().create(level.getLevel());
      if (bird == null) {
         return;
      }
      bird.moveTo((double)((float)pos.getX() + 0.5F), (double)((float)pos.getY() + 0.5F), (double)((float)pos.getZ() + 0.5F), 0.0F, 0.0F);
      AABB box = bird.getBoundingBox();
      for (BlockPos p : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ), BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
         BlockState s = level.getBlockState(p);
         FluidState f = s.getFluidState();
         if (!f.isEmpty() || !s.getCollisionShape(level, p).isEmpty() && s.getCollisionShape(level, p).move(p.getX(), p.getY(), p.getZ()).bounds().intersects(box)) {
            return;
         }
      }
      add(level, bird);
   }

   static float birdWidth() {
      return AirWaterEntities.STYMPHALIAN_BIRD.get().getWidth();
   }

   static float birdHeight() {
      return AirWaterEntities.STYMPHALIAN_BIRD.get().getHeight();
   }

   static float hippocampusWidth() {
      return AirWaterEntities.HIPPOCAMPUS.get().getWidth();
   }

   static float hippocampusHeight() {
      return AirWaterEntities.HIPPOCAMPUS.get().getHeight();
   }

   // ------------------------------------------------------------------ villages

   /** PixieVillagePieces.Village.spawnVillagers: a pixie with onInitialSpawn (its colour). */
   public static void pixie(WorldGenLevel level, double x, double y, double z) {
      EntityPixie pixie = AirWaterEntities.PIXIE.get().create(level.getLevel());
      if (pixie == null) {
         return;
      }
      pixie.moveTo(x, y, z, 0.0F, 0.0F);
      pixie.finalizeSpawn(level, difficulty(level, pixie.blockPosition()), MobSpawnType.STRUCTURE, null);
      add(level, pixie);
   }

   /** SnowVillagePieces.Village.spawnVillagers: a snow villager with a random profession, or a zombie villager in a zombie village. */
   public static void snowVillager(WorldGenLevel level, double x, double y, double z, boolean zombie) {
      if (zombie) {
         ZombieVillager entityzombie = EntityType.ZOMBIE_VILLAGER.create(level.getLevel());
         if (entityzombie != null) {
            entityzombie.moveTo(x, y, z, 0.0F, 0.0F);
            entityzombie.finalizeSpawn(level, difficulty(level, entityzombie.blockPosition()), MobSpawnType.STRUCTURE, null);
            entityzombie.setPersistenceRequired();
            add(level, entityzombie);
         }
         return;
      }
      EntitySnowVillager villager = AirWaterEntities.SNOW_VILLAGER.get().create(level.getLevel());
      if (villager == null) {
         return;
      }
      villager.moveTo(x, y, z, 0.0F, 0.0F);
      villager.finalizeSpawn(level, difficulty(level, villager.blockPosition()), MobSpawnType.STRUCTURE, null);
      // ModVillagers.setRandomProfession(entityvillager, worldIn.rand)
      villager.setProfessionId(level.getRandom().nextInt(3));
      add(level, villager);
   }

   // ------------------------------------------------------------------ myrmex hives

   /**
    * MyrmexWorldData.addHive: the hive planned with the 1.12 terrain joins the real hive list on the server thread (entities only keep
    * its UUID until their chunk is loaded, which happens on the server thread after this).
    */
   static void registerHive(WorldGenLevel level, MyrmexHive hive) {
      ServerLevel server = level.getLevel();
      MyrmexWorldData.WORLDGEN_PENDING.putIfAbsent(hive.hiveUUID, hive);
      // getHiveFromUUID adds a pending hive to the list (unless a myrmex of it was loaded first and did so)
      server.getServer().execute(() -> MyrmexWorldData.get(server).getHiveFromUUID(hive.hiveUUID));
   }

   /**
    * EntityLiving.onInitialSpawn (follow range bonus, left handed) and EntityMyrmexBase's (the nearest hive, which is this one: it was
    * added before the myrmexes spawned; the jungle variant, set again right after). Called without the level, which a world gen
    * thread must not read.
    */
   private static void myrmexInitialSpawn(Mob mob, WorldGenLevel level) {
      AttributeInstance follow = mob.getAttribute(Attributes.FOLLOW_RANGE);
      if (follow != null) {
         follow.addPermanentModifier(new AttributeModifier(net.minecraft.resources.ResourceLocation.withDefaultNamespace("random_spawn_bonus"),
            level.getRandom().triangle(0.0, 0.11485000000000001), AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
      }
      mob.setLeftHanded(level.getRandom().nextFloat() < 0.05F);
   }

   /** WorldGenMyrmexHive.generateMainRoom: caste 0 queen, 1 worker, 2 soldier, 3 sentinel, at the grounded centre + (0.5, 1, 0.5). */
   static void myrmex(WorldGenLevel level, MyrmexHive hive, int caste, BlockPos ground, boolean jungle) {
      EntityMyrmexBase myrmex = switch (caste) {
         case 0 -> IafLandMobs.MYRMEX_QUEEN.get().create(level.getLevel());
         case 1 -> IafLandMobs.MYRMEX_WORKER.get().create(level.getLevel());
         case 2 -> IafLandMobs.MYRMEX_SOLDIER.get().create(level.getLevel());
         default -> IafLandMobs.MYRMEX_SENTINEL.get().create(level.getLevel());
      };
      if (myrmex == null) {
         return;
      }
      myrmex.moveTo((double)ground.getX() + 0.5, (double)ground.getY() + 1.0, (double)ground.getZ() + 0.5, 0.0F, 0.0F);
      myrmexInitialSpawn(myrmex, level);
      myrmex.setHive(hive);
      myrmex.setJungleVariant(jungle);
      add(level, myrmex);
   }

   /**
    * WorldGenMyrmexHive.decorate NURSERY: a baby of caste rand(4) (1 soldier, 2 sentinel, 3 royal, else worker), growth stage rand(2),
    * at the block's corner, unless it is inside an opaque block.
    */
   static void babyMyrmex(WorldGenLevel level, MyrmexHive hive, int caste, int growth, BlockPos pos, float yaw, float headYaw, boolean jungle) {
      EntityMyrmexBase baby = switch (caste) {
         case 1 -> IafLandMobs.MYRMEX_SOLDIER.get().create(level.getLevel());
         case 2 -> IafLandMobs.MYRMEX_SENTINEL.get().create(level.getLevel());
         case 3 -> IafLandMobs.MYRMEX_ROYAL.get().create(level.getLevel());
         default -> IafLandMobs.MYRMEX_WORKER.get().create(level.getLevel());
      };
      if (baby == null) {
         return;
      }
      baby.setGrowthStage(growth);
      baby.moveTo((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), yaw, 0.0F);
      baby.setYHeadRot(headYaw);
      baby.setHive(hive);
      baby.setJungleVariant(jungle);
      // Entity.isEntityInsideOpaqueBlock: the block at the eyes
      BlockPos eyes = BlockPos.containing(baby.getX(), baby.getEyeY(), baby.getZ());
      if (level.getBlockState(eyes).isSuffocating(level, eyes)) {
         return;
      }
      add(level, baby);
   }
}
