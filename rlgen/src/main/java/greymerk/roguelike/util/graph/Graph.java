package greymerk.roguelike.util.graph;

import java.util.ArrayList;
import java.util.List;

public class Graph<P> {
   private List<P> points = new ArrayList<>();
   private List<Edge<P>> edges = new ArrayList<>();

   public void addEdge(Edge<P> edge) {
      P start = edge.getStart();
      P end = edge.getEnd();
      if (!this.points.contains(start)) {
         this.points.add(start);
      }

      if (!this.points.contains(end)) {
         this.points.add(end);
      }

      this.edges.add(edge);
   }

   public List<P> getPoints() {
      return this.points;
   }

   public List<Edge<P>> getEdges() {
      return this.edges;
   }
}
