package node;

import ir.ContextIR;
import ir.IR;

import java.io.PrintStream;
import java.util.List;
import java.util.Vector;

public class NRoot extends Node {
    public Vector<Node> body;

    public NRoot() {
        body = new Vector<>();
    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation, end, out);
        out.println("Root");

        for (Node i : body) {
            i.print(indentation + 1, body.indexOf(i) == body.size() - 1,out );
        }
    }

    @Override
    public void generate_ir(ContextIR ctx, List<IR> ir) throws Exception {
        for(Node i :this.body){
            i.generate_ir(ctx,ir);
        }
    }
}