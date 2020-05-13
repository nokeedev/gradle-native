package dev.nokee.core.exec;

/**
 * A parser for converting command line tool output into a structured representation.
 * Parsing outputs is useful for several use case such as extracting information like version, paths or comparing similar capability tool's output.
 * For example, comparing the output of {@literal dir} and {@literal ls} or {@literal jar} tool on different systems (Windows vs macOS).
 *
 * @param <T> the structured representation returned by this parser.
 * @since 0.4
 */
// TODO: Open question, the content should probably be a structured representation of the content (not a String).
//  It can't be CommandLineToolLogContent as it contains the 'parse' method which wouldn't make sense.
//  It could be a super type to CommandLineToolLogContent.
//  The reason against a String is users would have to always normalize the output as the parser expect it, vs the parser could manipulate it how ever it wants, like trim, normalize eol, drop lines, each lines, etc.
public interface CommandLineToolOutputParser<T> {
	T parse(String content);
}
