package parser;

import java_cup.runtime.*;
import node.*;
      
%%

%class Lexer

%line
%column
%public

%cup

%{
    StringBuffer string = new StringBuffer();
    private Symbol symbol(int type) {
        return new Symbol(type, yyline, yycolumn);
    }
    
    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline, yycolumn, value);
    }
%}
   

LineTerminator = \r|\n|\r\n
WhiteSpace     = {LineTerminator} | [ \t\f]
TraditionalComment   = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EndOfLineComment     = "//" [^\r\n]* {LineTerminator}?

%state STRING

%%
   
<YYINITIAL> {
    /** comment */
   {TraditionalComment} { }
    {EndOfLineComment} {  }

    /** Keywords. */
    "if"            { return symbol(sym.IF, new Integer(sym.IF)); }
    "else"            { return symbol(sym.ELSE, new Integer(sym.ELSE)); }
    "while"            { return symbol(sym.WHILE, new Integer(sym.WHILE)); }
    "for"            { return symbol(sym.FOR, new Integer(sym.FOR)); }
    "break"            { return symbol(sym.BREAK, new Integer(sym.BREAK)); }
    "continue"            { return symbol(sym.CONTINUE, new Integer(sym.CONTINUE)); }
    "return"            { return symbol(sym.RETURN, new Integer(sym.RETURN)); }
    "const"            { return symbol(sym.CONST, new Integer(sym.CONST)); }
    "int"            { return symbol(sym.INT, new Integer(sym.INT)); }
    "void"            { return symbol(sym.VOID, new Integer(sym.VOID)); }
    [a-zA-Z_][a-zA-Z0-9_]*  { return symbol(sym.IDENTIFIER, new String(yytext())); }
    [0-9]+              {
                          String s = yytext();
                          if(s.startsWith("0")){
                                return symbol(sym.INTEGER_VALUE, Integer.parseUnsignedInt(s,8));
                          }else {
                              return symbol(sym.INTEGER_VALUE, Integer.parseUnsignedInt(s));
                              }
                        }
    ("0x"|"0X")[0-9a-fA-F]+    {String s = yytext(); return symbol(sym.INTEGER_VALUE, new Integer(Integer.parseUnsignedInt(s.replaceAll("^0[x|X]", ""), 16))); }

     \"                             { string.setLength(0); yybegin(STRING); }

    "="            { return symbol(sym.ASSIGN, new Integer(sym.ASSIGN)); }
    "=="            { return symbol(sym.EQ, new Integer(sym.EQ)); }
    "!="            { return symbol(sym.NE, new Integer(sym.NE)); }
    "<"            { return symbol(sym.LT, new Integer(sym.LT)); }
    "<="            { return symbol(sym.LE, new Integer(sym.LE)); }
    ">"            { return symbol(sym.GT, new Integer(sym.GT)); }
    ">="            { return symbol(sym.GE, new Integer(sym.GE)); }
    "&&"            { return symbol(sym.AND, new Integer(sym.AND)); }
    "||"            { return symbol(sym.OR, new Integer(sym.OR)); }
   
    "("            { return symbol(sym.LPAREN, new Integer(sym.LPAREN)); }
    ")"            { return symbol(sym.RPAREN, new Integer(sym.RPAREN)); }
    "["            { return symbol(sym.LSQUARE, new Integer(sym.LSQUARE)); }
    "]"            { return symbol(sym.RSQUARE, new Integer(sym.RSQUARE)); }
    "{"            { return symbol(sym.LBRACE, new Integer(sym.LBRACE)); }
    "}"            { return symbol(sym.RBRACE, new Integer(sym.RBRACE)); }

    "."            { return symbol(sym.DOT, new Integer(sym.DOT)); }
    ","            { return symbol(sym.COMMA, new Integer(sym.COMMA)); }
    ";"            { return symbol(sym.SEMI, new Integer(sym.SEMI)); }

    "++"            { return symbol(sym.PLUSPLUS, new Integer(sym.PLUSPLUS)); }
    "--"            { return symbol(sym.MINUSMINUS, new Integer(sym.MINUSMINUS)); }

    "+"            { return symbol(sym.PLUS, new Integer(sym.PLUS)); }
    "-"            { return symbol(sym.MINUS, new Integer(sym.MINUS)); }
    "*"            { return symbol(sym.MUL, new Integer(sym.MUL)); }
    "/"            { return symbol(sym.DIV, new Integer(sym.DIV)); }
    "%"            { return symbol(sym.MOD, new Integer(sym.MOD)); }
    "!"            { return symbol(sym.NOT, new Integer(sym.NOT)); }

    {WhiteSpace}        {}
    <<EOF>>         { return symbol(sym.EOF); }
    


/* error */ 
[^]                    { throw new Error("Illegal character <"+yytext()+">"); }
}

<STRING> {
         \"                             { yybegin(YYINITIAL);
                                               return symbol(sym.STRING_LITERAL,
                                               string.toString()); }
              [^\n\r\"\\]+                   { string.append( yytext() ); }
              \\t                            { string.append('\t'); }
              \\n                            { string.append('\n'); }

              \\r                            { string.append('\r'); }
              \\\"                           { string.append('\"'); }
              \\                             { string.append('\\'); }
}