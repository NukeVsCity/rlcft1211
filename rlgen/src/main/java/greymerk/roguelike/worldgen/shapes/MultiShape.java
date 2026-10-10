package greymerk.roguelike.worldgen.shapes;

import com.google.common.collect.Sets;
import greymerk.roguelike.worldgen.Coord;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class MultiShape implements IShape {
   private Set<Coord> shape = new HashSet<>();

   public void addShape(IShape toAdd) {
      for (Coord pos : toAdd) {
         this.shape.add(pos);
      }
   }

   @Nonnull
   @Override
   public Iterator<Coord> iterator() {
      return this.shape.iterator();
   }

   @Override
   public List<Coord> get() {
      return this.shape.stream().map(Coord::copy).collect(Collectors.toList());
   }

   @Override
   public Set<Coord> getAnchors() {
      return Sets.newHashSet(this.shape);
   }
}
