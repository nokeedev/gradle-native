package dev.nokee.platform.nativebase;

import dev.nokee.platform.base.ComponentDependencies;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.util.ConfigureUtil;

/**
 * Allows the API and implementation dependencies of a native library to be specified.
 *
 * @since 0.4
 */
@Deprecated
public interface NativeLibraryDependencies extends NativeComponentDependencies, ComponentDependencies {
    /**
     * Adds an API dependency to this library. An API dependency is made visible to consumers that are compiled against this component.
     *
     * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
     */
    void api(Object notation);

    /**
     * Adds an API dependency to this library. An API dependency is made visible to consumers that are compiled against this component.
     *
     * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
     * @param action The action to run to configure the dependency (project dependencies are {@link ProjectDependency} and external dependencies are {@link ExternalModuleDependency}).
     */
    void api(Object notation, Action<? super ModuleDependency> action);

	/**
	 * Adds an API dependency to this library. An API dependency is made visible to consumers that are compiled against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param closure The closure to run to configure the dependency (project dependencies are {@link ProjectDependency} and external dependencies are {@link ExternalModuleDependency}).
	 */
	default void api(Object notation, @DelegatesTo(value = ModuleDependency.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		api(notation, ConfigureUtil.configureUsing(closure));
	}
}
