package dev.nokee.testing.base;

import dev.nokee.model.DomainObjectFactory;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.platform.base.internal.components.KnownComponent;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.util.ConfigureUtil;

public interface TestSuiteContainer {
	<U extends TestSuiteComponent> void registerFactory(Class<U> type, DomainObjectFactory<? extends U> factory);
	<U extends TestSuiteComponent> void registerBinding(Class<U> type, final Class<? extends U> implementationType);

	<T extends TestSuiteComponent> DomainObjectProvider<T> register(String name, Class<T> type);
	<T extends TestSuiteComponent> DomainObjectProvider<T> register(String name, Class<T> type, Action<? super T> action);
	default <T extends TestSuiteComponent> DomainObjectProvider<T> register(String name, Class<T> type, @DelegatesTo(type = "T", strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		return register(name, type, ConfigureUtil.configureUsing(closure));
	}

	void configureEach(Action<? super TestSuiteComponent> action);
	default void configureEach(@DelegatesTo(value = TestSuiteComponent.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		configureEach(ConfigureUtil.configureUsing(closure));
	}
	<T extends TestSuiteComponent> void configureEach(Class<T> type, Action<? super T> action);

	void whenElementKnown(Action<? super KnownComponent<? extends TestSuiteComponent>> action);
	default void whenElementKnown(@DelegatesTo(value = KnownComponent.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		whenElementKnown(ConfigureUtil.configureUsing(closure));
	}
	<T extends TestSuiteComponent> void whenElementKnown(Class<T> type, Action<? super KnownComponent<? extends T>> action);
	default <T extends TestSuiteComponent> void whenElementKnown(Class<T> type, @DelegatesTo(type = "T", strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		whenElementKnown(type, ConfigureUtil.configureUsing(closure));
	}
}
