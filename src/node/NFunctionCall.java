package node;

import java.io.PrintStream;

public class NFunctionCall extends NExpression {
    public NIdentifier name;
    public NFunctionCallArgList args;

    public NFunctionCall() {

    }

    public NFunctionCall(NIdentifier name, NFunctionCallArgList args) {
        this.args = args;
        this.name = name;
    }

    public void print(int indentation, boolean end,PrintStream out) {
        this.printIndentation(indentation, end,out);
        out.println("FunctionCall");
        name.print(indentation+1, false,out);
        args.print(indentation+1, false,out);
    }
}
