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
        FordFulkerson ff = new FordFulkerson(this.G, root);
        
        // compute min-cut for root to vertex t
        for (int t = 0; t < V; t++){    
            if (t != root && !G.contractedVertices.contains(t)) {
                ff.computeMaxFlow(t);
                double lamda = ff.getMaxFlow(t);
                Set<Integer> sink = ff.getMinCutSink(t);

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

        System.out.println("Rooted Connectivity method (Lemma 5) has min cut value: " + minCutValue + " and the sink component is: " + sinkOfMinCut);
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

        Set<Integer> rootComponent = new HashSet<>();

        for (int i = 0; i < scc.count(); i++) {
            Set<Integer> sccVertices = scc.getVerticesInSCC(i);
            if (sccVertices.contains(root)) {
                rootComponent = sccVertices;
            }
        }

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
            for (Integer element : rootComponent) {
                for (DirectedEdge e : G.adj(element)) {
                    if (sccVertices.contains(e.to())) {
                        sccCutValue += e.weight();
                    }
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
            minCutValue = minSCCSize;
        }

        Map<String, Object> finalResult = new HashMap<>();
        finalResult.put("lamda", minCutValue);
        finalResult.put("sink", sinkOfMinCut);

        System.out.println("Small sink components method (Lemma 8) has min cut value: " + minCutValue + "and the sink component is: " + sinkOfMinCut);
        return finalResult;
    }

    // iterate over sampled vertices in the graph to find the value of lamda (Lemma 7)
    public Map<String, Object> rootedConnectivityForSampledVertices(int root, int kLow, int kHigh, Set<Integer> sampledVertices){
        FordFulkerson ff = new FordFulkerson(G, root);
        
        Map<String, Object> finalResult = new HashMap<>();
        finalResult.put("lamda", Double.POSITIVE_INFINITY);
        finalResult.put("sink", new HashSet<Integer>());

        // compute min-cut for root to sampled vertex t
        for (int t : sampledVertices){
            ff.computeMaxFlow(t);
            double lamda = ff.getMaxFlow(t);
            Set<Integer> sink = ff.getMinCutSink(t);

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

        System.out.println("Sampling vertices method (Lemma 7): " + finalResult.get("lamda") + " and the sink component is: " + finalResult.get("sink"));
        return finalResult;
    }

    // public static void main(String[] args) {
    //     In in = new In("sample.txt");
    //     EdgeWeightedDigraph G = new EdgeWeightedDigraph(in);
    //     MinCut minCut = new MinCut(G, 0);
    //     Map<String,Object> result = minCut.maxFlowMinCut(G, 0, 4);
    //     System.out.println(result.get("lamda")); // make lamda static if i want to check it 
    // }
}
