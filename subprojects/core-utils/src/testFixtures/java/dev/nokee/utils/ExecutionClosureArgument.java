package dev.nokee.utils;

import groovy.lang.Closure;

interface ExecutionClosureArgument<T> extends ExecutionArgument<T>, ClosureExecutionDelegate<T> {
	static <T> ExecutionClosureArgument<T> of(Closure<?> thiz, T t) {
		return new ExecutionClosureArgument<T>() {
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
}
