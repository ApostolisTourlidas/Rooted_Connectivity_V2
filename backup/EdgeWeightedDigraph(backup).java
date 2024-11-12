package graphpackage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Stack;

import Bag;
import In;
import TarjanSCC;

/**
 *  The {@code EdgeWeightedDigraph} class represents an edge-weighted
 *  digraph of vertices named 0 through <em>V</em> - 1, where each
 *  directed edge is of type {@link DirectedEdge} and has a real-valued weight.
 *  It supports the following two primary operations: add a directed edge
 *  to the digraph and iterate over all edges incident from a given vertex.
 *  It also provides methods for returning the indegree or outdegree of a
 *  vertex, the number of vertices <em>V</em> in the digraph, and
 *  the number of edges <em>E</em> in the digraph.
 *  Parallel edges and self-loops are permitted.
 *  <p>
 *  This implementation uses an <em>adjacency-lists representation</em>, which
 *  is a vertex-indexed array of {@link Bag} objects.
 *  It uses &Theta;(<em>E</em> + <em>V</em>) space, where <em>E</em> is
 *  the number of edges and <em>V</em> is the number of vertices.
 *  All instance methods take &Theta;(1) time. (Though, iterating over
 *  the edges returned by {@link #adj(int)} takes time proportional
 *  to the outdegree of the vertex.)
 *  Constructing an empty edge-weighted digraph with <em>V</em> vertices
 *  takes &Theta;(<em>V</em>) time; constructing an edge-weighted digraph
 *  with <em>E</em> edges and <em>V</em> vertices takes
 *  &Theta;(<em>E</em> + <em>V</em>) time.
 *  <p>
 *  For additional documentation,
 *  see <a href="https://algs4.cs.princeton.edu/44sp">Section 4.4</a> of
 *  <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 *  @author Robert Sedgewick
 *  @author Kevin Wayne
 */
public class EdgeWeightedDigraph {
    private static String NEWLINE = System.getProperty("line.separator");

    private int V;                // number of vertices in this digraph
    private int E;                      // number of edges in this digraph
    private Bag<DirectedEdge>[] adj;    // adj[v] = adjacency list for vertex v
    private int[] indegree;             // indegree[v] = indegree of vertex v
    private double[] weightedInDegree;  // weightedInDegree[v] = total weight of incoming edges of vertex v 

    /**
     * Initializes an empty edge-weighted digraph with {@code V} vertices and 0 edges.
     *
     * @param  V the number of vertices
     * @throws IllegalArgumentException if {@code V < 0}
     */
    @SuppressWarnings("unchecked")
    public EdgeWeightedDigraph(int V) {
        if (V < 0) throw new IllegalArgumentException("Number of vertices in a Digraph must be non-negative");
        this.V = V;
        this.E = 0;
        this.indegree = new int[V];
        this.weightedInDegree = new double[V];
        adj = (Bag<DirectedEdge>[]) new Bag[V];
        for (int v = 0; v < V; v++){
            adj[v] = new Bag<DirectedEdge>();
        }
    }

    /**
     * Initializes a random edge-weighted digraph with {@code V} vertices and <em>E</em> edges.
     *
     * @param  V the number of vertices
     * @param  E the number of edges
     * @throws IllegalArgumentException if {@code V < 0}
     * @throws IllegalArgumentException if {@code E < 0}
     *
    public EdgeWeightedDigraph(int V, int E) {
        this(V);
        if (E < 0) throw new IllegalArgumentException("Number of edges in a Digraph must be non-negative");
        for (int i = 0; i < E; i++) {
            int v = StdRandom.uniformInt(V);
            int w = StdRandom.uniformInt(V);
            double weight = 0.01 * StdRandom.uniformInt(100);
            DirectedEdge e = new DirectedEdge(v, w, weight);
            addEdge(e);
        }
    }
    */

    /**
     * Initializes an edge-weighted digraph from the specified input stream.
     * The format is the number of vertices <em>V</em>,
     * followed by the number of edges <em>E</em>,
     * followed by <em>E</em> pairs of vertices and edge weights,
     * with each entry separated by whitespace.
     *
     * @param  in the input stream
     * @throws IllegalArgumentException if {@code in} is {@code null}
     * @throws IllegalArgumentException if the endpoints of any edge are not in prescribed range
     * @throws IllegalArgumentException if the number of vertices or edges is negative
     */
    
    @SuppressWarnings("unchecked")
    public EdgeWeightedDigraph(In in) {
        if (in == null) throw new IllegalArgumentException("argument is null");
        try {
            this.V = in.readInt();
            if (V < 0) throw new IllegalArgumentException("number of vertices in a Digraph must be non-negative");
            indegree = new int[V];
            this.weightedInDegree = new double[V];
            adj = (Bag<DirectedEdge>[]) new Bag[V];
            for (int v = 0; v < V; v++) {
                adj[v] = new Bag<DirectedEdge>();
            }

            int E = in.readInt();
            if (E < 0) throw new IllegalArgumentException("Number of edges must be non-negative");
            for (int i = 0; i < E; i++) {
                int v = in.readInt();
                int w = in.readInt();
                validateVertex(v);
                validateVertex(w);
                double weight = in.readDouble();
                addEdge(new DirectedEdge(v, w, weight));
            }
        }
        catch (NoSuchElementException e) {
            throw new IllegalArgumentException("invalid input format in EdgeWeightedDigraph constructor", e);
        }
    }

    /**
     * Initializes a new edge-weighted digraph that is a deep copy of {@code G}.
     *
     * @param  G the edge-weighted digraph to copy
     */
    // @SuppressWarnings("unchecked")
    @SuppressWarnings("unchecked")
    public EdgeWeightedDigraph(EdgeWeightedDigraph G) {
        this.V = G.V();
        this.E = G.E();
        this.indegree = new int[V];
        this.weightedInDegree = new double[V];
        this.adj = (Bag<DirectedEdge>[]) new Bag[V];
        for (int v = 0; v < G.V(); v++){
            this.indegree[v] = G.indegree(v);
            this.adj[v] = new Bag<DirectedEdge>();
        }
        for (int v = 0; v < G.V(); v++) {
            // reverse so that adjacency list is in same order as original
            Stack<DirectedEdge> reverse = new Stack<DirectedEdge>();
            for (DirectedEdge e : G.adj[v]) {
                reverse.push(new DirectedEdge(e.from(), e.to(), e.weight())); //new edge because if i push e will not be reset edges
            }
            while (reverse.isEmpty()==false) {
                this.adj[v].add(reverse.pop());
            }
        }
    }

    /**
     * Returns the number of vertices in this edge-weighted digraph.
     *
     * @return the number of vertices in this edge-weighted digraph
     */
    public int V() {
        return V;
    }

    /**
     * Returns the number of edges in this edge-weighted digraph.
     *
     * @return the number of edges in this edge-weighted digraph
     */
    public int E() {
        return E;
    }

    // throw an IllegalArgumentException unless {@code 0 <= v < V}
    private void validateVertex(int v) {
        if (v < 0 || v >= V)
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V-1));
    }

    /**
     * Adds the directed edge {@code e} to this edge-weighted digraph.
     *
     * @param  e the edge
     * @throws IllegalArgumentException unless endpoints of edge are between {@code 0}
     *         and {@code V-1}
     */
    public void addEdge(DirectedEdge e) {
        int u = e.from();
        int v = e.to();
        validateVertex(u);
        validateVertex(v);
        adj[u].add(e);
        indegree[v]++;
        weightedInDegree[v] += e.weight();
        E++;
    }


    /**
     * Returns the directed edges incident from vertex {@code v}.
     *
     * @param  v the vertex
     * @return the directed edges incident from vertex {@code v} as an Iterable
     * @throws IllegalArgumentException unless {@code 0 <= v < V}
     */
    public Iterable<DirectedEdge> adj(int v) {
        validateVertex(v);
        return adj[v];
    }

    /**
     * Returns the number of directed edges incident from vertex {@code v}.
     * This is known as the <em>outdegree</em> of vertex {@code v}.
     *
     * @param  v the vertex
     * @return the outdegree of vertex {@code v}
     * @throws IllegalArgumentException unless {@code 0 <= v < V}
     */
    public int outdegree(int v) {
        validateVertex(v);
        return adj[v].size();
    }

    /**
     * Returns the number of directed edges incident to vertex {@code v}.
     * This is known as the <em>indegree</em> of vertex {@code v}.
     *
     * @param  v the vertex
     * @return the indegree of vertex {@code v}
     * @throws IllegalArgumentException unless {@code 0 <= v < V}
     */
    public int indegree(int v) {
        validateVertex(v);
        return indegree[v];
    }

    /**
     * Returns all directed edges in this edge-weighted digraph.
     * To iterate over the edges in this edge-weighted digraph, use foreach notation:
     * {@code for (DirectedEdge e : G.edges())}.
     *
     * @return all edges in this edge-weighted digraph, as an iterable
     */
    public Iterable<DirectedEdge> edges() {
        Bag<DirectedEdge> list = new Bag<DirectedEdge>();
        for (int v = 0; v < V; v++) {
            for (DirectedEdge e : adj(v)) {
                list.add(e);
            }
        }
        return list;
    }

    /**
     * Returns the upper bound of egde weights
     */
    public double maxCapacity(){
        Bag<DirectedEdge> list = (Bag<DirectedEdge>) edges();
        double U = 0; 
            for (DirectedEdge e : list) {
                if (e.weight()>U) {
                    U = e.weight();
                }
            }
        return U;
    }

    /**
     * Returns a string representation of this edge-weighted digraph.
     *
     * @return the number of vertices <em>V</em>, followed by the number of edges <em>E</em>,
     *         followed by the <em>V</em> adjacency lists of edges
     */
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Number of Vertices:" + V + ", " + "Number of Edges:"+ E + NEWLINE);
        for (int v = 0; v < V; v++) {
            s.append(v + ": ");
            for (DirectedEdge e : adj[v]) {
                s.append(e + "  ");
            }
            s.append(NEWLINE);
        }
        return s.toString();
    }

    //iterate over all pair of s-t vertices and call minCut method for each pair
    private void globalMinCut(){
        int V = this.V();
        double bestMinCutValue = Double.POSITIVE_INFINITY;
        int bestS = -1;
        int bestT = -1;
        int[] sink = new int[V];
        
        Bag<DirectedEdge>[] bestMinCutEdges = new Bag[V];
        double[] maxFlow = new double[V];

        for (int v = 0; v < V; v++){
            bestMinCutEdges[v] = new Bag<DirectedEdge>();
        }

        // initialize maxFlow of each vertex to zero
        for (int v = 0; v < V; v++){
            maxFlow[v]=Double.POSITIVE_INFINITY;
        }

        for (int s = 0; s < V; s++){
            double cutValue = Double.POSITIVE_INFINITY;
            for (int t = 0; t < V; t++){
                if (t != s) {
                    Bag<DirectedEdge> MinCutEdges = new Bag<DirectedEdge>();
                    cutValue = minCut(s, t, MinCutEdges);

                    // compute the best min cut, the t vertex & the min cut edges from s to any other vertex of graph
                    if (cutValue < maxFlow[s] && cutValue > 0){
                        maxFlow[s] = cutValue;
                        System.out.println("Max flow-min cut (from vertex " + s + " to " + t + "): "  + maxFlow[s]);
                        sink[s] = t;
                        bestMinCutEdges[s] = new Bag<DirectedEdge>(); //clear every time
                        for (DirectedEdge e : MinCutEdges){
                            bestMinCutEdges[s].add(e);
                        }
                    }
                }
            }
        }

        // compare to find the best min cut all over the graph
        for (int v=0; v<V; v++){
            if (maxFlow[v] < bestMinCutValue){
                bestMinCutValue = maxFlow[v];
                bestS = v;
                bestT = sink[v];
            }
        }

        // System.out.println("Max flow-min cut(global): " + bestMinCutValue);
        System.out.println("Min Cut edges(global):");
        for (DirectedEdge e : bestMinCutEdges[bestS]) {
                System.out.println(e.from() + " - " + e.to()); //print the min cut edges
        }
    }

    //computes the min cut edges of a digraph from s to t (Ford Fulkerson algorithm)
    private double minCut(int s,int t, Bag<DirectedEdge> MinCutEdges ){
        //create a residual graph using the copy of the original graph
        EdgeWeightedDigraph residualG = new EdgeWeightedDigraph(this);
        int V = residualG.V();
        int[] parent = new int[V];   // array to store path
        double maxFlow = 0;

        //BFS in residual graph
        while (bfs(residualG, s, t, parent)) {

            double pathFlow = Double.POSITIVE_INFINITY;
            for (int v = t; v != s; v = parent[v]){
                int u = parent[v];
                pathFlow = Math.min(pathFlow, getResidualCapacity(residualG, u, v));
            }

            for (int v = t; v != s; v = parent[v]){
                int u = parent[v];
                updateResidualCapacity(residualG, u, v, -pathFlow);
                updateResidualCapacity(residualG, v, u, pathFlow);
            }

            maxFlow = maxFlow + pathFlow;
        }

        boolean[] isReachable = new boolean[V];
        dfs(residualG, s, isReachable);
        
        for (int v = 0; v < V; v++) {
            for (DirectedEdge e : residualG.adj[v]) {
                if (isReachable[e.from()] && !isReachable[e.to()]) {
                    // System.out.println("Min cut edges:\n" + e.from() + " - " + e.to());
                    MinCutEdges.add(e);
                }
            }
        }
        // System.out.println("Min cut equals to:" + maxFlow);

        return maxFlow;
    }

    private void dfs(EdgeWeightedDigraph residualG, int s, boolean[] isReachable) {
        isReachable[s] = true;
        for (DirectedEdge e : residualG.adj(s)) {
            int to = e.to();
            if (e.weight() > 0 && !isReachable[to]) {
                dfs(residualG, to, isReachable);
            }
        }
    }

    private void updateResidualCapacity(EdgeWeightedDigraph residualG, int u, int v, double flow) {
        for (DirectedEdge e : residualG.adj(u)) {
            if (e.to() == v) {
                e.setWeight(e.weight() + flow);
                return;
            }
        }
        // If edge doesn't exist, add a new edge with the flow
        residualG.addEdge(new DirectedEdge(u, v, flow));
    }

    private double getResidualCapacity(EdgeWeightedDigraph residualG, int u, int v) {
        for (DirectedEdge e : residualG.adj(u)) {
            if (e.to() == v) {
                return e.weight();
            }
        }
        return 0;
    }

    private boolean bfs(EdgeWeightedDigraph residualG, int s, int t, int[] parent) {
        int V = residualG.V();
        boolean [] visited = new boolean[V];
        Queue<Integer> queue = new LinkedList<>();
        queue.add(s);
        visited[s] = true;
        parent[s] = -1;

        while (!queue.isEmpty()) {
            int u = queue.poll();

            for (DirectedEdge e : residualG.adj(u)) {
                int v = e.to();
                if (!visited[v] && e.weight() > 0) {
                    queue.add(v);
                    parent[v] = u;
                    visited[v] = true;
                }
            }
        }

        return visited[t];
    }

    // Rooted Sparsification Lemma Implementation
    private EdgeWeightedDigraph computeContractedG(int r, double U, int k){
        EdgeWeightedDigraph contractedG = new EdgeWeightedDigraph(this.V()); // Create contracted graph without edges
        int V = this.V();
        List<Integer> highInDegree = new ArrayList<>();
        // vertices with weighted in-degree >= (1+U)*k
        for (int v = 0; v < V; v++){
            if (v != r && this.weightedInDegree[v] >= (1 + U) * k){
                highInDegree.add(v);
            }
        }
        // initialization of tempEdges linkedlist
        Bag<DirectedEdge>[] tempEdges = new Bag[V];
        for (int v = 0; v < V; v++){
            tempEdges[v] = new Bag<DirectedEdge>();
        }

        System.out.println("high InDegree vertices:");
        for (Integer element : highInDegree){
            System.out.println(element);
        }
        // create contracted graph
        for (int v = 0; v < V; v++){
            for (DirectedEdge e : this.adj(v)){
                int from = e.from();
                int to = e.to();
                double weight = e.weight();

                if (highInDegree.contains(from)){
                    from = r;
                }
                if (highInDegree.contains(to)){
                    to = r;
                }
                DirectedEdge tempEdge = new DirectedEdge(from, to, weight);
                contractedG.addEdge(tempEdge);
            }
        }
        for (int v = 0; v < V; v++){
            for (DirectedEdge e : contractedG.adj(v)){
                tempEdges[v].add(e);
            } 
        }

        // If i want to check the temp's list elements before removing duplicates
        // System.out.println("Prin to remove");
        // for (int v = 0; v < V; v++){
        //     System.out.print(v + ": ");
        //     for (DirectedEdge e2 : tempEdges[v]){
        //         System.out.print(e2 + " ");
        //     }
        //     System.out.println("\n");
        // }
        
        // handle highInDegree vertices edges that have common e.to() with r by sum the weights of these edges
        for (int v = 0; v < V; v++){
            for (DirectedEdge e1 : contractedG.adj[v]){
                for (DirectedEdge e2 : tempEdges[v]){
                    if ((e1.to()) == (e2.to()) && e1.weight() == e2.weight()){
                        tempEdges[v].remove(e2);
                    }
                    else if ((e1.to()) == (e2.to()) && e1.weight() != e2.weight()){
                        contractedG.addEdge(new DirectedEdge(v, e1.to(), e1.weight() + e2.weight()));
                        contractedG.adj[v].remove(e1);
                        contractedG.adj[v].remove(e2);
                        tempEdges[v].remove(e2);
                    }
                }
            }
        }
        // If i want to check the temp's list elements after removing duplicates
        // System.out.println("Meta to remove");
        // for (int v = 0; v < V; v++){
        //     System.out.print(v + ": ");
        //     for (DirectedEdge e2 : uniqueEdges[v]){
        //         System.out.print(e2 + " ");
        //     }
        //     System.out.println("\n");
        // }
        
        // reverse so that adjacency list is in same order as original (asc) ???? it doesnt work-check again ????
        /*for (int v = 0; v < V; v++) {
            Stack<DirectedEdge> reverse = new Stack<DirectedEdge>();
            for (DirectedEdge e : contractedG.adj[v]) {
                reverse.push(e);
            }
            while (reverse.isEmpty()==false) {
                contractedG.adj[v].add(reverse.pop());
            }
        }*/
        return contractedG;
    }

    public void computeSCC(){
        TarjanSCC scc = new TarjanSCC(this); // create TarjanSCC object
        scc.findScc(this);
    }

    public static void main(String[] args) {
        In in = new In(args[0]);
        EdgeWeightedDigraph G = new EdgeWeightedDigraph(in);
        // Scanner input = new Scanner(System.in);
        // System.out.println("Choose the rooted vertex:");
        // int r = input.nextInt();
        // G.validateVertex(r);
        System.out.println("Original G:\n" + G.toString());
        EdgeWeightedDigraph contractedG = G.computeContractedG(6, G.maxCapacity(), 1);
        System.out.println("---------------------\n");
        System.out.println("Contracted G:\n" + contractedG.toString());
        // G.globalMinCut();
        // G.computeSCC();
    }

}