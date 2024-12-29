package mainpackage;
import java.util.Scanner;

import graphpackage.EdgeWeightedDigraph;
import helpermethods.ContractedG;
import helpermethods.In;
import helpermethods.MinCut;
import helpermethods.TarjanSCC;


public class Main {
    /**
     * Unit tests the {@code EdgeWeightedDigraph} data type.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        In in = new In(args[0]);
        EdgeWeightedDigraph G = new EdgeWeightedDigraph(in);
        ContractedG cG;
        EdgeWeightedDigraph contractedG;
        int flag;
        
        Scanner input = new Scanner(System.in);
        System.out.println("Choose lemma: ");
        System.out.println("1.Rooted Sparcification Lemma (Lemma 4 & 5)");
        System.out.println("2.Fixed range of component sizes");
        System.out.println("3.Small sink components");
        int lemma = input.nextInt();
        switch (lemma) {
            case 1:
                cG = new ContractedG(G);
                MinCut lamda = new MinCut(G);
                flag = 0;
                contractedG = cG.computeContractedG(6, G.maxCapacity(), 2, flag);
                System.out.println("Contracted G:\n" + contractedG.toString());
                lamda.rootedConnectivity(6);
                break;
            case 2:
                cG = new ContractedG(G);
                flag = 0;
                contractedG = cG.computeContractedG(6, G.maxCapacity(), 2, flag);
                TarjanSCC scc = new TarjanSCC(contractedG);
                scc.printSCC(2);
                break;
            case 3:
                
                break;
            default:
                break;
        }
    }
}
