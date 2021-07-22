package node;

import ir.ContextIR;
import ir.IR;

import java.io.PrintStream;
import java.util.List;

public class Node {
    public Node() {

    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation, end, out);
        out.print("[node Node]");
    }

    public void printIndentation(int indentation, boolean end, PrintStream out) {
        for (int i = 0; i < indentation; i++) {
            out.print("│   ");
        }
        if (end)
            out.print("└──");
        else
            out.print("├──");
    }

    public void generate_ir(ContextIR ctx, List<IR> ir) throws Exception {
//        this.print(0, false, System.out);
        throw new Exception("Can't generate IR for this node.");
    }
}

