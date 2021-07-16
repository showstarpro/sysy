import java.io.*;
import java.util.*;

import node.*;

public class Main {

    static public void main(String argv[]) {
        try {
            parser p = new parser(new Lexer(new FileReader(argv[0])));
            Object result = p.parse().value;
            NRoot root = p.root;
            root.print(0,false,System.out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}