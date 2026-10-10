package greymerk.roguelike.util.graph;

public class Edge<P> implements Comparable<Edge<P>> {
   private P start;
   private P end;
   private double length;

   public Edge(P start, P end, double length) {
      this.start = start;
      this.end = end;
      this.length = length;
   }

   public int compareTo(Edge<P> other) {
      return Double.compare(this.length, other.length);
   }

   public P getStart() {
      return this.start;
   }

   public P getEnd() {
      return this.end;
   }
}
