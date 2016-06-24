import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Athithyaa on 15-06-2016.
 */
public class EdgeManager {

    private HashMap<Edge,HashMap<String,String>> edgeVarMap;
    private List<Edge> exclusionList;

    public EdgeManager(){
        edgeVarMap = new HashMap<>();
        exclusionList = new LinkedList<>();
    }

    public void putEdge(Edge e,HashMap<String,String> var){
        if(edgeVarMap!=null){
            HashMap<String,String> nVar = new HashMap<>();
            nVar.putAll(var);
            Edge edge = new Edge(e);
            edgeVarMap.put(edge,nVar);
        }
    }

    public HashMap<String,String> getEdge(Edge e){
        return edgeVarMap.get(e);
    }

    public boolean isExcluded(Edge e){
        return exclusionList.contains(e);
    }

    public void clearExclusionList(){
        exclusionList.clear();
    }

    public void addExcludedEdge(Edge e){
        if(exclusionList != null){
            exclusionList.add(e);
        }
    }

    public HashMap<Edge,HashMap<String,String>> getEdgeVarMap(){
        return edgeVarMap;
    }

}
