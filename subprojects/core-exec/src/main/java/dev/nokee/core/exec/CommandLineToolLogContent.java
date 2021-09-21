/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.core.exec;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static dev.nokee.core.exec.CommandLineToolLogContentEmptyImpl.EMPTY_LOG_CONTENT;

/**
 * A representation of the log content from a command line tool execution.
 * The log content can be manipulated according to the consumer's need.
 *
 * @since 0.4
 */
public interface CommandLineToolLogContent {
	/**
	 * Creates an log content of the specified string.
	 *
	 * @param content the raw content of the log content
	 * @return an {@link CommandLineToolLogContent} instance representing the specified content, never null.
	 * @since 0.5
	 */
	static CommandLineToolLogContent of(String content) {
		Objects.requireNonNull(content, "Cannot create log content from null.");
		if (content.isEmpty()) {
			return EMPTY_LOG_CONTENT;
		}
		return new CommandLineToolLogContentImpl(content, false);
	}

	/**
	 * Creates an empty log content.
	 *
	 * @return an empty {@link CommandLineToolLogContent} instance, never null.
	 * @since 0.5
	 */
	static CommandLineToolLogContent empty() {
		return EMPTY_LOG_CONTENT;
	}

	/**
	 * Returns a structured representation of the log content parsed by the specified parser.
	 * The parser will received the log content and return any object representing what needs to be parsed.
	 *
	 * @param parser the parser to use
	 * @param <T> the structured representation type returned by the parser
	 * @return a structured representation of the log content.
	 */
	// TODO: Open question, should null be a valid return value?
	<T> T parse(CommandLineToolOutputParser<T> parser);

	/**
	 * Returns the log content as a string.
	 * @return a {@link String} representation of the log content.
	 */
	String getAsString();

	/**
	 * Returns this content formatted using a new line char to separate lines.
	 *
	 * @return a new {@link CommandLineToolLogContent} instance using a new line char to separate lines, never null.
	 * @since 0.5
	 */
	CommandLineToolLogContent withNormalizedEndOfLine();

	/**
	 * Drops the first n lines.
	 *
	 * @param n the number of lines to drop from the log content
	 * @return a new {@link CommandLineToolLogContent} instance without the first n lines, never null.
	 * @since 0.5
	 */
	CommandLineToolLogContent drop(int n);

	/**
	 * Interprets a the ANSI control characters to produce plain text.
	 *
	 * @return a new {@link CommandLineToolLogContent} instance with all ANSI control characters interpreted, never null.
	 * @since 0.5
	 */
	CommandLineToolLogContent withAnsiControlCharactersInterpreted();

	/**
	 * Returns this content separated into lines. The line does not include the line separator.
	 *
	 * @return the lines of the content, never null.
	 * @since 0.5
	 */
	List<String> getLines();

	/**
	 * Visit each lines.
	 *
	 * @return a new {@link CommandLineToolLogContent} instance of the result following the visit, never null.
	 * @since 0.5
	 */
	CommandLineToolLogContent visitEachLine(Consumer<LineDetails> visitor);

	interface LineDetails {
		void dropLine();
		void drop(int n);
		void replaceWith(String newLineContent);
		String getLine();
	}
}
