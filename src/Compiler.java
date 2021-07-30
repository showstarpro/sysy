import java.io.*;
import java.util.*;

import assembly.Asm;
import node.*;
import parser.*;
import ir.*;

public class Compiler {

    static public void main(String argv[]) {
        String out = null;
        PrintStream file=null;
        FileOutputStream outfile = null;
        try {
            String from = argv[3];
            if (argv.length > 1) {
                out = argv[2];
                outfile = new FileOutputStream(out, false);
                file = new PrintStream(
                       outfile);
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
            // 删除生成的文件  看是不是CE了
            try {
                assert outfile != null;
                outfile.close();
                outfile = new FileOutputStream(out, false);
                outfile.write("".getBytes());
                outfile.close();

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }finally {
            try {
                assert outfile != null;
                outfile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}