package ir;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class IR {
   public enum OpCode {
        MALLOC_IN_STACK,  // dest = offset(new StackArray(size op1))
        MOV,              // dest = op1
        ADD,              // dest = op1 + op2
        SUB,              // dest = op1 - op2
        IMUL,             // dest = op1 * op2
        IDIV,             // dest = op1 / op2
        MOD,              // dest = op1 % op2
        SET_ARG,          // if dest < 4: R(dest)) = op1 else: push_stack(op1)
        CALL,             // call label
        CMP,              // cmp op1, op2
        JMP,              // jmp label
        JEQ,              // if EQ: jmp label
        JNE,              // if NE: jmp label
        JLE,              // if LE: jmp label
        JLT,              // if LT: jmp label
        JGE,              // if GE: jmp label
        JGT,              // if GT: jmp label
        MOVEQ,            // if EQ: dest = op1 else: dest = op2
        MOVNE,            // if NE: dest = op1 else: dest = op2
        MOVLE,            // if LE: dest = op1 else: dest = op2
        MOVLT,            // if LT: dest = op1 else: dest = op2
        MOVGE,            // if GE: dest = op1 else: dest = op2
        MOVGT,            // if GT: dest = op1 else: dest = op2
        AND,              // dest = op1 && op2
        OR,               // dest = op1 || op2
        SAL,              // dest = op1 << op2 算数左移
        SAR,              // dest = op1 >> op2 算数右移
        STORE,            // op1[op2] = op3
        LOAD,             // dest = op1[op2]
        RET,              // return / return op1
        LABEL,            // label:
        DATA_BEGIN,       //.data
        DATA_WORD,        //.word
        DATA_SPACE,       //.space 
        DATA_END,         // nothing
        FUNCTION_BEGIN,   // FUNCTION_BEGIN
        FUNCTION_END,     // FUNCTION_END
        PHI_MOV,          // PHI
        NOOP,             // no operation
        INFO,             // info for compiler
    }



    public OpCode op_code;
    public String label;
    public OpName op1, op2, op3, dest;
//    public ListIterator<IR> phi_block ;
//    public List<List<IR>> phi_block=new ArrayList<>();
    public IR phi_block;     // 指向一个List<IR>里面的第一条IR

    public IR()
    {

    }
    public IR(OpCode op_code, OpName dest, OpName op1, OpName op2, OpName op3,
              String label) {
        this.dest = dest;
        this.op1 = op1;
        this.op2 = op2;
        this.op3 = op3;
        this.label = label;
        this.op_code = op_code;
    }

    public IR(OpCode op_code, OpName dest, OpName op1, OpName op2,
              String label) {
        this.dest = dest;
        this.op1 = op1;
        this.op2 = op2;
        this.op3 = new OpName();
        this.label = label;
        this.op_code = op_code;
    }

    public IR(OpCode op_code, OpName dest, OpName op1, String label) {
        this.dest = dest;
        this.op1 = op1;
        this.op2 = new OpName();
        this.op3 = new OpName();
        this.label = label;
        this.op_code = op_code;
    }

    public IR(OpCode op_code, OpName dest, String label) {
        this.dest = dest;
        this.op1 = new OpName();
        this.op2 = new OpName();
        this.op3 = new OpName();
        this.label = label;
        this.op_code = op_code;
    }

    public IR(OpCode op_code, String label) {
        this.dest = new OpName();
        this.op1 = new OpName();
        this.op2 = new OpName();
        this.op3 = new OpName();
        this.label = label;
        this.op_code = op_code;

    }

    public void print(PrintStream out, boolean verbose){
        switch (this.op_code){
            case MALLOC_IN_STACK:
                out.print("MALLOC_IN_STACK");
                printSpace(16-"MALLOC_IN_STACK".length(),out);
                break;
            case MOV:
                out.print("MOV");
                printSpace(16-"MOV".length(),out);
                break;
            case ADD:
                out.print("ADD");
                printSpace(16-"ADD".length(),out);
                break;
            case SUB:
                out.print("SUB");
                printSpace(16-"SUB".length(),out);
                break;
            case IMUL:
                out.print("IMUL");
                printSpace(16-"IMUL".length(),out);
                break;
            case IDIV:
                out.print("IDIV");
                printSpace(16-"IDIV".length(),out);
                break;
            case MOD:
                out.print("MOD");
                printSpace(16-"MOD".length(),out);
                break;
            case SET_ARG:
                out.print("SET_ARG");
                printSpace(16-"SET_ARG".length(),out);
                break;
            case CALL:
                out.print("CALL");
                printSpace(16-"CALL".length(),out);
                break;
            case CMP:
                out.print("CMP");
                printSpace(16-"CMP".length(),out);
                break;
            case JMP:
                out.print("JMP");
                printSpace(16-"JMP".length(),out);
                break;
            case JEQ:
                out.print("JEQ");
                printSpace(16-"JEQ".length(),out);
                break;
            case JNE:
                out.print("JNE");
                printSpace(16-"JNE".length(),out);
                break;
            case JLE:
                out.print("JLE");
                printSpace(16-"JLE".length(),out);
                break;
            case JLT:
                out.print("JLT");
                printSpace(16-"JLT".length(),out);
                break;
            case JGE:
                out.print("JGE");
                printSpace(16-"JGE".length(),out);
                break;
            case JGT:
                out.print("JGT");
                printSpace(16-"JGT".length(),out);
                break;
            case MOVEQ:
                out.print("MOVEQ");
                printSpace(16-"MOVEQ".length(),out);
                break;
            case MOVNE:
                out.print("MOVNE");
                printSpace(16-"MOVNE".length(),out);
                break;
            case MOVLE:
                out.print("MOVLE");
                printSpace(16-"MOVLE".length(),out);
                break;
            case MOVLT:
                out.print("MOVLT");
                printSpace(16-"MOVLT".length(),out);
                break;
            case MOVGE:
                out.print("MOVGE");
                printSpace(16-"MOVGE".length(),out);
                break;
            case MOVGT:
                out.print("MOVGT");
                printSpace(16-"MOVGT".length(),out);
                break;
            case AND:
                out.print("AND");
                printSpace(16-"AND".length(),out);
                break;
            case OR:
                out.print("OR");
                printSpace(16-"OR".length(),out);
                break;
            case SAL:
                out.print("SAL");
                printSpace(16-"SAL".length(),out);
                break;
            case SAR:
                out.print("SAR");
                printSpace(16-"SAR".length(),out);
                break;
            case STORE:
                out.print("STORE");
                printSpace(16-"STORE".length(),out);
                break;
            case LOAD:
                out.print("LOAD");
                printSpace(16-"LOAD".length(),out);
                break;
            case RET:
                out.print("RET");
                printSpace(16-"RET".length(),out);
                break;
            case LABEL:
                out.print("LABEL");
                printSpace(16-"LABEL".length(),out);
                break;
            case DATA_BEGIN:
                out.print("DATA_BEGIN");
                printSpace(16-"DATA_BEGIN".length(),out);
                break;
            case DATA_SPACE:
                out.print("DATA_SPACE");
                printSpace(16-"DATA_SPACE".length(),out);
                break;
            case DATA_WORD:
                out.print("DATA_WORD");
                printSpace(16-"DATA_WORD".length(),out);
                break;
            case DATA_END:
                out.print("DATA_END");
                printSpace(16-"DATA_END".length(),out);
                break;
            case FUNCTION_BEGIN:
                out.print("FUNCTION_BEGIN");
                printSpace(16-"FUNCTION_BEGIN".length(),out);
                break;
            case FUNCTION_END:
                out.print("FUNCTION_END");
                printSpace(16-"FUNCTION_END".length(),out);
                break;
            case PHI_MOV:
                out.print("PHI_MOV");
                printSpace(16-"PHI_MOV".length(),out);
                break;
            case NOOP:
                out.print("NOOP");
                printSpace(16-"NOOP".length(),out);
                break;
            case INFO:
                out.print("INFO");
                printSpace(16-"INFO".length(),out);
                break;
        }
        F(this.dest,out);
        F(this.op1,out);
        F(this.op2,out);
        F(this.op3,out);
        out.println(this.label);

    }

    private static void printSpace(int n, PrintStream out){
        for(int i = 0;i<n;i++){
            out.print(" ");
        }
    }

    private static void F(OpName op, PrintStream out){
        if(op.type == OpName.Type.Imm){
            out.print(op.value+"\t");
        }
        else if(op.type == OpName.Type.Var){
            out.print(op.name+"\t");
        }
        else if(op.type == OpName.Type.Null){
            out.print("\t");
        }
    }
}


