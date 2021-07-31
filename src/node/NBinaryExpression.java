package node;

import ir.ContextIR;
import ir.IR;
import ir.OpName;
import parser.*;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static parser.sym.*;

public class NBinaryExpression extends NExpression {
    public int op;
    public NExpression lhs, rhs;

    public NBinaryExpression() {

    }

    public NBinaryExpression(NExpression lhs, int op, NExpression rhs) {
        this.lhs = lhs;
        this.op = op;
        this.rhs = rhs;
    }

    public void print(int indentation, boolean end,PrintStream out) {
        this.printIndentation(indentation, end,out);
        out.print("BinaryExpression OP: ");
        out.println(this.op);
        this.lhs.print(indentation+1, end,out);
        this.rhs.print(indentation+1, end,out);
    }

    public int eval(ContextIR ctx) throws Exception {
        switch (this.op)
        {
            case sym.PLUS:
                return lhs.eval(ctx)+ rhs.eval(ctx);


            case sym.MINUS:
                return lhs.eval(ctx) - rhs.eval(ctx);

            case MUL:
                return lhs.eval(ctx) * rhs.eval(ctx);


            case sym.DIV:
                return lhs.eval(ctx) / rhs.eval(ctx);

            case sym.MOD:
                return lhs.eval(ctx) % rhs.eval(ctx);


            case sym.EQ:
                return lhs.eval(ctx) == rhs.eval(ctx)? 1:0;


            case sym.NE:
                return lhs.eval(ctx) != rhs.eval(ctx)? 1:0;

            case sym.GT:
                return lhs.eval(ctx) > rhs.eval(ctx)?1:0;

            case sym.GE:
                return lhs.eval(ctx) >= lhs.eval(ctx) ?1:0;

            case sym.LT:
                return lhs.eval(ctx) < rhs.eval(ctx)?1:0;

            case sym.LE:
                return lhs.eval(ctx)<= rhs.eval(ctx)?1:0;

            case sym.AND:
                return (lhs.eval(ctx) != 0)&& (rhs.eval(ctx)!=0)?1:0;

            case sym.OR:
                return (lhs.eval(ctx)!=0) ||(rhs.eval(ctx)!=0) ?1:0;

            default:
                throw new Exception("Unknow OP");


        }
    }

    public int log2(int a)
    {
        int ans=-1;
        while (a>0)
        {
            ans++;
            a/=2;
        }
        return ans;
    }

    public OpName eval_runtime(ContextIR ctx, List<IR> ir) throws Exception {
        OpName dest =new OpName("%"+ctx.get_id());
        OpName lhs=new OpName() ,rhs=new OpName();

        if(this.op!=sym.AND&&this.op!=sym.OR)
        {
            lhs=this.lhs.eval_runtime(ctx,ir);
            rhs=this.rhs.eval_runtime(ctx,ir);
        }
        switch (this.op)
        {
            case sym.PLUS:
                ir.add(new IR(IR.OpCode.ADD,dest,lhs,rhs,""));
                break;
            case sym.MINUS:
                ir.add(new IR(IR.OpCode.SUB,dest,lhs,rhs,""));
                break;

            case MUL:
                ir.add(new IR(IR.OpCode.IMUL,dest,lhs,rhs,""));
                break;
            case DIV:
                ir.add(new IR(IR.OpCode.IDIV,dest,lhs,rhs,""));
                break;
            case MOD:
                ir.add(new IR(IR.OpCode.MOD,dest,lhs,rhs,""));
                break;
            case EQ:
                ir.add(new IR(IR.OpCode.CMP,new OpName(),lhs,rhs,""));
                ir.add(new IR(IR.OpCode.MOVEQ,dest,new OpName(1),new OpName(0), ""));
                break;
            case NE:
                ir.add(new IR(IR.OpCode.CMP,new OpName(),lhs,rhs,""));
                ir.add(new IR(IR.OpCode.MOVNE,dest,new OpName(1),new OpName(0), ""));
                break;

            case GT:
                ir.add(new IR(IR.OpCode.CMP,new OpName(),lhs,rhs,""));
                ir.add(new IR(IR.OpCode.MOVGT,dest,new OpName(1),new OpName(0), ""));
                break;

            case GE:
                ir.add(new IR(IR.OpCode.CMP,new OpName(),lhs,rhs,""));
                ir.add(new IR(IR.OpCode.MOVGE,dest,new OpName(1),new OpName(0), ""));
                break;
            case LT:
                ir.add(new IR(IR.OpCode.CMP,new OpName(),lhs,rhs,""));
                ir.add(new IR(IR.OpCode.MOVLT,dest,new OpName(1),new OpName(0), ""));
                break;
            case LE:
                ir.add(new IR(IR.OpCode.CMP,new OpName(),lhs,rhs,""));
                ir.add(new IR(IR.OpCode.MOVLE,dest,new OpName(1),new OpName(0), ""));
                break;
            case AND:
                String label=new String("COND"+ctx.get_id()+"_end");
                List<IR> end = new ArrayList<>();
                end.add(new IR(IR.OpCode.LABEL,label));
                CondResult lhs1= this.lhs.eval_cond_runntime(ctx,ir);
                ir.add(new IR(IR.OpCode.PHI_MOV,dest,new OpName(0),""));
//                ir.get(ir.size()-1).phi_block.add(end);//ir.back().phi_block = end.begin();可能有问题
                ir.get(ir.size()-1).phi_block = end.get(0);
                ir.add(new IR(lhs1.else_op,label));
                OpName rhs1=this.rhs.eval_runtime(ctx,ir);
                ir.add(new IR(IR.OpCode.PHI_MOV,dest,rhs1,""));
//                ir.get(ir.size()-1).phi_block.add(end);
                ir.get(ir.size()-1).phi_block = end.get(0);
                ir.addAll(end);
                break;

            case OR:
                String label1=new String("COND"+ctx.get_id()+"_end");
                List<IR> end1=new ArrayList<>();
                end1.add(new IR(IR.OpCode.LABEL,label1));
                CondResult lhs2=this.lhs.eval_cond_runntime(ctx,ir);
                ir.add(new IR(IR.OpCode.PHI_MOV,dest,new OpName(1),""));
//                ir.get(ir.size()-1).phi_block.add(end1);
                ir.get(ir.size()-1).phi_block = end1.get(0);
                ir.add(new IR(lhs2.then_op,label1));
                OpName rhs2=this.rhs.eval_runtime(ctx,ir);
                ir.add(new IR(IR.OpCode.PHI_MOV,dest,rhs2,""));
//                ir.get(ir.size()-1).phi_block.add(end1);
                ir.get(ir.size()-1).phi_block = end1.get(0);
                ir.addAll(end1);
                break;

            default:
                throw new Exception("Unknow OP");

        }
        return dest;
    }


    public CondResult eval_cond_runntime(ContextIR ctx,List<IR> ir) throws Exception {
        CondResult ret = new CondResult();
        OpName lhs = new OpName(),rhs = new OpName();
        if(this.op!=AND&&this.op!=OR)
        {
            lhs=this.lhs.eval_runtime(ctx, ir);
            rhs=this.rhs.eval_runtime(ctx, ir);
        }
        switch (this.op)
        {
            case EQ:
                ir.add(new IR(IR.OpCode.CMP,new OpName(),lhs,rhs,""));
                ret.then_op=IR.OpCode.JEQ;
                ret.else_op=IR.OpCode.JNE;
                break;
            case NE:
                ir.add(new IR(IR.OpCode.CMP,new OpName(),lhs,rhs,""));
                ret.then_op=IR.OpCode.JNE;
                ret.else_op=IR.OpCode.JEQ;
                break;
            case GT:
                ir.add(new IR(IR.OpCode.CMP,new OpName(),lhs,rhs,""));
                ret.then_op=IR.OpCode.JGT;
                ret.else_op=IR.OpCode.JLE;
                break;
            case GE:
                ir.add(new IR(IR.OpCode.CMP,new OpName(),lhs,rhs,""));
                ret.then_op=IR.OpCode.JGE;
                ret.else_op=IR.OpCode.JLT;
                break;
            case LT:
                ir.add(new IR(IR.OpCode.CMP,new OpName(),lhs,rhs,""));
                ret.then_op=IR.OpCode.JLT;
                ret.else_op=IR.OpCode.JGE;
                break;
            case LE:
                ir.add(new IR(IR.OpCode.CMP,new OpName(),lhs,rhs,""));
                ret.then_op=IR.OpCode.JLE;
                ret.else_op=IR.OpCode.JGT;
                break;
            default:
                ir.add(new IR(IR.OpCode.CMP,new OpName(),this.eval_runtime(ctx, ir),new OpName(0),""));
                ret.then_op=IR.OpCode.JNE;
                ret.else_op=IR.OpCode.JEQ;
                break;
        }
        return ret;
    }



}
