package mainpackage;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import graphpackage.DirectedEdge;
import graphpackage.EdgeWeightedDigraph;
import helpermethods.ContractedG;
import helpermethods.In;
import helpermethods.MinCut;
import helpermethods.MinCutBruteForce;
import helpermethods.MinCutResultBruteForce;

public class Main {
    /**
     * Unit tests the {@code EdgeWeightedDigraph} data type.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        // load graph from file
        In in = new In("1. n=8 - m=25.txt");
        EdgeWeightedDigraph G = new EdgeWeightedDigraph(in);
        boolean bruteForce = false;
        long startTime;
        long endTime;
        int root, k1, k2;

        int minRoot = 0;
        int minInDegree = Integer.MAX_VALUE;
        
        // int k = (int) Math.sqrt(G.V());  // isws na to valoume riza n tha doume
        System.out.println("Max capacity: " + G.maxCapacity());
        for (int i = 0; i < G.V(); i++) {
            System.out.println("Vertex " + i + " has indegree edges: " + G.indegree(i) + " with weighted indegree: " + G.weightedInDegree[i]);
            if (G.indegree(i) < minInDegree) {
                minInDegree = G.indegree(i);
                minRoot = i;
            }
        }
        System.out.println("Best min root: " + minRoot + " with in degree value: " + minInDegree);

        // User's input and validation for root and k
        Scanner input = new Scanner(System.in);
        while (true) {
            System.out.println("Choose a root vertex:");
            root = input.nextInt();
            try{
                G.validateVertex(root);
                break; //valid root - break condition
            }catch (IllegalArgumentException e){
                System.out.println(e.getMessage());
            }
        }
        
        System.out.println("Enter k1 value (minimum sink size) - (Suggested: " + Math.max(1,(int) Math.sqrt(G.V())) + "):");
        k1 = input.nextInt();
        while (k1 < 1 || k1 > G.V()){
            System.out.println("k1 must be between 0 and " + G.V() + ". Enter k1 again: ");
            k1 = input.nextInt();
        }
        
        System.out.println("Enter k2 value (maximum sink size)- (Suggested: " + (int) G.V() / 2 + "):");
        k2 = input.nextInt();
        while (k2 < k1 || k2 > G.V()){
            System.out.println("k2 must be between " + k1 + " and " + G.V() + ". Enter k2 again: ");
            k2 = input.nextInt();
        }
        input.close();

        // Brute Force
        if (bruteForce) {
            startTime = System.nanoTime();
            MinCutBruteForce minCutFinder = new MinCutBruteForce(G, root);
            MinCutResultBruteForce result = minCutFinder.findMinCut();
            endTime = System.nanoTime();
            System.out.println("Minimum Cut from " + root + ": " + result.getMinCutValue() + " and execution time is: " + (endTime-startTime) / 1e6 + "ms");
            System.out.println("Edges in the Minimum Cut:");
            for (DirectedEdge edge : result.getMinCutEdges()) {
                System.out.println(edge);
            }
        }

        startTime = System.nanoTime();
        Map<String, Object> edgeConnectivity = Theorem1(G, root, k1, k2);
        endTime = System.nanoTime();

        System.out.println("The minimal r-cut value after Theorem 1 execution is: " + edgeConnectivity.get("lamda") + " and sink component: " + edgeConnectivity.get("sink"));
        System.out.println("Execution time of Theorem 1: %.3f "  + (endTime - startTime) / 1e6 + "ms");
    }

    private static Map<String, Object> Theorem1(EdgeWeightedDigraph G, int root, int k1, int k2) {
        double U = G.maxCapacity();
        int sinkSize = (int) G.V() / k1;

        // timer for methods
        long startTime;
        long endTime;

        // contraction of original grapg G
        ContractedG cg = new ContractedG(G);
        EdgeWeightedDigraph contractedG = cg.computeContractedG(root, U, k2);        

        // initialize minCut object with contracted Graph 
        MinCut minCut = new MinCut(contractedG);
        double singletonMinCutValue, smallSinkMinCutValue, sampledValue;
        
        // ------------- min cut for singletons - Lemma 5 -------------
        startTime = System.nanoTime();
        Map<String, Object> singleton = minCut.rootedConnectivity(root);
        endTime = System.nanoTime();
        System.out.println("Execution time of Lemma 5: "  + (endTime - startTime) / 1e6 + "ms");
        singletonMinCutValue = (double) singleton.get("lamda");  

        // ------------- min cut for small sink components - at most l vertices - Lemma 8 -------------
        startTime = System.nanoTime();
        Map<String, Object> smallSink = minCut.rootedConnectivityForSCCs(root, k2, U);
        endTime = System.nanoTime();
        System.out.println("Execution time of Lemma 8: "  + (endTime - startTime) / 1e6 + "ms");
        smallSinkMinCutValue = (double) smallSink.get("lamda");

        // Skip Lemma 7 in case i have found the minimum cut which is 1
        if (singletonMinCutValue == 1 && singletonMinCutValue <= smallSinkMinCutValue) { 
            return singleton;  
        }else if (smallSinkMinCutValue == 1 && smallSinkMinCutValue <= singletonMinCutValue) {
            return smallSink;
        }
        
        // ------------- min cut for sampled sink components - Lemma 7 -------------
        System.out.println("\n---------- Running Lemma 7 ----------");
        Map<String, Object> finalSampledMinCut = new HashMap<>();
        if (contractedG.V() < k1) {
            System.out.println("Warning: Contracted graph has less than k1 vertices."); 
            sampledValue = Double.POSITIVE_INFINITY;
        }else{
            sampledValue = Double.POSITIVE_INFINITY;
            int logk1 = (int) (Math.log(k1) / Math.log(2));
            int logk2 = (int) (Math.log(k2) / Math.log(2));

            startTime = System.nanoTime();
            for (int i = logk1; i <= logk2; i++) {
                int kLow = (int) Math.pow(2, i);
                int kHigh = (int) Math.pow(2, i+1);
                Set<Integer> sampledVertices = cg.sampleVertexGenerator(root, sinkSize, contractedG);
                Map<String, Object> sampledSink = minCut.rootedConnectivityForSampledVertices(root, kLow, kHigh, sampledVertices);
                double currentValue = (double) sampledSink.get("lamda");
                if (currentValue < sampledValue){
                    sampledValue = currentValue;
                    finalSampledMinCut = sampledSink;
                }
            }
            endTime = System.nanoTime();
            System.out.println("\nExecution time of Lemma 7: "  + (endTime - startTime) / 1e6 + "ms");
        }
        double sampledMinCutValue = (double) finalSampledMinCut.get("lamda");

        if (singletonMinCutValue <= smallSinkMinCutValue && singletonMinCutValue <= sampledMinCutValue) {
            return singleton;
        }else if (smallSinkMinCutValue <= sampledMinCutValue) {
            return smallSink;
        }else {
            return finalSampledMinCut;
        }
    }
}
