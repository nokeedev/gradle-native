package dev.nokee.model;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.util.ConfigureUtil;

public interface DomainObjectContainer<T> extends NamedDomainObjectView<T> {
	<U extends T> DomainObjectProvider<U> register(String name, Class<U> type);

	<U extends T> DomainObjectProvider<U> register(String name, Class<U> type, Action<? super U> action);

	default <U extends T> DomainObjectProvider<U> register(String name, Class<U> type, @DelegatesTo(type = "U", strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		return register(name, type, ConfigureUtil.configureUsing(closure));
	}

	<U extends T> void registerFactory(Class<U> type, DomainObjectFactory<? extends U> factory);

	<U extends T> void registerBinding(Class<U> type, Class<? extends U> implementationType);
}
