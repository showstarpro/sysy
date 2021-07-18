package node;

import ir.ContextIR;
import ir.IR;

import java.io.PrintStream;
import java.util.List;
import java.util.Vector;

public class NArrayDeclare extends NDeclare {
    public NArrayIdentifier name;

    public NArrayDeclare() {

    }

    public NArrayDeclare(NArrayIdentifier name) {
        this.name = name;
    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation,end, out);
        out.println("ArrayDeclare");
        name.print(indentation+1, true, out);
    }

    @Override
    public void generate_ir(ContextIR ctx, List<IR> ir) throws Exception {
        Vector<Integer> shape = new Vector<>();
        for(NExpression i : this.name.shape){
            shape.addElement(i.eval(ctx));
        }
    }
}
