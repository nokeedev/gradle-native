package dev.nokee.utils;

import groovy.lang.Closure;

final class ExecutionArgumentsFactory {
	public static <T> ExecutionArgument<T> create(Object thiz, T t) {
		return ExecutionArgument.of(thiz, t);
	}

	public static <T, U> ExecutionBiArguments<T, U> create(Object thiz, T t, U u) {
		return ExecutionBiArguments.of(thiz, t, u);
	}

	public static <T> ExecutionClosureArgument<T> create(Closure<?> thiz, T t) {
		return ExecutionClosureArgument.of(thiz, t);
	}

	public static <T, U> ExecutionClosureBiArguments<T, U> create(Closure<?> thiz, T t, U u) {
		return ExecutionClosureBiArguments.of(thiz, t, u);
	}
}
