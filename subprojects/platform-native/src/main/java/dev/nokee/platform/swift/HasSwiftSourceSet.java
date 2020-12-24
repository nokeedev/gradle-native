package dev.nokee.platform.swift;

import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.platform.base.ComponentSources;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;

import static org.gradle.util.ConfigureUtil.configureUsing;

/**
 * Represents a component sources that carries an Swift source set named {@literal swift}.
 *
 * @see ComponentSources
 * @see SwiftSourceSet
 * @since 0.5
 */
public interface HasSwiftSourceSet {
	/**
	 * Returns a Swift source set provider for the source set named {@literal swift}.
	 *
	 * @return a provider for {@literal swift} source set, never null
	 */
	default DomainObjectProvider<SwiftSourceSet> getSwift() {
		return ((FunctionalSourceSet) this).get("swift", SwiftSourceSet.class);
	}

	/**
	 * Configures the Swift source set named {@literal swift} using the specified configuration action.
	 *
	 * @param action  the configuration action, must not be null
	 * @see #getSwift()
	 */
	default void swift(Action<? super SwiftSourceSet> action) {
		getSwift().configure(action);
	}

	/**
	 * Configures the Swift source set named {@literal swift} using the specified configuration closure.
	 *
	 * @param closure  the configuration action, must not be null
	 * @see #getSwift()
	 */
	default void swift(@DelegatesTo(value = SwiftSourceSet.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		swift(configureUsing(closure));
	}
}
