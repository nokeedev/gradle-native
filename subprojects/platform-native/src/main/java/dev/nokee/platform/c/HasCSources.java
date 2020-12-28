package dev.nokee.platform.c;

import dev.nokee.language.c.CSourceSet;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;

import static dev.nokee.platform.base.internal.SourceAwareComponentUtils.sourceViewOf;
import static org.gradle.util.ConfigureUtil.configureUsing;

/**
 * Represents a component that carries C sources.
 *
 * @see HasCSourceSet
 * @since 0.5
 */
public interface HasCSources {
	/**
	 * Defines the C sources of this component.
	 *
	 * <p>By default, the source set contains all files in the directory {@code src/componentName/c}, where {@literal componentName} represent this component's name, i.e. {@literal main} or {@literal test}.
	 *
	 * @return a source set containing the C sources of this component, never null
	 * @see CSourceSet
	 */
	default CSourceSet getCSources() {
		return ((HasCSourceSet) sourceViewOf(this)).getC().get();
	}

	/**
	 * Configures the C sources of this component using the specified configuration action.
	 *
	 * @param action  the configuration action, must not be null
	 * @see #getCSources()
	 */
	default void cSources(Action<? super CSourceSet> action) {
		((HasCSourceSet) sourceViewOf(this)).getC().configure(action);
	}

	/**
	 * Configures the C sources of this component using the specified configuration closure.
	 *
	 * @param closure  the configuration closure, must not be null
	 * @see #getCSources()
	 */
	default void cSources(@DelegatesTo(value = CSourceSet.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		cSources(configureUsing(closure));
	}
}
