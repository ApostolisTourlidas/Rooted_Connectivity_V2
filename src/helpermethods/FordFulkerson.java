package helpermethods;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import graphpackage.DirectedEdge;
import graphpackage.EdgeWeightedDigraph;

public class FordFulkerson {
    private EdgeWeightedDigraph residualG;
    private double maxFlow;
    private Set<Integer> minCutSink;

    public FordFulkerson(EdgeWeightedDigraph G, int s, int t) {
        //create a residual graph using the copy of the original graph
        this.residualG = new EdgeWeightedDigraph(G);
        int V = residualG.V();
        int[] parent = new int[V];
        this.maxFlow = 0;
        this. minCutSink = new HashSet<>();

        //BFS in residual graph
        while (bfs( s, t, parent)) {

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

        boolean[] isReachable = new boolean[V];
        dfs(residualG, s, isReachable);

        // add unreachable vertices in sink set
        for (int v = 0; v < residualG.V(); v++){
            if (!isReachable[v]){
                minCutSink.add(v);
            }
        }

    }

    private boolean bfs( int s, int t, int[] parent) {
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

    public double getMaxFlow() {
        return maxFlow;
    }

    public Set<Integer> getMinCutSink() {
        return minCutSink;
    }
    

}
