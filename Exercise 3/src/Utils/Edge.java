package Utils;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

public class Edge implements Serializable, Comparable{

    private static final long serialVersionUID = 4608245581791590791L;

    public static class EdgeComparator implements Comparator<Double> {

        Map<Double, Edge> base;

        public EdgeComparator(Map<Double, Edge> base) {
            this.base = base;
        }

        @Override
        public int compare(Double key1, Double key2) {
                if (base.get(key1).getFrom() < base.get(key2).getFrom())
                    return -1;
                else if (base.get(key1).getFrom() > base.get(key2).getFrom())
                    return 1;
                else {
                    if (base.get(key1).getTo() <= base.get(key2).getTo())
                        return -1;
                    else return 1;
                }
        }
    }

    private EdgeState state;
    private int from;
    private int to;
    private double weight;

    public Edge() {
    }

    public Edge(EdgeState s, int id1, int id2, double w) {
        state = s;
        from = id1;
        to = id2;
        weight = w;
    }
    public Edge(Edge edge){
        state = edge.getState();
        from = edge.getFrom();
        to = edge.getTo();
        weight = edge.getWeight();
    }

    public EdgeState getState() {
        return state;
    }

    public void setState(EdgeState state) {
        this.state = state;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public int compareTo(Object arg0) {
        if(arg0 == null){
            return -1;
        }
        // We can compare like this because we assume unique weights
        double otherWeight = ((Edge) arg0).getWeight();
        if (this.weight < otherWeight)
            return -1;
        if (this.weight > otherWeight)
            return 1;
        return 0;
    }
}