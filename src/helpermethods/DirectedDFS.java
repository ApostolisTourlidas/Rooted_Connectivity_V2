package helpermethods;
import graphpackage.DirectedEdge;
import graphpackage.EdgeWeightedDigraph;

/**
 *  The {@code DirectedDFS} class represents a data type for
 *  determining the vertices reachable from a given source vertex <em>s</em>
 *  (or set of source vertices) in a digraph. For versions that find the paths,
 *  see {@link DepthFirstDirectedPaths} and {@link BreadthFirstDirectedPaths}.
 *  <p>
 *  This implementation uses depth-first search.
 *  The constructor takes time proportional to <em>V</em> + <em>E</em>
 *  (in the worst case),
 *  where <em>V</em> is the number of vertices and <em>E</em> is the number of edges.
 *  Each instance method takes &Theta;(1) time.
 *  It uses &Theta;(<em>V</em>) extra space (not including the digraph).
 *  <p>
 *  For additional documentation,
 *  see <a href="https://algs4.cs.princeton.edu/42digraph">Section 4.2</a> of
 *  <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 *  @author Robert Sedgewick
 *  @author Kevin Wayne
 */
public class DirectedDFS {
    private boolean[] marked;  // marked[v] = true iff v is reachable from source(s)
    private int count;         // number of vertices reachable from source(s)

    /**
     * Computes the vertices in digraph {@code G} that are
     * reachable from the source vertex {@code s}.
     * @param G the digraph
     * @param s the source vertex
     * @throws IllegalArgumentException unless {@code 0 <= s < V}
     */
    public DirectedDFS(EdgeWeightedDigraph G, int s) {
        marked = new boolean[G.V()];
        validateVertex(s);
        dfs(G, s);
    }

    /**
     * Computes the vertices in digraph {@code G} that are
     * connected to any of the source vertices {@code sources}.
     * @param G the graph
     * @param sources the source vertices
     * @throws IllegalArgumentException if {@code sources} is {@code null}
     * @throws IllegalArgumentException if {@code sources} contains no vertices
     * @throws IllegalArgumentException unless {@code 0 <= s < V}
     *         for each vertex {@code s} in {@code sources}
     */
    public DirectedDFS(EdgeWeightedDigraph G, Iterable<Integer> sources) {
        marked = new boolean[G.V()];
        validateVertices(sources);
        for (int v : sources) {
            if (!marked[v]) dfs(G, v);
        }
    }

    private void dfs(EdgeWeightedDigraph G, int v) {
        count++;
        marked[v] = true;
        for (DirectedEdge w : G.adj(v)) {
            if (!marked[w.to()]) dfs(G, w.to());
        }
    }

    /**
     * Is there a directed path from the source vertex (or any
     * of the source vertices) and vertex {@code v}?
     * @param  v the vertex
     * @return {@code true} if there is a directed path, {@code false} otherwise
     * @throws IllegalArgumentException unless {@code 0 <= v < V}
     */
    public boolean marked(int v) {
        validateVertex(v);
        return marked[v];
    }

    /**
     * Returns the number of vertices reachable from the source vertex
     * (or source vertices).
     * @return the number of vertices reachable from the source vertex
     *   (or source vertices)
     */
    public int count() {
        return count;
    }

    // throw an IllegalArgumentException unless {@code 0 <= v < V}
    private void validateVertex(int v) {
        int V = marked.length;
        if (v < 0 || v >= V)
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V-1));
    }

    // throw an IllegalArgumentException if vertices is null, has zero vertices,
    // or has a vertex not between 0 and V-1
    private void validateVertices(Iterable<Integer> vertices) {
        if (vertices == null) {
            throw new IllegalArgumentException("argument is null");
        }
        int vertexCount = 0;
        for (Integer v : vertices) {
            vertexCount++;
            if (v == null) {
                throw new IllegalArgumentException("vertex is null");
            }
            validateVertex(v);
        }
        if (vertexCount == 0) {
            throw new IllegalArgumentException("zero vertices");
        }
    }

    /**
     * Unit tests the {@code DirectedDFS} data type.
     *
     * @param args the command-line arguments
     */
    /*public static void main(String[] args) {

        // read in digraph from command-line argument
        In in = new In(args[0]);
        EdgeWeightedDigraph G = new EdgeWeightedDigraph(in);

        // read in sources from command-line arguments
        Bag<Integer> sources = new Bag<Integer>();
        for (int i = 1; i < args.length; i++) {
            int s = Integer.parseInt(args[i]);
            sources.add(s);
        }

        // multiple-source reachability
        DirectedDFS dfs = new DirectedDFS(G, sources);

        // print out vertices reachable from sources
        for (int v = 0; v < G.V(); v++) {
            if (dfs.marked(v)) System.out.print(v + " ");
        }
        System.out.println();
    }*/

}
