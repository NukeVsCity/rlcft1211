package greymerk.roguelike.dungeon.rooms.prototype;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.SingleBlockBrush;
import com.github.fnar.minecraft.block.spawner.MobType;
import com.github.fnar.minecraft.material.Crop;
import com.github.fnar.roguelike.worldgen.generatables.Pillar;
import com.google.common.collect.Sets;
import greymerk.roguelike.dungeon.base.BaseRoom;
import greymerk.roguelike.dungeon.rooms.RoomSetting;
import greymerk.roguelike.dungeon.settings.LevelSettings;
import greymerk.roguelike.treasure.TreasureChest;
import greymerk.roguelike.treasure.loot.ChestType;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.BlockJumble;
import greymerk.roguelike.worldgen.BlockWeightedRandom;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import greymerk.roguelike.worldgen.shapes.RectSolid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NetherFortressRoom extends BaseRoom {
   public NetherFortressRoom(RoomSetting roomSetting, LevelSettings levelSettings, WorldEditor worldEditor) {
      super(roomSetting, levelSettings, worldEditor);
      this.wallDist = 8;
      this.ceilingHeight = 7;
   }

   @Override
   public BaseRoom generate(Coord at, List<Direction> entrances) {
      super.generate(at, entrances);
      ChestType[] chestTypes = new ChestType[]{ChestType.GARDEN, ChestType.SUPPLIES, ChestType.TOOLS};
      Coord.randomFrom(at.copy().down().newRect(2).get(), this.random().nextInt(3) + 1, this.random())
         .forEach(
            chestLocation -> new TreasureChest(chestLocation, this.worldEditor)
                  .withChestType(this.getChestTypeOrUse(ChestType.chooseRandomAmong(this.random(), chestTypes)))
                  .withFacing(this.getEntrance(entrances).reverse())
                  .withTrap(TreasureChest.shouldBeTrapped(this.random(), this.levelSettings.getLevel()))
                  .stroke(this.worldEditor, chestLocation)
         );
      return this;
   }

   @Override
   protected void generateFloor(Coord at, List<Direction> entrances) {
      super.generateFloor(at, entrances);
      this.primaryFloorBrush().fill(this.worldEditor, at.newRect(this.getWallDist()).down());
      this.generateGardenWithCrops(at.copy().down(2));
   }

   @Override
   protected void generateCeiling(Coord at, List<Direction> entrances) {
      super.generateCeiling(at, entrances);
      Coord ceilingLiquid = at.copy().up(this.getCeilingHeight());
      this.primaryWallBrush().fill(this.worldEditor, ceilingLiquid.newRect(this.getWallDist()).down());
      this.primaryLiquidBrush().fill(this.worldEditor, ceilingLiquid.newRect(3));
   }

   private void generateGardenWithCrops(Coord gardenLocation) {
      RectSolid cropRectangle = gardenLocation.copy().up().newRect(4);
      SingleBlockBrush.AIR.fill(this.worldEditor, cropRectangle);
      RectSolid gardenRectangle = gardenLocation.newRect(4);
      this.getSoil().fill(this.worldEditor, gardenRectangle);
      this.getCrops().fill(this.worldEditor, cropRectangle);
      this.primaryLiquidBrush().stroke(this.worldEditor, gardenLocation);
      SingleBlockBrush.AIR.stroke(this.worldEditor, gardenLocation.copy().up());
   }

   private SingleBlockBrush getSoil() {
      return this.isHotGarden() ? BlockType.SOUL_SAND.getBrush() : BlockType.FARMLAND.getBrush();
   }

   private boolean isHotGarden() {
      SingleBlockBrush liquid = (SingleBlockBrush)this.primaryLiquidBrush();
      boolean isBlockTypeLava = liquid.getBlockType() != null && liquid.getBlockType().equals(BlockType.LAVA_FLOWING);
      boolean hasJsonLava = liquid.getJson() != null && liquid.getJson().toString().toLowerCase().contains("lava");
      return isBlockTypeLava || hasJsonLava;
   }

   private BlockWeightedRandom getCrops() {
      BlockWeightedRandom crops = new BlockWeightedRandom();
      crops.addBlock(SingleBlockBrush.AIR, 3);
      crops.addBlock(this.selectCrop(), 1);
      return crops;
   }

   private BlockBrush selectCrop() {
      if (this.isHotGarden()) {
         return Crop.NETHER_WART.getBrush();
      } else {
         Set<Crop> eligibleCrops = Sets.newHashSet(new Crop[]{Crop.CARROTS, Crop.POTATOES, Crop.WHEAT});
         return new BlockJumble(eligibleCrops.stream().map(Crop::getBrush).collect(Collectors.toList()));
      }
   }

   @Override
   protected void generateDecorations(Coord at, List<Direction> entrances) {
      for (Direction cardinal : Direction.CARDINAL) {
         this.primaryWallBrush()
            .fill(
               this.worldEditor,
               RectSolid.newRect(
                  at.copy().up(5).translate(cardinal, 4).translate(cardinal.left(), 6), at.copy().up(5).translate(cardinal, 4).translate(cardinal.right(), 6)
               )
            );
         this.primaryWallBrush()
            .fill(
               this.worldEditor,
               RectSolid.newRect(
                  at.copy().up(5).translate(cardinal, 6).translate(cardinal.left(), 6), at.copy().up(5).translate(cardinal, 6).translate(cardinal.right(), 6)
               )
            );
         this.primaryStairBrush()
            .setUpsideDown(false)
            .setFacing(cardinal.reverse())
            .fill(
               this.worldEditor,
               RectSolid.newRect(
                  at.copy().down().translate(cardinal, 4).translate(cardinal.left(), 2), at.copy().down().translate(cardinal, 4).translate(cardinal.right(), 2)
               )
            );
         this.supportPillar(at.copy().translate(cardinal, 4).translate(cardinal.antiClockwise(), 4));

         for (Direction orthogonal : cardinal.orthogonals()) {
            Coord pillar = at.copy().translate(cardinal, 7).translate(orthogonal, 2);
            this.pillar(pillar);
            this.pillar(pillar.translate(orthogonal, 3));
         }
      }
   }

   private void supportPillar(Coord at) {
      for (Direction dir : Direction.CARDINAL) {
         this.primaryPillarBrush().fill(this.worldEditor, RectSolid.newRect(at.copy(), at.copy().translate(Direction.UP, 5)).translate(dir));
         this.primaryStairBrush().setUpsideDown(true).setFacing(dir).stroke(this.worldEditor, at.copy().translate(dir, 2).translate(Direction.UP, 4));
      }

      Coord start = at.copy();
      Coord end = start.copy();
      end.translate(Direction.UP, 5);
      this.primaryLiquidBrush().fill(this.worldEditor, RectSolid.newRect(start, end));
      List<Coord> core = RectSolid.newRect(start, end).get();
      Coord spawnerLocation = core.get(this.random().nextInt(core.size()));
      this.generateSpawner(spawnerLocation, new MobType[0]);
   }

   private void pillar(Coord origin) {
      Pillar.newPillar(this.worldEditor).withStairs(this.primaryStairBrush()).withPillar(this.primaryPillarBrush()).withHeight(5).generate(origin);
   }
}
