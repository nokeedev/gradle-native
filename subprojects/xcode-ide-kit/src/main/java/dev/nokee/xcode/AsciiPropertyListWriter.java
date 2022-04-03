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
import java.util.Stack;

public final class AsciiPropertyListWriter implements PropertyListWriter {
	private static final char NEW_LINE_CHAR = '\n';
	private final Stack<Context> contexts = new Stack<>();
	private final Writer delegate;

	public AsciiPropertyListWriter(Writer delegate) {
		this.delegate = delegate;
	}

	private enum Context {
		DOC, INTEGER, REAL, STRING, DATA, EMPTY_ARRAY, ARRAY, DICT, DICT_KEY;
	}

	// TODO: indentation should be tabs and newline most likely unix new line
	// TODO: Figure out how comments can be supported

	@Override
	public void writeStartDocument(PropertyListVersion version) {
		run(() -> {
			contexts.push(Context.DOC);
			delegate.write("// !$*UTF8*$!");
			delegate.write(NEW_LINE_CHAR);
		});
	}

	@Override
	public void writeEndDocument() {
		// nothing to do
		run(() -> {
			delegate.flush();
		});
	}

	@Override
	public void writeStartDictionary(long elementCount) {
		run(() -> {
			contexts.push(doEnterContext(Context.DICT));

			delegate.write("{");

			delegate.write(" ");
//			delegate.write(NEW_LINE_CHAR);
		});
	}

	@Override
	public void writeDictionaryKey(String key) {
		assert contexts.peek() == Context.DICT;
		run(() -> {
			contexts.push(Context.DICT_KEY);
			// TODO: Maybe does not support spaces in keys...
			delegate.write(key);
			// TODO: Add comment
			delegate.write(" = ");
			// TODO: include ';' after data
		});
	}

	@Override
	public void writeEndDictionary() {
		run(() -> {
			delegate.write("}");

			doExitContext(assertDictContext(contexts.pop()));
		});
	}

	@Override
	public void writeEmptyDictionary() {
		run(() -> {
			delegate.write("{}");
			doExitContext(Context.DICT);
		});
	}

	@Override
	public void writeStartArray(long elementCount) {
		run(() -> {
			contexts.push(doEnterContext(Context.EMPTY_ARRAY));
			delegate.write("(");

			delegate.write(" ");
			// TODO: Write comma after element, even last element
		});
	}

	@Override
	public void writeEndArray() {
		run(() -> {
			delegate.write(" ");
			delegate.write(")");

			doExitContext(assertArrayContext(contexts.pop()));
		});
	}

	@Override
	public void writeEmptyArray() {
		run(() -> {
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

			if (s.chars().allMatch(it -> Character.isAlphabetic(it) || Character.isDigit(it))) {
				delegate.write(s.toString());
			} else {
				delegate.write("\"");
				delegate.write(s.toString()); // TODO: What do we do with embedded new lines?
				delegate.write("\"");
			}

			doExitContext(Context.STRING);
		});
	}

	@Override
	public void writeInteger(long n) {
		writeString(String.valueOf(n)); // handle numbers as strings
	}

	@Override
	public void writeReal(double n) {
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
		} else if (contexts.peek() == Context.ARRAY) {
			delegate.write(", ");
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
			delegate.write(" ");

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
