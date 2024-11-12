package helpermethods;

import java.util.LinkedList;
import java.util.Queue;

import graphpackage.DirectedEdge;
import graphpackage.EdgeWeightedDigraph;

public class MinCut {

    private EdgeWeightedDigraph G;
    
    //constructor
    public MinCut(EdgeWeightedDigraph G){
        this.G = G;
    }

    //iterate over all pair of s-t vertices and call minCut method for each pair
    private void globalMinCut(){
        int V = this.G.V();
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
                    cutValue = maxFlowMinCut(s, t, MinCutEdges);

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
    private double maxFlowMinCut(int s,int t, Bag<DirectedEdge> MinCutEdges ){
        //create a residual graph using the copy of the original graph
        EdgeWeightedDigraph residualG = new EdgeWeightedDigraph(this.G);
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
}
