package dev.nokee.testing.base;

import dev.nokee.platform.base.DomainObjectProvider;
import dev.nokee.platform.base.KnownDomainObject;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.util.ConfigureUtil;

public interface TestSuiteContainer {
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

	void whenElementKnown(Action<KnownDomainObject<? extends TestSuiteComponent>> action);
	default void whenElementKnown(@DelegatesTo(value = KnownDomainObject.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		whenElementKnown(ConfigureUtil.configureUsing(closure));
	}
	<T extends TestSuiteComponent> void whenElementKnown(Class<T> type, Action<KnownDomainObject<? extends T>> action);
	default <T extends TestSuiteComponent> void whenElementKnown(Class<T> type, @DelegatesTo(type = "T", strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		whenElementKnown(type, ConfigureUtil.configureUsing(closure));
	}
}
