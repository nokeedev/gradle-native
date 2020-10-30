package dev.nokee.core.exec;

import com.google.common.collect.ImmutableList;

import java.util.List;

public interface CommandLineToolArguments {
	static CommandLineToolArguments empty() {
		return CommandLineToolArgumentsEmptyImpl.EMPTY_ARGUMENTS;
	}

	static CommandLineToolArguments of(Object... args) {
		return of(ImmutableList.copyOf(args));
	}

	static CommandLineToolArguments of(List<Object> args) {
		if (args.isEmpty()) {
			return CommandLineToolArgumentsEmptyImpl.EMPTY_ARGUMENTS;
		}
		return new CommandLineToolArgumentsImpl(args);
	}

	List<String> get();
}
