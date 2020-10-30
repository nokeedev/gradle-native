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
import java.util.function.Consumer;
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

	@Override
	public CommandLineToolLogContent visitEachLine(Consumer<LineDetails> visitor) {
		val builder = new StringBuilder();
		int indexOfStartOfLine = 0;
		int n = 0;
		String previousLineSeparator = "";
		boolean lastLine = false;
		do
		{
			int indexOfEndOfLine = content.indexOf('\n', indexOfStartOfLine);

			String currentLineSeparator = null;
			if (indexOfEndOfLine == -1) {
				indexOfEndOfLine = content.length();
				currentLineSeparator = "";
				lastLine = true;
			} else if (indexOfEndOfLine == 0) {
				currentLineSeparator = "\n";
			} else if (content.charAt(indexOfEndOfLine - 1) == '\r') {
				indexOfEndOfLine = indexOfEndOfLine - 1;
				currentLineSeparator = "\r\n";
			} else {
				currentLineSeparator = "\n";
			}

			String line = content.substring(indexOfStartOfLine, indexOfEndOfLine);
			if (n == 0) {
				val details = new LineDetailsImpl(line);
				visitor.accept(details);
				n = details.n;
				if (n == 0) {
					builder.append(previousLineSeparator).append(line);
					previousLineSeparator = currentLineSeparator;
				}
			}
			if (n > 0) {
				--n;
			}
			indexOfStartOfLine = indexOfEndOfLine + currentLineSeparator.length();
		} while (indexOfStartOfLine <= content.length() && !lastLine);

		val newContent = builder.toString();
		if (newContent.isEmpty()) {
			return EMPTY_LOG_CONTENT;
		}
		return new CommandLineToolLogContentImpl(newContent, false);
	}

	private static final class LineDetailsImpl implements LineDetails {
		private final String line;
		private int n = 0;

		LineDetailsImpl(String line) {
			this.line = line;
		}

		@Override
		public void dropLine() {
			n = 1;
		}

		@Override
		public void drop(int n) {
			this.n = n;
		}

		@Override
		public String getLine() {
			return line;
		}
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
