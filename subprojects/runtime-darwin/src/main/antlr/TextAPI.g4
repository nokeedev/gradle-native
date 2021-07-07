// TBD file grammar. It's only an approximation to unblock cross-compilation between x86-64 and arm64.
grammar TextAPI;

@header {
package dev.nokee.runtime.darwin.internal.parsers;
}

tbd: COMMENT header;

header: (tbdVersion | targets | uuids | installName | currentVersion | compatibilityVersion | reexportedLibraries)+;

tbdVersion: 'tbd-version:' number;
targets: 'targets:' array;
uuids: 'uuids:' uuidEntry+;

uuidEntry: '-' uuidTarget uuidValue;
uuidTarget: 'target:' string;
uuidValue: 'value:' string;

installName: 'install-name:' string; // maybe more like path???

currentVersion: 'current-version:' version;

compatibilityVersion: 'compatibility-version:' number;

reexportedLibraries: 'reexported-libraries:' reexportedLibrariesEntry+;
reexportedLibrariesEntry: '-' reexportedLibrariesTargets reexportedLibrariesLibraries;
reexportedLibrariesTargets: 'targets:' array;
reexportedLibrariesLibraries: 'libraries:' array;

version: VERSION;

START_ARRAY: '[';
ARRAY_COMMA: ',' ' '?;
END_ARRAY: ']';

array: START_ARRAY (value ARRAY_COMMA?)+ END_ARRAY;

string: QUOTED_STRING | STRING;
number: NUMBER;

value: array | string | number;


QUOTED_STRING: '\'' .*? '\'';
NUMBER: [0-9]+;
STRING: [a-zA-Z0-9_\-]+;
fragment DIGIT: [0-9];
fragment DOT: '.';

VERSION: DIGIT+ (DOT DIGIT+)*;

fragment WS: ' ' | '\t';
fragment COMMENT_CHAR: '---';
COMMENT: COMMENT_CHAR (WS | [!a-zA-Z\-])+;
IDENTIFIER: [a-zA-Z\-]+;

fragment CR : '\r';
fragment LF : '\n';
EOL : CR?LF ->skip;

OTHER: . -> skip;
