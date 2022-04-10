grammar AsciiPropertyListGrammar;

@header {
package dev.nokee.xcode.internal;
}

document
	:	value?
	;

// Note: date, real, integer and boolean are considered as string
// Apple mention that "numbers are handled as strings" (see https://developer.apple.com/library/archive/documentation/Cocoa/Conceptual/PropertyLists/OldStylePlists/OldStylePLists.html#//apple_ref/doc/uid/20001012-BBCBDBJE)
// Nokee team expanded the mention and will also treat date, real, integer, and boolean as strings.
// There is no example of those type used in the old-style ASCII property lists.
value
	:	dict
	|	data
	|	string
	|	array
	;

dict
	:	'{' dictKeyValuePair* '}'
	;

dictKeyValuePair
	:	dictKey '=' value ';'
	;

dictKey
	:	StringLiteral
	;

array
	:	'(' (arrayElementList ','?)? ')'
	;

arrayElementList
	:	value (',' value)*
	;

data
	:	DataLiteral
	;

string
	:	StringLiteral
	;




fragment
HexDigits
	:	HexDigit+
	;

fragment
HexDigit
	:	[0-9a-fA-F]
	;

fragment
OctalDigit
	:	[0-7]
	;




DataLiteral
	:	'<' ByteCharacters? '>'
	;

fragment
Bytes
	:	HexDigits ( ' ' HexDigits )*
	;

fragment
ByteCharacters
	:	ByteCharacter+
	;

fragment
ByteCharacter
	:	HexDigit
	|	' '
	;




StringLiteral
	:	('"' QuoteRequireStringCharacters? '"')
	|	QuoteOptionalStringCharacters+
	;

fragment
QuoteRequireStringCharacters
	:	QuoteRequireStringCharacter+
	;

// TODO: Test { "key" = "value"; }
fragment
QuoteRequireStringCharacter
	:	~[\\\r\n"]
	|	EscapeSequence
	;

fragment
QuoteOptionalStringCharacters
	:	QuoteOptionalStringCharacter+
	;

fragment
QuoteOptionalStringCharacter
	:	[a-zA-Z0-9./_]
	;


// NOTE: single quote escaping support is defensive only
fragment
EscapeSequence
	:	'\\' [abtnfrv"'\\]
	|	OctalEscape
	|	UnicodeEscape
	;

fragment
OctalEscape
	:	'\\' OctalDigit
	|	'\\' OctalDigit OctalDigit
	|	'\\' ZeroToThree OctalDigit OctalDigit
	;

fragment
ZeroToThree
	:	[0-3]
	;

fragment
UnicodeEscape
	:	'\\' ('u' | 'U' ) HexDigit HexDigit HexDigit HexDigit
	;




// We ignore comments as we don't have a need to understand them
COMMENT:	'/*' .*? '*/' -> skip;
LINE_COMMENT:	'//' ~[\r\n]* -> skip;

OTHER:	. -> skip;
