package helpermethods;

import java.util.List;
import java.util.Set;

import graphpackage.DirectedEdge;

public class MinCutResultBruteForce {
    private double minCutValue;
    private List<DirectedEdge> minCutEdges;
    private Set<Integer> minCutRootSide;
    private Set<Integer> minCutCutSide;

    public MinCutResultBruteForce(double minCutValue, List<DirectedEdge> minCutEdges, Set<Integer> minCutRootSide, Set<Integer> minCutCutSide) {
        this.minCutValue = minCutValue;
        this.minCutEdges = minCutEdges;
        this.minCutRootSide = minCutRootSide;
        this.minCutCutSide = minCutCutSide;
    }

    public double getMinCutValue() {
        return minCutValue;
    }

    public List<DirectedEdge> getMinCutEdges() {
        return minCutEdges;
    }

    public Set<Integer> getMinCutRootSide() {
        return minCutRootSide;
    }

    public Set<Integer> getMinCutCutSide() {
        return minCutCutSide;
    }
}