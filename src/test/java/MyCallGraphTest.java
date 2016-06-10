import com.ibm.wala.cfg.ShrikeCFG;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by Athithyaa on 09-06-2016.
 */
public class MyCallGraphTest {

    @Test
    public void controlGraphShouldBeGeneratedWithInputs(){
        String exFileName = "J2SEClassHierarchyExclusions.txt";
        String jarFileName = "SampleProgram.jar";
        String funSignature = "Program.fun(I)V";
        MyCallGraph mcg = new MyCallGraph(exFileName,jarFileName,funSignature);
        assertNotEquals("Does not return a null given valid jar and funcs", mcg.constructCFG(false), null);
        assertEquals("Returns a Shrike control flow graph",mcg.constructCFG(false).getClass(), ShrikeCFG.class);
        File f = new File("out.pdf");
        assertEquals("Out pdf should not be created",f.exists(),false);
    }

    @Test
    public void controlGraphPdfFileShouldBeGenerated(){
        String exFileName = "J2SEClassHierarchyExclusions.txt";
        String jarFileName = "SampleProgram.jar";
        String funSignature = "Program.fun(I)V";
        MyCallGraph mcg = new MyCallGraph(exFileName,jarFileName,funSignature);
        mcg.constructCFG(true);
        File f = new File("out.pdf");
        assertEquals("out pdf should be created",f.exists(),true);
    }
}
