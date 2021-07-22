package node;

import ir.ContextIR;
import ir.IR;
import ir.OpName;
import ir.VarInfo;

import java.io.PrintStream;
import java.util.List;
import java.util.Vector;

public class NAssignment extends NStatement {
    public NIdentifier lhs;
    public NExpression rhs;

    public NAssignment() {

    }

    public NAssignment(NIdentifier lhs, NExpression rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public void print(int indentation, boolean end,PrintStream out) {
        this.printIndentation(indentation, end,out);
        out.println("Assignment");
        lhs.print(indentation+1, false,out);
        rhs.print(indentation+1, true,out);
    }

    public OpName eval_runtime(ContextIR ctx, List<IR> ir) throws Exception{
        this.generate_ir(ctx,ir);
        if(this.lhs instanceof NArrayIdentifier){
            assert ir.get(ir.size()-1).op_code== IR.OpCode.STORE;
            return ir.get(ir.size()-1).op3;
        }
        else{
            assert ir.get(ir.size()-1).dest.type==OpName.Type.Var;
            return ir.get(ir.size()-1).dest;
        }
    }

    public void generate_ir(ContextIR ctx,List<IR> ir) throws Exception{
        if(this.lhs instanceof NArrayIdentifier){
            OpName rhs=this.rhs.eval_runtime(ctx,ir);
            NArrayIdentifier tmp=(NArrayIdentifier) this.lhs;
            tmp.store_runntime(rhs,ctx,ir);
        }
        else{
            OpName rhs=this.rhs.eval_runtime(ctx,ir);
            VarInfo v=ctx.find_symbol(this.lhs.name);
            if(v.is_array){
                throw new Exception("Can't assign to a array. ");
            }else{
                if(rhs.type==OpName.Type.Var&&rhs.name.charAt(0)=='%'&&
                        (v.name.charAt(0)=='%'|| v.name.startsWith("$arg"))&&
                        v.name.charAt(0)!='@'){
                    if(ctx.in_loop()){
                        boolean lhs_is_loop_var =false,rhs_is_loop_var=false;
                        int lhs_level=-1,rhs_level=-1;
                        for(String i: ctx.loop_var.peek()){
                                if(i.equals(v.name))
                                    lhs_is_loop_var=true;
                                if(i.equals(rhs.name))
                                    rhs_is_loop_var=true;
                        }
                        if(lhs_is_loop_var&&rhs_is_loop_var){
                            v.name="%"+ctx.get_id();
                            ir.add(new IR(IR.OpCode.MOV,new OpName(v.name),rhs,""));
                        }else{
                            v.name=rhs.name;
                        }
                    }else{
                        v.name=rhs.name;
                    }
                }
                else if(v.name.charAt(0)=='@')
                    ir.add(new IR(IR.OpCode.MOV,new OpName(v.name),rhs,""));
                else{
                    v.name="%"+ctx.get_id();
                    ir.add(new IR(IR.OpCode.MOV,new OpName(v.name),rhs,""));
                    //忽略optimize
                }
            }
        }
    }
}
