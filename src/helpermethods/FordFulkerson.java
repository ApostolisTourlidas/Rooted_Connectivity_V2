package helpermethods;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import graphpackage.DirectedEdge;
import graphpackage.EdgeWeightedDigraph;

public class FordFulkerson {
    private EdgeWeightedDigraph residualG;
    private Map<Integer, Double> maxFlowMap;
    private Map<Integer, Set<Integer>> minCutMap;
    private int root;

    public FordFulkerson(EdgeWeightedDigraph G, int root) {
        this.residualG = new EdgeWeightedDigraph(G);
        this.maxFlowMap = new HashMap<>();
        this.minCutMap = new HashMap<>();
        this.root = root;
        residualG.contractedVertices = G.contractedVertices;
    }

    public void computeMaxFlow(int t) {
        int V = residualG.V();
        int[] parent = new int[V];
            
        EdgeWeightedDigraph tempGraph = new EdgeWeightedDigraph(residualG);
        double maxFlow = 0;
        
        while (bfs(tempGraph, root, t, parent)) {
            double pathFlow = Double.POSITIVE_INFINITY;
            for (int to = t; to != root; to = parent[to]){
                int from = parent[to];
                pathFlow = Math.min(pathFlow, getResidualCapacity(tempGraph, from, to));
            }
            
            for (int to = t; to != root; to = parent[to]){
                int from = parent[to];
                updateResidualCapacity(tempGraph, from, to, -pathFlow);
                updateResidualCapacity(tempGraph, to, from, pathFlow);
            }
            maxFlow += pathFlow;
        }
        
        maxFlowMap.put(t, maxFlow);
        
        Set<Integer> minCutSet = computeMinCut(tempGraph, root);
        minCutSet.removeAll(residualG.contractedVertices);
        minCutMap.put(t, minCutSet);
    }

    private boolean bfs(EdgeWeightedDigraph graph, int s, int t, int[] parent) {
        int V = graph.V();
        boolean[] visited = new boolean[V];
        LinkedList<Integer> queue = new LinkedList<>();
        queue.add(s);
        visited[s] = true;
        parent[s] = -1;

        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (DirectedEdge e : graph.adj(u)) {
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

    private Set<Integer> computeMinCut(EdgeWeightedDigraph graph, int s) {
        boolean[] isReachable = new boolean[graph.V()];
        dfs(graph, s, isReachable);
        
        Set<Integer> minCutSink = new HashSet<>();
        for (int v = 0; v < graph.V(); v++) {
            if (!isReachable[v]) {
                minCutSink.add(v);
            }
        }
        return minCutSink;
    }

    private void dfs(EdgeWeightedDigraph graph, int s, boolean[] isReachable) {
        isReachable[s] = true;
        for (DirectedEdge e : graph.adj(s)) {
            int to = e.to();
            if (e.weight() > 0 && !isReachable[to]) {
                dfs(graph, to, isReachable);
            }
        }
    }

    private void updateResidualCapacity(EdgeWeightedDigraph graph, int u, int v, double flow) {
        for (DirectedEdge e : graph.adj(u)) {
            if (e.to() == v) {
                e.setWeight(e.weight() + flow);
                return;
            }
        }
        graph.addEdge(new DirectedEdge(u, v, flow));
    }

    private double getResidualCapacity(EdgeWeightedDigraph graph, int u, int v) {
        for (DirectedEdge e : graph.adj(u)) {
            if (e.to() == v) {
                return e.weight();
            }
        }
        return 0;
    }

    public double getMaxFlow(int t) {
        return maxFlowMap.getOrDefault(t, 0.0);
    }

    public Set<Integer> getMinCutSink(int t) {
        return minCutMap.getOrDefault(t, new HashSet<>());
    }

 
    // public static void main(String[] args) {
    //     In in = new In("1. n=8 - m=25.txt");
    //     EdgeWeightedDigraph G = new EdgeWeightedDigraph(in);
    //     int root = 0; // Θέτουμε το root κόμβο
    //     double lamda = Double.POSITIVE_INFINITY;
    //     Set<Integer> sink = new HashSet<>();
    //     FordFulkerson ff = new FordFulkerson(G, root);
        
    //     long startTime = System.nanoTime();
    //     for (int v = 0; v < G.V(); v++) {
    //         if (v != root) {
    //             ff.computeMaxFlow(v);
    //             double currentValue = ff.getMaxFlow(v);
    //             if (currentValue <= lamda) {
    //                 lamda = currentValue;
    //                 sink = ff.getMinCutSink(v);
    //             }
    //         }
    //     }
    //     long endTime = System.nanoTime();
    //     System.out.println("Minimum Cut: " + lamda + " || Execution time is: " + (endTime-startTime) / 1e6 + "ms");
    //     System.out.println("Vertices in sink component: " + sink);
    // }
}