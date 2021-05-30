package dev.nokee.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

final class ExecutionResult<T extends ExecutionArguments> {
	private final List<T> arguments = new ArrayList<>();

	public void record(T argument) {
		arguments.add(argument);
	}

	public Stream<T> getArguments() {
		return arguments.stream();
	}

	public static <T extends ExecutionArguments> ExecutionResult<T> from(HasExecutionResult<T> object) {
		try {
			return (ExecutionResult<T>) object.getClass().getDeclaredField("result").get(object);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}
}
