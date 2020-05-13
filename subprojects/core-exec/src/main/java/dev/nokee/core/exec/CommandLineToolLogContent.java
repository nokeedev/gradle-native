package dev.nokee.core.exec;

/**
 * A representation of the log content from a command line tool execution.
 * The log content can be manipulated according to the consumer's need.
 *
 * @since 0.4
 */
public interface CommandLineToolLogContent {
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
}
