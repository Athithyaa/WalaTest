import com.ibm.wala.cfg.ShrikeCFG;
import com.ibm.wala.shrikeBT.*;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

/**
 * Created by Athithyaa on 09-06-2016.
 */
public class ConstantPropagationTest {

    @Test
    public void constantShouldBeExtracted(){
        String obtainedVal="";
        ConstantInstruction testConstInstr = new ConstantInstruction((short) 10) {
            @Override
            public Object getValue() {
                return "3";
            }

            @Override
            public String getType() {
                return "Integer";
            }
        };
        ConstantPropagation cp = new ConstantPropagation();
        try {
            obtainedVal=cp.extractConstant(testConstInstr);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        assertEquals("expected constant should be 3",obtainedVal,"3");
    }

    @Test(expected = Exception.class)
    public void nonConstantInstructionShouldThrowException() throws Exception {
        IInstruction instr = new IInstruction() {
            @Override
            public boolean isFallThrough() {
                return false;
            }

            @Override
            public int[] getBranchTargets() {
                return new int[0];
            }

            @Override
            public IInstruction redirectTargets(int[] ints) {
                return null;
            }

            @Override
            public int getPoppedCount() {
                return 0;
            }

            @Override
            public String getPushedType(String[] strings) {
                return null;
            }

            @Override
            public byte getPushedWordSize() {
                return 0;
            }

            @Override
            public void visit(Visitor visitor) {

            }

            @Override
            public boolean isPEI() {
                return false;
            }
        };
        ConstantPropagation cp = new ConstantPropagation();
        cp.extractConstant(instr);
    }

    @Test
    public void evaluateShouldEvaluateStatementsProperly() {
        ConstantPropagation cp = new ConstantPropagation();
        ShrikeCFG cfg = cp.getCfg();
        ShrikeCFG.BasicBlock bb = cfg.getNode(1);
        HashMap<String,String> variables = new HashMap<>();
        cp.evaluate(bb,variables);
        assertEquals("a should have 5",variables.get("a"),"5");
        assertEquals("b should have 10",variables.get("b"),"10");
        assertEquals("z should have 7",variables.get("z"),"7");
        ShrikeCFG.BasicBlock bb1 = cfg.getNode(2);
        cp.evaluate(bb1,variables);
        assertEquals("a should have 10",variables.get("a"),"10");
        assertEquals("b should have 10",variables.get("b"),"10");
        assertEquals("z should have 1",variables.get("z"),"1");
    }

    @Test
    public void performUnionShouldPerformMeetOperation(){
        ConstantPropagation cp = new ConstantPropagation();
        HashMap<String,String>[] vars = new HashMap[2];
        vars[0] = new HashMap<>();
        vars[1] = new HashMap<>();
        vars[0].put("a","5");
        vars[0].put("b","10");
        vars[0].put("z","7");
        vars[0].put("x","Bottom");
        vars[0].put("c","2");
        vars[1].put("a","10");
        vars[1].put("b","10");
        vars[1].put("z","7");
        vars[1].put("x","2");
        vars[1].put("c","Top");
        HashMap<String,String> result = cp.perfomUnion(vars);

        assertEquals("a should have Bottom",result.get("a"),"Bottom");
        assertEquals("b should have 10",result.get("b"),"10");
        assertEquals("z should have 7",result.get("z"),"7");
        assertEquals("x should have Bottom",result.get("x"),"Bottom");
        assertEquals("c should have 7",result.get("c"),"2");
    }

    @Test
    public void performPropagate(){
        ConstantPropagation cp = new ConstantPropagation();
        HashMap<String,String> variables = cp.propagate();
        assertEquals("a should have Bottom",variables.get("a"),"15");
        assertEquals("b should have 10",variables.get("b"),"10");
        assertEquals("z should have 7",variables.get("z"),"3");
        assertEquals("x should have Bottom",variables.get("x"),null);
    }
}
