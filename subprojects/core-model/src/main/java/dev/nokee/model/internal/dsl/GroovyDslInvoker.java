package dev.nokee.model.internal.dsl;

public interface GroovyDslInvoker<T> {
	Object invokeMethod(String methodName, Object arguments);
}
