package dev.nokee.model;

import org.gradle.api.Action;
import org.gradle.api.specs.Spec;

public interface DomainObjectContainer<T> {
	<U extends T> DomainObjectProvider<U> register(String name, Class<U> type);
	<U extends T> DomainObjectProvider<U> register(String name, Class<U> type, Action<? super U> action);

	<U extends T> void registerFactory(Class<U> type, DomainObjectFactory<? extends U> factory);
	<U extends T> void registerBinding(Class<U> type, Class<? extends U> implementationType);

	void configureEach(Action<? super T> action);

	<U extends T> void configureEach(Class<U> type, Action<? super U> action);

	void configureEach(Spec<? super T> spec, Action<? super T> action);
}
