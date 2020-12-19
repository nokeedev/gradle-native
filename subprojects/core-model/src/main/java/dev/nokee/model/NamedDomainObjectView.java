package dev.nokee.model;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.util.ConfigureUtil;

public interface NamedDomainObjectView<T> extends DomainObjectView<T> {
	/**
	 * Configures an element of the specified name.
	 *
	 * @param name  the name of the element to configure.
	 * @param action  the configuration action.
	 */
	void configure(String name, Action<? super T> action);

	/**
	 * Configures an element of the specified name.
	 *
	 * @param name  the name of the element to configure.
	 * @param closure  the configuration closure.
	 */
	default void configure(String name, @DelegatesTo(type = "T", strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		configure(name, ConfigureUtil.configureUsing(closure));
	}

	/**
	 * Configures an element of the specified name and type.
	 *
	 * @param name  the name of the element to configure.
	 * @param type  the type of element to configure.
	 * @param action  the configuration action.
	 * @param <S>  the type of the element to configure.
	 */
	<S extends T> void configure(String name, Class<S> type, Action<? super S> action);

	/**
	 * Configures an element of the specified name and type.
	 *
	 * @param name The name of the element to configure.
	 * @param type The type of element to configure.
	 * @param closure The configuration closure.
	 * @param <S> The type of the element to configure.
	 */
	default <S extends T> void configure(String name, Class<S> type, @DelegatesTo(type = "S", strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		configure(name, type, ConfigureUtil.configureUsing(closure));
	}

	/**
	 * Returns an element provider for the specified name.
	 *
	 * @param name  the name of the element
	 * @return a provider for the specified name.
	 */
	DomainObjectProvider<T> get(String name);

	/**
	 * Returns an element provider for the specified name and type.
	 *
	 * @param name  the name of the element
	 * @param type  the type of the element
	 * @param <S>  the type of the element
	 * @return a provider for the specified name and name.
	 */
	<S extends T> DomainObjectProvider<S> get(String name, Class<S> type);
}
