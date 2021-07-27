package dev.nokee.utils;

import groovy.lang.Closure;

interface ExecutionClosureBiArguments<T, U> extends ExecutionBiArguments<T, U>, ClosureExecutionDelegate<T> {
	static <U, T> ExecutionClosureBiArguments<T, U> of(Closure<?> thiz, T t, U u) {
		return new ExecutionClosureBiArguments<T, U>() {
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
}
