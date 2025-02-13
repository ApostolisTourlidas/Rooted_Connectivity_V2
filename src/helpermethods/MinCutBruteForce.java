package helpermethods;

import graphpackage.DirectedEdge;
import graphpackage.EdgeWeightedDigraph;

import java.util.*;

public class MinCutBruteForce {
    
    private EdgeWeightedDigraph graph;
    private int V;
    private int root;
    private MinCutBruteForce result;
    private Set<DirectedEdge> lastComputedCutEdges = new HashSet<>();
    
    public MinCutBruteForce(EdgeWeightedDigraph graph, int root) {
        this.graph = graph;
        this.V = graph.V();
        this.root = root;
    }

    private boolean isConnected(boolean[] set) {
        // BFS για να ελέγξουμε αν το root παραμένει συνδεδεμένο με άλλους κόμβους του συνόλου
        Queue<Integer> queue = new LinkedList<>();
        boolean[] visited = new boolean[V];

        queue.add(root);
        visited[root] = true;

        while (!queue.isEmpty()) {
            int v = queue.poll();
            for (DirectedEdge edge : graph.adj(v)) {
                int neighbor = edge.to();
                if (!visited[neighbor] && set[neighbor]) {
                    visited[neighbor] = true;
                    queue.add(neighbor);
                }
            }
        }

        // Ελέγχουμε αν όλοι οι κόμβοι στο root-side είναι ακόμα συνδεδεμένοι
        for (int i = 0; i < V; i++) {
            if (set[i] && !visited[i]) {
                return false;
            }
        }
        return true;
    }

    private Set<Integer> findReachableNodes(Set<DirectedEdge> cutEdges) {
        Set<Integer> reachable = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        boolean[] visited = new boolean[V];

        queue.add(root);
        visited[root] = true;
        reachable.add(root);

        while (!queue.isEmpty()) {
            int node = queue.poll();
            for (DirectedEdge edge : graph.adj(node)) {
                int neighbor = edge.to();
                if (!visited[neighbor] && !cutEdges.contains(edge)) {
                    visited[neighbor] = true;
                    reachable.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        return reachable;
    }

    public MinCutResultBruteForce findMinCut() {
        double minCutValue = Double.MAX_VALUE;
        List<DirectedEdge> minCutEdges = new ArrayList<>();
        Set<Integer> minCutRootSide = new HashSet<>();
        Set<Integer> minCutCutSide = new HashSet<>();

        // Δοκιμάζουμε όλα τα πιθανά χωρίσματα κόμβων που περιέχουν τον root
        for (int mask = 1; mask < (1 << V); mask++) {
            if ((mask & (1 << root)) == 0) continue; // Αν το root δεν ανήκει στο σύνολο, αγνοούμε αυτή την περίπτωση

            boolean[] set = new boolean[V];
            for (int i = 0; i < V; i++) {
                if ((mask & (1 << i)) != 0) {
                    set[i] = true; // Ο κόμβος i ανήκει στο 1ο σύνολο (που περιέχει τον root)
                }
            }

            // Ελέγχουμε αν το root-side παραμένει συνδεδεμένο
            if (!isConnected(set)) {
                double cutValue = 0;
                List<DirectedEdge> cutEdges = new ArrayList<>();

                // Προσωρινό σύνολο των κομμένων ακμών
                Set<DirectedEdge> tempCutEdges = new HashSet<>();

                // Υπολογίζουμε το συνολικό βάρος των κομμένων ακμών
                for (DirectedEdge edge : graph.edges()) {
                    int u = edge.from();
                    int v = edge.to();
                    if (set[u] && !set[v]) { // Αν u είναι στο 1ο σύνολο και v στο 2ο
                        cutValue += edge.weight();
                        tempCutEdges.add(edge);
                    }
                }

                lastComputedCutEdges = new HashSet<>(tempCutEdges);

                // Βρίσκουμε ποιοι κόμβοι είναι πραγματικά συνδεδεμένοι με το root
                Set<Integer> reachableFromRoot = findReachableNodes(tempCutEdges);

                // Φιλτράρουμε τις ακμές που πραγματικά αποτελούν το min cut
                List<DirectedEdge> trueMinCutEdges = new ArrayList<>();
                for (DirectedEdge edge : tempCutEdges) {
                    if (reachableFromRoot.contains(edge.from()) && !reachableFromRoot.contains(edge.to())) {
                        trueMinCutEdges.add(edge);
                    }
                }

                // Αν αυτό το min cut είναι μικρότερο, το αποθηκεύουμε
                if (cutValue < minCutValue) {
                    minCutValue = 0; // Resetting min cut value
                    for (DirectedEdge edge : trueMinCutEdges) {
                        minCutValue += edge.weight();
                    }
                    minCutEdges = trueMinCutEdges;
                    minCutRootSide = reachableFromRoot;

                    // Υπολογίζουμε το cut-side ως οι κόμβοι που ΔΕΝ είναι στο root-side
                    minCutCutSide.clear();
                    for (int i = 0; i < V; i++) {
                        if (!reachableFromRoot.contains(i)) {
                            minCutCutSide.add(i);
                        }
                    }
                }
            }
        }
        return new MinCutResultBruteForce(minCutValue, minCutEdges, minCutRootSide, minCutCutSide);
    }

    public Set<DirectedEdge> getTempCutEdges() {
        return lastComputedCutEdges;
    } 

    public static void main(String[] args) {
        In in = new In("4. n=84 - m=124.txt");
        EdgeWeightedDigraph G = new EdgeWeightedDigraph(in);
        int root = 0; // Θέτουμε το root κόμβο
        
        long startTime = System.nanoTime();
        MinCutBruteForce minCutFinder = new MinCutBruteForce(G, root);
        MinCutResultBruteForce result = minCutFinder.findMinCut();
        long endTime = System.nanoTime();

        System.out.println("Minimum Cut from " + root + ": " + result.getMinCutValue() + " and execution time is: " + (endTime-startTime) / 1e6 + "ms");
        System.out.println("Edges in the Minimum Cut:");
        for (DirectedEdge edge : result.getMinCutEdges()) {
            System.out.println(edge);
        }

        System.out.println("Nodes in the Root Side:");
        System.out.println(result.getMinCutRootSide());

        System.out.println("Nodes in the Cut Side:");
        System.out.println(result.getMinCutCutSide());

        // Debugging: Υπολογισμός συνολικού βάρους των ακμών στο min-cut
        double totalCutWeight = 0;
        for (DirectedEdge edge : result.getMinCutEdges()) {
            totalCutWeight += edge.weight();
        }
        System.out.println("Debug: Calculated Cut Weight = " + totalCutWeight);

        // Επιπλέον Debug Εκτυπώσεις
        System.out.println("Debug: Checking all cut edges before filtering:");
        for (DirectedEdge edge : minCutFinder.getTempCutEdges()) {
            System.out.println(edge);
        }

        System.out.println("Debug: Checking true min cut edges after filtering:");
        for (DirectedEdge edge : result.getMinCutEdges()) {
            System.out.println(edge);
        }
    }
}