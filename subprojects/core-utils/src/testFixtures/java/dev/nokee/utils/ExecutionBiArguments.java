package dev.nokee.utils;

interface ExecutionBiArguments<T, U> extends ExecutionArguments {
	static <T, U> ExecutionBiArguments<T, U> of(Object thiz, T t, U u) {
		return new ExecutionBiArguments<T, U>() {
			@Override
			public Object[] getArguments() {
				return new Object[] {t, u};
			}

			@Override
			public Object getMock() {
				return thiz;
			}
		};
	}

	default T getFirst() {
		return getArgument(0);
	}

	default U getSecond() {
		return getArgument(1);
	}
}
