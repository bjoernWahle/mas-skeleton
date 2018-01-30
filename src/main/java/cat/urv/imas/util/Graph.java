package cat.urv.imas.util;

import cat.urv.imas.map.Cell;
import cat.urv.imas.map.PathCell;

import java.util.*;
import java.util.stream.Collectors;

public class Graph<T> {

    public HashMap<T, Vertex<T>> getVertices() {
        return vertices;
    }

    private HashMap<T, Vertex<T>> vertices;
    private HashSet<Edge> edges;

    public Graph(){
        this.vertices = new HashMap<T, Vertex<T>>();
        this.edges = new HashSet<Edge>();
    }

    /**
     * This constructor accepts an ArrayList<Vertex> and populates
     * this.vertices. If multiple Vertex objects have the same label,
     * then the last Vertex with the given label is used.
     *
     * @param vertices The initial Vertices to populate this Graph
     */
    public Graph(ArrayList<Vertex<T>> vertices){
        this.vertices = new HashMap<T, Vertex<T>>();
        this.edges = new HashSet<Edge>();

        for(Vertex<T> v: vertices){
            this.vertices.put(v.getLabel(), v);
        }

    }

    /**
     * This method adds am edge between Vertices one and two
     * of weight 1, if no Edge between these Vertices already
     * exists in the Graph.
     *
     * @param one The first vertex to add
     * @param two The second vertex to add
     * @return true iff no Edge relating one and two exists in the Graph
     */
    public boolean addEdge(Vertex<T> one, Vertex<T> two){
        return addEdge(one, two, 1);
    }


    /**
     * Accepts two vertices and a weight, and adds the edge
     * ({one, two}, weight) iff no Edge relating one and two
     * exists in the Graph.
     *
     * @param one The first Vertex of the Edge
     * @param two The second Vertex of the Edge
     * @param weight The weight of the Edge
     * @return true iff no Edge already exists in the Graph
     */
    public boolean addEdge(Vertex<T> one, Vertex<T> two, int weight){
        if(one.equals(two)){
            return false;
        }

        //ensures the Edge is not in the Graph
        Edge e = new Edge(one, two, weight);
        if(edges.contains(e)){
            return false;
        }

        //and that the Edge isn't already incident to one of the vertices
        else if(one.containsNeighbor(e) || two.containsNeighbor(e)){
            return false;
        }

        edges.add(e);
        one.addNeighbor(e);
        two.addNeighbor(e);
        return true;
    }

    /**
     *
     * @param e The Edge to look up
     * @return true iff this Graph contains the Edge e
     */
    public boolean containsEdge(Edge e){
        if(e.getOne() == null || e.getTwo() == null){
            return false;
        }

        return this.edges.contains(e);
    }


    /**
     * This method removes the specified Edge from the Graph,
     * including as each vertex's incidence neighborhood.
     *
     * @param e The Edge to remove from the Graph
     * @return Edge The Edge removed from the Graph
     */
    public Edge removeEdge(Edge e){
        e.getOne().removeNeighbor(e);
        e.getTwo().removeNeighbor(e);
        if (this.edges.remove(e)) {
            return e;
        } else {
            throw new IllegalArgumentException("Edge " + e + " not found in Graph");
        }
    }

    /**
     *
     * @param vertex The Vertex to look up
     * @return true iff this Graph contains vertex
     */
    public boolean containsVertex(Vertex<T> vertex){
        return this.vertices.get(vertex.getLabel()) != null;
    }

    /**
     *
     * @param label The specified Vertex label
     * @return Vertex The Vertex with the specified label
     */
    public Vertex<T> getVertex(T label){
        return vertices.get(label);
    }

    /**
     * This method adds a Vertex to the graph. If a Vertex with the same label
     * as the parameter exists in the Graph, the existing Vertex is overwritten
     * only if overwriteExisting is true. If the existing Vertex is overwritten,
     * the Edges incident to it are all removed from the Graph.
     *
     * @param vertex
     * @param overwriteExisting
     * @return true iff vertex was added to the Graph
     */
    public boolean addVertex(Vertex<T> vertex, boolean overwriteExisting){
        Vertex<T> current = this.vertices.get(vertex.getLabel());
        if(current != null){
            if(!overwriteExisting){
                return false;
            }

            while(current.getNeighborCount() > 0){
                this.removeEdge(current.getNeighbor(0));
            }
        }


        vertices.put(vertex.getLabel(), vertex);
        return true;
    }

    /**
     *
     * @param label The label of the Vertex to remove
     * @return Vertex The removed Vertex object
     */
    public Vertex removeVertex(T label){
        Vertex<T> v = vertices.remove(label);

        while(v.getNeighborCount() > 0){
            this.removeEdge(v.getNeighbor((0)));
        }

        return v;
    }

    /**
     *
     * @return Set<T> The unique labels of the Graph's Vertex objects
     */
    public Set<T> vertexKeys(){
        return this.vertices.keySet();
    }

    /**
     *
     * @return Set<Edge> The Edges of this graph
     */
    public Set<Edge> getEdges(){
        return new HashSet<Edge>(this.edges);
    }

    public List<T> getShortestPath(T startLabel, List<T> destLabels) {
        Vertex<T> startVertex =  this.getVertex(startLabel);
        DijkstraAlgorithm algo = new DijkstraAlgorithm(this);
        algo.execute(startVertex);
        int minDistance = Integer.MAX_VALUE;
        Vertex<T> bestDest = null;
        for(T dc : destLabels)  {
            Vertex<T> destVertex = this.getVertex(dc);
            int distance = algo.distance.get(destVertex);
            if(distance < minDistance) {
                minDistance = distance;
                bestDest = destVertex;
            }
        }
        List<Vertex> vertices = algo.getPath(bestDest);
        List<T> vList = vertices.stream().map(Vertex<T>::getLabel).collect(Collectors.toList());
        if (!(vList.get(vList.size() - 1) instanceof PathCell)) {
            vList.remove(vList.size() -1);
        }
        vList.remove(0);
        return vList;
    }
    
    public int getShortestDistance(T startLabel, List<T> destLabels) {
        Vertex<T> startVertex =  this.getVertex(startLabel);
        DijkstraAlgorithm algo = new DijkstraAlgorithm(this);
        algo.execute(startVertex);
        int minDistance = Integer.MAX_VALUE;
        if(destLabels == null) {
        	return minDistance;
        }
        Vertex<T> bestDest = null;
        for(T dc : destLabels)  {
            Vertex<T> destVertex = this.getVertex(dc);
            int distance = algo.distance.get(destVertex);
            if(distance < minDistance) {
                minDistance = distance;
                bestDest = destVertex;
            }
        }
        return minDistance;
        }
}
