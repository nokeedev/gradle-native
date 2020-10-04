package dev.nokee.platform.base;

import dev.nokee.model.DomainObjectFactory;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.platform.base.internal.components.KnownComponent;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.util.ConfigureUtil;

public interface ComponentContainer {
	<T extends Component> DomainObjectProvider<T> register(String name, Class<T> type);
	<T extends Component> DomainObjectProvider<T> register(String name, Class<T> type, Action<? super T> action);
	default <T extends Component> DomainObjectProvider<T> register(String name, Class<T> type, @DelegatesTo(type = "T", strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		return register(name, type, ConfigureUtil.configureUsing(closure));
	}

	<U extends Component> void registerFactory(Class<U> type, DomainObjectFactory<? extends U> factory);

	void configureEach(Action<? super Component> action);
	default void configureEach(@DelegatesTo(value = Component.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		configureEach(ConfigureUtil.configureUsing(closure));
	}
	<T extends Component> void configureEach(Class<T> type, Action<? super T> action);

	void whenElementKnown(Action<? super KnownComponent<Component>> action);
	default void whenElementKnown(@DelegatesTo(value = KnownComponent.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		whenElementKnown(ConfigureUtil.configureUsing(closure));
	}
	<T extends Component> void whenElementKnown(Class<T> type, Action<? super KnownComponent<T>> action);
	default <T extends Component> void whenElementKnown(Class<T> type, @DelegatesTo(type = "T", strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		whenElementKnown(type, ConfigureUtil.configureUsing(closure));
	}
}
