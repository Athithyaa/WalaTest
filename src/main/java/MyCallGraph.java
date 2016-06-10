import com.ibm.wala.cfg.ShrikeCFG;
import com.ibm.wala.classLoader.IBytecodeMethod;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrikeBT.IInstruction;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAOptions;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;
import com.ibm.wala.util.strings.StringStuff;
import com.ibm.wala.viz.DotUtil;
import com.ibm.wala.viz.NodeDecorator;
import com.ibm.wala.viz.PDFViewUtil;

import java.io.File;

/**
 * Created by Athithyaa on 05-06-2016.
 */
public class MyCallGraph {
    private static final String dot_exe = "C:\\Program Files (x86)\\Graphviz2.38\\bin\\dot.exe";
    private static final String pdf_exe = "C:\\Program Files (x86)\\Adobe\\Acrobat Reader DC\\Reader\\AcroRd32.exe";
    private IR intermediateRep;

    public MyCallGraph(String exclusionFileName,String jarFileName,String methodRef){
        try {
            File exFile = new FileProvider().getFile(exclusionFileName);
            AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(jarFileName, exFile);
            IClassHierarchy cha = ClassHierarchy.make(scope);
            MethodReference mr = StringStuff.makeMethodReference(methodRef);
            IMethod m = cha.resolveMethod(mr);
            AnalysisOptions options = new AnalysisOptions();
            options.getSSAOptions().setPiNodePolicy(SSAOptions.getAllBuiltInPiNodes());
            AnalysisCache cache = new AnalysisCache();
            intermediateRep = cache.getSSACache().findOrCreateIR(m, Everywhere.EVERYWHERE, options.getSSAOptions());
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public ShrikeCFG constructCFG(boolean drawGraph){
        try {

            File existingPDFFile = new File("out.pdf");
            if(existingPDFFile.exists())
                existingPDFFile.delete();

            IBytecodeMethod ibm = (IBytecodeMethod) intermediateRep.getMethod();
            ShrikeCFG scfg = ShrikeCFG.make(ibm);
            final IInstruction[] instrSet = scfg.getInstructions();


            NodeDecorator nd = new NodeDecorator() {
                @Override
                public String getLabel(Object o) throws WalaException {
                    ShrikeCFG.BasicBlock bb = (ShrikeCFG.BasicBlock) o;
                    StringBuffer instrs = new StringBuffer();
                    for (int i = bb.getFirstInstructionIndex(); i <= bb.getLastInstructionIndex(); i++) {
                        instrs.append(instrSet[i].toString()).append("\n");
                    }
                    return instrs.toString();
                }
            };

            if(drawGraph) {
                DotUtil.dotify(scfg, nd, "temp.dt", "out.pdf", dot_exe);
                //PDFViewUtil.launchPDFView("out.pdf", pdf_exe);
            }
            return scfg;
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

}

//other usefull api calls
//Iterable<Entrypoint> e = Util.makeMainEntrypoints(scope, cha);
//AnalysisOptions o = new AnalysisOptions(scope,e);
//CallGraphBuilder builder = Util.makeZeroCFABuilder(o, new AnalysisCache(), cha, scope);
//CallGraph cg = builder.makeCallGraph(o, null);
//Graph g = ir.getControlFlowGraph();