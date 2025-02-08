package helpermethods;

import java.util.HashSet;
import java.util.Set;

import graphpackage.DirectedEdge;
import graphpackage.EdgeWeightedDigraph;

public class MinCut{

    private EdgeWeightedDigraph G;
    private double lamda;
    private Set<Integer> sink; // initialize sink here so i can get the vertices of sink inside the maxFlowMinCut
    private int flag;
    
    //constructor
    public MinCut(EdgeWeightedDigraph G, int flag){
        this.G = G;
        this.lamda = Double.POSITIVE_INFINITY;
        this.flag = flag;
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
                // Check singletons components
                if (this.sink.size() == 1 && this.lamda < minCutValue){
                        minCutValue = this.lamda;
                        continue;
                }
                //sink size > 1 & λ < U * k
                if (this.lamda < U * this.sink.size() && this.lamda < minCutValue){
                    minCutValue = this.lamda;
                    sinkOfMinCut = new HashSet<>(this.sink);
                }
            }
        }
        return minCutValue;
    }

    // Iterate over SCCs in the contracted graph to find the value of lamda (Lemma 8)
    public double rootedConnectivityForSCCs(int root, EdgeWeightedDigraph contractedG, int k2, double U){
        GabowSCC scc = new GabowSCC(contractedG);
        double minCutValue = Double.POSITIVE_INFINITY;
        Set<Integer> sinkOfMinCut = new HashSet<>();

        // Iterate over SCCs
        for (int i = 0; i < scc.count(); i++) {
            Set<Integer> sccVertices = scc.getVerticesInSCC(i);

            // Skip singleton SCCs
            if (sccVertices.size() == 1) {
                continue;
            }

            // Compute λ(r, T) - edges from root to SCC
            double sccCutValue = 0.0;
            for (DirectedEdge e : contractedG.adj(root)) {
                if (sccVertices.contains(e.to())) {
                    sccCutValue += e.weight();
                }
            }

            // Lemma 8 verification & update min cut if SCC cut value is smaller
            if (sccVertices.size() <= k2 && sccCutValue < k2 * U && sccCutValue < minCutValue) {
                minCutValue = sccCutValue;
                sinkOfMinCut = new HashSet<>(sccVertices);
            }
        }

        System.out.println("Final Minimum r-Cut Value: " + minCutValue);
        System.out.println("Final Sink Component of minimum cut: " + sinkOfMinCut);

        return minCutValue;
    }

    // iterate over sampled vertices in the graph to find the value of lamda (Lemma 7)
    public double rootedConnectivityForSampledVertices(int root, int kLow, int kHigh, Set<Integer> sampledVertices){
        double minCutValue = Double.POSITIVE_INFINITY;
        Set<Integer> minCutSink = new HashSet<>();
        double singletonValue = Double.POSITIVE_INFINITY;
        Set<Integer> singletonSink = new HashSet<>();
        double sampledValue = Double.POSITIVE_INFINITY;
        Set<Integer> sampledSink = new HashSet<>();

        // compute min-cut for root to sampled vertex t
        for (int t : sampledVertices){
            this.sink = new HashSet<>();
            maxFlowMinCut(G, root, t);
            double currentValue = this.lamda;

            if (this.sink.size() == 1 && currentValue < singletonValue) {
                singletonValue = currentValue;
                singletonSink = new HashSet<>(this.sink);
            }

            // Lemma 7 verification
            if (this.sink.size() >= kLow && this.sink.size() <= kHigh){
                if (currentValue < sampledValue){
                    sampledValue = currentValue;
                    sampledSink = new HashSet<>(this.sink);
                }
            }
        }

        // compare singleton and sampled vertices min cut value
        if (singletonValue < sampledValue){
            minCutValue = singletonValue;
            minCutSink = new HashSet<>(singletonSink);
        }else{
            minCutValue = sampledValue;
            minCutSink = new HashSet<>(sampledSink);
        }

        return minCutValue;
    }

    //computes the min cut edges of a digraph from s to t (Ford Fulkerson algorithm)
    private void maxFlowMinCut(EdgeWeightedDigraph G, int s,int t){
        if (flag == 0) { //Push-Relabel algorithm
            PushRelabelMaxFlow pr = new PushRelabelMaxFlow(G.V());
            for (int v = 0; v < G.V(); v++) {
                for (DirectedEdge e : G.adj(v)) {
                    pr.addEdge(e.from(), e.to(), e.weight());
                }
            }
            this.lamda = pr.maxFlow(s, t);
            this.sink = pr.getMinCut(s);
        }else { // Ford-Fulkerson algorithm
            FordFulkerson ff = new FordFulkerson(G, s, t);
            this.lamda = ff.getMaxFlow();
            this.sink = ff.getMinCutSink();
        }
    }

    // public static void main(String[] args) {
    //     In in = new In("sample.txt");
    //     EdgeWeightedDigraph G = new EdgeWeightedDigraph(in);
    //     MinCut minCut = new MinCut(G, 0);
    //     minCut.maxFlowMinCut(G, 0, 4);
    //     System.out.println(lamda); // make lamda static if i want to check it 
    // }
}
