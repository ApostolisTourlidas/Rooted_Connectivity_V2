package helpermethods;

import graphpackage.EdgeWeightedDigraph;
import graphpackage.DirectedEdge;

import java.util.ArrayList;
import java.util.List;

public class ContractedG {

    private EdgeWeightedDigraph G;
    
    //constructor
    public ContractedG(EdgeWeightedDigraph G){
       this.G = G;
    }
    
    // Rooted Sparsification Lemma Implementation
    public EdgeWeightedDigraph computeContractedG(int r, double U, int k, int flag){
        EdgeWeightedDigraph contractedG = new EdgeWeightedDigraph(G.V()); // Create contracted graph without edges
        int V = G.V();
        List<Integer> highInDegree = new ArrayList<>();
        // vertices with weighted in-degree >= (1+U)*k
        for (int v = 0; v < V; v++){
            if (v != r && G.weightedInDegree[v] >= (1 + U) * k){
                highInDegree.add(v);
            }
        }
        // initialization of tempEdges linkedlist
        Bag<DirectedEdge>[] tempEdges = new Bag[V];
        for (int v = 0; v < V; v++){
            tempEdges[v] = new Bag<DirectedEdge>();
        }

        System.out.println("high InDegree vertices:");
        for (Integer element : highInDegree){
            System.out.println(element);
        }
        // create contracted graph
        for (int v = 0; v < V; v++){
            for (DirectedEdge e : G.adj(v)){
                int from = e.from();
                int to = e.to();
                double weight = e.weight();

                if (highInDegree.contains(from)){
                    from = r;
                }
                if (highInDegree.contains(to)){
                    to = r;
                }
                DirectedEdge tempEdge = new DirectedEdge(from, to, weight);
                contractedG.addEdge(tempEdge);
            }
        }
        for (int v = 0; v < V; v++){
            for (DirectedEdge e : contractedG.adj(v)){
                tempEdges[v].add(e);
            } 
        }

        // If i want to check the temp's list elements before removing duplicates
        // System.out.println("Prin to remove");
        // for (int v = 0; v < V; v++){
        //     System.out.print(v + ": ");
        //     for (DirectedEdge e2 : tempEdges[v]){
        //         System.out.print(e2 + " ");
        //     }
        //     System.out.println("\n");
        // }
        
        // handle highInDegree vertices edges that have common e.to() with r by sum the weights of these edges
        for (int v = 0; v < V; v++){
            for (DirectedEdge e1 : contractedG.adj[v]){
                for (DirectedEdge e2 : tempEdges[v]){
                    if ((e1.to()) == (e2.to()) && e1.weight() == e2.weight()){
                        tempEdges[v].remove(e2);
                    }
                    else if ((e1.to()) == (e2.to()) && e1.weight() != e2.weight()){
                        contractedG.addEdge(new DirectedEdge(v, e1.to(), e1.weight() + e2.weight()));
                        contractedG.adj[v].remove(e1);
                        contractedG.adj[v].remove(e2);
                        tempEdges[v].remove(e2);
                    }
                }
            }
        }
        // If i want to check the temp's list elements after removing duplicates
        System.out.println("Meta to remove");
        for (int v = 0; v < V; v++){
            System.out.print(v + ": ");
            for (DirectedEdge e2 : contractedG.adj[v]){
                System.out.print(e2 + " ");
            }
            System.out.println("\n");
        }
        
        // validation flag about number of contracted graph's edges
        if (contractedG.E() < (1 + U) * V * k){
            flag = 1;
        }

        return contractedG;
    }
}
