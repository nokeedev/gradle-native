package dev.nokee.platform.nativebase;

import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.platform.base.ComponentSources;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;

import static org.gradle.util.ConfigureUtil.configureUsing;

/**
 * Represents a component sources that carries an native header set named {@literal headers}.
 *
 * @see ComponentSources
 * @see NativeHeaderSet
 * @since 0.5
 */
public interface HasHeadersSourceSet {
	/**
	 * Returns a native header set provider for the source set named {@literal headers}.
	 *
	 * @return a provider for {@literal headers} source set, never null
	 */
	default DomainObjectProvider<NativeHeaderSet> getHeaders() {
		return ((FunctionalSourceSet) this).get("headers", NativeHeaderSet.class);
	}

	/**
	 * Configures the native header set named {@literal headers} using the specified configuration action.
	 *
	 * @param action  the configuration action, must not be null
	 * @see #getHeaders()
	 */
	default void headers(Action<? super NativeHeaderSet> action) {
		getHeaders().configure(action);
	}

	/**
	 * Configures the native header set named {@literal headers} using the specified configuration closure.
	 *
	 * @param closure  the configuration action, must not be null
	 * @see #getHeaders()
	 */
	default void headers(@DelegatesTo(value = NativeHeaderSet.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		headers(configureUsing(closure));
	}
}
