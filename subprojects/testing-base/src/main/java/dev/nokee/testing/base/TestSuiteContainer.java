package dev.nokee.testing.base;

import dev.nokee.platform.base.DomainObjectProvider;
import dev.nokee.platform.base.KnownDomainObject;
import org.gradle.api.Action;

public interface TestSuiteContainer {
	<T extends TestSuiteComponent> DomainObjectProvider<T> register(String name, Class<T> type);
	<T extends TestSuiteComponent> DomainObjectProvider<T> register(String name, Class<T> type, Action<? super T> action);

	void configureEach(Action<? super TestSuiteComponent> action);
	<T extends TestSuiteComponent> void configureEach(Class<T> type, Action<? super T> action);

	void whenElementKnown(Action<KnownDomainObject<? extends TestSuiteComponent>> action);
	<T extends TestSuiteComponent> void whenElementKnown(Class<T> type, Action<KnownDomainObject<? extends T>> action);
}
