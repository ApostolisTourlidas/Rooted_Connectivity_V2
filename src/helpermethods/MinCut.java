package helpermethods;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import graphpackage.DirectedEdge;
import graphpackage.EdgeWeightedDigraph;

public class MinCut{

    private EdgeWeightedDigraph G;

    //constructor
    public MinCut(EdgeWeightedDigraph G){
        this.G = G;  // this.G will be the contracted G for Theorem 1 as this one i use to my methods
    }

    // Iterate over vertices in the graph to find the value of lamda (Lemma 5)
    public Map<String, Object> rootedConnectivity(int root){
        System.out.println("\n---------- Running Lemma 5 ----------");
        int V = G.V();
        double minCutValue = Double.POSITIVE_INFINITY;
        double U = G.maxCapacity();
        Set<Integer> sinkOfMinCut = new HashSet<>();
        
        // compute min-cut for root to vertex t
        for (int t = 0; t < V; t++){    
            if (t != root && !G.contractedVertices.contains(t)) {
                Map<String, Object> result = maxFlowMinCut(G, root, t);
                double lamda = (double) result.get("lamda");
                Set<Integer> sink = (Set<Integer>) result.get("sink");

                // Lemma 5 verification
                // Check singletons components
                if (sink.size() == 1 && lamda < minCutValue && lamda >0){
                        minCutValue = lamda;
                        sinkOfMinCut = new HashSet<>(sink);
                        continue;
                }
                //sink size > 1 & λ < U * k
                if (lamda < U * sink.size() && lamda < minCutValue && lamda >0){
                    minCutValue = lamda;
                    sinkOfMinCut = new HashSet<>(sink);
                }
            }
        }
        Map<String, Object> finalResult = new HashMap<>();
        finalResult.put("lamda", minCutValue);
        finalResult.put("sink", sinkOfMinCut);

        System.out.println("Rooted Connectivity (Lemma 5) with min cut value: " + minCutValue + " and the sink component is: " + sinkOfMinCut + "\n");
        return finalResult;
    }

    // Iterate over SCCs in the contracted graph to find the value of lamda (Lemma 8)
    public Map<String, Object> rootedConnectivityForSCCs(int root, int k2, double U){
        System.out.println("\n---------- Running Lemma 8 ----------");
        GabowSCC scc = new GabowSCC(G);
        double minCutValue = Double.POSITIVE_INFINITY;
        Set<Integer> sinkOfMinCut = new HashSet<>();

        Set<Integer> smallestAvailableSCC = new HashSet<>();
        int minSCCSize = Integer.MAX_VALUE;

        // Iterate over SCCs
        for (int i = 0; i < scc.count(); i++) {
            Set<Integer> sccVertices = scc.getVerticesInSCC(i);
            // System.out.println("[DEBUG] SCC " + i + " has " + sccVertices.size() + " vertices: " + sccVertices);

            // Skip singleton SCCs || skip SCC that contains contracted vertices
            if (sccVertices.size() <= 1 || sccVertices.contains(root) ||G.contractedVertices.containsAll(sccVertices)) {
                // System.out.println("[DEBUG] Skipping SCC " + i + " because it's invalid.");
                continue;
            }

            // Compute λ(r, T) - edges from root to SCC
            double sccCutValue = 0.0;
            for (DirectedEdge e : G.adj(root)) {
                if (sccVertices.contains(e.to())) {
                    sccCutValue += e.weight();
                }
            }  
            System.out.println("[DEBUG] SCC with id " + i + " | Size: " + sccVertices.size() + " | Cut Value: " + sccCutValue);

            // Lemma 8 verification & update min cut if SCC cut value is smaller
            if (sccVertices.size() <= k2 && sccCutValue < k2 * U && sccCutValue < minCutValue) {
                minCutValue = sccCutValue;
                sinkOfMinCut = new HashSet<>(sccVertices);
            }

            // If does not exists SCC that satisfy Lemma's 8 conditions - find smallest available SCC
            if (sccVertices.size() < minSCCSize) {
                minSCCSize = sccVertices.size();
                smallestAvailableSCC = new HashSet<>(sccVertices);
            }
        }

        // If finally sinkOfMinCut is empty then choose the smallest available SCC 
        if (sinkOfMinCut.isEmpty() && !smallestAvailableSCC.isEmpty()) {
            System.out.println("[WARNING] No valid SCC found with k2! Using the smallest available SCC.");
            sinkOfMinCut = smallestAvailableSCC;

            minCutValue = 0.0;
            for (DirectedEdge e : G.adj(root)) {
                if (sinkOfMinCut.contains(e.to())) {
                    minCutValue += e.weight();
                }
            }

        }
        if (sinkOfMinCut.isEmpty()) {
            System.out.println("[WARNING] No valid SCC available.");
            return Map.of("lamda", Double.POSITIVE_INFINITY, "sink", Set.of());
        }

        Map<String, Object> finalResult = new HashMap<>();
        finalResult.put("lamda", minCutValue);
        finalResult.put("sink", sinkOfMinCut);

        System.out.println("Final Minimum r-Cut Value (Lemma 8): " + minCutValue);
        System.out.println("Final Sink Component of minimum cut (Lemma 8): " + sinkOfMinCut + "\n");

        return finalResult;
    }

    // iterate over sampled vertices in the graph to find the value of lamda (Lemma 7)
    public Map<String, Object> rootedConnectivityForSampledVertices(int root, int kLow, int kHigh, Set<Integer> sampledVertices){
        System.out.println("\n---------- Running Lemma 7 ----------");
        Map<String, Object> finalResult = new HashMap<>();
        finalResult.put("lamda", Double.POSITIVE_INFINITY);
        finalResult.put("sink", new HashSet<Integer>());

        // compute min-cut for root to sampled vertex t
        for (int t : sampledVertices){
            Map<String, Object> result = maxFlowMinCut(G, root, t);
            double lamda = (double) result.get("lamda");
            Set<Integer> sink = (Set<Integer>) result.get("sink");

            if (sink.size() == 1 && lamda < (double) finalResult.get("lamda") && lamda >0) {
                finalResult.put("lamda", lamda);
                finalResult.put("sink", sink);
                continue;
            }

            // Lemma 7 verification
            if (sink.size() >= kLow && sink.size() <= kHigh && lamda < (double) finalResult.get("lamda") && lamda >0){
                finalResult.put("lamda", lamda);
                finalResult.put("sink", sink);
            }
        }

        System.out.println("Final Minimum r-Cut Value (Lemma 7): " + finalResult.get("lamda") + "and sink component is: " + finalResult.get("sink"));
        return finalResult;
    }

    //computes the min cut edges of a digraph from s to t (Ford Fulkerson algorithm)
    public Map<String,Object> maxFlowMinCut(EdgeWeightedDigraph G, int s,int t){        
        Map<String,Object> result = new HashMap<>();
        FordFulkerson ff = new FordFulkerson(G, s, t);
        double lamda = ff.getMaxFlow();
        Set<Integer> sink = ff.getMinCutSink();
        
        // System.out.println("[DEBUG] Computed min-cut lamda: " + lamda + " | Sink: " + sink);
        result.put("lamda", lamda);
        result.put("sink", sink);
        return result;
    }

    // public static void main(String[] args) {
    //     In in = new In("sample.txt");
    //     EdgeWeightedDigraph G = new EdgeWeightedDigraph(in);
    //     MinCut minCut = new MinCut(G, 0);
    //     Map<String,Object> result = minCut.maxFlowMinCut(G, 0, 4);
    //     System.out.println(result.get("lamda")); // make lamda static if i want to check it 
    // }
}
