package mainpackage;
import java.util.Scanner;

import graphpackage.EdgeWeightedDigraph;
import helpermethods.ContractedG;
import helpermethods.In;


public class Main {
    /**
     * Unit tests the {@code EdgeWeightedDigraph} data type.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        In in = new In(args[0]);
        EdgeWeightedDigraph G = new EdgeWeightedDigraph(in);
        ContractedG cG = new ContractedG(G);
        // Scanner input = new Scanner(System.in);
        // System.out.println("Choose the rooted vertex:");
        System.out.println("Original G:\n" + G.toString());
        EdgeWeightedDigraph contractedG = cG.computeContractedG(6, G.maxCapacity(), 1);
        System.out.println("---------------------\n");
        System.out.println("Contracted G:\n" + contractedG.toString());
        // G.globalMinCut();
        // G.computeSCC();
    }
}
