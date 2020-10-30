package dev.nokee.core.exec;

import com.google.common.collect.ImmutableList;

import java.util.List;

enum CommandLineToolArgumentsEmptyImpl implements CommandLineToolArguments {
	EMPTY_ARGUMENTS;

	@Override
	public List<String> get() {
		return ImmutableList.of();
	}
}
