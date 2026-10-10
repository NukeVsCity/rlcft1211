package greymerk.roguelike.dungeon.layout.classic;

import greymerk.roguelike.dungeon.layout.DungeonNode;
import greymerk.roguelike.dungeon.layout.DungeonTunnel;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Node {
   private final Coord pos;
   private final Direction direction;
   private List<Edge> edges = new ArrayList<>();

   public Node(LayoutGeneratorClassic layoutGeneratorClassic, Coord pos, Direction direction) {
      this.pos = pos;
      this.direction = direction;
      if (layoutGeneratorClassic.getStart().distance(this.pos) <= (double)layoutGeneratorClassic.getRange()) {
         Direction.CARDINAL
            .stream()
            .filter(dir -> !dir.equals(this.direction.reverse()))
            .map(dir -> new Edge(layoutGeneratorClassic, this.pos.copy(), dir))
            .forEach(edge -> this.edges.add(edge));
      }
   }

   public void update(Random random) {
      this.edges.forEach(edge -> edge.update(random));
   }

   public boolean isDone() {
      return this.edges.stream().allMatch(Edge::isDone);
   }

   public Coord getPos() {
      return this.pos.copy();
   }

   public List<Direction> getEntrances() {
      List<Direction> entrances = new ArrayList<>();
      entrances.add(this.direction.reverse());
      this.edges.stream().map(Edge::getDir).forEach(entrances::add);
      return entrances;
   }

   public List<Edge> getEdges() {
      return this.edges;
   }

   public List<DungeonTunnel> createTunnels() {
      return this.getEdges().stream().map(Edge::asDungeonTunnel).collect(Collectors.toList());
   }

   public DungeonNode asDungeonNode() {
      return new DungeonNode(this.getEntrances(), this.pos);
   }

   public void cull() {
      this.edges = this.edges.stream().filter(Edge::isDone).collect(Collectors.toList());
   }
}
