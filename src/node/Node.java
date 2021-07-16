package node;

import java.io.PrintStream;

public class Node {
    public Node(){

    }
    public void print(int indentation, boolean end, PrintStream out){
        this.printIndentation(indentation, end, out);
        out.print("[node Node]");
    }

    public void printIndentation(int indentation, boolean end, PrintStream out){
        for (int i = 0; i < indentation; i++) {
            out.print("│   ");
        }
        if (end)
            out.print("└──");
        else
            out.print("├──");
    }
}

