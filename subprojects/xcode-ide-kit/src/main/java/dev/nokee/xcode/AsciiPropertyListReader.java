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

import dev.nokee.xcode.internal.AsciiPropertyListGrammarParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public final class AsciiPropertyListReader implements PropertyListReader {
	private final Reader delegate;
	private final AntlrParserIterator iterator;
	private Context next;
	private Context context;

	public AsciiPropertyListReader(Reader delegate) {
		this.delegate = delegate;
		try {
			dev.nokee.xcode.internal.AsciiPropertyListGrammarLexer lexer = new dev.nokee.xcode.internal.AsciiPropertyListGrammarLexer(CharStreams.fromReader(delegate));
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			dev.nokee.xcode.internal.AsciiPropertyListGrammarParser parser = new dev.nokee.xcode.internal.AsciiPropertyListGrammarParser(tokens);
			this.iterator = new AntlrParserIterator(parser.document());
			this.next = findNext();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Nullable
	private Context findNext() {
		while (iterator.hasNext()) {
			final AntlrParserIterator.AntlrEvent next = iterator.next();
			if (next.isRuleNode()) {
				final RuleNode rule = (RuleNode) next.getNode();
				if (next.isEntering()) {
					if (rule.getRuleContext().getRuleIndex() == AsciiPropertyListGrammarParser.RULE_document) {
						return new StartDocumentContext();
					} else if (rule.getRuleContext().getRuleIndex() == AsciiPropertyListGrammarParser.RULE_string) {
						return new StringContext((AsciiPropertyListGrammarParser.StringContext) rule);
					} else if (rule.getRuleContext().getRuleIndex() == AsciiPropertyListGrammarParser.RULE_value) {
						// skip this rule and find the actual value
					} else if (rule.getRuleContext().getRuleIndex() == AsciiPropertyListGrammarParser.RULE_array) {
						return new StartArrayContext();
					} else if (rule.getRuleContext().getRuleIndex() == AsciiPropertyListGrammarParser.RULE_dict) {
						return new StartDictContext();
					} else if (rule.getRuleContext().getRuleIndex() == AsciiPropertyListGrammarParser.RULE_dictKey) {
						return new DictKeyContext((AsciiPropertyListGrammarParser.DictKeyContext) rule);
					} else if (rule.getRuleContext().getRuleIndex() == AsciiPropertyListGrammarParser.RULE_data) {
						return new DataContext((AsciiPropertyListGrammarParser.DataContext) rule);
					}
				} else {
					if (rule.getRuleContext().getRuleIndex() == AsciiPropertyListGrammarParser.RULE_document) {
						return new EndDocumentContext();
					} else if (rule.getRuleContext().getRuleIndex() == AsciiPropertyListGrammarParser.RULE_array) {
						return new EndArrayContext();
					} else if (rule.getRuleContext().getRuleIndex() == AsciiPropertyListGrammarParser.RULE_dict) {
						return new EndDictContext();
					}
				}
			}
		}
		return null;
	}

	private interface Context {
		Event event();

		boolean readBoolean();
		long readInteger();
		String readString();
		String readDictionaryKey();
		byte[] readData();
	}

	private static abstract class ContextAdapter implements Context {
		@Override
		public boolean readBoolean() {
			throw new UnsupportedOperationException();
		}

		@Override
		public long readInteger() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String readString() {
			throw new UnsupportedOperationException(getClass().getSimpleName());
		}

		@Override
		public String readDictionaryKey() {
			throw new UnsupportedOperationException();
		}

		@Override
		public byte[] readData() {
			throw new UnsupportedOperationException();
		}
	}

	private static final class DataContext extends ContextAdapter {
		private final AsciiPropertyListGrammarParser.DataContext ctx;

		public DataContext(AsciiPropertyListGrammarParser.DataContext ctx) {
			this.ctx = ctx;
		}

		@Override
		public Event event() {
			return Event.DATA;
		}

		@Override
		public byte[] readData() {
			final ByteArrayOutputStream result = ctx.getText().chars().filter(it -> '<' != it && '>' != it && !Character.isSpaceChar(it)).map(it -> Character.digit(it, 16)).collect(() -> new ByteArrayOutputStream(ctx.getText().length()), ByteArrayOutputStream::write, (a, b) -> {
				try {
					b.writeTo(a);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
			return result.toByteArray();
		}
	}

	private static final class StartDocumentContext extends ContextAdapter {
		@Override
		public Event event() {
			return Event.DOCUMENT_START;
		}
	}

	private static final class EndDocumentContext extends ContextAdapter {
		@Override
		public Event event() {
			return Event.DOCUMENT_END;
		}
	}

	private static final class StringContext extends ContextAdapter {
		final ParseTree v;

		private StringContext(AsciiPropertyListGrammarParser.StringContext v) {
			this.v = v;
		}

		@Override
		public Event event() {
			return Event.STRING;
		}

		public boolean readBoolean() {
			// Try to parse boolean
			return Boolean.parseBoolean(readString());
		}

		@Override
		public long readInteger() {
			// Try to parse long
			return Long.parseLong(readString());
		}

		@Override
		public String readString() {
			return unquoteIfRequired(unescapeString(v.getText()));
		}
	}

	private static String unescapeString(String s) {
		final StringBuilder builder = new StringBuilder();
		new CodePointIterator(s).forEachRemaining(builder::appendCodePoint);
		return builder.toString();
	}

	private static final class CodePointIterator implements Iterator<Integer> {
		private final StringCharacterIterator iterator;
		private Integer next;

		public CodePointIterator(String s) {
			iterator = new StringCharacterIterator(s);
			next = findNext(iterator.current());
		}

		private Integer findNext(char c) {
			if (c == CharacterIterator.DONE) {
				return null;
			}

			if (c == '\\') {
				return parseEscapeSequence();
			} else {
				return (int) c;
			}
		}

		private int parseEscapeSequence() {
			final char c = iterator.next();
			switch (c) {
				case '\\':
				case '"':
				case '\'':
					return c;
				case 'u':
				case 'U':
					return parseHexUnicodeSequence();
				case 'b': return '\b';
				case 'n': return '\n';
				case 'r': return '\r';
				case 't': return '\t';
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
					return parseOctalUnicodeSequence(c);
				default:
					throw new IllegalStateException("invalid escape sequence \\" + c);
			}
		}

		private int parseHexUnicodeSequence() {
			// grab 4 hex digit to build Unicode value
			char unicodeValueOrHighSurrogate = parseHex(iterator.next(), iterator.next(), iterator.next(), iterator.next());
			if (Character.isHighSurrogate(unicodeValueOrHighSurrogate)) {
				char unicodeLowSurrogate = (char) Objects.requireNonNull(findNext(iterator.next())).intValue();
				if (Character.isLowSurrogate(unicodeLowSurrogate)) {
					return Character.toCodePoint(unicodeValueOrHighSurrogate, unicodeLowSurrogate);
				} else {
					throw new IllegalStateException("not low surrogate");
				}
			} else {
				return unicodeValueOrHighSurrogate;
			}
		}

		private static char parseHex(char d3, char d2, char d1, char d0) {
			String digits = new String(new char[] { d3, d2, d1, d0 });
			return (char) Integer.parseInt(digits, 16);
		}

		private char parseOctalUnicodeSequence(char firstDigit) {
			// grab 3 octal digit to build Unicode value
			String unicodeValue = new String(new char[] { firstDigit, iterator.next(), iterator.next() });
			return (char) Integer.parseInt(unicodeValue, 8);
		}

		public boolean hasNext() {
			return next != null;
		}

		@Override
		public Integer next() {
			if (next == null) {
				throw new NoSuchElementException();
			}
			Integer result = next;
			next = findNext(iterator.next());
			return result;
		}
	}

	private static String unquoteIfRequired(String s) {
		if (s.startsWith("\"") && s.endsWith("\"")) {
			return s.substring(1, s.length() - 1);
		}
		return s;
	}

	private static final class StartArrayContext extends ContextAdapter {
		@Override
		public Event event() {
			return Event.ARRAY_START;
		}
	}

	private static final class EndArrayContext extends ContextAdapter {
		@Override
		public Event event() {
			return Event.ARRAY_END;
		}
	}

	private static final class StartDictContext extends ContextAdapter {
		@Override
		public Event event() {
			return Event.DICTIONARY_START;
		}
	}

	private static final class EndDictContext extends ContextAdapter {
		@Override
		public Event event() {
			return Event.DICTIONARY_END;
		}
	}

	private static final class DictKeyContext extends ContextAdapter {
		final ParseTree v;

		private DictKeyContext(AsciiPropertyListGrammarParser.DictKeyContext v) {
			this.v = v;
		}

		@Override
		public Event event() {
			return Event.DICTIONARY_KEY;
		}

		@Override
		public String readDictionaryKey() {
			return unquoteIfRequired(v.getText());
		}

		@Override
		public String readString() {
			throw new UnsupportedOperationException("Use readDictionaryKey instead of readString because although a dictionary key in ASCII format are string, both differ in meaning.");
		}
	}

	@Override
	public Event next() {
		if (next == null) {
			throw new NoSuchElementException("DOCUMENT_END reached: no more elements on the stream.");
		}
		final Context result = next;
		context = result;
		next = findNext();
		return result.event();
	}

	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public String readDictionaryKey() {
		return context.readDictionaryKey();
	}

	@Override
	public byte[] readData() {
		return context.readData();
	}

	@Override
	public LocalDateTime readDate() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean readBoolean() {
		return context.readBoolean();
	}

	@Override
	public String readString() {
		return context.readString();
	}

	@Override
	public long readInteger() {
		return context.readInteger();
	}

	@Override
	public float readReal() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}
}
