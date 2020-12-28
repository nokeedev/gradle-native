package dev.nokee.platform.ios;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;

import static dev.nokee.platform.base.internal.SourceAwareComponentUtils.sourceViewOf;
import static org.gradle.util.ConfigureUtil.configureUsing;

/**
 * Represents a component that carries iOS resources.
 *
 * @see HasResourcesSourceSet
 * @since 0.5
 */
public interface HasIosResources {
	/**
	 * Defines the iOS resources of this component.
	 *
	 * <p>By default, the source set contains all files in the directory {@code src/componentName/resources}, where {@literal componentName} represent this component's name, i.e. {@literal main} or {@literal test}.
	 *
	 * @return a source set containing the iOS resources of this component, never null
	 * @see IosResourceSet
	 */
	default IosResourceSet getResources() {
		return ((HasResourcesSourceSet) sourceViewOf(this)).getResources().get();
	}

	/**
	 * Configures the iOS resources of this component using the specified configuration action.
	 *
	 * @param action  the configuration action, must not be null
	 * @see #getResources()
	 */
	default void resources(Action<? super IosResourceSet> action) {
		((HasResourcesSourceSet) sourceViewOf(this)).getResources().configure(action);
	}

	/**
	 * Configures the iOS resources of this component using the specified configuration closure.
	 *
	 * @param closure  the configuration closure, must not be null
	 * @see #getResources()
	 */
	default void resources(@DelegatesTo(value = IosResourceSet.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		resources(configureUsing(closure));
	}
}
