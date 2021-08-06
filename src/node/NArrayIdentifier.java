package node;

import ir.*;

import java.io.PrintStream;
import java.util.List;
import java.util.Vector;

public class NArrayIdentifier extends NIdentifier {
    public NIdentifier name;
    public Vector<NExpression> shape;

    public NArrayIdentifier() {
        shape = new Vector<>();
    }

    public NArrayIdentifier(NIdentifier name) {
        this();
        this.name = name;
    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation, false, out);
        out.println("ArrayIdentifier");
        name.print(indentation + 1, false, out);

        this.printIndentation(indentation + 1, true, out);
        out.println("Shape");
        for (NExpression i : shape) {
            i.print(indentation + 2, shape.indexOf(i) == shape.size() - 1, out);
        }
    }

    public int eval(ContextIR ctx) throws Exception {
        ConstInfo v = ctx.find_const(this.name.name);
        if (v.is_array) {
            if (this.shape.size() == v.shape.size()) {
                int index = 0, size = 1;
                // todo 可能发生错误的地方
                for (int i = this.shape.size() + 1; i >= 0; i++) {
                    index += this.shape.get(i).eval(ctx) * size;
                    size *= v.shape.get(i);
                }
                return v.value.get(index);
            } else {
                throw new Exception(this.name.name + "'s shape unmatch.");
            }
        } else {
            throw new Exception(this.name.name + " is not a array.");
        }

    }

    //
    public OpName eval_runtime(ContextIR ctx, List<IR> ir) throws Exception {
        VarInfo v = ctx.find_symbol(this.name.name);
        if (v.is_array) {
            if (this.shape.size() == v.shape.size()) {    //  value

//                int offset = 1;
//
//                OpName index = new OpName("%" + ctx.get_id());
//                ir.add(new IR(IR.OpCode.MOV, index, new OpName(0), ""));
//                for (int i = this.shape.size() - 1; i >= 0; i--) {
//                    OpName tmp = new OpName("%" + ctx.get_id());
//                    OpName tmp2 = new OpName("%" + ctx.get_id());
//                    ir.add(new IR(IR.OpCode.IMUL, tmp, new OpName(offset), this.shape.get(i).eval_runtime(ctx, ir), ""));
//                    ir.add(new IR(IR.OpCode.ADD, tmp2, index, tmp, ""));
//                    index = tmp2;
//                    offset *= v.shape.get(i);
//                }
//                OpName tmp3 = new OpName("%" + ctx.get_id());
//                OpName dest = new OpName("%" + ctx.get_id());
//                ir.add(new IR(IR.OpCode.SAL, tmp3, index, new OpName(2), ""));
//                ir.add(new IR(IR.OpCode.LOAD, dest, new OpName(v.name), tmp3, ""));
//                return dest;
                OpName dest = new OpName("%" + ctx.get_id());
                OpName index = new OpName("%"+ctx.get_id());
                OpName size = new OpName("%"+ctx.get_id());
                ir.add(new IR( IR.OpCode.SAL, index,
                        this.shape.get(this.shape.size()-1).eval_runtime(ctx, ir)
                , new OpName(2),""));
                if(this.shape.size() !=1){
                    OpName tmp = new OpName("%"+ctx.get_id());
                    ir.add(new IR(IR.OpCode.MOV, size,
                            new OpName(4* v.shape.get(this.shape.size()-1))
                            ,""));
                }
                for(int i = this.shape.size()-2; i>=0;i--){
                    OpName tmp = new OpName("%"+ctx.get_id());
                    OpName tmp2 = new OpName("%"+ctx.get_id());
                    ir.add(new IR(IR.OpCode.IMUL , tmp, size, this.shape.get(i).eval_runtime(ctx, ir),""));
                    ir.add(new IR(IR.OpCode.ADD, tmp2, index, tmp,""));
                    index = tmp2;
                    if (i != 0) {
                        OpName tmp_ = new OpName("%"+ctx.get_id());
                        ir.add(new IR(IR.OpCode.IMUL, tmp_, size, new OpName(v.shape.get(i)),""));
                        size = tmp_;
                    }
                }
                ir.add(new IR(IR.OpCode.LOAD, dest, new OpName(v.name), index,""));
                return dest;



            } else if (this.shape.size() < v.shape.size()) {  // address
                int offset = 1;
                for (int i = this.shape.size(); i < v.shape.size(); i++) {
                    offset *= v.shape.get(i);
                }

                OpName index = new OpName("%" + ctx.get_id());
                ir.add(new IR(IR.OpCode.MOV, index, new OpName(0), ""));
                for (int i = this.shape.size() - 1; i >= 0; i--) {
                    OpName tmp = new OpName("%" + ctx.get_id());
                    OpName tmp2 = new OpName("%" + ctx.get_id());
                    ir.add(new IR(IR.OpCode.IMUL, tmp, new OpName(offset), this.shape.get(i).eval_runtime(ctx, ir), ""));
                    ir.add(new IR(IR.OpCode.ADD, tmp2, index, tmp, ""));
                    index = tmp2;
                    offset *= v.shape.get(i);
                }
                OpName tmp3 = new OpName("%" + ctx.get_id());
                OpName dest = new OpName("%" + ctx.get_id());
                ir.add(new IR(IR.OpCode.SAL, tmp3, index, new OpName(2), ""));
                ir.add(new IR(IR.OpCode.ADD, dest, tmp3, new OpName(v.name), ""));
//                ir.add(new IR(IR.OpCode.LOAD, dest, new OpName(v.name), tmp3, ""));
                return dest;
            } else {
                throw new Exception(this.name.name + "'s shape unmatch.");
            }
        } else {
            throw new Exception(this.name.name + " is not a array.");
        }
    }


    public void store_runntime(OpName value, ContextIR ctx, List<IR> ir) throws Exception {
        VarInfo v = ctx.find_symbol(this.name.name);
        if (v.is_array) {
            if (this.shape.size() == v.shape.size()) {
                OpName index = new OpName("%" + ctx.get_id());
                OpName size = new OpName("%" + ctx.get_id());
                ir.add(new IR(IR.OpCode.SAL, index, this.shape.get(this.shape.size() - 1).eval_runtime(ctx, ir), new OpName(2), ""));
                if (this.shape.size() != 1) {
                    OpName tmp = new OpName("%" + ctx.get_id());
                    ir.add(new IR(IR.OpCode.MOV, size, new OpName(4 * v.shape.get(this.shape.size() - 1)), ""));
                }
                for (int i = this.shape.size() - 2; i >= 0; i--) {
                    OpName tmp = new OpName("%" + ctx.get_id());
                    OpName tmp2 = new OpName("%" + ctx.get_id());
                    ir.add(new IR(IR.OpCode.IMUL, tmp, size, this.shape.get(i).eval_runtime(ctx, ir), ""));
                    ir.add(new IR(IR.OpCode.ADD, tmp2, index, tmp, ""));
                    index = tmp2;
                    if (i != 0) {
                        OpName tmp3 = new OpName("%" + ctx.get_id());
                        ir.add(new IR(IR.OpCode.IMUL, tmp3, size, new OpName(v.shape.get(i)), ""));
                        size = tmp3;
                    }
                }
                ir.add(new IR(IR.OpCode.STORE, new OpName(), new OpName(v.name), index, value, ""));
            } else {
                throw new Exception(this.name.name + "'s shape unmatch'. ");
            }
        } else {
            throw new Exception(this.name.name + "is not a array. ");
        }
    }
}
