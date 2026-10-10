package greymerk.roguelike.dungeon.towers;

import com.github.fnar.minecraft.block.normal.StairsBlock;
import com.github.fnar.minecraft.block.redstone.DoorBlock;
import greymerk.roguelike.theme.Theme;
import greymerk.roguelike.treasure.TreasureChest;
import greymerk.roguelike.treasure.loot.ChestType;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;

public abstract class Tower {
   protected WorldEditor editor;
   protected Theme theme;

   public Tower(WorldEditor worldEditor, Theme theme) {
      this.editor = worldEditor;
      this.theme = theme;
   }

   public void chest(WorldEditor worldEditor, Direction dir, Coord coord) {
      new TreasureChest(coord, worldEditor).withChestType(ChestType.STARTER).withTrap(false).withFacing(dir).stroke(worldEditor, coord);
   }

   public abstract void generate(Coord var1);

   protected BlockBrush getPrimaryWall() {
      return this.theme.getPrimary().getWall();
   }

   protected BlockBrush getPrimaryPillar() {
      return this.theme.getPrimary().getPillar();
   }

   protected StairsBlock getPrimaryStair() {
      return this.theme.getPrimary().getStair();
   }

   protected DoorBlock getPrimaryDoor() {
      return this.theme.getPrimary().getDoor();
   }

   protected BlockBrush getPrimaryFloor() {
      return this.theme.getPrimary().getFloor();
   }

   protected BlockBrush getSecondaryFloor() {
      return this.theme.getSecondary().getFloor();
   }

   protected BlockBrush getSecondaryPillar() {
      return this.theme.getSecondary().getPillar();
   }

   protected StairsBlock getSecondaryStair() {
      return this.theme.getSecondary().getStair();
   }

   protected BlockBrush getSecondaryWall() {
      return this.theme.getSecondary().getWall();
   }

   protected BlockBrush getPrimaryLight() {
      return this.theme.getPrimary().getLightBlock();
   }
}
