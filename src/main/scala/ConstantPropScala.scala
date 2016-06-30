import com.ibm.wala.cfg.ShrikeCFG
import com.ibm.wala.classLoader.IBytecodeMethod
import com.ibm.wala.shrikeBT.IBinaryOpInstruction.Operator
import com.ibm.wala.shrikeBT.{LoadInstruction, _}
import com.ibm.wala.util.graph.traverse.BFSIterator

import scala.collection.JavaConversions
import scala.collection.mutable.ListBuffer

/**
  * Created by Athithyaa on 18-06-2016.
  */
//TO:DO change all string based lattice variables to case class
case class lat(varType: String)

class ConstantPropScala(jarFile:String,methodSig:String,viz:Boolean) {

  var cfg : ShrikeCFG = new ConstantPropagation(jarFile,methodSig,viz).getCfg
  val top = lat("Top")
  val bottom = lat("Bottom")

  def this() {
    this("SampleProgram.jar","Program.fun(I)V",true)
  }

  @throws[Exception]
  def extractVarOrConstant(instr: IInstruction, instrIndex: Int): String = {
    val bMethod: IBytecodeMethod = cfg.getMethod
    instr match {
      case constant: ConstantInstruction => constant.getValue.toString
      case load: LoadInstruction =>
        bMethod.getLocalVariableName(bMethod.getBytecodeIndex(instrIndex + 1), load.getVarIndex)
      case store: StoreInstruction =>
        bMethod.getLocalVariableName(bMethod.getBytecodeIndex(instrIndex + 1), store.getVarIndex)
      case _ =>
        throw new Exception
    }
  }


  def evaluate(e:Edge,varMap:java.util.HashMap[String,String]): Boolean = {
    val bb = e.getSource
    val instrSet = cfg.getInstructions
    var i = 0
    for( i <- bb.getFirstInstructionIndex to bb.getLastInstructionIndex){
      instrSet(i) match {
        case str: StoreInstruction => {
          if(instrSet(i-1).isInstanceOf[ConstantInstruction] ||
            instrSet(i-1).isInstanceOf[LoadInstruction]){
            val constOrVar = extractVarOrConstant(instrSet(i-1),i)
            val pattern = "(\\d*\\.?\\d*)".r
            var numb: String = ""
            constOrVar match {
              case pattern(decimal) => numb = decimal
              case other : String => {
                if(other.compareToIgnoreCase("Top")!=0){
                  if(varMap.get(other)!=null)
                    numb = varMap.get(other)
                  else
                    numb = "Top"
                }
              }
            }
            val storeName = extractVarOrConstant(instrSet(i),i)
            varMap.put(storeName,numb)
          }
        }
        case bOp: BinaryOpInstruction =>{
          val constOrVar1 = extractVarOrConstant(instrSet(i-1),i)
          val constOrVar2 = extractVarOrConstant(instrSet(i-2),i)
          val pattern = "(\\d*\\.?\\d*)".r
          var numb1 : String = ""
          var numb2 : String = ""
          val storeName = extractVarOrConstant(instrSet(i+1),i+1)
          constOrVar1 match {
            case pattern(decimal) => numb1 = decimal
            case other : String => {
              if(other.compareToIgnoreCase("Top")!=0){
                if(varMap.get(other)!=null)
                  numb1 = varMap.get(other)
                else
                  numb1 = "Top"
              }
            }
          }
          constOrVar2 match {
            case pattern(decimal) => numb1 = decimal
            case other : String => {
              if(other.compareToIgnoreCase("Top")!=0){
                if(varMap.get(other)!=null)
                  numb2 = varMap.get(other)
                else
                  numb2 = "Top"
              }
            }
          }
          if(numb1.compareTo("Top")==0 || numb2.compareTo("Top")==0){
            varMap.put(storeName, "Top")
          } else {
            bOp.getOperator match {
              case Operator.ADD => {
                varMap.put(storeName, (numb1.toDouble + numb2.toDouble).toString)
              }
              case Operator.SUB => {
                varMap.put(storeName, (numb1.toDouble - numb2.toDouble).toString)
              }
              case Operator.MUL => {
                varMap.put(storeName, (numb1.toDouble * numb2.toDouble).toString)
              }
              case _ => {

              }
            }
          }
        }
        case cond: ConditionalBranchInstruction => {
          val constOrVar1 = extractVarOrConstant(instrSet(i-1),i)
          val constOrVar2 = extractVarOrConstant(instrSet(i-2),i)
          val pattern = "(\\d*\\.?\\d*)".r
          var numb1 : String = ""
          var numb2 : String = ""
          var condFlag = 0
          var execFlag = 0
          constOrVar1 match {
            case pattern(decimal) => numb1 = decimal
            case other : String => {
              if(other.compareToIgnoreCase("Top")!=0){
                if(varMap.get(other)!=null)
                  numb1 = varMap.get(other)
                else
                  numb1 = "Top"
              }
            }
          }
          constOrVar2 match {
            case pattern(decimal) => numb1 = decimal
            case other : String => {
              if(other.compareToIgnoreCase("Top")!=0){
                if(varMap.get(other)!=null)
                  numb2 = varMap.get(other)
                else
                  numb2 = "Top"
              }
            }
          }
          if(numb1.compareTo("Top")==0 || numb2.compareTo("Top")==0){
            condFlag = 1
          }
          cond.getOperator match {
            case IConditionalBranchInstruction.Operator.EQ => {
              if( e.getDestination.equals(cfg.getBlockForInstruction(cond.getTarget))) {
                if (condFlag != 1 && numb2.toDouble == numb1.toDouble){
                  execFlag = 1
                } else if(condFlag == 1){
                  if(pattern.findAllIn(constOrVar1).next().length==0){
                    if(pattern.findAllIn(constOrVar2).next().length==0){
                      varMap.put(constOrVar2,varMap.get(constOrVar1))
                    }else{
                      varMap.put(constOrVar1,constOrVar2)
                    }
                  }else{
                    if(pattern.findAllIn(constOrVar2).next().length==0){
                      varMap.put(constOrVar2,constOrVar1)
                    }
                  }
                  execFlag = 1
                }
              }else{
                if (condFlag != 1 && numb1.toDouble != numb2.toDouble) {
                  execFlag = 1
                } else if (condFlag == 1) {
                  execFlag = 1
                }
              }
            }
            case IConditionalBranchInstruction.Operator.GE => {
              if (e.getDestination.equals(cfg.getBlockForInstruction(cond.getTarget))) {
                if (condFlag != 1 && numb2 >= numb1) {
                  execFlag = 1
                }
                else if (condFlag == 1) {
                  execFlag = 1
                }
              }
              else {
                if (condFlag != 1 && numb2 < numb1) {
                  execFlag = 1
                }
                else if (condFlag == 1) {
                  execFlag = 1
                }
              }
            }
            case IConditionalBranchInstruction.Operator.GT => {
              if (e.getDestination.equals(cfg.getBlockForInstruction(cond.getTarget))) {
                if (condFlag != 1 && numb2 > numb1) {
                  execFlag = 1
                }
                else if (condFlag == 1) {
                  execFlag = 1
                }
              }
              else {
                if (condFlag != 1 && numb2 <= numb1) {
                  execFlag = 1
                }
                else if (condFlag == 1) {
                  execFlag = 1
                }
              }
            }
            case IConditionalBranchInstruction.Operator.LE => {
              if (e.getDestination.equals(cfg.getBlockForInstruction(cond.getTarget))) {
                if (condFlag != 1 && numb2 <= numb1) {
                  execFlag = 1
                }
                else if (condFlag == 1) {
                  execFlag = 1
                }
              }
              else {
                if (condFlag != 1 && numb2 > numb1) {
                  execFlag = 1
                }
                else if (condFlag == 1) {
                  execFlag = 1
                }
              }
            }
            case IConditionalBranchInstruction.Operator.LT => {
              if (e.getDestination.equals(cfg.getBlockForInstruction(cond.getTarget))) {
                if (condFlag != 1 && numb2 < numb1) {
                  execFlag = 1
                }
                else if (condFlag == 1) {
                  execFlag = 1
                }
              }
              else {
                if (condFlag != 1 && numb2 >= numb1) {
                  execFlag = 1
                }
                else if (condFlag == 1) {
                  execFlag = 1
                }
              }
            }
            case IConditionalBranchInstruction.Operator.NE => {
              if( e.getDestination.equals(cfg.getBlockForInstruction(cond.getTarget))) {
                if (condFlag != 1 && numb2.toDouble != numb1.toDouble){
                  execFlag = 1
                } else if(condFlag == 1){
                  execFlag = 1
                }
              }else{
                if (condFlag != 1 && numb1.toDouble == numb2.toDouble) {
                  execFlag = 1
                } else if (condFlag == 1) {
                  if(pattern.findAllIn(constOrVar1).next().length==0){
                    if(pattern.findAllIn(constOrVar2).next().length==0){
                      varMap.put(constOrVar2,varMap.get(constOrVar1))
                    }else{
                      varMap.put(constOrVar1,constOrVar2)
                    }
                  }else{
                    if(pattern.findAllIn(constOrVar2).next().length==0){
                      varMap.put(constOrVar2,constOrVar1)
                    }
                  }
                  execFlag = 1
                }
              }
            }
          }
          if(execFlag==1){
            return true
          }else{
            return false
          }
        }
        case _ => {

        }
      }
    }
    true
  }

  def union(varMap1:java.util.HashMap[String, String],varMap2:java.util.HashMap[String, String])
  : java.util.HashMap[String, String] = {

    val union: java.util.HashMap[String, String] = new java.util.HashMap[String, String]
    var kS: java.util.Set[String] = null

    if (varMap1==null && varMap2==null){
      return union
    } else if(varMap1==null){
      return varMap2
    } else if(varMap2==null){
      return varMap1
    } else if(varMap1.keySet.size == 0 && varMap2.keySet.size == 0){
      kS = varMap1.keySet
    } else if (varMap1.keySet.size == 0){
      return varMap2
    } else if (varMap2.keySet.size == 0){
      return varMap1
    } else if (varMap1.keySet.size > varMap2.keySet.size) {
      kS = varMap1.keySet
    } else if(varMap1.keySet.size < varMap2.keySet.size) {
      kS = varMap2.keySet
    }  else{
      kS = varMap2.keySet
    }

    for (s <- JavaConversions.asScalaSet(kS)) {
      var val1: String = varMap1.get(s)
      var val2: String = varMap2.get(s)
      if (val1 == null) {
        val1 = "Top"
      }
      if (val2 == null) {
        val2 = "Top"
      }

      var valNew: String = null
      if (val1 == val2) {
        valNew = val1
      }
      else if (val1 == "Bottom") {
        valNew = val2
      }
      else if (val2 == "Bottom") {
        valNew = val1
      }
      else if (val1 == "Top") {
        valNew = "Top"
      }
      else if (val2 == "Top") {
        valNew = "Top"
      }
      else {
        var dval1: Double = .0
        var dval2: Double = .0
        try {
          dval1 = val1.toDouble
          dval2 = val2.toDouble
          if (dval1 == dval2) {
            valNew = java.lang.Double.toString(dval1)
          }
          else {
            valNew = "Top"
          }
        }
        catch {
          case ex: Exception => {
            valNew = "Top"
          }
        }
      }
      union.put(s, valNew)
    }
    union
  }

  def propagate(): java.util.HashMap[Edge, java.util.HashMap[String, String]] ={
    val itr = new BFSIterator[ShrikeCFG#BasicBlock](cfg)
    val listBuffer = new ListBuffer[ShrikeCFG#BasicBlock]()
    while(itr.hasNext){
      listBuffer += itr.next()
    }
    val bbList = listBuffer.result()

    val x = bbList.map((bb:ShrikeCFG#BasicBlock) => {
      val succItr = cfg.getSuccNodes(bb)
      val edgeArray = new Array[Edge](cfg.getSuccNodeCount(bb))
      var cntr = 0
      while(succItr.hasNext){
        val edge = new Edge(bb,succItr.next())
        edgeArray(cntr) = edge
        cntr += 1
      }
      edgeArray
    })

    val edgeManager = new EdgeManager()
    val loopCntr = 0
    //var dupCheck : java.util.HashMap[String,String] = new util.HashMap[String,String]()
    for(loopCntr <- 1 to 5) {
      x.foreach((edges: Array[Edge]) => {
        for (e <- edges) {
          val src = e.getSource
          if (!edgeManager.isBlockExcluded(src, cfg)) {
            val predItr: java.util.Iterator[ShrikeCFG#BasicBlock] = cfg.getPredNodes(src)
            val predNodeList = JavaConversions.asScalaIterator(predItr).toList
            val uVarMap = predNodeList.foldLeft(new java.util.HashMap[String, String]())((r, c: ShrikeCFG#BasicBlock) => {
              val edge1 = new Edge(c, src)
              val varMap = new java.util.HashMap[String, String]()
              if (edgeManager.getEdge(edge1) != null) {
                varMap.putAll(edgeManager.getEdge(edge1))
              }
              union(r, varMap)
            })
            val exec = evaluate(e, uVarMap)
            if (exec) {
              edgeManager.putEdge(e, uVarMap)
            } else {
              edgeManager.addExcludedEdge(e)
            }
          } else {
            val succItr = cfg.getSuccNodes(src)
            val succNodeList = JavaConversions.asScalaIterator(succItr).toList
            for (n <- succNodeList) {
              val edge1 = new Edge(src, n)
              edgeManager.addExcludedEdge(edge1)
            }
          }
          if (src.getNumber.equals(cfg.getNumberOfNodes-2)) {
            edgeManager.clearExclusionList()
          }
        }
      })
    }
    edgeManager.getEdgeVarMap
  }

}
