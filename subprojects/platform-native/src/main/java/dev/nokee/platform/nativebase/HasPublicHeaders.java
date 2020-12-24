package dev.nokee.platform.nativebase;

import dev.nokee.language.nativebase.NativeHeaderSet;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;

import static dev.nokee.platform.base.internal.SourceAwareComponentUtils.sourceViewOf;
import static org.gradle.util.ConfigureUtil.configureUsing;

/**
 * Represents a component that carries public sources.
 *
 * @see HasPublicSourceSet
 * @since 0.5
 */
public interface HasPublicHeaders {
	/**
	 * Defines the public headers of this component.
	 *
	 * <p>By default, the source set contains all files in the directory {@code src/componentName/public}, where {@literal componentName} represent this component's name, i.e. {@literal main} or {@literal test}.
	 *
	 * @return a source set containing the public headers of this component, never null
	 * @see NativeHeaderSet
	 */
	default NativeHeaderSet getPublicHeaders() {
		return ((HasPublicSourceSet) sourceViewOf(this)).getPublic().get();
	}

	/**
	 * Configures the public headers of this component using the specified configuration action.
	 *
	 * @param action  the configuration action, must not be null
	 * @see #getPublicHeaders()
	 */
	default void publicHeaders(Action<? super NativeHeaderSet> action) {
		((HasPublicSourceSet) sourceViewOf(this)).getPublic().configure(action);
	}

	/**
	 * Configures the public headers of this component using the specified configuration closure.
	 *
	 * @param closure  the configuration closure, must not be null
	 * @see #getPublicHeaders()
	 */
	default void publicHeaders(@DelegatesTo(value = NativeHeaderSet.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		publicHeaders(configureUsing(closure));
	}
}
