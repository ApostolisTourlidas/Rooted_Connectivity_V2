package graphpackage;
public class DirectedEdge {
    private int v;
    private int w;
    private double weight;
    // initializations for Push-Relabel
    private double flow;
    private DirectedEdge reverseEdge;

    /**
     * Initializes a directed edge from vertex {@code v} to vertex {@code w} with
     * the given {@code weight}.
     * @param v the tail vertex
     * @param w the head vertex
     * @param weight the weight of the directed edge
     * @throws IllegalArgumentException if either {@code v} or {@code w}
     *    is a negative integer
     * @throws IllegalArgumentException if {@code weight} is {@code NaN}
     */
    public DirectedEdge(int v, int w, double weight) {
        if (v < 0) throw new IllegalArgumentException("Vertex names must be non-negative integers");
        if (w < 0) throw new IllegalArgumentException("Vertex names must be non-negative integers");
        if (Double.isNaN(weight)) throw new IllegalArgumentException("Weight is NaN");
        this.v = v;
        this.w = w;
        this.weight = weight;
        this.flow = 0;
    }

    /**
     * Returns the tail vertex of the directed edge.
     * @return the tail vertex of the directed edge
     */
    public int from() {
        return v;
    }

    /**
     * Returns the head vertex of the directed edge.
     * @return the head vertex of the directed edge
     */
    public int to() {
        return w;
    }

    /**
     * Returns the weight of the directed edge.
     * @return the weight of the directed edge
     */
    public double weight() {
        return weight;
    }

    // setter for edge's weight
    public void setWeight(double weight){
        this.weight = weight;
    }

    /**
     * Returns a string representation of the directed edge.
     * @return a string representation of the directed edge
     */
    public String toString() {
        return v + "->" + w + " " + String.format("%5.2f", weight);
    }

    // extra methods for Push - Relabel

    public void setReverseEdge(DirectedEdge reverse) {
        reverseEdge = reverse;
    }

    // getter for residual capacity
    public double residualCapacity() {
        return weight - flow;
    }

    public void addFlow(double amount) {
        flow += amount;
        getReverseEdge().flow -= amount; //update reverse edge
    }
    
    public DirectedEdge getReverseEdge() {
        return reverseEdge;
    }

    public double getFlow() {
        return flow;
    }
    
    

    /**
     * Unit tests the {@code DirectedEdge} data type.
     *
     * @param args the command-line arguments
     */
    /*public static void main(String[] args) {
        DirectedEdge e = new DirectedEdge(12, 34, 5.67);
        System.out.println(e);
    }*/
}