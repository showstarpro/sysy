package util;

import java.util.*;

public class Multimap<K,V>{
    Map<K, Vector<V>> map;

    public Multimap(){
        map = new TreeMap<>();
    }

    public void put(K key, V value){
        Vector<V> find = map.get(key);
        if(find == null){
            Vector<V> t = new Vector<>();
            t.addElement(value);
            map.put(key,t );
            return ;
        }else{
            find.addElement(value);
            return;
        }
    }

    public Vector<Map.Entry<K,V> >  entries()
    {
        Vector<Map.Entry<K,V>> ret = new Vector<>();
        K key;
        V value;
        // ret.addElement(new AbstractMap.SimpleEntry());

        for(Map.Entry<K, Vector<V>> i : map.entrySet()){
            key = i.getKey();
            Vector<V> vs = i.getValue();
            for(V j: vs){
                value = j;
                ret.addElement(new AbstractMap.SimpleEntry(key,value));
            }
        }

        return ret;
    }


    public static void main(String[] argv){
        Multimap<Integer, String> map = new Multimap<>();
        map.put(1,"1a");
        map.put(1,"1b");
        map.put(1,"1a");
        map.put(1,"1a");

        for(Map.Entry<Integer, String> i: map.entries()){
            System.out.println(i.getKey()+"    "+i.getValue());
        }

    }
}
