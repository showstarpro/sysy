package node;

import java.io.PrintStream;

public class NFunctionDefine extends Node {
    public int return_type;
    public NIdentifier name;
    public NFunctionDefineArgList args;
    public NBlock body;

    public NFunctionDefine() {
    }

    public NFunctionDefine(int return_type, NIdentifier name, NFunctionDefineArgList args, NBlock body) {
        this.args = args;
        this.return_type = return_type;
        this.name = name;
        this.body = body;
    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation, end, out);
        out.println("FunctionDefine");

        this.printIndentation(indentation+1, false, out);
        out.println("Return Type: " + return_type);

        this.printIndentation(indentation +1 , false, out);
        out.println("Name");
        name.print(indentation+2, true, out);

        this.printIndentation(indentation+1, false, out);
        out.println("Args");
        args.print(indentation+2, true, out);

        this.printIndentation(indentation+1, true,out);
        out.println("Body");
        body.print(indentation+2,true,out );
    }
}
