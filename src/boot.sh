#!/bin/bash

#compile
cd ./parser;
jflex Lexer.flex;
java -cp .:java-cup-11b.jar java_cup.Main  < ycalc.cup;
cd ../;
javac -cp .:java-cup-11b.jar Main.java;


#run
dir=../test/

for file in $dir/*; do
    echo $file
    java -cp .:java-cup-11b-runtime.jar Main $file;
done

#java -cp .:java-cup-11b-runtime.jar Main ../test/006_arr_defn3.sy;


#clean
cd ./parser;
rm Lexer.java;
rm parser.java;
rm sym.java;
rm *.class;
cd ../
rm Main.class;
rm node/*.class;

