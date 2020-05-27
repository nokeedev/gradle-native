grammar XcodebuildSdks;

@header {
package dev.nokee.runtime.darwin.internal.parsers;
}

output: (.*? SDK_FLAG sdkIdentifier)+ .*?;

sdkIdentifier: SDK_IDENTIFIER;

SDK_FLAG: '-sdk';

fragment DIGIT: [0-9];
fragment VERSION: DIGIT+ ('.' DIGIT+)?;
fragment IDENTIFIER: [a-z.];
SDK_IDENTIFIER: IDENTIFIER+ VERSION;

OTHER: . -> skip;
