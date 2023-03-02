parser grammar MacroExpansionParser;

options { tokenVocab=MacroExpansionLexer; }

@header {
package dev.nokee.xcode;
}

parse: macroList?;

macroList: (macro | literal)+;

literal
	:	(ANY | ESCAPED_DOLLAR | RCURLY | RPAREN | RSQUAR | VARNAME | EQUAL | OPERATION_COMMA)+
	|	DOLLAR ~(VARNAME | DOLLAR | CLOSE_EXPANSION | OPERATION_COLON)*
	;

macro
	:	OPEN_EXPANSION macroRef? operationList? CLOSE_EXPANSION
	|	DOLLAR macroName
	;

macroRef
	:	macroList
	;

macroName
	:	VARNAME
	;

operationList
	: OPERATION_COLON (replacementOperation | (retrievalOperation ((OPERATION_COLON | OPERATION_COMMA) retrievalOperation)* ((OPERATION_COLON | OPERATION_COMMA) replacementOperation)?))?
	;

retrievalOperation
	:	operator
	;

replacementOperation
	:	operator EQUAL argument
	;

operator
	:	VARNAME
	;

argument
	:	VARNAME
	;
