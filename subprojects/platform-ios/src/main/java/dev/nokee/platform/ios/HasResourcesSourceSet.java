package dev.nokee.platform.ios;

import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.platform.base.ComponentSources;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;

import static org.gradle.util.ConfigureUtil.configureUsing;

/**
 * Represents a component sources that carries an iOS resource set named {@literal resources}.
 *
 * @see ComponentSources
 * @see IosResourceSet
 * @since 0.5
 */
public interface HasResourcesSourceSet {
	/**
	 * Returns an iOS resource set provider for the source set named {@literal resources}.
	 *
	 * @return a provider for {@literal resources} source set, never null
	 */
	default DomainObjectProvider<IosResourceSet> getResources() {
		return ((FunctionalSourceSet) this).get("resources", IosResourceSet.class);
	}

	/**
	 * Configures the iOS resource set named {@literal resources} using the specified configuration action.
	 *
	 * @param action  the configuration action, must not be null
	 * @see #getResources()
	 */
	default void resources(Action<? super IosResourceSet> action) {
		getResources().configure(action);
	}

	/**
	 * Configures the iOS resource set named {@literal resources} using the specified configuration closure.
	 *
	 * @param closure  the configuration action, must not be null
	 * @see #getResources()
	 */
	default void resources(@DelegatesTo(value = IosResourceSet.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		resources(configureUsing(closure));
	}
}
