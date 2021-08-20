华中科技大学 DragonAC队作品
# Sysy
A compiler that translates Sysy into ARMv7a

[GitHub项目地址](https://github.com/showstarpro/sysy.git)

[Gitlab项目地址](https://gitlab.com/yux20000304/sysy.git)
## Build
cd ./parser;

jflex Lexer.flex;

java -cp .:java-cup-11b.jar java_cup.Main  < ycalc.cup;

cd ../;

javac -cp .:java-cup-11b.jar Main.java;


## Run
功能测试：compiler -S -o testcase.s testcase.sy

性能测试：compiler -S -o testcase.s testcase.sy -O2


