package node;

import ir.ContextIR;
import ir.IR;
import ir.OpName;
import ir.VarInfo;

import java.io.PrintStream;
import java.util.List;
import java.util.Vector;

public class NArrayDeclareWithInit extends NDeclare {
    public NArrayIdentifier name;
    public NArrayDeclareInitValue value;
    public boolean is_const;

    public NArrayDeclareWithInit() {
    }

    public NArrayDeclareWithInit(NArrayIdentifier name, NArrayDeclareInitValue value, boolean is_const) {
        this.name = name;
        this.value = value;
        this.is_const = is_const;
    }

    public void print(int indentation, boolean end, PrintStream out) {
        this.printIndentation(indentation, end, out);
        out.println("ArrayDeclareWithInit");
        name.print(indentation + 1, false, out);
        value.print(indentation + 1, true, out);
    }

    @Override
    public void generate_ir(ContextIR ctx, List<IR> ir) throws Exception {
        Vector<Integer> shape = new Vector<>();
        for (NExpression i : this.name.shape) {
            shape.addElement(i.eval(ctx));
        }
        int size = 1;
        for (Integer i : shape) {
            size *= i;
        }
        // init value
        Vector<Integer> init_value = new Vector<>();
        if (ctx.is_global()) {
            ir.add(new IR(IR.OpCode.DATA_BEGIN, "@&" + this.name.name.name));
            //分析初始化值
            ArrayDeclareWithInit(this.value.value_list, this.name.shape,init_value, 0,ctx,ir);
            ir.add(new IR(IR.OpCode.DATA_END,""));
            ctx.insert_symbol(this.name.name.name,
                    new VarInfo("@&"+this.name.name.name,true,shape));
        }else {
            ctx.insert_symbol(this.name.name.name,
                    new VarInfo("%&"+ctx.get_id(),true,shape));
            ir.add(new IR(IR.OpCode.MALLOC_IN_STACK,
            new OpName(ctx.find_symbol(this.name.name.name).name),
            new OpName(size*4),""));
            ir.add(new IR(IR.OpCode.SET_ARG, new OpName(0),
                    new OpName(ctx.find_symbol(this.name.name.name).name),
                    ""));
            ir.add(new IR(IR.OpCode.SET_ARG,new OpName(1),
                    new OpName(0),""));
            ir.add(new IR(IR.OpCode.SET_ARG, new OpName(2),
                    new OpName(size*4),""));
            ir.add(new IR(IR.OpCode.CALL, "memset"));
            ArrayDeclareWithInit(this.value.value_list, this.name.shape, init_value,0,ctx,ir);
        }

    }

    /**
     * 分析初始化值， 用参数带回
     */
    private void ArrayDeclareWithInit(
            Vector<NArrayDeclareInitValue> v,
            Vector<NExpression> shape,
            Vector<Integer> init_value,         // 此参数带回初始化值
            int index,
            ContextIR ctx, List<IR> ir
    ) throws Exception {
        if (index >= shape.size()) return;
        int size = 1;
        int write_size = 0;
        for(int i = index;i<shape.size();i++){
            size *= shape.get(i).eval(ctx);
        }
        int size_this_shape = size / shape.get(index).eval(ctx);
        for (NArrayDeclareInitValue i : v) {
            if (i.is_number) {
                write_size++;
                try {
                    if (this.is_const) {
                        output_const(i.value.eval(ctx), false, init_value, ctx, ir);
                    } else {
                        output(new OpName(i.value.eval(ctx)), false, init_value, ctx, ir);
                    }
                } catch (Exception e) {
                    output(i.value.eval_runtime(ctx, ir), false, init_value, ctx, ir);
                }
            } else {
                if (write_size % size_this_shape != 0) {
                    if (this.is_const) {
                        output_const(size_this_shape - (write_size % size_this_shape), true, init_value, ctx, ir);
                    } else {
                        output(new OpName(size_this_shape - (write_size % size_this_shape)), true, init_value, ctx, ir);
                    }
                }
                ArrayDeclareWithInit(i.value_list, shape, init_value, index + 1, ctx, ir);
                write_size += size_this_shape;
            }
        }
        if (this.is_const) {
            output_const(size - write_size, true, init_value, ctx, ir);
        } else {
            output(new OpName(size - write_size), true, init_value, ctx, ir);
        }
    }

    private void output_const(int value, boolean is_space, Vector<Integer> init_value, ContextIR ctx, List<IR> ir) throws Exception {
        if (is_space) {
            if (ctx.is_global()) {
                for (int i = 0; i < value; i++) {
                    init_value.add(0);
                }
                ir.add(new IR(IR.OpCode.DATA_SPACE, new OpName(value * 4), ""));
            } else {
                for (int i = 0; i < value; i++) {
                    init_value.add(0);
                }
            }
        } else {
            init_value.add(value);
            if (ctx.is_global()) {
                ir.add(new IR(IR.OpCode.DATA_WORD, new OpName(value), ""));
            } else {
                ir.add(new IR(
                        IR.OpCode.STORE,
                        new OpName(),
                        new OpName(ctx.find_symbol(this.name.name.name).name),
                        new OpName(init_value.size() * 4 - 4),
                        new OpName(value), ""));
            }
        }
    }

    private void output(OpName value, boolean is_space, Vector<Integer> init_value, ContextIR ctx, List<IR> ir) throws Exception {
        if (is_space) {
            if (ctx.is_global()) {
                for (int i = 0; i < value.value; i++) init_value.add(0);
                ir.add(new IR(IR.OpCode.DATA_SPACE, new OpName(value.value * 4), ""));
            } else {
                for (int i = 0; i < value.value; i++) init_value.add(0);
            }
        } else {
            init_value.add(0);
            if (ctx.is_global())
                ir.add(new IR(IR.OpCode.DATA_WORD, value, ""));
            else
                ir.add(new IR(
                        IR.OpCode.STORE,
                        new OpName(),
                        new OpName(ctx.find_symbol(this.name.name.name).name),
                        new OpName(init_value.size() * 4 - 4),
                        value,
                        ""
                ));
        }
    }
}
