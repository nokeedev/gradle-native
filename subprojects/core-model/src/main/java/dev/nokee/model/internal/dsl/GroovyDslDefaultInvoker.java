package dev.nokee.model.internal.dsl;

import groovy.lang.GroovyObject;

public class GroovyDslDefaultInvoker<T> implements GroovyDslInvoker<T> {
	private final GroovyObject object;

	public GroovyDslDefaultInvoker(GroovyObject object) {
		this.object = object;
	}

	@Override
	public Object invokeMethod(String methodName, Object arguments) {
		return object.getMetaClass().invokeMethod(object, methodName, arguments);
	}
}
