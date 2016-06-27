import com.ibm.wala.shrikeBT.IInstruction.Visitor
import com.ibm.wala.shrikeBT.{ConstantInstruction, IInstruction}
import org.scalatest.FlatSpec

/**
  * Created by Athithyaa on 26-06-2016.
  */

class ConstantPropScalaTest extends FlatSpec{

  "extractVarOrConstant" should "return a string which is either a constant or a variable name" in {
    var obtainedVal: String = ""
    val testConstInstr: ConstantInstruction = new ConstantInstruction((10.toShort)) {
      def getValue: Object = {
        return "3"
      }

      def getType: String =
      {
        return "Integer"
      }
    }
    val result = ConstantPropScala.extractVarOrConstant(testConstInstr,0)
    assert(result==="3")
    val instrSet = ConstantPropScala.cfg.getInstructions
    val testVarInstr = instrSet(1)
    val result1 = ConstantPropScala.extractVarOrConstant(testVarInstr,1)
    assert(result1==="a")
  }

  it should "otherwise throw an exception" in {
    val sampleInstr = new IInstruction {
      override def getPushedType(poppedTypesToCheck: Array[String]): String = ???

      override def getPushedWordSize: Byte = ???

      override def isPEI: Boolean = ???

      override def visit(v: Visitor): Unit = ???

      override def isFallThrough: Boolean = ???

      override def getBranchTargets: Array[Int] = ???

      override def getPoppedCount: Int = ???

      override def redirectTargets(targetMap: Array[Int]): IInstruction = ???
    }
    intercept[Exception] {
      ConstantPropScala.extractVarOrConstant(sampleInstr, 0)
    }


  }
}
