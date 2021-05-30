package dev.nokee.utils;

import groovy.lang.Closure;

interface ClosureExecutionDelegate extends ExecutionArguments {
	default Object getDelegate() {
		return ((Closure<?>) getMock()).getDelegate();
	}

	default int getResolveStrategy() {
		return ((Closure<?>) getMock()).getResolveStrategy();
	}
}
