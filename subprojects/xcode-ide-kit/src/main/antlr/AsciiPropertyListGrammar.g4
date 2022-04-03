grammar AsciiPropertyListGrammar;

@header {
package dev.nokee.xcode.internal;
}

document: value;

// Note: date, real, integer and boolean are considered as string
// Apple mention that "numbers are handled as strings" (see https://developer.apple.com/library/archive/documentation/Cocoa/Conceptual/PropertyLists/OldStylePlists/OldStylePLists.html#//apple_ref/doc/uid/20001012-BBCBDBJE)
// Nokee team expanded the mention and will also treat date, real, integer, and boolean as strings.
// There is no example of those type used in the old-style ASCII property lists.
value: dict | string | array;

dict: '{' dictKeyValuePair* '}';
dictKeyValuePair: dictKey '=' value ';';
dictKey: STRING COMMENT?;

array: '(' arrayElementList* ')';
arrayElementList: value (',' value)* ','?;

// TODO: Add support for data field

string: STRING | QUOTED_STRING;
STRING: (AlphaLetter | Underscore | Digit)+;
QUOTED_STRING: '"' .*? '"';
fragment Underscore: '_';
fragment AlphaLetter: [a-zA-Z];
fragment Digit: [0-9];

// We ignore comments as we don't have a need to understand them
COMMENT: '/*' .*? '*/' -> skip;
LINE_COMMENT: '//' ~[\r\n]* -> skip;

OTHER: . -> skip;
