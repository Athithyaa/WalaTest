
/**
 * Created by Athithyaa on 06-06-2016.
 */
public class Main {
    public static void main(String[] args){
        if(args.length==3) {
            ConstantPropagation cp = new ConstantPropagation(args[0],args[1],Boolean.parseBoolean(args[2]));
            System.out.println("Variables after propagation: " + cp.propagate());
        }else {
            ConstantPropagation cp = new ConstantPropagation();
            System.out.println("Variables after propagation: " + cp.propagate());
        }
    }
}
