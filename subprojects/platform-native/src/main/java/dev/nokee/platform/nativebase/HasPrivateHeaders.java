package dev.nokee.platform.nativebase;

import dev.nokee.language.nativebase.NativeHeaderSet;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;

import static dev.nokee.platform.base.internal.SourceAwareComponentUtils.sourceViewOf;
import static org.gradle.util.ConfigureUtil.configureUsing;

/**
 * Represents a component that carries private headers.
 *
 * @see HasHeadersSourceSet
 * @since 0.5
 */
public interface HasPrivateHeaders {
	/**
	 * Defines the private headers of this component.
	 *
	 * <p>By default, the source set contains all files in the directory {@code src/componentName/headers}, where {@literal componentName} represent this component's name, i.e. {@literal main} or {@literal test}.
	 *
	 * @return a source set containing the private headers of this component, never null
	 * @see NativeHeaderSet
	 */
	default NativeHeaderSet getPrivateHeaders() {
		return ((HasHeadersSourceSet) sourceViewOf(this)).getHeaders().get();
	}

	/**
	 * Configures the private headers of this component using the specified configuration action.
	 *
	 * @param action  the configuration action, must not be null
	 * @see #getPrivateHeaders()
	 */
	default void privateHeaders(Action<? super NativeHeaderSet> action) {
		((HasHeadersSourceSet) sourceViewOf(this)).getHeaders().configure(action);
	}

	/**
	 * Configures the private headers of this component using the specified configuration closure.
	 *
	 * @param closure  the configuration closure, must not be null
	 * @see #getPrivateHeaders()
	 */
	default void privateHeaders(@DelegatesTo(value = NativeHeaderSet.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		privateHeaders(configureUsing(closure));
	}
}
