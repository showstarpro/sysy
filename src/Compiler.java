import java.io.*;
import java.util.*;

import assembly.Asm;
import node.*;
import parser.*;
import ir.*;

public class Compiler {

    static public void main(String argv[]) {
        try {
            String from = argv[0];
            String out;
            PrintStream file=null;
            if (argv.length > 1) {
                out = argv[3];
                file = new PrintStream(
                        new FileOutputStream(out, false));
            }

            parser p = new parser(new Lexer(new FileReader(from)));
            Object result = p.parse().value;
            NRoot root = p.root;
//            root.print(0,false,System.out);
            List<IR> ir = new ArrayList<>();
            ContextIR ctx = new ContextIR();
            root.generate_ir(ctx, ir);
            for (IR i : ir) {
                i.print(System.out, false);
            }

            if (argv.length > 1)
                Asm.generate_asm(ir, file);
            else
                Asm.generate_asm(ir, System.out);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}