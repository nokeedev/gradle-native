grammar TreeCommandOutput;

output: treeHeader Newline (entry)+ Newline summary;

treeHeader: RelativePath;
entry: level element Newline;
level: Indent+;
element: PathElement;
summary: DirectoryCount ', ' FileCount;

fragment DOT: '.';
fragment PATH_SEPARATOR: '/';
fragment LOWERCASE: [a-z];
fragment UPPERCASE: [A-Z];
fragment DIRECTORIES: 'directories'|'directory';
fragment FILES: 'files'|'file';
fragment DIGIT: [0-9];

RelativePath: DOT (PATH_SEPARATOR PathElement)*;

PathElement: (LOWERCASE | UPPERCASE | DIGIT | '_' | DOT | '-')+;

fragment LAST_ENTRY_IN_TREE_BRANCH: ('\u2514\u2500\u2500 ' | '`-- ');
fragment ENTRY_IN_TREE_BRANCH: ('\u251c\u2500\u2500 ' | '|-- ');
fragment PASSTHROUGH_IN_TREE_BRANCH: ('\u2502\u00a0\u00a0 ' | '|   ');
fragment SPACING_IN_TREE_BRACNH: '    ';
Indent: (ENTRY_IN_TREE_BRANCH | LAST_ENTRY_IN_TREE_BRANCH | PASSTHROUGH_IN_TREE_BRANCH | SPACING_IN_TREE_BRACNH);

DirectoryCount: DIGIT+ ' ' DIRECTORIES;
FileCount: DIGIT+ ' ' FILES;

Newline: ('\n' | '\r\n' | '\f');
