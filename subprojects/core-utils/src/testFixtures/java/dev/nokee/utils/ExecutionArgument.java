package dev.nokee.utils;

interface ExecutionArgument<T> extends ExecutionArguments {
	static <T> ExecutionArgument<T> of(Object thiz, T t) {
		return new ExecutionArgument<T>() {
			@Override
			public Object[] getArguments() {
				return new Object[] {t};
			}

			@Override
			public Object getMock() {
				return thiz;
			}
		};
	}

	default T get() {
		return getArgument(0);
	}
}
