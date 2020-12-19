package dev.nokee.internal.reflect;

import org.gradle.api.reflect.ObjectInstantiationException;

public interface Instantiator {
	<T> T newInstance(Class<? extends T> type, Object... parameters) throws ObjectInstantiationException;
}
