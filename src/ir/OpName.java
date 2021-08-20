package ir;

public class OpName {
    public enum Type {
        Var,
        Imm,
        Null
    }


    public Type type;
    public String name;
    public int value;

    public OpName clone(){
        OpName temp=new OpName();
        if(this.type!=null)
            temp.type=this.type;
        else
            temp.type=null;
        if(this.name!=null)
            temp.name=this.name;
        else
            temp.name=null;
            temp.value= this.value;
        return temp;
    }

    public OpName() {
        this.type = Type.Null;
    }

    public OpName(String name) {
        this.type = Type.Var;
        this.name = name;
    }

    public OpName(int value) {
        this.type = Type.Imm;
        this.value = value;
    }

    public boolean is_var(){
        return this.type== Type.Var;
    }

    public boolean is_local_var(){
        return this.is_var() &&
                (this.name.startsWith("%") || this.name.startsWith("$arg"));
    }

    public boolean is_global_var(){
        return this.is_var() && this.name.startsWith("@");
    }


}
