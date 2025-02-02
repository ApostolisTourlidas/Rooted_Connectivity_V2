package helpermethods;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import graphpackage.DirectedEdge;
import graphpackage.EdgeWeightedDigraph;

public class MinCut{

    private EdgeWeightedDigraph G;
    private double lamda;
    private Set<Integer> sink;
    
    //constructor
    public MinCut(EdgeWeightedDigraph G){
        this.G = G;
        this.lamda = Double.POSITIVE_INFINITY;
    }

    // Iterate over vertices in the graph to find the value of lamda (Lemma 5)
    public double rootedConnectivity(int root){
        int V = G.V();
        double minCutValue = Double.POSITIVE_INFINITY;
        double U = G.maxCapacity();
        Set<Integer> sinkOfMinCut = new HashSet<>();

        // compute min-cut for root to vertex t
        for (int t = 0; t < V; t++){
            
            if (t != root) {
                this.sink = new HashSet<>();
                maxFlowMinCut(G, root, t);

                // Lemma 5 verification
                if (this.sink.size() == 1){
                    if (this.lamda < minCutValue){
                        minCutValue = this.lamda;
                        sinkOfMinCut = new HashSet<>(this.sink);
                    }
                }else { //sink size > 1
                    if (this.lamda < U * this.sink.size()){
                        minCutValue = this.lamda;
                        sinkOfMinCut = new HashSet<>(this.sink);
                    }
                }
            }
        }
        return minCutValue;
    }

    // Iterate over SCCs in the contracted graph to find the value of lamda (Lemma 8)
    public double rootedConnectivityForSCCs(EdgeWeightedDigraph contractedG){
        GabowSCC scc = new GabowSCC(contractedG);

        double minCutValue = Double.POSITIVE_INFINITY;
        Set<Integer> sink = new HashSet<>();

        // Iterate over SCCs
        for (int i = 0; i < scc.count(); i++) {
            Set<Integer> sccVertices = scc.getVerticesInSCC(i);

            // Skip singleton SCCs
            if (sccVertices.size() == 1) {
                continue;
            }

            // Compute λ(r, T) for the SCC by adding the weight of the incoming in T edges
            double sccCutValue = 0.0;
            for (int v : sccVertices){
                for (DirectedEdge e : contractedG.adj(v)) {
                    if (!sccVertices.contains(e.from()) && sccVertices.contains(e.to())) {
                        sccCutValue += e.weight();
                    }
                }
            }

            // Update minimum cut if SCC cut value is smaller
            if (sccCutValue < minCutValue) {
                minCutValue = sccCutValue;
                sink = new HashSet<>(sccVertices);
            }
        }

        System.out.println("Final Minimum r-Cut Value: " + minCutValue);
        System.out.println("Final Sink Component of minimum cut: " + sink);

        return minCutValue;
    }

    // iterate over sampled vertices in the graph to find the value of lamda (Lemma 7)
    public double rootedConnectivityForSampledVertices(int root, Set<Integer> sampledVertices){
        double minCutValue = Double.POSITIVE_INFINITY;

        // compute min-cut for root to sampled vertex t
        for (int t : sampledVertices){
            this.sink = new HashSet<>();
            maxFlowMinCut(G, root, t);
            double currentValue = this.lamda;
            System.out.println("Target: " + t);
            System.out.println("Min-Cut Value: " + this.lamda);
            System.out.println("Sink Component: " + this.sink);
            System.out.println("");
            minCutValue = Math.min(currentValue, minCutValue);
        }

        return minCutValue;
    }

    //computes the min cut edges of a digraph from s to t (Ford Fulkerson algorithm)
    private void maxFlowMinCut(EdgeWeightedDigraph G, int s,int t){
        //create a residual graph using the copy of the original graph
        EdgeWeightedDigraph residualG = new EdgeWeightedDigraph(G);
        int V = residualG.V();
        int[] parent = new int[V];   // array to store path
        double maxFlow = 0;

        //BFS in residual graph
        while (bfs(residualG, s, t, parent)) {

            double pathFlow = Double.POSITIVE_INFINITY;
            for (int to = t; to != s; to = parent[to]){
                int from = parent[to];
                pathFlow = Math.min(pathFlow, getResidualCapacity(residualG, from, to));
            }

            // Update residual capacities
            for (int to = t; to != s; to = parent[to]){
                int from = parent[to];
                updateResidualCapacity(residualG, from, to, -pathFlow);
                updateResidualCapacity(residualG, to, from, pathFlow);
            }

            maxFlow += pathFlow;
        }

        this.lamda = maxFlow;

        boolean[] isReachable = new boolean[V];
        dfs(residualG, s, isReachable);

        // add unreachable vertices in sink set
        for (int v = 0; v < residualG.V(); v++){
            if (!isReachable[v]){
                this.sink.add(v);
            }
        }
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
}
