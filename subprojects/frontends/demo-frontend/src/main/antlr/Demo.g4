grammar Demo;

// TODO add rules for branching

// parser rules - order does not matter
model: (assignment)* assertion;

assignment: VarName ':=' (expression|value); // why do we have value instead of Const?

assertion: 'assert' comparison; // I could make a lexer rule for assert as well - both works

expression: (VarName|Const) BinOp (VarName|Const); // how to modify, so that we can have nested expressions?

// the fact that it is parseable does not mean it is semantically correct/meaningful!
// e.g., here: we do not check if varName is anywhere else
comparison: VarName ComparisonOp Const;

value: Const|'input';

// lexer rules - order does matter! How are they differentiated from
VarName: Letter(Letter|Digit|Underscore)*; // * vs ? vs + (like regex)

ComparisonOp: Equal | Less | Greater;
Equal: '==';
Less : '<';
Greater : '>';

// could we make this to be a parser rule instead? Any parser rules that could be made into a parser rule instead?
BinOp : Plus | Minus;

Plus : '+';
Minus : '-';

Underscore : '_';

Const: (Minus)?(Digit)+;
Letter: [a-z];
Digit: [0-9];

Whitespace
    :   [ \t]+
        -> skip
    ;

Newline
    :   (   '\r' '\n'?
        |   '\n'
        )
        -> skip
    ;