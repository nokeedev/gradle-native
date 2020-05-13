package dev.nokee.core.exec.internal;

import dev.nokee.core.exec.CommandLineToolLogContent;
import dev.nokee.core.exec.CommandLineToolOutputParser;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultCommandLineToolLogContent implements CommandLineToolLogContent {
	private final String content;

	@Override
	public <T> T parse(CommandLineToolOutputParser<T> parser) {
		return parser.parse(content);
	}

	@Override
	public String getAsString() {
		return content;
	}
}
