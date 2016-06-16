import com.ibm.wala.cfg.ShrikeCFG;

/**
 * Created by Athithyaa on 15-06-2016.
 */
public class Edge{

    private ShrikeCFG.BasicBlock source;
    private ShrikeCFG.BasicBlock destination;

    public Edge(){
        this.source = null;
        this.destination = null;
    }

    public Edge(ShrikeCFG.BasicBlock s,ShrikeCFG.BasicBlock d){
        this.source = s;
        this.destination = d;
    }

    public Edge(Edge e){
        this.destination = e.getDestination();
        this.source = e.getSource();
    }

    public ShrikeCFG.BasicBlock getSource() {
        return source;
    }

    public void setSource(ShrikeCFG.BasicBlock source) {
        this.source = source;
    }

    public ShrikeCFG.BasicBlock getDestination() {
        return destination;
    }

    public void setDestination(ShrikeCFG.BasicBlock destination) {
        this.destination = destination;
    }

    @Override
    public int hashCode(){
        return source.hashCode() + destination.hashCode();
    }

    @Override
    public boolean equals(Object e) {
        return (e!=null)
                && (e instanceof Edge)
                && (((Edge) e).getDestination().equals(getDestination()))
                && (((Edge) e).getSource().equals(getSource()));
    }

    @Override
    public String toString(){
        return "[" + "BB "+ source.getNumber() + " ---> " + "BB " + destination.getNumber() + "]";
    }
}
