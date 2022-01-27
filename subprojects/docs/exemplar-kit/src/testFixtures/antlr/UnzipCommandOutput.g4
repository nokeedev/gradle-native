grammar UnzipCommandOutput;

output: unzipHeader (action)+;

unzipHeader
	: 'Archive:' Space Path Newline
	;

action
    : Space actionType Space* Newline?
    ;

actionType
	: createAction
	| inflateAction
	| extractAction
  	;

createAction
	: 'creating' ':' Space Path
	;

inflateAction
	: 'inflating' ':' Space Path
	;

extractAction
	: 'extracting' ':' Space Path
	;

Path
	: PathSeparator (Alnum | Other | PathSeparator)+ (Alnum | PathEnding | PathSeparator)
	;

fragment PathSeparator
	: '/'
	;
fragment Alnum
	: [a-zA-Z0-9]
	;
fragment Other
	: [ \-._]
	;
fragment PathEnding
	: [-._]
	;

Space
    : (' ' | '\t')+
    ;

Newline
    : ('\n' | '\r\n' | '\f')
    ;


