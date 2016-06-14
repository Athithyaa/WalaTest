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
                valNew = "Top";
            }
            union.put(s,valNew);
        }

        return union;
    }

    public ShrikeCFG.BasicBlock[] evaluate(ShrikeCFG.BasicBlock next,HashMap<String,String> variables){
        try {
            IInstruction[] instrSet = cfg.getInstructions();

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
                    String storeName = extractVarName(instrSet[i+1],i);

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

                    if(condFlag!=1) {
                        ConditionalBranchInstruction condInstr = (ConditionalBranchInstruction)instrSet[i];

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

                        int flag=0;
                        ShrikeCFG.BasicBlock[] bbList = new ShrikeCFG.BasicBlock[1];
                        if (condInstr.getOperator().equals(IConditionalBranchInstruction.Operator.NE)) {
                            if(val2!=val1){
                                flag=1;
                            }
                        } else if (condInstr.getOperator().equals(IConditionalBranchInstruction.Operator.EQ)) {
                            if(val2==val1){
                                flag=1;
                            }
                        } else if (condInstr.getOperator().equals(IConditionalBranchInstruction.Operator.GE)) {
                            if(val2>=val1){
                                flag=1;
                            }
                        } else if (condInstr.getOperator().equals(IConditionalBranchInstruction.Operator.GT)) {
                            if(val2>val1){
                                flag=1;
                            }
                        } else if (condInstr.getOperator().equals(IConditionalBranchInstruction.Operator.LE)) {
                            if(val2<=val1){
                                flag=1;
                            }
                        } else if (condInstr.getOperator().equals(IConditionalBranchInstruction.Operator.LT)) {
                            if(val2<val1){
                                flag=1;
                            }
                        }

                        if(flag==1){
                            bbList[0] = cfg.getBlockForInstruction(condInstr.getTarget());
                        }else{
                            bbList[0] = cfg.getBlockForInstruction(i+1);
                        }

                        return bbList;
                    }else{
                        int ctr = 0;
                        ShrikeCFG.BasicBlock[] bbList = new ShrikeCFG.BasicBlock[cfg.getSuccNodeCount(next)];
                        Iterator<ShrikeCFG.BasicBlock> it = cfg.getSuccNodes(next);
                        while(it.hasNext()){
                            bbList[ctr] = it.next();
                            ctr++;
                        }
                        return bbList;
                    }
                }
            }
            int ctr = 0;
            ShrikeCFG.BasicBlock[] bbList = new ShrikeCFG.BasicBlock[cfg.getSuccNodeCount(next)];
            Iterator<ShrikeCFG.BasicBlock> it = cfg.getSuccNodes(next);
            while(it.hasNext()){
                bbList[ctr] = it.next();
                ctr++;
            }
            return bbList;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public HashMap<String,String> propagate(){
        HashMap<String,String> variables = new HashMap<>();

        ShrikeCFG.BasicBlock firstBB = cfg.entry();
        Queue<ShrikeCFG.BasicBlock> bbQueue = new LinkedList<>();
        HashMap<ShrikeCFG.BasicBlock,HashMap<String,String>> bVarMap = new HashMap<>();
        HashMap<ShrikeCFG.BasicBlock,List<ShrikeCFG.BasicBlock>> edgeExclMap = new HashMap<>();
        HashMap<String,String> dupCheck=null;

        bbQueue.add(firstBB);

        while(!bbQueue.isEmpty()) {
            ShrikeCFG.BasicBlock bbItem = bbQueue.remove();

            Iterator<ShrikeCFG.BasicBlock> itr = cfg.getPredNodes(bbItem);
            int predNodeCnt = cfg.getPredNodeCount(bbItem);
            HashMap<String,String>[] varList = new HashMap[predNodeCnt];
            int predCntr = 0;

            while(itr.hasNext()) {
                ShrikeCFG.BasicBlock itrb = itr.next();
                if(bVarMap.get(itrb) != null) {
                    if(edgeExclMap.get(itrb)!=null) {
                        if(!edgeExclMap.get(itrb).contains(bbItem)) {
                            varList[predCntr] = new HashMap<>();
                            varList[predCntr].putAll(bVarMap.get(itrb));
                            predCntr++;
                        }
                    }else{
                        varList[predCntr] = new HashMap<>();
                        varList[predCntr].putAll(bVarMap.get(itrb));
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

            ShrikeCFG.BasicBlock[] succBBs = evaluate(bbItem, variables);
            HashMap<String,String> v = new HashMap<>();
            v.putAll(variables);
            bVarMap.put(bbItem, v);

            if(succBBs.length < cfg.getSuccNodeCount(bbItem)){
                Iterator<ShrikeCFG.BasicBlock> itrSucc = cfg.getSuccNodes(bbItem);
                Set<ShrikeCFG.BasicBlock> succSet = new LinkedHashSet<>();

                while(itrSucc.hasNext()){
                    succSet.add(itrSucc.next());
                }

                for(ShrikeCFG.BasicBlock b: succBBs){
                    succSet.remove(b);
                }
                if(!succSet.isEmpty()){
                    for(ShrikeCFG.BasicBlock b : succSet){
                        List<ShrikeCFG.BasicBlock> adjList = new ArrayList<>();
                        adjList.add(b);
                        edgeExclMap.put(bbItem,adjList);
                    }
                }
            }


            if(bbItem.isExitBlock()){
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

            for(ShrikeCFG.BasicBlock bb : succBBs){
                if(!bbQueue.contains(bb)){
                    bbQueue.add(bb);
                }
            }
        }
        //System.out.println("Variables map:" + variables.toString());
        return variables;
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
