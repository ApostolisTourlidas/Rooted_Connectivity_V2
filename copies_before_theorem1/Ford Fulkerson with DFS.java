package helpermethods;

import java.util.HashSet;
import java.util.Set;

import graphpackage.DirectedEdge;
import graphpackage.EdgeWeightedDigraph;

public class FordFulkerson {
    private EdgeWeightedDigraph residualG;
    private double maxFlow;
    private Set<Integer> minCutSink;

    public FordFulkerson(EdgeWeightedDigraph G, int s, int t) {
        this.residualG = new EdgeWeightedDigraph(G);
        int V = residualG.V();
        this.maxFlow = 0;
        this.minCutSink = new HashSet<>();

        // Debugging Print
        System.out.println("[DEBUG] Running Ford-Fulkerson (DFS) from " + s + " to " + t);

        // Run DFS-based augmenting path search
        double pathFlow;
        do {
            boolean[] visited = new boolean[V];
            pathFlow = dfs(s, t, visited, Double.POSITIVE_INFINITY);
            maxFlow += pathFlow;
        } while (pathFlow > 0);

        // Find min cut using DFS
        boolean[] isReachable = new boolean[V];
        findMinCutDFS(s, isReachable);

        for (int v = 0; v < residualG.V(); v++) {
            if (!isReachable[v]) {
                minCutSink.add(v);
            }
        }

        System.out.println("[DEBUG] Max Flow: " + maxFlow + " | Min-Cut Sink: " + minCutSink);
    }

    private double dfs(int u, int t, boolean[] visited, double flow) {
        if (u == t) return flow;
        visited[u] = true;

        for (DirectedEdge e : residualG.adj(u)) {
            int v = e.to();
            if (!visited[v] && e.weight() > 0) {
                double bottleneck = Math.min(flow, e.weight());
                double pushedFlow = dfs(v, t, visited, bottleneck);

                if (pushedFlow > 0) {
                    updateResidualCapacity(u, v, -pushedFlow);
                    updateResidualCapacity(v, u, pushedFlow);
                    return pushedFlow;
                }
            }
        }
        return 0;
    }

    private void findMinCutDFS(int s, boolean[] isReachable) {
        isReachable[s] = true;
        for (DirectedEdge e : residualG.adj(s)) {
            int to = e.to();
            if (e.weight() > 0 && !isReachable[to]) {
                findMinCutDFS(to, isReachable);
            }
        }
    }

    private void updateResidualCapacity(int u, int v, double flow) {
        for (DirectedEdge e : residualG.adj(u)) {
            if (e.to() == v) {
                e.setWeight(e.weight() + flow);
                return;
            }
        }
        residualG.addEdge(new DirectedEdge(u, v, flow));
    }

    public double getMaxFlow() {
        return maxFlow;
    }

    public Set<Integer> getMinCutSink() {
        return minCutSink;
    }
}
