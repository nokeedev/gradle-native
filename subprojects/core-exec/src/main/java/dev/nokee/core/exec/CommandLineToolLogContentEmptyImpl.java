package dev.nokee.core.exec;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

enum CommandLineToolLogContentEmptyImpl implements CommandLineToolLogContent {
	EMPTY_LOG_CONTENT;

	@Override
	public <T> T parse(CommandLineToolOutputParser<T> parser) {
		return parser.parse(StringUtils.EMPTY);
	}

	@Override
	public String getAsString() {
		return StringUtils.EMPTY;
	}

	@Override
	public CommandLineToolLogContent withNormalizedEndOfLine() {
		return this;
	}

	@Override
	public CommandLineToolLogContent drop(int n) {
		return this;
	}

	@Override
	public CommandLineToolLogContent withAnsiControlCharactersInterpreted() {
		return this;
	}

	@Override
	public List<String> getLines() {
		return ImmutableList.of();
	}
}
