import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import graphpackage.DirectedEdge;
import graphpackage.EdgeWeightedDigraph;
import helpermethods.TarjanSCC;
import helpermethods.TransitiveClosure;

/******************************************************************************
 *  Compilation:  javac GabowSCC.java
 *  Execution:    java GabowSCC V E
 *  Dependencies: Digraph.java Stack.java TransitiveClosure.java StdOut.java
 *  Data files:   https://algs4.cs.princeton.edu/42digraph/tinyDG.txt
 *                https://algs4.cs.princeton.edu/42digraph/mediumDG.txt
 *                https://algs4.cs.princeton.edu/42digraph/largeDG.txt
 *
 *  Compute the strongly-connected components of a digraph using
 *  Gabow's algorithm (aka Cheriyan-Mehlhorn algorithm).
 *
 *  Runs in O(E + V) time.
 *
 *  % java GabowSCC tinyDG.txt
 *  5 components
 *  1
 *  0 2 3 4 5
 *  9 10 11 12
 *  6 8
 *  7
 *
 ******************************************************************************/
/**
 *  The {@code GabowSCC} class represents a data type for
 *  determining the strong components in a digraph.
 *  The <em>id</em> operation determines in which strong component
 *  a given vertex lies; the <em>areStronglyConnected</em> operation
 *  determines whether two vertices are in the same strong component;
 *  and the <em>count</em> operation determines the number of strong
 *  components.

 *  The <em>component identifier</em> of a component is one of the
 *  vertices in the strong component: two vertices have the same component
 *  identifier if and only if they are in the same strong component.

 *  <p>
 *  This implementation uses the Gabow's algorithm.
 *  The constructor takes &Theta;(<em>V</em> + <em>E</em>) time,
 *  where <em>V</em> is the number of vertices and <em>E</em> is
 *  the number of edges.
 *  Each instance method takes &Theta;(1) time.
 *  It uses &Theta;(<em>V</em>) extra space (not including the digraph).
 *  For alternative implementations of the same API, see
 *  {@link KosarajuSharirSCC} and {@link TarjanSCC}.
 *  <p>
 *  For additional documentation,
 *  see <a href="https://algs4.cs.princeton.edu/42digraph">Section 4.2</a> of
 *  <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 *  @author Robert Sedgewick
 *  @author Kevin Wayne
 */
public class GabowSCC {

    private boolean[] marked;        // marked[v] = has v been visited?
    private int[] id;                // id[v] = id of strong component containing v
    private int[] preorder;          // preorder[v] = preorder of v
    private int pre;                 // preorder number counter
    private int count;               // number of strongly-connected components
    private Stack<Integer> stack1;
    private Stack<Integer> stack2;


    /**
     * Computes the strong components of the digraph {@code G}.
     * @param G the digraph
     */
    public GabowSCC(EdgeWeightedDigraph G) {
        marked = new boolean[G.V()];
        stack1 = new Stack<Integer>();
        stack2 = new Stack<Integer>();
        id = new int[G.V()];
        preorder = new int[G.V()];
        for (int v = 0; v < G.V(); v++)
            id[v] = -1;

        for (int v = 0; v < G.V(); v++) {
            if (!marked[v]) dfs(G, v);
        }

        // check that id[] gives strong components
        assert check(G);
    }

    private void dfs(EdgeWeightedDigraph G, int v) {
        marked[v] = true;
        preorder[v] = pre++;
        stack1.push(v);
        stack2.push(v);
        for (DirectedEdge w : G.adj(v)) {
            if (!marked[w.to()]) dfs(G, w.to());
            else if (id[w.to()] == -1) {
                while (preorder[stack2.peek()] > preorder[w.to()])
                    stack2.pop();
            }
        }

        // found strong component containing v
        if (stack2.peek() == v) {
            stack2.pop();
            int w;
            do {
                w = stack1.pop();
                id[w] = count;
            } while (w != v);
            count++;
        }
    }

    /**
     * Returns the number of strong components.
     * @return the number of strong components
     */
    public int count() {
        return count;
    }

    /**
     * Are vertices {@code v} and {@code w} in the same strong component?
     * @param  v one vertex
     * @param  w the other vertex
     * @return {@code true} if vertices {@code v} and {@code w} are in the same
     *         strong component, and {@code false} otherwise
     * @throws IllegalArgumentException unless {@code 0 <= v < V}
     * @throws IllegalArgumentException unless {@code 0 <= w < V}
     */
    public boolean stronglyConnected(int v, int w) {
        validateVertex(v);
        validateVertex(w);
        return id[v] == id[w];
    }

    /**
     * Returns the component id of the strong component containing vertex {@code v}.
     * @param  v the vertex
     * @return the component id of the strong component containing vertex {@code v}
     * @throws IllegalArgumentException unless {@code 0 <= v < V}
     */
    public int id(int v) {
        validateVertex(v);
        return id[v];
    }

    // does the id[] array contain the strongly connected components?
    private boolean check(EdgeWeightedDigraph G) {
        TransitiveClosure tc = new TransitiveClosure(G);
        for (int v = 0; v < G.V(); v++) {
            for (int w = 0; w < G.V(); w++) {
                if (stronglyConnected(v, w) != (tc.reachable(v, w) && tc.reachable(w, v)))
                    return false;
            }
        }
        return true;
    }

    // throw an IllegalArgumentException unless {@code 0 <= v < V}
    private void validateVertex(int v) {
        int V = marked.length;
        if (v < 0 || v >= V)
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V-1));
    }


    /**
     * Unit tests the {@code GabowSCC} data type.
     *
     * @param args the command-line arguments
     */
    // public static void main(String[] args) {
    //     In in = new In(args[0]);
    //     EdgeWeightedDigraph G = new EdgeWeightedDigraph(in);
    //     GabowSCC scc = new GabowSCC(G);

    //     // number of connected components
    //     int m = scc.count();
    //     System.out.println(m + " components");

    //     // compute list of vertices in each strong component
    //     @SuppressWarnings("unchecked")
    //     Queue<Integer>[] components = (Queue<Integer>[]) new Queue[m];
    //     for (int i = 0; i < m; i++) {
    //         components[i] = new LinkedList<Integer>();
    //     }
    //     for (int v = 0; v < G.V(); v++) {
    //         components[scc.id(v)].add(v);
    //     }

    //     // print results
    //     for (int i = 0; i < m; i++) {
    //         for (int v : components[i]) {
    //             System.out.print(v + " ");
    //         }
    //         System.out.println();
    //     }

    // }

}
