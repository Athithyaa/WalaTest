import com.ibm.wala.cfg.ShrikeCFG;
import com.ibm.wala.classLoader.IBytecodeMethod;
import com.ibm.wala.shrikeBT.*;

import java.util.*;

/**
 * Created by Athithyaa on 24-05-2016.
 */
public class ConstantPropagation {

    private ShrikeCFG cfg;

    public ConstantPropagation(){
        MyCallGraph cg = new MyCallGraph("J2SEClassHierarchyExclusions.txt",
                "SampleProgram.jar","Program.fun(I)V");
        cfg = cg.constructCFG(true);
    }

    public ConstantPropagation(String jarFile,String methodSig,boolean viz){
        MyCallGraph cg = new MyCallGraph("J2SEClassHierarchyExclusions.txt"
                ,jarFile,methodSig);
        cfg = cg.constructCFG(viz);
    }

    public String extractConstant(IInstruction instr) throws Exception {
        if(instr instanceof ConstantInstruction){
            return ((ConstantInstruction)instr).getValue().toString();
        }else{
            throw new Exception();
        }
    }

    public String extractVarName(IInstruction instr, int instrIndex) throws Exception {
        IBytecodeMethod bMethod = cfg.getMethod();
        if(instr instanceof LoadInstruction){
            return bMethod.getLocalVariableName(bMethod.getBytecodeIndex(instrIndex + 1),
                    ((LoadInstruction) instr).getVarIndex());
        }else if(instr instanceof StoreInstruction){
            return bMethod.getLocalVariableName(bMethod.getBytecodeIndex(instrIndex + 1),
                    ((StoreInstruction)instr).getVarIndex());
        }else{
            throw new Exception();
        }
    }

    public HashMap<String,String> perfomUnion(HashMap<String,String>[] varList){
        HashMap<String,String> var1 = varList[0];
        HashMap<String,String> var2 = varList[1];
        HashMap<String,String> union = new HashMap<>();

        Set<String> kS;
        if(var1.keySet().size() > var2.keySet().size()){
            kS = var1.keySet();
        }else{
            kS = var2.keySet();
        }

        for(String s : kS){
            String val1 = var1.get(s);
            String val2 = var2.get(s);
            if(val1==null){
                val1="Top";
            }
            if(val2==null){
                val2="Top";
            }
            String valNew;
            if(val1.equals(val2)){
                valNew = val1;
            }else if(val1.equals("Bottom")){
                valNew = val2;
            }else if (val2.equals("Bottom")){
                valNew = val1;
            }else if(val1.equals("Top")){
                valNew = "Top";
            }else if(val2.equals("Top")){
                valNew = "Top";
            }else{
                double dval1,dval2;
                try{
                    dval1 = Double.parseDouble(val1);
                    dval2 = Double.parseDouble(val2);
                    if(dval1==dval2){
                        valNew = Double.toString(dval1);
                    }else{
                        valNew = "Top";
                    }
                }catch (Exception ex){
                    valNew = "Top";
                }
            }
            union.put(s,valNew);
        }

        return union;
    }

    public boolean evaluate(Edge edge,HashMap<String,String> variables){
        try {
            // getInstructionSet
            IInstruction[] instrSet = cfg.getInstructions();
            // get SourceBlock from the edge
            ShrikeCFG.BasicBlock next = edge.getSource();

            for (int i = next.getFirstInstructionIndex(); i <= next.getLastInstructionIndex(); i++) {
                //System.out.println(i + " " + instrSet[i].toString());
                if (instrSet[i] instanceof StoreInstruction) {
                        if(instrSet[i-1] instanceof ConstantInstruction ||
                            instrSet[i-1] instanceof LoadInstruction) {
                        String val;
                        try {
                            val = extractConstant(instrSet[i - 1]);
                        } catch (Exception ex) {
                            val = extractVarName(instrSet[i - 1], i);
                            if (variables.get(val) == null) {
                                variables.put(val, "Top");
                            }
                        }

                        String varName = extractVarName(instrSet[i], i);
                        try {
                            Double.parseDouble(val);
                            variables.put(varName, val);
                        } catch (Exception ex) {
                            variables.put(varName, variables.get(val));
                        }
                    }
                } else if(instrSet[i] instanceof BinaryOpInstruction){
                    String strVal1;
                    try {
                        strVal1 = extractConstant(instrSet[i - 1]);
                    }catch (Exception ex){
                        strVal1 = extractVarName(instrSet[i-1],i);
                        if(variables.get(strVal1)==null){
                            variables.put(strVal1,"Top");
                        }
                    }

                    String strVal2;
                    try{
                        strVal2 = extractConstant(instrSet[i-2]);
                    }catch(Exception ex){
                        strVal2 = extractVarName(instrSet[i-2],i);
                        if(variables.get(strVal2)==null){
                            variables.put(strVal2,"Top");
                        }
                    }

                    BinaryOpInstruction binInstr = (BinaryOpInstruction)instrSet[i];
                    String storeName = extractVarName(instrSet[i+1],i+1);

                    if((variables.get(strVal1)!=null &&
                            variables.get(strVal1).equals("Top"))||
                            (variables.get(strVal2)!=null &&
                                    variables.get(strVal2).equals("Top"))) {
                        variables.put(storeName,"Top");
                    }else{
                        double val1;
                        try {
                            val1 = Double.parseDouble(strVal1);
                        }catch (Exception ex){
                            val1 = Double.parseDouble(variables.get(strVal1));
                        }

                        double val2;
                        try {
                            val2=Double.parseDouble(strVal2);
                        }catch (Exception ex){
                            val2=Double.parseDouble(variables.get(strVal2));
                        }

                        if(binInstr.getOperator().equals(IBinaryOpInstruction.Operator.ADD)){
                            variables.put(storeName,Double.toString(val1+val2));
                        }
                        // other binary operators still remaining
                    }
                } else if(instrSet[i] instanceof ConditionalBranchInstruction){
                    int condFlag=0;
                    String strVal1;
                    try{
                        strVal1 = extractConstant(instrSet[i - 1]);
                    }catch (Exception ex) {
                        strVal1 = extractVarName(instrSet[i - 1], i);
                        if(variables.get(strVal1) == null){ // meaning that the variable is not initialized
                            variables.put(strVal1,"Top");
                        }
                    }

                    String strVal2;
                    try{
                        strVal2 = extractConstant(instrSet[i-2]);
                    }catch (Exception ex){
                        strVal2 = extractVarName(instrSet[i - 2],i);
                        if(variables.get(strVal2) == null){ // meaning that the variable is not initialized
                            variables.put(strVal2,"Top");
                        }
                    }

                    if(variables.get(strVal2)!=null){
                        if(variables.get(strVal2).equals("Top")) {
                            condFlag = 1;
                        }
                    }

                    if(variables.get(strVal1)!=null){
                        if(variables.get(strVal1).equals("Top")) {
                            condFlag = 1;
                        }
                    }

                    ConditionalBranchInstruction condInstr = (ConditionalBranchInstruction)instrSet[i];

                    double val1=0;
                    double val2=0;
                    int flag=0;

                    if(condFlag!=1) {
                        try {
                            val1 = Double.parseDouble(strVal1);
                        } catch (Exception ex) {
                            val1 = Double.parseDouble(variables.get(strVal1));
                        }


                        try {
                            val2 = Double.parseDouble(strVal2);
                        } catch (Exception ex) {
                            val2 = Double.parseDouble(variables.get(strVal2));
                        }
                    }


                    if (condInstr.getOperator().equals(IConditionalBranchInstruction.Operator.NE)) {
                        if(edge.getDestination().equals(cfg.getBlockForInstruction(condInstr.getTarget()))) {
                            if (condFlag != 1 && val2 != val1) {
                                flag = 1;
                            } else if(condFlag ==1){
                                flag = 1;
                            }
                        }else{
                            if (condFlag != 1 && val2 == val1) {
                                flag = 1;
                            } else if(condFlag ==1){
                                flag = 1;
                                try{
                                    Double.parseDouble(strVal1);
                                    try{
                                        Double.parseDouble(strVal2);
                                    }catch (Exception ex){
                                        variables.put(strVal2,strVal1);
                                    }
                                }catch (Exception ex){
                                    try{
                                        Double.parseDouble(strVal2);
                                        variables.put(strVal1,strVal2);
                                    }catch (Exception ex1){
                                        if(variables.get(strVal2)!=null){
                                            variables.put(strVal1,variables.get(strVal2));
                                        }
                                    }
                                }
                            }
                        }
                    } else if (condInstr.getOperator().equals(IConditionalBranchInstruction.Operator.EQ)) {
                        if(edge.getDestination().equals(cfg.getBlockForInstruction(condInstr.getTarget()))) {
                            if (condFlag != 1 && val2 == val1) {
                                flag = 1;
                            } else if(condFlag ==1){
                                flag = 1;
                                try{
                                    Double.parseDouble(strVal1);
                                    try{
                                        Double.parseDouble(strVal2);
                                    }catch (Exception ex){
                                        variables.put(strVal2,strVal1);
                                    }
                                }catch (Exception ex){
                                    try{
                                        Double.parseDouble(strVal2);
                                        variables.put(strVal1,strVal2);
                                    }catch (Exception ex1){
                                        if(variables.get(strVal2)!=null){
                                            variables.put(strVal1,variables.get(strVal2));
                                        }
                                    }
                                }
                            }
                        }else{
                            if (condFlag != 1 && val2 != val1) {
                                flag = 1;
                            } else if(condFlag ==1){
                                flag = 1;
                            }
                        }

                    } else if (condInstr.getOperator().equals(IConditionalBranchInstruction.Operator.GE)) {
                        if(edge.getDestination().equals(cfg.getBlockForInstruction(condInstr.getTarget()))) {
                            if( condFlag!=1 && val2>=val1){
                                flag=1;
                            }else if(condFlag==1){
                                flag=1;
                            }
                        }else{
                            if( condFlag!=1 && val2<val1){
                                flag=1;
                            }else if(condFlag==1){
                                flag=1;
                            }
                        }
                    } else if (condInstr.getOperator().equals(IConditionalBranchInstruction.Operator.GT)) {
                        if(edge.getDestination().equals(cfg.getBlockForInstruction(condInstr.getTarget()))) {
                            if( condFlag!=1 && val2>val1){
                                flag=1;
                            }else if(condFlag==1){
                                flag=1;
                            }
                        }else{
                            if( condFlag!=1 && val2<=val1){
                                flag=1;
                            }else if(condFlag==1){
                                flag=1;
                            }
                        }
                    } else if (condInstr.getOperator().equals(IConditionalBranchInstruction.Operator.LE)) {
                        if(edge.getDestination().equals(cfg.getBlockForInstruction(condInstr.getTarget()))) {
                            if( condFlag!=1 && val2<=val1){
                                flag=1;
                            }else if(condFlag==1){
                                flag=1;
                            }
                        }else{
                            if( condFlag!=1 && val2>val1){
                                flag=1;
                            }else if(condFlag==1){
                                flag=1;
                            }
                        }
                    } else if (condInstr.getOperator().equals(IConditionalBranchInstruction.Operator.LT)) {
                        if(edge.getDestination().equals(cfg.getBlockForInstruction(condInstr.getTarget()))) {
                            if( condFlag!=1 && val2<val1){
                                flag=1;
                            }else if(condFlag==1){
                                flag=1;
                            }
                        }else{
                            if( condFlag!=1 && val2>=val1){
                                flag=1;
                            }else if(condFlag==1){
                                flag=1;
                            }
                        }
                    }
                    return flag==1;
                }
            }
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    public HashMap<Edge,HashMap<String,String>> propagate(){

        // variables map
        HashMap<String,String> variables = new HashMap<>();

        // variable for entry block
        ShrikeCFG.BasicBlock firstBB = cfg.entry();

        // queue for traversing the control flow graph
        Queue<ShrikeCFG.BasicBlock> bbQueue = new LinkedList<>();

        // variable to manage statements and variables associated with it
        EdgeManager edgeManager = new EdgeManager();

        // variable to check for exit condition
        HashMap<String,String> dupCheck=null;

        bbQueue.add(firstBB);

        while(!bbQueue.isEmpty()) {
            ShrikeCFG.BasicBlock bbItem = bbQueue.remove();

            Iterator<ShrikeCFG.BasicBlock> itrPred = cfg.getPredNodes(bbItem);
            int predNodeCnt = cfg.getPredNodeCount(bbItem);
            HashMap<String,String>[] varList = new HashMap[predNodeCnt];
            int predCntr = 0;

            while(itrPred.hasNext()) {
                ShrikeCFG.BasicBlock itrb = itrPred.next();
                Edge e = new Edge(itrb,bbItem);
                if(edgeManager.getEdge(e) != null) {
                    if(!edgeManager.isExcluded(e)) {
                        varList[predCntr] = new HashMap<>();
                        varList[predCntr].putAll(edgeManager.getEdge(e));
                        predCntr++;
                    }
                }
            }

            if(predCntr!=predNodeCnt){
                predNodeCnt = predCntr;
            }

            if(predNodeCnt==1){
                variables.putAll(varList[0]);
            } else if(predNodeCnt > 1){
                variables.putAll(perfomUnion(varList));
            }

            List<Edge> succEdgeList = new ArrayList<>();
            Iterator<ShrikeCFG.BasicBlock> itrSucc = cfg.getSuccNodes(bbItem);

            while(itrSucc.hasNext()){
                HashMap<String,String> varResult  = new HashMap<>();
                varResult.putAll(variables);

                Edge e = new Edge(bbItem,itrSucc.next());
                boolean evaluated = evaluate(e,varResult);

                if(evaluated) {
                    succEdgeList.add(e);
                    edgeManager.putEdge(e, varResult);
                }else{
                    edgeManager.addExcludedEdge(e);
                }

                if(cfg.getSuccNodeCount(bbItem)==1){
                    variables.putAll(varResult);
                }
            }


            if(bbItem.isExitBlock()){
                edgeManager.clearExclusionList();
                if(dupCheck==null){
                    dupCheck = new HashMap<>();
                    dupCheck.putAll(variables);
                }else{
                    if(dupCheck.equals(variables)){
                        break;
                    }else{
                        dupCheck.putAll(variables);
                    }
                }
            }

            for(Edge e : succEdgeList){
                ShrikeCFG.BasicBlock dest = e.getDestination();
                if(!bbQueue.contains(dest)){
                    bbQueue.add(dest);
                }
            }
        }
        //System.out.println("Variables map:" + variables.toString());
        return edgeManager.getEdgeVarMap();
    }

    public ShrikeCFG getCfg() {
        return cfg;
    }

    public void setCfg(ShrikeCFG cfg) {
        this.cfg = cfg;
    }
}

/* // Union/Intersection conditions
                        if(variables.get(varName)!=null){
                            if(cVal.equals("Top") && variables.get(varName).equals("Top")){
                                variables.put(varName,"Top");
                            }else if(cVal.equals("Bottom") || variables.get(varName).equals("Bottom")){
                                variables.put(varName,"Bottom");
                            }else if(variables.get(varName).equals("Top")){
                                variables.put(varName,cVal);
                            }else if(cVal.equals("Top")){
                                // do nothing
                            }else if(!cVal.equals(variables.get(varName))){
                                variables.put(varName,"Bottom");
                            }
                        }else{*/
