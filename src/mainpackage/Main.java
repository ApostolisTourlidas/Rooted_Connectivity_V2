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
        // int k = (int) Math.sqrt(G.V());  // isws na to valoume riza n tha doume         

        // User's input and validation for root and k
        Scanner input = new Scanner(System.in);
        System.out.println("Choose a root vertex:");
        int root = input.nextInt();
        try{
            G.validateVertex(root);
        }catch (IllegalArgumentException e){
            System.out.println(e.getMessage());
            return;
        }

        System.out.println("Enter k value:");
        int k = input.nextInt();
        while (k < 0 || k > G.V()){
            System.out.println("k must be between 0 and " + G.V() + ". Enter k again: ");
            k = input.nextInt();
        } 

        double edgeConnectivity = Theorem1(G, root, k);
        System.out.println("The minimal r-cut value is: " + edgeConnectivity);
    }

    private static double Theorem1(EdgeWeightedDigraph G, int root, int k) {
        double U = G.maxCapacity();
        ContractedG cg = new ContractedG(G);
        EdgeWeightedDigraph contractedG = cg.computeContractedG(root, U, k);

        MinCut minCut = new MinCut(contractedG);
        double singletonMinCutValue = minCut.rootedConnectivity(root);  // min cut for singletons

        // min cut for small sink components - at most l vertices
        double smallSinkMinCutValue = minCut.rootedConnectivityForSCCs(contractedG);
        
        // min cut for sampled sink components
        double sinkSize = G.V()/k;
        Set<Integer> sampledVertices = cg.sampleVertexGenerator(root, sinkSize, contractedG);
        double sampledSinkMinCutValue = minCut.rootedConnectivityForSampledVertices(root, sampledVertices);

        // return the smallest min cut
        return Math.min(singletonMinCutValue, Math.min(smallSinkMinCutValue, sampledSinkMinCutValue));
    }
}
