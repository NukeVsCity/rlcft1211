package greymerk.roguelike.theme;

import com.github.fnar.minecraft.block.BlockType;
import com.github.fnar.minecraft.block.normal.StairsBlock;
import com.github.fnar.minecraft.block.redstone.DoorBlock;
import greymerk.roguelike.worldgen.BlockBrush;
import java.util.Optional;

public class BlockSet {
   private BlockBrush floor;
   private BlockBrush walls = BlockType.STONE_BRICK.getBrush();
   private StairsBlock stair = StairsBlock.stoneBrick();
   private BlockBrush pillar;
   private DoorBlock door = DoorBlock.oak();
   private BlockBrush lightBlock = BlockType.GLOWSTONE.getBrush();
   private BlockBrush liquid = BlockType.WATER_FLOWING.getBrush();

   public BlockSet() {
   }

   public BlockSet(BlockSet toCopy) {
      this.floor = toCopy.getFloor();
      this.walls = toCopy.getWall();
      this.stair = toCopy.getStair();
      this.pillar = toCopy.getPillar();
      this.door = toCopy.getDoor();
      this.lightBlock = toCopy.getLightBlock();
      this.liquid = toCopy.getLiquid();
   }

   public BlockSet(BlockBrush floor, BlockBrush walls, StairsBlock stair, BlockBrush pillar, DoorBlock door, BlockBrush lightBlock, BlockBrush liquid) {
      this.floor = floor;
      this.walls = walls;
      this.stair = stair;
      this.pillar = pillar;
      this.door = door;
      this.lightBlock = lightBlock;
      this.liquid = liquid;
   }

   public BlockSet(BlockBrush floor, BlockBrush walls, StairsBlock stair, BlockBrush pillar, DoorBlock door) {
      this(floor, walls, stair, pillar, door, BlockType.GLOWSTONE.getBrush(), BlockType.WATER_FLOWING.getBrush());
   }

   public BlockSet(BlockBrush floor, BlockBrush walls, StairsBlock stair, BlockBrush pillar) {
      this(floor, walls, stair, pillar, DoorBlock.oak());
   }

   public BlockSet(BlockBrush walls, StairsBlock stair, BlockBrush pillar) {
      this(walls, walls, stair, pillar);
   }

   static BlockSet inherit(BlockSet parentBlockSet, BlockSet childBlockSet) {
      if (parentBlockSet == null && childBlockSet == null) {
         return new BlockSet();
      } else if (parentBlockSet == null) {
         return childBlockSet;
      } else {
         return childBlockSet == null
            ? parentBlockSet
            : new BlockSet(
               Optional.ofNullable(childBlockSet.getFloor()).orElse(parentBlockSet.getFloor()),
               Optional.ofNullable(childBlockSet.getWall()).orElse(parentBlockSet.getWall()),
               Optional.ofNullable(childBlockSet.getStair()).orElse(parentBlockSet.getStair()),
               Optional.ofNullable(childBlockSet.getPillar()).orElse(parentBlockSet.getPillar()),
               Optional.ofNullable(childBlockSet.getDoor()).orElse(parentBlockSet.getDoor()),
               Optional.ofNullable(childBlockSet.getLightBlock()).orElse(parentBlockSet.getLightBlock()),
               Optional.ofNullable(childBlockSet.getLiquid()).orElse(parentBlockSet.getLiquid())
            );
      }
   }

   public BlockBrush getWall() {
      return this.walls.copy();
   }

   public StairsBlock getStair() {
      return this.stair.copy();
   }

   public BlockBrush getPillar() {
      return Optional.ofNullable(this.pillar).orElse(this.getWall()).copy();
   }

   public BlockBrush getFloor() {
      return Optional.ofNullable(this.floor).orElse(this.getWall()).copy();
   }

   public DoorBlock getDoor() {
      return this.door.copy();
   }

   public BlockBrush getLightBlock() {
      return this.lightBlock.copy();
   }

   public BlockBrush getLiquid() {
      return this.liquid.copy();
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof BlockSet)) {
         return false;
      } else {
         BlockSet other = (BlockSet)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$floor = this.getFloor();
            Object other$floor = other.getFloor();
            if (this$floor == null ? other$floor == null : this$floor.equals(other$floor)) {
               Object this$walls = this.walls;
               Object other$walls = other.walls;
               if (this$walls == null ? other$walls == null : this$walls.equals(other$walls)) {
                  Object this$stair = this.getStair();
                  Object other$stair = other.getStair();
                  if (this$stair == null ? other$stair == null : this$stair.equals(other$stair)) {
                     Object this$pillar = this.getPillar();
                     Object other$pillar = other.getPillar();
                     if (this$pillar == null ? other$pillar == null : this$pillar.equals(other$pillar)) {
                        Object this$door = this.getDoor();
                        Object other$door = other.getDoor();
                        if (this$door == null ? other$door == null : this$door.equals(other$door)) {
                           Object this$lightBlock = this.getLightBlock();
                           Object other$lightBlock = other.getLightBlock();
                           if (this$lightBlock == null ? other$lightBlock == null : this$lightBlock.equals(other$lightBlock)) {
                              Object this$liquid = this.getLiquid();
                              Object other$liquid = other.getLiquid();
                              return this$liquid == null ? other$liquid == null : this$liquid.equals(other$liquid);
                           } else {
                              return false;
                           }
                        } else {
                           return false;
                        }
                     } else {
                        return false;
                     }
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         }
      }
   }

   protected boolean canEqual(Object other) {
      return other instanceof BlockSet;
   }

   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $floor = this.getFloor();
      result = result * 59 + ($floor == null ? 43 : $floor.hashCode());
      Object $walls = this.walls;
      result = result * 59 + ($walls == null ? 43 : $walls.hashCode());
      Object $stair = this.getStair();
      result = result * 59 + ($stair == null ? 43 : $stair.hashCode());
      Object $pillar = this.getPillar();
      result = result * 59 + ($pillar == null ? 43 : $pillar.hashCode());
      Object $door = this.getDoor();
      result = result * 59 + ($door == null ? 43 : $door.hashCode());
      Object $lightBlock = this.getLightBlock();
      result = result * 59 + ($lightBlock == null ? 43 : $lightBlock.hashCode());
      Object $liquid = this.getLiquid();
      return result * 59 + ($liquid == null ? 43 : $liquid.hashCode());
   }

   @Override
   public String toString() {
      return "BlockSet(floor="
         + this.getFloor()
         + ", walls="
         + this.walls
         + ", stair="
         + this.getStair()
         + ", pillar="
         + this.getPillar()
         + ", door="
         + this.getDoor()
         + ", lightBlock="
         + this.getLightBlock()
         + ", liquid="
         + this.getLiquid()
         + ")";
   }
}
