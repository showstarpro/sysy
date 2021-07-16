package node;

import java.io.PrintStream;
import java.util.Vector;

public class NArrayIdentifier extends NIdentifier {
    public NIdentifier name;
    public Vector<NExpression> shape;

    public NArrayIdentifier() {
        shape = new Vector<>();
    }

    public NArrayIdentifier(NIdentifier name) {
        this();
        this.name = name;
    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation, false, out);
        out.println("ArrayIdentifier");
        name.print(indentation+1, false, out);

        this.printIndentation(indentation +1,true, out);
        out.println("Shape");
        for(NExpression i: shape){
            i.print(indentation+2, shape.indexOf(i)==shape.size()-1, out);
        }
    }
}
