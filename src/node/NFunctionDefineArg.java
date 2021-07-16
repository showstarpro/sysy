package node;

import java.io.PrintStream;

public class NFunctionDefineArg extends NExpression {
    public int type;
    public NIdentifier name;

    public NFunctionDefineArg() {

    }

    public NFunctionDefineArg(int type, NIdentifier name) {
        this.type = type;
        this.name = name;
    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation,end, out);
        out.println("FunctionDefineArg Type: "+type);
        name.print(indentation+1, true, out);
    }
}
