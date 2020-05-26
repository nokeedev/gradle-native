package dev.nokee.platform.nativebase;

import dev.nokee.platform.base.ComponentDependencies;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;

/**
 * Allows the API and implementation dependencies of a native library to be specified.
 *
 * @since 0.4
 */
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
}
