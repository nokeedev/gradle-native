package dev.nokee.utils;

import groovy.lang.Closure;

interface ClosureExecutionDelegate<T> extends ExecutionArguments {
	default T getDelegate() {
		return (T) ((Closure<?>) getMock()).getDelegate();
	}

	default int getResolveStrategy() {
		return ((Closure<?>) getMock()).getResolveStrategy();
	}
}
