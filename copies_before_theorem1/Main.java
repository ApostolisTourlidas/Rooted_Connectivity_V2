package mainpackage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import graphpackage.EdgeWeightedDigraph;
import helpermethods.ContractedG;
import helpermethods.GabowSCC;
import helpermethods.In;
import helpermethods.MinCut;
import helpermethods.GabowSCC;


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

        ContractedG cG;
        EdgeWeightedDigraph contractedG;
        MinCut lamda;
        GabowSCC scc;
        int flag;
        int root;
        Integer k;

        // User's input
        Scanner input = new Scanner(System.in);
        System.out.println("Choose a root vertex:");

        







        System.out.println("1.Rooted Sparcification Lemma (Lemma 4 & 5)");
        System.out.println("2.Fixed range of component sizes (Lemma 7)");
        System.out.println("3.Small sink components (Lemma 8)");
        int lemma = input.nextInt();
        switch (lemma) {
            case 1:
                cG = new ContractedG(G);
                lamda = new MinCut(G);
                flag = 0;
                root = -1;
                k = -1;

                // validations for k & root
                while (k < 0) {
                    System.out.println("Please give k:");
                    k = input.nextInt();
                    try{
                        G.validateVertex(k);
                    }catch (IllegalArgumentException e){
                        System.out.println(e.getMessage());
                        k = -1;
                    }
                }
                while (root <= 0) {
                    System.out.println("Please choose a root:");
                    root = input.nextInt();
                    try{
                        G.validateVertex(root);
                    }catch (IllegalArgumentException e){
                        System.out.println(e.getMessage());
                        root = -1;
                    }
                }

                contractedG = cG.computeContractedG(root, G.maxCapacity(), k);
                System.out.println("Contracted G:\n" + contractedG.toString());
                lamda.rootedConnectivity(root);
                break;

            case 2:
                cG = new ContractedG(G);
                Set<Integer> sampledVertices = new HashSet<>();
                flag = 0;
                root = -1;
                int k1 = -1;
                int k2 = -1;
                
                // validations for k1, k2 & root
                while (k1 >= k2) {
                    System.out.println("Please give k1:");
                    k1 = input.nextInt();
                    try{
                        G.validateVertex(k1);
                    }catch (IllegalArgumentException e){
                        System.out.println(e.getMessage());
                        k1 = -1;
                    }
                    System.out.println("Please give k2:");
                    k2 = input.nextInt();
                    try{
                        G.validateVertex(k2);
                    }catch (IllegalArgumentException e){
                        System.out.println(e.getMessage());
                        k2 = -1;
                    }
                }
                while (root <= 0) {
                    System.out.println("Please choose a root:");
                    root = input.nextInt();
                    try{
                        G.validateVertex(root);
                    }catch (IllegalArgumentException e){
                        System.out.println(e.getMessage());
                        root = -1;
                    }
                }

                double sinkSize = G.V() / k1;
                contractedG = cG.computeContractedG(root, G.maxCapacity(), k2);
                sampledVertices = cG.sampleVertexGenerator(root, sinkSize, contractedG);

                lamda = new MinCut(contractedG);
                lamda.rootedConnectivityForSampledVertices(root, sampledVertices);
                
                // mallon den xreaizetai edw, na to ksanadw
                // scc = new GabowSCC(contractedG);
                // scc.printSCC(2);
                break;

            case 3:
                cG = new ContractedG(G);
                lamda = new MinCut(G);
                Map<String, Object> singleton = new HashMap<>();
                root = -1;
                k = -1;

                // validations for k & root
                while (k < 0) {
                    System.out.println("Please give k:");
                    k = input.nextInt();
                    try{
                        G.validateVertex(k);
                    }catch (IllegalArgumentException e){
                        System.out.println(e.getMessage());
                        k = -1;
                    }
                }
                while (root <= 0) {
                    System.out.println("Please choose a root:");
                    root = input.nextInt();
                    try{
                        G.validateVertex(root);
                    }catch (IllegalArgumentException e){
                        System.out.println(e.getMessage());
                        root = -1;
                    }
                }

                singleton = lamda.rootedConnectivity(root, k);                  // find min cut for singleton sinks and return a Map
                contractedG = cG.computeContractedG(root, G.maxCapacity(), k);  // contracted G for λ < Uk
                lamda.rootedConnectivityForSCCs(contractedG, singleton);        // min cut for SCCs
                break;
            default:
                break;
        }
    }
}
