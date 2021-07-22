package util;


public class Pair {
    public Integer first;
    public String second;
    public Pair(){
    }
    public Pair(Integer first, String second){
        this.first = first;
        this.second = second;
    }
    public Integer getFirst() {
        return first;
    }
    public void setFirst(Integer first) {
        this.first = first;
    }
    public String getSecond() {
        return second;
    }
    public void setSecond(String second) {
        this.second = second;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Pair newobj = new Pair();
        newobj.first = this.first;
        newobj.second = new String( this.second);
        return newobj;
    }
}
