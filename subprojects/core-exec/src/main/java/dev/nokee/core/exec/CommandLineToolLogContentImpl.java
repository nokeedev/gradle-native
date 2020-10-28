package dev.nokee.core.exec;

import lombok.EqualsAndHashCode;
import lombok.val;
import net.rubygrapefruit.ansi.AnsiParser;
import net.rubygrapefruit.ansi.console.AnsiConsole;
import net.rubygrapefruit.ansi.token.NewLine;
import net.rubygrapefruit.ansi.token.Text;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static dev.nokee.core.exec.CommandLineToolLogContentEmptyImpl.EMPTY_LOG_CONTENT;

@EqualsAndHashCode
final class CommandLineToolLogContentImpl implements CommandLineToolLogContent {
	private final String content;
	@EqualsAndHashCode.Exclude private final boolean definitelyNoAnsiChars;

	CommandLineToolLogContentImpl(String content, boolean definitelyNoAnsiChars) {
		this.content = content;
		this.definitelyNoAnsiChars = definitelyNoAnsiChars;
	}

	@Override
	public <T> T parse(CommandLineToolOutputParser<T> parser) {
		Objects.requireNonNull(parser, "Command line tool output parser cannot be null.");
		return parser.parse(content);
	}

	@Override
	public String getAsString() {
		return content;
	}

	@Override
	public CommandLineToolLogContent withNormalizedEndOfLine() {
		return new CommandLineToolLogContentImpl(content.replaceAll("\r?\n", "\n"), definitelyNoAnsiChars);
	}

	@Override
	public CommandLineToolLogContent drop(int i) {
		String newContent = content;
		while (i > 0 && !newContent.isEmpty()) {
			val indexOfEndOfLine = newContent.indexOf('\n');
			if (indexOfEndOfLine != -1) {
				newContent = newContent.substring(indexOfEndOfLine + 1);
			} else {
				return EMPTY_LOG_CONTENT;
			}
			--i;
		}
		return new CommandLineToolLogContentImpl(newContent, definitelyNoAnsiChars);
	}

	@Override
	public CommandLineToolLogContent withAnsiControlCharactersInterpreted() {
		if (definitelyNoAnsiChars) {
			return this;
		}
		try {
			AnsiConsole console = interpretAnsiChars(content);
			StringBuilder result = new StringBuilder();
			console.contents(token -> {
				if (token instanceof Text) {
					result.append(((Text) token).getText());
				} else if (token instanceof NewLine) {
					result.append("\n");
				}
			});
			return new CommandLineToolLogContentImpl(result.toString(), true);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static AnsiConsole interpretAnsiChars(String content) throws IOException {
		AnsiConsole console = new AnsiConsole();
		AnsiParser parser = new AnsiParser();
		Writer writer = new OutputStreamWriter(parser.newParser("utf-8", console));
		writer.write(content);
		writer.flush();
		return console;
	}

	@Override
	public List<String> getLines() {
		return toLines(content);
	}

	private static List<String> toLines(String content) {
		return Arrays.stream(StringUtils.splitPreserveAllTokens(content, "\n", -1)).map(line -> {
			if (line.endsWith("\r")) {
				return line.substring(0, line.length() - 1);
			}
			return line;
		}).collect(Collectors.toList());
	}
}
