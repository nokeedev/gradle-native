package dev.nokee.language.base;

import dev.nokee.model.DomainObjectView;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.util.ConfigureUtil;

/**
 * A view of the language source set that are created and configured as they are required.
 *
 * @param <T> type of the elements in this view
 */
public interface LanguageSourceSetView<T extends LanguageSourceSet> extends DomainObjectView<T> {
	/**
	 * Returns a language source set view containing the objects in this view of the given type.
	 * The returned collection is live, so that when matching objects are later added to this view, they are also visible in the filtered variant view.
	 *
	 * @param type The type of language source set to find.
	 * @param <S> The base type of the new language source set view.
	 * @return the matching element as a {@link LanguageSourceSetView}, never null.
	 */
	<S extends T> LanguageSourceSetView<S> withType(Class<S> type);

	/**
	 * Configures a language source set of the specified name.
	 *
	 * @param name The name of the language source set to configure.
	 * @param action The configuration action.
	 */
	void configure(String name, Action<? super T> action);

	/**
	 * Configures a language source set of the specified name.
	 *
	 * @param name The name of the language source set to configure.
	 * @param closure The configuration closure.
	 */
	default void configure(String name, @DelegatesTo(type = "T", strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		configure(name, ConfigureUtil.configureUsing(closure));
	}

	/**
	 * Configures a language source set of the specified name and type.
	 *
	 * @param name The name of the language source set to configure.
	 * @param type The type of language source set to configure.
	 * @param action The configuration action.
	 * @param <S> The type of the language source set to configure.
	 */
	<S extends T> void configure(String name, Class<S> type, Action<? super S> action);

	/**
	 * Configures a language source set of the specified name and type.
	 *
	 * @param name The name of the language source set to configure.
	 * @param type The type of language source set to configure.
	 * @param closure The configuration closure.
	 * @param <S> The type of the language source set to configure.
	 */
	default <S extends T> void configure(String name, Class<S> type, @DelegatesTo(type = "S", strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		configure(name, type, ConfigureUtil.configureUsing(closure));
	}
}
