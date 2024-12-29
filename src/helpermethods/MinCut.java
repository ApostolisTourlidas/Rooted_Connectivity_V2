package helpermethods;

import java.util.ArrayList;
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

    // iterate over vertices in the graph to find the value of lamda 
    public void rootedConnectivity(int root){
        int V = this.G.V();
        double lamda = Double.POSITIVE_INFINITY;

        // compute min-cut for root to vertex t
        for (int t = 0; t < V; t++){
            if (t != root) {
                lamda = maxFlowMinCut(root, t);
            }
        }

        // // compare to find the best min cut all over the graph
        // for (int v=0; v<V; v++){
        //     if (maxFlow[v] < bestMinCutValue){
        //         bestMinCutValue = maxFlow[v];
        //         bestS = v;
        //         bestT = sink[v];
        //     }
        // }

        // // System.out.println("Max flow-min cut(global): " + bestMinCutValue);
        // System.out.println("Min Cut edges(global):");
        // for (DirectedEdge e : bestMinCutEdges[bestS]) {
        //         System.out.println(e.from() + " - " + e.to()); //print the min cut edges
        // }
    }

    //computes the min cut edges of a digraph from s to t (Ford Fulkerson algorithm)
    private double maxFlowMinCut(int s,int t){
        //create a residual graph using the copy of the original graph
        EdgeWeightedDigraph residualG = new EdgeWeightedDigraph(this.G);
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

            for (int to = t; to != s; to = parent[to]){
                int from = parent[to];
                updateResidualCapacity(residualG, from, to, -pathFlow);
                updateResidualCapacity(residualG, to, from, pathFlow);
            }

            maxFlow += pathFlow;
        }

        boolean[] isReachable = new boolean[V];
        dfs(residualG, s, isReachable);
        
        // Find out the vertices that belongs to sink component
        ArrayList<Integer> sinkT = new ArrayList<>();
        for (int v = 0; v < V; v++){
            if (!isReachable[v]){
                sinkT.add(v);
            }
        }

        int k = sinkT.size(); //k is the number of vertices in sink component

        // Lemma 5 verification
        if (k > 1){
            if (maxFlow < residualG.maxCapacity() * k){
                System.out.println("Lemma 5 holds, for root: " + s + " and for target: " + t);
            }else{
                System.out.println("Lemma 5 is not valid, for root: " + s + " and for target: " + t);
            }
        }else{
            System.out.println("sink component is a singleton, for root: " + s + " and for target: " + t);
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
