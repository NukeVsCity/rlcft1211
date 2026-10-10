package greymerk.roguelike.dungeon.layout.classic;

import greymerk.roguelike.dungeon.layout.DungeonNode;
import greymerk.roguelike.dungeon.layout.LayoutGenerator;
import greymerk.roguelike.dungeon.layout.LevelLayout;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.Direction;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LayoutGeneratorClassic implements LayoutGenerator {
   private static final int MIN_ROOMS = 6;
   private final LevelLayout layout = new LevelLayout();
   private Coord start;
   private final int numRooms;
   private final int scatter;
   private final int range;
   private List<Node> graphNodes;

   public LayoutGeneratorClassic(int numRooms, int scatter, int range) {
      this.numRooms = numRooms;
      this.scatter = scatter;
      this.range = range;
   }

   @Override
   public LevelLayout generate(Coord start, Random random) {
      this.setStart(start);
      Node startNode = new Node(this, start, Direction.randomCardinal(random));
      List<Node> gNodes = this.generateGraph(startNode, random);
      return this.generateLayoutFromGraph(gNodes, startNode, random);
   }

   private List<Node> generateGraph(Node startNode, Random random) {
      this.graphNodes = new ArrayList<>();
      this.graphNodes.add(startNode);

      while (!this.isDone(this.graphNodes)) {
         this.update(this.graphNodes, random);
      }

      this.graphNodes.forEach(Node::cull);
      return this.graphNodes;
   }

   private LevelLayout generateLayoutFromGraph(List<Node> graphNodes, Node startNode, Random random) {
      DungeonNode startDungeonNode = null;

      for (Node node : graphNodes) {
         DungeonNode dungeonNode = node.asDungeonNode();
         if (node == startNode) {
            startDungeonNode = dungeonNode;
         }

         this.layout.addNode(dungeonNode);
         this.layout.addTunnels(node.createTunnels());
      }

      this.layout.setStartEnd(random, startDungeonNode);
      return this.layout;
   }

   public void update(List<Node> nodes, Random random) {
      if (!this.isFull(nodes)) {
         for (int i = 0; i < nodes.size(); i++) {
            nodes.get(i).update(random);
         }
      }
   }

   private boolean isDone(List<Node> nodes) {
      return nodes.stream().allMatch(Node::isDone) || this.isFull(nodes);
   }

   private boolean isFull(List<Node> nodes) {
      return nodes.size() >= Math.max(this.numRooms, 6);
   }

   void createNode(Edge edge) {
      this.graphNodes.add(new Node(this, edge.getEnd(), edge.getDirection()));
   }

   boolean hasNodeNearby(Coord coord) {
      return this.graphNodes.stream().map(Node::getPos).mapToInt(coord::distanceAsInt).anyMatch(distance -> distance < this.scatter);
   }

   @Override
   public LevelLayout getLayout() {
      return this.layout;
   }

   public int getScatter() {
      return this.scatter;
   }

   public Coord getStart() {
      return this.start;
   }

   public void setStart(Coord start) {
      this.start = start;
   }

   public int getRange() {
      return this.range;
   }
}
