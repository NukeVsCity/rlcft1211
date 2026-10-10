package greymerk.roguelike.treasure;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.item.RldItemStack;
import greymerk.roguelike.dungeon.Dungeon;
import greymerk.roguelike.treasure.loot.ChestType;
import greymerk.roguelike.worldgen.BlockBrush;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import greymerk.roguelike.worldgen.WorldEditor;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TreasureChest {
   private ChestType chestType = ChestType.EMPTY;
   private final int level;
   private final Coord pos;
   private final WorldEditor worldEditor;
   private boolean isTrapped;
   private Direction facing;

   public TreasureChest(Coord pos, WorldEditor worldEditor) {
      this.level = Dungeon.getLevel(pos.getY());
      this.pos = pos.copy();
      this.worldEditor = worldEditor;
   }

   public void setSlot(int slot, RldItemStack itemStack) {
      this.worldEditor.setItem(this.pos, slot, itemStack);
   }

   public void setRandomEmptySlot(RldItemStack itemStack) {
      List<Integer> slots = IntStream.range(0, this.worldEditor.getCapacity(this)).boxed().collect(Collectors.toList());
      Collections.shuffle(slots);
      slots.stream()
         .mapToInt(slot -> slot)
         .filter(slot -> this.worldEditor.isEmptySlot(this, slot))
         .findFirst()
         .ifPresent(value -> this.setSlot(value, itemStack));
   }

   public ChestType getType() {
      return this.chestType;
   }

   public Coord getCoord() {
      return this.pos.copy();
   }

   public int getLevel() {
      return Math.max(0, Math.min(this.level, 4));
   }

   public void setLootTable(String table) {
      this.worldEditor.setLootTable(this.pos, table);
   }

   public boolean isNotEmpty() {
      return !this.isEmpty();
   }

   private boolean isEmpty() {
      return this.chestType == null || this.getType().equals(ChestType.EMPTY);
   }

   public WorldEditor getWorldEditor() {
      return this.worldEditor;
   }

   public TreasureChest withChestType(ChestType chestType) {
      this.chestType = chestType;
      return this;
   }

   public static boolean shouldBeTrapped(Random random, int level) {
      return random.nextInt(30 / Math.max(1, level)) == 0;
   }

   public TreasureChest withTrap(boolean isTrapped) {
      this.isTrapped = isTrapped;
      return this;
   }

   public TreasureChest withFacing(Direction facing) {
      this.facing = facing;
      return this;
   }

   public boolean isValidChestSpace() {
      return !this.isNextToChest();
   }

   private boolean isNextToChest() {
      return Direction.CARDINAL.stream().anyMatch(this::containsChestBlock);
   }

   private boolean containsChestBlock(Direction dir) {
      return this.worldEditor.isBlockOfTypeAt(BlockType.CHEST, this.getCoord().add(dir));
   }

   public Optional<TreasureChest> stroke(WorldEditor worldEditor, Coord coord) {
      if (!this.isValidChestSpace()) {
         return Optional.empty();
      } else {
         BlockBrush chestBlock = (this.isTrapped ? BlockType.TRAPPED_CHEST : BlockType.CHEST).getBrush().setFacing(this.facing);
         if (!chestBlock.stroke(worldEditor, coord)) {
            return Optional.empty();
         } else {
            this.strokeTrap();
            this.worldEditor.getTreasureManager().addChest(this);
            return Optional.of(this);
         }
      }
   }

   private void strokeTrap() {
      if (this.isTrapped) {
         Coord tntCoord = this.getCoord().copy().down(2);
         BlockType.TNT.getBrush().stroke(this.worldEditor, tntCoord);
         if (this.worldEditor.getRandom().nextBoolean()) {
            BlockType.TNT.getBrush().stroke(this.worldEditor, tntCoord.down());
         }
      }
   }
}
