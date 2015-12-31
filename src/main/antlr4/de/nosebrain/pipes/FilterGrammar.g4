grammar FilterGrammar;

LETTER : [a-zA-Z] ;
DIGIT  : [0-9] ;
SPACE  : ' ' ;
QUOTE : '"' ;

NEQ    : '!=' ;
EQ     : '==' ;
LIKE   : '~'  ;

AND    : '&&' ;
OR     : '||' ;

LPAREN : '(' ;
RPAREN : ')' ;

WS : [\r\t\n]+ -> channel(HIDDEN) ;

operator      : NEQ | EQ | LIKE ;
bool_op       : OR | AND ;
variable      : LETTER+ ;
/** FIXME: allow all chars*/
operand       : (LETTER|SPACE|DIGIT|'ä'|'ü'|'ö'|'-')+? ;

expression    : factor (SPACE? bool_op SPACE? factor)* ;

factor        : (variable SPACE? operator SPACE? QUOTE operand QUOTE) | (LPAREN expression RPAREN);