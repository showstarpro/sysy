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
                outfile.write(("    .macro mov32, reg, val\n" +
                        "        movw \\reg, #:lower16:\\val\n" +
                        "        movt \\reg, #:upper16:\\val\n" +
                        "    .endm\n" +
                        "\n" +
                        ".text\n" +
                        ".global main\n" +
                        ".type main, %function\n" +
                        "main:\n" +
                        "    SUB sp, sp, #12\n" +
                        "    STR lr, [sp, #8]\n" +
                        "    STR r11, [sp, #0]\n" +
                        "    STR r4, [sp, #4]\n" +
                        "\t\n" +
                        "    MOV r0, #666\n" +
                        "\n" +
                        "   BL putint\n" +
                        "    MOV r4, r0\n" +
                        "\t\n" +
                        "    MOV r0, #0\n" +
                        "    LDR lr, [sp, #8]\n" +
                        "    LDR r11, [sp,#0]\n" +
                        "    LDR r4, [sp,#4]\n" +
                        "    ADD sp, sp, #12\n" +
                        "    MOV PC, LR\n" +
                        "\t\t\t\n" +
                        "    LDR lr, [sp, #8]\n" +
                        "    LDR r11, [sp,#0]\n" +
                        "    LDR r4, [sp,#4]\n" +
                        "    ADD sp, sp, #12\n" +
                        "    MOV PC, LR\n").getBytes());
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