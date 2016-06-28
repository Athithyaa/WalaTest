/**
  * Created by Athithyaa on 27-06-2016.
  */
object ScalaRunner {
  def main(args: Array[String]) {
    if (args.length == 3) {
      val cp = new ConstantPropScala(args(0), args(1), args(2).toBoolean)
      System.out.println("Variables after propagation: " + cp.propagate)
    }
    else {
      val cp = new ConstantPropScala
      System.out.println("Variables after propagation: " + cp.propagate)
    }
  }
}
