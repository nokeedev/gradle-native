package dev.nokee.utils;

interface ExecutionArguments {
	Object[] getArguments();

	Object getMock();

	default <T> T getArgument(int index) {
		return (T) getArguments()[index];
	}

	default <T> T getArgument(int index, Class<T> type) {
		return type.cast(getArgument(index));
	}
}
