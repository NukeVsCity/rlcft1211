package com.nukevscity.rlgen.vanilla.mansion;

import com.mojang.logging.LogUtils;
import com.nukevscity.rlgen.deco.DWorld;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.LootTable;
import org.slf4j.Logger;

/**
 * 1.12 WoodlandMansionPieces.MansionTemplate (a StructureComponentTemplate) with the 1.12 templates converted to
 * data/rlgen/structure/mansion. The size and the data markers are read straight from the template file, in its block order, so
 * Template.getDataBlocks' HashMap comes out in 1.12's order.
 */
public final class MansionTemplate {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceKey<LootTable> CHESTS_WOODLAND_MANSION =
      ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.withDefaultNamespace("chests/woodland_mansion"));

   /** What the Placer needs from the TemplateManager. */
   public interface Sizes {
      BlockPos size(String templateName);
   }

   record Marker(BlockPos pos, String metadata) {
   }

   record TemplateData(BlockPos size, List<Marker> markers) {
   }

   private static final Map<String, TemplateData> DATA = new ConcurrentHashMap<>();

   static TemplateData data(String name) {
      return DATA.computeIfAbsent(name, MansionTemplate::load);
   }

   public static final Sizes SIZES = name -> data(name).size;

   private static TemplateData load(String name) {
      String path = "/data/rlgen/structure/mansion/" + name + ".nbt";
      try (InputStream in = MansionTemplate.class.getResourceAsStream(path)) {
         if (in == null) {
            throw new IllegalStateException("missing mansion template " + path);
         }
         CompoundTag tag = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
         ListTag size = tag.getList("size", 3);
         ListTag palette = tag.getList("palette", 10);
         List<Marker> markers = new ArrayList<>();
         ListTag blocks = tag.getList("blocks", 10);
         for (int i = 0; i < blocks.size(); i++) {
            CompoundTag b = blocks.getCompound(i);
            String block = palette.getCompound(b.getInt("state")).getString("Name");
            // Template.getDataBlocks: structure blocks with tile data in DATA mode
            if (block.equals("minecraft:structure_block") && b.contains("nbt", 10)) {
               CompoundTag nbt = b.getCompound("nbt");
               if ("DATA".equals(nbt.getString("mode"))) {
                  ListTag p = b.getList("pos", 3);
                  markers.add(new Marker(new BlockPos(p.getInt(0), p.getInt(1), p.getInt(2)), nbt.getString("metadata")));
               }
            }
         }
         return new TemplateData(new BlockPos(size.getInt(0), size.getInt(1), size.getInt(2)), markers);
      } catch (IOException e) {
         throw new IllegalStateException("bad mansion template " + path, e);
      }
   }

   final String templateName;
   final BlockPos templatePosition;
   final Rotation rotation;
   final Mirror mirror;
   final Box boundingBox;

   public MansionTemplate(Sizes sizes, String s, BlockPos blockpos, Rotation rotation) {
      this(sizes, s, blockpos, rotation, Mirror.NONE);
   }

   public MansionTemplate(Sizes sizes, String s, BlockPos blockpos, Rotation rotation, Mirror mirror) {
      this.templateName = s;
      this.templatePosition = blockpos;
      this.rotation = rotation;
      this.mirror = mirror;
      this.boundingBox = boundingBoxFromTemplate(sizes.size(s), blockpos, rotation, mirror);
   }

   /** StructureComponentTemplate.setBoundingBoxFromTemplate (maxX/maxZ are size, not size - 1, like 1.12) */
   private static Box boundingBoxFromTemplate(BlockPos size, BlockPos templatePosition, Rotation rotation, Mirror mirror) {
      BlockPos blockpos = rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90
         ? new BlockPos(size.getZ(), size.getY(), size.getX())
         : size;
      Box box = new Box(0, 0, 0, blockpos.getX(), blockpos.getY() - 1, blockpos.getZ());
      switch (rotation) {
         case NONE:
         default:
            break;
         case CLOCKWISE_90:
            box.offset(-blockpos.getX(), 0, 0);
            break;
         case COUNTERCLOCKWISE_90:
            box.offset(0, 0, -blockpos.getZ());
            break;
         case CLOCKWISE_180:
            box.offset(-blockpos.getX(), 0, -blockpos.getZ());
      }

      switch (mirror) {
         case NONE:
         default:
            break;
         case FRONT_BACK:
            BlockPos blockpos2 = BlockPos.ZERO;
            if (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90) {
               blockpos2 = blockpos2.relative(rotation.rotate(Direction.WEST), blockpos.getZ());
            } else if (rotation == Rotation.CLOCKWISE_180) {
               blockpos2 = blockpos2.relative(Direction.EAST, blockpos.getX());
            } else {
               blockpos2 = blockpos2.relative(Direction.WEST, blockpos.getX());
            }

            box.offset(blockpos2.getX(), 0, blockpos2.getZ());
            break;
         case LEFT_RIGHT:
            BlockPos blockpos1 = BlockPos.ZERO;
            if (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90) {
               blockpos1 = blockpos1.relative(rotation.rotate(Direction.NORTH), blockpos.getX());
            } else if (rotation == Rotation.CLOCKWISE_180) {
               blockpos1 = blockpos1.relative(Direction.SOUTH, blockpos.getZ());
            } else {
               blockpos1 = blockpos1.relative(Direction.NORTH, blockpos.getZ());
            }

            box.offset(blockpos1.getX(), 0, blockpos1.getZ());
      }

      box.offset(templatePosition.getX(), templatePosition.getY(), templatePosition.getZ());
      return box;
   }

   /** StructureComponentTemplate.addComponentParts: Template.addBlocksToWorld clipped to the box, then the data markers. */
   void addComponentParts(DWorld w, Random random, Box box) {
      StructureTemplate template = w.level.getLevel().getStructureManager()
         .get(ResourceLocation.fromNamespaceAndPath("rlgen", "mansion/" + this.templateName)).orElse(null);
      if (template == null) {
         LOGGER.error("rlgen: missing mansion template {}", this.templateName);
         return;
      }
      // PlacementSettings().setIgnoreEntities(true).setRotation(r).setMirror(m), ignoreStructureBlock, no replacedBlock
      StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(this.rotation).setMirror(this.mirror).setIgnoreEntities(true)
         .setBoundingBox(box.toModern()).addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK).setLiquidSettings(LiquidSettings.IGNORE_WATERLOGGING);
      template.placeInWorld(w.level, this.templatePosition, this.templatePosition, settings, RandomSource.create(0L), 2);
      // Template.getDataBlocks
      Map<BlockPos, String> map = new HashMap<>();
      for (Marker m : data(this.templateName).markers) {
         BlockPos p = StructureTemplate.calculateRelativePosition(settings, m.pos).offset(this.templatePosition);
         if (box.isVecInside(p)) {
            map.put(p, m.metadata);
         }
      }
      for (Map.Entry<BlockPos, String> entry : map.entrySet()) {
         this.handleDataMarker(entry.getValue(), entry.getKey(), w, random, box);
      }
   }

   private void handleDataMarker(String s, BlockPos blockpos, DWorld w, Random random, Box box) {
      if (s.startsWith("Chest")) {
         BlockState iblockstate = Blocks.CHEST.defaultBlockState();
         if ("ChestWest".equals(s)) {
            iblockstate = iblockstate.setValue(ChestBlock.FACING, this.rotation.rotate(Direction.WEST));
         } else if ("ChestEast".equals(s)) {
            iblockstate = iblockstate.setValue(ChestBlock.FACING, this.rotation.rotate(Direction.EAST));
         } else if ("ChestSouth".equals(s)) {
            iblockstate = iblockstate.setValue(ChestBlock.FACING, this.rotation.rotate(Direction.SOUTH));
         } else if ("ChestNorth".equals(s)) {
            iblockstate = iblockstate.setValue(ChestBlock.FACING, this.rotation.rotate(Direction.NORTH));
         }

         // StructureComponent.generateChest
         if (box.isVecInside(blockpos) && w.get(blockpos).getBlock() != Blocks.CHEST) {
            w.set(blockpos, iblockstate);
            BlockEntity be = w.level.getBlockEntity(blockpos);
            if (be instanceof ChestBlockEntity chest) {
               chest.setLootTable(CHESTS_WOODLAND_MANSION, random.nextLong());
            }
         }
      } else if ("Mage".equals(s)) {
         Evoker evoker = EntityType.EVOKER.create(w.level.getLevel());
         if (evoker != null) {
            evoker.setPersistenceRequired();
            evoker.moveTo(blockpos, 0.0F, 0.0F);
            w.level.addFreshEntityWithPassengers(evoker);
         }
         w.set(blockpos, Blocks.AIR.defaultBlockState());
      } else if ("Warrior".equals(s)) {
         Vindicator vindicator = EntityType.VINDICATOR.create(w.level.getLevel());
         if (vindicator != null) {
            vindicator.setPersistenceRequired();
            vindicator.moveTo(blockpos, 0.0F, 0.0F);
            vindicator.finalizeSpawn(w.level, w.level.getCurrentDifficultyAt(vindicator.blockPosition()), MobSpawnType.STRUCTURE, null);
            w.level.addFreshEntityWithPassengers(vindicator);
         }
         w.set(blockpos, Blocks.AIR.defaultBlockState());
      }
   }
}
