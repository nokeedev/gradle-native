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
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

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
			throw new UnsupportedOperationException();
		}

		@Override
		public String readDictionaryKey() {
			throw new UnsupportedOperationException();
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
			return unquoteIfRequired(v.getText());
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
		throw new UnsupportedOperationException();
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
