grammar demo;

// TODO check if anything can be simplified;
// TODO branching

// parser rules - order does not matter
model: (line)+;

line: assignment
    | assertion
    ;

assignment: VarName ':=' (expression|value); // why do we have value instead of Const?

assertion: 'assert' comparison; // I could make a lexer rule for assert as well - both works

expression: (VarName|Const) BinOp (VarName|Const); // how to modify, so that we can have nested expressions?

// the fact that it is parseable does not mean it is semantically correct/meaningful!
// e.g., here: we do not check if varName is anywhere else
comparison: VarName Equal Const;

value: Const|'Input';

// lexer rules - order does matter! How are they differentiated from
VarName: Letter(Letter|Digit|Underscore)*; // * vs ? vs + (like regex)

Equal: '==';

// could we make this to be a parser rule instead? Any parser rules that could be made into a parser rule instead?
BinOp : Plus | Minus;

Plus : '+';
Minus : '-';

Underscore : '_';

Const: (Minus)?(Digit)+;
Letter: [a-z];
Digit: [0-9];