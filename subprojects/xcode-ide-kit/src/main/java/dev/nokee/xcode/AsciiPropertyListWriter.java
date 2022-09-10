/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.xcode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Stack;
import java.util.stream.IntStream;

import static java.lang.Character.isAlphabetic;
import static java.lang.Character.isDigit;

public final class AsciiPropertyListWriter implements PropertyListWriter {
	private static final char NEW_LINE_CHAR = '\n';
	private final Stack<Context> contexts = new Stack<>();
	private final Writer delegate;
	private final boolean pretty;

	public AsciiPropertyListWriter(Writer delegate) {
		this(delegate, false);
	}

	// NOTE: pretty flag is a gross approximation for making the resulting file pretty, we should consider several cases:
	//  Compact (no new lines), indent before dict/array (similar to bracket in code, indent char (tab by default... may be a hard requirement of the format), inline array on x entry or less, pad inline arrays with spaces, i.e. ( v1, v2 ), quote all string
	public AsciiPropertyListWriter(Writer delegate, boolean pretty) {
		this.delegate = delegate;
		this.pretty = pretty;
	}

	private enum Context {
		EMPTY_DOC, DOC, INTEGER, REAL, STRING, DATA, EMPTY_ARRAY, ARRAY, DICT, DICT_KEY;
	}

	// TODO: indentation should be tabs and newline most likely unix new line
	// TODO: Figure out how comments can be supported

	@Override
	public void writeStartDocument(PropertyListVersion version) {
		run(() -> {
			contexts.push(Context.EMPTY_DOC);
			delegate.write("// !$*UTF8*$!");
			delegate.write(NEW_LINE_CHAR);
		});
	}

	@Override
	public void writeEndDocument() {
		// nothing to do
		run(() -> {
			if (contexts.peek() == Context.DOC) {
				delegate.write(NEW_LINE_CHAR);
			}
			delegate.flush();
		});
	}

	@Override
	public void writeStartDictionary(long elementCount) {
		run(() -> {
			contexts.push(doEnterContext(Context.DICT));

			delegate.write("{");

			if (pretty) {
				delegate.write(NEW_LINE_CHAR);
			} else {
				delegate.write(" ");
			}
		});
	}

	@Override
	public void writeDictionaryKey(String key) {
		assert contexts.peek() == Context.DICT;
		run(() -> {
			if (pretty) {
				delegate.write(indent(level()));
			}

			contexts.push(Context.DICT_KEY);
			writeStringInternal(key);
			// TODO: Add comment
			delegate.write(" = ");
			// TODO: include ';' after data
		});
	}

	@Override
	public void writeEndDictionary() {
		run(() -> {
			if (pretty) {
				delegate.write(indent(level() - 1));
			}
			delegate.write("}");

			doExitContext(assertDictContext(contexts.pop()));
		});
	}

	@Override
	public void writeEmptyDictionary() {
		run(() -> {
			doEnterContext(Context.DICT);
			delegate.write("{}");
			doExitContext(Context.DICT);
		});
	}

	@Override
	public void writeStartArray(long elementCount) {
		run(() -> {
			contexts.push(doEnterContext(Context.EMPTY_ARRAY));
			delegate.write("(");

			if (pretty) {
				delegate.write(NEW_LINE_CHAR);
			} else {
				delegate.write(" ");
			}
			// TODO: Write comma after element, even last element
		});
	}

	private int level() {
		return (int) contexts.stream().filter(it -> it == Context.DICT || it == Context.ARRAY).count();
	}

	private static char[] indent(int level) {
		char[] result = new char[level];
		Arrays.fill(result, '\t');
		return result;
	}

	@Override
	public void writeEndArray() {
		run(() -> {
			if (pretty) {
				delegate.write(NEW_LINE_CHAR);
				delegate.write(indent(level() - 1));
			} else {
				delegate.write(" ");
			}
			delegate.write(")");

			doExitContext(assertArrayContext(contexts.pop()));
		});
	}

	@Override
	public void writeEmptyArray() {
		run(() -> {
			doEnterContext(Context.EMPTY_ARRAY);
			delegate.write("()");
			doExitContext(Context.ARRAY);
		});
	}

	@Override
	public void writeData(byte[] bytes) {
		run(() -> {
			doEnterContext(Context.DATA);

			delegate.write("<");

			final StringBuilder builder = new StringBuilder();
			for (byte aByte : bytes) {
				builder.append(String.format("%02x", aByte));
			}
			delegate.write(builder.toString());
			delegate.write(">");

			doExitContext(Context.DATA);
		});
	}

	@Override
	public void writeDate(LocalDateTime date) {
		writeString(DateTimeFormatter.ISO_DATE_TIME.format(date)); // educated guess
	}

	@Override
	public void writeBoolean(boolean b) {
		writeString(String.valueOf(b)); // educated guess
	}

	@Override
	public void writeString(CharSequence s) {
		run(() -> {
			doEnterContext(Context.STRING);

			writeStringInternal(s);

			doExitContext(Context.STRING);
		});
	}

	private static boolean isValidUnquotedStringCharacter(int x) {
		// see https://opensource.apple.com/source/CF/CF-1153.18/CFOldStylePList.c for a macro of the same name
		return isAlphabetic(x) || isDigit(x) || x == '_' || x == '$' || x == '/' || x == ':' || x == '.' || x == '-';
	}

	private void writeStringInternal(CharSequence s) throws IOException {
		// Note: Quotes are optional around String if and only if the all chars are either alphanumeric or underscore
		if (s.length() > 0 && s.chars().allMatch(AsciiPropertyListWriter::isValidUnquotedStringCharacter)) {
			delegate.write(s.toString());
		} else {
			delegate.write("\"");

			// According to https://developer.apple.com/library/archive/documentation/Cocoa/Conceptual/PropertyLists/OldStylePlists/OldStylePLists.html
			// unicode characters should be written as-is which would violate the ASCII plain format.
			//   "You may see strings containing unreadable sequences of ASCII characters; these are used to represent Unicode characters"
			// Based on experience, Xcode escape unicode characters when writing ASCII property list.
			delegate.write(escapeUnicodeCharacters(s));
			delegate.write("\"");
		}
	}

	private static String escapeUnicodeCharacters(CharSequence s) {
		return s.chars().flatMap(it -> {
			if (it > 127) {
				// According to CFOldStylePList.c, we should use upper case u in the escape sequence
				return IntStream.concat(IntStream.of('\\', 'U'), String.format("%04x", it).chars());
			} else if (it == '\\') {
				return IntStream.of('\\', '\\');
			} else if (it == '"') {
				return IntStream.of('\\', '"');
			} else if (it == '\u0007') {
				return IntStream.of('\\', 'a');
			} else if (it == '\b') {
				return IntStream.of('\\', 'b');
			} else if (it == '\n') {
				return IntStream.of('\\', 'n');
			} else if (it == '\r') {
				return IntStream.of('\\', 'r');
			} else if (it == '\t') {
				return IntStream.of('\\', 't');
			} else if (it == '\u000b') {
				return IntStream.of('\\', 'v');
			}
			return IntStream.of(it);
		}).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
	}

	@Override
	public void writeInteger(long n) {
		writeString(String.valueOf(n)); // handle numbers as strings
	}

	@Override
	public void writeReal(float n) {
		writeString(String.valueOf(n)); // handle numbers as strings
	}

	@Override
	public void flush() {
		run(delegate::flush);
	}

	private Context doEnterContext(Context enteringContext) throws IOException {
		if (contexts.peek() == Context.EMPTY_ARRAY) {
			// Replace array context to notify it's not empty
			contexts.pop();
			contexts.push(Context.ARRAY);

			if (pretty) {
				delegate.write(indent(level()));
			}
		} else if (contexts.peek() == Context.ARRAY) {
			delegate.write(", ");

			if (pretty) {
				delegate.write(NEW_LINE_CHAR);
				delegate.write(indent(level()));
			}
		} else if (contexts.peek() == Context.EMPTY_DOC) {
			// Replace array context to notify it's not empty
			contexts.pop();
			contexts.push(Context.DOC);
		}
		return enteringContext;
	}

	private static Context assertDictContext(Context context) {
		assert context == Context.DICT;
		return context;
	}

	private static Context assertArrayContext(Context context) {
		assert context == Context.ARRAY;
		return context;
	}

	private void doExitContext(Context exitingContext) throws IOException {
		if (contexts.peek() == Context.DICT_KEY) {
			delegate.write(";");

			// TODO: formating
			if (pretty) {
				delegate.write(NEW_LINE_CHAR);
			} else {
				delegate.write(" ");
			}

			contexts.pop();
//		} else if (contexts.peek() == Context.ARRAY) {
//			delegate.write(",");
//
//			// TODO: formating
//			delegate.write(" ");
		}
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}

	private void run(AsciiStreamRunnable runnable) {
		try {
			runnable.run();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private interface AsciiStreamRunnable {
		void run() throws IOException;
	}
}
