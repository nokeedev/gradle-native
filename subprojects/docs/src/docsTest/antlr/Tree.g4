grammar Tree;

@header {
package dev.nokee.docs.fixtures;
}

output: treeHeader Newline (entry)+ Newline summary;

treeHeader: RelativePath;
entry: level element Newline;
level: Indent+;
element: PathElement;
summary: DirectoryCount ', ' FileCount;

fragment PATH_SEPARATOR: '/';
fragment LOWERCASE: [a-z];
fragment UPPERCASE: [A-Z];
fragment DIRECTORIES: 'directories'|'directory';
fragment FILES: 'files'|'file';
fragment DIGIT: [0-9];

PathElement: (LOWERCASE | UPPERCASE | DIGIT | '_' | '.' | '-')+;

RelativePath: '.' (PATH_SEPARATOR PathElement)+;

Indent: ('└── ' | '│   ' | '├── ' | '    ');

DirectoryCount: DIGIT+ ' ' DIRECTORIES;
FileCount: DIGIT+ ' ' FILES;

Newline: ('\n' | '\r\n' | '\f');
