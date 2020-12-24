package dev.nokee.platform.base.testers;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

final class InvokableMethod<T> {
	private final Method method;
	private final Object instance;

	private InvokableMethod(Method method, Object instance) {
		this.method = method;
		this.instance = instance;
	}

	public static <T> InvokableMethod<T> bind(Method method, Object instance) {
		return new InvokableMethod<>(method, instance);
	}

	public Type getReturnType() {
//		return Invokable.from(method).getReturnType().resolveType(instance.getClass()).getType();
//		return TypeToken.of(method.getGenericReturnType()).method();
		return method.getGenericReturnType();
	}

	@SuppressWarnings("unchecked")
	public T invoke(Object... args) throws Throwable {
		return (T) method.invoke(instance, args);
	}
}
