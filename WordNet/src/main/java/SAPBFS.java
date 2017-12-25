import edu.princeton.cs.algs4.Digraph;
import java.util.Deque;
import java.util.ArrayDeque;

public class SAPBFS {
  private final boolean[] marked;
  private final int[] distances;
  private final int common;
  private final int shortestToCommon;

  public SAPBFS(Digraph graph, int source, int destination) {
    marked = new boolean[graph.V()];
    distances = new int[graph.V()];
    for (int i = 0; i < distances.length; i++) distances[i] = -1;
    validateVertex(source);
    validateVertex(destination);
    bfs(graph, source);
    int[] numsForCommon = closestCommonVertex(graph, destination);
    common = numsForCommon[0];
    shortestToCommon = numsForCommon[1];
  }

  public SAPBFS(Digraph graph, Iterable<Integer> sources, Iterable<Integer> destinations) {
    marked = new boolean[graph.V()];
    distances = new int[graph.V()];
    for (int i = 0; i < distances.length; i++) distances[i] = -1;
    validateVertices(sources);
    validateVertices(destinations);
    bfs(graph, sources);
    int[] numsForCommon = closestCommonVertex(graph, destinations);
    common = numsForCommon[0];
    shortestToCommon = numsForCommon[1];
  }

  public int commonVertex() {
    return common;
  }

  public int shortestCommonPath() {
    return shortestToCommon;
  }

  private void bfs(Digraph graph, int source) {
    Deque<Integer> queue = new ArrayDeque<>();

    queue.addLast(source);
    marked[source] = true;
    distances[source] = 0;

    while (!queue.isEmpty()) {
      int processed = queue.removeFirst();

      for (int v : graph.adj(processed)) {
        if (!marked[v]) {
          marked[v] = true;
          distances[v] = distances[processed] + 1;
          queue.addLast(v);
        }
      }
    }
  }

  private void bfs(Digraph graph, Iterable<Integer> sources) {
    Deque<Integer> queue = new ArrayDeque<>();

    for (int source : sources) {
      queue.addLast(source);
      marked[source] = true;
      distances[source] = 0;
    }

    while (!queue.isEmpty()) {
      int processed = queue.removeFirst();

      for (int v : graph.adj(processed)) {
        if (!marked[v]) {
          marked[v] = true;
          distances[v] = distances[processed] + 1;
          queue.addLast(v);
        }
      }
    }
  }

  private int[] closestCommonVertex(Digraph graph, int destination) {
    Deque<Integer> queue = new ArrayDeque<>();
    boolean[] virtualMarked = new boolean[marked.length];

    for (int i = 0; i < marked.length; i++) virtualMarked[i] = marked[i];

    int distCom = 0;

    if (marked[destination]) return new int[] {destination, distances[destination]};
    queue.addLast(destination);

    while (!queue.isEmpty()) {
      int processed = queue.removeFirst();
      distCom++;

      for (int v : graph.adj(processed)) {
        if (marked[v]) return new int[] {v, distances[v] + distCom};
        if (!virtualMarked[v]) {
          virtualMarked[v] = true;
          queue.addLast(v);
        }
      }
    }

    return new int[] {-1, -1};
  }

  private int[] closestCommonVertex(Digraph graph, Iterable<Integer> destinations) {
    Deque<Integer> queue = new ArrayDeque<>();
    boolean[] virtualMarked = new boolean[marked.length];

    for (int i = 0; i < marked.length; i++) virtualMarked[i] = marked[i];

    int distCom = 0;

    for (int destination : destinations) {
      if (marked[destination]) return new int[] {destination, distances[destination]};
      queue.addLast(destination);
    }

    while (!queue.isEmpty()) {
      int processed = queue.removeFirst();

      for (int v : graph.adj(processed)) {
        if (marked[v]) return new int[] {v, distances[v] + distCom};
        if (!virtualMarked[v]) {
          virtualMarked[v] = true;
          queue.addLast(v);
        }
      }
      distCom++;
    }

    return new int[] {-1, -1};
  }

  public boolean isReachable(int destination) {
    validateVertex(destination);
    return marked[destination];
  }

  private void validateVertex(int v) {
    int V = marked.length;
    if (v < 0 || v >= V)
      throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V - 1));
  }

  private void validateVertices(Iterable<Integer> vertices) {
    if (vertices == null) {
      throw new IllegalArgumentException("argument is null");
    }
    int V = marked.length;
    for (int v : vertices) {
      if (v < 0 || v >= V) {
        throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V - 1));
      }
    }
  }

  public int distanceTo(int destination) {
    validateVertex(destination);
    return distances[destination];
  }
}
