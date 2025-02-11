package helpermethods;

import java.util.*;

import graphpackage.DirectedEdge;
import graphpackage.EdgeWeightedDigraph;

public class PushRelabelMaxFlow {
    private int V;
    private Bag<DirectedEdge>[] adj;
    private double[] excess;
    private int[] height;
    private EdgeWeightedDigraph G;
    Queue<Integer> activeNodes = new LinkedList<>();

    public PushRelabelMaxFlow(EdgeWeightedDigraph G) {
        this.G = G;
        this.V = G.V();
        adj = (Bag<DirectedEdge>[]) new Bag[V];
        for (int v = 0; v < V; v++){
            adj[v] = new Bag<DirectedEdge>();
        }
        excess = new double[V];
        height = new int[V];
    }

    public void addEdge(int u, int v, double weight) {
        if (u == v) return;  // Avoid self-loops
        DirectedEdge forward = new DirectedEdge(u, v, weight);
        DirectedEdge backward = new DirectedEdge(v, u, 0);
        forward.setReverseEdge(backward);
        backward.setReverseEdge(forward);
        adj[u].add(forward);
        adj[v].add(backward);
    }

    private void push(DirectedEdge e, int s, int t) {
        if (excess[e.from()] == 0 || e.residualCapacity() <= 0) return;
        double send = Math.min(excess[e.from()], e.residualCapacity());
        e.addFlow(send);
        excess[e.from()] -= send;
        excess[e.to()] += send;
        if (excess[e.to()] > 0 && e.to() != s && e.to() != t) {
            activeNodes.add(e.to());
        }        
    }

    private void relabel(int u) {
        int minHeight = Integer.MAX_VALUE;
        for (DirectedEdge e : adj[u]) {
            if (e.residualCapacity() > 0) {
                minHeight = Math.min(minHeight, height[e.to()]);
            }
        }
        if (minHeight < Integer.MAX_VALUE) {
            height[u] = minHeight + 1;
        }
    }

    private void discharge(int u, int s, int t) {
        while (excess[u] > 0) {
            for (DirectedEdge e : adj[u]) {
                if (e.residualCapacity() > 0 && height[u] == height[e.to()] + 1) {
                    push(e, s, t);
                    if (excess[u] == 0) return;
                }
            }
            if (excess[u] > 0) {
                relabel(u);
            }
        }
    }

    public double maxFlow(int s, int t) {
        height[s] = V;
        excess[s] = 0;
        for (DirectedEdge e : adj[s]) {
            double flow = e.residualCapacity();
            if (flow > 0) {
                e.addFlow(flow);
                excess[s] -= flow;
                excess[e.to()] += flow;
            }
        }

        for (int i = 0; i < V; i++) {
            if (i != s && i != t && excess[i] > 0) {
                activeNodes.add(i);
            }
        }

        while (!activeNodes.isEmpty()) {
            int u = activeNodes.poll();
            int oldHeight = height[u];
            discharge(u, s, t);
            if (excess[u] > 0 && height[u] > oldHeight) {
                activeNodes.add(u);
            }
        }

        return excess[t];
    }

    public Set<Integer> getMinCut(int s) {
        Set<Integer> reachable = new HashSet<>();
        Set<Integer> minCutSink = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>(); //ok
        boolean[] visited = new boolean[V]; //ok
        queue.add(s); //ok
        visited[s] = true; //ok

        while (!queue.isEmpty()) {
            int u = queue.poll();
            reachable.add(u);
            
            for (DirectedEdge e : adj[u]) {
                if (e.residualCapacity() > 0 && !visited[e.to()]) {
                    visited[e.to()] = true;
                    queue.add(e.to());
                }
            }
        }

        for (int v = 0; v < V; v++) {
            if (!reachable.contains(v) && !G.contractedVertices.contains(v)) {
                minCutSink.add(v);
            }
        }
            
        System.out.println("Eimai mesa stin getMinCut - prin to return: " + minCutSink);
        return minCutSink;
    }
}
