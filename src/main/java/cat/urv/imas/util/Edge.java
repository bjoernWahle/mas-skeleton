package cat.urv.imas.util;

import java.util.Objects;

public class Edge implements Comparable<Edge>, java.io.Serializable {

    private Vertex one, two;
    private int weight;

    /**
     *
     * @param one The first vertex in the Edge
     * @param two The second vertex in the Edge
     */
    public Edge(Vertex one, Vertex two){
        this(one, two, 1);
    }

    /**
     *
     * @param one The first vertex in the Edge
     * @param two The second vertex of the Edge
     * @param weight The weight of this Edge
     */
    public Edge(Vertex one, Vertex two, int weight){
        this.one = one;
        this.two = two;
        this.weight = weight;
    }

    /**
     *
     * @return Vertex this.one
     */
    public Vertex getOne(){
        return this.one;
    }

    /**
     *
     * @return Vertex this.two
     */
    public Vertex getTwo(){
        return this.two;
    }


    /**
     *
     * @return int The weight of this Edge
     */
    public int getWeight(){
        return this.weight;
    }


    /**
     *
     * @param weight The new weight of this Edge
     */
    public void setWeight(int weight){
        this.weight = weight;
    }


    /**
     * Note that the compareTo() method deviates from
     * the specifications in the Comparable interface. A
     * return value of 0 does not indicate that this.equals(other).
     * The equals() method checks the Vertex endpoints, while the
     * compareTo() is used to compare Edge weights
     *
     * @param other The Edge to compare against this
     * @return int this.weight - other.weight
     */
    public int compareTo(Edge other){
        return this.weight - other.weight;
    }

    /**
     *
     * @return String A String representation of this Edge
     */
    public String toString(){
        return "({" + one + ", " + two + "}, " + weight + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return weight == edge.weight &&
                Objects.equals(one, edge.one) &&
                Objects.equals(two, edge.two);
    }

    @Override
    public int hashCode() {

        return Objects.hash(one, two, weight);
    }
}
