package dev.nokee.platform.nativebase.internal;

import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.reflect.Method;

/**
 * The decorating factory act as an extension to AutoFactory by using an custom instantiator instead of using the classic new keyword.
 * The goal is to use Gradle's ObjectFactory with Dagger while having compile time checks by generating static factories.
 * This is a temporary solution until 1) we get rid of the ObjectFactory requirement for decoration or 2) a solution is provided to https://github.com/google/auto/issues/872.
 */
public class DecoratingFactory {
	@Inject
	protected Provider<ObjectFactory> objectFactoryProvider;

	private <T> Class<T> getType() {
		for (Method method : this.getClass().getMethods()) {
			if (method.getName().equals("create")) {
				return (Class<T>)method.getReturnType();
			}
		}
		throw new IllegalStateException();
	}

	protected final <T> T newInstance(Object... args) {
		return objectFactoryProvider.get().newInstance(getType(), args);
	}
}
