package mainpackage;
import java.util.Scanner;
import java.util.Set;

import graphpackage.EdgeWeightedDigraph;
import helpermethods.ContractedG;
import helpermethods.In;
import helpermethods.MinCut;

public class Main {
    /**
     * Unit tests the {@code EdgeWeightedDigraph} data type.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        // load graph from file
        In in = new In("tinySCC.txt");
        EdgeWeightedDigraph G = new EdgeWeightedDigraph(in);
        int root, k1, k2;
        
        // int k = (int) Math.sqrt(G.V());  // isws na to valoume riza n tha doume         

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

        long startTime = System.nanoTime();
        double edgeConnectivity = Theorem1(G, root, k1, k2);
        long endTime = System.nanoTime();

        System.out.println("The minimal r-cut value is: " + edgeConnectivity);
        System.out.println("Execution time: %.3f ms\n"  + (endTime - startTime) / 1e6);
    }

    private static double Theorem1(EdgeWeightedDigraph G, int root, int k1, int k2) {
        int usePushRelabel = 0; // 0 for Push-Relabel - 1 for Ford-Fulkerson
        double U = G.maxCapacity();
        int sinkSize = (int) G.V() / k1;
        ContractedG cg = new ContractedG(G);
        EdgeWeightedDigraph contractedG = cg.computeContractedG(root, U, k2);
        MinCut minCut = new MinCut(contractedG, usePushRelabel);
        double singletonMinCutValue, smallSinkMinCutValue, sampledMinCutValue;
        
        // min cut for singletons - Lemma 5
        singletonMinCutValue = minCut.rootedConnectivity(root);  

        // min cut for small sink components - at most l vertices - Lemma 8
        smallSinkMinCutValue = minCut.rootedConnectivityForSCCs(root, contractedG, k2, U);

        // Skip Lemma 7 in case i have found the minimum cut which is 1
        if (singletonMinCutValue == 1 || smallSinkMinCutValue == 1) { 
            return 1;  
        }
        
        // min cut for sampled sink components - Lemma 7
        if (contractedG.V() < k1) {
            System.out.println("Warning: Contracted graph has less than k1 vertices."); 
            sampledMinCutValue = Double.POSITIVE_INFINITY;
        }else{
            sampledMinCutValue = Double.POSITIVE_INFINITY;
            int logk1 = (int) (Math.log(k1) / Math.log(2));
            int logk2 = (int) (Math.log(k2) / Math.log(2));

            for (int i = logk1; i <= logk2; i++) {
                int kLow = (int) Math.pow(2, i);
                int kHigh = (int) Math.pow(2, i+1);
                Set<Integer> sampledVertices = cg.sampleVertexGenerator(root, sinkSize, contractedG);
                double currentValue = minCut.rootedConnectivityForSampledVertices(root, kLow, kHigh, sampledVertices);
                sampledMinCutValue = Math.min(sampledMinCutValue, currentValue);
            }
        }

        // return the smallest min cut
        return Math.min(singletonMinCutValue, Math.min(smallSinkMinCutValue, sampledMinCutValue));
    }
}
