grammar Jar;

@header {
package dev.nokee.docs.fixtures;
}

output: entry+;

entry: Newline? path;
path: RelativePath;

fragment PATH_SEPARATOR: '/';
fragment LOWERCASE: [a-z];
fragment UPPERCASE: [A-Z];
fragment DIGIT: [0-9];

PathElement: (LOWERCASE | UPPERCASE | DIGIT | '_' | '.' | '-' | '$')+;

RelativePath: (PathElement PATH_SEPARATOR)+ PathElement?;

Newline: ('\n' | '\r\n' | '\f') -> skip;
