package dev.nokee.model.internal.registry;

import dev.nokee.model.internal.core.ModelNode;
import org.junit.platform.commons.util.ExceptionUtils;

import java.lang.reflect.InvocationTargetException;

public abstract class AbstractDomainObjectFunctorTester<F> {
	protected abstract <T> F createSubject(Class<T> type);
	protected abstract <T> F createSubject(Class<T> type, ModelNode node);

	protected final <R> R invoke(Object target, String method, Class[] parameterTypes, Object[] arguments) {
		try {
			return (R) target.getClass().getMethod(method, parameterTypes).invoke(target, arguments);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			return (R) ExceptionUtils.throwAsUncheckedException(e);
		}
	}

	static class MyType {
		private String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}
	static class MyOtherType {}
	interface WrongType {}
}
