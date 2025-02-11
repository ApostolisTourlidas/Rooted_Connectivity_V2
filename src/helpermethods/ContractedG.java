package helpermethods;

import graphpackage.EdgeWeightedDigraph;
import graphpackage.DirectedEdge;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContractedG {

    private EdgeWeightedDigraph G;
    public Set<Integer> highInDegree = new HashSet<>();
    
    //constructor
    public ContractedG(EdgeWeightedDigraph G){
       this.G = G;
    }
    
    // Rooted Sparsification Lemma Implementation
    public EdgeWeightedDigraph computeContractedG(int root, double U, int k2){
        EdgeWeightedDigraph contractedG = new EdgeWeightedDigraph(G.V()); // Create contracted graph without edges
        int V = G.V();

        // vertices with weighted in-degree >= (1+U)*k
        for (int v = 0; v < V; v++){
            if (v != root && G.weightedInDegree[v] >= (1 + U) * k2){
                highInDegree.add(v);
                contractedG.contractedVertices.add(v);
            }
        }

        System.out.println("--[DEBUG]-- High InDegree vertices:");
        for (Integer element : highInDegree){
            System.out.println(element);
        }

        Map<AbstractMap.SimpleEntry<Integer, Integer>, Double> uniqueEdges = new HashMap<>();

        for (int v = 0; v < V; v++) {
            for (DirectedEdge e : G.adj(v)) {
                int from = e.from();
                int to = e.to();
                double weight = e.weight();
        
                if (highInDegree.contains(from)) {
                    from = root;
                }
                if (highInDegree.contains(to)) {
                    to = root;
                }
                if (from != to) {
                    AbstractMap.SimpleEntry<Integer, Integer> edgeKey = new AbstractMap.SimpleEntry<>(from, to);
                    uniqueEdges.put(edgeKey, uniqueEdges.getOrDefault(edgeKey, 0.0) + weight);
                }
            }
        }
        
        // Add unique edges to contractedG
        for (Map.Entry<AbstractMap.SimpleEntry<Integer, Integer>, Double> entry : uniqueEdges.entrySet()) {
            int from = entry.getKey().getKey();
            int to = entry.getKey().getValue();
            double weight = entry.getValue();
            contractedG.addEdge(new DirectedEdge(from, to, weight));
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
        // if (contractedG.E() < (1 + U) * V * k){
        //     flag = 1;
        // }

        return contractedG;
    }

    public Set<Integer> sampleVertexGenerator(int root, int sinkSize, EdgeWeightedDigraph contractedG){
        List<Integer> nonContractedVertices = new ArrayList<>();

        // store non contracted vertices
        for (int v = 0; v < contractedG.V(); v++) {
            if (v != root && !highInDegree.contains(v)) {
                nonContractedVertices.add(v);
            }
        }
        // check that non contracted vertices are enough for sampling
        if (nonContractedVertices.size() < sinkSize) {
            System.out.println("Warning: Not enough available vertices to sample.");
            return new HashSet<>(nonContractedVertices); // Return all non contracted vertices instead
        }
        // Shuffle and select random samples
        Collections.shuffle(nonContractedVertices);
        return new HashSet<>(nonContractedVertices.subList(0, (int) sinkSize));
    }

    // public static void main(String[] args) {
    //     In in = new In("testContraction.txt");
    //     EdgeWeightedDigraph G = new EdgeWeightedDigraph(in);
    //     int root = 0;
    //     int k2 = 2;

    //     ContractedG cg = new ContractedG(G);
    //     EdgeWeightedDigraph contractedG = cg.computeContractedG(root, G.maxCapacity(), k2);
    // }
}


