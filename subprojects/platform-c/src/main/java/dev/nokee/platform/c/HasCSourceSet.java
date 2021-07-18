package dev.nokee.platform.c;

import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.c.CSourceSet;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.platform.base.ComponentSources;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;

import static org.gradle.util.ConfigureUtil.configureUsing;

/**
 * Represents a component sources that carries an C source set named {@literal c}.
 *
 * @see ComponentSources
 * @see CSourceSet
 * @since 0.5
 */
public interface HasCSourceSet {
	/**
	 * Returns a C source set provider for the source set named {@literal c}.
	 *
	 * @return a provider for {@literal c} source set, never null
	 */
	default DomainObjectProvider<CSourceSet> getC() {
		return ((FunctionalSourceSet) this).get("c", CSourceSet.class);
	}

	/**
	 * Configures the C source set named {@literal c} using the specified configuration action.
	 *
	 * @param action  the configuration action, must not be null
	 * @see #getC()
	 */
	default void c(Action<? super CSourceSet> action) {
		getC().configure(action);
	}

	/**
	 * Configures the C source set named {@literal c} using the specified configuration closure.
	 *
	 * @param closure  the configuration action, must not be null
	 * @see #getC()
	 */
	default void c(@DelegatesTo(value = CSourceSet.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		c(configureUsing(closure));
	}
}
